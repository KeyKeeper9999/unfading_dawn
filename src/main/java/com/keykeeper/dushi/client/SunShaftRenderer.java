package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.SunShaftEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** A shaft of the sun's light from the sky to the ground: see SunShaftEntity. */
public class SunShaftRenderer extends EntityRenderer<SunShaftEntity, SunShaftRenderState> {
    private static final Identifier TEXTURE = Dushi.id("textures/entity/sun_shaft.png");
    /** Full light: a shaft is not darker for standing in a shadow. */
    private static final int BRIGHT = 0xF000F0;
    private final SunShaftModel model;

    public SunShaftRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SunShaftModel(context.bakeLayer(SunShaftModel.LAYER));
    }

    @Override
    public SunShaftRenderState createRenderState() {
        return new SunShaftRenderState();
    }

    @Override
    public void extractRenderState(SunShaftEntity entity, SunShaftRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.width = entity.width() * SunShaftEntity.share(entity.tickCount + partialTicks, entity.rise(), entity.strike(), entity.life());
    }

    @Override
    public void submit(SunShaftRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.width > 0.01F) {
            poseStack.pushPose();
            poseStack.scale(state.width, SunShaftEntity.HEIGHT, state.width);
            // (a model's own space has y pointing down and its ground at 24 sixteenths; the shaft's box stands on that ground)
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            poseStack.translate(0.0F, -1.5F, 0.0F);
            // (a wide one is drawn twice over: the shaft that kindles the Warden hides him while it stands on him)
            for (int layer = state.width > 3.0F ? 2 : 1; layer > 0; layer--) {
                collector.submitModel(this.model, state, poseStack, TEXTURE, BRIGHT, OverlayTexture.NO_OVERLAY, state.outlineColor);
            }
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }
}
