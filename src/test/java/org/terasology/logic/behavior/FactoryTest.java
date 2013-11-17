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
package org.terasology.logic.behavior;

import com.google.common.collect.Lists;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.xml.internal.ws.util.ByteArrayBuffer;
import junit.framework.Assert;
import org.junit.Test;
import org.terasology.logic.behavior.tree.CounterNode;
import org.terasology.logic.behavior.tree.MonitorNode;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.ParallelNode;
import org.terasology.logic.behavior.tree.RepeatNode;
import org.terasology.logic.behavior.tree.SequenceNode;
import org.terasology.logic.behavior.ui.RenderableNode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author synopia
 */
public class FactoryTest {
    @Test
    public void testLoadSaveNodes() {
        BehaviorFactory factory = new BehaviorFactory();
        SequenceNode sequence = buildSample();

        StringWriter out = new StringWriter();
        factory.saveNode(sequence, new JsonWriter(out));
        String jsonExpected = out.toString();

        StringReader in = new StringReader(jsonExpected);
        Node actual = factory.loadNode(new JsonReader(in));
        out = new StringWriter();
        factory.saveNode(actual, new JsonWriter(out));
        String jsonActual = out.toString();
        Assert.assertEquals(jsonActual, jsonExpected);
    }

    @Test
    public void testSaveLoadRenderables() {
        BehaviorFactory factory = new BehaviorFactory();
        SequenceNode sequence = buildSample();
        RenderableNode renderableSequence = factory.addNode(sequence);
        StringWriter out = new StringWriter();
        factory.saveRenderableNode(renderableSequence, new JsonWriter(out));
        String jsonExpected = out.toString();

        StringReader in = new StringReader(jsonExpected);
        RenderableNode actual = factory.loadRenderableNode(new JsonReader(in));
        out = new StringWriter();
        factory.saveRenderableNode(actual, new JsonWriter(out));
        String jsonActual = out.toString();
        Assert.assertEquals(jsonActual, jsonExpected);
    }

    @Test
    public void testSaveLoad() throws IOException {
        BehaviorFactory factory = new BehaviorFactory();
        SequenceNode sequence = buildSample();
        RenderableNode renderableSequence = factory.addNode(sequence);
        ByteArrayBuffer bab = new ByteArrayBuffer(10000);
        factory.save(renderableSequence, bab);
        String jsonExpected = bab.toString();

        RenderableNode actual = factory.load(new ByteArrayInputStream(jsonExpected.getBytes()));
        bab = new ByteArrayBuffer(10000);
        factory.save(actual, bab);
        String jsonActual = bab.toString();
        Assert.assertEquals(jsonActual, jsonExpected);
    }

    @Test
    public void testConstructRenderables() {
        BehaviorFactory factory = new BehaviorFactory();
        SequenceNode sequence = buildSample();
        RenderableNode renderableSequence = factory.addNode(sequence);
        final List<Node> actual = Lists.newArrayList();
        renderableSequence.visit(new RenderableNode.Visitor() {
            @Override
            public void visit(RenderableNode node) {
                actual.add(node.getNode());
            }
        });
        final List<Node> sequenceNodes = sequence.visit(new ArrayList<Node>(), new Node.Visitor<List<Node>>() {
            @Override
            public List<Node> visit(List<Node> result, Node node) {
                result.add(node);
                return result;
            }
        });
        Assert.assertEquals(sequenceNodes, actual);
    }

    private SequenceNode buildSample() {
        SequenceNode sequence = new SequenceNode();
        sequence.children().add(new CounterNode(1));
        sequence.children().add(new RepeatNode(new CounterNode(2)));
        ParallelNode parallel = new ParallelNode(ParallelNode.Policy.RequireAll, ParallelNode.Policy.RequireAll);
        sequence.children().add(parallel);
        parallel.children().add(new MonitorNode());
        parallel.children().add(new CounterNode(3));
        return sequence;
    }
}
