// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.pathfinding;

import org.terasology.core.world.generator.AbstractBaseWorldGenerator;
import org.terasology.core.world.generator.facetProviders.FlatSurfaceHeightProvider;
import org.terasology.core.world.generator.facetProviders.SeaLevelProvider;
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
    public static final int SURFACE_HEIGHT = 50;
    public static final int SEA_HEIGHT = 2;

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
                .addProvider(new FlatSurfaceHeightProvider(SURFACE_HEIGHT))
                .addProvider(new SeaLevelProvider(SEA_HEIGHT))
                .setSeaLevel(SEA_HEIGHT);
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
