package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.SunShaftEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * A shaft of the sun's light from the sky to the ground: see SunShaftEntity. The light of the Sky Beneath (what the
 * Coming looks with) is drawn another way: as the beam of a beacon - the game's own, laid along the ray whichever way
 * the ray goes - with a soft sheath of light round it, of the same breadth all its length.
 */
public class SunShaftRenderer extends EntityRenderer<SunShaftEntity, SunShaftRenderState> {
    private static final Identifier TEXTURE = Dushi.id("textures/entity/sun_shaft.png");
    private static final Identifier AMBER = Dushi.id("textures/entity/amber_shaft.png");
    private static final Identifier DUSK_SHEATH = Dushi.id("textures/entity/dusk_beam.png");
    /** The colour a beacon's beam is given for the light of the Sky Beneath; and how much of a ray's breadth its hard middle and the game's own glow round that take. */
    private static final int DUSK_BEAM = 0xFFC9B4FF;
    private static final float BEAM_SOLID = 0.13F;
    private static final float BEAM_GLOW = 0.2F;
    /** Full light: a shaft is not darker for standing in a shadow. */
    private static final int BRIGHT = 0xF000F0;
    private final SunShaftModel model;

    public SunShaftRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SunShaftModel(context.bakeLayer(SunShaftModel.LAYER));
    }

    @Override
    public SunShaftRenderState createRenderState() {
        return new SunShaftRenderState();
    }

    /** (a ray leans far out of the upright box the game would look for it in) */
    @Override
    protected boolean affectedByCulling(SunShaftEntity entity) {
        return false;
    }

    @Override
    public void extractRenderState(SunShaftEntity entity, SunShaftRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.width = entity.width() * SunShaftEntity.share(entity.tickCount + partialTicks, entity.rise(), entity.strike(), entity.life());
        state.hue = entity.hue();
        float x = entity.fromX();
        float y = entity.fromY();
        float z = entity.fromZ();
        float flat = (float) Math.sqrt(x * x + z * z);
        if (flat < 0.01F && y < 0.01F) {
            state.length = SunShaftEntity.HEIGHT;
            state.yaw = 0.0F;
            state.pitch = 0.0F;
        } else {
            // (a ray: its top is turned to where it comes from - as the cast anchor's crown is turned the way it flies)
            state.length = (float) Math.sqrt(flat * flat + y * y);
            state.yaw = (float) Math.toDegrees(Math.atan2(x, z));
            state.pitch = (float) Math.toDegrees(Math.atan2(flat, y));
        }
    }

    @Override
    public void submit(SunShaftRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.width > 0.01F) {
            poseStack.pushPose();
            poseStack.rotateDegrees(Axis.YP, state.yaw);
            poseStack.rotateDegrees(Axis.XP, state.pitch);
            boolean beam = state.hue == SunShaftEntity.DUSK;
            if (beam) {
                // (the game draws a beam so many whole blocks high from the middle of a block: it is stretched to the ray's length, and moved back by the half block)
                int blocks = Math.max(1, Mth.ceil(state.length));
                poseStack.pushPose();
                poseStack.scale(1.0F, state.length / blocks, 1.0F);
                poseStack.translate(-0.5F, 0.0F, -0.5F);
                BeaconRenderer.submitBeaconBeam(poseStack, collector, BeaconRenderer.BEAM_LOCATION, 1.0F, state.ageInTicks, 0, blocks, DUSK_BEAM, state.width * BEAM_SOLID, state.width * BEAM_GLOW);
                poseStack.popPose();
            }
            poseStack.scale(state.width, state.length, state.width);
            // (a model's own space has y pointing down and its ground at 24 sixteenths; the shaft's box stands on that ground)
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            poseStack.translate(0.0F, -1.5F, 0.0F);
            // (a wide one is drawn twice over: the shaft that kindles the Warden hides him while it stands on him)
            for (int layer = beam || state.width > 3.0F ? 2 : 1; layer > 0; layer--) {
                collector.submitModel(this.model, state, poseStack, beam ? DUSK_SHEATH : (state.hue == SunShaftEntity.AMBER ? AMBER : TEXTURE), BRIGHT, OverlayTexture.NO_OVERLAY, state.outlineColor);
            }
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }
}
