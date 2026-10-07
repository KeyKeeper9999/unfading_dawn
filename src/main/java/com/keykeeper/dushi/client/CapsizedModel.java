package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.CapsizedEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * The Capsized. The mesh between the markers is written by tools/gen_capsized.py (which also paints the texture and
 * draws the poses below on a sheet); the animation is written by hand.
 * <p>
 * Everything hangs from 'all', whose 0 is the ground. Lying (phase 1), or drawn in under itself in a fight, 'all' is
 * down by the height the gunwale is carried at and nothing shows but the boat. The hull turns about the edge of its
 * stern: that is how it rears, and what it comes down from.
 */
public class CapsizedModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("capsized"), "main");
    /** How high the gunwale is carried off the ground: must match STAND in tools/gen_capsized.py. */
    private static final float STAND = 8.0F;

    private final ModelPart all;
    private final ModelPart hull;
    private final ModelPart body;
    private final ModelPart eyeRight;
    private final ModelPart eyeLeft;
    private final ModelPart clawRight;
    private final ModelPart handRight;
    private final ModelPart thumbRight;
    private final ModelPart clawLeft;
    private final ModelPart[] legs = new ModelPart[6];
    private final ModelPart[] shins = new ModelPart[6];

    public CapsizedModel(ModelPart root) {
        super(root);
        this.all = root.getChild("all");
        this.hull = this.all.getChild("hull");
        this.body = this.all.getChild("body");
        this.eyeRight = this.body.getChild("eye_r");
        this.eyeLeft = this.body.getChild("eye_l");
        this.clawRight = this.body.getChild("claw_r");
        this.handRight = this.clawRight.getChild("hand_r");
        this.thumbRight = this.handRight.getChild("thumb_r");
        this.clawLeft = this.body.getChild("claw_l");
        for (int i = 0; i < 3; i++) {
            this.legs[i] = this.body.getChild("leg_r" + (i + 1));
            this.shins[i] = this.legs[i].getChild("shin_r" + (i + 1));
            this.legs[3 + i] = this.body.getChild("leg_l" + (i + 1));
            this.shins[3 + i] = this.legs[3 + i].getChild("shin_l" + (i + 1));
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition hull = all.addOrReplaceChild("hull", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-16.0F, -6.0F, -60.0F, 32.0F, 6.0F, 60.0F)
                .texOffs(0, 130).addBox(-13.0F, -11.0F, -58.0F, 26.0F, 5.0F, 56.0F)
                .texOffs(0, 191).addBox(-9.0F, -15.0F, -55.0F, 18.0F, 4.0F, 50.0F)
                .texOffs(0, 66).addBox(-1.0F, -17.0F, -62.0F, 2.0F, 2.0F, 62.0F)
                .texOffs(184, 102).addBox(-11.0F, -6.0F, -64.0F, 22.0F, 6.0F, 4.0F)
                .texOffs(184, 112).addBox(-6.0F, -6.0F, -67.0F, 12.0F, 6.0F, 3.0F)
                .texOffs(236, 0).addBox(-1.0F, -17.0F, -68.0F, 2.0F, 17.0F, 2.0F),
                PartPose.offset(0.0F, -8.0F, 30.0F));
        PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(128, 66).addBox(-9.0F, -6.0F, -12.0F, 18.0F, 8.0F, 26.0F),
                PartPose.offset(0.0F, -5.0F, 0.0F));
        PartDefinition eye_r = body.addOrReplaceChild("eye_r", CubeListBuilder.create()
                .texOffs(244, 0).addBox(-1.0F, -12.0F, -1.0F, 2.0F, 12.0F, 2.0F)
                .texOffs(224, 27).addBox(-1.5F, -15.0F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(-4.0F, -3.0F, -12.0F, 0.9F, 0.0F, -0.15F));
        PartDefinition eye_l = body.addOrReplaceChild("eye_l", CubeListBuilder.create()
                .texOffs(232, 70).addBox(-1.0F, -12.0F, -1.0F, 2.0F, 12.0F, 2.0F)
                .texOffs(240, 70).addBox(-1.5F, -15.0F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(4.0F, -3.0F, -12.0F, 0.9F, 0.0F, 0.15F));
        PartDefinition claw_r = body.addOrReplaceChild("claw_r", CubeListBuilder.create()
                .texOffs(184, 27).addBox(-3.0F, -3.0F, -14.0F, 6.0F, 6.0F, 14.0F),
                PartPose.offsetAndRotation(-10.0F, 0.0F, -10.0F, 0.0F, 0.25F, 0.0F));
        PartDefinition hand_r = claw_r.addOrReplaceChild("hand_r", CubeListBuilder.create()
                .texOffs(184, 0).addBox(-5.0F, -6.0F, -16.0F, 10.0F, 11.0F, 16.0F)
                .texOffs(216, 87).addBox(-4.0F, 1.0F, -27.0F, 8.0F, 4.0F, 11.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -14.0F, 0.0F, -0.25F, 0.0F));
        PartDefinition thumb_r = hand_r.addOrReplaceChild("thumb_r", CubeListBuilder.create()
                .texOffs(184, 47).addBox(-4.0F, -3.0F, -12.0F, 8.0F, 5.0F, 12.0F),
                PartPose.offset(0.0F, -3.0F, -15.0F));
        PartDefinition claw_l = body.addOrReplaceChild("claw_l", CubeListBuilder.create()
                .texOffs(158, 100).addBox(-2.0F, -2.0F, -9.0F, 4.0F, 4.0F, 9.0F),
                PartPose.offsetAndRotation(10.0F, 0.0F, -10.0F, 0.0F, -0.3F, 0.0F));
        PartDefinition hand_l = claw_l.addOrReplaceChild("hand_l", CubeListBuilder.create()
                .texOffs(128, 100).addBox(-3.0F, -3.0F, -9.0F, 6.0F, 6.0F, 9.0F)
                .texOffs(236, 102).addBox(-2.0F, 0.0F, -14.0F, 4.0F, 2.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, -9.0F, 0.0F, 0.3F, 0.0F));
        PartDefinition leg_r1 = body.addOrReplaceChild("leg_r1", CubeListBuilder.create()
                .texOffs(214, 112).addBox(-13.0F, -2.0F, -2.0F, 13.0F, 4.0F, 4.0F),
                PartPose.offsetAndRotation(-9.0F, 0.0F, -6.0F, 0.0F, -0.3F, 0.5F));
        PartDefinition shin_r1 = leg_r1.addOrReplaceChild("shin_r1", CubeListBuilder.create()
                .texOffs(236, 19).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 13.0F, 4.0F)
                .texOffs(244, 14).addBox(-1.0F, 12.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-13.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5F));
        PartDefinition leg_l1 = body.addOrReplaceChild("leg_l1", CubeListBuilder.create()
                .texOffs(128, 115).addBox(0.0F, -2.0F, -2.0F, 13.0F, 4.0F, 4.0F),
                PartPose.offsetAndRotation(9.0F, 0.0F, -6.0F, 0.0F, 0.3F, -0.5F));
        PartDefinition shin_l1 = leg_l1.addOrReplaceChild("shin_l1", CubeListBuilder.create()
                .texOffs(224, 36).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 13.0F, 4.0F)
                .texOffs(216, 64).addBox(-1.0F, 12.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(13.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5F));
        PartDefinition leg_r2 = body.addOrReplaceChild("leg_r2", CubeListBuilder.create()
                .texOffs(214, 120).addBox(-13.0F, -2.0F, -2.0F, 13.0F, 4.0F, 4.0F),
                PartPose.offsetAndRotation(-9.0F, 0.0F, 5.0F, 0.0F, 0.0F, 0.5F));
        PartDefinition shin_r2 = leg_r2.addOrReplaceChild("shin_r2", CubeListBuilder.create()
                .texOffs(240, 36).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 13.0F, 4.0F)
                .texOffs(240, 76).addBox(-1.0F, 12.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-13.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5F));
        PartDefinition leg_l2 = body.addOrReplaceChild("leg_l2", CubeListBuilder.create()
                .texOffs(162, 121).addBox(0.0F, -2.0F, -2.0F, 13.0F, 4.0F, 4.0F),
                PartPose.offsetAndRotation(9.0F, 0.0F, 5.0F, 0.0F, -0.0F, -0.5F));
        PartDefinition shin_l2 = leg_l2.addOrReplaceChild("shin_l2", CubeListBuilder.create()
                .texOffs(224, 53).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 13.0F, 4.0F)
                .texOffs(248, 76).addBox(-1.0F, 12.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(13.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5F));
        PartDefinition leg_r3 = body.addOrReplaceChild("leg_r3", CubeListBuilder.create()
                .texOffs(196, 128).addBox(-13.0F, -2.0F, -2.0F, 13.0F, 4.0F, 4.0F),
                PartPose.offsetAndRotation(-9.0F, 0.0F, 16.0F, 0.0F, 0.3F, 0.5F));
        PartDefinition shin_r3 = leg_r3.addOrReplaceChild("shin_r3", CubeListBuilder.create()
                .texOffs(240, 53).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 13.0F, 4.0F)
                .texOffs(240, 80).addBox(-1.0F, 12.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-13.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5F));
        PartDefinition leg_l3 = body.addOrReplaceChild("leg_l3", CubeListBuilder.create()
                .texOffs(164, 136).addBox(0.0F, -2.0F, -2.0F, 13.0F, 4.0F, 4.0F),
                PartPose.offsetAndRotation(9.0F, 0.0F, 16.0F, 0.0F, -0.3F, -0.5F));
        PartDefinition shin_l3 = leg_l3.addOrReplaceChild("shin_l3", CubeListBuilder.create()
                .texOffs(216, 70).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 13.0F, 4.0F)
                .texOffs(248, 80).addBox(-1.0F, 12.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(13.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 256, 256);
    }

    /** How far it is drawn in under the boat, 0 to 1, at tick {@code t} of move {@code id} in {@code phase}. */
    private static float tucked(int phase, int id, float t) {
        if (phase < 2) {
            return 1.0F;
        }
        if (id == CapsizedEntity.RISE) {
            return 1.0F - Anim.ramp(t, 4.0F, CapsizedEntity.RISE_UP);
        }
        if (id == CapsizedEntity.TUCK) {
            return Anim.pulse(t, 0.0F, CapsizedEntity.TUCK_DOWN, CapsizedEntity.TUCK_BURST - 2.0F, CapsizedEntity.TUCK_BURST + 3.0F);
        }
        if (id == CapsizedEntity.SPIN) {
            int last = CapsizedEntity.SPIN_HITS[CapsizedEntity.SPIN_HITS.length - 1];
            return 0.6F * Anim.pulse(t, 4.0F, 14.0F, last, last + 8.0F);
        }
        return 0.0F;
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float t = state.attackTime;
        int id = state.attackId;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float run = state.walkAnimationPos * 0.9F;
        float tuck = tucked(state.phase, id, t);
        boolean boat = tuck > 0.97F;
        // a boat on the sand has no legs
        this.eyeRight.visible = !boat;
        this.eyeLeft.visible = !boat;
        this.clawRight.visible = !boat;
        this.clawLeft.visible = !boat;
        for (ModelPart leg : this.legs) {
            leg.visible = !boat;
        }
        this.all.y += STAND * tuck;
        float up = 1.0F - tuck;
        // the legs: three and three; folded flat as it goes down
        for (int i = 0; i < 6; i++) {
            float side = i < 3 ? 1.0F : -1.0F;
            float lift = Math.max(0.0F, Mth.sin(run + (i % 3) * 2.1F + (i < 3 ? 0.0F : Mth.PI))) * 0.35F * speed;
            this.legs[i].zRot += side * (lift - 0.55F * tuck);
            this.shins[i].zRot += side * (-lift * 0.5F + 0.6F * tuck);
            this.legs[i].yRot += Mth.cos(run + (i % 3) * 2.1F + (i < 3 ? 0.0F : Mth.PI)) * 0.2F * speed;
        }
        // carried: the bow a little up, rolling as it walks; the eyes look about; the great claw before it, a little open
        this.hull.xRot -= 0.08F * up;
        this.hull.zRot += Mth.sin(run) * 0.04F * speed * up;
        this.all.y += Math.abs(Mth.sin(run)) * 0.8F * speed * up;
        this.eyeRight.yRot += Mth.sin(age * 0.07F) * 0.3F;
        this.eyeLeft.yRot += Mth.sin(age * 0.06F + 1.7F) * 0.3F;
        this.eyeRight.xRot += 0.6F * tuck;
        this.eyeLeft.xRot += 0.6F * tuck;
        Anim.pose(this.clawRight, up, 0.0F, 0.0F, 0.25F);
        this.thumbRight.xRot -= (0.4F + Mth.sin(age * 0.11F) * 0.15F) * up;
        Anim.pose(this.clawRight, tuck, 0.0F, 1.2F, 0.0F);
        Anim.pose(this.clawLeft, tuck, 0.0F, -1.0F, 0.0F);
        switch (id) {
            case CapsizedEntity.RISE -> this.hull.zRot += Mth.sin(t * 1.4F) * 0.07F * Anim.pulse(t, 0.0F, 4.0F, CapsizedEntity.RISE_UP - 4.0F, CapsizedEntity.RISE_UP + 6.0F);
            case CapsizedEntity.SNAP -> {
                // The great claw drawn back and across, wide open - and shot straight out, the whole of it going after.
                float impact = CapsizedEntity.SNAP_IMPACT;
                float draw = Anim.pulse(t, 0.0F, 10.0F, impact - 4.0F, impact - 1.0F);
                float shot = Anim.pulse(t, impact - 4.0F, impact, impact + 6.0F, CapsizedEntity.SNAP_DURATION);
                Anim.pose(this.hull, draw, -0.12F, 0.0F, 0.0F);
                Anim.pose(this.body, draw, 0.0F, 0.2F, 0.0F);
                Anim.pose(this.clawRight, draw, 0.0F, 0.7F, 0.3F);
                Anim.pose(this.handRight, draw, 0.0F, 0.5F, 0.0F);
                Anim.pose(this.thumbRight, draw, -0.9F, 0.0F, 0.0F);
                Anim.pose(this.body, shot, 0.0F, -0.15F, 0.0F);
                Anim.pose(this.clawRight, shot, -0.1F, -0.2F, 0.0F);
                Anim.pose(this.handRight, shot, 0.0F, 0.2F, 0.0F);
                Anim.pose(this.thumbRight, shot, 0.05F, 0.0F, 0.0F);
                this.all.z -= 8.0F * shot;
            }
            case CapsizedEntity.SWEEP -> {
                // Far out to its right, open; and across before it, low, to the other side.
                float impact = CapsizedEntity.SWEEP_IMPACT;
                float draw = Anim.pulse(t, 0.0F, 12.0F, impact - 5.0F, impact - 1.0F);
                float cut = Anim.pulse(t, impact - 5.0F, impact + 1.0F, impact + 8.0F, CapsizedEntity.SWEEP_DURATION);
                Anim.pose(this.hull, draw, -0.08F, 0.25F, 0.0F);
                Anim.pose(this.body, draw, 0.0F, 0.35F, 0.0F);
                Anim.pose(this.clawRight, draw, 0.0F, 1.0F, 0.2F);
                Anim.pose(this.thumbRight, draw, -0.7F, 0.0F, 0.0F);
                Anim.pose(this.hull, cut, -0.08F, -0.25F, 0.0F);
                Anim.pose(this.body, cut, 0.0F, -0.35F, 0.0F);
                Anim.pose(this.clawRight, cut, 0.0F, -1.0F, 0.1F);
                Anim.pose(this.handRight, cut, 0.0F, -0.2F, 0.0F);
            }
            case CapsizedEntity.SLAM -> {
                // The bow goes up until the boat stands on her stern, the claws spread under her - and she comes down
                // on whatever is before her, and lies there a moment with her bow dug in.
                float impact = CapsizedEntity.SLAM_IMPACT;
                float rear = Anim.pulse(t, 0.0F, 22.0F, impact - 5.0F, impact);
                float lie = Anim.pulse(t, impact - 5.0F, impact, CapsizedEntity.SLAM_STUCK, CapsizedEntity.SLAM_DURATION);
                Anim.pose(this.hull, rear, -0.75F, 0.0F, 0.0F);
                Anim.pose(this.body, rear, -0.3F, 0.0F, 0.0F);
                Anim.pose(this.clawRight, rear, -0.5F, 0.3F, 0.4F);
                Anim.pose(this.thumbRight, rear, -0.8F, 0.0F, 0.0F);
                this.eyeRight.xRot -= 0.5F * rear;
                this.eyeLeft.xRot -= 0.5F * rear;
                Anim.pose(this.hull, lie, 0.14F, 0.0F, 0.0F);
                Anim.pose(this.clawRight, lie, 0.2F, 0.5F, 0.0F);
                this.all.y += 3.0F * lie;
                this.all.z -= 6.0F * lie;
                this.eyeRight.xRot += 0.4F * lie;
                this.eyeLeft.xRot += 0.4F * lie;
            }
            case CapsizedEntity.SPIN -> {
                // Half drawn in, the legs short under it - and the boat goes round like a top, coming on; then it
                // stands, and sways.
                float first = CapsizedEntity.SPIN_HITS[0];
                float last = CapsizedEntity.SPIN_HITS[CapsizedEntity.SPIN_HITS.length - 1];
                float turn = CapsizedEntity.SPIN_HITS[1] - first;
                if (t >= first - turn && t <= last) {
                    this.all.yRot += -(t - first) / turn * Mth.TWO_PI;
                }
                float giddy = Anim.pulse(t, last, last + 6.0F, CapsizedEntity.SPIN_DURATION - 6.0F, CapsizedEntity.SPIN_DURATION);
                this.hull.zRot += Mth.sin(t * 0.45F) * 0.12F * giddy;
                this.hull.xRot += Mth.cos(t * 0.33F) * 0.06F * giddy;
                this.eyeRight.zRot += Mth.sin(t * 0.7F) * 0.5F * giddy;
                this.eyeLeft.zRot -= Mth.sin(t * 0.6F) * 0.5F * giddy;
            }
            case CapsizedEntity.TUCK -> {
                // down, and still; and then thrown up off the sand all at once
                float burst = Anim.pulse(t, CapsizedEntity.TUCK_BURST - 1.0F, CapsizedEntity.TUCK_BURST + 2.0F, CapsizedEntity.TUCK_BURST + 5.0F, CapsizedEntity.TUCK_BURST + 14.0F);
                this.all.y -= 9.0F * burst;
                this.hull.xRot -= 0.2F * burst;
                float shiver = Anim.pulse(t, CapsizedEntity.TUCK_BURST - 16.0F, CapsizedEntity.TUCK_BURST - 6.0F, CapsizedEntity.TUCK_BURST - 2.0F, CapsizedEntity.TUCK_BURST);
                this.hull.zRot += Mth.sin(t * 2.1F) * 0.05F * shiver;
            }
            case CapsizedEntity.BROOD -> {
                // The small claw comes up and raps on the planks of its own house: twice. Something under the sand hears.
                float lift = Anim.pulse(t, 0.0F, 10.0F, CapsizedEntity.BROOD_KNOCKS[1] + 4.0F, CapsizedEntity.BROOD_DURATION);
                float rap = 0.0F;
                for (int knock : CapsizedEntity.BROOD_KNOCKS) {
                    rap = Math.max(rap, Anim.pulse(t, knock - 5.0F, knock - 2.0F, knock - 1.0F, knock));
                }
                Anim.pose(this.clawLeft, lift, -0.9F + 0.4F * rap, -0.6F, 0.0F);
                Anim.pose(this.hull, lift, -0.16F, 0.0F, -0.05F);
            }
            default -> {
            }
        }
    }
}
