package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.WitheredEntity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * The withered: an ordinary humanoid with thin arms and a stoop, plus the things each kind wears (shown only on that kind).
 * The mesh between the markers is written by tools/gen_withered.py, which also paints the textures.
 */
public class WitheredModel extends HumanoidModel<SoulsRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("withered"), "main");
    /** How far the body leans forward. */
    private static final float STOOP = 0.24F;
    /** The butcher is twice the man the others are and carries himself straighter. */
    private final boolean bulky;

    public WitheredModel(ModelPart root, WitheredEntity.Kind kind) {
        super(root);
        this.bulky = kind == WitheredEntity.Kind.BUTCHER;
        this.body.getChild("bulk").visible = this.bulky;
        this.body.getChild("apron").visible = this.bulky;
        this.rightArm.getChild("brawn_r").visible = this.bulky;
        this.leftArm.getChild("brawn_l").visible = this.bulky;
        this.body.getChild("collar").visible = kind == WitheredEntity.Kind.PRISONER;
        this.leftArm.getChild("shackle").visible = kind == WitheredEntity.Kind.PRISONER;
        this.head.getChild("kettle_hat").visible = kind == WitheredEntity.Kind.GUARD || kind == WitheredEntity.Kind.WATCHMAN;
        this.head.getChild("cap").visible = kind == WitheredEntity.Kind.TOWNSMAN;
        this.body.getChild("belly").visible = kind == WitheredEntity.Kind.BUTCHER;
        this.body.getChild("coat_tail").visible = kind == WitheredEntity.Kind.WATCHMAN;
        this.rightArm.getChild("pauldron").visible = kind == WitheredEntity.Kind.GUARD;
        this.head.getChild("hood_tail").visible = kind == WitheredEntity.Kind.ARCHER || kind == WitheredEntity.Kind.ACOLYTE;
        this.body.getChild("quiver").visible = kind == WitheredEntity.Kind.ARCHER;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        PartDefinition body = root.getChild("body");
        // <generated mesh>
        PartDefinition right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(1.9F, 12.0F, 0.0F));
        PartDefinition collar = body.addOrReplaceChild("collar", CubeListBuilder.create()
                .texOffs(32, 74).addBox(-4.5F, -0.6F, -2.5F, 9.0F, 2.0F, 5.0F),
                PartPose.ZERO);
        PartDefinition shackle = left_arm.addOrReplaceChild("shackle", CubeListBuilder.create()
                .texOffs(46, 49).addBox(-2.0F, 0.0F, -2.5F, 4.0F, 2.0F, 5.0F)
                .texOffs(0, 53).addBox(-0.5F, 2.0F, -3.3F, 1.0F, 6.0F, 1.0F),
                PartPose.offset(0.5F, 7.0F, 0.0F));
        PartDefinition kettle_hat = head.addOrReplaceChild("kettle_hat", CubeListBuilder.create()
                .texOffs(0, 82).addBox(-6.0F, -6.6F, -6.0F, 12.0F, 1.0F, 12.0F)
                .texOffs(0, 95).addBox(-4.5F, -9.6F, -4.5F, 9.0F, 3.0F, 9.0F),
                PartPose.ZERO);
        PartDefinition pauldron = right_arm.addOrReplaceChild("pauldron", CubeListBuilder.create()
                .texOffs(44, 32).addBox(-3.0F, -3.0F, -3.0F, 4.0F, 3.0F, 6.0F),
                PartPose.ZERO);
        PartDefinition hood_tail = head.addOrReplaceChild("hood_tail", CubeListBuilder.create()
                .texOffs(0, 46).addBox(-2.0F, -1.0F, -1.0F, 4.0F, 4.0F, 3.0F)
                .texOffs(54, 27).addBox(-1.0F, 3.0F, 0.5F, 2.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -6.5F, 4.0F, 0.45F, 0.0F, 0.0F));
        PartDefinition quiver = body.addOrReplaceChild("quiver", CubeListBuilder.create()
                .texOffs(54, 16).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 9.0F, 2.0F)
                .texOffs(4, 53).addBox(-1.0F, -2.0F, 0.5F, 2.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(2.2F, 1.0F, 2.0F, 0.0F, 0.0F, 0.32F));
        PartDefinition cap = head.addOrReplaceChild("cap", CubeListBuilder.create()
                .texOffs(0, 107).addBox(-4.5F, -9.4F, -4.5F, 9.0F, 2.0F, 9.0F)
                .texOffs(46, 56).addBox(-3.5F, -8.0F, -6.5F, 7.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.07F));
        PartDefinition bulk = body.addOrReplaceChild("bulk", CubeListBuilder.create()
                .texOffs(0, 64).addBox(-5.0F, 0.0F, -3.0F, 10.0F, 12.0F, 6.0F, new CubeDeformation(0.25F)),
                PartPose.ZERO);
        PartDefinition belly = body.addOrReplaceChild("belly", CubeListBuilder.create()
                .texOffs(36, 95).addBox(-4.0F, 4.0F, -5.0F, 8.0F, 7.0F, 2.0F),
                PartPose.ZERO);
        PartDefinition apron = body.addOrReplaceChild("apron", CubeListBuilder.create()
                .texOffs(46, 41).addBox(-4.0F, 0.0F, -0.5F, 8.0F, 7.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 11.0F, -3.8F, -0.24F, 0.0F, 0.0F));
        PartDefinition brawn_r = right_arm.addOrReplaceChild("brawn_r", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-3.0F, -3.0F, -3.0F, 5.0F, 8.0F, 6.0F),
                PartPose.ZERO);
        PartDefinition brawn_l = left_arm.addOrReplaceChild("brawn_l", CubeListBuilder.create()
                .texOffs(22, 32).addBox(-2.0F, -3.0F, -3.0F, 5.0F, 8.0F, 6.0F),
                PartPose.ZERO);
        PartDefinition coat_tail = body.addOrReplaceChild("coat_tail", CubeListBuilder.create()
                .texOffs(32, 64).addBox(-4.0F, 12.0F, -2.0F, 8.0F, 6.0F, 4.0F, new CubeDeformation(0.45F)),
                PartPose.ZERO);
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 128);
    }

    @Override
    public void setupAnim(SoulsRenderState state) {
        super.setupAnim(state);
        // The stoop: the trunk leans forward from the hips, the head hangs, the arms dangle in front.
        float stoop = this.bulky ? 0.5F : 1.0F;
        this.body.xRot += STOOP * stoop;
        this.rightLeg.z += 2.9F * stoop;
        this.leftLeg.z += 2.9F * stoop;
        this.rightLeg.y -= 0.4F * stoop;
        this.leftLeg.y -= 0.4F * stoop;
        this.head.xRot += 0.18F * stoop;
        if (this.bulky) {
            // his arms hang wide of his bulk
            this.rightArm.x -= 1.5F;
            this.leftArm.x += 1.5F;
            this.rightArm.zRot += 0.1F;
            this.leftArm.zRot -= 0.1F;
        }
        float sway = Mth.sin(state.ageInTicks * 0.07F) * 0.05F;
        this.head.zRot += sway;
        boolean busy = state.isUsingItem;
        if (!busy) {
            this.rightArm.xRot -= 0.22F;
            this.leftArm.xRot -= 0.22F;
            this.rightArm.zRot += 0.06F + sway;
            this.leftArm.zRot -= 0.06F + sway;
        }

        if (state.attackId == WitheredEntity.CAST) {
            // The empty hand goes up over the head, hangs there while the light gathers, and comes down pointing at the target.
            float t = state.attackTime;
            float raise = Anim.pulse(t, 0.0F, 10.0F, 24.0F, 28.0F);
            float point = Anim.pulse(t, 24.0F, 28.0F, 34.0F, 43.0F);
            this.rightArm.xRot = Anim.lerp(raise, this.rightArm.xRot, -2.9F);
            this.rightArm.xRot = Anim.lerp(point, this.rightArm.xRot, -1.45F);
            this.rightArm.zRot += 0.25F * raise;
            this.head.xRot += -0.3F * raise + 0.1F * point;
            this.body.xRot += -0.1F * raise + 0.15F * point;
        }
        if (state.attackId == WitheredEntity.CLEAVE) {
            // The butcher's cut: the cleaver is drawn far back to his right, his whole trunk turning with it, held there for
            // a breath, and brought round level at the height of a man's chest; it carries him half round after it.
            float t = state.attackTime;
            float draw = Anim.pulse(t, 0.0F, 10.0F, 13.0F, 16.0F);
            float cut = Anim.pulse(t, 13.0F, 16.0F, 22.0F, 33.0F);
            float up = Math.max(draw, cut);
            this.rightArm.xRot = Anim.lerp(up, this.rightArm.xRot, -1.35F);
            this.rightArm.zRot = Anim.lerp(up, this.rightArm.zRot, 0.0F);
            this.rightArm.yRot += -1.25F * draw + 0.95F * cut;
            this.body.yRot += -0.6F * draw + 0.6F * cut;
            this.body.xRot += -0.06F * draw + 0.2F * cut;
            this.head.yRot += 0.35F * draw - 0.25F * cut;
            this.leftArm.xRot += 0.35F * draw - 0.5F * cut;
            this.leftArm.zRot -= 0.25F * up;
            this.rightLeg.xRot += 0.3F * draw - 0.35F * cut;
            this.leftLeg.xRot += -0.25F * draw + 0.3F * cut;
        }
        if (state.attackId == WitheredEntity.THRUST) {
            // A held thing points forward when the arm hangs, so a pitchfork is a pike carried at the hip. It is drawn back
            // past the body, the other hand coming across to steady it, and then driven forward and up at the chest with
            // a long step, the trunk going in behind it.
            float t = state.attackTime;
            float ready = Anim.pulse(t, 0.0F, 9.0F, 12.0F, 14.5F);
            float thrust = Anim.pulse(t, 12.0F, 14.5F, 19.0F, 27.0F);
            float held = Math.max(ready, thrust);
            this.rightArm.xRot = Anim.lerp(held, this.rightArm.xRot, 0.0F) + 0.4F * ready - 0.5F * thrust;
            this.rightArm.zRot = Anim.lerp(held, this.rightArm.zRot, 0.08F);
            this.rightArm.z += 5.0F * ready - 7.5F * thrust;
            this.rightArm.y += 1.0F * thrust;
            this.leftArm.xRot = Anim.lerp(held, this.leftArm.xRot, -0.75F) - 0.45F * thrust;
            this.leftArm.yRot += 0.55F * held;
            this.leftArm.z -= 3.0F * thrust;
            this.body.yRot += -0.4F * ready + 0.25F * thrust;
            this.body.xRot += -0.1F * ready + 0.3F * thrust;
            this.head.xRot -= 0.25F * thrust;
            this.rightLeg.xRot += -0.2F * ready + 0.6F * thrust;
            this.leftLeg.xRot += 0.15F * ready - 0.7F * thrust;
        }
        if (state.attackId == WitheredEntity.ALARM) {
            // The lantern goes up over his head and is shaken; he throws his head back to shout.
            float t = state.attackTime;
            float up = Anim.pulse(t, 0.0F, 8.0F, 26.0F, 33.0F);
            float shake = up * Mth.sin(t * 1.5F) * 0.22F;
            this.leftArm.xRot = Anim.lerp(up, this.leftArm.xRot, -2.75F);
            this.leftArm.zRot += shake - 0.2F * up;
            this.head.xRot -= 0.45F * up;
            this.body.xRot -= 0.2F * up;
        }
        if (state.attackId == WitheredEntity.SWING) {
            float t = state.attackTime;
            // The arm goes up and back, hangs for a moment, comes down hard and returns.
            float raise = Anim.pulse(t, 0.0F, 7.0F, 8.5F, 10.5F);
            float strike = Anim.pulse(t, 8.5F, 10.5F, 13.0F, 19.0F);
            this.rightArm.xRot = Anim.lerp(raise, this.rightArm.xRot, -2.75F);
            this.rightArm.xRot = Anim.lerp(strike, this.rightArm.xRot, -0.75F);
            this.rightArm.zRot += 0.35F * raise - 0.2F * strike;
            this.body.yRot += 0.35F * raise - 0.45F * strike;
            this.body.xRot += -0.12F * raise + 0.3F * strike;
            this.head.xRot += -0.25F * raise + 0.2F * strike;
            if (!state.armed) {
                // Bare hands: both arms claw at once.
                this.leftArm.xRot = Anim.lerp(raise, this.leftArm.xRot, -2.4F);
                this.leftArm.xRot = Anim.lerp(strike, this.leftArm.xRot, -0.9F);
                this.leftArm.zRot -= 0.3F * raise;
            }
        }
    }
}
