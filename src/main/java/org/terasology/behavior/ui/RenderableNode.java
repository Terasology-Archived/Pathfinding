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

import javax.vecmath.Vector2f;
import java.util.List;

/**
 * @author synopia
 */
public class RenderableNode implements AssetData {
    private final List<RenderableNode> children = Lists.newArrayList();
    private transient PortList portList;

    private Node node;
    private Vector2f position;
    private Vector2f size;

    public RenderableNode() {
        position = new Vector2f();
        size = new Vector2f(10, 5);
    }

    public PortList getPortList() {
        if (portList == null) {
            if (node instanceof CompositeNode) {
                CompositeNode compositeNode = (CompositeNode) node;
                portList = new PortList.ManyPortList(this, compositeNode);
            } else if (node instanceof DecoratorNode) {
                DecoratorNode decoratorNode = (DecoratorNode) node;
                portList = new PortList.OnePortList(this, decoratorNode);
            } else {
                portList = new PortList.EmptyPortList(this);
            }
        }
        return portList;
    }

    public void setNode(Node node) {
        this.node = node;
        portList = null;
    }

    public Port getHoveredPort(float worldX, float worldY) {
        if (getInputPort().contains(worldX, worldY)) {
            return getInputPort();
        }
        for (Port port : getPorts()) {
            if (port.contains(worldX, worldY)) {
                return port;
            }
        }
        return null;
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

    public Node getNode() {
        return node;
    }

    public Port.InputPort getInputPort() {
        return getPortList().getInputPort();
    }

    public Iterable<Port> getPorts() {
        return getPortList().ports;
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

    public boolean contains(float worldX, float worldY) {
        return position.x <= worldX && position.y <= worldY && worldX <= position.x + size.x && worldY <= position.y + size.y;
    }

    public void visit(Visitor visitor) {
        visitor.visit(this);
        for (RenderableNode child : children) {
            child.visit(visitor);
        }
    }

    public interface Visitor {
        void visit(RenderableNode node);
    }
}
