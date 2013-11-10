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

import org.junit.Test;
import org.terasology.minion.behavior.tree.Behavior;
import org.terasology.minion.behavior.tree.BehaviorTree;
import org.terasology.minion.behavior.tree.Status;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


/**
 * @author synopia
 */
public class BehaviorTreeTest {
    private Status result = Status.RUNNING;

    @Test
    public void testInit() {
        Behavior<Object> mock = create();

        BehaviorTree<Object> tree = new BehaviorTree<>(null);
        tree.start(mock);
        tree.tick(0);
        verify(mock).onInitialize(null);
    }

    @Test
    public void testUpdate() {
        Behavior<Object> mock = create();
        BehaviorTree<Object> tree = new BehaviorTree<>(null);
        tree.start(mock);
        tree.tick(0);
        verify(mock).update(any(), anyInt());
    }

    @Test
    public void testNoTerminate() {
        Behavior<Object> mock = create();
        BehaviorTree<Object> tree = new BehaviorTree<>(null);
        tree.start(mock);
        tree.tick(0);
        verify(mock, never()).onTerminate(any(), any(Status.class));
    }

    @Test
    public void testTerminate() {
        Behavior<Object> mock = create();
        BehaviorTree<Object> tree = new BehaviorTree<>(null);
        tree.start(mock);
        tree.tick(0);
        result = Status.SUCCESS;
        tree.tick(0);
        verify(mock).onTerminate(null, Status.SUCCESS);
    }

    private Behavior<Object> create() {
        return spy(new Behavior<Object>(null) {
            @Override
            public Status update(Object entity, float dt) {
                return result;
            }
        });
    }
}
