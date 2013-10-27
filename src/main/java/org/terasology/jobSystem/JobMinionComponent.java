package org.terasology.jobSystem;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;

/**
 * Job's minion component. Indicates, the minion is currently executing a job.
 * @author synopia
 */
public class JobMinionComponent implements Component {
    public transient EntityRef assigned;
    public transient Job job;

    public JobMinionComponent() {
    }

    public JobMinionComponent(Job job) {
        this.job = job;
    }
}
