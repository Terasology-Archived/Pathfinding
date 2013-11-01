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
package org.terasology.pathfinding.rendering;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.terasology.asset.Assets;
import org.terasology.engine.CoreRegistry;
import org.terasology.math.Vector3i;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glTranslatef;

/**
 * @author synopia
 */
public class InfoGrid {
    private Font font;
    private Map<Vector3i, GridPosition> grid = Maps.newHashMap();
    private List<Category> categories = Lists.newArrayList();
    private Stack<Color> colors = new Stack<>();

    public InfoGrid() {
        font = Assets.getFont("engine:default");
        colors.push(Color.orange);
        colors.push(Color.yellow);
        colors.push(Color.cyan);
        colors.push(Color.blue);
        colors.push(Color.red);
        colors.push(Color.darkGray);
    }

    private GridPosition create(Vector3i pos) {
        GridPosition gp = grid.get(pos);
        if (gp == null) {
            gp = new GridPosition();
            gp.position = pos;
            grid.put(pos, gp);
        }
        return gp;
    }

    public Category addCategory(String name) {
        for (Category category : categories) {
            if (category.name.equals(name)) {
                return category;
            }
        }
        Category category = new Category();
        category.name = name;
        category.color = colors.peek();
        categories.add(category);
        return category;
    }

    public void removeInfo(Vector3i pos, String category) {
        GridPosition gridPosition = create(pos);
        gridPosition.entries.remove(category);
    }

    public void removeCategory(String category) {
        for (GridPosition gridPosition : grid.values()) {
            gridPosition.entries.remove(category);
        }
        Category category1 = addCategory(category);
        categories.remove(category1);
    }

    public void addInfo(Vector3i pos, String category, String info) {
        GridPosition gridPosition = create(pos);
        gridPosition.entries.put(category, info);
        addCategory(category);
    }

    public void render() {
        int height = font.getHeight("ABC");
        for (GridPosition gp : grid.values()) {
            GL11.glPushMatrix();

            Vector3f cameraPosition = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
            Vector3f worldPos = new Vector3f(gp.position.x, gp.position.y, gp.position.z);
            worldPos.sub(cameraPosition);
            worldPos.y += 1f;
            renderBillboardBegin(-1, worldPos, null, new Vector3f(0.005f, -0.005f, 0.005f));
            int pos = 0;
            int cat = 0;

            for (Category category : categories) {
                String text = gp.entries.get(category.name);
                if (text != null) {
                    for (String line : text.split("\n")) {
                        font.drawString(0, pos, line, categories.get(cat).color);
                        pos += height;
                    }
                    cat++;
                }
            }
            renderBillboardEnd();

            GL11.glPopMatrix();
        }
    }

    private void renderBillboardBegin(int textureId, Vector3f position, Vector3f offset, Vector3f scale) {

        glDisable(GL11.GL_CULL_FACE);

        if (textureId >= 0) {
            CoreRegistry.get(ShaderManager.class).enableDefaultTextured();
            glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        }

        glDepthMask(false);
        glEnable(GL11.GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glPushMatrix();
        glTranslated(position.x, position.y, position.z);

        glPushMatrix();
        applyOrientation();

        if (offset != null) {
            glTranslatef(offset.x, offset.y, offset.z);
        }

        if (scale != null) {
            glScalef(scale.x, scale.y, scale.z);
        }
    }

    private void renderBillboardEnd() {
        glPopMatrix();
        glPopMatrix();
        glDisable(GL11.GL_BLEND);
        glDepthMask(true);
        glEnable(GL11.GL_CULL_FACE);
    }

    private void applyOrientation() {
        // Fetch the current modelview matrix
        final FloatBuffer model = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, model);

        // And undo all rotations and scaling
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == j) {
                    model.put(i * 4 + j, 1.0f);
                } else {
                    model.put(i * 4 + j, 0.0f);
                }
            }
        }

        GL11.glLoadMatrix(model);
    }

    private final class GridPosition {
        public Map<String, String> entries = Maps.newHashMap();
        public Vector3i position;
    }

    private final class Category {
        public String name;
        public Color color;
    }


}
