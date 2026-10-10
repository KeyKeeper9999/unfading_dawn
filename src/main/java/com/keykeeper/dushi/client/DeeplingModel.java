package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.DeeplingEntity;

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
 * The deepling. The mesh between the markers is written by tools/gen_dusk.py (which also paints the texture and draws
 * the poses below on a sheet); the animation is written by hand. It hangs in the air as a fish hangs in still water:
 * the fins fan, the tail goes over a little, the light on its stalk nods. Waiting (phase 1) its mouth is shut.
 */
public class DeeplingModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("deepling"), "main");
    /** Must match the scale in the generator. */
    private static final float MODEL_SCALE = 1.5F;

    private final ModelPart all;
    private final ModelPart body;
    private final ModelPart jaw;
    private final ModelPart stalk;
    private final ModelPart stalk2;
    private final ModelPart lure;
    private final ModelPart finRight;
    private final ModelPart finLeft;
    private final ModelPart tail;
    private final ModelPart tail2;
    private final ModelPart fluke;

    public DeeplingModel(ModelPart root) {
        super(root);
        this.all = root.getChild("all");
        this.body = this.all.getChild("body");
        this.jaw = this.body.getChild("jaw");
        this.stalk = this.body.getChild("stalk");
        this.stalk2 = this.stalk.getChild("stalk2");
        this.lure = this.stalk2.getChild("lure");
        this.finRight = this.body.getChild("fin_r");
        this.finLeft = this.body.getChild("fin_l");
        this.tail = this.body.getChild("tail");
        this.tail2 = this.tail.getChild("tail2");
        this.fluke = this.tail2.getChild("fluke");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 11.0F)
                .texOffs(82, 0).addBox(-4.0F, -7.0F, -4.0F, 8.0F, 2.0F, 8.0F)
                .texOffs(100, 10).addBox(-5.0F, -5.0F, -9.0F, 10.0F, 5.0F, 4.0F)
                .texOffs(0, 21).addBox(-4.0F, -1.5F, -8.5F, 8.0F, 5.0F, 1.0F),
                PartPose.offset(0.0F, -8.0F, 0.0F));
        PartDefinition fangs = body.addOrReplaceChild("fangs", CubeListBuilder.create()
                .texOffs(34, 21).addBox(-5.0F, 0.0F, 0.0F, 10.0F, 4.0F, 1.0F)
                .texOffs(18, 21).addBox(-5.0F, 0.0F, 1.0F, 1.0F, 3.0F, 3.0F)
                .texOffs(26, 21).addBox(4.0F, 0.0F, 1.0F, 1.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 0.0F, -9.0F));
        PartDefinition jaw = body.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(58, 20).addBox(-5.0F, 0.0F, -5.0F, 10.0F, 3.0F, 6.0F)
                .texOffs(34, 26).addBox(-5.0F, -4.0F, -5.0F, 10.0F, 4.0F, 1.0F)
                .texOffs(118, 0).addBox(-5.0F, -3.0F, -4.0F, 1.0F, 3.0F, 4.0F)
                .texOffs(116, 19).addBox(4.0F, -3.0F, -4.0F, 1.0F, 3.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 2.0F, -5.0F, 0.25F, 0.0F, 0.0F));
        PartDefinition stalk = body.addOrReplaceChild("stalk", CubeListBuilder.create()
                .texOffs(114, 0).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -7.0F, -2.0F, -0.7F, 0.0F, 0.0F));
        PartDefinition stalk2 = stalk.addOrReplaceChild("stalk2", CubeListBuilder.create()
                .texOffs(90, 20).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, -1.1F, 0.0F, 0.0F));
        PartDefinition lure = stalk2.addOrReplaceChild("lure", CubeListBuilder.create()
                .texOffs(74, 12).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F)
                .texOffs(116, 26).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, -1.2F, 0.0F, 0.0F));
        PartDefinition fin_r = body.addOrReplaceChild("fin_r", CubeListBuilder.create()
                .texOffs(58, 12).addBox(-7.0F, -3.0F, 0.0F, 7.0F, 7.0F, 1.0F),
                PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, 0.0F, 0.5F, 0.3F));
        PartDefinition fin_l = body.addOrReplaceChild("fin_l", CubeListBuilder.create()
                .texOffs(100, 19).addBox(0.0F, -3.0F, 0.0F, 7.0F, 7.0F, 1.0F),
                PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, 0.0F, -0.5F, -0.3F));
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(58, 0).addBox(-3.5F, -3.5F, 0.0F, 7.0F, 7.0F, 5.0F),
                PartPose.offset(0.0F, 0.0F, 6.0F));
        PartDefinition tail2 = tail.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(82, 10).addBox(-2.0F, -2.5F, 0.0F, 4.0F, 5.0F, 5.0F),
                PartPose.offset(0.0F, 0.0F, 5.0F));
        PartDefinition fluke = tail2.addOrReplaceChild("fluke", CubeListBuilder.create()
                .texOffs(42, 0).addBox(-0.5F, -7.0F, 0.0F, 1.0F, 14.0F, 7.0F),
                PartPose.offset(0.0F, 0.0F, 5.0F));
        PartDefinition barbel_r = jaw.addOrReplaceChild("barbel_r", CubeListBuilder.create()
                .texOffs(94, 20).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(-3.0F, 3.0F, -1.0F, 0.7F, 0.0F, 0.15F));
        PartDefinition barbel_l = jaw.addOrReplaceChild("barbel_l", CubeListBuilder.create()
                .texOffs(94, 25).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(3.0F, 3.0F, -1.0F, 0.7F, 0.0F, -0.15F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 128, 64).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float t = state.attackTime;
        boolean hunting = state.phase >= 2;
        float swim = hunting ? 1.0F : 0.35F;
        this.body.y += Mth.sin(age * 0.07F) * 0.6F;
        this.finRight.yRot += Mth.sin(age * 0.14F) * 0.3F * swim + 0.1F;
        this.finLeft.yRot -= Mth.sin(age * 0.14F) * 0.3F * swim + 0.1F;
        float beat = Mth.sin(age * (hunting ? 0.22F : 0.08F));
        this.tail.yRot += beat * 0.3F * swim;
        this.tail2.yRot += beat * 0.35F * swim;
        this.fluke.yRot += beat * 0.3F * swim;
        this.stalk.xRot += Mth.sin(age * 0.05F) * 0.06F;
        this.lure.xRot += Mth.sin(age * 0.09F) * 0.15F;
        // waiting, the mouth is shut; hunting, it hangs a little open, and works
        this.jaw.xRot += hunting ? 0.1F + Mth.sin(age * 0.1F) * 0.06F : -0.25F;
        switch (state.attackId) {
            case DeeplingEntity.GULP -> {
                float start = DeeplingEntity.GULP_START;
                float impact = DeeplingEntity.GULP_IMPACT;
                float gape = Anim.pulse(t, 0.0F, start - 2.0F, impact - 1.0F, impact + 1.0F);
                float shut = Anim.pulse(t, impact - 1.0F, impact + 1.0F, impact + 10.0F, DeeplingEntity.GULP_DURATION);
                Anim.pose(this.jaw, gape, 0.75F, 0.0F, 0.0F);
                Anim.pose(this.jaw, shut, -0.2F, 0.0F, 0.0F);
                this.body.xRot += -0.3F * gape + 0.15F * shut;
                this.stalk.xRot -= 0.5F * gape;
            }
            case DeeplingEntity.GLARE -> {
                float wide = Anim.pulse(t, 4.0F, DeeplingEntity.GLARE_FROM, DeeplingEntity.GLARE_UNTIL, DeeplingEntity.GLARE_DURATION);
                Anim.pose(this.jaw, wide, 1.0F, 0.0F, 0.0F);
                this.body.xRot -= 0.15F * wide;
                this.finRight.yRot -= 0.6F * wide;
                this.finLeft.yRot += 0.6F * wide;
                this.body.zRot += Mth.sin(t * 1.3F) * 0.03F * wide;
            }
            default -> {
            }
        }
    }
}
