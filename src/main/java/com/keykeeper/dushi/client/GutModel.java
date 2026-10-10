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
 * poses below on a sheet); the animation is written by hand. 'body' hangs at the hind quarters, so that it rears
 * about them; the two lips of its back are hinged along its flanks and open outwards.
 */
public class GutModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("gut"), "main");
    /** Must match the scale in the generator. */
    private static final float MODEL_SCALE = 1.5F;

    private final ModelPart all;
    private final ModelPart body;
    private final ModelPart stump;
    private final ModelPart lipRight;
    private final ModelPart lipLeft;
    private final ModelPart foreRight;
    private final ModelPart foreShinRight;
    private final ModelPart foreLeft;
    private final ModelPart foreShinLeft;
    private final ModelPart hindRight;
    private final ModelPart hindShinRight;
    private final ModelPart hindLeft;
    private final ModelPart hindShinLeft;
    private final ModelPart tail;

    public GutModel(ModelPart root) {
        super(root);
        this.all = root.getChild("all");
        this.body = this.all.getChild("body");
        this.stump = this.body.getChild("stump");
        this.lipRight = this.body.getChild("lip_r");
        this.lipLeft = this.body.getChild("lip_l");
        this.foreRight = this.body.getChild("fore_r");
        this.foreShinRight = this.foreRight.getChild("fore_shin_r");
        this.foreLeft = this.body.getChild("fore_l");
        this.foreShinLeft = this.foreLeft.getChild("fore_shin_l");
        this.hindRight = this.all.getChild("hind_r");
        this.hindShinRight = this.hindRight.getChild("hind_shin_r");
        this.hindLeft = this.all.getChild("hind_l");
        this.hindShinLeft = this.hindLeft.getChild("hind_shin_l");
        this.tail = this.body.getChild("tail");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6.0F, -5.0F, -18.0F, 12.0F, 10.0F, 21.0F)
                .texOffs(0, 69).addBox(-5.0F, -5.5F, -17.0F, 10.0F, 1.0F, 19.0F),
                PartPose.offset(0.0F, -17.0F, 7.0F));
        PartDefinition stump = body.addOrReplaceChild("stump", CubeListBuilder.create()
                .texOffs(40, 31).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 4.0F)
                .texOffs(58, 77).addBox(-3.0F, -3.0F, -5.0F, 6.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -1.0F, -18.0F, 0.2F, 0.0F, 0.0F));
        PartDefinition lip_r = body.addOrReplaceChild("lip_r", CubeListBuilder.create()
                .texOffs(66, 0).addBox(0.0F, -3.0F, -17.5F, 6.0F, 3.0F, 20.0F)
                .texOffs(0, 31).addBox(5.0F, -1.0F, -17.0F, 1.0F, 4.0F, 19.0F),
                PartPose.offset(-6.0F, -5.0F, 0.0F));
        PartDefinition lip_l = body.addOrReplaceChild("lip_l", CubeListBuilder.create()
                .texOffs(66, 23).addBox(-6.0F, -3.0F, -17.5F, 6.0F, 3.0F, 20.0F)
                .texOffs(40, 46).addBox(-6.0F, -1.0F, -17.0F, 1.0F, 4.0F, 19.0F),
                PartPose.offset(6.0F, -5.0F, 0.0F));
        PartDefinition fore_r = body.addOrReplaceChild("fore_r", CubeListBuilder.create()
                .texOffs(80, 46).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 7.0F, 5.0F),
                PartPose.offset(-4.5F, 4.0F, -14.0F));
        PartDefinition fore_shin_r = fore_r.addOrReplaceChild("fore_shin_r", CubeListBuilder.create()
                .texOffs(58, 69).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F)
                .texOffs(80, 58).addBox(-2.5F, 4.0F, -3.5F, 5.0F, 2.0F, 6.0F),
                PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition fore_l = body.addOrReplaceChild("fore_l", CubeListBuilder.create()
                .texOffs(100, 46).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 7.0F, 5.0F),
                PartPose.offset(4.5F, 4.0F, -14.0F));
        PartDefinition fore_shin_l = fore_l.addOrReplaceChild("fore_shin_l", CubeListBuilder.create()
                .texOffs(74, 74).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F)
                .texOffs(102, 58).addBox(-2.5F, 4.0F, -3.5F, 5.0F, 2.0F, 6.0F),
                PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition hind_r = all.addOrReplaceChild("hind_r", CubeListBuilder.create()
                .texOffs(0, 54).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 7.0F, 5.0F),
                PartPose.offset(-4.5F, -13.0F, 7.0F));
        PartDefinition hind_shin_r = hind_r.addOrReplaceChild("hind_shin_r", CubeListBuilder.create()
                .texOffs(90, 74).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F)
                .texOffs(80, 66).addBox(-2.5F, 4.0F, -3.5F, 5.0F, 2.0F, 6.0F),
                PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition hind_l = all.addOrReplaceChild("hind_l", CubeListBuilder.create()
                .texOffs(20, 54).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 7.0F, 5.0F),
                PartPose.offset(4.5F, -13.0F, 7.0F));
        PartDefinition hind_shin_l = hind_l.addOrReplaceChild("hind_shin_l", CubeListBuilder.create()
                .texOffs(106, 74).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F)
                .texOffs(102, 66).addBox(-2.5F, 4.0F, -3.5F, 5.0F, 2.0F, 6.0F),
                PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(118, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 9.0F, 2.0F)
                .texOffs(118, 11).addBox(-2.0F, 9.0F, -0.5F, 4.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -2.0F, 3.0F, 0.9F, 0.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 128, 128).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    private void lips(float open) {
        this.lipRight.zRot -= open;
        this.lipLeft.zRot += open;
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float t = state.attackTime;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float step = state.walkAnimationPos * 0.8F;
        // the back is never quite shut: it breathes through it
        this.lips(0.06F + Mth.sin(age * 0.06F) * 0.05F);
        this.tail.zRot += Mth.sin(age * 0.11F) * 0.25F;
        this.stump.yRot += Mth.sin(age * 0.045F) * 0.12F;
        this.foreRight.xRot += Mth.sin(step) * 0.5F * speed;
        this.foreLeft.xRot -= Mth.sin(step) * 0.5F * speed;
        this.hindRight.xRot -= Mth.sin(step) * 0.5F * speed;
        this.hindLeft.xRot += Mth.sin(step) * 0.5F * speed;
        this.body.y += Math.abs(Mth.sin(step)) * 0.5F * speed;
        switch (state.attackId) {
            case GutEntity.CHARGE -> {
                float start = GutEntity.CHARGE_START;
                float paw = Anim.pulse(t, 0.0F, 6.0F, start - 3.0F, start);
                float run = Anim.pulse(t, start - 2.0F, start + 2.0F, GutEntity.CHARGE_END, GutEntity.CHARGE_DURATION);
                this.body.xRot += 0.12F * paw + 0.08F * run;
                this.foreRight.xRot += (-0.7F + Mth.sin(t * 0.8F) * 0.4F) * paw;
                this.foreShinRight.xRot += 1.0F * paw;
                float gallop = Mth.sin(t * 1.1F);
                this.foreRight.xRot += gallop * 0.8F * run;
                this.foreLeft.xRot += Mth.sin(t * 1.1F + 0.6F) * 0.8F * run;
                this.hindRight.xRot -= gallop * 0.8F * run;
                this.hindLeft.xRot -= Mth.sin(t * 1.1F + 0.6F) * 0.8F * run;
                this.lips(0.3F * paw + 0.45F * run);
            }
            case GutEntity.SLAM -> {
                float impact = GutEntity.SLAM_IMPACT;
                float reared = Anim.pulse(t, 0.0F, 18.0F, impact - 5.0F, impact);
                float down = Anim.pulse(t, impact - 5.0F, impact, impact + 14.0F, GutEntity.SLAM_DURATION);
                this.body.xRot += -0.75F * reared + 0.2F * down;
                this.foreRight.xRot += -0.5F * reared - 0.35F * down;
                this.foreLeft.xRot += -0.9F * reared - 0.35F * down;
                this.foreShinRight.xRot += 0.8F * reared;
                this.foreShinLeft.xRot += 0.6F * reared;
                this.lips(0.9F * reared + 1.1F * down);
            }
            case GutEntity.GAPE -> {
                float impact = GutEntity.GAPE_IMPACT;
                float open = Anim.pulse(t, 0.0F, impact - 7.0F, impact - 2.0F, impact + 1.0F);
                this.lips(1.15F * open);
                this.all.y += 1.5F * open;
            }
            case GutEntity.STAGGER -> {
                float open = Anim.pulse(t, 1.0F, 8.0F, GutEntity.STAGGER_DURATION - 12.0F, GutEntity.STAGGER_DURATION);
                this.lips(1.15F * open);
                this.all.y += 2.0F * open;
                this.body.xRot += 0.1F * open;
                this.body.zRot += Mth.sin(t * 0.35F) * 0.05F * open;
                this.stump.xRot += 0.4F * open;
            }
            default -> {
            }
        }
    }
}
