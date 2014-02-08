/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.pathfinding.componentSystem;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.Vector3i;
import org.terasology.navgraph.NavGraphSystem;
import org.terasology.navgraph.WalkableBlock;
import org.terasology.pathfinding.model.LineOfSight;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.Pathfinder;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;

import javax.vecmath.Vector3f;
import java.util.List;

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
public class PathfinderSystem implements ComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(PathfinderSystem.class);

    @In
    private NavGraphSystem navGraphSystem;
    @In
    private LineOfSight lineOfSight;
    private Pathfinder pathfinder;
    private int nextId;
    private int pathsSearched;

    public PathfinderSystem() {
        CoreRegistry.put(PathfinderSystem.class, this);
    }

    @Override
    public void initialise() {
        pathfinder = createPathfinder();
        logger.info("PathfinderSystem started");
    }

    @Override
    public void shutdown() {
    }

    public int requestPath(EntityRef requestor, Vector3i target, List<Vector3i> start) {
        FindPathTask task = new FindPathTask(start, target, requestor);
        navGraphSystem.offer(task);
        return task.pathId;
    }

    public Path findPath(final WalkableBlock target, final WalkableBlock start) {
        return pathfinder.findPath(target, start);
    }

    public List<Path> findPath(final WalkableBlock target, final List<WalkableBlock> starts) {
        return pathfinder.findPath(target, starts);
    }

    public WalkableBlock getBlock(Vector3i pos) {
        return navGraphSystem.getBlock(pos);
    }

    public WalkableBlock getBlock(EntityRef minion) {
        return navGraphSystem.getBlock(minion);
    }

    public WalkableBlock getBlock(Vector3f pos) {
        return navGraphSystem.getBlock(pos);
    }

    public int getPathsSearched() {
        return pathsSearched;
    }

    protected Pathfinder createPathfinder() {
        return new Pathfinder(navGraphSystem, lineOfSight);
    }

    /**
     * Task to find a path.
     * <p/>
     * Note: this class has a natural ordering that is inconsistent with equals.
     */
    private final class FindPathTask implements NavGraphSystem.NavGraphTask {
        public EntityRef entity;
        public List<Path> paths;
        public List<Vector3i> start;
        public Vector3i target;
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
                    startBlocks.add(navGraphSystem.getBlock(pos));
                }
            }
            WalkableBlock targetBlock = navGraphSystem.getBlock(this.target);
            paths = null;
            if (targetBlock != null && startBlocks.size() > 0) {
                paths = pathfinder.findPath(targetBlock, startBlocks);
            }
            entity.send(new PathReadyEvent(pathId, paths, targetBlock, startBlocks));
        }

        @Override
        public int getPriority() {
            return 1 + pathId;
        }

        @Override
        public boolean isTerminateSignal() {
            return false;
        }

        @Override
        public int compareTo(NavGraphSystem.NavGraphTask o) {
            return Integer.compare(this.getPriority(), o.getPriority());
        }

    }
}
