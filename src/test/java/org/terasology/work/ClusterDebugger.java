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
package org.terasology.work;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.event.internal.EventSystemImpl;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.prefab.internal.PojoPrefabManager;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.math.Vector3i;
import org.terasology.navgraph.Entrance;
import org.terasology.navgraph.Floor;
import org.terasology.navgraph.NavGraphSystem;
import org.terasology.navgraph.WalkableBlock;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.pathfinding.PathfinderTestGenerator;
import org.terasology.pathfinding.TestHelper;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.persistence.typeSerialization.TypeSerializationLibrary;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.InjectionHelper;
import org.terasology.rendering.nui.properties.OneOfProviderFactory;
import org.terasology.work.kmeans.Cluster;
import org.terasology.work.systems.WalkToBlock;
import org.terasology.world.block.BlockComponent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.vecmath.Vector3f;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author synopia
 */
public class ClusterDebugger extends JFrame {
    private TestHelper helper;
    private final int mapWidth;
    private final int mapHeight;
    private int level;
    private WalkableBlock hovered;
    private NavGraphSystem world;
    private Cluster rootCluster;
    private PojoEntityManager entityManager;
    private WalkToBlock walkToBlock;
    private int distanceChecks;
    private Vector3i nearest;
    private Vector3i target;
    private final WorkBoard workBoard;
    private ComponentSystemManager componentSystemManager;

    public ClusterDebugger() throws HeadlessException {
        setup();
        mapWidth = 160;
        mapHeight = 100;
        helper = new TestHelper();

//        helper.init(new MazeChunkGenerator(mapWidth, mapHeight, 4, 0, 20));
        helper.init(new PathfinderTestGenerator(true, true));

        world = start(NavGraphSystem.class, new NavGraphSystem());
        start(OneOfProviderFactory.class, new OneOfProviderFactory());
        PathfinderSystem pathfinderSystem = start(PathfinderSystem.class, new PathfinderSystem());

        workBoard = start(WorkBoard.class, new WorkBoard());

        for (int x = 0; x < mapWidth / 16 + 1; x++) {
            for (int z = 0; z < mapHeight / 16 + 1; z++) {
                world.updateChunk(new Vector3i(x, 0, z));
            }
        }
        level = 45;
        rootCluster = new Cluster(8, 4, 1, new Cluster.DistanceFunction() {
            @Override
            public float distance(Vector3i element, Vector3f target) {
                distanceChecks++;
                Vector3f diff = element.toVector3f();
                diff.sub(target);
                return diff.length();
            }

            @Override
            public float distance(Vector3i element, Vector3i target) {
                return 0;
            }
        });
        start(WorkFactory.class, new WorkFactory());
        walkToBlock = start(WalkToBlock.class, new WalkToBlock());

        add(new DebugPanel());
    }

    private void setup() {
        ReflectFactory reflectFactory = new ReflectionReflectFactory();
        CopyStrategyLibrary copyStrategies = new CopyStrategyLibrary(reflectFactory);
        TypeSerializationLibrary serializationLibrary = new TypeSerializationLibrary(reflectFactory, copyStrategies);

        EntitySystemLibrary entitySystemLibrary = new EntitySystemLibrary(reflectFactory, copyStrategies, serializationLibrary);
        ComponentLibrary compLibrary = entitySystemLibrary.getComponentLibrary();
        entityManager = new PojoEntityManager();
        entityManager.setEntitySystemLibrary(entitySystemLibrary);
        entityManager.setPrefabManager(new PojoPrefabManager());
        NetworkSystem networkSystem = mock(NetworkSystem.class);
        when(networkSystem.getMode()).thenReturn(NetworkMode.NONE);
        EventSystem eventSystem = new EventSystemImpl(entitySystemLibrary.getEventLibrary(), networkSystem);
        entityManager.setEventSystem(eventSystem);

        componentSystemManager = new ComponentSystemManager();
        componentSystemManager.initialise();

        CoreRegistry.put(ComponentSystemManager.class, componentSystemManager);
        CoreRegistry.put(EngineEntityManager.class, entityManager);
        CoreRegistry.put(EntityManager.class, entityManager);
        CoreRegistry.put(NetworkSystem.class, networkSystem);
        CoreRegistry.put(EventSystem.class, eventSystem);
    }

