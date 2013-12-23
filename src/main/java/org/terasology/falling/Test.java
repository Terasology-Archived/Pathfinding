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
package org.terasology.falling;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * @author synopia
 */
public class Test extends JPanel {
    private Model model;
    private Simulator simulator;
    private int valueToWrite;
    private int clickedX;
    private int clickedY;

    public Test() {
        setPreferredSize(new Dimension(800,600));
        model = new Model(80,60);
        simulator = new Simulator(model);
        simulator.reset();
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int worldX = e.getX();
                int worldY = e.getY();
                float cellSizeX = (float) getWidth() / model.getSizeX();
                float cellSizeY = (float) getHeight() / model.getSizeY();
                int cellX = (int) (worldX / cellSizeX);
                int cellY = (int) ((getHeight() - worldY) / cellSizeY) + 1;
                if( e.isShiftDown() ) {
                    if( cellX==clickedX || cellY==clickedY ) {
                        model.set(cellX, cellY, valueToWrite);
                    }
                } else {
                    model.set(cellX, cellY, valueToWrite);
                }
                simulator.step();
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }
        });
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                int worldX = e.getX();
                int worldY = e.getY();
                float cellSizeX = (float) getWidth()/model.getSizeX();
                float cellSizeY = (float) getHeight()/model.getSizeY();
                int cellX = (int) (worldX/cellSizeX);
                int cellY = (int) ((getHeight()-worldY)/cellSizeY)+1;
                clickedX = cellX;
                clickedY = cellY;
                valueToWrite = e.getButton()==MouseEvent.BUTTON1 ? 1 : 0;
                model.set(cellX, cellY, valueToWrite);
                simulator.step();
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                simulator.step();
                repaint();
            }
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0,0,getWidth(), getHeight());
        float cellSizeX = (float) getWidth()/model.getSizeX();
        float cellSizeY = (float) getHeight()/model.getSizeY();

        for (int y = 0; y < model.getSizeY(); y++) {
            for (int x = 0; x < model.getSizeX(); x++) {
                int map = model.get(x, y);

                if( map !=0 ) {
                    int sx = (int) (x * cellSizeX);
                    int sy = getHeight()-(int) (y * cellSizeY);
                    Segment segment = simulator.getSegments().getSegment(x, y);
                    if( segment !=null ) {
                        if( segment.isGround() ) {
                            g.setColor(Color.LIGHT_GRAY);
                        } else {
                            float value = Math.max(0, Math.min(1, Math.abs(segment.totalForce/200)));
                            g.setColor(new Color(1f,1f-value,1f-value));
                        }
                        g.fillRect(sx, sy, (int) cellSizeX, (int) cellSizeY);
                        if( x%4==0 ){
                            g.setColor(Color.BLACK);
                            g.drawString( String.format("%.0f", Math.abs(segment.totalForce/2)), sx, sy);
                        }
                    }
                }

            }

        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new Test());
        frame.pack();
        frame.setVisible(true);
    }
}
