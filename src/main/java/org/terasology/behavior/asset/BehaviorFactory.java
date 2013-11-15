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
package org.terasology.behavior.asset;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.terasology.behavior.tree.Node;
import org.terasology.behavior.ui.RenderableNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

/**
 * @author synopia
 */
public class BehaviorFactory { //implements AssetLoader<RenderableNode> {
    private int currentId;
    private Map<Node, Integer> nodeIds = Maps.newHashMap();
    private Map<Integer, Node> idNodes = Maps.newHashMap();


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

    //    @Override
    public RenderableNode load(InputStream stream) throws IOException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(stream))) {
            reader.setLenient(true);
            reader.beginObject();
            reader.nextName();
            Node node = gsonNode.fromJson(reader, Node.class);
            reader.nextName();
            RenderableNode renderableNode = gsonRenderableNode.fromJson(reader, RenderableNode.class);
            reader.endObject();

            renderableNode.update();
            return renderableNode;
        }
    }

    public void save(RenderableNode node, OutputStream stream) throws IOException {
        try (JsonWriter write = new JsonWriter(new OutputStreamWriter(stream))) {
            write.beginObject().name("model");
            gsonNode.toJson(node.getNode(), Node.class, write);
            write.name("renderer");
            gsonRenderableNode.toJson(node, RenderableNode.class, write);
            write.endObject();
        }
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
