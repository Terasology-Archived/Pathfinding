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
package org.terasology.logic.behavior.ui;

import com.google.common.collect.Lists;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.behavior.BehaviorNodeComponent;
import org.terasology.logic.behavior.BehaviorTree;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

/**
 * @author synopia
 */
@RegisterSystem
public class BehaviorUiSystem implements ComponentSystem {
    @In
    private EntityManager entityManager;
    private BehaviorTree behaviorTree;

    private BTreeMain main;
    private boolean first;

    @Override
    public void initialise() {
        final Collection<BehaviorNodeComponent> items = Lists.newArrayList();
        Collection<Prefab> prefabs = CoreRegistry.get(PrefabManager.class).listPrefabs(BehaviorNodeComponent.class);
        for (Prefab prefab : prefabs) {
            EntityRef entityRef = entityManager.create(prefab);
            items.add(entityRef.getComponent(BehaviorNodeComponent.class));
        }
        behaviorTree = new BehaviorTree();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                main = new BTreeMain(behaviorTree, items);

                main.setPreferredSize(new Dimension(500, 400));

                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.add(main);
                frame.pack();
                frame.setVisible(true);
            }
        });
        first = true;
    }

    @Override
    public void shutdown() {

    }
}
