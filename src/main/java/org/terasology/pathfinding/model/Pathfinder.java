// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.pathfinding.model;

import com.google.common.collect.Lists;
import org.terasology.pathfinding.navgraph.NavGraphSystem;
import org.terasology.pathfinding.navgraph.WalkableBlock;

import java.util.Collections;
import java.util.List;

/**
 *
 */
public class Pathfinder {
    private final HAStar haStar;
    private final PathCache cache;
    private final NavGraphSystem world;

    public Pathfinder(NavGraphSystem world, LineOfSight lineOfSight) {
        this.world = world;
        haStar = new HAStar(lineOfSight);
        cache = new PathCache();
    }

    public void clearCache() {
        cache.clear();
    }

    public Path findPath(final WalkableBlock target, final WalkableBlock start) {
        return findPath(target, Collections.singletonList(start)).get(0);
    }

    public List<Path> findPath(final WalkableBlock target, final List<WalkableBlock> starts) {
        List<Path> result = Lists.newArrayList();
        haStar.reset();
        for (WalkableBlock start : starts) {
            Path path = cache.findPath(start, target, (from, to) -> {
                if (from == null || to == null) {
                    return Path.INVALID;
                }
                WalkableBlock refFrom = world.getBlock(from.getBlockPosition());
                WalkableBlock refTo = world.getBlock(to.getBlockPosition());

                Path path1;
                if (haStar.run(refFrom, refTo)) {
                    path1 = haStar.getPath();
                } else {
                    path1 = Path.INVALID;
                }
                return path1;
            });
            result.add(path);
        }

        return result;
    }


    @Override
    public String toString() {
        return haStar.toString();
    }
}
