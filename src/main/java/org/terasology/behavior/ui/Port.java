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
package org.terasology.behavior.ui;

import org.terasology.math.Rect2f;

/**
 * @author synopia
 */
public abstract class Port {
    protected RenderableNode renderableNode;
    protected Rect2f rect;

    Port(RenderableNode renderableNode) {
        this.renderableNode = renderableNode;
//        updateRect();
    }

    public int index() {
        return renderableNode.getPortList().ports.indexOf(this) / 2;
    }

    public abstract void updateRect();

    public RenderableNode getSource() {
        return renderableNode;
    }

    public RenderableNode getTargetNode() {
        return getTarget() != null ? getTarget().getSource() : null;
    }

    public abstract Port getTarget();

    public boolean isInput() {
        return false;
    }

    public boolean contains(float worldX, float worldY) {
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
        return getSource() + "[" + index() + "]";
    }

    public abstract static class OutputPort extends Port {
        public OutputPort(RenderableNode renderableNode) {
            super(renderableNode);
        }

        @Override
        public void updateRect() {
            this.rect = Rect2f.createFromMinAndSize(
                    renderableNode.getPosition().x + index() + 0.3f,
                    renderableNode.getPosition().y + renderableNode.getSize().y - 0.95f,
                    0.7f, 0.9f);
        }

        public void setTarget(InputPort inputPort) {
            if (inputPort != null) {
                inputPort.setTarget(this);
                assignChild(index(), inputPort.getSource());
            } else {
                RenderableNode child = getChild(index());
                if (child != null) {
                    child.getInputPort().setTarget(null);
                }
                assignChild(index(), null);
            }
        }

        @Override
        public InputPort getTarget() {
            RenderableNode child = getChild(index());
            if (child != null) {
                return child.getInputPort();
            }
            return null;
        }

        public abstract void assignChild(int index, RenderableNode child);

        public abstract RenderableNode getChild(int index);
    }

    public abstract static class InsertOutputPort extends OutputPort {
        private OutputPort outputPort;

        protected InsertOutputPort(RenderableNode renderableNode, OutputPort outputPort) {
            super(renderableNode);
            this.outputPort = outputPort;
        }

        @Override
        public void updateRect() {
            this.rect = Rect2f.createFromMinAndSize(
                    renderableNode.getPosition().x + index(),
                    renderableNode.getPosition().y + renderableNode.getSize().y - 0.95f,
                    0.3f, 0.9f);
        }

        @Override
        public void setTarget(InputPort inputPort) {
            if (inputPort != null) {
                inputPort.setTarget(outputPort);
                assignChild(index(), inputPort.getSource());
            } else {
                assignChild(index(), null);
            }
        }
    }

    public static class InputPort extends Port {
        private OutputPort outputPort;

        public InputPort(RenderableNode node) {
            super(node);
        }

        @Override
        public void updateRect() {
            rect = Rect2f.createFromMinAndSize(renderableNode.getPosition().x + renderableNode.getSize().x / 2f - 0.5f, renderableNode.getPosition().y + 0.05f, 1f, 1f);
        }

        public void setTarget(OutputPort port) {
            this.outputPort = port;
        }

        @Override
        public Port getTarget() {
            return outputPort;
        }

        @Override
        public boolean isInput() {
            return true;
        }
    }
}
