package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.RustKnightEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.Identifier;

public class RustKnightRenderer extends HumanoidMobRenderer<RustKnightEntity, SoulsRenderState, RustKnightModel> {
    private static final Identifier TEXTURE = Dushi.id("textures/entity/rust_knight.png");
    /** A normal humanoid is 1.95 blocks tall; the knight's hitbox is 2.6. */
    private static final float SIZE = 1.3F;

    public RustKnightRenderer(EntityRendererProvider.Context context) {
        super(context, new RustKnightModel(context.bakeLayer(RustKnightModel.LAYER)), 0.6F);
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
    public Identifier getTextureLocation(SoulsRenderState state) {
        return TEXTURE;
    }
}
