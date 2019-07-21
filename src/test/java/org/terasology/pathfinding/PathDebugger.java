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
package org.terasology.pathfinding;

import com.google.common.collect.Sets;
import org.terasology.WorldProvidingHeadlessEnvironment;
import org.terasology.core.world.generator.AbstractBaseWorldGenerator;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.SimpleUri;
import org.terasology.math.geom.Vector3i;
import org.terasology.navgraph.Entrance;
import org.terasology.navgraph.Floor;
import org.terasology.navgraph.NavGraphSystem;
import org.terasology.navgraph.WalkableBlock;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.model.LineOfSight;
import org.terasology.pathfinding.model.LineOfSight2d;
import org.terasology.pathfinding.model.Path;
import org.terasology.registry.CoreRegistry;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.util.Set;

/**
 * @author synopia
 */
public class PathDebugger extends JFrame {
    private WorldProvidingHeadlessEnvironment env;
    private final int mapWidth;
    private final int mapHeight;
    private int level;
    private WalkableBlock hovered;
    private WalkableBlock start;
    private WalkableBlock target;
    private Path path;
    private final NavGraphSystem world;
    private boolean isSight;
    private final PathfinderSystem pathfinderSystem;
    private transient LineOfSight lineOfSight;

    public PathDebugger() throws HeadlessException {
        env = new WorldProvidingHeadlessEnvironment();
        env.setupWorldProvider(new AbstractBaseWorldGenerator(new SimpleUri("")) {
            @Override
            public void initialize() {
                register(new PathfinderTestGenerator(true, true));
            }
        });

        mapWidth = 160;
        mapHeight = 100;

        world = new NavGraphSystem();
        CoreRegistry.get(ComponentSystemManager.class).register(world);

        lineOfSight = new LineOfSight2d();
        CoreRegistry.get(ComponentSystemManager.class).register(lineOfSight);

        pathfinderSystem = new PathfinderSystem();
        CoreRegistry.get(ComponentSystemManager.class).register(pathfinderSystem);

        for (int x = 0; x < mapWidth / 16 + 1; x++) {
            for (int z = 0; z < mapHeight / 16 + 1; z++) {
                this.world.updateChunk(new Vector3i(x, 0, z));
            }
        }
        level = 45;
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
        PathDebugger debugger = new PathDebugger();
        debugger.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        debugger.pack();
        debugger.setVisible(true);
    }

    private final class DebugPanel extends JPanel {
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
                public void mouseReleased(MouseEvent e) {
                    int clickedX = e.getX() * mapWidth / getWidth();
                    int clickedZ = e.getY() * mapHeight / getHeight();
                    WalkableBlock block = world.getBlock(new Vector3i(clickedX, level, clickedZ));
                    if (start == null) {
                        start = block;
                        isSight = false;
                    } else {
                        if (target == null) {
                            target = block;
                            if (e.getButton() == MouseEvent.BUTTON1) {
                                path = pathfinderSystem.findPath(target, start);
                                isSight = false;
                            } else {
                                isSight = lineOfSight.inSight(start, target);
                            }
                        } else {
                            isSight = false;
                            start = block;
                            target = null;
                        }
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
                    if (block != null && (block == start || block == target)) {
                        g.setColor(Color.yellow);
                        g.fillOval(screenX, screenY, tileWidth, tileHeight);
                    }
                }
            }
            if (path != null) {
                for (WalkableBlock block : path.getNodes()) {
                    if (block.height() != level) {
                        continue;
                    }
                    int screenX = block.x() * getWidth() / mapWidth;
                    int screenY = block.z() * getHeight() / mapHeight;
                    int tileWidth = (block.x() + 1) * getWidth() / mapWidth - screenX;
                    int tileHeight = (block.z() + 1) * getHeight() / mapHeight - screenY;
                    g.setColor(Color.yellow);
                    g.fillOval(screenX, screenY, tileWidth, tileHeight);
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
            if (start != null && target != null && isSight) {
                int x0 = start.x() * getWidth() / mapWidth;
                int z0 = start.z() * getHeight() / mapHeight;
                int x1 = target.x() * getWidth() / mapWidth;
                int z1 = target.z() * getHeight() / mapHeight;
                g.setColor(Color.BLUE);
                g.drawLine(x0, z0, x1, z1);
            }
        }
    }
}
