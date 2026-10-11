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
import net.minecraft.util.Mth;

/**
 * An eye in the rock (WallEyeEntity): two kinds on one sheet, each two plates - the white of it between its lids, and
 * before that the ring and the slit, which go to whoever it looks at. The mesh between the markers is written by
 * tools/gen_coming.py (which also paints it). Its middle is where the entity is; it opens sideways, standing on end.
 */
public class WallEyeModel extends EntityModel<WallEyeRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("wall_eye"), "main");
    private ModelPart great;
    private ModelPart greatIris;
    private ModelPart lesser;
    private ModelPart lesserIris;

    public WallEyeModel(ModelPart root) {
        super(root);
        this.parts(root);
    }

    /** The same mesh, to be drawn as what shines of it (the second argument only tells the two apart). */
    public WallEyeModel(ModelPart root, Glow glow) {
        super(root, RenderTypes::eyes);
        this.parts(root);
    }

    /** Asks for the model that draws what shines. */
    public enum Glow {
        SHINING
    }

    private void parts(ModelPart root) {
        this.great = root.getChild("great");
        this.greatIris = this.great.getChild("great_iris");
        this.lesser = root.getChild("lesser");
        this.lesserIris = this.lesser.getChild("lesser_iris");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition great = root.addOrReplaceChild("great", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-19.0F, -34.0F, -0.5F, 38.0F, 68.0F, 1.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition great_iris = great.addOrReplaceChild("great_iris", CubeListBuilder.create()
                .texOffs(78, 0).addBox(-13.5F, -23.0F, -0.5F, 27.0F, 46.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, -0.6F));
        PartDefinition lesser = root.addOrReplaceChild("lesser", CubeListBuilder.create()
                .texOffs(134, 0).addBox(-7.0F, -12.0F, -0.5F, 14.0F, 24.0F, 1.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition lesser_iris = lesser.addOrReplaceChild("lesser_iris", CubeListBuilder.create()
                .texOffs(164, 0).addBox(-6.0F, -9.5F, -0.5F, 12.0F, 19.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, -0.6F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 256, 128);
    }

    @Override
    public void setupAnim(WallEyeRenderState state) {
        super.setupAnim(state);
        this.great.visible = state.great;
        this.lesser.visible = !state.great;
        ModelPart eye = state.great ? this.great : this.lesser;
        ModelPart iris = state.great ? this.greatIris : this.lesserIris;
        float age = state.ageInTicks;
        // it opens sideways; open, it narrows and widens a little, slowly, and widens when it looks
        eye.xScale = state.lid * (1.0F + Mth.sin(age * 0.045F + state.roll) * 0.05F + 0.22F * state.flare);
        eye.visible = eye.visible && state.lid > 0.02F;
        eye.zRot += (float) Math.toRadians(state.roll);
        // the ring goes to whoever it looks at, as far as the lids let it; and is never quite still
        float reachX = state.great ? 4.0F : 1.4F;
        float reachY = state.great ? 8.0F : 2.6F;
        iris.x += -state.lookX * reachX + Mth.sin(age * 0.31F + state.roll) * 0.12F;
        iris.y += -state.lookY * reachY + Mth.cos(age * 0.23F) * 0.12F;
        float wide = 1.0F - 0.25F * state.flare;
        iris.xScale = wide;
    }
}
