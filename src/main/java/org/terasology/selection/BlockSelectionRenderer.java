package org.terasology.selection;

import org.lwjgl.opengl.GL11;
import org.terasology.asset.Assets;
import org.terasology.engine.CoreRegistry;
import org.terasology.math.Vector3i;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glPopMatrix;

/**
 * @author synopia
 */
public class BlockSelectionRenderer {
    private Mesh overlayMesh;
    private Texture effectsTexture;
    private Material defaultTextured;

    public BlockSelectionRenderer() {
        effectsTexture = Assets.getTexture("engine:foliagecolor");
        Vector2f texPos = new Vector2f(0.0f, 0.0f);
        Vector2f texWidth = new Vector2f(1,1);

        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1, 1, 1, 1), texPos, texWidth, 1.001f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f);
        overlayMesh = tessellator.generateMesh();
        defaultTextured = Assets.getMaterial("engine:defaultTextured");
    }

    public void beginRenderOverlay() {
        if (effectsTexture == null) {
            return;
        }

        defaultTextured.activateFeature(ShaderProgramFeature.FEATURE_ALPHA_REJECT);
        defaultTextured.enable();

        glBindTexture(GL11.GL_TEXTURE_2D, effectsTexture.getId());

        glEnable(GL11.GL_BLEND);
        glBlendFunc(GL_DST_COLOR, GL_ZERO);
    }
    public void endRenderOverlay() {
        glDisable(GL11.GL_BLEND);

        defaultTextured.deactivateFeature(ShaderProgramFeature.FEATURE_ALPHA_REJECT);
    }

    public void renderMark(Vector3i blockPos, Vector3f cameraPos) {
        glPushMatrix();
        glTranslated(blockPos.x - cameraPos.x, blockPos.y - cameraPos.y, blockPos.z - cameraPos.z);

        glMatrixMode(GL_MODELVIEW);

        overlayMesh.render();

        glPopMatrix();
    }

}
