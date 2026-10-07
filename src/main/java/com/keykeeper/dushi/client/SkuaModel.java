package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.SkuaEntity;

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
 * The skua of the Morning Wharf. The mesh between the markers is written by tools/gen_skua.py (which also paints the
 * texture and draws the poses below on a sheet); the animation is written by hand. Perched (phase 1) it sits hunched
 * with its wings folded and turns its head; flying, they beat; stooping, they are swept back and it comes down beak first.
 */
public class SkuaModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("skua"), "main");
    /** Must match SCALE in tools/gen_skua.py. */
    private static final float MODEL_SCALE = 1.4F;

    private final ModelPart all;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart tail;
    private final ModelPart wingRight;
    private final ModelPart wingLeft;
    private final ModelPart tipRight;
    private final ModelPart tipLeft;
    private final ModelPart legs;

    public SkuaModel(ModelPart root) {
        super(root);
        this.all = root.getChild("all");
        this.body = this.all.getChild("body");
        this.head = this.body.getChild("head");
        this.tail = this.body.getChild("tail");
        this.wingRight = this.body.getChild("wing_r");
        this.wingLeft = this.body.getChild("wing_l");
        this.tipRight = this.wingRight.getChild("tip_r");
        this.tipLeft = this.wingLeft.getChild("tip_l");
        this.legs = this.all.getChild("legs");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 19.0F, 0.0F));
        PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.5F, -2.5F, -4.0F, 5.0F, 4.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.25F, 0.0F, 0.0F));
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(48, 14).addBox(-2.0F, -0.5F, 0.0F, 4.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -1.0F, 4.0F, 0.15F, 0.0F, 0.0F));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(52, 0).addBox(-1.5F, -3.0F, -2.5F, 3.0F, 3.0F, 3.0F)
                .texOffs(0, 18).addBox(-0.5F, -1.8F, -5.5F, 1.0F, 1.0F, 3.0F)
                .texOffs(60, 6).addBox(-0.5F, -0.8F, -5.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -1.5F, -4.0F, 0.25F, 0.0F, 0.0F));
        PartDefinition wing_r = body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(26, 0).addBox(-7.0F, -0.5F, -3.0F, 7.0F, 1.0F, 6.0F),
                PartPose.offset(-2.5F, -2.0F, -1.0F));
        PartDefinition tip_r = wing_r.addOrReplaceChild("tip_r", CubeListBuilder.create()
                .texOffs(0, 12).addBox(-7.0F, -0.5F, -2.5F, 7.0F, 1.0F, 5.0F),
                PartPose.offset(-7.0F, 0.0F, 0.0F));
        PartDefinition wing_l = body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(26, 7).addBox(0.0F, -0.5F, -3.0F, 7.0F, 1.0F, 6.0F),
                PartPose.offset(2.5F, -2.0F, -1.0F));
        PartDefinition tip_l = wing_r.addOrReplaceChild("tip_l", CubeListBuilder.create()
                .texOffs(24, 14).addBox(0.0F, -0.5F, -2.5F, 7.0F, 1.0F, 5.0F),
                PartPose.offset(7.0F, 0.0F, 0.0F));
        PartDefinition legs = all.addOrReplaceChild("legs", CubeListBuilder.create()
                .texOffs(52, 6).addBox(-1.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F)
                .texOffs(56, 6).addBox(0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F)
                .texOffs(52, 11).addBox(-2.0F, 3.5F, -2.0F, 2.0F, 1.0F, 2.0F)
                .texOffs(8, 18).addBox(0.0F, 3.5F, -2.0F, 2.0F, 1.0F, 2.0F),
                PartPose.offset(0.0F, 1.0F, 1.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 32).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float t = state.attackTime;
        if (state.phase < 2) {
            // perched: the wings folded along its back, the head going from side to side, looking
            this.wingRight.yRot += 1.35F;
            this.wingRight.zRot -= 0.35F;
            this.tipRight.yRot += 2.7F;
            this.tipRight.zRot += 0.08F;
            this.wingLeft.yRot -= 1.35F;
            this.wingLeft.zRot += 0.35F;
            this.tipLeft.yRot -= 2.7F;
            this.tipLeft.zRot -= 0.08F;
            this.head.xRot += 0.2F;
            this.head.yRot += Mth.sin(age * 0.05F) * 0.7F;
            this.tail.xRot += Mth.sin(age * 0.11F) * 0.05F;
            return;
        }
        // in the air: the legs laid back, the body level
        this.all.y -= 3.0F;
        this.body.xRot += 0.25F;
        float stoop = state.attackId == SkuaEntity.STOOP ? Anim.pulse(t, SkuaEntity.STOOP_FALLS - 4.0F, SkuaEntity.STOOP_FALLS, SkuaEntity.STOOP_RISES, SkuaEntity.STOOP_RISES + 6.0F) : 0.0F;
        float hang = state.attackId == SkuaEntity.STOOP ? Anim.pulse(t, 0.0F, 4.0F, SkuaEntity.STOOP_FALLS - 4.0F, SkuaEntity.STOOP_FALLS) : 0.0F;
        // it hangs a moment before it stoops, beating fast, the head down at what it has chosen
        float beat = Mth.sin(age * (0.9F + 0.9F * hang)) * (0.55F - 0.2F * hang) * (1.0F - stoop) + 0.05F;
        this.wingRight.zRot += beat;
        this.wingLeft.zRot -= beat;
        this.tipRight.zRot += Mth.sin(age * (0.9F + 0.9F * hang) - 0.7F) * 0.3F * (1.0F - stoop);
        this.tipLeft.zRot -= Mth.sin(age * (0.9F + 0.9F * hang) - 0.7F) * 0.3F * (1.0F - stoop);
        this.all.y += Mth.cos(age * 0.9F) * 0.6F * (1.0F - stoop);
        this.legs.xRot += 0.9F - 1.5F * stoop;
        this.head.xRot += 0.5F * hang - 0.4F * stoop;
        // the stoop: wings swept back, the whole of it tipped down along its fall
        this.all.xRot += 0.9F * stoop;
        this.wingRight.yRot += 0.9F * stoop;
        this.wingRight.zRot += 0.15F * stoop;
        this.tipRight.yRot += 0.5F * stoop;
        this.wingLeft.yRot -= 0.9F * stoop;
        this.wingLeft.zRot -= 0.15F * stoop;
        this.tipLeft.yRot -= 0.5F * stoop;
    }
}
