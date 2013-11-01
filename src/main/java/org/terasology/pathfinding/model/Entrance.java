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
import org.terasology.math.Rect2i;

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
    private Rect2i area;
    private Type type;
    private Floor floor;

    public Entrance(Floor floor) {
        this.floor = floor;
    }

    public boolean isPartOfEntrance(int x, int y) {
        if (area == null) {
            return true;
        }
        if (area.encompasses(x, y)) {
            return true;
        }
        int x1 = Math.min(area.minX(), x);
        int y1 = Math.min(area.minY(), y);
        int x2 = Math.max(area.maxX(), x);
        int y2 = Math.max(area.maxY(), y);

        if (type == Type.VERTICAL) {
            return y2 - y1 == 0 && area.width() + 1 == x2 - x1;
        } else if (type == Type.HORIZONTAL) {
            return x2 - x1 == 0 && area.height() + 1 == y2 - y1;
        } else {
            return x2 - x1 <= 1 && y2 - y1 <= 1;
        }
    }

    public void addToEntrance(int x, int y) {
        if (area == null) {
            area = Rect2i.createFromMinAndSize(x, y, 0, 0);
        } else {
            if (!area.encompasses(x, y)) {
                int x1 = Math.min(area.minX(), x);
                int y1 = Math.min(area.minY(), y);
                int x2 = Math.max(area.maxX(), x);
                int y2 = Math.max(area.maxY(), y);
                area = Rect2i.createFromMinAndSize(x1, y1, x2 - x1, y2 - y1);
                if (area.width() > area.height()) {
                    type = Type.VERTICAL;
                } else if (area.width() < area.height()) {
                    type = Type.HORIZONTAL;
                }
            }
        }
    }

    public WalkableBlock getAbstractBlock() {
        int mx = area.minX() + area.width() / 2;
        int my = area.minY() + area.height() / 2;

        return floor.getBlock(mx, my);
    }
}
