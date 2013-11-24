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
import org.terasology.logic.behavior.BehaviorTree;
import org.terasology.logic.behavior.BehaviorTreeFactory;
import org.terasology.logic.behavior.tree.Actor;
import org.terasology.logic.behavior.tree.Interpreter;
import org.terasology.logic.behavior.tree.RepeatNode;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Collection;


/**
 * @author synopia
 */
public class BTreeMain extends JPanel {

    private BTreePanel panel;
    private Debugger debugger;

    public BTreeMain(BehaviorTree factory, Collection<BehaviorNodeComponent> items) throws HeadlessException {
        setLayout(new BorderLayout());
        panel = new BTreePanel();
        panel.setTree(factory);
        JToolBar bar = new JToolBar();
        debugger = new Debugger();
        bar.add(debugger);
        bar.add(panel.createToolBar());
        add(bar, BorderLayout.NORTH);
        add(new Palette(items), BorderLayout.WEST);
        add(panel, BorderLayout.CENTER);
        add(panel.getProperties(), BorderLayout.EAST);
        panel.init();
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
        Interpreter interpreter = new Interpreter(new Actor(null));
        BehaviorTree tree = new BehaviorTree();
        RenderableNode root;

        root = tree.addNode(new BehaviorTreeFactory().get(""));
//            root = tree.load(new FileInputStream("test.json"));
        interpreter.start(root.getNode());

        JFrame frame = new JFrame();
        BTreeMain main = new BTreeMain(tree, Arrays.asList(createItem("logic", "Repeat", RepeatNode.class)));
        main.setInterpreter(interpreter);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(main);
        frame.pack();
        frame.setVisible(true);
    }
}
