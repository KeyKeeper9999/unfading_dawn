package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.LankyEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * The lanky. The mesh between the markers is written by tools/gen_lanky.py (which also paints the textures); the
 * animation is written by hand. The trunk turns about the hips, where the legs begin, so that bending it never leaves
 * them behind; the arms hang from the trunk and have a joint each.
 */
public class LankyModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("lanky"), "main");
    private static final float DEG = (float) (Math.PI / 180.0);

    private final ModelPart torso;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightForearm;
    private final ModelPart leftForearm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightShin;
    private final ModelPart leftShin;

    public LankyModel(ModelPart root) {
        super(root);
        this.torso = root.getChild("torso");
        this.head = this.torso.getChild("head");
        this.rightArm = this.torso.getChild("right_arm");
        this.leftArm = this.torso.getChild("left_arm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.rightShin = this.rightLeg.getChild("right_shin");
        this.leftShin = this.leftLeg.getChild("left_shin");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(32, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 14.0F, 2.0F),
                PartPose.offset(-1.5F, -4.0F, 0.0F));
        PartDefinition right_shin = right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(40, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 14.0F, 2.0F)
                .texOffs(0, 41).addBox(-1.5F, 13.0F, -3.0F, 3.0F, 1.0F, 4.0F),
                PartPose.offset(0.0F, 14.0F, 0.0F));
        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(48, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 14.0F, 2.0F),
                PartPose.offset(1.5F, -4.0F, 0.0F));
        PartDefinition left_shin = left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(56, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 14.0F, 2.0F)
                .texOffs(14, 42).addBox(-1.5F, 13.0F, -3.0F, 3.0F, 1.0F, 4.0F),
                PartPose.offset(0.0F, 14.0F, 0.0F));
        PartDefinition torso = root.addOrReplaceChild("torso", CubeListBuilder.create()
                .texOffs(42, 31).addBox(-2.5F, -6.0F, -1.5F, 5.0F, 6.0F, 3.0F)
                .texOffs(0, 18).addBox(-3.5F, -16.0F, -2.0F, 7.0F, 10.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.22F, 0.0F, 0.0F));
        PartDefinition head = torso.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(22, 31).addBox(-2.5F, -6.0F, -2.5F, 5.0F, 6.0F, 5.0F)
                .texOffs(32, 16).addBox(-3.0F, -6.5F, -3.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(0.25F))
                .texOffs(56, 16).addBox(-0.5F, -11.0F, 0.5F, 1.0F, 5.0F, 1.0F)
                .texOffs(60, 16).addBox(1.5F, -9.0F, -1.0F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 0.1F, 0.0F, 0.0F));
        PartDefinition right_arm = torso.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(-4.0F, -15.0F, 0.0F, 0.05F, 0.0F, 0.1F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(8, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 16.0F, 2.0F)
                .texOffs(0, 32).addBox(-1.5F, 16.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 15.0F, 0.0F, -0.28F, 0.0F, 0.0F));
        PartDefinition left_arm = torso.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(16, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(4.0F, -15.0F, 0.0F, 0.05F, 0.0F, -0.1F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(24, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 16.0F, 2.0F)
                .texOffs(42, 40).addBox(-1.5F, 16.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 15.0F, 0.0F, -0.28F, 0.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float walk = state.walkAnimationPos * 0.5F;

        // Standing, it is a dead tree in a little wind: the trunk leans and comes back, the arms turn on their own
        // weight, the head is still - and then it turns to look.
        float sway = Mth.sin(age * 0.035F);
        this.torso.zRot += sway * 0.03F;
        this.torso.xRot += Mth.sin(age * 0.05F + 1.0F) * 0.02F;
        this.rightArm.xRot += Mth.sin(age * 0.045F) * 0.05F;
        this.leftArm.xRot += Mth.sin(age * 0.045F + 2.0F) * 0.05F;
        this.rightArm.zRot += 0.02F + sway * 0.02F;
        this.leftArm.zRot -= 0.02F - sway * 0.02F;
        this.head.yRot += Mth.clamp(state.yRot, -70.0F, 70.0F) * DEG;
        this.head.xRot += Mth.clamp(state.xRot, -40.0F, 40.0F) * DEG * 0.6F;

        // Walking: long slow strides, the knee coming up high and the shin swinging out under it; the trunk goes with
        // the leading leg and the long arms swing the other way.
        float stride = Mth.sin(walk);
        float lift = Mth.cos(walk);
        this.rightLeg.xRot += -stride * 0.7F * speed;
        this.leftLeg.xRot += stride * 0.7F * speed;
        this.rightShin.xRot += Math.max(0.0F, lift) * 0.8F * speed;
        this.leftShin.xRot += Math.max(0.0F, -lift) * 0.8F * speed;
        this.torso.yRot += stride * 0.12F * speed;
        this.torso.xRot += 0.1F * speed;
        this.rightArm.xRot += stride * 0.4F * speed;
        this.leftArm.xRot -= stride * 0.4F * speed;
        this.rightForearm.xRot -= 0.15F * speed;
        this.leftForearm.xRot -= 0.15F * speed;

        float t = state.attackTime;
        switch (state.attackId) {
            case LankyEntity.REACH -> {
                // The right arm folds back beside the trunk, elbow high, the trunk turning away with it - a long moment -
                // and goes out straight as a pole, the whole of it leaning in behind the hand.
                float draw = Anim.pulse(t, 0.0F, 9.0F, 12.0F, 15.0F);
                float thrust = Anim.pulse(t, 12.0F, 15.0F, 20.0F, 32.0F);
                this.torso.yRot += 0.5F * draw - 0.35F * thrust;
                this.torso.xRot += -0.1F * draw + 0.38F * thrust;
                this.rightArm.xRot = Anim.lerp(draw, this.rightArm.xRot, 0.9F);
                this.rightForearm.xRot = Anim.lerp(draw, this.rightForearm.xRot, -1.7F);
                this.rightArm.xRot = Anim.lerp(thrust, this.rightArm.xRot, -1.95F);
                this.rightForearm.xRot = Anim.lerp(thrust, this.rightForearm.xRot, 0.2F);
                this.rightArm.zRot = Anim.lerp(Math.max(draw, thrust), this.rightArm.zRot, 0.05F);
                this.leftArm.xRot += 0.5F * thrust;
                this.head.xRot -= 0.3F * thrust;
                this.rightLeg.xRot -= 0.3F * thrust;
                this.leftLeg.xRot += 0.25F * thrust;
            }
            case LankyEntity.SWEEP -> {
                // It folds down over whoever is under it, both arms carried round to its right, and brings them back
                // across low, one after the other, turning from the hips.
                float draw = Anim.pulse(t, 0.0F, 10.0F, 14.0F, 17.0F);
                float cut = Anim.pulse(t, 14.0F, 17.0F, 23.0F, 38.0F);
                float held = Math.max(draw, cut);
                this.torso.xRot += 0.5F * held;
                this.torso.yRot += 0.75F * draw - 0.8F * cut;
                this.rightArm.xRot = Anim.lerp(held, this.rightArm.xRot, -1.25F);
                this.leftArm.xRot = Anim.lerp(held, this.leftArm.xRot, -1.15F);
                this.rightArm.yRot += 0.9F * draw - 0.7F * cut;
                this.leftArm.yRot += 0.3F * draw - 1.1F * cut;
                this.rightForearm.xRot = Anim.lerp(held, this.rightForearm.xRot, 0.15F);
                this.leftForearm.xRot = Anim.lerp(held, this.leftForearm.xRot, 0.15F);
                this.head.yRot += -0.4F * draw + 0.4F * cut;
            }
            case LankyEntity.SLAM -> {
                // Both arms go straight up - it is twice its height for a breath - and come down together like the beam
                // of a well, the trunk folding at the hips behind them.
                float raise = Anim.pulse(t, 0.0F, 13.0F, 19.0F, 22.0F);
                float fall = Anim.pulse(t, 19.0F, 22.0F, 30.0F, 46.0F);
                this.torso.xRot += -0.28F * raise + 0.78F * fall;
                this.rightArm.xRot = Anim.lerp(raise, this.rightArm.xRot, -3.0F);
                this.leftArm.xRot = Anim.lerp(raise, this.leftArm.xRot, -3.0F);
                this.rightArm.xRot = Anim.lerp(fall, this.rightArm.xRot, -1.5F);
                this.leftArm.xRot = Anim.lerp(fall, this.leftArm.xRot, -1.5F);
                this.rightForearm.xRot = Anim.lerp(Math.max(raise, fall), this.rightForearm.xRot, 0.25F);
                this.leftForearm.xRot = Anim.lerp(Math.max(raise, fall), this.leftForearm.xRot, 0.25F);
                this.head.xRot += -0.35F * raise + 0.1F * fall;
                this.rightLeg.xRot -= 0.3F * fall;
                this.leftLeg.xRot += 0.3F * fall;
            }
            default -> {
            }
        }
    }
}
