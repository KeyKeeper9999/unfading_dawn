package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.SleeperEntity;
import com.keykeeper.dushi.entity.WakefulEntity;
import com.keykeeper.dushi.entity.WakerEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * The Unsleeping of the Black Firwood: the wakeful, the waker and the sleeper are one mesh, of which each kind shows its
 * own parts. The mesh between the markers is written by tools/gen_sect.py (which also paints the textures and draws the
 * poses below on a sheet); the animation is written by hand.
 * <p>
 * The trunk turns about the hips; the skirt of the shift hangs from the waist and is turned back by whatever the trunk
 * is bent by, so that it hangs. What a kind holds is part of the mesh and lies across the forearm, pointing forward
 * when the arm hangs: a thing is held upright by raising the arm before the body, and thrust level with the arm down.
 */
public class SectModel extends EntityModel<BossRenderState> {
    public enum Kind {
        WAKEFUL, WAKER, SLEEPER
    }

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("sect"), "main");
    private static final float DEG = (float) (Math.PI / 180.0);
    /** How far the body comes down when it kneels: the length of a shin. */
    private static final float KNEEL = 7.0F;

    private final Kind kind;
    private final ModelPart torso;
    private final ModelPart skirt;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightForearm;
    private final ModelPart leftForearm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightShin;
    private final ModelPart leftShin;

    public SectModel(ModelPart root, Kind kind) {
        super(root);
        this.kind = kind;
        this.torso = root.getChild("torso");
        this.skirt = this.torso.getChild("skirt");
        this.head = this.torso.getChild("head");
        this.rightArm = this.torso.getChild("right_arm");
        this.leftArm = this.torso.getChild("left_arm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.rightShin = this.rightLeg.getChild("right_shin");
        this.leftShin = this.leftLeg.getChild("left_shin");
        this.head.getChild("cloth").visible = kind == Kind.WAKEFUL;
        this.head.getChild("crown").visible = kind == Kind.WAKEFUL;
        this.rightForearm.getChild("stick").visible = kind == Kind.WAKEFUL;
        this.head.getChild("cowl").visible = kind == Kind.WAKER;
        this.torso.getChild("board").visible = kind == Kind.WAKER;
        this.rightForearm.getChild("mallet").visible = kind == Kind.WAKER;
        this.torso.getChild("lantern").visible = kind == Kind.WAKER;
        this.torso.getChild("stake").visible = kind == Kind.SLEEPER;
        this.torso.getChild("ropes").visible = kind == Kind.SLEEPER;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(22, 21).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offset(-2.0F, 11.0F, 0.0F));
        PartDefinition right_shin = right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(46, 31).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F)
                .texOffs(0, 27).addBox(-1.5F, 5.0F, -3.5F, 3.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(34, 21).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offset(2.0F, 11.0F, 0.0F));
        PartDefinition left_shin = left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(58, 34).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F)
                .texOffs(0, 30).addBox(-1.5F, 5.0F, -3.5F, 3.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition torso = root.addOrReplaceChild("torso", CubeListBuilder.create()
                .texOffs(94, 16).addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 11.0F, 0.0F, 0.12F, 0.0F, 0.0F));
        PartDefinition skirt = torso.addOrReplaceChild("skirt", CubeListBuilder.create()
                .texOffs(58, 19).addBox(-4.5F, 0.0F, -2.5F, 9.0F, 10.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.12F, 0.0F, 0.0F));
        PartDefinition head = torso.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(94, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, -0.06F, 0.0F, 0.0F));
        PartDefinition right_arm = torso.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(46, 21).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offset(-5.5F, -10.5F, 0.0F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(10, 26).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 5.5F, 0.0F, -0.15F, 0.0F, 0.0F));
        PartDefinition left_arm = torso.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(22, 31).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offset(5.5F, -10.5F, 0.0F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(34, 31).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 5.5F, 0.0F, -0.15F, 0.0F, 0.0F));
        PartDefinition cloth = head.addOrReplaceChild("cloth", CubeListBuilder.create()
                .texOffs(22, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 12.0F, 9.0F),
                PartPose.ZERO);
        PartDefinition crown = head.addOrReplaceChild("crown", CubeListBuilder.create()
                .texOffs(122, 16).addBox(-0.5F, -4.0F, -1.5F, 1.0F, 4.0F, 1.0F)
                .texOffs(122, 21).addBox(-3.5F, -3.0F, 0.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(86, 25).addBox(2.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(90, 25).addBox(-0.5F, -6.0F, -1.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(122, 25).addBox(-3.5F, -5.0F, 0.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(86, 28).addBox(2.5F, -4.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -8.5F, 0.0F));
        PartDefinition stick = right_forearm.addOrReplaceChild("stick", CubeListBuilder.create()
                .texOffs(10, 0).addBox(-0.5F, -19.0F, -0.5F, 1.0F, 25.0F, 1.0F)
                .texOffs(0, 36).addBox(-1.5F, -20.0F, -1.5F, 3.0F, 1.0F, 3.0F)
                .texOffs(86, 19).addBox(-1.0F, -24.0F, -1.0F, 2.0F, 4.0F, 2.0F)
                .texOffs(90, 28).addBox(-0.5F, -26.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 1.5708F, 0.0F, 0.0F));
        PartDefinition cowl = head.addOrReplaceChild("cowl", CubeListBuilder.create()
                .texOffs(58, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 10.0F, 9.0F, new CubeDeformation(0.2F)),
                PartPose.ZERO);
        PartDefinition board = torso.addOrReplaceChild("board", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.0F, -11.0F, -1.5F, 2.0F, 24.0F, 3.0F),
                PartPose.offsetAndRotation(4.5F, -13.2F, 0.0F, 1.4108F, 0.0F, 0.0F));
        PartDefinition mallet = right_forearm.addOrReplaceChild("mallet", CubeListBuilder.create()
                .texOffs(118, 16).addBox(-0.5F, -12.0F, -0.5F, 1.0F, 15.0F, 1.0F)
                .texOffs(70, 34).addBox(-1.5F, -15.0F, -2.5F, 3.0F, 3.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 1.5708F, 0.0F, 0.0F));
        PartDefinition lantern = torso.addOrReplaceChild("lantern", CubeListBuilder.create()
                .texOffs(46, 40).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F)
                .texOffs(122, 28).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-3.0F, -1.0F, -2.5F, -0.12F, 0.0F, 0.0F));
        PartDefinition stake = torso.addOrReplaceChild("stake", CubeListBuilder.create()
                .texOffs(14, 0).addBox(-1.0F, -19.0F, 0.0F, 2.0F, 23.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 2.6F, 0.05F, 0.0F, 0.04F));
        PartDefinition ropes = torso.addOrReplaceChild("ropes", CubeListBuilder.create()
                .texOffs(86, 32).addBox(-4.5F, -10.0F, -2.5F, 9.0F, 2.0F, 7.0F)
                .texOffs(0, 41).addBox(-4.5F, -4.0F, -2.5F, 9.0F, 1.0F, 7.0F),
                PartPose.ZERO);
        // </generated mesh>
        return LayerDefinition.create(mesh, 128, 64);
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

        switch (this.kind) {
            case WAKEFUL -> this.wakeful(state, age, stride * speed, t);
            case WAKER -> this.waker(state, age, stride * speed, t);
            default -> this.sleeper(state, age, stride * speed, t);
        }
        // the skirt hangs, whatever the trunk has been bent by; and goes a little with the legs
        this.skirt.xRot = Mth.clamp(-this.torso.xRot, -0.9F, 0.5F) + Math.abs(stride) * 0.05F * speed;
    }

    // ------------------------------------------------------------------ the wakeful

    private void wakeful(BossRenderState state, float age, float stride, float t) {
        // On its knees at its vigil until something rouses it (phase 1); getting up is its first move.
        float kneel = state.phase < 2 ? 1.0F : state.attackId == WakefulEntity.RISE ? 1.0F - Anim.ramp(t, 3.0F, 22.0F) : 0.0F;
        // standing: the candlestick carried upright before the right shoulder, the left arm swinging
        Anim.pose(this.rightArm, 1.0F, -0.75F, 0.0F, -0.1F);
        Anim.pose(this.rightForearm, 1.0F, -0.6F, 0.0F, 0.0F);
        this.rightArm.xRot += Mth.sin(age * 0.07F) * 0.03F;
        this.leftArm.xRot += 0.1F + stride * 0.5F;
        if (kneel > 0.0F) {
            float down = KNEEL * kneel;
            this.torso.y += down;
            this.rightLeg.y += down;
            this.leftLeg.y += down;
            Anim.pitch(this.rightLeg, kneel, 0.0F);
            Anim.pitch(this.leftLeg, kneel, 0.0F);
            Anim.pitch(this.rightShin, kneel, 1.5F);
            Anim.pitch(this.leftShin, kneel, 1.5F);
            // both hands on the stick, the head bowed to it; it rocks, slowly, as those do who must not sleep
            float rock = Mth.sin(age * 0.045F);
            Anim.pose(this.torso, kneel, 0.1F + rock * 0.05F, 0.0F, 0.0F);
            Anim.pose(this.head, kneel, 0.3F - rock * 0.08F, 0.0F, 0.0F);
            Anim.pose(this.rightArm, kneel, -0.75F, 0.0F, -0.35F);
            Anim.pose(this.rightForearm, kneel, -0.75F, 0.0F, 0.0F);
            Anim.pose(this.leftArm, kneel, -0.7F, 0.0F, 0.5F);
            Anim.pose(this.leftForearm, kneel, -0.9F, 0.0F, 0.0F);
        }
        switch (state.attackId) {
            case WakefulEntity.STRIKE -> {
                // Both hands take the stick up over the head, the flame of it far behind - and bring it down like a flail.
                float raise = Anim.pulse(t, 0.0F, 12.0F, 17.0F, 20.0F);
                float fall = Anim.pulse(t, 17.0F, 20.0F, 28.0F, 38.0F);
                Anim.pose(this.torso, raise, -0.2F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, raise, -2.5F, 0.0F, -0.1F);
                Anim.pose(this.rightForearm, raise, -0.3F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, raise, -2.3F, 0.0F, 0.3F);
                Anim.pose(this.leftForearm, raise, -0.5F, 0.0F, 0.0F);
                Anim.pose(this.torso, fall, 0.5F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, fall, -0.5F, 0.0F, -0.1F);
                Anim.pose(this.rightForearm, fall, 0.0F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, fall, -0.6F, 0.0F, 0.3F);
                Anim.pose(this.leftForearm, fall, -0.3F, 0.0F, 0.0F);
                this.rightLeg.xRot -= 0.4F * fall;
                this.leftLeg.xRot += 0.3F * fall;
            }
            case WakefulEntity.THRUST -> {
                // The stick comes down level at the hip, is drawn back, and goes in burning end first with a long step.
                float draw = Anim.pulse(t, 0.0F, 8.0F, 12.0F, 15.0F);
                float go = Anim.pulse(t, 12.0F, 15.0F, 20.0F, 28.0F);
                Anim.pose(this.torso, draw, 0.1F, 0.4F, 0.0F);
                Anim.pose(this.rightArm, draw, 0.55F, 0.0F, 0.1F);
                Anim.pose(this.rightForearm, draw, 0.15F, 0.0F, 0.0F);
                Anim.pose(this.torso, go, 0.3F, -0.3F, 0.0F);
                Anim.pose(this.rightArm, go, -0.42F, 0.0F, 0.1F);
                Anim.pose(this.rightForearm, go, 0.15F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, go, -0.9F, 0.0F, 0.2F);
                this.rightLeg.xRot += 0.4F * go;
                this.leftLeg.xRot -= 0.5F * go;
            }
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------ the waker

    private void waker(BossRenderState state, float age, float stride, float t) {
        // the left hand holds the board on the shoulder, always; the mallet hangs from the right and swings as he walks
        Anim.pose(this.leftArm, 1.0F, -2.3F, 0.0F, 0.25F);
        Anim.pose(this.leftForearm, 1.0F, -0.7F, 0.0F, 0.0F);
        this.rightArm.xRot += -stride * 0.35F + Mth.sin(age * 0.05F) * 0.03F;
        switch (state.attackId) {
            case WakerEntity.KNOCK -> {
                // The mallet goes up to the end of the board and beats it: three strokes, the head thrown back.
                float held = Anim.pulse(t, 0.0F, 6.0F, 34.0F, 42.0F);
                float away = 0.0F;
                for (int knock : WakerEntity.KNOCKS) {
                    away = Math.max(away, Anim.pulse(t, knock - 7.0F, knock - 3.0F, knock - 1.0F, knock));
                }
                Anim.pose(this.rightArm, held, -2.2F + 0.55F * away, 0.0F, -0.45F);
                Anim.pose(this.rightForearm, held, -0.9F + 0.3F * away, 0.0F, 0.0F);
                Anim.pose(this.head, held, -0.25F, 0.0F, 0.0F);
            }
            case WakerEntity.SWING -> {
                // Drawn far round to his right with the whole body, and brought back across level.
                float draw = Anim.pulse(t, 0.0F, 9.0F, 13.0F, 16.0F);
                float cut = Anim.pulse(t, 13.0F, 16.0F, 22.0F, 34.0F);
                Anim.pose(this.torso, draw, 0.1F, 0.6F, 0.0F);
                Anim.pose(this.rightArm, draw, -1.3F, 0.9F, 0.0F);
                Anim.pose(this.torso, cut, 0.15F, -0.6F, 0.0F);
                Anim.pose(this.rightArm, cut, -1.3F, -0.8F, 0.0F);
                Anim.pose(this.rightForearm, Math.max(draw, cut), -0.2F, 0.0F, 0.0F);
            }
            case WakerEntity.SMASH -> {
                // Up over the head until the mallet hangs down his back, and down with his weight behind it.
                float raise = Anim.pulse(t, 0.0F, 14.0F, 21.0F, 24.0F);
                float fall = Anim.pulse(t, 21.0F, 24.0F, 32.0F, 44.0F);
                Anim.pose(this.torso, raise, -0.25F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, raise, -2.9F, 0.0F, 0.0F);
                Anim.pose(this.rightForearm, raise, -0.5F, 0.0F, 0.0F);
                Anim.pose(this.torso, fall, 0.55F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, fall, -0.6F, 0.0F, 0.0F);
                Anim.pose(this.rightForearm, fall, 0.15F, 0.0F, 0.0F);
                this.rightLeg.xRot -= 0.35F * fall;
                this.leftLeg.xRot += 0.3F * fall;
            }
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------ the sleeper

    private void sleeper(BossRenderState state, float age, float stride, float t) {
        // Asleep on his feet: bent forward under the stake, the head fallen on one shoulder and rolling, the arms held
        // out before him and wavering.
        float roll = Mth.sin(age * 0.05F);
        Anim.pose(this.torso, 1.0F, 0.25F, 0.0F, 0.06F + roll * 0.03F);
        Anim.pose(this.head, 1.0F, 0.35F, 0.2F, 0.5F + roll * 0.08F);
        Anim.pose(this.rightArm, 1.0F, -1.2F + Mth.sin(age * 0.09F) * 0.08F, 0.0F, 0.1F);
        Anim.pose(this.leftArm, 1.0F, -1.0F + Mth.sin(age * 0.08F + 2.0F) * 0.08F, 0.0F, -0.15F);
        Anim.pose(this.rightForearm, 1.0F, -0.1F, 0.0F, 0.0F);
        Anim.pose(this.leftForearm, 1.0F, -0.3F, 0.0F, 0.0F);
        switch (state.attackId) {
            case SleeperEntity.FLURRY -> {
                // Three blows of somebody fighting in a dream: the right arm, the left, the right, the body thrown after each.
                for (int i = 0; i < SleeperEntity.FLURRY_HITS.length; i++) {
                    float hit = SleeperEntity.FLURRY_HITS[i];
                    float up = Anim.pulse(t, hit - 7.0F, hit - 3.0F, hit - 2.0F, hit);
                    float down = Anim.pulse(t, hit - 2.0F, hit, hit + 2.0F, hit + 6.0F);
                    ModelPart arm = i % 2 == 0 ? this.rightArm : this.leftArm;
                    float side = i % 2 == 0 ? 1.0F : -1.0F;
                    Anim.pose(arm, up, -2.5F, 0.0F, 0.3F * side);
                    Anim.pose(arm, down, -0.5F, -0.4F * side, 0.0F);
                    Anim.pose(this.torso, up, 0.2F, 0.4F * side, 0.0F);
                    Anim.pose(this.torso, down, 0.5F, -0.4F * side, 0.0F);
                }
            }
            case SleeperEntity.LUNGE, SleeperEntity.LUNGE_FALL -> {
                // He sinks on his haunches, the arms going back - and throws himself the length of a room.
                float crouch = Anim.pulse(t, 0.0F, 8.0F, SleeperEntity.LUNGE_LEAP - 2.0F, SleeperEntity.LUNGE_LEAP);
                float fly = Anim.pulse(t, SleeperEntity.LUNGE_LEAP - 1.0F, SleeperEntity.LUNGE_LEAP + 1.0F, SleeperEntity.LUNGE_LAND - 1.0F, SleeperEntity.LUNGE_LAND + 4.0F);
                Anim.pose(this.torso, crouch, 0.7F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, crouch, 0.9F, 0.0F, 0.2F);
                Anim.pose(this.leftArm, crouch, 0.9F, 0.0F, -0.2F);
                Anim.pitch(this.rightLeg, crouch, -0.9F);
                Anim.pitch(this.leftLeg, crouch, -0.9F);
                Anim.pitch(this.rightShin, crouch, 1.3F);
                Anim.pitch(this.leftShin, crouch, 1.3F);
                this.torso.y += 3.0F * crouch;
                this.rightLeg.y += 3.0F * crouch;
                this.leftLeg.y += 3.0F * crouch;
                Anim.pose(this.torso, fly, 1.0F, 0.0F, 0.0F);
                Anim.pose(this.head, fly, -0.6F, 0.0F, 0.2F);
                Anim.pose(this.rightArm, fly, -2.6F, 0.0F, 0.2F);
                Anim.pose(this.leftArm, fly, -2.6F, 0.0F, -0.2F);
                Anim.pitch(this.rightLeg, fly, 0.9F);
                Anim.pitch(this.leftLeg, fly, 0.7F);
                if (state.attackId == SleeperEntity.LUNGE_FALL) {
                    // and does not land on his feet: flat on his face under the stake, a long moment, before he gathers himself
                    float flat = Anim.pulse(t, SleeperEntity.LUNGE_LAND - 1.0F, SleeperEntity.LUNGE_LAND + 3.0F, SleeperEntity.LIES_UNTIL, SleeperEntity.LIES_UNTIL + 12.0F);
                    this.torso.y += 9.0F * flat;
                    this.torso.z -= 4.0F * flat;
                    this.rightLeg.y += 9.0F * flat;
                    this.rightLeg.z -= 4.0F * flat;
                    this.leftLeg.y += 9.0F * flat;
                    this.leftLeg.z -= 4.0F * flat;
                    Anim.pose(this.torso, flat, 1.35F, 0.0F, 0.0F);
                    Anim.pose(this.head, flat, -0.5F, 0.6F, 0.0F);
                    Anim.pose(this.rightArm, flat, -2.6F, 0.0F, 0.3F);
                    Anim.pose(this.leftArm, flat, -2.4F, 0.0F, -0.4F);
                    Anim.pose(this.rightLeg, flat, 1.45F, 0.0F, 0.1F);
                    Anim.pose(this.leftLeg, flat, 1.3F, 0.0F, -0.1F);
                    Anim.pitch(this.rightShin, flat, 0.0F);
                    Anim.pitch(this.leftShin, flat, 0.0F);
                }
            }
            default -> {
            }
        }
    }
}
