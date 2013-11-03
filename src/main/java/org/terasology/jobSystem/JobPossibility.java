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
package org.terasology.jobSystem;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Vector3i;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.WalkableBlock;

/**
 * @author synopia
 */
public class JobPossibility {
    public Vector3i targetPos;
    public WalkableBlock targetBlock;
    public EntityRef targetEntity;
    public Job job;
    public Path path;
    public EntityRef minion;

    public JobPossibility() {

    }

    public JobPossibility(JobPossibility other) {
        targetPos = other.targetPos;
        targetBlock = other.targetBlock;
        targetEntity = other.targetEntity;
        job = other.job;
        path = other.path;
        minion = other.minion;
    }
}
