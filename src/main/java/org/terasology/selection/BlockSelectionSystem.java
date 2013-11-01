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
package org.terasology.selection;

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.input.cameraTarget.CameraTargetChangedEvent;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector3f;

/**
 * @author synopia
 */
@RegisterSystem(RegisterMode.CLIENT)
public class BlockSelectionSystem implements ComponentSystem, RenderSystem {
    @In
    private Physics physics;
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

        if (result.isHit() && event.isDown()) {
            if (startPos == null) {
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

    @ReceiveEvent(components = {LocationComponent.class})
    public void onCamTargetChanged(CameraTargetChangedEvent event, EntityRef entity) {
        if (startPos == null) {
            return;
        }
        EntityRef target = event.getNewTarget();
        LocationComponent locationComponent = target.getComponent(LocationComponent.class);
        if (locationComponent != null) {
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

        if (startPos != null) {
            selectionRenderer.beginRenderOverlay();
            if (currentSelection == null) {
                selectionRenderer.renderMark(startPos, cameraPosition);
            } else {
                Vector3i size = currentSelection.size();
                Vector3i block = new Vector3i();
                for (int z = 0; z < size.z; z++) {
                    for (int y = 0; y < size.y; y++) {
                        for (int x = 0; x < size.x; x++) {
                            block.set(x, y, z);
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
