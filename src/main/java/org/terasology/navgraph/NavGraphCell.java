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

import java.util.ArrayList;
import java.util.List;

/**
 * @author synopia
 * Represents each cell in a region.
 */
public class NavGraphCell {
    public final List<WalkableBlock> blocks = new ArrayList<WalkableBlock>();

    /**
     * Adds a Walkable block to a Cell in the map.
     * Since the cells, are only in 2D, each cell can have multiple WalkableBlocks at different heights
     *
     * @param walkableBlock the Walkable Block you want to add to the NavGraphCell
     */
    public void addBlock(WalkableBlock walkableBlock) {
        if (blocks.size() == 0) {
            blocks.add(walkableBlock);
        } else {
            blocks.add(0, walkableBlock);
        }
    }

    /**
     * Returns a Walkable block at a particular height in the NavGraphCell
     *
     * @param height the height of the Walkable Block you want
     * @return the Walkable Block at some height in this NavGraphCell
     */
    public WalkableBlock getBlock(int height) {
        for (WalkableBlock block : blocks) {
            if (block.height() == height) {
                return block;
            }
        }
        return null;
    }
}
