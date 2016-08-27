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
package org.terasology.pathfinding;

import org.terasology.core.world.generator.AbstractBaseWorldGenerator;
import org.terasology.core.world.generator.facetProviders.FlatSurfaceHeightProvider;
import org.terasology.core.world.generator.facetProviders.SeaLevelProvider;
import org.terasology.engine.SimpleUri;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.generation.World;
import org.terasology.world.generation.WorldBuilder;
import org.terasology.world.generator.RegisterWorldGenerator;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;

/**
 * @author synopia
 */
@RegisterWorldGenerator(id = "pathfinder", displayName = "Pathfinder TestWorld")
public class PathfinderTestWorldMapGenerator extends AbstractBaseWorldGenerator {
    private World world;
    private WorldBuilder worldBuilder;

    public PathfinderTestWorldMapGenerator(SimpleUri uri) {
        super(uri);
    }

    @Override
    public void initialize() {
        register(new PathfinderTestGenerator());
        getWorld().initialize();
    }

    @Override
    public void setWorldSeed(String seed) {
        worldBuilder = new WorldBuilder(CoreRegistry.get(WorldGeneratorPluginLibrary.class))
                .addProvider(new FlatSurfaceHeightProvider(50))
                .addProvider(new SeaLevelProvider(2))
                .setSeaLevel(2);
        worldBuilder.setSeed(seed.hashCode());
        world = null;
        super.setWorldSeed(seed);
    }

    @Override
    public World getWorld() {
        if (world == null) {
            world = worldBuilder.build();
        }
        return world;
    }

}
