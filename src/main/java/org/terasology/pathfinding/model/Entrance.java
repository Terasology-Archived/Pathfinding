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

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author synopia
 */
public class Entrance {
    public enum Type {
        VERTICAL,
        HORIZONTAL
    }

    public final Set<Floor> neighborFloors = Sets.newHashSet();
    private Rect area;
    private Type type;
    private Floor floor;

    public Entrance(Floor floor) {
        this.floor = floor;
    }

    public boolean isPartOfEntrance(int x, int y) {
        if (area == null) {
            return true;
        }
        if (area.contains(x, y)) {
            return true;
        }
        int x1 = Math.min(area.x, x);
        int y1 = Math.min(area.y, y);
        int x2 = Math.max(area.x + area.w, x);
        int y2 = Math.max(area.y + area.h, y);

        if (type == Type.VERTICAL) {
            return y2 - y1 == 0 && area.w + 1 == x2 - x1;
        } else if (type == Type.HORIZONTAL) {
            return x2 - x1 == 0 && area.h + 1 == y2 - y1;
        } else {
            return x2 - x1 <= 1 && y2 - y1 <= 1;
        }
    }

    public void addToEntrance(int x, int y) {
        if (area == null) {
            area = new Rect(x, y, 0, 0);
        } else {
            if (!area.contains(x, y)) {
                int x1 = Math.min(area.x, x);
                int y1 = Math.min(area.y, y);
                int x2 = Math.max(area.x + area.w, x);
                int y2 = Math.max(area.y + area.h, y);
                area = new Rect(x1, y1, x2 - x1, y2 - y1);
                if (area.w > area.h) {
                    type = Type.VERTICAL;
                } else if (area.w < area.h) {
                    type = Type.HORIZONTAL;
                }
            }
        }
    }

    public WalkableBlock getAbstractBlock() {
        int mx = area.x + area.w / 2;
        int my = area.y + area.h / 2;

        return floor.getBlock(mx, my);
    }

    private final class Rect {
        int x;
        int y;
        int w;
        int h;

        private Rect(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        private boolean contains(int px, int py) {
            return px >= x && py >= y && px < x + h && py < y + h;
        }
    }
}
