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
package org.terasology.jobSystem;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;

import java.util.List;
import java.util.Map;

/**
 * @author synopia
 */
@RegisterSystem
public class JobFactory implements ComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(JobFactory.class);

    private Map<SimpleUri, Job> jobRegistry = Maps.newHashMap();
    private List<Job> jobs = Lists.newArrayList();
    private SimpleUri idle = new SimpleUri("Pathfinding:idle");

    public JobFactory() {
        logger.info("Create JobFactory");
        CoreRegistry.put(JobFactory.class, this);
    }

    public void register(Job job) {
        jobRegistry.put(job.getUri(), job);
        jobs.add(job);
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public Job getJob(String uri) {
        return jobRegistry.get(new SimpleUri(uri));
    }

    public Job getJob(EntityRef jobItem) {
        JobComponent jobComponent = jobItem.getComponent(JobComponent.class);
        if (jobComponent != null) {
            return jobRegistry.get(jobComponent.getUri());
        }
        return jobRegistry.get(idle);
    }

    @Override
    public void initialise() {
        logger.info("Initialize JobFactory");
    }

    @Override
    public void shutdown() {
    }
}
