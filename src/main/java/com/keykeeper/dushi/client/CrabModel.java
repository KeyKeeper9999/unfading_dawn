package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.SandCrabEntity;

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
 * The sand crab of the Long Spit. The mesh between the markers is written by tools/gen_crab.py (which also paints the
 * texture and draws the poses below on a sheet); the animation is written by hand. Buried (phase 1) it is down in the
 * sand to its eyes, its legs drawn in; up, it carries the great claw before it and runs on six legs, three and three.
 */
public class CrabModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("sand_crab"), "main");
    /** Must match SCALE in tools/gen_crab.py. */
    private static final float MODEL_SCALE = 1.5F;
    /** How far down it lies when it is buried. */
    private static final float BURIED = 7.0F;

    private final ModelPart all;
    private final ModelPart body;
    private final ModelPart eyeRight;
    private final ModelPart eyeLeft;
    private final ModelPart clawRight;
    private final ModelPart handRight;
    private final ModelPart thumbRight;
    private final ModelPart clawLeft;
    private final ModelPart[] legsRight = new ModelPart[3];
    private final ModelPart[] legsLeft = new ModelPart[3];

    public CrabModel(ModelPart root) {
        super(root);
        this.all = root.getChild("all");
        this.body = this.all.getChild("body");
        this.eyeRight = this.body.getChild("eye_r");
        this.eyeLeft = this.body.getChild("eye_l");
        this.clawRight = this.body.getChild("claw_r");
        this.handRight = this.clawRight.getChild("hand_r");
        this.thumbRight = this.handRight.getChild("thumb_r");
        this.clawLeft = this.body.getChild("claw_l");
        for (int i = 0; i < 3; i++) {
            this.legsRight[i] = this.body.getChild("leg_r" + (i + 1));
            this.legsLeft[i] = this.body.getChild("leg_l" + (i + 1));
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 20.0F, 0.0F));
        PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5.0F, -3.0F, -4.0F, 10.0F, 4.0F, 8.0F)
                .texOffs(36, 10).addBox(-4.0F, -4.0F, -3.0F, 8.0F, 1.0F, 6.0F),
                PartPose.ZERO);
        PartDefinition eye_r = body.addOrReplaceChild("eye_r", CubeListBuilder.create()
                .texOffs(60, 17).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(54, 6).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(-2.0F, -3.0F, -3.5F));
        PartDefinition eye_l = body.addOrReplaceChild("eye_l", CubeListBuilder.create()
                .texOffs(0, 18).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(28, 12).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(2.0F, -3.0F, -3.5F));
        PartDefinition claw_r = body.addOrReplaceChild("claw_r", CubeListBuilder.create()
                .texOffs(48, 17).addBox(-4.0F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-5.0F, -1.0F, -2.5F, 0.0F, -0.5F, 0.0F));
        PartDefinition hand_r = claw_r.addOrReplaceChild("hand_r", CubeListBuilder.create()
                .texOffs(36, 0).addBox(-1.5F, -2.0F, -6.0F, 3.0F, 4.0F, 6.0F)
                .texOffs(28, 17).addBox(-1.0F, 0.5F, -9.0F, 2.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, 0.0F, -0.6F, 0.0F));
        PartDefinition thumb_r = hand_r.addOrReplaceChild("thumb_r", CubeListBuilder.create()
                .texOffs(0, 12).addBox(-1.0F, -1.0F, -3.5F, 2.0F, 2.0F, 4.0F),
                PartPose.offset(0.0F, -1.5F, -5.5F));
        PartDefinition claw_l = body.addOrReplaceChild("claw_l", CubeListBuilder.create()
                .texOffs(20, 18).addBox(0.0F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(5.0F, -1.0F, -2.5F, 0.0F, 0.5F, 0.0F));
        PartDefinition hand_l = claw_l.addOrReplaceChild("hand_l", CubeListBuilder.create()
                .texOffs(38, 17).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 2.0F, 3.0F)
                .texOffs(4, 18).addBox(-0.5F, -0.5F, -5.0F, 1.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.0F, 0.6F, 0.0F));
        PartDefinition leg_r1 = body.addOrReplaceChild("leg_r1", CubeListBuilder.create()
                .texOffs(10, 18).addBox(-4.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F)
                .texOffs(54, 0).addBox(-5.0F, -0.5F, -0.5F, 1.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(-5.0F, 0.0F, -1.5F, 0.0F, -0.35F, 0.0F));
        PartDefinition leg_l1 = body.addOrReplaceChild("leg_l1", CubeListBuilder.create()
                .texOffs(10, 20).addBox(0.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F)
                .texOffs(58, 0).addBox(4.0F, -0.5F, -0.5F, 1.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(5.0F, 0.0F, -1.5F, 0.0F, 0.35F, 0.0F));
        PartDefinition leg_r2 = body.addOrReplaceChild("leg_r2", CubeListBuilder.create()
                .texOffs(48, 21).addBox(-4.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F)
                .texOffs(12, 12).addBox(-5.0F, -0.5F, -0.5F, 1.0F, 5.0F, 1.0F),
                PartPose.offset(-5.0F, 0.0F, 1.0F));
        PartDefinition leg_l2 = body.addOrReplaceChild("leg_l2", CubeListBuilder.create()
                .texOffs(0, 22).addBox(0.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F)
                .texOffs(16, 12).addBox(4.0F, -0.5F, -0.5F, 1.0F, 5.0F, 1.0F),
                PartPose.offset(5.0F, 0.0F, 1.0F));
        PartDefinition leg_r3 = body.addOrReplaceChild("leg_r3", CubeListBuilder.create()
                .texOffs(10, 22).addBox(-4.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F)
                .texOffs(20, 12).addBox(-5.0F, -0.5F, -0.5F, 1.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(-5.0F, 0.0F, 3.5F, 0.0F, 0.35F, 0.0F));
        PartDefinition leg_l3 = body.addOrReplaceChild("leg_l3", CubeListBuilder.create()
                .texOffs(20, 22).addBox(0.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F)
                .texOffs(24, 12).addBox(4.0F, -0.5F, -0.5F, 1.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(5.0F, 0.0F, 3.5F, 0.0F, -0.35F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 32).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float t = state.attackTime;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float run = state.walkAnimationPos * 1.6F;
        // How deep it lies: all the way while it waits; coming up is its first move.
        float down = state.phase < 2 ? 1.0F : state.attackId == SandCrabEntity.EMERGE ? 1.0F - Anim.ramp(t, 1.0F, 9.0F) : 0.0F;
        this.eyeRight.zRot += Mth.sin(age * 0.13F) * 0.12F;
        this.eyeLeft.zRot += Mth.sin(age * 0.11F + 2.0F) * 0.12F;
        // the legs: three and three, like oars out of time
        for (int i = 0; i < 3; i++) {
            float beat = Mth.sin(run + i * 2.1F) * 0.45F * speed;
            this.legsRight[i].zRot += beat - 0.6F * down;
            this.legsLeft[i].zRot += beat + 0.6F * down;
            this.legsRight[i].yRot += Mth.cos(run + i * 2.1F) * 0.25F * speed;
            this.legsLeft[i].yRot += Mth.cos(run + i * 2.1F) * 0.25F * speed;
        }
        this.body.y += Math.abs(Mth.sin(run)) * 0.5F * speed;
        // up, the great claw is carried high and open a little; buried, both are folded in under the shell
        Anim.pose(this.clawRight, 1.0F - down, 0.0F, 0.0F, 0.5F + Mth.sin(age * 0.08F) * 0.06F);
        Anim.pose(this.handRight, 1.0F - down, 0.0F, 0.2F, 0.0F);
        this.thumbRight.xRot -= (0.25F + Mth.sin(age * 0.17F) * 0.12F) * (1.0F - down);
        Anim.pose(this.clawRight, down, 0.0F, 0.9F, -0.2F);
        Anim.pose(this.handRight, down, 0.0F, 0.9F, 0.0F);
        Anim.pose(this.clawLeft, down, 0.0F, -0.8F, 0.0F);
        this.all.y += BURIED * down;
        if (state.attackId == SandCrabEntity.EMERGE) {
            this.all.xRot += Mth.sin(t * 2.2F) * 0.12F * Anim.pulse(t, 0.0F, 3.0F, 8.0F, 14.0F);
        }
        if (state.attackId == SandCrabEntity.PINCH) {
            this.pinch(t, SandCrabEntity.PINCH_IMPACT);
        } else if (state.attackId == SandCrabEntity.SNIPS) {
            this.pinch(t, SandCrabEntity.SNIPS_FIRST);
            this.pinch(t, SandCrabEntity.SNIPS_SECOND);
        }
    }

    /** The great claw drawn back and wide open, and shot out shut. */
    private void pinch(float t, float impact) {
        float draw = Anim.pulse(t, impact - 9.0F, impact - 4.0F, impact - 2.0F, impact);
        float snap = Anim.pulse(t, impact - 2.0F, impact, impact + 3.0F, impact + 8.0F);
        Anim.pose(this.body, draw, 0.0F, 0.4F, 0.0F);
        Anim.pose(this.clawRight, draw, 0.0F, -0.6F, 0.6F);
        Anim.pose(this.handRight, draw, 0.0F, -0.3F, 0.0F);
        Anim.pose(this.thumbRight, draw, -0.9F, 0.0F, 0.0F);
        Anim.pose(this.body, snap, 0.0F, -0.3F, 0.0F);
        Anim.pose(this.clawRight, snap, 0.0F, 0.75F, 0.1F);
        Anim.pose(this.handRight, snap, 0.0F, 0.5F, 0.0F);
        Anim.pose(this.thumbRight, snap, 0.05F, 0.0F, 0.0F);
    }
}
