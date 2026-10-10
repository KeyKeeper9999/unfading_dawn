package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.TendrilEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** Draws what the Coming reaches into its hall with: the hole in the floor where the entity is, and the arm out of it. */
public class TendrilRenderer extends EntityRenderer<TendrilEntity, TendrilRenderState> {
    private static final Identifier TEXTURE = Dushi.id("textures/entity/tendril.png");
    private static final Identifier GLOW = Dushi.id("textures/entity/tendril_glow.png");
    private static final int BRIGHT = 0xF000F0;
    private final TendrilModel model;
    private final TendrilModel glow;

    public TendrilRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new TendrilModel(context.bakeLayer(TendrilModel.LAYER));
        this.glow = new TendrilModel(context.bakeLayer(TendrilModel.LAYER), TendrilModel.Glow.SHINING);
    }

    @Override
    public TendrilRenderState createRenderState() {
        return new TendrilRenderState();
    }

    @Override
    public void extractRenderState(TendrilEntity entity, TendrilRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.age = entity.tickCount + partialTicks;
        state.warning = entity.warning();
        state.time = state.age - state.warning;
        state.yRot = entity.fallYaw();
        state.stands = entity.stands();
    }

    @Override
    public void submit(TendrilRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.rotateDegrees(Axis.YP, 180.0F - state.yRot);
        // (a model's own space has y pointing down and the ground at 24 sixteenths: the same turn the game gives its mobs)
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        collector.submitModel(this.model, state, poseStack, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        collector.submitModel(this.glow, state, poseStack, GLOW, BRIGHT, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
