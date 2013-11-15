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

import java.awt.*;

/**
 * A render context contains all information needed for rendering. These are:
 * <ul>
 * <li>size of the screen in pixel</li>
 * <li>the window to display on screen in 2d coordinates</li>
 * </ul>
 * So panning and zooming may be realized by modifying the window to display and request a repaint.
 *
 * @author synopia
 */
public class RenderContext {
    private Graphics2D g;
    private double pixelSizeX;
    private double pixelSizeY;
    private int screenSizeX;
    private int screenSizeY;
    private double windowPositionX;
    private double windowPositionY;
    private double windowSizeX;
    private double windowSizeY;

    public RenderContext() {
    }

    public void setGraphics(Graphics2D g) {
        this.g = g;
    }

    public double screenToWorldX(int screenPosX) {
        return screenPosX / pixelSizeX + windowPositionX;
    }

    public double screenToWorldY(int screenPosY) {
        return screenPosY / pixelSizeY + windowPositionY;
    }

    public int worldToScreenX(double worldX) {
        return (int) ((worldX - windowPositionX) * pixelSizeX);
    }

    public int worldToScreenY(double worldY) {
        return (int) ((worldY - windowPositionY) * pixelSizeY);
    }

    public int screenUnitX() {
        int dx = (int) (pixelSizeX);

        return Math.max(1, Math.abs(dx));
    }

    public int screenUnitY() {
        int dy = (int) (pixelSizeY);

        return Math.max(1, Math.abs(dy));
    }

    public void setWindowPosition(double x, double y) {
        this.windowPositionX = x;
        this.windowPositionY = y;
    }

    public void setWindowSize(double x, double y) {
        this.windowSizeX = x;
        this.windowSizeY = y;
    }

    public void setScreenSize(int x, int y) {
        this.screenSizeX = x;
        this.screenSizeY = y;
    }

    public void init(double posX, double posY, double sizeX, double sizeY, int screenWidth, int screenHeight) {
        setWindowPosition(posX, posY);
        setWindowSize(sizeX, sizeY);
        setScreenSize(screenWidth, screenHeight);
        calculateSizes();
    }

    public void calculateSizes() {
        if (windowSizeX > windowSizeY) {
            windowSizeX = windowSizeY;
        }

        if (windowSizeX < windowSizeY) {
            windowSizeY = windowSizeX;
        }

        if ((screenSizeX != 0) && (screenSizeY != 0)) {
            if (screenSizeX > screenSizeY) {
                windowSizeX *= (double) screenSizeX / screenSizeY;
            } else {
                windowSizeY *= (double) screenSizeY / screenSizeX;
            }
        }

        if ((windowSizeX > 0) && (windowSizeY > 0)) {
            pixelSizeX = screenSizeX / windowSizeX;
            pixelSizeY = screenSizeY / windowSizeY;
        } else {
            pixelSizeX = 0;
            pixelSizeY = 0;
        }
    }

    public void zoom(double zoomX, double zoomY, Point mousePos) {
        double midX = windowPositionX + windowSizeX / 2.;
        double midY = windowPositionY + windowSizeY / 2.;
        double posX = screenToWorldX(mousePos.x);
        double posY = screenToWorldY(mousePos.y);

        windowSizeX *= zoomX;
        windowSizeY *= zoomY;
        calculateSizes();
        windowPositionX += (posX - midX) * 0.1;
        windowPositionY += (posY - midY) * 0.1;
    }

    public Graphics2D getGraphics() {
        return g;
    }

    public double getPixelSizeX() {
        return pixelSizeX;
    }

    public double getPixelSizeY() {
        return pixelSizeY;
    }

    public int getScreenSizeX() {
        return screenSizeX;
    }

    public int getScreenSizeY() {
        return screenSizeY;
    }

    public double getWindowSizeX() {
        return windowSizeX;
    }

    public double getWindowSizeY() {
        return windowSizeY;
    }

    public double getWindowPositionX() {
        return windowPositionX;
    }

    public double getWindowPositionY() {
        return windowPositionY;
    }

    public int getWindowStartX() {
        return (int) windowPositionX;
    }

    public int getWindowStartY() {
        return (int) windowPositionY;
    }

    public int getWindowEndX() {
        return (int) (windowPositionX + windowSizeX);
    }

    public int getWindowEndY() {
        return (int) (windowPositionY + windowSizeY);
    }

    @Override
    public String toString() {
        return String.format("window = %.2f, %.2f; %.2f, %.2f - screen = %d, %d", windowPositionX, windowPositionY,
                windowSizeX, windowSizeY, screenSizeX, screenSizeY);
    }
}
