package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.WispEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * The wisp. The mesh between the markers is written by tools/gen_wisp.py (which also paints the textures); the animation
 * is written by hand.
 */
public class WispModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("wisp"), "main");
    private static final float DEG = (float) (Math.PI / 180.0);

    private final ModelPart all;
    private final ModelPart skull;
    private final ModelPart jaw;
    private final ModelPart flame;
    private final ModelPart halo;
    private final ModelPart ragA;
    private final ModelPart ragB;
    private final ModelPart ragC;

    public WispModel(ModelPart root) {
        super(root);
        this.all = root.getChild("all");
        this.skull = this.all.getChild("skull");
        this.jaw = this.skull.getChild("jaw");
        this.flame = this.all.getChild("flame");
        this.halo = this.all.getChild("halo");
        this.ragA = this.all.getChild("rag_a");
        this.ragB = this.all.getChild("rag_b");
        this.ragC = this.all.getChild("rag_c");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 14.0F, 0.0F));
        PartDefinition skull = all.addOrReplaceChild("skull", CubeListBuilder.create()
                .texOffs(32, 0).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F),
                PartPose.ZERO);
        PartDefinition jaw = skull.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.5F, 4.0F, 2.0F, 4.0F),
                PartPose.offset(0.0F, 2.0F, 0.5F));
        PartDefinition flame = all.addOrReplaceChild("flame", CubeListBuilder.create()
                .texOffs(52, 0).addBox(-1.5F, -5.0F, -1.5F, 3.0F, 5.0F, 3.0F)
                .texOffs(60, 8).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offset(0.0F, -2.5F, 0.0F));
        PartDefinition halo = all.addOrReplaceChild("halo", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.ZERO);
        PartDefinition rag_a = all.addOrReplaceChild("rag_a", CubeListBuilder.create()
                .texOffs(52, 8).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 7.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, 1.5F, 0.3F, 0.0F, 0.0F));
        PartDefinition rag_b = all.addOrReplaceChild("rag_b", CubeListBuilder.create()
                .texOffs(38, 10).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(-1.5F, 3.0F, -0.5F, 0.2F, 0.0F, 0.35F));
        PartDefinition rag_c = all.addOrReplaceChild("rag_c", CubeListBuilder.create()
                .texOffs(32, 10).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(1.5F, 3.0F, -0.5F, 0.2F, 0.0F, -0.35F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        // It hangs and turns a little; the flame leans and rights itself, the rags trail and stir, the shell of sparks
        // goes round the other way.
        this.all.y += Mth.sin(age * 0.13F) * 1.2F;
        this.skull.yRot += Mth.clamp(state.yRot, -80.0F, 80.0F) * DEG;
        this.skull.xRot += Mth.clamp(state.xRot, -50.0F, 50.0F) * DEG;
        this.skull.zRot += Mth.sin(age * 0.07F) * 0.12F;
        this.flame.zRot += Mth.sin(age * 0.31F) * 0.14F;
        this.flame.xRot += Mth.sin(age * 0.23F + 1.0F) * 0.12F;
        this.halo.yRot -= age * 0.03F;
        this.halo.xRot += age * 0.017F;
        this.ragA.xRot += Mth.sin(age * 0.17F) * 0.3F;
        this.ragB.xRot += Mth.sin(age * 0.19F + 1.3F) * 0.3F;
        this.ragC.xRot += Mth.sin(age * 0.15F + 2.6F) * 0.3F;
        if (state.attackId == WispEntity.BOLT) {
            // The flame stands up to three times its height, the jaw drops, the rags fly back - and it spits.
            float t = state.attackTime;
            float flare = Anim.pulse(t, 0.0F, 14.0F, 21.0F, 24.0F);
            float spit = Anim.pulse(t, 21.0F, 23.0F, 28.0F, 40.0F);
            this.flame.yScale = 1.0F + 2.0F * flare;
            this.flame.xScale = 1.0F + 0.3F * flare;
            this.flame.zScale = 1.0F + 0.3F * flare;
            this.halo.xScale = this.halo.yScale = this.halo.zScale = 1.0F + 0.5F * flare;
            this.jaw.xRot += 0.7F * Math.max(flare, spit);
            this.skull.xRot -= 0.25F * flare - 0.2F * spit;
            this.ragA.xRot += 0.8F * spit;
            this.ragB.xRot += 0.7F * spit;
            this.ragC.xRot += 0.7F * spit;
        }
    }
}
