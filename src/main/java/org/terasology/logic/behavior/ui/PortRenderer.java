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
public class PortRenderer extends BaseRenderer {
    public void render(RenderContext rc, Port port) {
        port.updateRect();
        if (port.getTargetNode() == null) {
            rc.getGraphics().setColor(Color.DARK_GRAY);
        } else {
            rc.getGraphics().setColor(Color.WHITE);
        }
        rc.getGraphics().fillRect(
                rc.worldToScreenX(port.getRect().minX()), rc.worldToScreenY(port.getRect().minY()),
                rc.screenUnitX(port.getRect().width()), rc.screenUnitY(port.getRect().height()));
    }

    public void renderNetwork(RenderContext rc, Port port) {
        port.updateRect();
        if (port.getTargetNode() != null && !port.isInput()) {
            rc.getGraphics().setColor(Color.BLACK);
            rc.getGraphics().drawLine(
                    rc.worldToScreenX(port.midX()), rc.worldToScreenY(port.midY()),
                    rc.worldToScreenX(port.getTargetNode().getInputPort().midX()), rc.worldToScreenY(port.getTargetNode().getInputPort().midY()));
        }
    }

    public void renderActive(RenderContext rc, Port port) {
        port.updateRect();
        rc.getGraphics().setColor(Color.YELLOW);
        rc.getGraphics().fillRect(
                rc.worldToScreenX(port.getRect().minX()), rc.worldToScreenY(port.getRect().minY()),
                rc.screenUnitX(port.getRect().width()), rc.screenUnitY(port.getRect().height()));
        if (port.getTargetNode() != null) {
            rc.getGraphics().setColor(Color.YELLOW);
            rc.getGraphics().drawLine(
                    rc.worldToScreenX(port.midX()), rc.worldToScreenY(port.midY()),
                    rc.worldToScreenX(port.getTargetPort().midX()), rc.worldToScreenY(port.getTargetPort().midY()));
        }
    }
}
