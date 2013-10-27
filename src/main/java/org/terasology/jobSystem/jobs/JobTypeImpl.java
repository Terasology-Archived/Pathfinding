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
package org.terasology.jobSystem.jobs;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.jobSystem.JobType;
import org.terasology.math.Vector3i;

import java.util.List;

/**
 * @author synopia
 */
public enum JobTypeImpl implements JobType {
    IDLE(new JobType() {
        @Override
        public List<Vector3i> getTargetPositions(EntityRef block) {
            return Lists.newArrayList();
        }

        @Override
        public boolean canMinionWork(EntityRef block, EntityRef minion) {
            return false;
        }

        @Override
        public boolean isAssignable(EntityRef block) {
            return false;
        }

        @Override
        public void letMinionWork(EntityRef block, EntityRef minion) {
        }

        @Override
        public boolean isRequestable(EntityRef block) {
            return false;
        }
    }),
    WALK_ON_BLOCK(new WalkOnBlock()),
    BUILD_BLOCK(new BuildBlock());

    private JobType jobType;

    private JobTypeImpl(JobType jobType) {
        this.jobType = jobType;
    }

    public JobType getJobType() {
        return jobType;
    }


    @Override
    public List<Vector3i> getTargetPositions(EntityRef block) {
        return jobType.getTargetPositions(block);
    }

    @Override
    public boolean canMinionWork(EntityRef block, EntityRef minion) {
        return jobType.canMinionWork(block, minion);
    }

    @Override
    public boolean isAssignable(EntityRef block) {
        return jobType.isAssignable(block);
    }

    @Override
    public void letMinionWork(EntityRef block, EntityRef minion) {
        jobType.letMinionWork(block, minion);
    }

    @Override
    public boolean isRequestable(EntityRef block) {
        return jobType.isRequestable(block);
    }
}
