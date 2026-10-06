package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.RootEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** Draws a root of the Weeping Mother: a model that is not a living thing's, set on the ground where the entity is. */
public class RootRenderer extends EntityRenderer<RootEntity, RootRenderState> {
    private static final Identifier TEXTURE = Dushi.id("textures/entity/root.png");
    private final RootModel model;

    public RootRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new RootModel(context.bakeLayer(RootModel.LAYER));
    }

    @Override
    public RootRenderState createRenderState() {
        return new RootRenderState();
    }

    @Override
    public void extractRenderState(RootEntity entity, RootRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.age = entity.tickCount + partialTicks;
        state.warning = entity.warning();
        state.stand = entity.stand();
        state.yRot = entity.getYRot();
    }

    @Override
    public void submit(RootRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));
        // (a model's own space has y pointing down and the ground at 24 sixteenths: the same turn the game gives its mobs)
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        collector.submitModel(this.model, state, poseStack, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
