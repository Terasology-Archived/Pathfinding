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
    private final List<EntityRef> jobs = Lists.newArrayList();
    private final List<Vector3i> possibleJobs = Lists.newArrayList();
    private final Map<Vector3i, EntityRef> jobMap = Maps.newHashMap();
    private boolean jobsDirty = true;

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
        scanJobs();
    }

    private void scanJobs() {
        if (jobsDirty) {
            possibleJobs.clear();
            jobMap.clear();
            for (EntityRef job : jobs) {
                JobBlockComponent jobComponent = job.getComponent(JobBlockComponent.class);
                List<Vector3i> targetPositions = jobComponent.getJob().getTargetPositions(job);
                possibleJobs.addAll(targetPositions);
                for (Vector3i targetPosition : targetPositions) {
                    jobMap.put(targetPosition, job);
                }
            }
            jobsDirty = false;
        }
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
                        jobs.add(blockEntity);
                    }
                }
            }
        }
        jobsDirty = true;
    }


    public void removeJob(EntityRef block) {
        JobBlockComponent job = block.getComponent(JobBlockComponent.class);
        if (job != null) {
            jobs.remove(block);
            block.removeComponent(JobBlockComponent.class);
            jobsDirty = true;
        }
    }

    @Override
    public void shutdown() {

    }

    public List<Vector3i> findJobTargets(EntityRef entity) {
        scanJobs();
        return possibleJobs;
    }

    public EntityRef getJob(WalkableBlock block) {
        scanJobs();
        return jobMap.get(block.getBlockPosition());
    }
}
