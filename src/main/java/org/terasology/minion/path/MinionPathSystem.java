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
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.minion.move.CannotReachEvent;
import org.terasology.minion.move.MinionMoveComponent;
import org.terasology.minion.move.MovingFinishedEvent;
import org.terasology.pathfinding.componentSystem.PathReadyEvent;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.WalkableBlock;

import java.util.Arrays;

/**
 * @author synopia
 */
@RegisterSystem
public class MinionPathSystem implements ComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(MinionPathSystem.class);

    @In
    private PathfinderSystem pathfinderSystem;
    @In
    private EntityManager entityManager;

    @ReceiveEvent(components = {MinionPathComponent.class, MinionMoveComponent.class, LocationComponent.class})
    public void onTargetReached(MovingFinishedEvent event, EntityRef minion) {
        LocationComponent location = minion.getComponent(LocationComponent.class);
        MinionPathComponent pathComponent = minion.getComponent(MinionPathComponent.class);
        MinionMoveComponent move = minion.getComponent(MinionMoveComponent.class);

        if (pathComponent.pathStep < 0) {
            pathComponent.pathState = MinionPathComponent.PathState.FINISHED_MOVING;
            pathComponent.targetBlock = null;
            minion.saveComponent(pathComponent);
            minion.send(new MovingPathFinishedEvent(pathComponent.pathId, pathComponent.targetBlock, pathComponent.path));
        } else {
            WalkableBlock target = pathComponent.path.get(pathComponent.pathStep);
            move.targetBlock = target.getBlockPosition();
            pathComponent.pathStep--;
            minion.saveComponent(pathComponent);
            minion.saveComponent(move);
        }
    }


    @ReceiveEvent(components = {MinionPathComponent.class, MinionMoveComponent.class, LocationComponent.class})
    public void onCannotReach(CannotReachEvent event, EntityRef minion) {
        MinionPathComponent pathComponent = minion.getComponent(MinionPathComponent.class);
        minion.send(new MoveToEvent(pathComponent.targetBlock));
    }

    @ReceiveEvent(components = {MinionPathComponent.class, MinionMoveComponent.class, LocationComponent.class})
    public void onNewTarget(MoveToEvent event, EntityRef minion) {
        MinionPathComponent pathComponent = minion.getComponent(MinionPathComponent.class);
        MinionMoveComponent moveComponent = minion.getComponent(MinionMoveComponent.class);
        if (moveComponent.currentBlock == null) {
            return;
        }
        if (event.getTarget() != null) {
            WalkableBlock targetBlock = pathfinderSystem.getBlock(event.getTarget());
            if (targetBlock != null) {
                pathComponent.pathId = pathfinderSystem.requestPath(minion, event.getTarget(), Arrays.asList(moveComponent.currentBlock.getBlockPosition()));
                pathComponent.pathState = MinionPathComponent.PathState.PATH_REQUESTED;
                minion.saveComponent(pathComponent);
                return;
            }
        }
        minion.send(new MovingPathAbortedEvent());
    }

    @ReceiveEvent(components = {MinionPathComponent.class, MinionMoveComponent.class})
    public void onPathReady(PathReadyEvent event, EntityRef minion) {
        final MinionPathComponent pathComponent = minion.getComponent(MinionPathComponent.class);
        if (pathComponent.pathId != event.getPathId()) {
            return;
        }
        if (event.getPath() == null) {
            minion.send(new MoveToEvent(pathComponent.targetBlock));
            return;
        }
        logger.info("Minion received paths " + event.getPathId() +
                " from " + minion.getComponent(LocationComponent.class).getWorldPosition() +
                " to " + event.getTarget().getBlockPosition());


        pathComponent.path = event.getPath().get(0);
        if (pathComponent.path != Path.INVALID) {
            pathComponent.pathState = MinionPathComponent.PathState.MOVING_PATH;
            pathComponent.pathStep = pathComponent.path.size() - 1;
            pathComponent.targetBlock = pathComponent.path.getStart().getBlockPosition();
            MinionMoveComponent move = minion.getComponent(MinionMoveComponent.class);
            move.targetBlock = pathComponent.path.get(pathComponent.pathStep).getBlockPosition();
            minion.saveComponent(move);
        } else {
            minion.send(new MovingPathAbortedEvent());
        }
    }

    @Override
    public void initialise() {

    }

    @Override
    public void shutdown() {

    }
}
