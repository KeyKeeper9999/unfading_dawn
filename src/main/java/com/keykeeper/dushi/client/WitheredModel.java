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
    private final WitheredEntity.Kind kind;
    /** The barrowman's barrow: it hangs from the root, before him, and turns about its axle. */
    private final ModelPart barrow;

    public WitheredModel(ModelPart root, WitheredEntity.Kind kind) {
        super(root);
        this.bulky = kind == WitheredEntity.Kind.BUTCHER;
        this.kind = kind;
        this.barrow = root.getChild("barrow");
        this.barrow.visible = kind == WitheredEntity.Kind.BARROWMAN;
        this.head.getChild("candle_cap").visible = kind == WitheredEntity.Kind.DIGGER;
        this.body.getChild("keg").visible = kind == WitheredEntity.Kind.SAPPER;
        this.body.getChild("bulk").visible = this.bulky;
        this.body.getChild("apron").visible = this.bulky;
        this.rightArm.getChild("brawn_r").visible = this.bulky || kind == WitheredEntity.Kind.BARROWMAN;
        this.leftArm.getChild("brawn_l").visible = this.bulky || kind == WitheredEntity.Kind.BARROWMAN;
        this.body.getChild("collar").visible = kind == WitheredEntity.Kind.PRISONER;
        this.leftArm.getChild("shackle").visible = kind == WitheredEntity.Kind.PRISONER;
        this.head.getChild("kettle_hat").visible = kind == WitheredEntity.Kind.GUARD || kind == WitheredEntity.Kind.WATCHMAN;
        this.head.getChild("cap").visible = kind == WitheredEntity.Kind.TOWNSMAN;
        this.body.getChild("belly").visible = kind == WitheredEntity.Kind.BUTCHER;
        this.body.getChild("coat_tail").visible = kind == WitheredEntity.Kind.WATCHMAN || kind == WitheredEntity.Kind.SAPPER;
        this.rightArm.getChild("pauldron").visible = kind == WitheredEntity.Kind.GUARD;
        this.head.getChild("hood_tail").visible = kind == WitheredEntity.Kind.ARCHER || kind == WitheredEntity.Kind.ACOLYTE || kind == WitheredEntity.Kind.SAPPER;
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
                .texOffs(36, 151).addBox(-4.5F, -0.6F, -2.5F, 9.0F, 2.0F, 5.0F),
                PartPose.ZERO);
        PartDefinition shackle = left_arm.addOrReplaceChild("shackle", CubeListBuilder.create()
                .texOffs(36, 158).addBox(-2.0F, 0.0F, -2.5F, 4.0F, 2.0F, 5.0F)
                .texOffs(12, 55).addBox(-0.5F, 2.0F, -3.3F, 1.0F, 6.0F, 1.0F),
                PartPose.offset(0.5F, 7.0F, 0.0F));
        PartDefinition kettle_hat = head.addOrReplaceChild("kettle_hat", CubeListBuilder.create()
                .texOffs(0, 102).addBox(-6.0F, -6.6F, -6.0F, 12.0F, 1.0F, 12.0F)
                .texOffs(0, 128).addBox(-4.5F, -9.6F, -4.5F, 9.0F, 3.0F, 9.0F),
                PartPose.ZERO);
        PartDefinition pauldron = right_arm.addOrReplaceChild("pauldron", CubeListBuilder.create()
                .texOffs(38, 125).addBox(-3.0F, -3.0F, -3.0F, 4.0F, 3.0F, 6.0F),
                PartPose.ZERO);
        PartDefinition hood_tail = head.addOrReplaceChild("hood_tail", CubeListBuilder.create()
                .texOffs(0, 48).addBox(-2.0F, -1.0F, -1.0F, 4.0F, 4.0F, 3.0F)
                .texOffs(54, 27).addBox(-1.0F, 3.0F, 0.5F, 2.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -6.5F, 4.0F, 0.45F, 0.0F, 0.0F));
        PartDefinition quiver = body.addOrReplaceChild("quiver", CubeListBuilder.create()
                .texOffs(54, 16).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 9.0F, 2.0F)
                .texOffs(52, 78).addBox(-1.0F, -2.0F, 0.5F, 2.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(2.2F, 1.0F, 2.0F, 0.0F, 0.0F, 0.32F));
        PartDefinition cap = head.addOrReplaceChild("cap", CubeListBuilder.create()
                .texOffs(0, 152).addBox(-4.5F, -9.4F, -4.5F, 9.0F, 2.0F, 9.0F)
                .texOffs(32, 97).addBox(-3.5F, -8.0F, -6.5F, 7.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.07F));
        PartDefinition bulk = body.addOrReplaceChild("bulk", CubeListBuilder.create()
                .texOffs(0, 84).addBox(-5.0F, 0.0F, -3.0F, 10.0F, 12.0F, 6.0F, new CubeDeformation(0.25F)),
                PartPose.ZERO);
        PartDefinition belly = body.addOrReplaceChild("belly", CubeListBuilder.create()
                .texOffs(36, 134).addBox(-4.0F, 4.0F, -5.0F, 8.0F, 7.0F, 2.0F),
                PartPose.ZERO);
        PartDefinition apron = body.addOrReplaceChild("apron", CubeListBuilder.create()
                .texOffs(36, 143).addBox(-4.0F, 0.0F, -0.5F, 8.0F, 7.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 11.0F, -3.8F, -0.24F, 0.0F, 0.0F));
        PartDefinition brawn_r = right_arm.addOrReplaceChild("brawn_r", CubeListBuilder.create()
                .texOffs(20, 32).addBox(-3.0F, -3.0F, -3.0F, 5.0F, 8.0F, 6.0F),
                PartPose.ZERO);
        PartDefinition brawn_l = left_arm.addOrReplaceChild("brawn_l", CubeListBuilder.create()
                .texOffs(42, 32).addBox(-2.0F, -3.0F, -3.0F, 5.0F, 8.0F, 6.0F),
                PartPose.ZERO);
        PartDefinition coat_tail = body.addOrReplaceChild("coat_tail", CubeListBuilder.create()
                .texOffs(38, 115).addBox(-4.0F, 12.0F, -2.0F, 8.0F, 6.0F, 4.0F, new CubeDeformation(0.45F)),
                PartPose.ZERO);
        PartDefinition candle_cap = head.addOrReplaceChild("candle_cap", CubeListBuilder.create()
                .texOffs(0, 140).addBox(-4.5F, -9.2F, -4.5F, 9.0F, 3.0F, 9.0F)
                .texOffs(20, 46).addBox(-1.5F, -7.6F, -5.5F, 3.0F, 1.0F, 1.0F)
                .texOffs(60, 27).addBox(-0.5F, -10.6F, -5.5F, 1.0F, 3.0F, 1.0F),
                PartPose.ZERO);
        PartDefinition barrow = root.addOrReplaceChild("barrow", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-1.0F, -4.0F, -4.0F, 2.0F, 8.0F, 8.0F)
                .texOffs(0, 64).addBox(-6.0F, -9.0F, 3.0F, 12.0F, 6.0F, 14.0F)
                .texOffs(0, 115).addBox(-4.5F, -12.0F, 5.0F, 9.0F, 3.0F, 10.0F)
                .texOffs(46, 46).addBox(-6.5F, -8.5F, 17.0F, 1.0F, 1.0F, 8.0F)
                .texOffs(46, 55).addBox(5.5F, -8.5F, 17.0F, 1.0F, 1.0F, 8.0F)
                .texOffs(52, 71).addBox(-5.5F, -3.0F, 14.0F, 1.0F, 6.0F, 1.0F)
                .texOffs(56, 71).addBox(4.5F, -3.0F, 14.0F, 1.0F, 6.0F, 1.0F)
                .texOffs(0, 55).addBox(-2.5F, -1.0F, -1.0F, 1.0F, 2.0F, 5.0F)
                .texOffs(52, 64).addBox(1.5F, -1.0F, -1.0F, 1.0F, 2.0F, 5.0F),
                PartPose.offset(0.0F, 20.0F, -24.0F));
        PartDefinition keg = body.addOrReplaceChild("keg", CubeListBuilder.create()
                .texOffs(32, 84).addBox(-3.0F, 1.0F, 2.0F, 6.0F, 8.0F, 5.0F)
                .texOffs(60, 71).addBox(0.5F, -2.0F, 4.0F, 1.0F, 3.0F, 1.0F),
                PartPose.ZERO);
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 192);
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

        if (this.kind == WitheredEntity.Kind.BARROWMAN) {
            this.barrowman(state);
        }
        if (state.attackId == WitheredEntity.PICK) {
            // The pick goes up over the head in both hands until it hangs down his back, stays there for a breath, and
            // comes down point first with the whole trunk bent after it. Then it is fast in the floor: he heaves at the
            // haft twice before it comes.
            float t = state.attackTime;
            float raise = Anim.pulse(t, 0.0F, 11.0F, 14.0F, 17.0F);
            float strike = Anim.pulse(t, 14.0F, 17.0F, 39.0F, 47.0F);
            float tug = Anim.pulse(t, 26.0F, 29.0F, 31.0F, 34.0F) + Anim.pulse(t, 35.0F, 38.0F, 40.0F, 43.0F);
            float held = Math.max(raise, strike);
            this.rightArm.xRot = Anim.lerp(raise, this.rightArm.xRot, -2.75F);
            this.rightArm.xRot = Anim.lerp(strike, this.rightArm.xRot, -0.55F + 0.25F * tug);
            this.rightArm.zRot = Anim.lerp(held, this.rightArm.zRot, -0.3F);
            this.leftArm.xRot = Anim.lerp(raise, this.leftArm.xRot, -2.55F);
            this.leftArm.xRot = Anim.lerp(strike, this.leftArm.xRot, -0.75F + 0.25F * tug);
            this.leftArm.zRot = Anim.lerp(held, this.leftArm.zRot, 0.5F);
            this.body.xRot += -0.3F * raise + 0.45F * strike - 0.2F * tug;
            this.head.xRot += -0.35F * raise + 0.1F * strike;
            this.rightLeg.xRot += -0.25F * strike;
            this.leftLeg.xRot += 0.2F * strike;
        }
        if (state.attackId == WitheredEntity.LOB) {
            // The pot is held out and the left hand brings the match to it; when it spits he swings it back low and
            // lobs it underhand, high.
            float t = state.attackTime;
            float light = Anim.pulse(t, 0.0F, 6.0F, 18.0F, 22.0F);
            float back = Anim.pulse(t, 17.0F, 23.0F, 24.0F, 26.5F);
            float toss = Anim.pulse(t, 24.0F, 26.5F, 31.0F, 41.0F);
            this.rightArm.xRot = Anim.lerp(light, this.rightArm.xRot, -1.2F);
            this.rightArm.yRot += -0.35F * light;
            this.leftArm.xRot = Anim.lerp(light, this.leftArm.xRot, -1.35F);
            this.leftArm.yRot += 0.6F * light;
            this.head.xRot += 0.2F * light - 0.3F * toss;
            this.rightArm.xRot = Anim.lerp(back, this.rightArm.xRot, 0.9F);
            this.rightArm.xRot = Anim.lerp(toss, this.rightArm.xRot, -2.3F);
            this.body.xRot += 0.15F * back - 0.2F * toss;
            this.body.yRot += 0.3F * back - 0.25F * toss;
        }
        if (state.attackId == WitheredEntity.FUSE) {
            // He reaches over his shoulder with the match, and then runs with his arms flung up and his head back.
            float t = state.attackTime;
            float reach = Anim.pulse(t, 0.0F, 7.0F, 13.0F, 17.0F);
            float run = Anim.ramp(t, 14.0F, 18.0F);
            float stride = Mth.sin(t * 1.25F) * run;
            this.leftArm.xRot = Anim.lerp(reach, this.leftArm.xRot, -3.3F);
            this.leftArm.zRot += 0.4F * reach;
            this.rightArm.xRot = Anim.lerp(run, this.rightArm.xRot, -2.7F + stride * 0.3F);
            this.leftArm.xRot = Anim.lerp(run, this.leftArm.xRot, -2.7F - stride * 0.3F);
            this.rightArm.zRot += 0.5F * run;
            this.leftArm.zRot -= 0.5F * run;
            this.head.xRot -= 0.5F * run;
            this.body.xRot += 0.25F * run;
            this.rightLeg.xRot += stride * 1.0F;
            this.leftLeg.xRot -= stride * 1.0F;
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
            // A held thing points forward when the arm hangs, so a pitchfork is a pike carried at the hip. It is drawn
            // straight back past the body, level, the other hand coming across to steady it, and then driven forward at
            // the belly with a long step, the trunk going in behind it.
            float t = state.attackTime;
            float ready = Anim.pulse(t, 0.0F, 9.0F, 12.0F, 14.5F);
            float thrust = Anim.pulse(t, 12.0F, 14.5F, 19.0F, 27.0F);
            float held = Math.max(ready, thrust);
            // (a long weapon hangs a little below the level when the arm hangs: the arm is held half a radian forward for it)
            this.rightArm.xRot = Anim.lerp(held, this.rightArm.xRot, -0.5F) - 0.18F * thrust;
            this.rightArm.zRot = Anim.lerp(held, this.rightArm.zRot, 0.08F);
            this.rightArm.z += 7.5F * ready - 7.0F * thrust;
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
        if (state.attackId == WitheredEntity.SWING && this.kind != WitheredEntity.Kind.BARROWMAN) {
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

    /**
     * The barrowman never lets go of the handles: his arms are set on them whatever his legs do, and the barrow rolls,
     * rattles, is run at somebody, is shoved, and at the end of a run stops dead and tips while he falls over it.
     */
    private void barrowman(SoulsRenderState state) {
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float rattle = Mth.sin(state.walkAnimationPos * 2.2F) * 0.03F * speed;
        float t = state.attackTime;
        float lean = 0.0F;
        float tip = 0.0F;
        float push = 0.0F;
        if (state.attackId == WitheredEntity.CHARGE) {
            float set = Anim.pulse(t, 0.0F, 12.0F, WitheredEntity.CHARGE_END, WitheredEntity.CHARGE_END + 3.0F);
            float run = Anim.pulse(t, WitheredEntity.CHARGE_START - 1.0F, WitheredEntity.CHARGE_START + 1.0F, WitheredEntity.CHARGE_END - 1.0F, WitheredEntity.CHARGE_END + 1.0F);
            float fall = Anim.pulse(t, WitheredEntity.CHARGE_END, WitheredEntity.CHARGE_END + 4.0F, 46.0F, 55.0F);
            float stride = Mth.sin(t * 1.3F) * run;
            lean = 0.4F * set + 0.75F * fall;
            tip = 0.75F * fall;
            push = -2.0F * set * (1.0F - run) + 2.5F * fall;
            rattle += Mth.sin(t * 2.9F) * 0.05F * run;
            this.rightLeg.xRot += stride * 1.1F + 0.5F * fall;
            this.leftLeg.xRot += -stride * 1.1F + 0.3F * fall;
            this.head.xRot += 0.35F * set - 0.5F * fall;
        } else if (state.attackId == WitheredEntity.RAM) {
            float draw = Anim.pulse(t, 0.0F, 6.0F, 7.5F, 10.0F);
            float shove = Anim.pulse(t, 7.5F, 10.0F, 14.0F, 23.0F);
            push = -3.0F * draw + 6.5F * shove;
            lean = -0.08F * draw + 0.3F * shove;
            this.rightLeg.xRot += 0.2F * draw - 0.5F * shove;
            this.leftLeg.xRot += -0.2F * draw + 0.4F * shove;
        }
        this.body.xRot += 0.1F + lean;
        this.head.y += 2.0F * lean;
        this.head.z -= 4.0F * lean;
        this.barrow.z -= push;
        this.barrow.xRot += rattle + tip;
        this.barrow.y += Math.abs(rattle) * 6.0F;
        // the hands: on the handles, which go forward with the barrow and up when it tips
        float reach = -0.5F - push * 0.07F - tip * 1.0F;
        this.rightArm.xRot = reach;
        this.leftArm.xRot = reach;
        this.rightArm.yRot = 0.0F;
        this.leftArm.yRot = 0.0F;
        this.rightArm.zRot = 0.12F;
        this.leftArm.zRot = -0.12F;
        this.rightArm.y += 2.5F * lean;
        this.leftArm.y += 2.5F * lean;
        this.rightArm.z -= 5.0F * lean;
        this.leftArm.z -= 5.0F * lean;
        this.rightArm.x -= 1.0F;
        this.leftArm.x += 1.0F;
    }
}
