package org.terasology.minion;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.Component;
import org.terasology.rendering.assets.animation.MeshAnimation;

import java.util.List;

/**
 * @author synopia
 */
public class AnimationComponent implements Component {
    public MeshAnimation walkAnim; // Different speeds?
    public MeshAnimation idleAnim; // combine with randomanims
    public MeshAnimation attackAnim; // Different speeds?
    // same as byebye, wrath of god (destroy all minions)
    public MeshAnimation dieAnim;

    // Teleport at location, also spawnanimation
    public MeshAnimation fadeInAnim;

    public MeshAnimation fadeOutAnim; // Teleport to location
    public MeshAnimation workAnim;
    public MeshAnimation terraformAnim;

    public List<MeshAnimation> randomAnim = Lists.newArrayList(); // random animations while Idle
}
