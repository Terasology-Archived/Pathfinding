// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.pathfinding.navgraph;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.math.ChunkMath;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.utilities.concurrency.Task;
import org.terasology.engine.utilities.concurrency.TaskMaster;
import org.terasology.engine.world.WorldChangeListener;
import org.terasology.engine.world.WorldComponent;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.chunks.event.OnChunkLoaded;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;

import java.util.HashMap;
import java.util.Map;

@RegisterSystem
@Share(value = NavGraphSystem.class)
public class NavGraphSystem extends BaseComponentSystem implements UpdateSubscriberSystem, WorldChangeListener {
    private static final float EVENT_COOLDOWN = 0.4f;
    private final Map<Vector3i, NavGraphChunk> heightMaps = new HashMap<>();
    private final TaskMaster<NavGraphTask> taskMaster = TaskMaster.createPriorityTaskMaster("Pathfinder", 1, 1024);
    private final Map<Vector3i, NavGraphChunk> maps = new HashMap<>();
    @In
    private WorldProvider world;
    @In
    private EntityManager entityManager;
    private boolean dirty;
    private float coolDown = EVENT_COOLDOWN;
    private int chunkUpdates;

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
        Vector3i blockPos = new Vector3i(pos.x + 0.25f, pos.y, pos.z + 0.25f);
        WalkableBlock block = getBlock(blockPos);
        if (block == null) {
            while (blockPos.y >= (int) pos.y - 4 && block == null) {
                blockPos.y--;
                block = getBlock(blockPos);
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
