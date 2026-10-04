package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.KeykeeperEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

public class KeykeeperRenderer extends HumanoidMobRenderer<KeykeeperEntity, HumanoidRenderState, KeykeeperModel> {
    private static final Identifier TEXTURE = Dushi.id("textures/entity/keykeeper.png");

    public KeykeeperRenderer(EntityRendererProvider.Context context) {
        super(context, new KeykeeperModel(context.bakeLayer(KeykeeperModel.LAYER)), 0.5F);
    }

    @Override
    public HumanoidRenderState createRenderState() {
        return new HumanoidRenderState();
    }

    @Override
    public Identifier getTextureLocation(HumanoidRenderState state) {
        return TEXTURE;
    }
}
