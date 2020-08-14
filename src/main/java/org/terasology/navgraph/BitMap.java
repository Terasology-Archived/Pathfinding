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

import java.util.BitSet;
import java.util.List;

/**
 * @author synopia Representation of a region or floor {@link Floor} The BitMap consists of a number of bits, each of
 *         which represent a block. A value of true or 1 means that the block is passable
 */
public class BitMap {
    public static final int KERNEL_SIZE = 3;
    public static final float SQRT_2 = (float) Math.sqrt(2);
    BitSet map;

    /**
     * Creates a new Bitmap
     * A bitmap created this way always has a fixed number of nodes, decided by in-game Chunk
     * constants The default value for all the bits are false or 0.
     */
    public BitMap() {
        map = new BitSet(getNumberOfNodes());
    }

    public int offset(int x, int y) {
        return x + y * getWidth();
    }

    public void setPassable(int offset) {
        map.set(offset, true);
    }

    public boolean isPassable(int offset) {
        return map.get(offset);
    }

    public void setPassable(int x, int y) {
        setPassable(offset(x, y));
    }

    public boolean isPassable(int x, int y) {
        if (x < 0 || y < 0 || x >= getWidth() || y >= getHeight()) {
            return false;
        }
        return isPassable(offset(x, y));
    }

    /**
     * Function that adds all the possible nodes that you could possibly go to from the current position
     *
     * @param offset the offset of the current position
     * @param successors the list into which the successor nodes will be added
     */
    public void getSuccessors(int offset, List<Integer> successors) {
        int x = getX(offset);
        int y = getY(offset);
        if (y > 0 && isPassable(offset - getWidth())) {
            successors.add(offset - getWidth());
        }
        if (x < getWidth() - 1 && isPassable(offset + 1)) {
            successors.add(offset + 1);
        }
        if (y < getHeight() - 1 && isPassable(offset + getWidth())) {
            successors.add(offset + getWidth());
        }
        if (x > 0 && isPassable(offset - 1)) {
            successors.add(offset - 1);
        }

        if (x < getWidth() - 1 && y > 0 && isPassable(offset + 1 - getWidth())) {
            successors.add(offset + 1 - getWidth());
        }
        if (x < getWidth() - 1 && y < getHeight() - 1 && isPassable(offset + 1 + getWidth())) {
            successors.add(offset + 1 + getWidth());
        }
        if (x > 0 && y < getHeight() - 1 && isPassable(offset - 1 + getWidth())) {
            successors.add(offset - 1 + getWidth());
        }
        if (x > 0 && y > 0 && isPassable(offset - 1 - getWidth())) {
            successors.add(offset - 1 - getWidth());
        }
    }

    /**
     * Tells you whether there is any overlap between 'this' (the one that called the function) map and 'other' map
     *
     * @param other the map with which you want to check overlap
     * @return true if there is any overlap
     */
    public boolean overlap(BitMap other) {
        BitSet temp = (BitSet) map.clone();
        temp.and(other.map);
        return temp.cardinality() > 0;
    }

    /**
     * Merges 'this' map and 'other' and sets its value to 'this'
     *
     * @param other bitmap with which you want to merge
     */
    public void merge(BitMap other) {
        map.or(other.map);
    }

    /**
     * Gives information about the map with 'X's representing Walkable Blocks and spaces representing blocked areas
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int count = map.cardinality();
        for (int z = 0; z < getHeight(); z++) {
            for (int x = 0; x < getWidth(); x++) {
                if (map.get(offset(x, z))) {
                    sb.append("X");
                    count--;
                } else {
                    sb.append(" ");
                }
            }
            if (count == 0) {
                break;
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Function that return number of nodes in the bitmap
     *
     * @return number of cells in the map
     */
    public int getNumberOfNodes() {
        return getWidth() * getHeight();
    }

    public int getWidth() {
        return NavGraphChunk.SIZE_X;
    }

    public int getHeight() {
        return NavGraphChunk.SIZE_Z;
    }

    /**
     * @param from start position
     * @param to end postion
     * @return the Euclidean distance between two neighbouring cells in 2D
     */
    public float exactDistance(int from, int to) {
        int diff = to - from;
        if (diff == -getWidth() || diff == 1 || diff == getWidth() || diff == -1) {
            return 1;
        }
        if (diff == -getWidth() + 1 || diff == getWidth() + 1 || diff == getWidth() - 1 || diff == -getWidth() - 1) {
            return SQRT_2;
        }
        return 0;
    }

    public float fastDistance(int from, int to) {
        int fromX = getX(from);
        int fromY = getY(from);
        int toX = getX(to);
        int toY = getY(to);

        return (float) Math.abs(fromX - toX) + Math.abs(fromY - toY);
//        return (float) Math.sqrt( (fromX-toX)*(fromX-toX)+ (fromY-toY)*(fromY-toY));

    }

    public int getY(int id) {
        return id / getWidth();
    }

    public int getX(int id) {
        return id % getWidth();
    }
}
