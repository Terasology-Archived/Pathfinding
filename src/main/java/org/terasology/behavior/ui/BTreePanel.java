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

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;

/**
 * @author synopia
 */
public class BTreePanel extends ZoomPanel {
    private List<RenderableNode> nodes = Lists.newArrayList();

    public BTreePanel() {

    }

    @Override
    public void init() {
        super.init();
        context.setWindowSize(9 * 3, 5 * 3);
        context.calculateSizes();

        RenderableNode one = new RenderableNode();
        one.setPosition(9, 0);
        RenderableNode two = new RenderableNode();
        two.setPosition(0, 5);
        RenderableNode three = new RenderableNode();
        three.setPosition(9, 5);
        nodes.add(one);
        nodes.add(two);
        nodes.add(three);
    }

    @Override
    protected RenderContext createRenderContext() {
        return new RenderContext();
    }

    @Override
    protected void paintContent(Graphics2D g) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());
        for (RenderableNode node : nodes) {
            node.render(context);
        }
    }

    @Override
    protected void onMouseRectangle(MouseEvent e, int x, int y, int width, int height) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onMouseDragged(MouseEvent e, int x, int y, int lastX, int lastY) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onMouseWheel(MouseWheelEvent e, int x, int y, int wheelRotation) {
        //To change body of implemented methods use File | Settings | File Templates.
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
