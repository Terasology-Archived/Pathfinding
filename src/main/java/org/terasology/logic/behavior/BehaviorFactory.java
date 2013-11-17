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

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.terasology.jobSystem.FindJobNode;
import org.terasology.jobSystem.FinishJobNode;
import org.terasology.jobSystem.SetTargetJobNode;
import org.terasology.logic.behavior.tree.CompositeNode;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.RepeatNode;
import org.terasology.logic.behavior.tree.SequenceNode;
import org.terasology.logic.behavior.ui.Port;
import org.terasology.logic.behavior.ui.RenderableNode;
import org.terasology.minion.move.FindWalkableBlockNode;
import org.terasology.minion.move.MoveToWalkableBlockNode;
import org.terasology.minion.move.PlayAnimationNode;
import org.terasology.minion.path.FindPathToNode;
import org.terasology.minion.path.MoveAlongPathNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Map;

/**
 * @author synopia
 */
public class BehaviorFactory { //implements AssetLoader<RenderableNode> {
    private int currentId;
    private Map<Node, Integer> nodeIds = Maps.newHashMap();
    private Map<Integer, Node> idNodes = Maps.newHashMap();
    private Map<Node, RenderableNode> renderableNodes = Maps.newHashMap();

    private Gson gsonNode;
    private Gson gsonRenderableNode;

    public BehaviorFactory() {
        gsonNode = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapterFactory(new NodeTypeAdapterFactory())
                .create();
        gsonRenderableNode = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeHierarchyAdapter(Node.class, new NodeTypeAdapter())
                .create();
    }

    public Node get(String uri) {
        SequenceNode job = new SequenceNode();
        job.children().add(new FindJobNode());
        job.children().add(new SetTargetJobNode());
        job.children().add(new FindPathToNode());
        job.children().add(new MoveAlongPathNode());
        job.children().add(new FindWalkableBlockNode());
        job.children().add(new FinishJobNode());

        SequenceNode toWalkableBlock = new SequenceNode();
        toWalkableBlock.children().add(new MoveToWalkableBlockNode());
        toWalkableBlock.children().add(new PlayAnimationNode(false));

        CompositeNode main = new SequenceNode();
        main.children().add(toWalkableBlock);
        main.children().add(job);
        return new RepeatNode(main);
    }

    public void connectNodes(Port startPort, Port endPort) {
        if (startPort == endPort || startPort.getSourceNode() == endPort.getSourceNode()) {
            return;
        }
        if (startPort.isInput() == endPort.isInput()) {
            return;
        }
        Port.InputPort inputPort = startPort.isInput() ? (Port.InputPort) startPort : (Port.InputPort) endPort;
        Port.OutputPort outputPort = !startPort.isInput() ? (Port.OutputPort) startPort : (Port.OutputPort) endPort;

        outputPort.setTarget(inputPort);

    }

    public void disconnectNodes(Port startPort, Port endPort) {
        if (startPort == endPort || startPort.getSourceNode() == endPort.getSourceNode()) {
            return;
        }
        if (startPort.isInput() == endPort.isInput()) {
            return;
        }
        Port.InputPort inputPort = startPort.isInput() ? (Port.InputPort) startPort : (Port.InputPort) endPort;
        Port.OutputPort outputPort = !startPort.isInput() ? (Port.OutputPort) startPort : (Port.OutputPort) endPort;

        outputPort.setTarget(null);
    }

    public RenderableNode addNode(Node node) {
        return addNode(node, 0, 0);
    }

    public RenderableNode addNode(Node node, final float midX, final float midY) {
        RenderableNode renderableNode = node.visit(null, new Node.Visitor<RenderableNode>() {
            @Override
            public RenderableNode visit(RenderableNode parent, Node node) {
                RenderableNode self = new RenderableNode();
                self.setNode(node);
                if (parent != null) {
                    int total = parent.getNode().getChildrenCount();
                    int curr = parent.getChildrenCount();
                    self.setPosition(12 * curr - 6 * total, 7);
                    parent.withoutModel().insertChild(-1, self);
                }

                renderableNodes.put(self.getNode(), self);
                return self;
            }
        });

        return renderableNode;
    }

