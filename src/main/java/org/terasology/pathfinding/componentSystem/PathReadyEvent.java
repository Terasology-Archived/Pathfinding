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
