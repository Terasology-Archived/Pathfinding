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
package org.terasology.navgraph;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.terasology.TextWorldBuilder;
import org.terasology.WorldProvidingHeadlessEnvironment;
import org.terasology.core.world.generator.AbstractBaseWorldGenerator;
import org.terasology.engine.SimpleUri;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.WorldProvider;

/**
 * @author synopia
 */
public class WalkableBlockFinderTest {

    private TextWorldBuilder builder;

    @Test
    public void testNeighbors4() {
        builder.setGround(
                "XXX|   |   |",
                "XXX|X  |X  |",
                "XXX|   |   |"
        );
        final NavGraphChunk chunk = new NavGraphChunk(CoreRegistry.get(WorldProvider.class), new Vector3i());
        chunk.update();

        WalkableBlock sut = chunk.getBlock(1, 0, 1);
        WalkableBlock lu = chunk.getBlock(0, 0, 0);
        WalkableBlock ld = chunk.getBlock(0, 0, 2);

        Assert.assertFalse(sut.hasNeighbor(lu));
        Assert.assertFalse(sut.hasNeighbor(ld));
    }

    @Test
    public void testNeighbors3() {
        builder.setGround(
                "XXXXXX|      |      |      |XXXXXX|",
                "XX    |  X   |   X  |    X |  X  X|",
                "XX    |  X   |   X  |    X |  X  X|",
                "XXXXXX|      |      |      |XXXXXX|"
        );
        final NavGraphChunk chunk = new NavGraphChunk(CoreRegistry.get(WorldProvider.class), new Vector3i());
        chunk.update();

        WalkableBlock left = chunk.getBlock(2, 1, 1);
        WalkableBlock right = chunk.getBlock(3, 2, 1);

        Assert.assertFalse(left.hasNeighbor(right));
        Assert.assertFalse(right.hasNeighbor(left));
    }

    @Test
    public void testNeighbors2() {
        builder.setGround(
                " X ",
                "X X",
                " X "
        );
        final NavGraphChunk chunk = new NavGraphChunk(CoreRegistry.get(WorldProvider.class), new Vector3i());
        chunk.update();

        WalkableBlock left = chunk.getBlock(0, 0, 1);
        WalkableBlock up = chunk.getBlock(1, 0, 0);
        WalkableBlock right = chunk.getBlock(2, 0, 1);
        WalkableBlock down = chunk.getBlock(1, 0, 2);

        Assert.assertTrue(left.hasNeighbor(up));
        Assert.assertTrue(left.hasNeighbor(down));
        Assert.assertFalse(left.hasNeighbor(right));

        Assert.assertTrue(up.hasNeighbor(left));
        Assert.assertTrue(up.hasNeighbor(right));
        Assert.assertFalse(up.hasNeighbor(down));

        Assert.assertTrue(right.hasNeighbor(up));
        Assert.assertTrue(right.hasNeighbor(down));
        Assert.assertFalse(right.hasNeighbor(left));

        Assert.assertTrue(down.hasNeighbor(left));
        Assert.assertTrue(down.hasNeighbor(right));
        Assert.assertFalse(down.hasNeighbor(up));
    }

    @Test
    public void testFind1() {
        assertWalkableBlocks(new String[]{
                "XXX",
                "XXX",
                "XXX"
        }, new String[]{
                "XXX",
                "XXX",
                "XXX"
        });
    }

    @Test
    public void testFind2() {
        assertWalkableBlocks(new String[]{
                "XXX|   |   |XXX",
                "XXX|   |   |XXX",
                "XXX|   |   |XXX"
        }, new String[]{
                "XXX|   |   |XXX",
                "XXX|   |   |XXX",
                "XXX|   |   |XXX"
        });
    }

    @Test
    public void testFind3() {
        assertWalkableBlocks(new String[]{
                "XXX|   |XXX",
                "XXX|   |XXX",
                "XXX|   |XXX"
        }, new String[]{
                "   |   |XXX",
                "   |   |XXX",
                "   |   |XXX"
        });
    }

