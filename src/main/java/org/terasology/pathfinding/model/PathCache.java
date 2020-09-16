// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.pathfinding.model;

import com.google.common.collect.Maps;
import org.terasology.pathfinding.navgraph.WalkableBlock;

import java.util.HashMap;
import java.util.Map;

/**
 * @author synopia
 */
public class PathCache {
    private final Map<WalkableBlock, Map<WalkableBlock, Path>> paths = Maps.newHashMap();

    public Path getCachedPath(WalkableBlock from, WalkableBlock to) {
        Map<WalkableBlock, Path> fromMap = paths.get(from);
        if (fromMap != null) {
            return fromMap.get(to);
        }
        return null;
    }

    private void insert(WalkableBlock from, WalkableBlock to, Path path) {
        Map<WalkableBlock, Path> fromMap = paths.get(from);
        if (fromMap == null) {
            fromMap = new HashMap<>();
            paths.put(from, fromMap);
        }
        fromMap.put(to, path);
    }

    public boolean hasPath(WalkableBlock from, WalkableBlock to) {
        return getCachedPath(from, to) != null;
    }

    public Path findPath(WalkableBlock from, WalkableBlock to, Callback callback) {
        Path path = getCachedPath(from, to);
        if (path == null) {
            path = callback.run(from, to);
            insert(from, to, path);
//        insert(to, from, paths);
        }
        return path;
    }

    public void clear() {
        for (Map.Entry<WalkableBlock, Map<WalkableBlock, Path>> entry : paths.entrySet()) {
            entry.getValue().clear();
        }
        paths.clear();
    }

    public interface Callback {
        Path run(WalkableBlock from, WalkableBlock to);
    }
}
