/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.pathfinding.componentSystem;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.pathfinding.model.HeightMap;
import org.terasology.pathfinding.model.LineOfSight3d;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.Pathfinder;
import org.terasology.pathfinding.model.PathfinderWorld;
import org.terasology.pathfinding.model.WalkableBlock;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.utilities.concurrency.Task;
import org.terasology.utilities.concurrency.TaskMaster;
import org.terasology.world.WorldChangeListener;
import org.terasology.world.WorldComponent;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.event.OnChunkLoaded;

import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This systems helps finding a paths through the game world.
 * <p/>
 * Since paths finding takes some time, it completely runs in a background thread. So, a requested paths is not
 * available in the moment it is requested. Instead you need to listen for a PathReadyEvent.
 * <p/>
 * Here we also listen for world changes (OnChunkReady and OnBlockChanged). Currently, both events reset the
 * pathfinder (clear path cache) and rebuild the modified chunk.
 * </p>
 * Chunk updates are processed before any pathfinding request. However, this system does not inform about
 * paths getting invalid.
 *
 * @author synopia
 */
@RegisterSystem
public class PathfinderSystem implements ComponentSystem, WorldChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(PathfinderSystem.class);

    @In
    private WorldProvider world;

    private TaskMaster<PathfinderTask> taskMaster = TaskMaster.createPriorityTaskMaster("Pathfinder", 1, 1024);

    private int pathsSearched;
    private int chunkUpdates;
    private Map<Vector3i, HeightMap> maps = new HashMap<>();
    private PathfinderWorld pathfinderWorld;
    private Pathfinder pathfinder;
    private int nextId;

    public PathfinderSystem() {
        CoreRegistry.put(PathfinderSystem.class, this);
    }

    public int requestPath(EntityRef requestor, Vector3i target, List<Vector3i> start) {
        FindPathTask task = new FindPathTask(start, target, requestor);
        taskMaster.offer(task);

        return task.pathId;
    }

    public HeightMap getHeightMap(Vector3i chunkPos) {
        return maps.get(chunkPos);
    }

    public WalkableBlock getBlock(Vector3i pos) {
        return pathfinderWorld.getBlock(pos);
    }

    public WalkableBlock getBlock(EntityRef minion) {
        Vector3f pos = minion.getComponent(LocationComponent.class).getWorldPosition();
        return getBlock(pos);
    }

    public WalkableBlock getBlock(Vector3f pos) {
        Vector3i blockPos = new Vector3i(pos.x + 0.25f, pos.y, pos.z + 0.25f);
        WalkableBlock block = pathfinderWorld.getBlock(blockPos);
        if (block == null) {
            blockPos.y += 2;
            while (blockPos.y >= (int) pos.y - 4 && (block = pathfinderWorld.getBlock(blockPos)) == null) {
                blockPos.y--;
            }
        }
        return block;
    }

    @Override
    public void initialise() {
        world.registerListener(this);
        pathfinderWorld = createWorld();
        pathfinder = createPathfinder();
        logger.info("Pathfinder started");
    }

    protected Pathfinder createPathfinder() {
        return new Pathfinder(pathfinderWorld, new LineOfSight3d(world));
    }

    protected PathfinderWorld createWorld() {
         return new PathfinderWorld(world);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void onBlockChanged(Vector3i pos, Block newBlock, Block originalBlock) {
        Vector3i chunkPos = TeraMath.calcChunkPos(pos);
        taskMaster.offer(new UpdateChunkTask(chunkPos));
    }

    @ReceiveEvent(components = WorldComponent.class)
    public void chunkReady(OnChunkLoaded event, EntityRef worldEntity) {
        taskMaster.offer(new UpdateChunkTask(event.getChunkPos()));
    }

    public int getPathsSearched() {
        return pathsSearched;
    }

    public int getChunkUpdates() {
        return chunkUpdates;
    }

    private abstract class PathfinderTask implements Task, Comparable<PathfinderTask> {
        @Override
        public boolean isTerminateSignal() {
            return false;
        }
    }

    /**
     * Task to update a chunk
     */
    private final class UpdateChunkTask extends PathfinderTask {
        public Vector3i chunkPos;

        private UpdateChunkTask(Vector3i chunkPos) {
            this.chunkPos = chunkPos;
        }

        @Override
        public String getName() {
            return "Pathfinder:UpdateChunk";
        }

        @Override
        public void enact() {
            chunkUpdates++;
            maps.remove(chunkPos);
            HeightMap map = pathfinderWorld.update(chunkPos);
            maps.put(chunkPos, map);
            pathfinder.clearCache();
        }

        @Override
        public int compareTo(PathfinderTask o) {
            if (o instanceof FindPathTask) {
                return -1;
            }
            return 0;
        }
    }

    /**
     * Task to find a path.
     */
    private final class FindPathTask extends PathfinderTask {
        public EntityRef entity;
        public List<Path> paths;
        public List<Vector3i> start;
        public Vector3i target;
        public boolean processed;
        public int pathId;

        private FindPathTask(List<Vector3i> start, Vector3i target, EntityRef entity) {
            this.start = start;
            this.target = target;
            this.entity = entity;
            this.pathId = nextId;
            nextId++;
        }

        @Override
        public String getName() {
            return "Pathfinder:FindPath";
        }

        @Override
        public void enact() {
            pathsSearched++;
            List<WalkableBlock> startBlocks = Lists.newArrayList();
            for (Vector3i pos : start) {
                if (pos != null) {
                    startBlocks.add(pathfinderWorld.getBlock(pos));
                }
            }
            WalkableBlock targetBlock = pathfinderWorld.getBlock(this.target);
            paths = null;
            if (targetBlock != null && startBlocks.size() > 0) {
                paths = pathfinder.findPath(targetBlock, startBlocks);
            }
            processed = true;
            entity.send(new PathReadyEvent(pathId, paths, targetBlock, startBlocks));
        }

        @Override
        public int compareTo(PathfinderTask o) {
            if (o instanceof UpdateChunkTask) {
                return 1;
            }
            if (o instanceof FindPathTask) {
                FindPathTask find = (FindPathTask) o;
                return Integer.compare(pathId, find.pathId);
            }
            return 0;
        }
    }
}
