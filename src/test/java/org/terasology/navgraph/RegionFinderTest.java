// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.navgraph;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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

import java.util.HashSet;
import java.util.Set;

/**
 * @author synopia
 */
@Tag("MteTest")
@ExtendWith(MTEExtension.class)
@Dependencies("Pathfinding")
public class RegionFinderTest {
    TextWorldBuilder builder;
    WorldProvider worldProvider;
    Vector3ic chunkLocation = new Vector3i(0, 0, 0);

    @Test
    public void testFindRegion() {
        assertRegions(new String[]{"X"}, new String[]{"0"});
        assertRegions(new String[]{"XX", "XX"}, new String[]{"00", "00"});
        assertRegions(new String[]{
            "XXX",
            "XX ",
            "XXX"
        }, new String[]{
            "000",
            "00 ",
            "000"
        });
        assertRegions(new String[]{
            "XXX",
            " XX",
            "XXX"
        }, new String[]{
            "000",
            " 00",
            "000"
        });
        assertRegions(new String[]{
            "XXX",
            "X X",
            "XXX"
        }, new String[]{
            "000",
            "1 2",
            "333"
        }, new int[][]{{1, 2}, {0, 3}, {0, 3}, {1, 2}});
        assertRegions(new String[]{
            "X X X",
            "XXXXX",
            "X X X"
        }, new String[]{
            "0 1 2",
            "33333",
            "4 5 6"
        }, new int[][]{{3}, {3}, {3}, {0, 1, 2, 4, 5, 6}, {3}, {3}, {3}});
        assertRegions(new String[]{
            "XXXXX",
            "XXXXX",
            "XXXXX"
        }, new String[]{
            "00000",
            "00000",
            "00000"
        });
    }

    @Test
    public void bigTest() {
        assertRegions(new String[]{
            "XXXXXXXXX|         |         ",
            "XXXXXXXXX|         |         ",
            "XXXXXXXXX|         |         ",
            "XXXXXXXXX|   XXX   |   XXX   ",
            "XXXXXXXXX|   XXX   |   XXX   ",
            "XXXXXXXXX|   XXX   |   XXX   ",
            "XXXXXXXXX|         |         ",
            "XXXXXXXXX|         |         ",
            "XXXXXXXXX|         |         ",
        }, new String[]{
            "000000000|         |         ",
            "000000000|         |         ",
            "000000000|         |         ",
            "111   333|         |   222   ",
            "111   333|         |   222   ",
            "111   333|         |   222   ",
            "444444444|         |         ",
            "444444444|         |         ",
            "444444444|         |         ",
        });
        assertRegions(new String[]{
            "XXXXXXXXX|         |         |XXXXXXXXX",
            "XXXXXXXXX|         |         |XXXXXXXXX",
            "XXXXXXXXX|         |         |XXXXXXXXX",
            "XXXXXXXXX|         |         |XXXXXXXXX",
            "XXXXXXXXX|         |         |XXXXXXXXX",
            "XXXXXXXXX|         |         |XXXXXXXXX",
            "XXXXXXXXX|         |         |XXXXXXXXX",
            "XXXXXXXXX|         |         |XXXXXXXXX",
            "XXXXXXXXX|         |         |XXXXXXXXX",
        }, new String[]{
            "000000000|         |         |111111111",
            "000000000|         |         |111111111",
            "000000000|         |         |111111111",
            "000000000|         |         |111111111",
            "000000000|         |         |111111111",
            "000000000|         |         |111111111",
            "000000000|         |         |111111111",
            "000000000|         |         |111111111",
            "000000000|         |         |111111111",
        });
        assertRegions(new String[]{
            "XXXXXXXXX|         |         |XXXXXXXXX",
            "XXXXXXXXX|         |         |XXXXXXXXX",
            "XXXXXXXXX|         |         |XXXXXXXXX",
            "XXXXXXXXX|         |         |XXX   XXX",
            "XXX   XXX|   XXX   |         |XXX   XXX",
            "XXX   XXX|         |   XXX   |XXX   XXX",
            "XXXXXXXXX|         |         |XXXXXXXXX",
            "XXXXXXXXX|         |         |XXXXXXXXX",
            "XXXXXXXXX|         |         |XXXXXXXXX",
        }, new String[]{
            "000000000|         |         |111111111",
            "000000000|         |         |111111111",
            "000000000|         |         |111111111",
            "000000000|         |         |222   333",
            "444   666|   555   |         |222   333",
            "444   666|         |   777   |777   777",
            "888888888|         |         |777777777",
            "888888888|         |         |777777777",
            "888888888|         |         |777777777",
        });
    }

    @BeforeEach
    public void setup(Context context, WorldProvider worldProvider, ModuleTestingHelper mteHelp) {
        builder = new TextWorldBuilder(context, mteHelp);
        this.worldProvider = worldProvider;
    }

    @AfterEach
    public void reset(){
        builder.reset();
    }

    private void assertRegions(String[] data, String[] regions) {
        assertRegions(data, regions, null);
    }

    private void assertRegions(String[] data, String[] regions, int[][] connections) {
        builder.setGround(data);
        final NavGraphChunk chunk = new NavGraphChunk(worldProvider, chunkLocation);
        new WalkableBlockFinder(worldProvider).findWalkableBlocks(chunk);
        final FloorFinder finder = new FloorFinder();
        finder.findRegions(chunk);
        String[] actual = builder.evaluate(new TextWorldBuilder.Runner() {
            @Override
            public char run(int x, int y, int z, char value) {
                WalkableBlock block = chunk.getBlock(x, y, z);
                if (block != null) {
                    Region region = finder.region(block);
                    return (char) ('0' + region.id);
                }
                return ' ';
            }
        });
        Assertions.assertArrayEquals(regions, actual);
        if (connections != null) {
            for (Region region : finder.regions()) {
                Set<Integer> all = new HashSet<Integer>();
                for (Region neighbor : region.getNeighborRegions()) {
                    all.add(neighbor.id);
                }
                for (int id : connections[region.id]) {
                    Assertions.assertTrue(all.remove(id), "region " + id + " not found in neighbors of region " + region.id);
                }
                Assertions.assertEquals(0, all.size());
            }
        }
    }
}
