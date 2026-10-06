package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.WallowerEntity;

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
 * The Wallower. The mesh between the markers is written by tools/gen_wallower.py (which also paints the textures); the
 * animation is written by hand. Everything hangs from the part {@code all}, which goes down through the floor when it
 * dives: a hand's depth of water hides all of it.
 */
public class WallowerModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("wallower"), "main");
    /** Must match SCALE in tools/gen_wallower.py. */
    private static final float MODEL_SCALE = 1.8F;
    private static final float DEG = (float) (Math.PI / 180.0);
    /** How far down the whole of it goes when it is under, in model units. */
    private static final float DEPTH = 17.0F;

    private final ModelPart all;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart rightWhisker;
    private final ModelPart leftWhisker;
    private final ModelPart rightBarbel;
    private final ModelPart leftBarbel;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart tail;
    private final ModelPart tailMid;
    private final ModelPart tailTip;

    public WallowerModel(ModelPart root) {
        super(root);
        this.all = root.getChild("all");
        this.body = this.all.getChild("body");
        this.head = this.body.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.rightWhisker = this.head.getChild("right_whisker");
        this.leftWhisker = this.head.getChild("left_whisker");
        this.rightBarbel = this.jaw.getChild("right_barbel");
        this.leftBarbel = this.jaw.getChild("left_barbel");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.rightLeg = this.body.getChild("right_leg");
        this.leftLeg = this.body.getChild("left_leg");
        this.tail = this.body.getChild("tail");
        this.tailMid = this.tail.getChild("tail_mid");
        this.tailTip = this.tailMid.getChild("tail_tip");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.ZERO);
        PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-8.0F, -6.0F, -12.0F, 16.0F, 11.0F, 20.0F)
                .texOffs(0, 63).addBox(-6.0F, -7.0F, -11.0F, 12.0F, 1.0F, 9.0F)
                .texOffs(0, 31).addBox(-1.0F, -9.0F, -8.0F, 2.0F, 3.0F, 15.0F),
                PartPose.offset(0.0F, 17.0F, 0.0F));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(66, 44).addBox(-9.0F, -4.0F, -10.0F, 18.0F, 7.0F, 10.0F)
                .texOffs(106, 9).addBox(-7.0F, -5.0F, -8.0F, 3.0F, 1.0F, 3.0F)
                .texOffs(48, 49).addBox(4.0F, -5.0F, -8.0F, 3.0F, 1.0F, 3.0F),
                PartPose.offset(0.0F, -1.0F, -12.0F));
        PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(48, 61).addBox(-9.0F, 0.0F, -10.0F, 18.0F, 3.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.08F, 0.0F, 0.0F));
        PartDefinition right_whisker = head.addOrReplaceChild("right_whisker", CubeListBuilder.create()
                .texOffs(98, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 14.0F, 1.0F),
                PartPose.offsetAndRotation(-9.0F, 1.0F, -9.0F, 0.35F, 0.0F, 1.3F));
        PartDefinition left_whisker = head.addOrReplaceChild("left_whisker", CubeListBuilder.create()
                .texOffs(102, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 14.0F, 1.0F),
                PartPose.offsetAndRotation(9.0F, 1.0F, -9.0F, 0.35F, 0.0F, -1.3F));
        PartDefinition right_barbel = jaw.addOrReplaceChild("right_barbel", CubeListBuilder.create()
                .texOffs(66, 31).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(-6.0F, 3.0F, -9.0F, 0.9F, 0.0F, 0.5F));
        PartDefinition left_barbel = jaw.addOrReplaceChild("left_barbel", CubeListBuilder.create()
                .texOffs(122, 41).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(6.0F, 3.0F, -9.0F, 0.9F, 0.0F, -0.5F));
        PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(106, 0).addBox(-5.0F, -1.5F, -2.0F, 5.0F, 4.0F, 5.0F)
                .texOffs(98, 15).addBox(-10.0F, 0.0F, -4.0F, 6.0F, 2.0F, 8.0F),
                PartPose.offsetAndRotation(-8.0F, 2.0F, -8.0F, 0.0F, 0.2F, 0.5F));
        PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(104, 61).addBox(0.0F, -1.5F, -2.0F, 5.0F, 4.0F, 5.0F)
                .texOffs(0, 73).addBox(4.0F, 0.0F, -4.0F, 6.0F, 2.0F, 8.0F),
                PartPose.offsetAndRotation(8.0F, 2.0F, -8.0F, 0.0F, -0.2F, -0.5F));
        PartDefinition right_leg = body.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(112, 25).addBox(-4.0F, -1.5F, -2.0F, 4.0F, 4.0F, 4.0F)
                .texOffs(104, 70).addBox(-8.0F, 0.0F, -3.0F, 5.0F, 2.0F, 6.0F),
                PartPose.offsetAndRotation(-8.0F, 2.0F, 5.0F, 0.0F, -0.3F, 0.5F));
        PartDefinition left_leg = body.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(112, 33).addBox(0.0F, -1.5F, -2.0F, 4.0F, 4.0F, 4.0F)
                .texOffs(28, 74).addBox(3.0F, 0.0F, -3.0F, 5.0F, 2.0F, 6.0F),
                PartPose.offsetAndRotation(8.0F, 2.0F, 5.0F, 0.0F, 0.3F, -0.5F));
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(72, 25).addBox(-5.0F, -4.5F, 0.0F, 10.0F, 9.0F, 10.0F)
                .texOffs(0, 49).addBox(-1.0F, -8.0F, 0.0F, 2.0F, 4.0F, 10.0F),
                PartPose.offset(0.0F, -1.0F, 8.0F));
        PartDefinition tail_mid = tail.addOrReplaceChild("tail_mid", CubeListBuilder.create()
                .texOffs(34, 31).addBox(-3.0F, -4.0F, 0.0F, 6.0F, 8.0F, 10.0F)
                .texOffs(24, 49).addBox(-1.0F, -8.0F, 0.0F, 2.0F, 4.0F, 10.0F),
                PartPose.offset(0.0F, 0.0F, 10.0F));
        PartDefinition tail_tip = tail_mid.addOrReplaceChild("tail_tip", CubeListBuilder.create()
                .texOffs(72, 0).addBox(-1.0F, -7.0F, 0.0F, 2.0F, 14.0F, 11.0F),
                PartPose.offset(0.0F, 0.0F, 10.0F));
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

        // Lying still: the gills work, the mouth opens a finger and shuts, the barbels feel the water on their own, the
        // tail lies to one side and comes slowly over to the other.
        float breath = Mth.sin(age * (roused ? 0.13F : 0.07F));
        this.body.y += breath * 0.3F;
        this.jaw.xRot += 0.05F + Math.max(0.0F, breath) * (roused ? 0.2F : 0.09F);
        this.rightWhisker.yRot += Mth.sin(age * 0.09F) * 0.3F;
        this.leftWhisker.yRot -= Mth.sin(age * 0.08F + 1.4F) * 0.3F;
        this.rightWhisker.zRot += Mth.sin(age * 0.05F) * 0.12F;
        this.leftWhisker.zRot -= Mth.sin(age * 0.06F + 0.7F) * 0.12F;
        this.rightBarbel.xRot += Mth.sin(age * 0.11F) * 0.2F;
        this.leftBarbel.xRot += Mth.sin(age * 0.1F + 2.0F) * 0.2F;
        float lie = Mth.sin(age * 0.03F);
        this.tail.yRot += lie * 0.3F;
        this.tailMid.yRot += lie * 0.35F;
        this.tailTip.yRot += Mth.sin(age * 0.03F - 0.6F) * 0.4F;
        this.head.yRot += Mth.clamp(state.yRot, -35.0F, 35.0F) * DEG * 0.6F;

        // Going: it rows itself along on its hands, the body bending from side to side like a newt's and the tail after it.
        float bend = Mth.sin(walk);
        this.body.yRot += bend * 0.14F * speed;
        this.head.yRot -= bend * 0.1F * speed;
        this.tail.yRot -= bend * 0.4F * speed;
        this.tailMid.yRot -= Mth.sin(walk - 0.8F) * 0.45F * speed;
        this.tailTip.yRot -= Mth.sin(walk - 1.6F) * 0.5F * speed;
        this.rightArm.yRot += bend * 0.6F * speed;
        this.leftArm.yRot += bend * 0.6F * speed;
        this.rightLeg.yRot -= bend * 0.5F * speed;
        this.leftLeg.yRot -= bend * 0.5F * speed;

        float t = state.attackTime;
        switch (state.attackId) {
            case WallowerEntity.SNAP -> {
                // It draws back on its arms with the mouth coming open - and goes forward its own head's length with it wide.
                float back = Anim.pulse(t, 0.0F, 8.0F, 9.0F, 12.0F);
                float go = Anim.pulse(t, 9.0F, 12.0F, 16.0F, 28.0F);
                float shut = Anim.ramp(t, 12.0F, 13.5F);
                this.body.z += 4.0F * back - 7.0F * go;
                this.head.xRot -= 0.3F * Math.max(back, go) * (1.0F - shut * 0.6F);
                this.jaw.xRot += (0.6F * back + 0.95F * go) * (1.0F - shut);
                this.rightArm.yRot += 0.5F * back - 0.7F * go;
                this.leftArm.yRot += -0.5F * back + 0.7F * go;
                this.tail.xRot += 0.2F * go;
            }
            case WallowerEntity.TAIL -> {
                // The tail is carried round to one side as far as it will go - the head turns to look - and comes back
                // flat along the water.
                float draw = Anim.pulse(t, 0.0F, 10.0F, 12.0F, 15.0F);
                float lash = Anim.pulse(t, 12.0F, 15.0F, 21.0F, 36.0F);
                this.tail.yRot += 0.9F * draw - 1.1F * lash;
                this.tailMid.yRot += 0.8F * draw - 1.0F * lash;
                this.tailTip.yRot += 0.6F * draw - 0.8F * lash;
                this.body.yRot += -0.3F * draw + 0.4F * lash;
                this.head.yRot += 0.5F * draw - 0.3F * lash;
            }
            case WallowerEntity.SPIT -> {
                // The head comes up, the throat fills - and it jerks forward once for every clot.
                float last = roused ? WallowerEntity.SPIT_DURATION_THREE : WallowerEntity.SPIT_DURATION;
                float rear = Anim.pulse(t, 0.0F, 14.0F, last - 16.0F, last - 2.0F);
                float jerk = 0.0F;
                for (int i = 0; i < (roused ? 3 : 1); i++) {
                    float at = WallowerEntity.SPIT_FIRST + i * WallowerEntity.SPIT_EVERY;
                    jerk += Anim.pulse(t, at - 2.0F, at, at + 1.0F, at + 5.0F);
                }
                this.body.xRot -= 0.2F * rear;
                this.head.xRot += -0.5F * rear + 0.35F * jerk;
                this.jaw.xRot += 0.3F * rear + 0.5F * jerk;
                this.body.z -= 2.0F * jerk;
                this.rightWhisker.zRot += 0.4F * rear;
                this.leftWhisker.zRot -= 0.4F * rear;
            }
            case WallowerEntity.DIVE -> {
                // It puts its head down and is gone; it comes back up standing on its tail with its arms out and its
                // mouth open, and falls forward.
                float nod = Anim.pulse(t, 0.0F, 6.0F, 16.0F, 20.0F);
                float down = Anim.ramp(t, 5.0F, WallowerEntity.DIVE_UNDER) * (1.0F - Anim.ramp(t, WallowerEntity.DIVE_BREACH - 1.0F, WallowerEntity.DIVE_BREACH + 2.0F));
                float burst = Anim.pulse(t, WallowerEntity.DIVE_BREACH - 1.0F, WallowerEntity.DIVE_BREACH + 2.0F, WallowerEntity.DIVE_BREACH + 8.0F, WallowerEntity.DIVE_DURATION - 4.0F);
                this.all.y += DEPTH * down - 9.0F * burst;
                this.body.xRot += 0.3F * nod - 0.9F * burst;
                this.head.xRot += 0.25F * nod - 0.3F * burst;
                this.jaw.xRot += 1.1F * burst;
                this.rightArm.zRot -= 0.9F * burst;
                this.leftArm.zRot += 0.9F * burst;
                this.tail.xRot += 0.6F * burst;
                this.tailMid.xRot += 0.5F * burst;
            }
            case WallowerEntity.GULP -> {
                // The head goes back and the mouth opens to twice its width, and stays so while the breath goes in - the
                // whole of it shaking - and at the end of the breath it throws itself forward and the mouth shuts.
                float open = Anim.pulse(t, 0.0F, 11.0F, WallowerEntity.GULP_BITE - 3.0F, WallowerEntity.GULP_BITE);
                float bite = Anim.pulse(t, WallowerEntity.GULP_BITE - 3.0F, WallowerEntity.GULP_BITE, WallowerEntity.GULP_BITE + 6.0F, WallowerEntity.GULP_DURATION - 2.0F);
                float shake = open * Mth.sin(age * 2.4F) * 0.03F;
                this.body.xRot += -0.12F * open + shake;
                this.head.xRot += -0.45F * open + 0.2F * bite + shake;
                this.jaw.xRot += 1.25F * open;
                this.body.z += 2.0F * open - 7.0F * bite;
                this.rightArm.yRot -= 0.5F * open;
                this.leftArm.yRot += 0.5F * open;
                this.rightWhisker.yRot -= 0.9F * open;
                this.leftWhisker.yRot += 0.9F * open;
                this.rightBarbel.xRot -= 0.8F * open;
                this.leftBarbel.xRot -= 0.8F * open;
            }
            default -> {
            }
        }
    }
}
