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
package org.terasology.behavior;

import org.terasology.behavior.tree.Node;
import org.terasology.behavior.tree.RepeatNode;
import org.terasology.behavior.tree.SequenceNode;
import org.terasology.minion.move.MoveToWalkableBlockNode;
import org.terasology.minion.move.PlayAnimationNode;
import org.terasology.minion.move.SetTargetLocalPlayerNode;
import org.terasology.minion.path.FindPathToNode;
import org.terasology.minion.path.MoveAlongPathNode;

/**
 * @author synopia
 */
public class BehaviorFactory {
    public Node get(String uri) {
        SequenceNode sequence = new SequenceNode();
        SequenceNode parallel = new SequenceNode();
        sequence.children().add(new SetTargetLocalPlayerNode());
        sequence.children().add(parallel);
        parallel.children().add(new MoveToWalkableBlockNode());
        parallel.children().add(new PlayAnimationNode(false));
        sequence.children().add(new FindPathToNode());
        sequence.children().add(new MoveAlongPathNode());
        return new RepeatNode(sequence);
    }
}
