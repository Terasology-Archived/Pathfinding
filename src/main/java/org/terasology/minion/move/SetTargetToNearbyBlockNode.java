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

import com.google.common.collect.Lists;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;
import org.terasology.navgraph.WalkableBlock;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.registry.In;

import java.util.List;
import java.util.Random;


public class SetTargetToNearbyBlockNode extends Node {
    @Override
    public SetTargetToNearbyBlockTask createTask() {
        return new SetTargetToNearbyBlockTask(this);
    }

    public static class SetTargetToNearbyBlockTask extends Task {
        private static final int RANDOM_BLOCK_ITERATIONS = 10;
        private Random random = new Random();

        @In
        private PathfinderSystem pathfinderSystem;

        public SetTargetToNearbyBlockTask(Node node) {
            super(node);
        }

        @Override
        public Status update(float dt) {
            MinionMoveComponent moveComponent = actor().getComponent(MinionMoveComponent.class);
            if (moveComponent.currentBlock != null) {
                WalkableBlock target = findRandomNearbyBlock(moveComponent.currentBlock);
                moveComponent.target = target.getBlockPosition().toVector3f();
                actor().save(moveComponent);
            } else {
                return Status.FAILURE;
            }
            return Status.SUCCESS;
        }

        private WalkableBlock findRandomNearbyBlock(WalkableBlock startBlock) {
            WalkableBlock currentBlock = startBlock;
            for (int i = 0; i < RANDOM_BLOCK_ITERATIONS; i++) {
                WalkableBlock[] neighbors = currentBlock.neighbors;
                List<WalkableBlock> existingNeighbors = Lists.newArrayList();
                for (WalkableBlock neighbor : neighbors) {
                    if (neighbor != null) {
                        existingNeighbors.add(neighbor);
                    }
                }
                if (existingNeighbors.size() > 0) {
                    currentBlock = existingNeighbors.get(random.nextInt(existingNeighbors.size()));
                }
            }
            return currentBlock;
        }

        @Override
        public void handle(Status result) {

        }

        @Override
        public SetTargetToNearbyBlockNode getNode() {
            return (SetTargetToNearbyBlockNode) super.getNode();
        }
    }
}