    @Test
    public void testFind4() {
        assertWalkableBlocks(new String[]{
                "XXX|   |XXX",
                "XXX|   |X X",
                "XXX|   |XXX"
        }, new String[]{
                "   |   |XXX",
                " X |   |X X",
                "   |   |XXX"
        });
    }

    @Test
    public void testNeighbors() {
        assertNeighbors3x3(
                "XXX",
                "XXX",
                "XXX"
        );
        assertNeighbors3x3(
                "XXX|   ",
                "XXX| X ",
                "XXX|   "
        );
        assertNeighbors3x3(
                "XXX|   ",
                "X X| X ",
                "XXX|   "
        );
        assertNeighbors3x3(
                " X |X X",
                "X X| X ",
                " X |X X"
        );
    }

    private void assertNeighbors3x3(String... data) {
        builder.setGround(data);
        WalkableBlockFinder finder = new WalkableBlockFinder(CoreRegistry.get(WorldProvider.class));
        final NavGraphChunk chunk = new NavGraphChunk(CoreRegistry.get(WorldProvider.class), new Vector3i());
        finder.findWalkableBlocks(chunk);

        WalkableBlock lu = chunk.getCell(0, 0).blocks.get(0);
        WalkableBlock up = chunk.getCell(1, 0).blocks.get(0);
        WalkableBlock ru = chunk.getCell(2, 0).blocks.get(0);
        WalkableBlock left = chunk.getCell(0, 1).blocks.get(0);
        WalkableBlock center = chunk.getCell(1, 1).blocks.get(0);
        WalkableBlock right = chunk.getCell(2, 1).blocks.get(0);
        WalkableBlock ld = chunk.getCell(0, 2).blocks.get(0);
        WalkableBlock down = chunk.getCell(1, 2).blocks.get(0);
        WalkableBlock rd = chunk.getCell(2, 2).blocks.get(0);

        assertNeighbors(lu, null, null, up, left);
        assertNeighbors(up, lu, null, ru, center);
        assertNeighbors(ru, up, null, null, right);
        assertNeighbors(left, null, lu, center, ld);
        assertNeighbors(center, left, up, right, down);
        assertNeighbors(right, center, ru, null, rd);
        assertNeighbors(ld, null, left, down, null);
        assertNeighbors(down, ld, center, rd, null);
        assertNeighbors(rd, down, right, null, null);
    }

    private void assertNeighbors(WalkableBlock block, WalkableBlock left, WalkableBlock up, WalkableBlock right, WalkableBlock down) {
        Assert.assertSame(left, block.neighbors[NavGraphChunk.DIR_LEFT]);
        Assert.assertSame(up, block.neighbors[NavGraphChunk.DIR_UP]);
        Assert.assertSame(right, block.neighbors[NavGraphChunk.DIR_RIGHT]);
        Assert.assertSame(down, block.neighbors[NavGraphChunk.DIR_DOWN]);
    }

    private void assertWalkableBlocks(String[] data, String[] walkable) {
        builder.setGround(data);
        WalkableBlockFinder finder = new WalkableBlockFinder(CoreRegistry.get(WorldProvider.class));
        final NavGraphChunk chunk = new NavGraphChunk(CoreRegistry.get(WorldProvider.class), new Vector3i());
        finder.findWalkableBlocks(chunk);

        String[] evaluate = builder.evaluate(new TextWorldBuilder.Runner() {
            @Override
            public char run(int x, int y, int z, char value) {
                return chunk.getBlock(x, y, z) == null ? ' ' : 'X';
            }
        });

        Assert.assertArrayEquals(walkable, evaluate);

    }

    @Before
    public void setup() {
        WorldProvidingHeadlessEnvironment env = new WorldProvidingHeadlessEnvironment();
        env.setupWorldProvider(new AbstractBaseWorldGenerator(new SimpleUri("")) {
            @Override
            public void initialize() {

            }
        });
        builder = new TextWorldBuilder(env);
    }
}
