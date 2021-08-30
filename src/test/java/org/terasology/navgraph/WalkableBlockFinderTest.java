// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.navgraph;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.terasology.TextWorldBuilder;
import org.terasology.engine.context.Context;
import org.terasology.engine.world.WorldProvider;
import org.terasology.moduletestingenvironment.MTEExtension;
import org.terasology.moduletestingenvironment.ModuleTestingHelper;
import org.terasology.moduletestingenvironment.extension.Dependencies;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author synopia
 */
@Tag("MteTest")
@ExtendWith(MTEExtension.class)
@Dependencies("Pathfinding")
public class WalkableBlockFinderTest {

    TextWorldBuilder builder;
    WorldProvider worldProvider;

    Vector3ic chunkLocation = new Vector3i(0, 0, 0);

    @Test
    public void testNeighbors4() {
        builder.setGround(
                "XXX|   |   |",
                "XXX|X  |X  |",
                "XXX|   |   |"
        );
        final NavGraphChunk chunk = new NavGraphChunk(worldProvider, chunkLocation);
        chunk.update();

        WalkableBlock sut = chunk.getBlock(1, 0, 1);
        WalkableBlock lu = chunk.getBlock(0, 0, 0);
        WalkableBlock ld = chunk.getBlock(0, 0, 2);

        assertFalse(sut.hasNeighbor(lu));
        assertFalse(sut.hasNeighbor(ld));
    }

    @Test
    public void testNeighbors3() {
        builder.setGround(
                "XXXXXX|      |      |      |XXXXXX|",
                "XX    |  X   |   X  |    X |  X  X|",
                "XX    |  X   |   X  |    X |  X  X|",
                "XXXXXX|      |      |      |XXXXXX|"
        );
        final NavGraphChunk chunk = new NavGraphChunk(worldProvider, chunkLocation);
        chunk.update();

        WalkableBlock left = chunk.getBlock(2, 1, 1);
        WalkableBlock right = chunk.getBlock(3, 2, 1);

        assertFalse(left.hasNeighbor(right));
        assertFalse(right.hasNeighbor(left));
    }

    @Test
    public void testNeighbors2() {
        builder.setGround(
                " X ",
                "X X",
                " X "
        );
        final NavGraphChunk chunk = new NavGraphChunk(worldProvider, chunkLocation);
        chunk.update();

        WalkableBlock left = chunk.getBlock(0, 0, 1);
        WalkableBlock up = chunk.getBlock(1, 0, 0);
        WalkableBlock right = chunk.getBlock(2, 0, 1);
        WalkableBlock down = chunk.getBlock(1, 0, 2);

        assertTrue(left.hasNeighbor(up));
        assertTrue(left.hasNeighbor(down));
        assertFalse(left.hasNeighbor(right));

        assertTrue(up.hasNeighbor(left));
        assertTrue(up.hasNeighbor(right));
        assertFalse(up.hasNeighbor(down));

        assertTrue(right.hasNeighbor(up));
        assertTrue(right.hasNeighbor(down));
        assertFalse(right.hasNeighbor(left));

        assertTrue(down.hasNeighbor(left));
        assertTrue(down.hasNeighbor(right));
        assertFalse(down.hasNeighbor(up));
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
        WalkableBlockFinder finder = new WalkableBlockFinder(worldProvider);
        final NavGraphChunk chunk = new NavGraphChunk(worldProvider, chunkLocation);
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
        assertSame(left, block.neighbors[NavGraphChunk.DIR_LEFT]);
        assertSame(up, block.neighbors[NavGraphChunk.DIR_UP]);
        assertSame(right, block.neighbors[NavGraphChunk.DIR_RIGHT]);
        assertSame(down, block.neighbors[NavGraphChunk.DIR_DOWN]);
    }

    private void assertWalkableBlocks(String[] data, String[] walkable) {
        builder.setGround(data);
        WalkableBlockFinder finder = new WalkableBlockFinder(worldProvider);
        final NavGraphChunk chunk = new NavGraphChunk(worldProvider, chunkLocation);
        finder.findWalkableBlocks(chunk);

        String[] evaluate = builder.evaluate(new TextWorldBuilder.Runner() {
            @Override
            public char run(int x, int y, int z, char value) {
                return chunk.getBlock(x, y, z) == null ? ' ' : 'X';
            }
        });

        assertArrayEquals(walkable, evaluate);

    }

    @BeforeEach
    public void setup(Context context, WorldProvider worldProvider, ModuleTestingHelper mteHelp) {
        builder = new TextWorldBuilder(context);
        this.worldProvider = worldProvider;
        mteHelp.forceAndWaitForGeneration(chunkLocation);
    }

    @AfterEach
    public void reset(){
        builder.reset();
    }
}
