// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.pathfinding.navgraph;

import org.terasology.math.geom.Vector3i;

/**
 * @author synopia
 */
public class WalkableBlock {
    public WalkableBlock[] neighbors = new WalkableBlock[8];
    public Floor floor;
    private final Vector3i position;

    public WalkableBlock(int x, int z, int height) {
        position = new Vector3i(x, height, z);
    }

    public Vector3i getBlockPosition() {
        return position;
    }

    public int x() {
        return position.x;
    }

    public int z() {
        return position.z;
    }

    public int height() {
        return position.y;
    }

    @Override
    public String toString() {
        return position.toString();
    }

    public boolean hasNeighbor(WalkableBlock block) {
        for (WalkableBlock neighbor : neighbors) {
            if (neighbor == block) {
                return true;
            }
        }
        return false;
    }
}
