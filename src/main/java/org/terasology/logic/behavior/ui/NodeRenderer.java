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

import java.awt.*;

/**
 * @author synopia
 */
public class NodeRenderer extends BaseRenderer {
    private PortRenderer portRenderer = new PortRenderer();

    public void render(RenderContext rc, RenderableNode node) {
        int startX = rc.worldToScreenX(node.getPosition().x);
        int startY = rc.worldToScreenY(node.getPosition().y);
        int endX = rc.worldToScreenX(node.getPosition().x + node.getSize().x);
        int endY = rc.worldToScreenY(node.getPosition().y + node.getSize().y);

        rc.getGraphics().setColor(node.getData().getColor());
        switch (node.getData().shape) {
            case "diamond":
                Polygon s = new Polygon();
                int midX = (startX + endX) / 2;
                int midY = (startY + endY) / 2;
                s.addPoint(midX, startY);
                s.addPoint(endX, midY);
                s.addPoint(midX, endY);
                s.addPoint(startX, midY);
                rc.getGraphics().fill(s);
                break;

            default:
                rc.getGraphics().fillRect(startX, startY, endX - startX, endY - startY);
                break;
        }
        for (Port port : node.getPorts()) {
            portRenderer.render(rc, port);
        }
        portRenderer.render(rc, node.getInputPort());
        Font currentFont = FONT.deriveFont((float) rc.screenUnitX(1.5d));
        rc.getGraphics().setFont(currentFont);

        String text = node.getData().name;

        rc.getGraphics().setColor(node.getData().getTextColor());
        if (text != null) {
            drawText(rc, (startX + endX) / 2, (startY + endY) / 2, text);
        }
    }

}
