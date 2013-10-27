package org.terasology.jobSystem;

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Vector3i;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.selection.BlockSelectionRenderer;

import javax.vecmath.Vector3f;

/**
 * @author synopia
 */
@RegisterSystem(RegisterMode.CLIENT)
public class JobRenderSystem implements RenderSystem {
    @In
    private EntityManager entityManager;
    private BlockSelectionRenderer selectionRenderer;

    @Override
    public void initialise() {
        selectionRenderer = new BlockSelectionRenderer();
    }

    @Override
    public void renderOverlay() {
        Vector3f cameraPosition = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();

        selectionRenderer.beginRenderOverlay();
        Vector3i pos = new Vector3i();
        for (EntityRef entityRef : entityManager.getEntitiesWith(JobBlockComponent.class)) {
            LocationComponent location = entityRef.getComponent(LocationComponent.class);
            Vector3f worldPosition = location.getWorldPosition();
            pos.set((int) worldPosition.x, (int) worldPosition.y, (int) worldPosition.z);
            selectionRenderer.renderMark(pos, cameraPosition);
        }
        selectionRenderer.endRenderOverlay();
    }

    @Override
    public void renderAlphaBlend() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void renderOpaque() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void renderFirstPerson() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void renderShadows() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void shutdown() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
