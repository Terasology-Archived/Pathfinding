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

import org.terasology.logic.behavior.BehaviorNodeComponent;
import org.terasology.logic.behavior.BehaviorNodeFactory;
import org.terasology.logic.behavior.BehaviorTreeFactory;
import org.terasology.logic.behavior.RenderableBehaviorTree;
import org.terasology.logic.behavior.tree.Actor;
import org.terasology.logic.behavior.tree.Interpreter;
import org.terasology.logic.behavior.tree.RepeatNode;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;


/**
 * @author synopia
 */
public class BTreeMain extends JPanel {

    private BTreePanel panel;
    private Debugger debugger;
    private BehaviorNodeFactory factory;
    private final Palette palette;

    public BTreeMain(BehaviorNodeFactory factory) throws HeadlessException {
        this.factory = factory;
        setLayout(new BorderLayout());
        panel = new BTreePanel(factory);
        JToolBar bar = new JToolBar();
        debugger = new Debugger();
        bar.add(debugger);
        bar.add(panel.createToolBar());
        add(bar, BorderLayout.NORTH);
        palette = new Palette(factory);
        palette.setObserver(panel);
        add(palette, BorderLayout.WEST);
        add(panel, BorderLayout.CENTER);
        add(panel.getProperties(), BorderLayout.EAST);
        panel.init();
    }

    public void setTree(RenderableBehaviorTree tree) {
        panel.setTree(tree);
    }

    public RenderableBehaviorTree getTree() {
        return panel.getTree();
    }

    public void setInterpreter(Interpreter interpreter) {
        panel.setInterpreter(interpreter);
        debugger.setInterpreter(interpreter);
    }

    private static BehaviorNodeComponent createItem(String category, String name, Class type) {
        BehaviorNodeComponent component = new BehaviorNodeComponent();
        component.name = name;
        component.type = type.getName();
        component.category = category;
        return component;
    }


    public static void main(String[] args) {
        List<BehaviorNodeComponent> items = Arrays.asList(createItem("logic", "Repeat", RepeatNode.class));
        Interpreter interpreter = new Interpreter(new Actor(null));
        BehaviorNodeFactory nodeFactory = new BehaviorNodeFactory(items);
        RenderableBehaviorTree tree = new RenderableBehaviorTree(new BehaviorTreeFactory().get(""), nodeFactory);
//            root = tree.load(new FileInputStream("test.json"));
        interpreter.setTree(tree.getBehaviorTree());

        JFrame frame = new JFrame();
        BTreeMain main = new BTreeMain(nodeFactory);
        main.setTree(tree);
        main.setInterpreter(interpreter);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(main);
        frame.pack();
        frame.setVisible(true);
    }
}
