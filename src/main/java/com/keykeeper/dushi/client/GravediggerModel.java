package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.GravediggerEntity;

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
 * The Gravedigger of the borough. The mesh between the markers is written by tools/gen_gravedigger.py (which also
 * paints the textures); the animation is written by hand.
 */
public class GravediggerModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("gravedigger"), "main");
    /** Must match SCALE in tools/gen_gravedigger.py. */
    private static final float MODEL_SCALE = 1.3F;
    private static final float DEG = (float) (Math.PI / 180.0);
    /** At rest the spade is carried blade up over the shoulder. */
    private static final float SPADE_REST = 0.5F;

    private final ModelPart body;
    private final ModelPart chest;
    private final ModelPart head;
    private final ModelPart skirtFront;
    private final ModelPart skirtBack;
    private final ModelPart lantern;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightForearm;
    private final ModelPart leftForearm;
    private final ModelPart spade;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightShin;
    private final ModelPart leftShin;

    public GravediggerModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.skirtFront = this.body.getChild("skirt_front");
        this.skirtBack = this.body.getChild("skirt_back");
        this.lantern = this.body.getChild("lantern");
        this.chest = this.body.getChild("chest");
        this.head = this.chest.getChild("head");
        this.rightArm = this.chest.getChild("right_arm");
        this.leftArm = this.chest.getChild("left_arm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.spade = this.rightForearm.getChild("spade");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.rightShin = this.rightLeg.getChild("right_shin");
        this.leftShin = this.leftLeg.getChild("left_shin");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(32, 37).addBox(-5.5F, -4.0F, -3.5F, 11.0F, 6.0F, 7.0F),
                PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition skirt_front = body.addOrReplaceChild("skirt_front", CubeListBuilder.create()
                .texOffs(68, 37).addBox(-5.5F, 0.0F, -0.5F, 11.0F, 12.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 2.0F, -3.5F, -0.05F, 0.0F, 0.0F));
        PartDefinition skirt_back = body.addOrReplaceChild("skirt_back", CubeListBuilder.create()
                .texOffs(8, 28).addBox(-5.5F, 0.0F, -0.5F, 11.0F, 13.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 2.0F, 3.5F, 0.08F, 0.0F, 0.0F));
        PartDefinition lantern = body.addOrReplaceChild("lantern", CubeListBuilder.create()
                .texOffs(116, 6).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F)
                .texOffs(44, 62).addBox(-2.0F, 3.0F, -2.0F, 4.0F, 6.0F, 4.0F),
                PartPose.offset(6.5F, -1.0F, -1.0F));
        PartDefinition chest = body.addOrReplaceChild("chest", CubeListBuilder.create()
                .texOffs(34, 0).addBox(-6.5F, -13.0F, -4.0F, 13.0F, 13.0F, 8.0F)
                .texOffs(80, 68).addBox(-5.0F, -15.0F, -1.0F, 10.0F, 2.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.32F, 0.0F, 0.0F));
        PartDefinition coffin = chest.addOrReplaceChild("coffin", CubeListBuilder.create()
                .texOffs(8, 0).addBox(-4.5F, -8.0F, 0.0F, 9.0F, 24.0F, 4.0F)
                .texOffs(52, 50).addBox(-5.5F, -2.0F, 0.0F, 11.0F, 8.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -13.0F, 4.5F, -0.12F, 0.0F, 0.0F));
        PartDefinition head = chest.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(94, 31).addBox(-3.5F, -7.0F, -3.5F, 7.0F, 7.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, -13.0F, -2.0F, -0.2F, 0.0F, 0.0F));
        PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create()
                .texOffs(34, 21).addBox(-7.5F, -1.0F, -7.5F, 15.0F, 1.0F, 15.0F)
                .texOffs(20, 50).addBox(-4.0F, -5.0F, -4.0F, 8.0F, 4.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, -5.5F, 0.0F, -0.06F, 0.0F, 0.0F));
        PartDefinition right_arm = chest.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(76, 0).addBox(-5.0F, -2.0F, -2.5F, 5.0F, 11.0F, 5.0F),
                PartPose.offset(-6.5F, -11.0F, 0.0F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(94, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 11.0F, 4.0F)
                .texOffs(60, 62).addBox(-2.5F, 10.0F, -2.5F, 5.0F, 4.0F, 5.0F),
                PartPose.offset(-2.5F, 9.0F, 0.0F));
        PartDefinition spade = right_forearm.addOrReplaceChild("spade", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.0F, -24.0F, -1.0F, 2.0F, 32.0F, 2.0F)
                .texOffs(76, 16).addBox(-3.0F, 8.0F, -1.0F, 6.0F, 2.0F, 2.0F)
                .texOffs(0, 55).addBox(-4.5F, -35.0F, -0.5F, 9.0F, 11.0F, 1.0F)
                .texOffs(116, 0).addBox(-1.5F, -26.0F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 12.5F, 0.0F, 1.5F, 0.0F, 0.0F));
        PartDefinition left_arm = chest.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(96, 0).addBox(0.0F, -2.0F, -2.5F, 5.0F, 11.0F, 5.0F),
                PartPose.offset(6.5F, -11.0F, 0.0F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(110, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 11.0F, 4.0F)
                .texOffs(0, 67).addBox(-2.5F, 10.0F, -2.5F, 5.0F, 4.0F, 5.0F),
                PartPose.offset(2.5F, 9.0F, 0.0F));
        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 42).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 8.0F, 5.0F),
                PartPose.offset(-3.0F, 9.0F, 0.0F));
        PartDefinition right_shin = right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(112, 45).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F)
                .texOffs(82, 58).addBox(-2.5F, 4.0F, -4.5F, 5.0F, 3.0F, 7.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(92, 45).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 8.0F, 5.0F),
                PartPose.offset(3.0F, 9.0F, 0.0F));
        PartDefinition left_shin = left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(112, 56).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F)
                .texOffs(20, 62).addBox(-2.5F, 4.0F, -4.5F, 5.0F, 3.0F, 7.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 128, 128).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float walk = state.walkAnimationPos * 0.5F;
        boolean roused = state.phase >= 2;

        // Idle: slow breath under the weight of the coffin; the spade over the shoulder, the other hand hanging.
        float breath = Mth.sin(age * (roused ? 0.12F : 0.06F));
        this.chest.xRot += breath * 0.025F;
        this.head.xRot += breath * 0.02F;
        this.rightArm.zRot += 0.1F;
        this.leftArm.zRot -= 0.1F;
        this.rightArm.xRot += -0.35F;
        this.rightForearm.xRot += -1.1F;
        this.leftArm.xRot += 0.1F;
        this.leftForearm.xRot += -0.25F;
        this.lantern.xRot += Mth.sin(age * 0.08F) * (0.1F + 0.35F * speed);
        this.lantern.zRot += Mth.cos(age * 0.06F) * 0.06F;

        this.head.yRot += Mth.clamp(state.yRot, -55.0F, 55.0F) * DEG * 0.8F;
        this.head.xRot += Mth.clamp(state.xRot, -30.0F, 30.0F) * DEG * 0.6F;

        // Walking: a long, stiff, swinging stride; the coat tails follow the legs.
        float step = Mth.cos(walk);
        this.rightLeg.xRot += step * 0.75F * speed;
        this.leftLeg.xRot -= step * 0.75F * speed;
        this.rightShin.xRot += Math.max(0.0F, Mth.sin(walk)) * 0.7F * speed;
        this.leftShin.xRot += Math.max(0.0F, -Mth.sin(walk)) * 0.7F * speed;
        this.body.y += Math.abs(Mth.sin(walk)) * 0.8F * speed;
        this.chest.yRot += step * 0.07F * speed;
        this.leftArm.xRot += step * 0.4F * speed;
        this.skirtFront.xRot -= Math.abs(step) * 0.35F * speed;
        this.skirtBack.xRot += Math.abs(step) * 0.35F * speed;

        float t = state.attackTime;
        switch (state.attackId) {
            case GravediggerEntity.SWEEP -> {
                // Drawn far back to his right, then mown across at the height of a man's chest.
                float draw = Anim.pulse(t, 0.0F, 11.0F, 13.5F, 16.5F);
                float sweep = Anim.pulse(t, 13.5F, 16.5F, 23.0F, 35.0F);
                float out = Math.max(draw, sweep);
                this.rightArm.xRot = Anim.lerp(out, this.rightArm.xRot, -1.45F);
                this.rightForearm.xRot = Anim.lerp(out, this.rightForearm.xRot, -0.1F);
                this.rightArm.yRot += -1.0F * draw + 0.9F * sweep;
                this.spade.xRot += 0.3F * out;
                this.chest.yRot += -0.7F * draw + 0.75F * sweep;
                this.chest.xRot += 0.12F * out;
                this.leftArm.zRot -= 0.4F * out;
            }
            case GravediggerEntity.SLAM -> {
                // Both hands take the spade over his head; it comes down edge first and stays in the ground while he works it loose.
                float raise = Anim.pulse(t, 0.0F, 13.0F, 17.0F, 20.5F);
                float strike = Anim.pulse(t, 17.0F, 20.5F, 40.0F, 49.0F);
                float stuck = Anim.pulse(t, 24.0F, 28.0F, 38.0F, 44.0F);
                this.rightArm.xRot = Anim.lerp(raise, this.rightArm.xRot, -2.9F);
                this.rightArm.xRot = Anim.lerp(strike, this.rightArm.xRot, -1.0F);
                this.rightForearm.xRot = Anim.lerp(Math.max(raise, strike), this.rightForearm.xRot, -0.2F);
                this.leftArm.xRot = Anim.lerp(raise, this.leftArm.xRot, -2.6F);
                this.leftArm.xRot = Anim.lerp(strike, this.leftArm.xRot, -0.9F);
                this.leftArm.zRot -= 0.25F * Math.max(raise, strike);
                this.spade.xRot += 0.45F * strike - 0.2F * raise;
                this.chest.xRot += -0.3F * raise + 0.5F * strike + Mth.sin(t * 0.9F) * 0.05F * stuck;
                this.head.xRot += -0.2F * raise + 0.2F * strike;
                this.body.y += 2.0F * strike;
                this.rightShin.xRot += 0.45F * strike;
                this.leftShin.xRot += 0.3F * strike;
                this.rightLeg.xRot -= 0.3F * strike;
            }
            case GravediggerEntity.DIRT -> {
                // The blade goes into the ground beside him, low, and comes up and forward with a load of earth.
                float dig = Anim.pulse(t, 0.0F, 9.0F, 14.0F, 17.0F);
                float fling = Anim.pulse(t, 14.0F, 18.0F, 26.0F, 37.0F);
                this.chest.xRot += 0.55F * dig - 0.15F * fling;
                this.chest.yRot += -0.5F * dig + 0.35F * fling;
                this.rightArm.xRot = Anim.lerp(dig, this.rightArm.xRot, 0.2F);
                this.rightArm.xRot = Anim.lerp(fling, this.rightArm.xRot, -1.9F);
                this.rightForearm.xRot = Anim.lerp(Math.max(dig, fling), this.rightForearm.xRot, -0.3F);
                this.spade.xRot += 0.9F * dig - 0.4F * fling;
                this.leftArm.xRot += -0.6F * dig - 1.2F * fling;
                this.body.y += 2.5F * dig;
                this.rightShin.xRot += 0.5F * dig;
                this.leftShin.xRot += 0.5F * dig;
                this.rightLeg.xRot -= 0.35F * dig;
                this.leftLeg.xRot -= 0.35F * dig;
            }
            case GravediggerEntity.RUSH -> {
                // Turns a shoulder to the target, lowers his head and runs.
                float set = Anim.pulse(t, 0.0F, 12.0F, 28.0F, 40.0F);
                float run = Anim.pulse(t, 13.0F, 15.0F, 27.0F, 30.0F);
                float stride = Mth.sin(t * 1.1F) * run;
                this.chest.xRot += 0.55F * set;
                this.chest.yRot += 0.5F * set;
                this.head.xRot -= 0.35F * set;
                this.head.yRot -= 0.4F * set;
                this.leftArm.xRot += -0.8F * set;
                this.leftForearm.xRot += -1.0F * set;
                this.rightArm.xRot += 0.6F * set;
                this.rightLeg.xRot += stride * 0.9F - 0.2F * set * (1.0F - run);
                this.leftLeg.xRot -= stride * 0.9F;
                this.rightShin.xRot += Math.max(0.0F, stride) * 0.8F + 0.3F * set;
                this.leftShin.xRot += Math.max(0.0F, -stride) * 0.8F + 0.3F * set;
                this.body.y += 1.5F * set;
                this.skirtBack.xRot += 0.7F * run;
            }
            case GravediggerEntity.RAISE -> {
                // Drives the spade into the ground with both hands, leans on it and throws his head back; the earth answers.
                float plant = Anim.pulse(t, 0.0F, 14.0F, 44.0F, 55.0F);
                float call = Anim.pulse(t, 18.0F, 26.0F, 40.0F, 50.0F);
                float shake = call * Mth.sin(age * 2.2F) * 0.035F;
                this.rightArm.xRot = Anim.lerp(plant, this.rightArm.xRot, -1.15F);
                this.rightForearm.xRot = Anim.lerp(plant, this.rightForearm.xRot, -0.2F);
                this.leftArm.xRot = Anim.lerp(plant, this.leftArm.xRot, -1.15F);
                this.leftArm.zRot += 0.3F * plant;
                this.leftForearm.xRot = Anim.lerp(plant, this.leftForearm.xRot, -0.2F);
                this.spade.xRot += 0.55F * plant;
                this.chest.xRot += 0.2F * plant - 0.5F * call + shake;
                this.head.xRot -= 0.6F * call;
                this.head.zRot += shake * 2.0F;
                this.body.y += 1.5F * plant;
                this.rightShin.xRot += 0.3F * plant;
                this.leftShin.xRot += 0.3F * plant;
                this.rightLeg.xRot -= 0.2F * plant;
                this.leftLeg.xRot -= 0.2F * plant;
            }
            default -> {
            }
        }
        this.spade.xRot += SPADE_REST * (1.0F - busy(state));
    }

    /** 1 while a move is running, easing in over its first ticks and out over its last. */
    private static float busy(BossRenderState state) {
        if (state.attackId == 0) {
            return 0.0F;
        }
        float length = switch (state.attackId) {
            case GravediggerEntity.SWEEP -> GravediggerEntity.SWEEP_DURATION;
            case GravediggerEntity.SLAM -> GravediggerEntity.SLAM_DURATION;
            case GravediggerEntity.DIRT -> GravediggerEntity.DIRT_DURATION;
            case GravediggerEntity.RUSH -> GravediggerEntity.RUSH_DURATION;
            default -> GravediggerEntity.RAISE_DURATION;
        };
        return Anim.pulse(state.attackTime, 0.0F, 6.0F, length - 8.0F, length);
    }
}
