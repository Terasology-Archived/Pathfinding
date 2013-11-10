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

/**
 * @author synopia
 */
public class Counter<C> extends Behavior<C> {
    private int count;

    public Counter(CounterNode<C> node) {
        super(node);
    }

    @Override
    public void onInitialize(C context) {
        count = getNode().limit;
    }

    @Override
    public Status update(C context, float dt) {
        if (count > 0) {
            count--;
            return Status.RUNNING;
        } else {
            return Status.SUCCESS;
        }
    }

    @Override
    public CounterNode<C> getNode() {
        return (CounterNode<C>) super.getNode();
    }

    public static class CounterNode<C> implements Node<C> {
        private int limit;

        public CounterNode(int limit) {
            this.limit = limit;
        }

        @Override
        public Counter<C> create(BehaviorTree<C> tree) {
            return new Counter<>(this);
        }
    }
}
