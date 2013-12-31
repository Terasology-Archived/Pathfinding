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
package org.terasology.falling.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * Created by synopia on 12/29/13.
 */
public class Simulator {
    private List<SegmentMap> maps = Lists.newArrayList();
    private Map<Segment, MassPoint> massPoints = Maps.newHashMap();
    private List<Spring> springs = Lists.newArrayList();

    public void build() {
        massPoints.clear();
        springs.clear();
        for (SegmentMap map : maps) {
            for (Segment segment : map.segments()) {
                MassPoint massPoint = new MassPoint();
                massPoints.put(segment, massPoint);
                massPoint.force = 0;
                massPoint.mass = segment.height;
                massPoint.position = segment.y;
            }
        }
        for (SegmentMap map : maps) {
            for (Segment segment : map.segments()) {
                for (Map.Entry<Segment, Integer> entry : segment.neighbors.entrySet()) {
                    Segment neighbor = entry.getKey();
                    Integer thickness = entry.getValue();
                    Spring spring = new Spring();
                    springs.add(spring);
                    spring.one = massPoints.get(segment);
                    spring.two = massPoints.get(neighbor);
                    spring.thickness = thickness;
                    spring.restLength = spring.two.position - spring.one.position;
                }
            }
        }
    }

    public void tick(float dt) {
        prepare();
        applySpringForces(dt);
        verlet(dt);
    }

    public void prepare() {
        for (MassPoint massPoint : massPoints.values()) {
            massPoint.totalForce = 0;
            massPoint.force = 0;
        }
    }

    public void applySpringForces(float dt) {
        float ks = 0;
        float kd = 0;
        for (Spring spring : springs) {
            float velocity1 = (spring.one.position - spring.one.lastPosition) / dt;
            float velocity2 = (spring.two.position - spring.two.lastPosition) / dt;
            float deltaY = spring.one.position - spring.two.position;
            float deltaV = velocity1 - velocity2;
            float dist = Math.abs(deltaY);

            float left = -ks * (dist - spring.restLength);
            float right = kd * deltaV * deltaY / dist;
            float springForce = (left + right);
            spring.one.force += springForce / spring.one.mass;
            spring.two.force += springForce / spring.two.mass;
            spring.one.totalForce += Math.abs(springForce / spring.one.mass);
            spring.two.totalForce += Math.abs(springForce / spring.two.mass);
        }
    }

    public void verlet(float dt) {
        float g = 1;
        for (MassPoint massPoint : massPoints.values()) {
            float a = (massPoint.force + massPoint.mass * g) / massPoint.mass;
            float last = massPoint.position;
            massPoint.position = massPoint.position * 2 - massPoint.lastPosition + a * dt * dt;
            massPoint.lastPosition = last;
        }
    }

    private static class MassPoint {
        public float mass;
        public float position;
        public float lastPosition;
        public float force;
        public float totalForce;

    }

    private static class Spring {
        public MassPoint one;
        public MassPoint two;
        public float thickness;
        public float restLength;

    }
}
