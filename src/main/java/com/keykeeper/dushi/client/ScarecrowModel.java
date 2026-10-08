package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.ScarecrowEntity;
import com.keykeeper.dushi.entity.ScorchedWardenEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * The scarecrows of the Stubble: the one that gets down off its stake, and the Scorched Warden, which is the same mesh
 * drawn three men high with the poles it has for arms shown. The mesh between the markers is written by
 * tools/gen_scarecrow.py (which also paints the textures and draws the poses below on a sheet); the animation is
 * written by hand.
 * <p>
 * Its arms are bound out along a crossbar and do not bend, so everything it does it does by turning: a turn about
 * the stake is the yaw of 'all', and the tip of the bar - one hand down by the ground, the other in the air - is the
 * roll of the trunk. On its stake (phase 1) the whole of it is drawn a little higher, with the stake under it.
 */
public class ScarecrowModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("scarecrow"), "main");
    public static final ModelLayerLocation GIANT = new ModelLayerLocation(Dushi.id("scarecrow"), "giant");
    private static final float DEG = (float) (Math.PI / 180.0);
    /** How much higher it hangs than it stands, in model units. */
    private static final float HANG = 5.0F;

    private final boolean warden;
    private final ModelPart all;
    private final ModelPart stake;
    private final ModelPart torso;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightHand;
    private final ModelPart leftHand;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public ScarecrowModel(ModelPart root, boolean warden) {
        super(root);
        this.warden = warden;
        this.all = root.getChild("all");
        this.stake = this.all.getChild("stake");
        this.torso = this.all.getChild("torso");
        this.head = this.torso.getChild("head");
        this.rightArm = this.torso.getChild("right_arm");
        this.leftArm = this.torso.getChild("left_arm");
        this.rightHand = this.rightArm.getChild("right_hand");
        this.leftHand = this.leftArm.getChild("left_hand");
        this.rightLeg = this.all.getChild("right_leg");
        this.leftLeg = this.all.getChild("left_leg");
        this.rightArm.getChild("right_pole").visible = warden;
        this.leftArm.getChild("left_pole").visible = warden;
    }

    public static LayerDefinition createLayer() {
        return mesh().apply(MeshTransformer.scaling(1.15F));
    }

    public static LayerDefinition createGiantLayer() {
        return mesh().apply(MeshTransformer.scaling(2.4F));
    }

    private static LayerDefinition mesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 14.0F, 0.0F));
        PartDefinition stake = all.addOrReplaceChild("stake", CubeListBuilder.create()
                .texOffs(34, 0).addBox(-1.0F, 0.0F, 1.5F, 2.0F, 15.0F, 2.0F),
                PartPose.ZERO);
        PartDefinition right_leg = all.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(70, 13).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 9.0F, 3.0F)
                .texOffs(0, 27).addBox(-2.0F, 8.0F, -2.0F, 4.0F, 2.0F, 4.0F),
                PartPose.offset(-2.0F, 0.0F, 0.0F));
        PartDefinition left_leg = all.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(82, 13).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 9.0F, 3.0F)
                .texOffs(16, 27).addBox(-2.0F, 8.0F, -2.0F, 4.0F, 2.0F, 4.0F),
                PartPose.offset(2.0F, 0.0F, 0.0F));
        PartDefinition torso = all.addOrReplaceChild("torso", CubeListBuilder.create()
                .texOffs(8, 0).addBox(-4.0F, -12.0F, -2.5F, 8.0F, 12.0F, 5.0F)
                .texOffs(0, 0).addBox(-1.0F, -17.0F, 2.0F, 2.0F, 17.0F, 2.0F)
                .texOffs(32, 31).addBox(-14.0F, -11.0F, 1.5F, 28.0F, 2.0F, 2.0F),
                PartPose.ZERO);
        PartDefinition skirt = torso.addOrReplaceChild("skirt", CubeListBuilder.create()
                .texOffs(94, 13).addBox(-4.5F, 0.0F, -3.0F, 9.0F, 5.0F, 6.0F),
                PartPose.offset(0.0F, -1.0F, 0.0F));
        PartDefinition head = torso.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(42, 0).addBox(-3.5F, -7.0F, -3.5F, 7.0F, 7.0F, 7.0F)
                .texOffs(8, 17).addBox(-4.0F, -9.0F, -4.0F, 8.0F, 2.0F, 8.0F)
                .texOffs(70, 0).addBox(-6.0F, -7.5F, -6.0F, 12.0F, 1.0F, 12.0F),
                PartPose.offset(0.0F, -12.0F, 0.0F));
        PartDefinition right_arm = torso.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(94, 24).addBox(-9.0F, -1.5F, -1.5F, 9.0F, 3.0F, 3.0F),
                PartPose.offset(-4.0F, -10.0F, 0.0F));
        PartDefinition right_hand = right_arm.addOrReplaceChild("right_hand", CubeListBuilder.create()
                .texOffs(92, 30).addBox(-6.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F)
                .texOffs(118, 24).addBox(-9.0F, -2.0F, -0.5F, 4.0F, 1.0F, 1.0F)
                .texOffs(80, 25).addBox(-9.0F, 1.0F, -0.5F, 4.0F, 1.0F, 1.0F)
                .texOffs(62, 21).addBox(-8.0F, -0.5F, 1.0F, 3.0F, 1.0F, 1.0F)
                .texOffs(118, 0).addBox(-2.0F, 1.0F, -1.5F, 2.0F, 4.0F, 3.0F),
                PartPose.offset(-9.0F, 0.0F, 0.0F));
        PartDefinition left_arm = torso.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(56, 25).addBox(0.0F, -1.5F, -1.5F, 9.0F, 3.0F, 3.0F),
                PartPose.offset(4.0F, -10.0F, 0.0F));
        PartDefinition left_hand = left_arm.addOrReplaceChild("left_hand", CubeListBuilder.create()
                .texOffs(108, 30).addBox(0.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F)
                .texOffs(118, 26).addBox(5.0F, -2.0F, -0.5F, 4.0F, 1.0F, 1.0F)
                .texOffs(80, 27).addBox(5.0F, 1.0F, -0.5F, 4.0F, 1.0F, 1.0F)
                .texOffs(0, 23).addBox(5.0F, -0.5F, 1.0F, 3.0F, 1.0F, 1.0F)
                .texOffs(58, 14).addBox(0.0F, 1.0F, -1.5F, 2.0F, 4.0F, 3.0F),
                PartPose.offset(9.0F, 0.0F, 0.0F));
        PartDefinition right_pole = right_arm.addOrReplaceChild("right_pole", CubeListBuilder.create()
                .texOffs(0, 35).addBox(-17.0F, -0.5F, -0.5F, 19.0F, 1.0F, 1.0F)
                .texOffs(42, 14).addBox(-18.0F, -0.5F, -3.5F, 1.0F, 1.0F, 7.0F)
                .texOffs(118, 7).addBox(-18.0F, 0.5F, -3.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(122, 7).addBox(-18.0F, 0.5F, -1.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(124, 11).addBox(-18.0F, 0.5F, 0.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(124, 15).addBox(-18.0F, 0.5F, 2.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offset(-9.0F, 0.0F, 0.0F));
        PartDefinition left_pole = left_arm.addOrReplaceChild("left_pole", CubeListBuilder.create()
                .texOffs(40, 35).addBox(-2.0F, -0.5F, -0.5F, 19.0F, 1.0F, 1.0F)
                .texOffs(40, 22).addBox(17.0F, -0.5F, -3.5F, 1.0F, 1.0F, 7.0F)
                .texOffs(0, 19).addBox(17.0F, 0.5F, -3.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(4, 19).addBox(17.0F, 0.5F, -1.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(124, 19).addBox(17.0F, 0.5F, 0.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(58, 21).addBox(17.0F, 0.5F, 2.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offset(9.0F, 0.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float stride = Mth.sin(state.walkAnimationPos * 0.6662F) * speed;
        float t = state.attackTime;
        int id = state.attackId;

        // how far it is still up on its stake: 1 hung, 0 down
        float hung = state.phase < 2 ? 1.0F : 0.0F;
        if (!this.warden && id == ScarecrowEntity.WAKE) {
            hung = 1.0F - Anim.ramp(t, 2.0F, ScarecrowEntity.WAKE_DROP);
        } else if (this.warden && id == ScorchedWardenEntity.KINDLE && state.phase >= 2) {
            hung = 1.0F - Anim.ramp(t, ScorchedWardenEntity.KINDLE_BURST, ScorchedWardenEntity.KINDLE_BURST + 10.0F);
        }
        this.all.y -= HANG * hung;
        this.stake.visible = hung > 0.5F;
        float down = 1.0F - hung;

        // hung: the head down on its chest, the hands hanging from the bar, the legs moving a little in no wind at all
        Anim.pose(this.head, hung, 0.5F, 0.0F, 0.15F);
        Anim.pose(this.rightHand, hung, 0.0F, 0.0F, -0.5F);
        Anim.pose(this.leftHand, hung, 0.0F, 0.0F, 0.5F);
        this.rightLeg.xRot += (0.06F + Mth.sin(age * 0.03F) * 0.02F) * hung;
        this.leftLeg.xRot += (0.02F + Mth.sin(age * 0.03F + 1.3F) * 0.02F) * hung;
        // down: stooped under the bar, the head up and turning, walking on legs that are only straw
        Anim.pose(this.torso, down, 0.25F, 0.0F, stride * 0.1F);
        this.head.xRot += (-0.2F + Mth.clamp(state.xRot, -40.0F, 40.0F) * DEG * 0.6F) * down;
        this.head.yRot += Mth.clamp(state.yRot, -70.0F, 70.0F) * DEG * down;
        this.rightHand.yRot += 0.4F * down;
        this.leftHand.yRot -= 0.4F * down;
        this.rightLeg.xRot -= stride * 0.9F * down;
        this.leftLeg.xRot += stride * 0.9F * down;
        this.all.zRot += Mth.sin(state.walkAnimationPos * 0.6662F * 0.5F) * 0.05F * speed * down;

        if (this.warden) {
            this.warden(state, age, t, id);
        } else {
            this.scarecrow(t, id);
        }
    }

    /** Round on the stake (or on its feet) with the bar tipped by {@code tip}: from {@code from} to {@code to}, the turn made between the two ticks. */
    private void turn(float t, float start, float impactFrom, float impactTo, float end, float from, float to, float tip) {
        float in = Anim.pulse(t, start, impactFrom - 2.0F, end - 8.0F, end);
        float through = Anim.ramp(t, impactFrom, impactTo);
        this.all.yRot += Anim.lerp(through, from, to) * in;
        this.torso.zRot += tip * in;
        this.head.yRot -= Anim.lerp(through, from, to) * 0.3F * in;
    }

    private void scarecrow(float t, int id) {
        switch (id) {
            case ScarecrowEntity.WAKE -> {
                // the head comes up before anything else does
                float look = Anim.ramp(t, 0.0F, 6.0F);
                this.head.xRot -= 0.5F * look * (1.0F - Anim.ramp(t, ScarecrowEntity.WAKE_DROP, ScarecrowEntity.WAKE_DURATION));
                float land = Anim.pulse(t, ScarecrowEntity.WAKE_DROP - 1.0F, ScarecrowEntity.WAKE_DROP + 2.0F, ScarecrowEntity.WAKE_DROP + 4.0F, ScarecrowEntity.WAKE_DURATION);
                this.torso.xRot += 0.5F * land;
                this.rightLeg.xRot -= 0.6F * land;
                this.leftLeg.xRot -= 0.3F * land;
            }
            case ScarecrowEntity.TURN -> this.turn(t, 0.0F, ScarecrowEntity.TURN_IMPACT - 4.0F, ScarecrowEntity.TURN_IMPACT + 2.0F, ScarecrowEntity.TURN_DURATION, -0.4F, 2.6F, 0.3F);
            case ScarecrowEntity.WHIRL -> {
                // round and round, a turn for every blow
                float last = ScarecrowEntity.WHIRL_HITS[ScarecrowEntity.WHIRL_HITS.length - 1];
                float in = Anim.pulse(t, 0.0F, 6.0F, last + 4.0F, ScarecrowEntity.WHIRL_DURATION);
                float turned = Mth.clamp((t - 4.0F) / 10.0F, 0.0F, (last - 2.0F) / 10.0F) * (float) (Math.PI * 2.0);
                this.all.yRot += turned * (in > 0.0F ? 1.0F : 0.0F);
                this.torso.zRot += 0.25F * in;
                this.torso.xRot += 0.15F * in;
            }
            default -> {
            }
        }
    }

    private void warden(BossRenderState state, float age, float t, int id) {
        switch (id) {
            case ScorchedWardenEntity.RAKE -> this.turn(t, 0.0F, ScorchedWardenEntity.RAKE_IMPACT - 7.0F, ScorchedWardenEntity.RAKE_IMPACT + 4.0F,
                    ScorchedWardenEntity.RAKE_DURATION, -0.3F, 2.8F, 0.6F);
            case ScorchedWardenEntity.THRESH -> {
                // turned side on, the pole that is forward heaved up, and brought down along the ground
                float impact = ScorchedWardenEntity.THRESH_IMPACT;
                float in = Anim.pulse(t, 0.0F, 12.0F, ScorchedWardenEntity.THRESH_DURATION - 12.0F, ScorchedWardenEntity.THRESH_DURATION);
                float up = Anim.pulse(t, 4.0F, 20.0F, impact - 5.0F, impact - 1.0F);
                float fall = Anim.pulse(t, impact - 5.0F, impact, ScorchedWardenEntity.THRESH_DURATION - 14.0F, ScorchedWardenEntity.THRESH_DURATION - 4.0F);
                this.all.yRot += 1.45F * in;
                this.head.yRot -= 1.0F * in;
                this.torso.zRot += -0.7F * up + 0.75F * fall;
            }
            case ScorchedWardenEntity.ROUND -> {
                // the whole of it round on the stake, twice, the bar tipped
                float first = ScorchedWardenEntity.ROUND_HITS[0];
                float last = ScorchedWardenEntity.ROUND_HITS[1];
                float in = Anim.pulse(t, 0.0F, 14.0F, last + 10.0F, ScorchedWardenEntity.ROUND_DURATION);
                float wind = Anim.pulse(t, 0.0F, 16.0F, first - 12.0F, first - 6.0F);
                float turned = Mth.clamp((t - (first - 10.0F)) / (last + 6.0F - (first - 10.0F)), 0.0F, 1.0F);
                turned = turned * turned * (3.0F - 2.0F * turned);
                this.all.yRot += -0.7F * wind + turned * (float) (Math.PI * 4.0);
                this.torso.zRot += 0.55F * in;
            }
            case ScorchedWardenEntity.KINDLE -> {
                // the head back, the whole of it shaking on the cross as it takes
                float burst = ScorchedWardenEntity.KINDLE_BURST;
                float take = Anim.pulse(t, ScorchedWardenEntity.KINDLE_LIGHT, burst - 8.0F, burst + 14.0F, ScorchedWardenEntity.KINDLE_DURATION);
                float shake = Mth.sin(t * 2.1F) * 0.04F * take * (t < burst ? 1.0F : 0.0F);
                this.head.xRot -= 1.1F * take;
                this.torso.xRot += -0.25F * take + shake;
                this.torso.zRot += shake;
                float land = Anim.pulse(t, burst + 6.0F, burst + 12.0F, burst + 18.0F, ScorchedWardenEntity.KINDLE_DURATION);
                this.torso.xRot += 0.6F * land;
                this.rightLeg.xRot -= 0.6F * land;
                this.leftLeg.xRot -= 0.3F * land;
            }
            case ScorchedWardenEntity.FLAIL -> {
                this.turn(t, 0.0F, ScorchedWardenEntity.FLAIL_FIRST - 6.0F, ScorchedWardenEntity.FLAIL_FIRST + 3.0F, ScorchedWardenEntity.FLAIL_SECOND - 5.0F, -0.3F, 2.8F, 0.6F);
                float back = Anim.ramp(t, ScorchedWardenEntity.FLAIL_SECOND - 6.0F, ScorchedWardenEntity.FLAIL_SECOND + 3.0F);
                float in = Anim.pulse(t, ScorchedWardenEntity.FLAIL_SECOND - 7.0F, ScorchedWardenEntity.FLAIL_SECOND - 5.0F, ScorchedWardenEntity.FLAIL_DURATION - 8.0F,
                        ScorchedWardenEntity.FLAIL_DURATION);
                this.all.yRot += Anim.lerp(back, 2.8F, -0.3F) * in;
                this.torso.zRot += 0.6F * in;
            }
            case ScorchedWardenEntity.FALL -> {
                // up on its toes, the arms wide - and down full length; and a long time getting its feet under it again
                float impact = ScorchedWardenEntity.FALL_IMPACT;
                float rear = Anim.pulse(t, 0.0F, 16.0F, impact - 7.0F, impact - 2.0F);
                float lain = Anim.pulse(t, impact - 7.0F, impact, ScorchedWardenEntity.FALL_LAIN, ScorchedWardenEntity.FALL_DURATION - 4.0F);
                this.all.xRot += -0.3F * rear + 1.45F * lain;
                this.all.y += 8.0F * lain;
                this.all.z -= 4.0F * lain;
                this.head.xRot -= 0.5F * rear + 0.9F * lain;
                this.rightLeg.xRot -= 0.25F * lain;
                this.leftLeg.xRot -= 0.25F * lain;
            }
            case ScorchedWardenEntity.EMBERS -> {
                // wound back, and flung open: what is alight in it comes out of it
                float at = ScorchedWardenEntity.EMBERS_THROW;
                float wind = Anim.pulse(t, 0.0F, 12.0F, at - 4.0F, at);
                float fling = Anim.pulse(t, at - 4.0F, at, at + 12.0F, ScorchedWardenEntity.EMBERS_DURATION);
                this.all.yRot += -1.0F * wind + 1.2F * fling;
                this.torso.xRot += -0.2F * wind + 0.5F * fling;
            }
            default -> {
            }
        }
    }
}
