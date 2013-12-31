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

import java.util.List;

/**
 * Created by synopia on 12/29/13.
 */
public class SegmentCell {
    private int x;
    private int z;
    public final List<Segment> segments = Lists.newArrayList();

    public SegmentCell(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public void addSegment(Segment segment) {
        segments.add(segment);
    }

    public Segment getSegment(int height) {
        for (Segment segment : segments) {
            if (segment.contains(height)) {
                return segment;
            }
        }
        return null;
    }
}
