/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.pathfinding.model;

import org.terasology.config.Config;
import org.terasology.engine.CoreRegistry;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.world.WorldChangeListener;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.internal.ChunkImpl;
import org.terasology.world.generator.FirstPassGenerator;
import org.terasology.world.internal.ChunkViewCore;
import org.terasology.world.internal.WorldInfo;
import org.terasology.world.internal.WorldProviderCore;
import org.terasology.world.internal.WorldProviderWrapper;
import org.terasology.world.liquid.LiquidData;
import org.terasology.world.time.WorldTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author synopia
 */
public class TestHelper {
    public HeightMap map;
    public WorldProvider world;
    public int sizeX;
    public int sizeY;
    public int sizeZ;
    public Block ground;

    public TestHelper() {
        CoreRegistry.put(Config.class, new Config());
        ground = new Block();
        ground.setPenetrable(false);
        ground.setUri(new BlockUri("test:ground"));
        ground.setId((short) 1);
        CoreRegistry.put(BlockManager.class, new BlockManager() {
            @Override
            public List<BlockUri> resolveAllBlockFamilyUri(String uri) {
                return null;
            }

            @Override
            public BlockUri resolveBlockFamilyUri(String name) {
                return null;
            }

            @Override
            public Map<String, Short> getBlockIdMap() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Iterable<BlockUri> getBlockFamiliesWithCategory(String category) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Iterable<String> getBlockCategories() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public BlockFamily getBlockFamily(String uri) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public BlockFamily getBlockFamily(BlockUri uri) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Block getBlock(String uri) {
                return ground;
            }

            @Override
            public Block getBlock(BlockUri uri) {
                return ground;
            }

            @Override
            public Block getBlock(short id) {
                return id == 0 ? getAir() : ground;
            }

            @Override
            public Iterable<BlockUri> listRegisteredBlockUris() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Iterable<BlockFamily> listRegisteredBlockFamilies() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Iterable<BlockUri> listFreeformBlockUris() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean isFreeformFamily(BlockUri familyUri) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Iterable<BlockFamily> listAvailableBlockFamilies() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public BlockFamily getAvailableBlockFamily(BlockUri uri) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Iterable<BlockUri> listAvailableBlockUris() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getBlockFamilyCount() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean hasBlockFamily(BlockUri uri) {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Iterable<Block> listRegisteredBlocks() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }

    public void init() {
        init(null);
    }

    public void init(FirstPassGenerator generator) {
        WorldProviderCore worldStub = new TestWorld(BlockManager.getAir(), generator);
        world = new WorldProviderWrapper(worldStub);
        map = new HeightMap(world, new Vector3i(0, 0, 0));

    }

    public void setGround(int x, int y, int z) {
        world.setBlock(new Vector3i(x, y, z), ground);
    }

    public void setAir(int x, int y, int z) {
        world.setBlock(new Vector3i(x, y, z), BlockManager.getAir());
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

    public void run() {
        map.update();
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

    public static class TestWorld implements WorldProviderCore {
        private Map<Vector3i, Block> blocks = new HashMap<>();
        private Map<Vector3i, Chunk> chunks = new HashMap<>();
        private FirstPassGenerator chunkGenerator;
        private Block air;

        public TestWorld(Block air, FirstPassGenerator chunkGenerator) {
            this.air = air;
            this.chunkGenerator = chunkGenerator;
        }

        @Override
        public void processPropagation() {
        }

        @Override
        public void registerListener(WorldChangeListener listener) {
        }

        @Override
        public void unregisterListener(WorldChangeListener listener) {
        }

        @Override
        public boolean isBlockRelevant(int x, int y, int z) {
            return false;
        }

        @Override
        public Block setBlock(Vector3i pos, Block type) {
            return blocks.put(pos, type);
        }

        @Override
        public float getFog(float x, float y, float z) {
            return 0;
        }


        @Override
        public Block getBlock(int x, int y, int z) {
            Vector3i pos = new Vector3i(x, y, z);
            Block block = blocks.get(pos);
            if (block != null) {
                return block;
            }
            Vector3i chunkPos = TeraMath.calcChunkPos(pos);
            Chunk chunk = chunks.get(chunkPos);
            if (chunk == null && chunkGenerator != null) {
                chunk = new ChunkImpl(chunkPos);
                chunkGenerator.generateChunk(chunk);
                chunks.put(chunkPos, chunk);
            }
            if (chunk != null) {
                return chunk.getBlock(TeraMath.calcBlockPos(pos.x, pos.y, pos.z));
            }
            return air;
        }

        @Override
        public String getTitle() {
            return "";
        }

        @Override
        public String getSeed() {
            return "1";
        }

        @Override
        public WorldInfo getWorldInfo() {
            return null;

        }

        @Override
        public ChunkViewCore getLocalView(Vector3i chunkPos) {
            return null;
        }

        @Override
        public ChunkViewCore getWorldViewAround(Vector3i chunk) {
            return null;
        }

        @Override
        public boolean setLiquid(int x, int y, int z, LiquidData newData, LiquidData oldData) {
            return false;
        }

        @Override
        public LiquidData getLiquid(int x, int y, int z) {
            return null;
        }

        @Override
        public byte getLight(int x, int y, int z) {
            return 0;
        }

        @Override
        public byte getSunlight(int x, int y, int z) {
            return 0;
        }

        @Override
        public byte getTotalLight(int x, int y, int z) {
            return 0;
        }

        @Override
        public WorldTime getTime() {
            return null;
        }

        @Override
        public void dispose() {
        }

        @Override
        public float getTemperature(float x, float y, float z) {
            return 0;
        }

        @Override
        public float getHumidity(float x, float y, float z) {
            return 0;
        }
    }

    public interface Runner {
        char run(int x, int y, int z, char value);
    }


}
