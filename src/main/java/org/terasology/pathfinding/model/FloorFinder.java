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
import com.google.common.collect.Maps;
import org.terasology.math.Vector3i;

import java.util.*;

/**
 * @author synopia
 */
public class FloorFinder {
    private List<Region> regions = Lists.newArrayList();
    private Map<Region, Integer> count = Maps.newHashMap();
    private List<Sweep> sweeps = Lists.newArrayList();
    private Map<WalkableBlock, Region> regionMap = Maps.newHashMap();
    private Map<WalkableBlock, Sweep> sweepMap = Maps.newHashMap();


    public FloorFinder() {
    }

    public Region region(WalkableBlock block) {
        return regionMap.get(block);
    }

    public List<Region> regions() {
        return regions;
    }

    public void findFloors(HeightMap map) {
        findRegions(map);

        map.floors.clear();
        for (Region region : regions) {
            if (region.floor != null) {
                continue;
            }
            Floor floor = new Floor(map, map.floors.size());
            map.floors.add(floor);

            List<Region> stack = Lists.newLinkedList();
            stack.add(0, region);

            while (!stack.isEmpty()) {
                Collections.sort(stack, new Comparator<Region>() {
                    @Override
                    public int compare(Region o1, Region o2) {
                        return o1.id < o2.id ? -1 : o1.id > o2.id ? 1 : 0;
                    }
                });
                Region current = stack.remove(0);
                if (current.floor != null) {
                    continue;
                }
                if (!floor.overlap(current)) {
                    floor.merge(current);

                    Set<Region> neighborRegions = current.getNeighborRegions();
                    for (Region neighborRegion : neighborRegions) {
                        if (neighborRegion.floor == null) {
                            stack.add(neighborRegion);
                        }
                    }
                }
            }
        }

        for (Map.Entry<WalkableBlock, Region> entry : regionMap.entrySet()) {
            entry.getKey().floor = entry.getValue().floor;
        }
    }

    void findRegions(HeightMap map) {
        Vector3i worldPos = map.worldPos;
        regions.clear();
        regionMap.clear();
        sweepMap.clear();

        for (int z = 0; z < HeightMap.SIZE_Z; z++) {
            count.clear();
            sweeps.clear();
            // find sweeps
            for (int x = 0; x < HeightMap.SIZE_X; x++) {
                int offset = x + z * HeightMap.SIZE_Z;
                HeightMapCell cell = map.cells[offset];

                findSweeps(cell);
            }
            // map sweeps to regions
            for (Sweep sweep : sweeps) {
                if (sweep.neighbor != null && sweep.neighborCount == count.get(sweep.neighbor)) {
                    sweep.region = sweep.neighbor;
                } else {
                    sweep.region = new Region(regions.size());
                    regions.add(sweep.region);
                }
            }
            for (int x = 0; x < HeightMap.SIZE_X; x++) {
                int offset = x + z * HeightMap.SIZE_Z;
                HeightMapCell cell = map.cells[offset];

                for (WalkableBlock block : cell.blocks) {
                    Sweep sweep = sweepMap.remove(block);
                    Region region = sweep.region;
                    regionMap.put(block, region);
                    region.setPassable(block.x() - worldPos.x, block.z() - worldPos.z);
                }
            }
        }
        findRegionNeighbors(map);
    }

    private void findSweeps(HeightMapCell cell) {
        for (WalkableBlock block : cell.blocks) {
            Sweep sweep;
            WalkableBlock leftNeighbor = block.neighbors[HeightMap.DIR_LEFT];
            if (leftNeighbor != null) {
                sweep = sweepMap.get(leftNeighbor);
            } else {
                sweep = new Sweep();
                sweeps.add(sweep);
            }
            WalkableBlock upNeighbor = block.neighbors[HeightMap.DIR_UP];
            if (upNeighbor != null) {
                Region neighborRegion = regionMap.get(upNeighbor);
                if (neighborRegion != null) {
                    if (sweep.neighborCount == 0) {
                        sweep.neighbor = neighborRegion;
                    }
                    if (sweep.neighbor == neighborRegion) {
                        sweep.neighborCount++;
                        int c = count.containsKey(neighborRegion) ? count.get(neighborRegion) : 0;
                        c++;
                        count.put(neighborRegion, c);
                    } else {
                        sweep.neighbor = null;
                    }
                }
            }
            sweepMap.put(block, sweep);
        }
    }

    private void findRegionNeighbors(HeightMap map) {
        for (int z = 0; z < HeightMap.SIZE_Z; z++) {
            for (int x = 0; x < HeightMap.SIZE_X; x++) {
                int offset = x + z * HeightMap.SIZE_Z;
                HeightMapCell cell = map.cells[offset];
                findRegionNeighbors(cell);
            }
        }
    }

    private void findRegionNeighbors(HeightMapCell cell) {
        for (WalkableBlock block : cell.blocks) {
            Region region = regionMap.get(block);
            if (region == null) {
                continue;
            }

            for (WalkableBlock neighbor : block.neighbors) {
                Region neighborRegion = regionMap.get(neighbor);
                if ((neighbor == null) || (neighborRegion != null && neighborRegion.id != region.id)) {
                    region.addNeighborBlock(block, neighbor, neighborRegion);
                }
            }
        }
    }

}
