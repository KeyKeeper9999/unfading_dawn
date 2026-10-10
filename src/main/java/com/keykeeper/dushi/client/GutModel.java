package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.GutEntity;

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
 * The gut. The mesh between the markers is written by tools/gen_dusk.py (which also paints the texture and draws the
 * poses below on a sheet); the animation is written by hand. It is a mound in a shell of two halves that lean together
 * like a roof and are hinged along its flanks; it goes on a row of hands down each side; the arms that lie folded in
 * its mouth come out over the edges of the open shell.
 */
public class GutModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("gut"), "main");
    /** Must match the scale in the generator. */
    private static final float MODEL_SCALE = 2.0F;
    private static final int FEET = 5;
    private static final String[] SIDES = {"r", "l"};
    /** Which way a pose that turns the right side by a number turns each side. */
    private static final float[] SIDE = {1.0F, -1.0F};

    private final ModelPart all;
    private final ModelPart body;
    private final ModelPart stump;
    private final ModelPart tail;
    private final ModelPart[] lip = new ModelPart[2];
    private final ModelPart[] whisk = new ModelPart[2];
    private final ModelPart[][] foot = new ModelPart[2][FEET];
    private final ModelPart[][] grab = new ModelPart[2][2];
    private final ModelPart[][] grabFore = new ModelPart[2][2];

    public GutModel(ModelPart root) {
        super(root);
        this.all = root.getChild("all");
        this.body = this.all.getChild("body");
        this.stump = this.body.getChild("stump");
        this.tail = this.body.getChild("tail");
        for (int s = 0; s < 2; s++) {
            this.lip[s] = this.body.getChild("lip_" + SIDES[s]);
            this.whisk[s] = this.stump.getChild("whisk_" + SIDES[s]);
            for (int i = 0; i < FEET; i++) {
                this.foot[s][i] = this.body.getChild("foot_" + SIDES[s] + i);
            }
            for (int k = 0; k < 2; k++) {
                this.grab[s][k] = this.body.getChild("grab_" + SIDES[s] + k);
                this.grabFore[s][k] = this.grab[s][k].getChild("grabf_" + SIDES[s] + k);
            }
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-8.0F, -9.0F, -18.0F, 16.0F, 9.0F, 20.0F)
                .texOffs(152, 0).addBox(-5.5F, -12.0F, -17.5F, 11.0F, 3.0F, 19.0F)
                .texOffs(152, 22).addBox(-5.0F, -12.6F, -16.5F, 10.0F, 1.0F, 17.0F)
                .texOffs(148, 40).addBox(-6.0F, -8.0F, 2.0F, 12.0F, 8.0F, 3.0F)
                .texOffs(38, 29).addBox(-4.0F, -6.5F, 5.0F, 8.0F, 6.0F, 3.0F)
                .texOffs(72, 0).addBox(-9.0F, -8.5F, -17.5F, 1.0F, 6.0F, 19.0F)
                .texOffs(112, 0).addBox(8.0F, -8.5F, -17.5F, 1.0F, 6.0F, 19.0F),
                PartPose.offset(0.0F, -4.0F, 8.0F));
        PartDefinition stump = body.addOrReplaceChild("stump", CubeListBuilder.create()
                .texOffs(120, 25).addBox(-6.0F, -6.0F, -4.0F, 12.0F, 8.0F, 4.0F)
                .texOffs(212, 42).addBox(-4.0F, -4.5F, -8.0F, 8.0F, 6.0F, 4.0F)
                .texOffs(56, 50).addBox(-3.0F, -4.0F, -8.6F, 6.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -3.0F, -18.0F, 0.2F, 0.0F, 0.0F));
        PartDefinition lip_r = body.addOrReplaceChild("lip_r", CubeListBuilder.create()
                .texOffs(178, 40).addBox(0.0F, -2.0F, -19.0F, 9.0F, 2.0F, 8.0F)
                .texOffs(0, 29).addBox(-0.5F, -3.0F, -12.5F, 10.0F, 3.0F, 9.0F)
                .texOffs(76, 38).addBox(0.0F, -2.0F, -5.0F, 9.0F, 2.0F, 9.0F)
                .texOffs(212, 0).addBox(8.0F, 0.0F, -16.5F, 1.0F, 4.0F, 17.0F),
                PartPose.offsetAndRotation(-8.5F, -9.0F, 0.0F, 0.0F, 0.0F, -0.4F));
        PartDefinition lip_l = body.addOrReplaceChild("lip_l", CubeListBuilder.create()
                .texOffs(0, 41).addBox(-9.0F, -2.0F, -19.0F, 9.0F, 2.0F, 8.0F)
                .texOffs(38, 38).addBox(-9.5F, -3.0F, -12.5F, 10.0F, 3.0F, 9.0F)
                .texOffs(112, 38).addBox(-9.0F, -2.0F, -5.0F, 9.0F, 2.0F, 9.0F)
                .texOffs(212, 21).addBox(-9.0F, 0.0F, -16.5F, 1.0F, 4.0F, 17.0F),
                PartPose.offsetAndRotation(8.5F, -9.0F, 0.0F, 0.0F, 0.0F, 0.4F));
        PartDefinition foot_r0 = body.addOrReplaceChild("foot_r0", CubeListBuilder.create()
                .texOffs(248, 38).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F)
                .texOffs(76, 49).addBox(-3.0F, 4.0F, -4.0F, 5.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(-7.0F, -1.0F, -16.0F, 0.0F, 0.0F, 0.12F));
        PartDefinition foot_l0 = body.addOrReplaceChild("foot_l0", CubeListBuilder.create()
                .texOffs(164, 51).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F)
                .texOffs(98, 49).addBox(-2.0F, 4.0F, -4.0F, 5.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(7.0F, -1.0F, -16.0F, 0.0F, 0.0F, -0.12F));
        PartDefinition foot_r1 = body.addOrReplaceChild("foot_r1", CubeListBuilder.create()
                .texOffs(78, 56).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F)
                .texOffs(120, 49).addBox(-3.0F, 4.0F, -4.0F, 5.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(-7.0F, -1.0F, -12.0F, 0.0F, 0.0F, 0.12F));
        PartDefinition foot_l1 = body.addOrReplaceChild("foot_l1", CubeListBuilder.create()
                .texOffs(86, 56).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F)
                .texOffs(34, 50).addBox(-2.0F, 4.0F, -4.0F, 5.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(7.0F, -1.0F, -12.0F, 0.0F, 0.0F, -0.12F));
        PartDefinition foot_r2 = body.addOrReplaceChild("foot_r2", CubeListBuilder.create()
                .texOffs(94, 56).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F)
                .texOffs(178, 50).addBox(-3.0F, 4.0F, -4.0F, 5.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(-7.0F, -1.0F, -8.0F, 0.0F, 0.0F, 0.12F));
        PartDefinition foot_l2 = body.addOrReplaceChild("foot_l2", CubeListBuilder.create()
                .texOffs(102, 56).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F)
                .texOffs(0, 51).addBox(-2.0F, 4.0F, -4.0F, 5.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(7.0F, -1.0F, -8.0F, 0.0F, 0.0F, -0.12F));
        PartDefinition foot_r3 = body.addOrReplaceChild("foot_r3", CubeListBuilder.create()
                .texOffs(110, 56).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F)
                .texOffs(142, 51).addBox(-3.0F, 4.0F, -4.0F, 5.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(-7.0F, -1.0F, -4.0F, 0.0F, 0.0F, 0.12F));
        PartDefinition foot_l3 = body.addOrReplaceChild("foot_l3", CubeListBuilder.create()
                .texOffs(118, 56).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F)
                .texOffs(200, 52).addBox(-2.0F, 4.0F, -4.0F, 5.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(7.0F, -1.0F, -4.0F, 0.0F, 0.0F, -0.12F));
        PartDefinition foot_r4 = body.addOrReplaceChild("foot_r4", CubeListBuilder.create()
                .texOffs(126, 56).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F)
                .texOffs(222, 52).addBox(-3.0F, 4.0F, -4.0F, 5.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(-7.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.12F));
        PartDefinition foot_l4 = body.addOrReplaceChild("foot_l4", CubeListBuilder.create()
                .texOffs(134, 56).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F)
                .texOffs(56, 56).addBox(-2.0F, 4.0F, -4.0F, 5.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(7.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.12F));
        PartDefinition grab_r0 = body.addOrReplaceChild("grab_r0", CubeListBuilder.create()
                .texOffs(248, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(-3.0F, -10.5F, -15.0F, 1.5708F, 0.0F, 0.0F));
        PartDefinition grabf_r0 = grab_r0.addOrReplaceChild("grabf_r0", CubeListBuilder.create()
                .texOffs(248, 13).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F)
                .texOffs(60, 29).addBox(-2.5F, 11.0F, -0.5F, 5.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 11.0F, 0.0F, -3.0F, 0.0F, 0.0F));
        PartDefinition grab_l0 = body.addOrReplaceChild("grab_l0", CubeListBuilder.create()
                .texOffs(72, 25).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(3.0F, -10.5F, -15.0F, 1.5708F, 0.0F, 0.0F));
        PartDefinition grabf_l0 = grab_l0.addOrReplaceChild("grabf_l0", CubeListBuilder.create()
                .texOffs(80, 25).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F)
                .texOffs(236, 42).addBox(-2.5F, 11.0F, -0.5F, 5.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 11.0F, 0.0F, -3.0F, 0.0F, 0.0F));
        PartDefinition grab_r1 = body.addOrReplaceChild("grab_r1", CubeListBuilder.create()
                .texOffs(88, 25).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(-3.0F, -10.5F, -8.0F, 1.5708F, 0.0F, 0.0F));
        PartDefinition grabf_r1 = grab_r1.addOrReplaceChild("grabf_r1", CubeListBuilder.create()
                .texOffs(96, 25).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F)
                .texOffs(244, 49).addBox(-2.5F, 11.0F, -0.5F, 5.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 11.0F, 0.0F, -3.0F, 0.0F, 0.0F));
        PartDefinition grab_l1 = body.addOrReplaceChild("grab_l1", CubeListBuilder.create()
                .texOffs(104, 25).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(3.0F, -10.5F, -8.0F, 1.5708F, 0.0F, 0.0F));
        PartDefinition grabf_l1 = grab_l1.addOrReplaceChild("grabf_l1", CubeListBuilder.create()
                .texOffs(112, 25).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F)
                .texOffs(22, 51).addBox(-2.5F, 11.0F, -0.5F, 5.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 11.0F, 0.0F, -3.0F, 0.0F, 0.0F));
        PartDefinition whisk_r = stump.addOrReplaceChild("whisk_r", CubeListBuilder.create()
                .texOffs(206, 22).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F)
                .texOffs(248, 45).addBox(-1.5F, 4.0F, -0.5F, 3.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(-3.0F, 1.0F, -7.0F, -0.9F, 0.0F, 0.3F));
        PartDefinition whisk_l = stump.addOrReplaceChild("whisk_l", CubeListBuilder.create()
                .texOffs(206, 27).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F)
                .texOffs(34, 57).addBox(-1.5F, 4.0F, -0.5F, 3.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(3.0F, 1.0F, -7.0F, -0.9F, 0.0F, -0.3F));
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(248, 26).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F)
                .texOffs(244, 56).addBox(-2.0F, 10.0F, -0.5F, 4.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -3.0F, 7.5F, 1.2F, 0.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 256, 128).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    /** The two halves of the shell apart by so much (about 1.1 is wide open: they stand up from its flanks). */
    private void lips(float open) {
        this.lip[0].zRot -= open;
        this.lip[1].zRot += open;
    }

    /** The arms of its mouth so far out of it: up through the open shell, over its edge and down beside it. */
    private void arms(float out) {
        for (int s = 0; s < 2; s++) {
            for (int k = 0; k < 2; k++) {
                Anim.pose(this.grab[s][k], out, -1.5708F, SIDE[s] * (k == 0 ? -0.45F : 0.45F), SIDE[s] * 2.87F);
                Anim.pose(this.grabFore[s][k], out, 3.0F, 0.0F, -SIDE[s] * 1.9F);
            }
        }
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float t = state.attackTime;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float step = state.walkAnimationPos * 1.6F;
        // the shell is never quite shut: it breathes through the seam of it
        this.lips(0.03F + Mth.sin(age * 0.05F) * 0.03F);
        this.tail.yRot += Mth.sin(age * 0.07F) * 0.3F;
        this.stump.yRot += Mth.sin(age * 0.04F) * 0.14F;
        this.whisk[0].xRot += Mth.sin(age * 0.19F) * 0.3F;
        this.whisk[1].xRot += Mth.sin(age * 0.16F + 1.3F) * 0.3F;
        // it goes on its hands: a wave runs down each row of them, the two rows out of step, and it rolls a little as it goes
        float scrabble = 0.0F;
        if (state.attackId == GutEntity.HEAVE) {
            scrabble = Anim.pulse(t, GutEntity.HEAVE_START - 3.0F, GutEntity.HEAVE_START + 2.0F, GutEntity.HEAVE_END, GutEntity.HEAVE_END + 6.0F);
        }
        float going = Math.max(speed, scrabble);
        float beat = scrabble > 0.0F ? t * 1.7F : step;
        for (int s = 0; s < 2; s++) {
            for (int i = 0; i < FEET; i++) {
                float wave = beat - i * 1.1F + s * Mth.PI;
                this.foot[s][i].xRot += Mth.sin(wave) * 0.55F * going + Mth.sin(age * 0.05F + i + s * 2.0F) * 0.04F;
                this.foot[s][i].y -= Math.max(0.0F, Mth.cos(wave)) * 1.2F * going;
            }
        }
        this.body.zRot += Mth.sin(step * 0.5F) * 0.035F * speed;
        this.body.y += Mth.sin(age * 0.05F) * 0.25F;
        switch (state.attackId) {
            case GutEntity.HEAVE -> {
                float start = GutEntity.HEAVE_START;
                float drawn = Anim.pulse(t, 0.0F, start - 8.0F, start - 2.0F, start + 2.0F);
                float gone = Anim.pulse(t, start - 2.0F, start + 3.0F, GutEntity.HEAVE_END, GutEntity.HEAVE_DURATION);
                // it gathers itself back on its hind end, and throws its whole weight forward
                this.body.xRot += -0.14F * drawn + 0.1F * gone;
                this.body.z += 3.0F * drawn - 2.0F * gone;
                this.body.y += 1.0F * drawn;
                this.stump.xRot += -0.3F * drawn + 0.2F * gone;
                this.lips(0.15F * drawn + 0.3F * gone);
                this.body.zRot += Mth.sin(t * 1.5F) * 0.03F * drawn;
            }
            case GutEntity.SLAM -> {
                float impact = GutEntity.SLAM_IMPACT;
                float reared = Anim.pulse(t, 0.0F, impact - 12.0F, impact - 5.0F, impact);
                float down = Anim.pulse(t, impact - 5.0F, impact, GutEntity.SLAM_SHUT - 8.0F, GutEntity.SLAM_SHUT + 6.0F);
                this.body.xRot += -0.6F * reared + 0.07F * down;
                this.stump.xRot += -0.25F * reared + 0.3F * down;
                this.lips(0.4F * reared + 1.0F * down);
                // the hands of its fore end, off the ground and working
                for (int s = 0; s < 2; s++) {
                    for (int i = 0; i < 3; i++) {
                        this.foot[s][i].xRot += (0.5F + Mth.sin(t * 0.9F + i) * 0.3F) * reared;
                    }
                }
            }
            case GutEntity.GAPE -> {
                float impact = GutEntity.GAPE_IMPACT;
                float open = Anim.pulse(t, 0.0F, impact - 12.0F, GutEntity.GAPE_SHUT - 8.0F, GutEntity.GAPE_SHUT);
                float out = Anim.pulse(t, impact - 9.0F, impact, impact + 12.0F, GutEntity.GAPE_SHUT - 8.0F);
                this.lips(1.1F * open);
                this.arms(out);
                this.all.y += 1.0F * open;
                // they come down beside it and feel along the ground
                for (int s = 0; s < 2; s++) {
                    for (int k = 0; k < 2; k++) {
                        this.grabFore[s][k].zRot += SIDE[s] * Mth.sin(t * 0.7F + k * 2.0F) * 0.18F * out;
                        this.grab[s][k].xRot += Mth.sin(t * 0.5F + k * 3.0F) * 0.2F * out;
                    }
                }
            }
            case GutEntity.STAGGER -> {
                float open = Anim.pulse(t, 1.0F, 8.0F, GutEntity.STAGGER_DURATION - 12.0F, GutEntity.STAGGER_DURATION);
                this.lips(1.1F * open);
                this.all.y += 1.5F * open;
                this.body.xRot += 0.08F * open;
                this.body.zRot += Mth.sin(t * 0.35F) * 0.05F * open;
                this.stump.xRot += 0.35F * open;
                this.arms(0.35F * open);
            }
            default -> {
            }
        }
    }
}
