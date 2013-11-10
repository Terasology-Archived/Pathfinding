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

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.minion.behavior.tree.Behavior;
import org.terasology.minion.behavior.tree.BehaviorTree;
import org.terasology.minion.behavior.tree.Node;
import org.terasology.minion.behavior.tree.Status;
import org.terasology.minion.move.MinionMoveComponent;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.WalkableBlock;

import java.util.Arrays;

/**
 * @author synopia
 */
public class FindPathTo extends Behavior<EntityRef> {
    public FindPathTo(FindPathToNode node) {
        super(node);
    }

    @Override
    public void onInitialize(EntityRef entity) {
        MinionPathComponent pathComponent = entity.getComponent(MinionPathComponent.class);
        if (pathComponent != null && pathComponent.pathState == MinionPathComponent.PathState.NEW_TARGET) {
            PathfinderSystem pathfinder = CoreRegistry.get(PathfinderSystem.class);
            WalkableBlock currentBlock = entity.getComponent(MinionMoveComponent.class).currentBlock;
            pathComponent.pathId = pathfinder.requestPath(entity, currentBlock.getBlockPosition(), Arrays.asList(pathComponent.targetBlock));
            pathComponent.pathState = MinionPathComponent.PathState.PATH_REQUESTED;
            entity.saveComponent(pathComponent);
        }
    }

    @Override
    public Status update(EntityRef entity, float dt) {
        MinionPathComponent pathComponent = entity.getComponent(MinionPathComponent.class);
        return pathComponent.pathState == MinionPathComponent.PathState.PATH_REQUESTED ? Status.RUNNING : (pathComponent.path == Path.INVALID ? Status.FAILURE : Status.SUCCESS);
    }

    @Override
    public FindPathToNode getNode() {
        return (FindPathToNode) super.getNode();
    }

    public static class FindPathToNode implements Node<EntityRef> {
        @Override
        public Behavior<EntityRef> create(BehaviorTree<EntityRef> tree) {
            return new FindPathTo(this);
        }
    }
}
