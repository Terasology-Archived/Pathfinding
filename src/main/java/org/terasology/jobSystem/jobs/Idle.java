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
package org.terasology.jobSystem.jobs;

import com.google.common.collect.Lists;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.jobSystem.Job;
import org.terasology.jobSystem.JobFactory;
import org.terasology.math.Vector3i;

import java.util.List;

/**
 * @author synopia
 */
@RegisterSystem
public class Idle implements Job, ComponentSystem {

    private SimpleUri uri;

    public Idle() {
        uri = new SimpleUri("Pathfinding:idle");
    }

    @Override
    public void initialise() {
        CoreRegistry.get(JobFactory.class).register(this);
    }

    @Override
    public void shutdown() {

    }

    @Override
    public List<Vector3i> getTargetPositions(EntityRef block) {
        return Lists.newArrayList();
    }

    @Override
    public boolean canMinionWork(EntityRef block, EntityRef minion) {
        return false;
    }

    @Override
    public boolean isAssignable(EntityRef block) {
        return false;
    }

    @Override
    public void letMinionWork(EntityRef block, EntityRef minion) {
    }

    @Override
    public boolean isRequestable(EntityRef block) {
        return false;
    }

    @Override
    public SimpleUri getUri() {
        return uri;
    }
}
