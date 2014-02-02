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
import org.terasology.registry.In;
import org.terasology.rendering.nui.properties.OneOf;

/**
 * <strong>FindJob</strong>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
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

        private boolean jobAssigned;
        @In
        private JobBoard jobBoard;
        @In
        private JobFactory jobFactory;

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
                logger.info("Found job entity for " + interpreter().toString() + " " + job);
                JobTargetComponent jobTargetComponent = job.getComponent(JobTargetComponent.class);
                logger.info(" found new job " + jobTargetComponent.getUri() + " at " + job);
                jobTargetComponent.assignedMinion = actor().minion();
                job.saveComponent(jobTargetComponent);

                actorJob.currentJob = job;
                actor().save(actorJob);
                jobAssigned = true;

                start(getNode().child);
            } else {
                actorJob.currentJob = null;
                actor().save(actorJob);
                jobAssigned = false;
            }
        }

        @Override
        public Status update(float dt) {
            return jobAssigned ? Status.RUNNING : Status.FAILURE;
        }

        @Override
        public void handle(Status result) {
            logger.info("FindJob.handle for " + interpreter().toString());

            JobMinionComponent actorJob = actor().component(JobMinionComponent.class);

            if (actorJob.currentJob != null) {
                JobTargetComponent currentJob = actorJob.currentJob.getComponent(JobTargetComponent.class);
                if (currentJob != null) {
                    logger.info(" finished job " + currentJob.getUri() + " at " + currentJob);
                    currentJob.assignedMinion = null;
                }
                if (result == Status.SUCCESS) {
                    actorJob.currentJob.removeComponent(JobTargetComponent.class);
                }
            }
            actorJob.currentJob = null;
            actor().save(actorJob);

            stop(result);
            logger.info("finished with " + result);
        }

        @Override
        public FindJobNode getNode() {
            return (FindJobNode) super.getNode();
        }
    }
}
