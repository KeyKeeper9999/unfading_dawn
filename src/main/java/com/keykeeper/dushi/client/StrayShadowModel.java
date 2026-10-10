package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.StrayShadowEntity;

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
 * The stray shadow. The mesh between the markers is written by tools/gen_dusk.py (which also paints the texture and
 * draws the poses below on a sheet); the animation is written by hand. Its rest is lying: 'figure' is hinged at the
 * feet and lies forward along the floor, its arms stretched on before it; to strike it swings up about that hinge.
 */
public class StrayShadowModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("stray_shadow"), "main");
    /** Must match the scale in the generator. */
    private static final float MODEL_SCALE = 1.2F;

    private final ModelPart all;
    private final ModelPart pool;
    private final ModelPart figure;
    private final ModelPart torso;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart rightFore;
    private final ModelPart leftArm;
    private final ModelPart leftFore;

    public StrayShadowModel(ModelPart root) {
        super(root);
        this.all = root.getChild("all");
        this.pool = this.all.getChild("pool");
        this.figure = this.all.getChild("figure");
        this.torso = this.figure.getChild("torso");
        this.head = this.torso.getChild("head");
        this.rightArm = this.torso.getChild("right_arm");
        this.rightFore = this.rightArm.getChild("right_fore");
        this.leftArm = this.torso.getChild("left_arm");
        this.leftFore = this.leftArm.getChild("left_fore");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition pool = all.addOrReplaceChild("pool", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-7.0F, -1.0F, -7.0F, 14.0F, 1.0F, 14.0F),
                PartPose.ZERO);
        PartDefinition figure = all.addOrReplaceChild("figure", CubeListBuilder.create()
                .texOffs(56, 0).addBox(-3.5F, -13.0F, -0.5F, 3.0F, 13.0F, 1.0F)
                .texOffs(56, 14).addBox(0.5F, -13.0F, -0.5F, 3.0F, 13.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 1.5F, 0.0F, 0.0F));
        PartDefinition torso = figure.addOrReplaceChild("torso", CubeListBuilder.create()
                .texOffs(0, 15).addBox(-4.5F, -12.0F, -0.5F, 9.0F, 12.0F, 1.0F),
                PartPose.offset(0.0F, -13.0F, 0.0F));
        PartDefinition head = torso.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(32, 26).addBox(-3.0F, -8.0F, -0.5F, 6.0F, 8.0F, 1.0F),
                PartPose.offset(0.0F, -12.0F, 0.0F));
        PartDefinition right_arm = torso.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(20, 15).addBox(-2.0F, 0.0F, -0.5F, 2.0F, 11.0F, 1.0F),
                PartPose.offsetAndRotation(-4.5F, -11.0F, 0.0F, 0.0F, 0.0F, 2.7F));
        PartDefinition right_fore = right_arm.addOrReplaceChild("right_fore", CubeListBuilder.create()
                .texOffs(32, 15).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 10.0F, 1.0F)
                .texOffs(44, 15).addBox(-2.5F, 9.0F, -0.5F, 5.0F, 7.0F, 1.0F),
                PartPose.offsetAndRotation(-1.0F, 11.0F, 0.0F, 0.0F, 0.0F, -0.3F));
        PartDefinition left_arm = torso.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(26, 15).addBox(0.0F, 0.0F, -0.5F, 2.0F, 11.0F, 1.0F),
                PartPose.offsetAndRotation(4.5F, -11.0F, 0.0F, 0.0F, 0.0F, -2.7F));
        PartDefinition left_fore = left_arm.addOrReplaceChild("left_fore", CubeListBuilder.create()
                .texOffs(38, 15).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 10.0F, 1.0F)
                .texOffs(20, 27).addBox(-2.5F, 9.0F, -0.5F, 5.0F, 7.0F, 1.0F),
                PartPose.offsetAndRotation(1.0F, 11.0F, 0.0F, 0.0F, 0.0F, 0.3F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 64).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float t = state.attackTime;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float step = state.walkAnimationPos * 1.4F;
        // on the floor it goes as a swimmer goes: one arm, then the other; and its edge is never still
        this.rightArm.zRot += Mth.sin(step) * 0.3F * speed + Mth.sin(age * 0.07F) * 0.04F;
        this.leftArm.zRot += Mth.sin(step) * 0.3F * speed + Mth.sin(age * 0.06F + 1.3F) * 0.04F;
        this.torso.zRot += Mth.sin(step) * 0.12F * speed;
        this.pool.yRot += age * 0.01F;
        switch (state.attackId) {
            case StrayShadowEntity.RISE -> {
                float up = Anim.pulse(t, 1.0F, StrayShadowEntity.RISE_UP, StrayShadowEntity.RISE_SINK, StrayShadowEntity.RISE_DURATION - 2.0F);
                float impact = StrayShadowEntity.RISE_IMPACT;
                float spread = Anim.pulse(t, 2.0F, StrayShadowEntity.RISE_UP, impact - 3.0F, impact);
                float claw = Anim.pulse(t, impact - 3.0F, impact + 1.0F, impact + 8.0F, StrayShadowEntity.RISE_SINK);
                Anim.pose(this.figure, up, -1.5F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, up, 0.0F, 0.0F, -1.4F);
                Anim.pose(this.leftArm, up, 0.0F, 0.0F, 1.4F);
                this.torso.xRot += -0.2F * spread + 0.45F * claw;
                Anim.pose(this.rightArm, claw, -1.5F, 0.0F, -2.4F);
                Anim.pose(this.leftArm, claw, -1.5F, 0.0F, 2.4F);
                this.rightFore.zRot += 0.3F * claw;
                this.leftFore.zRot -= 0.3F * claw;
            }
            case StrayShadowEntity.SNATCH -> {
                // the arms come together along the floor
                float impact = StrayShadowEntity.SNATCH_IMPACT;
                float wide = Anim.pulse(t, 0.0F, impact - 5.0F, impact - 3.0F, impact);
                float shut = Anim.pulse(t, impact - 3.0F, impact, impact + 8.0F, StrayShadowEntity.SNATCH_DURATION);
                this.rightArm.zRot += -0.7F * wide + 0.45F * shut;
                this.leftArm.zRot += 0.7F * wide - 0.45F * shut;
                this.figure.xRot -= 0.12F * shut;
            }
            default -> {
            }
        }
    }
}
