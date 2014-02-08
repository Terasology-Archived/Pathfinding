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

import org.terasology.math.Vector3i;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

/**
 * @author synopia
 */
public class WalkableBlockFinder {
    private WorldProvider world;

    public WalkableBlockFinder(WorldProvider world) {
        this.world = world;
    }

    public void findWalkableBlocks(NavGraphChunk map) {
        int[] airMap = new int[NavGraphChunk.SIZE_X * NavGraphChunk.SIZE_Z];
        Vector3i blockPos = new Vector3i();
        map.walkableBlocks.clear();
        Vector3i worldPos = map.worldPos;
        for (int y = NavGraphChunk.SIZE_Y - 1; y >= 0; y--) {
            for (int z = 0; z < NavGraphChunk.SIZE_Z; z++) {
                for (int x = 0; x < NavGraphChunk.SIZE_X; x++) {
                    blockPos.set(x + worldPos.x, y + worldPos.y, z + worldPos.z);
                    Block block = world.getBlock(blockPos);
                    int offset = x + z * NavGraphChunk.SIZE_Z;
                    if (block.isPenetrable()) {
                        airMap[offset]++;
                    } else {
                        if (airMap[offset] >= 2) {
                            WalkableBlock walkableBlock = new WalkableBlock(blockPos.x, blockPos.z, blockPos.y);
                            map.cells[offset].addBlock(walkableBlock);
                            map.walkableBlocks.add(walkableBlock);
                        }
                        airMap[offset] = 0;
                    }
                }
            }
        }

        findNeighbors(map);
    }

    private void findNeighbors(NavGraphChunk map) {
        map.borderBlocks.clear();
        for (int z = 0; z < NavGraphChunk.SIZE_Z; z++) {
            for (int x = 0; x < NavGraphChunk.SIZE_X; x++) {
                int offset = x + z * NavGraphChunk.SIZE_Z;
                NavGraphCell cell = map.cells[offset];
                for (WalkableBlock block : cell.blocks) {
                    for (int i = 0; i < NavGraphChunk.DIRECTIONS.length; i++) {
                        connectToDirection(map, x, z, block, i);
                    }
                }
            }
        }
    }

    private void connectToDirection(NavGraphChunk map, int x, int z, WalkableBlock block, int direction) {
        int dx = NavGraphChunk.DIRECTIONS[direction][0];
        int dy = NavGraphChunk.DIRECTIONS[direction][1];
        int nx = x + dx;
        int nz = z + dy;
        if (nx < 0 || nz < 0 || nx >= NavGraphChunk.SIZE_X || nz >= NavGraphChunk.SIZE_Z) {
            map.borderBlocks.add(block);
            return;
        }
        NavGraphCell neighbor = map.cells[nx + nz * NavGraphChunk.SIZE_Z];
        for (WalkableBlock neighborBlock : neighbor.blocks) {
            connectBlocks(block, neighborBlock, direction);
        }
    }

    private void connectBlocks(WalkableBlock block, WalkableBlock neighborBlock, int direction) {
        int heightDiff = block.height() - neighborBlock.height();
        boolean diagonal = (direction % 2) == 1;
        if (heightDiff == 0) {
            if (!diagonal) {
                block.neighbors[direction] = neighborBlock;
            } else {
                int dx = block.x() - neighborBlock.x();
                int dz = block.z() - neighborBlock.z();
                boolean free1 = world.getBlock(block.x() - dx, block.height() + 1, block.z()).isPenetrable();
                free1 &= world.getBlock(block.x() - dx, block.height() + 2, block.z()).isPenetrable();
                boolean free2 = world.getBlock(block.x(), block.height() + 1, block.z() - dz).isPenetrable();
                free2 &= world.getBlock(block.x(), block.height() + 2, block.z() - dz).isPenetrable();
                if (free1 && free2) {
                    block.neighbors[direction] = neighborBlock;
                }
            }
        } else if (Math.abs(heightDiff) < 2 && !diagonal) {
            WalkableBlock lower = heightDiff < 0 ? block : neighborBlock;
            Block jumpCheck = world.getBlock(lower.x(), lower.height() + 3, lower.z());
            if (jumpCheck.isPenetrable()) {
                block.neighbors[direction] = neighborBlock;
            }
        }
    }

}
