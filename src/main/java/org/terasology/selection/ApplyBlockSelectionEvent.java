package org.terasology.selection;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.math.Region3i;

/**
 * @author synopia
 */
public class ApplyBlockSelectionEvent implements Event {
    private final Region3i selection;
    private final EntityRef selectedItemEntity;

    public ApplyBlockSelectionEvent(EntityRef selectedItemEntity, Region3i selection) {
        this.selectedItemEntity = selectedItemEntity;
        this.selection = selection;
    }

    public Region3i getSelection() {
        return selection;
    }

    public EntityRef getSelectedItemEntity() {
        return selectedItemEntity;
    }
}
