package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.OverseerEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class OverseerRenderer extends MobRenderer<OverseerEntity, BossRenderState, OverseerModel> {
    private static final Identifier TEXTURE = Dushi.id("textures/entity/overseer.png");
    private static final Identifier ENRAGED_TEXTURE = Dushi.id("textures/entity/overseer_enraged.png");
    private static final RenderType GLOW = RenderTypes.eyes(Dushi.id("textures/entity/overseer_glow.png"));
    private static final RenderType ENRAGED_GLOW = RenderTypes.eyes(Dushi.id("textures/entity/overseer_enraged_glow.png"));

    public OverseerRenderer(EntityRendererProvider.Context context) {
        super(context, new OverseerModel(context.bakeLayer(OverseerModel.LAYER)), 1.3F);
        this.addLayer(new GlowLayer<>(this, GLOW, 1));
        this.addLayer(new GlowLayer<>(this, ENRAGED_GLOW, 2));
    }

    @Override
    public BossRenderState createRenderState() {
        return new BossRenderState();
    }

    @Override
    public void extractRenderState(OverseerEntity entity, BossRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attackId = entity.attackId();
        state.attackTime = entity.attackTime + partialTicks;
        state.phase = entity.phase();
        state.onGround = entity.onGround();
    }

    @Override
    public Identifier getTextureLocation(BossRenderState state) {
        return state.phase >= 2 ? ENRAGED_TEXTURE : TEXTURE;
    }
}
