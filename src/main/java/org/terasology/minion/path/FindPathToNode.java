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
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;
import org.terasology.minion.move.MinionMoveComponent;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.WalkableBlock;

import java.util.Arrays;

/**
 * @author synopia
 */
public class FindPathToNode extends Node {
    @Override
    public FindPathToTask createTask() {
        return new FindPathToTask(this);
    }

    public static class FindPathToTask extends Task {
        public FindPathToTask(FindPathToNode node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            MinionPathComponent pathComponent = actor().component(MinionPathComponent.class);
            if (pathComponent.pathState == MinionPathComponent.PathState.NEW_TARGET) {
                PathfinderSystem pathfinder = CoreRegistry.get(PathfinderSystem.class);
                WalkableBlock currentBlock = actor().component(MinionMoveComponent.class).currentBlock;
                pathComponent.pathId = pathfinder.requestPath(actor().minion(), currentBlock.getBlockPosition(), Arrays.asList(pathComponent.targetBlock));
                pathComponent.pathState = MinionPathComponent.PathState.PATH_REQUESTED;
                actor().save(pathComponent);
            }
        }

        @Override
        public Status update(float dt) {
            MinionPathComponent pathComponent = actor().component(MinionPathComponent.class);
            if (pathComponent.pathState == MinionPathComponent.PathState.PATH_REQUESTED) {
                return Status.RUNNING;
            } else {
                return pathComponent.path == Path.INVALID ? Status.FAILURE : Status.SUCCESS;
            }
        }

        @Override
        public FindPathToNode getNode() {
            return (FindPathToNode) super.getNode();
        }
    }
}
