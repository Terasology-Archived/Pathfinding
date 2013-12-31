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

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by synopia on 12/29/13.
 */
public class Segment {
    public float y;
    public int height;
    public final Map<Segment, Integer> neighbors = Maps.newHashMap();

    public Segment(int y, int height) {
        this.y = y;
        this.height = height;
    }

    public boolean isGround() {
        return y == 0;
    }

    public boolean contains(int p) {
        return y <= p && p < y + height;
    }

    public void connectTo(Segment neighbor) {
        addNeighbor(neighbor);
        neighbor.addNeighbor(this);
    }

    private void addNeighbor(Segment segment) {
        if (neighbors.containsKey(segment)) {
            neighbors.put(segment, neighbors.get(segment) + 1);
        } else {
            neighbors.put(segment, 1);
        }
    }
}
