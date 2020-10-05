// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.pathfinding;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.terasology.navgraph.BitMap;
import org.terasology.pathfinding.model.AStar;

import java.util.Collections;
import java.util.List;

/**
 * @author synopia
 */
public class AStarTest {
    @Test
    public void maze() {
        assertAStar(
                "XXXXXXXX",
                "XS**** X",
                "XXXXXX*X",
                "X **** X",
                "X*XXXXXX",
                "X **** X",
                "XXXXXX*X",
                "XE**** X",
                "XXXXXXXX"
        );

    }

    @Test
    public void simple() {
        assertAStar(
                "XXXXX",
                "XS*EX",
                "XXXXX"
        );
        assertAStar(
                "XXXXXXXX",
                "XS     X",
                "X *    X",
                "X  *   X",
                "X   *  X",
                "X    * X",
                "X     EX",
                "XXXXXXXX"
        );
        assertAStar(
                "XXXXXXXX",
                "X     EX",
                "X    * X",
                "X   *  X",
                "X  *   X",
                "X *    X",
                "XS     X",
                "XXXXXXXX"
        );
        assertAStar(
                "XXXXXXXX",
                "XE     X",
                "X *    X",
                "X  *   X",
                "X   *  X",
                "X    * X",
                "X     SX",
                "XXXXXXXX"
        );
        assertAStar(
                "XXXXXXXX",
                "X     SX",
                "X    * X",
                "X   *  X",
                "X  *   X",
                "X *    X",
                "XE     X",
                "XXXXXXXX"
        );
    }

    private void assertAStar(String... data) {
        BitMap map = new BitMap();
        int start = -1;
        int end = -1;
        List<Integer> expected = Lists.newArrayList();
        for (int y = 0; y < data.length; y++) {
            String row = data[y];
            for (int x = 0; x < row.length(); x++) {
                int offset = map.offset(x, y);
                switch (row.charAt(x)) {
                    case 'X':
                        break;
                    case 'S':
                        start = offset;
                        map.setPassable(start);
                        expected.add(offset);
                        break;
                    case 'E':
                        end = offset;
                        map.setPassable(end);
                        expected.add(offset);
                        break;
                    case '*':
                        map.setPassable(offset);
                        expected.add(offset);
                        break;
                    case ' ':
                        map.setPassable(offset);
                        break;
                    default:
                        break;
                }
            }
        }

        AStar sut = new AStar(map);
        sut.run(start, end);
        List<Integer> path = sut.getPath();
        Collections.sort(path);
        Collections.sort(expected);
        Assertions.assertArrayEquals(expected.toArray(), path.toArray());
    }
}
