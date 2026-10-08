package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.MillstoneEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * The runner stone of the great mill, on its beam. The mesh between the markers is written by tools/gen_stook.py. The
 * stone rolls without slipping: it is turned by how far round the post it has gone, which the entity keeps where a
 * mob keeps the clock of its moves, in thousandths of a radian.
 */
public class MillstoneModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("millstone"), "main");
    /** The stone's radius in model units. */
    private static final float WHEEL = 11.0F;

    private final ModelPart wheel;

    public MillstoneModel(ModelPart root) {
        super(root);
        this.wheel = root.getChild("all").getChild("wheel");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition beam = all.addOrReplaceChild("beam", CubeListBuilder.create()
                .texOffs(50, 34).addBox(-96.0F, -1.5F, -1.5F, 100.0F, 3.0F, 3.0F)
                .texOffs(168, 0).addBox(4.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F),
                PartPose.offset(0.0F, -11.0F, 0.0F));
        PartDefinition wheel = all.addOrReplaceChild("wheel", CubeListBuilder.create()
                .texOffs(106, 0).addBox(-3.0F, -11.0F, -6.0F, 6.0F, 22.0F, 12.0F)
                .texOffs(50, 0).addBox(-3.0F, -6.0F, -11.0F, 6.0F, 12.0F, 22.0F)
                .texOffs(0, 0).addBox(-3.2F, -9.0F, -9.0F, 7.0F, 18.0F, 18.0F)
                .texOffs(142, 0).addBox(-4.0F, -2.5F, -2.5F, 8.0F, 5.0F, 5.0F),
                PartPose.offset(0.0F, -11.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 256, 64);
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float gone = state.attackTime / 1000.0F;
        this.wheel.xRot = (float) ((gone * MillstoneEntity.RADIUS * 16.0 / WHEEL) % (Math.PI * 2.0));
    }
}
