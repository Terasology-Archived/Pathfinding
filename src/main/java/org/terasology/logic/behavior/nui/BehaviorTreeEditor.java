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
package org.terasology.logic.behavior.nui;

import com.google.common.collect.Lists;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.logic.behavior.BehaviorNodeComponent;
import org.terasology.logic.behavior.BehaviorNodeFactory;
import org.terasology.logic.behavior.BehaviorTreeFactory;
import org.terasology.logic.behavior.RenderableBehaviorTree;
import org.terasology.logic.behavior.tree.BehaviorTree;
import org.terasology.rendering.nui.UIScreen;
import org.terasology.rendering.nui.baseWidgets.UIButton;
import org.terasology.rendering.nui.baseWidgets.UIDropdown;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.layout.MigLayout;
import org.terasology.rendering.nui.layout.ZoomableLayout;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author synopia
 */
public class BehaviorTreeEditor extends UIScreen {

    public BehaviorTreeEditor() {
        BehaviorTree tree;
        BehaviorTreeFactory treeFactory = new BehaviorTreeFactory();
        tree = treeFactory.get("");

        List<BehaviorNodeComponent> items = Lists.newArrayList();
        Collection<Prefab> prefabs = CoreRegistry.get(PrefabManager.class).listPrefabs(BehaviorNodeComponent.class);
        for (Prefab prefab : prefabs) {
            EntityRef entityRef = CoreRegistry.get(EntityManager.class).create(prefab);
            items.add(entityRef.getComponent(BehaviorNodeComponent.class));
        }
        BehaviorNodeFactory nodeFactory = new BehaviorNodeFactory(items);
        RenderableBehaviorTree renderableBehaviorTree = new RenderableBehaviorTree(tree, nodeFactory);
        tree.setEditable(true);
        ZoomableLayout layout = new ZoomableLayout();
        layout.init(0, 0, 50, 50, 1200, 1000);
        for (RenderableNode renderableNode : renderableBehaviorTree.getRenderableNodes()) {
            layout.addWidget(renderableNode);
        }
        MigLayout treeSelectBar = new MigLayout();
        treeSelectBar.addElement(new UIDropdown<String>("tree"), "w 180!, h 40!");
        treeSelectBar.addElement(new UIDropdown<String>("entity"), "w 180!, h 40!");
        MigLayout debugTools = new MigLayout();
        debugTools.addElement(new UIButton("stop", "[]"), "w 40!, h 40!");
        debugTools.addElement(new UIButton("pause", "||"), "w 40!, h 40!");
        debugTools.addElement(new UIButton("step", "|>"), "w 40!, h 40!");
        debugTools.addElement(new UIButton("continue", ">>"), "w 40!, h 40!");
        MigLayout palette = new MigLayout();
        palette.addElement(new UIButton("selector", "Selector"), "w 150!, h 40!, wrap");
        palette.addElement(new UIButton("sequence", "Sequence"), "w 150!, h 40!, wrap");
        MigLayout migLayout = new MigLayout("", "[min][grow][min]", "");
        migLayout.addElement(treeSelectBar, "cell 0 0 3");
        migLayout.addElement(debugTools, "cell 0 1 3");
        migLayout.addElement(palette, "cell 0 2, top");
        migLayout.addElement(layout, "cell 1 2, w 500!, h 500!");
        setContents(migLayout);

        find("tree", UIDropdown.class).bindOptions(new Binding<List<String>>() {
            @Override
            public List<String> get() {
                return Arrays.asList("x", "y");
            }

            @Override
            public void set(List<String> value) {
            }
        });
    }
}
