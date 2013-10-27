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

import com.google.common.collect.Lists;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.jobSystem.jobs.JobTypeImpl;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.selection.ApplyBlockSelectionEvent;
import org.terasology.world.BlockEntityRegistry;

import java.util.List;

/**
 * @author synopia
 */
@RegisterSystem
public class JobBoard implements ComponentSystem {
    @In
    private BlockEntityRegistry blockEntityRegistry;

    private List<JobBlockComponent> jobInfos = Lists.newArrayList();

    @Override
    public void initialise() {

    }


    @ReceiveEvent(components = {LocationComponent.class, CharacterComponent.class})
    public void onSelectionChanged(ApplyBlockSelectionEvent event, EntityRef entity) {
        event.getSelectedItemEntity();
        Region3i selection = event.getSelection();
        Vector3i size = selection.size();
        Vector3i block = new Vector3i();
        JobTypeImpl jobType = JobTypeImpl.BUILD_BLOCK;
        for (int z = 0; z < size.z; z++) {
            for (int y = 0; y < size.y; y++) {
                for (int x = 0; x < size.x; x++) {
                    block.set(x, y, z);
                    block.add(selection.min());
                    EntityRef blockEntity = blockEntityRegistry.getBlockEntityAt(block);
                    if (jobType.getJobType().isAssignable(blockEntity)) {
                        JobBlockComponent job = blockEntity.getComponent(JobBlockComponent.class);
                        if (job == null) {
                            job = new JobBlockComponent(jobType);
                            blockEntity.addComponent(job);
                        } else {
                            job.jobType = jobType;
                            blockEntity.saveComponent(job);
                        }
                    }
                }
            }
        }
    }

    public void removeJob(EntityRef block) {
        JobBlockComponent job = block.getComponent(JobBlockComponent.class);
        if (job != null) {
            jobInfos.remove(job);
            block.removeComponent(JobBlockComponent.class);
        }
    }

    @Override
    public void shutdown() {

    }
}
