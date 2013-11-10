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
package org.terasology.minion.spawn;

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.model.WalkableBlock;

import javax.vecmath.Vector3f;

/**
 * @author synopia
 */
@RegisterSystem
public class MinionSpawnSystem implements ComponentSystem, UpdateSubscriberSystem {
    public static final float COOLDOWN = 1;
    @In
    private EntityManager entityManager;

    private float cooldown;

    @Override
    public void initialise() {

    }

    @Override
    public void update(float delta) {
        if (cooldown > 0) {
            cooldown -= delta;
        }
    }

    @ReceiveEvent(components = {SpawnerComponent.class})
    public void onPlaced(ActivateEvent event, EntityRef itemEntity) {
        if (cooldown > 0) {
            return;
        }
        SpawnerComponent spawner = itemEntity.getComponent(SpawnerComponent.class);
        WalkableBlock block = CoreRegistry.get(PathfinderSystem.class).getBlock(event.getHitPosition());
        if (block != null) {
            Vector3f pos = block.getBlockPosition().toVector3f();
            pos.add(new Vector3f(0, 1f, 0));
            entityManager.create(spawner.nextPrefab(itemEntity), pos);
            cooldown = COOLDOWN;
        }
    }

    @Override
    public void shutdown() {

    }
}
