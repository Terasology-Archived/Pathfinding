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
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author synopia
 */
@RegisterSystem
public class JobBoard implements ComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(JobBoard.class);
    private final Map<Job, JobType> jobTypes = Maps.newHashMap();
    private final List<EntityRef> toRemove = Lists.newArrayList();

    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private EntityManager entityManager;

    @In
    private PathfinderSystem pathfinderSystem;

    @In
    private JobFactory jobFactory;
    private Job walk;

    public JobBoard() {
        logger.info("Creating JobBoard");
        CoreRegistry.put(JobBoard.class, this);
    }

    @Override
    public void initialise() {
        logger.info("Initialize JobBoard");
        walk = jobFactory.getJob("Pathfinding:walkToBlock");
    }

    public void refresh() {
        for (EntityRef block : entityManager.getEntitiesWith(JobBlockComponent.class)) {
            JobBlockComponent jobBlock = block.getComponent(JobBlockComponent.class);
            JobType jobType = getJobType(jobBlock.getJob());
            if (!jobType.contains(block)) {
                jobType.add(block);
            }
        }
        if (toRemove.size() > 0) {
            for (EntityRef block : toRemove) {
                JobBlockComponent jobBlock = block.getComponent(JobBlockComponent.class);
                if (jobBlock != null) {
                    getJobType(jobBlock.getJob()).remove(block);
                    block.removeComponent(JobBlockComponent.class);
                }
            }
        }
        scanJobs();
    }

    public List<JobPossibility> findJobTargets(EntityRef entity) {
        List<JobPossibility> possibleJobs = Lists.newArrayList();
        for (Job job : jobFactory.getJobs()) {
            JobType jobType = getJobType(job);
            List<JobPossibility> jobs = jobType.findJobs(entity);
            possibleJobs.addAll(jobs);
        }
        return possibleJobs;
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
                        JobBlockComponent jobBlockComponent = blockEntity.getComponent(JobBlockComponent.class);
                        if (jobBlockComponent != null) {
                            blockEntity.removeComponent(JobBlockComponent.class);
                        }
                        jobBlockComponent = new JobBlockComponent();
                        jobBlockComponent.setJob(job);
                        blockEntity.addComponent(jobBlockComponent);
                    }
                }
            }
        }
    }

    public void removeJob(EntityRef block) {
        JobBlockComponent job = block.getComponent(JobBlockComponent.class);
        if (job != null) {
            toRemove.add(block);
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
        public final Set<EntityRef> openJobs = Sets.newHashSet();
        public final Map<Floor, List<JobPossibility>> possibleJobs = Maps.newHashMap();

        private JobType(Job job) {
            this.job = job;
        }

        public List<JobPossibility> findJobs(EntityRef minion) {
            List<JobPossibility> result = Lists.newArrayList();
            WalkableBlock currentBlock = pathfinderSystem.getBlock(minion);
            if (currentBlock != null) {
                if (addJobPossibilities(result, currentBlock.floor, minion) == 0) {
                    int total = 0;
                    for (Floor floor : currentBlock.floor.getNeighborRegions()) {
                        total += addJobPossibilities(result, floor, minion);
                    }
                    if (total < 2) {
                        for (Floor floor : possibleJobs.keySet()) {
                            List<Entrance> entrances = floor.entrances();
                            if (entrances.size() > 0) {
                                for (Entrance entrance : entrances) {
                                    WalkableBlock block = entrance.getAbstractBlock();
                                    JobPossibility possibility = new JobPossibility();
                                    possibility.targetBlock = block;
                                    possibility.targetPos = block.getBlockPosition();
                                    possibility.targetEntity = null;
                                    possibility.job = walk;
                                    possibility.minion = minion;
                                    result.add(possibility);
                                }
                            }
                            logger.info("Added floor entrances for " + minion);
                        }
                    } else {
                        logger.info("Added jobs on neighbor floors for " + minion);
                    }
                } else {
                    logger.info("Added jobs on current floor for " + minion);
                }
            }
            return result;
        }

        private int addJobPossibilities(List<JobPossibility> result, Floor floor, EntityRef minion) {
            int count = 0;
            List<JobPossibility> jobsOnFloor = possibleJobs.get(floor);
            if (jobsOnFloor != null && jobsOnFloor.size() > 0) {
                for (JobPossibility possibility : jobsOnFloor) {
                    if (possibility.job.isRequestable(possibility.targetEntity)) {
                        JobPossibility p = new JobPossibility(possibility);
                        p.minion = minion;
                        result.add(p);
                    }
                    count++;
                }
            }
            return count;
        }

        public void scanJobs() {
            possibleJobs.clear();
            Iterator<EntityRef> it = openJobs.iterator();
            while (it.hasNext()) {
                EntityRef openJob = it.next();
                JobBlockComponent jobComponent = openJob.getComponent(JobBlockComponent.class);
                if (jobComponent != null && jobComponent.assignedMinion == null) {
                    List<WalkableBlock> targetPositions = jobComponent.getJob().getTargetPositions(openJob);
                    for (WalkableBlock targetPosition : targetPositions) {
                        Floor floor = targetPosition.floor;
                        List<JobPossibility> floorJobs = possibleJobs.get(floor);
                        if (floorJobs == null) {
                            floorJobs = Lists.newArrayList();
                            possibleJobs.put(floor, floorJobs);
                        }
                        JobPossibility possibility = new JobPossibility();
                        possibility.job = jobComponent.getJob();
                        possibility.targetEntity = openJob;
                        possibility.targetPos = targetPosition.getBlockPosition();
                        possibility.targetBlock = targetPosition;
                        floorJobs.add(possibility);
                    }
                } else {
                    it.remove();
                }
            }
        }

        public void remove(EntityRef jobBlock) {
            openJobs.remove(jobBlock);
        }

        public void add(EntityRef jobBlock) {
            openJobs.add(jobBlock);
        }

        public boolean contains(EntityRef jobBlock) {
            return openJobs.contains(jobBlock);
        }
    }

}
