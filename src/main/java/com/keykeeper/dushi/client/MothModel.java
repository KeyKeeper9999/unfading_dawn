package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.SleepMothEntity;

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
 * The sleep-moth. The mesh between the markers is written by tools/gen_moth.py (which also paints the textures); the
 * animation is written by hand. Sitting (phase 1) it lies on the ground with its wings spread, and they open and close
 * a little, slowly; flying, they beat; shedding its dust, it rears in the air and they shiver.
 */
public class MothModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("sleep_moth"), "main");
    /** Must match SCALE in tools/gen_moth.py. */
    private static final float MODEL_SCALE = 1.5F;

    private final ModelPart all;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart feelerRight;
    private final ModelPart feelerLeft;
    private final ModelPart foreRight;
    private final ModelPart foreLeft;
    private final ModelPart hindRight;
    private final ModelPart hindLeft;

    public MothModel(ModelPart root) {
        super(root);
        this.all = root.getChild("all");
        this.body = this.all.getChild("body");
        this.head = this.body.getChild("head");
        this.feelerRight = this.head.getChild("feeler_r");
        this.feelerLeft = this.head.getChild("feeler_l");
        this.foreRight = this.body.getChild("fore_r");
        this.foreLeft = this.body.getChild("fore_l");
        this.hindRight = this.body.getChild("hind_r");
        this.hindLeft = this.body.getChild("hind_l");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(38, 8).addBox(-1.5F, -1.5F, -2.0F, 3.0F, 3.0F, 4.0F)
                .texOffs(38, 0).addBox(-1.0F, -1.0F, 2.0F, 2.0F, 2.0F, 6.0F),
                PartPose.ZERO);
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(52, 12).addBox(-1.5F, -1.5F, -2.0F, 3.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, -0.5F, -2.0F));
        PartDefinition feeler_r = head.addOrReplaceChild("feeler_r", CubeListBuilder.create()
                .texOffs(54, 0).addBox(-1.0F, -5.0F, 0.0F, 2.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(-1.0F, -1.5F, -1.5F, -0.5F, 0.0F, -0.5F));
        PartDefinition feeler_l = head.addOrReplaceChild("feeler_l", CubeListBuilder.create()
                .texOffs(54, 6).addBox(-1.0F, -5.0F, 0.0F, 2.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(1.0F, -1.5F, -1.5F, -0.5F, 0.0F, 0.5F));
        PartDefinition fore_r = body.addOrReplaceChild("fore_r", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-11.0F, 0.0F, -4.0F, 11.0F, 1.0F, 8.0F),
                PartPose.offset(-1.5F, -1.0F, -1.0F));
        PartDefinition hind_r = body.addOrReplaceChild("hind_r", CubeListBuilder.create()
                .texOffs(0, 18).addBox(-7.0F, 0.0F, -1.0F, 7.0F, 1.0F, 7.0F),
                PartPose.offsetAndRotation(-1.5F, -1.0F, 2.0F, 0.0F, 0.25F, 0.0F));
        PartDefinition fore_l = body.addOrReplaceChild("fore_l", CubeListBuilder.create()
                .texOffs(0, 9).addBox(0.0F, 0.0F, -4.0F, 11.0F, 1.0F, 8.0F),
                PartPose.offset(1.5F, -1.0F, -1.0F));
        PartDefinition hind_l = body.addOrReplaceChild("hind_l", CubeListBuilder.create()
                .texOffs(28, 18).addBox(0.0F, 0.0F, -1.0F, 7.0F, 1.0F, 7.0F),
                PartPose.offsetAndRotation(1.5F, -1.0F, 2.0F, 0.0F, -0.25F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 64).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        this.feelerRight.xRot += Mth.sin(age * 0.21F) * 0.15F;
        this.feelerLeft.xRot += Mth.sin(age * 0.19F + 1.0F) * 0.15F;
        float beat;
        if (state.phase < 2) {
            // sitting: down on the ground, the wings flat and breathing
            this.all.y += 6.4F;
            beat = -0.05F + Mth.sin(age * 0.06F) * 0.07F;
        } else if (state.attackId == SleepMothEntity.SHED) {
            // it stands up in the air over whoever it is after, and shakes
            float t = state.attackTime;
            float rear = Anim.pulse(t, 0.0F, 8.0F, SleepMothEntity.SHED_UNTIL, SleepMothEntity.SHED_UNTIL + 8.0F);
            this.body.xRot -= 0.7F * rear;
            beat = Mth.sin(age * 2.4F) * (0.5F - 0.25F * rear) + 0.3F * rear;
        } else {
            beat = Mth.sin(age * 1.25F) * 0.7F + 0.1F;
            this.all.y += Mth.cos(age * 1.25F) * 0.8F;
            this.body.xRot -= 0.15F;
        }
        this.foreRight.zRot += beat;
        this.foreLeft.zRot -= beat;
        this.hindRight.zRot += beat * 0.8F;
        this.hindLeft.zRot -= beat * 0.8F;
    }
}