    public void addNode(RenderableNode node) {
        renderableNodes.put(node.getNode(), node);
    }

    public Collection<Node> getNodes() {
        return nodeIds.keySet();
    }

    public Collection<RenderableNode> getRenderableNodes() {
        return renderableNodes.values();
    }

    public RenderableNode getRenderableNode(Node node) {
        return renderableNodes.get(node);
    }

    //    @Override
    public RenderableNode load(InputStream stream) throws IOException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(stream))) {
            reader.setLenient(true);
            reader.beginObject();
            reader.nextName();
            Node node = loadNode(reader);
            reader.nextName();
            RenderableNode renderableNode = loadRenderableNode(reader);
            reader.endObject();
            renderableNode.visit(new RenderableNode.Visitor() {
                @Override
                public void visit(RenderableNode node) {
                    renderableNodes.put(node.getNode(), node);
                }
            });
            for (RenderableNode it : renderableNodes.values()) {
                it.update();
            }
            return renderableNode;
        }
    }

    public void save(RenderableNode node, OutputStream stream) throws IOException {
        try (JsonWriter write = new JsonWriter(new OutputStreamWriter(stream))) {
            write.beginObject().name("model");
            saveNode(node.getNode(), write);
            write.name("renderer");
            saveRenderableNode(node, write);
            write.endObject();
        }
    }

    public RenderableNode loadRenderableNode(JsonReader reader) {
        return gsonRenderableNode.fromJson(reader, RenderableNode.class);
    }

    public void saveRenderableNode(RenderableNode node, JsonWriter writer) {
        gsonRenderableNode.toJson(node, RenderableNode.class, writer);
    }

    public Node loadNode(JsonReader reader) {
        resetIds();
        return gsonNode.fromJson(reader, Node.class);
    }

    public void saveNode(Node node, JsonWriter writer) {
        resetIds();
        gsonNode.toJson(node, Node.class, writer);
    }

    private void resetIds() {
        idNodes.clear();
        nodeIds.clear();
        currentId = 0;
    }

    private class NodeTypeAdapter extends TypeAdapter<Node> {
        @Override
        public void write(JsonWriter out, Node value) throws IOException {
            Integer id = nodeIds.get(value);
            out.value(id);
        }

        @Override
        public Node read(JsonReader in) throws IOException {
            int id = in.nextInt();
            return idNodes.get(id);
        }
    }

    private class NodeTypeAdapterFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(final Gson gson, TypeToken<T> type) {
            final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
            return new TypeAdapter<T>() {
                @Override
                public void write(JsonWriter out, T value) throws IOException {
                    if (value instanceof Node) {
                        idNodes.put(currentId, (Node) value);
                        nodeIds.put((Node) value, currentId);

                        TypeAdapter<T> delegateAdapter = (TypeAdapter<T>) gson.getDelegateAdapter(NodeTypeAdapterFactory.this, TypeToken.get(value.getClass()));
                        out.beginObject()
                                .name("nodeType").value(value.getClass().getCanonicalName())
                                .name("nodeId").value(currentId);
                        currentId++;
                        out.name("node");
                        delegateAdapter.write(out, value);
                        out.endObject();
                    } else {
                        delegate.write(out, value);
                    }
                }

                @Override
                public T read(JsonReader in) throws IOException {
                    if (in.peek() == JsonToken.BEGIN_OBJECT) {
                        in.beginObject();
                        in.nextName();
                        String nodeType = in.nextString();
                        try {
                            Class cls = Class.forName(nodeType);
                            TypeAdapter<T> delegateAdapter = (TypeAdapter<T>) gson.getDelegateAdapter(NodeTypeAdapterFactory.this, TypeToken.get(cls));
                            in.nextName();
                            int id = in.nextInt();
                            in.nextName();
                            T read = delegateAdapter.read(in);
                            idNodes.put(id, (Node) read);
                            nodeIds.put((Node) read, id);
                            in.endObject();
                            return read;
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        return delegate.read(in);
                    }
                }
            };
        }
    }
}
