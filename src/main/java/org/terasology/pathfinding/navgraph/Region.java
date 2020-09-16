// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.pathfinding.navgraph;

/**
 * @author synopia
 */
public class Region extends BaseRegion<Region> {
    public Region(int id) {
        super(id);
    }

    public void addNeighborBlock(WalkableBlock current, WalkableBlock neighbor, Region neighborRegion) {
        if (neighborRegion != null) {
            neighborRegions.add(neighborRegion);
            neighborRegion.neighborRegions.add(this);
        }
    }

}
