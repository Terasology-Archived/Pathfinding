package org.terasology.selection;

import org.lwjgl.opengl.GL11;
import org.terasology.asset.Assets;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.*;
import org.terasology.input.cameraTarget.CameraTargetChangedEvent;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author synopia
 */
@RegisterSystem(RegisterMode.CLIENT)
public class BlockSelectionSystem implements ComponentSystem, RenderSystem {
    @In
    private Physics physics;
    @In
    private EntityManager entityManager;
    @In
    private WorldProvider worldProvider;
    @In
    private SlotBasedInventoryManager inventoryManager;

    private CollisionGroup[] filter = {StandardCollisionGroup.DEFAULT, StandardCollisionGroup.WORLD};
    private Vector3i startPos;
    private Region3i currentSelection;
    private BlockSelectionRenderer selectionRenderer;

    @ReceiveEvent(components = {CharacterComponent.class, LocationComponent.class, InventoryComponent.class})
    public void onMarkBlockClicked(BlockSelectionButton event, EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        CharacterComponent characterComponent = entity.getComponent(CharacterComponent.class);
        Vector3f direction = characterComponent.getLookDirection();
        Vector3f originPos = location.getWorldPosition();
        originPos.y += characterComponent.eyeOffset;

        HitResult result = physics.rayTrace(originPos, direction, characterComponent.interactionRange, filter);

        if (result.isHit()) {
            if( event.isDown() ) {
                if( startPos==null ) {
                    startPos = result.getBlockPosition();
                    currentSelection = Region3i.createBounded(startPos, startPos);
                } else {
                    CharacterComponent character = entity.getComponent(CharacterComponent.class);
                    EntityRef selectedItemEntity = inventoryManager.getItemInSlot(entity, character.selectedItem);

                    entity.send(new ApplyBlockSelectionEvent(selectedItemEntity, currentSelection));
                    currentSelection = null;
                    startPos = null;
                }
            }
        }
    }

    @ReceiveEvent(components = {LocationComponent.class})
    public void onCamTargetChanged(CameraTargetChangedEvent event, EntityRef entity) {
        if( startPos==null ) {
            return;
        }
        EntityRef target = event.getNewTarget();
        LocationComponent locationComponent = target.getComponent(LocationComponent.class);
        if( locationComponent!=null ) {
            Vector3f worldPosition = locationComponent.getWorldPosition();
            Vector3i currentEndPos = new Vector3i(worldPosition.x, worldPosition.y, worldPosition.z);
            currentSelection = Region3i.createBounded(startPos, currentEndPos);
        }
    }

    @Override
    public void initialise() {
        selectionRenderer = new BlockSelectionRenderer();
    }

    @Override
    public void renderOverlay() {
        selectionRenderer.beginRenderOverlay();
        Vector3f cameraPosition = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();

        if( startPos!=null ) {
            selectionRenderer.beginRenderOverlay();
            if( currentSelection==null ) {
                selectionRenderer.renderMark(startPos, cameraPosition);
            } else {
                Vector3i size = currentSelection.size();
                Vector3i block = new Vector3i();
                for (int z = 0; z < size.z; z++) {
                    for (int y = 0; y < size.y; y++) {
                        for (int x = 0; x < size.x; x++) {
                            block.set(x,y,z);
                            block.add(currentSelection.min());
                            selectionRenderer.renderMark(block, cameraPosition);
                        }
                    }
                }
            }
            selectionRenderer.endRenderOverlay();
        }

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

    }

    @Override
    public void renderOpaque() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void renderAlphaBlend() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
