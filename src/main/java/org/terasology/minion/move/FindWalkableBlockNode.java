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

import org.terasology.logic.behavior.tree.DecoratorNode;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.model.WalkableBlock;
import org.terasology.registry.In;

/**
 * <strong>FindWalkableBlock</strong> <code>Decorator</code>
 * <p/>
 * Searches for the next valid walkable block for pathfinder. Best use of this node is probably
 * using a <code>Parallel</code> on top of the behavior tree and a <code>MoveTo</code> as the child.
 * <p/>
 * This node must be run successfully <strong></stong>before<strong></stong> calling <code>FindPathTo</code>, because here the start position for pathfinding is queried.
 * <p/>
 * The walkable block is stored into <code>MinionMoveComponent</code>.
 * <p/>
 * <code>SUCCESS</code>: if the child returns <code>SUCCESS</code>.
 * <code>FAILURE</code>: if no walkable block can be found.
 * <p/>
 * Auto generated javadoc - modify README.markdown instead!
 */
public class FindWalkableBlockNode extends DecoratorNode {
    @Override
    public FindWalkableBlockTask createTask() {
        return new FindWalkableBlockTask(this);
    }

    public static class FindWalkableBlockTask extends DecoratorTask {
        @In
        private PathfinderSystem pathfinderSystem;
        private WalkableBlock block;

        public FindWalkableBlockTask(FindWalkableBlockNode node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            MinionMoveComponent moveComponent = actor().component(MinionMoveComponent.class);
            block = pathfinderSystem.getBlock(actor().minion());
            moveComponent.currentBlock = block;
            if (block != null) {
                moveComponent.target = block.getBlockPosition().toVector3f();
                start(getNode().child);
            }
            actor().save(moveComponent);
        }

        @Override
        public Status update(float dt) {
            if (block == null) {
                return Status.FAILURE;
            }
            return Status.RUNNING;
        }

        @Override
        public void handle(Status result) {
            stop(result);
        }

        @Override
        public FindWalkableBlockNode getNode() {
            return (FindWalkableBlockNode) super.getNode();
        }
    }

}
