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
package org.terasology.minion.behavior;

import org.junit.Assert;
import org.junit.Test;
import org.terasology.minion.behavior.tree.Behavior;
import org.terasology.minion.behavior.tree.BehaviorTree;
import org.terasology.minion.behavior.tree.Node;
import org.terasology.minion.behavior.tree.Parallel;
import org.terasology.minion.behavior.tree.Status;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author synopia
 */
public class ParallelTest {
    @Test
    public void testSuccessRequireAll() {
        BehaviorTree<Object> tree = new BehaviorTree<>(null);
        Parallel.ParallelNode<Object> parallel = new Parallel.ParallelNode<>(Parallel.Policy.RequireAll, Parallel.Policy.RequireOne);

        Node<Object> one = create(new Mocker() {
            @Override
            public void mock(Behavior<Object> spy) {
                when(spy.update(any(), anyInt())).thenReturn(Status.RUNNING, Status.SUCCESS);
            }
        });
        Node<Object> two = create(new Mocker() {
            @Override
            public void mock(Behavior<Object> spy) {
                when(spy.update(any(), anyInt())).thenReturn(Status.RUNNING, Status.RUNNING, Status.SUCCESS);
            }
        });
        parallel.children.add(one);
        parallel.children.add(two);

        Parallel<Object> behavior = parallel.create(tree);

        tree.start(behavior);
        tree.tick(0);
        Assert.assertEquals(Status.RUNNING, behavior.getStatus());
        tree.tick(0);
        Assert.assertEquals(Status.RUNNING, behavior.getStatus());
        tree.tick(0);
        Assert.assertEquals(Status.SUCCESS, behavior.getStatus());
    }

    @Test
    public void testSuccessRequireOne() {
        BehaviorTree<Object> tree = new BehaviorTree<>(null);
        Parallel.ParallelNode<Object> parallel = new Parallel.ParallelNode<>(Parallel.Policy.RequireOne, Parallel.Policy.RequireAll);
        Node<Object> one = create(new Mocker() {
            @Override
            public void mock(Behavior<Object> spy) {
                when(spy.update(any(), anyInt())).thenReturn(Status.RUNNING, Status.SUCCESS);
            }
        });
        Node<Object> two = create(new Mocker() {
            @Override
            public void mock(Behavior<Object> spy) {
                when(spy.update(any(), anyInt())).thenReturn(Status.RUNNING, Status.RUNNING);
            }
        });
        parallel.children.add(one);
        parallel.children.add(two);

        Parallel<Object> behavior = parallel.create(tree);

        tree.start(behavior);
        tree.tick(0);
        Assert.assertEquals(Status.RUNNING, behavior.getStatus());
        tree.tick(0);
        Assert.assertEquals(Status.SUCCESS, behavior.getStatus());
    }

    @Test
    public void testFailureRequireAll() {
        BehaviorTree<Object> tree = new BehaviorTree<>(null);
        Parallel.ParallelNode<Object> parallel = new Parallel.ParallelNode<>(Parallel.Policy.RequireOne, Parallel.Policy.RequireAll);
        Node<Object> one = create(new Mocker() {
            @Override
            public void mock(Behavior<Object> spy) {
                when(spy.update(any(), anyInt())).thenReturn(Status.RUNNING, Status.FAILURE);
            }
        });
        Node<Object> two = create(new Mocker() {
            @Override
            public void mock(Behavior<Object> spy) {
                when(spy.update(any(), anyInt())).thenReturn(Status.RUNNING, Status.RUNNING, Status.FAILURE);
            }
        });
        parallel.children.add(one);
        parallel.children.add(two);

        Parallel<Object> behavior = parallel.create(tree);

        tree.start(behavior);
        tree.tick(0);
        Assert.assertEquals(Status.RUNNING, behavior.getStatus());
        tree.tick(0);
        Assert.assertEquals(Status.RUNNING, behavior.getStatus());
        tree.tick(0);
        Assert.assertEquals(Status.FAILURE, behavior.getStatus());
    }

    @Test
    public void testFailureRequireOne() {
        BehaviorTree<Object> tree = new BehaviorTree<>(null);
        Parallel.ParallelNode<Object> parallel = new Parallel.ParallelNode<>(Parallel.Policy.RequireAll, Parallel.Policy.RequireOne);
        Node<Object> one = create(new Mocker() {
            @Override
            public void mock(Behavior<Object> spy) {
                when(spy.update(any(), anyInt())).thenReturn(Status.RUNNING, Status.FAILURE);
            }
        });
        Node<Object> two = create(new Mocker() {
            @Override
            public void mock(Behavior<Object> spy) {
                when(spy.update(any(), anyInt())).thenReturn(Status.RUNNING, Status.RUNNING);
            }
        });
        parallel.children.add(one);
        parallel.children.add(two);

        Parallel<Object> behavior = parallel.create(tree);

        tree.start(behavior);
        tree.tick(0);
        Assert.assertEquals(Status.RUNNING, behavior.getStatus());
        tree.tick(0);
        Assert.assertEquals(Status.FAILURE, behavior.getStatus());
    }

    private Node<Object> create(final Mocker mocker) {
        return new Node<Object>() {
            @Override
            public Behavior<Object> create(BehaviorTree<Object> tree) {
                Behavior<Object> spy = spy(new Behavior<Object>(null) {
                    @Override
                    public Status update(Object entity, float dt) {
                        return null;
                    }
                });
                mocker.mock(spy);
                return spy;
            }
        };
    }

    private interface Mocker {
        void mock(Behavior<Object> spy);
    }
}
