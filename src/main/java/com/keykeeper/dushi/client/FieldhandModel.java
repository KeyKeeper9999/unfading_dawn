package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.HollowMillerEntity;
import com.keykeeper.dushi.entity.MowerEntity;
import com.keykeeper.dushi.entity.ReaperEntity;
import com.keykeeper.dushi.entity.SackmanEntity;
import com.keykeeper.dushi.entity.ScorchedEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * The people of the Stubble: the reaper, the mower, the sackman, the scorched and the Hollow Miller are one mesh, of
 * which each kind shows its own parts. The mesh between the markers is written by tools/gen_fieldhand.py (which also
 * paints the textures and draws the poses below on a sheet); the animation is written by hand.
 * <p>
 * The trunk turns about the hips; the hem of the shirt (and the Miller's apron) hangs from the waist and is turned back
 * by whatever the trunk is bent by. What a kind holds is part of the mesh and lies across the forearm, pointing forward
 * when the arm hangs. The Miller's stone, once he has it off its axle, stands on the floor before him and hangs from
 * nothing: it is moved by hand with him.
 */
public class FieldhandModel extends EntityModel<BossRenderState> {
    public enum Kind {
        REAPER, MOWER, SACKMAN, SCORCHED, MILLER
    }

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("fieldhand"), "main");
    /** The mower and the sackman are a fifth larger; the Miller half as large again. */
    public static final ModelLayerLocation BIG = new ModelLayerLocation(Dushi.id("fieldhand"), "big");
    public static final ModelLayerLocation HUGE = new ModelLayerLocation(Dushi.id("fieldhand"), "huge");
    private static final float DEG = (float) (Math.PI / 180.0);

    private final Kind kind;
    private final ModelPart torso;
    private final ModelPart tail;
    private final ModelPart apron;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightForearm;
    private final ModelPart leftForearm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightShin;
    private final ModelPart leftShin;
    private final ModelPart bag;
    private final ModelPart lever;
    private final ModelPart lamp;
    private final ModelPart stone;

    public FieldhandModel(ModelPart root, Kind kind) {
        super(root);
        this.kind = kind;
        this.torso = root.getChild("torso");
        this.tail = this.torso.getChild("tail");
        this.apron = this.torso.getChild("apron");
        this.head = this.torso.getChild("head");
        this.rightArm = this.torso.getChild("right_arm");
        this.leftArm = this.torso.getChild("left_arm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.rightShin = this.rightLeg.getChild("right_shin");
        this.leftShin = this.leftLeg.getChild("left_shin");
        this.bag = this.rightForearm.getChild("bag");
        this.lever = this.rightForearm.getChild("lever");
        this.lamp = this.leftForearm.getChild("lamp");
        this.stone = root.getChild("stone");
        this.head.getChild("kerchief").visible = kind == Kind.REAPER;
        this.torso.getChild("sheaf").visible = kind == Kind.REAPER;
        this.rightForearm.getChild("sickle").visible = kind == Kind.REAPER || kind == Kind.SCORCHED;
        this.head.getChild("straw_hat").visible = kind == Kind.MOWER;
        this.rightForearm.getChild("scythe").visible = kind == Kind.MOWER;
        this.head.getChild("flat_cap").visible = kind == Kind.SACKMAN || kind == Kind.MILLER;
        this.torso.getChild("bigsack").visible = kind == Kind.SACKMAN;
        this.bag.visible = kind == Kind.SACKMAN;
        this.head.getChild("beard").visible = kind == Kind.MILLER;
        this.torso.getChild("bulk").visible = kind == Kind.MILLER;
        this.apron.visible = kind == Kind.MILLER;
        this.lamp.visible = kind == Kind.MILLER;
        this.lever.visible = kind == Kind.MILLER;
        this.stone.visible = false;
    }

    public static LayerDefinition createBigLayer() {
        return createLayer().apply(MeshTransformer.scaling(1.2F));
    }

    public static LayerDefinition createHugeLayer() {
        return createLayer().apply(MeshTransformer.scaling(1.5F));
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(116, 43).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offset(-2.0F, 11.0F, 0.0F));
        PartDefinition right_shin = right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(102, 82).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F)
                .texOffs(0, 42).addBox(-1.5F, 5.0F, -3.0F, 3.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(0, 62).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offset(2.0F, 11.0F, 0.0F));
        PartDefinition left_shin = left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(114, 82).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F)
                .texOffs(106, 43).addBox(-1.5F, 5.0F, -3.0F, 3.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition torso = root.addOrReplaceChild("torso", CubeListBuilder.create()
                .texOffs(0, 46).addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 11.0F, 0.0F, 0.1F, 0.0F, 0.0F));
        PartDefinition tail = torso.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(98, 61).addBox(-4.5F, 0.0F, -2.5F, 9.0F, 6.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.1F, 0.0F, 0.0F));
        PartDefinition head = torso.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(12, 30).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, -0.05F, 0.0F, 0.0F));
        PartDefinition right_arm = torso.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(12, 62).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offset(-5.5F, -10.5F, 0.0F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(98, 72).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 5.5F, 0.0F, -0.15F, 0.0F, 0.0F));
        PartDefinition left_arm = torso.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(110, 72).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offset(5.5F, -10.5F, 0.0F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(68, 73).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 5.5F, 0.0F, -0.15F, 0.0F, 0.0F));
        PartDefinition kerchief = head.addOrReplaceChild("kerchief", CubeListBuilder.create()
                .texOffs(80, 46).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 6.0F, 9.0F),
                PartPose.ZERO);
        PartDefinition sheaf = torso.addOrReplaceChild("sheaf", CubeListBuilder.create()
                .texOffs(106, 28).addBox(-2.5F, -13.0F, 2.0F, 5.0F, 12.0F, 3.0F)
                .texOffs(80, 82).addBox(-3.5F, -18.0F, 1.5F, 7.0F, 5.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.18F));
        PartDefinition sickle = right_forearm.addOrReplaceChild("sickle", CubeListBuilder.create()
                .texOffs(124, 0).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 5.0F, 1.0F)
                .texOffs(124, 6).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(122, 28).addBox(0.5F, -8.0F, -0.5F, 2.0F, 2.0F, 1.0F)
                .texOffs(44, 33).addBox(2.5F, -9.0F, -0.5F, 3.0F, 1.0F, 1.0F)
                .texOffs(124, 10).addBox(5.5F, -8.0F, -0.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(124, 13).addBox(6.5F, -6.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 1.5708F, 0.0F, 0.0F));
        PartDefinition straw_hat = head.addOrReplaceChild("straw_hat", CubeListBuilder.create()
                .texOffs(62, 61).addBox(-4.5F, -10.0F, -4.5F, 9.0F, 3.0F, 9.0F)
                .texOffs(24, 46).addBox(-7.0F, -7.5F, -7.0F, 14.0F, 1.0F, 14.0F),
                PartPose.ZERO);
        PartDefinition scythe = right_forearm.addOrReplaceChild("scythe", CubeListBuilder.create()
                .texOffs(8, 0).addBox(-0.5F, -26.0F, -0.5F, 1.0F, 36.0F, 1.0F)
                .texOffs(122, 31).addBox(-2.5F, -1.0F, -0.5F, 2.0F, 1.0F, 1.0F)
                .texOffs(122, 33).addBox(-2.5F, -12.0F, -0.5F, 2.0F, 1.0F, 1.0F)
                .texOffs(34, 90).addBox(0.5F, -27.0F, -0.5F, 13.0F, 2.0F, 1.0F)
                .texOffs(44, 35).addBox(13.5F, -26.5F, -0.5F, 3.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 1.5708F, 0.0F, 0.0F));
        PartDefinition flat_cap = head.addOrReplaceChild("flat_cap", CubeListBuilder.create()
                .texOffs(24, 61).addBox(-4.5F, -9.5F, -5.5F, 9.0F, 2.0F, 10.0F),
                PartPose.ZERO);
        PartDefinition bigsack = torso.addOrReplaceChild("bigsack", CubeListBuilder.create()
                .texOffs(52, 28).addBox(-5.0F, -15.0F, 2.0F, 10.0F, 12.0F, 6.0F)
                .texOffs(34, 85).addBox(-2.0F, -17.0F, 3.5F, 4.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.12F, 0.0F, 0.0F));
        PartDefinition bag = right_forearm.addOrReplaceChild("bag", CubeListBuilder.create()
                .texOffs(80, 73).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F)
                .texOffs(44, 30).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition beard = head.addOrReplaceChild("beard", CubeListBuilder.create()
                .texOffs(0, 85).addBox(-3.5F, -2.5F, -4.8F, 7.0F, 6.0F, 1.0F),
                PartPose.ZERO);
        PartDefinition bulk = torso.addOrReplaceChild("bulk", CubeListBuilder.create()
                .texOffs(0, 73).addBox(-6.0F, -12.8F, -3.0F, 12.0F, 6.0F, 6.0F)
                .texOffs(36, 73).addBox(-4.8F, -6.8F, -2.8F, 10.0F, 6.0F, 6.0F),
                PartPose.ZERO);
        PartDefinition apron = torso.addOrReplaceChild("apron", CubeListBuilder.create()
                .texOffs(84, 28).addBox(-5.0F, 0.0F, -3.4F, 10.0F, 17.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, -0.1F, 0.0F, 0.0F));
        PartDefinition lamp = left_forearm.addOrReplaceChild("lamp", CubeListBuilder.create()
                .texOffs(116, 53).addBox(-1.5F, 1.0F, -1.5F, 3.0F, 4.0F, 3.0F)
                .texOffs(124, 16).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition lever = right_forearm.addOrReplaceChild("lever", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.0F, -30.0F, -1.0F, 2.0F, 40.0F, 2.0F)
                .texOffs(68, 83).addBox(-1.5F, -34.0F, -1.5F, 3.0F, 4.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 1.5708F, 0.0F, 0.0F));
        PartDefinition stone = root.addOrReplaceChild("stone", CubeListBuilder.create()
                .texOffs(96, 0).addBox(-2.0F, -9.0F, -5.0F, 4.0F, 18.0F, 10.0F)
                .texOffs(52, 0).addBox(-2.0F, -5.0F, -9.0F, 4.0F, 10.0F, 18.0F)
                .texOffs(12, 0).addBox(-2.2F, -7.5F, -7.5F, 5.0F, 15.0F, 15.0F)
                .texOffs(16, 85).addBox(-2.6F, -1.5F, -1.5F, 6.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 15.0F, -12.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float walk = state.walkAnimationPos * 0.6662F;
        float stride = Mth.sin(walk);
        float t = state.attackTime;

        this.head.yRot += Mth.clamp(state.yRot, -60.0F, 60.0F) * DEG;
        this.head.xRot += Mth.clamp(state.xRot, -40.0F, 40.0F) * DEG * 0.6F;
        this.rightLeg.xRot += -stride * 0.8F * speed;
        this.leftLeg.xRot += stride * 0.8F * speed;
        this.rightShin.xRot += Math.max(0.0F, Mth.cos(walk)) * 0.7F * speed;
        this.leftShin.xRot += Math.max(0.0F, -Mth.cos(walk)) * 0.7F * speed;
        this.torso.xRot += Mth.sin(age * 0.06F) * 0.015F;
        // (set every time: one model draws them all)
        this.bag.visible = this.kind == Kind.SACKMAN;
        this.lever.visible = this.kind == Kind.MILLER && state.phase < 2;
        this.stone.visible = this.kind == Kind.MILLER && state.phase >= 2;

        switch (this.kind) {
            case REAPER -> this.reaper(state, age, stride * speed, t);
            case MOWER -> this.mower(state, age, stride * speed, t);
            case SACKMAN -> this.sackman(state, age, stride * speed, t);
            case SCORCHED -> this.scorched(state, age, stride * speed, t);
            default -> this.miller(state, age, stride * speed, t);
        }
        // what hangs from the waist hangs, whatever the trunk has been bent by; and goes a little with the legs
        float hang = Mth.clamp(-this.torso.xRot, -0.9F, 0.5F) + Math.abs(stride) * 0.05F * speed;
        this.tail.xRot = hang;
        this.apron.xRot = hang;
        // and so does a lantern, from the hand that holds it
        this.lamp.xRot = -(this.torso.xRot + this.leftArm.xRot + this.leftForearm.xRot);
        this.lamp.zRot = -(this.torso.zRot + this.leftArm.zRot);
    }

    // ------------------------------------------------------------------ the reaper

    private void reaper(BossRenderState state, float age, float stride, float t) {
        // Bent double, the head up to see from under the kerchief, the sickle held before, the left hand out to gather.
        Anim.pose(this.torso, 1.0F, 0.5F, 0.0F, 0.0F);
        this.head.xRot -= 0.4F;
        Anim.pose(this.rightArm, 1.0F, -0.9F - stride * 0.15F, 0.0F, -0.1F);
        Anim.pose(this.rightForearm, 1.0F, -0.5F, 0.0F, 0.0F);
        Anim.pose(this.leftArm, 1.0F, -0.5F + stride * 0.3F, 0.0F, 0.1F);
        Anim.pose(this.leftForearm, 1.0F, -0.6F, 0.0F, 0.0F);
        switch (state.attackId) {
            case ReaperEntity.SLASH -> this.cut(t, ReaperEntity.SLASH_IMPACT, 1.0F);
            case ReaperEntity.DOUBLE -> {
                this.cut(t, ReaperEntity.DOUBLE_FIRST, 1.0F);
                this.cut(t, ReaperEntity.DOUBLE_SECOND, -1.0F);
            }
            case ReaperEntity.LUNGE -> {
                // Gathered down on the heels, and out at full stretch with the sickle before.
                float crouch = Anim.pulse(t, 0.0F, 9.0F, ReaperEntity.LUNGE_SPRING - 2.0F, ReaperEntity.LUNGE_SPRING + 2.0F);
                float out = Anim.pulse(t, ReaperEntity.LUNGE_SPRING - 2.0F, ReaperEntity.LUNGE_SPRING + 3.0F, ReaperEntity.LUNGE_IMPACT + 5.0F, ReaperEntity.LUNGE_DURATION);
                Anim.pose(this.torso, crouch, 0.9F, 0.2F, 0.0F);
                Anim.pose(this.rightArm, crouch, 0.4F, 0.0F, -0.2F);
                this.rightLeg.xRot -= 0.7F * crouch;
                this.leftLeg.xRot -= 0.7F * crouch;
                this.rightShin.xRot += 1.2F * crouch;
                this.leftShin.xRot += 1.2F * crouch;
                Anim.pose(this.torso, out, 0.8F, -0.2F, 0.0F);
                Anim.pose(this.rightArm, out, -1.9F, 0.0F, 0.0F);
                Anim.pose(this.rightForearm, out, -0.1F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, out, 0.6F, 0.0F, 0.3F);
                this.rightLeg.xRot -= 0.7F * out;
                this.leftLeg.xRot += 0.6F * out;
            }
            default -> {
            }
        }
    }

    /** The sickle across, level, low: from the right if {@code side} is 1, back from the left if it is -1. */
    private void cut(float t, float impact, float side) {
        float draw = Anim.pulse(t, impact - 10.0F, impact - 5.0F, impact - 3.0F, impact);
        float through = Anim.pulse(t, impact - 3.0F, impact, impact + 3.0F, impact + 10.0F);
        Anim.pose(this.torso, draw, 0.45F, 0.6F * side, 0.0F);
        Anim.pose(this.rightArm, draw, -1.3F, 0.9F * side, 0.0F);
        Anim.pose(this.torso, through, 0.55F, -0.7F * side, 0.0F);
        Anim.pose(this.rightArm, through, -1.3F, -0.9F * side, 0.0F);
        Anim.pose(this.rightForearm, Math.max(draw, through), -0.3F, 0.0F, 0.0F);
    }

    // ------------------------------------------------------------------ the mower

    private void mower(BossRenderState state, float age, float stride, float t) {
        // The scythe carried low across him in both hands, the blade out to his left; he sways as he walks, as mowers do.
        Anim.pose(this.torso, 1.0F, 0.15F, 0.25F + stride * 0.12F, 0.0F);
        Anim.pose(this.rightArm, 1.0F, -0.35F, 0.3F, 0.0F);
        Anim.pose(this.rightForearm, 1.0F, -0.2F, 0.0F, 0.0F);
        Anim.pose(this.leftArm, 1.0F, -0.9F, 0.5F, 0.0F);
        Anim.pose(this.leftForearm, 1.0F, -0.5F, 0.0F, 0.0F);
        switch (state.attackId) {
            case MowerEntity.SWEEP -> {
                float impact = MowerEntity.SWEEP_IMPACT;
                float draw = Anim.pulse(t, 0.0F, 14.0F, impact - 5.0F, impact - 1.0F);
                float through = Anim.pulse(t, impact - 5.0F, impact + 1.0F, impact + 8.0F, MowerEntity.SWEEP_DURATION);
                this.sweep(draw, through, 1.0F);
            }
            case MowerEntity.BACK -> {
                // the same swathe back: he is already round to his left, and comes through to his right
                float impact = MowerEntity.BACK_IMPACT;
                float draw = Anim.pulse(t, -6.0F, 0.0F, impact - 5.0F, impact - 1.0F);
                float through = Anim.pulse(t, impact - 5.0F, impact + 1.0F, impact + 8.0F, MowerEntity.BACK_DURATION);
                this.sweep(draw, through, -1.0F);
            }
            case MowerEntity.CLEAVE -> {
                float impact = MowerEntity.CLEAVE_IMPACT;
                float raise = Anim.pulse(t, 0.0F, 18.0F, impact - 5.0F, impact - 1.0F);
                float fall = Anim.pulse(t, impact - 5.0F, impact, impact + 10.0F, MowerEntity.CLEAVE_DURATION);
                this.overhead(raise, fall);
            }
            default -> {
            }
        }
    }

    /**
     * A long thing swept level: the arm that holds it is let down, so that it lies level across the arm, and the whole
     * trunk turns - from far round on the right ({@code draw}) to far round on the left ({@code through}); the other
     * way if {@code side} is -1.
     */
    private void sweep(float draw, float through, float side) {
        Anim.pose(this.torso, draw, 0.2F, 1.0F * side, 0.0F);
        Anim.pose(this.rightArm, draw, -0.1F, 0.6F * side, 0.0F);
        Anim.pose(this.rightForearm, draw, 0.1F, 0.0F, 0.0F);
        Anim.pose(this.leftArm, draw, -0.9F, 0.3F * side, 0.0F);
        Anim.pose(this.leftForearm, draw, -0.8F, 0.0F, 0.0F);
        Anim.pose(this.torso, through, 0.3F, -0.9F * side, 0.0F);
        Anim.pose(this.rightArm, through, -0.1F, -0.5F * side, 0.0F);
        Anim.pose(this.rightForearm, through, 0.1F, 0.0F, 0.0F);
        Anim.pose(this.leftArm, through, -0.7F, -0.4F * side, 0.0F);
        Anim.pose(this.leftForearm, through, -0.9F, 0.0F, 0.0F);
        this.rightLeg.xRot -= 0.3F * through * side;
        this.leftLeg.xRot += 0.3F * through * side;
    }

    /** Both hands take it up over the head, and bring it down before him. */
    private void overhead(float raise, float fall) {
        Anim.pose(this.torso, raise, -0.25F, 0.0F, 0.0F);
        Anim.pose(this.rightArm, raise, -2.8F, 0.0F, 0.0F);
        Anim.pose(this.rightForearm, raise, -0.3F, 0.0F, 0.0F);
        Anim.pose(this.leftArm, raise, -2.6F, 0.0F, 0.3F);
        Anim.pose(this.leftForearm, raise, -0.5F, 0.0F, 0.0F);
        Anim.pose(this.torso, fall, 0.6F, 0.0F, 0.0F);
        Anim.pose(this.rightArm, fall, -0.75F, 0.0F, 0.0F);
        Anim.pose(this.rightForearm, fall, 0.1F, 0.0F, 0.0F);
        Anim.pose(this.leftArm, fall, -0.8F, 0.0F, 0.3F);
        Anim.pose(this.leftForearm, fall, -0.3F, 0.0F, 0.0F);
        this.rightLeg.xRot -= 0.4F * fall;
        this.leftLeg.xRot += 0.3F * fall;
    }

    // ------------------------------------------------------------------ the sackman

    private void sackman(BossRenderState state, float age, float stride, float t) {
        // Bowed under the great sack, the left hand up at its cords; a small one hanging from his right.
        Anim.pose(this.torso, 1.0F, 0.4F, 0.0F, stride * 0.05F);
        this.head.xRot -= 0.3F;
        Anim.pose(this.rightArm, 1.0F, -0.1F - stride * 0.2F, 0.0F, -0.1F);
        Anim.pose(this.leftArm, 1.0F, -0.8F, 0.0F, 0.1F);
        Anim.pose(this.leftForearm, 1.0F, -1.2F, 0.0F, 0.0F);
        switch (state.attackId) {
            case SackmanEntity.THROW -> {
                // Swung back at arm's length, and up and over.
                float release = SackmanEntity.THROW_RELEASE;
                float back = Anim.pulse(t, 0.0F, 10.0F, release - 5.0F, release - 1.0F);
                float over = Anim.pulse(t, release - 5.0F, release, release + 8.0F, SackmanEntity.THROW_DURATION);
                Anim.pose(this.torso, back, 0.2F, 0.6F, 0.0F);
                Anim.pose(this.rightArm, back, 1.0F, 0.0F, -0.2F);
                Anim.pose(this.rightForearm, back, -0.2F, 0.0F, 0.0F);
                Anim.pose(this.torso, over, 0.6F, -0.4F, 0.0F);
                Anim.pose(this.rightArm, over, -2.0F, -0.2F, 0.0F);
                Anim.pose(this.rightForearm, over, -0.1F, 0.0F, 0.0F);
                this.rightLeg.xRot -= 0.5F * over;
                this.leftLeg.xRot += 0.4F * over;
                this.bag.visible = t < release || t >= SackmanEntity.THROW_REARMED;
            }
            case SackmanEntity.CLUB -> {
                // Round by the neck of it at the full length of his arm.
                float impact = SackmanEntity.CLUB_IMPACT;
                float draw = Anim.pulse(t, 0.0F, 12.0F, impact - 5.0F, impact - 1.0F);
                float through = Anim.pulse(t, impact - 5.0F, impact + 1.0F, impact + 8.0F, SackmanEntity.CLUB_DURATION);
                Anim.pose(this.torso, draw, 0.3F, 0.9F, 0.0F);
                Anim.pose(this.rightArm, draw, -1.2F, 0.9F, 0.0F);
                Anim.pose(this.torso, through, 0.4F, -0.8F, 0.0F);
                Anim.pose(this.rightArm, through, -1.3F, -0.9F, 0.0F);
                Anim.pose(this.rightForearm, Math.max(draw, through), -0.2F, 0.0F, 0.0F);
                this.rightLeg.xRot -= 0.3F * through;
                this.leftLeg.xRot += 0.3F * through;
            }
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------ the scorched

    private void scorched(BossRenderState state, float age, float stride, float t) {
        // Upright and stiff, the arms held off him as a burnt man holds them; he does not swing them.
        Anim.pose(this.torso, 1.0F, -0.05F, 0.0F, Mth.sin(age * 0.11F) * 0.03F);
        Anim.pose(this.rightArm, 1.0F, -0.3F, 0.0F, -0.35F);
        Anim.pose(this.rightForearm, 1.0F, -0.5F, 0.0F, 0.0F);
        Anim.pose(this.leftArm, 1.0F, -0.3F, 0.0F, 0.35F);
        Anim.pose(this.leftForearm, 1.0F, -0.5F, 0.0F, 0.0F);
        switch (state.attackId) {
            case ScorchedEntity.GRASP -> {
                // Both hands out before him, and closed.
                float impact = ScorchedEntity.GRASP_IMPACT;
                float open = Anim.pulse(t, 0.0F, 12.0F, impact - 4.0F, impact);
                float take = Anim.pulse(t, impact - 4.0F, impact, impact + 8.0F, ScorchedEntity.GRASP_DURATION);
                Anim.pose(this.rightArm, open, -1.4F, 0.0F, -0.8F);
                Anim.pose(this.leftArm, open, -1.4F, 0.0F, 0.8F);
                Anim.pose(this.torso, take, 0.3F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, take, -1.5F, 0.3F, 0.0F);
                Anim.pose(this.rightForearm, take, -0.2F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, take, -1.5F, -0.3F, 0.0F);
                Anim.pose(this.leftForearm, take, -0.2F, 0.0F, 0.0F);
                this.rightLeg.xRot -= 0.4F * take;
                this.leftLeg.xRot += 0.3F * take;
            }
            case ScorchedEntity.FLARE -> {
                // Drawn in on himself, shaking - and thrown open.
                float burst = ScorchedEntity.FLARE_BURST;
                float gather = Anim.pulse(t, 0.0F, 14.0F, burst - 3.0F, burst);
                float open = Anim.pulse(t, burst - 3.0F, burst, burst + 12.0F, ScorchedEntity.FLARE_DURATION);
                float shake = Mth.sin(t * 2.4F) * 0.05F * gather;
                Anim.pose(this.torso, gather, 0.6F, shake, shake);
                Anim.pose(this.rightArm, gather, -1.0F, 0.9F, 0.0F);
                Anim.pose(this.rightForearm, gather, -1.6F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, gather, -1.0F, -0.9F, 0.0F);
                Anim.pose(this.leftForearm, gather, -1.6F, 0.0F, 0.0F);
                Anim.pose(this.torso, open, -0.4F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, open, -0.4F, 0.0F, -1.6F);
                Anim.pose(this.rightForearm, open, -0.1F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, open, -0.4F, 0.0F, 1.6F);
                Anim.pose(this.leftForearm, open, -0.1F, 0.0F, 0.0F);
                this.head.xRot -= 0.6F * open;
            }
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------ the Hollow Miller

    private void miller(BossRenderState state, float age, float stride, float t) {
        if (state.phase >= 2) {
            this.carrying(state, age, stride, t);
            return;
        }
        // The bar on his right shoulder, the lantern hanging from his left hand.
        Anim.pose(this.torso, 1.0F, 0.06F, 0.0F, stride * 0.05F);
        Anim.pose(this.rightArm, 1.0F, -2.2F, 0.0F, -0.1F);
        Anim.pose(this.rightForearm, 1.0F, -0.9F, 0.0F, 0.0F);
        Anim.pose(this.leftArm, 1.0F, 0.1F + stride * 0.2F, 0.0F, 0.12F);
        switch (state.attackId) {
            case HollowMillerEntity.SWING -> {
                float impact = HollowMillerEntity.SWING_IMPACT;
                float draw = Anim.pulse(t, 0.0F, 15.0F, impact - 5.0F, impact - 1.0F);
                float through = Anim.pulse(t, impact - 5.0F, impact + 1.0F, impact + 8.0F, HollowMillerEntity.SWING_DURATION);
                Anim.pose(this.torso, draw, 0.1F, 1.0F, 0.0F);
                Anim.pose(this.rightArm, draw, -0.1F, 0.6F, 0.0F);
                Anim.pose(this.rightForearm, draw, 0.1F, 0.0F, 0.0F);
                Anim.pose(this.torso, through, 0.2F, -0.9F, 0.0F);
                Anim.pose(this.rightArm, through, -0.1F, -0.5F, 0.0F);
                Anim.pose(this.rightForearm, through, 0.1F, 0.0F, 0.0F);
                this.leftArm.zRot += 0.5F * Math.max(draw, through);
                this.rightLeg.xRot -= 0.3F * through;
                this.leftLeg.xRot += 0.3F * through;
            }
            case HollowMillerEntity.CLEAVE -> {
                float impact = HollowMillerEntity.CLEAVE_IMPACT;
                float raise = Anim.pulse(t, 0.0F, 19.0F, impact - 5.0F, impact - 1.0F);
                float fall = Anim.pulse(t, impact - 5.0F, impact, impact + 10.0F, HollowMillerEntity.CLEAVE_DURATION);
                Anim.pose(this.torso, raise, -0.3F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, raise, -2.9F, 0.0F, 0.0F);
                Anim.pose(this.rightForearm, raise, -0.3F, 0.0F, 0.0F);
                Anim.pose(this.torso, fall, 0.6F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, fall, -0.75F, 0.0F, 0.0F);
                Anim.pose(this.rightForearm, fall, 0.1F, 0.0F, 0.0F);
                this.leftArm.zRot += 0.5F * Math.max(raise, fall);
                this.rightLeg.xRot -= 0.4F * fall;
                this.leftLeg.xRot += 0.3F * fall;
            }
            case HollowMillerEntity.HEAVE -> {
                // The bar out before him like a pole on a capstan, and his whole weight behind it.
                float at = HollowMillerEntity.HEAVE_AT;
                float set = Anim.pulse(t, 0.0F, 12.0F, at - 4.0F, at);
                float push = Anim.pulse(t, at - 4.0F, at + 2.0F, at + 16.0F, HollowMillerEntity.HEAVE_DURATION);
                Anim.pose(this.torso, set, -0.1F, 0.3F, 0.0F);
                Anim.pose(this.rightArm, set, -0.6F, 0.2F, 0.0F);
                Anim.pose(this.rightForearm, set, -1.2F, 0.0F, 0.0F);
                Anim.pose(this.torso, push, 0.7F, -0.2F, 0.0F);
                Anim.pose(this.rightArm, push, -1.5F, 0.0F, 0.0F);
                Anim.pose(this.rightForearm, push, -0.1F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, push, -1.4F, 0.0F, 0.0F);
                this.rightLeg.xRot += 0.6F * push;
                this.leftLeg.xRot -= 0.5F * push;
            }
            case HollowMillerEntity.CHECK -> {
                // The bar driven down into the floor before him and leant on: the brake.
                float at = HollowMillerEntity.CHECK_AT;
                float raise = Anim.pulse(t, 0.0F, 9.0F, at - 4.0F, at);
                float plant = Anim.pulse(t, at - 4.0F, at, at + 16.0F, HollowMillerEntity.CHECK_DURATION);
                Anim.pose(this.rightArm, raise, -2.6F, 0.0F, 0.0F);
                Anim.pose(this.rightForearm, raise, -0.2F, 0.0F, 0.0F);
                Anim.pose(this.torso, plant, 0.45F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, plant, -0.2F, 0.0F, 0.0F);
                Anim.pose(this.rightForearm, plant, -1.2F, 0.0F, 0.0F);
                this.rightLeg.xRot -= 0.4F * plant;
            }
            case HollowMillerEntity.UNSHIP -> {
                // He lets the bar fall, takes the beam in both arms, and straightens under it until it breaks.
                float brk = HollowMillerEntity.UNSHIP_BREAK;
                float stoop = Anim.pulse(t, 0.0F, 14.0F, brk - 16.0F, brk - 4.0F);
                float lift = Anim.pulse(t, brk - 16.0F, brk, brk + 20.0F, HollowMillerEntity.UNSHIP_DURATION);
                Anim.pose(this.torso, stoop, 0.9F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, stoop, -1.0F, 0.3F, 0.0F);
                Anim.pose(this.rightForearm, stoop, -0.4F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, stoop, -1.0F, -0.3F, 0.0F);
                Anim.pose(this.leftForearm, stoop, -0.4F, 0.0F, 0.0F);
                Anim.pose(this.torso, lift, -0.35F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, lift, -2.4F, 0.3F, 0.0F);
                Anim.pose(this.rightForearm, lift, -0.6F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, lift, -2.4F, -0.3F, 0.0F);
                Anim.pose(this.leftForearm, lift, -0.6F, 0.0F, 0.0F);
                this.rightLeg.xRot -= 0.5F * stoop;
                this.leftLeg.xRot -= 0.5F * stoop;
                this.rightShin.xRot += 0.9F * stoop;
                this.leftShin.xRot += 0.9F * stoop;
            }
            default -> {
            }
        }
    }

    /** The Miller with the stone off its axle: on edge on the floor before him, his hands on the top of it. */
    private void carrying(BossRenderState state, float age, float stride, float t) {
        Anim.pose(this.torso, 1.0F, 0.45F, 0.0F, 0.0F);
        Anim.pose(this.rightArm, 1.0F, -1.25F, 0.25F, 0.0F);
        Anim.pose(this.rightForearm, 1.0F, -0.2F, 0.0F, 0.0F);
        Anim.pose(this.leftArm, 1.0F, -1.25F, -0.25F, 0.0F);
        Anim.pose(this.leftForearm, 1.0F, -0.2F, 0.0F, 0.0F);
        // it rolls as he walks it along
        this.stone.xRot = state.walkAnimationPos * 0.9F;
        switch (state.attackId) {
            case HollowMillerEntity.ROLL -> {
                float start = HollowMillerEntity.ROLL_START;
                float back = Anim.pulse(t, 0.0F, 14.0F, start - 6.0F, start);
                float run = Anim.ramp(t, start - 6.0F, start) * (1.0F - Anim.ramp(t, HollowMillerEntity.ROLL_END - 4.0F, HollowMillerEntity.ROLL_END + 6.0F));
                // drawn back against his chest, and then away, with him bent to it and his legs going
                this.stone.z += 5.0F * back;
                Anim.pose(this.torso, back, 0.1F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, back, -0.9F, 0.25F, 0.0F);
                Anim.pose(this.leftArm, back, -0.9F, -0.25F, 0.0F);
                Anim.pose(this.torso, run, 0.75F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, run, -1.6F, 0.2F, 0.0F);
                Anim.pose(this.leftArm, run, -1.6F, -0.2F, 0.0F);
                this.stone.z -= 3.0F * run;
                this.stone.xRot += t * 0.55F * run;
                float legs = Mth.sin(t * 0.9F) * run;
                this.rightLeg.xRot += legs * 0.9F;
                this.leftLeg.xRot -= legs * 0.9F;
                // spent, hanging over it
                float spent = Anim.ramp(t, HollowMillerEntity.ROLL_END + 4.0F, HollowMillerEntity.ROLL_END + 14.0F);
                Anim.pose(this.torso, spent * (1.0F - run), 0.8F, 0.0F, 0.0F);
                this.head.xRot += 0.5F * spent * (1.0F - run);
            }
            case HollowMillerEntity.DROP -> {
                // Heaved up before his face, and let fall flat.
                float impact = HollowMillerEntity.DROP_IMPACT;
                float raise = Anim.pulse(t, 0.0F, 22.0F, impact - 5.0F, impact - 1.0F);
                float fall = Anim.pulse(t, impact - 5.0F, impact, impact + 16.0F, HollowMillerEntity.DROP_DURATION);
                Anim.pose(this.torso, raise, -0.3F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, raise, -2.5F, 0.3F, 0.0F);
                Anim.pose(this.leftArm, raise, -2.5F, -0.3F, 0.0F);
                this.stone.y -= 20.0F * raise;
                this.stone.z += 6.0F * raise;
                Anim.pose(this.torso, fall, 0.7F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, fall, -0.9F, 0.3F, 0.0F);
                Anim.pose(this.leftArm, fall, -0.9F, -0.3F, 0.0F);
                // (flat: on its side on the floor, a pace further out)
                this.stone.zRot = (float) (Math.PI / 2.0) * fall;
                this.stone.y += 6.5F * fall;
                this.stone.z -= 6.0F * fall;
            }
            case HollowMillerEntity.SACKS -> {
                // One hand left on the stone, and a kick at what stands by the wall.
                float burst = HollowMillerEntity.SACKS_BURST;
                float back = Anim.pulse(t, 0.0F, 10.0F, burst - 4.0F, burst);
                float kick = Anim.pulse(t, burst - 4.0F, burst, burst + 8.0F, HollowMillerEntity.SACKS_DURATION);
                Anim.pose(this.torso, Math.max(back, kick), 0.1F, -0.3F, 0.0F);
                this.rightLeg.xRot += 0.7F * back;
                this.rightShin.xRot += 0.9F * back;
                this.rightLeg.xRot -= 1.3F * kick;
                Anim.pose(this.rightArm, Math.max(back, kick), -0.3F, 0.0F, -0.6F);
            }
            case HollowMillerEntity.LANTERN -> {
                // The lantern up at the full length of his arm, and held there.
                float up = Anim.pulse(t, 0.0F, 14.0F, HollowMillerEntity.LANTERN_LIT + 12.0F, HollowMillerEntity.LANTERN_DURATION);
                Anim.pose(this.torso, up, 0.0F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, up, -2.9F, 0.0F, 0.25F);
                Anim.pose(this.leftForearm, up, -0.1F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, up, -0.8F, 0.3F, 0.0F);
                this.head.xRot -= 0.4F * up;
            }
            default -> {
            }
        }
    }
}
