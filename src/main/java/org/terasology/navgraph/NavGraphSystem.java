// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.navgraph;

import com.google.common.collect.Sets;
import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.core.GameScheduler;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.world.WorldChangeListener;
import org.terasology.engine.world.WorldComponent;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.event.BeforeChunkUnload;
import org.terasology.engine.world.chunks.event.OnChunkLoaded;
import reactor.core.publisher.Sinks;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RegisterSystem
@Share(value = NavGraphSystem.class)
public class NavGraphSystem extends BaseComponentSystem implements UpdateSubscriberSystem, WorldChangeListener {
    private static final float EVENT_COOLDOWN = 0.4f;

    @In
    private WorldProvider world;
    @In
    private EntityManager entityManager;

    private Map<Vector3i, NavGraphChunk> heightMaps = new HashMap<>();
    private boolean dirty;
    private float coolDown = EVENT_COOLDOWN;
    private int chunkUpdates;

    private Map<Vector3i, NavGraphChunk> maps = new HashMap<>();
    private Sinks.Many<Vector3i> chunkProcessingPublisher = Sinks.many().unicast().onBackpressureBuffer();
    private final Set<Vector3i> chunkProcessing = Sets.newHashSet();
    public NavGraphSystem() {
        chunkProcessingPublisher.asFlux()
                .name("nav-graph")
                .tag("monitor", "display-metric")
                .metrics()
                .distinct(k -> k, () -> chunkProcessing)
                .parallel().runOn(GameScheduler.parallel())
                .map(this::updateChunk)
                .sequential()
                .publishOn(GameScheduler.gameMain())
                .doOnNext(k -> chunkProcessing.remove(k.worldPos))
                .subscribe(navGraphChunk -> {
                    maps.put(navGraphChunk.worldPos, navGraphChunk);
                    dirty = true;
                });
    }

    @Override
    public void initialise() {
        world.registerListener(this);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void update(float delta) {
        if (dirty) {
            coolDown -= delta;
            if (coolDown < 0) {
                coolDown = EVENT_COOLDOWN;
                dirty = false;
                world.getWorldEntity().send(new NavGraphChanged());
            }
        }
    }

    @ReceiveEvent(components = WorldComponent.class)
    public void chunkReady(OnChunkLoaded event, EntityRef worldEntity) {
        chunkProcessingPublisher.tryEmitNext(new Vector3i(event.getChunkPos()));
    }

    @ReceiveEvent(components = WorldComponent.class)
    public void chunkReady(BeforeChunkUnload event, EntityRef worldEntity) {
        maps.remove(new Vector3i(event.getChunkPos()));
    }


    public WalkableBlock getBlock(Vector3ic pos) {
        Vector3i chunkPos = Chunks.toChunkPos(pos, new Vector3i());
        NavGraphChunk navGraphChunk = heightMaps.get(chunkPos);
        if (navGraphChunk != null) {
            return navGraphChunk.getBlock(pos.x(), pos.y(), pos.z());
        } else {
            return null;
        }
    }

    public WalkableBlock getBlock(EntityRef minion) {
        Vector3f pos = minion.getComponent(LocationComponent.class).getWorldPosition(new Vector3f());
        return getBlock(pos);
    }

    public WalkableBlock getBlock(Vector3fc pos) {
        Vector3i blockPos = new Vector3i(new Vector3f(pos.x() + 0.25f, pos.y(), pos.z() + 0.25f), RoundingMode.FLOOR);
        WalkableBlock block = getBlock(blockPos);
        if (block == null) {
            while (blockPos.y >= (int) pos.y() - 4 && block == null) {
                blockPos.y--;
                block = getBlock(blockPos);
            }
        }
        return block;
    }

    public int getChunkUpdates() {
        return chunkUpdates;
    }

    public NavGraphChunk updateChunk(Vector3i chunkPos) {
        if (chunkPos == null) {
            return null;
        }
        NavGraphChunk navGraphChunk = heightMaps.remove(chunkPos);
        if (navGraphChunk != null) {
            navGraphChunk.disconnectNeighborMaps(getNeighbor(chunkPos, -1, 0), getNeighbor(chunkPos, 0, -1), getNeighbor(chunkPos, 1, 0), getNeighbor(chunkPos, 0, 1));
            navGraphChunk.cells = null;
        }
        navGraphChunk = new NavGraphChunk(world, chunkPos);
        navGraphChunk.update();
        heightMaps.put(chunkPos, navGraphChunk);
        navGraphChunk.connectNeighborMaps(getNeighbor(chunkPos, -1, 0), getNeighbor(chunkPos, 0, -1), getNeighbor(chunkPos, 1, 0), getNeighbor(chunkPos, 0, 1));
        return navGraphChunk;
    }

    private NavGraphChunk getNeighbor(Vector3i chunkPos, int x, int z) {
        Vector3i neighborPos = new Vector3i(chunkPos);
        neighborPos.add(x, 0, z);
        return heightMaps.get(neighborPos);
    }

    @Override
    public void onExtraDataChanged(int i, Vector3ic pos, int newData, int oldData) {

    }

    @Override
    public void onBlockChanged(Vector3ic pos, Block newBlock, Block originalBlock) {
        chunkProcessingPublisher.tryEmitNext(Chunks.toChunkPos(pos, new Vector3i()));
    }
}
