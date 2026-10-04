package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.ChaplainEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class ChaplainRenderer extends MobRenderer<ChaplainEntity, BossRenderState, ChaplainModel> {
    private static final Identifier TEXTURE = Dushi.id("textures/entity/chaplain.png");
    private static final Identifier DARK_TEXTURE = Dushi.id("textures/entity/chaplain_dark.png");
    private static final RenderType GLOW = RenderTypes.eyes(Dushi.id("textures/entity/chaplain_glow.png"));
    private static final RenderType DARK_GLOW = RenderTypes.eyes(Dushi.id("textures/entity/chaplain_dark_glow.png"));

    public ChaplainRenderer(EntityRendererProvider.Context context) {
        super(context, new ChaplainModel(context.bakeLayer(ChaplainModel.LAYER)), 0.6F);
        this.addLayer(new GlowLayer<>(this, GLOW, 1));
        this.addLayer(new GlowLayer<>(this, DARK_GLOW, 2));
    }

    @Override
    public BossRenderState createRenderState() {
        return new BossRenderState();
    }

    @Override
    public void extractRenderState(ChaplainEntity entity, BossRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attackId = entity.attackId();
        state.attackTime = entity.attackTime + partialTicks;
        state.phase = entity.phase();
        state.onGround = entity.onGround();
    }

    @Override
    public Identifier getTextureLocation(BossRenderState state) {
        return state.phase >= 2 ? DARK_TEXTURE : TEXTURE;
    }
}
