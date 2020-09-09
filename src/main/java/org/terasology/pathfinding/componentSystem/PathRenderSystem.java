// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.pathfinding.componentSystem;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.RenderSystem;
import org.terasology.engine.registry.Share;
import org.terasology.engine.rendering.world.selection.BlockSelectionRenderer;
import org.terasology.engine.utilities.Assets;
import org.terasology.navgraph.WalkableBlock;
import org.terasology.pathfinding.model.Path;

import java.util.List;

@RegisterSystem(RegisterMode.CLIENT)
@Share(value = PathRenderSystem.class)
public class PathRenderSystem extends BaseComponentSystem implements RenderSystem {
    private final List<Path> paths = Lists.newArrayList();
    private BlockSelectionRenderer selectionRenderer;

    @Override
    public void initialise() {
        selectionRenderer = new BlockSelectionRenderer(Assets.getTexture("engine:selection").get());
    }

    public void addPath(Path path) {
        paths.add(path);
    }

    public void removePath(Path path) {
        paths.remove(path);
    }

    @Override
    public void renderOverlay() {
        selectionRenderer.beginRenderOverlay();
        for (Path path : paths) {
            for (WalkableBlock block : path) {
                selectionRenderer.renderMark2(block.getBlockPosition());
            }
        }
        selectionRenderer.endRenderOverlay();
    }

    @Override
    public void renderAlphaBlend() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void renderOpaque() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void renderShadows() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void shutdown() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
