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

import org.terasology.behavior.tree.Behavior;
import org.terasology.behavior.tree.BehaviorTree;
import org.terasology.behavior.tree.Decorator;
import org.terasology.behavior.tree.Status;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.minion.move.MinionMoveComponent;
import org.terasology.minion.move.MoveTo;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.WalkableBlock;

import javax.vecmath.Vector3f;

/**
 * @author synopia
 */
public class MoveAlongPath extends Decorator<EntityRef> implements Behavior.Observer<EntityRef> {
    private Path path;
    private int currentIndex;

    public MoveAlongPath(MoveAlongPathNode node, BehaviorTree<EntityRef> tree) {
        super(node, tree);
    }

    @Override
    public void onInitialize(EntityRef minion) {
        MinionPathComponent pathComponent = minion.getComponent(MinionPathComponent.class);
        assert pathComponent.pathState == MinionPathComponent.PathState.PATH_RECEIVED;
        pathComponent.pathState = MinionPathComponent.PathState.MOVING_PATH;
        minion.saveComponent(pathComponent);

        path = pathComponent.path;
        currentIndex = 0;
        WalkableBlock block = pathComponent.path.get(currentIndex);
        MinionMoveComponent moveComponent = minion.getComponent(MinionMoveComponent.class);
        moveComponent.target = block.getBlockPosition().toVector3f();
        minion.saveComponent(moveComponent);

        behaviorTree.start(getNode().getChild().create(behaviorTree), this);
    }

    @Override
    public Status update(EntityRef minion, float dt) {
        return Status.RUNNING;
    }

    @Override
    public void handle(EntityRef minion, Status result) {
        if (result != Status.SUCCESS) {
            behaviorTree.stop(this, Status.FAILURE);
        }
        MinionPathComponent pathComponent = minion.getComponent(MinionPathComponent.class);
        currentIndex++;
        if (currentIndex < path.size()) {
            WalkableBlock block = pathComponent.path.get(currentIndex);
            MinionMoveComponent moveComponent = minion.getComponent(MinionMoveComponent.class);
            Vector3f pos = block.getBlockPosition().toVector3f();
            pos.add(new Vector3f(0, 1, 0));
            moveComponent.target = pos;
            minion.saveComponent(moveComponent);
            behaviorTree.start(getNode().getChild().create(behaviorTree), this);
        } else {
            behaviorTree.stop(this, Status.SUCCESS);
        }
    }

    @Override
    public MoveAlongPathNode getNode() {
        return (MoveAlongPathNode) super.getNode();
    }

    public static class MoveAlongPathNode extends DecoratorNode<EntityRef> {
        public MoveAlongPathNode() {
            super(new MoveTo.MoveToNode());
        }

        @Override
        public MoveAlongPath create(BehaviorTree<EntityRef> tree) {
            return new MoveAlongPath(this, tree);
        }
    }

}
