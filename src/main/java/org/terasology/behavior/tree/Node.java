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
package org.terasology.behavior.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author synopia
 */
public abstract class Node {
    public static final List<Node> EMPTY = Collections.unmodifiableList(new ArrayList<Node>());

    public abstract Task create();

    public int maxChildren() {
        return 0;
    }

//    public List<Node> children() {
//        return EMPTY;
//    }

    public <T> T visit(T item, Visitor<T> visitor) {
        return visitor.visit(item, this);
    }

    public interface Visitor<T> {
        T visit(T item, Node node);
    }
}
