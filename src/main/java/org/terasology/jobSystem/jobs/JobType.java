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

import org.terasology.jobSystem.Job;

/**
 * @author synopia
 */
public enum JobType {
    WALK_ON_BLOCK(new WalkOnBlock()),
    BUILD_BLOCK(new BuildBlock());

    private Job job;

    private JobType(Job job) {
        this.job = job;
    }

    public Job getJob() {
        return job;
    }
}
