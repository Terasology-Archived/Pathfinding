// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.pathfinding.componentSystem;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.SettableFuture;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.GameThread;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.navgraph.NavGraphSystem;
import org.terasology.navgraph.WalkableBlock;
import org.terasology.pathfinding.model.LineOfSight;
import org.terasology.pathfinding.model.LineOfSight2d;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.Pathfinder;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

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
    private AtomicInteger pathsSearched = new AtomicInteger();

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

    public static class  RequestPath {
        EntityRef requestor;
        Vector3i target;
        List<Vector3i> start;

        public RequestPath(EntityRef requestor, Vector3i target, List<Vector3i> start) {
            this.requestor = requestor;
            this.target = target;
            this.start = start;
        }
    }

    public Maybe<List<Path>> requestPath(EntityRef requestor, Vector3i target, List<Vector3i> start) {
        return requestPath(new RequestPath(requestor, target, start));
    }

    public Maybe<List<Path>> requestPath(RequestPath requestPath) {
        return Single
            .just(requestPath)
            .observeOn(GameThread.computation())
            .<List<Path>>mapOptional(requestPath1 -> {
                pathsSearched.incrementAndGet();
                List<WalkableBlock> startBlocks = Lists.newArrayList();
                for (Vector3i pos : requestPath1.start) {
                    if (pos != null) {
                        startBlocks.add(navGraphSystem.getBlock(pos));
                    }
                }
                WalkableBlock targetBlock = navGraphSystem.getBlock(requestPath1.target);
                if (targetBlock != null && startBlocks.size() > 0) {
                    return Optional.of(pathfinder.findPath(targetBlock, startBlocks));
                }
                return Optional.empty();

            });
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
        return pathsSearched.get();
    }

    protected Pathfinder createPathfinder() {
        return new Pathfinder(navGraphSystem, lineOfSight);
    }
}
