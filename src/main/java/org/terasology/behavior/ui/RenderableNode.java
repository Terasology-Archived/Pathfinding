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

import com.google.common.collect.Lists;
import org.terasology.asset.AssetData;
import org.terasology.behavior.tree.CompositeNode;
import org.terasology.behavior.tree.DecoratorNode;
import org.terasology.behavior.tree.Node;

import javax.swing.*;
import javax.vecmath.Vector2f;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * @author synopia
 */
public class RenderableNode implements AssetData {
    private final List<RenderableNode> children = Lists.newArrayList();
    private final transient List<Port> ports = Lists.newArrayList();
    private final transient InputPort inputPort;
    private final transient Font font;

    private Node node;
    private Vector2f position;
    private Vector2f size;

    public RenderableNode() {
        position = new Vector2f();
        size = new Vector2f(10, 5);
        font = new JLabel().getFont();
        inputPort = new InputPort();
    }

    public void update() {
        if (children.size() > 0) {
            int index = 0;
            for (RenderableNode child : children) {
                child.update();
                ports.add(new Port(index, child));
                index++;
            }
            if (children.size() < maxChildren()) {
                ports.add(new Port(index, null));
            }
        }
    }

    public void setNode(Node node) {
        this.node = node;
        List<Node> childrenList = null;
        if (node instanceof CompositeNode) {
            CompositeNode compositeNode = (CompositeNode) node;
            childrenList = compositeNode.children();
        } else if (node instanceof DecoratorNode) {
            DecoratorNode decoratorNode = (DecoratorNode) node;
            childrenList = Lists.newArrayList();
            childrenList.add(decoratorNode.getChild());
        }
        if (childrenList != null) {
            int index = 0;
            float x = position.x + size.x / 2 - childrenList.size() * 6;
            float y = position.y + 7;
            for (Node child : childrenList) {
                RenderableNode renderableNode = new RenderableNode();
                renderableNode.setPosition(x, y);
                renderableNode.setNode(child);
                children.add(renderableNode);
                ports.add(new Port(index, renderableNode));
                index++;
                x += 12;
            }
            ports.add(new Port(index, null));
        }
    }

    public int maxChildren() {
        return (node instanceof CompositeNode ? Integer.MAX_VALUE : node instanceof DecoratorNode ? 1 : 0);
    }

    public void setPosition(Vector2f position) {
        this.position = position;
    }

    public void setPosition(float x, float y) {
        position = new Vector2f(x, y);
    }

    public void setSize(Vector2f size) {
        this.size = size;
    }

    public void setChildren(List<RenderableNode> nodes) {

    }

    public Node getNode() {
        return node;
    }

    public void render(RenderContext rc) {
        int startX = rc.worldToScreenX(position.x);
        int startY = rc.worldToScreenY(position.y);
        int endX = rc.worldToScreenX(position.x + size.x);
        int endY = rc.worldToScreenY(position.y + size.y);
        rc.getGraphics().setColor(Color.WHITE);
        rc.getGraphics().fillRect(startX, startY, endX - startX, endY - startY);

        drawPorts(rc);

        Font currentFont = font.deriveFont((float) rc.screenUnitX(1.5d));
        rc.getGraphics().setFont(currentFont);

        rc.getGraphics().setColor(Color.BLACK);
        String text = node.getClass().getSimpleName();
        text = text.substring(0, text.length() - 4);
        drawText(rc, (startX + endX) / 2, (startY + endY) / 2, text);
    }

    private void drawPorts(RenderContext rc) {
        for (Port port : ports) {
            port.draw(rc);
        }
    }

    private void drawText(RenderContext rc, int midX, int midY, String text) {
        Rectangle2D textBounds = rc.getGraphics().getFontMetrics().getStringBounds(text, rc.getGraphics());
        float textX = midX - ((float) textBounds.getWidth() / 2);
        float textY = midY + ((float) textBounds.getHeight() / 3);
        rc.getGraphics().drawString(text, textX, textY);
    }

    private class Port {
        private int index;
        private RenderableNode target;

        private Port(int index, RenderableNode target) {
            this.index = index;
            this.target = target;
        }

        public float midX() {
            return position.x + index + 0.5f;
        }

        public float midY() {
            return position.y + size.y - 0.5f;
        }

        public void draw(RenderContext rc) {
            if (target == null) {
                rc.getGraphics().setColor(Color.LIGHT_GRAY);
            } else {
                rc.getGraphics().setColor(Color.BLACK);
                rc.getGraphics().drawLine(rc.worldToScreenX(midX()), rc.worldToScreenY(midY()), rc.worldToScreenX(target.inputPort.midX()), rc.worldToScreenY(target.inputPort.midY()));
                target.render(rc);
            }
            rc.getGraphics().fillRect(rc.worldToScreenX(position.x + index + 0.05d), rc.worldToScreenY(position.y + size.y - 0.95d), rc.screenUnitX(0.9d), rc.screenUnitY(0.9d));

        }
    }

    public String getNodeType() {
        return node.getClass().getSimpleName();
    }

    public List<RenderableNode> getChildren() {
        return children;
    }

    public Vector2f getPosition() {
        return position;
    }

    public Vector2f getSize() {
        return size;
    }

    private class InputPort {
        public float midX() {
            return position.x + size.x / 2 - 0.5f;
        }

        public float midY() {
            return position.y + 0.5f;
        }
    }
}
