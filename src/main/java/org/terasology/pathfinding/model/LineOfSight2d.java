// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.pathfinding.model;

import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.navgraph.WalkableBlock;

/**
 * Created by synopia on 01.02.14.
 */
@RegisterSystem
public class LineOfSight2d extends BaseComponentSystem implements LineOfSight {
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
