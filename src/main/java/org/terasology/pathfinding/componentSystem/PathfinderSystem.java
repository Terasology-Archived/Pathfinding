// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.pathfinding.componentSystem;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.SettableFuture;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.navgraph.NavGraphSystem;
import org.terasology.navgraph.WalkableBlock;
import org.terasology.pathfinding.model.LineOfSight;
import org.terasology.pathfinding.model.LineOfSight2d;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.Pathfinder;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.registry.Share;

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
 */
@RegisterSystem(RegisterMode.AUTHORITY)
@Share(value = PathfinderSystem.class)
public class PathfinderSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(PathfinderSystem.class);

    @In
    private NavGraphSystem navGraphSystem;
    @In
    private LineOfSight lineOfSight;
    private Pathfinder pathfinder;
    private int nextId;
    private int pathsSearched;

    public PathfinderSystem() {
        CoreRegistry.put(LineOfSight.class, new LineOfSight2d());
    }

    @Override
    public void initialise() {
        pathfinder = createPathfinder();
        logger.info("PathfinderSystem started");
    }

    @Override
    public void shutdown() {
    }

    public SettableFuture<List<Path>> requestPath(EntityRef requestor, Vector3i target, List<Vector3i> start) {
        SettableFuture<List<Path>> future = SettableFuture.create();
        FindPathTask task = new FindPathTask(start, target, requestor, future);
        navGraphSystem.offer(task);
        return future;
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
        public SettableFuture<List<Path>> future;

        private FindPathTask(List<Vector3i> start, Vector3i target, EntityRef entity, SettableFuture<List<Path>> future) {
            this.start = start;
            this.target = target;
            this.entity = entity;
            this.pathId = nextId;
            this.future = future;
            nextId++;
        }

        @Override
        public String getName() {
            return "Pathfinder:FindPath";
        }

        @Override
        public void run() {
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
            if (future != null) {
                future.set(paths);
            }
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
