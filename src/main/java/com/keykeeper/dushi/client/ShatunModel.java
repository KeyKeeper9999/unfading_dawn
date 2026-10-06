package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.ShatunEntity;

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
 * The Shatun. The mesh between the markers is written by tools/gen_shatun.py (which also paints the textures and draws
 * the poses below on a sheet); the animation is written by hand.
 * <p>
 * The trunk hangs from the hips, so that it rears: chest, head and fore legs go up with it and the hind legs stay under
 * it. Standing still it does what a bear does that has been kept from sleep too long: it weaves, the head low, from
 * one fore foot to the other, and does not stop.
 */
public class ShatunModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("shatun"), "main");
    /** Must match SCALE in tools/gen_shatun.py. */
    private static final float MODEL_SCALE = 1.5F;
    private static final float DEG = (float) (Math.PI / 180.0);

    private final ModelPart all;
    private final ModelPart body;
    private final ModelPart chest;
    private final ModelPart chain;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightForearm;
    private final ModelPart leftForearm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightShin;
    private final ModelPart leftShin;

    public ShatunModel(ModelPart root) {
        super(root);
        this.all = root.getChild("all");
        this.body = this.all.getChild("body");
        this.chest = this.body.getChild("chest");
        this.chain = this.chest.getChild("chain");
        this.head = this.chest.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.rightArm = this.chest.getChild("right_arm");
        this.leftArm = this.chest.getChild("left_arm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.rightLeg = this.all.getChild("right_leg");
        this.leftLeg = this.all.getChild("left_leg");
        this.rightShin = this.rightLeg.getChild("right_shin");
        this.leftShin = this.leftLeg.getChild("left_shin");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.ZERO);
        PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(60, 0).addBox(-6.0F, -8.0F, -6.0F, 12.0F, 13.0F, 11.0F)
                .texOffs(66, 66).addBox(-1.5F, -6.0F, 5.0F, 3.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, 6.0F, 9.0F));
        PartDefinition chest = body.addOrReplaceChild("chest", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-7.5F, -8.0F, -15.0F, 15.0F, 15.0F, 15.0F)
                .texOffs(0, 47).addBox(-5.0F, -11.0F, -13.0F, 10.0F, 3.0F, 9.0F)
                .texOffs(56, 44).addBox(-6.0F, -6.0F, -17.0F, 12.0F, 11.0F, 2.0F),
                PartPose.offset(0.0F, -2.0F, -6.0F));
        PartDefinition chain = chest.addOrReplaceChild("chain", CubeListBuilder.create()
                .texOffs(112, 17).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(-3.0F, 5.0F, -16.0F, 0.15F, 0.0F, 0.12F));
        PartDefinition head = chest.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 30).addBox(-4.5F, -5.0F, -8.0F, 9.0F, 9.0F, 8.0F)
                .texOffs(106, 57).addBox(-2.5F, -0.5F, -13.0F, 5.0F, 4.0F, 5.0F)
                .texOffs(76, 66).addBox(-5.0F, -7.0F, -3.0F, 3.0F, 3.0F, 2.0F)
                .texOffs(86, 66).addBox(2.0F, -7.0F, -3.0F, 3.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -1.0F, -15.0F, 0.25F, 0.0F, 0.0F));
        PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(48, 66).addBox(-2.0F, 0.0F, -5.0F, 4.0F, 2.0F, 5.0F)
                .texOffs(112, 66).addBox(-2.0F, -1.0F, -5.0F, 4.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 3.5F, -8.0F, 0.1F, 0.0F, 0.0F));
        PartDefinition right_arm = chest.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(106, 0).addBox(-3.0F, -1.0F, -3.0F, 5.0F, 11.0F, 6.0F),
                PartPose.offset(-5.0F, 1.0F, -10.0F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(84, 44).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 8.0F, 5.0F)
                .texOffs(54, 57).addBox(-3.0F, 7.0F, -5.5F, 6.0F, 2.0F, 7.0F)
                .texOffs(112, 41).addBox(-3.0F, 8.0F, -7.5F, 6.0F, 1.0F, 2.0F),
                PartPose.offset(-0.5F, 10.0F, 0.0F));
        PartDefinition left_arm = chest.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(34, 30).addBox(-2.0F, -1.0F, -3.0F, 5.0F, 11.0F, 6.0F),
                PartPose.offset(5.0F, 1.0F, -10.0F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(104, 44).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 8.0F, 5.0F)
                .texOffs(80, 57).addBox(-3.0F, 7.0F, -5.5F, 6.0F, 2.0F, 7.0F)
                .texOffs(96, 66).addBox(-3.0F, 8.0F, -7.5F, 6.0F, 1.0F, 2.0F),
                PartPose.offset(0.5F, 10.0F, 0.0F));
        PartDefinition right_leg = all.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(60, 24).addBox(-3.0F, -2.0F, -4.0F, 5.0F, 12.0F, 8.0F),
                PartPose.offset(-5.0F, 7.0F, 11.0F));
        PartDefinition right_shin = right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(112, 30).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F)
                .texOffs(0, 59).addBox(-2.5F, 5.0F, -5.0F, 5.0F, 2.0F, 7.0F),
                PartPose.offset(-0.5F, 10.0F, 1.0F));
        PartDefinition left_leg = all.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(86, 24).addBox(-2.0F, -2.0F, -4.0F, 5.0F, 12.0F, 8.0F),
                PartPose.offset(5.0F, 7.0F, 11.0F));
        PartDefinition left_shin = left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(38, 47).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F)
                .texOffs(24, 59).addBox(-2.5F, 5.0F, -5.0F, 5.0F, 2.0F, 7.0F),
                PartPose.offset(0.5F, 10.0F, 1.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 128, 128).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    /** One paw: drawn far back and out, then across. {@code side} is 1 for the right paw, -1 for the left. */
    private void swipe(float draw, float cut, float side) {
        ModelPart arm = side > 0.0F ? this.rightArm : this.leftArm;
        ModelPart forearm = side > 0.0F ? this.rightForearm : this.leftForearm;
        Anim.pose(this.body, draw, -0.25F, 0.3F * side, 0.0F);
        Anim.pose(this.chest, draw, 0.0F, 0.2F * side, -0.15F * side);
        Anim.pose(arm, draw, -1.9F, 0.0F, 0.7F * side);
        Anim.pose(forearm, draw, -0.5F, 0.0F, 0.0F);
        Anim.pose(this.head, draw, 0.1F, -0.5F * side, 0.0F);
        Anim.pose(this.body, cut, 0.0F, -0.3F * side, 0.0F);
        Anim.pose(this.chest, cut, 0.1F, -0.3F * side, 0.15F * side);
        Anim.pose(arm, cut, -0.9F, 0.0F, -0.6F * side);
        Anim.pose(forearm, cut, -0.3F, 0.0F, 0.0F);
        Anim.pose(this.head, cut, 0.2F, 0.3F * side, 0.0F);
        this.jaw.xRot += 0.5F * draw + 0.3F * cut;
    }

    /** Up on its hind legs, the paws high; then down on both. */
    private void rearAndSlam(float rear, float slam) {
        Anim.pose(this.body, rear, -1.05F, 0.0F, 0.0F);
        Anim.pose(this.chest, rear, -0.15F, 0.0F, 0.0F);
        Anim.pose(this.head, rear, 0.75F, 0.0F, 0.0F);
        Anim.pose(this.rightArm, rear, -1.3F, 0.0F, 0.5F);
        Anim.pose(this.leftArm, rear, -1.3F, 0.0F, -0.5F);
        Anim.pose(this.rightForearm, rear, -0.6F, 0.0F, 0.0F);
        Anim.pose(this.leftForearm, rear, -0.6F, 0.0F, 0.0F);
        Anim.pitch(this.rightLeg, rear, 0.25F);
        Anim.pitch(this.leftLeg, rear, 0.25F);
        this.jaw.xRot += 0.7F * rear;
        Anim.pose(this.body, slam, 0.14F, 0.0F, 0.0F);
        Anim.pose(this.chest, slam, 0.1F, 0.0F, 0.0F);
        Anim.pose(this.head, slam, 0.2F, 0.0F, 0.0F);
        Anim.pose(this.rightArm, slam, -0.7F, 0.0F, 0.1F);
        Anim.pose(this.leftArm, slam, -0.7F, 0.0F, -0.1F);
        Anim.pose(this.rightForearm, slam, 0.2F, 0.0F, 0.0F);
        Anim.pose(this.leftForearm, slam, 0.2F, 0.0F, 0.0F);
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float walk = state.walkAnimationPos * 0.45F;
        float t = state.attackTime;
        boolean roused = state.phase >= 2;

        // It weaves: the weight from one fore foot to the other, the head swinging low the other way. It breathes hard.
        float weave = Mth.sin(age * (roused ? 0.13F : 0.085F));
        float still = 1.0F - speed;
        this.chest.zRot += weave * 0.05F * still;
        this.chest.yRot += weave * 0.06F * still;
        this.head.yRot += -weave * 0.22F * still + Mth.clamp(state.yRot, -40.0F, 40.0F) * DEG;
        this.head.xRot += 0.08F * still + Mth.clamp(state.xRot, -25.0F, 25.0F) * DEG * 0.5F;
        this.jaw.xRot += 0.15F + Mth.sin(age * 0.5F) * 0.06F;
        this.chest.yScale = 1.0F + Mth.sin(age * 0.22F) * 0.015F;
        this.chain.zRot += Mth.sin(age * 0.11F + walk) * (0.08F + 0.25F * speed);
        this.chain.xRot += Mth.cos(age * 0.09F + walk) * (0.05F + 0.25F * speed);
        // Walking: a fore foot and the hind foot across from it together; the head goes down with the leading shoulder.
        float stride = Mth.sin(walk);
        this.rightArm.xRot += -stride * 0.55F * speed;
        this.leftArm.xRot += stride * 0.55F * speed;
        this.rightForearm.xRot += Math.max(0.0F, Mth.cos(walk)) * 0.5F * speed;
        this.leftForearm.xRot += Math.max(0.0F, -Mth.cos(walk)) * 0.5F * speed;
        this.rightLeg.xRot += stride * 0.5F * speed;
        this.leftLeg.xRot += -stride * 0.5F * speed;
        this.rightShin.xRot += Math.max(0.0F, -Mth.cos(walk)) * 0.4F * speed;
        this.leftShin.xRot += Math.max(0.0F, Mth.cos(walk)) * 0.4F * speed;
        this.body.zRot += stride * 0.04F * speed;
        this.head.yRot += stride * 0.1F * speed;

        switch (state.attackId) {
            case ShatunEntity.SWIPE -> this.swipe(Anim.pulse(t, 0.0F, 9.0F, 12.0F, 15.0F), Anim.pulse(t, 12.0F, 15.0F, 21.0F, 34.0F), 1.0F);
            case ShatunEntity.SWIPE_LEFT -> this.swipe(Anim.pulse(t, 0.0F, 9.0F, 12.0F, 15.0F), Anim.pulse(t, 12.0F, 15.0F, 21.0F, 34.0F), -1.0F);
            case ShatunEntity.BITE -> {
                // The head goes back between the shoulders - and out with all of it behind, the jaws wide.
                float draw = Anim.pulse(t, 0.0F, 9.0F, ShatunEntity.BITE_LUNGE - 1.0F, ShatunEntity.BITE_LUNGE + 2.0F);
                float snap = Anim.pulse(t, ShatunEntity.BITE_LUNGE - 1.0F, ShatunEntity.BITE_LUNGE + 2.0F, ShatunEntity.BITE_IMPACT, ShatunEntity.BITE_IMPACT + 12.0F);
                float shut = Anim.ramp(t, ShatunEntity.BITE_IMPACT - 1.0F, ShatunEntity.BITE_IMPACT + 1.0F);
                Anim.pose(this.chest, draw, -0.1F, 0.0F, 0.0F);
                Anim.pose(this.head, draw, -0.4F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, draw, 0.3F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, draw, 0.3F, 0.0F, 0.0F);
                Anim.pose(this.chest, snap, 0.15F, 0.0F, 0.0F);
                Anim.pose(this.head, snap, -0.3F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, snap, -0.5F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, snap, -0.5F, 0.0F, 0.0F);
                Anim.pitch(this.rightLeg, snap, 0.5F);
                Anim.pitch(this.leftLeg, snap, 0.5F);
                this.jaw.xRot += 0.2F * draw + 1.0F * snap * (1.0F - shut);
            }
            case ShatunEntity.REAR -> this.rearAndSlam(Anim.pulse(t, 0.0F, 18.0F, ShatunEntity.REAR_IMPACT - 5.0F, ShatunEntity.REAR_IMPACT),
                    Anim.pulse(t, ShatunEntity.REAR_IMPACT - 4.0F, ShatunEntity.REAR_IMPACT, ShatunEntity.REAR_IMPACT + 14.0F, ShatunEntity.REAR_DURATION - 4.0F));
            case ShatunEntity.CHARGE -> {
                // It rakes the ground with one fore paw, the head right down - and goes, bounding.
                float paw = Anim.pulse(t, 0.0F, 6.0F, ShatunEntity.CHARGE_START - 3.0F, ShatunEntity.CHARGE_START);
                float run = Anim.pulse(t, ShatunEntity.CHARGE_START - 2.0F, ShatunEntity.CHARGE_START + 2.0F, ShatunEntity.CHARGE_END, ShatunEntity.CHARGE_END + 8.0F);
                Anim.pose(this.head, paw, 0.45F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, paw, -0.5F + Mth.sin(t * 0.9F) * 0.5F, 0.0F, 0.0F);
                Anim.pose(this.rightForearm, paw, 0.6F + Mth.sin(t * 0.9F) * 0.4F, 0.0F, 0.0F);
                float bound = Mth.sin(t * 0.75F);
                Anim.pose(this.body, run, 0.08F + bound * 0.1F, 0.0F, 0.0F);
                Anim.pose(this.head, run, 0.3F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, run, -0.2F - bound * 0.9F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, run, -0.1F - bound * 0.8F, 0.0F, 0.0F);
                Anim.pose(this.rightForearm, run, 0.5F, 0.0F, 0.0F);
                Anim.pose(this.leftForearm, run, 0.4F, 0.0F, 0.0F);
                Anim.pitch(this.rightLeg, run, bound * 0.8F);
                Anim.pitch(this.leftLeg, run, bound * 0.7F);
                this.all.y -= Math.max(0.0F, bound) * 2.0F * run;
            }
            case ShatunEntity.CRASH -> {
                // Into something that did not give: it sits back on its haunches, the head rolling, before it finds its feet.
                float stunned = Anim.pulse(t, 0.0F, 5.0F, ShatunEntity.CRASH_DURATION - 16.0F, ShatunEntity.CRASH_DURATION - 2.0F);
                Anim.pose(this.body, stunned, -0.35F, 0.0F, Mth.sin(t * 0.3F) * 0.06F);
                Anim.pose(this.head, stunned, 0.7F, Mth.sin(t * 0.37F) * 0.5F, Mth.sin(t * 0.29F) * 0.3F);
                Anim.pose(this.rightArm, stunned, 0.4F, 0.0F, 0.3F);
                Anim.pose(this.leftArm, stunned, 0.5F, 0.0F, -0.3F);
                Anim.pitch(this.rightLeg, stunned, -0.9F);
                Anim.pitch(this.leftLeg, stunned, -0.9F);
                Anim.pitch(this.rightShin, stunned, 0.9F);
                Anim.pitch(this.leftShin, stunned, 0.9F);
                this.all.y += 4.0F * stunned;
                this.jaw.xRot += 0.5F * stunned;
            }
            case ShatunEntity.ROAR -> {
                float roar = Anim.pulse(t, 0.0F, ShatunEntity.ROAR_SOUND, ShatunEntity.ROAR_SOUND + 22.0F, ShatunEntity.ROAR_DURATION - 2.0F);
                float shake = Mth.sin(t * 2.3F) * 0.05F * roar;
                Anim.pose(this.body, roar, -0.3F, 0.0F, 0.0F);
                Anim.pose(this.chest, roar, -0.1F, 0.0F, shake);
                Anim.pose(this.head, roar, -0.8F, shake * 2.0F, 0.0F);
                Anim.pose(this.rightArm, roar, 0.25F, 0.0F, 0.15F);
                Anim.pose(this.leftArm, roar, 0.25F, 0.0F, -0.15F);
                this.jaw.xRot += 1.1F * roar;
            }
            case ShatunEntity.MAUL -> {
                // Right, left, and then up and down on both: it does not wait between them.
                this.swipe(Anim.pulse(t, 0.0F, 8.0F, ShatunEntity.MAUL_FIRST - 3.0F, ShatunEntity.MAUL_FIRST),
                        Anim.pulse(t, ShatunEntity.MAUL_FIRST - 3.0F, ShatunEntity.MAUL_FIRST, ShatunEntity.MAUL_FIRST + 4.0F, ShatunEntity.MAUL_FIRST + 10.0F), 1.0F);
                this.swipe(Anim.pulse(t, ShatunEntity.MAUL_FIRST + 4.0F, ShatunEntity.MAUL_SECOND - 5.0F, ShatunEntity.MAUL_SECOND - 3.0F, ShatunEntity.MAUL_SECOND),
                        Anim.pulse(t, ShatunEntity.MAUL_SECOND - 3.0F, ShatunEntity.MAUL_SECOND, ShatunEntity.MAUL_SECOND + 4.0F, ShatunEntity.MAUL_SECOND + 10.0F), -1.0F);
                this.rearAndSlam(Anim.pulse(t, ShatunEntity.MAUL_SECOND + 5.0F, ShatunEntity.MAUL_THIRD - 8.0F, ShatunEntity.MAUL_THIRD - 5.0F, ShatunEntity.MAUL_THIRD),
                        Anim.pulse(t, ShatunEntity.MAUL_THIRD - 4.0F, ShatunEntity.MAUL_THIRD, ShatunEntity.MAUL_THIRD + 12.0F, ShatunEntity.MAUL_DURATION - 4.0F));
            }
            default -> {
            }
        }
    }
}
