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
package org.terasology.pathfinding;

import org.junit.Assert;
import org.junit.Test;
import org.terasology.TextWorldBuilder;
import org.terasology.WorldProvidingHeadlessEnvironment;
import org.terasology.core.world.generator.AbstractBaseWorldGenerator;
import org.terasology.engine.SimpleUri;
import org.terasology.math.geom.Vector3i;
import org.terasology.naming.Name;
import org.terasology.navgraph.NavGraphChunk;
import org.terasology.navgraph.WalkableBlock;
import org.terasology.pathfinding.model.HAStar;
import org.terasology.pathfinding.model.LineOfSight;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.WorldProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author synopia
 */
public class HAStarTest {
    private WalkableBlock start;
    private WalkableBlock end;

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
                " 1       |         |         |         |         |         |         ",
                "  2      |         |         |         |         |         |         ",
                "   3     |         |         |    89   |         |         |         ",
                "         |   4     |         |    7    |     0   |         |         ",
                "         |         |   5     |    6    |         |     a   |      b  ",
                "         |         |         |         |         |         |       c ",
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
                " 1 ",
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
                "?1234567890ab   ",
                "             c  ",
                "   mlkjihgfed  ",
                "  n             ",
                "   opqrstuvwxyz ",
                "               !",
        });

    }

    private void executeExample(String[] ground, String[] pathData) {
        WorldProvidingHeadlessEnvironment env = new WorldProvidingHeadlessEnvironment(new Name("Pathfinding"));
        env.setupWorldProvider(new AbstractBaseWorldGenerator(new SimpleUri("")) {
            @Override
            public void initialize() {

            }
        });
        TextWorldBuilder builder = new TextWorldBuilder(env);
        builder.setGround(ground);
        final NavGraphChunk chunk = new NavGraphChunk(CoreRegistry.get(WorldProvider.class), new Vector3i());
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

        HAStar haStar = new HAStar(new LineOfSight() {
            @Override
            public void initialise() {

            }

            @Override
            public void shutdown() {

            }

            @Override
            public boolean inSight(WalkableBlock one, WalkableBlock two) {
                return false;
            }

            @Override
            public void preBegin() {

            }

            @Override
            public void postBegin() {

            }

            @Override
            public void preSave() {

            }

            @Override
            public void postSave() {

            }
        });

        haStar.run(end, start);
        List<WalkableBlock> path = haStar.getPath();
        int pos = 1;
        Assert.assertEquals(expected.size(), path.size());
        for (WalkableBlock block : path) {
            Vector3i p = expected.get(pos);
            Assert.assertEquals(p, block.getBlockPosition());
            pos++;
        }
    }

}
