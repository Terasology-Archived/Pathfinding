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
package org.terasology.minion.behavior.tree;

import java.util.Iterator;

/**
 * @author synopia
 */
public class Selector<C> extends Composite<C> implements Behavior.Observer<C> {
    private BehaviorTree<C> behaviorTree;
    private Iterator<Node<C>> iterator;
    private Behavior<C> current;

    public Selector(SelectorNode<C> node, BehaviorTree<C> behaviorTree) {
        super(node);
        this.behaviorTree = behaviorTree;
    }

    @Override
    public void onInitialize(C context) {
        iterator = getNode().children.iterator();
        current = iterator.next().create(behaviorTree);
        behaviorTree.start(current, this);
    }

    @Override
    public void handle(C context, Status result) {
        if (current.getStatus() == Status.SUCCESS) {
            behaviorTree.stop(this, Status.SUCCESS);
            return;
        }

        if (iterator.hasNext()) {
            current = iterator.next().create(behaviorTree);
            behaviorTree.start(current, this);
        } else {
            behaviorTree.stop(this, Status.FAILURE);
        }
    }

    @Override
    public Status update(C context, float dt) {
        return Status.RUNNING;
    }

    @Override
    public SelectorNode<C> getNode() {
        return (SelectorNode<C>) super.getNode();
    }

    public static class SelectorNode<C> extends CompositeNode<C> {
        @Override
        public Selector<C> create(BehaviorTree<C> tree) {
            return new Selector<>(this, tree);
        }
    }
}
