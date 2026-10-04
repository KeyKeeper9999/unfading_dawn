package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.WitheredEntity;

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
 * The withered: an ordinary humanoid with thin arms and a stoop, plus the things each kind wears (shown only on that kind).
 * The mesh between the markers is written by tools/gen_withered.py, which also paints the textures.
 */
public class WitheredModel extends HumanoidModel<SoulsRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("withered"), "main");
    /** How far the body leans forward. */
    private static final float STOOP = 0.24F;

    public WitheredModel(ModelPart root, WitheredEntity.Kind kind) {
        super(root);
        this.body.getChild("collar").visible = kind == WitheredEntity.Kind.PRISONER;
        this.leftArm.getChild("shackle").visible = kind == WitheredEntity.Kind.PRISONER;
        this.head.getChild("kettle_hat").visible = kind == WitheredEntity.Kind.GUARD;
        this.rightArm.getChild("pauldron").visible = kind == WitheredEntity.Kind.GUARD;
        this.head.getChild("hood_tail").visible = kind == WitheredEntity.Kind.ARCHER;
        this.body.getChild("quiver").visible = kind == WitheredEntity.Kind.ARCHER;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        PartDefinition body = root.getChild("body");
        // <generated mesh>
        PartDefinition right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(1.9F, 12.0F, 0.0F));
        PartDefinition collar = body.addOrReplaceChild("collar", CubeListBuilder.create()
                .texOffs(36, 73).addBox(-4.5F, -0.6F, -2.5F, 9.0F, 2.0F, 5.0F),
                PartPose.ZERO);
        PartDefinition shackle = left_arm.addOrReplaceChild("shackle", CubeListBuilder.create()
                .texOffs(46, 45).addBox(-2.0F, 0.0F, -2.5F, 4.0F, 2.0F, 5.0F)
                .texOffs(0, 45).addBox(-0.5F, 2.0F, -3.3F, 1.0F, 6.0F, 1.0F),
                PartPose.offset(0.5F, 7.0F, 0.0F));
        PartDefinition kettle_hat = head.addOrReplaceChild("kettle_hat", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-6.0F, -6.6F, -6.0F, 12.0F, 1.0F, 12.0F)
                .texOffs(0, 64).addBox(-4.5F, -9.6F, -4.5F, 9.0F, 3.0F, 9.0F),
                PartPose.ZERO);
        PartDefinition pauldron = right_arm.addOrReplaceChild("pauldron", CubeListBuilder.create()
                .texOffs(36, 64).addBox(-3.0F, -3.0F, -3.0F, 4.0F, 3.0F, 6.0F),
                PartPose.ZERO);
        PartDefinition hood_tail = head.addOrReplaceChild("hood_tail", CubeListBuilder.create()
                .texOffs(48, 32).addBox(-2.0F, -1.0F, -1.0F, 4.0F, 4.0F, 3.0F)
                .texOffs(54, 27).addBox(-1.0F, 3.0F, 0.5F, 2.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -6.5F, 4.0F, 0.45F, 0.0F, 0.0F));
        PartDefinition quiver = body.addOrReplaceChild("quiver", CubeListBuilder.create()
                .texOffs(54, 16).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 9.0F, 2.0F)
                .texOffs(48, 39).addBox(-1.0F, -2.0F, 0.5F, 2.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(2.2F, 1.0F, 2.0F, 0.0F, 0.0F, 0.32F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 96);
    }

    @Override
    public void setupAnim(SoulsRenderState state) {
        super.setupAnim(state);
        // The stoop: the trunk leans forward from the hips, the head hangs, the arms dangle in front.
        this.body.xRot += STOOP;
        this.rightLeg.z += 2.9F;
        this.leftLeg.z += 2.9F;
        this.rightLeg.y -= 0.4F;
        this.leftLeg.y -= 0.4F;
        this.head.xRot += 0.18F;
        float sway = Mth.sin(state.ageInTicks * 0.07F) * 0.05F;
        this.head.zRot += sway;
        boolean busy = state.isUsingItem;
        if (!busy) {
            this.rightArm.xRot -= 0.22F;
            this.leftArm.xRot -= 0.22F;
            this.rightArm.zRot += 0.06F + sway;
            this.leftArm.zRot -= 0.06F + sway;
        }

        if (state.attackId == WitheredEntity.SWING) {
            float t = state.attackTime;
            // The arm goes up and back, hangs for a moment, comes down hard and returns.
            float raise = Anim.pulse(t, 0.0F, 7.0F, 8.5F, 10.5F);
            float strike = Anim.pulse(t, 8.5F, 10.5F, 13.0F, 19.0F);
            this.rightArm.xRot = Anim.lerp(raise, this.rightArm.xRot, -2.75F);
            this.rightArm.xRot = Anim.lerp(strike, this.rightArm.xRot, -0.75F);
            this.rightArm.zRot += 0.35F * raise - 0.2F * strike;
            this.body.yRot += 0.35F * raise - 0.45F * strike;
            this.body.xRot += -0.12F * raise + 0.3F * strike;
            this.head.xRot += -0.25F * raise + 0.2F * strike;
            if (!state.armed) {
                // Bare hands: both arms claw at once.
                this.leftArm.xRot = Anim.lerp(raise, this.leftArm.xRot, -2.4F);
                this.leftArm.xRot = Anim.lerp(strike, this.leftArm.xRot, -0.9F);
                this.leftArm.zRot -= 0.3F * raise;
            }
        }
    }
}
