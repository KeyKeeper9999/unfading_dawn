package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** The Helmsman's anchor in the air: its middle at the origin (the mesh is tools/gen_anchor.py's; AnchorRenderer turns and scales it). */
public class AnchorModel extends EntityModel<AnchorRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("thrown_anchor"), "main");
    /** Must match SCALE in tools/gen_anchor.py: the anchor is as large as it is in his hand. */
    public static final float MODEL_SCALE = 1.5F;

    public AnchorModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition anchor = root.addOrReplaceChild("anchor", CubeListBuilder.create()
                .texOffs(40, 9).addBox(-1.5F, 10.0F, -0.5F, 3.0F, 3.0F, 1.0F)
                .texOffs(8, 0).addBox(-0.5F, 8.0F, -5.0F, 1.0F, 1.0F, 10.0F)
                .texOffs(0, 0).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 21.0F, 2.0F)
                .texOffs(8, 11).addBox(-7.0F, -13.0F, -1.0F, 14.0F, 2.0F, 2.0F)
                .texOffs(30, 0).addBox(-9.0F, -13.0F, -1.0F, 2.0F, 7.0F, 2.0F)
                .texOffs(38, 0).addBox(7.0F, -13.0F, -1.0F, 2.0F, 7.0F, 2.0F)
                .texOffs(50, 0).addBox(-10.0F, -8.0F, -1.5F, 3.0F, 3.0F, 3.0F)
                .texOffs(50, 6).addBox(7.0F, -8.0F, -1.5F, 3.0F, 3.0F, 3.0F)
                .texOffs(46, 0).addBox(-0.5F, 13.0F, -0.5F, 1.0F, 6.0F, 1.0F),
                PartPose.ZERO);
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 32);
    }
}
