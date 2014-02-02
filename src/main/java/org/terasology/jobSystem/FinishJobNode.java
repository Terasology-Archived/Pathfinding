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
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;

/**
 * Work at a job. If actor is in range of currently assigned job, the child node is started.<br/>
 * <br/>
 * <b>SUCCESS</b>: when job is done (depends on job type).<br/>
 * <b>FAILURE</b>: if no job is assigned or job is not reachable.<br/>
 * <br/>
 * Auto generated javadoc - modify README.markdown instead!
 */
public class FinishJobNode extends DecoratorNode {
    @Override
    public Task createTask() {
        return new FinishJobTask(this);
    }

    public static class FinishJobTask extends DecoratorTask {
        private static final Logger logger = LoggerFactory.getLogger(FinishJobTask.class);
        private Job job;
        private EntityRef currentJob;

        public FinishJobTask(Node node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            JobMinionComponent actorJob = actor().component(JobMinionComponent.class);
            currentJob = actorJob.currentJob;
            if (currentJob == null) {
                return;
            }
            JobTargetComponent jobTargetComponent = currentJob.getComponent(JobTargetComponent.class);
            if (jobTargetComponent == null) {
                return;
            }
            job = jobTargetComponent.getJob();
            if (!job.canMinionWork(currentJob, actor().minion())) {
                logger.info("Not in range, job aborted " + currentJob);
                jobTargetComponent.assignedMinion = null;
                currentJob.saveComponent(jobTargetComponent);
                actorJob.currentJob = null;
                actor().save(actorJob);
                job = null;
                return;
            }

            logger.info("Reached job " + currentJob);
            start(getNode().child);
        }

        @Override
        public Status update(float dt) {
            if (job != null) {
                boolean result = job.letMinionWork(currentJob, actor().minion(), dt);
                if (result) {
                    return Status.RUNNING;
                } else {
                    logger.info("Job finished");
                    JobMinionComponent actorJob = actor().component(JobMinionComponent.class);
                    actorJob.currentJob = null;
                    actor().save(actorJob);
                    return Status.SUCCESS;
                }
            }
            return Status.FAILURE;
        }

        @Override
        public void handle(Status result) {
            if (result == Status.FAILURE) {
                stop(result);
            }
        }

        @Override
        public FinishJobNode getNode() {
            return (FinishJobNode) super.getNode();
        }
    }
}
