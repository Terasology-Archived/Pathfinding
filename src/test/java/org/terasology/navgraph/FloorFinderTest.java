// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.navgraph;

import org.joml.Vector3i;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.terasology.TextWorldBuilder;
import org.terasology.core.world.generator.AbstractBaseWorldGenerator;
import org.terasology.engine.WorldProvidingHeadlessEnvironment;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.gestalt.naming.Name;

import java.util.HashSet;
import java.util.Set;

/**
 * @author synopia
 */

public class FloorFinderTest {
    @Test
    public void stairs3() {
        assertFloors(new String[]{
            "XXXXXXXXX|         |         |         |         |XXXXXXXXX",
            "XXXXXXXXX|   XX    |         |         |         |XXX  XXXX",
            "XXXXXXXXX|         |   XX    |         |         |XXX  XXXX",
            "XXXXXXXXX|         |         |   XX    |         |XXX  XXXX",
            "XXXXXXXXX|         |         |         |   XX    |XXX  XXXX",
            "XXXXXXXXX|         |         |         |         |XXXXXXXXX",
            "XXXXXXXXX|         |         |         |         |XXXXXXXXX",
            "XXXXXXXXX|         |         |         |         |XXXXXXXXX",
        }, new String[]{
            "000000000|         |         |         |         |111111111",
            "000  0000|   00    |         |         |         |111  1111",
            "000  0000|         |   00    |         |         |111  1111",
            "222222222|         |         |   00    |         |111  1111",
            "222222222|         |         |         |   00    |000  0000",
            "222222222|         |         |         |         |000000000",
            "222222222|         |         |         |         |000000000",
            "222222222|         |         |         |         |000000000",
        }, new String[]{
            "IIIIIIIII|         |         |         |         |IIIIIIIII",
            "III  IIII|   II    |         |         |         |III  IIII",
            "CCC  CCCC|         |   II    |         |         |III  IIII",
            "CCCIICCCC|         |         |   II    |         |CCC  CCCC",
            "IIIIIIIII|         |         |         |   II    |CCC  CCCC",
            "IIIIIIIII|         |         |         |         |IIIIIIIII",
            "IIIIIIIII|         |         |         |         |IIIIIIIII",
            "IIIIIIIII|         |         |         |         |IIIIIIIII",
        }, new int[][]{{1, 2}, {0}, {0}});
    }

    @Test
    public void stairs2() {
        assertFloors(new String[]{
            "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
            "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
            "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
            "XXXXXXXXX|         |         |XXX XXXXX|         |         |XXXXX XXX",
            "XXX XXXXX|   X     |         |XXX XXXXX|     X   |         |XXXXX XXX",
            "XXX XXXXX|         |   X     |XXX XXXXX|         |     X   |XXXXX XXX",
            "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
            "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
        }, new String[]{
            "000000000|         |         |111111111|         |         |222222222",
            "000000000|         |         |111111111|         |         |222222222",
            "000000000|         |         |111111111|         |         |222222222",
            "000000000|         |         |111 11111|         |         |22222 222",
            "000 00000|   0     |         |111 1 111|     1   |         |22222 222",
            "000 00000|         |   1     |111 1 111|         |     2   |22222 222",
            "000000000|         |         |111111111|         |         |222222222",
            "000000000|         |         |111111111|         |         |222222222",
        }, new String[]{
            "IIIIIIIII|         |         |IIIIIIIII|         |         |IIIIIIIII",
            "IIIIIIIII|         |         |IIIIIIIII|         |         |IIIIIIIII",
            "IIIIIIIII|         |         |IIIIIIIII|         |         |IIIIIIIII",
            "IIIIIIIII|         |         |III IIIII|         |         |IIIII III",
            "III IIIII|   C     |         |III I III|     C   |         |IIIII III",
            "III IIIII|         |   C     |III I III|         |     C   |IIIII III",
            "IIIIIIIII|         |         |IIIIIIIII|         |         |IIIIIIIII",
            "IIIIIIIII|         |         |IIIIIIIII|         |         |IIIIIIIII",
        }, new int[][]{{1}, {0, 2}, {1}});
    }

