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

import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;
import org.terasology.navgraph.NavGraphSystem;
import org.terasology.navgraph.WalkableBlock;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.model.Path;
import org.terasology.registry.In;

import java.util.Arrays;
import java.util.List;

/**
 * Requests a path to a target defined using the <b>MinionMoveComponent.target</b>.<br/>
 * <br/>
 * <b>SUCCESS</b> / <b>FAILURE</b>: when paths is found or not found (invalid).<br/>
 * <b>RUNNING</b>: as long as path is searched.<br/>
 * <br/>
 * Auto generated javadoc - modify README.markdown instead!
 */
public class FindPathToNode extends Node {
    @Override
    public FindPathToTask createTask() {
        return new FindPathToTask(this);
    }

    public static class FindPathToTask extends Task {
        @In
        private NavGraphSystem navGraphSystem;
        @In
        private PathfinderSystem pathfinderSystem;
        private Path foundPath;

        public FindPathToTask(FindPathToNode node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            MinionMoveComponent moveComponent = actor().component(MinionMoveComponent.class);
            WalkableBlock workTarget = navGraphSystem.getBlock(moveComponent.target);
            if (moveComponent.currentBlock != null && workTarget != null) {
                pathfinderSystem.requestPath(actor().minion(), moveComponent.currentBlock.getBlockPosition(), Arrays.asList(workTarget.getBlockPosition()), new PathfinderSystem.PathReadyCallback() {
                    @Override
                    public void pathReady(int pathId, List<Path> path, WalkableBlock target, List<WalkableBlock> start) {
                        if (path == null) {
                            foundPath = Path.INVALID;
                        } else if (path.size() > 0) {
                            foundPath = path.get(0);
                        }
                    }
                });
            }
        }

        @Override
        public Status update(float dt) {
            if (foundPath == null) {
                return Status.RUNNING;
            }
            MinionMoveComponent component = actor().component(MinionMoveComponent.class);
            component.path = foundPath;
            actor().save(component);
            return foundPath == Path.INVALID ? Status.FAILURE : Status.SUCCESS;
        }

        @Override
        public void handle(Status result) {
        }

        @Override
        public FindPathToNode getNode() {
            return (FindPathToNode) super.getNode();
        }
    }
}
