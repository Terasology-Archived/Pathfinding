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

import org.terasology.engine.SimpleUri;
import org.terasology.world.generator.AbstractBaseWorldGenerator;
import org.terasology.world.generator.RegisterWorldGenerator;

/**
 * @author synopia
 */
@RegisterWorldGenerator(id = "maze", displayName = "Maze")
public class MazeMapGenerator extends AbstractBaseWorldGenerator {

    public MazeMapGenerator(SimpleUri uri) {
        super(uri);
        MazeChunkGenerator generator = new MazeChunkGenerator(10, 10, 1, 50, 1);
        register(generator);
    }
}
