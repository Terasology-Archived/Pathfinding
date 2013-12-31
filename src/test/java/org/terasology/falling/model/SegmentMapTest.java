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

import junit.framework.Assert;
import org.junit.Test;
import org.terasology.pathfinding.model.TestHelper;

/**
 * Created by synopia on 12/30/13.
 */
public class SegmentMapTest {

    @Test
    public void testGround1() {
        final TestHelper helper = new TestHelper();
        helper.init();

        helper.setGround(" XXX|  XX|   X");

        SegmentMap map = new SegmentMap();
        map.load(helper.world);

        SegmentCell cell = map.getCell(0, 0);
        Assert.assertEquals(1, cell.segments.size());
        Assert.assertTrue(cell.segments.get(0).isGround());
        Assert.assertEquals(0, cell.segments.get(0).height);

        cell = map.getCell(1, 0);
        Assert.assertEquals(1, cell.segments.size());
        Assert.assertTrue(cell.segments.get(0).isGround());
        Assert.assertEquals(1, cell.segments.get(0).height);

        cell = map.getCell(2, 0);
        Assert.assertEquals(1, cell.segments.size());
        Assert.assertTrue(cell.segments.get(0).isGround());
        Assert.assertEquals(2, cell.segments.get(0).height);

        cell = map.getCell(3, 0);
        Assert.assertEquals(1, cell.segments.size());
        Assert.assertTrue(cell.segments.get(0).isGround());
        Assert.assertEquals(3, cell.segments.get(0).height);
    }

    @Test
    public void testGround2() {
        final TestHelper helper = new TestHelper();
        helper.init();

        helper.setGround(" X  |X   | X  ");

        SegmentMap map = new SegmentMap();
        map.load(helper.world);

        SegmentCell cell = map.getCell(0, 0);
        Assert.assertEquals(2, cell.segments.size());
        Assert.assertTrue(cell.segments.get(0).isGround());
        Assert.assertEquals(0, cell.segments.get(0).height);
        Assert.assertTrue(!cell.segments.get(1).isGround());
        Assert.assertEquals(1, cell.segments.get(1).height);

        cell = map.getCell(1, 0);
        Assert.assertEquals(2, cell.segments.size());
        Assert.assertTrue(cell.segments.get(0).isGround());
        Assert.assertEquals(1, cell.segments.get(0).height);
        Assert.assertTrue(!cell.segments.get(1).isGround());
        Assert.assertEquals(1, cell.segments.get(1).height);

    }

    @Test
    public void testNeighbors() {
        final TestHelper helper = new TestHelper();
        helper.init();

        helper.setGround(
                "XXX|XXX|XXX",
                "XXX|XXX|XXX",
                "XXX|XXX|XXX"
        );

        SegmentMap map = new SegmentMap();
        map.load(helper.world);

        Segment up = map.getCell(1, 0).segments.get(0);
        Segment right = map.getCell(2, 1).segments.get(0);
        Segment down = map.getCell(1, 2).segments.get(0);
        Segment left = map.getCell(0, 1).segments.get(0);
        Segment mid = map.getCell(1, 1).segments.get(0);

        Assert.assertEquals(4, mid.neighbors.size());

        Assert.assertTrue(mid.neighbors.containsKey(up));
        Assert.assertTrue(mid.neighbors.containsKey(right));
        Assert.assertTrue(mid.neighbors.containsKey(down));
        Assert.assertTrue(mid.neighbors.containsKey(left));

        Assert.assertEquals(3, (int) mid.neighbors.get(up));
        Assert.assertEquals(3, (int) mid.neighbors.get(right));
        Assert.assertEquals(3, (int) mid.neighbors.get(down));
        Assert.assertEquals(3, (int) mid.neighbors.get(left));
    }
}
