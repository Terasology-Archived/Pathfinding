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
 * @author synopia
 */
public class NodeRenderer extends BaseRenderer {
    private PortRenderer portRenderer = new PortRenderer();

    public void render(RenderContext rc, RenderableNode node, int startX, int startY, int endX, int endY) {
        rc.getGraphics().setColor(Color.LIGHT_GRAY);
        rc.getGraphics().fillRect(startX, startY, endX - startX, endY - startY);

        for (Port port : node.getPorts()) {
            portRenderer.render(rc, port);
        }
        portRenderer.render(rc, node.getInputPort());

        Font currentFont = FONT.deriveFont((float) rc.screenUnitX(1.5d));
        rc.getGraphics().setFont(currentFont);

        rc.getGraphics().setColor(Color.BLACK);
        String text = node.getNode().getClass().getSimpleName();
        text = text.substring(0, text.length() - 4);
        drawText(rc, (startX + endX) / 2, (startY + endY) / 2, text);
    }

}
