/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.minion.path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.logic.behavior.tree.DecoratorNode;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.location.LocationComponent;
import org.terasology.minion.move.MinionMoveComponent;
import org.terasology.pathfinding.componentSystem.PathRenderSystem;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.WalkableBlock;
import org.terasology.registry.CoreRegistry;

import javax.vecmath.Vector3f;

/**
 * @author synopia
 */
public class MoveAlongPathNode extends DecoratorNode {
    public MoveAlongPathNode() {
    }

    @Override
    public MoveAlongPathTask createTask() {
        return new MoveAlongPathTask(this);
    }

    public static class MoveAlongPathTask extends DecoratorNode.DecoratorTask {
        private static final Logger logger = LoggerFactory.getLogger(MoveAlongPathNode.class);
        private Path path;
        private int currentIndex;

        public MoveAlongPathTask(MoveAlongPathNode node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            MinionPathComponent pathComponent = actor().component(MinionPathComponent.class);
            if (pathComponent.pathState == MinionPathComponent.PathState.PATH_RECEIVED) {
                pathComponent.pathState = MinionPathComponent.PathState.MOVING_ALONG_PATH;
                actor().save(pathComponent);

                path = pathComponent.path;
                CoreRegistry.get(PathRenderSystem.class).addPath(path);
                currentIndex = 0;
                WalkableBlock block = path.get(currentIndex);
                logger.info("Start moving along path to step " + currentIndex + " " + block.getBlockPosition());
                MinionMoveComponent moveComponent = actor().component(MinionMoveComponent.class);
                moveComponent.target = block.getBlockPosition().toVector3f();
                actor().save(moveComponent);

                start(getNode().child);
            }
        }

        @Override
        public Status update(float dt) {
            return path != null ? Status.RUNNING : Status.FAILURE;
        }

        @Override
        public void handle(Status result) {
            logger.info(" Finished moving along path to step " + currentIndex + " " + result);
            if (result == Status.FAILURE) {
                stop(Status.FAILURE);
                return;
            }
            currentIndex = findNextPathIndex();
            if (currentIndex >= 0) {
                WalkableBlock block = path.get(currentIndex);
                logger.info(" Continue moving along path to step " + currentIndex + " " + block.getBlockPosition());
                MinionMoveComponent moveComponent = actor().component(MinionMoveComponent.class);
                Vector3f pos = block.getBlockPosition().toVector3f();
                pos.add(new Vector3f(0, 1, 0));
                moveComponent.target = pos;
                actor().save(moveComponent);
                start(getNode().child);
            } else {
                CoreRegistry.get(PathRenderSystem.class).removePath(path);
                logger.info("Finished moving along path " + currentIndex + " " + result);
                stop(Status.SUCCESS);
            }
        }

        private int findNextPathIndex() {
            LocationComponent location = actor().location();
            Vector3f pos = location.getWorldPosition();
            float lastDist = Float.MAX_VALUE;
            currentIndex++;
            if (currentIndex == path.size() - 1) {
                return currentIndex;
            }
            for (; currentIndex < path.size(); currentIndex++) {
                Vector3f diff = path.get(currentIndex).getBlockPosition().toVector3f();
                diff.sub(pos);
                float dist = diff.lengthSquared();
                if (dist > lastDist) {
                    return currentIndex - 1;
                }
                lastDist = dist;
            }
            return -1;
        }

        @Override
        public MoveAlongPathNode getNode() {
            return (MoveAlongPathNode) super.getNode();
        }
    }

}
