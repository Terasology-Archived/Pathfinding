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

import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;

/**
 * @author synopia
 */
public class BehaviorTree<C> {
    private static final Logger logger = LoggerFactory.getLogger(BehaviorTree.class);

    private static final Behavior TERMINAL = new Behavior<Object>(null) {
        @Override
        public Status update(Object ctx, float dt) {
            return null;
        }
    };

    private C context;
    private Deque<Behavior<C>> behaviors = Queues.newArrayDeque();

    public BehaviorTree(C context) {
        this.context = context;
    }

    public void start(Behavior<C> behavior) {
        start(behavior, null);
    }

    public void start(Behavior<C> behavior, Behavior.Observer<C> observer) {
        if (observer != null) {
            behavior.setObserver(observer);
        }
        behaviors.addFirst(behavior);
    }

    public void stop(Behavior<C> behavior, Status result) {
        behavior.setStatus(result);
        Behavior.Observer<C> observer = behavior.getObserver();
        if (observer != null) {
            observer.handle(context, result);
        }
    }

    public void tick(float dt) {
        behaviors.addLast(TERMINAL);
        while (step(dt)) {
            continue;
        }
    }

    public boolean step(float dt) {
        Behavior<C> current = behaviors.pollFirst();
        if (current == TERMINAL) {
            return false;
        }

        current.tick(context, dt);

        if (current.getStatus() != Status.RUNNING && current.getObserver() != null) {
            logger.info("Finished " + current + " with status " + current.getStatus());
            current.getObserver().handle(context, current.getStatus());
        } else {
            behaviors.addLast(current);
        }
        return true;
    }

    public C getContext() {
        return context;
    }
}
