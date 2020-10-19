// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.navgraph;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author synopia
 */
public class EntranceTest {
    @Test
    public void testHorizontal() {
        Entrance entrance = new Entrance(null);
        entrance.addToEntrance(1, 1);
        Assertions.assertTrue(entrance.isPartOfEntrance(1, 1));
        Assertions.assertTrue(entrance.isPartOfEntrance(2, 1));
        Assertions.assertFalse(entrance.isPartOfEntrance(3, 1));
        entrance.addToEntrance(2, 1);
        Assertions.assertTrue(entrance.isPartOfEntrance(3, 1));
        Assertions.assertFalse(entrance.isPartOfEntrance(1, 2));
    }

    @Test
    public void testVertical() {
        Entrance entrance = new Entrance(null);
        entrance.addToEntrance(1, 1);
        Assertions.assertTrue(entrance.isPartOfEntrance(1, 1));
        Assertions.assertTrue(entrance.isPartOfEntrance(1, 2));
        Assertions.assertFalse(entrance.isPartOfEntrance(1, 3));
        entrance.addToEntrance(1, 2);
        Assertions.assertTrue(entrance.isPartOfEntrance(1, 3));
        Assertions.assertFalse(entrance.isPartOfEntrance(2, 1));
    }
}
