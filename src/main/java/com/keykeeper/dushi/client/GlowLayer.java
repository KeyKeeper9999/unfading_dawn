package com.keykeeper.dushi.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;

/** The parts of a boss that shine in the dark; one layer per phase, shown only in that phase. */
class GlowLayer<M extends EntityModel<BossRenderState>> extends EyesLayer<BossRenderState, M> {
    private final RenderType renderType;
    private final int phase;

    GlowLayer(RenderLayerParent<BossRenderState, M> renderer, RenderType renderType, int phase) {
        super(renderer);
        this.renderType = renderType;
        this.phase = phase;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, BossRenderState state, float yRot, float xRot) {
        if ((state.phase >= 2 ? 2 : 1) == this.phase) {
            super.submit(poseStack, collector, lightCoords, state, yRot, xRot);
        }
    }

    @Override
    public RenderType renderType() {
        return this.renderType;
    }
}
