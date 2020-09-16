// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.pathfinding.navgraph;

import java.util.ArrayList;
import java.util.List;

/**
 * @author synopia
 */
public class NavGraphCell {
    public final List<WalkableBlock> blocks = new ArrayList<WalkableBlock>();

    public void addBlock(WalkableBlock walkableBlock) {
        if (blocks.size() == 0) {
            blocks.add(walkableBlock);
        } else {
            blocks.add(0, walkableBlock);
        }
    }

    public WalkableBlock getBlock(int height) {
        for (WalkableBlock block : blocks) {
            if (block.height() == height) {
                return block;
            }
        }
        return null;
    }
}
