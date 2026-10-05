package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.SoulsMob;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * The renderer of the quarry's creatures, which differ only in their models: a texture named after the mob and, for those
 * that have something shining, a glow layer for the first phase, the second, or both.
 */
public class QuarryRenderer<T extends SoulsMob, M extends EntityModel<BossRenderState>> extends MobRenderer<T, BossRenderState, M> {
    private final Identifier texture;

    public QuarryRenderer(EntityRendererProvider.Context context, M model, String name, float shadow, boolean glowAtRest, boolean glowRoused) {
        super(context, model, shadow);
        this.texture = Dushi.id("textures/entity/" + name + ".png");
        if (glowAtRest || glowRoused) {
            RenderType glow = RenderTypes.eyes(Dushi.id("textures/entity/" + name + "_glow.png"));
            if (glowAtRest) {
                this.addLayer(new GlowLayer<>(this, glow, 1));
            }
            if (glowRoused) {
                this.addLayer(new GlowLayer<>(this, glow, 2));
            }
        }
    }

    @Override
    public BossRenderState createRenderState() {
        return new BossRenderState();
    }

    @Override
    public void extractRenderState(T entity, BossRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attackId = entity.attackId();
        state.attackTime = entity.attackTime + partialTicks;
        state.phase = entity.phase();
        state.onGround = entity.onGround();
    }

    @Override
    public Identifier getTextureLocation(BossRenderState state) {
        return this.texture;
    }
}
