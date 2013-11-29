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

import org.terasology.jobSystem.FindJobNode;
import org.terasology.jobSystem.FinishJobNode;
import org.terasology.jobSystem.SetTargetJobNode;
import org.terasology.logic.behavior.tree.BehaviorTree;
import org.terasology.logic.behavior.tree.CompositeNode;
import org.terasology.logic.behavior.tree.RepeatNode;
import org.terasology.logic.behavior.tree.SequenceNode;
import org.terasology.minion.move.FindWalkableBlockNode;
import org.terasology.minion.move.MoveToWalkableBlockNode;
import org.terasology.minion.move.PlayAnimationNode;
import org.terasology.minion.path.FindPathToNode;
import org.terasology.minion.path.MoveAlongPathNode;

/**
 * Factory to access behavior trees by URIs. Same uri will return the same instances every call. So BT may be shared
 * by multiple entities.
 *
 * @author synopia
 */
public class BehaviorTreeFactory {

    private BehaviorTree tree;

    public BehaviorTree get(String uri) {
        if (tree != null) {
            return tree;
        }
        SequenceNode job = new SequenceNode();
        job.children().add(new FindJobNode());
        job.children().add(new SetTargetJobNode());
        job.children().add(new FindPathToNode());
        job.children().add(new MoveAlongPathNode());
        job.children().add(new FindWalkableBlockNode());
        job.children().add(new FinishJobNode());

        SequenceNode toWalkableBlock = new SequenceNode();
        toWalkableBlock.children().add(new MoveToWalkableBlockNode());
        toWalkableBlock.children().add(new PlayAnimationNode());

        CompositeNode main = new SequenceNode();
        main.children().add(toWalkableBlock);
        main.children().add(job);
        RepeatNode root = new RepeatNode(main);
        tree = new BehaviorTree();
        tree.setRoot(root);
        return tree;
    }

}
