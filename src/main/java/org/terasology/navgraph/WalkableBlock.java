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

import org.terasology.math.geom.Vector3i;

/**
 * @author synopia Represents a Block through which characters can walk through. They are blocks which have atleast 2
 *         penetrable blocks above them and are themselves not penetrable
 */
public class WalkableBlock {
    public WalkableBlock[] neighbors = new WalkableBlock[8];
    public Floor floor;
    private Vector3i position;

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

    /**
     * @param block
     * @return if the block has a neighbour.
     */
    public boolean hasNeighbor(WalkableBlock block) {
        for (WalkableBlock neighbor : neighbors) {
            if (neighbor == block) {
                return true;
            }
        }
        return false;
    }
}
