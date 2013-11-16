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
import org.terasology.behavior.tree.CompositeNode;
import org.terasology.behavior.tree.DecoratorNode;

import java.util.List;

/**
 * @author synopia
 */
public class PortList {
    protected List<Port> ports = Lists.newLinkedList();
    private RenderableNode node;
    private Port.InputPort inputPort;

    public PortList(RenderableNode node) {
        this.node = node;
        inputPort = new Port.InputPort(node);
    }

    public Port.InputPort getInputPort() {
        return inputPort;
    }

    public List<Port> ports() {
        return ports;
    }


    public static class EmptyPortList extends PortList {
        public EmptyPortList(RenderableNode node) {
            super(node);
        }
    }

    public static class OnePortList extends PortList {
        public OnePortList(final RenderableNode node, final DecoratorNode decoratorNode) {
            super(node);
            Port.OutputPort port = new Port.OutputPort(node) {
                @Override
                public void assignChild(int index, RenderableNode child) {
                    if (child != null) {
                        decoratorNode.setChild(child.getNode());
                        node.getChildren().set(0, child);
                    } else {
                        decoratorNode.setChild(null);
                        node.getChildren().set(0, null);
                    }
                }

                public RenderableNode getChild(int index) {
                    return node.getChildren().get(0);
                }
            };
            ports.add(port);
            if (node.getChildren().size() > 0) {
                Port.InputPort inputPort = node.getChildren().get(0).getInputPort();
                inputPort.setTarget(port);
            } else {
                node.getChildren().add(null);
            }
        }
    }

    public static class ManyPortList extends PortList {
        public ManyPortList(final RenderableNode renderableNode, final CompositeNode compositeNode) {
            super(renderableNode);
            for (RenderableNode node : renderableNode.getChildren()) {
                Port.OutputPort output = add(renderableNode, compositeNode);
                node.getInputPort().setTarget(output);
            }
            add(renderableNode, compositeNode);
            renderableNode.getChildren().add(null);
            ports.remove(ports.size() - 1);
        }

        private Port.OutputPort add(final RenderableNode renderableNode, final CompositeNode compositeNode) {
            Port.OutputPort set = new Port.OutputPort(renderableNode) {
                @Override
                public void assignChild(int index, RenderableNode child) {
                    if (child != null) {
                        compositeNode.children().set(index, child.getNode());
                        renderableNode.getChildren().set(index, child);
                    } else {
                        compositeNode.children().remove(index);
                        renderableNode.getChildren().remove(index);
                        ports.remove(index * 2);
                        ports.remove(index * 2);
                    }
                }

                @Override
                public RenderableNode getChild(int index) {
                    return renderableNode.getChildren().get(index);
                }
            };
            Port.InsertOutputPort insert = new Port.InsertOutputPort(renderableNode, set) {
                @Override
                public void assignChild(int index, RenderableNode child) {
                    compositeNode.children().add(index, child.getNode());
                    renderableNode.getChildren().add(index, child);
                    ports.remove(ports.size() - 1);
                    add(renderableNode, compositeNode);
                    add(renderableNode, compositeNode);
                    ports.remove(ports.size() - 1);
                    renderableNode.getChildren().add(null);
                }

                @Override
                public RenderableNode getChild(int index) {
                    return null;
                }
            };
            ports.add(insert);
            ports.add(set);
            return set;
        }
    }

}
