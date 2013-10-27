package org.terasology.jobs;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Vector3i;
import org.terasology.network.Replicate;
import org.terasology.world.block.ForceBlockActive;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
 * @author synopia
 */
@ForceBlockActive
public class JobBlockComponent implements Component {
    @Replicate
    public int id;

    public EntityRef assignedMinion;
    public boolean requested;

    private transient List<Vector3i> targetPositions;


    public JobBlockComponent() {
    }

    public JobBlockComponent(int id) {
        this.id = id;
    }

    public List<Vector3i> getTargetPositions( EntityRef block ) {
        if( targetPositions==null) {
            Vector3f position = block.getComponent(LocationComponent.class).getWorldPosition();
            targetPositions = Lists.newArrayList();
            targetPositions.add(new Vector3i(position.x, position.y, position.z));
        }
        return targetPositions;
    }
}
