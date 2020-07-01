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
 * Abstract class which floors and regions derive from
 */
public abstract class BaseRegion<N extends BaseRegion> {
    public int id;
    public Set<N> neighborRegions = new HashSet<N>();
    protected BitMap map;
    protected Floor floor;

    /**
     * Creates a BaseRegion
     * @param id id of the base region
     */
    protected BaseRegion(int id) {
        this.id = id;
        map = new BitMap();
    }

    /**
     * Marks a point in the region as walkable by characters
     * @param x x co-ordinate of the point with respect to region's top-left corner
     * @param y y co-ordinate of the point with respect ot region's rop-left corner
     */

    public void setPassable(int x, int y) {
        map.setPassable(x, y);
    }

    /**
     * Function to get the map representing the region
     * @return Bitmap of the region
     */

    public BitMap getMap() {
        return map;
    }

    /**
     * Returns neighbours with whom this region shares a side
     * @return set of neighbouring regions
     */

    public Set<N> getNeighborRegions() {
        return neighborRegions;
    }

    /**
     *
     * @return The id of the base region along with the number of neighbouring regions
     */

    @Override
    public String toString() {
        return id + "\nrc = " + neighborRegions.size();
    }
}
