package org.terasology.jobs;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Vector3i;

/**
 * @author synopia
 */
public class JobComponent implements Component {
    public transient EntityRef assigned;

}
