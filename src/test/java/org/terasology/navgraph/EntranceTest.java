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
package org.terasology.navgraph;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author synopia
 */
public class EntranceTest {
    @Test
    public void testHorizontal() {
        Entrance entrance = new Entrance(null);
        entrance.addToEntrance(1, 1);
        Assert.assertTrue(entrance.isPartOfEntrance(1, 1));
        Assert.assertTrue(entrance.isPartOfEntrance(2, 1));
        Assert.assertFalse(entrance.isPartOfEntrance(3, 1));
        entrance.addToEntrance(2, 1);
        Assert.assertTrue(entrance.isPartOfEntrance(3, 1));
        Assert.assertFalse(entrance.isPartOfEntrance(1, 2));
    }

    @Test
    public void testVertical() {
        Entrance entrance = new Entrance(null);
        entrance.addToEntrance(1, 1);
        Assert.assertTrue(entrance.isPartOfEntrance(1, 1));
        Assert.assertTrue(entrance.isPartOfEntrance(1, 2));
        Assert.assertFalse(entrance.isPartOfEntrance(1, 3));
        entrance.addToEntrance(1, 2);
        Assert.assertTrue(entrance.isPartOfEntrance(1, 3));
        Assert.assertFalse(entrance.isPartOfEntrance(2, 1));
    }
}
