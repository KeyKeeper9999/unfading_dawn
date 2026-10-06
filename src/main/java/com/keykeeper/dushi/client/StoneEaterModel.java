package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.StoneEaterEntity;

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
 * The Stone-Eater. The mesh between the markers is written by tools/gen_stone_eater.py (which also paints the textures);
 * the animation is written by hand. Everything hangs from the part {@code all}, which goes down through the floor when
 * the beast digs itself under.
 */
public class StoneEaterModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("stone_eater"), "main");
    /** Must match SCALE in tools/gen_stone_eater.py. */
    private static final float MODEL_SCALE = 1.9F;
    private static final float DEG = (float) (Math.PI / 180.0);
    /** How far down the whole of it goes when it is under the floor, in model units. */
    private static final float DEPTH = 30.0F;

    private final ModelPart all;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightForearm;
    private final ModelPart leftForearm;
    private final ModelPart rightClaw;
    private final ModelPart leftClaw;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart tail;
    private final ModelPart tailTip;

    public StoneEaterModel(ModelPart root) {
        super(root);
        this.all = root.getChild("all");
        this.body = this.all.getChild("body");
        this.head = this.body.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.rightClaw = this.rightForearm.getChild("right_claw");
        this.leftClaw = this.leftForearm.getChild("left_claw");
        this.rightLeg = this.all.getChild("right_leg");
        this.leftLeg = this.all.getChild("left_leg");
        this.tail = this.body.getChild("tail");
        this.tailTip = this.tail.getChild("tail_tip");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.ZERO);
        PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-9.0F, -7.0F, -12.0F, 18.0F, 13.0F, 22.0F)
                .texOffs(44, 87).addBox(-7.0F, -10.0F, -11.0F, 14.0F, 3.0F, 7.0F)
                .texOffs(0, 65).addBox(-8.0F, -11.0F, -4.0F, 16.0F, 4.0F, 8.0F)
                .texOffs(24, 97).addBox(-6.0F, -9.0F, 4.0F, 12.0F, 2.0F, 6.0F),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(80, 18).addBox(-6.0F, -5.0F, -9.0F, 12.0F, 8.0F, 9.0F)
                .texOffs(96, 65).addBox(-4.5F, -3.0F, -14.0F, 9.0F, 6.0F, 5.0F)
                .texOffs(86, 87).addBox(-5.0F, -7.0F, -8.0F, 10.0F, 2.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, -1.0F, -12.0F, 0.1F, 0.0F, 0.0F));
        PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(80, 35).addBox(-5.0F, 0.0F, -12.0F, 10.0F, 3.0F, 12.0F)
                .texOffs(24, 88).addBox(-4.5F, -2.0F, -12.5F, 9.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, -1.0F, 0.12F, 0.0F, 0.0F));
        PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(80, 0).addBox(-5.0F, -2.0F, -3.5F, 5.0F, 11.0F, 7.0F),
                PartPose.offsetAndRotation(-9.0F, -3.0F, -8.0F, 0.15F, 0.0F, 0.35F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(0, 51).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 9.0F, 5.0F),
                PartPose.offsetAndRotation(-2.5F, 8.5F, 0.0F, -0.5F, 0.0F, -0.3F));
        PartDefinition right_claw = right_forearm.addOrReplaceChild("right_claw", CubeListBuilder.create()
                .texOffs(60, 97).addBox(-3.5F, -1.0F, -3.0F, 7.0F, 3.0F, 5.0F)
                .texOffs(48, 76).addBox(-3.5F, 0.0F, -12.0F, 2.0F, 2.0F, 9.0F)
                .texOffs(48, 64).addBox(-1.0F, 0.0F, -13.0F, 2.0F, 2.0F, 10.0F)
                .texOffs(96, 76).addBox(1.5F, 0.0F, -12.0F, 2.0F, 2.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 9.0F, 0.0F, 0.55F, 0.0F, 0.0F));
        PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(104, 0).addBox(0.0F, -2.0F, -3.5F, 5.0F, 11.0F, 7.0F),
                PartPose.offsetAndRotation(9.0F, -3.0F, -8.0F, 0.15F, 0.0F, -0.35F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(20, 51).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 9.0F, 5.0F),
                PartPose.offsetAndRotation(2.5F, 8.5F, 0.0F, -0.5F, 0.0F, 0.3F));
        PartDefinition left_claw = left_forearm.addOrReplaceChild("left_claw", CubeListBuilder.create()
                .texOffs(84, 97).addBox(-3.5F, -1.0F, -3.0F, 7.0F, 3.0F, 5.0F)
                .texOffs(0, 77).addBox(1.5F, 0.0F, -12.0F, 2.0F, 2.0F, 9.0F)
                .texOffs(72, 65).addBox(-1.0F, 0.0F, -13.0F, 2.0F, 2.0F, 10.0F)
                .texOffs(22, 77).addBox(-3.5F, 0.0F, -12.0F, 2.0F, 2.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 9.0F, 0.0F, 0.55F, 0.0F, 0.0F));
        PartDefinition right_leg = all.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(32, 35).addBox(-3.0F, -2.0F, -3.5F, 5.0F, 9.0F, 7.0F)
                .texOffs(70, 77).addBox(-3.0F, 7.0F, -5.0F, 5.0F, 3.0F, 7.0F),
                PartPose.offset(-8.0F, 14.0F, 7.0F));
        PartDefinition left_leg = all.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(56, 35).addBox(-2.0F, -2.0F, -3.5F, 5.0F, 9.0F, 7.0F)
                .texOffs(0, 88).addBox(-2.0F, 7.0F, -5.0F, 5.0F, 3.0F, 7.0F),
                PartPose.offset(8.0F, 14.0F, 7.0F));
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(0, 35).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 6.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, -1.0F, 10.0F, -0.25F, 0.0F, 0.0F));
        PartDefinition tail_tip = tail.addOrReplaceChild("tail_tip", CubeListBuilder.create()
                .texOffs(40, 51).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 9.0F)
                .texOffs(80, 50).addBox(-4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 10.0F, 0.2F, 0.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 128, 128).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float walk = state.walkAnimationPos * 0.6F;
        boolean roused = state.phase >= 2;

        // Idle: the flanks heave, the head swings low from side to side tasting the air, the tail drags and stirs.
        float breath = Mth.sin(age * (roused ? 0.14F : 0.08F));
        this.body.y += breath * 0.35F;
        this.head.yRot += Mth.sin(age * 0.04F) * 0.25F * (1.0F - speed);
        this.jaw.xRot += 0.08F + Math.max(0.0F, breath) * (roused ? 0.22F : 0.1F);
        this.tail.yRot += Mth.sin(age * 0.06F) * 0.2F;
        this.tailTip.yRot += Mth.sin(age * 0.06F - 0.8F) * 0.25F;
        this.head.yRot += Mth.clamp(state.yRot, -40.0F, 40.0F) * DEG * 0.7F;

        // Walking: it hauls itself on its arms, a shoulder at a time, the hind legs paddling after.
        float step = Mth.cos(walk);
        float lift = Mth.sin(walk);
        this.rightArm.xRot += step * 0.55F * speed;
        this.leftArm.xRot -= step * 0.55F * speed;
        this.rightForearm.xRot -= Math.max(0.0F, lift) * 0.4F * speed;
        this.leftForearm.xRot -= Math.max(0.0F, -lift) * 0.4F * speed;
        this.rightLeg.xRot -= step * 0.6F * speed;
        this.leftLeg.xRot += step * 0.6F * speed;
        this.body.yRot += step * 0.1F * speed;
        this.body.zRot += lift * 0.05F * speed;
        this.tail.yRot -= step * 0.25F * speed;

        float t = state.attackTime;
        switch (state.attackId) {
            case StoneEaterEntity.CLAW -> {
                this.rake(Anim.pulse(t, 0.0F, 8.0F, 10.5F, 13.5F), Anim.pulse(t, 10.5F, 13.5F, 17.0F, 26.0F), 1.0F);
                if (roused) {
                    this.rake(Anim.pulse(t, 12.0F, 19.0F, 20.5F, 23.5F), Anim.pulse(t, 20.5F, 23.5F, 28.0F, 40.0F), -1.0F);
                }
            }
            case StoneEaterEntity.LUNGE -> {
                // Sinks on its arms with the jaws coming open - then goes forward along the floor with them wide.
                float crouch = Anim.pulse(t, 0.0F, 10.0F, 13.0F, 16.0F);
                float go = Anim.pulse(t, 13.0F, 16.0F, 26.0F, 31.0F);
                float snap = Anim.ramp(t, 25.0F, 27.5F);
                this.body.y += 3.0F * crouch + 1.0F * go;
                this.body.z += 4.0F * crouch - 5.0F * go;
                this.body.xRot += 0.12F * crouch + 0.08F * go;
                this.head.xRot -= 0.25F * Math.max(crouch, go);
                this.jaw.xRot += (0.5F * crouch + 1.0F * go) * (1.0F - snap);
                this.rightArm.xRot += -0.5F * crouch + 0.9F * go;
                this.leftArm.xRot += -0.5F * crouch + 0.9F * go;
                this.rightLeg.xRot += 0.4F * crouch - 0.8F * go;
                this.leftLeg.xRot += 0.4F * crouch - 0.8F * go;
                this.tail.xRot += 0.3F * go;
            }
            case StoneEaterEntity.BURROW -> {
                // A few great strokes of both claws, alternately, and the floor takes it; it comes back up on its hind
                // legs with its arms thrown wide.
                float dig = Anim.pulse(t, 0.0F, 4.0F, 18.0F, 22.0F);
                float down = Anim.ramp(t, 6.0F, 20.0F) * (1.0F - Anim.ramp(t, 75.0F, 78.0F));
                float burst = Anim.pulse(t, 75.0F, 78.0F, 86.0F, 99.0F);
                float stroke = Mth.sin(t * 0.95F);
                this.all.y += DEPTH * down;
                this.body.xRot += 0.3F * dig - 0.55F * burst;
                this.body.y -= 4.0F * burst;
                this.head.xRot += 0.2F * dig - 0.3F * burst;
                this.rightArm.xRot += (-0.9F + stroke * 0.9F) * dig - 0.6F * burst;
                this.leftArm.xRot += (-0.9F - stroke * 0.9F) * dig - 0.6F * burst;
                this.rightForearm.xRot += stroke * 0.4F * dig;
                this.leftForearm.xRot -= stroke * 0.4F * dig;
                this.rightArm.zRot += 0.9F * burst;
                this.leftArm.zRot -= 0.9F * burst;
                this.jaw.xRot += 0.9F * burst;
                this.tail.xRot += 0.5F * burst;
            }
            case StoneEaterEntity.ROAR -> {
                // Rears until it stands on its hind legs and its tail, the arms spread, and empties its lungs at the roof.
                float rear = Anim.pulse(t, 0.0F, 13.0F, 42.0F, 57.0F);
                float voice = Anim.pulse(t, 13.0F, 17.0F, 38.0F, 46.0F);
                float shake = voice * Mth.sin(age * 2.6F) * 0.035F;
                this.body.xRot += -0.5F * rear + shake;
                this.body.y -= 4.0F * rear;
                this.head.xRot += -0.35F * rear + shake;
                this.head.zRot += shake * 2.0F;
                this.jaw.xRot += 0.3F * rear + 0.7F * voice;
                this.rightArm.xRot -= 0.6F * rear;
                this.leftArm.xRot -= 0.6F * rear;
                this.rightArm.zRot += 0.9F * rear;
                this.leftArm.zRot -= 0.9F * rear;
                this.tail.xRot += 0.5F * rear;
            }
            case StoneEaterEntity.TAIL -> {
                // The tail is drawn round to one side - the head turns to look for what is behind - and whipped across.
                float draw = Anim.pulse(t, 0.0F, 9.0F, 11.0F, 14.0F);
                float lash = Anim.pulse(t, 11.0F, 14.0F, 20.0F, 35.0F);
                this.tail.yRot += 1.1F * draw - 1.3F * lash;
                this.tailTip.yRot += 0.7F * draw - 0.9F * lash;
                this.tail.xRot += 0.25F * Math.max(draw, lash);
                this.body.yRot += -0.3F * draw + 0.4F * lash;
                this.head.yRot += 0.5F * draw - 0.3F * lash;
            }
            default -> {
            }
        }
    }

    /**
     * One rake: the arm goes up and out from the shoulder with the blades opened, and comes down and across the front.
     * {@code side} is 1 for the right arm, -1 for the left.
     */
    private void rake(float raise, float cut, float side) {
        ModelPart arm = side > 0.0F ? this.rightArm : this.leftArm;
        ModelPart forearm = side > 0.0F ? this.rightForearm : this.leftForearm;
        ModelPart claw = side > 0.0F ? this.rightClaw : this.leftClaw;
        arm.xRot += -1.5F * raise - 0.5F * cut;
        arm.zRot += side * (0.55F * raise - 0.75F * cut);
        arm.yRot += side * 0.2F * cut;
        forearm.xRot += 0.3F * raise + 0.5F * cut;
        claw.xRot -= 0.4F * raise;
        this.body.xRot += -0.12F * raise + 0.1F * cut;
        this.body.yRot += side * (0.25F * raise - 0.3F * cut);
        this.jaw.xRot += 0.5F * raise + 0.3F * cut;
    }
}
