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
package org.terasology.minion.move;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.events.HorizontalCollisionEvent;
import org.terasology.logic.characters.events.OnEnterBlockEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.navgraph.NavGraphChanged;
import org.terasology.navgraph.WalkableBlock;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.registry.In;

import com.google.common.collect.Sets;

/**
 * Created by synopia on 02.02.14.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class MinionMoveSystem extends BaseComponentSystem {
    private static final Logger LOG = LoggerFactory.getLogger(MinionMoveSystem.class);

    @In
    private PathfinderSystem pathfinderSystem;

    private Set<EntityRef> entities = Sets.newHashSet();

    @ReceiveEvent
    public void worldChanged(NavGraphChanged event, EntityRef entityRef) {
        for (EntityRef entity : entities) {
            setupEntity(entity);
        }
    }

    @ReceiveEvent(components = {MinionMoveComponent.class})
    public void onCollision(HorizontalCollisionEvent event, EntityRef minion) {
        MinionMoveComponent movementComponent = minion.getComponent(MinionMoveComponent.class);
        movementComponent.horizontalCollision = true;
        minion.saveComponent(movementComponent);
    }

    @ReceiveEvent
    public void onMinionEntersBlock(OnEnterBlockEvent event, EntityRef minion, LocationComponent locationComponent, MinionMoveComponent moveComponent) {
        setupEntity(minion);
    }

    @ReceiveEvent(components = {MinionMoveComponent.class, LocationComponent.class})
    public void onMinionActivation(OnActivatedComponent event, EntityRef minion) {
        setupEntity(minion);
        entities.add(minion);
    }

    @ReceiveEvent
    public void onMinionDeactivation(BeforeDeactivateComponent event, EntityRef minion, LocationComponent locationComponent, MinionMoveComponent moveComponent) {
        entities.remove(minion);
    }

    private void setupEntity(EntityRef minion) {
        MinionMoveComponent moveComponent = minion.getComponent(MinionMoveComponent.class);
        WalkableBlock block = pathfinderSystem.getBlock(minion);
        moveComponent.currentBlock = block;
        if (block != null && moveComponent.target == null) {
            moveComponent.target = block.getBlockPosition().toVector3f();
        }
        minion.saveComponent(moveComponent);
    }

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {

    }
}
