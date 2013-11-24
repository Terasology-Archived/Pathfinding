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
package org.terasology.logic.behavior.tree;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author synopia
 */
public class BehaviorTree {
    private int currentId;
    private Map<Node, Integer> nodeIds = Maps.newHashMap();
    private Map<Integer, Node> idNodes = Maps.newHashMap();

    private Gson gsonNode;
    private Node root;
    private boolean editable;
    private List<EditableObserver> observers = Lists.newArrayList();

    public BehaviorTree() {
        gsonNode = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapterFactory(new NodeTypeAdapterFactory())
                .create();
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        for (EditableObserver observer : observers) {
            observer.editableChanged(editable);
        }
    }

    public void addEditableListener(EditableObserver observer) {
        observers.add(observer);
    }

    public void loadNode(JsonReader reader) {
        resetIds();
        root = gsonNode.fromJson(reader, Node.class);
    }

    public void saveNode(JsonWriter writer) {
        resetIds();
        gsonNode.toJson(root, Node.class, writer);
    }

    public Node getNode(int id) {
        return idNodes.get(id);
    }

    public int getId(Node node) {
        return nodeIds.get(node);
    }

    private void resetIds() {
        idNodes.clear();
        nodeIds.clear();
        currentId = 0;
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

    public interface EditableObserver {
        void editableChanged(boolean editable);
    }
}
