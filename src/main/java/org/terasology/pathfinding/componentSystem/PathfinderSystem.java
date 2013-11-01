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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.pathfinding.model.HeightMap;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.Pathfinder;
import org.terasology.pathfinding.model.WalkableBlock;
import org.terasology.world.WorldChangeListener;
import org.terasology.world.WorldComponent;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.event.OnChunkLoaded;

import javax.vecmath.Vector3f;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This systems helps finding a paths through the game world.
 * <p/>
 * Since paths finding takes some time, it completely runs in a background thread. So, a requested paths is not
 * available in the moment it is requested. Instead you need to listen for a PathReadyEvent.
 *
 * @author synopia
 */
@RegisterSystem
public class PathfinderSystem implements ComponentSystem, WorldChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(PathfinderSystem.class);

    @In
    private WorldProvider world;

    private BlockingQueue<UpdateChunkTask> updateChunkQueue = new ArrayBlockingQueue<>(100);
    private BlockingQueue<FindPathTask> findPathTasks = new ArrayBlockingQueue<>(1000);
    private Set<Vector3i> invalidChunks = Collections.synchronizedSet(new HashSet<Vector3i>());

    private ExecutorService inputThreads;

    private Map<Vector3i, HeightMap> maps = new HashMap<>();
    private Pathfinder pathfinder;
    private int nextId;

    public PathfinderSystem() {
        CoreRegistry.put(PathfinderSystem.class, this);
    }

    public int requestPath(EntityRef requestor, Vector3f target, Vector3f... newStarts) {
        Vector3i[] starts = new Vector3i[newStarts.length];
        WalkableBlock block;
        for (int i = 0; i < newStarts.length; i++) {
            block = getBlock(newStarts[i]);
            if (block != null) {
                starts[i] = block.getBlockPosition();
            }
        }
        block = getBlock(target);
        if (block != null) {
            return requestPath(requestor, block.getBlockPosition(), starts);
        }
        return -1;
    }

    public int requestPath(EntityRef requestor, Vector3i target, Vector3i... start) {
        FindPathTask task = new FindPathTask(start, target, requestor);
        findPathTasks.add(task);
        return task.pathId;
    }

    public HeightMap getHeightMap(Vector3i chunkPos) {
        return maps.get(chunkPos);
    }

    public WalkableBlock getBlock(Vector3i pos) {
        return pathfinder.getBlock(pos);
    }

    public WalkableBlock getBlock(Vector3f pos) {
        Vector3i blockPos = new Vector3i(pos.x + 0.25f, pos.y, pos.z + 0.25f);

        WalkableBlock block = pathfinder.getBlock(blockPos);
        if (block == null) {
            blockPos.y += 1;
            while (blockPos.y >= (int) pos.y - 1 && (block = pathfinder.getBlock(blockPos)) == null) {
                blockPos.y--;
            }
        }
        return block;
    }

    @Override
    public void initialise() {
        world.registerListener(this);
        pathfinder = new Pathfinder(world);
        logger.info("Pathfinder started");

        inputThreads = Executors.newFixedThreadPool(1);
        inputThreads.execute(new Runnable() {
            @Override
            public void run() {

                boolean running = true;
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                while (running) {
                    try {
                        UpdateChunkTask task = updateChunkQueue.poll(1, TimeUnit.SECONDS);
                        if (task != null) {
                            task.process();
                        } else {
                            findPaths();
                        }
                    } catch (InterruptedException e) {
                        logger.error("Thread interrupted", e);
                    } catch (Exception e) {
                        logger.error("Error in thread", e);
                    }
                }
                logger.debug("Thread shutdown safely");
            }
        });
    }

    private void findPaths() {
        long time = System.nanoTime();
        int count = 0;
        while (!findPathTasks.isEmpty()) {
            FindPathTask pathTask = findPathTasks.poll();
            if (pathTask.processed) {
                continue;
            }
            pathTask.process();
            count++;
        }
        float ms = (System.nanoTime() - time) / 1000 / 1000f;
        if (count > 0) {
            logger.info("Searching " + count + " paths took " + ms + " ms");
        }
    }

    @Override
    public void shutdown() {
        inputThreads.shutdown();
    }

    @Override
    public void onBlockChanged(Vector3i pos, Block newBlock, Block originalBlock) {
        Vector3i chunkPos = TeraMath.calcChunkPos(pos);
        invalidChunks.add(chunkPos);
        updateChunkQueue.offer(new UpdateChunkTask(chunkPos));
        for (FindPathTask task : findPathTasks) {
            task.cancel();
        }
        findPathTasks.clear();
    }

    @ReceiveEvent(components = WorldComponent.class)
    public void chunkReady(OnChunkLoaded event, EntityRef worldEntity) {
        invalidChunks.add(event.getChunkPos());
        updateChunkQueue.offer(new UpdateChunkTask(event.getChunkPos()));
    }

    /**
     * Task to update a chunk
     */
    private final class UpdateChunkTask {
        public Vector3i chunkPos;

        private UpdateChunkTask(Vector3i chunkPos) {
            this.chunkPos = chunkPos;
        }

        public void process() {
            maps.remove(chunkPos);
            HeightMap map = pathfinder.update(chunkPos);
            maps.put(chunkPos, map);
            pathfinder.clearCache();
        }
    }

    /**
     * Task to find a paths.
     */
    private final class FindPathTask {
        public EntityRef entity;
        public Path[] paths;
        public Vector3i[] start;
        public Vector3i target;
        public boolean processed;
        public int pathId;

        private FindPathTask(Vector3i[] start, Vector3i target, EntityRef entity) {
            this.start = start;
            this.target = target;
            this.entity = entity;
            this.pathId = nextId;
            nextId++;
        }

        /**
         * Does the actual paths finding. When its done, the outputQueue is filled with the result.
         * This method should be called from a thread only, it may take long.
         */
        public void process() {
            WalkableBlock[] startBlocks = new WalkableBlock[start.length];
            int startCount = 0;
            for (int i = 0; i < start.length; i++) {
                if (this.start[i] != null) {
                    startBlocks[i] = pathfinder.getBlock(this.start[i]);
                    startCount++;
                }
            }
            WalkableBlock targetBlock = pathfinder.getBlock(this.target);
            paths = null;
            if (targetBlock != null && startCount > 0) {
                paths = pathfinder.findPath(targetBlock, startBlocks);
            }
            processed = true;
            entity.send(new PathReadyEvent(pathId, paths, targetBlock, startBlocks));
        }

        public void cancel() {
            entity.send(new PathReadyEvent(pathId, null, null, null));
        }
    }
}
