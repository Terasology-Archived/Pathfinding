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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

/**
* @author synopia
*/
public class Segment {
    public int x;
    public int y;
    public int blocksAbove;
    public float pos;
    public float lastPos;
    public float downForce;
    public float springForce;
    public float totalForce;
    public Map<Segment, Integer> neighbors = Maps.newHashMap();
    public Set<Segment> parents = Sets.newHashSet();

    Segment(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isGround() {
        return y==0;
    }

    public void addBlock() {
        blocksAbove ++;
    }

    public void connectTo( Segment other ) {
        addNeighbor( other );
        other.addNeighbor(this);
    }

    private void addNeighbor(Segment other) {
        if( neighbors.containsKey(other) ) {
            neighbors.put(other, neighbors.get(other)+1);
        } else {
            neighbors.put(other, 1);
        }
    }

    public boolean moveUp() {
        return lastPos<pos;
    }

    public boolean moveDown() {
        return lastPos>pos;
    }

    public void verlet() {
        float dt = 0.01f;
        if( !isGround() ) {
            float a = (springForce + downForce) / blocksAbove;
            float oldPos = lastPos;
            lastPos = pos;
            pos += (pos- oldPos) + a*dt*dt;
        }
    }
}
