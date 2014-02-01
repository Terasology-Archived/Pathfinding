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
package org.terasology.pathfinding.model;

import com.google.common.collect.Lists;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.world.WorldProvider;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author synopia
 */
public class Pathfinder {
    private HAStar haStar;
    private PathCache cache;
    private PathfinderWorld world;

    public Pathfinder(PathfinderWorld world) {
        this.world = world;
        haStar = new HAStar();
        cache = new PathCache();
    }

    public void clearCache() {
        cache.clear();
    }

    public Path findPath(final WalkableBlock target, final WalkableBlock start) {
        return findPath(target, Arrays.asList(start)).get(0);
    }

    public List<Path> findPath(final WalkableBlock target, final List<WalkableBlock> starts) {
        List<Path> result = Lists.newArrayList();
        haStar.reset();
        for (WalkableBlock start : starts) {
            result.add(cache.findPath(start, target, new PathCache.Callback() {
                @Override
                public Path run(WalkableBlock from, WalkableBlock to) {
                    if (from == null || to == null) {
                        return Path.INVALID;
                    }
                    WalkableBlock refFrom = world.getBlock(from.getBlockPosition());
                    WalkableBlock refTo = world.getBlock(to.getBlockPosition());

                    Path path;
                    if (haStar.run(refFrom, refTo)) {
                        path = haStar.getPath();
                        path.add(refFrom);
                    } else {
                        path = Path.INVALID;
                    }
                    return path;
                }
            }));
        }
        return result;
    }


    @Override
    public String toString() {
        return haStar.toString();
    }
}
