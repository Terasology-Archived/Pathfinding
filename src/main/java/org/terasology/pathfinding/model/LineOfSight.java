// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.pathfinding.model;

import org.terasology.engine.entitySystem.systems.ComponentSystem;
import org.terasology.navgraph.WalkableBlock;

/**
 * Created by synopia on 01.02.14.
 */
public interface LineOfSight extends ComponentSystem {
    boolean inSight(WalkableBlock one, WalkableBlock two);
}
