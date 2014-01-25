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

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.behavior.tree.DecoratorNode;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;

/**
 * @author synopia
 */
public class FindJobNode extends DecoratorNode {
    @Override
    public Task createTask() {
        return new FindJobTask(this);
    }

    public static class FindJobTask extends DecoratorTask {
        private boolean jobAssigned;

        public FindJobTask(FindJobNode node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            JobMinionComponent actorJob = actor().component(JobMinionComponent.class);
            if (actorJob.currentJob != null) {
                JobBlockComponent currentJob = actorJob.currentJob.getComponent(JobBlockComponent.class);
                if (currentJob != null) {
                    currentJob.assignedMinion = null;
                    actorJob.currentJob.saveComponent(currentJob);
                }
            }
            CoreRegistry.get(JobBoard.class).refresh();
            EntityRef job = CoreRegistry.get(JobBoard.class).getJob(actor().minion());
            if (job != null) {
                JobBlockComponent jobBlockComponent = job.getComponent(JobBlockComponent.class);
                jobBlockComponent.assignedMinion = actor().minion();
                job.saveComponent(jobBlockComponent);

                actorJob.currentJob = job;
                actor().save(actorJob);
                jobAssigned = true;

                interpreter().start(getNode().child, this);
            } else {
                actorJob.currentJob = null;
                actor().save(actorJob);
                jobAssigned = false;
            }
        }

        @Override
        public Status update(float dt) {
            return jobAssigned ? Status.RUNNING : Status.SUCCESS;
        }

        @Override
        public void handle(Status result) {
            JobMinionComponent actorJob = actor().component(JobMinionComponent.class);

            if (actorJob.currentJob != null) {
                JobBlockComponent currentJob = actorJob.currentJob.getComponent(JobBlockComponent.class);
                if (currentJob != null) {
                    currentJob.assignedMinion = null;
                }
                if (result == Status.SUCCESS) {
                    actorJob.currentJob.removeComponent(JobBlockComponent.class);
                }
            }
            actorJob.currentJob = null;
            actor().save(actorJob);

            interpreter().stop(this, result);
        }

        @Override
        public FindJobNode getNode() {
            return (FindJobNode) super.getNode();
        }
    }
}
