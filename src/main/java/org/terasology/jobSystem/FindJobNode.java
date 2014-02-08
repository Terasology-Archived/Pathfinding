/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.jobSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.behavior.tree.DecoratorNode;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;
import org.terasology.minion.move.MinionMoveComponent;
import org.terasology.navgraph.WalkableBlock;
import org.terasology.registry.In;
import org.terasology.rendering.nui.properties.OneOf;

import java.util.List;

/**
 * <b>Properties</b>: <b>filter</b><br/>
 * <br/>
 * Searches for an open job of specific type (<b>filter</b>). If a job is found, the actor is assigned to that job and child is started.<br/>
 * <br/>
 * <b>SUCCESS</b>: when actor reached a target position.<br/>
 * <b>FAILURE</b>: if no open job can be found.<br/>
 * <br/>
 * Auto generated javadoc - modify README.markdown instead!
 */
public class FindJobNode extends DecoratorNode {
    @OneOf.Provider(name = "jobs")
    private String filter;

    @Override
    public Task createTask() {
        return new FindJobTask(this);
    }

    public static class FindJobTask extends DecoratorTask {
        private static final Logger logger = LoggerFactory.getLogger(FindJobTask.class);

        private EntityRef assignedJob;
        @In
        private JobBoard jobBoard;
        @In
        private JobFactory jobFactory;
        private boolean firstTick = true;

        public FindJobTask(FindJobNode node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            JobMinionComponent actorJob = actor().component(JobMinionComponent.class);
            if (actorJob.currentJob != null) {
                JobTargetComponent currentJob = actorJob.currentJob.getComponent(JobTargetComponent.class);
                if (currentJob != null) {
                    logger.info("Removing current job from actor " + currentJob.getUri() + " at " + actorJob.currentJob);
                    currentJob.assignedMinion = null;
                    actorJob.currentJob.saveComponent(currentJob);
                }
            }
            EntityRef job = jobBoard.getJob(actor().minion(), getNode().filter != null ? jobFactory.getJob(getNode().filter) : null);
            if (job != null) {
                JobTargetComponent jobTargetComponent = job.getComponent(JobTargetComponent.class);
                logger.info("Found new job for " + interpreter().toString() + " " + jobTargetComponent.getUri() + " at " + job);
                jobTargetComponent.assignedMinion = actor().minion();
                job.saveComponent(jobTargetComponent);
                assignedJob = job;
            } else {
                assignedJob = null;
            }
            actorJob.currentJob = assignedJob;
            actor().save(actorJob);
            if (assignedJob != null) {
                start(getNode().child);
            }
        }

        @Override
        public Status update(float dt) {
            if (firstTick) {
                firstTick = false;
                return Status.RUNNING;
            }
            if (assignedJob != null) {
                JobTargetComponent jobTargetComponent = assignedJob.getComponent(JobTargetComponent.class);
                List<WalkableBlock> targetPositions = jobTargetComponent.getTargetPositions(assignedJob);
                MinionMoveComponent moveComponent = actor().component(MinionMoveComponent.class);
                if (moveComponent != null) {
                    WalkableBlock currentBlock = moveComponent.currentBlock;
                    for (WalkableBlock targetPosition : targetPositions) {
                        if (currentBlock == targetPosition) {
                            return Status.SUCCESS;
                        }
                    }
                }
                return Status.RUNNING;
            }
            return Status.FAILURE;
        }

        @Override
        public void handle(Status result) {
            stop(result);
        }

        @Override
        public FindJobNode getNode() {
            return (FindJobNode) super.getNode();
        }
    }
}
