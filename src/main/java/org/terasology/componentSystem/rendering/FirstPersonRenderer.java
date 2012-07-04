package org.terasology.componentSystem.rendering;

import com.google.common.collect.Maps;
import org.lwjgl.BufferChecks;
import org.lwjgl.opengl.GL11;
import org.terasology.componentSystem.RenderSystem;
import org.terasology.components.CharacterMovementComponent;
import org.terasology.components.InventoryComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.components.world.BlockItemComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.AssetManager;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.logic.world.WorldProvider;
import org.terasology.math.Side;
import org.terasology.math.TeraMath;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.BlockFamily;
import org.terasology.model.inventory.Icon;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.rendering.primitives.MeshFactory;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;
import org.terasology.rendering.shader.ShaderProgram;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.*;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Immortius <immortius@gmail.com>
 */
@RegisterComponentSystem(headedOnly = true)
public class FirstPersonRenderer implements RenderSystem {

    private WorldProvider worldProvider;
    private LocalPlayer localPlayer;
    private WorldRenderer worldRenderer;
    private Mesh handMesh;
    private Texture handTex;

    private Map<String, Mesh> iconMeshes = Maps.newHashMap();

    @Override
    public void initialise() {
        localPlayer = CoreRegistry.get(LocalPlayer.class);
        worldProvider = CoreRegistry.get(WorldProvider.class);
        worldRenderer = CoreRegistry.get(WorldRenderer.class);

        Vector2f texPos = new Vector2f(40.0f * 0.015625f, 32.0f * 0.03125f);
        Vector2f texWidth = new Vector2f(4.0f * 0.015625f, -12.0f * 0.03125f);

        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1, 1, 1, 1), texPos, texWidth, 1.0f, 1.0f, 0.9f, 0.0f, 0.0f, 0.0f);
        handMesh = tessellator.generateMesh();
        handTex = AssetManager.loadTexture("engine:char");
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void renderOpaque() {

    }

    @Override
    public void renderTransparent() {

    }

    @Override
    public void renderFirstPerson() {
        CharacterMovementComponent charMoveComp = localPlayer.getEntity().getComponent(CharacterMovementComponent.class);
        float bobOffset = calcBobbingOffset(charMoveComp.footstepDelta / charMoveComp.distanceBetweenFootsteps, (float) java.lang.Math.PI / 8f, 0.05f, 1f);
        float handMovementAnimationOffset = localPlayer.getEntity().getComponent(LocalPlayerComponent.class).handAnimation;

        int invSlotIndex = localPlayer.getEntity().getComponent(LocalPlayerComponent.class).selectedTool;
        EntityRef heldItem = localPlayer.getEntity().getComponent(InventoryComponent.class).itemSlots.get(invSlotIndex);
        ItemComponent heldItemComp = heldItem.getComponent(ItemComponent.class);
        BlockItemComponent blockItem = heldItem.getComponent(BlockItemComponent.class);
        if (blockItem != null) {
            renderBlock(blockItem.blockFamily, bobOffset, handMovementAnimationOffset);
        } else if (heldItemComp != null && heldItemComp.renderWithIcon) {
            renderIcon(heldItemComp.icon, bobOffset, handMovementAnimationOffset);
        } else {
            renderHand(bobOffset, handMovementAnimationOffset);
        }

    }

    @Override
    public void renderOverlay() {

    }

    private void renderHand(float bobOffset, float handMovementAnimationOffset) {
        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("block");
        shader.enable();
        shader.setFloat("light", worldRenderer.getRenderingLightValue());
        glBindTexture(GL11.GL_TEXTURE_2D, handTex.getId());

        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.setIdentity();

        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.setIdentity();

        modelMatrix.setTranslation(new Vector3f(0.5f, -0.8f + bobOffset - handMovementAnimationOffset * 0.5f + 0.25f, -1.0f - handMovementAnimationOffset * 0.5f));
        modelMatrix.m00 = 0.4f;
        modelMatrix.m11 = 0.6f;
        modelMatrix.m22 = 0.3f;

        Quat4f rotation1 = new Quat4f();
        Quat4f rotation2 = new Quat4f();
        rotation1.set(new AxisAngle4f(new Vector3f(1.0f, 0.0f, 0.0f), TeraMath.DEG_TO_RAD * (-45f - handMovementAnimationOffset * 64.0f)));
        rotation2.set(new AxisAngle4f(new Vector3f(0.0f, 1.0f, 0.0f), TeraMath.DEG_TO_RAD * -35f));
        rotation1.mul(rotation2);

        modelMatrix.setRotation(rotation1);

        shader.setAndCalcRenderingMatrices(modelMatrix, viewMatrix, CoreRegistry.get(WorldRenderer.class).getActiveCamera().getProjectionMatrix());

        handMesh.render(viewMatrix,  modelMatrix);
    }

    private void renderIcon(String iconName, float bobOffset, float handMovementAnimationOffset) {
        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("block");
        shader.enable();

        shader.setInt("textured", 0);
        shader.setFloat("light", worldRenderer.getRenderingLightValue());

        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.setIdentity();

        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.setIdentity();

        modelMatrix.setTranslation(new Vector3f(1.0f, -0.7f + bobOffset - handMovementAnimationOffset * 0.5f, -1.5f - handMovementAnimationOffset * 0.5f));

        // TODO: Holy... Please optimize this.
        Quat4f rotation1 = new Quat4f();
        Quat4f rotation2 = new Quat4f();
        Quat4f rotation3 = new Quat4f();
        Quat4f rotation4 = new Quat4f();

        rotation1.set(new AxisAngle4f(new Vector3f(1.0f, 0.0f, 0.0f), TeraMath.DEG_TO_RAD * (-handMovementAnimationOffset * 64.0f)));
        rotation2.set(new AxisAngle4f(new Vector3f(1.0f, 0.0f, 0.0f), TeraMath.DEG_TO_RAD * -20f));
        rotation3.set(new AxisAngle4f(new Vector3f(0.0f, 1.0f, 0.0f), TeraMath.DEG_TO_RAD * -80f));
        rotation4.set(new AxisAngle4f(new Vector3f(0.0f, 0.0f, 1.0f), TeraMath.DEG_TO_RAD * 45f));

        rotation1.mul(rotation2);
        rotation1.mul(rotation3);
        rotation1.mul(rotation4);

        modelMatrix.setRotation(rotation1);

        shader.setAndCalcRenderingMatrices(modelMatrix, viewMatrix, CoreRegistry.get(WorldRenderer.class).getActiveCamera().getProjectionMatrix());

        Mesh itemMesh = iconMeshes.get(iconName);
        if (itemMesh == null) {
            Icon icon = Icon.get(iconName);
            itemMesh = MeshFactory.getInstance().generateItemMesh(icon.getX(), icon.getY());
            iconMeshes.put(iconName, itemMesh);
        }

        itemMesh.render(viewMatrix, modelMatrix);
    }

    private void renderBlock(BlockFamily blockFamily, float bobOffset, float handMovementAnimationOffset) {
        Block activeBlock = blockFamily.getArchetypeBlock();
        Vector3f playerPos = localPlayer.getPosition();

        // Adjust the brightness of the block according to the current position of the player
        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("block");
        shader.enable();

        // Apply biome and overall color offset
        // TODO: Should get temperature, etc from world provider
        Vector4f color = activeBlock.calcColorOffsetFor(Side.FRONT, worldProvider.getBiomeProvider().getTemperatureAt(TeraMath.floorToInt(playerPos.x), TeraMath.floorToInt(playerPos.z)), worldProvider.getBiomeProvider().getHumidityAt(TeraMath.floorToInt(playerPos.x), TeraMath.floorToInt(playerPos.z)));
        shader.setFloat3("colorOffset", color.x, color.y, color.z);

        glEnable(GL11.GL_BLEND);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glAlphaFunc(GL_GREATER, 0.1f);
        if (activeBlock.isTranslucent()) {
            glEnable(GL11.GL_ALPHA_TEST);
        }

        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.setIdentity();

        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.setIdentity();

        modelMatrix.setTranslation(new Vector3f(1.0f, -0.7f + bobOffset - handMovementAnimationOffset * 0.5f + 0.25f, -1.5f - handMovementAnimationOffset * 0.5f));

        Quat4f rotation1 = new Quat4f();
        Quat4f rotation2 = new Quat4f();
        rotation1.set(new AxisAngle4f(new Vector3f(1.0f, 0.0f, 0.0f), TeraMath.DEG_TO_RAD * (-25f - handMovementAnimationOffset * 64.0f)));
        rotation2.set(new AxisAngle4f(new Vector3f(0.0f, 1.0f, 0.0f), TeraMath.DEG_TO_RAD * 35f));
        rotation1.mul(rotation2);

        modelMatrix.setRotation(rotation1);

        activeBlock.renderWithLightValue(worldRenderer.getRenderingLightValue(), modelMatrix, viewMatrix);

        if (activeBlock.isTranslucent()) {
            glDisable(GL11.GL_ALPHA_TEST);
        }
        glDisable(GL11.GL_BLEND);
    }

    private float calcBobbingOffset(float counter, float phaseOffset, float amplitude, float frequency) {
        return (float) java.lang.Math.sin(2 * Math.PI * counter * frequency + phaseOffset) * amplitude;
    }


}
