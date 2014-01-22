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
package org.terasology.minion.move;

import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.TeraMath;

import javax.vecmath.Vector3f;

/**
 * @author synopia
 */
public class MoveToNode extends Node {
    @Override
    public MoveToTask createTask() {
        return new MoveToTask(this);
    }

    public static class MoveToTask extends Task {
        public MoveToTask(MoveToNode node) {
            super(node);
        }

        @Override
        public Status update(float dt) {
            Status status = Status.FAILURE;
            MinionMoveComponent moveComponent = actor().component(MinionMoveComponent.class);
            if (moveComponent != null && moveComponent.target != null) {
                status = setMovement(moveComponent.target);
            }
            return status;
        }

        private Status setMovement(Vector3f currentTarget) {
            Status result;
            LocationComponent location = actor().location();
            Vector3f worldPos = new Vector3f(location.getWorldPosition());
            Vector3f targetDirection = new Vector3f();
            targetDirection.sub(currentTarget, worldPos);
            Vector3f drive = new Vector3f();
            boolean jump = currentTarget.y - worldPos.y > 0.5f;
            float yaw = (float) Math.atan2(targetDirection.x, targetDirection.z);

            if (targetDirection.x * targetDirection.x + targetDirection.z * targetDirection.z > 0.01f) {
                drive.set(targetDirection);
                result = Status.RUNNING;
            } else {
                drive.set(0, 0, 0);
                result = Status.SUCCESS;
            }

            float requestedYaw = 180f + yaw * TeraMath.RAD_TO_DEG;

            CharacterMoveInputEvent wantedInput = new CharacterMoveInputEvent(0, 0, requestedYaw, drive, false, jump);

            CharacterMovementComponent characterMovement = actor().minion().getComponent(CharacterMovementComponent.class);

            CharacterMoveInputEvent adjustedInput = calculateMovementInput(location, characterMovement, wantedInput, currentTarget);

            actor().minion().send(adjustedInput);

            return result;
        }

        protected CharacterMoveInputEvent calculateMovementInput(LocationComponent location,
                                                                 CharacterMovementComponent movementComp, CharacterMoveInputEvent input,
                                                                 Vector3f currentTarget) {

            Vector3f moveDelta = input.getMovementDirection();
            float delta = input.getDelta();

            Vector3f term1 = new Vector3f(moveDelta);
            moveDelta.scale(1f / delta);

            Vector3f term2 = new Vector3f(term1);
            term2.sub(movementComp.getVelocity());

            Vector3f term3 = new Vector3f(term2);
            term3.scale(1f / Math.min(movementComp.groundFriction * delta, 1f));

            Vector3f term4 = new Vector3f(term3);
            term4.add(movementComp.getVelocity());

            Vector3f term5 = new Vector3f(term4);
            term5.scale(1f / movementComp.maxGroundSpeed);

            Vector3f desiredVelocity = term5;


            // Does not account for runFactor -- we are not currently supporting running
            // Does not account for removing y component while maintaining speed -- we are not currently setting a non-zero y component
            // Does not account for gravity's effect on the Y axis. -- this shouldn't affect horizontal travel.
            // Does not account for anything the physics engine might be doing

            CharacterMoveInputEvent newInput = new CharacterMoveInputEvent(input.getSequenceNumber(),
                    input.getPitch(), input.getYaw(), desiredVelocity, input.isRunning(), input.isJumpRequested());

            return newInput;
        }

        @Override
        public MoveToNode getNode() {
            return (MoveToNode) super.getNode();
        }
    }

}
