package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.GearEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/**
 * Draws the works of the stopped clock (GearEntity): a wheel standing on its edge on the ground where the entity is -
 * turning in the wall, or running along the floor - or a weight on its chain over it. The amber of each is drawn a
 * second time at full brightness, as the shining parts of the mobs are.
 */
public class GearRenderer extends EntityRenderer<GearEntity, GearRenderState> {
    private static final Identifier TEXTURE = Dushi.id("textures/entity/gear.png");
    private static final Identifier GLOW = Dushi.id("textures/entity/gear_glow.png");
    private static final int BRIGHT = 0xF000F0;
    private final GearModel model;
    private final GearModel glow;

    public GearRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new GearModel(context.bakeLayer(GearModel.LAYER));
        this.glow = new GearModel(context.bakeLayer(GearModel.LAYER), GearModel.Glow.SHINING);
    }

    @Override
    public GearRenderState createRenderState() {
        return new GearRenderState();
    }

    @Override
    public void extractRenderState(GearEntity entity, GearRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        float age = entity.tickCount + partialTicks;
        state.kind = entity.kind();
        state.age = age;
        state.size = entity.size();
        state.yRot = entity.getYRot();
        state.hang = 0.0F;
        if (state.kind == GearEntity.MOUNT) {
            state.spin = (float) Math.toRadians(age * entity.speed());
        } else if (state.kind == GearEntity.ROLL) {
            // standing, it turns faster and faster; running, it turns as far as it has run
            float warning = entity.warning();
            float wound = Math.min(age, warning);
            float run = Math.max(0.0F, age - warning) * entity.speed();
            state.spin = wound * wound * 0.012F + run / GearEntity.RADIUS;
        } else {
            state.spin = 0.0F;
            state.hang = GearEntity.hang(age, entity.warning(), entity.speed());
        }
    }

    @Override
    public void submit(GearRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0F, state.hang, 0.0F);
        poseStack.rotateDegrees(Axis.YP, 180.0F - state.yRot);
        poseStack.scale(state.size, state.size, state.size);
        // (a model's own space has y pointing down and the ground at 24 sixteenths: the same turn the game gives its mobs)
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        collector.submitModel(this.model, state, poseStack, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        collector.submitModel(this.glow, state, poseStack, GLOW, BRIGHT, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
