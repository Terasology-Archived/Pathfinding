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
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.model.Entrance;
import org.terasology.pathfinding.model.Floor;
import org.terasology.pathfinding.model.WalkableBlock;
import org.terasology.selection.ApplyBlockSelectionEvent;
import org.terasology.world.BlockEntityRegistry;

import java.util.List;
import java.util.Map;

/**
 * @author synopia
 */
@RegisterSystem
public class JobBoard implements ComponentSystem, UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(JobBoard.class);
    private final Map<Job, JobType> jobTypes = Maps.newHashMap();
    private final Map<WalkableBlock, EntityRef> jobMap = Maps.newHashMap();

    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private EntityManager entityManager;

    @In
    private PathfinderSystem pathfinderSystem;

    @In
    private JobFactory jobFactory;

    public JobBoard() {
        logger.info("Creating JobBoard");
        CoreRegistry.put(JobBoard.class, this);
    }

    @Override
    public void initialise() {
        logger.info("Initialize JobBoard");
    }

    @Override
    public void update(float delta) {
    }

    public List<Vector3i> findJobTargets(EntityRef entity) {
        List<Vector3i> possibleJobs = Lists.newArrayList();
        scanJobs();
        for (Job job : jobFactory.getJobs()) {
            JobType jobType = getJobType(job);
            List<WalkableBlock> jobs = jobType.findJobs(entity);
            for (WalkableBlock block : jobs) {
                possibleJobs.add(block.getBlockPosition());
            }
        }
        return possibleJobs;
    }

    public EntityRef getJob(WalkableBlock block) {
        scanJobs();
        return jobMap.get(block);
    }


    @ReceiveEvent(components = {LocationComponent.class, CharacterComponent.class})
    public void onSelectionChanged(ApplyBlockSelectionEvent event, EntityRef entity) {
        Job job = jobFactory.getJob(event.getSelectedItemEntity());
        if (job == null) {
            return;
        }
        JobType type = getJobType(job);
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
                        JobBlockComponent jobBlockComponent = blockEntity.getComponent(JobBlockComponent.class);
                        if (jobBlockComponent != null) {
                            blockEntity.removeComponent(JobBlockComponent.class);
                        }
                        jobBlockComponent = new JobBlockComponent();
                        jobBlockComponent.setJob(job);
                        blockEntity.addComponent(jobBlockComponent);

                        type.add(blockEntity);
                    }
                }
            }
        }
    }


    public void removeJob(EntityRef block) {
        JobBlockComponent job = block.getComponent(JobBlockComponent.class);
        if (job != null) {
            getJobType(job.getJob()).remove(block);
            block.removeComponent(JobBlockComponent.class);
        }
    }

    @Override
    public void shutdown() {

    }

    private void scanJobs() {
        for (Job job : jobFactory.getJobs()) {
            JobType jobType = getJobType(job);
            jobType.scanJobs();
        }
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
        public final List<EntityRef> openJobs = Lists.newArrayList();
        public final Map<Floor, List<WalkableBlock>> possibleJobs = Maps.newHashMap();

        public boolean jobsDirty = true;

        private JobType(Job job) {
            this.job = job;
        }

        public List<WalkableBlock> findJobs(EntityRef minion) {
            List<WalkableBlock> result = Lists.newArrayList();
            LocationComponent locationComponent = minion.getComponent(LocationComponent.class);
            WalkableBlock currentBlock = pathfinderSystem.getBlock(locationComponent.getWorldPosition());
            if (currentBlock != null) {
                List<WalkableBlock> jobsOnFloor = possibleJobs.get(currentBlock.floor);
                if (jobsOnFloor != null && jobsOnFloor.size() > 0) {
                    result.addAll(jobsOnFloor);
                } else {
                    for (Floor floor : possibleJobs.keySet()) {
                        List<Entrance> entrances = floor.entrances();
                        if (entrances.size() > 0) {
                            result.add(entrances.get(0).getAbstractBlock());
                        }
                    }
                }
            }
            return result;
        }

        public void scanJobs() {
            if (jobsDirty) {
                possibleJobs.clear();
                jobMap.clear();
                for (EntityRef openJob : openJobs) {
                    JobBlockComponent jobComponent = openJob.getComponent(JobBlockComponent.class);
                    if (jobComponent.assignedMinion == null) {
                        List<WalkableBlock> targetPositions = jobComponent.getJob().getTargetPositions(openJob);
                        for (WalkableBlock targetPosition : targetPositions) {
                            Floor floor = targetPosition.floor;
                            List<WalkableBlock> floorJobs = possibleJobs.get(floor);
                            if (floorJobs == null) {
                                floorJobs = Lists.newArrayList();
                                possibleJobs.put(floor, floorJobs);
                            }
                            floorJobs.add(targetPosition);
                            jobMap.put(targetPosition, openJob);
                        }
                    }
                }
                jobsDirty = false;
            }
        }

        public void remove(EntityRef jobBlock) {
            openJobs.remove(jobBlock);
            jobsDirty = true;
        }

        public void add(EntityRef jobBlock) {
            openJobs.add(jobBlock);
            jobsDirty = true;
        }
    }
}
