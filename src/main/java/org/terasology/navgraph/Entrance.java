// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.navgraph;

import com.google.common.collect.Sets;
import org.terasology.engine.world.block.BlockArea;

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
    private final BlockArea area = new BlockArea(BlockArea.INVALID);
    private Type type;
    private final Floor floor;

    public Entrance(Floor floor) {
        this.floor = floor;
    }

    public boolean isPartOfEntrance(int x, int y) {
        if (!area.isValid()) {
            return true;
        }
        if (area.contains(x, y)) {
            return true;
        }
        int x1 = Math.min(area.minX(), x);
        int y1 = Math.min(area.minY(), y);
        int x2 = Math.max(area.maxX(), x);
        int y2 = Math.max(area.maxY(), y);

        if (type == Type.VERTICAL) {
            return y2 - y1 == 0 && area.getSizeX() + 1 == x2 - x1;
        } else if (type == Type.HORIZONTAL) {
            return x2 - x1 == 0 && area.getSizeY() + 1 == y2 - y1;
        } else {
            return x2 - x1 <= 1 && y2 - y1 <= 1;
        }
    }

    public void addToEntrance(int x, int y) {

        if (!area.contains(x, y)) {
            area.union(x, y);
            if (area.getSizeX() > area.getSizeY()) {
                type = Type.VERTICAL;
            } else if (area.getSizeX() < area.getSizeY()) {
                type = Type.HORIZONTAL;
            }
        }
    }

    public WalkableBlock getAbstractBlock() {
        int mx = (area.minX() + area.getSizeX()) / 2;
        int my = (area.minY() + area.getSizeY()) / 2;

        return floor.getBlock(mx, my);
    }
}
