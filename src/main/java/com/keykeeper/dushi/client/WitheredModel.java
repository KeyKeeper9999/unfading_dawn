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
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 64);
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
