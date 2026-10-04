package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.OverseerEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * The Overseer of the Stockade. The mesh between the markers is written by tools/gen_overseer.py (which also paints
 * the textures); the animation is written by hand.
 */
public class OverseerModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("overseer"), "main");
    /** Must match SCALE in tools/gen_overseer.py. */
    private static final float MODEL_SCALE = 1.35F;
    private static final float DEG = (float) (Math.PI / 180.0);
    private static final float KEY_REST = 0.6F;

    private final ModelPart body;
    private final ModelPart chest;
    private final ModelPart head;
    private final ModelPart apron;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightForearm;
    private final ModelPart leftForearm;
    private final ModelPart key;
    private final ModelPart chain;
    private final ModelPart lantern;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightShin;
    private final ModelPart leftShin;

    public OverseerModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.apron = this.body.getChild("apron");
        this.chest = this.body.getChild("chest");
        this.head = this.chest.getChild("head");
        this.rightArm = this.chest.getChild("right_arm");
        this.leftArm = this.chest.getChild("left_arm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.key = this.rightForearm.getChild("key");
        this.chain = this.leftForearm.getChild("chain");
        this.lantern = this.chain.getChild("lantern");
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
                .texOffs(8, 27).addBox(-8.0F, -5.0F, -5.0F, 16.0F, 7.0F, 10.0F),
                PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition apron = body.addOrReplaceChild("apron", CubeListBuilder.create()
                .texOffs(32, 44).addBox(-6.0F, 0.0F, -1.0F, 12.0F, 14.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -2.0F, -5.2F, -0.06F, 0.0F, 0.0F));
        PartDefinition chest = body.addOrReplaceChild("chest", CubeListBuilder.create()
                .texOffs(8, 0).addBox(-10.0F, -15.0F, -6.0F, 20.0F, 15.0F, 12.0F)
                .texOffs(32, 99).addBox(-8.0F, -17.0F, -3.0F, 16.0F, 2.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, -5.0F, 0.0F, 0.22F, 0.0F, 0.0F));
        PartDefinition head = chest.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 44).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, -15.0F, -2.0F, -0.12F, 0.0F, 0.0F));
        PartDefinition cage = head.addOrReplaceChild("cage", CubeListBuilder.create()
                .texOffs(72, 0).addBox(-5.5F, -10.0F, -5.5F, 11.0F, 11.0F, 11.0F)
                .texOffs(116, 7).addBox(-1.5F, -12.0F, -1.5F, 3.0F, 2.0F, 3.0F),
                PartPose.ZERO);
        PartDefinition right_arm = chest.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 60).addBox(-7.0F, -3.0F, -4.0F, 8.0F, 6.0F, 8.0F)
                .texOffs(58, 57).addBox(-6.0F, 3.0F, -3.0F, 6.0F, 9.0F, 6.0F),
                PartPose.offset(-10.0F, -12.0F, 0.0F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(72, 22).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 11.0F, 7.0F)
                .texOffs(72, 86).addBox(-4.0F, 11.0F, -4.0F, 8.0F, 5.0F, 8.0F),
                PartPose.offset(-3.0F, 12.0F, 0.0F));
        PartDefinition key = right_forearm.addOrReplaceChild("key", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.0F, -26.0F, -1.0F, 2.0F, 30.0F, 2.0F)
                .texOffs(106, 57).addBox(-3.0F, 4.0F, -1.0F, 6.0F, 6.0F, 2.0F)
                .texOffs(0, 74).addBox(1.0F, -25.0F, -1.0F, 6.0F, 4.0F, 2.0F)
                .texOffs(116, 12).addBox(1.0F, -19.0F, -1.0F, 4.0F, 3.0F, 2.0F)
                .texOffs(106, 65).addBox(-2.0F, -28.0F, -2.0F, 4.0F, 3.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 13.5F, 0.0F, 1.5F, 0.0F, 0.0F));
        PartDefinition left_arm = chest.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(32, 72).addBox(-1.0F, -3.0F, -4.0F, 8.0F, 6.0F, 8.0F)
                .texOffs(82, 57).addBox(0.0F, 3.0F, -3.0F, 6.0F, 9.0F, 6.0F),
                PartPose.offset(10.0F, -12.0F, 0.0F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(100, 22).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 11.0F, 7.0F)
                .texOffs(0, 99).addBox(-4.0F, 11.0F, -4.0F, 8.0F, 5.0F, 8.0F),
                PartPose.offset(3.0F, 12.0F, 0.0F));
        PartDefinition chain = left_forearm.addOrReplaceChild("chain", CubeListBuilder.create()
                .texOffs(116, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F),
                PartPose.offset(0.0F, 15.0F, 0.0F));
        PartDefinition lantern = chain.addOrReplaceChild("lantern", CubeListBuilder.create()
                .texOffs(32, 59).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 7.0F, 6.0F)
                .texOffs(16, 74).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 1.0F, 4.0F),
                PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(60, 40).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 9.0F, 8.0F),
                PartPose.offset(-5.0F, 8.0F, 0.0F));
        PartDefinition right_shin = right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(64, 72).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 7.0F, 7.0F)
                .texOffs(0, 86).addBox(-4.0F, 4.0F, -6.0F, 8.0F, 3.0F, 10.0F),
                PartPose.offset(0.0F, 9.0F, 0.0F));
        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(92, 40).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 9.0F, 8.0F),
                PartPose.offset(5.0F, 8.0F, 0.0F));
        PartDefinition left_shin = left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(92, 72).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 7.0F, 7.0F)
                .texOffs(36, 86).addBox(-4.0F, 4.0F, -6.0F, 8.0F, 3.0F, 10.0F),
                PartPose.offset(0.0F, 9.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 128, 128).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float walk = state.walkAnimationPos * 0.5F;
        boolean enraged = state.phase >= 2;

        // Idle: heavy breathing; the key rests on the shoulder, the lantern swings on its chain.
        float breath = Mth.sin(age * (enraged ? 0.13F : 0.07F));
        this.chest.xRot += breath * 0.03F;
        this.head.xRot += breath * 0.02F;
        this.rightArm.zRot += 0.12F;
        this.leftArm.zRot -= 0.12F;
        this.rightArm.xRot += -0.55F;
        this.rightForearm.xRot += -1.25F;
        this.leftForearm.xRot += -1.2F;
        this.apron.xRot += -0.1F * speed + breath * 0.01F;

        // The head follows what he looks at, as far as the cage lets it.
        this.head.yRot += Mth.clamp(state.yRot, -50.0F, 50.0F) * DEG * 0.8F;
        this.head.xRot += Mth.clamp(state.xRot, -30.0F, 30.0F) * DEG * 0.6F;

        // Walking: long heavy strides, the trunk rolls from side to side.
        float step = Mth.cos(walk);
        this.rightLeg.xRot += step * 0.7F * speed;
        this.leftLeg.xRot -= step * 0.7F * speed;
        this.rightShin.xRot += Math.max(0.0F, Mth.sin(walk)) * 0.8F * speed;
        this.leftShin.xRot += Math.max(0.0F, -Mth.sin(walk)) * 0.8F * speed;
        this.body.y += Math.abs(Mth.sin(walk)) * 1.0F * speed;
        this.body.zRot += step * 0.04F * speed;
        this.chest.yRot += step * 0.08F * speed;
        this.leftArm.xRot += step * 0.35F * speed;

        float t = state.attackTime;
        switch (state.attackId) {
            case OverseerEntity.SLAM -> {
                // The key goes up in both hands' worth of effort, hangs over his head, and comes down in front of him.
                float raise = Anim.pulse(t, 0.0F, 13.0F, 17.0F, 20.5F);
                float strike = Anim.pulse(t, 17.0F, 20.5F, 27.0F, 39.0F);
                this.rightArm.xRot = Anim.lerp(raise, this.rightArm.xRot, -2.9F);
                this.rightArm.xRot = Anim.lerp(strike, this.rightArm.xRot, -1.05F);
                this.rightForearm.xRot = Anim.lerp(Math.max(raise, strike), this.rightForearm.xRot, -0.25F);
                this.key.xRot += 0.5F * strike - 0.25F * raise;
                this.chest.xRot += -0.32F * raise + 0.55F * strike;
                this.head.xRot += -0.25F * raise + 0.2F * strike;
                this.leftArm.xRot += -0.6F * raise + 0.5F * strike;
                this.body.y += 2.0F * strike;
                this.rightLeg.xRot -= 0.3F * strike;
                this.rightShin.xRot += 0.5F * strike;
                this.leftShin.xRot += 0.3F * strike;
            }
            case OverseerEntity.SWEEP -> {
                // Winds up to his right and mows everything in front of him down to his left.
                float draw = Anim.pulse(t, 0.0F, 11.0F, 13.5F, 16.5F);
                float sweep = Anim.pulse(t, 13.5F, 16.5F, 23.0F, 35.0F);
                float out = Math.max(draw, sweep);
                this.rightArm.xRot = Anim.lerp(out, this.rightArm.xRot, -1.5F);
                this.rightForearm.xRot = Anim.lerp(out, this.rightForearm.xRot, -0.15F);
                this.rightArm.yRot += -1.0F * draw + 0.9F * sweep;
                this.key.xRot += 0.35F * out;
                this.chest.yRot += -0.7F * draw + 0.75F * sweep;
                this.chest.xRot += 0.15F * out;
                this.leftArm.zRot -= 0.5F * out;
            }
            case OverseerEntity.LEAP -> {
                // Squats, jumps with the key over his head, lands with it.
                float squat = Anim.pulse(t, 0.0F, 9.0F, 11.0F, 14.0F);
                float air = state.onGround && t > 18.0F ? 0.0F : Anim.ramp(t, 11.0F, 15.0F);
                float land = state.onGround && t > 18.0F ? 1.0F - Anim.ramp(t, OverseerEntity.LEAP_DURATION - 12.0F, OverseerEntity.LEAP_DURATION) : 0.0F;
                this.body.y += 5.0F * squat + 3.0F * land;
                this.rightLeg.xRot += -0.9F * squat - 0.5F * land + 0.3F * air;
                this.leftLeg.xRot += -0.9F * squat - 0.5F * land + 0.5F * air;
                this.rightShin.xRot += 1.5F * squat + 0.9F * land + 0.6F * air;
                this.leftShin.xRot += 1.5F * squat + 0.9F * land + 0.9F * air;
                this.chest.xRot += 0.35F * squat - 0.3F * air + 0.6F * land;
                this.rightArm.xRot = Anim.lerp(air, this.rightArm.xRot, -3.0F);
                this.rightArm.xRot = Anim.lerp(land, this.rightArm.xRot, -1.0F);
                this.rightForearm.xRot = Anim.lerp(Math.max(air, land), this.rightForearm.xRot, -0.2F);
                this.key.xRot += 0.5F * land;
                this.leftArm.xRot += -2.2F * air + 0.4F * land;
                this.leftArm.zRot -= 0.5F * air;
            }
            case OverseerEntity.CHAIN -> {
                // Whirls the lantern back over his shoulder and throws it straight out.
                float wind = Anim.pulse(t, 0.0F, 12.0F, 15.0F, 18.0F);
                float thrown = Anim.pulse(t, 15.0F, 18.0F, 30.0F, 43.0F);
                this.leftArm.xRot = Anim.lerp(wind, this.leftArm.xRot, 1.0F);
                this.leftArm.xRot = Anim.lerp(thrown, this.leftArm.xRot, -1.55F);
                this.leftForearm.xRot = Anim.lerp(thrown, this.leftForearm.xRot, 0.0F);
                this.chain.xRot = Mth.sin(t * 1.3F) * 0.8F * wind;
                this.chest.yRot += 0.45F * wind - 0.5F * thrown;
                this.chest.xRot += 0.25F * thrown;
            }
            case OverseerEntity.ROAR -> {
                // Throws his arms wide and his head back; the whole body shakes.
                float open = Anim.pulse(t, 0.0F, 14.0F, 44.0F, 59.0F);
                float shake = open * Mth.sin(age * 2.4F) * 0.04F;
                this.chest.xRot += -0.4F * open + shake;
                this.head.xRot += -0.5F * open;
                this.head.zRot += shake * 2.0F;
                this.rightArm.xRot = Anim.lerp(open, this.rightArm.xRot, -0.5F);
                this.rightArm.zRot += 1.0F * open;
                this.rightForearm.xRot = Anim.lerp(open, this.rightForearm.xRot, -0.6F);
                this.leftArm.xRot += -0.5F * open;
                this.leftArm.zRot -= 1.0F * open;
                this.leftForearm.xRot += -0.4F * open;
                this.body.y += 1.5F * open;
                this.rightShin.xRot += 0.3F * open;
                this.leftShin.xRot += 0.3F * open;
                this.rightLeg.xRot -= 0.2F * open;
                this.leftLeg.xRot -= 0.2F * open;
            }
            default -> {
            }
        }
        // At rest the key leans back over his shoulder; in a move it stands straight out of the fist.
        this.key.xRot += KEY_REST * (1.0F - busy(state));
        // The chain hangs straight down whatever the arm does, and swings a little; thrown, it flies out along the arm.
        float swing = Mth.sin(age * 0.09F) * (0.14F + 0.3F * speed);
        float thrown = state.attackId == OverseerEntity.CHAIN ? Anim.ramp(t, 0.0F, 12.0F) * (1.0F - Anim.ramp(t, 34.0F, 43.0F)) : 0.0F;
        float hang = -(this.chest.xRot + this.leftArm.xRot + this.leftForearm.xRot) + swing;
        this.chain.xRot = Anim.lerp(thrown, hang, this.chain.xRot);
        this.chain.zRot += Mth.cos(age * 0.07F) * 0.08F - this.leftArm.zRot;
        this.lantern.xRot += swing * 0.6F;
    }

    /** 1 while a move is running, easing in over its first ticks and out over its last. */
    private static float busy(BossRenderState state) {
        if (state.attackId == 0) {
            return 0.0F;
        }
        float length = switch (state.attackId) {
            case OverseerEntity.SLAM -> OverseerEntity.SLAM_DURATION;
            case OverseerEntity.SWEEP -> OverseerEntity.SWEEP_DURATION;
            case OverseerEntity.LEAP -> OverseerEntity.LEAP_DURATION;
            case OverseerEntity.CHAIN -> OverseerEntity.CHAIN_DURATION;
            default -> OverseerEntity.ROAR_DURATION;
        };
        return Anim.pulse(state.attackTime, 0.0F, 6.0F, length - 8.0F, length);
    }
}
