package org.terasology.jobSystem;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.jobSystem.jobs.WalkOnBlock;
import org.terasology.math.Vector3i;
import org.terasology.world.block.ForceBlockActive;

import java.util.ArrayList;
import java.util.List;

/**
 * Job's block component. Using this component, jobs can be assigned to individual blocks
 *
 * @author synopia
 */
@ForceBlockActive
public class JobBlockComponent implements Component {
    public enum JobBlockState {
        UNASSIGNED,
        PATHS_REQUESTED,
        ASSIGNED
    }
    public transient EntityRef assignedMinion;

    public transient Job job = new WalkOnBlock();
    public transient JobBlockState state = JobBlockComponent.JobBlockState.UNASSIGNED;

    public JobBlockComponent() {
    }

    public JobBlockComponent(Job job) {
        this.job = job;
    }

    public List<Vector3i> getTargetPositions( EntityRef block ) {
        return job.getTargetPositions(block);
    }
}
