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
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.minion.move.MinionMoveComponent;
import org.terasology.pathfinding.componentSystem.PathReadyEvent;
import org.terasology.pathfinding.model.Path;

/**
 * @author synopia
 */
@RegisterSystem
public class MinionPathSystem implements ComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(MinionPathSystem.class);

    @ReceiveEvent(components = {MinionPathComponent.class, MinionMoveComponent.class})
    public void onPathReady(PathReadyEvent event, EntityRef minion) {
        final MinionPathComponent pathComponent = minion.getComponent(MinionPathComponent.class);
        if (pathComponent.pathId != event.getPathId()) {
            return;
        }
        if (event.getPath() == null) {
            logger.warn("Minion " + minion + " received path (id = " + event.getPathId() + ") " +
                    " invalid path " +
                    " old path state " + pathComponent.pathState);

            pathComponent.path = Path.INVALID;
            pathComponent.pathState = MinionPathComponent.PathState.IDLE;
        } else {

            logger.info("Minion " + minion + " received path (id = " + event.getPathId() + ") " +
                    minion.getComponent(MinionMoveComponent.class).target +
                    " to " + event.getTarget().getBlockPosition() +
                    " old path state " + pathComponent.pathState);

            pathComponent.path = event.getPath().get(0);
            pathComponent.pathState = MinionPathComponent.PathState.PATH_RECEIVED;
        }
        minion.saveComponent(pathComponent);
    }

    @Override
    public void initialise() {

    }

    @Override
    public void shutdown() {

    }
}
