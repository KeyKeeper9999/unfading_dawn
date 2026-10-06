package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.WeepingMotherEntity;

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
 * The Weeping Mother. The mesh between the markers is written by tools/gen_mother.py (which also paints the textures and
 * shows the poses below on a sheet); the animation is written by hand.
 * <p>
 * She is rooted: the roots do not turn with her (they are turned back by as much as the game turns the whole of her),
 * and the hair hangs whatever she bends by. Her moves are thought out by where her hands are - a hand's breadth over
 * the ground, six blocks out - and the numbers were read off the generator's sheet, not guessed.
 */
public class WeepingMotherModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("weeping_mother"), "main");
    /** Must match SCALE in tools/gen_mother.py. */
    private static final float MODEL_SCALE = 2.0F;
    private static final float DEG = (float) (Math.PI / 180.0);

    private final ModelPart roots;
    private final ModelPart trunk;
    private final ModelPart torso;
    private final ModelPart head;
    private final ModelPart hairBack;
    private final ModelPart hairRight;
    private final ModelPart hairLeft;
    private final ModelPart hairFront;
    private final ModelPart rightArm;
    private final ModelPart rightForearm;
    private final ModelPart leftArm;
    private final ModelPart leftForearm;

    public WeepingMotherModel(ModelPart root) {
        super(root);
        ModelPart all = root.getChild("all");
        this.roots = all.getChild("roots");
        this.trunk = all.getChild("trunk");
        this.torso = this.trunk.getChild("torso");
        this.head = this.torso.getChild("head");
        this.hairBack = this.head.getChild("hair_back");
        this.hairRight = this.head.getChild("hair_right");
        this.hairLeft = this.head.getChild("hair_left");
        this.hairFront = this.head.getChild("hair_front");
        this.rightArm = this.torso.getChild("right_arm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftArm = this.torso.getChild("left_arm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.ZERO);
        PartDefinition roots = all.addOrReplaceChild("roots", CubeListBuilder.create()
                .texOffs(60, 0).addBox(-8.0F, -4.0F, -8.0F, 16.0F, 4.0F, 16.0F)
                .texOffs(28, 61).addBox(-15.0F, -2.0F, -2.0F, 7.0F, 2.0F, 4.0F)
                .texOffs(0, 65).addBox(8.0F, -2.0F, -3.0F, 7.0F, 2.0F, 4.0F)
                .texOffs(72, 56).addBox(-3.0F, -2.0F, -15.0F, 4.0F, 2.0F, 7.0F)
                .texOffs(94, 56).addBox(-1.0F, -2.0F, 8.0F, 4.0F, 2.0F, 7.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition trunk = all.addOrReplaceChild("trunk", CubeListBuilder.create()
                .texOffs(0, 33).addBox(-5.0F, -9.0F, -4.0F, 10.0F, 9.0F, 8.0F)
                .texOffs(60, 40).addBox(-4.0F, -18.0F, -3.0F, 8.0F, 9.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 20.0F, 0.0F, 0.06F, 0.0F, 0.0F));
        PartDefinition torso = trunk.addOrReplaceChild("torso", CubeListBuilder.create()
                .texOffs(92, 20).addBox(-6.0F, -13.0F, -3.0F, 12.0F, 13.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, -18.0F, 0.0F, 0.12F, 0.0F, 0.0F));
        PartDefinition head = torso.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 50).addBox(-3.5F, -8.0F, -3.5F, 7.0F, 8.0F, 7.0F)
                .texOffs(120, 39).addBox(-1.0F, -13.0F, -1.0F, 2.0F, 5.0F, 2.0F)
                .texOffs(54, 48).addBox(-4.0F, -11.0F, 0.0F, 2.0F, 4.0F, 1.0F)
                .texOffs(120, 46).addBox(2.0F, -12.0F, -1.0F, 1.0F, 5.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -13.0F, 0.0F, 0.22F, 0.0F, 0.0F));
        PartDefinition hair_back = head.addOrReplaceChild("hair_back", CubeListBuilder.create()
                .texOffs(36, 0).addBox(-5.5F, 0.0F, 0.0F, 11.0F, 28.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -8.0F, 3.5F, 0.06F, 0.0F, 0.0F));
        PartDefinition hair_right = head.addOrReplaceChild("hair_right", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.0F, 0.0F, -4.0F, 1.0F, 25.0F, 8.0F),
                PartPose.offsetAndRotation(-3.5F, -8.0F, 0.0F, 0.0F, 0.0F, 0.07F));
        PartDefinition hair_left = head.addOrReplaceChild("hair_left", CubeListBuilder.create()
                .texOffs(18, 0).addBox(0.0F, 0.0F, -4.0F, 1.0F, 25.0F, 8.0F),
                PartPose.offsetAndRotation(3.5F, -8.0F, 0.0F, 0.0F, 0.0F, -0.07F));
        PartDefinition hair_front = head.addOrReplaceChild("hair_front", CubeListBuilder.create()
                .texOffs(84, 20).addBox(-5.5F, 0.0F, -1.0F, 3.0F, 19.0F, 1.0F)
                .texOffs(112, 39).addBox(2.5F, 0.0F, -1.0F, 3.0F, 16.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -8.0F, -3.5F, -0.07F, 0.0F, 0.0F));
        PartDefinition right_arm = torso.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(60, 20).addBox(-2.0F, -1.5F, -1.5F, 3.0F, 17.0F, 3.0F),
                PartPose.offsetAndRotation(-7.0F, -11.0F, 0.0F, 0.08F, 0.0F, 0.14F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(36, 29).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 16.0F, 3.0F)
                .texOffs(36, 48).addBox(-2.5F, 16.0F, -2.0F, 5.0F, 9.0F, 4.0F)
                .texOffs(92, 39).addBox(-0.5F, 2.0F, 1.5F, 1.0F, 13.0F, 4.0F),
                PartPose.offsetAndRotation(-0.5F, 15.5F, 0.0F, -0.3F, 0.0F, 0.0F));
        PartDefinition left_arm = torso.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(72, 20).addBox(-1.0F, -1.5F, -1.5F, 3.0F, 17.0F, 3.0F),
                PartPose.offsetAndRotation(7.0F, -11.0F, 0.0F, 0.08F, 0.0F, -0.14F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(48, 29).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 16.0F, 3.0F)
                .texOffs(54, 55).addBox(-2.5F, 16.0F, -2.0F, 5.0F, 9.0F, 4.0F)
                .texOffs(102, 39).addBox(-0.5F, 2.0F, 1.5F, 1.0F, 13.0F, 4.0F),
                PartPose.offsetAndRotation(0.5F, 15.5F, 0.0F, -0.3F, 0.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 128, 128).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    /** How far a part has been bent forward from the way it was made. */
    private static float bent(ModelPart part) {
        PartPose made = part.getInitialPose();
        return part.xRot - made.xRot();
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        boolean roused = state.phase >= 2;
        float quick = roused ? 1.7F : 1.0F;

        // The roots stay where they grew.
        this.roots.yRot = -state.bodyRot * DEG;

        // Standing: she sways as a tree does and breathes as a tree does not; the head hangs, and every so often it
        // shakes with a sob. Her arms hang to the ground and stir. Roused, the head is up.
        float sway = Mth.sin(age * 0.04F * quick);
        this.trunk.zRot += sway * 0.02F;
        this.torso.zRot += sway * 0.03F;
        this.torso.xRot += Mth.sin(age * 0.06F * quick) * 0.015F;
        this.head.zRot += Mth.sin(age * 0.04F * quick - 0.5F) * 0.05F;
        float sob = (float) Math.pow(Math.max(0.0F, Mth.sin(age * 0.045F)), 8.0) * Mth.sin(age * 1.1F);
        this.head.xRot += sob * 0.05F + (roused ? -0.3F : 0.0F);
        this.torso.xRot += sob * 0.02F;
        this.rightArm.xRot += Mth.sin(age * 0.05F * quick) * 0.04F;
        this.leftArm.xRot += Mth.sin(age * 0.05F * quick + 1.9F) * 0.04F;
        this.rightForearm.xRot += Mth.sin(age * 0.06F * quick + 0.6F) * 0.05F;
        this.leftForearm.xRot += Mth.sin(age * 0.06F * quick + 2.6F) * 0.05F;
        if (roused) {
            this.rightArm.zRot += 0.16F;
            this.leftArm.zRot -= 0.16F;
        }
        // she looks at whoever she is fighting from the breast up
        float look = Mth.clamp(state.yRot, -70.0F, 70.0F) * DEG;
        this.torso.yRot += look * 0.45F;
        this.head.yRot += look * 0.45F;
        this.head.xRot += Mth.clamp(state.xRot, -20.0F, 30.0F) * DEG * 0.3F;

        float fly = 0.0F;                 // how far the hair stands out from her, when she turns or throws her head back
        float t = state.attackTime;
        switch (state.attackId) {
            case WeepingMotherEntity.LASH_RIGHT, WeepingMotherEntity.LASH_LEFT -> {
                // The arm is carried up and out to its own side, the breast turning after it; then she goes down from the
                // bole and the breast, and the arm comes round level and low to the other side, straight as a pole.
                boolean right = state.attackId == WeepingMotherEntity.LASH_RIGHT;
                float side = right ? 1.0F : -1.0F;
                ModelPart arm = right ? this.rightArm : this.leftArm;
                ModelPart forearm = right ? this.rightForearm : this.leftForearm;
                ModelPart other = right ? this.leftArm : this.rightArm;
                float back = Anim.ramp(t, 27.0F, 39.0F);
                float high = Anim.ramp(t, 0.0F, 10.0F) * (1.0F - Anim.ramp(t, 11.0F, 16.0F));
                float low = Anim.ramp(t, 11.0F, 16.0F) * (1.0F - back);
                float round = Anim.ramp(t, 0.0F, 10.0F) * (1.0F - back) * Anim.lerp(Anim.ramp(t, 12.0F, 20.0F), 1.0F, -1.0F);
                this.trunk.yRot += side * 0.5F * round;
                this.torso.yRot += side * 0.45F * round;
                arm.yRot += side * 0.35F * round;
                this.trunk.xRot += -0.1F * high + 0.24F * low;
                this.torso.xRot += -0.15F * high + 0.48F * low;
                arm.xRot += -1.38F * high - 1.965F * low;
                arm.zRot += side * 0.36F * high;
                forearm.xRot += 0.5F * high + 0.3F * low;
                other.xRot -= 0.7F * low;
                this.head.xRot -= 0.3F * low;
            }
            case WeepingMotherEntity.SLAM -> {
                // Both arms go up over her head, the head back with them - and the whole of her comes down after them,
                // hands together, onto one place.
                float up = Anim.pulse(t, 0.0F, 14.0F, 19.0F, WeepingMotherEntity.SLAM_IMPACT);
                float down = Anim.pulse(t, 19.0F, WeepingMotherEntity.SLAM_IMPACT, 36.0F, 50.0F);
                float jar = Anim.pulse(t, WeepingMotherEntity.SLAM_IMPACT, WeepingMotherEntity.SLAM_IMPACT + 1.0F, WeepingMotherEntity.SLAM_IMPACT + 2.0F,
                        WeepingMotherEntity.SLAM_IMPACT + 9.0F) * Mth.sin(age * 2.6F);
                this.trunk.xRot += 0.29F * down + 0.02F * jar;
                this.torso.xRot += -0.37F * up + 0.48F * down + 0.03F * jar;
                this.head.xRot += -0.62F * up - 0.5F * down;
                for (ModelPart arm : new ModelPart[] {this.rightArm, this.leftArm}) {
                    float side = arm == this.rightArm ? 1.0F : -1.0F;
                    arm.xRot += -2.98F * up - 2.0F * down;
                    arm.zRot += side * (0.06F * up - 0.1F * down);
                }
                this.rightForearm.xRot += 0.6F * up + 0.3F * down;
                this.leftForearm.xRot += 0.6F * up + 0.3F * down;
                fly = 0.35F * up;
            }
            case WeepingMotherEntity.ROOTS -> {
                // She bows over and puts both hands to the ground at her own foot, and leans on them once for every root
                // she sends; her own roots heave.
                float last = roused ? WeepingMotherEntity.ROOTS_DURATION_MANY : WeepingMotherEntity.ROOTS_DURATION;
                float hold = Anim.pulse(t, 0.0F, 12.0F, last - 14.0F, last - 2.0F);
                float push = 0.0F;
                for (int i = 0; i < (roused ? 5 : 3); i++) {
                    float at = WeepingMotherEntity.ROOTS_FIRST + i * WeepingMotherEntity.ROOTS_EVERY;
                    push += Anim.pulse(t, at - 3.0F, at, at + 1.0F, at + 6.0F);
                }
                this.trunk.xRot += 0.3F * hold + 0.05F * push;
                this.torso.xRot += 0.5F * hold + 0.07F * push;
                this.rightArm.xRot -= 0.5F * hold;
                this.leftArm.xRot -= 0.5F * hold;
                this.rightArm.zRot += 0.3F * hold;
                this.leftArm.zRot -= 0.3F * hold;
                this.rightForearm.xRot += 0.6F * hold;
                this.leftForearm.xRot += 0.6F * hold;
                float heave = 1.0F + 0.07F * hold * Mth.sin(age * 0.9F) + 0.08F * push;
                this.roots.xScale = heave;
                this.roots.zScale = heave;
            }
            case WeepingMotherEntity.WAIL -> {
                // She straightens, the head goes right back and the arms out and open, and she shakes with it.
                float rise = Anim.pulse(t, 4.0F, 18.0F, 40.0F, 54.0F);
                float cry = Anim.pulse(t, WeepingMotherEntity.WAIL_CRY - 2.0F, WeepingMotherEntity.WAIL_CRY, 38.0F, 44.0F);
                float shake = cry * Mth.sin(age * 1.9F) * 0.035F;
                this.torso.xRot += -0.47F * rise + shake;
                this.head.xRot += -0.92F * rise + shake;
                this.head.zRot += shake * 1.5F;
                this.rightArm.xRot += -0.48F * rise;
                this.leftArm.xRot += -0.48F * rise;
                this.rightArm.zRot += 0.96F * rise + shake;
                this.leftArm.zRot -= 0.96F * rise + shake;
                this.rightForearm.xRot += 0.9F * rise;
                this.leftForearm.xRot += 0.9F * rise;
                fly = 0.5F * rise;
            }
            case WeepingMotherEntity.WHIRL -> {
                // The arms come out to the sides, straight and low, and she goes round on her bole twice - once for each
                // time they strike - with the hair standing out behind.
                float out = Anim.pulse(t, 2.0F, 13.0F, 45.0F, 60.0F);
                float turn = Mth.clamp((t - 14.5F) / 30.0F, 0.0F, 1.0F);
                this.trunk.yRot += turn * Mth.TWO_PI * 2.0F;
                this.trunk.xRot += 0.1F * out;
                this.rightArm.zRot += 0.76F * out;
                this.leftArm.zRot -= 0.76F * out;
                this.rightForearm.xRot += 0.3F * out;
                this.leftForearm.xRot += 0.3F * out;
                fly = out * (turn > 0.0F && turn < 1.0F ? 1.0F : 0.5F);
                this.hairRight.zRot += 0.7F * fly;
                this.hairLeft.zRot -= 0.7F * fly;
            }
            default -> {
            }
        }

        // Last, the hair: it hangs straight down whatever the bole, the breast and the head have been bent by, stirs
        // a little by itself, and stands out when she moves fast.
        float lean = bent(this.trunk) + bent(this.torso) + bent(this.head);
        float stir = Mth.sin(age * 0.07F * quick);
        this.hairBack.xRot += -lean * 0.9F + stir * 0.04F + 0.7F * fly;
        this.hairFront.xRot += -lean * 0.9F - stir * 0.03F - 0.5F * fly;
        this.hairRight.xRot += -lean * 0.9F;
        this.hairLeft.xRot += -lean * 0.9F;
        this.hairRight.zRot += Mth.sin(age * 0.06F * quick + 1.0F) * 0.03F;
        this.hairLeft.zRot += Mth.sin(age * 0.06F * quick + 2.2F) * 0.03F;
    }
}
