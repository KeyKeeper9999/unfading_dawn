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
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 96);
    }

    @Override
    public void setupAnim(SoulsRenderState state) {
        super.setupAnim(state);
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        // The torn cape trails behind when he walks and stirs when he stands.
        this.cape.xRot += 0.08F + 0.35F * speed + Mth.sin(state.ageInTicks * 0.05F) * 0.03F;
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
                // Crouches behind the point, then throws his whole weight forward.
                float aim = Anim.pulse(t, 0.0F, 8.0F, 24.0F, 31.0F);
                float thrust = Anim.pulse(t, 10.0F, 13.0F, 20.0F, 28.0F);
                this.rightArm.xRot = Anim.lerp(aim, this.rightArm.xRot, -1.2F - 0.45F * thrust);
                this.rightArm.yRot += -0.25F * aim * (1.0F - thrust);
                this.body.xRot += 0.2F * aim + 0.3F * thrust;
                this.head.xRot -= 0.3F * thrust;
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
