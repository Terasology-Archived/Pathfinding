/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.pathfinding.componentSystem;

import com.google.common.collect.Lists;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.entitySystem.event.internal.EventReceiver;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.event.internal.EventSystemImpl;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.prefab.internal.PojoPrefabManager;
import org.terasology.math.Vector3i;
import org.terasology.minion.path.MinionPathComponent;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.pathfinding.model.Pathfinder;
import org.terasology.pathfinding.model.PathfinderWorld;
import org.terasology.persistence.typeSerialization.TypeSerializationLibrary;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.InjectionHelper;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.event.OnChunkLoaded;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author synopia
 */
public class PathfinderSystemTest {

    private PojoEntityManager entityManager;
    private EventSystem eventSystem;

    @Test
    public void updateChunkBeforePathRequests() throws InterruptedException {
        WorldProvider worldProvider = mock(WorldProvider.class);
        CoreRegistry.put(WorldProvider.class, worldProvider);
        final PathfinderSystem system = new PathfinderSystem() {
            @Override
            protected Pathfinder createPathfinder() {
                return mock(Pathfinder.class);
            }

            @Override
            protected PathfinderWorld createWorld() {
                return mock(PathfinderWorld.class);
            }
        };
        InjectionHelper.inject(system);

        system.initialise();

        final List<Integer> list = Lists.newArrayList();
        EntityRef entityRef = entityManager.create();
        entityRef.addComponent(new MinionPathComponent());
        eventSystem.registerEventReceiver(new EventReceiver<PathReadyEvent>() {
            @Override
            public void onEvent(PathReadyEvent event, EntityRef entity) {
                Assert.assertEquals(1, system.getChunkUpdates());
                list.add(event.getPathId());
            }
        }, PathReadyEvent.class, MinionPathComponent.class);
        system.chunkReady(mock(OnChunkLoaded.class), entityRef);
        int id1 = system.requestPath(entityRef, new Vector3i(), Lists.newArrayList(new Vector3i()));
        int id2 = system.requestPath(entityRef, new Vector3i(), Lists.newArrayList(new Vector3i()));
        int id3 = system.requestPath(entityRef, new Vector3i(), Lists.newArrayList(new Vector3i()));
        while (system.getPathsSearched() != 3) {
            Thread.sleep(10);
            eventSystem.process();
        }
        Assert.assertEquals(Arrays.asList(id1, id2, id3), list);
    }

    @Test
    public void updateChunkAfterPathRequests() throws InterruptedException {
        WorldProvider worldProvider = mock(WorldProvider.class);
        CoreRegistry.put(WorldProvider.class, worldProvider);
        final PathfinderSystem system = new PathfinderSystem() {
            @Override
            protected Pathfinder createPathfinder() {
                return mock(Pathfinder.class);
            }

            @Override
            protected PathfinderWorld createWorld() {
                return mock(PathfinderWorld.class);
            }
        };
        InjectionHelper.inject(system);

        system.initialise();

        final List<Integer> list = Lists.newArrayList();
        EntityRef entityRef = entityManager.create();
        entityRef.addComponent(new MinionPathComponent());
        eventSystem.registerEventReceiver(new EventReceiver<PathReadyEvent>() {
            @Override
            public void onEvent(PathReadyEvent event, EntityRef entity) {
                Assert.assertEquals(1, system.getChunkUpdates());
                list.add(event.getPathId());
            }
        }, PathReadyEvent.class, MinionPathComponent.class);
        int id1 = system.requestPath(entityRef, new Vector3i(), Lists.newArrayList(new Vector3i()));
        int id2 = system.requestPath(entityRef, new Vector3i(), Lists.newArrayList(new Vector3i()));
        int id3 = system.requestPath(entityRef, new Vector3i(), Lists.newArrayList(new Vector3i()));
        system.chunkReady(mock(OnChunkLoaded.class), entityRef);
        while (system.getPathsSearched() != 3) {
            Thread.sleep(10);
            eventSystem.process();
        }
        Assert.assertEquals(Arrays.asList(id1, id2, id3), list);
    }

    @Before
    public void setup() {
        ReflectFactory reflectFactory = new ReflectionReflectFactory();
        CopyStrategyLibrary copyStrategies = new CopyStrategyLibrary(reflectFactory);
        TypeSerializationLibrary serializationLibrary = new TypeSerializationLibrary(reflectFactory, copyStrategies);

        EntitySystemLibrary entitySystemLibrary = new EntitySystemLibrary(reflectFactory, copyStrategies, serializationLibrary);
        entityManager = new PojoEntityManager();
        entityManager.setEntitySystemLibrary(entitySystemLibrary);
        entityManager.setPrefabManager(new PojoPrefabManager());
        NetworkSystem networkSystem = mock(NetworkSystem.class);
        when(networkSystem.getMode()).thenReturn(NetworkMode.NONE);
        eventSystem = new EventSystemImpl(entitySystemLibrary.getEventLibrary(), networkSystem);
        entityManager.setEventSystem(eventSystem);
    }
}