    @Test
    public void stairsClosed2() {
        assertFloors(new String[]{
            "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
            "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
            "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
            "XXXXXXXXX|         |         |XXX XXXXX|         |         |XXXXX XXX",
            "XXX XXXXX|   X     |         |XXX XXXXX|     X   |         |XXXXX XXX",
            "XXX XXXXX|         |   X     |XXXXXXXXX|         |     X   |XXXXX XXX",
            "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
            "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
        }, new String[]{
            "000000000|         |         |111111111|         |         |222222222",
            "000000000|         |         |111111111|         |         |222222222",
            "000000000|         |         |111111111|         |         |222222222",
            "000000000|         |         |111 11111|         |         |22222 222",
            "000 00000|   0     |         |111 1 111|     1   |         |22222 222",
            "000 00000|         |         |11111 111|         |     2   |22222 222",
            "000000000|         |         |111111111|         |         |222222222",
            "000000000|         |         |111111111|         |         |222222222",
        }, new String[]{
            "IIIIIIIII|         |         |IIIIIIIII|         |         |IIIIIIIII",
            "IIIIIIIII|         |         |IIIIIIIII|         |         |IIIIIIIII",
            "IIIIIIIII|         |         |IIIIIIIII|         |         |IIIIIIIII",
            "IIIIIIIII|         |         |III IIIII|         |         |IIIII III",
            "III IIIII|   I     |         |III I III|     C   |         |IIIII III",
            "III IIIII|         |         |IIIII III|         |     C   |IIIII III",
            "IIIIIIIII|         |         |IIIIIIIII|         |         |IIIIIIIII",
            "IIIIIIIII|         |         |IIIIIIIII|         |         |IIIIIIIII",
        }, new int[][]{{}, {2}, {1}});
    }


    @Test
    public void testUnconnected() {
        assertFloors(new String[]{
            "XXX|   |   |XXX",
            "XXX|   |   |XXX",
            "XXX|   |   |XXX"
        }, new String[]{
            "000|   |   |111",
            "000|   |   |111",
            "000|   |   |111"
        }, new String[]{
            "III|   |   |III",
            "III|   |   |III",
            "III|   |   |III",
        }, new int[][]{{}, {}});
    }

    @Test
    public void testStairs() {
        assertFloors(new String[]{
            "XXXX|    |    |XXXX",
            "X XX| X  |  X |   X",
            "XXXX|    |    |XXXX"
        }, new String[]{
            "0000|    |    |1111",
            "0  2| 0  |  0 |   0",
            "0000|    |    |3333"
        }, new String[]{
            "IIIC|    |    |IICC",
            "I  C| I  |  C |   C",
            "IIIC|    |    |IICC"
        }, new int[][]{{1, 2, 3}, {0}, {0}, {0}});
    }

    @Test
    public void bigTest() {
        assertFloors(new String[]{
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
            "000   000|         |   111   ",
            "000   000|         |   111   ",
            "000   000|         |   111   ",
            "000000000|         |         ",
            "000000000|         |         ",
            "000000000|         |         ",
        }, new String[]{
            "IIIIIIIII|         |         ",
            "IIIIIIIII|         |         ",
            "IIIIIIIII|         |         ",
            "III   III|         |   III   ",
            "III   III|         |   III   ",
            "III   III|         |   III   ",
            "IIIIIIIII|         |         ",
            "IIIIIIIII|         |         ",
            "IIIIIIIII|         |         ",
        });
        assertFloors(new String[]{
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
        }, new String[]{
            "IIIIIIIII|         |         |IIIIIIIII",
            "IIIIIIIII|         |         |IIIIIIIII",
            "IIIIIIIII|         |         |IIIIIIIII",
            "IIIIIIIII|         |         |IIIIIIIII",
            "IIIIIIIII|         |         |IIIIIIIII",
            "IIIIIIIII|         |         |IIIIIIIII",
            "IIIIIIIII|         |         |IIIIIIIII",
            "IIIIIIIII|         |         |IIIIIIIII",
            "IIIIIIIII|         |         |IIIIIIIII",
        });
        assertFloors(new String[]{
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
            "000000000|         |         |111   111",
            "000   000|   000   |         |111   111",
            "000   000|         |   111   |111   111",
            "000000000|         |         |111111111",
            "000000000|         |         |111111111",
            "000000000|         |         |111111111",
        }, new String[]{
            "IIIIIIIII|         |         |IIIIIIIII",
            "IIIIIIIII|         |         |IIIIIIIII",
            "IIIIIIIII|         |         |IIIIIIIII",
            "IIIIIIIII|         |         |III   III",
            "III   III|   CCC   |         |III   III",
            "III   III|         |   CCC   |III   III",
            "IIIIIIIII|         |         |IIIIIIIII",
            "IIIIIIIII|         |         |IIIIIIIII",
            "IIIIIIIII|         |         |IIIIIIIII",
        });
    }


