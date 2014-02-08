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
package org.terasology.pathfinding.model;

import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.navgraph.WalkableBlock;

/**
 * Created by synopia on 01.02.14.
 */
@RegisterSystem
public class LineOfSight2d implements LineOfSight {
    private WalkableBlock current;

    @Override
    public void initialise() {

    }

    @Override
    public void shutdown() {

    }

    public boolean inSight(WalkableBlock one, WalkableBlock two) {
        current = one;
        int x0 = one.x();
        int y0 = one.z();
        int x1 = two.x();
        int y1 = two.z();
        int dy = y1 - y0;
        int dx = x1 - x0;
        int sx;
        int sy;
        int f = 0;
        if (dy < 0) {
            dy = -dy;
            sy = -1;
        } else {
            sy = 1;
        }
        if (dx < 0) {
            dx = -dx;
            sx = -1;
        } else {
            sx = 1;
        }
        if (dx > dy) {
            while (x0 != x1) {
                f += dy;
                if (f > dx) {
                    if (isBlocked(x0, y0, sx, sy)) {
                        return false;
                    }
                    y0 += sy;
                    f -= dx;
                }
                if (f != 0 && isBlocked(x0, y0, sx, sy)) {
                    return false;
                }
                if (dy == 0 && isBlocked(x0, y0, sx, 1)) {
                    return false;
                }
                x0 += sx;
            }
        } else {
            while (y0 != y1) {
                f += dx;
                if (f > dy) {
                    if (isBlocked(x0, y0, sx, sy)) {
                        return false;
                    }
                    x0 += sx;
                    f -= dy;
                }
                if (f != 0 && isBlocked(x0, y0, sx, sy)) {
                    return false;
                }
                if (dx == 0 && isBlocked(x0, y0, 1, sy)) {
                    return false;
                }
                y0 += sy;
            }
        }
        return true;
    }

    private boolean isBlocked(int x0, int y0, int sx, int sy) {
        int x = x0 + ((sx - 1) / 2);
        int z = y0 + ((sy - 1) / 2);
        if (current.getBlockPosition().x == x && current.getBlockPosition().z == z) {
            return false;
        }
        for (WalkableBlock neighbor : current.neighbors) {
            if (neighbor != null && neighbor.x() == x && neighbor.z() == z) {
                current = neighbor;
                return false;
            }
        }
        return true;
    }
}
