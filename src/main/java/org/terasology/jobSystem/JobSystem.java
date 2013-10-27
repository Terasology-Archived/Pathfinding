package org.terasology.jobSystem;

import com.google.common.collect.Lists;
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
import org.terasology.jobSystem.jobs.WalkOnBlock;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.minion.MovingPathFinishedEvent;
import org.terasology.minion.MinionPathComponent;
import org.terasology.pathfinding.componentSystem.PathReadyEvent;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.WalkableBlock;
import org.terasology.selection.ApplyBlockSelectionEvent;
import org.terasology.world.BlockEntityRegistry;

import javax.vecmath.Vector3f;
import java.util.*;

/**
 * @author synopia
 */
@RegisterSystem
public class JobSystem implements ComponentSystem, UpdateSubscriberSystem {
    private class JobCandidate {
        public EntityRef minion;
        public EntityRef block;
        public Path path;
    }
    private static final Logger logger = LoggerFactory.getLogger(JobSystem.class);

    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private PathfinderSystem pathfinderSystem;
    @In
    private EntityManager entityManager;

    private Job walkJob = new WalkOnBlock();
    private List<Integer> pathRequests = Lists.newArrayList();
    private List<EntityRef> unassignedEntities;
    private Vector3f[] startPositions;
    private List<JobCandidate> candidates = Lists.newArrayList();

    @Override
    public void update(float delta) {
        if( pathRequests.size()>0 ) {
            return;
        }

        if( candidates.size()>0 ) {
            Collections.sort(candidates, new Comparator<JobCandidate>() {
                @Override
                public int compare(JobCandidate o1, JobCandidate o2) {
                    return Integer.compare(o1.path.size(), o2.path.size());
                }
            });
            for (EntityRef entity : unassignedEntities) {
                for (JobCandidate candidate : candidates) {
                    if( candidate.minion==entity ) {
                        candidates.remove(candidate);
                        assignJob(candidate);
                        break;
                    }
                }
            }
            for (JobCandidate candidate : candidates) {
                JobBlockComponent job = candidate.block.getComponent(JobBlockComponent.class);
                if( job.state!= JobBlockComponent.JobBlockState.UNASSIGNED ) {
                    job.state = JobBlockComponent.JobBlockState.UNASSIGNED;
                    candidate.block.saveComponent(job);
                }
            }
            candidates.clear();
            return;
        }

        findUnassignedMinions();

        if( unassignedEntities.size()==0 ) {
            return;
        }

        for (EntityRef entity : entityManager.getEntitiesWith(JobBlockComponent.class)) {
            JobBlockComponent jobBlock = entity.getComponent(JobBlockComponent.class);
            if( jobBlock.state== JobBlockComponent.JobBlockState.UNASSIGNED ) {
                jobBlock.state = JobBlockComponent.JobBlockState.PATHS_REQUESTED;
                entity.saveComponent(jobBlock);

                List<Vector3i> targetPositions = jobBlock.getTargetPositions(entity);
                for (Vector3i targetPosition : targetPositions) {
                    int id = pathfinderSystem.requestPath(entity, targetPosition.toVector3f(), startPositions);
                    pathRequests.add(id);
                }
            }
        }
    }

    private void findUnassignedMinions() {
        unassignedEntities = Lists.newArrayList();
        List<Vector3f> startPos = Lists.newArrayList();
        for (EntityRef entity : entityManager.getEntitiesWith(JobMinionComponent.class, MinionPathComponent.class)) {
            JobMinionComponent job = entity.getComponent(JobMinionComponent.class);
            if( job.assigned==null ) {
                unassignedEntities.add(entity);
                startPos.add(entity.getComponent(LocationComponent.class).getWorldPosition());
            }
        }
        startPositions = startPos.toArray(new Vector3f[startPos.size()]);
    }

    @ReceiveEvent(components = {JobBlockComponent.class})
    public void onPathReady(final PathReadyEvent event, EntityRef block ) {
        Path[] path1 = event.getPath();
        for (int i = 0; i < path1.length; i++) {
            Path path = path1[i];
            JobCandidate candidate = new JobCandidate();
            candidate.path = path;
            candidate.block = block;
            candidate.minion = unassignedEntities.get(i);
            candidates.add(candidate);
        }
        pathRequests.remove((Object)event.getPathId());
    }

    private void assignJob(JobCandidate candidate) {
        logger.info("Assign job from " + candidate.minion.getComponent(LocationComponent.class).getWorldPosition() + " to " + candidate.path.get(0).getBlockPosition());
        JobBlockComponent jobBlock = candidate.block.getComponent(JobBlockComponent.class);
        jobBlock.assignedMinion = candidate.minion;
        jobBlock.state = JobBlockComponent.JobBlockState.ASSIGNED;
        candidate.block.saveComponent(jobBlock);

        JobMinionComponent job = candidate.minion.getComponent(JobMinionComponent.class);
        job.job = jobBlock.job;
        job.assigned = candidate.block;

        MinionPathComponent path = candidate.minion.getComponent(MinionPathComponent.class);
        path.pathState = MinionPathComponent.PathState.NEW_TARGET;
        path.targetBlock = candidate.path.get(0).getBlockPosition();
        candidate.minion.saveComponent(job);
        candidate.minion.saveComponent(path);
    }

    @ReceiveEvent(components = {JobMinionComponent.class})
    public void onMovingPathFinished(MovingPathFinishedEvent event, EntityRef minion) {
        logger.info("Finished moving along "+event.getPathId());
        JobMinionComponent job = minion.getComponent(JobMinionComponent.class);
        EntityRef block = job.assigned;
        job.assigned = null;
        job.job = null;
        JobBlockComponent jobBlock = block.getComponent(JobBlockComponent.class);
        jobBlock.assignedMinion = null;
        jobBlock.state= JobBlockComponent.JobBlockState.UNASSIGNED;
        block.saveComponent(jobBlock);
        minion.saveComponent(job);

        Vector3f worldPosition = minion.getComponent(LocationComponent.class).getWorldPosition();
        WalkableBlock actualBlock = pathfinderSystem.getBlock(worldPosition);
        if( actualBlock!=null ) {
            EntityRef actualBlockEntity = blockEntityRegistry.getExistingBlockEntityAt(actualBlock.getBlockPosition());
            if( actualBlockEntity==block ) {
                logger.info("Reached target, remove job");
                block.removeComponent(JobBlockComponent.class);
            }
        }
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
                        blockEntity.addComponent(new JobBlockComponent(walkJob));
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
