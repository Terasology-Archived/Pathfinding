package org.terasology.minion;

import org.terasology.entitySystem.Component;
import org.terasology.math.Vector3i;

/**
 * @author synopia
 */
public class MinionMoveComponent implements Component {
    /**
     * if set to a value other then null, this minion is requested to move to this position
     * once reached, targetBlock is set to null
     */
    public transient Vector3i targetBlock;

    public transient float firstRunTime=1f;

}
