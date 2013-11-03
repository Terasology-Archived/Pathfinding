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

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.model.WalkableBlock;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.logic.SkeletalMeshComponent;

import javax.vecmath.Vector3f;

/**
 * @author synopia
 */
@RegisterSystem
public class MinionMoveSystem implements ComponentSystem, UpdateSubscriberSystem {
    public static final float COOLDOWN = 0.1f;

    @In
    private EntityManager entityManager;
    @In
    private PathfinderSystem pathfinderSystem;

    private float timeRemain = COOLDOWN;

    @Override
    public void initialise() {

    }

    @Override
    public void update(float delta) {
        timeRemain -= delta;
        if (timeRemain > 0) {
            return;
        }
        timeRemain = COOLDOWN;
        for (EntityRef entity : entityManager.getEntitiesWith(MinionMoveComponent.class, CharacterMovementComponent.class, LocationComponent.class)) {
            MinionMoveComponent move = entity.getComponent(MinionMoveComponent.class);
            AnimationComponent animation = entity.getComponent(AnimationComponent.class);
            WalkableBlock currentBlock = pathfinderSystem.getBlock(entity);

            if (move.firstRunTime > 0 || currentBlock == null) {
                entity.send(new CharacterMoveInputEvent(0, 0, 0, new Vector3f(0f, 0f, 0f), false, true));
                move.firstRunTime -= timeRemain;
            } else {
                if (move.currentBlock == null) {
                    move.currentBlock = currentBlock;
                    entity.send(new ReachedWalkableBlockEvent(move.currentBlock, null));
                }
                entity.saveComponent(move);

                if (move.targetBlock != null) {

                    if (move.currentBlock != currentBlock) {
                        WalkableBlock old = move.currentBlock;
                        move.currentBlock = currentBlock;
                        entity.saveComponent(move);
                        entity.send(new ReachedWalkableBlockEvent(currentBlock, old));
                    }
                    WalkableBlock target = pathfinderSystem.getBlock(move.targetBlock);
                    if (target != null) {
                        Vector3i targetBlockPosition = target.getBlockPosition();
                        Vector3f targetBlock = new Vector3f(targetBlockPosition.x, targetBlockPosition.y + 1.2f, targetBlockPosition.z);
                        if (setMovement(targetBlock, entity)) {
                            move.targetBlock = null;
                            entity.saveComponent(move);
                            changeAnimation(entity, animation.idleAnim, true);
                            entity.send(new MovingFinishedEvent(currentBlock));
                        } else {
                            changeAnimation(entity, animation.walkAnim, true);
                        }
                    } else {
                        entity.send(new CannotReachEvent(move.targetBlock));
                    }
                }
            }
        }
    }

    private void changeAnimation(EntityRef entity, MeshAnimation animation,
                                 boolean loop) {
        SkeletalMeshComponent skeletalMesh = entity.getComponent(SkeletalMeshComponent.class);
        if (skeletalMesh.animation != animation) {
            skeletalMesh.animation = animation;
            skeletalMesh.loop = loop;
            entity.saveComponent(skeletalMesh);
        }
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
    public void shutdown() {

    }
}
