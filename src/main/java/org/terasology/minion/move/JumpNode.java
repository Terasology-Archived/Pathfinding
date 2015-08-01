/*
 * Copyright 2015 MovingBlocks
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

import org.terasology.engine.Time;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.CoreRegistry;

/**
 * Trigger a single jump into the air.<br/>
 * <br/>
 * <b>SUCCESS</b>: when the actor is grounded after the jump again.<br/>
 * <br/>
 * Auto generated javadoc - modify README.markdown instead!
 */
public class JumpNode extends Node {
    @Override
    public Task createTask() {
        return new JumpTask(this);
    }

    public static class JumpTask extends Task {
        public JumpTask(Node node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            actor().minion().send(new CharacterMoveInputEvent(0, 0, 0, new Vector3f(), false, true, CoreRegistry.get(Time.class).getGameDeltaInMs()));
        }

        @Override
        public Status update(float dt) {
            return actor().component(CharacterMovementComponent.class).grounded ? Status.SUCCESS : Status.RUNNING;
        }

        @Override
        public void handle(Status result) {

        }
    }
}
