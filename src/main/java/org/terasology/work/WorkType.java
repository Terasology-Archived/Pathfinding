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
package org.terasology.work;

import com.google.common.collect.Sets;
import org.terasology.entitySystem.entity.EntityRef;

import java.util.Set;

/**
 * Created by synopia on 08.02.14.
 */
public class WorkType {
    public final Work work;
    public final Set<EntityRef> openWork = Sets.newHashSet();

    public WorkType(Work work) {
        this.work = work;
    }

    public void update(EntityRef workEntity) {
        WorkTargetComponent workComponent = workEntity.getComponent(WorkTargetComponent.class);
        if (workComponent != null && workComponent.assignedMinion == null && work.isAssignable(workEntity)) {
            openWork.add(workEntity);
        } else {
            openWork.remove(workEntity);
        }
    }

    public void remove(EntityRef workEntity) {
        openWork.remove(workEntity);
    }
}
