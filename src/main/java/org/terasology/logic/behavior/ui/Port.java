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
package org.terasology.logic.behavior.ui;

import org.terasology.math.Rect2f;

/**
 * @author synopia
 */
public abstract class Port {
    protected RenderableNode node;
    protected Rect2f rect;

    protected Port(RenderableNode node) {
        this.node = node;
    }

    public int index() {
        return node.getPortList().indexOfPort(this);
    }

    public abstract void updateRect();

    public RenderableNode getSourceNode() {
        return node;
    }

    public RenderableNode getTargetNode() {
        return getTargetPort() != null ? getTargetPort().getSourceNode() : null;
    }

    public abstract Port getTargetPort();

    public boolean isInput() {
        return false;
    }

    public boolean contains(float worldX, float worldY) {
        updateRect();
        return rect.contains(worldX, worldY);
    }

    public Rect2f getRect() {
        return rect;
    }

    public float midX() {
        return (rect.minX() + rect.maxX()) / 2;
    }

    public float midY() {
        return (rect.minY() + rect.maxY()) / 2;
    }

    @Override
    public String toString() {
        return getSourceNode() + "[" + index() + "]";
    }

    public static class OutputPort extends Port {
        public OutputPort(RenderableNode renderableNode) {
            super(renderableNode);
        }

        @Override
        public void updateRect() {
            this.rect = Rect2f.createFromMinAndSize(
                    node.getPosition().x + index() + 0.3f,
                    node.getPosition().y + node.getSize().y - 0.95f,
                    0.7f, 0.9f);
        }

        public void setTarget(InputPort inputPort) {
            if (inputPort != null) {
                node.withModel().setChild(index(), inputPort.getSourceNode());
            } else {
                node.withModel().removeChild(index());
            }
        }

        @Override
        public InputPort getTargetPort() {
            RenderableNode child = node.withModel().getChild(index());
            if (child != null) {
                return child.getInputPort();
            }
            return null;
        }
    }

    public static class InsertOutputPort extends OutputPort {

        protected InsertOutputPort(RenderableNode renderableNode) {
            super(renderableNode);
        }

        @Override
        public void updateRect() {
            this.rect = Rect2f.createFromMinAndSize(
                    node.getPosition().x + index(),
                    node.getPosition().y + node.getSize().y - 0.95f,
                    0.3f, 0.9f);
        }

        @Override
        public void setTarget(InputPort inputPort) {
            if (inputPort != null) {
                node.withModel().insertChild(index(), inputPort.getSourceNode());
            } else {
                throw new IllegalStateException("Cannot remove target from an insert output port");
            }
        }

        @Override
        public InputPort getTargetPort() {
            return null;
        }
    }

    public static class InputPort extends Port {
        private OutputPort outputPort;

        public InputPort(RenderableNode node) {
            super(node);
        }

        @Override
        public void updateRect() {
            rect = Rect2f.createFromMinAndSize(node.getPosition().x + node.getSize().x / 2f - 0.5f, node.getPosition().y + 0.05f, 1f, 1f);
        }

        public void setTarget(OutputPort port) {
            this.outputPort = port;
        }

        @Override
        public Port getTargetPort() {
            return outputPort;
        }

        @Override
        public boolean isInput() {
            return true;
        }
    }
}
