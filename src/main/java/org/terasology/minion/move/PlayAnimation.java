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

import org.terasology.behavior.tree.Behavior;
import org.terasology.behavior.tree.BehaviorTree;
import org.terasology.behavior.tree.Node;
import org.terasology.behavior.tree.Status;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.logic.SkeletalMeshComponent;

/**
 * @author synopia
 */
public class PlayAnimation extends Behavior<EntityRef> {
    public PlayAnimation(PlayAnimationNode node) {
        super(node);
    }

    @Override
    public void onInitialize(EntityRef minion) {
        AnimationComponent animationComponent = minion.getComponent(AnimationComponent.class);
        MeshAnimation animation = animationComponent.walkAnim;
        changeAnimation(minion, animation, getNode().loop);
    }

    @Override
    public Status update(EntityRef minion, float dt) {
        return minion.getComponent(SkeletalMeshComponent.class).animation == null ? Status.SUCCESS : Status.RUNNING;
    }

    @Override
    public PlayAnimationNode getNode() {
        return (PlayAnimationNode) super.getNode();
    }

    private void changeAnimation(EntityRef entity, MeshAnimation animation, boolean loop) {
        SkeletalMeshComponent skeletalMesh = entity.getComponent(SkeletalMeshComponent.class);
        if (skeletalMesh.animation != animation) {
            skeletalMesh.animation = animation;
            skeletalMesh.loop = loop;
            entity.saveComponent(skeletalMesh);
        }
    }

    public static class PlayAnimationNode implements Node<EntityRef> {
        private boolean loop;

        public PlayAnimationNode(boolean loop) {
            this.loop = loop;
        }

        @Override
        public PlayAnimation create(BehaviorTree<EntityRef> tree) {
            return new PlayAnimation(this);
        }

    }
}
