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
package org.terasology.work.systems;

import com.google.common.collect.Lists;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.Vector3i;
import org.terasology.navgraph.WalkableBlock;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.registry.In;
import org.terasology.work.Work;
import org.terasology.work.WorkFactory;
import org.terasology.work.WorkTargetComponent;
import org.terasology.world.block.BlockComponent;

import java.util.List;

/**
 * @author synopia
 */
@RegisterSystem
public class WalkToBlock implements Work, ComponentSystem {
    private final SimpleUri uri;
    @In
    private PathfinderSystem pathfinderSystem;
    @In
    private WorkFactory workFactory;

    public WalkToBlock() {
        uri = new SimpleUri("Pathfinding:walkToBlock");
    }

    @Override
    public void initialise() {
        workFactory.register(this);
    }

    @Override
    public void shutdown() {

    }

    @Override
    public SimpleUri getUri() {
        return uri;
    }

    public List<WalkableBlock> getTargetPositions(EntityRef block) {
        List<WalkableBlock> targetPositions = Lists.newArrayList();
        if (block == null || !block.hasComponent(BlockComponent.class)) {
            return targetPositions;
        }
        Vector3i position = block.getComponent(BlockComponent.class).getPosition();
        WalkableBlock walkableBlock = pathfinderSystem.getBlock(position);
        if (walkableBlock != null) {
            targetPositions.add(walkableBlock);
        }

        return targetPositions;
    }

    @Override
    public boolean canMinionWork(EntityRef block, EntityRef minion) {
        WalkableBlock actualBlock = pathfinderSystem.getBlock(minion);
        WalkableBlock expectedBlock = pathfinderSystem.getBlock(block.getComponent(BlockComponent.class).getPosition());

        return actualBlock == expectedBlock;
    }

    @Override
    public boolean isAssignable(EntityRef block) {
        if (block == null || !block.hasComponent(BlockComponent.class)) {
            return false;
        }
        WalkableBlock walkableBlock = pathfinderSystem.getBlock(block.getComponent(BlockComponent.class).getPosition());
        return walkableBlock != null;
    }

    @Override
    public boolean letMinionWork(EntityRef block, EntityRef minion, float dt) {
        block.removeComponent(WorkTargetComponent.class);
        return false;
    }

    @Override
    public boolean isRequestable(EntityRef block) {
        return true;
    }


    @Override
    public String toString() {
        return "Walk To Block";
    }
}
