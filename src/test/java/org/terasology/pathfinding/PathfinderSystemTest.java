// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.pathfinding;

import com.badlogic.gdx.physics.bullet.Bullet;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import org.joml.Vector3i;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.WorldProvidingHeadlessEnvironment;
import org.terasology.core.world.generator.AbstractBaseWorldGenerator;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.navgraph.NavGraphSystem;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.model.Pathfinder;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.chunks.event.OnChunkLoaded;

import static org.mockito.Mockito.mock;

/**
 * @author synopia
 */
public class PathfinderSystemTest {
    private PojoEntityManager entityManager;
    private EventSystem eventSystem;
    private NavGraphSystem navGraphSystem;
    private PathfinderSystem pathfinderSystem;
    private WorldProvidingHeadlessEnvironment environment;

    @BeforeEach
    public void before() throws Exception {
        // Hack to get natives to load for bullet
        PathManager.getInstance().useDefaultHomePath();
        Bullet.init();
    }


    @Test
    public void updateChunkBeforePathRequests() throws InterruptedException {
        EntityRef entityRef = entityManager.create();
        entityRef.addComponent(new CharacterComponent());

        navGraphSystem.chunkReady(mock(OnChunkLoaded.class), entityRef);
        ListenableFuture f1 = pathfinderSystem.requestPath(entityRef, new Vector3i(), Lists.newArrayList(new Vector3i()));
        ListenableFuture f2 = pathfinderSystem.requestPath(entityRef, new Vector3i(), Lists.newArrayList(new Vector3i()));
        ListenableFuture f3 = pathfinderSystem.requestPath(entityRef, new Vector3i(), Lists.newArrayList(new Vector3i()));
        while (pathfinderSystem.getPathsSearched() != 3) {
            Thread.sleep(10);
            eventSystem.process();
        }
        Assert.assertTrue(f1.isDone());
        Assert.assertTrue(f2.isDone());
        Assert.assertTrue(f3.isDone());
    }

    @Test
    public void updateChunkAfterPathRequests() throws InterruptedException {
        EntityRef entityRef = entityManager.create();
        entityRef.addComponent(new CharacterComponent());
        ListenableFuture f1 = pathfinderSystem.requestPath(entityRef, new Vector3i(), Lists.newArrayList(new Vector3i()));
        ListenableFuture f2 = pathfinderSystem.requestPath(entityRef, new Vector3i(), Lists.newArrayList(new Vector3i()));
        ListenableFuture f3 = pathfinderSystem.requestPath(entityRef, new Vector3i(), Lists.newArrayList(new Vector3i()));
        navGraphSystem.chunkReady(mock(OnChunkLoaded.class), entityRef);
        while (pathfinderSystem.getPathsSearched() != 3) {
            Thread.sleep(50);
            eventSystem.process();
        }
        Assert.assertTrue(f1.isDone());
        Assert.assertTrue(f2.isDone());
        Assert.assertTrue(f3.isDone());
    }

    @AfterEach
    public void teardown() throws Exception {
        environment.close();
    }

    @BeforeEach
    public void setup() {
        environment = new WorldProvidingHeadlessEnvironment();
        environment.setupWorldProvider(new AbstractBaseWorldGenerator(new SimpleUri("")) {
            @Override
            public void initialize() {

            }
        });
        entityManager = (PojoEntityManager) CoreRegistry.get(EntityManager.class);
        eventSystem = CoreRegistry.get(EventSystem.class);
        navGraphSystem = new NavGraphSystem();
        CoreRegistry.get(ComponentSystemManager.class).register(navGraphSystem);
        CoreRegistry.put(NavGraphSystem.class, navGraphSystem);

        pathfinderSystem = new PathfinderSystem() {
            @Override
            protected Pathfinder createPathfinder() {
                return mock(Pathfinder.class);
            }
        };
        CoreRegistry.get(ComponentSystemManager.class).register(pathfinderSystem);
        CoreRegistry.put(PathfinderSystem.class, pathfinderSystem);
    }
}
