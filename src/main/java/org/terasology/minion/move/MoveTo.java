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

import org.terasology.behavior.tree.Behavior;
import org.terasology.behavior.tree.BehaviorTree;
import org.terasology.behavior.tree.Node;
import org.terasology.behavior.tree.Status;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.TeraMath;

import javax.vecmath.Vector3f;

/**
 * @author synopia
 */
public class MoveTo extends Behavior<EntityRef> {
    public MoveTo(MoveToNode node) {
        super(node);
    }

    @Override
    public Status update(EntityRef entity, float dt) {
        MinionMoveComponent moveComponent = entity.getComponent(MinionMoveComponent.class);
        if (moveComponent != null && moveComponent.target != null) {
            if (setMovement(moveComponent.target, entity)) {
                return Status.SUCCESS;
            } else {
                return Status.RUNNING;
            }
        }

        return Status.SUCCESS;
    }

    private boolean setMovement(Vector3f currentTarget, EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        Vector3f worldPos = new Vector3f(location.getWorldPosition());

        Vector3f targetDirection = new Vector3f();
        targetDirection.sub(currentTarget, worldPos);
        boolean finished;
        Vector3f drive = new Vector3f();
        boolean jump = currentTarget.y - worldPos.y > 0.5f;
        float yaw = 0;
        if (targetDirection.x * targetDirection.x + targetDirection.z * targetDirection.z > 0.01f) {

            drive.set(targetDirection);

            yaw = (float) Math.atan2(targetDirection.x, targetDirection.z);
            finished = false;
        } else {
            drive.set(0, 0, 0);
            finished = true;
        }
        entity.send(new CharacterMoveInputEvent(0, 0, 180 + yaw * TeraMath.RAD_TO_DEG, drive, false, jump));

        return finished;
    }

    @Override
    public MoveToNode getNode() {
        return (MoveToNode) super.getNode();
    }

    public static class MoveToNode implements Node<EntityRef> {
        @Override
        public Behavior<EntityRef> create(BehaviorTree<EntityRef> tree) {
            return new MoveTo(this);
        }
    }
}
