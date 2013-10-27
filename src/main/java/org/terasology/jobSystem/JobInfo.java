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

/**
 * @author synopia
 */
public class JobInfo {
    private final JobType type;
    private final EntityRef block;
    private EntityRef assignee;

    public JobInfo(EntityRef block) {
        this.type = block.getComponent(JobBlockComponent.class).jobType;
        this.block = block;
    }

    public EntityRef getAssignee() {
        return assignee;
    }

    public void setAssignee(EntityRef assignee) {
        this.assignee = assignee;
    }

    public JobType getType() {
        return type;
    }

    public EntityRef getBlock() {
        return block;
    }
}
