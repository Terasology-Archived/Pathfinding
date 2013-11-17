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
package org.terasology.logic.behavior.tree;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.jobSystem.JobMinionComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.minion.move.AnimationComponent;
import org.terasology.minion.move.MinionMoveComponent;
import org.terasology.minion.path.MinionPathComponent;
import org.terasology.rendering.logic.SkeletalMeshComponent;

/**
 * @author synopia
 */
public class Actor {
    private final EntityRef minion;

    public Actor(EntityRef minion) {
        this.minion = minion;
    }

    public MinionMoveComponent move() {
        MinionMoveComponent moveComponent = minion.getComponent(MinionMoveComponent.class);
        if (moveComponent == null) {
            moveComponent = new MinionMoveComponent();
            minion.addComponent(moveComponent);
        }
        return moveComponent;
    }

    public MinionPathComponent path() {
        MinionPathComponent pathComponent = minion.getComponent(MinionPathComponent.class);
        if (pathComponent == null) {
            pathComponent = new MinionPathComponent();
            minion.addComponent(pathComponent);
        }
        return pathComponent;
    }

    public JobMinionComponent job() {
        JobMinionComponent jobMinionComponent = minion.getComponent(JobMinionComponent.class);
        if (jobMinionComponent == null) {
            jobMinionComponent = new JobMinionComponent();
            minion.addComponent(jobMinionComponent);
        }
        return jobMinionComponent;
    }

    public AnimationComponent animation() {
        return minion.getComponent(AnimationComponent.class);
    }

    public SkeletalMeshComponent skeletalMesh() {
        return minion.getComponent(SkeletalMeshComponent.class);
    }

    public LocationComponent location() {
        return minion.getComponent(LocationComponent.class);
    }

    public void save(Component component) {
        minion.saveComponent(component);
    }

    public EntityRef minion() {
        return minion;
    }
}
