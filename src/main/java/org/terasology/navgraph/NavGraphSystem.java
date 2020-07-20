/*
 * Copyright 2018 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.navgraph;

import org.terasology.biomesAPI.Biome;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.ChunkMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.utilities.concurrency.Task;
import org.terasology.utilities.concurrency.TaskMaster;
import org.terasology.world.WorldChangeListener;
import org.terasology.world.WorldComponent;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.event.OnChunkLoaded;

import java.util.HashMap;
import java.util.Map;

import static org.joml.Math.round;

@RegisterSystem
@Share(value = NavGraphSystem.class)
public class NavGraphSystem extends BaseComponentSystem implements UpdateSubscriberSystem, WorldChangeListener {
    private static final float EVENT_COOLDOWN = 0.4f;

    @In
    private WorldProvider world;
    @In
    private EntityManager entityManager;

    private Map<Vector3i, NavGraphChunk> heightMaps = new HashMap<>();
    private TaskMaster<NavGraphTask> taskMaster = TaskMaster.createPriorityTaskMaster("Pathfinder", 1, 1024);
    private boolean dirty;
    private float coolDown = EVENT_COOLDOWN;
    private int chunkUpdates;

    private Map<Vector3i, NavGraphChunk> maps = new HashMap<>();

    @Override
    public void initialise() {
        world.registerListener(this);
    }

    @Override
    public void shutdown() {
        taskMaster.shutdown(new ShutdownTask(), false);
    }

    @Override
    public void update(float delta) {
        if (dirty) {
            coolDown -= delta;
            if (coolDown < 0) {
                coolDown = EVENT_COOLDOWN;
                dirty = false;
                world.getWorldEntity().send(new NavGraphChanged());
            }
        }
    }

    @Override
    public void onBlockChanged(Vector3i pos, Block newBlock, Block originalBlock) {
        Vector3i chunkPos = ChunkMath.calcChunkPos(pos);
        taskMaster.offer(new UpdateChunkTask(chunkPos));
    }

    @ReceiveEvent(components = WorldComponent.class)
    public void chunkReady(OnChunkLoaded event, EntityRef worldEntity) {
        taskMaster.offer(new UpdateChunkTask(event.getChunkPos()));
    }

    public WalkableBlock getBlock(Vector3i pos) {
        Vector3i chunkPos = ChunkMath.calcChunkPos(pos);
        NavGraphChunk navGraphChunk = heightMaps.get(chunkPos);
        if (navGraphChunk != null) {
            return navGraphChunk.getBlock(pos.x, pos.y, pos.z);
        } else {
            return null;
        }
    }

    public WalkableBlock getBlock(EntityRef minion) {
        Vector3f pos = minion.getComponent(LocationComponent.class).getWorldPosition();
        return getBlock(pos);
    }

    public WalkableBlock getBlock(Vector3f pos) {
        Vector3i blockPos = new Vector3i(round(pos.x), round(pos.y), round(pos.z));
        blockPos.y += 2; //Added in case the height of minion is really low
        WalkableBlock block = getBlock(blockPos);
        if (block == null) {
            while (blockPos.y >= (int) pos.y - 4 && block == null) {
                if (block == null) {
                    block = getBlock(blockPos);
                }

                // Checking Neighbours as minion could be hanging on the edge of a block

                for (int i = 0; i < 8; i++) {
                    int dx = NavGraphChunk.DIRECTIONS[i][0];
                    int dz = NavGraphChunk.DIRECTIONS[i][1];
                    Vector3i directionVector = new Vector3i(dx, 0, dz);
                    directionVector.add(blockPos);
                    block = getBlock(directionVector);
                    if (block != null) {
                        break;
                    }
                }


            }
        }


        return block;
    }

    public void offer(NavGraphTask task) {
        taskMaster.offer(task);
    }

    public int getChunkUpdates() {
        return chunkUpdates;
    }

    public NavGraphChunk updateChunk(Vector3i chunkPos) {
        if (chunkPos == null) {
            return null;
        }
        NavGraphChunk navGraphChunk = heightMaps.remove(chunkPos);
        if (navGraphChunk != null) {
            navGraphChunk.disconnectNeighborMaps(getNeighbor(chunkPos, -1, 0), getNeighbor(chunkPos, 0, -1),
                    getNeighbor(chunkPos, 1, 0), getNeighbor(chunkPos, 0, 1));
            navGraphChunk.cells = null;
        }
        navGraphChunk = new NavGraphChunk(world, chunkPos);
        navGraphChunk.update();
        heightMaps.put(chunkPos, navGraphChunk);
        navGraphChunk.connectNeighborMaps(getNeighbor(chunkPos, -1, 0), getNeighbor(chunkPos, 0, -1),
                getNeighbor(chunkPos, 1, 0), getNeighbor(chunkPos, 0, 1));
        return navGraphChunk;
    }

    private NavGraphChunk getNeighbor(Vector3i chunkPos, int x, int z) {
        Vector3i neighborPos = new Vector3i(chunkPos);
        neighborPos.add(x, 0, z);
        return heightMaps.get(neighborPos);
    }

    @Override
    public void onExtraDataChanged(int i, Vector3i pos, int newData, int oldData) {

    }

    public interface NavGraphTask extends Task, Comparable<NavGraphTask> {
        int getPriority();
    }

    public static class ShutdownTask implements NavGraphTask {
        @Override
        public int getPriority() {
            return -1;
        }

        @Override
        public int compareTo(NavGraphTask o) {
            return Integer.compare(this.getPriority(), o.getPriority());
        }

        @Override
        public String getName() {
            return "Pathfinder:UpdateChunk";
        }

        @Override
        public void run() {

        }

        @Override
        public boolean isTerminateSignal() {
            return true;
        }
    }

    /**
     * Task to update a chunk
     * <p/>
     * Note: this class has a natural ordering that is inconsistent with equals.
     */
    private final class UpdateChunkTask implements NavGraphTask {
        public Vector3i chunkPos;

        private UpdateChunkTask(Vector3i chunkPos) {
            this.chunkPos = chunkPos;
        }

        @Override
        public String getName() {
            return "Pathfinder:UpdateChunk";
        }

        @Override
        public void run() {
            chunkUpdates++;
            maps.remove(chunkPos);
            NavGraphChunk map = updateChunk(chunkPos);
            maps.put(chunkPos, map);
            dirty = true;
        }

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public boolean isTerminateSignal() {
            return false;
        }

        @Override
        public int compareTo(NavGraphTask o) {
            return Integer.compare(this.getPriority(), o.getPriority());
        }
    }
}
