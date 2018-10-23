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

    /**
     * Creates a new Floor object
     * @param navGraphChunk the navGraphChunk parameter, not null
     * @param id the id parameter, not null
     */
    public Floor(NavGraphChunk navGraphChunk, int id) {
        super(id);
        this.navGraphChunk = navGraphChunk;
        entranceMap = new Entrance[NavGraphChunk.SIZE_X * NavGraphChunk.SIZE_Z];
        entrances = Lists.newArrayList();
    }

    /**
     * Returns whether or not there is an overlap
     * @param region the region parameter, not null
     * @return if there is an overlap
     */
    public boolean overlap(Region region) {
        return map.overlap(region.map);
    }

    /**
     * Adds a neighbor block
     * @param neighbor the neighbor parameter, not null
     */
    public void addNeighborBlock(WalkableBlock neighbor) {
        neighborRegions.add(neighbor.floor);
    }

    /**
     * Removes a neighbor block
     * @param neighbor the neighbor parameter, not null
     */
    public void removeNeighborBlock(WalkableBlock neighbor) {
        neighborRegions.remove(neighbor.floor);
    }

    /**
     * Merges regions
     * @param neighbor the neighbor parameter, not null
     */
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

    /**
     * Resets Entrances
     */
    public void resetEntrances() {
        Arrays.fill(entranceMap, null);
        entrances.clear();
    }

    /**
     * Returns whether or not a block is an entrance
     * @block the block parameter, not null
     * @return if it is an entrance
     */
    public boolean isEntrance(WalkableBlock block) {
        Vector3i position = ChunkMath.calcBlockPos(block.getBlockPosition());
        return isEntrance(position.x, position.z);
    }

    /**
     * Returns whether or not a location in entranceMap is an entrance
     * @param x the x parameter, not null
     * @param y the y parameter, not null
     * @return if it an entrance
     */
    public boolean isEntrance(int x, int y) {
        return entranceMap[x + y * NavGraphChunk.SIZE_Z] != null;
    }

    /**
     * Sets a location in entranceMap to an entrance
     * @param x the x parameter, not null
     * @param y the y parameter, not null
     * @return Entrance object at (x,y) 
     */
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

    /**
     * Sets a WalkableBlock as an entrance
     * @param block the block parameter, not null
     * @param neighbor the neighbor parameter, not null
     */
    public void setEntrance(WalkableBlock block, WalkableBlock neighbor) {
        Vector3i position = ChunkMath.calcBlockPos(block.getBlockPosition());
        Entrance entrance = setEntrance(position.x, position.z);
        entrance.neighborFloors.add(neighbor.floor);
    }

    /**
     * Returns list of all entrances
     * @return list of entrances
     */
    public List<Entrance> entrances() {
        return entrances;
    }

    /**
     * Returns the block at the parameters location
     * @param fx the fx parameter, not null
     * @param fy the fy parameter, not null
     * @return The block at (fx,fy)
     */
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
