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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.behavior.tree.Actor;
import org.terasology.logic.behavior.tree.BehaviorTree;
import org.terasology.logic.behavior.tree.Interpreter;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Behavior tree system
 * <p/>
 * Each entity with BehaviorComponent is kept under control by this system. For each such entity a behavior tree
 * is loaded and an interpreter is started.
 * <p/>
 * Modifications made to a behavior tree will reflect to all entities using this tree.
 *
 * @author synopia
 */
@RegisterSystem
public class BehaviorSystem implements ComponentSystem, UpdateSubscriberSystem {
    @In
    private EntityManager entityManager;

    private BehaviorTreeFactory behaviorTreeFactory;
    private Map<BehaviorTree, List<Interpreter>> interpreters = Maps.newHashMap();

    private float speed;

    @Override
    public void initialise() {
        CoreRegistry.put(BehaviorSystem.class, this);
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
                BehaviorTree tree = behaviorTreeFactory.get(behaviorComponent.behavior);

                behaviorComponent.interpreter = interpreter;
                behaviorComponent.tree = tree;
                interpreter.setTree(tree);
                minion.saveComponent(behaviorComponent);
                List<Interpreter> list = interpreters.get(tree);
                if (list == null) {
                    list = Lists.newArrayList();
                    interpreters.put(tree, list);
                }
                list.add(interpreter);
            }
            interpreter.tick(delta);
        }
    }

    public Set<BehaviorTree> getTrees() {
        return interpreters.keySet();
    }

    public List<Interpreter> getInterpreter(BehaviorTree tree) {
        return interpreters.get(tree);
    }

    @Override
    public void shutdown() {

    }
}
