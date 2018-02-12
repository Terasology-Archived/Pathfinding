/*
 * Copyright 2018 MovingBlocks
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

//import org.terasology.logic.behavior.tree.Node;
//import org.terasology.logic.behavior.tree.Status;
//import org.terasology.logic.behavior.tree.Task;

public class CheckTargetSetNode { //TODO: Update to BT v2.0
                            // extends Node {
/*   @Override
    public Task createTask() {
        return new CheckTargetSetTask(this);
    }

    public static class CheckTargetSetTask extends Task {
        public CheckTargetSetTask(Node node) {
            super(node);
        }

        @Override
        public Status update(float dt) {
            MinionMoveComponent minionMoveComponent = actor().getComponent(MinionMoveComponent.class);
            if (minionMoveComponent.target != null) {
                return Status.SUCCESS;
            }
            return Status.FAILURE;
        }

        @Override
        public void handle(Status result) {
        }
    }
    */
}
