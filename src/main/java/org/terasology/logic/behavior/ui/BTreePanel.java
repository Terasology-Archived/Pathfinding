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

import org.terasology.logic.behavior.BehaviorNodeComponent;
import org.terasology.logic.behavior.BehaviorNodeFactory;
import org.terasology.logic.behavior.RenderableBehaviorTree;
import org.terasology.logic.behavior.tree.Interpreter;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Task;
import org.terasology.logic.behavior.ui.properties.PropertiesPanelFactory;

import javax.swing.*;
import javax.vecmath.Vector2f;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * @author synopia
 */
public class BTreePanel extends ZoomPanel implements Palette.SelectionObserver {
    private RenderableBehaviorTree tree;
    private Interpreter interpreter;

    private NodeRenderer nodeRenderer = new NodeRenderer();
    private TaskRenderer taskRenderer = new TaskRenderer();
    private PortRenderer portRenderer = new PortRenderer();
    private RenderableNode currentBlueprint;
    private Port startPort;
    private Port hoveredPort;
    private RenderableNode hoveredNode;
    private RenderableNode movingNode;
    private float clickOffsetX;
    private float clickOffsetY;
    private float currentMouseX;
    private float currentMouseY;
    private PropertiesPanelFactory properties;
    private BehaviorNodeFactory nodeFactory;

    public BTreePanel(BehaviorNodeFactory nodeFactory) {
        this.nodeFactory = nodeFactory;
        properties = new PropertiesPanelFactory();
        setPreferredSize(new Dimension(800, 600));
    }