    private boolean isEntrance(WalkableBlock block) {
        boolean isEntrance = false;
        for (Entrance entrance : block.floor.entrances()) {
            if (entrance.getAbstractBlock() == block) {
                isEntrance = true;
                break;
            }
        }
        return isEntrance;
    }

    public static void main(String[] args) {
        ClusterDebugger debugger = new ClusterDebugger();
        debugger.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        debugger.pack();
        debugger.setVisible(true);

        while (true) {
            try {
                Thread.sleep(100);
                debugger.update(0.1f);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update(float dt) {
        entityManager.getEventSystem().process();
        distanceChecks = 0;
        Stopwatch stopwatch = Stopwatch.createStarted();
        Cluster cluster = workBoard.getWorkType(walkToBlock).getCluster();
        if (cluster.hkMean()) {
            System.out.println("kMean = " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms  n=" + cluster.getDistances().size() + " distance checks=" + distanceChecks);
            repaint();
        }
    }

    private <T> T start(Class<T> type, T system) {
        CoreRegistry.put(type, system);
        InjectionHelper.inject(system);
        if (system instanceof ComponentSystem) {
            ComponentSystem componentSystem = (ComponentSystem) system;
            entityManager.getEventSystem().registerEventHandler(componentSystem);
            componentSystemManager.register(componentSystem);
        }
        return system;
    }

    private final class DebugPanel extends JPanel {

        private WalkableBlock block;

        private DebugPanel() {
            addMouseMotionListener(new MouseMotionListener() {
                @Override
                public void mouseDragged(MouseEvent e) {
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    int hoverX = e.getX() * mapWidth / getWidth();
                    int hoverZ = e.getY() * mapHeight / getHeight();
                    hovered = world.getBlock(new Vector3i(hoverX, level, hoverZ));
                    repaint();
                }

            });
            addMouseWheelListener(new MouseAdapter() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    level += e.getWheelRotation();
                    repaint();
                }
            });
            addMouseListener(new MouseAdapter() {


                @Override
                public void mousePressed(MouseEvent e) {
                    int clickedX = e.getX() * mapWidth / getWidth();
                    int clickedZ = e.getY() * mapHeight / getHeight();
                    block = world.getBlock(new Vector3i(clickedX, level, clickedZ));
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    int clickedX = e.getX() * mapWidth / getWidth();
                    int clickedZ = e.getY() * mapHeight / getHeight();
                    WalkableBlock lastBlock = world.getBlock(new Vector3i(clickedX, level, clickedZ));

                    if (e.getButton() == MouseEvent.BUTTON1) {
                        Vector3i pos = new Vector3i();
                        for (int x = Math.min(block.x(), lastBlock.x()); x <= Math.max(block.x(), lastBlock.x()); x++) {
                            for (int y = Math.min(block.height(), lastBlock.height()); y <= Math.max(block.height(), lastBlock.height()); y++) {
                                for (int z = Math.min(block.z(), lastBlock.z()); z <= Math.max(block.z(), lastBlock.z()); z++) {
                                    pos.set(x, y, z);
                                    WalkableBlock currentBlock = world.getBlock(pos);
                                    if (currentBlock != null) {

                                        EntityRef job = entityManager.create();
                                        WorkTargetComponent jobTargetComponent = new WorkTargetComponent();
                                        jobTargetComponent.setWork(walkToBlock);
                                        BlockComponent blockComponent = new BlockComponent();
                                        blockComponent.setPosition(currentBlock.getBlockPosition());

                                        job.addComponent(blockComponent);
                                        job.addComponent(jobTargetComponent);
                                    }
                                }

                            }

                        }
                    } else {
                        target = new Vector3i(lastBlock.x(), lastBlock.height(), lastBlock.z());
                        distanceChecks = 0;
                        Cluster cluster = workBoard.getWorkType(walkToBlock).getCluster();
                        nearest = cluster.findNearest(target);
                        Stopwatch stopwatch = Stopwatch.createStarted();
                        System.out.println("find nearest = " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms distance checks=" + distanceChecks);
                    }
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Floor hoveredFloor = null;
            if (hovered != null) {
                hoveredFloor = hovered.floor;
            }

            for (int z = 0; z < mapHeight; z++) {
                for (int x = 0; x < mapWidth; x++) {
                    int screenX = x * getWidth() / mapWidth;
                    int screenY = z * getHeight() / mapHeight;
                    int tileWidth = (x + 1) * getWidth() / mapWidth - screenX;
                    int tileHeight = (z + 1) * getHeight() / mapHeight - screenY;
                    WalkableBlock block = world.getBlock(new Vector3i(x, level, z));
                    if (block != null) {
                        boolean isEntrance = isEntrance(block);

                        if (block.floor == hoveredFloor) {
                            if (isEntrance) {
                                g.setColor(Color.red);

                            } else {
                                g.setColor(Color.blue);
                            }
                        } else {
                            if (isEntrance) {
                                g.setColor(Color.lightGray);
                            } else {
                                g.setColor(Color.cyan);
                            }
                        }
                    } else {
                        g.setColor(Color.black);
                    }
                    g.fillRect(screenX, screenY, tileWidth, tileHeight);
                }
            }
            if (hovered != null) {
                boolean isEntrance = isEntrance(hovered);

                int screenX = hovered.x() * getWidth() / mapWidth;
                int screenY = hovered.z() * getHeight() / mapHeight;
                int tileWidth = (hovered.x() + 1) * getWidth() / mapWidth - screenX;
                int tileHeight = (hovered.z() + 1) * getHeight() / mapHeight - screenY;
                int x = screenX + tileWidth / 2;
                int y = screenY + tileHeight / 2;
                Set<Entrance> entrances;
                if (isEntrance) {
                    entrances = Sets.newHashSet();
                    for (Floor floor : hovered.floor.neighborRegions) {
                        entrances.addAll(floor.entrances());
                    }
                } else {
                    entrances = Sets.newHashSet(hovered.floor.entrances());
                }

                for (Entrance entrance : entrances) {
                    WalkableBlock block = entrance.getAbstractBlock();
                    screenX = block.x() * getWidth() / mapWidth;
                    screenY = block.z() * getHeight() / mapHeight;
                    tileWidth = (block.x() + 1) * getWidth() / mapWidth - screenX;
                    tileHeight = (block.z() + 1) * getHeight() / mapHeight - screenY;
                    int ex = screenX + tileWidth / 2;
                    int ey = screenY + tileHeight / 2;
                    if (block.height() == level) {
                        g.setColor(Color.BLACK);
                    } else {
                        g.setColor(Color.LIGHT_GRAY);
                    }
                    g.drawLine(x, y, ex, ey);
                }
            }
            WorkType workType = workBoard.getWorkType(walkToBlock);
            List<Cluster> leafCluster = workType.getCluster().getLeafCluster();

            int id = 1;
            for (Cluster cluster : leafCluster) {
                drawCluster(g, cluster, id, (float) id / leafCluster.size());
                id++;
            }
            if (nearest != null) {
                drawBlock(g, nearest.x, nearest.z, "O", Color.white);
            }
            if (target != null) {
                drawBlock(g, target.x, target.z, "X", Color.white);
            }
        }

        private void drawCluster(Graphics g, Cluster parent, int id, float color) {
            Map<Vector3i, Cluster.Distance> distances = parent.getDistances();
            Color col = new Color(color, color, color);
            for (Map.Entry<Vector3i, Cluster.Distance> entry : distances.entrySet()) {
                Vector3i position = entry.getKey();
                drawBlock(g, position.x, position.z, "", col);
            }
        }

        private void drawBlock(Graphics g, int x, int z, String text, Color color) {
            int screenX = x * getWidth() / mapWidth;
            int screenY = z * getHeight() / mapHeight;
            int tileWidth = (x + 1) * getWidth() / mapWidth - screenX;
            int tileHeight = (z + 1) * getHeight() / mapHeight - screenY;
            g.setColor(color);
            g.fillRect(screenX, screenY, tileWidth, tileHeight);
            g.setColor(Color.black);
            g.drawString(text, screenX, screenY + 8);
        }
    }
}
