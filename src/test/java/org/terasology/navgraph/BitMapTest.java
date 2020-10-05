// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.navgraph;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

        Assertions.assertFalse(r1.overlap(r2));
        Assertions.assertFalse(r2.overlap(r1));
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

        Assertions.assertTrue(r1.overlap(r2));
        Assertions.assertTrue(r2.overlap(r1));
    }
}
