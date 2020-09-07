/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.pathfinding;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.terasology.TextWorldBuilder;
import org.terasology.WorldProvidingHeadlessEnvironment;
import org.terasology.core.world.generator.AbstractBaseWorldGenerator;
import org.terasology.engine.SimpleUri;
import org.terasology.gestalt.naming.Name;
import org.terasology.math.geom.Vector3i;
import org.terasology.navgraph.NavGraphChunk;
import org.terasology.navgraph.NavGraphSystem;
import org.terasology.navgraph.WalkableBlock;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.Pathfinder;
import org.terasology.registry.InjectionHelper;

/**
 * @author synopia
 */
public class PathfinderTest {
    private Pathfinder pathfinder;
    private NavGraphSystem world;
    private TextWorldBuilder builder;

    @Test
    public void test() {
        world.updateChunk(new Vector3i(0, 0, 0));
        world.updateChunk(new Vector3i(1, 0, 0));
        world.updateChunk(new Vector3i(2, 0, 0));
        world.updateChunk(new Vector3i(0, 0, 1));
        world.updateChunk(new Vector3i(1, 0, 1));
        world.updateChunk(new Vector3i(2, 0, 1));
        world.updateChunk(new Vector3i(0, 0, 2));
        world.updateChunk(new Vector3i(1, 0, 2));
        world.updateChunk(new Vector3i(2, 0, 2));

        Path path = pathfinder.findPath(world.getBlock(new Vector3i(14 + 16, 45, 12)), world.getBlock(new Vector3i(0, 51, 1)));
        Assert.assertEquals(0, path.size());

        builder.setAir(7, 50, 7);
        builder.setAir(7, 50, 8);
        builder.setAir(NavGraphChunk.SIZE_X - 1, 47, 7);
        builder.setAir(NavGraphChunk.SIZE_X - 1, 47, 8);
        builder.setAir(NavGraphChunk.SIZE_X, 47, 7);
        builder.setAir(NavGraphChunk.SIZE_X, 47, 8);


        world.updateChunk(new Vector3i(0, 0, 0));
        world.updateChunk(new Vector3i(1, 0, 0));

        path = pathfinder.findPath(world.getBlock(new Vector3i(14 + 16, 45, 12)), world.getBlock(new Vector3i(0, 51, 1)));
        Assert.assertTrue(0 < path.size());
    }

    @Test
    public void testStairs() {
        assertStairs(0, 0);
        assertStairs(1, 0);
        assertStairs(0, 1);
        assertStairs(1, 1);
    }

    public void assertStairs(int chunkX, int chunkZ) {
        int x = chunkX * NavGraphChunk.SIZE_X;
        int z = chunkZ * NavGraphChunk.SIZE_Z;
        NavGraphChunk map = world.updateChunk(new Vector3i(chunkX, 0, chunkZ));

        WalkableBlock startBlock = world.getBlock(new Vector3i(0 + x, 51, 1 + z));
        WalkableBlock targetBlock = world.getBlock(new Vector3i(x + NavGraphChunk.SIZE_X - 2, 45, z + NavGraphChunk.SIZE_Z - 4));

        Assert.assertEquals(map, startBlock.floor.navGraphChunk);
        Assert.assertEquals(map, targetBlock.floor.navGraphChunk);
        Assert.assertEquals(map.getBlock(x + 0, 51, z + 1), startBlock);
        Assert.assertEquals(map.getBlock(x + 30, 45, z + 28), targetBlock);
        Path path = pathfinder.findPath(targetBlock, startBlock);
        Assert.assertEquals(0, path.size());

        builder.setAir(x + 7, 50, z + 7);
        builder.setAir(x + 7, 50, z + 8);

        world.updateChunk(new Vector3i(chunkX, 0, chunkZ));
        pathfinder.clearCache();
        path = pathfinder.findPath(targetBlock, startBlock);
        Assert.assertTrue(0 < path.size());
    }

    @Before
    public void setup() {
        WorldProvidingHeadlessEnvironment env = new WorldProvidingHeadlessEnvironment(new Name("Pathfinding"));
        env.setupWorldProvider(new AbstractBaseWorldGenerator(new SimpleUri("")) {
            @Override
            public void initialize() {
                register(new PathfinderTestGenerator(true));
            }
        });
        builder = new TextWorldBuilder(env);

        world = new NavGraphSystem();
        InjectionHelper.inject(world);

        pathfinder = new Pathfinder(world, null);
    }

}
