package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.RustKnightEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;

public class RustKnightRenderer extends HumanoidMobRenderer<RustKnightEntity, SoulsRenderState, RustKnightModel> {
    private final Identifier texture;
    /** A normal humanoid is 1.95 blocks tall; the knight's hitbox is 2.6. */
    private static final float SIZE = 1.3F;

    /** @param name "rust_knight" or "gilded_knight": the same body in other armour */
    public RustKnightRenderer(EntityRendererProvider.Context context, String name) {
        super(context, new RustKnightModel(context.bakeLayer(RustKnightModel.LAYER)), 0.6F);
        this.texture = Dushi.id("textures/entity/" + name + ".png");
    }

    @Override
    public SoulsRenderState createRenderState() {
        return new SoulsRenderState();
    }

    @Override
    public void extractRenderState(RustKnightEntity entity, SoulsRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attackId = entity.attackId();
        state.attackTime = entity.attackTime + partialTicks;
        state.armed = !entity.getMainHandItem().isEmpty();
    }

    @Override
    protected void scale(SoulsRenderState state, PoseStack poseStack) {
        poseStack.scale(SIZE, SIZE, SIZE);
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(RustKnightEntity entity, HumanoidArm arm) {
        return ArmPoses.of(entity, arm);
    }

    @Override
    public Identifier getTextureLocation(SoulsRenderState state) {
        return this.texture;
    }
}
