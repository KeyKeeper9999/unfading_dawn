package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.WallEyeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Draws an eye in the rock: flat on the face it is in, looking the way the entity faces, at whoever is looking at it. */
public class WallEyeRenderer extends EntityRenderer<WallEyeEntity, WallEyeRenderState> {
    private static final Identifier TEXTURE = Dushi.id("textures/entity/wall_eye.png");
    private static final Identifier GLOW = Dushi.id("textures/entity/wall_eye_glow.png");
    private static final int BRIGHT = 0xF000F0;
    /** The great eye is drawn at the Coming's own scale: it is as tall as the crack. */
    private static final float GREAT_SCALE = 2.4F;
    private final WallEyeModel model;
    private final WallEyeModel glow;

    public WallEyeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new WallEyeModel(context.bakeLayer(WallEyeModel.LAYER));
        this.glow = new WallEyeModel(context.bakeLayer(WallEyeModel.LAYER), WallEyeModel.Glow.SHINING);
    }

    @Override
    public WallEyeRenderState createRenderState() {
        return new WallEyeRenderState();
    }

    @Override
    protected boolean affectedByCulling(WallEyeEntity entity) {
        return false;
    }

    @Override
    public void extractRenderState(WallEyeEntity entity, WallEyeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        float age = entity.tickCount + partialTicks;
        state.great = entity.great();
        state.size = entity.great() ? GREAT_SCALE : entity.size();
        state.yRot = entity.getYRot();
        state.roll = entity.roll();
        state.lid = entity.lid(age);
        float since = age - entity.lookedAt();
        state.flare = since >= 0.0F && since < 24.0F ? Mth.sin(since / 24.0F * (float) Math.PI) : 0.0F;
        // it looks at whoever is looking at it
        Entity seen = Minecraft.getInstance().player;
        state.lookX = 0.0F;
        state.lookY = 0.0F;
        if (seen != null) {
            Vec3 to = seen.getEyePosition(partialTicks).subtract(entity.position());
            double far = Math.max(1.0, to.length());
            Vec3 out = Vec3.directionFromRotation(0.0F, entity.getYRot());
            // (to the eye's own left is the way it faces turned a quarter round)
            state.lookX = (float) Mth.clamp((to.x * -out.z + to.z * out.x) / far * 1.6, -1.0, 1.0);
            state.lookY = (float) Mth.clamp(to.y / far * 1.6, -1.0, 1.0);
        }
    }

    @Override
    public void submit(WallEyeRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.lid > 0.02F) {
            poseStack.pushPose();
            poseStack.rotateDegrees(Axis.YP, 180.0F - state.yRot);
            poseStack.scale(state.size, state.size, state.size);
            // (a model's own space has y pointing down and the ground at 24 sixteenths: the same turn the game gives its mobs)
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            poseStack.translate(0.0F, -1.501F, 0.0F);
            collector.submitModel(this.model, state, poseStack, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            collector.submitModel(this.glow, state, poseStack, GLOW, BRIGHT, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }
}
