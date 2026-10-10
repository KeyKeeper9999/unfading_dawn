package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.GearEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/**
 * The works of the stopped clock: a wheel standing on its edge, and a weight on its chain. The mesh between the
 * markers is written by tools/gen_clock.py (which also paints the texture); which of the two is shown, and how far
 * the wheel has turned, is said by the state.
 */
public class GearModel extends EntityModel<GearRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("gear"), "main");
    /** Must match the scale in the generator. */
    private static final float MODEL_SCALE = 2.0F;

    private ModelPart cog;
    private ModelPart wheel;
    private ModelPart weight;

    public GearModel(ModelPart root) {
        super(root);
        this.parts(root);
    }

    /** The same mesh, to be drawn as what shines of it (the second argument only tells the two apart). */
    public GearModel(ModelPart root, Glow glow) {
        super(root, RenderTypes::eyes);
        this.parts(root);
    }

    /** Asks for the model that draws what shines. */
    public enum Glow {
        SHINING
    }

    private void parts(ModelPart root) {
        this.cog = root.getChild("cog");
        this.wheel = this.cog.getChild("wheel");
        this.weight = root.getChild("weight");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition cog = root.addOrReplaceChild("cog", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition wheel = cog.addOrReplaceChild("wheel", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.0F, -13.0F, -13.0F, 1.0F, 26.0F, 26.0F)
                .texOffs(54, 0).addBox(1.0F, -13.0F, -13.0F, 1.0F, 26.0F, 26.0F)
                .texOffs(78, 52).addBox(-1.5F, -9.5F, -4.0F, 3.0F, 19.0F, 8.0F)
                .texOffs(34, 52).addBox(-1.5F, -4.0F, -9.5F, 3.0F, 8.0F, 19.0F)
                .texOffs(0, 52).addBox(-1.5F, -7.0F, -7.0F, 3.0F, 14.0F, 14.0F)
                .texOffs(100, 54).addBox(-3.0F, -2.5F, -2.5F, 6.0F, 5.0F, 5.0F),
                PartPose.offset(0.0F, -13.0F, 0.0F));
        PartDefinition weight = root.addOrReplaceChild("weight", CubeListBuilder.create()
                .texOffs(34, 79).addBox(-4.0F, -14.0F, -4.0F, 8.0F, 14.0F, 8.0F)
                .texOffs(100, 64).addBox(-2.5F, -17.0F, -2.5F, 5.0F, 3.0F, 5.0F)
                .texOffs(116, 36).addBox(-1.5F, -19.0F, -0.5F, 3.0F, 2.0F, 1.0F)
                .texOffs(108, 0).addBox(-1.0F, -35.0F, -1.0F, 2.0F, 16.0F, 2.0F)
                .texOffs(116, 0).addBox(-1.0F, -51.0F, -1.0F, 2.0F, 16.0F, 2.0F)
                .texOffs(108, 18).addBox(-1.0F, -67.0F, -1.0F, 2.0F, 16.0F, 2.0F)
                .texOffs(116, 18).addBox(-1.0F, -83.0F, -1.0F, 2.0F, 16.0F, 2.0F)
                .texOffs(108, 36).addBox(-1.0F, -99.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 128, 128).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    @Override
    public void setupAnim(GearRenderState state) {
        super.setupAnim(state);
        boolean hangs = state.kind == GearEntity.WEIGHT;
        this.cog.visible = !hangs;
        this.weight.visible = hangs;
        this.wheel.xRot += state.spin;
        if (hangs) {
            // a weight on a chain swings a little while it comes, and not at all once it stands
            this.weight.zRot += (float) Math.sin(state.age * 0.3F) * 0.05F * Math.min(1.0F, state.hang);
        }
    }
}
