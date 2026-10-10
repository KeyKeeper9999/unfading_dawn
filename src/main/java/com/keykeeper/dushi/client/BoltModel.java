package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.BoltEntity;

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
 * The Bolt. The mesh between the markers is written by tools/gen_clock.py (which also paints the texture and draws
 * the poses below on a sheet); the animation is written by hand. On its rod (phase 1) the whole of it leans as the
 * pendulum swings - BossRenderState.extra is how far it has swung, as a share of the rod's length - and its four
 * hands lie folded up along the rod. Off it (phase 2) the rod is not drawn, the hands are down and are its legs, and
 * the lens is carried that much higher.
 */
public class BoltModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("bolt"), "main");
    /** Must match the scale in the generator. */
    private static final float MODEL_SCALE = 2.2F;

    private final ModelPart all;
    private final ModelPart bob;
    private final ModelPart rod;
    private final ModelPart stub;

    /** front right, front left, back right, back left: on which side each is (-1 its right), and which end (-1 the front). */
    private static final String[] LEGS = {"fr", "fl", "br", "bl"};
    private static final float[] SIDE = {-1.0F, 1.0F, -1.0F, 1.0F};
    private static final float[] END = {-1.0F, -1.0F, 1.0F, 1.0F};
    /** How much higher the lens is carried on its legs than it hangs on its rod, in model units. */
    private static final float LIFT = 11.0F;
    private final ModelPart[] leg = new ModelPart[4];
    private final ModelPart[] shin = new ModelPart[4];

    public BoltModel(ModelPart root) {
        super(root);
        this.all = root.getChild("all");
        this.bob = this.all.getChild("bob");
        this.rod = this.bob.getChild("rod");
        this.stub = this.bob.getChild("stub");
        for (int i = 0; i < 4; i++) {
            this.leg[i] = this.bob.getChild("leg_" + LEGS[i]);
            this.shin[i] = this.leg[i].getChild("shin_" + LEGS[i]);
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition bob = all.addOrReplaceChild("bob", CubeListBuilder.create()
                .texOffs(0, 27).addBox(-5.0F, -9.0F, -3.0F, 10.0F, 2.0F, 6.0F)
                .texOffs(32, 32).addBox(-5.0F, 7.0F, -3.0F, 10.0F, 2.0F, 6.0F)
                .texOffs(106, 0).addBox(-9.0F, -5.0F, -3.0F, 2.0F, 10.0F, 6.0F)
                .texOffs(106, 16).addBox(7.0F, -5.0F, -3.0F, 2.0F, 10.0F, 6.0F)
                .texOffs(72, 17).addBox(-8.0F, -8.0F, -3.0F, 3.0F, 3.0F, 6.0F)
                .texOffs(0, 18).addBox(5.0F, -8.0F, -3.0F, 3.0F, 3.0F, 6.0F)
                .texOffs(18, 18).addBox(-8.0F, 5.0F, -3.0F, 3.0F, 3.0F, 6.0F)
                .texOffs(72, 26).addBox(5.0F, 5.0F, -3.0F, 3.0F, 3.0F, 6.0F)
                .texOffs(40, 0).addBox(-7.0F, -7.0F, -1.5F, 14.0F, 14.0F, 3.0F),
                PartPose.offset(0.0F, -10.0F, 0.0F));
        PartDefinition rod = bob.addOrReplaceChild("rod", CubeListBuilder.create()
                .texOffs(90, 32).addBox(-3.0F, -3.0F, -1.5F, 6.0F, 3.0F, 3.0F)
                .texOffs(0, 0).addBox(-1.0F, -19.0F, -1.0F, 2.0F, 16.0F, 2.0F)
                .texOffs(8, 0).addBox(-1.0F, -35.0F, -1.0F, 2.0F, 16.0F, 2.0F)
                .texOffs(16, 0).addBox(-1.0F, -51.0F, -1.0F, 2.0F, 16.0F, 2.0F)
                .texOffs(24, 0).addBox(-1.0F, -67.0F, -1.0F, 2.0F, 16.0F, 2.0F)
                .texOffs(32, 0).addBox(-1.0F, -83.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offset(0.0F, -9.0F, 0.0F));
        PartDefinition stub = bob.addOrReplaceChild("stub", CubeListBuilder.create()
                .texOffs(108, 32).addBox(-3.0F, -3.0F, -1.5F, 6.0F, 3.0F, 3.0F)
                .texOffs(90, 17).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 6.0F, 2.0F)
                .texOffs(64, 32).addBox(-1.5F, -11.0F, -0.5F, 3.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -9.0F, 0.0F));
        PartDefinition leg_fr = bob.addOrReplaceChild("leg_fr", CubeListBuilder.create()
                .texOffs(40, 17).addBox(-1.0F, -13.0F, -1.0F, 2.0F, 13.0F, 2.0F)
                .texOffs(90, 25).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(-8.5F, 1.0F, -2.0F, 0.0F, 0.0F, -0.1F));
        PartDefinition shin_fr = leg_fr.addOrReplaceChild("shin_fr", CubeListBuilder.create()
                .texOffs(74, 0).addBox(-1.5F, -16.0F, -0.5F, 3.0F, 16.0F, 1.0F)
                .texOffs(98, 17).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -13.0F, 0.0F));
        PartDefinition leg_fl = bob.addOrReplaceChild("leg_fl", CubeListBuilder.create()
                .texOffs(48, 17).addBox(-1.0F, -13.0F, -1.0F, 2.0F, 13.0F, 2.0F)
                .texOffs(0, 35).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(8.5F, 1.0F, -2.0F, 0.0F, 0.0F, 0.1F));
        PartDefinition shin_fl = leg_fl.addOrReplaceChild("shin_fl", CubeListBuilder.create()
                .texOffs(82, 0).addBox(-1.5F, -16.0F, -0.5F, 3.0F, 16.0F, 1.0F)
                .texOffs(98, 21).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -13.0F, 0.0F));
        PartDefinition leg_br = bob.addOrReplaceChild("leg_br", CubeListBuilder.create()
                .texOffs(56, 17).addBox(-1.0F, -13.0F, -1.0F, 2.0F, 13.0F, 2.0F)
                .texOffs(12, 35).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(-8.5F, 1.0F, 2.0F, 0.0F, 0.0F, -0.1F));
        PartDefinition shin_br = leg_br.addOrReplaceChild("shin_br", CubeListBuilder.create()
                .texOffs(90, 0).addBox(-1.5F, -16.0F, -0.5F, 3.0F, 16.0F, 1.0F)
                .texOffs(32, 27).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -13.0F, 0.0F));
        PartDefinition leg_bl = bob.addOrReplaceChild("leg_bl", CubeListBuilder.create()
                .texOffs(64, 17).addBox(-1.0F, -13.0F, -1.0F, 2.0F, 13.0F, 2.0F)
                .texOffs(64, 35).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(8.5F, 1.0F, 2.0F, 0.0F, 0.0F, 0.1F));
        PartDefinition shin_bl = leg_bl.addOrReplaceChild("shin_bl", CubeListBuilder.create()
                .texOffs(98, 0).addBox(-1.5F, -16.0F, -0.5F, 3.0F, 16.0F, 1.0F)
                .texOffs(24, 35).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -13.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 128, 128).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float t = state.attackTime;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float step = state.walkAnimationPos * 0.9F;
        boolean hung = state.phase < 2;
        this.rod.visible = hung;
        this.stub.visible = !hung;
        // how far the hands are down: not at all on the rod, wholly off it, and coming down just after it has parted
        float down = hung ? 0.0F : (state.attackId == BoltEntity.BREAK ? Anim.ramp(t, BoltEntity.BREAK_SNAP, BoltEntity.BREAK_SNAP + 18.0F) : 1.0F);
        if (hung) {
            this.bob.zRot -= (float) Math.atan(state.extra);
            for (int i = 0; i < 4; i++) {
                this.leg[i].zRot += SIDE[i] * (0.03F + Mth.sin(age * 0.5F + i) * 0.015F);    // (they rattle against the rod)
            }
        }
        this.bob.y -= LIFT * down;
        for (int i = 0; i < 4; i++) {
            Anim.pose(this.leg[i], down, 0.0F, -END[i] * SIDE[i] * 0.6F, SIDE[i] * 1.8F);
            Anim.pose(this.shin[i], down, 0.0F, 0.0F, SIDE[i] * 1.04F);
            // walking: the hands lifted two at a time, corner and corner
            float lift = Math.max(0.0F, Mth.sin(step + (i == 0 || i == 3 ? 0.0F : Mth.PI))) * speed * down;
            this.leg[i].zRot += SIDE[i] * 0.3F * lift;
            this.leg[i].yRot += -SIDE[i] * Mth.cos(step + (i == 0 || i == 3 ? 0.0F : Mth.PI)) * 0.25F * speed * down;
        }
        this.bob.y += Mth.sin(age * 0.08F) * 0.4F * down;
        switch (state.attackId) {
            case BoltEntity.TOLL -> {
                float ring = Anim.pulse(t, BoltEntity.TOLL_RING - 12.0F, BoltEntity.TOLL_RING, BoltEntity.TOLL_RING + 8.0F, BoltEntity.TOLL_END);
                this.bob.xRot += Mth.sin(t * 1.9F) * 0.07F * ring;
                this.bob.y += 3.0F * ring * down;
                for (int i = 0; i < 4; i++) {
                    this.leg[i].zRot += SIDE[i] * 0.5F * ring;
                }
            }
            case BoltEntity.WEIGHTS -> {
                float call = Anim.pulse(t, 2.0F, BoltEntity.WEIGHTS_CALL, BoltEntity.WEIGHTS_CALL + 10.0F, BoltEntity.WEIGHTS_DURATION);
                this.bob.yRot += Mth.sin(t * 0.5F) * 0.4F * call;
            }
            case BoltEntity.BREAK -> {
                float strain = Anim.pulse(t, 2.0F, BoltEntity.BREAK_SNAP - 4.0F, BoltEntity.BREAK_SNAP, BoltEntity.BREAK_SNAP + 2.0F);
                this.bob.zRot += Mth.sin(t * 2.3F) * 0.1F * strain;
                this.bob.xRot += Mth.sin(t * 1.7F) * 0.08F * strain;
                for (int i = 0; i < 4; i++) {
                    this.leg[i].zRot += SIDE[i] * 0.6F * strain;
                }
            }
            case BoltEntity.STAB -> {
                float impact = BoltEntity.STAB_IMPACT;
                float back = Anim.pulse(t, 0.0F, impact - 9.0F, impact - 4.0F, impact - 1.0F);
                float through = Anim.pulse(t, impact - 4.0F, impact, impact + 12.0F, BoltEntity.STAB_DURATION);
                this.bob.xRot += -0.25F * back + 0.3F * through;
                this.leg[0].zRot += 0.9F * back + 0.4F * through;
                this.shin[0].zRot += -1.6F * back - 1.0F * through;
                this.leg[0].xRot += -1.2F * through;
                this.leg[0].yRot += 0.5F * through;
            }
            case BoltEntity.SWEEP -> {
                float first = BoltEntity.SWEEP_HITS[0];
                float last = BoltEntity.SWEEP_HITS[1];
                float wide = Anim.pulse(t, 4.0F, first - 8.0F, last + 6.0F, BoltEntity.SWEEP_DURATION);
                this.all.yRot += Anim.ramp(t, first - 10.0F, last + 6.0F) * Mth.TWO_PI * 2.0F;
                this.bob.y += 4.0F * wide;
                for (int i = 0; i < 4; i++) {
                    this.leg[i].zRot += -SIDE[i] * 0.35F * wide;
                    this.shin[i].zRot += -SIDE[i] * 0.8F * wide;
                }
            }
            case BoltEntity.LEAP -> {
                float jump = BoltEntity.LEAP_JUMP;
                float land = BoltEntity.LEAP_LAND;
                float crouch = Anim.pulse(t, 0.0F, jump - 6.0F, jump - 1.0F, jump + 2.0F);
                float air = Anim.pulse(t, jump, jump + 4.0F, land - 2.0F, land);
                float crash = Anim.pulse(t, land - 1.0F, land + 2.0F, land + 16.0F, BoltEntity.LEAP_DURATION);
                this.bob.y += 6.0F * crouch + 8.0F * crash;
                this.bob.xRot += 0.1F * crash;
                for (int i = 0; i < 4; i++) {
                    this.leg[i].zRot += SIDE[i] * (0.5F * crouch + 0.5F * crash - 0.5F * air);
                    this.shin[i].zRot += SIDE[i] * 0.6F * air;
                }
            }
            default -> {
            }
        }
    }
}
