// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.pathfinding.model;

import org.terasology.navgraph.WalkableBlock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author synopia
 */
public class Path implements Iterable<WalkableBlock>{
    public static final Path INVALID = new Path();
    private ArrayList<WalkableBlock> nodes = new ArrayList<>();

    public Iterator<WalkableBlock> iterator() {
        return this.nodes.iterator();
    }

    public void remove(int index) {
        nodes.remove(index);
    }

    public void addAll(List<WalkableBlock> nodeGroup) {
        nodes.addAll(nodeGroup);
    }

    public void add(WalkableBlock node) {
        nodes.add(node);
    }

    public int size() {
        return nodes.size();
    }

    public ArrayList<WalkableBlock> getNodes() {
        return nodes;
    }

    public WalkableBlock get(int index) {
        return nodes.get(index);
    }

    public WalkableBlock getTarget() {
        if (size() == 0) {
            return null;
        }
        return get(size() - 1);
    }

    public WalkableBlock getStart() {
        if (size() == 0) {
            return null;
        }
        return get(0);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (WalkableBlock block : this.nodes) {
            sb.append(block.getBlockPosition().toString());
            sb.append("->");
        }
        return sb.toString();
    }
}
