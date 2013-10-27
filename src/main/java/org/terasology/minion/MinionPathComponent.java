package org.terasology.minion;

import org.terasology.entitySystem.Component;
import org.terasology.math.Vector3i;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.model.Path;

/**
 * @author synopia
 */
public class MinionPathComponent implements Component {
    /**
     * if set to a value other then null, this minion is requested to move to this position
     * once reached, targetBlock is set to null
     */
    public Vector3i targetBlock;

    public enum PathState {
        IDLE,
        NEW_TARGET,
        PATH_REQUESTED,
        MOVING_PATH,
        FINISHED_MOVING
    }
    public transient Path path = null;
    public transient int pathId = -1;
    public transient int pathStep = 0;
    public transient PathState pathState = PathState.IDLE;
}
