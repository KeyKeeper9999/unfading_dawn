package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;

/**
 * The blind keykeeper sits on the floor with his legs stretched out, his head on his chest and a ring of keys at his belt.
 * The mesh between the markers is written by tools/gen_keykeeper.py, which also paints the texture.
 */
public class KeykeeperModel extends HumanoidModel<HumanoidRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("keykeeper"), "main");
    /** The seated model is lowered by this much so that he sits on the ground. */
    private static final float SIT_DROP = 10.0F;

    private final ModelPart keys;

    public KeykeeperModel(ModelPart root) {
        super(root);
        this.keys = this.body.getChild("key_ring");
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
        PartDefinition beard = head.addOrReplaceChild("beard", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-2.5F, -1.5F, -4.7F, 5.0F, 8.0F, 1.0F),
                PartPose.ZERO);
        PartDefinition pendant = body.addOrReplaceChild("pendant", CubeListBuilder.create()
                .texOffs(56, 16).addBox(-1.0F, 3.0F, -2.9F, 2.0F, 6.0F, 1.0F),
                PartPose.ZERO);
        PartDefinition key_ring = body.addOrReplaceChild("key_ring", CubeListBuilder.create()
                .texOffs(56, 29).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 3.0F, 1.0F)
                .texOffs(60, 23).addBox(-1.5F, 3.0F, -0.5F, 1.0F, 4.0F, 1.0F)
                .texOffs(56, 23).addBox(-0.3F, 3.0F, -0.5F, 1.0F, 5.0F, 1.0F)
                .texOffs(12, 32).addBox(0.9F, 3.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offset(3.2F, 9.5F, -2.3F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
        super.setupAnim(state);
        float breath = Mth.sin(state.ageInTicks * 0.045F);
        for (ModelPart part : new ModelPart[] {this.head, this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg}) {
            part.y += SIT_DROP;
        }
        // Legs straight out in front, a little apart.
        this.rightLeg.xRot = -1.5F;
        this.leftLeg.xRot = -1.45F;
        this.rightLeg.yRot = 0.22F;
        this.leftLeg.yRot = -0.18F;
        // Slumped: the back rounds forward, the head hangs but still turns to whoever comes near.
        this.body.xRot = 0.16F + breath * 0.012F;
        this.head.xRot = this.head.xRot * 0.5F + 0.42F;
        this.head.z -= 1.6F;
        this.head.y += 0.3F;
        // Hands in the lap.
        this.rightArm.xRot = -0.85F + breath * 0.015F;
        this.leftArm.xRot = -0.8F - breath * 0.015F;
        this.rightArm.zRot = -0.28F;
        this.leftArm.zRot = 0.28F;
        this.rightArm.z -= 1.2F;
        this.leftArm.z -= 1.2F;
        this.keys.zRot = Mth.sin(state.ageInTicks * 0.09F) * 0.06F;
    }
}
