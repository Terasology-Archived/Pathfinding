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
import org.terasology.logic.behavior.BehaviorNodeComponent;
import org.terasology.logic.behavior.BehaviorNodeFactory;

import javax.swing.*;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author synopia
 */
public class Palette extends JPanel implements TreeSelectionListener {
    private List<BehaviorNodeComponent> allItems = Lists.newArrayList();
    private Map<String, List<BehaviorNodeComponent>> items = Maps.newHashMap();
    private List<String> categories = Lists.newArrayList();
    private SelectionObserver observer;
    private final JTree tree;

    public Palette(BehaviorNodeFactory components) {
        this.allItems.addAll(components.getNodeComponents());
        for (BehaviorNodeComponent component : components.getNodeComponents()) {
            String category = component.category;
            if (category == null) {
                category = "";
            }
            List<BehaviorNodeComponent> list = items.get(category);
            if (list == null) {
                list = Lists.newArrayList();
                items.put(category, list);
                categories.add(category);
            }
            list.add(component);
        }
        for (List<BehaviorNodeComponent> list : items.values()) {
            Collections.sort(list, new Comparator<BehaviorNodeComponent>() {
                @Override
                public int compare(BehaviorNodeComponent o1, BehaviorNodeComponent o2) {
                    return o1.name.compareTo(o2.name);
                }
            });
        }
        Collections.sort(categories);
        setLayout(new BorderLayout());
        tree = new JTree(new PaletteTreeModel());
        tree.setRootVisible(false);
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
        add(tree, BorderLayout.CENTER);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        TreePath path = e.getPath();
        Object last = path.getLastPathComponent();
        if (last instanceof BehaviorNodeComponent) {
            BehaviorNodeComponent nodeComponent = (BehaviorNodeComponent) last;
            if (observer != null) {
                observer.selectionChanged(nodeComponent);
                tree.getSelectionModel().removeSelectionPath(path);
            }
        }
    }

    public void setObserver(SelectionObserver observer) {
        this.observer = observer;
    }

    public class PaletteTreeModel implements TreeModel {
        private String root = "";

        @Override
        public Object getRoot() {
            return root;
        }

        @Override
        public Object getChild(Object parent, int index) {
            if (parent == root) {
                return categories.get(index);
            }
            if (categories.contains(parent.toString())) {
                return items.get(parent).get(index);
            }
            return null;
        }

        @Override
        public int getChildCount(Object parent) {
            if (parent == root) {
                return categories.size();
            }
            if (categories.contains(parent.toString())) {
                return items.get(parent).size();
            }
            return 0;
        }

        @Override
        public boolean isLeaf(Object node) {
            return !(node instanceof String);
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {

        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            if (parent == root) {
                return categories.indexOf(child);
            }
            if (categories.contains(parent)) {
                return items.get(parent).indexOf(child);
            }
            return 0;
        }

        @Override
        public void addTreeModelListener(TreeModelListener l) {

        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {

        }
    }

    public interface SelectionObserver {
        void selectionChanged(BehaviorNodeComponent nodeComponent);
    }
}
