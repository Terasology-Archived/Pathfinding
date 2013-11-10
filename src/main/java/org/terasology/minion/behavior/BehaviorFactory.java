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
package org.terasology.minion.behavior;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.minion.behavior.tree.Behavior;
import org.terasology.minion.behavior.tree.BehaviorTree;
import org.terasology.minion.behavior.tree.Node;
import org.terasology.minion.behavior.tree.Repeat;
import org.terasology.minion.behavior.tree.Sequence;
import org.terasology.minion.move.MoveToWalkableBlockNode;
import org.terasology.minion.move.PlayAnimation;
import org.terasology.minion.move.SetTargetLocalPlayer;
import org.terasology.minion.path.FindPathTo;
import org.terasology.minion.path.MoveAlongPath;

/**
 * @author synopia
 */
public class BehaviorFactory {
    public Node<EntityRef> get(String uri) {
        Sequence.SequenceNode<EntityRef> sequence = new Sequence.SequenceNode<>();
        Sequence.SequenceNode<EntityRef> parallel = new Sequence.SequenceNode<>();
        sequence.children.add(new SetTargetLocalPlayer.SetTargetLocalPlayerNode());
        sequence.children.add(parallel);
        parallel.children.add(new MoveToWalkableBlockNode());
        parallel.children.add(new PlayAnimation.PlayAnimationNode(false));
        sequence.children.add(new FindPathTo.FindPathToNode());
        sequence.children.add(new MoveAlongPath.MoveAlongPathNode());
        return new Repeat.RepeatNode<>(sequence);
    }

    public Behavior<EntityRef> create(BehaviorTree<EntityRef> tree, String uri) {
        Node<EntityRef> node = get(uri);
        Behavior<EntityRef> behavior = node.create(tree);
        tree.start(behavior);
        return behavior;
    }
}
