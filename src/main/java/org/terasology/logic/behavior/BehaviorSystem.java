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
package org.terasology.logic.behavior;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.behavior.tree.Actor;
import org.terasology.logic.behavior.tree.Interpreter;
import org.terasology.logic.behavior.tree.Node;

/**
 * @author synopia
 */
@RegisterSystem
public class BehaviorSystem implements ComponentSystem, UpdateSubscriberSystem {
    @In
    private EntityManager entityManager;

    private BehaviorTreeFactory behaviorTreeFactory;

    private float speed;

    @Override
    public void initialise() {
        behaviorTreeFactory = new BehaviorTreeFactory();
    }

    @Override
    public void update(float delta) {
        if (speed > 0) {
            speed -= delta;
            return;
        }
        speed = 0.1f;
        for (EntityRef minion : entityManager.getEntitiesWith(BehaviorComponent.class)) {
            BehaviorComponent behaviorComponent = minion.getComponent(BehaviorComponent.class);
            Interpreter interpreter = behaviorComponent.interpreter;
            if (interpreter == null) {
                interpreter = new Interpreter(new Actor(minion));
                BehaviorTree tree = new BehaviorTree();
                Node node = behaviorTreeFactory.get(behaviorComponent.behavior);
                tree.addNode(node);

                behaviorComponent.interpreter = interpreter;
                behaviorComponent.tree = tree;
                interpreter.start(node);
                minion.saveComponent(behaviorComponent);
            }
            interpreter.tick(delta);
        }
    }

    @Override
    public void shutdown() {

    }
}
