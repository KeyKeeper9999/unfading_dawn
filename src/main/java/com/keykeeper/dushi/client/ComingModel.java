package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.ComingEntity;

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
 * The Coming. The mesh between the markers is written by tools/gen_coming.py (which also paints the texture and draws
 * the poses below on a sheet); the animation is written by hand. Its neck is three lengths, each bowed a little on
 * the one under it, and the disc at the top of it looks down at whoever is before it. The mask is drawn in its first
 * phase only; in its second the arms folded behind the disc are open round it.
 */
public class ComingModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("coming"), "main");
    /** Must match the scale in the generator. */
    private static final float MODEL_SCALE = 2.0F;

    private final ModelPart all;
    private final ModelPart rift;
    private final ModelPart neck1;
    private final ModelPart neck2;
    private final ModelPart neck3;
    private final ModelPart mawRight;
    private final ModelPart mawLeft;
    private final ModelPart head;
    private final ModelPart mask;
    private final ModelPart armRight;
    private final ModelPart foreRight;
    private final ModelPart handRight;
    private final ModelPart armLeft;
    private final ModelPart foreLeft;
    private final ModelPart handLeft;

    /** The arms round the disc: must match CROWN in tools/gen_coming.py. */
    private static final int CROWN = 6;
    private final ModelPart[] crown = new ModelPart[CROWN];
    private final ModelPart[] crownFore = new ModelPart[CROWN];

    public ComingModel(ModelPart root) {
        super(root);
        this.all = root.getChild("all");
        this.rift = this.all.getChild("rift");
        this.neck1 = this.all.getChild("neck1");
        this.neck2 = this.neck1.getChild("neck2");
        this.neck3 = this.neck2.getChild("neck3");
        this.mawRight = this.neck3.getChild("maw_r");
        this.mawLeft = this.neck3.getChild("maw_l");
        this.head = this.neck3.getChild("head");
        this.mask = this.head.getChild("mask");
        for (int i = 0; i < CROWN; i++) {
            this.crown[i] = this.head.getChild("crown" + i);
            this.crownFore[i] = this.crown[i].getChild("crownf" + i);
        }
        this.armRight = this.all.getChild("arm_r");
        this.foreRight = this.armRight.getChild("fore_r");
        this.handRight = this.foreRight.getChild("hand_r");
        this.armLeft = this.all.getChild("arm_l");
        this.foreLeft = this.armLeft.getChild("fore_l");
        this.handLeft = this.foreLeft.getChild("hand_l");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition rift = all.addOrReplaceChild("rift", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-16.0F, -1.0F, -16.0F, 32.0F, 1.0F, 32.0F),
                PartPose.ZERO);
        PartDefinition neck1 = all.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(128, 0).addBox(-6.0F, -18.0F, -6.0F, 12.0F, 18.0F, 12.0F)
                .texOffs(128, 30).addBox(-0.5F, -18.0F, 6.0F, 1.0F, 18.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -1.0F, 3.0F, -0.12F, 0.0F, 0.0F));
        PartDefinition neck2 = neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(176, 0).addBox(-5.0F, -16.0F, -5.0F, 10.0F, 16.0F, 10.0F)
                .texOffs(138, 30).addBox(-0.5F, -16.0F, 5.0F, 1.0F, 16.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -18.0F, 0.0F, 0.18F, 0.0F, 0.0F));
        PartDefinition neck3 = neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(216, 23).addBox(-4.0F, -14.0F, -4.0F, 8.0F, 14.0F, 8.0F)
                .texOffs(248, 23).addBox(-0.5F, -14.0F, 4.0F, 1.0F, 14.0F, 3.0F)
                .texOffs(216, 45).addBox(-3.0F, -12.0F, -4.5F, 6.0F, 10.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 0.28F, 0.0F, 0.0F));
        PartDefinition maw_r = neck3.addOrReplaceChild("maw_r", CubeListBuilder.create()
                .texOffs(148, 43).addBox(0.0F, 0.0F, -1.0F, 4.0F, 11.0F, 1.0F),
                PartPose.offset(-4.0F, -12.5F, -5.0F));
        PartDefinition maw_l = neck3.addOrReplaceChild("maw_l", CubeListBuilder.create()
                .texOffs(158, 43).addBox(-4.0F, 0.0F, -1.0F, 4.0F, 11.0F, 1.0F),
                PartPose.offset(4.0F, -12.5F, -5.0F));
        PartDefinition head = neck3.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 33).addBox(-9.0F, -9.0F, -4.0F, 18.0F, 18.0F, 3.0F)
                .texOffs(0, 54).addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -14.0F, 0.0F, 0.5F, 0.0F, 0.0F));
        PartDefinition mask = head.addOrReplaceChild("mask", CubeListBuilder.create()
                .texOffs(42, 33).addBox(-7.0F, -9.0F, -1.5F, 14.0F, 17.0F, 1.0F)
                .texOffs(72, 33).addBox(-13.0F, -20.0F, -1.0F, 26.0F, 12.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, -4.0F));
        PartDefinition crown0 = head.addOrReplaceChild("crown0", CubeListBuilder.create()
                .texOffs(208, 26).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(4.0F, -6.93F, -1.0F, -2.5F, 0.0F, 0.5236F));
        PartDefinition crownf0 = crown0.addOrReplaceChild("crownf0", CubeListBuilder.create()
                .texOffs(172, 30).addBox(-0.5F, -10.0F, -0.5F, 1.0F, 10.0F, 1.0F)
                .texOffs(84, 46).addBox(-2.5F, -17.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown1 = head.addOrReplaceChild("crown1", CubeListBuilder.create()
                .texOffs(148, 30).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(8.0F, -0.0F, -1.0F, -2.5F, 0.0F, 1.5708F));
        PartDefinition crownf1 = crown1.addOrReplaceChild("crownf1", CubeListBuilder.create()
                .texOffs(172, 41).addBox(-0.5F, -10.0F, -0.5F, 1.0F, 10.0F, 1.0F)
                .texOffs(96, 46).addBox(-2.5F, -17.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown2 = head.addOrReplaceChild("crown2", CubeListBuilder.create()
                .texOffs(156, 30).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(4.0F, 6.93F, -1.0F, -2.5F, 0.0F, 2.618F));
        PartDefinition crownf2 = crown2.addOrReplaceChild("crownf2", CubeListBuilder.create()
                .texOffs(168, 43).addBox(-0.5F, -10.0F, -0.5F, 1.0F, 10.0F, 1.0F)
                .texOffs(108, 46).addBox(-2.5F, -17.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown3 = head.addOrReplaceChild("crown3", CubeListBuilder.create()
                .texOffs(164, 30).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(-4.0F, 6.93F, -1.0F, -2.5F, 0.0F, 3.6652F));
        PartDefinition crownf3 = crown3.addOrReplaceChild("crownf3", CubeListBuilder.create()
                .texOffs(230, 45).addBox(-0.5F, -10.0F, -0.5F, 1.0F, 10.0F, 1.0F)
                .texOffs(176, 48).addBox(-2.5F, -17.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown4 = head.addOrReplaceChild("crown4", CubeListBuilder.create()
                .texOffs(208, 39).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(-8.0F, 0.0F, -1.0F, -2.5F, 0.0F, 4.7124F));
        PartDefinition crownf4 = crown4.addOrReplaceChild("crownf4", CubeListBuilder.create()
                .texOffs(234, 45).addBox(-0.5F, -10.0F, -0.5F, 1.0F, 10.0F, 1.0F)
                .texOffs(188, 48).addBox(-2.5F, -17.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown5 = head.addOrReplaceChild("crown5", CubeListBuilder.create()
                .texOffs(248, 40).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(-4.0F, -6.93F, -1.0F, -2.5F, 0.0F, 5.7596F));
        PartDefinition crownf5 = crown5.addOrReplaceChild("crownf5", CubeListBuilder.create()
                .texOffs(238, 45).addBox(-0.5F, -10.0F, -0.5F, 1.0F, 10.0F, 1.0F)
                .texOffs(42, 51).addBox(-2.5F, -17.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition arm_r = all.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(216, 0).addBox(-2.5F, -18.0F, -2.5F, 5.0F, 18.0F, 5.0F),
                PartPose.offsetAndRotation(-12.0F, -1.0F, 2.0F, 0.2F, 0.0F, -0.45F));
        PartDefinition fore_r = arm_r.addOrReplaceChild("fore_r", CubeListBuilder.create()
                .texOffs(176, 26).addBox(-2.0F, -18.0F, -2.0F, 4.0F, 18.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -18.0F, 0.0F, -1.0F, 0.0F, 0.35F));
        PartDefinition hand_r = fore_r.addOrReplaceChild("hand_r", CubeListBuilder.create()
                .texOffs(54, 51).addBox(-3.0F, -5.0F, -1.5F, 6.0F, 5.0F, 3.0F)
                .texOffs(120, 46).addBox(-3.0F, -13.0F, -0.5F, 1.0F, 8.0F, 1.0F)
                .texOffs(242, 45).addBox(-1.0F, -14.0F, -0.5F, 1.0F, 9.0F, 1.0F)
                .texOffs(72, 46).addBox(1.0F, -14.0F, -0.5F, 1.0F, 9.0F, 1.0F)
                .texOffs(200, 48).addBox(3.0F, -12.0F, -0.5F, 1.0F, 7.0F, 1.0F)
                .texOffs(138, 50).addBox(-5.0F, -6.0F, -0.5F, 2.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -18.0F, 0.0F, -0.5F, 0.0F, 0.0F));
        PartDefinition arm_l = all.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(236, 0).addBox(-2.5F, -18.0F, -2.5F, 5.0F, 18.0F, 5.0F),
                PartPose.offsetAndRotation(12.0F, -1.0F, 2.0F, 0.2F, 0.0F, 0.45F));
        PartDefinition fore_l = arm_l.addOrReplaceChild("fore_l", CubeListBuilder.create()
                .texOffs(192, 26).addBox(-2.0F, -18.0F, -2.0F, 4.0F, 18.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -18.0F, 0.0F, -1.0F, 0.0F, -0.35F));
        PartDefinition hand_l = fore_l.addOrReplaceChild("hand_l", CubeListBuilder.create()
                .texOffs(128, 52).addBox(-3.0F, -5.0F, -1.5F, 6.0F, 5.0F, 3.0F)
                .texOffs(124, 46).addBox(2.0F, -13.0F, -0.5F, 1.0F, 8.0F, 1.0F)
                .texOffs(76, 46).addBox(0.0F, -14.0F, -0.5F, 1.0F, 9.0F, 1.0F)
                .texOffs(80, 46).addBox(-2.0F, -14.0F, -0.5F, 1.0F, 9.0F, 1.0F)
                .texOffs(204, 48).addBox(-4.0F, -12.0F, -0.5F, 1.0F, 7.0F, 1.0F)
                .texOffs(208, 52).addBox(3.0F, -6.0F, -0.5F, 2.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -18.0F, 0.0F, -0.5F, 0.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 256, 128).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float t = state.attackTime;
        boolean bare = state.phase >= 2;
        this.mask.visible = !bare;
        // it sways, as a thing does that is held up by nothing; the tear turns under it
        this.rift.yRot += age * 0.006F;
        this.neck1.zRot += Mth.sin(age * 0.03F) * 0.04F;
        this.neck2.xRot += Mth.sin(age * 0.041F) * 0.04F;
        this.neck2.zRot += Mth.sin(age * 0.027F + 1.0F) * 0.04F;
        this.neck3.xRot += Mth.sin(age * 0.05F + 2.0F) * 0.04F;
        this.head.zRot += Mth.sin(age * 0.022F) * 0.06F;
        this.armRight.zRot += Mth.sin(age * 0.035F) * 0.06F;
        this.armLeft.zRot -= Mth.sin(age * 0.031F + 1.5F) * 0.06F;
        this.foreRight.xRot += Mth.sin(age * 0.045F) * 0.08F;
        this.foreLeft.xRot += Mth.sin(age * 0.04F + 0.7F) * 0.08F;
        this.handRight.xRot += Mth.sin(age * 0.07F) * 0.12F;
        this.handLeft.xRot += Mth.sin(age * 0.06F + 2.2F) * 0.12F;
        // the crown: folded behind the disc under the mask, open round it without
        float open = bare ? (state.attackId == ComingEntity.UNMASK ? Anim.ramp(t, ComingEntity.UNMASK_BREAK, ComingEntity.UNMASK_BREAK + 22.0F) : 1.0F) : 0.0F;
        float spread = 0.0F;
        if (state.attackId == ComingEntity.RAIN) {
            spread = 0.35F * Anim.pulse(t, 4.0F, ComingEntity.RAIN_FROM, ComingEntity.RAIN_UNTIL, ComingEntity.RAIN_DURATION);
        } else if (state.attackId == ComingEntity.PULL) {
            spread = 0.2F * Anim.pulse(t, 4.0F, ComingEntity.PULL_FROM, ComingEntity.PULL_BITE, ComingEntity.PULL_DURATION);
        }
        for (int i = 0; i < CROWN; i++) {
            Anim.pose(this.crown[i], open, 2.75F + spread + Mth.sin(age * 0.05F + i * 1.3F) * 0.08F, 0.0F, 0.35F + Mth.sin(age * 0.03F + i) * 0.1F);
            Anim.pose(this.crownFore[i], open, -3.0F + spread * 1.2F + Mth.sin(age * 0.07F + i * 2.1F) * 0.15F, 0.0F, 0.0F);
            if (!bare) {
                this.crownFore[i].xRot += Mth.sin(age * 0.1F + i * 1.7F) * 0.06F;       // (fingers working at the edge of the mask)
            }
        }
        switch (state.attackId) {
            case ComingEntity.GAZE -> {
                float up = Anim.pulse(t, 2.0F, ComingEntity.GAZE_LOOK - 4.0F, ComingEntity.GAZE_DURATION - 24.0F, ComingEntity.GAZE_DURATION);
                this.neck2.xRot -= 0.15F * up;
                this.neck3.xRot -= 0.3F * up;
                this.head.xRot -= 0.25F * up;
            }
            case ComingEntity.SLAM -> {
                float impact = ComingEntity.SLAM_IMPACT;
                float back = Anim.pulse(t, 2.0F, impact - 14.0F, impact - 8.0F, impact - 2.0F);
                float down = Anim.pulse(t, impact - 8.0F, impact, ComingEntity.SLAM_LAIN, ComingEntity.SLAM_DURATION - 4.0F);
                this.neck1.xRot += -0.4F * back + 0.72F * down;
                this.neck2.xRot += -0.3F * back + 0.42F * down;
                this.neck3.xRot += -0.3F * back + 0.2F * down;
                this.head.xRot += -0.6F * back - 0.45F * down;
                this.armRight.zRot -= 0.3F * down;
                this.armLeft.zRot += 0.3F * down;
            }
            case ComingEntity.SWEEP -> {
                float impact = ComingEntity.SWEEP_IMPACT;
                float low = Anim.pulse(t, 4.0F, impact - 12.0F, impact + 12.0F, ComingEntity.SWEEP_DURATION);
                float through = Anim.ramp(t, impact - 8.0F, impact + 6.0F);
                Anim.pose(this.armRight, low, 1.0F, 0.9F - 2.1F * through, -0.6F);
                Anim.pose(this.foreRight, low, 0.8F, 0.0F, -0.3F);
                Anim.pose(this.handRight, low, 0.4F, 0.0F, 0.0F);
                this.neck1.yRot += (0.25F - 0.5F * through) * low;
            }
            case ComingEntity.CRUSH -> {
                float impact = ComingEntity.CRUSH_IMPACT;
                float up = Anim.pulse(t, 0.0F, impact - 12.0F, impact - 6.0F, impact - 2.0F);
                float down = Anim.pulse(t, impact - 6.0F, impact, impact + 14.0F, ComingEntity.CRUSH_DURATION);
                Anim.pose(this.armRight, up, -0.5F, 0.0F, 0.3F);
                Anim.pose(this.armLeft, up, -0.5F, 0.0F, -0.3F);
                Anim.pose(this.armRight, down, 0.9F, 0.0F, 0.35F);
                Anim.pose(this.armLeft, down, 0.9F, 0.0F, -0.35F);
                Anim.pose(this.foreRight, down, 1.3F, 0.0F, -0.35F);
                Anim.pose(this.foreLeft, down, 1.3F, 0.0F, 0.35F);
                this.neck1.xRot -= 0.2F * up;
            }
            case ComingEntity.UNMASK -> {
                float strain = Anim.pulse(t, 6.0F, 20.0F, ComingEntity.UNMASK_BREAK - 2.0F, ComingEntity.UNMASK_BREAK + 2.0F);
                float thrown = Anim.pulse(t, ComingEntity.UNMASK_BREAK, ComingEntity.UNMASK_BREAK + 6.0F, ComingEntity.UNMASK_DURATION - 20.0F, ComingEntity.UNMASK_DURATION);
                this.head.zRot += Mth.sin(t * 1.7F) * 0.14F * strain;
                this.head.xRot += Mth.sin(t * 1.1F) * 0.1F * strain - 0.7F * thrown;
                this.neck3.xRot += 0.25F * strain - 0.35F * thrown;
                this.neck1.xRot -= 0.25F * thrown;
                // its own hands at the mask, and flung wide when it goes
                Anim.pose(this.armRight, strain, -0.35F, 0.0F, 0.55F);
                Anim.pose(this.armLeft, strain, -0.35F, 0.0F, -0.55F);
                Anim.pose(this.foreRight, strain, 0.5F, 0.0F, -0.75F);
                Anim.pose(this.foreLeft, strain, 0.5F, 0.0F, 0.75F);
                this.armRight.zRot -= 0.5F * thrown;
                this.armLeft.zRot += 0.5F * thrown;
            }
            case ComingEntity.PULL -> {
                float gape = Anim.pulse(t, 6.0F, ComingEntity.PULL_FROM, ComingEntity.PULL_BITE - 3.0F, ComingEntity.PULL_BITE + 1.0F);
                float bite = Anim.pulse(t, ComingEntity.PULL_BITE - 6.0F, ComingEntity.PULL_BITE, ComingEntity.PULL_BITE + 12.0F, ComingEntity.PULL_DURATION);
                Anim.pose(this.mawRight, gape, 0.0F, 1.35F, 0.0F);
                Anim.pose(this.mawLeft, gape, 0.0F, -1.35F, 0.0F);
                this.neck2.xRot += 0.15F * gape + 0.3F * bite;
                this.neck3.xRot += 0.2F * gape + 0.2F * bite;
                this.head.xRot -= 0.7F * gape;
                this.neck1.xRot += 0.5F * bite;
                this.neck1.zRot += Mth.sin(t * 0.9F) * 0.03F * gape;
            }
            case ComingEntity.RAIN -> {
                float up = Anim.pulse(t, 4.0F, ComingEntity.RAIN_FROM, ComingEntity.RAIN_UNTIL, ComingEntity.RAIN_DURATION);
                Anim.pose(this.armRight, up, -0.1F, 0.0F, 0.2F);
                Anim.pose(this.armLeft, up, -0.1F, 0.0F, -0.2F);
                Anim.pose(this.foreRight, up, 0.9F, 0.0F, -0.3F);
                Anim.pose(this.foreLeft, up, 0.9F, 0.0F, 0.3F);
                this.neck1.xRot -= 0.25F * up;
                this.head.xRot -= 0.9F * up;
            }
            default -> {
            }
        }
    }
}
