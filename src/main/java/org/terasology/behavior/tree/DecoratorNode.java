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
public abstract class DecoratorNode extends Node {
    protected Node child;

    protected DecoratorNode(Node child) {
        this.child = child;
    }

    public Node getChild() {
        return child;
    }

    public void setChild(Node child) {
        this.child = child;
    }

    public abstract static class DecoratorTask extends Task {
        protected DecoratorTask(Node node) {
            super(node);
        }

        @Override
        public DecoratorNode getNode() {
            return (DecoratorNode) super.getNode();
        }
    }
}
