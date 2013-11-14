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
package org.terasology.behavior;

import org.junit.Assert;
import org.junit.Test;
import org.terasology.behavior.tree.Behavior;
import org.terasology.behavior.tree.BehaviorTree;
import org.terasology.behavior.tree.Node;
import org.terasology.behavior.tree.Sequence;
import org.terasology.behavior.tree.Status;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author synopia
 */
public class SequenceTest {
    @Test
    public void testTwoChildrenFails() {
        final Behavior<Object> spies[] = new Behavior[2];
        BehaviorTree<Object> tree = new BehaviorTree<>(null);
        Node<Object> one = create(new Mocker() {
            @Override
            public void mock(Behavior<Object> spy) {
                when(spy.update(any(), anyInt())).thenReturn(Status.RUNNING, Status.FAILURE);
                spies[0] = spy;
            }
        });
        Node<Object> two = create(new Mocker() {
            @Override
            public void mock(Behavior<Object> spy) {
                spies[1] = spy;
            }
        });
        Sequence.SequenceNode<Object> node = new Sequence.SequenceNode<>();
        node.children.add(one);
        node.children.add(two);

        Behavior<Object> selector = node.create(tree);
        tree.start(selector);
        tree.tick(0);
        Assert.assertEquals(Status.RUNNING, selector.getStatus());
        tree.tick(0);
        Assert.assertEquals(Status.FAILURE, selector.getStatus());
        verify(spies[0]).onTerminate(null, Status.FAILURE);
        Assert.assertNull(spies[1]);
    }

    @Test
    public void testTwoChildrenContinues() {
        final Behavior<Object> spies[] = new Behavior[2];
        BehaviorTree<Object> tree = new BehaviorTree<>(null);
        Node<Object> one = create(new Mocker() {
            @Override
            public void mock(Behavior<Object> spy) {
                when(spy.update(any(), anyInt())).thenReturn(Status.RUNNING, Status.SUCCESS);
                spies[0] = spy;
            }
        });
        Node<Object> two = create(new Mocker() {
            @Override
            public void mock(Behavior<Object> spy) {
                when(spy.update(any(), anyInt())).thenReturn(Status.RUNNING);
                spies[1] = spy;
            }
        });
        Sequence.SequenceNode<Object> node = new Sequence.SequenceNode<>();
        node.children.add(one);
        node.children.add(two);

        Behavior<Object> selector = node.create(tree);

        tree.start(selector);
        tree.tick(0);
        Assert.assertEquals(Status.RUNNING, selector.getStatus());
        tree.tick(0);
        Assert.assertEquals(Status.RUNNING, selector.getStatus());

        verify(spies[0]).onTerminate(null, Status.SUCCESS);
        verify(spies[1]).onInitialize(null);
    }

    @Test
    public void testOneChildPassThrough() {
        final Behavior<Object> spies[] = new Behavior[1];
        Status stats[] = new Status[]{Status.SUCCESS, Status.FAILURE};
        for (final Status status : stats) {
            BehaviorTree<Object> tree = new BehaviorTree<>(null);
            Node<Object> mock = create(new Mocker() {
                @Override
                public void mock(Behavior<Object> spy) {
                    when(spy.update(null, 0)).thenReturn(Status.RUNNING, status);
                    spies[0] = spy;
                }
            });
            Sequence.SequenceNode<Object> node = new Sequence.SequenceNode<>();

            node.children.add(mock);

            Sequence<Object> sequence = node.create(tree);
            tree.start(sequence);
            tree.tick(0);
            Assert.assertEquals(Status.RUNNING, sequence.getStatus());
            tree.tick(0);
            Assert.assertEquals(status, sequence.getStatus());
            verify(spies[0]).onTerminate(null, status);
        }
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
