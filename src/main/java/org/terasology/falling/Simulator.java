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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author synopia
 */
public class Simulator {
    private Model model;
    private Segments segments;
    private List<Segment> active = Lists.newArrayList();
    private Set<Segment> ground = Sets.newHashSet();
    private boolean pause;
    private boolean allUp;

    public Simulator(Model model) {
        this.model = model;
        segments = new Segments(model);
    }

    public void reset() {
        scan();
        calculateDownForces();
        pause = false;
    }

    public void tick() {
        calculateSpringForces(1);
        verlet();
        resolve();
    }

    public void step() {
        reset();

        int ticks = 200;
        while (!allUp) {
            tick();
            ticks--;
        }
        for (int i = 0; i < ticks; i++) {
             tick();
        }
        for (Segment segment : active) {
            if( segment.totalForce>200 ) {
                for (int y = 0; y < segment.blocksAbove; y++) {
                    model.set(segment.x, segment.y + y, 0);
                }
            } else if( Math.abs(segment.pos-segment.y)>=0.5f ) {
                fallDown(segment);
            }
        }
    }

    private void verlet() {
        for (Segment segment : active) {
            segment.verlet();
        }
    }
    private void calculateSpringForces(float damp) {
        for (Segment segment : active) {
            float springForce = 0;
            float totalForce = 0;
            for (Map.Entry<Segment, Integer> entry : segment.neighbors.entrySet()) {
                Segment neighbor = entry.getKey();
                float l0 = segment.y - neighbor.y;
                float l = segment.pos - neighbor.pos;
                float s = l - l0;
                float k = 10000f;
                float force = -s * k;
                springForce += force * damp * entry.getValue();
                totalForce += Math.abs(force);
            }
            segment.springForce = springForce;
            if( totalForce>segment.totalForce) {
                segment.totalForce = totalForce;
            }
        }
    }

    private void calculateDownForces() {
        active.clear();
        ground.clear();
        for (int x = 0; x < model.getSizeX(); x++) {
            for (Segment segment : segments.getSegments(x)) {
                if( !segment.isGround() ) {
                    segment.downForce = -segment.blocksAbove;
                    segment.pos = segment.y;
                    segment.lastPos = segment.y;
                    segment.totalForce = 0;
                    active.add(segment);
                } else {
                    for (Segment s : segment.neighbors.keySet()) {
                        if( !s.isGround() ) {
                            ground.add(s);
                        }
                    }
                }
            }
        }
    }

    private void resolve() {
        allUp = true;
        for (Segment segment : ground) {
            if( segment.moveDown() ) {
                allUp = false;
                break;
            }
        }
    }

    private void fallDown(Segment segment) {
        int y = segment.y-1;
        while (y<model.getSizeY() && model.get(segment.x, y+1)!=0 ) {
            model.set(segment.x, y, model.get(segment.x, y+1));
            y++;
        }
        model.set(segment.x, y, 0);
        segment.y--;
    }

    public void scan() {
        segments.scan();
    }

    public Segments getSegments() {
        return segments;
    }

}
