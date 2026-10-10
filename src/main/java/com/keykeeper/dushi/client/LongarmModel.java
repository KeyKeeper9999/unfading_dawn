package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.LongarmEntity;

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
 * The long-armed. The mesh between the markers is written by tools/gen_dusk.py (which also paints the texture and
 * draws the poses below on a sheet); the animation is written by hand. Its arms hang folded in three; to take hold
 * they go out straight, to rake they go out wide.
 */
public class LongarmModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("longarm"), "main");
    /** Must match the scale in the generator. */
    private static final float MODEL_SCALE = 1.5F;

    private final ModelPart all;
    private final ModelPart hump;
    private final ModelPart rightArm;
    private final ModelPart rightFore;
    private final ModelPart rightWrist;
    private final ModelPart rightHand;
    private final ModelPart leftArm;
    private final ModelPart leftFore;
    private final ModelPart leftWrist;
    private final ModelPart leftHand;
    private final ModelPart clingRight;
    private final ModelPart clingLeft;
    private final ModelPart clingTop;

    public LongarmModel(ModelPart root) {
        super(root);
        this.all = root.getChild("all");
        this.hump = this.all.getChild("hump");
        this.rightArm = this.hump.getChild("right_arm");
        this.rightFore = this.rightArm.getChild("right_fore");
        this.rightWrist = this.rightFore.getChild("right_wrist");
        this.rightHand = this.rightWrist.getChild("right_hand");
        this.leftArm = this.hump.getChild("left_arm");
        this.leftFore = this.leftArm.getChild("left_fore");
        this.leftWrist = this.leftFore.getChild("left_wrist");
        this.leftHand = this.leftWrist.getChild("left_hand");
        this.clingRight = this.hump.getChild("cling_r");
        this.clingLeft = this.hump.getChild("cling_l");
        this.clingTop = this.hump.getChild("cling_t");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition hump = all.addOrReplaceChild("hump", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6.0F, -13.0F, -3.0F, 12.0F, 13.0F, 8.0F)
                .texOffs(0, 21).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 12.0F, 5.0F),
                PartPose.offset(0.0F, -12.0F, 3.0F));
        PartDefinition right_arm = hump.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(40, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 13.0F, 3.0F),
                PartPose.offsetAndRotation(-5.0F, -10.0F, -3.0F, -0.9F, 0.0F, 0.12F));
        PartDefinition right_fore = right_arm.addOrReplaceChild("right_fore", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 13.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 13.0F, 0.0F, 2.25F, 0.0F, 0.0F));
        PartDefinition right_wrist = right_fore.addOrReplaceChild("right_wrist", CubeListBuilder.create()
                .texOffs(56, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 13.0F, 0.0F, -2.3F, 0.0F, 0.0F));
        PartDefinition right_hand = right_wrist.addOrReplaceChild("right_hand", CubeListBuilder.create()
                .texOffs(38, 31).addBox(-3.0F, 0.0F, -0.5F, 6.0F, 9.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.7F, 0.0F, 0.0F));
        PartDefinition left_arm = hump.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(52, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 13.0F, 3.0F),
                PartPose.offsetAndRotation(5.0F, -10.0F, -3.0F, -0.9F, 0.0F, -0.12F));
        PartDefinition left_fore = left_arm.addOrReplaceChild("left_fore", CubeListBuilder.create()
                .texOffs(48, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 13.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 13.0F, 0.0F, 2.25F, 0.0F, 0.0F));
        PartDefinition left_wrist = left_fore.addOrReplaceChild("left_wrist", CubeListBuilder.create()
                .texOffs(30, 21).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 13.0F, 0.0F, -2.3F, 0.0F, 0.0F));
        PartDefinition left_hand = left_wrist.addOrReplaceChild("left_hand", CubeListBuilder.create()
                .texOffs(0, 38).addBox(-3.0F, 0.0F, -0.5F, 6.0F, 9.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.7F, 0.0F, 0.0F));
        PartDefinition cling_r = hump.addOrReplaceChild("cling_r", CubeListBuilder.create()
                .texOffs(56, 30).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F)
                .texOffs(30, 35).addBox(-1.5F, 6.0F, -0.5F, 3.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(-6.5F, -7.0F, -3.0F, 0.0F, 0.0F, 0.5F));
        PartDefinition cling_l = hump.addOrReplaceChild("cling_l", CubeListBuilder.create()
                .texOffs(60, 30).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F)
                .texOffs(52, 37).addBox(-1.5F, 6.0F, -0.5F, 3.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(6.5F, -3.0F, -3.0F, 0.0F, 0.0F, -0.9F));
        PartDefinition cling_t = hump.addOrReplaceChild("cling_t", CubeListBuilder.create()
                .texOffs(52, 31).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F)
                .texOffs(14, 38).addBox(-1.5F, 5.0F, -0.5F, 3.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(2.0F, -13.0F, -3.0F, 0.0F, 0.0F, 2.6F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 64).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    private void reach(float amount, float yaw) {
        Anim.pose(this.rightArm, amount, -0.62F, yaw, -0.1F);
        Anim.pose(this.leftArm, amount, -0.62F, -yaw, 0.1F);
        Anim.pose(this.rightFore, amount, -2.2F, 0.0F, 0.0F);
        Anim.pose(this.leftFore, amount, -2.2F, 0.0F, 0.0F);
        Anim.pose(this.rightWrist, amount, 2.3F, 0.0F, 0.0F);
        Anim.pose(this.leftWrist, amount, 2.3F, 0.0F, 0.0F);
        Anim.pose(this.rightHand, amount, -0.9F, 0.0F, 0.0F);
        Anim.pose(this.leftHand, amount, -0.9F, 0.0F, 0.0F);
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float t = state.attackTime;
        // the hump rises and falls; the fingers work, one hand and then the other
        this.hump.y += Mth.sin(age * 0.05F) * 0.3F;
        this.rightHand.xRot += Math.max(0.0F, Mth.sin(age * 0.17F)) * 0.5F;
        this.leftHand.xRot += Math.max(0.0F, Mth.sin(age * 0.13F + 2.0F)) * 0.5F;
        this.leftWrist.xRot += Mth.sin(age * 0.06F) * 0.1F;
        this.clingRight.zRot += Mth.sin(age * 0.09F) * 0.08F;
        this.clingLeft.zRot += Mth.sin(age * 0.07F + 1.0F) * 0.08F;
        this.clingTop.zRot += Mth.sin(age * 0.08F + 2.0F) * 0.08F;
        switch (state.attackId) {
            case LongarmEntity.GRAB -> {
                float impact = LongarmEntity.GRAB_IMPACT;
                float drawn = Anim.pulse(t, 0.0F, impact - 7.0F, impact - 4.0F, impact);
                float out = Anim.pulse(t, impact - 4.0F, impact, impact + 6.0F, impact + 26.0F);
                float pull = Anim.pulse(t, impact + 2.0F, impact + 12.0F, impact + 20.0F, LongarmEntity.GRAB_DURATION);
                this.rightArm.xRot += -0.5F * drawn;
                this.leftArm.xRot += -0.5F * drawn;
                this.rightFore.xRot += 0.4F * drawn;
                this.leftFore.xRot += 0.4F * drawn;
                this.reach(out, 0.0F);
                // and back, with what it has
                this.rightFore.xRot += 1.2F * pull;
                this.leftFore.xRot += 1.2F * pull;
                this.rightWrist.xRot -= 1.2F * pull;
                this.leftWrist.xRot -= 1.2F * pull;
            }
            case LongarmEntity.RAKE -> {
                float impact = LongarmEntity.RAKE_IMPACT;
                float wide = Anim.pulse(t, 0.0F, impact - 6.0F, impact + 10.0F, LongarmEntity.RAKE_DURATION);
                float through = Anim.ramp(t, impact - 5.0F, impact + 3.0F);
                this.reach(wide, 0.9F - 1.6F * through);
            }
            default -> {
            }
        }
    }
}
