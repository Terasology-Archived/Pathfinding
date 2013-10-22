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
package org.terasology.pathfinding.model;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author synopia
 */
public class BitMapTest {
    @Test
    public void testNoOverlap() {
        BitMap r1 = new BitMap();
        BitMap r2 = new BitMap();

        r1.setPassable(0, 0);
        r1.setPassable(1, 0);
        r1.setPassable(2, 0);

        r2.setPassable(0, 1);
        r2.setPassable(1, 1);
        r2.setPassable(2, 1);

        Assert.assertFalse(r1.overlap(r2));
        Assert.assertFalse(r2.overlap(r1));
    }

    @Test
    public void testOverlap() {
        BitMap r1 = new BitMap();
        BitMap r2 = new BitMap();

        r1.setPassable(0, 0);
        r1.setPassable(1, 0);
        r1.setPassable(2, 0);
        r2.setPassable(0, 1);
        r2.setPassable(1, 0);
        r2.setPassable(1, 1);
        r2.setPassable(2, 1);

        Assert.assertTrue(r1.overlap(r2));
        Assert.assertTrue(r2.overlap(r1));
    }
}
