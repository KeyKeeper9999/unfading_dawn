package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.UsherEntity;

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
 * The usher of the Sky Beneath. The mesh between the markers is written by tools/gen_dusk.py (which also paints the
 * texture and draws the poses below on a sheet); the animation is written by hand. It walks stiffly and without haste.
 * To take hold the hands over its face open like shutters, outwards, and the one over its crown goes back.
 */
public class UsherModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("usher"), "main");
    /** Must match the scale in the generator. */
    private static final float MODEL_SCALE = 1.4F;

    private final ModelPart all;
    private final ModelPart body;
    private final ModelPart chest;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart palmRight;
    private final ModelPart palmLeft;
    private final ModelPart crown;
    private final ModelPart rightArm;
    private final ModelPart rightFore;
    private final ModelPart rightHand;
    private final ModelPart leftArm;
    private final ModelPart leftFore;
    private final ModelPart leftHand;
    private final ModelPart rightLeg;
    private final ModelPart rightShin;
    private final ModelPart leftLeg;
    private final ModelPart leftShin;

    public UsherModel(ModelPart root) {
        super(root);
        this.all = root.getChild("all");
        this.body = this.all.getChild("body");
        this.chest = this.body.getChild("chest");
        this.neck = this.chest.getChild("neck");
        this.head = this.neck.getChild("head");
        this.palmRight = this.head.getChild("palm_r");
        this.palmLeft = this.head.getChild("palm_l");
        this.crown = this.head.getChild("crown");
        this.rightArm = this.chest.getChild("right_arm");
        this.rightFore = this.rightArm.getChild("right_fore");
        this.rightHand = this.rightFore.getChild("right_hand");
        this.leftArm = this.chest.getChild("left_arm");
        this.leftFore = this.leftArm.getChild("left_fore");
        this.leftHand = this.leftFore.getChild("left_hand");
        this.rightLeg = this.all.getChild("right_leg");
        this.rightShin = this.rightLeg.getChild("right_shin");
        this.leftLeg = this.all.getChild("left_leg");
        this.leftShin = this.leftLeg.getChild("left_shin");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition right_leg = all.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(54, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 9.0F, 2.0F),
                PartPose.offset(-1.5F, -18.0F, 0.0F));
        PartDefinition right_shin = right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 9.0F, 2.0F)
                .texOffs(54, 11).addBox(-1.0F, 8.0F, -3.0F, 2.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, 9.0F, 0.0F));
        PartDefinition left_leg = all.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(8, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 9.0F, 2.0F),
                PartPose.offset(1.5F, -18.0F, 0.0F));
        PartDefinition left_shin = left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(40, 25).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 9.0F, 2.0F)
                .texOffs(32, 33).addBox(-1.0F, 8.0F, -3.0F, 2.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, 9.0F, 0.0F));
        PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(48, 34).addBox(-2.5F, -3.0F, -1.5F, 5.0F, 3.0F, 3.0F)
                .texOffs(0, 0).addBox(-3.5F, -3.0F, -2.0F, 7.0F, 12.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -18.0F, 0.0F, 0.1F, 0.0F, 0.0F));
        PartDefinition chest = body.addOrReplaceChild("chest", CubeListBuilder.create()
                .texOffs(22, 14).addBox(-3.0F, -10.0F, -1.5F, 6.0F, 10.0F, 3.0F)
                .texOffs(22, 36).addBox(-4.0F, -10.5F, -2.0F, 8.0F, 2.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.12F, 0.0F, 0.0F));
        PartDefinition neck = chest.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(32, 27).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -0.1F, 0.0F, 0.0F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(40, 14).addBox(-2.5F, -6.0F, -2.5F, 5.0F, 6.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.05F, 0.0F, 0.0F));
        PartDefinition palm_r = head.addOrReplaceChild("palm_r", CubeListBuilder.create()
                .texOffs(12, 27).addBox(0.0F, -5.0F, -0.5F, 4.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(-3.0F, -2.5F, -3.2F, 0.0F, 0.12F, 0.0F));
        PartDefinition palm_l = head.addOrReplaceChild("palm_l", CubeListBuilder.create()
                .texOffs(22, 27).addBox(-4.0F, -5.0F, -0.5F, 4.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(3.0F, -2.5F, -3.2F, 0.0F, -0.12F, 0.0F));
        PartDefinition crown = head.addOrReplaceChild("crown", CubeListBuilder.create()
                .texOffs(0, 36).addBox(-2.0F, -0.5F, -7.0F, 4.0F, 1.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, -6.3F, 2.5F, 0.15F, 0.0F, 0.0F));
        PartDefinition right_arm = chest.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(22, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F),
                PartPose.offsetAndRotation(-4.2F, -9.0F, 0.0F, 0.0F, 0.0F, 0.06F));
        PartDefinition right_fore = right_arm.addOrReplaceChild("right_fore", CubeListBuilder.create()
                .texOffs(30, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, -0.12F, 0.0F, 0.0F));
        PartDefinition right_hand = right_fore.addOrReplaceChild("right_hand", CubeListBuilder.create()
                .texOffs(48, 25).addBox(-2.5F, 0.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        PartDefinition left_arm = chest.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(38, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F),
                PartPose.offsetAndRotation(4.2F, -9.0F, 0.0F, 0.0F, 0.0F, -0.06F));
        PartDefinition left_fore = left_arm.addOrReplaceChild("left_fore", CubeListBuilder.create()
                .texOffs(46, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, -0.12F, 0.0F, 0.0F));
        PartDefinition left_hand = left_fore.addOrReplaceChild("left_hand", CubeListBuilder.create()
                .texOffs(0, 27).addBox(-2.5F, 0.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 64).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float t = state.attackTime;
        float speed = Math.min(1.0F, state.walkAnimationSpeed * 1.6F);
        float step = state.walkAnimationPos * 0.9F;
        // it hardly breathes; the fingers over its face are never quite still
        this.chest.xRot += Mth.sin(age * 0.04F) * 0.02F;
        this.palmRight.yRot += Mth.sin(age * 0.13F) * 0.05F;
        this.palmLeft.yRot -= Mth.sin(age * 0.11F + 1.0F) * 0.05F;
        this.rightHand.xRot += Mth.sin(age * 0.09F) * 0.08F;
        this.leftHand.xRot += Mth.sin(age * 0.08F + 2.0F) * 0.08F;
        // a long stride, the knee hardly bent, the arms hardly swung
        this.rightLeg.xRot += Mth.sin(step) * 0.5F * speed;
        this.leftLeg.xRot -= Mth.sin(step) * 0.5F * speed;
        this.rightShin.xRot += Math.max(0.0F, -Mth.sin(step - 0.6F)) * 0.5F * speed;
        this.leftShin.xRot += Math.max(0.0F, Mth.sin(step - 0.6F)) * 0.5F * speed;
        this.rightArm.xRot -= Mth.sin(step) * 0.2F * speed;
        this.leftArm.xRot += Mth.sin(step) * 0.2F * speed;
        switch (state.attackId) {
            case UsherEntity.GRASP -> {
                float open = Anim.pulse(t, 2.0F, UsherEntity.GRASP_OPEN, UsherEntity.GRASP_THROW + 8.0F, UsherEntity.GRASP_DURATION);
                float out = Anim.pulse(t, UsherEntity.GRASP_OPEN - 6.0F, UsherEntity.GRASP_IMPACT, UsherEntity.GRASP_IMPACT + 2.0F, UsherEntity.GRASP_IMPACT + 9.0F);
                float lift = Anim.pulse(t, UsherEntity.GRASP_IMPACT + 1.0F, UsherEntity.GRASP_IMPACT + 9.0F, UsherEntity.GRASP_THROW, UsherEntity.GRASP_THROW + 10.0F);
                Anim.pose(this.palmRight, open, 0.0F, 1.9F, 0.0F);
                Anim.pose(this.palmLeft, open, 0.0F, -1.9F, 0.0F);
                Anim.pose(this.crown, open, -1.3F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, out, -1.2F, 0.0F, 0.25F);
                Anim.pose(this.leftArm, out, -1.2F, 0.0F, -0.25F);
                Anim.pose(this.rightFore, out, -0.5F, 0.0F, 0.0F);
                Anim.pose(this.leftFore, out, -0.5F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, lift, -2.7F, 0.0F, 0.2F);
                Anim.pose(this.leftArm, lift, -2.7F, 0.0F, -0.2F);
                this.chest.xRot += 0.25F * out - 0.15F * lift;
            }
            case UsherEntity.REACH -> {
                float impact = UsherEntity.REACH_IMPACT;
                float up = Anim.pulse(t, 0.0F, 14.0F, impact - 5.0F, impact - 1.0F);
                float down = Anim.pulse(t, impact - 5.0F, impact, impact + 14.0F, UsherEntity.REACH_DURATION);
                Anim.pose(this.rightArm, up, -3.0F, 0.0F, 0.1F);
                Anim.pose(this.rightFore, up, 0.3F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, down, -1.0F, 0.0F, 0.1F);
                Anim.pose(this.rightFore, down, -0.1F, 0.0F, 0.0F);
                this.chest.xRot += -0.1F * up + 0.5F * down;
                this.chest.yRot += 0.3F * up - 0.3F * down;
                this.body.xRot += 0.25F * down;
            }
            default -> {
            }
        }
    }
}
