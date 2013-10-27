package org.terasology.minion;

import org.terasology.entitySystem.event.Event;
import org.terasology.math.Vector3i;

/**
 * @author synopia
 */
public class MovingPathFinishedEvent implements Event {
    private final int pathId;
    private final Vector3i target;

    public MovingPathFinishedEvent(int pathId, Vector3i target) {
        this.pathId = pathId;
        this.target = target;
    }

    public Vector3i getTarget() {
        return target;
    }

    public int getPathId() {
        return pathId;
    }
}