    @Test
    public void testSingleFloor() {
        assertFloors(new String[]{"X"}, new String[]{"0"});
        assertFloors(new String[]{"XX", "XX"}, new String[]{"00", "00"});
        assertFloors(new String[]{
            "XXX",
            "XX ",
            "XXX"
        }, new String[]{
            "000",
            "00 ",
            "000"
        });
        assertFloors(new String[]{
            "XXX",
            " XX",
            "XXX"
        }, new String[]{
            "000",
            " 00",
            "000"
        });
        assertFloors(new String[]{
            "XXX",
            "X X",
            "XXX"
        }, new String[]{
            "000",
            "0 0",
            "000"
        });
        assertFloors(new String[]{
            "X X X",
            "XXXXX",
            "X X X"
        }, new String[]{
            "0 0 0",
            "00000",
            "0 0 0"
        });
    }

    private void assertFloors(String[] data, String[] floors) {
        assertFloors(data, floors, null);
    }

    private void assertFloors(String[] data, String[] floors, String[] contour) {
        assertFloors(data, floors, contour, null);
    }

    private void assertFloors(String[] data, String[] floors, String[] contour, int[][] connections) {
        WorldProvidingHeadlessEnvironment env = new WorldProvidingHeadlessEnvironment(new Name("Pathfinding"));
        env.setupWorldProvider(new AbstractBaseWorldGenerator(new SimpleUri("")) {
            @Override
            public void initialize() {

            }
        });
        TextWorldBuilder builder = new TextWorldBuilder();
        builder.setGround(data);
        final NavGraphChunk chunk = new NavGraphChunk(CoreRegistry.get(WorldProvider.class), new Vector3i());

        new WalkableBlockFinder(CoreRegistry.get(WorldProvider.class)).findWalkableBlocks(chunk);
        final FloorFinder finder = new FloorFinder();
        finder.findFloors(chunk);
        chunk.findContour();
        String[] actual = builder.evaluate(new TextWorldBuilder.Runner() {
            @Override
            public char run(int x, int y, int z, char value) {
                WalkableBlock block = chunk.getBlock(x, y, z);
                if (block != null) {
                    return (char) ('0' + block.floor.id);
                }
                return ' ';
            }
        });
        Assertions.assertArrayEquals(floors, actual);
        if (contour != null) {
            actual = builder.evaluate(new TextWorldBuilder.Runner() {
                @Override
                public char run(int x, int y, int z, char value) {
                    WalkableBlock block = chunk.getBlock(x, y, z);
                    if (block != null) {
                        if (block.floor.isEntrance(block)) {
                            return 'C';
                        }
                        return 'I';
                    }
                    return ' ';
                }
            });
            Assertions.assertArrayEquals(contour, actual);
        }
        if (connections != null) {
            for (Floor floor : chunk.floors) {
                Set<Integer> all = new HashSet<Integer>();
                for (Floor neighbor : floor.getNeighborRegions()) {
                    all.add(neighbor.id);
                }
                for (int id : connections[floor.id]) {
                    Assertions.assertTrue(all.remove(id), "floor " + id + " not found in neighbors of floor " + floor.id);
                }
                Assertions.assertEquals(0, all.size(), "floor " + floor.id + " remains connections " + all.toString());
            }
        }
    }
}
