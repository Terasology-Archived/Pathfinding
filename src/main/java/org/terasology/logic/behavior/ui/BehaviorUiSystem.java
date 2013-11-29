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
import com.google.common.collect.Maps;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.behavior.BehaviorNodeComponent;
import org.terasology.logic.behavior.BehaviorNodeFactory;
import org.terasology.logic.behavior.BehaviorSystem;
import org.terasology.logic.behavior.BehaviorTreeFactory;
import org.terasology.logic.behavior.RenderableBehaviorTree;
import org.terasology.logic.behavior.tree.BehaviorTree;
import org.terasology.logic.behavior.tree.Interpreter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author synopia
 */
@RegisterSystem
public class BehaviorUiSystem implements ComponentSystem {
    @In
    private EntityManager entityManager;

    private BTreeMain main;
    private BehaviorNodeFactory nodeFactory;
    private JComboBox<RenderableBehaviorTree> trees;
    private JComboBox<Interpreter> interpreters;
    private Map<BehaviorTree, RenderableBehaviorTree> behaviorTrees = Maps.newHashMap();

    @Override
    public void initialise() {
        final BehaviorTreeFactory factory = new BehaviorTreeFactory();
        List<BehaviorNodeComponent> items = Lists.newArrayList();
        Collection<Prefab> prefabs = CoreRegistry.get(PrefabManager.class).listPrefabs(BehaviorNodeComponent.class);
        for (Prefab prefab : prefabs) {
            EntityRef entityRef = entityManager.create(prefab);
            items.add(entityRef.getComponent(BehaviorNodeComponent.class));
        }
        nodeFactory = new BehaviorNodeFactory(items);

        trees = new JComboBox<>();
        interpreters = new JComboBox<>();

        trees.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED && main.getTree() == null) {
                    interpreters.removeAllItems();
                    RenderableBehaviorTree tree = (RenderableBehaviorTree) e.getItem();
                    List<Interpreter> interpreter = CoreRegistry.get(BehaviorSystem.class).getInterpreter(tree.getBehaviorTree());
                    for (Interpreter i : interpreter) {
                        interpreters.addItem(i);
                    }
                    main.setTree(tree);
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    main.setTree(null);
                }
            }
        });

        interpreters.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    main.setInterpreter((Interpreter) e.getItem());
                }
            }
        });

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                main = new BTreeMain(nodeFactory);
                main.setPreferredSize(new Dimension(500, 400));

                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());

                JPanel p = new JPanel();
                p.add(trees);
                p.add(interpreters);
                frame.add(p, BorderLayout.NORTH);
                frame.add(main, BorderLayout.CENTER);
                frame.pack();
                frame.setVisible(true);
            }
        });

        new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Set<BehaviorTree> newTrees = CoreRegistry.get(BehaviorSystem.class).getTrees();
                for (BehaviorTree newTree : newTrees) {
                    RenderableBehaviorTree renderableBehaviorTree = behaviorTrees.get(newTree);
                    if (renderableBehaviorTree == null) {
                        renderableBehaviorTree = new RenderableBehaviorTree(newTree, nodeFactory);
                        behaviorTrees.put(newTree, renderableBehaviorTree);
                        trees.addItem(renderableBehaviorTree);
                    }
                }
            }
        }).start();
    }


    @Override
    public void shutdown() {

    }
}
