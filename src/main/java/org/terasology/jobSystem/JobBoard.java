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
package org.terasology.jobSystem;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.selection.ApplyBlockSelectionEvent;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;

import java.util.Map;
import java.util.Set;

/**
 * @author synopia
 */
@RegisterSystem
public class JobBoard implements ComponentSystem, UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(JobBoard.class);
    private final Map<Job, JobType> jobTypes = Maps.newHashMap();

    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private EntityManager entityManager;

    @In
    private JobFactory jobFactory;

    private float cooldown = 5;

    public JobBoard() {
        logger.info("Creating JobBoard");
        CoreRegistry.put(JobBoard.class, this);
    }

    @Override
    public void update(float delta) {
        cooldown -= delta;
        if (cooldown < 0) {
            for (JobType type : jobTypes.values()) {
                String s = "";
                for (EntityRef openJob : type.openJobs) {
                    s += openJob + ", ";
                }
                logger.info(type.job.getUri() + ": " + type.openJobs.size() + "[" + s + "]");
            }
            cooldown = 5;
        }
    }

    @Override
    public void initialise() {
        logger.info("Initialize JobBoard");
    }

    @ReceiveEvent
    public void onActivated(OnActivatedComponent event, EntityRef entityRef, JobTargetComponent jobTarget) {
        logger.info("activated " + entityRef + " " + jobTarget);
        JobType jobType = getJobType(jobTarget.getJob());
        jobType.update(entityRef);
    }

    @ReceiveEvent
    public void onRemove(BeforeRemoveComponent event, EntityRef entityRef, JobTargetComponent jobTarget) {
        logger.info("removed " + entityRef + " " + jobTarget);
        JobType jobType = getJobType(jobTarget.getJob());
        jobType.remove(entityRef);
    }

    @ReceiveEvent
    public void onChange(OnChangedComponent event, EntityRef entityRef, JobTargetComponent jobTarget) {
        logger.info("changed " + entityRef + " " + jobTarget);
        JobType jobType = getJobType(jobTarget.getJob());
        jobType.update(entityRef);
    }

    public EntityRef getJob(EntityRef entity) {
        for (Job job : jobFactory.getJobs()) {
            JobType jobType = getJobType(job);
            if (jobType.openJobs.size() > 0) {
                return jobType.openJobs.iterator().next();
            }
        }
        return null;
    }

    public EntityRef getJob(EntityRef entity, Job job) {
        if (job == null) {
            return getJob(entity);
        }
        JobType jobType = getJobType(job);
        if (jobType.openJobs.size() > 0) {
            return jobType.openJobs.iterator().next();
        }
        return null;
    }

    @ReceiveEvent(components = {LocationComponent.class, CharacterComponent.class})
    public void onSelectionChanged(ApplyBlockSelectionEvent event, EntityRef entity) {
        Job job = jobFactory.getJob(event.getSelectedItemEntity());
        if (job == null) {
            return;
        }
        Region3i selection = event.getSelection();
        Vector3i size = selection.size();
        Vector3i block = new Vector3i();

        for (int z = 0; z < size.z; z++) {
            for (int y = 0; y < size.y; y++) {
                for (int x = 0; x < size.x; x++) {
                    block.set(x, y, z);
                    block.add(selection.min());
                    EntityRef blockEntity = blockEntityRegistry.getBlockEntityAt(block);
                    if (job.isAssignable(blockEntity)) {
                        JobTargetComponent jobTargetComponent = blockEntity.getComponent(JobTargetComponent.class);
                        if (jobTargetComponent != null) {
                            blockEntity.removeComponent(JobTargetComponent.class);
                        }
                        jobTargetComponent = new JobTargetComponent();
                        jobTargetComponent.setJob(job);
                        blockEntity.addComponent(jobTargetComponent);
                    }
                }
            }
        }
    }

    @Override
    public void shutdown() {
    }

    private JobType getJobType(Job job) {
        JobType jobType = jobTypes.get(job);
        if (jobType == null) {
            jobType = new JobType(job);
            jobTypes.put(job, jobType);
        }
        return jobType;
    }

    private final class JobType {
        public final Job job;
        public final Set<EntityRef> openJobs = Sets.newHashSet();

        private JobType(Job job) {
            this.job = job;
        }

        public void update(EntityRef jobEntity) {
            JobTargetComponent jobComponent = jobEntity.getComponent(JobTargetComponent.class);
            if (jobComponent != null && jobComponent.assignedMinion == null) {
                openJobs.add(jobEntity);
            } else {
                openJobs.remove(jobEntity);
            }
        }

        public void remove(EntityRef jobEntity) {
            openJobs.remove(jobEntity);
        }
    }

}
