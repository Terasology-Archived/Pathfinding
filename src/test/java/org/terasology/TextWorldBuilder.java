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
package org.terasology;

import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.loader.BlockFamilyDefinitionData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by synopia on 11.02.14.
 */
public class TextWorldBuilder {
    private int sizeX;
    private int sizeY;
    private int sizeZ;
    private WorldProvider world;
    private Block ground;
    private Block air;

    public TextWorldBuilder(WorldProvidingHeadlessEnvironment environment) {
        world = CoreRegistry.get(WorldProvider.class);
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        AssetManager assetManager = CoreRegistry.get(AssetManager.class);

        BlockFamilyDefinitionData data = new BlockFamilyDefinitionData();
        data.setBlockFamily(SymmetricFamily.class);
        assetManager.loadAsset(new ResourceUrn("temp:ground"), data, BlockFamilyDefinition.class);
        this.ground = blockManager.getBlock("temp:ground");
        this.ground.setPenetrable(false);
        this.air = blockManager.getBlock(BlockManager.AIR_ID);
    }

    public void setGround(int x, int y, int z) {
        world.setBlock(new Vector3i(x, y, z), ground);
    }

    public void setAir(int x, int y, int z) {
        world.setBlock(new Vector3i(x, y, z), air);
    }

    public void setGround(String... lines) {
        parse(new Runner() {
            @Override
            public char run(int x, int y, int z, char value) {
                switch (value) {
                    case 'X':
                        setGround(x, y, z);
                        break;
                    case ' ':
                        setAir(x, y, z);
                        break;
                    default:
                        break;
                }
                return 0;
            }
        }, lines);
    }

    public String[] evaluate(Runner runner) {
        return evaluate(runner, 0, 0, 0, sizeX, sizeY, sizeZ);
    }

    public String[] evaluate(Runner runner, int xs, int ys, int zs, int sx, int sy, int sz) {
        String[][] table = new String[sy][sz];
        for (int y = 0; y < sy; y++) {
            for (int z = 0; z < sz; z++) {
                StringBuilder line = new StringBuilder();
                for (int x = 0; x < sx; x++) {
                    char value = runner.run(x + xs, y + ys, z + zs, (char) 0);
                    line.append(value);
                }
                table[y][z] = line.toString();
            }
        }
        return combine("|", table);
    }

    public void parse(Runner runner, String... lines) {
        String[][] expected = split("\\|", lines);
        sizeX = 0;
        sizeY = 0;
        sizeZ = 0;

        for (int y = 0; y < expected.length; y++) {
            if (y > sizeY) {
                sizeY = y;
            }
            for (int z = 0; z < expected[y].length; z++) {
                if (z > sizeZ) {
                    sizeZ = z;
                }
                String line = expected[y][z];
                for (int x = 0; x < line.length(); x++) {
                    if (x > sizeX) {
                        sizeX = x;
                    }
                    char c = line.charAt(x);
                    runner.run(x, y, z, c);
                }
            }
        }
        sizeX++;
        sizeY++;
        sizeZ++;
    }

    public static String[][] split(String separator, String... lines) {
        List<List<String>> table = new ArrayList<>();
        for (String line : lines) {
            if (line == null || line.length() == 0) {
                continue;
            }
            String[] parts = line.split(separator);
            for (int i = table.size(); i < parts.length; i++) {
                table.add(new ArrayList<String>());
            }
            for (int i = 0; i < parts.length; i++) {
                table.get(i).add(parts[i]);
            }
        }
        String[][] result = new String[table.size()][lines.length];
        for (int i = 0; i < table.size(); i++) {
            List<String> col = table.get(i);
            for (int j = 0; j < col.size(); j++) {
                result[i][j] = col.get(j);
            }
        }
        return result;
    }

    public static String[] combine(String separator, String[][] table) {
        String[] result = new String[table[0].length];
        for (int z = 0; z < table[0].length; z++) {
            StringBuilder line = new StringBuilder();
            for (int y = 0; y < table.length; y++) {
                if (y != 0) {
                    line.append(separator);
                }
                line.append(table[y][z]);
            }
            result[z] = line.toString();
        }
        return result;
    }

    public interface Runner {
        char run(int x, int y, int z, char value);
    }
}
