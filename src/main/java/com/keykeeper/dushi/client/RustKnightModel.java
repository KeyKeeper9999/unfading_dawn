package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.RustKnightEntity;

import net.minecraft.client.model.HumanoidModel;
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
 * The rust knight: a humanoid in plate, drawn a third larger than a man by the renderer.
 * The mesh between the markers is written by tools/gen_knight.py, which also paints the texture.
 */
public class RustKnightModel extends HumanoidModel<SoulsRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("rust_knight"), "main");

    private final ModelPart cape;

    public RustKnightModel(ModelPart root) {
        super(root);
        this.cape = this.body.getChild("cape");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        PartDefinition body = root.getChild("body");
        // <generated mesh>
        PartDefinition right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(1.9F, 12.0F, 0.0F));
        PartDefinition great_helm = head.addOrReplaceChild("great_helm", CubeListBuilder.create()
                .texOffs(0, 64).addBox(-4.5F, -8.7F, -4.5F, 9.0F, 9.0F, 9.0F)
                .texOffs(20, 82).addBox(-0.5F, -11.7F, -4.0F, 1.0F, 3.0F, 8.0F),
                PartPose.ZERO);
        PartDefinition breastplate = body.addOrReplaceChild("breastplate", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-4.5F, -0.3F, -2.5F, 9.0F, 8.0F, 5.0F)
                .texOffs(20, 93).addBox(-4.5F, 8.2F, -2.5F, 9.0F, 5.0F, 5.0F),
                PartPose.ZERO);
        PartDefinition cape = body.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(36, 64).addBox(-4.0F, 0.0F, 0.0F, 8.0F, 17.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.5F, 2.6F, 0.08F, 0.0F, 0.0F));
        PartDefinition right_pauldron = right_arm.addOrReplaceChild("right_pauldron", CubeListBuilder.create()
                .texOffs(38, 82).addBox(-4.0F, -3.2F, -3.0F, 5.0F, 4.0F, 6.0F)
                .texOffs(22, 103).addBox(-3.5F, 5.5F, -2.5F, 5.0F, 5.0F, 5.0F),
                PartPose.ZERO);
        PartDefinition left_pauldron = left_arm.addOrReplaceChild("left_pauldron", CubeListBuilder.create()
                .texOffs(0, 103).addBox(-1.0F, -3.2F, -3.0F, 5.0F, 4.0F, 6.0F)
                .texOffs(42, 103).addBox(-1.5F, 5.5F, -2.5F, 5.0F, 5.0F, 5.0F),
                PartPose.ZERO);
        PartDefinition right_greave = right_leg.addOrReplaceChild("right_greave", CubeListBuilder.create()
                .texOffs(28, 32).addBox(-2.5F, 5.5F, -2.5F, 5.0F, 7.0F, 5.0F),
                PartPose.ZERO);
        PartDefinition left_greave = left_leg.addOrReplaceChild("left_greave", CubeListBuilder.create()
                .texOffs(0, 82).addBox(-2.5F, 5.5F, -2.5F, 5.0F, 7.0F, 5.0F),
                PartPose.ZERO);
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 128);
    }

    @Override
    public void setupAnim(SoulsRenderState state) {
        super.setupAnim(state);
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        // The torn cape trails behind when he walks and stirs when he stands.
        this.cape.xRot += 0.08F + 0.35F * speed + Mth.sin(state.ageInTicks * 0.05F) * 0.03F;
        // At rest the greatsword is carried with its point up, ready, rather than dragged.
        if (state.armed) {
            this.rightArm.xRot -= 0.42F;
        }
        float t = state.attackTime;
        switch (state.attackId) {
            case RustKnightEntity.CHOP -> {
                // The greatsword goes up over the shoulder, hangs, and comes straight down.
                float raise = Anim.pulse(t, 0.0F, 12.0F, 14.5F, 17.5F);
                float strike = Anim.pulse(t, 14.5F, 17.5F, 22.0F, 33.0F);
                this.rightArm.xRot = Anim.lerp(raise, this.rightArm.xRot, -3.0F);
                this.rightArm.xRot = Anim.lerp(strike, this.rightArm.xRot, -0.55F);
                this.rightArm.zRot += 0.2F * raise;
                this.body.xRot += -0.18F * raise + 0.38F * strike;
                this.head.xRot += -0.15F * raise + 0.25F * strike;
                this.rightLeg.z += 3.5F * strike;
                this.leftLeg.z += 3.5F * strike;
            }
            case RustKnightEntity.SWEEP -> {
                // Drawn back to the right, then swept across to the left at chest height.
                float draw = Anim.pulse(t, 0.0F, 10.0F, 12.5F, 15.5F);
                float sweep = Anim.pulse(t, 12.5F, 15.5F, 21.0F, 31.0F);
                this.rightArm.xRot = Anim.lerp(Math.max(draw, sweep), this.rightArm.xRot, -1.45F);
                this.rightArm.yRot += -1.1F * draw + 1.0F * sweep;
                this.body.yRot += -0.55F * draw + 0.6F * sweep;
                this.leftArm.xRot += -0.3F * sweep;
            }
            case RustKnightEntity.LUNGE -> {
                // The sword comes up level beside the helmet, point at the target; then his whole weight goes in behind it.
                float aim = Anim.pulse(t, 0.0F, 8.0F, 24.0F, 31.0F);
                float thrust = Anim.pulse(t, 10.0F, 13.0F, 20.0F, 28.0F);
                this.rightArm.xRot = Anim.lerp(aim, this.rightArm.xRot, -0.08F);
                this.rightArm.zRot += 1.25F * aim;
                this.rightArm.z += 3.5F * aim - 9.5F * thrust;
                this.leftArm.z -= 4.0F * thrust;
                this.body.xRot += 0.15F * aim + 0.3F * thrust;
                this.body.yRot += -0.3F * aim * (1.0F - thrust);
                this.head.xRot -= 0.2F * thrust;
                this.rightLeg.z += (2.4F * aim + 3.6F * thrust);
                this.leftLeg.z += (2.4F * aim + 3.6F * thrust);
                this.leftLeg.xRot += 0.5F * thrust;
                this.rightLeg.xRot -= 0.5F * thrust;
            }
            default -> {
            }
        }
    }
}
