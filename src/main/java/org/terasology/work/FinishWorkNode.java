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
package org.terasology.work;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.behavior.tree.DecoratorNode;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;

/**
 * Does the actual work, once the actor is in range. The child node is started.<br/>
 * <br/>
 * <b>SUCCESS</b>: when work is done (depends on work type).<br/>
 * <b>FAILURE</b>: if no work is assigned or target is not reachable.<br/>
 * <br/>
 * Auto generated javadoc - modify README.markdown instead!
 */
public class FinishWorkNode extends DecoratorNode {
    @Override
    public Task createTask() {
        return new FinishWorkTask(this);
    }

    public static class FinishWorkTask extends DecoratorTask {
        private static final Logger logger = LoggerFactory.getLogger(FinishWorkTask.class);
        private Work work;
        private EntityRef currentJob;

        public FinishWorkTask(Node node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            MinionWorkComponent actorJob = actor().getComponent(MinionWorkComponent.class);
            currentJob = actorJob.currentWork;
            if (currentJob == null) {
                return;
            }
            WorkTargetComponent jobTargetComponent = currentJob.getComponent(WorkTargetComponent.class);
            if (jobTargetComponent == null) {
                return;
            }
            work = jobTargetComponent.getWork();
            if (!work.canMinionWork(currentJob, actor().getEntity())) {
                logger.info("Not in range, work aborted " + currentJob);
                jobTargetComponent.assignedMinion = null;
                currentJob.saveComponent(jobTargetComponent);
                actorJob.currentWork = null;
                actor().save(actorJob);
                work = null;
                return;
            }
            actorJob.cooldown = work.cooldownTime();
            actor().save(actorJob);
            logger.info("Reached work " + currentJob);
            work.letMinionWork(currentJob, actor().getEntity());
            start(getNode().child);
        }

        @Override
        public Status update(float dt) {
            if (work != null) {
                MinionWorkComponent actorJob = actor().getComponent(MinionWorkComponent.class);
                actorJob.cooldown -= dt;
                actor().save(actorJob);

                if (actorJob.cooldown > 0) {
                    return Status.RUNNING;
                } else {
                    logger.info("Work finished");
                    actorJob.currentWork = null;
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
        public FinishWorkNode getNode() {
            return (FinishWorkNode) super.getNode();
        }
    }
}
