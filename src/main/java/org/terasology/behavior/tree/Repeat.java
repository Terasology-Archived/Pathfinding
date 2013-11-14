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
package org.terasology.behavior.tree;

/**
 * @author synopia
 */
public class Repeat<C> extends Decorator<C> implements Behavior.Observer<C> {

    public Repeat(RepeatNode<C> node, BehaviorTree<C> behaviorTree) {
        super(node, behaviorTree);
    }

    @Override
    public void onInitialize(C context) {
        behaviorTree.start(getNode().child.create(behaviorTree), this);
    }

    @Override
    public Status update(C context, float dt) {
        return Status.RUNNING;
    }

    @Override
    public void handle(C context, Status result) {
        if (result == Status.FAILURE) {
            behaviorTree.stop(this, Status.FAILURE);
            return;
        }

        behaviorTree.start(getNode().child.create(behaviorTree), this);
    }

    @Override
    public RepeatNode<C> getNode() {
        return (RepeatNode<C>) super.getNode();
    }

    public static class RepeatNode<C> extends Decorator.DecoratorNode<C> {
        public RepeatNode(Node<C> child) {
            super(child);
        }

        @Override
        public Repeat<C> create(BehaviorTree<C> tree) {
            return new Repeat<>(this, tree);
        }
    }

}
