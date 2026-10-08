package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.AnchorEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/** The Helmsman's anchor, cast: the iron itself, its crown leading the way it flies, turning slowly over its shank. */
public class AnchorRenderer extends EntityRenderer<AnchorEntity, AnchorRenderState> {
    private static final Identifier TEXTURE = Dushi.id("textures/entity/thrown_anchor.png");
    private final AnchorModel model;

    public AnchorRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new AnchorModel(context.bakeLayer(AnchorModel.LAYER));
    }

    @Override
    public AnchorRenderState createRenderState() {
        return new AnchorRenderState();
    }

    @Override
    public void extractRenderState(AnchorEntity entity, AnchorRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        Vec3 way = entity.getDeltaMovement();
        double flat = Math.sqrt(way.x * way.x + way.z * way.z);
        if (way.lengthSqr() > 1.0E-4) {
            state.yaw = (float) Math.toDegrees(Math.atan2(way.x, way.z));
            state.pitch = (float) Math.toDegrees(Math.atan2(flat, way.y));
        }
        state.spin = (entity.tickCount + partialTicks) * 14.0F;
    }

    @Override
    public void submit(AnchorRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.45F, 0.0F);
        // (the crown is the model's top: turned so that its top is the way the anchor flies, then turned about its shank)
        poseStack.rotateDegrees(Axis.YP, state.yaw);
        poseStack.rotateDegrees(Axis.XP, state.pitch);
        poseStack.rotateDegrees(Axis.YP, state.spin);
        poseStack.scale(-AnchorModel.MODEL_SCALE, -AnchorModel.MODEL_SCALE, AnchorModel.MODEL_SCALE);
        collector.submitModel(this.model, state, poseStack, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
