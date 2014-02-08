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
package org.terasology.pathfinding.componentSystem;

import com.google.common.collect.Lists;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.navgraph.WalkableBlock;
import org.terasology.pathfinding.model.Path;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.world.selection.BlockSelectionRenderer;

import java.util.List;

/**
 * Created by synopia on 01.02.14.
 */
@RegisterSystem
public class PathRenderSystem implements RenderSystem {
    @In
    private EntityManager entityManager;
    private BlockSelectionRenderer selectionRenderer;
    private List<Path> paths = Lists.newArrayList();

    public PathRenderSystem() {
        CoreRegistry.put(PathRenderSystem.class, this);
    }

    @Override
    public void initialise() {
        selectionRenderer = new BlockSelectionRenderer(Assets.getTexture("engine:selection"));
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
    public void renderFirstPerson() {
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
