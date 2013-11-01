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
package org.terasology.pathfinding.componentSystem;

import org.terasology.entitySystem.event.Event;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.WalkableBlock;

/**
 * @author synopia
 */
public class PathReadyEvent implements Event {
    private final int pathId;
    private final WalkableBlock[] start;
    private final WalkableBlock target;
    private final Path[] path;

    public PathReadyEvent(int pathId, Path[] path, WalkableBlock target, WalkableBlock[] start) {
        this.pathId = pathId;
        this.path = path;
        this.target = target;
        this.start = start;
    }

    public int getPathId() {
        return pathId;
    }

    public WalkableBlock[] getStart() {
        return start;
    }

    public WalkableBlock getTarget() {
        return target;
    }

    public Path[] getPath() {
        return path;
    }
}
