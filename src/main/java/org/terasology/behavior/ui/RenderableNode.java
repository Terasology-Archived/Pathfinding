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
import org.terasology.math.Rect2f;

import java.awt.*;
import java.util.List;

/**
 * @author synopia
 */
public class RenderableNode {
    private final List<RenderableNode> children = Lists.newArrayList();
    private Rect2f bounds;

    public RenderableNode() {
    }

    public void setPosition(float x, float y) {
        bounds = Rect2f.createFromMinAndSize(x, y, 9, 5);
    }

    public void render(RenderContext rc) {
        int startX = rc.worldToScreenX(bounds.minX());
        int startY = rc.worldToScreenX(bounds.minY());
        int endX = rc.worldToScreenX(bounds.maxX());
        int endY = rc.worldToScreenX(bounds.maxY());
        rc.getGraphics().setColor(Color.WHITE);
        rc.getGraphics().fillRect(startX, startY, endX - startX, endY - startY);
    }
}
