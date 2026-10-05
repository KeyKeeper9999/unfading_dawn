package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.WitheredEntity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;

public class WitheredRenderer extends HumanoidMobRenderer<WitheredEntity, SoulsRenderState, WitheredModel> {
    private final Identifier texture;
    private final float size;

    public WitheredRenderer(EntityRendererProvider.Context context, WitheredEntity.Kind kind) {
        super(context, new WitheredModel(context.bakeLayer(WitheredModel.LAYER), kind), 0.45F);
        this.texture = Dushi.id("textures/entity/" + kind.id + ".png");
        this.size = kind == WitheredEntity.Kind.BUTCHER ? WitheredEntity.BUTCHER_SIZE : 1.0F;
    }

    @Override
    public SoulsRenderState createRenderState() {
        return new SoulsRenderState();
    }

    @Override
    public void extractRenderState(WitheredEntity entity, SoulsRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attackId = entity.attackId();
        state.attackTime = entity.attackTime + partialTicks;
        state.armed = !entity.getMainHandItem().isEmpty();
    }

    @Override
    protected void scale(SoulsRenderState state, com.mojang.blaze3d.vertex.PoseStack poseStack) {
        super.scale(state, poseStack);
        poseStack.scale(this.size, this.size, this.size);
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(WitheredEntity entity, HumanoidArm arm) {
        return ArmPoses.of(entity, arm);
    }

    @Override
    public Identifier getTextureLocation(SoulsRenderState state) {
        return this.texture;
    }
}
