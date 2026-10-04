package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.CarrionHoundEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class CarrionHoundRenderer extends MobRenderer<CarrionHoundEntity, BossRenderState, CarrionHoundModel> {
    private static final Identifier TEXTURE = Dushi.id("textures/entity/carrion_hound.png");

    public CarrionHoundRenderer(EntityRendererProvider.Context context) {
        super(context, new CarrionHoundModel(context.bakeLayer(CarrionHoundModel.LAYER)), 0.45F);
    }

    @Override
    public BossRenderState createRenderState() {
        return new BossRenderState();
    }

    @Override
    public void extractRenderState(CarrionHoundEntity entity, BossRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attackId = entity.attackId();
        state.attackTime = entity.attackTime + partialTicks;
        state.onGround = entity.onGround();
    }

    @Override
    public Identifier getTextureLocation(BossRenderState state) {
        return TEXTURE;
    }
}
