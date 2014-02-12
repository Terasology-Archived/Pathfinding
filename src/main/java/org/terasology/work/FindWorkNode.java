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
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;
import org.terasology.math.Vector3i;
import org.terasology.registry.In;
import org.terasology.rendering.nui.properties.OneOf;
import org.terasology.work.kmeans.Cluster;

/**
 * <b>Properties</b>: <b>filter</b><br/>
 * <br/>
 * Searches for open work of specific type (<b>filter</b>). If work is found, the actor is assigned.<br/>
 * <br/>
 * <b>SUCCESS</b>: When work is found and assigned.<br/>
 * <br/>
 * Auto generated javadoc - modify README.markdown instead!
 */
public class FindWorkNode extends Node {
    @OneOf.Provider(name = "work")
    private String filter;

    @Override
    public Task createTask() {
        return new FindWorkTask(this);
    }

    public static class FindWorkTask extends Task {
        private static final Logger logger = LoggerFactory.getLogger(FindWorkTask.class);

        @In
        private WorkBoard workBoard;
        @In
        private WorkFactory workFactory;

        private EntityRef foundWork;
        private Vector3i foundPosition;
        private Work filter;
        private boolean workSearchDone;

        public FindWorkTask(FindWorkNode node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            MinionWorkComponent actorWork = actor().component(MinionWorkComponent.class);
            if (actorWork.currentWork != null) {
                WorkTargetComponent currentJob = actorWork.currentWork.getComponent(WorkTargetComponent.class);
                if (currentJob != null) {
                    logger.info("Removing current work from actor " + currentJob.getUri() + " at " + actorWork.currentWork);
                    currentJob.assignedMinion = null;
                    actorWork.currentWork.saveComponent(currentJob);
                }
            }
            filter = getNode().filter != null ? workFactory.getWork(getNode().filter) : null;
            if (filter != null) {
                workBoard.getWork(actor().minion(), filter, new WorkBoard.WorkBoardCallback() {
                    @Override
                    public boolean workReady(Cluster cluster, Vector3i position, EntityRef work) {
                        workSearchDone = true;
                        foundWork = work;
                        foundPosition = position;
                        return true;
                    }
                });
            } else {
                workSearchDone = true;
            }
        }

        @Override
        public Status update(float dt) {
            if (!workSearchDone) {
                return Status.RUNNING;
            }
            if (foundWork != null) {
                WorkTargetComponent workTargetComponent = foundWork.getComponent(WorkTargetComponent.class);
                if (workTargetComponent != null && workTargetComponent.getWork() != null) {
                    logger.info("Found new work for " + interpreter().toString() + " " + workTargetComponent.getUri() + " at " + foundWork);
                    workTargetComponent.assignedMinion = actor().minion();
                    foundWork.saveComponent(workTargetComponent);
                    MinionWorkComponent actorWork = actor().component(MinionWorkComponent.class);
                    actorWork.currentWork = foundWork;
                    actorWork.target = foundPosition;
                    actor().save(actorWork);
                    return Status.SUCCESS;
                }
            }
            return Status.FAILURE;
        }

        @Override
        public void handle(Status result) {
            stop(result);
        }

        @Override
        public FindWorkNode getNode() {
            return (FindWorkNode) super.getNode();
        }
    }
}
