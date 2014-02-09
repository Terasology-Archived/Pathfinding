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
 * Searches for open work of specific type (<b>filter</b>). If work is found, the actor is assigned to that and the child is started.<br/>
 * <br/>
 * <b>SUCCESS</b>: when actor reached a target position.<br/>
 * <b>FAILURE</b>: if no open work can be found.<br/>
 * <br/>
 * Auto generated javadoc - modify README.markdown instead!
 */
public class FindWorkNode extends DecoratorNode {
    @OneOf.Provider(name = "work")
    private String filter;

    @Override
    public Task createTask() {
        return new FindWorkTask(this);
    }

    public static class FindWorkTask extends DecoratorTask {
        private static final Logger logger = LoggerFactory.getLogger(FindWorkTask.class);

        private EntityRef assignedWork;
        @In
        private WorkBoard workBoard;
        @In
        private WorkFactory workFactory;
        private boolean firstTick = true;

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
            EntityRef work = workBoard.getWork(actor().minion(), getNode().filter != null ? workFactory.getWork(getNode().filter) : null);
            assignedWork = null;
            if (work != null) {
                WorkTargetComponent workTargetComponent = work.getComponent(WorkTargetComponent.class);
                if (workTargetComponent != null && workTargetComponent.getWork() != null) {
                    workBoard.removeRequestable(work);
                    logger.info("Found new work for " + interpreter().toString() + " " + workTargetComponent.getUri() + " at " + work);
                    workTargetComponent.assignedMinion = actor().minion();
                    work.saveComponent(workTargetComponent);
                    assignedWork = work;
                }
            }
            actorWork.currentWork = assignedWork;
            actor().save(actorWork);
            if (assignedWork != null) {
                start(getNode().child);
            }
        }

        @Override
        public Status update(float dt) {
            if (firstTick) {
                firstTick = false;
                return Status.RUNNING;
            }
            if (assignedWork != null && assignedWork.hasComponent(WorkTargetComponent.class)) {
                WorkTargetComponent jobTargetComponent = assignedWork.getComponent(WorkTargetComponent.class);
                List<WalkableBlock> targetPositions = jobTargetComponent.getTargetPositions(assignedWork);
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
        public FindWorkNode getNode() {
            return (FindWorkNode) super.getNode();
        }
    }
}