    @Override
    public void init() {
        super.init();
        context.setWindowSize(9 * 3, 5 * 3);
        context.calculateSizes();
        new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        }).start();
    }

    @Override
    public void selectionChanged(BehaviorNodeComponent nodeComponent) {
        if (!tree.getBehaviorTree().isEditable()) {
            return;
        }
        Node node = nodeFactory.getNode(nodeComponent);
        if (node != null) {
            currentBlueprint = new RenderableNode(nodeComponent);
            currentBlueprint.setNode(node);
        }
    }

    public JToolBar createToolBar() {
        JToolBar bar = new JToolBar(SwingConstants.HORIZONTAL);
        bar.addSeparator();
        return bar;
    }

    public PropertiesPanelFactory getProperties() {
        return properties;
    }

    @Override
    protected RenderContext createRenderContext() {
        return new RenderContext();
    }

    @Override
    protected void paintContent(Graphics2D g) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (tree == null) {
            return;
        }
        for (RenderableNode node : tree.getRenderableNodes()) {
            nodeRenderer.render(context, node);
        }
        for (RenderableNode node : tree.getRenderableNodes()) {
            for (Port port : node.getPorts()) {
                portRenderer.renderNetwork(context, port);
            }
        }
        if (interpreter != null) {
            for (Task task : interpreter.tasks()) {
                Node n = task.getNode();
                RenderableNode node = tree.getRenderableNode(n);
                if (node == null) {
                    continue;
                }
                taskRenderer.render(context, node, task);
            }
        }

        if (currentBlueprint != null) {
            currentBlueprint.setPosition(currentMouseX - currentBlueprint.getSize().x / 2, currentMouseY - currentBlueprint.getSize().y / 2);
            nodeRenderer.render(context, currentBlueprint);
        }
        if (hoveredPort != null) {
            portRenderer.renderActive(context, hoveredPort);
        }
        if (startPort != null) {
            int startX = context.worldToScreenX(startPort.midX());
            int startY = context.worldToScreenY(startPort.midY());
            int endX = context.worldToScreenX(currentMouseX);
            int endY = context.worldToScreenY(currentMouseY);
            if (hoveredPort != null && hoveredPort.isInput() != startPort.isInput()) {
                portRenderer.renderActive(context, hoveredPort);
            }
            context.getGraphics().setColor(Color.WHITE);
            context.getGraphics().drawLine(startX, startY, endX, endY);
        }
    }

    @Override
    protected void onMouseRectangle(MouseEvent e, int x, int y, int width, int height) {
    }

    @Override
    protected void onMouseDragged(MouseEvent e, int x, int y, int lastX, int lastY) {
        double windowPosX = context.screenToWorldX(lastX) - context.screenToWorldX(x)
                + context.getWindowPositionX();
        double windowPosY = context.screenToWorldY(lastY) - context.screenToWorldY(y)
                + context.getWindowPositionY();

        context.setWindowPosition(windowPosX, windowPosY);
        repaint();
    }

    @Override
    protected void onMouseWheel(MouseWheelEvent e, int x, int y, int wheelRotation) {
        double scale = 1 + wheelRotation * 0.05;

        context.zoom(scale, scale, e.getPoint());
        repaint();
    }

    @Override
    protected void onMouseMoved(MouseEvent e, int x, int y) {
        if (tree == null) {
            return;
        }
        float worldX = (float) context.screenToWorldX(x);
        float worldY = (float) context.screenToWorldY(y);
        currentMouseX = worldX;
        currentMouseY = worldY;
        if (movingNode != null) {
            RenderableNode parent = movingNode.getInputPort().getTargetNode();
            if (parent != null) {
                Vector2f parentPos = parent.getPosition();
                movingNode.setPosition((worldX - clickOffsetX) - parentPos.x, (worldY - clickOffsetY) - parentPos.y);
            } else {
                movingNode.setPosition(worldX - clickOffsetX, worldY - clickOffsetY);
            }
        } else {
            findHover(worldX, worldY);
        }

        repaint();
    }

    @Override
    protected void onMousePressed(MouseEvent e, int x, int y) {
        if (tree == null) {
            return;
        }
        float worldX = (float) context.screenToWorldX(x);
        float worldY = (float) context.screenToWorldY(y);

        findHover(worldX, worldY);
        if (tree.getBehaviorTree().isEditable()) {
            if (startPort == null && hoveredPort != null) {
                if (hoveredPort.getTargetNode() != null) {
                    startPort = hoveredPort.getTargetPort();
                    interpreter.reset();

                    tree.disconnectNodes(hoveredPort.getTargetPort(), hoveredPort);
                    if (!startPort.isInput()) {
                        startPort = hoveredPort;
                    }
                } else {
                    startPort = hoveredPort;
                }
            }
        }
        if (startPort == null) {
            movingNode = hoveredNode;
        }
        repaint();
    }

    @Override
    protected void onMouseReleased(MouseEvent e, int x, int y) {
        if (tree == null) {
            return;
        }
        float worldX = (float) context.screenToWorldX(x);
        float worldY = (float) context.screenToWorldY(y);
        findHover(worldX, worldY);
        if (hoveredNode != null) {
            properties.bindObject(hoveredNode.getNode());
        }
        if (movingNode != null) {
            movingNode = null;
            return;
        }
        if (tree.getBehaviorTree().isEditable()) {
            if (currentBlueprint != null) {
                tree.addNode(currentBlueprint);
                currentBlueprint = null;
            }
            if (startPort != null) {
                if (hoveredPort != null && hoveredPort != startPort && hoveredPort.isInput() != startPort.isInput()) {
                    if (hoveredPort.isInput()) {
                        tree.connectNodes(startPort, hoveredPort);
                    } else {
                        tree.connectNodes(hoveredPort, startPort);
                    }
                    interpreter.reset();
                }
                startPort = null;
            }
        }

        repaint();
    }

    private void findHover(float worldX, float worldY) {
        hoveredNode = findNode(worldX, worldY);
        if (hoveredNode != null) {
            clickOffsetX = worldX - hoveredNode.getPosition().x;
            clickOffsetY = worldY - hoveredNode.getPosition().y;
            hoveredPort = hoveredNode.getHoveredPort(worldX, worldY);
        }
    }

    private RenderableNode findNode(float worldX, float worldY) {
        for (RenderableNode node : tree.getRenderableNodes()) {
            if (node.contains(worldX, worldY)) {
                return node;
            }
        }
        return null;
    }

    @Override
    public boolean isRectMode(MouseEvent e) {
        return super.isRectMode(e) && (e.isShiftDown() || e.isControlDown());
    }

    public RenderableBehaviorTree getTree() {
        return tree;
    }

    public void setTree(RenderableBehaviorTree tree) {
        this.tree = tree;
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }

    public void setInterpreter(Interpreter interpreter) {
        this.interpreter = interpreter;
    }
}
