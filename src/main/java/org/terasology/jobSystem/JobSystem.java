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
import org.terasology.math.Vector3i;
import org.terasology.minion.path.MinionPathComponent;
import org.terasology.minion.path.MoveToEvent;
import org.terasology.minion.path.MovingPathAbortedEvent;
import org.terasology.minion.path.MovingPathFinishedEvent;
import org.terasology.pathfinding.componentSystem.PathReadyEvent;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.WalkableBlock;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Assigns free jobs to minions
 *
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

    private List<JobPossibility> jobPossibilities = Lists.newArrayList();
    private Set<Integer> pendingRequests = Sets.newHashSet();

    @Override
    public void update(float delta) {
        requestPaths();
        if (pendingRequests.size() == 0 && jobPossibilities.size() > 0) {
            assign();
        }
    }

    @ReceiveEvent(components = {JobMinionComponent.class, MinionPathComponent.class})
    public void onMovingPathAborted(MovingPathAbortedEvent event, EntityRef minion) {
        logger.info("Moving path aborted " + minion);
        JobMinionComponent job = minion.getComponent(JobMinionComponent.class);
        JobPossibility chosenPossibility = job.chosenPossibility;
        job.chosenPossibility = null;
        job.state = JobMinionComponent.JobMinionState.UNASSIGNED;
        minion.saveComponent(job);

        if (chosenPossibility != null) {
            EntityRef block = chosenPossibility.targetEntity;
            if (block != null) {
                JobBlockComponent jobBlock = block.getComponent(JobBlockComponent.class);
                jobBlock.assignedMinion = null;
                block.saveComponent(jobBlock);
            }
        }
    }

    @ReceiveEvent(components = {JobMinionComponent.class})
    public void onMovingPathFinished(MovingPathFinishedEvent event, EntityRef minion) {
        logger.info("Finished moving along " + event.getPathId());
        JobMinionComponent job = minion.getComponent(JobMinionComponent.class);
        JobPossibility chosenPossibility = job.chosenPossibility;
        job.chosenPossibility = null;
        job.state = JobMinionComponent.JobMinionState.UNASSIGNED;
        minion.saveComponent(job);

        EntityRef block = chosenPossibility.targetEntity;
        if (block != null) {
            JobBlockComponent jobBlock = block.getComponent(JobBlockComponent.class);
            jobBlock.assignedMinion = null;
            block.saveComponent(jobBlock);
            if (jobBlock.canMinionWork(block, minion)) {
                logger.info("Reached target, remove job");
                jobBlock.letMinionWork(block, minion);

                jobBoard.removeJob(block);
            } else {
                logger.error(minion + "cannot work at " + block);
            }
        } else {
            if (chosenPossibility.job != null) {
                chosenPossibility.job.letMinionWork(null, minion);
            }
        }
    }

    @ReceiveEvent(components = {JobMinionComponent.class})
    public void onPathReady(final PathReadyEvent event, EntityRef minion) {
        if (!pendingRequests.contains(event.getPathId())) {
            return;
        }
        pendingRequests.remove(event.getPathId());
        JobMinionComponent minionComponent = minion.getComponent(JobMinionComponent.class);

        List<Path> allPaths = event.getPath();
        List<JobPossibility> possibilities = minionComponent.possibilities;
        if (allPaths != null) {
            for (int i = 0; i < allPaths.size(); i++) {
                JobPossibility possibility = possibilities.get(i);
                possibility.path = allPaths.get(i);
                if (possibility.path != Path.INVALID && possibility.path.size() > 0) {
                    jobPossibilities.add(possibility);
                }
            }
            minionComponent.state = JobMinionComponent.JobMinionState.WAITING_SCHEDULE;
        } else {
            minionComponent.state = JobMinionComponent.JobMinionState.UNASSIGNED;
        }
        minion.saveComponent(minionComponent);
    }

    private void assign() {
        Collections.sort(jobPossibilities, new Comparator<JobPossibility>() {
            @Override
            public int compare(JobPossibility o1, JobPossibility o2) {
                Vector3i s1 = o1.path.getStart().getBlockPosition();
                Vector3i e1 = o2.path.getTarget().getBlockPosition();
                int diff1 = Math.abs(s1.y - e1.y);
                Vector3i s2 = o1.path.getStart().getBlockPosition();
                Vector3i e2 = o2.path.getTarget().getBlockPosition();
                int diff2 = Math.abs(s2.y - e2.y);
                return Integer.compare(o1.path.size() + diff1 * 30, o2.path.size() + diff2 * 30);
            }
        });
        while (!jobPossibilities.isEmpty()) {
            JobPossibility job = jobPossibilities.remove(0);
            assign(job);
            Iterator<JobPossibility> it = jobPossibilities.iterator();
            while (it.hasNext()) {
                JobPossibility next = it.next();
                if (next.minion == job.minion || (next.targetEntity != null && next.targetEntity == job.targetEntity)) {
                    it.remove();
                }
            }
        }
        for (EntityRef minion : entityManager.getEntitiesWith(JobMinionComponent.class)) {
            JobMinionComponent job = minion.getComponent(JobMinionComponent.class);
            if (job.state == JobMinionComponent.JobMinionState.WAITING_SCHEDULE) {
                job.state = JobMinionComponent.JobMinionState.UNASSIGNED;
                minion.saveComponent(job);
            }
        }
    }

    private void assign(JobPossibility possibility) {
        JobMinionComponent minionComponent = possibility.minion.getComponent(JobMinionComponent.class);
        logger.info("Path assigned to " + possibility.minion);

        minionComponent.state = JobMinionComponent.JobMinionState.ASSIGNED;
        minionComponent.chosenPossibility = possibility;
        possibility.minion.saveComponent(minionComponent);
        if (possibility.targetEntity != null) {
            JobBlockComponent blockComponent = possibility.targetEntity.getComponent(JobBlockComponent.class);
            blockComponent.assignedMinion = possibility.minion;
            possibility.targetEntity.saveComponent(blockComponent);
        }

        possibility.minion.send(new MoveToEvent(possibility.path.getTarget().getBlockPosition()));

    }

    private void requestPaths() {
        boolean refreshed = false;
        for (EntityRef entity : entityManager.getEntitiesWith(JobMinionComponent.class, MinionPathComponent.class)) {
            JobMinionComponent job = entity.getComponent(JobMinionComponent.class);

            if (job.state == JobMinionComponent.JobMinionState.UNASSIGNED) {
                if (!refreshed) {
                    refreshed = true;
                    jobBoard.refresh();
                }
                WalkableBlock block = pathfinderSystem.getBlock(entity);
                if (block != null) {
                    List<JobPossibility> possibilities = jobBoard.findJobTargets(entity);

                    if (possibilities.size() > 0) {
                        job.state = JobMinionComponent.JobMinionState.PATHS_REQUESTED;
                        job.possibilities = possibilities;
                        entity.saveComponent(job);

                        logger.info("Requesting " + possibilities.size() + " paths for " + entity);
                        List<Vector3i> targetPositions = Lists.newArrayList();
                        for (JobPossibility possibility : possibilities) {
                            targetPositions.add(possibility.targetPos);
                        }
                        pendingRequests.add(pathfinderSystem.requestPath(entity, block.getBlockPosition(), targetPositions));
                    }
                }
            }
        }
    }

    @Override
    public void initialise() {
        logger.info("Initialize JobSystem");
    }

    @Override
    public void shutdown() {
    }
}
