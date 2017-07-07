/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.pathfinding;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.terasology.WorldProvidingHeadlessEnvironment;
import org.terasology.core.world.generator.AbstractBaseWorldGenerator;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.math.geom.Vector3i;
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

    @After
    public void teardown() throws Exception {
        environment.close();
    }

    @Before
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
