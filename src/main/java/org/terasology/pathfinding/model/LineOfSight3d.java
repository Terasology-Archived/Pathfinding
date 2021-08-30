// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.pathfinding.model;

import org.joml.Vector3i;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.navgraph.WalkableBlock;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;

/**
 * Created by synopia on 01.02.14.
 */
@RegisterSystem
public class LineOfSight3d extends BaseComponentSystem implements LineOfSight {
    @In
    private WorldProvider world;

    @Override
    public void initialise() {

    }

    @Override
    public void shutdown() {

    }

    public boolean inSight(WalkableBlock one, WalkableBlock two) {
        int x0 = one.x();
        int y0 = one.height();
        int z0 = one.z();
        int x1 = two.x();
        int y1 = two.height();
        int z1 = two.z();
        int dx = x1 - x0;
        int dy = y1 - y0;
        int dz = z1 - z0;
        int sx;
        int sy;
        int sz;
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
        if (dz < 0) {
            dz = -dz;
            sz = -1;
        } else {
            sz = 1;
        }
        if (dx > Math.max(dy, dz)) {
            int yd = 0;
            int zd = 0;
            while (x0 != x1) {
                // x dominant
                yd += dy;
                zd += dz;

                // walk along y
                if (yd > dx) {
                    if (isBlocked(x0, sx, y0, sy, z0, sz)) {
                        return false;
                    }
                    y0 += sy;
                    yd -= dx;
                }
                if (yd != 0 && isBlocked(x0, sx, y0, sy, z0, sz)) {
                    return false;
                }
                if (dy == 0 && isBlocked(x0, sx, y0, 1, z0, sz) && isBlocked(x0, sx, y0 - 1, 1, z0, sz)) {
                    return false;
                }

                // walk along z
                if (zd > dx) {
                    if (isBlocked(x0, sx, y0, sy, z0, sz)) {
                        return false;
                    }
                    z0 += sz;
                    zd -= dx;
                }
                if (zd != 0 && isBlocked(x0, sx, y0, sy, z0, sz)) {
                    return false;
                }
                if (dz == 0 && isBlocked(x0, sx, y0, sy, z0, 1) && isBlocked(x0, sx, y0, sy, z0 - 1, 1)) {
                    return false;
                }
                x0 += sx;
            }
        } else if (dy > Math.max(dx, dz)) {
            int xd = 0;
            int zd = 0;
            while (y0 != y1) {
                // y dominant
                xd += dx;
                zd += dz;

                // walk along x
                if (xd > dy) {
                    if (isBlocked(x0, sx, y0, sy, z0, sz)) {
                        return false;
                    }
                    x0 += sx;
                    xd -= dy;
                }
                if (xd != 0 && isBlocked(x0, sx, y0, sy, z0, sz)) {
                    return false;
                }
                if (dx == 0 && isBlocked(x0, 1, y0, sy, z0, sz) && isBlocked(x0 - 1, 1, y0, sy, z0, sz)) {
                    return false;
                }

                // walk along z
                if (zd > dy) {
                    if (isBlocked(x0, sx, y0, sy, z0, sz)) {
                        return false;
                    }
                    z0 += sz;
                    zd -= dy;
                }
                if (zd != 0 && isBlocked(x0, sx, y0, sy, z0, sz)) {
                    return false;
                }
                if (dz == 0 && isBlocked(x0, sx, y0, sy, z0, 1) && isBlocked(x0, sx, y0, sy, z0 - 1, 1)) {
                    return false;
                }
                y0 += sy;
            }
        } else {
            int xd = 0;
            int yd = 0;
            while (z0 != z1) {
                // z dominant
                xd += dx;
                yd += dy;

                // walk along x
                if (xd > dz) {
                    if (isBlocked(x0, sx, y0, sy, z0, sz)) {
                        return false;
                    }
                    x0 += sx;
                    xd -= dz;
                }
                if (xd != 0 && isBlocked(x0, sx, y0, sy, z0, sz)) {
                    return false;
                }
                if (dx == 0 && isBlocked(x0, 1, y0, sy, z0, sz) && isBlocked(x0 - 1, 1, y0, sy, z0, sz)) {
                    return false;
                }

                // walk along y
                if (yd > dz) {
                    if (isBlocked(x0, sx, y0, sy, z0, sz)) {
                        return false;
                    }
                    y0 += sy;
                    yd -= dz;
                }
                if (yd != 0 && isBlocked(x0, sx, y0, sy, z0, sz)) {
                    return false;
                }
                if (dy == 0 && isBlocked(x0, sx, y0, 1, z0, sz) && isBlocked(x0, sx, y0 - 1, 1, z0, sz)) {
                    return false;
                }
                z0 += sz;
            }
        }
        return true;
    }

    private boolean isBlocked(int x, int sx, int y, int sy, int z, int sz) {
        int x0 = x + ((sx - 1) / 2);
        int y0 = y + ((sy - 1) / 2);
        int z0 = z + ((sz - 1) / 2);
        Block block = world.getBlock(new Vector3i(x0, y0 + 1, z0));
        return block != null && !block.isPenetrable();
    }
}
