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

import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.rendering.nui.properties.Checkbox;
import org.terasology.rendering.nui.properties.OneOf;

import java.util.Random;

/**
 * @author synopia
 */
public class PlayAnimationNode extends Node {
    @OneOf.List(items= {"idle", "walk", "attack", "die", "fadeIn", "fadeOut", "work", "terraform", "random"})
    private String animation;

    @Checkbox
    private boolean loop;

    public PlayAnimationNode() {
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    @Override
    public PlayAnimationTask createTask() {
        return new PlayAnimationTask(this);
    }

    public static class PlayAnimationTask extends Task {
        private Random random;

        public PlayAnimationTask(PlayAnimationNode node) {
            super(node);
            random = new Random();
        }

        @Override
        public void onInitialize() {
            if (getNode().animation != null) {
                AnimationComponent animationComponent = actor().component(AnimationComponent.class);
                MeshAnimation animation = null;
                switch (getNode().animation) {
                    case "idle":
                        animation = animationComponent.idleAnim;
                        break;
                    case "walk":
                        animation = animationComponent.walkAnim;
                        break;
                    case "attack":
                        animation = animationComponent.attackAnim;
                        break;
                    case "die":
                        animation = animationComponent.dieAnim;
                        break;
                    case "fadeIn":
                        animation = animationComponent.fadeInAnim;
                        break;
                    case "fadeOut":
                        animation = animationComponent.fadeOutAnim;
                        break;
                    case "work":
                        animation = animationComponent.workAnim;
                        break;
                    case "terraform":
                        animation = animationComponent.terraformAnim;
                        break;
                    case "random":
                        animation = animationComponent.randomAnim.get(random.nextInt(animationComponent.randomAnim.size()));
                        break;
                }
                changeAnimation(animation, getNode().loop);
            }
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
            if (skeletalMesh.animation != animation || skeletalMesh.loop != loop) {
                skeletalMesh.animation = animation;
                skeletalMesh.loop = loop;
                actor().save(skeletalMesh);
            }
        }

    }
}
