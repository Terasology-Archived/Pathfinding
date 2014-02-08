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
package org.terasology.pathfinding.model;

import com.google.common.collect.Lists;
import org.terasology.navgraph.BitMap;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author synopia
 */
public class AStar {
    private BitMap graph;
    private float[] gMap;
    private float[] fMap;
    private int[] pMap;
    private List<Integer> successors = Lists.newArrayList();

    private int start;
    private int end;

    private BinaryHeap openList;

    private List<Integer> closedList = Lists.newArrayList();

    public AStar(BitMap graph) {
        this.graph = graph;
        gMap = new float[graph.getNumberOfNodes()];
        fMap = new float[graph.getNumberOfNodes()];
        pMap = new int[graph.getNumberOfNodes()];
        openList = new BinaryHeap(new Comparator<Integer>() {
            public int compare(Integer a, Integer b) {
                float fA = fMap[a];
                float fB = fMap[b];
                return -(fA < fB ? -1 : (fA > fB ? 1 : 0));
            }
        }, 1024, graph.getNumberOfNodes());
    }

    public void reset() {
        Arrays.fill(gMap, 0);
        Arrays.fill(fMap, 0);
        Arrays.fill(pMap, 0);
        closedList.clear();
        openList.clear();
    }

    public boolean run(int newStart, int newEnd) {
        reset();
        this.start = newStart;
        this.end = newEnd;

        fMap[start] = 0;
        openList.insert(start);

        while (!openList.isEmpty()) {
            int current = openList.removeMin();
            if (current == end) {
                return true;
            }
            expand(current);
            closedList.add(current);
        }
        return false;
    }

    @Override
    public String toString() {
        List<Integer> path = Lists.newArrayList();
        getPath(path);
        int id = 0;
        StringBuffer sb = new StringBuffer();
        for (int y = 0; y < graph.getHeight(); y++) {
            for (int x = 0; x < graph.getWidth(); x++) {
                char ch = ' ';
                if (!graph.isPassable(id)) {
                    ch = 'X';
                }
                if (path.contains(id)) {
                    ch = '*';
                }
                sb.append(ch);
                id++;
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public List<Integer> getPath() {
        List<Integer> result = Lists.newArrayList();
        getPath(result);
        return result;
    }

    public void getPath(List<Integer> list) {
        int current = end;
        while (current != start && current != 0) {
            list.add(current);
            current = pMap[current];
        }
        list.add(start);
    }

    protected void expand(int current) {
        successors.clear();
        graph.getSuccessors(current, successors);
        for (Integer successor : successors) {
            if (closedList.contains(successor)) {
                continue;
            }

            float tentativeG = gMap[current] + c(current, successor);
            if (openList.contains(successor) && tentativeG >= gMap[successor]) {
                continue;
            }

            pMap[successor] = current;
            gMap[successor] = tentativeG;
            fMap[successor] = tentativeG + h(successor);

            if (openList.contains(successor)) {
                openList.update(successor);
            } else {
                openList.insert(successor);
            }
        }
    }

    protected float c(int from, int to) {
        return graph.exactDistance(from, to);
    }

    protected float h(int current) {
        return graph.fastDistance(current, end);
    }

    public float getG(int id) {
        return gMap[id];
    }

    public float getF(int id) {
        return fMap[id];
    }

    public int getP(int id) {
        return pMap[id];
    }
}
