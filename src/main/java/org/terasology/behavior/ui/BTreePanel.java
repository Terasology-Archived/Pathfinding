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

import org.terasology.behavior.asset.BehaviorFactory;
import org.terasology.behavior.tree.Actor;
import org.terasology.behavior.tree.Interpreter;
import org.terasology.behavior.tree.Node;
import org.terasology.behavior.tree.SequenceNode;
import org.terasology.behavior.tree.Task;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author synopia
 */
public class BTreePanel extends ZoomPanel {
    private RenderableNode root;
    private BehaviorFactory factory;
    private Interpreter interpreter;

    private NodeRenderer nodeRenderer = new NodeRenderer();
    private TaskRenderer taskRenderer = new TaskRenderer();
    private PortRenderer portRenderer = new PortRenderer();
    private RenderableNode currentBlueprint;
    private Port startPort;
    private Port hoveredPort;
    private float currentMouseX;
    private float currentMouseY;

    public BTreePanel() {

    }

    @Override
    public void init() {
        super.init();
        context.setWindowSize(9 * 3, 5 * 3);
        context.calculateSizes();

        factory = new BehaviorFactory();
        interpreter = new Interpreter(new Actor(null));

        try {
            root = factory.load(new FileInputStream("test.json"));
            interpreter.start(root.getNode());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public JToolBar createToolBar() {
        JToolBar bar = new JToolBar(SwingConstants.HORIZONTAL);
        bar.add(new AbstractAction("||") {
            @Override
            public void actionPerformed(ActionEvent e) {
                interpreter.reset();
                interpreter.start(root.getNode());
                repaint();
            }
        });
        bar.add(new AbstractAction("|>") {
            @Override
            public void actionPerformed(ActionEvent e) {
                interpreter.step(0.2f);
                repaint();
            }
        });
        bar.add(new AbstractAction(">>") {
            @Override
            public void actionPerformed(ActionEvent e) {
                interpreter.step(0.2f);
                repaint();
            }
        });
        bar.addSeparator();
        bar.add(new AbstractAction("-->") {
            @Override
            public void actionPerformed(ActionEvent e) {
                SequenceNode node = new SequenceNode();
                currentBlueprint = new RenderableNode();
                currentBlueprint.setNode(node);
            }
        });
        return bar;
    }

    @Override
    protected RenderContext createRenderContext() {
        return new RenderContext();
    }

    @Override
    protected void paintContent(Graphics2D g) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());
        for (RenderableNode node : factory.getRenderableNodes()) {
            int startX = context.worldToScreenX(node.getPosition().x);
            int startY = context.worldToScreenY(node.getPosition().y);
            int endX = context.worldToScreenX(node.getPosition().x + node.getSize().x);
            int endY = context.worldToScreenY(node.getPosition().y + node.getSize().y);

            nodeRenderer.render(context, node, startX, startY, endX, endY);
        }
        for (RenderableNode node : factory.getRenderableNodes()) {
            for (Port port : node.getPorts()) {
                portRenderer.renderNetwork(context, port);
            }
        }

        for (Task task : interpreter.tasks()) {
            Node n = task.getNode();
            RenderableNode node = factory.getRenderableNode(n);
            if (node == null) {
                continue;
            }
            int startX = context.worldToScreenX(node.getPosition().x);
            int startY = context.worldToScreenY(node.getPosition().y);
            int endX = context.worldToScreenX(node.getPosition().x + node.getSize().x);
            int endY = context.worldToScreenY(node.getPosition().y + node.getSize().y);

            taskRenderer.render(context, node, task, startX, startY, endX, endY);
        }

        if (currentBlueprint != null) {
            currentBlueprint.setPosition(currentMouseX - currentBlueprint.getSize().x / 2, currentMouseY - currentBlueprint.getSize().y / 2);
            int startX = context.worldToScreenX(currentBlueprint.getPosition().x);
            int startY = context.worldToScreenY(currentBlueprint.getPosition().y);
            int endX = context.worldToScreenX(currentBlueprint.getPosition().x + currentBlueprint.getSize().x);
            int endY = context.worldToScreenY(currentBlueprint.getPosition().y + currentBlueprint.getSize().y);
            nodeRenderer.render(context, currentBlueprint, startX, startY, endX, endY);
        }
        if (hoveredPort != null) {
            if (hoveredPort.getTarget() != null) {

                int startX = context.worldToScreenX(hoveredPort.midX());
                int startY = context.worldToScreenY(hoveredPort.midY());
                int endX = context.worldToScreenX(hoveredPort.getTarget().midX());
                int endY = context.worldToScreenY(hoveredPort.getTarget().midY());
                context.getGraphics().setColor(Color.YELLOW);
                context.getGraphics().drawLine(startX, startY, endX, endY);
            }
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
        //To change body of implemented methods use File | Settings | File Templates.
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
        currentMouseX = (float) context.screenToWorldX(x);
        currentMouseY = (float) context.screenToWorldY(y);
        hoveredPort = findPort(currentMouseX, currentMouseY);
        repaint();
    }

    @Override
    protected void onMousePressed(MouseEvent e, int x, int y) {
        float worldX = (float) context.screenToWorldX(x);
        float worldY = (float) context.screenToWorldY(y);

        Port port = findPort(worldX, worldY);
        if (startPort == null && port != null) {
            if (port.getTargetNode() != null) {
                startPort = port.getTarget();
                factory.disconnectNodes(port.getTarget(), port);
            } else {
                startPort = port;
            }
        }
        repaint();
    }

    @Override
    protected void onMouseReleased(MouseEvent e, int x, int y) {
        float worldX = (float) context.screenToWorldX(x);
        float worldY = (float) context.screenToWorldY(y);

        if (currentBlueprint != null) {
            factory.addNode(currentBlueprint);
            currentBlueprint = null;
        }
        if (startPort != null) {
            Port targetPort = findPort(worldX, worldY);
            if (targetPort != null && targetPort != startPort && targetPort.isInput() != startPort.isInput()) {
                if (targetPort.isInput()) {
                    factory.connectNodes(startPort, targetPort);
                } else {
                    factory.connectNodes(targetPort, startPort);
                }
            }
            startPort = null;
        }

        for (RenderableNode node : factory.getRenderableNodes()) {
            if (node.contains(worldX, worldY)) {
            }
        }
        repaint();
    }

    private Port findPort(float worldX, float worldY) {
        for (RenderableNode node : factory.getRenderableNodes()) {
            if (node.contains(worldX, worldY)) {
                Port port = node.getHoveredPort(worldX, worldY);
                if (port != null) {
                    return port;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isRectMode(MouseEvent e) {
        return super.isRectMode(e) && (e.isShiftDown() || e.isControlDown());
    }
}
