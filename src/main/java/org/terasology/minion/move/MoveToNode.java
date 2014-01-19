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
        private Vector3f lastPos;

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
            if( lastPos!=null ) {
                lastPos.sub(worldPos);
                if( lastPos.lengthSquared()<0.1f ) {
                    // no movement
                    return Status.FAILURE;
                }
            }
            lastPos = new Vector3f(worldPos);

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
            actor().minion().send(new CharacterMoveInputEvent(0, 0, 180 + yaw * TeraMath.RAD_TO_DEG, drive, false, jump));

            return result;
        }

        @Override
        public MoveToNode getNode() {
            return (MoveToNode) super.getNode();
        }
    }
}
