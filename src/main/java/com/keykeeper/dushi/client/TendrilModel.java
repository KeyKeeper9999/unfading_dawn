package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.TendrilEntity;

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
import net.minecraft.util.Mth;

/**
 * What the Coming reaches into its hall with: a hole in the floor and an arm of four lengths out of it. The mesh
 * between the markers is written by tools/gen_coming.py (which also paints the texture); the animation is written by
 * hand, and goes by the entity's own clock (TendrilEntity): the hole opens and only fingers show; the arm comes up;
 * it stands and leans back; it comes down along the floor, the way the model faces; it lies, gets up, and goes back
 * down, and the hole shuts after it.
 */
public class TendrilModel extends EntityModel<TendrilRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("tendril"), "main");
    /** Must match the scale in the generator. */
    private static final float MODEL_SCALE = 2.0F;
    /** How far it stands over the floor, in model units (TENDRIL_HEIGHT in the generator), and how much of it shows in the hole before it comes. */
    private static final float HEIGHT = 57.0F;
    private static final float POKE = 9.0F;

    private ModelPart portal;
    private ModelPart seg0;
    private ModelPart seg1;
    private ModelPart seg2;
    private ModelPart seg3;
    private ModelPart hand;

    public TendrilModel(ModelPart root) {
        super(root);
        this.parts(root);
    }

    /** The same mesh, to be drawn as what shines of it (the second argument only tells the two apart). */
    public TendrilModel(ModelPart root, Glow glow) {
        super(root, RenderTypes::eyes);
        this.parts(root);
    }

    /** Asks for the model that draws what shines. */
    public enum Glow {
        SHINING
    }

    private void parts(ModelPart root) {
        this.portal = root.getChild("portal");
        this.seg0 = root.getChild("seg0");
        this.seg1 = this.seg0.getChild("seg1");
        this.seg2 = this.seg1.getChild("seg2");
        this.seg3 = this.seg2.getChild("seg3");
        this.hand = this.seg3.getChild("hand");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition portal = root.addOrReplaceChild("portal", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-13.0F, -0.5F, -13.0F, 26.0F, 1.0F, 26.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition seg0 = root.addOrReplaceChild("seg0", CubeListBuilder.create()
                .texOffs(104, 0).addBox(-2.5F, -12.0F, -2.5F, 5.0F, 12.0F, 5.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition seg1 = seg0.addOrReplaceChild("seg1", CubeListBuilder.create()
                .texOffs(104, 17).addBox(-2.0F, -12.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(0.0F, -12.0F, 0.0F));
        PartDefinition seg2 = seg1.addOrReplaceChild("seg2", CubeListBuilder.create()
                .texOffs(0, 27).addBox(-1.5F, -11.0F, -1.5F, 3.0F, 11.0F, 3.0F),
                PartPose.offset(0.0F, -12.0F, 0.0F));
        PartDefinition seg3 = seg2.addOrReplaceChild("seg3", CubeListBuilder.create()
                .texOffs(120, 17).addBox(-1.0F, -10.0F, -1.0F, 2.0F, 10.0F, 2.0F),
                PartPose.offset(0.0F, -11.0F, 0.0F));
        PartDefinition hand = seg3.addOrReplaceChild("hand", CubeListBuilder.create()
                .texOffs(16, 27).addBox(-3.0F, -4.0F, -1.0F, 6.0F, 4.0F, 2.0F)
                .texOffs(124, 9).addBox(-3.0F, -10.0F, -0.5F, 1.0F, 6.0F, 1.0F)
                .texOffs(124, 0).addBox(-1.0F, -12.0F, -0.5F, 1.0F, 8.0F, 1.0F)
                .texOffs(12, 27).addBox(1.0F, -12.0F, -0.5F, 1.0F, 8.0F, 1.0F)
                .texOffs(32, 27).addBox(3.0F, -9.0F, -0.5F, 1.0F, 5.0F, 1.0F)
                .texOffs(36, 27).addBox(-5.0F, -4.0F, -0.5F, 2.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, -10.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 128, 64).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    @Override
    public void setupAnim(TendrilRenderState state) {
        super.setupAnim(state);
        float age = state.age;
        float t = state.time;
        float up = TendrilEntity.RISE;
        float stood = up + state.stands;
        float fallen = stood + TendrilEntity.FALL;
        float lain = fallen + TendrilEntity.LIE;
        float back = lain + TendrilEntity.BACK;
        float gone = back + TendrilEntity.SINK;
        // the hole: it opens as soon as there is anything, and shuts last
        float open = Anim.ramp(age, 0.0F, 8.0F) * (1.0F - Anim.ramp(t, gone - 3.0F, gone + 1.0F));
        this.portal.xScale = open;
        this.portal.zScale = open;
        this.portal.yRot += age * 0.02F;
        this.portal.visible = open > 0.02F;
        // how much of the arm is out of it
        float poke = t < 0.0F ? Anim.ramp(age, 3.0F, 10.0F) * (POKE + Mth.sin(age * 1.3F) * 1.5F) : 0.0F;
        float out = Anim.ramp(t, 0.0F, up) * (1.0F - Anim.ramp(t, back, gone));
        float shown = Math.max(poke, out * HEIGHT);
        this.seg0.y += HEIGHT - shown;
        this.seg0.visible = shown > 0.05F;
        // standing it sways, and the fingers work; then it leans back, and comes down; and gets up again
        float sway = out * (1.0F - Anim.ramp(t, stood, fallen)) + Anim.ramp(t, lain, back);
        this.seg1.zRot += Mth.sin(age * 0.23F) * 0.1F * sway;
        this.seg2.xRot += Mth.sin(age * 0.29F + 1.0F) * 0.14F * sway;
        this.seg3.zRot += Mth.cos(age * 0.31F) * 0.16F * sway;
        this.hand.xRot += Mth.sin(age * 0.4F) * 0.25F * sway;
        float lean = Anim.pulse(t, stood - 9.0F, stood - 2.0F, stood, stood + 2.0F);
        float down = Anim.ramp(t, stood, fallen) * (1.0F - Anim.ramp(t, lain, back));
        this.seg0.xRot += -0.3F * lean + 1.38F * down;
        this.seg1.xRot += -0.15F * lean + 0.14F * down;
        this.seg2.xRot += 0.1F * lean + 0.05F * down;
        this.hand.xRot += 0.5F * lean - 0.25F * down;
    }
}
