/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.grid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.grid.renderers.DefaultBlockRenderer;
import org.terasology.grid.renderers.WalkableBlockRenderer;
import org.terasology.input.Keyboard;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.layouts.ZoomableLayout;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

/**
 * Created by synopia on 11.02.14.
 */
public class GridRenderer extends ZoomableLayout {
    private static final Logger logger = LoggerFactory.getLogger(GridRenderer.class);

    private DefaultBlockRenderer blockRenderer;
    private WalkableBlockRenderer walkableBlockRenderer;
    private EntityRenderer entityRenderer;
    private int yDiff = -1;
    private InteractionListener listener = new BaseInteractionListener() {
        @Override
        public boolean onMouseWheel(int wheelTurns, Vector2i pos) {
            if (!Keyboard.isKeyDown(Keyboard.Key.LEFT_SHIFT.getId())) {
                yDiff += wheelTurns > 0 ? -1 : +1;
            }
            return false;
        }
    };

    public GridRenderer() {
        initialize();
    }

    public GridRenderer(String id) {
        super(id);
        initialize();
    }

    private void initialize() {
        blockRenderer = CoreRegistry.get(DefaultBlockRenderer.class);
        walkableBlockRenderer = CoreRegistry.get(WalkableBlockRenderer.class);
        entityRenderer = CoreRegistry.get(EntityRenderer.class);

    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.addInteractionRegion(listener);

        Vector3f playerPosition = CoreRegistry.get(LocalPlayer.class).getCharacterEntity().getComponent(LocationComponent.class).getWorldPosition();
        Vector2f windowSize = getWindowSize();
        Vector2f topLeft = new Vector2f(playerPosition.x - windowSize.x / 2, playerPosition.z - windowSize.y / 2);
        setWindowPosition(topLeft);

        Rect2i region = canvas.getRegion();
        Vector2f worldStart = screenToWorld(region.min());
        Vector2f worldEnd = screenToWorld(new Vector2i(region.maxX(), region.maxY()));

        int y = (int) playerPosition.y + yDiff;

        for (int z = (int) worldStart.y; z < (int) worldEnd.y; z++) {
            for (int x = (int) worldStart.x; x < (int) worldEnd.x; x++) {
                Vector2i tileStart = worldToScreen(new Vector2f(x, z));
                Vector2i tileEnd = worldToScreen(new Vector2f(x + 1, z + 1));
                Rect2i screenRegion = Rect2i.createFromMinAndMax(tileStart.x, tileStart.y, tileEnd.x - 1, tileEnd.y - 1);

                blockRenderer.renderBlock(canvas, new Vector3i(x, y, z), screenRegion);
                walkableBlockRenderer.renderBlock(canvas, new Vector3i(x, y, z), screenRegion);
            }
        }

        EntityRef entity = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
        Vector3f worldPos = entity.getComponent(LocationComponent.class).getWorldPosition();
        Vector2i min = worldToScreen(new Vector2f(worldPos.x - 0.4f, worldPos.z - 0.4f));
        Vector2i max = worldToScreen(new Vector2f(worldPos.x + 0.4f, worldPos.z + 0.4f));
        entityRenderer.renderBlock(canvas, entity, Rect2i.createFromMinAndMax(min, max));
    }
}
