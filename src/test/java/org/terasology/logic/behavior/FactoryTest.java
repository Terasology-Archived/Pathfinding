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

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import junit.framework.Assert;
import org.junit.Test;
import org.terasology.logic.behavior.tree.BehaviorTree;
import org.terasology.logic.behavior.tree.CounterNode;
import org.terasology.logic.behavior.tree.MonitorNode;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.ParallelNode;
import org.terasology.logic.behavior.tree.RepeatNode;
import org.terasology.logic.behavior.tree.SequenceNode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * @author synopia
 */
public class FactoryTest {
    @Test
    public void testLoadSaveNodes() {
        BehaviorTree factory = buildSample();

        StringWriter out = new StringWriter();
        factory.saveNode(new JsonWriter(out));
        String jsonExpected = out.toString();

        StringReader in = new StringReader(jsonExpected);
        factory.loadNode(new JsonReader(in));
        out = new StringWriter();
        Node root = factory.getRoot();
        factory = new BehaviorTree();
        factory.setRoot(root);
        factory.saveNode(new JsonWriter(out));
        String jsonActual = out.toString();
        Assert.assertEquals(jsonActual, jsonExpected);
    }

    @Test
    public void testSaveLoad() throws IOException {
        BehaviorNodeFactory nodeFactory = new BehaviorNodeFactory(new ArrayList<BehaviorNodeComponent>());
        RenderableBehaviorTree factory = new RenderableBehaviorTree(buildSample(), nodeFactory);

        OutputStream os = new ByteArrayOutputStream(10000);
        factory.save(os);
        String jsonExpected = os.toString();

        factory.load(new ByteArrayInputStream(jsonExpected.getBytes()));
        os = new ByteArrayOutputStream(10000);
        factory = new RenderableBehaviorTree(factory.getBehaviorTree(), nodeFactory);
        factory.save(os);
        String jsonActual = os.toString();
        Assert.assertEquals(jsonActual, jsonExpected);
    }

    private BehaviorTree buildSample() {
        SequenceNode sequence = new SequenceNode();
        sequence.children().add(new CounterNode(1));
        sequence.children().add(new RepeatNode(new CounterNode(2)));
        ParallelNode parallel = new ParallelNode(ParallelNode.Policy.RequireAll, ParallelNode.Policy.RequireAll);
        sequence.children().add(parallel);
        parallel.children().add(new MonitorNode());
        parallel.children().add(new CounterNode(3));
        BehaviorTree tree = new BehaviorTree();
        tree.setRoot(sequence);
        return tree;
    }
}
