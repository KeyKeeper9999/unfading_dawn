package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.SoulsMob;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * The renderer of the quarry's creatures, which differ only in their models: a texture named after the mob and, for those
 * that have something shining, a glow layer for the first phase, the second, or both.
 */
public class QuarryRenderer<T extends SoulsMob, M extends EntityModel<BossRenderState>> extends MobRenderer<T, BossRenderState, M> {
    private final Identifier texture;

    public QuarryRenderer(EntityRendererProvider.Context context, M model, String name, float shadow, boolean glowAtRest, boolean glowRoused) {
        this(context, model, name, shadow, glowAtRest ? name + "_glow" : null, glowRoused ? name + "_glow" : null);
    }

    /** With a picture of its own for what shines in each phase (null: nothing does): the Elder's crown goes out when he sleeps. */
    public QuarryRenderer(EntityRendererProvider.Context context, M model, String name, float shadow, String glowAtRest, String glowRoused) {
        super(context, model, shadow);
        this.texture = Dushi.id("textures/entity/" + name + ".png");
        if (glowAtRest != null) {
            this.addLayer(new GlowLayer<>(this, RenderTypes.eyes(Dushi.id("textures/entity/" + glowAtRest + ".png")), 1));
        }
        if (glowRoused != null) {
            this.addLayer(new GlowLayer<>(this, RenderTypes.eyes(Dushi.id("textures/entity/" + glowRoused + ".png")), 2));
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
        state.extra = entity.renderExtra();
    }

    @Override
    public Identifier getTextureLocation(BossRenderState state) {
        return this.texture;
    }
}
