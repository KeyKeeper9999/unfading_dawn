package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.ColossusEntity;

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
 * What stands up out of the sea when the bell is struck: a knot of the night breaking the water, a rope of three necks twisted
 * together out of it, and on the end of the rope a black disc with a rim of amber, a broken ring of light behind it and a crown
 * of twelve arms round that. The mesh between the markers is written by tools/gen_colossus.py (which also paints the texture and
 * has the same animation for its preview: what is changed here is changed there); the animation is written by hand and goes by
 * the entity's age in ticks ({@link ColossusEntity#RISE} is the last tick of the rising):
 * the patch of sea comes up round its foot, the knot comes up out of the water, the arms thrown up come down on the sea, the
 * rope that was coiled on itself straightens ring by ring from the foot, the bowed head lifts, the crown that was folded
 * behind the disc opens, and three times it roars, its head thrown back. Once risen it stands and sways.
 */
public class ColossusModel extends EntityModel<ColossusRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("colossus"), "main");
    /** Must match the scale in the generator. It is also what a length of the generator's is multiplied by to be a length of this model's own. */
    private static final float MODEL_SCALE = 12.0F;
    private static final int RINGS = 8;
    private static final int CROWN = 12;
    /** The side of each brace arm (the sign of its turn): the generator's ARMS. */
    private static final int[] ARM_SIDE = {1, -1, 1, -1};
    /** The roars: begin to throw the head back, at the full, begin to bring it down, the head up again. */
    private static final float[][] ROARS = {{285.0F, 295.0F, 325.0F, 340.0F}, {395.0F, 408.0F, 470.0F, 490.0F}, {495.0F, 505.0F, 530.0F, 545.0F}};

    private ModelPart sea;
    private ModelPart knot;
    private ModelPart head;
    private ModelPart halo;
    private final ModelPart[] arm = new ModelPart[4];
    private final ModelPart[] fore = new ModelPart[4];
    private final ModelPart[] ring = new ModelPart[RINGS];
    private final ModelPart[] crown = new ModelPart[CROWN];
    private final ModelPart[] crownB = new ModelPart[CROWN];
    private final ModelPart[] crownC = new ModelPart[CROWN];

    public ColossusModel(ModelPart root) {
        super(root);
        this.parts(root);
    }

    /** The same mesh, to be drawn as what shines of it (the second argument only tells the two apart). */
    public ColossusModel(ModelPart root, Glow glow) {
        super(root, net.minecraft.client.renderer.rendertype.RenderTypes::eyes);
        this.parts(root);
    }

    /** Asks for the model that draws what shines. */
    public enum Glow {
        SHINING
    }

    private void parts(ModelPart root) {
        this.sea = root.getChild("sea");
        this.knot = root.getChild("knot");
        for (int i = 0; i < 4; i++) {
            this.arm[i] = this.knot.getChild("arm" + i);
            this.fore[i] = this.arm[i].getChild("fore" + i);
        }
        ModelPart above = this.knot;
        for (int i = 0; i < RINGS; i++) {
            this.ring[i] = above.getChild("ring" + i);
            above = this.ring[i];
        }
        this.head = above.getChild("head");
        this.halo = this.head.getChild("disc").getChild("halo");
        for (int j = 0; j < CROWN; j++) {
            this.crown[j] = this.halo.getChild("crown" + j);
            this.crownB[j] = this.crown[j].getChild("crown" + j + "b");
            this.crownC[j] = this.crownB[j].getChild("crown" + j + "c");
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition sea = root.addOrReplaceChild("sea", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-84.0F, -0.5F, -84.0F, 168.0F, 1.0F, 168.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition knot = root.addOrReplaceChild("knot", CubeListBuilder.create()
                .texOffs(0, 169).addBox(-52.0F, -30.0F, -46.0F, 104.0F, 30.0F, 92.0F)
                .texOffs(672, 114).addBox(-40.0F, -52.0F, -36.0F, 80.0F, 22.0F, 72.0F)
                .texOffs(564, 208).addBox(-27.0F, -64.0F, -25.0F, 54.0F, 12.0F, 50.0F)
                .texOffs(564, 169).addBox(-66.0F, -16.0F, -14.0F, 16.0F, 16.0F, 22.0F)
                .texOffs(940, 254).addBox(50.0F, -22.0F, 8.0F, 18.0F, 22.0F, 20.0F)
                .texOffs(392, 255).addBox(-34.0F, -24.0F, 38.0F, 28.0F, 24.0F, 16.0F)
                .texOffs(480, 255).addBox(18.0F, -18.0F, -60.0F, 26.0F, 18.0F, 16.0F)
                .texOffs(940, 208).addBox(-58.0F, -30.0F, -38.0F, 18.0F, 30.0F, 16.0F)
                .texOffs(156, 320).addBox(36.0F, -44.0F, -38.0F, 12.0F, 14.0F, 10.0F)
                .texOffs(976, 200).addBox(-30.0F, -46.0F, -37.0F, 7.0F, 7.0F, 1.0F)
                .texOffs(704, 326).addBox(-14.0F, -41.0F, -37.0F, 5.0F, 5.0F, 1.0F)
                .texOffs(640, 199).addBox(4.0F, -47.0F, -37.0F, 8.0F, 8.0F, 1.0F)
                .texOffs(716, 326).addBox(22.0F, -40.0F, -37.0F, 5.0F, 5.0F, 1.0F)
                .texOffs(620, 328).addBox(-6.0F, -36.0F, -37.0F, 4.0F, 4.0F, 1.0F)
                .texOffs(996, 320).addBox(-28.0F, -61.0F, -12.0F, 1.0F, 6.0F, 6.0F)
                .texOffs(1010, 320).addBox(27.0F, -59.0F, -10.0F, 1.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition arm0 = knot.addOrReplaceChild("arm0", CubeListBuilder.create()
                .texOffs(940, 56).addBox(-7.0F, -40.0F, -7.0F, 14.0F, 40.0F, 14.0F),
                PartPose.offsetAndRotation(44.0F, -40.0F, -22.0F, 0.0F, 0.0F, 1.7F));
        PartDefinition fore0 = arm0.addOrReplaceChild("fore0", CubeListBuilder.create()
                .texOffs(900, 0).addBox(-5.0F, -46.0F, -5.0F, 10.0F, 46.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, -40.0F, 0.0F, 0.0F, 0.0F, 1.05F));
        PartDefinition claw0 = fore0.addOrReplaceChild("claw0", CubeListBuilder.create()
                .texOffs(556, 302).addBox(-6.0F, -8.0F, -3.0F, 12.0F, 8.0F, 6.0F)
                .texOffs(876, 316).addBox(-6.0F, -22.0F, -2.0F, 2.0F, 14.0F, 4.0F)
                .texOffs(660, 169).addBox(-2.0F, -26.0F, -2.0F, 2.0F, 18.0F, 4.0F)
                .texOffs(1008, 206).addBox(2.0F, -26.0F, -2.0F, 2.0F, 18.0F, 4.0F)
                .texOffs(660, 191).addBox(5.0F, -20.0F, -2.0F, 2.0F, 12.0F, 4.0F),
                PartPose.offset(0.0F, -46.0F, 0.0F));
        PartDefinition arm1 = knot.addOrReplaceChild("arm1", CubeListBuilder.create()
                .texOffs(772, 208).addBox(-7.0F, -40.0F, -7.0F, 14.0F, 40.0F, 14.0F),
                PartPose.offsetAndRotation(-44.0F, -40.0F, -22.0F, 0.0F, 0.0F, -1.7F));
        PartDefinition fore1 = arm1.addOrReplaceChild("fore1", CubeListBuilder.create()
                .texOffs(940, 0).addBox(-5.0F, -46.0F, -5.0F, 10.0F, 46.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, -40.0F, 0.0F, 0.0F, 0.0F, -1.05F));
        PartDefinition claw1 = fore1.addOrReplaceChild("claw1", CubeListBuilder.create()
                .texOffs(480, 320).addBox(-6.0F, -8.0F, -3.0F, 12.0F, 8.0F, 6.0F)
                .texOffs(468, 317).addBox(-6.0F, -22.0F, -2.0F, 2.0F, 14.0F, 4.0F)
                .texOffs(1008, 228).addBox(-2.0F, -26.0F, -2.0F, 2.0F, 18.0F, 4.0F)
                .texOffs(756, 270).addBox(2.0F, -26.0F, -2.0F, 2.0F, 18.0F, 4.0F)
                .texOffs(256, 320).addBox(5.0F, -20.0F, -2.0F, 2.0F, 12.0F, 4.0F),
                PartPose.offset(0.0F, -46.0F, 0.0F));
        PartDefinition arm2 = knot.addOrReplaceChild("arm2", CubeListBuilder.create()
                .texOffs(828, 208).addBox(-7.0F, -40.0F, -7.0F, 14.0F, 40.0F, 14.0F),
                PartPose.offsetAndRotation(44.0F, -40.0F, 22.0F, 0.0F, 0.0F, 1.7F));
        PartDefinition fore2 = arm2.addOrReplaceChild("fore2", CubeListBuilder.create()
                .texOffs(980, 0).addBox(-5.0F, -46.0F, -5.0F, 10.0F, 46.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, -40.0F, 0.0F, 0.0F, 0.0F, 1.05F));
        PartDefinition claw2 = fore2.addOrReplaceChild("claw2", CubeListBuilder.create()
                .texOffs(888, 320).addBox(-6.0F, -8.0F, -3.0F, 12.0F, 8.0F, 6.0F)
                .texOffs(364, 319).addBox(-6.0F, -22.0F, -2.0F, 2.0F, 14.0F, 4.0F)
                .texOffs(876, 294).addBox(-2.0F, -26.0F, -2.0F, 2.0F, 18.0F, 4.0F)
                .texOffs(468, 295).addBox(2.0F, -26.0F, -2.0F, 2.0F, 18.0F, 4.0F)
                .texOffs(268, 320).addBox(5.0F, -20.0F, -2.0F, 2.0F, 12.0F, 4.0F),
                PartPose.offset(0.0F, -46.0F, 0.0F));
        PartDefinition arm3 = knot.addOrReplaceChild("arm3", CubeListBuilder.create()
                .texOffs(884, 208).addBox(-7.0F, -40.0F, -7.0F, 14.0F, 40.0F, 14.0F),
                PartPose.offsetAndRotation(-44.0F, -40.0F, 22.0F, 0.0F, 0.0F, -1.7F));
        PartDefinition fore3 = arm3.addOrReplaceChild("fore3", CubeListBuilder.create()
                .texOffs(900, 56).addBox(-5.0F, -46.0F, -5.0F, 10.0F, 46.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, -40.0F, 0.0F, 0.0F, 0.0F, -1.05F));
        PartDefinition claw3 = fore3.addOrReplaceChild("claw3", CubeListBuilder.create()
                .texOffs(0, 321).addBox(-6.0F, -8.0F, -3.0F, 12.0F, 8.0F, 6.0F)
                .texOffs(376, 319).addBox(-6.0F, -22.0F, -2.0F, 2.0F, 14.0F, 4.0F)
                .texOffs(736, 300).addBox(-2.0F, -26.0F, -2.0F, 2.0F, 18.0F, 4.0F)
                .texOffs(608, 302).addBox(2.0F, -26.0F, -2.0F, 2.0F, 18.0F, 4.0F)
                .texOffs(280, 320).addBox(5.0F, -20.0F, -2.0F, 2.0F, 12.0F, 4.0F),
                PartPose.offset(0.0F, -46.0F, 0.0F));
        PartDefinition ring0 = knot.addOrReplaceChild("ring0", CubeListBuilder.create()
                .texOffs(772, 262).addBox(1.0F, -17.0F, -7.5F, 15.0F, 17.0F, 15.0F)
                .texOffs(832, 262).addBox(-11.7F, -17.0F, -0.1F, 15.0F, 17.0F, 15.0F)
                .texOffs(564, 270).addBox(-11.8F, -17.0F, -14.9F, 15.0F, 17.0F, 15.0F),
                PartPose.offset(0.0F, -64.0F, 0.0F));
        PartDefinition ring1 = ring0.addOrReplaceChild("ring1", CubeListBuilder.create()
                .texOffs(624, 270).addBox(-2.1F, -17.0F, -0.8F, 14.0F, 17.0F, 14.0F)
                .texOffs(680, 270).addBox(-14.9F, -17.0F, -5.8F, 14.0F, 17.0F, 14.0F)
                .texOffs(480, 289).addBox(-4.1F, -17.0F, -14.4F, 14.0F, 17.0F, 14.0F),
                PartPose.offset(0.0F, -17.0F, 0.0F));
        PartDefinition ring2 = ring1.addOrReplaceChild("ring2", CubeListBuilder.create()
                .texOffs(0, 291).addBox(-8.2F, -17.0F, 0.7F, 13.0F, 17.0F, 13.0F)
                .texOffs(52, 291).addBox(-11.9F, -17.0F, -11.6F, 13.0F, 17.0F, 13.0F)
                .texOffs(104, 291).addBox(0.6F, -17.0F, -8.6F, 13.0F, 17.0F, 13.0F),
                PartPose.offset(0.0F, -17.0F, 0.0F));
        PartDefinition ring3 = ring2.addOrReplaceChild("ring3", CubeListBuilder.create()
                .texOffs(156, 291).addBox(-12.2F, -17.0F, -3.1F, 12.0F, 17.0F, 12.0F)
                .texOffs(204, 291).addBox(-5.4F, -17.0F, -12.8F, 12.0F, 17.0F, 12.0F)
                .texOffs(252, 291).addBox(-0.4F, -17.0F, -2.1F, 12.0F, 17.0F, 12.0F),
                PartPose.offset(0.0F, -17.0F, 0.0F));
        PartDefinition ring4 = ring3.addOrReplaceChild("ring4", CubeListBuilder.create()
                .texOffs(300, 291).addBox(-11.1F, -17.0F, -8.3F, 11.0F, 17.0F, 11.0F)
                .texOffs(344, 291).addBox(-0.3F, -17.0F, -9.0F, 11.0F, 17.0F, 11.0F)
                .texOffs(892, 292).addBox(-5.1F, -17.0F, 0.8F, 11.0F, 17.0F, 11.0F),
                PartPose.offset(0.0F, -17.0F, 0.0F));
        PartDefinition ring5 = ring4.addOrReplaceChild("ring5", CubeListBuilder.create()
                .texOffs(956, 296).addBox(-6.2F, -17.0F, -10.6F, 10.0F, 17.0F, 10.0F)
                .texOffs(624, 301).addBox(0.5F, -17.0F, -3.2F, 10.0F, 17.0F, 10.0F)
                .texOffs(664, 301).addBox(-9.3F, -17.0F, -1.1F, 10.0F, 17.0F, 10.0F),
                PartPose.offset(0.0F, -17.0F, 0.0F));
        PartDefinition ring6 = ring5.addOrReplaceChild("ring6", CubeListBuilder.create()
                .texOffs(704, 301).addBox(-0.7F, -17.0F, -8.0F, 8.0F, 17.0F, 8.0F)
                .texOffs(300, 319).addBox(-2.2F, -17.0F, 0.9F, 8.0F, 17.0F, 8.0F)
                .texOffs(332, 319).addBox(-9.1F, -17.0F, -4.8F, 8.0F, 17.0F, 8.0F),
                PartPose.offset(0.0F, -17.0F, 0.0F));
        PartDefinition ring7 = ring6.addOrReplaceChild("ring7", CubeListBuilder.create()
                .texOffs(996, 296).addBox(1.1F, -17.0F, -3.4F, 7.0F, 17.0F, 7.0F)
                .texOffs(200, 320).addBox(-5.9F, -17.0F, 0.5F, 7.0F, 17.0F, 7.0F)
                .texOffs(228, 320).addBox(-5.8F, -17.0F, -7.6F, 7.0F, 17.0F, 7.0F),
                PartPose.offset(0.0F, -17.0F, 0.0F));
        PartDefinition head = ring7.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(536, 317).addBox(-9.0F, -8.0F, -9.0F, 18.0F, 8.0F, 18.0F),
                PartPose.offset(0.0F, -17.0F, 0.0F));
        PartDefinition disc = head.addOrReplaceChild("disc", CubeListBuilder.create()
                .texOffs(392, 169).addBox(-40.0F, -86.0F, -3.0F, 80.0F, 80.0F, 6.0F),
                PartPose.offset(0.0F, -6.0F, -6.0F));
        PartDefinition halo = disc.addOrReplaceChild("halo", CubeListBuilder.create()
                .texOffs(672, 0).addBox(-56.0F, -56.0F, 0.0F, 112.0F, 112.0F, 2.0F),
                PartPose.offset(0.0F, -46.0F, 4.0F));
        PartDefinition crown0 = halo.addOrReplaceChild("crown0", CubeListBuilder.create()
                .texOffs(536, 289).addBox(-3.0F, -24.0F, -2.0F, 6.0F, 24.0F, 4.0F),
                PartPose.offset(0.0F, -54.0F, 0.0F));
        PartDefinition crown0b = crown0.addOrReplaceChild("crown0b", CubeListBuilder.create()
                .texOffs(996, 56).addBox(-3.0F, -26.0F, -2.0F, 6.0F, 26.0F, 4.0F),
                PartPose.offset(0.0F, -24.0F, 0.0F));
        PartDefinition crown0c = crown0b.addOrReplaceChild("crown0c", CubeListBuilder.create()
                .texOffs(36, 321).addBox(-4.0F, -8.0F, -1.5F, 8.0F, 8.0F, 3.0F)
                .texOffs(528, 320).addBox(-4.0F, -17.0F, -1.0F, 1.0F, 9.0F, 2.0F)
                .texOffs(1016, 56).addBox(-1.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(1016, 69).addBox(2.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(456, 323).addBox(4.0F, -15.0F, -1.0F, 1.0F, 7.0F, 2.0F),
                PartPose.offset(0.0F, -26.0F, 0.0F));
        PartDefinition crown1 = halo.addOrReplaceChild("crown1", CubeListBuilder.create()
                .texOffs(756, 294).addBox(-3.0F, -24.0F, -2.0F, 6.0F, 24.0F, 4.0F),
                PartPose.offsetAndRotation(27.0F, -46.765F, 0.0F, 0.0F, 0.0F, 0.5236F));
        PartDefinition crown1b = crown1.addOrReplaceChild("crown1b", CubeListBuilder.create()
                .texOffs(996, 86).addBox(-3.0F, -26.0F, -2.0F, 6.0F, 26.0F, 4.0F),
                PartPose.offset(0.0F, -24.0F, 0.0F));
        PartDefinition crown1c = crown1b.addOrReplaceChild("crown1c", CubeListBuilder.create()
                .texOffs(58, 321).addBox(-4.0F, -8.0F, -1.5F, 8.0F, 8.0F, 3.0F)
                .texOffs(924, 320).addBox(-4.0F, -17.0F, -1.0F, 1.0F, 9.0F, 2.0F)
                .texOffs(1016, 82).addBox(-1.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(1016, 95).addBox(2.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(462, 323).addBox(4.0F, -15.0F, -1.0F, 1.0F, 7.0F, 2.0F),
                PartPose.offset(0.0F, -26.0F, 0.0F));
        PartDefinition crown2 = halo.addOrReplaceChild("crown2", CubeListBuilder.create()
                .texOffs(776, 294).addBox(-3.0F, -24.0F, -2.0F, 6.0F, 24.0F, 4.0F),
                PartPose.offsetAndRotation(46.765F, -27.0F, 0.0F, 0.0F, 0.0F, 1.0472F));
        PartDefinition crown2b = crown2.addOrReplaceChild("crown2b", CubeListBuilder.create()
                .texOffs(976, 110).addBox(-3.0F, -26.0F, -2.0F, 6.0F, 26.0F, 4.0F),
                PartPose.offset(0.0F, -24.0F, 0.0F));
        PartDefinition crown2c = crown2b.addOrReplaceChild("crown2c", CubeListBuilder.create()
                .texOffs(80, 321).addBox(-4.0F, -8.0F, -1.5F, 8.0F, 8.0F, 3.0F)
                .texOffs(930, 320).addBox(-4.0F, -17.0F, -1.0F, 1.0F, 9.0F, 2.0F)
                .texOffs(1016, 108).addBox(-1.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(1016, 121).addBox(2.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(956, 323).addBox(4.0F, -15.0F, -1.0F, 1.0F, 7.0F, 2.0F),
                PartPose.offset(0.0F, -26.0F, 0.0F));
        PartDefinition crown3 = halo.addOrReplaceChild("crown3", CubeListBuilder.create()
                .texOffs(796, 294).addBox(-3.0F, -24.0F, -2.0F, 6.0F, 24.0F, 4.0F),
                PartPose.offsetAndRotation(54.0F, -0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));
        PartDefinition crown3b = crown3.addOrReplaceChild("crown3b", CubeListBuilder.create()
                .texOffs(996, 116).addBox(-3.0F, -26.0F, -2.0F, 6.0F, 26.0F, 4.0F),
                PartPose.offset(0.0F, -24.0F, 0.0F));
        PartDefinition crown3c = crown3b.addOrReplaceChild("crown3c", CubeListBuilder.create()
                .texOffs(102, 321).addBox(-4.0F, -8.0F, -1.5F, 8.0F, 8.0F, 3.0F)
                .texOffs(146, 321).addBox(-4.0F, -17.0F, -1.0F, 1.0F, 9.0F, 2.0F)
                .texOffs(1016, 134).addBox(-1.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(1016, 147).addBox(2.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(962, 323).addBox(4.0F, -15.0F, -1.0F, 1.0F, 7.0F, 2.0F),
                PartPose.offset(0.0F, -26.0F, 0.0F));
        PartDefinition crown4 = halo.addOrReplaceChild("crown4", CubeListBuilder.create()
                .texOffs(816, 294).addBox(-3.0F, -24.0F, -2.0F, 6.0F, 24.0F, 4.0F),
                PartPose.offsetAndRotation(46.765F, 27.0F, 0.0F, 0.0F, 0.0F, 2.0944F));
        PartDefinition crown4b = crown4.addOrReplaceChild("crown4b", CubeListBuilder.create()
                .texOffs(976, 140).addBox(-3.0F, -26.0F, -2.0F, 6.0F, 26.0F, 4.0F),
                PartPose.offset(0.0F, -24.0F, 0.0F));
        PartDefinition crown4c = crown4b.addOrReplaceChild("crown4c", CubeListBuilder.create()
                .texOffs(124, 321).addBox(-4.0F, -8.0F, -1.5F, 8.0F, 8.0F, 3.0F)
                .texOffs(736, 322).addBox(-4.0F, -17.0F, -1.0F, 1.0F, 9.0F, 2.0F)
                .texOffs(1016, 160).addBox(-1.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(1016, 173).addBox(2.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(968, 323).addBox(4.0F, -15.0F, -1.0F, 1.0F, 7.0F, 2.0F),
                PartPose.offset(0.0F, -26.0F, 0.0F));
        PartDefinition crown5 = halo.addOrReplaceChild("crown5", CubeListBuilder.create()
                .texOffs(836, 294).addBox(-3.0F, -24.0F, -2.0F, 6.0F, 24.0F, 4.0F),
                PartPose.offsetAndRotation(27.0F, 46.765F, 0.0F, 0.0F, 0.0F, 2.618F));
        PartDefinition crown5b = crown5.addOrReplaceChild("crown5b", CubeListBuilder.create()
                .texOffs(996, 146).addBox(-3.0F, -26.0F, -2.0F, 6.0F, 26.0F, 4.0F),
                PartPose.offset(0.0F, -24.0F, 0.0F));
        PartDefinition crown5c = crown5b.addOrReplaceChild("crown5c", CubeListBuilder.create()
                .texOffs(754, 322).addBox(-4.0F, -8.0F, -1.5F, 8.0F, 8.0F, 3.0F)
                .texOffs(742, 322).addBox(-4.0F, -17.0F, -1.0F, 1.0F, 9.0F, 2.0F)
                .texOffs(1016, 186).addBox(-1.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(1016, 250).addBox(2.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(974, 323).addBox(4.0F, -15.0F, -1.0F, 1.0F, 7.0F, 2.0F),
                PartPose.offset(0.0F, -26.0F, 0.0F));
        PartDefinition crown6 = halo.addOrReplaceChild("crown6", CubeListBuilder.create()
                .texOffs(856, 294).addBox(-3.0F, -24.0F, -2.0F, 6.0F, 24.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 54.0F, 0.0F, 0.0F, 0.0F, 3.1416F));
        PartDefinition crown6b = crown6.addOrReplaceChild("crown6b", CubeListBuilder.create()
                .texOffs(640, 169).addBox(-3.0F, -26.0F, -2.0F, 6.0F, 26.0F, 4.0F),
                PartPose.offset(0.0F, -24.0F, 0.0F));
        PartDefinition crown6c = crown6b.addOrReplaceChild("crown6c", CubeListBuilder.create()
                .texOffs(776, 322).addBox(-4.0F, -8.0F, -1.5F, 8.0F, 8.0F, 3.0F)
                .texOffs(864, 322).addBox(-4.0F, -17.0F, -1.0F, 1.0F, 9.0F, 2.0F)
                .texOffs(932, 262).addBox(-1.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(1016, 263).addBox(2.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(980, 323).addBox(4.0F, -15.0F, -1.0F, 1.0F, 7.0F, 2.0F),
                PartPose.offset(0.0F, -26.0F, 0.0F));
        PartDefinition crown7 = halo.addOrReplaceChild("crown7", CubeListBuilder.create()
                .texOffs(388, 295).addBox(-3.0F, -24.0F, -2.0F, 6.0F, 24.0F, 4.0F),
                PartPose.offsetAndRotation(-27.0F, 46.765F, 0.0F, 0.0F, 0.0F, 3.6652F));
        PartDefinition crown7b = crown7.addOrReplaceChild("crown7b", CubeListBuilder.create()
                .texOffs(976, 170).addBox(-3.0F, -26.0F, -2.0F, 6.0F, 26.0F, 4.0F),
                PartPose.offset(0.0F, -24.0F, 0.0F));
        PartDefinition crown7c = crown7b.addOrReplaceChild("crown7c", CubeListBuilder.create()
                .texOffs(798, 322).addBox(-4.0F, -8.0F, -1.5F, 8.0F, 8.0F, 3.0F)
                .texOffs(870, 322).addBox(-4.0F, -17.0F, -1.0F, 1.0F, 9.0F, 2.0F)
                .texOffs(932, 275).addBox(-1.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(1016, 276).addBox(2.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(986, 323).addBox(4.0F, -15.0F, -1.0F, 1.0F, 7.0F, 2.0F),
                PartPose.offset(0.0F, -26.0F, 0.0F));
        PartDefinition crown8 = halo.addOrReplaceChild("crown8", CubeListBuilder.create()
                .texOffs(408, 295).addBox(-3.0F, -24.0F, -2.0F, 6.0F, 24.0F, 4.0F),
                PartPose.offsetAndRotation(-46.765F, 27.0F, 0.0F, 0.0F, 0.0F, 4.1888F));
        PartDefinition crown8b = crown8.addOrReplaceChild("crown8b", CubeListBuilder.create()
                .texOffs(996, 176).addBox(-3.0F, -26.0F, -2.0F, 6.0F, 26.0F, 4.0F),
                PartPose.offset(0.0F, -24.0F, 0.0F));
        PartDefinition crown8c = crown8b.addOrReplaceChild("crown8c", CubeListBuilder.create()
                .texOffs(820, 322).addBox(-4.0F, -8.0F, -1.5F, 8.0F, 8.0F, 3.0F)
                .texOffs(432, 323).addBox(-4.0F, -17.0F, -1.0F, 1.0F, 9.0F, 2.0F)
                .texOffs(556, 289).addBox(-1.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(748, 300).addBox(2.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(608, 324).addBox(4.0F, -15.0F, -1.0F, 1.0F, 7.0F, 2.0F),
                PartPose.offset(0.0F, -26.0F, 0.0F));
        PartDefinition crown9 = halo.addOrReplaceChild("crown9", CubeListBuilder.create()
                .texOffs(428, 295).addBox(-3.0F, -24.0F, -2.0F, 6.0F, 24.0F, 4.0F),
                PartPose.offsetAndRotation(-54.0F, 0.0F, 0.0F, 0.0F, 0.0F, 4.7124F));
        PartDefinition crown9b = crown9.addOrReplaceChild("crown9b", CubeListBuilder.create()
                .texOffs(892, 262).addBox(-3.0F, -26.0F, -2.0F, 6.0F, 26.0F, 4.0F),
                PartPose.offset(0.0F, -24.0F, 0.0F));
        PartDefinition crown9c = crown9b.addOrReplaceChild("crown9c", CubeListBuilder.create()
                .texOffs(842, 322).addBox(-4.0F, -8.0F, -1.5F, 8.0F, 8.0F, 3.0F)
                .texOffs(438, 323).addBox(-4.0F, -17.0F, -1.0F, 1.0F, 9.0F, 2.0F)
                .texOffs(592, 302).addBox(-1.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(598, 302).addBox(2.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(614, 324).addBox(4.0F, -15.0F, -1.0F, 1.0F, 7.0F, 2.0F),
                PartPose.offset(0.0F, -26.0F, 0.0F));
        PartDefinition crown10 = halo.addOrReplaceChild("crown10", CubeListBuilder.create()
                .texOffs(448, 295).addBox(-3.0F, -24.0F, -2.0F, 6.0F, 24.0F, 4.0F),
                PartPose.offsetAndRotation(-46.765F, -27.0F, 0.0F, 0.0F, 0.0F, 5.236F));
        PartDefinition crown10b = crown10.addOrReplaceChild("crown10b", CubeListBuilder.create()
                .texOffs(912, 262).addBox(-3.0F, -26.0F, -2.0F, 6.0F, 26.0F, 4.0F),
                PartPose.offset(0.0F, -24.0F, 0.0F));
        PartDefinition crown10c = crown10b.addOrReplaceChild("crown10c", CubeListBuilder.create()
                .texOffs(388, 323).addBox(-4.0F, -8.0F, -1.5F, 8.0F, 8.0F, 3.0F)
                .texOffs(444, 323).addBox(-4.0F, -17.0F, -1.0F, 1.0F, 9.0F, 2.0F)
                .texOffs(748, 313).addBox(-1.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(292, 320).addBox(2.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(936, 324).addBox(4.0F, -15.0F, -1.0F, 1.0F, 7.0F, 2.0F),
                PartPose.offset(0.0F, -26.0F, 0.0F));
        PartDefinition crown11 = halo.addOrReplaceChild("crown11", CubeListBuilder.create()
                .texOffs(936, 296).addBox(-3.0F, -24.0F, -2.0F, 6.0F, 24.0F, 4.0F),
                PartPose.offsetAndRotation(-27.0F, -46.765F, 0.0F, 0.0F, 0.0F, 5.7596F));
        PartDefinition crown11b = crown11.addOrReplaceChild("crown11b", CubeListBuilder.create()
                .texOffs(736, 270).addBox(-3.0F, -26.0F, -2.0F, 6.0F, 26.0F, 4.0F),
                PartPose.offset(0.0F, -24.0F, 0.0F));
        PartDefinition crown11c = crown11b.addOrReplaceChild("crown11c", CubeListBuilder.create()
                .texOffs(410, 323).addBox(-4.0F, -8.0F, -1.5F, 8.0F, 8.0F, 3.0F)
                .texOffs(450, 323).addBox(-4.0F, -17.0F, -1.0F, 1.0F, 9.0F, 2.0F)
                .texOffs(516, 320).addBox(-1.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(522, 320).addBox(2.0F, -19.0F, -1.0F, 1.0F, 11.0F, 2.0F)
                .texOffs(942, 324).addBox(4.0F, -15.0F, -1.0F, 1.0F, 7.0F, 2.0F),
                PartPose.offset(0.0F, -26.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 1024, 1024).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    /** How far it is throwing its head back, 0 to 1 (more than one while two of the roars overlap). */
    private static float roar(float age) {
        float sum = 0.0F;
        for (float[] r : ROARS) {
            sum += Anim.ramp(age, r[0], r[1]) * (1.0F - Anim.ramp(age, r[2], r[3]));
        }
        return sum;
    }

    @Override
    public void setupAnim(ColossusRenderState state) {
        super.setupAnim(state);
        float age = state.age;
        float roar = roar(age);
        // the sea comes up round its foot, and heaves
        float plate = Anim.lerp(Anim.ramp(age, 0.0F, 60.0F), 0.25F, 1.0F);
        this.sea.xScale = plate;
        this.sea.zScale = plate;
        // the knot comes up out of the water, and shakes where it roars
        float sink = 76.0F * (1.0F - Anim.ramp(age, 30.0F, 210.0F));
        this.knot.y += (sink + Mth.sin(age * 0.09F) * 0.8F * (1.0F - Anim.ramp(age, 210.0F, 300.0F))) * MODEL_SCALE;
        this.knot.x += Mth.sin(age * 2.3F) * 0.5F * roar * MODEL_SCALE;
        // the arms thrown up come down on the sea
        float slam = Anim.ramp(age, 190.0F, 226.0F);
        for (int i = 0; i < 4; i++) {
            this.arm[i].zRot -= ARM_SIDE[i] * 1.4F * (1.0F - slam);
            this.fore[i].zRot -= ARM_SIDE[i] * 0.95F * (1.0F - slam);
        }
        // the rope was coiled on itself: it straightens from its foot, and sways once it stands
        float stand = Anim.ramp(age, 330.0F, 520.0F);
        for (int i = 0; i < RINGS; i++) {
            float bend = 0.30F * (1.0F - Anim.ramp(age, 150.0F + 16.0F * i, 330.0F + 16.0F * i));
            this.ring[i].xRot += bend + Mth.cos(age * 0.04F - i * 0.4F) * 0.02F * stand;
            this.ring[i].zRot += Mth.sin(age * 0.05F - i * 0.5F) * 0.025F * stand;
        }
        // the head was bowed, and is lifted; in a roar it is thrown back
        this.head.xRot += 1.25F * (1.0F - Anim.ramp(age, 300.0F, 470.0F)) - 0.5F * roar;
        // the crown was folded behind the disc and opens one arm after another; open, it turns very slowly, and its arms stir
        float open = Anim.ramp(age, 400.0F, 560.0F);
        this.halo.zRot += age * 0.0025F * open;
        for (int j = 0; j < CROWN; j++) {
            float bloom = Anim.ramp(age, 400.0F + 5.0F * j, 530.0F + 5.0F * j);
            this.crown[j].xRot += -1.45F * (1.0F - bloom) + 0.3F * roar * bloom;
            this.crownB[j].xRot += -1.2F * (1.0F - bloom) + Mth.sin(age * 0.07F + j * 0.9F) * 0.06F * bloom;
            this.crownC[j].xRot += -0.9F * (1.0F - bloom) + Mth.sin(age * 0.09F + j * 1.3F) * 0.1F * bloom;
        }
    }
}
