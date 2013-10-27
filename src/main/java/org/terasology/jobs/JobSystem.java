package org.terasology.jobs;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.minion.MovingPathFinishedEvent;
import org.terasology.minion.MinionPathComponent;
import org.terasology.pathfinding.componentSystem.PathReadyEvent;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.model.WalkableBlock;
import org.terasology.selection.ApplyBlockSelectionEvent;
import org.terasology.world.BlockEntityRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author synopia
 */
@RegisterSystem
public class JobSystem implements ComponentSystem, UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(JobSystem.class);

    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private PathfinderSystem pathfinderSystem;
    @In
    private EntityManager entityManager;

    private HashMap<Integer, EntityRef> pathRequests = Maps.newHashMap();

    @Override
    public void update(float delta) {
        Set<EntityRef> unassignedEntities = Sets.newHashSet();
        for (EntityRef entity : entityManager.getEntitiesWith(JobComponent.class, MinionPathComponent.class)) {
            JobComponent job = entity.getComponent(JobComponent.class);
            if( job.assigned==null ) {
                unassignedEntities.add(entity);
            }
        }

        for (EntityRef entity : entityManager.getEntitiesWith(JobBlockComponent.class)) {
            JobBlockComponent jobBlock = entity.getComponent(JobBlockComponent.class);
            if( jobBlock.assignedMinion==null && !jobBlock.requested ) {
                jobBlock.requested = true;
                entity.saveComponent(jobBlock);

                List<Vector3i> targetPositions = jobBlock.getTargetPositions(entity);
                for (Vector3i targetPosition : targetPositions) {
                    for (EntityRef candidate : unassignedEntities) {
                        LocationComponent location = candidate.getComponent(LocationComponent.class);
                        int id = pathfinderSystem.requestPath(entity, location.getWorldPosition(), targetPosition.toVector3f());
                        pathRequests.put(id, candidate);
                    }
                }
            }
        }
    }

    @ReceiveEvent(components = {JobComponent.class})
    public void onMovingPathFinished(MovingPathFinishedEvent event, EntityRef minion) {
        logger.info("Finished moving along "+event.getPathId());
        JobComponent job = minion.getComponent(JobComponent.class);
        EntityRef block = job.assigned;
        job.assigned = null;
        minion.saveComponent(job);

        JobBlockComponent jobBlock = block.getComponent(JobBlockComponent.class);
        jobBlock.assignedMinion = null;
        block.saveComponent(jobBlock);
    }

    @ReceiveEvent(components = {JobBlockComponent.class})
    public void onPathReady(PathReadyEvent event, EntityRef block ) {

        JobBlockComponent jobBlock = block.getComponent(JobBlockComponent.class);
        EntityRef minion = pathRequests.get(event.getPathId());
        jobBlock.assignedMinion = minion;
        block.saveComponent(jobBlock);
        logger.info("Received path "+event.getPathId()+" from "+minion.getComponent(LocationComponent.class).getWorldPosition()+" to "+event.getTarget().getBlockPosition());
        JobComponent job = minion.getComponent(JobComponent.class);
        job.assigned = block;
        MinionPathComponent path = minion.getComponent(MinionPathComponent.class);
        path.pathState = MinionPathComponent.PathState.NEW_TARGET;
        path.targetBlock = event.getTarget().getBlockPosition();
        minion.saveComponent(job);
        minion.saveComponent(path);
    }

    @ReceiveEvent(components = {LocationComponent.class, CharacterComponent.class})
    public void onSelectionChanged(ApplyBlockSelectionEvent event, EntityRef entity) {
//        event.getSelectedItemEntity();
        Region3i selection = event.getSelection();
        Vector3i size = selection.size();
        Vector3i block = new Vector3i();
        for (int z = 0; z < size.z; z++) {
            for (int y = 0; y < size.y; y++) {
                for (int x = 0; x < size.x; x++) {
                    block.set(x,y,z);
                    block.add(selection.min());
                    WalkableBlock walkableBlock = pathfinderSystem.getBlock(block);
                    if( walkableBlock!=null ) {
                        EntityRef blockEntity = blockEntityRegistry.getEntityAt(block);
                        JobBlockComponent job = new JobBlockComponent(1);
                        blockEntity.addComponent(job);
                    }
                }
            }
        }
    }

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }
}
