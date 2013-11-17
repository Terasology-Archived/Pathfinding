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

import org.terasology.logic.behavior.tree.Task;

import java.awt.*;

/**
 * @author synopia
 */
public class TaskRenderer extends BaseRenderer {
    private PortRenderer portRenderer = new PortRenderer();

    public void render(RenderContext rc, RenderableNode node, Task task) {
        int startX = rc.worldToScreenX(node.getPosition().x + 0.05);
        int startY = rc.worldToScreenY(node.getPosition().y + 0.05);
        int endX = rc.worldToScreenX(node.getPosition().x + 3);
        int endY = rc.worldToScreenY(node.getPosition().y + 2);

        Font currentFont = FONT.deriveFont((float) rc.screenUnitX(0.5d));
        rc.getGraphics().setFont(currentFont);

        Color color = null;
        switch (task.getStatus()) {
            case FAILURE:
                color = Color.RED;
                break;
            case SUCCESS:
                color = Color.BLUE;
                break;
            case RUNNING:
                color = Color.YELLOW;
                break;
            case INVALID:
                color = Color.DARK_GRAY;
                break;
            case SUSPENDED:
                color = Color.BLACK;
                break;
        }
        rc.getGraphics().setColor(color);
        rc.getGraphics().fillArc(startX, startY, endX - startX, endY - startY, 0, 360);
//        String text = task.getStatus().toString();
//        drawText(rc, (startX + endX) / 2, startY + (endY - startY) / 5, text);

        if (node.getInputPort().getTargetPort() != null) {
            portRenderer.renderActive(rc, node.getInputPort().getTargetPort());
        }
    }
}
