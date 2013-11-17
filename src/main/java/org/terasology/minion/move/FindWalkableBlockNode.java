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

import org.terasology.engine.CoreRegistry;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;
import org.terasology.logic.location.LocationComponent;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.model.WalkableBlock;

import javax.vecmath.Vector3f;

/**
 * @author synopia
 */
public class FindWalkableBlockNode extends Node {
    @Override
    public FindWalkableBlockTask create() {
        return new FindWalkableBlockTask(this);
    }

    public static class FindWalkableBlockTask extends Task {
        public FindWalkableBlockTask(FindWalkableBlockNode node) {
            super(node);
        }

        @Override
        public Status update(float dt) {
            MinionMoveComponent moveComponent = actor().move();
            WalkableBlock block = CoreRegistry.get(PathfinderSystem.class).getBlock(actor().minion());
            moveComponent.currentBlock = block;
            actor().save(moveComponent);
            if (block == null) {
                return Status.FAILURE;
            }
            LocationComponent currentLocation = actor().location();
            Vector3f diff = new Vector3f();
            diff.sub(currentLocation.getWorldPosition(), block.getBlockPosition().toVector3f());

            if (diff.length() > .5f) {
                moveComponent.target = block.getBlockPosition().toVector3f();
                actor().save(moveComponent);
                return Status.SUCCESS;
            }
            return Status.SUCCESS;
        }

        @Override
        public FindWalkableBlockNode getNode() {
            return (FindWalkableBlockNode) super.getNode();
        }
    }

}
