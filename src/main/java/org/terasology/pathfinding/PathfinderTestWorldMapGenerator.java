// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.pathfinding;

import org.terasology.coreworlds.generator.AbstractBaseWorldGenerator;
import org.terasology.coreworlds.generator.facetProviders.FlatSurfaceHeightProvider;
import org.terasology.coreworlds.generator.facetProviders.SeaLevelProvider;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.generation.World;
import org.terasology.engine.world.generation.WorldBuilder;
import org.terasology.engine.world.generator.RegisterWorldGenerator;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPluginLibrary;

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
