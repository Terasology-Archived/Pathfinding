// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.pathfinding;

import com.badlogic.gdx.physics.bullet.Bullet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.core.paths.PathManager;
import org.terasology.pathfinding.GeneralPathFinder.DefaultEdge;
import org.terasology.pathfinding.GeneralPathFinder.Edge;
import org.terasology.pathfinding.GeneralPathFinder.Path;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


/**
 * Tests the {@link org.terasology.pathfinding.model.Pathfinder} class.
 */
public class GeneralPathFinderTest {
    @BeforeEach
    public void before() throws Exception {
        // Hack to get natives to load for bullet
        PathManager.getInstance().useDefaultHomePath();
        Bullet.init();
    }

    private final Vertex fra = new Vertex("Frankfurt");
    private final Vertex man = new Vertex("Mannheim");
    private final Vertex wrz = new Vertex("Wuerzburg");
    private final Vertex stu = new Vertex("Stuttgart");
    private final Vertex kas = new Vertex("Kassel");
    private final Vertex kar = new Vertex("Karlsruhe");
    private final Vertex erf = new Vertex("Erfurt");
    private final Vertex nrn = new Vertex("Nuernberg");
    private final Vertex aug = new Vertex("Augsburg");
    private final Vertex muc = new Vertex("Muenchen");
    private final Vertex lon = new Vertex("London");  // not in the graph

    private final Collection<Edge<Vertex>> edges = Collections.unmodifiableList(Arrays.asList(
        new DefaultEdge<Vertex>(fra, man, 85),
        new DefaultEdge<Vertex>(fra, wrz, 217),
        new DefaultEdge<Vertex>(fra, kas, 173),
        new DefaultEdge<Vertex>(man, kar, 80),
        new DefaultEdge<Vertex>(wrz, erf, 186),
        new DefaultEdge<Vertex>(wrz, nrn, 103),
        new DefaultEdge<Vertex>(stu, nrn, 183),
        new DefaultEdge<Vertex>(kar, aug, 250),
        new DefaultEdge<Vertex>(kas, muc, 502),
        new DefaultEdge<Vertex>(aug, muc, 84),
        new DefaultEdge<Vertex>(nrn, muc, 167)));

    @Test
    public void testUnconstrainedFraMuc() {
        GeneralPathFinder<Vertex> dijkstra = new GeneralPathFinder<Vertex>(edges, true);
        Path<Vertex> path = dijkstra.computePath(fra, muc, Double.MAX_VALUE).get();

        Assertions.assertEquals(487, path.getLength(), 0.01);
        Assertions.assertEquals(fra, path.getStart());
        Assertions.assertEquals(fra, path.getSequence().get(0));
        Assertions.assertEquals(wrz, path.getSequence().get(1));
        Assertions.assertEquals(nrn, path.getSequence().get(2));
        Assertions.assertEquals(muc, path.getEnd());
        Assertions.assertEquals(muc, path.getSequence().get(3));

        Assertions.assertEquals(path.getLength(), path.getDistance(muc), 0.01);
    }

    @Test
    public void testUndirectedMucFra() {
        GeneralPathFinder<Vertex> dijkstra = new GeneralPathFinder<Vertex>(edges, false);
        Path<Vertex> path = dijkstra.computePath(muc, fra, Double.MAX_VALUE).get();

        Assertions.assertEquals(487, path.getLength(), 0.01);
        Assertions.assertEquals(muc, path.getStart());
        Assertions.assertEquals(wrz, path.getSequence().get(2));
        Assertions.assertEquals(nrn, path.getSequence().get(1));
        Assertions.assertEquals(fra, path.getEnd());
    }

    @Test
    public void testConstrainedFraMuc() {
        GeneralPathFinder<Vertex> dijkstra = new GeneralPathFinder<Vertex>(edges, true);
        Assertions.assertFalse(dijkstra.computePath(fra, muc, 450).isPresent());
    }

    @Test
    public void testDirectedMucFra() {
        GeneralPathFinder<Vertex> dijkstra = new GeneralPathFinder<Vertex>(edges, true);
        Assertions.assertFalse(dijkstra.computePath(muc, fra, Double.MAX_VALUE).isPresent());
    }

    @Test
    public void testDegenerated() {
        GeneralPathFinder<Vertex> dijkstra = new GeneralPathFinder<Vertex>(Collections.emptyList(), true);
        Assertions.assertFalse(dijkstra.computePath(muc, fra, Double.MAX_VALUE).isPresent());

        Path<Vertex> singleVertexPath = dijkstra.computePath(muc, muc, Double.MAX_VALUE).get();
        Assertions.assertEquals(muc, singleVertexPath.getStart());
        Assertions.assertEquals(muc, singleVertexPath.getEnd());
        Assertions.assertEquals(0, singleVertexPath.getLength(), 0);
    }

    @Test
    public void testDisconnected() {
        GeneralPathFinder<Vertex> dijkstra = new GeneralPathFinder<Vertex>(edges, true);
        Assertions.assertFalse(dijkstra.computePath(muc, lon, Double.MAX_VALUE).isPresent());
        Assertions.assertFalse(dijkstra.computePath(lon, fra, Double.MAX_VALUE).isPresent());
    }

    private static class Vertex {
        private final String name;

        public Vertex(String n) {
            this.name = n;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
