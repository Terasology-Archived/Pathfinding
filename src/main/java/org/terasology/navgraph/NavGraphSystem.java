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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static org.joml.Math.ceil;
import static org.joml.Math.floor;
import static org.joml.Math.round;

@RegisterSystem
@Share(value = NavGraphSystem.class)
public class NavGraphSystem extends BaseComponentSystem implements UpdateSubscriberSystem, WorldChangeListener {
    private static final float EVENT_COOLDOWN = 0.4f;

    Logger logger = LoggerFactory.getLogger(NavGraphSystem.class);

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

    /**
     * Custom comparator that sorts the nearest blocks based on how close they are to the
     * location of the entity
     */
    static class SurroundingBlockComparator implements Comparator<Vector3f> {
        //Stores the point for which we are finding the closest blocks
        Vector3f pos;

        public SurroundingBlockComparator(Vector3f pos) {
            this.pos = pos;

        }

        @Override
        public int compare(Vector3f o1, Vector3f o2) {
            //Comparing distances to find out which should come before
            if (o1.distanceSquared(pos) < o2.distanceSquared(pos)) {
                return -1;
            } else {
                return 1;
            }

        }
    }

    public WalkableBlock getBlock(Vector3f pos) {

        //Stores closest block
        Vector3i blockPos = new Vector3i(round(pos.x), round(pos.y), round(pos.z));

        //computing all the possible closest blocks
        int floorValueX = (int) floor(pos.x);
        int floorValueZ = (int) floor(pos.z);
        int ceilValueX = (int) ceil(pos.x);
        int ceilValueZ = (int) ceil(pos.z);

        WalkableBlock block;

        //ArrayList to store all the possible blockPositions around pos
        ArrayList<Vector3f> blockPosList = new ArrayList<>();
        blockPosList.add(new Vector3f(floorValueX, blockPos.y, floorValueZ));
        blockPosList.add(new Vector3f(ceilValueX, blockPos.y, floorValueZ));
        blockPosList.add(new Vector3f(floorValueX, blockPos.y, ceilValueZ));
        blockPosList.add(new Vector3f(ceilValueX, blockPos.y, ceilValueZ));

        //Sorting the ArrayList based on the custom comparator defined above
        Collections.sort(blockPosList, new SurroundingBlockComparator(pos));


        while (blockPos.y >= (int) pos.y - 4) {

            for (Vector3f blockPosition : blockPosList) {

                //iterating through the block positions based on proximity to pos

                block = getBlock(new Vector3i(round(blockPosition.x), blockPos.y, round(blockPosition.z)));
                if (block != null) {
                    return block;
                }

            }

            blockPos.y--;

        }

        return null;
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
