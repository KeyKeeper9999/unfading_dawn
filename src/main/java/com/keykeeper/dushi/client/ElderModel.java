package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.ElderEntity;

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
 * The Elder of the Unsleeping. The mesh between the markers is written by tools/gen_elder.py (which also paints the
 * textures and draws the poses below on a sheet); the animation is written by hand.
 * <p>
 * The skirt of his robe stands on the ground and the trunk turns over it, about the hips. The great candlestick lies
 * across his forearm: he carries it upright by holding the arm out before him, and it comes level when the arm is let
 * down - so that a turn of the trunk with the arm down is a sweep of the whole length of it, and the arm driven
 * forward from behind the hip is a thrust. Awake (phase 1) he is slow and upright. Asleep (phase 2) he hangs forward,
 * the head on his shoulder, and moves in jerks.
 */
public class ElderModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("elder"), "main");
    /** Must match SCALE in tools/gen_elder.py. */
    private static final float MODEL_SCALE = 1.35F;
    private static final float DEG = (float) (Math.PI / 180.0);

    private final ModelPart skirt;
    private final ModelPart torso;
    private final ModelPart head;
    private final ModelPart beard;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightForearm;
    private final ModelPart leftForearm;

    public ElderModel(ModelPart root) {
        super(root);
        this.skirt = root.getChild("skirt");
        this.torso = root.getChild("torso");
        this.head = this.torso.getChild("head");
        this.beard = this.head.getChild("beard");
        this.rightArm = this.torso.getChild("right_arm");
        this.leftArm = this.torso.getChild("left_arm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition skirt = root.addOrReplaceChild("skirt", CubeListBuilder.create()
                .texOffs(4, 0).addBox(-4.5F, 0.0F, -3.0F, 9.0F, 16.0F, 6.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition torso = root.addOrReplaceChild("torso", CubeListBuilder.create()
                .texOffs(66, 0).addBox(-4.0F, -13.0F, -2.0F, 8.0F, 13.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, 0.08F, 0.0F, 0.0F));
        PartDefinition head = torso.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(90, 0).addBox(-3.5F, -8.0F, -3.5F, 7.0F, 8.0F, 7.0F)
                .texOffs(34, 0).addBox(-4.0F, -8.5F, -4.0F, 8.0F, 13.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, -13.0F, 0.0F, -0.04F, 0.0F, 0.0F));
        PartDefinition beard = head.addOrReplaceChild("beard", CubeListBuilder.create()
                .texOffs(90, 15).addBox(-2.5F, 0.0F, -1.0F, 5.0F, 11.0F, 1.0F),
                PartPose.offset(0.0F, -1.5F, -3.6F));
        PartDefinition crown = head.addOrReplaceChild("crown", CubeListBuilder.create()
                .texOffs(90, 27).addBox(-4.5F, -1.0F, -4.5F, 9.0F, 1.0F, 9.0F)
                .texOffs(22, 22).addBox(-4.5F, -5.0F, -4.5F, 1.0F, 4.0F, 1.0F)
                .texOffs(30, 22).addBox(-4.5F, -7.0F, -4.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(118, 11).addBox(3.5F, -4.0F, -4.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(30, 25).addBox(3.5F, -6.0F, -4.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(122, 11).addBox(-4.5F, -4.0F, 3.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(22, 27).addBox(-4.5F, -6.0F, 3.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(118, 0).addBox(3.5F, -6.0F, 3.5F, 1.0F, 5.0F, 1.0F)
                .texOffs(26, 27).addBox(3.5F, -8.0F, 3.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(122, 0).addBox(-0.5F, -6.0F, -4.5F, 1.0F, 5.0F, 1.0F)
                .texOffs(30, 28).addBox(-0.5F, -8.0F, -4.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(26, 22).addBox(-0.5F, -5.0F, 3.5F, 1.0F, 4.0F, 1.0F)
                .texOffs(80, 29).addBox(-0.5F, -7.0F, 3.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -8.0F, 0.0F));
        PartDefinition right_arm = torso.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(102, 15).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 9.0F, 3.0F),
                PartPose.offset(-5.5F, -11.5F, 0.0F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(114, 15).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 9.0F, 3.0F)
                .texOffs(46, 21).addBox(-2.0F, 3.0F, -1.5F, 4.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 7.5F, 0.0F, -0.12F, 0.0F, 0.0F));
        PartDefinition left_arm = torso.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(66, 17).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 9.0F, 3.0F),
                PartPose.offset(5.5F, -11.5F, 0.0F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(78, 17).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 9.0F, 3.0F)
                .texOffs(4, 22).addBox(-2.0F, 3.0F, -1.5F, 4.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 7.5F, 0.0F, -0.12F, 0.0F, 0.0F));
        PartDefinition staff = right_forearm.addOrReplaceChild("staff", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.5F, -26.0F, -0.5F, 1.0F, 35.0F, 1.0F)
                .texOffs(64, 29).addBox(-2.0F, -27.0F, -2.0F, 4.0F, 1.0F, 4.0F)
                .texOffs(34, 21).addBox(-1.5F, -36.0F, -1.5F, 3.0F, 9.0F, 3.0F)
                .texOffs(118, 6).addBox(-1.0F, -39.0F, -1.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, 1.5708F, 0.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 128, 128).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    /** The staff level at the hip, carried round from his right to his left by the whole trunk: {@code side} -1 is the same from left to right. */
    private void sweep(float draw, float cut, float side) {
        Anim.pose(this.torso, draw, 0.1F, 0.8F * side, 0.0F);
        Anim.pose(this.rightArm, draw, -0.35F, 0.5F * side, 0.15F);
        Anim.pose(this.torso, cut, 0.2F, -0.8F * side, 0.0F);
        Anim.pose(this.rightArm, cut, -0.45F, -0.5F * side, 0.15F);
        Anim.pose(this.rightForearm, Math.max(draw, cut), 0.12F, 0.0F, 0.0F);
        Anim.pose(this.leftArm, draw, -0.6F, 0.0F, -0.3F);
        Anim.pose(this.leftArm, cut, 0.4F, 0.0F, -0.5F);
    }

    private void thrust(float draw, float go) {
        Anim.pose(this.torso, draw, 0.0F, 0.45F, 0.0F);
        Anim.pose(this.rightArm, draw, 0.7F, 0.0F, 0.1F);
        Anim.pose(this.leftArm, draw, -0.9F, 0.0F, 0.0F);
        Anim.pose(this.torso, go, 0.35F, -0.35F, 0.0F);
        Anim.pose(this.rightArm, go, -0.43F, 0.0F, 0.1F);
        Anim.pose(this.leftArm, go, 0.5F, 0.0F, -0.2F);
        Anim.pose(this.rightForearm, Math.max(draw, go), 0.12F, 0.0F, 0.0F);
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float walk = state.walkAnimationPos * 0.5F;
        float t = state.attackTime;
        // how far gone in sleep he is: wholly, in his second phase; going, while he falls asleep
        float asleep = state.phase >= 2 ? 1.0F : state.attackId == ElderEntity.SLUMBER ? Anim.ramp(t, 8.0F, ElderEntity.SLUMBER_DURATION - 6.0F) : 0.0F;

        // He carries the candle upright before his right shoulder; the robe sways as he goes.
        Anim.pose(this.rightArm, 1.0F, -0.75F, 0.0F, -0.08F);
        Anim.pose(this.rightForearm, 1.0F, -0.68F, 0.0F, 0.0F);
        this.rightArm.xRot += Mth.sin(age * 0.05F) * 0.02F;
        this.leftArm.xRot += Mth.sin(walk) * 0.25F * speed;
        this.skirt.zRot += Mth.sin(walk) * 0.04F * speed;
        this.skirt.xRot += 0.05F * speed;
        this.torso.xRot += 0.06F * speed + Mth.sin(age * 0.045F) * 0.012F;
        this.head.yRot += Mth.clamp(state.yRot, -60.0F, 60.0F) * DEG * (1.0F - asleep);
        this.head.xRot += Mth.clamp(state.xRot, -35.0F, 35.0F) * DEG * 0.6F * (1.0F - asleep);
        if (asleep > 0.0F) {
            // asleep on his feet: hung forward, the head rolled onto a shoulder, the candle let sink; and he twitches
            float twitch = Mth.sin(age * 0.9F) * Mth.sin(age * 0.23F) * 0.04F;
            Anim.pose(this.torso, asleep, 0.3F + twitch, 0.0F, 0.08F);
            Anim.pose(this.head, asleep, 0.4F, 0.15F, 0.3F + Mth.sin(age * 0.07F) * 0.06F);
            Anim.pose(this.rightArm, asleep, -0.5F, 0.0F, 0.1F);
            Anim.pose(this.rightForearm, asleep, -0.3F, 0.0F, 0.0F);
            Anim.pose(this.leftArm, asleep, 0.1F + twitch * 3.0F, 0.0F, -0.1F);
        }

        switch (state.attackId) {
            case ElderEntity.SWEEP -> this.sweep(Anim.pulse(t, 0.0F, 13.0F, ElderEntity.SWEEP_IMPACT - 3.0F, ElderEntity.SWEEP_IMPACT),
                    Anim.pulse(t, ElderEntity.SWEEP_IMPACT - 3.0F, ElderEntity.SWEEP_IMPACT + 1.0F, ElderEntity.SWEEP_IMPACT + 8.0F, ElderEntity.SWEEP_DURATION - 2.0F), 1.0F);
            case ElderEntity.THRUST -> this.thrust(Anim.pulse(t, 0.0F, 11.0F, ElderEntity.THRUST_IMPACT - 3.0F, ElderEntity.THRUST_IMPACT),
                    Anim.pulse(t, ElderEntity.THRUST_IMPACT - 3.0F, ElderEntity.THRUST_IMPACT, ElderEntity.THRUST_IMPACT + 7.0F, ElderEntity.THRUST_DURATION - 2.0F));
            case ElderEntity.WAX_RAIN -> {
                // The candle goes up as high as he can hold it, the face lifted to it and the left hand open beside it; it is shaken.
                float up = Anim.pulse(t, 0.0F, ElderEntity.RAIN_FROM - 2.0F, ElderEntity.RAIN_UNTIL, ElderEntity.RAIN_DURATION - 2.0F);
                float shake = Mth.sin(t * 1.4F) * 0.06F * up;
                Anim.pose(this.torso, up, -0.15F, 0.0F, 0.0F);
                Anim.pose(this.head, up, -0.4F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, up, -2.2F + shake, 0.0F, -0.2F);
                Anim.pose(this.rightForearm, up, 0.8F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, up, -2.6F, 0.0F, 0.35F);
                Anim.pose(this.leftForearm, up, -0.2F, 0.0F, 0.0F);
            }
            case ElderEntity.CALL -> {
                // The foot of the candlestick is lifted and brought down on the ground, and the left hand thrown out at those on their knees.
                float lift = Anim.pulse(t, 0.0F, 14.0F, ElderEntity.CALL_KNOCK - 3.0F, ElderEntity.CALL_KNOCK);
                float point = Anim.pulse(t, ElderEntity.CALL_KNOCK - 4.0F, ElderEntity.CALL_KNOCK + 2.0F, ElderEntity.CALL_DURATION - 14.0F, ElderEntity.CALL_DURATION - 2.0F);
                Anim.pose(this.rightArm, lift, -1.1F, 0.0F, -0.1F);
                Anim.pose(this.rightForearm, lift, -0.35F, 0.0F, 0.0F);
                Anim.pose(this.torso, point, 0.1F, 0.0F, 0.0F);
                Anim.pose(this.head, point, -0.3F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, point, -1.9F, 0.0F, 0.9F);
                Anim.pose(this.leftForearm, point, -0.6F, 0.0F, 0.0F);
            }
            case ElderEntity.SLUMBER -> {
                // He sways; the hand goes to his face, slowly, and does not get there.
                float sway = Mth.sin(t * 0.25F) * (1.0F - asleep) * 0.12F;
                this.torso.zRot += sway;
                Anim.pose(this.leftArm, Anim.pulse(t, 0.0F, 12.0F, 26.0F, 44.0F), -1.6F, 0.0F, 0.4F);
            }
            case ElderEntity.FLURRY -> {
                // Asleep, he lays about him: four sweeps, to and fro, each a step further.
                for (int i = 0; i < ElderEntity.FLURRY_HITS.length; i++) {
                    float hit = ElderEntity.FLURRY_HITS[i];
                    this.sweep(Anim.pulse(t, hit - 8.0F, hit - 4.0F, hit - 3.0F, hit), Anim.pulse(t, hit - 3.0F, hit, hit + 2.0F, hit + 7.0F), i % 2 == 0 ? 1.0F : -1.0F);
                }
            }
            case ElderEntity.LUNGE -> this.thrust(Anim.pulse(t, 0.0F, 7.0F, ElderEntity.LUNGE_START - 1.0F, ElderEntity.LUNGE_START + 2.0F),
                    Anim.pulse(t, ElderEntity.LUNGE_START, ElderEntity.LUNGE_START + 3.0F, ElderEntity.LUNGE_IMPACT + 6.0F, ElderEntity.LUNGE_DURATION - 2.0F));
            case ElderEntity.DREAM -> {
                // He stands still and folds over the candle with both hands on it: what he is dreaming goes out from him.
                float fold = Anim.pulse(t, 0.0F, 14.0F, ElderEntity.DREAM_SPREADS + 8.0F, ElderEntity.DREAM_DURATION - 4.0F);
                Anim.pose(this.torso, fold, 0.36F, 0.0F, 0.0F);
                Anim.pose(this.head, fold, 0.5F, 0.0F, 0.35F);
                Anim.pose(this.rightArm, fold, -0.85F, 0.0F, -0.5F);
                Anim.pose(this.rightForearm, fold, -0.75F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, fold, -0.9F, 0.0F, 0.5F);
                Anim.pose(this.leftForearm, fold, -0.9F, 0.0F, 0.0F);
            }
            default -> {
            }
        }
        // the beard hangs: it is turned back by whatever the head and the trunk have been bent by
        this.beard.xRot = Mth.clamp(-(this.torso.xRot + this.head.xRot), -1.2F, 0.3F);
        this.beard.zRot = -(this.torso.zRot + this.head.zRot);
    }
}
