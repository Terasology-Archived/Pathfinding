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
package org.terasology.work.kmeans;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.navgraph.WalkableBlock;
import org.terasology.utilities.random.MersenneRandom;
import org.terasology.utilities.random.Random;
import org.terasology.work.WorkTargetComponent;

import javax.vecmath.Vector3f;
import java.util.List;

/**
 * Created by synopia on 07.02.14.
 */
public class Cluster {
    private List<Distance> distances = Lists.newArrayList();
    private List<Cluster> children = Lists.newArrayList();
    private Vector3f position;
    private DistanceFunction distanceFunction;
    private int maxNumberOfCluster;
    private Random random = new MersenneRandom();

    public Cluster(int maxNumberOfCluster, DistanceFunction distanceFunction) {
        this.maxNumberOfCluster = maxNumberOfCluster;
        this.distanceFunction = distanceFunction;
    }

    protected Cluster create() {
        return new Cluster(maxNumberOfCluster, distanceFunction);
    }

    public void add(EntityRef target) {
        WorkTargetComponent jobTarget = target.getComponent(WorkTargetComponent.class);
        List<WalkableBlock> targetPositions = jobTarget.getTargetPositions(target);
        for (WalkableBlock targetPosition : targetPositions) {
            add(new Distance(targetPosition.getBlockPosition().toVector3f(), target));
        }
    }

    public List<Distance> getDistances() {
        return distances;
    }

    public void add(Distance distance) {
        distances.add(distance);
    }

    public void clear() {
        distances.clear();
    }

    public void clearChildren() {
        for (Cluster cluster : children) {
            cluster.clear();
        }
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public void kmean() {
        int max = Math.min(maxNumberOfCluster, distances.size());
        children.clear();
        for (int i = 0; i < max; i++) {
            Cluster cluster = create();
            children.add(cluster);
            cluster.setPosition(new Vector3f(random.nextFloat(0, 8 * 16), 45, random.nextFloat(0, 8 * 16)));
        }

        float dist = Float.MAX_VALUE;
        while (dist > 100f) {
            dist = iterate() / distances.size();
        }

        for (Cluster cluster : children) {
            if (cluster.getDistances().size() > 16 * 16) {
                cluster.kmean();
            }
        }
    }

    public float iterate() {
        float totalDistChange = 0;
        clearChildren();
        for (Distance distance : distances) {
            float minDist = Float.MAX_VALUE;
            Cluster nearestCluster = null;
            for (Cluster cluster : children) {
                float dist = distanceFunction.distance(distance.getPosition(), cluster);
                if (dist < minDist) {
                    nearestCluster = cluster;
                    minDist = dist;
                }
            }
            if (nearestCluster != null) {
                totalDistChange += distance.setDistance(minDist);
                nearestCluster.add(distance);
            }
        }
        for (Cluster cluster : children) {
            cluster.update();
        }
        return totalDistChange;
    }

    public List<Cluster> getChildren() {
        return children;
    }

    public void update() {
        position = new Vector3f();
        for (Distance distance : distances) {
            position.add(distance.getPosition());
        }
        position.scale(1.f / distances.size());
    }

    public static final class Distance {
        private final Vector3f position;
        private final EntityRef job;
        private float distance;

        private Distance(Vector3f position, EntityRef job) {
            this.position = position;
            this.job = job;
        }

        public float getDistance() {
            return distance;
        }

        public float setDistance(float dist) {
            float old = this.distance;
            this.distance = dist;
            return Math.abs(old - dist);
        }

        public Vector3f getPosition() {
            return position;
        }

        public EntityRef getJob() {
            return job;
        }
    }

    public interface DistanceFunction {
        float distance(Vector3f target, Cluster cluster);
    }
}
