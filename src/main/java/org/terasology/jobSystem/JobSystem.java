/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.jobSystem;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Vector3i;
import org.terasology.minion.MinionPathComponent;
import org.terasology.minion.MovingPathFinishedEvent;
import org.terasology.pathfinding.componentSystem.PathReadyEvent;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.model.Path;

import javax.vecmath.Vector3f;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author synopia
 */
@RegisterSystem
public class JobSystem implements ComponentSystem, UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(JobSystem.class);

    @In
    private PathfinderSystem pathfinderSystem;
    @In
    private EntityManager entityManager;
    @In
    private JobBoard jobBoard;

    private List<Integer> pathRequests = Lists.newArrayList();
    private List<EntityRef> unassignedEntities;
    private Vector3f[] startPositions;
    private List<JobCandidate> candidates = Lists.newArrayList();

    @Override
    public void update(float delta) {
        if (pathRequests.size() > 0) {
            // wait for all requested paths
            return;
        }

        if (candidates.size() > 0) {
            assignJobs();
            return;
        }

        findUnassignedMinions();

        if (unassignedEntities.size() == 0) {
            return;
        }

        requestPaths();
    }

    @ReceiveEvent(components = {JobMinionComponent.class})
    public void onMovingPathFinished(MovingPathFinishedEvent event, EntityRef minion) {
        logger.info("Finished moving along " + event.getPathId());
        JobMinionComponent job = minion.getComponent(JobMinionComponent.class);
        EntityRef block = job.assigned;
        job.assigned = null;
        job.job = null;
        JobBlockComponent jobBlock = block.getComponent(JobBlockComponent.class);
        jobBlock.assignedMinion = null;
        jobBlock.state = JobBlockComponent.JobBlockState.UNASSIGNED;
        block.saveComponent(jobBlock);
        minion.saveComponent(job);

        if (jobBlock.jobType.canMinionWork(block, minion)) {
            logger.info("Reached target, remove job");
            jobBlock.jobType.letMinionWork(block, minion);
//            jobBoard.removeJob(block);
        }
    }

    @ReceiveEvent(components = {JobBlockComponent.class})
    public void onPathReady(final PathReadyEvent event, EntityRef block) {
        pathRequests.remove((Object) event.getPathId());
        Path[] path1 = event.getPath();
        if (path1 != null) {
            for (int i = 0; i < path1.length; i++) {
                Path path = path1[i];
                if (path == Path.INVALID) {
                    continue;
                }
                JobCandidate candidate = new JobCandidate();
                candidate.path = path;
                candidate.block = block;
                candidate.minion = unassignedEntities.get(i);
                candidates.add(candidate);
            }
        } else {
            JobBlockComponent jobBlock = block.getComponent(JobBlockComponent.class);
            jobBlock.assignedMinion = null;
            jobBlock.state = JobBlockComponent.JobBlockState.UNASSIGNED;
            block.saveComponent(jobBlock);
        }
    }

    private void assignJobs() {
        Collections.sort(candidates, new Comparator<JobCandidate>() {
            @Override
            public int compare(JobCandidate o1, JobCandidate o2) {
                return Integer.compare(o1.path.size(), o2.path.size());
            }
        });
        for (EntityRef entity : unassignedEntities) {
            JobCandidate job = null;
            for (JobCandidate candidate : candidates) {
                if (candidate.minion == entity) {
                    job = candidate;
                    break;
                }
            }
            if (job != null) {
                assignJob(job);

                Iterator<JobCandidate> it = candidates.iterator();
                while (it.hasNext()) {
                    JobCandidate next = it.next();
                    if (next.block == job.block) {
                        it.remove();
                    }
                }
            }
        }
        for (JobCandidate candidate : candidates) {
            JobBlockComponent job = candidate.block.getComponent(JobBlockComponent.class);
            if (job.state != JobBlockComponent.JobBlockState.UNASSIGNED) {
                job.state = JobBlockComponent.JobBlockState.UNASSIGNED;
                candidate.block.saveComponent(job);
            }
        }
        candidates.clear();
    }

    private void requestPaths() {
        for (EntityRef entity : entityManager.getEntitiesWith(JobBlockComponent.class)) {
            JobBlockComponent jobBlock = entity.getComponent(JobBlockComponent.class);
            if (jobBlock.state != JobBlockComponent.JobBlockState.ASSIGNED) {
                jobBlock.state = JobBlockComponent.JobBlockState.PATHS_REQUESTED;
                entity.saveComponent(jobBlock);

                List<Vector3i> targetPositions = jobBlock.jobType.getTargetPositions(entity);
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
            if (job.assigned == null) {
                Vector3f worldPosition = entity.getComponent(LocationComponent.class).getWorldPosition();
                if (pathfinderSystem.getBlock(worldPosition) != null) {
                    unassignedEntities.add(entity);
                    startPos.add(worldPosition);
                }
            }
        }
        startPositions = startPos.toArray(new Vector3f[startPos.size()]);
    }

    private void assignJob(JobCandidate candidate) {
        logger.info("Assign job from " + candidate.minion.getComponent(LocationComponent.class).getWorldPosition() + " to " + candidate.path.get(0).getBlockPosition());
        JobBlockComponent jobBlock = candidate.block.getComponent(JobBlockComponent.class);
        jobBlock.assignedMinion = candidate.minion;
        jobBlock.state = JobBlockComponent.JobBlockState.ASSIGNED;
        candidate.block.saveComponent(jobBlock);

        JobMinionComponent job = candidate.minion.getComponent(JobMinionComponent.class);
        job.job = jobBlock.jobType;
        job.assigned = candidate.block;

        MinionPathComponent path = candidate.minion.getComponent(MinionPathComponent.class);
        path.pathState = MinionPathComponent.PathState.NEW_TARGET;
        path.targetBlock = candidate.path.get(0).getBlockPosition();
        candidate.minion.saveComponent(job);
        candidate.minion.saveComponent(path);
    }


    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    private class JobCandidate {
        public EntityRef minion;
        public EntityRef block;
        public Path path;
    }
}
