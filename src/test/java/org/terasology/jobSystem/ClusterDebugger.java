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

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.jobSystem.jobs.WalkToBlock;
import org.terasology.jobSystem.kmeans.Cluster;
import org.terasology.math.Vector3i;
import org.terasology.navgraph.Entrance;
import org.terasology.navgraph.Floor;
import org.terasology.navgraph.NavGraphSystem;
import org.terasology.navgraph.WalkableBlock;
import org.terasology.pathfinding.PathfinderTestGenerator;
import org.terasology.pathfinding.TestHelper;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.InjectionHelper;
import org.terasology.world.block.BlockComponent;

import javax.swing.*;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
    private final EntityManager entityManager;
    private WalkToBlock walkToBlock;
    private int distanceChecks;

    public ClusterDebugger() throws HeadlessException {
        entityManager = new PojoEntityManager();
        mapWidth = 160;
        mapHeight = 100;
        helper = new TestHelper();

//        helper.init(new MazeChunkGenerator(mapWidth, mapHeight, 4, 0, 20));
        helper.init(new PathfinderTestGenerator(true, true));

        CoreRegistry.put(EntityManager.class, entityManager);
        world = new NavGraphSystem();
        InjectionHelper.inject(world);
        world.initialise();
        PathfinderSystem pathfinderSystem = new PathfinderSystem();
        InjectionHelper.inject(pathfinderSystem);
        pathfinderSystem.initialise();

        for (int x = 0; x < mapWidth / 16 + 1; x++) {
            for (int z = 0; z < mapHeight / 16 + 1; z++) {
                world.updateChunk(new Vector3i(x, 0, z));
            }
        }
        level = 45;
        rootCluster = new Cluster(8, new Cluster.DistanceFunction() {
            @Override
            public float distance(Vector3f target, Cluster cluster) {
                distanceChecks++;
                Vector3f diff = new Vector3f(target);
                diff.sub(cluster.getPosition());
                return diff.lengthSquared();
            }
        });
        CoreRegistry.put(JobFactory.class, new JobFactory());
        walkToBlock = new WalkToBlock();
        InjectionHelper.inject(walkToBlock);
        walkToBlock.initialise();

        add(new DebugPanel());
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

                    Vector3i pos = new Vector3i();
                    for (int x = Math.min(block.x(), lastBlock.x()); x <= Math.max(block.x(), lastBlock.x()); x++) {
                        for (int y = Math.min(block.height(), lastBlock.height()); y <= Math.max(block.height(), lastBlock.height()); y++) {
                            for (int z = Math.min(block.z(), lastBlock.z()); z <= Math.max(block.z(), lastBlock.z()); z++) {
                                pos.set(x, y, z);
                                WalkableBlock currentBlock = world.getBlock(pos);
                                if (currentBlock != null) {
                                    EntityRef job = entityManager.create();
                                    JobTargetComponent jobTargetComponent = new JobTargetComponent();
                                    jobTargetComponent.setJob(walkToBlock);
                                    BlockComponent blockComponent = new BlockComponent();
                                    blockComponent.setPosition(currentBlock.getBlockPosition());

                                    job.addComponent(blockComponent);
                                    job.addComponent(jobTargetComponent);
                                    rootCluster.add(job);
                                }
                            }

                        }

                    }
                    distanceChecks = 0;
                    Stopwatch stopwatch = Stopwatch.createStarted();
                    rootCluster.kmean();
                    System.out.println("kmean = " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms  n=" + rootCluster.getDistances().size() + " distance checks=" + distanceChecks);
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
            for (int x = 0; x < mapWidth; x++) {
                for (int z = 0; z < mapHeight; z++) {
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
            drawCluster(g, rootCluster);
        }

        private void drawCluster(Graphics g, Cluster parent) {
            int id = 0;
            for (Cluster cluster : parent.getChildren()) {
                id++;
                if (cluster.getChildren().size() > 0) {
                    drawCluster(g, cluster);
                } else {
                    List<Cluster.Distance> distances = cluster.getDistances();
                    for (Cluster.Distance distance : distances) {
                        Vector3f position = distance.getPosition();
                        int x = (int) position.x;
                        int z = (int) position.z;
                        int screenX = x * getWidth() / mapWidth;
                        int screenY = z * getHeight() / mapHeight;
                        int tileWidth = (x + 1) * getWidth() / mapWidth - screenX;
                        int tileHeight = (z + 1) * getHeight() / mapHeight - screenY;
                        g.setColor(Color.yellow);
                        g.fillRect(screenX, screenY, tileWidth, tileHeight);
                        g.setColor(Color.black);
                        g.drawString(id + "", screenX, screenY + 8);
                    }
                }
            }
            id = 0;
            for (Cluster cluster : parent.getChildren()) {
                id++;
                if (cluster.getChildren().size() > 0) {
                    continue;
                }
                g.setColor(Color.BLUE);
                int x = (int) cluster.getPosition().x;
                int z = (int) cluster.getPosition().z;
                int screenX = x * getWidth() / mapWidth;
                int screenY = z * getHeight() / mapHeight;
                int tileWidth = (x + 1) * getWidth() / mapWidth - screenX;
                int tileHeight = (z + 1) * getHeight() / mapHeight - screenY;
                g.fillRect(screenX, screenY, tileWidth, tileHeight);
                g.setColor(Color.black);
                g.drawString(id + "", screenX, screenY + 8);
            }
        }
    }
}
