package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.BlindCrawlerEntity;

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
 * The blind crawler. The mesh between the markers is written by tools/gen_crawler.py (which also paints the texture);
 * the animation is written by hand.
 */
public class BlindCrawlerModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("blind_crawler"), "main");
    private static final float DEG = (float) (Math.PI / 180.0);

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart rightEar;
    private final ModelPart leftEar;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightForearm;
    private final ModelPart leftForearm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightShin;
    private final ModelPart leftShin;

    public BlindCrawlerModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.rightEar = this.head.getChild("right_ear");
        this.leftEar = this.head.getChild("left_ear");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.rightLeg = this.body.getChild("right_leg");
        this.leftLeg = this.body.getChild("left_leg");
        this.rightShin = this.rightLeg.getChild("right_shin");
        this.leftShin = this.leftLeg.getChild("left_shin");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 7.0F, 9.0F)
                .texOffs(0, 16).addBox(-3.0F, -3.5F, 1.0F, 6.0F, 5.0F, 7.0F)
                .texOffs(34, 0).addBox(-0.5F, -5.0F, -7.0F, 1.0F, 1.0F, 14.0F),
                PartPose.offsetAndRotation(0.0F, 13.0F, 1.0F, 0.16F, 0.0F, 0.0F));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 28).addBox(-3.5F, -4.0F, -6.0F, 7.0F, 6.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, -1.0F, -8.0F, 0.1F, 0.0F, 0.0F));
        PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(24, 48).addBox(-3.0F, 0.0F, -5.0F, 6.0F, 2.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.15F, 0.0F, 0.0F));
        PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create()
                .texOffs(24, 40).addBox(-5.0F, -6.0F, 0.0F, 5.0F, 7.0F, 1.0F),
                PartPose.offsetAndRotation(-3.5F, -3.0F, -1.5F, 0.0F, 0.5F, -0.35F));
        PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create()
                .texOffs(36, 40).addBox(0.0F, -6.0F, 0.0F, 5.0F, 7.0F, 1.0F),
                PartPose.offsetAndRotation(3.5F, -3.0F, -1.5F, 0.0F, -0.5F, 0.35F));
        PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(26, 28).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 9.0F, 3.0F),
                PartPose.offsetAndRotation(-4.0F, -2.0F, -6.0F, 0.2F, 0.0F, 0.75F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(34, 15).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F)
                .texOffs(0, 50).addBox(-2.0F, 10.0F, -4.0F, 4.0F, 1.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, -0.75F, 0.0F, -0.75F));
        PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(38, 28).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 9.0F, 3.0F),
                PartPose.offsetAndRotation(4.0F, -2.0F, -6.0F, 0.2F, 0.0F, -0.75F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(46, 15).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F)
                .texOffs(46, 50).addBox(-2.0F, 10.0F, -4.0F, 4.0F, 1.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, -0.75F, 0.0F, 0.75F));
        PartDefinition right_leg = body.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(50, 28).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 8.0F, 3.0F),
                PartPose.offsetAndRotation(-3.0F, 0.0F, 6.0F, -1.25F, 0.0F, 0.3F));
        PartDefinition right_shin = right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(0, 40).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F)
                .texOffs(18, 55).addBox(-1.5F, 6.0F, -3.5F, 3.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 1.95F, 0.0F, 0.0F));
        PartDefinition left_leg = body.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(50, 39).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 8.0F, 3.0F),
                PartPose.offsetAndRotation(3.0F, 0.0F, 6.0F, -1.25F, 0.0F, -0.3F));
        PartDefinition left_shin = left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(12, 40).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F)
                .texOffs(30, 55).addBox(-1.5F, 6.0F, -3.5F, 3.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 1.95F, 0.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float walk = state.walkAnimationPos * 1.1F;

        // Idle: it listens. The head swings slowly from side to side and tilts, the ears turn on their own, the ribs work.
        float breath = Mth.sin(age * 0.2F);
        float listen = Mth.sin(age * 0.045F);
        this.body.y += breath * 0.2F;
        this.head.yRot += listen * 0.45F * (1.0F - speed);
        this.head.zRot += Mth.sin(age * 0.031F + 1.0F) * 0.2F * (1.0F - speed);
        this.rightEar.yRot += Mth.sin(age * 0.13F) * 0.18F + (Mth.sin(age * 0.71F) > 0.93F ? 0.3F : 0.0F);
        this.leftEar.yRot -= Mth.sin(age * 0.11F + 2.0F) * 0.18F + (Mth.sin(age * 0.67F + 1.0F) > 0.93F ? 0.3F : 0.0F);
        this.jaw.xRot += Math.max(0.0F, breath) * 0.1F;
        this.head.yRot += Mth.clamp(state.yRot, -50.0F, 50.0F) * DEG * 0.6F;

        // Crawling: opposite arm and leg together, the back twisting, low.
        float step = Mth.cos(walk);
        float lift = Mth.sin(walk);
        this.rightArm.xRot += step * 0.8F * speed;
        this.leftArm.xRot -= step * 0.8F * speed;
        this.rightForearm.xRot -= Math.max(0.0F, lift) * 0.6F * speed;
        this.leftForearm.xRot -= Math.max(0.0F, -lift) * 0.6F * speed;
        this.rightLeg.xRot -= step * 0.5F * speed;
        this.leftLeg.xRot += step * 0.5F * speed;
        this.body.yRot += step * 0.12F * speed;
        this.body.zRot += lift * 0.05F * speed;

        float t = state.attackTime;
        switch (state.attackId) {
            case BlindCrawlerEntity.SHRIEK -> {
                // Rears on its arms with the head thrown back, the mouth wide and the ears flat out.
                float up = Anim.pulse(t, 0.0F, 5.0F, 16.0F, 21.0F);
                float shake = up * Mth.sin(t * 2.6F) * 0.05F;
                this.body.xRot -= 0.35F * up;
                this.head.xRot += -0.6F * up + shake;
                this.head.yRot *= 1.0F - up;
                this.jaw.xRot += 0.9F * up;
                this.rightEar.yRot -= 0.5F * up;
                this.leftEar.yRot += 0.5F * up;
                this.rightArm.xRot += 0.3F * up;
                this.leftArm.xRot += 0.3F * up;
            }
            case BlindCrawlerEntity.FLURRY -> {
                // Right, left, and both: each rake goes out from the shoulder and across.
                float right = Anim.pulse(t, 2.0F, 6.0F, 8.5F, 12.0F) + Anim.pulse(t, 14.0F, 17.5F, 20.0F, 27.0F);
                float left = Anim.pulse(t, 7.0F, 11.0F, 13.5F, 17.0F) + Anim.pulse(t, 14.0F, 17.5F, 20.0F, 27.0F);
                float rightCut = Anim.ramp(t, 7.0F, 9.0F) * (1.0F - Anim.ramp(t, 10.0F, 13.0F)) + Anim.ramp(t, 18.0F, 20.0F) * (1.0F - Anim.ramp(t, 22.0F, 28.0F));
                float leftCut = Anim.ramp(t, 12.0F, 14.0F) * (1.0F - Anim.ramp(t, 15.0F, 17.0F)) + Anim.ramp(t, 18.0F, 20.0F) * (1.0F - Anim.ramp(t, 22.0F, 28.0F));
                this.rightArm.xRot += -1.9F * right + 1.0F * rightCut;
                this.rightArm.zRot += -0.3F * right - 0.5F * rightCut;
                this.rightForearm.xRot += 0.5F * right;
                this.rightForearm.zRot += 0.5F * right;
                this.leftArm.xRot += -1.9F * left + 1.0F * leftCut;
                this.leftArm.zRot += 0.3F * left + 0.5F * leftCut;
                this.leftForearm.xRot += 0.5F * left;
                this.leftForearm.zRot -= 0.5F * left;
                this.body.yRot += 0.3F * (right - left) * (1.0F - Anim.ramp(t, 14.0F, 17.0F));
                this.body.xRot -= 0.12F * Math.max(right, left);
                this.jaw.xRot += 0.5F * Math.max(right, left);
            }
            case BlindCrawlerEntity.LEAP -> {
                // Gathers itself flat to the floor - the warning - and goes through the air with both arms reaching.
                float crouch = Anim.pulse(t, 0.0F, 8.0F, 11.0F, 13.0F);
                float air = state.onGround && t > 15.0F ? 0.0F : Anim.ramp(t, 11.0F, 14.0F);
                float land = state.onGround && t > 15.0F ? 1.0F - Anim.ramp(t, 22.0F, 33.0F) : 0.0F;
                this.body.y += 3.5F * crouch - 3.0F * air + 1.5F * land;
                this.body.xRot += 0.2F * crouch - 0.3F * air;
                this.head.xRot -= 0.3F * crouch + 0.2F * air;
                this.rightArm.xRot += 0.5F * crouch - 1.7F * air;
                this.leftArm.xRot += 0.5F * crouch - 1.7F * air;
                this.rightArm.zRot -= 0.4F * air;
                this.leftArm.zRot += 0.4F * air;
                this.rightForearm.xRot += -0.5F * crouch + 0.6F * air;
                this.leftForearm.xRot += -0.5F * crouch + 0.6F * air;
                this.rightForearm.zRot += 0.6F * air;
                this.leftForearm.zRot -= 0.6F * air;
                this.rightLeg.xRot += -0.2F * crouch + 1.3F * air;
                this.leftLeg.xRot += -0.2F * crouch + 1.3F * air;
                this.rightShin.xRot -= 1.2F * air;
                this.leftShin.xRot -= 1.2F * air;
                this.jaw.xRot += 0.8F * air + 0.3F * crouch;
            }
            default -> {
            }
        }
    }
}
