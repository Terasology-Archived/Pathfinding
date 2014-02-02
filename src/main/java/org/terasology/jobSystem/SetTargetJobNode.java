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
package org.terasology.jobSystem;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;
import org.terasology.minion.path.MinionPathComponent;
import org.terasology.pathfinding.model.WalkableBlock;

import java.util.List;

/**
 * Set <b>MinionPathComponent</b>'s target to the job's target.<br/>
 * <br/>
 * <b>SUCCESS</b>: if valid job target position found.<br/>
 * <b>FAILURE</b>: otherwise<br/>
 * <br/>
 * Auto generated javadoc - modify README.markdown instead!
 */
public class SetTargetJobNode extends Node {
    @Override
    public Task createTask() {
        return new SetTargetJobTask(this);
    }

    public static class SetTargetJobTask extends Task {
        public SetTargetJobTask(Node node) {
            super(node);
        }

        @Override
        public Status update(float dt) {
            EntityRef job = actor().component(JobMinionComponent.class).currentJob;
            if (job != null) {
                JobTargetComponent jobComponent = job.getComponent(JobTargetComponent.class);
                List<WalkableBlock> targetPositions = jobComponent.getJob().getTargetPositions(job);
                if (targetPositions.size() > 0) {
                    WalkableBlock block = targetPositions.get(0);
                    MinionPathComponent pathComponent = actor().component(MinionPathComponent.class);
                    pathComponent.targetBlock = block.getBlockPosition();
                    pathComponent.pathState = MinionPathComponent.PathState.NEW_TARGET;
                    actor().save(pathComponent);
                    return Status.SUCCESS;
                }
            }
            return Status.FAILURE;
        }

        @Override
        public void handle(Status result) {

        }
    }
}
