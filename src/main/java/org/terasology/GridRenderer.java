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
package org.terasology;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.layouts.ZoomableLayout;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockPart;
import org.terasology.world.block.loader.WorldAtlas;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

/**
 * Created by synopia on 11.02.14.
 */
public class GridRenderer extends ZoomableLayout {
    private static final Logger logger = LoggerFactory.getLogger(GridRenderer.class);

    public GridRenderer() {
    }

    public GridRenderer(String id) {
        super(id);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Texture terrainTex = Assets.getTexture("engine:terrain");

        Rect2i region = canvas.getRegion();
        Vector2f worldStart = screenToWorld(region.min());
        Vector2f worldEnd = screenToWorld(new Vector2i(region.maxX(), region.maxY()));

        Vector3f playerPosition = CoreRegistry.get(LocalPlayer.class).getPosition();

        Vector3i blockPos = new Vector3i();
        int hx = (int) (worldEnd.x - worldStart.x) / 2;
        int hz = (int) (worldEnd.y - worldStart.y) / 2;
        for (int z = (int) worldStart.y; z < (int) worldEnd.y; z++) {
            for (int x = (int) worldStart.x; x < (int) worldEnd.x; x++) {
                Vector2i tileStart = worldToScreen(new Vector2f(x, z));
                Vector2i tileEnd = worldToScreen(new Vector2f(x + 1, z + 1));
                int y = (int) playerPosition.y + 1;
                Color color = new Color(1, 1, 1, 1);
                int depth = 0;
                do {
                    blockPos.set((int) playerPosition.x + x - hx, y, (int) playerPosition.z + z - hz);
                    Block block = CoreRegistry.get(WorldProvider.class).getBlock(blockPos);

                    if (!block.isPenetrable()) {
                        Vector2f textureAtlasPos = block.getPrimaryAppearance().getTextureAtlasPos(BlockPart.TOP);
                        float uw = CoreRegistry.get(WorldAtlas.class).getRelativeTileSize();
                        float uh = CoreRegistry.get(WorldAtlas.class).getRelativeTileSize();
                        Rect2i screenRegion = Rect2i.createFromMinAndMax(tileStart.x, tileStart.y, tileEnd.x, tileEnd.y);
                        canvas.drawTextureRaw(terrainTex, screenRegion, color, ScaleMode.SCALE_FILL, textureAtlasPos.x, textureAtlasPos.y, uw, uh);
                        break;
                    } else {
                        y--;
                        depth++;
                        if (depth >= 10) {
                            break;
                        }
                        color = new Color(1 - depth / 10.f, 1 - depth / 10.f, 1 - depth / 10.f, 1);
                    }
                } while (true);
            }
        }
    }
}
