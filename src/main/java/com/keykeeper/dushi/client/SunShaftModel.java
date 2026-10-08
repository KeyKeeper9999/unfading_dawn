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
import net.minecraft.client.renderer.rendertype.RenderTypes;

/**
 * A shaft of light a block across and a block high, its sides fading to nothing at their edges (tools/gen_beam.py):
 * SunShaftRenderer stretches it to its width and height. Drawn shining and see-through, as the game draws eyes in the dark.
 */
public class SunShaftModel extends EntityModel<SunShaftRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("sun_shaft"), "main");

    public SunShaftModel(ModelPart root) {
        super(root, RenderTypes::eyes);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition shaft = root.addOrReplaceChild("shaft", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F)
                .texOffs(0, 32).addBox(-3.0F, -16.0F, -3.0F, 6.0F, 16.0F, 6.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 64);
    }
}
