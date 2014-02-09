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

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.terasology.minion.move.MinionMoveComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;

import java.util.Collection;
import java.util.Map;

/**
 * @author synopia
 */
@RegisterSystem
public class WorkBoard implements ComponentSystem, UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(WorkBoard.class);
    private final Map<Work, WorkType> workTypes = Maps.newHashMap();

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private WorkFactory workFactory;

    private float cooldown = 5;

    public WorkBoard() {
        logger.info("Creating WorkBoard");
        CoreRegistry.put(WorkBoard.class, this);
    }

    @Override
    public void update(float delta) {
        cooldown -= delta;
        if (cooldown < 0) {
            for (WorkType type : workTypes.values()) {
                logger.info(type.toString());
            }
            cooldown = 5;
        }
    }

    @Override
    public void initialise() {
        logger.info("Initialize WorkBoard");
    }

    @ReceiveEvent
    public void onActivated(OnActivatedComponent event, EntityRef entityRef, WorkTargetComponent workTarget) {
        if (workTarget == null) {
            return;
        }
        WorkType workType = getWorkType(workTarget.getWork());
        workType.update(entityRef);
    }

    @ReceiveEvent
    public void onRemove(BeforeRemoveComponent event, EntityRef entityRef, WorkTargetComponent workTarget) {
        WorkType workType = getWorkType(workTarget.getWork());
        workType.remove(entityRef);
    }

    @ReceiveEvent
    public void onChange(OnChangedComponent event, EntityRef entityRef, WorkTargetComponent workTarget) {
        WorkType workType = getWorkType(workTarget.getWork());
        workType.update(entityRef);
    }

    public EntityRef getWork(EntityRef entity) {
        for (Work work : workFactory.getWorks()) {
            WorkType workType = getWorkType(work);
            Vector3i target = workType.findNearestTarget(entity.getComponent(MinionMoveComponent.class).currentBlock.getBlockPosition());
            if (target != null) {
                return workType.getWorkForTarget(target);
            }
        }
        return null;
    }

    public EntityRef getWork(EntityRef entity, Work work) {
        if (work == null) {
            return getWork(entity);
        }
        WorkType workType = getWorkType(work);
        Vector3i target = workType.findNearestTarget(entity.getComponent(MinionMoveComponent.class).currentBlock.getBlockPosition());
        if (target != null) {
            return workType.getWorkForTarget(target);
        }
        return null;
    }

    @ReceiveEvent(components = {LocationComponent.class, CharacterComponent.class})
    public void onSelectionChanged(ApplyBlockSelectionEvent event, EntityRef entity) {
        Work work = workFactory.getWork(event.getSelectedItemEntity());
        if (work == null) {
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
                    if (work.isAssignable(blockEntity)) {
                        WorkTargetComponent workTargetComponent = blockEntity.getComponent(WorkTargetComponent.class);
                        if (workTargetComponent != null) {
                            blockEntity.removeComponent(WorkTargetComponent.class);
                        }
                        workTargetComponent = new WorkTargetComponent();
                        workTargetComponent.setWork(work);
                        blockEntity.addComponent(workTargetComponent);
                    }
                }
            }
        }
    }

    public Collection<WorkType> getWorkTypes() {
        return workTypes.values();
    }

    @Override
    public void shutdown() {
    }

    public WorkType getWorkType(Work work) {
        WorkType workType = workTypes.get(work);
        if (workType == null) {
            workType = new WorkType(work);
            workTypes.put(work, workType);
        }
        return workType;
    }

}
