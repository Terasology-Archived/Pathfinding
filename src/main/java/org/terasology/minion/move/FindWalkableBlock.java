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
package org.terasology.minion.move;

import org.terasology.behavior.tree.Behavior;
import org.terasology.behavior.tree.BehaviorTree;
import org.terasology.behavior.tree.Node;
import org.terasology.behavior.tree.Status;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.model.WalkableBlock;

import javax.vecmath.Vector3f;

/**
 * @author synopia
 */
public class FindWalkableBlock extends Behavior<EntityRef> {
    public FindWalkableBlock(FindWalkableBlockNode node) {
        super(node);
    }

    @Override
    public Status update(EntityRef entity, float dt) {
        MinionMoveComponent moveComponent = entity.getComponent(MinionMoveComponent.class);
        if (moveComponent == null) {
            moveComponent = new MinionMoveComponent();
            entity.addComponent(moveComponent);
        }
        WalkableBlock block = CoreRegistry.get(PathfinderSystem.class).getBlock(entity);
        moveComponent.currentBlock = block;
        entity.saveComponent(moveComponent);
        if (block == null) {
            return Status.FAILURE;
        }
        LocationComponent currentLocation = entity.getComponent(LocationComponent.class);
        Vector3f diff = new Vector3f();
        diff.sub(currentLocation.getWorldPosition(), block.getBlockPosition().toVector3f());

        if (diff.length() > .5f) {
            moveComponent.target = block.getBlockPosition().toVector3f();
            entity.saveComponent(moveComponent);
            return Status.SUCCESS;
        }
        return Status.SUCCESS;
    }

    @Override
    public FindWalkableBlockNode getNode() {
        return (FindWalkableBlockNode) super.getNode();
    }

    public static class FindWalkableBlockNode implements Node<EntityRef> {
        @Override
        public FindWalkableBlock create(BehaviorTree<EntityRef> tree) {
            return new FindWalkableBlock(this);
        }
    }

}
