/*
 * Copyright 2014 MovingBlocks
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

import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.pathfinding.model.WalkableBlock;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.ForceBlockActive;

import java.util.List;

/**
 * Job's block component. Using this component, jobs can be assigned to individual blocks
 *
 * @author synopia
 */
@ForceBlockActive
public class JobBlockComponent implements Component, Job {
    public String jobUri;
    public transient EntityRef assignedMinion;
    private transient Job job;

    public JobBlockComponent() {
    }

    public void setJob(Job job) {
        this.job = job;
        this.jobUri = job.getUri().toString();
    }

    public Job getJob() {
        if (job == null) {
            job = CoreRegistry.get(JobFactory.class).getJob(jobUri);
        }
        return job;
    }

    @Override
    public List<WalkableBlock> getTargetPositions(EntityRef block) {
        return getJob().getTargetPositions(block);
    }

    @Override
    public boolean canMinionWork(EntityRef block, EntityRef minion) {
        return getJob().canMinionWork(block, minion);
    }

    @Override
    public boolean isAssignable(EntityRef block) {
        return getJob().isAssignable(block);
    }

    @Override
    public void letMinionWork(EntityRef block, EntityRef minion) {
        getJob().letMinionWork(block, minion);
    }

    @Override
    public boolean isRequestable(EntityRef block) {
        return getJob().isRequestable(block);
    }

    @Override
    public SimpleUri getUri() {
        return getJob().getUri();
    }
}
