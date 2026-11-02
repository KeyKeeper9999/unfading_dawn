package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.Folk;
import com.keykeeper.dushi.entity.FolkEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/** One of the folk: the shared mesh, posed and dressed for him ({@link FolkModel}), and his own texture. */
public class FolkRenderer extends HumanoidMobRenderer<FolkEntity, HumanoidRenderState, FolkModel> {
    private final Identifier texture;

    public FolkRenderer(EntityRendererProvider.Context context, Folk who) {
        super(context, new FolkModel(context.bakeLayer(FolkModel.LAYER), who), 0.5F);
        this.texture = Dushi.id("textures/entity/" + who.id + ".png");
    }

    @Override
    public HumanoidRenderState createRenderState() {
        return new HumanoidRenderState();
    }

    @Override
    public Identifier getTextureLocation(HumanoidRenderState state) {
        return this.texture;
    }
}
