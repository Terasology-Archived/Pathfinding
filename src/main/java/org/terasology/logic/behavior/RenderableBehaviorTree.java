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
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.terasology.logic.behavior.nui.Port;
import org.terasology.logic.behavior.nui.RenderableNode;
import org.terasology.logic.behavior.tree.BehaviorTree;
import org.terasology.logic.behavior.tree.Node;

import javax.vecmath.Vector2f;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Map;

/**
 * TODO refactor to AssetLoader
 *
 * @author synopia
 */
public class RenderableBehaviorTree {
    private Map<Node, RenderableNode> renderableNodes = Maps.newHashMap();

    private Gson gsonNode;
    private BehaviorTree behaviorTree;
    private BehaviorNodeFactory factory;
    private RenderableNode root;

    public RenderableBehaviorTree(BehaviorTree tree, BehaviorNodeFactory factory) {
        this.factory = factory;

        gsonNode = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeHierarchyAdapter(Node.class, new NodeTypeAdapter())
                .create();

        setTree(tree);
    }

    public RenderableNode getRoot() {
        return root;
    }

    public BehaviorTree getBehaviorTree() {
        return behaviorTree;
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
        Vector2f position = inputPort.getSourceNode().getPosition();
        position.sub(outputPort.getSourceNode().getPosition());
        outputPort.setTarget(inputPort);
        inputPort.getSourceNode().setPosition(position);
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
        Vector2f position = inputPort.getSourceNode().getPosition();
        outputPort.setTarget(null);
        inputPort.getSourceNode().setPosition(position);
    }

    public void setTree(BehaviorTree tree) {
        this.behaviorTree = tree;
        root = tree.getRoot().visit(null, new Node.Visitor<RenderableNode>() {
            @Override
            public RenderableNode visit(RenderableNode parent, Node node) {
                BehaviorNodeComponent nodeComponent = factory.getNodeComponent(node);
                RenderableNode self = new RenderableNode(nodeComponent);
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
    }

    public void addNode(RenderableNode node) {
        renderableNodes.put(node.getNode(), node);
    }

    public Collection<RenderableNode> getRenderableNodes() {
        return renderableNodes.values();
    }

    public RenderableNode getRenderableNode(Node node) {
        return renderableNodes.get(node);
    }

    //    @Override
    public void load(InputStream stream) throws IOException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(stream))) {
            reader.setLenient(true);
            reader.beginObject();
            reader.nextName();
            behaviorTree.loadNode(reader);
            reader.nextName();
            root = loadRenderableNode(reader);
            reader.endObject();
            root.visit(new RenderableNode.Visitor() {
                @Override
                public void visit(RenderableNode node) {
                    renderableNodes.put(node.getNode(), node);
                }
            });
            for (RenderableNode it : renderableNodes.values()) {
                it.update();
            }
        }
    }

    public void save(OutputStream stream) throws IOException {
        try (JsonWriter write = new JsonWriter(new OutputStreamWriter(stream))) {
            write.beginObject().name("model");
            behaviorTree.saveNode(write);
            write.name("renderer");
            saveRenderableNode(root, write);
            write.endObject();
        }
    }

    public RenderableNode loadRenderableNode(JsonReader reader) {
        return gsonNode.fromJson(reader, RenderableNode.class);
    }

    public void saveRenderableNode(RenderableNode node, JsonWriter writer) {
        gsonNode.toJson(node, RenderableNode.class, writer);
    }


    private class NodeTypeAdapter extends TypeAdapter<Node> {
        @Override
        public void write(JsonWriter out, Node value) throws IOException {
            int id = behaviorTree.getId(value);
            out.value(id);
        }

        @Override
        public Node read(JsonReader in) throws IOException {
            int id = in.nextInt();
            return behaviorTree.getNode(id);
        }
    }

}
