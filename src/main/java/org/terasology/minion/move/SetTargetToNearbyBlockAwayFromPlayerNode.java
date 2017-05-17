/*
 * Copyright 2017 MovingBlocks
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.navgraph.WalkableBlock;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.components.FleeComponent;
import org.terasology.registry.In;
import org.terasology.world.chunks.remoteChunkProvider.RemoteChunkProvider;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;


public class SetTargetToNearbyBlockAwayFromPlayerNode extends Node {


    private static final Logger logger = LoggerFactory.getLogger(SetTargetToNearbyBlockAwayFromPlayerNode.class);

    @Override
    public SetTargetToNearbyBlockAwayFromPlayerTask createTask() {
        return new SetTargetToNearbyBlockAwayFromPlayerTask(this);
    }

    public static class SetTargetToNearbyBlockAwayFromPlayerTask extends Task {
        private static final int RANDOM_BLOCK_ITERATIONS = 10;
        private Random random = new Random();

        @In
        private PathfinderSystem pathfinderSystem;

        public SetTargetToNearbyBlockAwayFromPlayerTask(Node node) {
            super(node);
        }

        @Override
        public Status update(float dt) {
            MinionMoveComponent moveComponent = actor().getComponent(MinionMoveComponent.class);
            if (moveComponent.currentBlock != null) {
                WalkableBlock target = findRandomNearbyBlockAwayFromPlayer(moveComponent.currentBlock);
                moveComponent.target = target.getBlockPosition().toVector3f();
                actor().save(moveComponent);
            } else {
                return Status.FAILURE;
            }
            return Status.SUCCESS;
        }

        private WalkableBlock findRandomNearbyBlockAwayFromPlayer(WalkableBlock startBlock) {
            WalkableBlock currentBlock = startBlock;
            Vector3i playerPosition = new Vector3i(actor().getComponent(FleeComponent.class).instigator.getComponent(LocationComponent.class).getWorldPosition());
            for (int i = 0; i < RANDOM_BLOCK_ITERATIONS; i++) {
                WalkableBlock[] neighbors = currentBlock.neighbors;
                List<WalkableBlock> existingNeighbors = Lists.newArrayList();
                for (WalkableBlock neighbor : neighbors) {
                    if (neighbor != null) {
                        existingNeighbors.add(neighbor);
                    }
                }
                if (existingNeighbors.size() > 0) {
                    // Sorting the list of neighboring blocks based on distance from player (farthest first)
                    existingNeighbors.sort((one, two) -> {
                        double a = one.getBlockPosition().distance(playerPosition);
                        double b = two.getBlockPosition().distance(playerPosition);
                        return a > b ? -1
                                : a < b ? 1
                                : 0;
                    });
                    // Select any of the first 4 neighboring blocks to make path random and not linear
                    currentBlock = existingNeighbors.get(random.nextInt(4));
                }
            }
            return currentBlock;
        }

        @Override
        public void handle(Status result) {

        }

        @Override
        public SetTargetToNearbyBlockAwayFromPlayerNode getNode() {
            return (SetTargetToNearbyBlockAwayFromPlayerNode) super.getNode();
        }
    }
}
