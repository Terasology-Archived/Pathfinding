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
package org.terasology.jobSystem.jobs;

import com.google.common.collect.Lists;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.jobSystem.Job;
import org.terasology.jobSystem.JobFactory;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Vector3i;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.model.WalkableBlock;

import javax.vecmath.Vector3f;
import java.util.List;

/**
 * @author synopia
 */
@RegisterSystem
public class WalkToBlock implements Job, ComponentSystem {
    private final SimpleUri uri;
    @In
    private PathfinderSystem pathfinderSystem;

    public WalkToBlock() {
        uri = new SimpleUri("Pathfinding:walkToBlock");
    }

    @Override
    public void initialise() {
        CoreRegistry.get(JobFactory.class).register(this);
    }

    @Override
    public void shutdown() {

    }

    @Override
    public SimpleUri getUri() {
        return uri;
    }

    public List<Vector3i> getTargetPositions(EntityRef block) {
        List<Vector3i> targetPositions = Lists.newArrayList();
        Vector3f position = block.getComponent(LocationComponent.class).getWorldPosition();
        targetPositions.add(new Vector3i(position.x, position.y, position.z));
        return targetPositions;
    }

    @Override
    public boolean canMinionWork(EntityRef block, EntityRef minion) {
        Vector3f worldPosition = minion.getComponent(LocationComponent.class).getWorldPosition();
        WalkableBlock actualBlock = pathfinderSystem.getBlock(worldPosition);
        WalkableBlock expectedBlock = pathfinderSystem.getBlock(block.getComponent(LocationComponent.class).getWorldPosition());

        return actualBlock == expectedBlock;
    }

    @Override
    public boolean isAssignable(EntityRef block) {
        WalkableBlock walkableBlock = pathfinderSystem.getBlock(block.getComponent(LocationComponent.class).getWorldPosition());
        return walkableBlock != null;
    }

    @Override
    public void letMinionWork(EntityRef block, EntityRef minion) {

    }

    @Override
    public boolean isRequestable(EntityRef block) {
        return true;
    }
}
