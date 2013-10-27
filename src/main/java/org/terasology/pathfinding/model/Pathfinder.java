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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.world.WorldProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * @author synopia
 */
public class Pathfinder {
    private static final Logger logger = LoggerFactory.getLogger(Pathfinder.class);

    private WorldProvider world;
    private Map<Vector3i, HeightMap> heightMaps = new HashMap<>();
    private HAStar haStar;
    private PathCache cache;

    public Pathfinder(WorldProvider world) {
        this.world = world;
        haStar = new HAStar();
        cache = new PathCache();
    }

    public void clearCache() {
        cache.clear();
    }

    public Path[] findPath(final WalkableBlock target, final WalkableBlock... starts) {
        Path[] result = new Path[starts.length];
        haStar.reset();
        for (int i = 0; i < starts.length; i++) {
            WalkableBlock start = starts[i];
            result[i] = cache.findPath(start, target, new PathCache.Callback() {
                @Override
                public Path run(WalkableBlock from, WalkableBlock to) {
                    WalkableBlock refFrom = getBlock(from.getBlockPosition());
                    WalkableBlock refTo = getBlock(to.getBlockPosition());

                    Path path;
                    if (haStar.run(refFrom, refTo)) {
                        path = haStar.getPath();
                        path.add(refFrom);
                    } else {
                        path = Path.INVALID;
                    }
                    return path;
                }
            });
        }
        return result;
    }

    public HeightMap init(Vector3i chunkPos) {
        clearCache();
        HeightMap heightMap = heightMaps.get(chunkPos);
        if (heightMap == null) {
            heightMap = new HeightMap(world, chunkPos);
            heightMap.update();
            heightMaps.put(chunkPos, heightMap);
            heightMap.connectNeighborMaps(getNeighbor(chunkPos, -1, 0), getNeighbor(chunkPos, 0, -1), getNeighbor(chunkPos, 1, 0), getNeighbor(chunkPos, 0, 1));
        }
        return heightMap;
    }

    private HeightMap getNeighbor(Vector3i chunkPos, int x, int z) {
        Vector3i neighborPos = new Vector3i(chunkPos);
        neighborPos.add(x, 0, z);
        return heightMaps.get(neighborPos);
    }

    public HeightMap update(Vector3i chunkPos) {
        HeightMap heightMap = heightMaps.remove(chunkPos);
        if (heightMap != null) {
            heightMap.disconnectNeighborMaps(getNeighbor(chunkPos, -1, 0), getNeighbor(chunkPos, 0, -1), getNeighbor(chunkPos, 1, 0), getNeighbor(chunkPos, 0, 1));
            heightMap.cells = null;
        }
        return init(chunkPos);
    }

    public WalkableBlock getBlock(Vector3i pos) {
        Vector3i chunkPos = TeraMath.calcChunkPos(pos);
        HeightMap heightMap = heightMaps.get(chunkPos);
        if (heightMap != null) {
            return heightMap.getBlock(pos.x, pos.y, pos.z);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return haStar.toString();
    }
}
