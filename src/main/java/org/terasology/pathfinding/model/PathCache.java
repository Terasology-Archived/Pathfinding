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

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;

/**
 * @author synopia
 */
public class PathCache {
    private Map<WalkableBlock, Map<WalkableBlock, Path>> paths = Maps.newHashMap();

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
