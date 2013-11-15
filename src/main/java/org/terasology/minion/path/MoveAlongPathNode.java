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

import org.terasology.behavior.tree.DecoratorNode;
import org.terasology.behavior.tree.Status;
import org.terasology.behavior.tree.Task;
import org.terasology.minion.move.MinionMoveComponent;
import org.terasology.minion.move.MoveToNode;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.WalkableBlock;

import javax.vecmath.Vector3f;

/**
 * @author synopia
 */
public class MoveAlongPathNode extends DecoratorNode {

    public MoveAlongPathNode() {
        super(new MoveToNode());
    }

    @Override
    public MoveAlongPathTask create() {
        return new MoveAlongPathTask(this);
    }

    public static class MoveAlongPathTask extends DecoratorNode.DecoratorTask implements Task.Observer {
        private Path path;
        private int currentIndex;

        public MoveAlongPathTask(MoveAlongPathNode node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            MinionPathComponent pathComponent = actor().path();
            assert pathComponent.pathState == MinionPathComponent.PathState.PATH_RECEIVED;
            pathComponent.pathState = MinionPathComponent.PathState.MOVING_PATH;
            actor().save(pathComponent);

            path = pathComponent.path;
            currentIndex = 0;
            WalkableBlock block = pathComponent.path.get(currentIndex);

            MinionMoveComponent moveComponent = actor().move();
            moveComponent.target = block.getBlockPosition().toVector3f();
            actor().save(moveComponent);

            interpreter().start(getNode().child, this);
        }

        @Override
        public Status update(float dt) {
            return Status.RUNNING;
        }

        @Override
        public void handle(Status result) {
            if (result != Status.SUCCESS) {
                interpreter().stop(this, Status.FAILURE);
            }
            MinionPathComponent pathComponent = actor().path();
            currentIndex++;
            if (currentIndex < path.size()) {
                WalkableBlock block = pathComponent.path.get(currentIndex);
                MinionMoveComponent moveComponent = actor().move();
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
