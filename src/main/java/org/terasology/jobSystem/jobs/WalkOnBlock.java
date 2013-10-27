package org.terasology.jobSystem.jobs;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.jobSystem.Job;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Vector3i;

import javax.vecmath.Vector3f;
import java.util.List;

/**
 * @author synopia
 */
public class WalkOnBlock implements Job {
    public List<Vector3i> getTargetPositions( EntityRef block ) {
        List<Vector3i> targetPositions = Lists.newArrayList();
        Vector3f position = block.getComponent(LocationComponent.class).getWorldPosition();
        targetPositions.add(new Vector3i(position.x, position.y, position.z));
        return targetPositions;
    }
}
