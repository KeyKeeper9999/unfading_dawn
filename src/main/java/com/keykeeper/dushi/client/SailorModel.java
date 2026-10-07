package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.DockhandEntity;
import com.keykeeper.dushi.entity.HarpoonerEntity;
import com.keykeeper.dushi.entity.HelmsmanEntity;
import com.keykeeper.dushi.entity.OarsmanEntity;
import com.keykeeper.dushi.entity.PilotEntity;

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
 * The people of the Morning Wharf: the dockhand, the harpooner, the oarsman and the pilot are one mesh, of which each
 * kind shows its own parts. The mesh between the markers is written by tools/gen_sailor.py (which also paints the
 * textures and draws the poses below on a sheet); the animation is written by hand.
 * <p>
 * The trunk turns about the hips; the tail of the smock (and the pilot's long coat) hangs from the waist and is turned
 * back by whatever the trunk is bent by. What a kind holds is part of the mesh and lies across the forearm, pointing
 * forward when the arm hangs - all but the pilot's lead, which hangs from his hand on its line and is kept hanging.
 */
public class SailorModel extends EntityModel<BossRenderState> {
    public enum Kind {
        DOCKHAND, HARPOONER, OARSMAN, PILOT, HELMSMAN
    }

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("sailor"), "main");
    /** The same mesh a fifth larger: the oarsman and the pilot. */
    public static final ModelLayerLocation BIG = new ModelLayerLocation(Dushi.id("sailor"), "big");
    /** And half as large again: the Helmsman. */
    public static final ModelLayerLocation HUGE = new ModelLayerLocation(Dushi.id("sailor"), "huge");
    private static final float DEG = (float) (Math.PI / 180.0);

    private final Kind kind;
    private final ModelPart torso;
    private final ModelPart tail;
    private final ModelPart coat;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightForearm;
    private final ModelPart leftForearm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightShin;
    private final ModelPart leftShin;
    private final ModelPart harpoon;
    private final ModelPart line;
    private final ModelPart anchor;

    public SailorModel(ModelPart root, Kind kind) {
        super(root);
        this.kind = kind;
        this.torso = root.getChild("torso");
        this.tail = this.torso.getChild("tail");
        this.coat = this.torso.getChild("coat");
        this.head = this.torso.getChild("head");
        this.rightArm = this.torso.getChild("right_arm");
        this.leftArm = this.torso.getChild("left_arm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.rightShin = this.rightLeg.getChild("right_shin");
        this.leftShin = this.leftLeg.getChild("left_shin");
        this.harpoon = this.rightForearm.getChild("harpoon");
        this.line = this.rightForearm.getChild("line");
        this.anchor = this.rightForearm.getChild("anchor");
        this.head.getChild("sack").visible = kind == Kind.DOCKHAND;
        this.torso.getChild("pad").visible = kind == Kind.DOCKHAND;
        this.rightForearm.getChild("hook").visible = kind == Kind.DOCKHAND;
        this.head.getChild("souwester").visible = kind == Kind.HARPOONER || kind == Kind.PILOT;
        this.torso.getChild("irons").visible = kind == Kind.HARPOONER;
        this.torso.getChild("coil").visible = kind == Kind.HARPOONER;
        this.harpoon.visible = kind == Kind.HARPOONER;
        this.head.getChild("cap").visible = kind == Kind.OARSMAN;
        this.head.getChild("beard").visible = kind == Kind.OARSMAN || kind == Kind.HELMSMAN;
        this.torso.getChild("bulk").visible = kind == Kind.OARSMAN || kind == Kind.HELMSMAN;
        this.rightForearm.getChild("oar").visible = kind == Kind.OARSMAN;
        this.coat.visible = kind == Kind.PILOT || kind == Kind.HELMSMAN;
        this.tail.visible = !this.coat.visible;
        this.torso.getChild("collar").visible = this.coat.visible;
        this.head.getChild("hat").visible = kind == Kind.HELMSMAN;
        this.torso.getChild("chains").visible = kind == Kind.HELMSMAN;
        this.anchor.visible = kind == Kind.HELMSMAN;
        this.torso.getChild("lamp").visible = kind == Kind.PILOT;
        this.line.visible = kind == Kind.PILOT;
    }

    public static LayerDefinition createBigLayer() {
        return createLayer().apply(MeshTransformer.scaling(1.2F));
    }

    public static LayerDefinition createHugeLayer() {
        return createLayer().apply(MeshTransformer.scaling(1.5F));
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(70, 60).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offset(-2.0F, 11.0F, 0.0F));
        PartDefinition right_shin = right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(108, 48).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 7.0F, 4.0F)
                .texOffs(64, 78).addBox(-2.0F, 5.0F, -4.0F, 4.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(82, 60).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offset(2.0F, 11.0F, 0.0F));
        PartDefinition left_shin = left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(54, 57).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 7.0F, 4.0F)
                .texOffs(106, 78).addBox(-2.0F, 5.0F, -4.0F, 4.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition torso = root.addOrReplaceChild("torso", CubeListBuilder.create()
                .texOffs(52, 19).addBox(-4.0F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 11.0F, 0.0F, 0.1F, 0.0F, 0.0F));
        PartDefinition tail = torso.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(44, 35).addBox(-4.5F, 0.0F, -2.5F, 9.0F, 5.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.1F, 0.0F, 0.0F));
        PartDefinition head = torso.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(82, 17).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, -0.05F, 0.0F, 0.0F));
        PartDefinition right_arm = torso.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(94, 60).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offset(-5.5F, -10.5F, 0.0F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(32, 68).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 5.5F, 0.0F, -0.15F, 0.0F, 0.0F));
        PartDefinition left_arm = torso.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(44, 68).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offset(5.5F, -10.5F, 0.0F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(56, 68).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 7.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 5.5F, 0.0F, -0.15F, 0.0F, 0.0F));
        PartDefinition sack = head.addOrReplaceChild("sack", CubeListBuilder.create()
                .texOffs(16, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 11.0F, 9.0F),
                PartPose.ZERO);
        PartDefinition pad = torso.addOrReplaceChild("pad", CubeListBuilder.create()
                .texOffs(108, 59).addBox(-3.5F, -11.5F, 2.0F, 7.0F, 7.0F, 3.0F),
                PartPose.ZERO);
        PartDefinition hook = right_forearm.addOrReplaceChild("hook", CubeListBuilder.create()
                .texOffs(64, 81).addBox(-2.5F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F)
                .texOffs(114, 17).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F)
                .texOffs(96, 76).addBox(-0.5F, -9.0F, -3.5F, 1.0F, 1.0F, 4.0F)
                .texOffs(114, 26).addBox(-0.5F, -8.0F, -3.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 1.5708F, 0.0F, 0.0F));
        PartDefinition souwester = head.addOrReplaceChild("souwester", CubeListBuilder.create()
                .texOffs(0, 45).addBox(-4.5F, -9.0F, -4.5F, 9.0F, 3.0F, 9.0F)
                .texOffs(76, 33).addBox(-6.0F, -6.5F, -6.0F, 12.0F, 1.0F, 14.0F),
                PartPose.ZERO);
        PartDefinition irons = torso.addOrReplaceChild("irons", CubeListBuilder.create()
                .texOffs(114, 0).addBox(1.0F, -17.0F, 2.2F, 1.0F, 16.0F, 1.0F)
                .texOffs(118, 0).addBox(-2.5F, -16.0F, 2.4F, 1.0F, 15.0F, 1.0F)
                .texOffs(52, 78).addBox(0.5F, -20.0F, 2.2F, 2.0F, 3.0F, 1.0F)
                .texOffs(58, 78).addBox(-3.0F, -19.0F, 2.4F, 2.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.12F));
        PartDefinition coil = torso.addOrReplaceChild("coil", CubeListBuilder.create()
                .texOffs(106, 69).addBox(3.6F, -4.0F, -2.0F, 2.0F, 5.0F, 4.0F),
                PartPose.ZERO);
        PartDefinition harpoon = right_forearm.addOrReplaceChild("harpoon", CubeListBuilder.create()
                .texOffs(4, 0).addBox(-0.5F, -22.0F, -0.5F, 1.0F, 30.0F, 1.0F)
                .texOffs(76, 27).addBox(-1.0F, -27.0F, -0.5F, 2.0F, 5.0F, 1.0F)
                .texOffs(114, 30).addBox(-2.0F, -24.0F, -0.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(72, 35).addBox(1.0F, -24.0F, -0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 1.5708F, 0.0F, 0.0F));
        PartDefinition cap = head.addOrReplaceChild("cap", CubeListBuilder.create()
                .texOffs(16, 20).addBox(-4.5F, -8.7F, -4.5F, 9.0F, 4.0F, 9.0F),
                PartPose.ZERO);
        PartDefinition beard = head.addOrReplaceChild("beard", CubeListBuilder.create()
                .texOffs(80, 77).addBox(-3.5F, -2.5F, -4.8F, 7.0F, 5.0F, 1.0F),
                PartPose.ZERO);
        PartDefinition bulk = torso.addOrReplaceChild("bulk", CubeListBuilder.create()
                .texOffs(36, 45).addBox(-6.0F, -12.8F, -3.0F, 12.0F, 6.0F, 6.0F)
                .texOffs(0, 57).addBox(-4.8F, -6.8F, -2.8F, 10.0F, 6.0F, 6.0F),
                PartPose.ZERO);
        PartDefinition oar = right_forearm.addOrReplaceChild("oar", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.5F, -28.0F, -0.5F, 1.0F, 38.0F, 1.0F)
                .texOffs(118, 16).addBox(-2.0F, -42.0F, -0.5F, 4.0F, 14.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 1.5708F, 0.0F, 0.0F));
        PartDefinition coat = torso.addOrReplaceChild("coat", CubeListBuilder.create()
                .texOffs(82, 0).addBox(-5.0F, 0.0F, -3.0F, 10.0F, 11.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.1F, 0.0F, 0.0F));
        PartDefinition collar = torso.addOrReplaceChild("collar", CubeListBuilder.create()
                .texOffs(0, 69).addBox(-5.0F, -13.0F, -3.0F, 10.0F, 3.0F, 6.0F),
                PartPose.ZERO);
        PartDefinition lamp = torso.addOrReplaceChild("lamp", CubeListBuilder.create()
                .texOffs(80, 70).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F)
                .texOffs(4, 31).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-3.5F, -1.0F, -3.0F, -0.1F, 0.0F, 0.0F));
        PartDefinition line = right_forearm.addOrReplaceChild("line", CubeListBuilder.create()
                .texOffs(122, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(68, 70).addBox(-1.5F, 14.0F, -1.5F, 3.0F, 5.0F, 3.0F),
                PartPose.offset(0.0F, 6.5F, 0.0F));
        PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create()
                .texOffs(72, 48).addBox(-4.5F, -10.0F, -4.5F, 9.0F, 3.0F, 9.0F)
                .texOffs(4, 33).addBox(-5.0F, -7.5F, -5.0F, 10.0F, 2.0F, 10.0F),
                PartPose.ZERO);
        PartDefinition chains = torso.addOrReplaceChild("chains", CubeListBuilder.create()
                .texOffs(52, 0).addBox(-4.5F, -12.5F, -2.6F, 9.0F, 13.0F, 6.0F),
                PartPose.ZERO);
        PartDefinition anchor = right_forearm.addOrReplaceChild("anchor", CubeListBuilder.create()
                .texOffs(44, 78).addBox(-1.5F, -3.0F, -0.5F, 3.0F, 3.0F, 1.0F)
                .texOffs(32, 57).addBox(-0.5F, -5.0F, -5.0F, 1.0F, 1.0F, 10.0F)
                .texOffs(8, 0).addBox(-1.0F, -24.0F, -1.0F, 2.0F, 21.0F, 2.0F)
                .texOffs(12, 78).addBox(-7.0F, -26.0F, -1.0F, 14.0F, 2.0F, 2.0F)
                .texOffs(8, 23).addBox(-9.0F, -26.0F, -1.0F, 2.0F, 7.0F, 2.0F)
                .texOffs(118, 69).addBox(7.0F, -26.0F, -1.0F, 2.0F, 7.0F, 2.0F)
                .texOffs(92, 70).addBox(-10.0F, -21.0F, -1.5F, 3.0F, 3.0F, 3.0F)
                .texOffs(0, 78).addBox(7.0F, -21.0F, -1.5F, 3.0F, 3.0F, 3.0F)
                .texOffs(76, 19).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 1.5708F, 0.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float walk = state.walkAnimationPos * 0.6662F;
        float stride = Mth.sin(walk);
        float t = state.attackTime;

        this.head.yRot += Mth.clamp(state.yRot, -60.0F, 60.0F) * DEG;
        this.head.xRot += Mth.clamp(state.xRot, -40.0F, 40.0F) * DEG * 0.6F;
        this.rightLeg.xRot += -stride * 0.8F * speed;
        this.leftLeg.xRot += stride * 0.8F * speed;
        this.rightShin.xRot += Math.max(0.0F, Mth.cos(walk)) * 0.7F * speed;
        this.leftShin.xRot += Math.max(0.0F, -Mth.cos(walk)) * 0.7F * speed;
        this.torso.xRot += Mth.sin(age * 0.06F) * 0.015F;
        // (a harpooner's hand is empty between the throw and the next iron: set every time, one model draws them all)
        this.harpoon.visible = this.kind == Kind.HARPOONER;
        this.anchor.visible = this.kind == Kind.HELMSMAN;

        switch (this.kind) {
            case DOCKHAND -> this.dockhand(state, age, stride * speed, t);
            case HARPOONER -> this.harpooner(state, age, stride * speed, t);
            case OARSMAN -> this.oarsman(state, age, stride * speed, t);
            case HELMSMAN -> this.helmsman(state, age, stride * speed, t);
            default -> this.pilot(state, age, stride * speed, t);
        }
        // what hangs from the waist hangs, whatever the trunk has been bent by; and goes a little with the legs
        float hang = Mth.clamp(-this.torso.xRot, -0.9F, 0.5F) + Math.abs(stride) * 0.05F * speed;
        this.tail.xRot = hang;
        this.coat.xRot = hang;
    }

    // ------------------------------------------------------------------ the dockhand

    private void dockhand(BossRenderState state, float age, float stride, float t) {
        // Bent a little, as one who has carried all his life; the hook held low before him, the left arm swinging.
        Anim.pose(this.torso, 1.0F, 0.18F, 0.0F, 0.0F);
        Anim.pose(this.rightArm, 1.0F, -0.25F - stride * 0.2F, 0.0F, -0.1F);
        Anim.pose(this.rightForearm, 1.0F, -0.5F, 0.0F, 0.0F);
        this.leftArm.xRot += 0.15F + stride * 0.5F;
        this.leftArm.zRot += 0.1F;
        switch (state.attackId) {
            case DockhandEntity.HOOK -> {
                // Up over the shoulder, over and down into whatever is before him - and hauled home, as a bale is.
                float up = Anim.pulse(t, 0.0F, 9.0F, DockhandEntity.HOOK_IMPACT - 4.0F, DockhandEntity.HOOK_IMPACT - 1.0F);
                float down = Anim.pulse(t, DockhandEntity.HOOK_IMPACT - 4.0F, DockhandEntity.HOOK_IMPACT, DockhandEntity.HOOK_IMPACT + 2.0F, DockhandEntity.HOOK_IMPACT + 6.0F);
                float pull = Anim.pulse(t, DockhandEntity.HOOK_IMPACT + 2.0F, DockhandEntity.HOOK_PULLED, DockhandEntity.HOOK_PULLED + 4.0F, DockhandEntity.HOOK_DURATION);
                Anim.pose(this.torso, up, -0.15F, 0.3F, 0.0F);
                Anim.pose(this.rightArm, up, -2.6F, 0.0F, -0.2F);
                Anim.pose(this.rightForearm, up, -0.7F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, up, -0.6F, 0.0F, 0.3F);
                Anim.pose(this.torso, down, 0.55F, -0.2F, 0.0F);
                Anim.pose(this.rightArm, down, -1.1F, 0.0F, 0.0F);
                Anim.pose(this.rightForearm, down, -0.1F, 0.0F, 0.0F);
                this.rightLeg.xRot -= 0.5F * down;
                this.leftLeg.xRot += 0.3F * down;
                Anim.pose(this.torso, pull, -0.1F, 0.3F, 0.0F);
                Anim.pose(this.rightArm, pull, -0.2F, 0.0F, 0.0F);
                Anim.pose(this.rightForearm, pull, -1.5F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, pull, -0.9F, 0.0F, 0.2F);
            }
            case DockhandEntity.RIP -> this.rip(t, DockhandEntity.RIP_IMPACT, 1.0F);
            case DockhandEntity.RIPS -> {
                this.rip(t, DockhandEntity.RIPS_FIRST, 1.0F);
                this.rip(t, DockhandEntity.RIPS_SECOND, -1.0F);
            }
            default -> {
            }
        }
    }

    /** The hook across, level: from his right if {@code side} is 1, back from his left if it is -1. */
    private void rip(float t, float impact, float side) {
        float draw = Anim.pulse(t, impact - 11.0F, impact - 5.0F, impact - 3.0F, impact);
        float cut = Anim.pulse(t, impact - 3.0F, impact, impact + 3.0F, impact + 10.0F);
        Anim.pose(this.torso, draw, 0.15F, 0.6F * side, 0.0F);
        Anim.pose(this.rightArm, draw, -1.3F, 0.9F * side, 0.0F);
        Anim.pose(this.torso, cut, 0.2F, -0.7F * side, 0.0F);
        Anim.pose(this.rightArm, cut, -1.3F, -0.9F * side, 0.0F);
        Anim.pose(this.rightForearm, Math.max(draw, cut), -0.3F, 0.0F, 0.0F);
    }

    // ------------------------------------------------------------------ the harpooner

    private void harpooner(BossRenderState state, float age, float stride, float t) {
        // The iron carried upright before the right shoulder, point up; the left arm swinging.
        Anim.pose(this.rightArm, 1.0F, -0.9F, 0.0F, -0.1F);
        Anim.pose(this.rightForearm, 1.0F, -0.8F, 0.0F, 0.0F);
        this.rightArm.xRot += Mth.sin(age * 0.07F) * 0.03F;
        this.leftArm.xRot += 0.1F + stride * 0.5F;
        switch (state.attackId) {
            case HarpoonerEntity.THROW -> {
                // The whole of him turned away and the iron far back over the shoulder, level; then everything comes
                // round after it. His hand is empty until he has drawn another from his back.
                float release = HarpoonerEntity.THROW_RELEASE;
                float back = Anim.pulse(t, 0.0F, 12.0F, release - 3.0F, release);
                float thrown = Anim.pulse(t, release - 3.0F, release, release + 5.0F, release + 12.0F);
                float reach = Anim.pulse(t, release + 8.0F, release + 14.0F, release + 16.0F, HarpoonerEntity.THROW_DURATION);
                Anim.pose(this.torso, back, -0.2F, 0.6F, 0.0F);
                Anim.pose(this.rightArm, back, -2.5F, 0.4F, -0.3F);
                Anim.pose(this.rightForearm, back, -1.1F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, back, -1.4F, 0.0F, 0.2F);
                this.leftLeg.xRot -= 0.4F * back;
                this.rightLeg.xRot += 0.3F * back;
                Anim.pose(this.torso, thrown, 0.55F, -0.5F, 0.0F);
                Anim.pose(this.rightArm, thrown, -1.5F, -0.2F, 0.0F);
                Anim.pose(this.rightForearm, thrown, -0.1F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, thrown, 0.7F, 0.0F, 0.3F);
                this.rightLeg.xRot -= 0.5F * thrown;
                this.leftLeg.xRot += 0.4F * thrown;
                // the hand goes over the shoulder for the next
                Anim.pose(this.rightArm, reach, -2.9F, 0.0F, 0.2F);
                Anim.pose(this.rightForearm, reach, -1.6F, 0.0F, 0.0F);
                this.harpoon.visible = t < release || t >= release + 15.0F;
            }
            case HarpoonerEntity.STAB -> {
                // The iron comes down level at the hip, is drawn back, and goes in with a long step.
                float impact = HarpoonerEntity.STAB_IMPACT;
                float draw = Anim.pulse(t, 0.0F, 7.0F, impact - 3.0F, impact);
                float go = Anim.pulse(t, impact - 3.0F, impact, impact + 5.0F, impact + 13.0F);
                Anim.pose(this.torso, draw, 0.1F, 0.4F, 0.0F);
                Anim.pose(this.rightArm, draw, 0.55F, 0.0F, 0.1F);
                Anim.pose(this.rightForearm, draw, 0.15F, 0.0F, 0.0F);
                Anim.pose(this.torso, go, 0.3F, -0.3F, 0.0F);
                Anim.pose(this.rightArm, go, -0.45F, 0.0F, 0.1F);
                Anim.pose(this.rightForearm, go, 0.15F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, go, -0.8F, 0.0F, 0.2F);
                this.rightLeg.xRot += 0.4F * go;
                this.leftLeg.xRot -= 0.5F * go;
            }
            case HarpoonerEntity.HOP -> {
                // A jump back: knees up, the arms out
                float air = Anim.pulse(t, 0.0F, 3.0F, HarpoonerEntity.HOP_DURATION - 5.0F, HarpoonerEntity.HOP_DURATION);
                Anim.pose(this.torso, air, 0.3F, 0.0F, 0.0F);
                Anim.pitch(this.rightLeg, air, -0.7F);
                Anim.pitch(this.leftLeg, air, -0.4F);
                Anim.pitch(this.rightShin, air, 0.9F);
                Anim.pitch(this.leftShin, air, 0.7F);
                Anim.pose(this.leftArm, air, -0.5F, 0.0F, 0.6F);
            }
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------ the oarsman

    private void oarsman(BossRenderState state, float age, float stride, float t) {
        // The oar across his body in both hands, the blade up over his left shoulder; he rolls as he walks.
        Anim.pose(this.torso, 1.0F, 0.1F, 0.25F, stride * 0.06F);
        Anim.pose(this.rightArm, 1.0F, -0.7F, 0.45F, 0.0F);
        Anim.pose(this.rightForearm, 1.0F, -0.5F, 0.0F, 0.0F);
        Anim.pose(this.leftArm, 1.0F, -1.0F, 0.5F, 0.0F);
        Anim.pose(this.leftForearm, 1.0F, -0.6F, 0.0F, 0.0F);
        switch (state.attackId) {
            case OarsmanEntity.SWEEP -> {
                // Drawn right round behind him with the whole body, and brought across level: the length of it clears a pier.
                float impact = OarsmanEntity.SWEEP_IMPACT;
                float draw = Anim.pulse(t, 0.0F, 14.0F, impact - 5.0F, impact - 1.0F);
                float cut = Anim.pulse(t, impact - 5.0F, impact + 1.0F, impact + 8.0F, OarsmanEntity.SWEEP_DURATION);
                this.sweep(draw, cut);
            }
            case OarsmanEntity.PROD -> {
                // The blade let down level before him, drawn back along his hip, and driven out: a shove more than a blow.
                float impact = OarsmanEntity.PROD_IMPACT;
                float draw = Anim.pulse(t, 0.0F, 10.0F, impact - 4.0F, impact);
                float go = Anim.pulse(t, impact - 4.0F, impact, impact + 6.0F, OarsmanEntity.PROD_DURATION);
                Anim.pose(this.torso, draw, 0.1F, 0.4F, 0.0F);
                Anim.pose(this.rightArm, draw, 0.6F, 0.0F, 0.1F);
                Anim.pose(this.rightForearm, draw, 0.15F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, draw, -0.5F, 0.0F, 0.0F);
                Anim.pose(this.torso, go, 0.35F, -0.3F, 0.0F);
                Anim.pose(this.rightArm, go, -0.4F, 0.0F, 0.1F);
                Anim.pose(this.rightForearm, go, 0.15F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, go, -0.9F, -0.3F, 0.0F);
                Anim.pose(this.leftForearm, go, -0.4F, 0.0F, 0.0F);
                this.rightLeg.xRot += 0.4F * go;
                this.leftLeg.xRot -= 0.5F * go;
            }
            case OarsmanEntity.SLAM -> {
                // Both hands take it up until the blade stands over him like a mast, and bring it down flat.
                float impact = OarsmanEntity.SLAM_IMPACT;
                float raise = Anim.pulse(t, 0.0F, 18.0F, impact - 5.0F, impact - 1.0F);
                float fall = Anim.pulse(t, impact - 5.0F, impact, impact + 10.0F, OarsmanEntity.SLAM_DURATION);
                Anim.pose(this.torso, raise, -0.3F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, raise, -2.9F, 0.0F, 0.0F);
                Anim.pose(this.rightForearm, raise, -0.3F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, raise, -2.7F, 0.0F, 0.3F);
                Anim.pose(this.leftForearm, raise, -0.5F, 0.0F, 0.0F);
                Anim.pose(this.torso, fall, 0.6F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, fall, -0.75F, 0.0F, 0.0F);
                Anim.pose(this.rightForearm, fall, 0.1F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, fall, -0.8F, 0.0F, 0.3F);
                Anim.pose(this.leftForearm, fall, -0.3F, 0.0F, 0.0F);
                this.rightLeg.xRot -= 0.4F * fall;
                this.leftLeg.xRot += 0.3F * fall;
            }
            default -> {
            }
        }
    }

    /**
     * A long thing swept level: the arm that holds it is let down, so that it lies level across the arm, and the whole
     * trunk turns - from far round on the right ({@code draw}) to far round on the left ({@code cut}).
     */
    private void sweep(float draw, float cut) {
        Anim.pose(this.torso, draw, 0.1F, 1.0F, 0.0F);
        Anim.pose(this.rightArm, draw, -0.1F, 0.6F, 0.0F);
        Anim.pose(this.rightForearm, draw, 0.1F, 0.0F, 0.0F);
        Anim.pose(this.leftArm, draw, -0.9F, 0.3F, 0.0F);
        Anim.pose(this.leftForearm, draw, -0.8F, 0.0F, 0.0F);
        Anim.pose(this.torso, cut, 0.2F, -0.9F, 0.0F);
        Anim.pose(this.rightArm, cut, -0.1F, -0.5F, 0.0F);
        Anim.pose(this.rightForearm, cut, 0.1F, 0.0F, 0.0F);
        Anim.pose(this.leftArm, cut, -0.7F, -0.4F, 0.0F);
        Anim.pose(this.leftForearm, cut, -0.9F, 0.0F, 0.0F);
        this.rightLeg.xRot -= 0.3F * cut;
        this.leftLeg.xRot += 0.3F * cut;
    }

    // ------------------------------------------------------------------ the Helmsman

    private void helmsman(BossRenderState state, float age, float stride, float t) {
        // The anchor lies back over his right shoulder, held by the ring; the left arm swings. Hurt (phase 2) he is bent.
        float bent = state.phase >= 2 ? 0.14F : 0.0F;
        Anim.pose(this.torso, 1.0F, 0.06F + bent, 0.0F, 0.0F);
        Anim.pose(this.rightArm, 1.0F, -2.2F, 0.0F, -0.1F);
        Anim.pose(this.rightForearm, 1.0F, -0.9F, 0.0F, 0.0F);
        this.leftArm.xRot += 0.1F + stride * 0.4F;
        this.leftArm.zRot -= 0.12F;
        this.anchor.visible = true;
        switch (state.attackId) {
            case HelmsmanEntity.SWEEP -> {
                // Off the shoulder, far round behind him at the length of his arm, and across: the whole deck's width of it.
                float impact = HelmsmanEntity.SWEEP_IMPACT;
                float draw = Anim.pulse(t, 0.0F, 15.0F, impact - 5.0F, impact - 1.0F);
                float cut = Anim.pulse(t, impact - 5.0F, impact + 1.0F, impact + 9.0F, HelmsmanEntity.SWEEP_DURATION);
                this.sweep(draw, cut);
            }
            case HelmsmanEntity.SLAM -> {
                // Both hands to the ring, the anchor up until its crown stands over him - and down on the deck.
                float impact = HelmsmanEntity.SLAM_IMPACT;
                float raise = Anim.pulse(t, 0.0F, 20.0F, impact - 5.0F, impact - 1.0F);
                float fall = Anim.pulse(t, impact - 5.0F, impact, impact + 12.0F, HelmsmanEntity.SLAM_DURATION);
                Anim.pose(this.torso, raise, -0.3F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, raise, -2.9F, 0.0F, 0.0F);
                Anim.pose(this.rightForearm, raise, -0.3F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, raise, -2.7F, 0.0F, 0.3F);
                Anim.pose(this.leftForearm, raise, -0.5F, 0.0F, 0.0F);
                Anim.pose(this.torso, fall, 0.6F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, fall, -0.75F, 0.0F, 0.0F);
                Anim.pose(this.rightForearm, fall, 0.1F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, fall, -0.8F, 0.0F, 0.3F);
                Anim.pose(this.leftForearm, fall, -0.3F, 0.0F, 0.0F);
                this.rightLeg.xRot -= 0.4F * fall;
                this.leftLeg.xRot += 0.3F * fall;
            }
            case HelmsmanEntity.CAST -> {
                // Swung back and let go, as it is let go from the bow; and then the chain, hand over hand, a long time:
                // his hands are empty until it is home.
                float release = HelmsmanEntity.CAST_RELEASE;
                float back = Anim.pulse(t, 0.0F, 16.0F, release - 4.0F, release);
                float thrown = Anim.pulse(t, release - 4.0F, release, release + 6.0F, release + 12.0F);
                float haul = Anim.pulse(t, release + 8.0F, release + 14.0F, HelmsmanEntity.CAST_HOME - 2.0F, HelmsmanEntity.CAST_HOME + 6.0F);
                Anim.pose(this.torso, back, -0.2F, 0.6F, 0.0F);
                Anim.pose(this.rightArm, back, -2.5F, 0.4F, -0.3F);
                Anim.pose(this.rightForearm, back, -1.1F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, back, -1.4F, 0.0F, 0.2F);
                Anim.pose(this.torso, thrown, 0.55F, -0.5F, 0.0F);
                Anim.pose(this.rightArm, thrown, -1.5F, -0.2F, 0.0F);
                Anim.pose(this.rightForearm, thrown, -0.1F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, thrown, 0.7F, 0.0F, 0.3F);
                float pull = Mth.sin(t * 0.45F);
                Anim.pose(this.torso, haul, 0.35F, 0.3F * pull, 0.0F);
                Anim.pose(this.rightArm, haul, -1.3F + 0.45F * pull, 0.0F, 0.2F);
                Anim.pose(this.rightForearm, haul, -0.9F - 0.5F * pull, 0.0F, 0.0F);
                Anim.pose(this.leftArm, haul, -1.3F - 0.45F * pull, 0.0F, -0.2F);
                Anim.pose(this.leftForearm, haul, -0.9F + 0.5F * pull, 0.0F, 0.0F);
                this.rightLeg.xRot += 0.3F * haul;
                this.leftLeg.xRot -= 0.4F * haul;
                this.anchor.visible = t < release || t >= HelmsmanEntity.CAST_HOME;
            }
            case HelmsmanEntity.SHOULDER -> {
                // The left shoulder down and forward, and the whole weight of him after it.
                float go = Anim.pulse(t, 0.0F, HelmsmanEntity.SHOULDER_START, HelmsmanEntity.SHOULDER_END, HelmsmanEntity.SHOULDER_DURATION);
                Anim.pose(this.torso, go, 0.5F, 0.9F, 0.0F);
                Anim.pose(this.head, go, -0.3F, -0.7F, 0.0F);
                Anim.pose(this.leftArm, go, -0.5F, 0.0F, 0.2F);
                Anim.pose(this.leftForearm, go, -1.6F, 0.0F, 0.0F);
                this.rightLeg.xRot -= 0.5F * go;
                this.leftLeg.xRot += 0.4F * go;
            }
            case HelmsmanEntity.BELLOW -> {
                // The order nobody has obeyed since that morning: head back, the free arm flung out to the sea.
                float cry = Anim.pulse(t, 0.0F, 12.0F, 36.0F, HelmsmanEntity.BELLOW_DURATION);
                Anim.pose(this.torso, cry, -0.3F, 0.0F, 0.0F);
                Anim.pose(this.head, cry, -0.5F + Mth.sin(t * 1.3F) * 0.04F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, cry, -1.9F, 0.0F, 0.9F);
            }
            case HelmsmanEntity.WHIRL -> {
                // The anchor let down to the length of his arm, level - and he goes round with it, four times, coming on;
                // then he stands, and the deck goes on turning for him a while.
                float first = HelmsmanEntity.WHIRL_HITS[0];
                float last = HelmsmanEntity.WHIRL_HITS[HelmsmanEntity.WHIRL_HITS.length - 1];
                float turn = HelmsmanEntity.WHIRL_HITS[1] - first;
                float out = Anim.pulse(t, 0.0F, first - turn, last, last + 8.0F);
                Anim.pose(this.torso, out, 0.2F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, out, -0.1F, 0.0F, 0.0F);
                Anim.pose(this.rightForearm, out, 0.1F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, out, -0.4F, 0.0F, 0.8F);
                if (t >= first - turn && t <= last) {
                    // (the anchor is before him at every blow: a whole turn from one to the next)
                    float round = -(t - first) / turn * Mth.TWO_PI;
                    this.torso.yRot += round;
                    this.rightLeg.yRot += round;
                    this.leftLeg.yRot += round;
                }
                float giddy = Anim.pulse(t, last, last + 6.0F, HelmsmanEntity.WHIRL_DURATION - 6.0F, HelmsmanEntity.WHIRL_DURATION);
                Anim.pose(this.torso, giddy, 0.35F + Mth.sin(t * 0.5F) * 0.1F, Mth.sin(t * 0.3F) * 0.25F, Mth.cos(t * 0.4F) * 0.12F);
                Anim.pose(this.head, giddy, 0.4F, Mth.sin(t * 0.6F) * 0.3F, 0.0F);
                Anim.pose(this.rightArm, giddy, 0.15F, 0.0F, 0.0F);
            }
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------ the pilot

    private void pilot(BossRenderState state, float age, float stride, float t) {
        // Upright and slow; the lead hangs from his right hand on its line and swings as he walks.
        Anim.pose(this.rightArm, 1.0F, -0.3F - stride * 0.15F, 0.0F, -0.15F);
        Anim.pose(this.rightForearm, 1.0F, -0.4F, 0.0F, 0.0F);
        this.leftArm.xRot += 0.1F + stride * 0.4F;
        float out = 0.0F;          // how far the line is thrown out of its hanging: 0 hangs, 1 lies where the move puts it
        float lineX = 0.0F;
        float lineY = 0.0F;
        switch (state.attackId) {
            case PilotEntity.WHIRL -> {
                // The arm goes up, and the lead goes round over his head on the whole of its line: three times, a step each.
                float up = Anim.pulse(t, 0.0F, 12.0F, PilotEntity.WHIRL_HITS[2] + 4.0F, PilotEntity.WHIRL_DURATION);
                Anim.pose(this.torso, up, -0.1F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, up, -2.8F, 0.0F, -0.2F);
                Anim.pose(this.rightForearm, up, -0.3F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, up, -0.4F, 0.0F, 0.5F);
                out = up;
                lineX = 1.3F;
                // (once round between one hit and the next: it is before him at each)
                lineY = (t - PilotEntity.WHIRL_HITS[0]) / (PilotEntity.WHIRL_HITS[1] - PilotEntity.WHIRL_HITS[0]) * Mth.TWO_PI;
            }
            case PilotEntity.CAST -> {
                // Swung back past his shoulder, and cast as it is cast from a ship's side: out to the end of the line.
                float impact = PilotEntity.CAST_IMPACT;
                float back = Anim.pulse(t, 0.0F, 12.0F, impact - 4.0F, impact - 1.0F);
                float cast = Anim.pulse(t, impact - 4.0F, impact, impact + 8.0F, PilotEntity.CAST_DURATION);
                Anim.pose(this.torso, back, -0.15F, 0.6F, 0.0F);
                Anim.pose(this.rightArm, back, -2.2F, 0.5F, -0.3F);
                Anim.pose(this.rightForearm, back, -0.8F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, back, -1.2F, 0.0F, 0.2F);
                Anim.pose(this.torso, cast, 0.5F, -0.4F, 0.0F);
                Anim.pose(this.rightArm, cast, -1.5F, -0.2F, 0.0F);
                Anim.pose(this.rightForearm, cast, -0.05F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, cast, 0.6F, 0.0F, 0.3F);
                this.rightLeg.xRot -= 0.5F * cast;
                this.leftLeg.xRot += 0.4F * cast;
                out = Math.max(back, cast);
                lineX = cast > 0.0F ? 0.0F : -0.6F;
            }
            case PilotEntity.SOUND -> {
                // Both hands lift it high, and it is let go straight down before him, as one sounds a channel.
                float impact = PilotEntity.SOUND_IMPACT;
                float raise = Anim.pulse(t, 0.0F, 16.0F, impact - 4.0F, impact - 1.0F);
                float fall = Anim.pulse(t, impact - 4.0F, impact, impact + 10.0F, PilotEntity.SOUND_DURATION);
                Anim.pose(this.torso, raise, -0.2F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, raise, -2.9F, 0.0F, 0.0F);
                Anim.pose(this.rightForearm, raise, -0.2F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, raise, -2.6F, 0.0F, 0.3F);
                Anim.pose(this.torso, fall, 0.55F, 0.0F, 0.0F);
                Anim.pose(this.rightArm, fall, -1.0F, 0.0F, 0.0F);
                Anim.pose(this.rightForearm, fall, 0.0F, 0.0F, 0.0F);
                Anim.pose(this.leftArm, fall, -0.9F, 0.0F, 0.3F);
                this.rightLeg.xRot -= 0.4F * fall;
                this.leftLeg.xRot += 0.3F * fall;
            }
            default -> {
            }
        }
        // the line hangs: turned back by all that the arm above it has been turned by - unless a move has thrown it out
        float hang = -(this.torso.xRot + this.rightArm.xRot + this.rightForearm.xRot) + Mth.sin(age * 0.09F) * 0.06F + stride * 0.2F;
        this.line.xRot = Anim.lerp(out, hang, lineX);
        this.line.yRot = out * lineY;
    }
}
