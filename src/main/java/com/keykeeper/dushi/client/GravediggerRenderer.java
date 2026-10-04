package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.GravediggerEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class GravediggerRenderer extends MobRenderer<GravediggerEntity, BossRenderState, GravediggerModel> {
    private static final Identifier TEXTURE = Dushi.id("textures/entity/gravedigger.png");
    private static final RenderType GLOW = RenderTypes.eyes(Dushi.id("textures/entity/gravedigger_glow.png"));

    public GravediggerRenderer(EntityRendererProvider.Context context) {
        super(context, new GravediggerModel(context.bakeLayer(GravediggerModel.LAYER)), 1.0F);
        this.addLayer(new GlowLayer<>(this, GLOW, 1));
        this.addLayer(new GlowLayer<>(this, GLOW, 2));
    }

    @Override
    public BossRenderState createRenderState() {
        return new BossRenderState();
    }

    @Override
    public void extractRenderState(GravediggerEntity entity, BossRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attackId = entity.attackId();
        state.attackTime = entity.attackTime + partialTicks;
        state.phase = entity.phase();
        state.onGround = entity.onGround();
    }

    @Override
    public Identifier getTextureLocation(BossRenderState state) {
        return TEXTURE;
    }
}
