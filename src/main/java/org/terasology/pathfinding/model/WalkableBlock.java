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

import org.terasology.math.Vector3i;

/**
 * @author synopia
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
        StringBuilder sb = new StringBuilder();
        sb.append(position.toString()).append("\n");
        if (floor != null) {
            sb.append(floor.toString()).append("\n");
        } else {
            sb.append("no floor\n");
        }
        return sb.toString();
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
