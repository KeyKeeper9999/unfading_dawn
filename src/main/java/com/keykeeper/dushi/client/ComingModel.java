package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.ComingEntity;

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
 * The Coming. The mesh and the poses between the markers are written by tools/gen_coming.py (which also paints the
 * texture and draws the poses on a sheet); the animation is written by hand: it goes from one pose of the sheet to
 * another, and adds what breathes and sways.
 * <p>
 * It does not turn: it is in a crack of the wall behind it. Masked, all of it but its neck and its two great arms is
 * {@link #BACK_IN} back in the rock; with the mask off it is out to the breast, a second pair of arms and the crown
 * round its disc with it, and the eye in the crack behind it is open. It looks at whoever it fights with its neck
 * ({@code extra} of the state is how far round, see ComingEntity.renderExtra).
 */
public class ComingModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("coming"), "main");
    /** Must match the scale in the generator. */
    private static final float MODEL_SCALE = 2.4F;
    /** How far back in the rock it is under its mask, in model units: BACK_IN in the generator. */
    private static final float BACK_IN = 24.0F;
    /** The arms round the disc: must match CROWN in tools/gen_coming.py. */
    private static final int CROWN = 8;
    /** The parts a pose turns, in the order of the rows of a pose: POSED in the generator. */
    private static final String[][] POSED = {{"chest", "neck1"}, {"chest", "neck1", "neck2"}, {"chest", "neck1", "neck2", "neck3"},
            {"chest", "neck1", "neck2", "neck3", "head"}, {"chest", "arm_r"}, {"chest", "arm_r", "fore_r"}, {"chest", "arm_r", "fore_r", "hand_r"},
            {"chest", "arm_l"}, {"chest", "arm_l", "fore_l"}, {"chest", "arm_l", "fore_l", "hand_l"}, {"chest", "arm2_r"}, {"chest", "arm2_r", "fore2_r"},
            {"chest", "arm2_l"}, {"chest", "arm2_l", "fore2_l"}, {"chest", "neck1", "neck2", "neck3", "maw_r"}, {"chest", "neck1", "neck2", "neck3", "maw_l"}};
    private static final int NECK1 = 0;
    private static final int NECK2 = 1;
    private static final int NECK3 = 2;
    private static final int HEAD = 3;
    private static final int ARM_R = 4;
    private static final int ARM_L = 7;
    private static final int ARM2_R = 10;
    private static final int ARM2_L = 12;

    // <generated poses>
    private static final float[][] MASKED = {{0.800F, 0.000F, 0.000F}, {0.250F, 0.000F, 0.000F}, {-0.300F, 0.000F, 0.000F}, {-0.800F, 0.000F, 0.000F}, {0.892F, 0.000F, -2.141F}, {0.554F, 0.000F, -1.167F}, {0.900F, 0.000F, 0.000F}, {0.892F, 0.000F, 2.141F}, {0.554F, 0.000F, 1.167F}, {0.900F, 0.000F, 0.000F}, {-1.373F, 0.000F, 3.142F}, {-0.098F, 0.000F, -0.000F}, {-1.373F, 0.000F, 3.142F}, {-0.098F, 0.000F, -0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] GAZE = {{0.500F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {-0.300F, 0.000F, 0.000F}, {-0.800F, 0.000F, 0.000F}, {0.892F, 0.000F, -2.141F}, {0.554F, 0.000F, -1.167F}, {0.900F, 0.000F, 0.000F}, {0.892F, 0.000F, 2.141F}, {0.554F, 0.000F, 1.167F}, {0.900F, 0.000F, 0.000F}, {-1.373F, 0.000F, 3.142F}, {-0.098F, 0.000F, -0.000F}, {-1.373F, 0.000F, 3.142F}, {-0.098F, 0.000F, -0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] SLAM_BACK = {{0.300F, 0.000F, 0.000F}, {-0.250F, 0.000F, 0.000F}, {-0.400F, 0.000F, 0.000F}, {-0.700F, 0.000F, 0.000F}, {0.892F, 0.000F, -2.141F}, {0.554F, 0.000F, -1.167F}, {0.900F, 0.000F, 0.000F}, {0.892F, 0.000F, 2.141F}, {0.554F, 0.000F, 1.167F}, {0.900F, 0.000F, 0.000F}, {-1.373F, 0.000F, 3.142F}, {-0.098F, 0.000F, -0.000F}, {-1.373F, 0.000F, 3.142F}, {-0.098F, 0.000F, -0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] SLAM = {{1.250F, 0.000F, 0.000F}, {0.250F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {-0.800F, 0.000F, 0.000F}, {0.892F, 0.000F, -2.141F}, {0.554F, 0.000F, -1.167F}, {0.900F, 0.000F, 0.000F}, {0.892F, 0.000F, 2.141F}, {0.554F, 0.000F, 1.167F}, {0.900F, 0.000F, 0.000F}, {-1.373F, 0.000F, 3.142F}, {-0.098F, 0.000F, -0.000F}, {-1.373F, 0.000F, 3.142F}, {-0.098F, 0.000F, -0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] SWEEP_BACK = {{0.800F, 0.000F, 0.000F}, {0.250F, 0.000F, 0.000F}, {-0.300F, 0.000F, 0.000F}, {-0.800F, 0.000F, 0.000F}, {0.520F, 0.000F, -1.802F}, {-0.212F, 0.000F, -0.088F}, {0.400F, 0.000F, 0.000F}, {0.892F, 0.000F, 2.141F}, {0.554F, 0.000F, 1.167F}, {0.900F, 0.000F, 0.000F}, {-1.373F, 0.000F, 3.142F}, {-0.098F, 0.000F, -0.000F}, {-1.373F, 0.000F, 3.142F}, {-0.098F, 0.000F, -0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] SWEEP = {{0.800F, 0.000F, 0.000F}, {0.250F, 0.000F, 0.000F}, {-0.300F, 0.000F, 0.000F}, {-0.800F, 0.000F, 0.000F}, {0.963F, 0.000F, 2.232F}, {-0.394F, 0.000F, -0.157F}, {0.400F, 0.000F, 0.000F}, {0.892F, 0.000F, 2.141F}, {0.554F, 0.000F, 1.167F}, {0.900F, 0.000F, 0.000F}, {-1.373F, 0.000F, 3.142F}, {-0.098F, 0.000F, -0.000F}, {-1.373F, 0.000F, 3.142F}, {-0.098F, 0.000F, -0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] CRUSH_UP = {{0.600F, 0.000F, 0.000F}, {0.200F, 0.000F, 0.000F}, {-0.200F, 0.000F, 0.000F}, {-0.200F, 0.000F, 0.000F}, {0.654F, 0.000F, -0.464F}, {0.014F, 0.000F, 0.271F}, {0.300F, 0.000F, 0.000F}, {0.654F, 0.000F, 0.464F}, {0.014F, 0.000F, -0.271F}, {0.300F, 0.000F, 0.000F}, {-1.373F, 0.000F, 3.142F}, {-0.098F, 0.000F, -0.000F}, {-1.373F, 0.000F, 3.142F}, {-0.098F, 0.000F, -0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] CRUSH = {{0.800F, 0.000F, 0.000F}, {0.250F, 0.000F, 0.000F}, {-0.300F, 0.000F, 0.000F}, {-0.800F, 0.000F, 0.000F}, {1.012F, 0.000F, -2.423F}, {-0.033F, 0.000F, -0.701F}, {1.200F, 0.000F, 0.000F}, {1.012F, 0.000F, 2.423F}, {-0.033F, 0.000F, 0.701F}, {1.200F, 0.000F, 0.000F}, {-1.373F, 0.000F, 3.142F}, {-0.098F, 0.000F, -0.000F}, {-1.373F, 0.000F, 3.142F}, {-0.098F, 0.000F, -0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] REACH = {{0.950F, 0.000F, 0.000F}, {0.350F, 0.000F, 0.000F}, {-0.100F, 0.000F, 0.000F}, {-0.500F, 0.000F, 0.000F}, {0.947F, 0.000F, -2.111F}, {0.008F, 0.000F, -1.197F}, {1.500F, 0.000F, 0.000F}, {0.947F, 0.000F, 2.111F}, {0.008F, 0.000F, 1.197F}, {1.500F, 0.000F, 0.000F}, {-1.373F, 0.000F, 3.142F}, {-0.098F, 0.000F, -0.000F}, {-1.373F, 0.000F, 3.142F}, {-0.098F, 0.000F, -0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] BARE = {{0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.594F, 0.000F, -2.296F}, {0.383F, 0.000F, -0.442F}, {0.900F, 0.000F, 0.000F}, {0.594F, 0.000F, 2.296F}, {0.383F, 0.000F, 0.442F}, {0.900F, 0.000F, 0.000F}, {0.597F, 0.000F, -1.816F}, {0.039F, 0.000F, -0.948F}, {0.597F, 0.000F, 1.816F}, {0.039F, 0.000F, 0.948F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] BARE_GAZE = {{-0.100F, 0.000F, 0.000F}, {-0.100F, 0.000F, 0.000F}, {-0.200F, 0.000F, 0.000F}, {-0.250F, 0.000F, 0.000F}, {0.594F, 0.000F, -2.296F}, {0.383F, 0.000F, -0.442F}, {0.900F, 0.000F, 0.000F}, {0.594F, 0.000F, 2.296F}, {0.383F, 0.000F, 0.442F}, {0.900F, 0.000F, 0.000F}, {0.597F, 0.000F, -1.816F}, {0.039F, 0.000F, -0.948F}, {0.597F, 0.000F, 1.816F}, {0.039F, 0.000F, 0.948F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] BARE_SLAM_BACK = {{-0.400F, 0.000F, 0.000F}, {-0.300F, 0.000F, 0.000F}, {-0.300F, 0.000F, 0.000F}, {-0.500F, 0.000F, 0.000F}, {0.594F, 0.000F, -2.296F}, {0.383F, 0.000F, -0.442F}, {0.900F, 0.000F, 0.000F}, {0.594F, 0.000F, 2.296F}, {0.383F, 0.000F, 0.442F}, {0.900F, 0.000F, 0.000F}, {0.597F, 0.000F, -1.816F}, {0.039F, 0.000F, -0.948F}, {0.597F, 0.000F, 1.816F}, {0.039F, 0.000F, 0.948F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] BARE_SLAM = {{1.080F, 0.000F, 0.000F}, {0.420F, 0.000F, 0.000F}, {0.200F, 0.000F, 0.000F}, {-0.750F, 0.000F, 0.000F}, {0.594F, 0.000F, -2.296F}, {0.383F, 0.000F, -0.442F}, {0.900F, 0.000F, 0.000F}, {0.594F, 0.000F, 2.296F}, {0.383F, 0.000F, 0.442F}, {0.900F, 0.000F, 0.000F}, {0.597F, 0.000F, -1.816F}, {0.039F, 0.000F, -0.948F}, {0.597F, 0.000F, 1.816F}, {0.039F, 0.000F, 0.948F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] BARE_SWEEP_BACK = {{0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.413F, 0.000F, -1.736F}, {-0.102F, 0.000F, -0.150F}, {0.400F, 0.000F, 0.000F}, {0.594F, 0.000F, 2.296F}, {0.383F, 0.000F, 0.442F}, {0.900F, 0.000F, 0.000F}, {0.597F, 0.000F, -1.816F}, {0.039F, 0.000F, -0.948F}, {0.597F, 0.000F, 1.816F}, {0.039F, 0.000F, 0.948F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] BARE_SWEEP = {{0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.639F, 0.000F, 1.951F}, {0.155F, 0.000F, 0.105F}, {0.400F, 0.000F, 0.000F}, {0.594F, 0.000F, 2.296F}, {0.383F, 0.000F, 0.442F}, {0.900F, 0.000F, 0.000F}, {0.597F, 0.000F, -1.816F}, {0.039F, 0.000F, -0.948F}, {0.597F, 0.000F, 1.816F}, {0.039F, 0.000F, 0.948F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] BARE_CRUSH_UP = {{0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.418F, 0.000F, -0.588F}, {0.124F, 0.000F, 0.311F}, {0.300F, 0.000F, 0.000F}, {0.418F, 0.000F, 0.588F}, {0.124F, 0.000F, -0.311F}, {0.300F, 0.000F, 0.000F}, {0.597F, 0.000F, -1.816F}, {0.039F, 0.000F, -0.948F}, {0.597F, 0.000F, 1.816F}, {0.039F, 0.000F, 0.948F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] BARE_CRUSH = {{0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.722F, 0.000F, -2.498F}, {-0.207F, 0.000F, -0.548F}, {1.200F, 0.000F, 0.000F}, {0.722F, 0.000F, 2.498F}, {-0.207F, 0.000F, 0.548F}, {1.200F, 0.000F, 0.000F}, {0.597F, 0.000F, -1.816F}, {0.039F, 0.000F, -0.948F}, {0.597F, 0.000F, 1.816F}, {0.039F, 0.000F, 0.948F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] BARE_REACH = {{0.500F, 0.000F, 0.000F}, {0.400F, 0.000F, 0.000F}, {0.200F, 0.000F, 0.000F}, {-0.300F, 0.000F, 0.000F}, {0.780F, 0.000F, -2.356F}, {-0.211F, 0.000F, -0.861F}, {1.500F, 0.000F, 0.000F}, {0.780F, 0.000F, 2.356F}, {-0.211F, 0.000F, 0.861F}, {1.500F, 0.000F, 0.000F}, {0.597F, 0.000F, -1.816F}, {0.039F, 0.000F, -0.948F}, {0.597F, 0.000F, 1.816F}, {0.039F, 0.000F, 0.948F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] PULL = {{0.250F, 0.000F, 0.000F}, {0.250F, 0.000F, 0.000F}, {0.200F, 0.000F, 0.000F}, {-0.700F, 0.000F, 0.000F}, {0.594F, 0.000F, -2.296F}, {0.383F, 0.000F, -0.442F}, {0.900F, 0.000F, 0.000F}, {0.594F, 0.000F, 2.296F}, {0.383F, 0.000F, 0.442F}, {0.900F, 0.000F, 0.000F}, {0.597F, 0.000F, -1.816F}, {0.039F, 0.000F, -0.948F}, {0.597F, 0.000F, 1.816F}, {0.039F, 0.000F, 0.948F}, {0.000F, 1.350F, 0.000F}, {0.000F, -1.350F, 0.000F}};
    private static final float[][] BITE = {{0.750F, 0.000F, 0.000F}, {0.450F, 0.000F, 0.000F}, {0.300F, 0.000F, 0.000F}, {-0.600F, 0.000F, 0.000F}, {0.594F, 0.000F, -2.296F}, {0.383F, 0.000F, -0.442F}, {0.900F, 0.000F, 0.000F}, {0.594F, 0.000F, 2.296F}, {0.383F, 0.000F, 0.442F}, {0.900F, 0.000F, 0.000F}, {0.597F, 0.000F, -1.816F}, {0.039F, 0.000F, -0.948F}, {0.597F, 0.000F, 1.816F}, {0.039F, 0.000F, 0.948F}, {0.000F, 0.200F, 0.000F}, {0.000F, -0.200F, 0.000F}};
    private static final float[][] RAIN = {{-0.250F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {-0.900F, 0.000F, 0.000F}, {0.594F, 0.000F, -2.296F}, {0.383F, 0.000F, -0.442F}, {0.900F, 0.000F, 0.000F}, {0.594F, 0.000F, 2.296F}, {0.383F, 0.000F, 0.442F}, {0.900F, 0.000F, 0.000F}, {0.360F, 0.000F, -0.633F}, {-0.126F, 0.000F, 0.420F}, {0.360F, 0.000F, 0.633F}, {-0.126F, 0.000F, -0.420F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] GLARE = {{0.600F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.350F, 0.000F, 0.000F}, {-0.200F, 0.000F, 0.000F}, {0.594F, 0.000F, -2.296F}, {0.383F, 0.000F, -0.442F}, {0.900F, 0.000F, 0.000F}, {0.594F, 0.000F, 2.296F}, {0.383F, 0.000F, 0.442F}, {0.900F, 0.000F, 0.000F}, {0.597F, 0.000F, -1.816F}, {0.039F, 0.000F, -0.948F}, {0.597F, 0.000F, 1.816F}, {0.039F, 0.000F, 0.948F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    // </generated poses>

    private final ModelPart eye;
    private final ModelPart body;
    private final ModelPart mask;
    private final ModelPart[] part = new ModelPart[POSED.length];
    private final ModelPart[] crown = new ModelPart[CROWN];
    private final ModelPart[] crownFore = new ModelPart[CROWN];

    public ComingModel(ModelPart root) {
        super(root);
        ModelPart all = root.getChild("all");
        this.eye = all.getChild("eye");
        this.body = all.getChild("body");
        for (int i = 0; i < POSED.length; i++) {
            ModelPart at = this.body;
            for (String name : POSED[i]) {
                at = at.getChild(name);
            }
            this.part[i] = at;
        }
        this.mask = this.part[HEAD].getChild("mask");
        for (int i = 0; i < CROWN; i++) {
            this.crown[i] = this.part[HEAD].getChild("crown" + i);
            this.crownFore[i] = this.crown[i].getChild("crownf" + i);
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition eye = all.addOrReplaceChild("eye", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-17.0F, -26.0F, 0.0F, 34.0F, 52.0F, 1.0F),
                PartPose.offset(0.0F, -52.0F, 9.5F));
        PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(154, 0).addBox(-8.0F, -19.0F, 14.0F, 16.0F, 17.0F, 22.0F),
                PartPose.ZERO);
        PartDefinition chest = body.addOrReplaceChild("chest", CubeListBuilder.create()
                .texOffs(70, 0).addBox(-10.0F, -11.0F, -8.0F, 20.0F, 22.0F, 22.0F)
                .texOffs(64, 69).addBox(-13.0F, -12.0F, -10.0F, 26.0F, 9.0F, 11.0F)
                .texOffs(138, 69).addBox(-7.0F, -3.0F, -13.0F, 14.0F, 14.0F, 6.0F)
                .texOffs(32, 75).addBox(-0.5F, -16.0F, 1.0F, 1.0F, 4.0F, 13.0F),
                PartPose.offset(0.0F, -13.0F, 2.0F));
        PartDefinition neck1 = chest.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(154, 39).addBox(-6.0F, -18.0F, -6.0F, 12.0F, 18.0F, 12.0F)
                .texOffs(242, 25).addBox(-0.5F, -18.0F, 6.0F, 1.0F, 18.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -9.0F, -7.0F, 0.15F, 0.0F, 0.0F));
        PartDefinition neck2 = neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(202, 39).addBox(-5.0F, -16.0F, -5.0F, 10.0F, 16.0F, 10.0F)
                .texOffs(236, 68).addBox(-0.5F, -16.0F, 5.0F, 1.0F, 16.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -18.0F, 0.0F, 0.2F, 0.0F, 0.0F));
        PartDefinition neck3 = neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(32, 53).addBox(-4.0F, -14.0F, -4.0F, 8.0F, 14.0F, 8.0F)
                .texOffs(186, 69).addBox(-0.5F, -14.0F, 4.0F, 1.0F, 14.0F, 3.0F)
                .texOffs(88, 89).addBox(-3.0F, -12.0F, -4.5F, 6.0F, 10.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 0.25F, 0.0F, 0.0F));
        PartDefinition maw_r = neck3.addOrReplaceChild("maw_r", CubeListBuilder.create()
                .texOffs(68, 89).addBox(0.0F, 0.0F, -1.0F, 4.0F, 11.0F, 1.0F),
                PartPose.offset(-4.0F, -12.5F, -5.0F));
        PartDefinition maw_l = neck3.addOrReplaceChild("maw_l", CubeListBuilder.create()
                .texOffs(78, 89).addBox(-4.0F, 0.0F, -1.0F, 4.0F, 11.0F, 1.0F),
                PartPose.offset(4.0F, -12.5F, -5.0F));
        PartDefinition head = neck3.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(70, 44).addBox(-11.0F, -11.0F, -4.0F, 22.0F, 22.0F, 3.0F)
                .texOffs(170, 98).addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -14.0F, 0.0F, 0.3F, 0.0F, 0.0F));
        PartDefinition mask = head.addOrReplaceChild("mask", CubeListBuilder.create()
                .texOffs(202, 65).addBox(-8.0F, -11.0F, -1.5F, 16.0F, 20.0F, 1.0F)
                .texOffs(186, 88).addBox(-15.0F, -24.0F, -1.0F, 30.0F, 14.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, -4.0F));
        PartDefinition crown0 = head.addOrReplaceChild("crown0", CubeListBuilder.create()
                .texOffs(230, 25).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(3.83F, -9.24F, -1.0F, -2.5F, 0.0F, 0.3927F));
        PartDefinition crownf0 = crown0.addOrReplaceChild("crownf0", CubeListBuilder.create()
                .texOffs(250, 0).addBox(-0.5F, -10.0F, -0.5F, 1.0F, 10.0F, 1.0F)
                .texOffs(122, 89).addBox(-2.5F, -17.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown1 = head.addOrReplaceChild("crown1", CubeListBuilder.create()
                .texOffs(194, 69).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(9.24F, -3.83F, -1.0F, -2.5F, 0.0F, 1.1781F));
        PartDefinition crownf1 = crown1.addOrReplaceChild("crownf1", CubeListBuilder.create()
                .texOffs(250, 11).addBox(-0.5F, -10.0F, -0.5F, 1.0F, 10.0F, 1.0F)
                .texOffs(134, 89).addBox(-2.5F, -17.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown2 = head.addOrReplaceChild("crown2", CubeListBuilder.create()
                .texOffs(0, 77).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(9.24F, 3.83F, -1.0F, -2.5F, 0.0F, 1.9635F));
        PartDefinition crownf2 = crown2.addOrReplaceChild("crownf2", CubeListBuilder.create()
                .texOffs(252, 22).addBox(-0.5F, -10.0F, -0.5F, 1.0F, 10.0F, 1.0F)
                .texOffs(146, 89).addBox(-2.5F, -17.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown3 = head.addOrReplaceChild("crown3", CubeListBuilder.create()
                .texOffs(8, 77).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(3.83F, 9.24F, -1.0F, -2.5F, 0.0F, 2.7489F));
        PartDefinition crownf3 = crown3.addOrReplaceChild("crownf3", CubeListBuilder.create()
                .texOffs(238, 25).addBox(-0.5F, -10.0F, -0.5F, 1.0F, 10.0F, 1.0F)
                .texOffs(158, 89).addBox(-2.5F, -17.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown4 = head.addOrReplaceChild("crown4", CubeListBuilder.create()
                .texOffs(16, 77).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(-3.83F, 9.24F, -1.0F, -2.5F, 0.0F, 3.5343F));
        PartDefinition crownf4 = crown4.addOrReplaceChild("crownf4", CubeListBuilder.create()
                .texOffs(252, 33).addBox(-0.5F, -10.0F, -0.5F, 1.0F, 10.0F, 1.0F)
                .texOffs(170, 89).addBox(-2.5F, -17.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown5 = head.addOrReplaceChild("crown5", CubeListBuilder.create()
                .texOffs(24, 77).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(-9.24F, 3.83F, -1.0F, -2.5F, 0.0F, 4.3197F));
        PartDefinition crownf5 = crown5.addOrReplaceChild("crownf5", CubeListBuilder.create()
                .texOffs(64, 53).addBox(-0.5F, -10.0F, -0.5F, 1.0F, 10.0F, 1.0F)
                .texOffs(0, 90).addBox(-2.5F, -17.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown6 = head.addOrReplaceChild("crown6", CubeListBuilder.create()
                .texOffs(248, 88).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(-9.24F, -3.83F, -1.0F, -2.5F, 0.0F, 5.1051F));
        PartDefinition crownf6 = crown6.addOrReplaceChild("crownf6", CubeListBuilder.create()
                .texOffs(60, 75).addBox(-0.5F, -10.0F, -0.5F, 1.0F, 10.0F, 1.0F)
                .texOffs(12, 90).addBox(-2.5F, -17.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown7 = head.addOrReplaceChild("crown7", CubeListBuilder.create()
                .texOffs(60, 89).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(-3.83F, -9.24F, -1.0F, -2.5F, 0.0F, 5.8905F));
        PartDefinition crownf7 = crown7.addOrReplaceChild("crownf7", CubeListBuilder.create()
                .texOffs(102, 89).addBox(-0.5F, -10.0F, -0.5F, 1.0F, 10.0F, 1.0F)
                .texOffs(24, 92).addBox(-2.5F, -17.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition arm_r = chest.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(230, 0).addBox(-2.5F, -20.0F, -2.5F, 5.0F, 20.0F, 5.0F),
                PartPose.offset(-13.0F, -8.0F, -6.0F));
        PartDefinition fore_r = arm_r.addOrReplaceChild("fore_r", CubeListBuilder.create()
                .texOffs(0, 53).addBox(-2.0F, -20.0F, -2.0F, 4.0F, 20.0F, 4.0F),
                PartPose.offset(0.0F, -20.0F, 0.0F));
        PartDefinition hand_r = fore_r.addOrReplaceChild("hand_r", CubeListBuilder.create()
                .texOffs(126, 98).addBox(-3.0F, -5.0F, -1.5F, 6.0F, 5.0F, 3.0F)
                .texOffs(182, 89).addBox(-3.0F, -13.0F, -0.5F, 1.0F, 8.0F, 1.0F)
                .texOffs(106, 89).addBox(-1.0F, -14.0F, -0.5F, 1.0F, 9.0F, 1.0F)
                .texOffs(110, 89).addBox(1.0F, -14.0F, -0.5F, 1.0F, 9.0F, 1.0F)
                .texOffs(162, 98).addBox(3.0F, -12.0F, -0.5F, 1.0F, 7.0F, 1.0F)
                .texOffs(64, 64).addBox(-5.0F, -6.0F, -0.5F, 2.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, -20.0F, 0.0F));
        PartDefinition arm2_r = chest.addOrReplaceChild("arm2_r", CubeListBuilder.create()
                .texOffs(140, 44).addBox(-1.5F, -18.0F, -1.5F, 3.0F, 18.0F, 3.0F),
                PartPose.offset(-10.0F, 3.0F, 2.0F));
        PartDefinition fore2_r = arm2_r.addOrReplaceChild("fore2_r", CubeListBuilder.create()
                .texOffs(246, 68).addBox(-1.0F, -18.0F, -1.0F, 2.0F, 18.0F, 2.0F)
                .texOffs(36, 92).addBox(-2.5F, -26.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offset(0.0F, -18.0F, 0.0F));
        PartDefinition arm_l = chest.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(120, 44).addBox(-2.5F, -20.0F, -2.5F, 5.0F, 20.0F, 5.0F),
                PartPose.offset(13.0F, -8.0F, -6.0F));
        PartDefinition fore_l = arm_l.addOrReplaceChild("fore_l", CubeListBuilder.create()
                .texOffs(16, 53).addBox(-2.0F, -20.0F, -2.0F, 4.0F, 20.0F, 4.0F),
                PartPose.offset(0.0F, -20.0F, 0.0F));
        PartDefinition hand_l = fore_l.addOrReplaceChild("hand_l", CubeListBuilder.create()
                .texOffs(144, 98).addBox(-3.0F, -5.0F, -1.5F, 6.0F, 5.0F, 3.0F)
                .texOffs(122, 98).addBox(2.0F, -13.0F, -0.5F, 1.0F, 8.0F, 1.0F)
                .texOffs(114, 89).addBox(0.0F, -14.0F, -0.5F, 1.0F, 9.0F, 1.0F)
                .texOffs(118, 89).addBox(-2.0F, -14.0F, -0.5F, 1.0F, 9.0F, 1.0F)
                .texOffs(166, 98).addBox(-4.0F, -12.0F, -0.5F, 1.0F, 7.0F, 1.0F)
                .texOffs(140, 65).addBox(3.0F, -6.0F, -0.5F, 2.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, -20.0F, 0.0F));
        PartDefinition arm2_l = chest.addOrReplaceChild("arm2_l", CubeListBuilder.create()
                .texOffs(242, 47).addBox(-1.5F, -18.0F, -1.5F, 3.0F, 18.0F, 3.0F),
                PartPose.offset(10.0F, 3.0F, 2.0F));
        PartDefinition fore2_l = arm2_l.addOrReplaceChild("fore2_l", CubeListBuilder.create()
                .texOffs(178, 69).addBox(-1.0F, -18.0F, -1.0F, 2.0F, 18.0F, 2.0F)
                .texOffs(48, 92).addBox(-2.5F, -26.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offset(0.0F, -18.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 256, 256).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    /** Every part {@code amount} of the way from where it is to a pose of the sheet. */
    private void pose(float amount, float[][] to) {
        if (amount <= 0.0F) {
            return;
        }
        for (int i = 0; i < this.part.length; i++) {
            Anim.pose(this.part[i], amount, to[i][0], to[i][1], to[i][2]);
        }
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float t = state.attackTime;
        int move = state.attackId;
        boolean bare = state.phase >= 2;
        // how far out of the rock it is, and how far the eye behind it has opened: both happen when the mask goes
        float out = bare ? (move == ComingEntity.EMERGE ? Anim.ramp(t, ComingEntity.EMERGE_BREAK + 4.0F, ComingEntity.EMERGE_BREAK + 40.0F) : 1.0F) : 0.0F;
        float lid = bare ? (move == ComingEntity.EMERGE ? Anim.ramp(t, ComingEntity.EMERGE_BREAK + 30.0F, ComingEntity.EMERGE_BREAK + 52.0F) : 1.0F) : 0.0F;
        this.mask.visible = !bare;
        this.pose(1.0F, MASKED);
        this.pose(out, BARE);
        this.body.z += BACK_IN * (1.0F - out);
        this.part[ARM2_R].visible = out > 0.02F;
        this.part[ARM2_L].visible = out > 0.02F;

        // the crown: folded behind the disc under the mask, open round it without
        float spread = 0.0F;
        float curl = 0.0F;
        if (move == ComingEntity.RAIN) {
            spread = 0.35F * Anim.pulse(t, 4.0F, ComingEntity.RAIN_FROM, ComingEntity.RAIN_UNTIL, ComingEntity.RAIN_DURATION);
        } else if (move == ComingEntity.PULL) {
            spread = 0.2F * Anim.pulse(t, 4.0F, ComingEntity.PULL_FROM, ComingEntity.PULL_BITE, ComingEntity.PULL_DURATION);
        } else if (move == ComingEntity.GLARE) {
            curl = Anim.pulse(t, 4.0F, ComingEntity.GLARE_FROM, ComingEntity.GLARE_UNTIL, ComingEntity.GLARE_DURATION);
            spread = -0.3F * curl;
        }
        for (int i = 0; i < CROWN; i++) {
            Anim.pose(this.crown[i], out, 2.75F + spread + Mth.sin(age * 0.05F + i * 1.3F) * 0.08F, 0.0F, 0.35F + Mth.sin(age * 0.03F + i) * 0.1F);
            Anim.pose(this.crownFore[i], out, -3.0F + spread * 1.2F - 0.4F * curl + Mth.sin(age * 0.07F + i * 2.1F) * 0.15F, 0.0F, 0.0F);
            if (!bare) {
                this.crownFore[i].xRot += Mth.sin(age * 0.1F + i * 1.7F) * 0.06F;       // (fingers working at the edge of the mask)
            }
        }

        switch (move) {
            case ComingEntity.GAZE -> {
                float up = Anim.pulse(t, 2.0F, ComingEntity.GAZE_LOOK - 4.0F, ComingEntity.GAZE_DURATION - 24.0F, ComingEntity.GAZE_DURATION);
                this.pose(up, bare ? BARE_GAZE : GAZE);
            }
            case ComingEntity.SLAM -> {
                float impact = ComingEntity.SLAM_IMPACT;
                float back = Anim.pulse(t, 2.0F, impact - 14.0F, impact - 8.0F, impact - 2.0F);
                float down = Anim.pulse(t, impact - 8.0F, impact, ComingEntity.SLAM_LAIN, ComingEntity.SLAM_DURATION - 4.0F);
                this.pose(back, bare ? BARE_SLAM_BACK : SLAM_BACK);
                this.pose(down, bare ? BARE_SLAM : SLAM);
            }
            case ComingEntity.SWEEP -> {
                float impact = ComingEntity.SWEEP_IMPACT;
                float low = Anim.pulse(t, 4.0F, impact - 14.0F, impact + 14.0F, ComingEntity.SWEEP_DURATION);
                float through = Anim.ramp(t, impact - 7.0F, impact + 5.0F);
                this.pose(low, bare ? BARE_SWEEP_BACK : SWEEP_BACK);
                this.pose(low * through, bare ? BARE_SWEEP : SWEEP);
                this.part[NECK1].yRot += (0.25F - 0.5F * through) * low;
            }
            case ComingEntity.CRUSH -> {
                float impact = ComingEntity.CRUSH_IMPACT;
                float up = Anim.pulse(t, 0.0F, impact - 12.0F, impact - 6.0F, impact - 2.0F);
                float down = Anim.pulse(t, impact - 6.0F, impact, impact + 14.0F, ComingEntity.CRUSH_DURATION);
                this.pose(up, bare ? BARE_CRUSH_UP : CRUSH_UP);
                this.pose(down, bare ? BARE_CRUSH : CRUSH);
            }
            case ComingEntity.REACH -> {
                // its hands go into the floor, and stay in it while what it reaches with is up elsewhere
                float sunk = Anim.pulse(t, 2.0F, ComingEntity.REACH_CALL - 4.0F, ComingEntity.REACH_DURATION - 22.0F, ComingEntity.REACH_DURATION);
                this.pose(sunk, bare ? BARE_REACH : REACH);
                this.part[ARM_R].xRot += Mth.sin(t * 0.8F) * 0.05F * sunk;
                this.part[ARM_L].xRot += Mth.sin(t * 0.8F + 1.0F) * 0.05F * sunk;
            }
            case ComingEntity.EMERGE -> {
                float strain = Anim.pulse(t, 6.0F, 20.0F, ComingEntity.EMERGE_BREAK - 2.0F, ComingEntity.EMERGE_BREAK + 2.0F);
                float thrown = Anim.pulse(t, ComingEntity.EMERGE_BREAK, ComingEntity.EMERGE_BREAK + 6.0F, ComingEntity.EMERGE_DURATION - 30.0F, ComingEntity.EMERGE_DURATION - 6.0F);
                // it shakes the mask as a dog shakes a rat, and when it goes throws its head up and comes
                this.pose(strain, SLAM_BACK);
                this.part[HEAD].zRot += Mth.sin(t * 1.7F) * 0.2F * strain;
                this.part[HEAD].xRot += Mth.sin(t * 1.1F) * 0.12F * strain;
                this.part[NECK2].zRot += Mth.sin(t * 1.3F) * 0.1F * strain;
                this.pose(thrown, RAIN);
            }
            case ComingEntity.PULL -> {
                float gape = Anim.pulse(t, 6.0F, ComingEntity.PULL_FROM, ComingEntity.PULL_BITE - 3.0F, ComingEntity.PULL_BITE + 1.0F);
                float bite = Anim.pulse(t, ComingEntity.PULL_BITE - 6.0F, ComingEntity.PULL_BITE, ComingEntity.PULL_BITE + 12.0F, ComingEntity.PULL_DURATION);
                this.pose(gape, PULL);
                this.pose(bite, BITE);
                this.part[NECK1].zRot += Mth.sin(t * 0.9F) * 0.03F * gape;
            }
            case ComingEntity.RAIN -> {
                float up = Anim.pulse(t, 4.0F, ComingEntity.RAIN_FROM, ComingEntity.RAIN_UNTIL, ComingEntity.RAIN_DURATION);
                this.pose(up, RAIN);
            }
            case ComingEntity.GLARE -> {
                // it lies low, out of the way of what looks over it
                float low = Anim.pulse(t, 4.0F, ComingEntity.GLARE_FROM, ComingEntity.GLARE_UNTIL, ComingEntity.GLARE_DURATION);
                this.pose(low, GLARE);
                lid *= 1.0F + 0.3F * low;
            }
            default -> {
            }
        }

        // it looks at whoever it fights with its neck: the breast does not turn. And it sways, as a thing does that hangs out of a hole.
        float look = state.extra;
        this.part[NECK1].yRot += look * 0.5F;
        this.part[NECK2].yRot += look * 0.3F;
        this.part[HEAD].yRot += look * 0.2F;
        this.part[NECK1].zRot += Mth.sin(age * 0.03F) * 0.04F;
        this.part[NECK2].xRot += Mth.sin(age * 0.041F) * 0.04F;
        this.part[NECK2].zRot += Mth.sin(age * 0.027F + 1.0F) * 0.04F;
        this.part[NECK3].xRot += Mth.sin(age * 0.05F + 2.0F) * 0.04F;
        this.part[HEAD].zRot += Mth.sin(age * 0.022F) * 0.06F;
        this.part[ARM_R].zRot += Mth.sin(age * 0.035F) * 0.03F;
        this.part[ARM_L].zRot -= Mth.sin(age * 0.031F + 1.5F) * 0.03F;
        this.part[ARM2_R].zRot += Mth.sin(age * 0.045F) * 0.05F * out;
        this.part[ARM2_L].zRot -= Mth.sin(age * 0.04F + 0.7F) * 0.05F * out;

        // the eye in the crack: shut under the mask; open, it narrows and widens a little, slowly
        this.eye.visible = lid > 0.02F;
        this.eye.xScale = lid * (1.0F + Mth.sin(age * 0.045F) * 0.05F);
    }
}
