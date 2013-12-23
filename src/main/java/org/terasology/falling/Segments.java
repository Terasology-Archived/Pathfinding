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

import java.util.List;

/**
 * @author synopia
 */
public class Segments {
    private Model model;
    private List<Segment>[] map;

    public Segments(Model model) {
        this.model = model;
        map = new List[model.getSizeX()];
        for (int i = 0; i < model.getSizeX(); i++) {
             map[i] = Lists.newArrayList();
        }
    }

    public Segment getSegment(int x, int y) {
        Segment found = null;
        for (Segment segment : map[x]) {
            if( segment.y<=y && y<segment.y+segment.blocksAbove) {
                found = segment;
            }
        }
        return found;
    }

    public List<Segment> getSegments(int x) {
        return map[x];
    }

    public void scan() {
        for (int x = 0; x < model.getSizeX(); x++) {
            map[x].clear();

            boolean wall = true;
            Segment current = new Segment(x, 0);
            for( int y=0; y<model.getSizeY(); y++ ) {
                if( wall ) {
                    if( model.get(x,y)==0 ) {
                        // found gap
                        map[x].add(current);
                        wall = false;
                    }
                } else {
                    if( model.get(x,y)!=0 ) {
                        wall = true;
                        current = new Segment(x, y);
                    }
                }
                if( model.get(x,y)!=0 ) {
                    Segment neighbor = null;
                    if( x>0 ) {
                        neighbor = getSegment(x - 1, y);
                    }
                    current.addBlock();
                    if( neighbor!=null ) {
                        current.connectTo(neighbor);
                    }
                }
            }
        }
    }

}
