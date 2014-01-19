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
package org.terasology.minion.path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.logic.behavior.tree.DecoratorNode;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;
import org.terasology.minion.move.MinionMoveComponent;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.WalkableBlock;

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

    public static class MoveAlongPathTask extends DecoratorNode.DecoratorTask implements Task.Observer {
        private Logger logger = LoggerFactory.getLogger(MoveAlongPathTask.class);
        private Path path;
        private int currentIndex;

        public MoveAlongPathTask(MoveAlongPathNode node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            MinionPathComponent pathComponent = actor().component(MinionPathComponent.class);
            if (pathComponent.pathState == MinionPathComponent.PathState.PATH_RECEIVED) {
                pathComponent.pathState = MinionPathComponent.PathState.MOVING_PATH;
                actor().save(pathComponent);

                path = pathComponent.path;
                currentIndex = 0;
                WalkableBlock block = path.get(currentIndex);

                MinionMoveComponent moveComponent = actor().component(MinionMoveComponent.class);
                moveComponent.target = block.getBlockPosition().toVector3f();
                actor().save(moveComponent);

                interpreter().start(getNode().child, this);
            }
        }

        @Override
        public Status update(float dt) {
            return path != null ? Status.RUNNING : Status.FAILURE;
        }

        @Override
        public void handle(Status result) {
            if (result == Status.FAILURE) {
                interpreter().stop(this, Status.FAILURE);
                return;
            }
            MinionPathComponent pathComponent = actor().component(MinionPathComponent.class);
//            assert pathComponent.path == path;
            currentIndex++;
            if (currentIndex < path.size()) {
                WalkableBlock block = path.get(currentIndex);
                MinionMoveComponent moveComponent = actor().component(MinionMoveComponent.class);
                Vector3f pos = block.getBlockPosition().toVector3f();
                pos.add(new Vector3f(0, 1, 0));
                moveComponent.target = pos;
                actor().save(moveComponent);
                interpreter().start(getNode().child, this);
            } else {
                interpreter().stop(this, Status.SUCCESS);
            }
        }

        @Override
        public MoveAlongPathNode getNode() {
            return (MoveAlongPathNode) super.getNode();
        }
    }

}
