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
import org.terasology.behavior.tree.Node;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author synopia
 */
public class BTreePanel extends ZoomPanel {
    private RenderableNode root;

    public BTreePanel() {

    }

    @Override
    public void init() {
        super.init();
        context.setWindowSize(9 * 3, 5 * 3);
        context.calculateSizes();

        root = new RenderableNode();
        root.setPosition(9, 0);
        org.terasology.behavior.BehaviorFactory factory = new org.terasology.behavior.BehaviorFactory();
        Node node = factory.get("");
        root.setNode(node);

        BehaviorFactory loader = new BehaviorFactory();
        try {
            loader.save(root, new FileOutputStream("test.json"));
            root = loader.load(new FileInputStream("test.json"));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    protected RenderContext createRenderContext() {
        return new RenderContext();
    }

    @Override
    protected void paintContent(Graphics2D g) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());
        root.render(context);
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
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onMousePressed(MouseEvent e, int x, int y) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onMouseReleased(MouseEvent e, int x, int y) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
