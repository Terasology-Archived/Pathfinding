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
package org.terasology.minion.move;

import org.terasology.behavior.tree.Node;
import org.terasology.behavior.tree.Status;
import org.terasology.behavior.tree.Task;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.logic.SkeletalMeshComponent;

/**
 * @author synopia
 */
public class PlayAnimationNode extends Node {
    private boolean loop;

    public PlayAnimationNode(boolean loop) {
        this.loop = loop;
    }

    @Override
    public PlayAnimationTask create() {
        return new PlayAnimationTask(this);
    }

    public static class PlayAnimationTask extends Task {
        public PlayAnimationTask(PlayAnimationNode node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            AnimationComponent animationComponent = actor().animation();
            MeshAnimation animation = animationComponent.walkAnim;
            changeAnimation(animation, getNode().loop);
        }

        @Override
        public Status update(float dt) {
            return actor().skeletalMesh().animation == null ? Status.SUCCESS : Status.RUNNING;
        }

        @Override
        public PlayAnimationNode getNode() {
            return (PlayAnimationNode) super.getNode();
        }

        private void changeAnimation(MeshAnimation animation, boolean loop) {
            SkeletalMeshComponent skeletalMesh = actor().skeletalMesh();
            if (skeletalMesh.animation != animation) {
                skeletalMesh.animation = animation;
                skeletalMesh.loop = loop;
                actor().save(skeletalMesh);
            }
        }

    }
}
