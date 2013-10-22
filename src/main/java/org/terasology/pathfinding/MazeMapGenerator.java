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
package org.terasology.pathfinding;

import org.terasology.world.generator.BaseMapGenerator;
import org.terasology.world.generator.MapGeneratorUri;

/**
 * @author synopia
 */
public class MazeMapGenerator extends BaseMapGenerator {
    public MazeMapGenerator() {
        super(new MapGeneratorUri("pathfinding:maze"));
    }

    @Override
    public String name() {
        return "Maze";
    }

    @Override
    public void setup() {
        MazeChunkGenerator generator = new MazeChunkGenerator(10, 10, 1, 50, 1);
        registerChunkGenerator(generator);
    }

    @Override
    public boolean hasSetup() {
        return false;
    }
}
