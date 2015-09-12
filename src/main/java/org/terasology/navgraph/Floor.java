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
package org.terasology.navgraph;

import com.google.common.collect.Lists;

import org.terasology.math.ChunkMath;
import org.terasology.math.geom.Vector3i;

import java.util.Arrays;
import java.util.List;

/**
 * @author synopia
 */
public class Floor extends BaseRegion<Floor> {
    public NavGraphChunk navGraphChunk;
    private Entrance[] entranceMap;
    private List<Entrance> entrances;

    public Floor(NavGraphChunk navGraphChunk, int id) {
        super(id);
        this.navGraphChunk = navGraphChunk;
        entranceMap = new Entrance[NavGraphChunk.SIZE_X * NavGraphChunk.SIZE_Z];
        entrances = Lists.newArrayList();
    }

    public boolean overlap(Region region) {
        return map.overlap(region.map);
    }

    public void addNeighborBlock(WalkableBlock neighbor) {
        neighborRegions.add(neighbor.floor);
    }

    public void removeNeighborBlock(WalkableBlock neighbor) {
        neighborRegions.remove(neighbor.floor);
    }

    public void merge(Region neighbor) {
        map.merge(neighbor.map);
        neighbor.floor = this;
        for (Region neighborRegion : neighbor.neighborRegions) {
            if (neighborRegion.floor != null && neighborRegion.floor != this) {
                neighborRegions.add(neighborRegion.floor);
                neighborRegion.floor.neighborRegions.add(this);
            }
        }
    }

    public void resetEntrances() {
        Arrays.fill(entranceMap, null);
        entrances.clear();
    }

    public boolean isEntrance(WalkableBlock block) {
        Vector3i position = ChunkMath.calcBlockPos(block.getBlockPosition());
        return isEntrance(position.x, position.z);
    }

    public boolean isEntrance(int x, int y) {
        return entranceMap[x + y * NavGraphChunk.SIZE_Z] != null;
    }

    public Entrance setEntrance(int x, int y) {
        if (entranceMap[x + y * NavGraphChunk.SIZE_Z] != null) {
            return entranceMap[x + y * NavGraphChunk.SIZE_Z];
        }
        Entrance left = null;
        Entrance up = null;
        Entrance leftUp = null;
        if (x > 0) {
            left = entranceMap[x - 1 + y * NavGraphChunk.SIZE_Z];
        }
        if (y > 0) {
            up = entranceMap[x + (y - 1) * NavGraphChunk.SIZE_Z];
        }
        if (x > 0 && y > 0) {
            leftUp = entranceMap[x - 1 + (y - 1) * NavGraphChunk.SIZE_Z];
        }
        Entrance entrance;
        if (left == null && up == null && leftUp == null) {
            entrance = new Entrance(this);
            entrance.addToEntrance(x, y);
            entrances.add(entrance);
        } else {
            entrance = left != null ? left : up != null ? up : leftUp;
            if (entrance.isPartOfEntrance(x, y)) {
                entrance.addToEntrance(x, y);
            } else {
                entrance = new Entrance(this);
                entrance.addToEntrance(x, y);
                entrances.add(entrance);
            }
        }
        entranceMap[x + y * NavGraphChunk.SIZE_Z] = entrance;
        return entrance;
    }

    public void setEntrance(WalkableBlock block, WalkableBlock neighbor) {
        Vector3i position = ChunkMath.calcBlockPos(block.getBlockPosition());
        Entrance entrance = setEntrance(position.x, position.z);
        entrance.neighborFloors.add(neighbor.floor);
    }

    public List<Entrance> entrances() {
        return entrances;
    }

    WalkableBlock getBlock(int fx, int fy) {
        NavGraphCell cell = navGraphChunk.getCell(fx, fy);
        for (WalkableBlock block : cell.blocks) {
            if (block.floor == this) {
                return block;
            }
        }
        return null;
    }
}
