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

import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;

/**
 * @author synopia
 */
public class Interpreter {
    private static final Logger logger = LoggerFactory.getLogger(Interpreter.class);

    private static final Task TERMINAL = new Task(null) {
        @Override
        public Status update(float dt) {
            return null;
        }
    };

    private Actor actor;
    private Deque<Task> tasks = Queues.newArrayDeque();

    public Interpreter(Actor actor) {
        this.actor = actor;
        tasks.addLast(TERMINAL);
    }

    public Deque<Task> tasks() {
        return tasks;
    }

    public void reset() {
        tasks.clear();
        tasks.addLast(TERMINAL);
    }

    public void start(Node node) {
        start(node, null);
    }

    public void start(Task task) {
        start(task, null);
    }

    public void start(Node node, Task.Observer observer) {
        start(node.create(), observer);
    }

    public void start(Task task, Task.Observer observer) {
        task.setActor(actor);
        task.setInterpreter(this);
        task.setObserver(observer);

        tasks.addFirst(task);
    }

    public void stop(Task task, Status result) {
        task.setStatus(result);
        Task.Observer observer = task.getObserver();
        if (observer != null) {
            observer.handle(result);
        }
    }

    public void tick(float dt) {
        while (step(dt)) {
            continue;
        }
    }

    public boolean step(float dt) {
        Task current = tasks.pollFirst();
        if (current == TERMINAL) {
            tasks.addLast(TERMINAL);
            return false;
        }

        try {
            current.tick(dt);
        } catch (Exception e) {
            current.setStatus(Status.SUCCESS);
        }

        if (current.getStatus() != Status.RUNNING && current.getObserver() != null) {
            logger.info("Finished " + current + " with status " + current.getStatus());
            current.getObserver().handle(current.getStatus());
        } else {
            tasks.addLast(current);
        }
        return true;
    }
}
