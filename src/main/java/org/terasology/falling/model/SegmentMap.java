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
package org.terasology.falling.model;

import com.google.common.collect.Lists;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.ChunkConstants;

import java.util.List;

/**
 * Created by synopia on 12/29/13.
 */
public class SegmentMap {
    public static final int SIZE_X = ChunkConstants.SIZE_X;
    public static final int SIZE_Y = ChunkConstants.SIZE_Y;
    public static final int SIZE_Z = ChunkConstants.SIZE_Z;

    private SegmentCell[] cells = new SegmentCell[SIZE_X * SIZE_Z];
    private List<Segment> segments = Lists.newArrayList();

    public SegmentMap() {
        for (int z = 0; z < SIZE_Z; z++) {
            for (int x = 0; x < SIZE_X; x++) {
                cells[x + z * SIZE_Z] = new SegmentCell(x, z);
            }
        }
    }

    public SegmentCell getCell(int x, int z) {
        return cells[x + z * SIZE_Z];
    }

    public Segment getSegment(int x, int y, int z) {
        return getCell(x, z).getSegment(y);
    }

    public void load(WorldProvider world) {
        segments.clear();
        for (int z = 0; z < SIZE_Z; z++) {
            for (int x = 0; x < SIZE_X; x++) {
                scanCell(world, x, z);
            }
        }
    }

    private void scanCell(WorldProvider world, int x, int z) {
        boolean wall = true;
        SegmentCell cell = getCell(x, z);
        Segment current = new Segment(0, 0); // ground
        for (int y = 0; y < SIZE_Y; y++) {
            Block block = world.getBlock(x, y, z);
            boolean penetrable = block.isPenetrable();

            if (wall) {
                if (penetrable) {
                    // found gap
                    cell.addSegment(current);
                    wall = false;
                }
            } else {
                if (!penetrable) {
                    wall = true;
                    current = new Segment(y, 0);
                    segments.add(current);
                }
            }
            if (!penetrable) {
                current.height++;
                if (x > 0) {
                    Segment neighbor = getSegment(x - 1, y, z);
                    if (neighbor != null) {
                        current.connectTo(neighbor);
                    }
                }
                if (z > 0) {
                    Segment neighbor = getSegment(x, y, z - 1);
                    if (neighbor != null) {
                        current.connectTo(neighbor);
                    }
                }
            }
        }
    }

    public List<Segment> segments() {
        return segments;
    }
}
