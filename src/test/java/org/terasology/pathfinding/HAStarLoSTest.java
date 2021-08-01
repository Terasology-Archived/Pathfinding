// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.pathfinding;

import org.joml.Vector3i;
import org.joml.Vector3ic;
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
import org.terasology.navgraph.NavGraphChunk;
import org.terasology.navgraph.WalkableBlock;
import org.terasology.pathfinding.model.HAStar;
import org.terasology.pathfinding.model.LineOfSight2d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author synopia
 */
@Tag("MteTest")
@ExtendWith(MTEExtension.class)
@Dependencies("Pathfinding")
public class HAStarLoSTest {
    private WalkableBlock start;
    private WalkableBlock end;
    TextWorldBuilder builder;
    WorldProvider worldProvider;
    Vector3ic chunkLocation = new Vector3i(0, 0, 0);

    @Test
    public void flat() {
        executeExample(new String[]{
                "XXXXXXXXX",
                "XXXXXXXXX",
                "XXXXXXXXX",
                "XXXXXXXXX",
                "XXXXXXXXX",
                "XXXXXXXXX",
                "XXXXXXXXX",
                "XXXXXXXXX",
        }, new String[]{
                "?        ",
                "         ",
                "         ",
                "         ",
                "         ",
                "         ",
                "         ",
                "        !",
        });

    }

    @Test
    public void stairs2() {
        executeExample(new String[]{
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXX XXXXX|         |         |XXXXX XXX",
                "XXX XXXXX|   X     |         |XXX XXXXX|     X   |         |XXXXX XXX",
                "XXX XXXXX|         |   X     |XXX XXXXX|         |     X   |XXXXX XXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
        }, new String[]{
                "?        |         |         |         |         |         |         ",
                "         |         |         |         |         |         |         ",
                "         |         |         |         |         |         |         ",
                "   1     |         |         |     4   |         |         |         ",
                "         |         |         |         |         |         |         ",
                "         |         |   2     |    3    |         |     5   |         ",
                "         |         |         |         |         |         |         ",
                "         |         |         |         |         |         |        !",
        });
    }

    @Test
    public void stairsClosed2() {
        executeExample(new String[]{
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXX XXXXX|         |         |XXXXX XXX",
                "XXX XXXXX|   X     |         |XXX XXXXX|     X   |         |XXXXX XXX",
                "XXX XXXXX|         |   X     |XXXXXXXXX|         |     X   |XXXXX XXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
        }, new String[]{
                "?        |         |         |         |         |         |         ",
                "         |         |         |         |         |         |         ",
                "         |         |         |         |         |         |         ",
                "         |         |         |         |         |         |         ",
                "         |         |         |         |         |         |         ",
                "         |         |         |         |         |         |         ",
                "         |         |         |         |         |         |         ",
                "         |         |         |         |         |         |        !",
        });
    }

    @Test
    public void simple() {
        executeExample(new String[]{
                "XXX",
                "XXX",
                "XXX",
        }, new String[]{
                "?  ",
                "   ",
                "  !"
        });
        executeExample(new String[]{
                "XXXXXXXXXXXXXXXX",
                "             XXX",
                "XXXXXXXXXXXXXXXX",
                "XXX             ",
                "XXXXXXXXXXXXXXXX",
                "             XXX",
        }, new String[]{
                "?               ",
                "             1  ",
                "            2  ",
                "  3             ",
                "   4            ",
                "               !",
        });

    }

    @BeforeEach
    public void setup(Context context, WorldProvider worldProvider, ModuleTestingHelper mteHelp) {
        builder = new TextWorldBuilder(context);
        this.worldProvider = worldProvider;
        mteHelp.forceAndWaitForGeneration(chunkLocation);
    }

    private void executeExample(String[] ground, String[] pathData) {
        builder.setGround(ground);
        final NavGraphChunk chunk = new NavGraphChunk(worldProvider, chunkLocation);
        chunk.update();

        final Map<Integer, Vector3i> expected = new HashMap<Integer, Vector3i>();
        builder.parse(new TextWorldBuilder.Runner() {
            @Override
            public char run(int x, int y, int z, char value) {

                switch (value) {
                    case '?':
                        start = chunk.getBlock(x, y, z);
                        break;
                    case '!':
                        end = chunk.getBlock(x, y, z);
                        break;
                    default:
                        if (value == '0') {
                            expected.put(10, chunk.getBlock(x, y, z).getBlockPosition());
                        } else if (value > '0' && value <= '9') {
                            expected.put(value - '0', chunk.getBlock(x, y, z).getBlockPosition());
                        } else if (value >= 'a' && value <= 'z') {
                            expected.put(value - 'a' + 11, chunk.getBlock(x, y, z).getBlockPosition());
                        } else if (value >= 'A' && value <= 'Z') {
                            expected.put(value - 'A' + 11 + 27, chunk.getBlock(x, y, z).getBlockPosition());
                        }
                        break;
                }
                return 0;
            }
        }, pathData);
        expected.put(expected.size() + 1, end.getBlockPosition());

        HAStar haStar = new HAStar(new LineOfSight2d());

        haStar.run(end, start);
        List<WalkableBlock> path = haStar.getPath().getNodes();
        int pos = 1;
        Assertions.assertEquals(expected.size(), path.size());
        for (WalkableBlock block : path) {
            Vector3i p = expected.get(pos);
            Assertions.assertEquals(p, block.getBlockPosition());
            pos++;
        }
    }

}
