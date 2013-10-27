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

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.jobSystem.jobs.JobTypeImpl;
import org.terasology.world.block.ForceBlockActive;

/**
 * JobType's block component. Using this component, jobs can be assigned to individual blocks
 *
 * @author synopia
 */
@ForceBlockActive
public class JobBlockComponent implements Component {
    public enum JobBlockState {
        UNASSIGNED,
        PATHS_REQUESTED,
        ASSIGNED
    }

    public JobTypeImpl jobType = JobTypeImpl.IDLE;
    public transient EntityRef assignedMinion;

    public transient JobBlockState state = JobBlockComponent.JobBlockState.UNASSIGNED;

    public JobBlockComponent() {
    }

    public JobBlockComponent(JobTypeImpl jobType) {
        this.jobType = jobType;
    }
}
