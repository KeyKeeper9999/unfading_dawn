package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.ColossusEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/**
 * Draws what stands up out of the sea ({@link ColossusEntity}): always, wherever it is (it is farther than the game draws anything,
 * and larger than a view can see the edges of), and lit the way a thing is that is seen by its own light.
 */
public class ColossusRenderer extends EntityRenderer<ColossusEntity, ColossusRenderState> {
    private static final Identifier TEXTURE = Dushi.id("textures/entity/colossus.png");
    private static final Identifier GLOW = Dushi.id("textures/entity/colossus_glow.png");
    private static final int BRIGHT = 0xF000F0;
    /** The body: blue-black as it is painted, in the light of a night at sea. */
    private static final int DIM = 0xA000A0;
    private final ColossusModel model;
    private final ColossusModel glow;

    public ColossusRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ColossusModel(context.bakeLayer(ColossusModel.LAYER));
        this.glow = new ColossusModel(context.bakeLayer(ColossusModel.LAYER), ColossusModel.Glow.SHINING);
    }

    @Override
    public ColossusRenderState createRenderState() {
        return new ColossusRenderState();
    }

    @Override
    public boolean shouldRender(ColossusEntity entity, Frustum frustum, double camX, double camY, double camZ, float partialTicks) {
        return true;
    }

    @Override
    public void extractRenderState(ColossusEntity entity, ColossusRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.age = entity.age(partialTicks);
        state.yRot = entity.getYRot();
    }

    @Override
    public void submit(ColossusRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.age < -2.0F) {
            return;                                                   // not risen: the sea is only the sea
        }
        poseStack.pushPose();
        poseStack.rotateDegrees(Axis.YP, 180.0F - state.yRot);
        // (a model's own space has y pointing down and the ground at 24 sixteenths: the same turn the game gives its mobs)
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        collector.submitModel(this.model, state, poseStack, TEXTURE, DIM, OverlayTexture.NO_OVERLAY, state.outlineColor);
        collector.submitModel(this.glow, state, poseStack, GLOW, BRIGHT, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
