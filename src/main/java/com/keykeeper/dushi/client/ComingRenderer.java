package com.keykeeper.dushi.client;

import com.keykeeper.dushi.entity.ComingEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

/**
 * The Coming is drawn as the other lords of the place are, but for two things. It is many times the size of the box
 * it is struck by (a rope a dozen blocks long with the disc on the end of it), so it is drawn whether or not that box
 * is in sight. And it turns slowly and strikes the way it faces, so it is drawn the way it faces - not the way the
 * game, which turns a body after its head, would have its body by now.
 */
public class ComingRenderer extends QuarryRenderer<ComingEntity, ComingModel> {
    public ComingRenderer(EntityRendererProvider.Context context) {
        super(context, new ComingModel(context.bakeLayer(ComingModel.LAYER)), "coming", 0.0F, true, true);
    }

    @Override
    protected boolean affectedByCulling(ComingEntity entity) {
        return false;
    }

    @Override
    public void extractRenderState(ComingEntity entity, BossRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.bodyRot = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
    }
}
