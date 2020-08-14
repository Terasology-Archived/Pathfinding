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

import java.util.HashSet;
import java.util.Set;

/**
 * @author synopia
 * Abstract class representing a 2d region which floors {@link Floor} and regions {@link Region} derive
 * from. They allow us to divide the landscape and store information about the nature of blocks
 * (Passable or not) making it easier to handle.
 */
public abstract class BaseRegion<N extends BaseRegion> {
    public int id;
    public Set<N> neighborRegions = new HashSet<N>();
    protected BitMap map;
    protected Floor floor;

    /**
     * Creates a BaseRegion. The newly created Base Region has a fixed size defined by in-game ChunkConstants
     *
     * @param id the id of the base region. It is used to uniquely identify a base region, Floor or Region and
     *         is particularly useful while merging regions when they are connected
     */
    protected BaseRegion(int id) {
        this.id = id;
        map = new BitMap();
    }

    /**
     * Marks a point in the region as walkable by characters
     *
     * @param x the x co-ordinate of the point with respect to region's top-left corner (the corner with the
     *         smallest x and y values)
     * @param y the y co-ordinate of the point with respect to region's top-left corner (the corner with the
     *         smallest x and y values)
     */
    public void setPassable(int x, int y) {
        map.setPassable(x, y);
    }

    /**
     * Function to get the map representing the region
     *
     * @return bitmap of the region
     */
    public BitMap getMap() {
        return map;
    }

    /**
     * Returns neighbours with whom this region shares a side The neighbour regions are random and the order depends on
     * the landscape
     *
     * @return set of neighbouring regions
     */
    public Set<N> getNeighborRegions() {
        return neighborRegions;
    }

    /**
     * @return the id of the base region along with the number of neighbouring regions
     */
    @Override
    public String toString() {
        return id + "\nrc = " + neighborRegions.size();
    }
}
