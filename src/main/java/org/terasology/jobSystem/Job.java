package org.terasology.jobSystem;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Vector3i;

import java.util.List;

/**
 * Defines a job on a block.
 *
 * @author synopia
 */
public interface Job {
    /**
     * Returns list of positions that are valid to work on this job.
     */
    List<Vector3i> getTargetPositions( EntityRef block );
}
