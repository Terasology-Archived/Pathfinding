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
package org.terasology.minion.behavior;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.minion.behavior.tree.BehaviorTree;

/**
 * @author synopia
 */
@RegisterSystem
public class BehaviorSystem implements ComponentSystem, UpdateSubscriberSystem {
    @In
    private EntityManager entityManager;
    private BehaviorFactory behaviorFactory;

    @Override
    public void initialise() {
        behaviorFactory = new BehaviorFactory();
    }

    @Override
    public void update(float delta) {
        for (EntityRef minion : entityManager.getEntitiesWith(BehaviorComponent.class)) {
            BehaviorComponent behaviorComponent = minion.getComponent(BehaviorComponent.class);
            BehaviorTree<EntityRef> tree = behaviorComponent.behaviorTree;
            if (tree == null) {
                tree = new BehaviorTree<>(minion);
                behaviorFactory.create(tree, behaviorComponent.behavior);
                behaviorComponent.behaviorTree = tree;
                minion.saveComponent(behaviorComponent);
            }
            tree.tick(delta);
        }
    }

    @Override
    public void shutdown() {

    }
}
