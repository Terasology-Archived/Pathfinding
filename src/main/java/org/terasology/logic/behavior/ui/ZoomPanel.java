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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * @author synopia
 */
public abstract class ZoomPanel extends JPanel {
    private final Logger logger = LoggerFactory.getLogger(ZoomPanel.class);
    protected RenderContext context;
    private Rectangle lastRect;
    private Rectangle rect;
    private Point start;

    protected abstract RenderContext createRenderContext();

    public void init() {
        context = createRenderContext();
        context.init(0, 0, getWidth(), getHeight(), getWidth(), getHeight());
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                onMouseMoved(e, e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                onMouseMoved(e, e.getX(), e.getY());

                if (start == null) {
                    return;
                }

                if (isRectMode(e)) {
                    rectModeDragged(e);
                } else if (isDragMode(e)) {
                    dragModeDragged(e);
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isRectMode(e)) {
                    rectModeReleased(e);
                } else if (!isDragMode(e)) {
                    onMouseReleased(e, e.getX(), e.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isRectMode(e) || isDragMode(e)) {
                    start = e.getPoint();
                } else {
                    onMousePressed(e, e.getX(), e.getY());
                }
            }
        });
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                onMouseWheel(e, e.getX(), e.getY(), e.getWheelRotation());
                onMouseMoved(e, e.getX(), e.getY());
            }
        });
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        context.setScreenSize(width, height);
        context.calculateSizes();
    }

    /**
     * Overwrite this method, to define when zooming.
     *
     * @param e current MouseEvent
     * @return true if the MouseEvent is a zooming event, false if not
     */
    public boolean isRectMode(MouseEvent e) {
        return (e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0;
    }

    public boolean isDragMode(MouseEvent e) {
        return (e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0;
    }

    protected abstract void paintContent(Graphics2D graphics2D);

    protected abstract void onMouseRectangle(MouseEvent e, int x, int y, int width, int height);

    protected abstract void onMouseDragged(MouseEvent e, int x, int y, int lastX, int lastY);

    protected abstract void onMouseWheel(MouseWheelEvent e, int x, int y, int wheelRotation);

    protected abstract void onMouseMoved(MouseEvent e, int x, int y);

    protected abstract void onMousePressed(MouseEvent e, int x, int y);

    protected abstract void onMouseReleased(MouseEvent e, int x, int y);

    protected void initContext(Graphics2D g) {
        context.setGraphics(g);
    }

    protected void rectModeReleased(MouseEvent e) {
        rect = null;

        Point end = e.getPoint();

        if ((start != null) && !start.equals(end)) {
            e.consume();

            int x = (start.x < end.x)
                    ? start.x
                    : end.x;
            int y = (start.y < end.y)
                    ? start.y
                    : end.y;
            int width = Math.abs(end.x - start.x);
            int height = Math.abs(end.y - start.y);

            if ((width > 10) && (height > 10)) {
                onMouseRectangle(e, x, y, width, height);
            }
        }
    }

    protected void dragModeDragged(MouseEvent e) {
        onMouseDragged(e, e.getX(), e.getY(), start.x, start.y);
        start = e.getPoint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;

        initContext(graphics2D);
        paintContent(graphics2D);
        graphics2D.setXORMode(Color.WHITE);

        if (rect != null) {
            graphics2D.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
        }
    }

    protected void rectModeDragged(MouseEvent e) {
        e.consume();

        Point mousePos = e.getPoint();
        Point start = this.start;
        int width = mousePos.x - start.x;
        int height = mousePos.y - start.y;
        int w = Math.abs(width);
        int h = Math.abs(height);
        int x = (width > 0)
                ? start.x
                : mousePos.x;
        int y = (height > 0)
                ? start.y
                : mousePos.y;

        repaintRect(new Rectangle(x, y, w, h));
    }

    public void applyWindow(int startX, int startY, int width, int height) {
        int x = (width > 0)
                ? startX
                : startX + width;
        int y = (height > 0)
                ? startY
                : startY + height;
        int w = Math.abs(width);
        int h = Math.abs(height);

        if ((w < 10) || (h < 10)) {
            return;
        }

        double windowPositionX = context.screenToWorldX(x);
        double windowPositionY = context.screenToWorldY(y);

        context.init(windowPositionX, windowPositionY, context.screenToWorldX(x + w) - windowPositionX,
                context.screenToWorldY(y + h) - windowPositionY, getWidth(), getHeight());
        repaint();
    }

    protected void scrollTo(double x, double y) {
        double startX = x - context.getWindowSizeX() / 2;
        double startY = y - context.getWindowSizeY() / 2;

        context.init(startX, startY, context.getWindowSizeX(), context.getWindowSizeY(), context.getScreenSizeX(),
                context.getScreenSizeY());
        repaint();
    }

    private void repaintRect(Rectangle rectangle) {
        rect = rectangle;

        Rectangle union = rect;

        if (lastRect != null) {
            union = rect.union(lastRect);
        }

        repaint(union);
        lastRect = rect;
    }
}
