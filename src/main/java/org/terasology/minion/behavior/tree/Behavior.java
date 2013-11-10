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
public abstract class Behavior<C> {
    private Node<C> node;
    private Status status = Status.INVALID;
    private Observer<C> observer;

    protected Behavior(Node<C> node) {
        this.node = node;
    }

    public abstract Status update(C context, float dt);

    public void onInitialize(C context) {
    }

    public void onTerminate(C context, Status result) {
    }

    public Status tick(C context, float dt) {
        if (status == Status.INVALID) {
            onInitialize(context);
        }

        status = update(context, dt);

        if (status != Status.RUNNING) {
            onTerminate(context, status);
        }
        return status;
    }

    public Status getStatus() {
        return status;
    }

    public Node<C> getNode() {
        return node;
    }

    void setStatus(Status status) {
        this.status = status;
    }

    void setObserver(Observer<C> observer) {
        this.observer = observer;
    }

    Observer<C> getObserver() {
        return observer;
    }

    public interface Observer<C> {
        void handle(C context, Status result);
    }

}
