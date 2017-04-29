/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.minion.move;

import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.navgraph.NavGraphSystem;
import org.terasology.navgraph.WalkableBlock;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.components.NPCMovementComponent;
import org.terasology.pathfinding.model.Path;
import org.terasology.registry.In;

import java.util.Arrays;
import java.util.List;

public class GoToNode extends Node {

    @Override
    public GoToTask createTask() {
        return new GoToTask(this);
    }

    public static class GoToTask extends Task {
        private volatile Path nextPath;
        private Path path;
        private int currentIndex;
        private boolean calculatingPath;

        @In
        private NavGraphSystem navGraphSystem;
        @In
        private PathfinderSystem pathfinderSystem;

        public GoToTask(GoToNode node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            path = null;
            nextPath = null;
            currentIndex = 0;
            calculatingPath = false;
        }

        @Override
        public Status update(float dt) {
            Status status = updateWithoutFailureHandling();
            if (status == Status.FAILURE) {
                NPCMovementComponent moveComponent = actor().getComponent(NPCMovementComponent.class);
                moveComponent.targetPosition = null;
                actor().save(moveComponent);
            }
            return status;
        }

        private Status updateWithoutFailureHandling() {
            MinionMoveComponent minionMoveComponent = actor().getComponent(MinionMoveComponent.class);
            Vector3f targetPosition = minionMoveComponent.target;
            WalkableBlock targetBlock = pathfinderSystem.getBlock(targetPosition);
            if (targetBlock == null) {
                return Status.FAILURE;
            }

            if (nextPath != null) {
                calculatingPath = false;
                if (nextPath == Path.INVALID) {
                    return Status.FAILURE;
                }
                path = nextPath;
                nextPath = null;
                currentIndex = 0;
                setTargetBasedOnPathIndex();
            }

            if ((path == null || !path.getTarget().getBlockPosition().equals(targetBlock.getBlockPosition()))
                    && !calculatingPath) {
                WalkableBlock currentBlock = minionMoveComponent.currentBlock;
                if (currentBlock == null) {
                    return Status.FAILURE;
                }
                requestNextPath(targetBlock, currentBlock);
                calculatingPath = true;
            }
            if (path == null) {
                return Status.RUNNING;
            }
            if (currentIndex < path.size() && atSubTarget()) {
                currentIndex++;
                setTargetBasedOnPathIndex();
            }
            if (currentIndex < path.size()) {
                return Status.RUNNING;
            } else {
                return Status.SUCCESS;
            }
        }

        private void requestNextPath(WalkableBlock targetBlock, WalkableBlock currentBlock) {
            pathfinderSystem.requestPath(
                    actor().getEntity(), currentBlock.getBlockPosition(),
                    Arrays.asList(targetBlock.getBlockPosition()), new PathfinderSystem.PathReadyCallback() {
                        @Override
                        public void pathReady(int pathId, List<Path> path, WalkableBlock target, List<WalkableBlock> start) {

                            if (path == null) {
                                nextPath = Path.INVALID;
                            } else if (path.size() > 0) {
                                nextPath = path.get(0);
                            }
                        }
                    });
        }

        private boolean atSubTarget() {
            LocationComponent location = actor().getComponent(LocationComponent.class);
            Vector3f worldPos = new Vector3f(location.getWorldPosition());
            Vector3f targetDelta = new Vector3f();
            targetDelta.sub(getSubTarget(), worldPos);
            float minDistance = 0.1f;
            return (targetDelta.x * targetDelta.x + targetDelta.z * targetDelta.z < minDistance * minDistance);
        }

        private void setTargetBasedOnPathIndex() {
            NPCMovementComponent moveComponent = actor().getComponent(NPCMovementComponent.class);
            if (currentIndex < path.size()) {
                moveComponent.targetPosition = getSubTarget();
                LocationComponent location = actor().getComponent(LocationComponent.class);
                CharacterMoveInputEvent inputEvent = null;
                Vector3f worldPos = new Vector3f(location.getWorldPosition());
                Vector3f targetDirection = new Vector3f();
                targetDirection.sub(moveComponent.targetPosition, worldPos);
                float yaw = (float) Math.atan2(targetDirection.x, targetDirection.z);
                moveComponent.yaw = 180f + yaw * TeraMath.RAD_TO_DEG;
            } else {
                moveComponent.targetPosition = null;
            }
            actor().save(moveComponent);
        }

        /**
         * Calculates the rotation around y axis according to a formula from
         * https://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles
         */
        private float calculateYAxisRotation(Quat4f q) {
            return (float) Math.asin(2 * (q.getX() * q.getZ() + q.getW() * q.getY()));
        }

        private Vector3f getSubTarget() {
            WalkableBlock subTargetBlock = path.get(currentIndex);
            Vector3f subTargetPosition = subTargetBlock.getBlockPosition().toVector3f();
            subTargetPosition.add(new Vector3f(0, 1, 0));
            return subTargetPosition;
        }

        @Override
        public GoToNode getNode() {
            return (GoToNode) super.getNode();
        }

        @Override
        public void handle(Status result) {

        }

        @Override
        public void onTerminate(Status result) {
            NPCMovementComponent moveComponent = actor().getComponent(NPCMovementComponent.class);
            moveComponent.targetPosition = null;
            actor().save(moveComponent);
        }
    }


}
