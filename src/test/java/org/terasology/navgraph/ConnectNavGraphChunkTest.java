// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.navgraph;

import org.joml.Vector3i;
import org.joml.Vector3ic;
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author synopia
 */
@Tag("MteTest")
@ExtendWith(MTEExtension.class)
@Dependencies("Pathfinding")
public class ConnectNavGraphChunkTest {
    TextWorldBuilder builder;
    WorldProvider world;
    Vector3ic chunkLocation = new Vector3i(0, 0, 0);

    private static final String[] CONTOUR_EXPECTED = new String[]{
            "               C                ",
            "                                ",
            "                                ",
            "                                ",
            "                                ",
            "                                ",
            "                                ",
            "                                ",
            "                                ",
            "                                ",
            "                                ",
            "                                ",
            "                                ",
            "                                ",
            "                                ",
            "                               C",
            "C                               ",
            "                                ",
            "                                ",
            "                                ",
            "                                ",
            "                                ",
            "                                ",
            "                                ",
            "                                ",
            "                                ",
            "                                ",
            "                                ",
            "                                ",
            "                                ",
            "                                ",
            "                C               ",
    };

    @Test
    public void test1() {
        NavGraphChunk center = new NavGraphChunk(world, new Vector3i(1, 0, 1));
        NavGraphChunk up = new NavGraphChunk(world, new Vector3i(1, 0, 0));
        NavGraphChunk down = new NavGraphChunk(world, new Vector3i(1, 0, 2));
        NavGraphChunk left = new NavGraphChunk(world, new Vector3i(0, 0, 1));
        NavGraphChunk right = new NavGraphChunk(world, new Vector3i(2, 0, 1));

        center.update();
        up.update();
        down.update();
        left.update();
        right.update();

        center.connectNeighborMaps(left, up, right, down);
        assertCenter(center, left, up, right, down, CONTOUR_EXPECTED);

        center.disconnectNeighborMaps(left, up, right, down);
        center = new NavGraphChunk(world, new Vector3i(1, 0, 1));
        center.update();
        center.connectNeighborMaps(left, up, right, down);
        assertCenter(center, left, up, right, down, CONTOUR_EXPECTED);
    }

    @Test
    public void test2() {
        NavGraphChunk center = new NavGraphChunk(world, new Vector3i(1, 0, 1));
        NavGraphChunk up = new NavGraphChunk(world, new Vector3i(1, 0, 0));
        NavGraphChunk down = new NavGraphChunk(world, new Vector3i(1, 0, 2));
        NavGraphChunk left = new NavGraphChunk(world, new Vector3i(0, 0, 1));
        NavGraphChunk right = new NavGraphChunk(world, new Vector3i(2, 0, 1));
        NavGraphChunk lu = new NavGraphChunk(world, new Vector3i(0, 0, 0));
        NavGraphChunk ru = new NavGraphChunk(world, new Vector3i(2, 0, 0));
        NavGraphChunk ld = new NavGraphChunk(world, new Vector3i(0, 0, 2));
        NavGraphChunk rd = new NavGraphChunk(world, new Vector3i(2, 0, 2));


        lu.update();
        lu.connectNeighborMaps(null, null, null, null);
        up.update();
        up.connectNeighborMaps(lu, null, null, null);

        ru.update();
        ru.connectNeighborMaps(up, null, null, null);

        left.update();
        left.connectNeighborMaps(null, lu, null, null);
        center.update();
        center.connectNeighborMaps(left, up, null, null);
        right.update();
        right.connectNeighborMaps(center, ru, null, null);

        ld.update();
        ld.connectNeighborMaps(null, left, null, null);
        down.update();
        down.connectNeighborMaps(ld, center, null, null);
        rd.update();
        rd.connectNeighborMaps(down, right, null, null);

        assertCenter(center, left, up, right, down, CONTOUR_EXPECTED);
    }

    @BeforeEach
    public void setup(Context context, WorldProvider worldProvider, ModuleTestingHelper mteHelp) {
        builder = new TextWorldBuilder(context);
        world = worldProvider;
        mteHelp.forceAndWaitForGeneration(chunkLocation);
    }

    private void assertCenter(final NavGraphChunk center, NavGraphChunk left, NavGraphChunk up, NavGraphChunk right, NavGraphChunk down, String[] contours) {
        final Floor centerFloor = center.getFloor(0);
        Floor upFloor = up.getFloor(0);
        Floor downFloor = down.getFloor(0);
        Floor leftFloor = left.getFloor(0);
        Floor rightFloor = right.getFloor(0);
        assertSet(centerFloor.getNeighborRegions(), upFloor, leftFloor, rightFloor, downFloor);

        if (contours != null) {
            String[] actual = builder.evaluate(new TextWorldBuilder.Runner() {
                @Override
                public char run(int x, int y, int z, char value) {
                    return isEntrance(center.getCell(x, z).getBlock(y)) ? 'C' : ' ';
                }
            }, 0, 51, 0, 32, 1, 32);
            assertArrayEquals(contours, actual);
        }
    }


    private <T> void assertSet(Set<T> set, T... items) {
        Set<T> rest = new HashSet<T>(set);
        for (T item : items) {
            assertTrue(rest.remove(item));
        }
        assertEquals(0, rest.size());

    }

    private boolean isEntrance(WalkableBlock block) {
        if (block == null) {
            return false;
        }
        boolean isEntrance = false;
        for (Entrance entrance : block.floor.entrances()) {
            if (entrance.getAbstractBlock() == block) {
                isEntrance = true;
                break;
            }
        }
        return isEntrance;
    }
}
