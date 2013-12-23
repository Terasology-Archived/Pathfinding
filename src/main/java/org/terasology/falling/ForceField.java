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

import java.util.Arrays;

/**
 * @author synopia
 */
public class ForceField {
    private float[] data;
    private int sizeX;
    private int sizeY;

    public ForceField(int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        data = new float[sizeX*sizeY];
    }

    public void reset() {
        Arrays.fill(data, 0);
    }

    public float get(int x, int y ) {
        return data[x + y*sizeX];
    }
    public void set( int x, int y, float v ) {
        data[x + y*sizeX] = v;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

}
