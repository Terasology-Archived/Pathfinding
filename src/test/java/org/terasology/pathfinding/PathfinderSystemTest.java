// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.pathfinding;

import com.google.common.collect.Lists;
import org.joml.Vector3i;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.engine.entitySystem.event.internal.EventSystem;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.world.chunks.event.OnChunkLoaded;
import org.terasology.navgraph.NavGraphSystem;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.Pathfinder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author synopia
 */
@Tag("MteTest")
@ExtendWith(MTEExtension.class)
@Dependencies("Pathfinding")
public class PathfinderSystemTest {
    private PojoEntityManager entityManager;
    private EventSystem eventSystem;
    private NavGraphSystem navGraphSystem;
    private PathfinderSystem pathfinderSystem;

    @Test
    public void updateChunkBeforePathRequests() throws InterruptedException {
        EntityRef entityRef = entityManager.create();
        entityRef.addComponent(new CharacterComponent());

        OnChunkLoaded chunkLoadedDummyEvent = new OnChunkLoaded(new Vector3i());

        navGraphSystem.chunkReady(chunkLoadedDummyEvent, entityRef);
        Optional<List<Path>> f1 = pathfinderSystem.requestPath(entityRef, new Vector3i(), Lists.newArrayList(new Vector3i())).blockOptional();
        Optional<List<Path>> f2 = pathfinderSystem.requestPath(entityRef, new Vector3i(), Lists.newArrayList(new Vector3i())).blockOptional();
        Optional<List<Path>> f3 = pathfinderSystem.requestPath(entityRef, new Vector3i(), Lists.newArrayList(new Vector3i())).blockOptional();
        while (pathfinderSystem.getPathsSearched() != 3) {
            Thread.sleep(10);
            eventSystem.process();
        }
        assertTrue(f1.isPresent());
        assertFalse(f2.isPresent());
        assertFalse(f3.isPresent());
    }

    @Test
    public void updateChunkAfterPathRequests() throws InterruptedException {
        EntityRef entityRef = entityManager.create();
        entityRef.addComponent(new CharacterComponent());

        OnChunkLoaded chunkLoadedDummyEvent = new OnChunkLoaded(new Vector3i());

        Optional<List<Path>> f1 = pathfinderSystem.requestPath(entityRef, new Vector3i(), Lists.newArrayList(new Vector3i())).blockOptional();
        Optional<List<Path>> f2 = pathfinderSystem.requestPath(entityRef, new Vector3i(), Lists.newArrayList(new Vector3i())).blockOptional();
        Optional<List<Path>> f3 = pathfinderSystem.requestPath(entityRef, new Vector3i(), Lists.newArrayList(new Vector3i())).blockOptional();
        navGraphSystem.chunkReady(chunkLoadedDummyEvent, entityRef);
        while (pathfinderSystem.getPathsSearched() != 3) {
            Thread.sleep(50);
            eventSystem.process();
        }
        assertTrue(f1.isPresent());
        assertTrue(f2.isPresent());
        assertTrue(f3.isPresent());
    }

    @BeforeEach
    public void setup(EntityManager entityManager, EventSystem eventSystem, ComponentSystemManager componentSystemManager) {
        this.entityManager = (PojoEntityManager) entityManager;
        this.eventSystem = eventSystem;
        navGraphSystem = new NavGraphSystem();
        componentSystemManager.register(navGraphSystem);

        pathfinderSystem = new PathfinderSystem() {
            @Override
            protected Pathfinder createPathfinder() {
                return mock(Pathfinder.class);
            }
        };
        componentSystemManager.register(pathfinderSystem);
    }
}
