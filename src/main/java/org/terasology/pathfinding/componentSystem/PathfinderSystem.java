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

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.pathfinding.model.HeightMap;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.Pathfinder;
import org.terasology.pathfinding.model.WalkableBlock;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.chunks.remoteChunkProvider.ChunkReadyListener;
import org.terasology.world.propagation.BlockChange;

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
 * This systems helps finding a path through the game world.
 * <p/>
 * Since path finding takes some time, it completely runs in a background thread. So, a requested path is not
 * available in the moment it is requested. Instead you need to use a callback interface (PathRequest) which methods
 * gets called on the normal game update thread, once the path is ready.
 * <p/>
 * In addition, paths may change or even get invalid. In such cases the callback interface is called too.
 *
 * @author synopia
 */
@RegisterSystem
public class PathfinderSystem implements ChunkReadyListener, UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(PathfinderSystem.class);

    /**
     * Callback interface for path requests
     */
    public interface PathRequest {
        /**
         * Is called when a requested path is available. This code runs in the game update thread (this systems update
         * method).
         *
         * @param path the found path. null if path cannot be requested, since world changes too much. Path.INVALID
         *             if no path can be found between start and target.
         */
        void onPathReady(Path path);

        /**
         * Is called once this requested path gets invalid. Invalid paths are processed in background. Once its
         * available again, onPathReady is called.
         */
        void invalidate();
    }

    /**
     * Task to update a chunk
     */
    private class UpdateChunkTask {
        public Vector3i chunkPos;

        private UpdateChunkTask(Vector3i chunkPos) {
            this.chunkPos = chunkPos;
        }

        public void process() {
            maps.remove(chunkPos);
            HeightMap map = pathfinder.update(chunkPos);
            maps.put(chunkPos, map);
        }
    }

    /**
     * Task to find a path. Tasks live until they get killed using kill(). As long as a task lives, it will inform
     * its associated callback, when path is available or gets invalid.
     */
    public class FindPathTask {
        public Path path;
        public PathRequest pathRequest;
        public Vector3i start;
        public Vector3i target;
        public boolean processed;

        private FindPathTask(Vector3i start, Vector3i target, PathRequest pathRequest) {
            this.pathRequest = pathRequest;
            this.start = start;
            this.target = target;
        }

        /**
         * Does the actual path finding. When its done, the outputQueue is filled with the result.
         * This method should be called from a thread only, it may take long.
         */
        public void process() {
            WalkableBlock startBlock = pathfinder.getBlock(this.start);
            WalkableBlock targetBlock = pathfinder.getBlock(this.target);
            path = null;
            if (start != null && target != null) {
                path = pathfinder.findPath(targetBlock, startBlock);
            }
            processed = true;
            outputQueue.offer(this);
        }

        /**
         * Kills this task. Stops updating this path request.
         */
        public void kill() {
            taskMap.remove(this);
            pathRequest = null;
            path = null;
        }

        /**
         * Invalidates the path request, after the world or at least a chunk part of this path has changed.
         */
        public void invalidate() {
            processed = false;
            pathRequest.invalidate();
        }
    }

    private BlockingQueue<UpdateChunkTask> updateChunkQueue = new ArrayBlockingQueue<UpdateChunkTask>(100);
    private BlockingQueue<FindPathTask> outputQueue = new ArrayBlockingQueue<FindPathTask>(100);
    private Set<FindPathTask> taskMap = Collections.synchronizedSet(new HashSet<FindPathTask>());
    private Set<Vector3i> invalidChunks = Collections.synchronizedSet(new HashSet<Vector3i>());

    private ExecutorService inputThreads;

    private Map<Vector3i, HeightMap> maps = new HashMap<Vector3i, HeightMap>();
    private Pathfinder pathfinder;
    @In
    private WorldProvider world;

    public PathfinderSystem() {
        CoreRegistry.put(PathfinderSystem.class, this);
    }

    public FindPathTask requestPath(Vector3i start, Vector3i target, PathRequest pathRequest) {
        FindPathTask task = new FindPathTask(start, target, pathRequest);
        taskMap.add(task);
        return task;
    }

    public HeightMap getHeightMap(Vector3i chunkPos) {
        return maps.get(chunkPos);
    }

    public WalkableBlock getBlock(Vector3i pos) {
        return pathfinder.getBlock(pos);
    }

    @Override
    public void initialise() {
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
                            findPaths(Sets.newHashSet(taskMap));
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

    private void findPaths(Set<FindPathTask> tasks) {
        pathfinder.clearCache();

        int count = 0;
        long time = System.nanoTime();
        int notFound = 0;
        int invalid = 0;
        int processed = 0;
        for (FindPathTask pathTask : tasks) {
            if (pathTask != null) {
                processed++;
                if (pathTask.processed) {
                    continue;
                }
                if (pathTask.pathRequest == null) {
                    continue;
                }
                count++;
                pathTask.process();
                if (pathTask.path == null) {
                    invalid++;
                }
                if (pathTask.path == Path.INVALID) {
                    notFound++;
                }
            }
        }
        float ms = (System.nanoTime() - time) / 1000 / 1000f;
        if (count > 0) {
            logger.info("Searching " + count + " pathes took " + ms + " ms ("
                + (1000f / ms * count) + " pps), processed=" + processed + ", invalid=" + invalid + ", not found=" + notFound);
        }
    }

    @Override
    public void update(float delta) {
        while (!outputQueue.isEmpty()) {
            FindPathTask task = outputQueue.poll();
            if (task.pathRequest != null) {
                task.pathRequest.onPathReady(task.path);
            }
        }
    }

    @Override
    public void shutdown() {
        inputThreads.shutdown();
    }

    @ReceiveEvent(components = BlockComponent.class)
    public void blockChanged(BlockChange event, EntityRef entity) {
        Vector3i chunkPos = TeraMath.calcChunkPos(event.getPosition());
        invalidateChunk(chunkPos);
        updateChunkQueue.offer(new UpdateChunkTask(chunkPos));
    }

    @Override
    public void onChunkReady(Vector3i chunkPos) {
        invalidateChunk(chunkPos);
        updateChunkQueue.offer(new UpdateChunkTask(chunkPos));
    }

    private void invalidateChunk(Vector3i chunkPos) {
        invalidChunks.add(chunkPos);
        Set<FindPathTask> tasks = Sets.newHashSet(this.taskMap);
        for (FindPathTask task : tasks) {
            task.invalidate();
        }
    }
}
