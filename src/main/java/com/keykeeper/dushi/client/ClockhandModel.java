package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.ClockhandEntity;

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
 * The clockhand. The mesh between the markers is written by tools/gen_clock.py (which also paints the texture and
 * draws the poses below on a sheet); the animation is written by hand. Nothing of it moves smoothly: what it does
 * between two of its seconds it does in a few ticks, and for the rest of the second it holds what it has come to.
 */
public class ClockhandModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("clockhand"), "main");
    /** Must match the scale in the generator. */
    private static final float MODEL_SCALE = 1.15F;

    private final ModelPart all;
    private final ModelPart body;
    private final ModelPart chest;
    private final ModelPart head;
    private final ModelPart key;
    private final ModelPart rightArm;
    private final ModelPart rightBlade;
    private final ModelPart leftArm;
    private final ModelPart leftBlade;
    private final ModelPart rightLeg;
    private final ModelPart rightShin;
    private final ModelPart leftLeg;
    private final ModelPart leftShin;

    public ClockhandModel(ModelPart root) {
        super(root);
        this.all = root.getChild("all");
        this.body = this.all.getChild("body");
        this.chest = this.body.getChild("chest");
        this.head = this.chest.getChild("head");
        this.key = this.head.getChild("key");
        this.rightArm = this.chest.getChild("right_arm");
        this.rightBlade = this.rightArm.getChild("right_blade");
        this.leftArm = this.chest.getChild("left_arm");
        this.leftBlade = this.leftArm.getChild("left_blade");
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
                .texOffs(20, 13).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F)
                .texOffs(36, 13).addBox(-1.0F, 1.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offset(-2.0F, -14.0F, 0.0F));
        PartDefinition right_shin = right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(24, 13).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F)
                .texOffs(8, 21).addBox(-1.0F, 6.0F, -3.0F, 2.0F, 1.0F, 4.0F),
                PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition left_leg = all.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(28, 13).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F)
                .texOffs(8, 14).addBox(-1.0F, 1.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offset(2.0F, -14.0F, 0.0F));
        PartDefinition left_shin = left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(32, 13).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F)
                .texOffs(20, 21).addBox(-1.0F, 6.0F, -3.0F, 2.0F, 1.0F, 4.0F),
                PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(44, 16).addBox(-2.0F, -3.0F, -1.5F, 4.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, -14.0F, 0.0F));
        PartDefinition chest = body.addOrReplaceChild("chest", CubeListBuilder.create()
                .texOffs(20, 0).addBox(-5.5F, -11.0F, -1.0F, 11.0F, 11.0F, 2.0F)
                .texOffs(46, 0).addBox(-3.0F, -8.5F, 1.0F, 6.0F, 6.0F, 2.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));
        PartDefinition head = chest.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 23).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(46, 8).addBox(-3.0F, -8.0F, -1.0F, 6.0F, 6.0F, 2.0F),
                PartPose.offset(0.0F, -11.0F, 0.0F));
        PartDefinition key = head.addOrReplaceChild("key", CubeListBuilder.create()
                .texOffs(4, 23).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(32, 22).addBox(-3.0F, -5.0F, -0.5F, 6.0F, 3.0F, 1.0F),
                PartPose.offset(0.0F, -8.0F, 0.0F));
        PartDefinition right_arm = chest.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(46, 22).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F)
                .texOffs(16, 14).addBox(-0.5F, 1.0F, -0.5F, 1.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(-6.0F, -8.5F, 0.0F, -0.25F, 0.0F, 0.3F));
        PartDefinition right_blade = right_arm.addOrReplaceChild("right_blade", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 22.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, -0.2F, 0.0F, 0.0F));
        PartDefinition left_arm = chest.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(54, 22).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F)
                .texOffs(58, 16).addBox(-0.5F, 1.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(6.0F, -8.5F, 0.0F, -0.2F, 0.0F, -0.3F));
        PartDefinition left_blade = left_arm.addOrReplaceChild("left_blade", CubeListBuilder.create()
                .texOffs(8, 0).addBox(-2.5F, 0.0F, -0.5F, 5.0F, 13.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, -0.15F, 0.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 64).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    /** Time that goes in steps: it stands for most of a second and is then at the next. */
    private static float ticked(float age, float second) {
        float whole = Mth.floor(age / second);
        float part = age / second - whole;
        return whole + Anim.ramp(part, 0.7F, 1.0F);
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float t = state.attackTime;
        float speed = Math.min(1.0F, state.walkAnimationSpeed * 2.0F);
        float step = state.walkAnimationPos * 1.1F;
        float second = ticked(age, ClockhandEntity.SECOND);
        // the key in its head goes round a twelfth of a turn a second; the head ticks over with it
        this.key.yRot += second * Mth.PI / 6.0F;
        this.head.zRot += (Mth.floor(second) % 2 == 0 ? 0.06F : -0.06F);
        this.rightLeg.xRot += Mth.sin(step) * 0.7F * speed;
        this.leftLeg.xRot -= Mth.sin(step) * 0.7F * speed;
        this.rightShin.xRot += Math.max(0.0F, -Mth.sin(step)) * 0.6F * speed;
        this.leftShin.xRot += Math.max(0.0F, Mth.sin(step)) * 0.6F * speed;
        switch (state.attackId) {
            case ClockhandEntity.STAB -> {
                float impact = ClockhandEntity.STAB_IMPACT;
                float back = Anim.pulse(t, 0.0F, 4.0F, impact - 3.0F, impact - 1.0F);
                float through = Anim.pulse(t, impact - 3.0F, impact, impact + 12.0F, ClockhandEntity.STAB_DURATION);
                Anim.pose(this.rightArm, back, 0.6F, 0.0F, 0.2F);
                Anim.pose(this.rightBlade, back, -1.4F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, through, -1.25F, 0.0F, -0.2F);
                Anim.pose(this.rightBlade, through, 0.15F, 0.0F, 0.0F);
                this.chest.yRot += 0.5F * back - 0.4F * through;
                this.chest.xRot += 0.15F * through;
                this.body.xRot += 0.2F * through;
            }
            case ClockhandEntity.SWEEP -> {
                float impact = ClockhandEntity.SWEEP_IMPACT;
                float back = Anim.pulse(t, 0.0F, 4.0F, impact - 3.0F, impact - 1.0F);
                float through = Anim.pulse(t, impact - 3.0F, impact + 1.0F, impact + 12.0F, ClockhandEntity.SWEEP_DURATION);
                Anim.pose(this.leftArm, Math.max(back, through), -1.3F, 0.0F, -0.9F + 1.5F * through);
                this.chest.yRot += -0.7F * back + 0.8F * through;
            }
            case ClockhandEntity.WIND -> {
                float wind = Anim.pulse(t, 0.0F, 6.0F, ClockhandEntity.WIND_DURATION - 6.0F, ClockhandEntity.WIND_DURATION);
                this.key.yRot += ticked(t, 4.0F) * 1.2F;
                this.chest.xRot += 0.25F * wind;
                Anim.pose(this.rightArm, wind, 0.3F, 0.0F, 0.5F);
                Anim.pose(this.leftArm, wind, 0.3F, 0.0F, -0.5F);
            }
            default -> {
            }
        }
    }
}
