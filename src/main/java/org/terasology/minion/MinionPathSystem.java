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
package org.terasology.minion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.pathfinding.componentSystem.PathReadyEvent;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.WalkableBlock;
import org.terasology.world.BlockEntityRegistry;

/**
 * @author synopia
 */
@RegisterSystem
public class MinionPathSystem implements ComponentSystem, UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(MinionPathSystem.class);

    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private PathfinderSystem pathfinderSystem;
    @In
    private EntityManager entityManager;

    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(MinionPathComponent.class, LocationComponent.class)) {
            LocationComponent location = entity.getComponent(LocationComponent.class);
            final MinionPathComponent pathComponent = entity.getComponent(MinionPathComponent.class);
            MinionMoveComponent move = entity.getComponent(MinionMoveComponent.class);
            if (move == null) {
                move = new MinionMoveComponent();
                entity.addComponent(move);
            }
            switch (pathComponent.pathState) {
                case NEW_TARGET:
                    pathComponent.pathId = pathfinderSystem.requestPath(entity, pathComponent.targetBlock.toVector3f(), location.getWorldPosition());
                    pathComponent.pathState = MinionPathComponent.PathState.PATH_REQUESTED;
                    entity.saveComponent(pathComponent);
                    break;
                case MOVING_PATH:
                    if (move.targetBlock == null) {
                        if (pathComponent.pathStep < 0) {
                            pathComponent.pathState = MinionPathComponent.PathState.FINISHED_MOVING;
                            pathComponent.targetBlock = null;
                            entity.saveComponent(pathComponent);
                            entity.send(new MovingPathFinishedEvent(pathComponent.pathId, pathComponent.targetBlock, pathComponent.path));
                        } else {
                            WalkableBlock target = pathComponent.path.get(pathComponent.pathStep);
                            move.targetBlock = target.getBlockPosition();
                            pathComponent.pathStep--;
                            entity.saveComponent(pathComponent);
                            entity.saveComponent(move);
                        }
                    }
                    break;
            }
        }
    }

    @ReceiveEvent(components = {MinionPathComponent.class, MinionMoveComponent.class})
    public void onPathReady(PathReadyEvent event, EntityRef minion) {
        final MinionPathComponent pathComponent = minion.getComponent(MinionPathComponent.class);
        if (event.getPath() == null) {
            pathComponent.path = null;
            pathComponent.pathId = -1;
            pathComponent.pathState = MinionPathComponent.PathState.NEW_TARGET;
            minion.saveComponent(pathComponent);
            logger.info("Cannot find path -> rerequest");
            return;
        }
        logger.info("Minion received paths " + event.getPathId() +
                " from " + minion.getComponent(LocationComponent.class).getWorldPosition() +
                " to " + event.getTarget().getBlockPosition());

        MinionMoveComponent move = minion.getComponent(MinionMoveComponent.class);
        move.targetBlock = null;
        minion.saveComponent(move);


        pathComponent.path = event.getPath()[0];
        if (pathComponent.path != Path.INVALID) {
            pathComponent.pathState = MinionPathComponent.PathState.MOVING_PATH;
            pathComponent.pathStep = pathComponent.path.size() - 1;
        } else {
            pathComponent.pathState = MinionPathComponent.PathState.IDLE;
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
