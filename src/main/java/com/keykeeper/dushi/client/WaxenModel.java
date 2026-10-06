package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.WaxenEntity;

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
 * The waxen. The mesh between the markers is written by tools/gen_waxen.py (which also paints the textures); the
 * animation is written by hand. It has no legs: the foot of it stays on the ground and the rest leans the way it
 * creeps, heaving a little, like something thick being poured.
 */
public class WaxenModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("waxen"), "main");
    /** Must match SCALE in tools/gen_waxen.py. */
    private static final float MODEL_SCALE = 1.3F;
    private static final float DEG = (float) (Math.PI / 180.0);

    private final ModelPart foot;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightForearm;
    private final ModelPart leftForearm;

    public WaxenModel(ModelPart root) {
        super(root);
        ModelPart all = root.getChild("all");
        this.foot = all.getChild("foot");
        this.body = all.getChild("body");
        this.head = this.body.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition foot = all.addOrReplaceChild("foot", CubeListBuilder.create()
                .texOffs(48, 0).addBox(-9.0F, -2.0F, -9.0F, 18.0F, 2.0F, 18.0F)
                .texOffs(48, 20).addBox(-7.5F, -7.0F, -7.5F, 15.0F, 5.0F, 15.0F),
                PartPose.ZERO);
        PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6.0F, -10.0F, -6.0F, 12.0F, 10.0F, 12.0F),
                PartPose.offset(0.0F, -7.0F, 0.0F));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(16, 22).addBox(-4.0F, -7.0F, -4.0F, 8.0F, 7.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 0.12F, 0.0F, 0.0F));
        PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(32, 37).addBox(-3.0F, 0.0F, -1.0F, 6.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, -2.5F, -4.0F));
        PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(108, 20).addBox(-4.0F, -1.5F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offsetAndRotation(-6.0F, -8.0F, 0.0F, 0.0F, 0.0F, 0.36F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(108, 36).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 9.0F, 4.0F)
                .texOffs(124, 6).addBox(-1.5F, 9.0F, -1.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(124, 18).addBox(0.5F, 9.0F, 0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(-2.0F, 10.5F, 0.0F, -0.3F, 0.0F, -0.2F));
        PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(0, 22).addBox(0.0F, -1.5F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offsetAndRotation(6.0F, -8.0F, 0.0F, 0.0F, 0.0F, -0.36F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(16, 37).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 9.0F, 4.0F)
                .texOffs(124, 10).addBox(0.5F, 9.0F, -1.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(124, 21).addBox(-1.5F, 9.0F, 0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(2.0F, 10.5F, 0.0F, -0.3F, 0.0F, 0.2F));
        PartDefinition head_candles = head.addOrReplaceChild("head_candles", CubeListBuilder.create()
                .texOffs(120, 11).addBox(-2.5F, -10.0F, -1.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(124, 24).addBox(-2.5F, -12.0F, -1.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(120, 0).addBox(1.5F, -12.0F, 0.5F, 1.0F, 5.0F, 1.0F)
                .texOffs(124, 27).addBox(1.5F, -14.0F, 0.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(124, 30).addBox(-0.5F, -9.0F, 2.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(124, 33).addBox(-0.5F, -11.0F, 2.0F, 1.0F, 2.0F, 1.0F),
                PartPose.ZERO);
        PartDefinition body_candles = body.addOrReplaceChild("body_candles", CubeListBuilder.create()
                .texOffs(120, 6).addBox(-6.0F, -14.0F, -3.0F, 1.0F, 4.0F, 1.0F)
                .texOffs(124, 36).addBox(-6.0F, -16.0F, -3.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(0, 38).addBox(-5.0F, -12.0F, 2.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(4, 38).addBox(-5.0F, -14.0F, 2.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(124, 14).addBox(4.0F, -13.0F, -2.0F, 1.0F, 3.0F, 1.0F)
                .texOffs(8, 38).addBox(4.0F, -15.0F, -2.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(124, 0).addBox(5.0F, -15.0F, 3.0F, 1.0F, 5.0F, 1.0F)
                .texOffs(12, 38).addBox(5.0F, -17.0F, 3.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(120, 15).addBox(-2.0F, -13.0F, 4.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(124, 39).addBox(-2.0F, -15.0F, 4.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(48, 40).addBox(1.5F, -12.0F, 5.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(52, 40).addBox(1.5F, -14.0F, 5.0F, 1.0F, 2.0F, 1.0F),
                PartPose.ZERO);
        // </generated mesh>
        return LayerDefinition.create(mesh, 128, 64).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float walk = state.walkAnimationPos * 0.5F;
        float t = state.attackTime;

        // It heaves: the body swells and settles, the foot spreads as the body settles; the head hangs.
        float heave = Mth.sin(age * 0.08F);
        this.body.yScale = 1.0F + heave * 0.03F;
        this.foot.xScale = 1.0F - heave * 0.02F;
        this.foot.zScale = 1.0F - heave * 0.02F;
        this.head.yRot += Mth.clamp(state.yRot, -50.0F, 50.0F) * DEG;
        this.head.xRot += Mth.clamp(state.xRot, -30.0F, 30.0F) * DEG * 0.5F + Mth.sin(age * 0.05F) * 0.04F;
        this.jaw.xRot += 0.12F + Mth.sin(age * 0.11F) * 0.08F;
        // creeping: it leans the way it goes and hauls itself with one arm, then the other
        float haul = Mth.sin(walk);
        Anim.pose(this.body, speed, 0.25F, haul * 0.1F, 0.0F);
        this.rightArm.xRot += -haul * 0.4F * speed;
        this.leftArm.xRot += haul * 0.4F * speed;

        switch (state.attackId) {
            case WaxenEntity.SLAM -> {
                // Both arms go up over it, the whole of it stretching after them - and come down together.
                float raise = Anim.pulse(t, 0.0F, 14.0F, 19.0F, 22.0F);
                float fall = Anim.pulse(t, 19.0F, 22.0F, 30.0F, 42.0F);
                this.body.yScale += 0.12F * raise - 0.1F * fall;
                Anim.pose(this.body, raise, -0.25F, 0.0F, 0.0F);
                Anim.pose(this.head, raise, -0.3F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, raise, -2.6F, 0.0F, 0.2F);
                Anim.pose(this.leftArm, raise, -2.6F, 0.0F, -0.2F);
                Anim.pose(this.rightForearm, Math.max(raise, fall), 0.5F - 0.2F * fall, 0.0F, 0.2F);
                Anim.pose(this.leftForearm, Math.max(raise, fall), 0.5F - 0.2F * fall, 0.0F, -0.2F);
                Anim.pose(this.body, fall, 0.55F, 0.0F, 0.0F);
                Anim.pose(this.head, fall, 0.2F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, fall, -1.2F, 0.0F, -0.1F);
                Anim.pose(this.leftArm, fall, -1.2F, 0.0F, 0.1F);
            }
            case WaxenEntity.SPEW -> {
                // It draws itself up, the head going back and the jaw falling - and what is in it comes out in a long hot rope.
                float fill = Anim.pulse(t, 0.0F, 12.0F, WaxenEntity.SPEW_FROM - 2.0F, WaxenEntity.SPEW_FROM);
                float out = Anim.pulse(t, WaxenEntity.SPEW_FROM - 2.0F, WaxenEntity.SPEW_FROM, WaxenEntity.SPEW_UNTIL, WaxenEntity.SPEW_UNTIL + 10.0F);
                this.body.yScale += 0.1F * fill - 0.06F * out;
                Anim.pose(this.body, fill, -0.15F, 0.0F, 0.0F);
                Anim.pose(this.head, fill, -0.35F, 0.0F, 0.0F);
                Anim.pose(this.body, out, 0.3F, 0.0F, 0.0F);
                Anim.pose(this.head, out, 0.1F + Mth.sin(age * 1.1F) * 0.05F, 0.0F, 0.0F);
                this.jaw.xRot += 0.9F * Math.max(fill * 0.5F, out);
                Anim.pose(this.rightArm, Math.max(fill, out), 0.4F, 0.0F, 0.3F);
                Anim.pose(this.leftArm, Math.max(fill, out), 0.4F, 0.0F, -0.3F);
            }
            default -> {
            }
        }
    }
}
