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
public class Parallel<C> extends Composite<C> implements Behavior.Observer<C> {
    public enum Policy {
        RequireOne,
        RequireAll
    }

    private int successCount;
    private int failureCount;
    private BehaviorTree<C> behaviorTree;

    public Parallel(ParallelNode<C> node, BehaviorTree<C> behaviorTree) {
        super(node);
        this.behaviorTree = behaviorTree;
    }

    @Override
    public void onInitialize(C context) {
        for (Node<C> child : getNode().children) {
            behaviorTree.start(child.create(behaviorTree), this);
        }
        successCount = 0;
        failureCount = 0;
    }

    @Override
    public Status update(C context, float dt) {
        return Status.RUNNING;
    }

    @Override
    public void handle(C context, Status result) {
        if (result == Status.SUCCESS) {
            successCount++;
            if (getNode().successPolicy == Policy.RequireOne) {
                behaviorTree.stop(this, Status.SUCCESS);
            }
        }
        if (result == Status.FAILURE) {
            failureCount++;
            if (getNode().failurePolicy == Policy.RequireOne) {
                behaviorTree.stop(this, Status.FAILURE);
            }
        }
        if (getNode().failurePolicy == Policy.RequireAll && failureCount == getNode().children.size()) {
            behaviorTree.stop(this, Status.FAILURE);
        }
        if (getNode().successPolicy == Policy.RequireAll && successCount == getNode().children.size()) {
            behaviorTree.stop(this, Status.SUCCESS);
        }
    }

    @Override
    public void onTerminate(C context, Status result) {
    }

    @Override
    public ParallelNode<C> getNode() {
        return (ParallelNode<C>) super.getNode();
    }

    public static class ParallelNode<C> extends CompositeNode<C> {
        private Policy successPolicy;
        private Policy failurePolicy;

        public ParallelNode(Policy forSuccess, Policy forFailure) {
            successPolicy = forSuccess;
            failurePolicy = forFailure;
        }

        @Override
        public Parallel<C> create(BehaviorTree<C> tree) {
            return new Parallel<>(this, tree);
        }
    }
}
