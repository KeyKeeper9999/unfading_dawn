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
 * It is a weaving: three necks twisted into a rope, with a great disc on the end of the rope, a ring of light and a
 * crown of arms round the disc; the rope grows out of a knot of the same necks that has a mouth in it, six arms a
 * house long, a tail, two lesser necks, and ends hanging under it. Masked, the knot is in the rock behind it and the
 * rope hangs out of the crack over the hall ({@code turn} is turned to whoever it fights: {@code extra} of the state,
 * see ComingEntity.renderExtra); what else there is of it has crept out along the rock round the crack ({@code roots}).
 * With the mask off all of it is out and walks: the game turns the whole of it, and the walk is four poses gone
 * through by how far it has gone.
 */
public class ComingModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("coming"), "main");
    /** Must match the scale in the generator. */
    private static final float MODEL_SCALE = 2.4F;
    /** CROWN, NECK, TAIL and the rest in tools/gen_coming.py. */
    private static final int CROWN = 12;
    private static final int NECK = 8;
    private static final int TAIL = 4;
    private static final int DANGLE = 6;
    private static final int ROOTS = 6;
    private static final int[] LESSER = {5, 4};
    /** Where in a pose the parts are (the order of POSED in the generator): the knot, the lengths of the rope, the head; the last row is where the body is. */
    private static final int TRUNK = 0;
    private static final int NECK0 = 1;
    private static final int HEAD = NECK0 + NECK;
    private static final int ARM_R = HEAD + 1;
    private static final int LESSER_R = ARM_R + 18;

    // <generated poses>
    private static final String[][] POSED = {{"trunk"}, {"trunk", "neck0"}, {"trunk", "neck0", "neck1"}, {"trunk", "neck0", "neck1", "neck2"}, {"trunk", "neck0", "neck1", "neck2", "neck3"}, {"trunk", "neck0", "neck1", "neck2", "neck3", "neck4"}, {"trunk", "neck0", "neck1", "neck2", "neck3", "neck4", "neck5"}, {"trunk", "neck0", "neck1", "neck2", "neck3", "neck4", "neck5", "neck6"}, {"trunk", "neck0", "neck1", "neck2", "neck3", "neck4", "neck5", "neck6", "neck7"}, {"trunk", "neck0", "neck1", "neck2", "neck3", "neck4", "neck5", "neck6", "neck7", "head"}, {"trunk", "arm_r"}, {"trunk", "arm_r", "fore_r"}, {"trunk", "arm_r", "fore_r", "hand_r"}, {"trunk", "arm_l"}, {"trunk", "arm_l", "fore_l"}, {"trunk", "arm_l", "fore_l", "hand_l"}, {"trunk", "mid_r"}, {"trunk", "mid_r", "midf_r"}, {"trunk", "mid_r", "midf_r", "midh_r"}, {"trunk", "mid_l"}, {"trunk", "mid_l", "midf_l"}, {"trunk", "mid_l", "midf_l", "midh_l"}, {"trunk", "leg_r"}, {"trunk", "leg_r", "shin_r"}, {"trunk", "leg_r", "shin_r", "foot_r"}, {"trunk", "leg_l"}, {"trunk", "leg_l", "shin_l"}, {"trunk", "leg_l", "shin_l", "foot_l"}, {"trunk", "lesser_r0"}, {"trunk", "lesser_l0"}, {"trunk", "maw_r"}, {"trunk", "maw_l"}};
    private static final float[][] MASKED = {{0.000F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.250F, 0.000F, 0.000F}, {0.250F, 0.000F, 0.000F}, {0.350F, 0.000F, 0.000F}, {0.400F, 0.000F, 0.000F}, {0.350F, 0.000F, 0.000F}, {0.200F, 0.000F, 0.000F}, {-0.100F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {2.337F, -0.115F, 1.461F}, {-0.578F, -0.688F, 1.744F}, {1.200F, 2.811F, -0.397F}, {0.805F, -0.115F, 1.681F}, {2.564F, -0.689F, -1.398F}, {1.200F, -2.811F, 0.397F}, {-1.571F, -1.540F, 1.127F}, {-1.393F, 1.127F, 1.540F}, {-0.506F, 1.367F, -2.085F}, {1.571F, -1.602F, -1.127F}, {1.749F, 1.127F, -1.602F}, {-0.505F, -1.367F, 2.084F}, {-1.571F, -1.373F, 0.960F}, {-1.301F, 1.076F, 1.429F}, {1.280F, 0.693F, -0.308F}, {1.571F, -1.769F, -0.960F}, {1.840F, 1.076F, -1.713F}, {1.281F, -0.693F, 0.308F}, {1.000F, 0.000F, -0.500F}, {0.800F, 0.000F, 0.550F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, -12.0F, 36.0F}};
    private static final float[][] GAZE = {{0.000F, 0.000F, 0.000F}, {0.700F, 0.000F, 0.000F}, {0.300F, 0.000F, 0.000F}, {0.300F, 0.000F, 0.000F}, {0.300F, 0.000F, 0.000F}, {0.300F, 0.000F, 0.000F}, {0.200F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {0.200F, 0.000F, 0.000F}, {0.150F, 0.000F, 0.000F}, {2.337F, -0.115F, 1.461F}, {-0.578F, -0.688F, 1.744F}, {1.200F, 2.811F, -0.397F}, {0.805F, -0.115F, 1.681F}, {2.564F, -0.689F, -1.398F}, {1.200F, -2.811F, 0.397F}, {-1.571F, -1.540F, 1.127F}, {-1.393F, 1.127F, 1.540F}, {-0.506F, 1.367F, -2.085F}, {1.571F, -1.602F, -1.127F}, {1.749F, 1.127F, -1.602F}, {-0.505F, -1.367F, 2.084F}, {-1.571F, -1.373F, 0.960F}, {-1.301F, 1.076F, 1.429F}, {1.280F, 0.693F, -0.308F}, {1.571F, -1.769F, -0.960F}, {1.840F, 1.076F, -1.713F}, {1.281F, -0.693F, 0.308F}, {1.000F, 0.000F, -0.500F}, {0.800F, 0.000F, 0.550F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, -12.0F, 36.0F}};
    private static final float[][] SLAM_BACK = {{0.000F, 0.000F, 0.000F}, {0.450F, 0.000F, 0.000F}, {0.050F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {0.200F, 0.000F, 0.000F}, {0.300F, 0.000F, 0.000F}, {0.400F, 0.000F, 0.000F}, {0.400F, 0.000F, 0.000F}, {0.300F, 0.000F, 0.000F}, {-0.200F, 0.000F, 0.000F}, {2.337F, -0.115F, 1.461F}, {-0.578F, -0.688F, 1.744F}, {1.200F, 2.811F, -0.397F}, {0.805F, -0.115F, 1.681F}, {2.564F, -0.689F, -1.398F}, {1.200F, -2.811F, 0.397F}, {-1.571F, -1.540F, 1.127F}, {-1.393F, 1.127F, 1.540F}, {-0.506F, 1.367F, -2.085F}, {1.571F, -1.602F, -1.127F}, {1.749F, 1.127F, -1.602F}, {-0.505F, -1.367F, 2.084F}, {-1.571F, -1.373F, 0.960F}, {-1.301F, 1.076F, 1.429F}, {1.280F, 0.693F, -0.308F}, {1.571F, -1.769F, -0.960F}, {1.840F, 1.076F, -1.713F}, {1.281F, -0.693F, 0.308F}, {1.000F, 0.000F, -0.500F}, {0.800F, 0.000F, 0.550F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, -12.0F, 36.0F}};
    private static final float[][] SLAM = {{0.000F, 0.000F, 0.000F}, {1.600F, 0.000F, 0.000F}, {0.350F, 0.000F, 0.000F}, {0.250F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {-0.100F, 0.000F, 0.000F}, {-0.200F, 0.000F, 0.000F}, {-0.200F, 0.000F, 0.000F}, {-0.150F, 0.000F, 0.000F}, {-0.100F, 0.000F, 0.000F}, {2.337F, -0.115F, 1.461F}, {-0.578F, -0.688F, 1.744F}, {1.200F, 2.811F, -0.397F}, {0.805F, -0.115F, 1.681F}, {2.564F, -0.689F, -1.398F}, {1.200F, -2.811F, 0.397F}, {-1.571F, -1.540F, 1.127F}, {-1.393F, 1.127F, 1.540F}, {-0.506F, 1.367F, -2.085F}, {1.571F, -1.602F, -1.127F}, {1.749F, 1.127F, -1.602F}, {-0.505F, -1.367F, 2.084F}, {-1.571F, -1.373F, 0.960F}, {-1.301F, 1.076F, 1.429F}, {1.280F, 0.693F, -0.308F}, {1.571F, -1.769F, -0.960F}, {1.840F, 1.076F, -1.713F}, {1.281F, -0.693F, 0.308F}, {1.000F, 0.000F, -0.500F}, {0.800F, 0.000F, 0.550F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, -12.0F, 36.0F}};
    private static final float[][] SWEEP_BACK = {{0.000F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.062F}, {0.250F, 0.000F, 0.062F}, {0.250F, 0.000F, 0.062F}, {0.350F, 0.000F, 0.062F}, {0.400F, 0.000F, 0.062F}, {0.350F, 0.000F, 0.062F}, {0.200F, 0.000F, 0.062F}, {-0.100F, 0.000F, 0.062F}, {0.000F, 0.000F, 0.000F}, {2.812F, 0.039F, 1.702F}, {-0.271F, -0.370F, 1.568F}, {1.238F, 2.804F, 0.025F}, {0.805F, -0.115F, 1.681F}, {2.564F, -0.689F, -1.398F}, {1.200F, -2.811F, 0.397F}, {-1.571F, -1.540F, 1.127F}, {-1.393F, 1.127F, 1.540F}, {-0.506F, 1.367F, -2.085F}, {1.571F, -1.602F, -1.127F}, {1.749F, 1.127F, -1.602F}, {-0.505F, -1.367F, 2.084F}, {-1.571F, -1.373F, 0.960F}, {-1.301F, 1.076F, 1.429F}, {1.280F, 0.693F, -0.308F}, {1.571F, -1.769F, -0.960F}, {1.840F, 1.076F, -1.713F}, {1.281F, -0.693F, 0.308F}, {1.000F, 0.000F, -0.500F}, {0.800F, 0.000F, 0.550F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, -12.0F, 36.0F}};
    private static final float[][] SWEEP = {{0.000F, 0.000F, 0.000F}, {0.500F, 0.000F, -0.050F}, {0.250F, 0.000F, -0.050F}, {0.250F, 0.000F, -0.050F}, {0.350F, 0.000F, -0.050F}, {0.400F, 0.000F, -0.050F}, {0.350F, 0.000F, -0.050F}, {0.200F, 0.000F, -0.050F}, {-0.100F, 0.000F, -0.050F}, {0.000F, 0.000F, 0.000F}, {1.287F, -0.542F, 1.351F}, {-2.488F, -1.464F, 2.713F}, {0.810F, 3.059F, -0.743F}, {0.805F, -0.115F, 1.681F}, {2.564F, -0.689F, -1.398F}, {1.200F, -2.811F, 0.397F}, {-1.571F, -1.540F, 1.127F}, {-1.393F, 1.127F, 1.540F}, {-0.506F, 1.367F, -2.085F}, {1.571F, -1.602F, -1.127F}, {1.749F, 1.127F, -1.602F}, {-0.505F, -1.367F, 2.084F}, {-1.571F, -1.373F, 0.960F}, {-1.301F, 1.076F, 1.429F}, {1.280F, 0.693F, -0.308F}, {1.571F, -1.769F, -0.960F}, {1.840F, 1.076F, -1.713F}, {1.281F, -0.693F, 0.308F}, {1.000F, 0.000F, -0.500F}, {0.800F, 0.000F, 0.550F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, -12.0F, 36.0F}};
    private static final float[][] CRUSH_UP = {{0.000F, 0.000F, 0.000F}, {0.300F, 0.000F, 0.000F}, {0.200F, 0.000F, 0.000F}, {0.250F, 0.000F, 0.000F}, {0.350F, 0.000F, 0.000F}, {0.400F, 0.000F, 0.000F}, {0.350F, 0.000F, 0.000F}, {0.200F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {3.439F, 0.206F, 2.567F}, {-1.172F, 0.006F, 2.827F}, {0.145F, 3.370F, -0.488F}, {-0.298F, 0.206F, 0.574F}, {1.970F, 0.006F, -0.314F}, {0.145F, -3.370F, 0.488F}, {-1.571F, -1.540F, 1.127F}, {-1.393F, 1.127F, 1.540F}, {-0.506F, 1.367F, -2.085F}, {1.571F, -1.602F, -1.127F}, {1.749F, 1.127F, -1.602F}, {-0.505F, -1.367F, 2.084F}, {-1.571F, -1.373F, 0.960F}, {-1.301F, 1.076F, 1.429F}, {1.280F, 0.693F, -0.308F}, {1.571F, -1.769F, -0.960F}, {1.840F, 1.076F, -1.713F}, {1.281F, -0.693F, 0.308F}, {1.000F, 0.000F, -0.500F}, {0.800F, 0.000F, 0.550F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, -12.0F, 36.0F}};
    private static final float[][] CRUSH = {{0.000F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.250F, 0.000F, 0.000F}, {0.250F, 0.000F, 0.000F}, {0.350F, 0.000F, 0.000F}, {0.400F, 0.000F, 0.000F}, {0.350F, 0.000F, 0.000F}, {0.200F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {1.991F, -0.253F, 1.366F}, {-0.818F, -1.069F, 1.575F}, {0.990F, 2.899F, -0.425F}, {1.151F, -0.253F, 1.775F}, {2.323F, -1.069F, -1.565F}, {0.991F, -2.899F, 0.425F}, {-1.571F, -1.540F, 1.127F}, {-1.393F, 1.127F, 1.540F}, {-0.506F, 1.367F, -2.085F}, {1.571F, -1.602F, -1.127F}, {1.749F, 1.127F, -1.602F}, {-0.505F, -1.367F, 2.084F}, {-1.571F, -1.373F, 0.960F}, {-1.301F, 1.076F, 1.429F}, {1.280F, 0.693F, -0.308F}, {1.571F, -1.769F, -0.960F}, {1.840F, 1.076F, -1.713F}, {1.281F, -0.693F, 0.308F}, {1.000F, 0.000F, -0.500F}, {0.800F, 0.000F, 0.550F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, -12.0F, 36.0F}};
    private static final float[][] REACH = {{0.000F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.300F, 0.000F, 0.000F}, {0.300F, 0.000F, 0.000F}, {0.350F, 0.000F, 0.000F}, {0.400F, 0.000F, 0.000F}, {0.350F, 0.000F, 0.000F}, {0.200F, 0.000F, 0.000F}, {-0.100F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {2.247F, -0.279F, 1.314F}, {-0.594F, -0.827F, 1.486F}, {1.161F, 2.825F, -0.371F}, {0.894F, -0.279F, 1.828F}, {2.548F, -0.827F, -1.656F}, {1.161F, -2.826F, 0.371F}, {-1.571F, -1.540F, 1.127F}, {-1.393F, 1.127F, 1.540F}, {-0.506F, 1.367F, -2.085F}, {1.571F, -1.602F, -1.127F}, {1.749F, 1.127F, -1.602F}, {-0.505F, -1.367F, 2.084F}, {-1.571F, -1.373F, 0.960F}, {-1.301F, 1.076F, 1.429F}, {1.280F, 0.693F, -0.308F}, {1.571F, -1.769F, -0.960F}, {1.840F, 1.076F, -1.713F}, {1.281F, -0.693F, 0.308F}, {1.000F, 0.000F, -0.500F}, {0.800F, 0.000F, 0.550F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, -12.0F, 36.0F}};
    private static final float[][] EMERGE_GRIP = {{0.000F, 0.000F, 0.000F}, {0.200F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {0.200F, 0.000F, 0.000F}, {0.400F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.400F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {2.296F, -0.194F, 1.387F}, {-0.567F, -0.890F, 1.310F}, {0.978F, 2.876F, -0.190F}, {0.846F, -0.194F, 1.755F}, {2.576F, -0.890F, -1.832F}, {0.979F, -2.875F, 0.190F}, {-1.571F, -1.540F, 1.127F}, {-1.393F, 1.127F, 1.540F}, {-0.506F, 1.367F, -2.085F}, {1.571F, -1.602F, -1.127F}, {1.749F, 1.127F, -1.602F}, {-0.505F, -1.367F, 2.084F}, {-1.571F, -1.373F, 0.960F}, {-1.301F, 1.076F, 1.429F}, {1.280F, 0.693F, -0.308F}, {1.571F, -1.769F, -0.960F}, {1.840F, 1.076F, -1.713F}, {1.281F, -0.693F, 0.308F}, {-0.500F, 0.000F, -0.450F}, {-0.350F, 0.000F, 0.500F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, -12.0F, 36.0F}};
    private static final float[][] BARE = {{0.000F, 0.000F, 0.000F}, {-0.550F, 0.000F, 0.000F}, {-0.150F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {0.350F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {2.746F, 0.177F, 1.880F}, {-0.145F, -0.438F, 1.790F}, {1.299F, 2.616F, -0.031F}, {0.396F, 0.177F, 1.262F}, {2.997F, -0.438F, -1.352F}, {1.299F, -2.616F, 0.031F}, {-2.262F, -1.522F, 0.960F}, {-1.495F, -0.061F, 1.494F}, {-0.982F, 1.477F, -2.223F}, {2.262F, -1.620F, -0.960F}, {1.647F, -0.061F, -1.648F}, {-0.981F, -1.477F, 2.222F}, {-1.886F, -0.946F, 0.792F}, {-1.084F, 0.401F, 1.002F}, {1.171F, 0.674F, -0.118F}, {1.886F, -2.195F, -0.792F}, {2.057F, 0.401F, -2.139F}, {1.171F, -0.675F, 0.118F}, {-0.500F, 0.000F, -0.450F}, {-0.350F, 0.000F, 0.500F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, 0.0F, 0.0F}};
    private static final float[][] BARE_GAZE = {{0.080F, 0.000F, 0.000F}, {-0.300F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {0.300F, 0.000F, 0.000F}, {0.450F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.450F, 0.000F, 0.000F}, {0.400F, 0.000F, 0.000F}, {0.150F, 0.000F, 0.000F}, {2.786F, 0.264F, 1.916F}, {-0.140F, -0.431F, 1.817F}, {1.301F, 2.616F, -0.026F}, {0.356F, 0.264F, 1.226F}, {3.001F, -0.432F, -1.325F}, {1.300F, -2.617F, 0.027F}, {-2.615F, -1.445F, 1.310F}, {-1.490F, -0.062F, 1.490F}, {-0.959F, 1.473F, -2.201F}, {2.615F, -1.696F, -1.310F}, {1.651F, -0.062F, -1.652F}, {-0.954F, -1.473F, 2.196F}, {-1.992F, -0.867F, 0.849F}, {-1.068F, 0.381F, 0.988F}, {1.172F, 0.674F, -0.121F}, {1.992F, -2.274F, -0.849F}, {2.074F, 0.380F, -2.153F}, {1.171F, -0.675F, 0.121F}, {-0.500F, 0.000F, -0.450F}, {-0.350F, 0.000F, 0.500F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, 0.0F, 0.0F}};
    private static final float[][] BARE_SLAM_BACK = {{-0.220F, 0.000F, 0.000F}, {-0.900F, 0.000F, 0.000F}, {-0.200F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.200F, 0.000F, 0.000F}, {0.400F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.400F, 0.000F, 0.000F}, {-0.200F, 0.000F, 0.000F}, {2.658F, -0.071F, 1.801F}, {-0.164F, -0.462F, 1.717F}, {1.287F, 2.617F, -0.044F}, {0.484F, -0.071F, 1.341F}, {2.978F, -0.462F, -1.425F}, {1.287F, -2.617F, 0.044F}, {-2.954F, -1.754F, 1.666F}, {-1.507F, -0.051F, 1.505F}, {-1.068F, 1.486F, -2.307F}, {2.954F, -1.387F, -1.666F}, {1.634F, -0.051F, -1.636F}, {-1.072F, -1.485F, 2.311F}, {-1.467F, -1.138F, 0.501F}, {-1.124F, 0.459F, 1.034F}, {1.160F, 0.673F, -0.114F}, {1.467F, -2.003F, -0.501F}, {2.017F, 0.459F, -2.107F}, {1.161F, -0.673F, 0.113F}, {-0.500F, 0.000F, -0.450F}, {-0.350F, 0.000F, 0.500F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, 0.0F, 0.0F}};
    private static final float[][] BARE_SLAM = {{0.180F, 0.000F, 0.000F}, {-0.100F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.950F, 0.000F, 0.000F}, {0.900F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {-0.300F, 0.000F, 0.000F}, {-0.550F, 0.000F, 0.000F}, {-0.400F, 0.000F, 0.000F}, {-0.100F, 0.000F, 0.000F}, {2.845F, 0.371F, 1.969F}, {-0.135F, -0.425F, 1.850F}, {1.300F, 2.616F, -0.020F}, {0.297F, 0.371F, 1.172F}, {3.006F, -0.425F, -1.291F}, {1.300F, -2.616F, 0.020F}, {-2.712F, -1.345F, 1.403F}, {-1.485F, -0.064F, 1.484F}, {-0.924F, 1.469F, -2.165F}, {2.712F, -1.797F, -1.403F}, {1.657F, -0.064F, -1.657F}, {-0.929F, -1.469F, 2.171F}, {-2.106F, -0.765F, 0.898F}, {-1.044F, 0.354F, 0.968F}, {1.168F, 0.674F, -0.125F}, {2.106F, -2.376F, -0.898F}, {2.097F, 0.354F, -2.173F}, {1.168F, -0.674F, 0.125F}, {-0.500F, 0.000F, -0.450F}, {-0.350F, 0.000F, 0.500F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, 0.0F, 0.0F}};
    private static final float[][] BARE_SWEEP_BACK = {{0.000F, 0.000F, 0.000F}, {-0.550F, 0.000F, 0.050F}, {-0.150F, 0.000F, 0.050F}, {0.100F, 0.000F, 0.050F}, {0.350F, 0.000F, 0.050F}, {0.500F, 0.000F, 0.050F}, {0.600F, 0.000F, 0.050F}, {0.600F, 0.000F, 0.050F}, {0.500F, 0.000F, 0.050F}, {0.000F, 0.000F, 0.000F}, {3.120F, 0.237F, 2.170F}, {0.384F, -0.089F, 1.731F}, {1.712F, 2.704F, 0.492F}, {0.396F, 0.177F, 1.262F}, {2.997F, -0.438F, -1.352F}, {1.299F, -2.616F, 0.031F}, {-2.262F, -1.522F, 0.960F}, {-1.495F, -0.061F, 1.494F}, {-0.982F, 1.477F, -2.223F}, {2.262F, -1.620F, -0.960F}, {1.647F, -0.061F, -1.648F}, {-0.981F, -1.477F, 2.222F}, {-1.886F, -0.946F, 0.792F}, {-1.084F, 0.401F, 1.002F}, {1.171F, 0.674F, -0.118F}, {1.886F, -2.195F, -0.792F}, {2.057F, 0.401F, -2.139F}, {1.171F, -0.675F, 0.118F}, {-0.500F, 0.000F, -0.450F}, {-0.350F, 0.000F, 0.500F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, 0.0F, 0.0F}};
    private static final float[][] BARE_SWEEP = {{0.050F, 0.250F, 0.000F}, {-0.550F, 0.000F, -0.050F}, {-0.150F, 0.000F, -0.050F}, {0.100F, 0.000F, -0.050F}, {0.350F, 0.000F, -0.050F}, {0.500F, 0.000F, -0.050F}, {0.600F, 0.000F, -0.050F}, {0.600F, 0.000F, -0.050F}, {0.500F, 0.000F, -0.050F}, {0.000F, 0.000F, 0.000F}, {0.853F, 0.234F, 1.824F}, {0.690F, -2.499F, 0.155F}, {0.995F, 2.764F, -0.952F}, {0.083F, 0.146F, 1.206F}, {3.063F, -0.391F, -1.395F}, {1.334F, -2.616F, -0.042F}, {-1.013F, -1.844F, -0.293F}, {-1.539F, -0.083F, 1.534F}, {-1.418F, 1.507F, -2.643F}, {1.617F, -1.911F, -0.304F}, {1.699F, -0.074F, -1.696F}, {-0.757F, -1.432F, 1.995F}, {-2.399F, -1.069F, 1.211F}, {-1.101F, 0.323F, 1.017F}, {1.209F, 0.677F, -0.044F}, {1.698F, -2.488F, -0.633F}, {2.113F, 0.427F, -2.188F}, {1.118F, -0.668F, 0.189F}, {-0.500F, 0.000F, -0.450F}, {-0.350F, 0.000F, 0.500F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, 0.0F, 0.0F}};
    private static final float[][] BARE_CRUSH_UP = {{-0.500F, 0.000F, 0.000F}, {-0.200F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.200F, 0.000F, 0.000F}, {0.350F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {3.171F, -0.208F, 2.241F}, {-2.108F, 0.443F, 2.308F}, {-0.729F, 3.744F, -0.611F}, {-0.029F, -0.208F, 0.900F}, {1.033F, 0.442F, -0.834F}, {-0.730F, -3.743F, 0.611F}, {-2.848F, -2.037F, 1.582F}, {-1.522F, -0.033F, 1.518F}, {-1.192F, 1.495F, -2.426F}, {2.848F, -1.104F, -1.582F}, {1.619F, -0.033F, -1.624F}, {-1.187F, -1.495F, 2.421F}, {-0.524F, -1.227F, -0.299F}, {-1.155F, 0.524F, 1.061F}, {1.121F, 0.670F, -0.116F}, {0.524F, -1.914F, 0.299F}, {1.987F, 0.524F, -2.080F}, {1.121F, -0.670F, 0.115F}, {-0.500F, 0.000F, -0.450F}, {-0.350F, 0.000F, 0.500F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, 0.0F, 0.0F}};
    private static final float[][] BARE_CRUSH = {{0.150F, 0.000F, 0.000F}, {-0.450F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.200F, 0.000F, 0.000F}, {0.350F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {2.215F, 0.295F, 1.856F}, {-0.878F, -0.757F, 2.383F}, {1.139F, 2.655F, -0.504F}, {0.927F, 0.295F, 1.285F}, {2.262F, -0.757F, -0.757F}, {1.139F, -2.655F, 0.504F}, {-2.693F, -1.375F, 1.385F}, {-1.486F, -0.063F, 1.486F}, {-0.934F, 1.470F, -2.176F}, {2.693F, -1.767F, -1.385F}, {1.656F, -0.063F, -1.656F}, {-0.932F, -1.470F, 2.174F}, {-2.074F, -0.796F, 0.886F}, {-1.051F, 0.362F, 0.975F}, {1.169F, 0.675F, -0.124F}, {2.074F, -2.345F, -0.886F}, {2.090F, 0.362F, -2.167F}, {1.170F, -0.674F, 0.124F}, {-0.500F, 0.000F, -0.450F}, {-0.350F, 0.000F, 0.500F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, 0.0F, 0.0F}};
    private static final float[][] BARE_REACH = {{0.000F, 0.000F, 0.000F}, {-0.400F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.200F, 0.000F, 0.000F}, {0.400F, 0.000F, 0.000F}, {0.550F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {2.731F, 0.175F, 1.876F}, {-0.152F, -0.422F, 2.004F}, {1.400F, 2.607F, -0.178F}, {0.411F, 0.175F, 1.265F}, {2.990F, -0.422F, -1.137F}, {1.401F, -2.607F, 0.178F}, {-2.239F, -1.530F, 1.105F}, {-1.503F, 0.069F, 1.502F}, {-0.960F, 1.473F, -2.164F}, {2.239F, -1.612F, -1.105F}, {1.639F, 0.069F, -1.640F}, {-0.959F, -1.473F, 2.162F}, {-1.780F, -1.035F, 0.835F}, {-1.136F, 0.499F, 1.046F}, {1.126F, 0.670F, -0.121F}, {1.780F, -2.107F, -0.835F}, {2.005F, 0.499F, -2.096F}, {1.126F, -0.670F, 0.120F}, {-0.500F, 0.000F, -0.450F}, {-0.350F, 0.000F, 0.500F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, 5.0F, 0.0F}};
    private static final float[][] PULL = {{-0.700F, 0.000F, 0.000F}, {0.200F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {0.250F, 0.000F, 0.000F}, {0.400F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.400F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {2.600F, 0.359F, 3.039F}, {-0.056F, -0.395F, 2.287F}, {0.934F, 2.532F, 0.561F}, {0.541F, 0.359F, 0.103F}, {3.086F, -0.394F, -0.855F}, {0.934F, -2.532F, -0.561F}, {-2.815F, -2.240F, 1.567F}, {-1.531F, -0.017F, 1.525F}, {-1.264F, 1.500F, -2.494F}, {2.815F, -0.902F, -1.567F}, {1.611F, -0.017F, -1.616F}, {-1.270F, -1.500F, 2.499F}, {-3.066F, -2.015F, 2.333F}, {-1.162F, 0.564F, 1.070F}, {1.078F, 0.666F, -0.124F}, {3.066F, -1.126F, -2.333F}, {1.979F, 0.564F, -2.072F}, {1.078F, -0.666F, 0.124F}, {-0.500F, 0.000F, -0.450F}, {-0.350F, 0.000F, 0.500F}, {0.000F, 1.350F, 0.000F}, {0.000F, -1.350F, 0.000F}, {0.000F, 0.0F, 0.0F}};
    private static final float[][] BITE = {{0.350F, 0.000F, 0.000F}, {-1.000F, 0.000F, 0.000F}, {-0.200F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {0.350F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {3.237F, 0.552F, 2.464F}, {-0.034F, -0.366F, 2.117F}, {1.327F, 2.617F, 0.102F}, {-0.096F, 0.552F, 0.678F}, {3.108F, -0.365F, -1.025F}, {1.327F, -2.617F, -0.102F}, {-2.410F, -1.139F, 1.326F}, {-1.367F, 0.113F, 1.405F}, {-0.592F, 1.331F, -1.775F}, {2.410F, -2.003F, -1.326F}, {1.775F, 0.113F, -1.737F}, {-0.591F, -1.331F, 1.774F}, {-2.112F, -0.604F, 0.968F}, {-0.941F, 0.412F, 0.890F}, {0.938F, 0.643F, -0.222F}, {2.112F, -2.538F, -0.968F}, {2.200F, 0.412F, -2.252F}, {0.939F, -0.643F, 0.221F}, {-0.500F, 0.000F, -0.450F}, {-0.350F, 0.000F, 0.500F}, {0.000F, 0.200F, 0.000F}, {0.000F, -0.200F, 0.000F}, {0.000F, 8.0F, -8.0F}};
    private static final float[][] RAIN = {{-0.300F, 0.000F, 0.000F}, {0.150F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.050F, 0.000F, 0.000F}, {0.050F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {-0.200F, 0.000F, 0.000F}, {2.645F, -0.071F, 1.948F}, {0.187F, 0.669F, -1.193F}, {1.310F, 2.851F, 3.087F}, {0.497F, -0.071F, 1.194F}, {3.329F, 0.669F, -4.335F}, {1.309F, -2.851F, -3.087F}, {-2.905F, -1.835F, 1.623F}, {-1.512F, -0.046F, 1.509F}, {-1.104F, 1.489F, -2.341F}, {2.905F, -1.307F, -1.623F}, {1.630F, -0.046F, -1.633F}, {-1.101F, -1.489F, 2.339F}, {-1.243F, -1.189F, 0.320F}, {-1.135F, 0.478F, 1.044F}, {1.152F, 0.673F, -0.113F}, {1.243F, -1.952F, -0.320F}, {2.006F, 0.478F, -2.097F}, {1.153F, -0.673F, 0.112F}, {-0.500F, 0.000F, -0.450F}, {-0.350F, 0.000F, 0.500F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, 0.0F, 0.0F}};
    private static final float[][] GLARE = {{0.250F, 0.000F, 0.000F}, {0.200F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.400F, 0.000F, 0.000F}, {0.200F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {-0.200F, 0.000F, 0.000F}, {-0.200F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {3.210F, 0.482F, 2.495F}, {0.074F, -0.337F, 2.155F}, {1.428F, 2.625F, 0.174F}, {-0.069F, 0.482F, 0.647F}, {3.216F, -0.336F, -0.987F}, {1.428F, -2.624F, -0.174F}, {-2.485F, -1.285F, 1.509F}, {-1.492F, 0.177F, 1.498F}, {-0.853F, 1.453F, -2.005F}, {2.485F, -1.857F, -1.509F}, {1.650F, 0.178F, -1.644F}, {-0.850F, -1.453F, 2.004F}, {-1.987F, -0.852F, 1.050F}, {-1.130F, 0.530F, 1.045F}, {1.075F, 0.665F, -0.137F}, {1.987F, -2.289F, -1.050F}, {2.011F, 0.530F, -2.096F}, {1.075F, -0.665F, 0.137F}, {-0.500F, 0.000F, -0.450F}, {-0.350F, 0.000F, 0.500F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, 10.0F, 0.0F}};
    private static final float[][] STAMP_UP = {{0.000F, 0.000F, 0.000F}, {-0.550F, 0.000F, 0.000F}, {-0.150F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {0.350F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {2.718F, 0.069F, 1.631F}, {-0.112F, -0.416F, 1.685F}, {1.415F, 2.609F, -0.124F}, {0.424F, 0.069F, 1.510F}, {3.029F, -0.416F, -1.456F}, {1.414F, -2.608F, 0.124F}, {-2.286F, -1.507F, 0.737F}, {-1.490F, -0.107F, 1.489F}, {-1.093F, 1.486F, -2.534F}, {2.286F, -1.635F, -0.737F}, {1.652F, -0.107F, -1.653F}, {-1.092F, -1.486F, 2.533F}, {-2.040F, -0.814F, 0.707F}, {-1.052F, 0.340F, 0.978F}, {1.328F, 0.691F, -0.158F}, {2.040F, -2.327F, -0.707F}, {2.089F, 0.340F, -2.163F}, {1.329F, -0.692F, 0.158F}, {-0.500F, 0.000F, -0.450F}, {-0.350F, 0.000F, 0.500F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, -9.0F, 0.0F}};
    private static final float[][] STAMP = {{0.060F, 0.000F, 0.000F}, {-0.200F, 0.000F, 0.000F}, {-0.100F, 0.000F, 0.000F}, {0.150F, 0.000F, 0.000F}, {0.400F, 0.000F, 0.000F}, {0.550F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {2.991F, 0.375F, 2.416F}, {-0.185F, -0.440F, 1.903F}, {1.033F, 2.596F, 0.206F}, {0.151F, 0.375F, 0.725F}, {2.957F, -0.441F, -1.238F}, {1.034F, -2.596F, -0.207F}, {-2.284F, -1.478F, 1.439F}, {-1.458F, -0.012F, 1.475F}, {-0.789F, 1.407F, -1.623F}, {2.284F, -1.663F, -1.439F}, {1.683F, -0.012F, -1.666F}, {-0.790F, -1.408F, 1.624F}, {-1.685F, -1.160F, 0.974F}, {-1.104F, 0.434F, 1.018F}, {0.835F, 0.658F, -0.002F}, {1.685F, -1.981F, -0.974F}, {2.037F, 0.434F, -2.123F}, {0.836F, -0.658F, 0.002F}, {-0.500F, 0.000F, -0.450F}, {-0.350F, 0.000F, 0.500F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, 21.0F, 0.0F}};
    private static final float[][] WALK0 = {{0.000F, 0.000F, 0.000F}, {-0.550F, 0.000F, 0.000F}, {-0.150F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {0.350F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {2.596F, 0.138F, 1.802F}, {-0.302F, -0.580F, 1.685F}, {1.130F, 2.625F, -0.087F}, {0.252F, 0.194F, 1.210F}, {3.166F, -0.307F, -1.290F}, {1.462F, -2.617F, -0.031F}, {-1.759F, -1.387F, 0.429F}, {-1.337F, -0.092F, 1.349F}, {-0.574F, 1.335F, -1.814F}, {1.275F, -1.464F, 0.037F}, {1.493F, -0.082F, -1.508F}, {0.287F, -1.668F, 0.941F}, {-1.943F, -1.077F, 0.880F}, {-1.258F, 0.437F, 1.147F}, {1.387F, 0.685F, -0.004F}, {1.882F, -2.357F, -0.712F}, {2.263F, 0.328F, -2.316F}, {0.952F, -0.645F, 0.219F}, {-0.500F, 0.000F, -0.450F}, {-0.350F, 0.000F, 0.500F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, 0.0F, 0.0F}};
    private static final float[][] WALKB = {{0.000F, 0.000F, 0.000F}, {-0.550F, 0.000F, 0.000F}, {-0.150F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {0.350F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {2.746F, 0.177F, 1.880F}, {-0.145F, -0.438F, 1.790F}, {1.299F, 2.616F, -0.031F}, {0.231F, 0.323F, 0.806F}, {2.968F, -0.413F, -1.033F}, {1.177F, -2.616F, -0.035F}, {-2.342F, -1.545F, 1.505F}, {-1.510F, 0.257F, 1.512F}, {-0.901F, 1.462F, -1.997F}, {2.262F, -1.620F, -0.960F}, {1.647F, -0.061F, -1.648F}, {-0.981F, -1.477F, 2.222F}, {-1.886F, -0.946F, 0.792F}, {-1.084F, 0.401F, 1.002F}, {1.171F, 0.674F, -0.118F}, {1.570F, -1.936F, -0.879F}, {1.942F, 0.651F, -2.034F}, {1.007F, -0.659F, 0.120F}, {-0.500F, 0.000F, -0.450F}, {-0.350F, 0.000F, 0.500F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, 0.0F, 0.0F}};
    private static final float[][] WALK1 = {{0.000F, 0.000F, 0.000F}, {-0.550F, 0.000F, 0.000F}, {-0.150F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {0.350F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {2.890F, 0.194F, 1.931F}, {0.025F, -0.306F, 1.851F}, {1.462F, 2.616F, 0.032F}, {0.546F, 0.138F, 1.339F}, {2.839F, -0.580F, -1.456F}, {1.130F, -2.625F, 0.087F}, {-1.275F, -1.677F, -0.037F}, {-1.649F, -0.082F, 1.633F}, {0.285F, 1.668F, -0.943F}, {1.759F, -1.755F, -0.429F}, {1.805F, -0.091F, -1.793F}, {-0.573F, -1.335F, 1.814F}, {-1.882F, -0.784F, 0.712F}, {-0.878F, 0.328F, 0.826F}, {0.952F, 0.646F, -0.219F}, {1.943F, -2.065F, -0.880F}, {1.884F, 0.437F, -1.995F}, {1.387F, -0.685F, 0.004F}, {-0.500F, 0.000F, -0.450F}, {-0.350F, 0.000F, 0.500F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, 0.0F, 0.0F}};
    private static final float[][] WALKA = {{0.000F, 0.000F, 0.000F}, {-0.550F, 0.000F, 0.000F}, {-0.150F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {0.350F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.600F, 0.000F, 0.000F}, {0.500F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {2.910F, 0.323F, 2.336F}, {-0.174F, -0.414F, 2.109F}, {1.177F, 2.616F, 0.035F}, {0.396F, 0.177F, 1.262F}, {2.997F, -0.438F, -1.352F}, {1.299F, -2.616F, 0.031F}, {-2.262F, -1.522F, 0.960F}, {-1.495F, -0.061F, 1.494F}, {-0.982F, 1.477F, -2.223F}, {2.342F, -1.597F, -1.505F}, {1.632F, 0.257F, -1.630F}, {-0.899F, -1.462F, 1.994F}, {-1.570F, -1.206F, 0.879F}, {-1.199F, 0.651F, 1.108F}, {1.006F, 0.659F, -0.121F}, {1.886F, -2.195F, -0.792F}, {2.057F, 0.401F, -2.139F}, {1.171F, -0.675F, 0.118F}, {-0.500F, 0.000F, -0.450F}, {-0.350F, 0.000F, 0.500F}, {0.000F, 0.000F, 0.000F}, {0.000F, -0.000F, 0.000F}, {0.000F, 0.0F, 0.0F}};
    // </generated poses>

    private final ModelPart roots;
    private final ModelPart turn;
    private final ModelPart body;
    private final ModelPart mask;
    private final ModelPart halo;
    private final ModelPart disc;
    private final ModelPart[] part = new ModelPart[POSED.length];
    private final ModelPart[] crown = new ModelPart[CROWN];
    private final ModelPart[] crownFore = new ModelPart[CROWN];
    private final ModelPart[] fray = new ModelPart[NECK];
    private final ModelPart[] frayFore = new ModelPart[NECK];
    private final ModelPart[] dangle = new ModelPart[DANGLE];
    private final ModelPart[] dangleFore = new ModelPart[DANGLE];
    private final ModelPart[] tail = new ModelPart[TAIL];
    private final ModelPart[][] lesser = new ModelPart[2][];
    private final ModelPart[] root = new ModelPart[ROOTS * 2];
    private final ModelPart[] rootFore = new ModelPart[ROOTS * 2];
    /** The pose being put together: a row of three turns for every part of POSED, and where the body is. */
    private final float[][] now = new float[POSED.length + 1][3];
    private final float[][] walk = new float[POSED.length + 1][3];

    public ComingModel(ModelPart root) {
        super(root);
        ModelPart all = root.getChild("all");
        this.roots = all.getChild("roots");
        this.turn = all.getChild("turn");
        this.body = this.turn.getChild("body");
        for (int i = 0; i < POSED.length; i++) {
            ModelPart at = this.body;
            for (String name : POSED[i]) {
                at = at.getChild(name);
            }
            this.part[i] = at;
        }
        this.disc = this.part[HEAD].getChild("disc");
        this.mask = this.disc.getChild("mask");
        this.halo = this.disc.getChild("halo");
        for (int i = 0; i < CROWN; i++) {
            this.crown[i] = this.disc.getChild("crown" + i);
            this.crownFore[i] = this.crown[i].getChild("crownf" + i);
        }
        for (int i = 0; i < NECK; i++) {
            this.fray[i] = this.part[NECK0 + i].getChild("fray" + i);
            this.frayFore[i] = this.fray[i].getChild("frayf" + i);
        }
        for (int i = 0; i < DANGLE; i++) {
            this.dangle[i] = this.part[TRUNK].getChild("dangle" + i);
            this.dangleFore[i] = this.dangle[i].getChild("danglef" + i);
        }
        ModelPart at = this.part[TRUNK];
        for (int i = 0; i < TAIL; i++) {
            at = at.getChild("tail" + i);
            this.tail[i] = at;
        }
        for (int side = 0; side < 2; side++) {
            String name = side == 0 ? "lesser_r" : "lesser_l";
            this.lesser[side] = new ModelPart[LESSER[side]];
            at = this.part[TRUNK];
            for (int i = 0; i < LESSER[side]; i++) {
                at = at.getChild(name + i);
                this.lesser[side][i] = at;
            }
        }
        for (int i = 0; i < ROOTS * 2; i++) {
            String side = i % 2 == 0 ? "r" : "l";
            this.root[i] = this.roots.getChild("root_" + side + (i / 2));
            this.rootFore[i] = this.root[i].getChild("rootf_" + side + (i / 2));
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition roots = all.addOrReplaceChild("roots", CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 8.4F));
        PartDefinition root_r0 = roots.addOrReplaceChild("root_r0", CubeListBuilder.create()
                .texOffs(212, 51).addBox(-1.5F, -18.0F, -1.5F, 3.0F, 18.0F, 3.0F),
                PartPose.offsetAndRotation(-22.0F, -6.0F, 0.0F, 0.0F, 0.0F, -1.95F));
        PartDefinition rootf_r0 = root_r0.addOrReplaceChild("rootf_r0", CubeListBuilder.create()
                .texOffs(502, 23).addBox(-1.0F, -14.0F, -1.0F, 2.0F, 14.0F, 2.0F)
                .texOffs(248, 147).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -18.0F, 0.0F, 0.0F, 0.0F, 0.5F));
        PartDefinition root_l0 = roots.addOrReplaceChild("root_l0", CubeListBuilder.create()
                .texOffs(212, 72).addBox(-1.5F, -18.0F, -1.5F, 3.0F, 18.0F, 3.0F),
                PartPose.offsetAndRotation(22.0F, -6.0F, 0.0F, 0.0F, 0.0F, 1.95F));
        PartDefinition rootf_l0 = root_l0.addOrReplaceChild("rootf_l0", CubeListBuilder.create()
                .texOffs(12, 118).addBox(-1.0F, -14.0F, -1.0F, 2.0F, 14.0F, 2.0F)
                .texOffs(260, 147).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -18.0F, 0.0F, 0.0F, 0.0F, -0.5F));
        PartDefinition root_r1 = roots.addOrReplaceChild("root_r1", CubeListBuilder.create()
                .texOffs(486, 0).addBox(-1.5F, -20.0F, -1.5F, 3.0F, 20.0F, 3.0F),
                PartPose.offsetAndRotation(-23.0F, -25.0F, 0.0F, 0.0F, 0.0F, -1.45F));
        PartDefinition rootf_r1 = root_r1.addOrReplaceChild("rootf_r1", CubeListBuilder.create()
                .texOffs(280, 131).addBox(-1.0F, -14.0F, -1.0F, 2.0F, 14.0F, 2.0F)
                .texOffs(272, 147).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -20.0F, 0.0F, 0.0F, 0.0F, -0.45F));
        PartDefinition root_l1 = roots.addOrReplaceChild("root_l1", CubeListBuilder.create()
                .texOffs(498, 0).addBox(-1.5F, -20.0F, -1.5F, 3.0F, 20.0F, 3.0F),
                PartPose.offsetAndRotation(23.0F, -25.0F, 0.0F, 0.0F, 0.0F, 1.45F));
        PartDefinition rootf_l1 = root_l1.addOrReplaceChild("rootf_l1", CubeListBuilder.create()
                .texOffs(460, 132).addBox(-1.0F, -14.0F, -1.0F, 2.0F, 14.0F, 2.0F)
                .texOffs(492, 147).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -20.0F, 0.0F, 0.0F, 0.0F, 0.45F));
        PartDefinition root_r2 = roots.addOrReplaceChild("root_r2", CubeListBuilder.create()
                .texOffs(446, 76).addBox(-1.5F, -18.0F, -1.5F, 3.0F, 18.0F, 3.0F),
                PartPose.offsetAndRotation(-29.0F, -42.0F, 0.0F, 0.0F, 0.0F, -1.2F));
        PartDefinition rootf_r2 = root_r2.addOrReplaceChild("rootf_r2", CubeListBuilder.create()
                .texOffs(224, 51).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F)
                .texOffs(460, 148).addBox(-2.5F, -23.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -18.0F, 0.0F, 0.0F, 0.0F, 0.5F));
        PartDefinition root_l2 = roots.addOrReplaceChild("root_l2", CubeListBuilder.create()
                .texOffs(458, 76).addBox(-1.5F, -18.0F, -1.5F, 3.0F, 18.0F, 3.0F),
                PartPose.offsetAndRotation(29.0F, -42.0F, 0.0F, 0.0F, 0.0F, 1.2F));
        PartDefinition rootf_l2 = root_l2.addOrReplaceChild("rootf_l2", CubeListBuilder.create()
                .texOffs(104, 57).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F)
                .texOffs(472, 148).addBox(-2.5F, -23.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -18.0F, 0.0F, 0.0F, 0.0F, -0.5F));
        PartDefinition root_r3 = roots.addOrReplaceChild("root_r3", CubeListBuilder.create()
                .texOffs(312, 50).addBox(-1.5F, -20.0F, -1.5F, 3.0F, 20.0F, 3.0F),
                PartPose.offsetAndRotation(-29.0F, -58.0F, 0.0F, 0.0F, 0.0F, -0.85F));
        PartDefinition rootf_r3 = root_r3.addOrReplaceChild("rootf_r3", CubeListBuilder.create()
                .texOffs(468, 132).addBox(-1.0F, -14.0F, -1.0F, 2.0F, 14.0F, 2.0F)
                .texOffs(76, 149).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -20.0F, 0.0F, 0.0F, 0.0F, -0.55F));
        PartDefinition root_l3 = roots.addOrReplaceChild("root_l3", CubeListBuilder.create()
                .texOffs(324, 50).addBox(-1.5F, -20.0F, -1.5F, 3.0F, 20.0F, 3.0F),
                PartPose.offsetAndRotation(29.0F, -58.0F, 0.0F, 0.0F, 0.0F, 0.85F));
        PartDefinition rootf_l3 = root_l3.addOrReplaceChild("rootf_l3", CubeListBuilder.create()
                .texOffs(476, 132).addBox(-1.0F, -14.0F, -1.0F, 2.0F, 14.0F, 2.0F)
                .texOffs(88, 149).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -20.0F, 0.0F, 0.0F, 0.0F, 0.55F));
        PartDefinition root_r4 = roots.addOrReplaceChild("root_r4", CubeListBuilder.create()
                .texOffs(56, 78).addBox(-1.5F, -18.0F, -1.5F, 3.0F, 18.0F, 3.0F),
                PartPose.offsetAndRotation(-16.0F, -73.0F, 0.0F, 0.0F, 0.0F, -0.5F));
        PartDefinition rootf_r4 = root_r4.addOrReplaceChild("rootf_r4", CubeListBuilder.create()
                .texOffs(196, 79).addBox(-1.0F, -12.0F, -1.0F, 2.0F, 12.0F, 2.0F)
                .texOffs(100, 149).addBox(-2.5F, -19.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -18.0F, 0.0F, 0.0F, 0.0F, 0.6F));
        PartDefinition root_l4 = roots.addOrReplaceChild("root_l4", CubeListBuilder.create()
                .texOffs(68, 78).addBox(-1.5F, -18.0F, -1.5F, 3.0F, 18.0F, 3.0F),
                PartPose.offsetAndRotation(16.0F, -73.0F, 0.0F, 0.0F, 0.0F, 0.5F));
        PartDefinition rootf_l4 = root_l4.addOrReplaceChild("rootf_l4", CubeListBuilder.create()
                .texOffs(204, 79).addBox(-1.0F, -12.0F, -1.0F, 2.0F, 12.0F, 2.0F)
                .texOffs(284, 149).addBox(-2.5F, -19.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -18.0F, 0.0F, 0.0F, 0.0F, -0.6F));
        PartDefinition root_r5 = roots.addOrReplaceChild("root_r5", CubeListBuilder.create()
                .texOffs(0, 118).addBox(-1.5F, -14.0F, -1.5F, 3.0F, 14.0F, 3.0F),
                PartPose.offsetAndRotation(-7.0F, -85.0F, 0.0F, 0.0F, 0.0F, -0.12F));
        PartDefinition rootf_r5 = root_r5.addOrReplaceChild("rootf_r5", CubeListBuilder.create()
                .texOffs(256, 99).addBox(-1.0F, -10.0F, -1.0F, 2.0F, 10.0F, 2.0F)
                .texOffs(296, 149).addBox(-2.5F, -17.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -14.0F, 0.0F, 0.0F, 0.0F, -0.5F));
        PartDefinition root_l5 = roots.addOrReplaceChild("root_l5", CubeListBuilder.create()
                .texOffs(112, 130).addBox(-1.5F, -14.0F, -1.5F, 3.0F, 14.0F, 3.0F),
                PartPose.offsetAndRotation(7.0F, -85.0F, 0.0F, 0.0F, 0.0F, 0.12F));
        PartDefinition rootf_l5 = root_l5.addOrReplaceChild("rootf_l5", CubeListBuilder.create()
                .texOffs(264, 99).addBox(-1.0F, -10.0F, -1.0F, 2.0F, 10.0F, 2.0F)
                .texOffs(308, 149).addBox(-2.5F, -17.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -14.0F, 0.0F, 0.0F, 0.0F, 0.5F));
        PartDefinition turn = all.addOrReplaceChild("turn", CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 10.0F));
        PartDefinition body = turn.addOrReplaceChild("body", CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, -10.0F));
        PartDefinition trunk = body.addOrReplaceChild("trunk", CubeListBuilder.create()
                .texOffs(234, 0).addBox(-12.0F, -11.0F, -14.0F, 24.0F, 22.0F, 28.0F)
                .texOffs(234, 50).addBox(-0.5F, -16.0F, -12.0F, 1.0F, 5.0F, 24.0F)
                .texOffs(118, 99).addBox(-8.0F, -2.0F, -15.2F, 16.0F, 12.0F, 1.0F),
                PartPose.offset(0.0F, -34.0F, 0.0F));
        PartDefinition coil0 = trunk.addOrReplaceChild("coil0", CubeListBuilder.create()
                .texOffs(80, 79).addBox(-19.0F, -5.0F, -5.0F, 38.0F, 10.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, -5.0F, 2.0F, 0.5F, 0.3F, 0.9F));
        PartDefinition coil1 = trunk.addOrReplaceChild("coil1", CubeListBuilder.create()
                .texOffs(324, 98).addBox(-18.0F, -4.5F, -4.5F, 36.0F, 9.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, -4.0F, -0.4F, -0.5F, -0.8F));
        PartDefinition coil2 = trunk.addOrReplaceChild("coil2", CubeListBuilder.create()
                .texOffs(20, 99).addBox(-20.0F, -4.5F, -4.5F, 40.0F, 9.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, -9.0F, 6.0F, 0.2F, 1.2F, 0.3F));
        PartDefinition coil3 = trunk.addOrReplaceChild("coil3", CubeListBuilder.create()
                .texOffs(332, 78).addBox(-17.0F, -5.0F, -5.0F, 34.0F, 10.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, 9.0F, -0.9F, 0.4F, 0.2F));
        PartDefinition coil4 = trunk.addOrReplaceChild("coil4", CubeListBuilder.create()
                .texOffs(208, 113).addBox(-17.0F, -4.5F, -4.5F, 34.0F, 9.0F, 9.0F),
                PartPose.offsetAndRotation(9.0F, -1.0F, -3.0F, 0.1F, -0.2F, 1.45F));
        PartDefinition coil5 = trunk.addOrReplaceChild("coil5", CubeListBuilder.create()
                .texOffs(294, 116).addBox(-17.0F, -4.5F, -4.5F, 34.0F, 9.0F, 9.0F),
                PartPose.offsetAndRotation(-9.0F, 0.0F, 3.0F, -0.2F, 0.25F, -1.4F));
        PartDefinition coil6 = trunk.addOrReplaceChild("coil6", CubeListBuilder.create()
                .texOffs(172, 131).addBox(-20.0F, -4.0F, -4.0F, 40.0F, 8.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, 9.0F, -1.0F, 0.15F, 0.1F, 0.12F));
        PartDefinition coil7 = trunk.addOrReplaceChild("coil7", CubeListBuilder.create()
                .texOffs(118, 112).addBox(-18.0F, -4.5F, -4.5F, 36.0F, 9.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, -1.0F, 13.0F, 0.9F, 1.45F, 0.0F));
        PartDefinition maw_r = trunk.addOrReplaceChild("maw_r", CubeListBuilder.create()
                .texOffs(152, 99).addBox(0.0F, 0.0F, -1.0F, 8.0F, 12.0F, 1.0F),
                PartPose.offset(-8.0F, -2.0F, -15.6F));
        PartDefinition maw_l = trunk.addOrReplaceChild("maw_l", CubeListBuilder.create()
                .texOffs(170, 99).addBox(-8.0F, 0.0F, -1.0F, 8.0F, 12.0F, 1.0F),
                PartPose.offset(8.0F, -2.0F, -15.6F));
        PartDefinition dangle0 = trunk.addOrReplaceChild("dangle0", CubeListBuilder.create()
                .texOffs(504, 132).addBox(-1.0F, -13.0F, -1.0F, 2.0F, 13.0F, 2.0F),
                PartPose.offsetAndRotation(-7.0F, 10.0F, -8.0F, 3.0216F, 0.0F, -0.1F));
        PartDefinition danglef0 = dangle0.addOrReplaceChild("danglef0", CubeListBuilder.create()
                .texOffs(188, 99).addBox(-0.75F, -11.0F, -0.75F, 2.0F, 11.0F, 2.0F)
                .texOffs(320, 149).addBox(-2.5F, -18.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -13.0F, 0.0F, 0.25F, 0.0F, 0.0F));
        PartDefinition dangle1 = trunk.addOrReplaceChild("dangle1", CubeListBuilder.create()
                .texOffs(460, 97).addBox(-1.0F, -15.0F, -1.0F, 2.0F, 15.0F, 2.0F),
                PartPose.offsetAndRotation(6.0F, 10.0F, -3.0F, 3.1416F, 0.0F, 0.1F));
        PartDefinition danglef1 = dangle1.addOrReplaceChild("danglef1", CubeListBuilder.create()
                .texOffs(208, 147).addBox(-0.75F, -9.0F, -0.75F, 2.0F, 9.0F, 2.0F)
                .texOffs(332, 149).addBox(-2.5F, -16.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -15.0F, 0.0F, -0.3F, 0.0F, 0.0F));
        PartDefinition dangle2 = trunk.addOrReplaceChild("dangle2", CubeListBuilder.create()
                .texOffs(248, 99).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offsetAndRotation(-3.0F, 10.0F, 4.0F, 3.2616F, 0.0F, -0.1F));
        PartDefinition danglef2 = dangle2.addOrReplaceChild("danglef2", CubeListBuilder.create()
                .texOffs(224, 99).addBox(-0.75F, -12.0F, -0.75F, 2.0F, 12.0F, 2.0F)
                .texOffs(344, 149).addBox(-2.5F, -19.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 0.2F, 0.0F, 0.0F));
        PartDefinition dangle3 = trunk.addOrReplaceChild("dangle3", CubeListBuilder.create()
                .texOffs(484, 132).addBox(-1.0F, -14.0F, -1.0F, 2.0F, 14.0F, 2.0F),
                PartPose.offsetAndRotation(8.0F, 10.0F, 9.0F, 3.0216F, 0.0F, 0.1F));
        PartDefinition danglef3 = dangle3.addOrReplaceChild("danglef3", CubeListBuilder.create()
                .texOffs(224, 69).addBox(-0.75F, -8.0F, -0.75F, 2.0F, 8.0F, 2.0F)
                .texOffs(356, 149).addBox(-2.5F, -15.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -14.0F, 0.0F, 0.35F, 0.0F, 0.0F));
        PartDefinition dangle4 = trunk.addOrReplaceChild("dangle4", CubeListBuilder.create()
                .texOffs(272, 99).addBox(-1.0F, -10.0F, -1.0F, 2.0F, 10.0F, 2.0F),
                PartPose.offsetAndRotation(-8.0F, 10.0F, 10.0F, 3.1416F, 0.0F, -0.1F));
        PartDefinition danglef4 = dangle4.addOrReplaceChild("danglef4", CubeListBuilder.create()
                .texOffs(444, 134).addBox(-0.75F, -10.0F, -0.75F, 2.0F, 10.0F, 2.0F)
                .texOffs(368, 149).addBox(-2.5F, -17.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -0.25F, 0.0F, 0.0F));
        PartDefinition dangle5 = trunk.addOrReplaceChild("dangle5", CubeListBuilder.create()
                .texOffs(216, 147).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 9.0F, 2.0F),
                PartPose.offsetAndRotation(1.0F, 10.0F, -11.0F, 3.2616F, 0.0F, 0.1F));
        PartDefinition danglef5 = dangle5.addOrReplaceChild("danglef5", CubeListBuilder.create()
                .texOffs(224, 147).addBox(-0.75F, -9.0F, -0.75F, 2.0F, 9.0F, 2.0F)
                .texOffs(8, 155).addBox(-2.5F, -16.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -9.0F, 0.0F, 0.3F, 0.0F, 0.0F));
        PartDefinition neck0 = trunk.addOrReplaceChild("neck0", CubeListBuilder.create(),
                PartPose.offset(0.0F, -12.0F, -8.0F));
        PartDefinition ns0_0 = neck0.addOrReplaceChild("ns0_0", CubeListBuilder.create()
                .texOffs(284, 50).addBox(-3.5F, -16.0F, -3.5F, 7.0F, 16.0F, 7.0F),
                PartPose.offsetAndRotation(8.5F, 0.0F, 0.0F, -0.479F, -0.1575F, -0.2933F));
        PartDefinition ns0_1 = neck0.addOrReplaceChild("ns0_1", CubeListBuilder.create()
                .texOffs(0, 57).addBox(-3.5F, -16.0F, -3.5F, 7.0F, 16.0F, 7.0F),
                PartPose.offsetAndRotation(-4.25F, 0.0F, 7.36F, -0.8607F, -2.308F, 0.4387F));
        PartDefinition ns0_2 = neck0.addOrReplaceChild("ns0_2", CubeListBuilder.create()
                .texOffs(28, 57).addBox(-3.5F, -16.0F, -3.5F, 7.0F, 16.0F, 7.0F),
                PartPose.offsetAndRotation(-4.25F, 0.0F, -7.36F, 0.1239F, 2.0282F, 0.7032F));
        PartDefinition fray0 = neck0.addOrReplaceChild("fray0", CubeListBuilder.create()
                .texOffs(452, 134).addBox(-0.75F, -10.0F, -0.75F, 2.0F, 10.0F, 2.0F),
                PartPose.offsetAndRotation(-10.5F, -6.0F, -2.0F, -0.7F, 0.0F, -1.9F));
        PartDefinition frayf0 = fray0.addOrReplaceChild("frayf0", CubeListBuilder.create()
                .texOffs(280, 99).addBox(-0.5F, -9.0F, -0.5F, 1.0F, 9.0F, 1.0F)
                .texOffs(384, 158).addBox(-2.0F, -14.0F, -0.5F, 4.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -0.9F, 0.0F, 0.0F));
        PartDefinition neck1 = neck0.addOrReplaceChild("neck1", CubeListBuilder.create(),
                PartPose.offset(0.0F, -12.0F, 0.0F));
        PartDefinition ns1_0 = neck1.addOrReplaceChild("ns1_0", CubeListBuilder.create()
                .texOffs(56, 57).addBox(-3.0F, -15.0F, -3.0F, 6.0F, 15.0F, 6.0F),
                PartPose.offsetAndRotation(3.78F, 0.0F, 6.59F, 0.1283F, -1.1023F, -0.6528F));
        PartDefinition ns1_1 = neck1.addOrReplaceChild("ns1_1", CubeListBuilder.create()
                .texOffs(80, 57).addBox(-3.0F, -15.0F, -3.0F, 6.0F, 15.0F, 6.0F),
                PartPose.offsetAndRotation(-7.6F, 0.0F, -0.02F, -0.4314F, 3.0057F, 0.2805F));
        PartDefinition ns1_2 = neck1.addOrReplaceChild("ns1_2", CubeListBuilder.create()
                .texOffs(284, 73).addBox(-3.0F, -15.0F, -3.0F, 6.0F, 15.0F, 6.0F),
                PartPose.offsetAndRotation(3.82F, 0.0F, -6.57F, -0.8069F, 0.8583F, -0.433F));
        PartDefinition fray1 = neck1.addOrReplaceChild("fray1", CubeListBuilder.create()
                .texOffs(0, 135).addBox(-0.75F, -10.0F, -0.75F, 2.0F, 10.0F, 2.0F),
                PartPose.offsetAndRotation(9.6F, -6.0F, 1.0F, -0.45F, 0.0F, 1.78F));
        PartDefinition frayf1 = fray1.addOrReplaceChild("frayf1", CubeListBuilder.create()
                .texOffs(112, 117).addBox(-0.5F, -9.0F, -0.5F, 1.0F, 9.0F, 1.0F)
                .texOffs(394, 158).addBox(-2.0F, -14.0F, -0.5F, 4.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -0.9F, 0.0F, 0.0F));
        PartDefinition neck2 = neck1.addOrReplaceChild("neck2", CubeListBuilder.create(),
                PartPose.offset(0.0F, -12.0F, 0.0F));
        PartDefinition ns2_0 = neck2.addOrReplaceChild("ns2_0", CubeListBuilder.create()
                .texOffs(308, 73).addBox(-3.0F, -15.0F, -3.0F, 6.0F, 15.0F, 6.0F),
                PartPose.offsetAndRotation(-3.38F, 0.0F, 5.78F, -0.7465F, -2.2557F, 0.4149F));
        PartDefinition ns2_1 = neck2.addOrReplaceChild("ns2_1", CubeListBuilder.create()
                .texOffs(480, 74).addBox(-3.0F, -15.0F, -3.0F, 6.0F, 15.0F, 6.0F),
                PartPose.offsetAndRotation(-3.32F, 0.0F, -5.82F, 0.1185F, 2.0473F, 0.5896F));
        PartDefinition ns2_2 = neck2.addOrReplaceChild("ns2_2", CubeListBuilder.create()
                .texOffs(422, 76).addBox(-3.0F, -15.0F, -3.0F, 6.0F, 15.0F, 6.0F),
                PartPose.offsetAndRotation(6.7F, 0.0F, 0.04F, -0.3874F, -0.1144F, -0.2596F));
        PartDefinition fray2 = neck2.addOrReplaceChild("fray2", CubeListBuilder.create()
                .texOffs(52, 135).addBox(-0.75F, -10.0F, -0.75F, 2.0F, 10.0F, 2.0F),
                PartPose.offsetAndRotation(-8.7F, -6.0F, 1.0F, -0.2F, 0.0F, -1.66F));
        PartDefinition frayf2 = fray2.addOrReplaceChild("frayf2", CubeListBuilder.create()
                .texOffs(8, 135).addBox(-0.5F, -9.0F, -0.5F, 1.0F, 9.0F, 1.0F)
                .texOffs(404, 158).addBox(-2.0F, -14.0F, -0.5F, 4.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -0.9F, 0.0F, 0.0F));
        PartDefinition neck3 = neck2.addOrReplaceChild("neck3", CubeListBuilder.create(),
                PartPose.offset(0.0F, -12.0F, 0.0F));
        PartDefinition ns3_0 = neck3.addOrReplaceChild("ns3_0", CubeListBuilder.create()
                .texOffs(176, 79).addBox(-2.5F, -15.0F, -2.5F, 5.0F, 15.0F, 5.0F),
                PartPose.offsetAndRotation(-5.9F, 0.0F, -0.05F, -0.347F, 3.0453F, 0.2375F));
        PartDefinition ns3_1 = neck3.addOrReplaceChild("ns3_1", CubeListBuilder.create()
                .texOffs(264, 79).addBox(-2.5F, -15.0F, -2.5F, 5.0F, 15.0F, 5.0F),
                PartPose.offsetAndRotation(2.99F, 0.0F, -5.08F, -0.6856F, 0.9104F, -0.3917F));
        PartDefinition ns3_2 = neck3.addOrReplaceChild("ns3_2", CubeListBuilder.create()
                .texOffs(0, 80).addBox(-2.5F, -15.0F, -2.5F, 5.0F, 15.0F, 5.0F),
                PartPose.offsetAndRotation(2.91F, 0.0F, 5.13F, 0.1077F, -1.0884F, -0.5292F));
        PartDefinition fray3 = neck3.addOrReplaceChild("fray3", CubeListBuilder.create()
                .texOffs(60, 135).addBox(-0.75F, -10.0F, -0.75F, 2.0F, 10.0F, 2.0F),
                PartPose.offsetAndRotation(7.9F, -6.0F, -2.0F, -0.7F, 0.0F, 1.54F));
        PartDefinition frayf3 = fray3.addOrReplaceChild("frayf3", CubeListBuilder.create()
                .texOffs(8, 145).addBox(-0.5F, -9.0F, -0.5F, 1.0F, 9.0F, 1.0F)
                .texOffs(414, 158).addBox(-2.0F, -14.0F, -0.5F, 4.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -0.9F, 0.0F, 0.0F));
        PartDefinition neck4 = neck3.addOrReplaceChild("neck4", CubeListBuilder.create(),
                PartPose.offset(0.0F, -12.0F, 0.0F));
        PartDefinition ns4_0 = neck4.addOrReplaceChild("ns4_0", CubeListBuilder.create()
                .texOffs(20, 80).addBox(-2.5F, -14.0F, -2.5F, 5.0F, 14.0F, 5.0F),
                PartPose.offsetAndRotation(-2.55F, 0.0F, -4.53F, 0.096F, 2.0571F, 0.4729F));
        PartDefinition ns4_1 = neck4.addOrReplaceChild("ns4_1", CubeListBuilder.create()
                .texOffs(196, 93).addBox(-2.5F, -14.0F, -2.5F, 5.0F, 14.0F, 5.0F),
                PartPose.offsetAndRotation(5.2F, 0.0F, 0.06F, -0.311F, -0.0818F, -0.2153F));
        PartDefinition ns4_2 = neck4.addOrReplaceChild("ns4_2", CubeListBuilder.create()
                .texOffs(284, 94).addBox(-2.5F, -14.0F, -2.5F, 5.0F, 14.0F, 5.0F),
                PartPose.offsetAndRotation(-2.65F, 0.0F, 4.47F, -0.6261F, -2.2104F, 0.3647F));
        PartDefinition fray4 = neck4.addOrReplaceChild("fray4", CubeListBuilder.create()
                .texOffs(68, 135).addBox(-0.75F, -10.0F, -0.75F, 2.0F, 10.0F, 2.0F),
                PartPose.offsetAndRotation(-7.2F, -6.0F, 1.0F, -0.45F, 0.0F, -1.42F));
        PartDefinition frayf4 = fray4.addOrReplaceChild("frayf4", CubeListBuilder.create()
                .texOffs(232, 147).addBox(-0.5F, -9.0F, -0.5F, 1.0F, 9.0F, 1.0F)
                .texOffs(424, 158).addBox(-2.0F, -14.0F, -0.5F, 4.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -0.9F, 0.0F, 0.0F));
        PartDefinition neck5 = neck4.addOrReplaceChild("neck5", CubeListBuilder.create(),
                PartPose.offset(0.0F, -12.0F, 0.0F));
        PartDefinition ns5_0 = neck5.addOrReplaceChild("ns5_0", CubeListBuilder.create()
                .texOffs(304, 94).addBox(-2.5F, -14.0F, -2.5F, 5.0F, 14.0F, 5.0F),
                PartPose.offsetAndRotation(2.36F, 0.0F, -3.95F, -0.5694F, 0.947F, -0.343F));
        PartDefinition ns5_1 = neck5.addOrReplaceChild("ns5_1", CubeListBuilder.create()
                .texOffs(470, 95).addBox(-2.5F, -14.0F, -2.5F, 5.0F, 14.0F, 5.0F),
                PartPose.offsetAndRotation(2.24F, 0.0F, 4.02F, 0.0976F, -1.0805F, -0.4304F));
        PartDefinition ns5_2 = neck5.addOrReplaceChild("ns5_2", CubeListBuilder.create()
                .texOffs(490, 95).addBox(-2.5F, -14.0F, -2.5F, 5.0F, 14.0F, 5.0F),
                PartPose.offsetAndRotation(-4.6F, 0.0F, -0.06F, -0.2731F, 3.071F, 0.1982F));
        PartDefinition fray5 = neck5.addOrReplaceChild("fray5", CubeListBuilder.create()
                .texOffs(444, 146).addBox(-0.75F, -10.0F, -0.75F, 2.0F, 10.0F, 2.0F),
                PartPose.offsetAndRotation(6.6F, -6.0F, 1.0F, -0.2F, 0.0F, 1.3F));
        PartDefinition frayf5 = fray5.addOrReplaceChild("frayf5", CubeListBuilder.create()
                .texOffs(236, 147).addBox(-0.5F, -9.0F, -0.5F, 1.0F, 9.0F, 1.0F)
                .texOffs(434, 158).addBox(-2.0F, -14.0F, -0.5F, 4.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -0.9F, 0.0F, 0.0F));
        PartDefinition neck6 = neck5.addOrReplaceChild("neck6", CubeListBuilder.create(),
                PartPose.offset(0.0F, -12.0F, 0.0F));
        PartDefinition ns6_0 = neck6.addOrReplaceChild("ns6_0", CubeListBuilder.create()
                .texOffs(40, 80).addBox(-2.0F, -14.0F, -2.0F, 4.0F, 14.0F, 4.0F),
                PartPose.offsetAndRotation(4.0F, 0.0F, 0.07F, -0.2479F, -0.0607F, -0.1709F));
        PartDefinition ns6_1 = neck6.addOrReplaceChild("ns6_1", CubeListBuilder.create()
                .texOffs(460, 114).addBox(-2.0F, -14.0F, -2.0F, 4.0F, 14.0F, 4.0F),
                PartPose.offsetAndRotation(-2.06F, 0.0F, 3.43F, -0.5091F, -2.1776F, 0.3022F));
        PartDefinition ns6_2 = neck6.addOrReplaceChild("ns6_2", CubeListBuilder.create()
                .texOffs(476, 114).addBox(-2.0F, -14.0F, -2.0F, 4.0F, 14.0F, 4.0F),
                PartPose.offsetAndRotation(-1.94F, 0.0F, -3.5F, 0.0701F, 2.0611F, 0.3687F));
        PartDefinition fray6 = neck6.addOrReplaceChild("fray6", CubeListBuilder.create()
                .texOffs(452, 146).addBox(-0.75F, -10.0F, -0.75F, 2.0F, 10.0F, 2.0F),
                PartPose.offsetAndRotation(-6.0F, -6.0F, -2.0F, -0.7F, 0.0F, -1.18F));
        PartDefinition frayf6 = fray6.addOrReplaceChild("frayf6", CubeListBuilder.create()
                .texOffs(240, 147).addBox(-0.5F, -9.0F, -0.5F, 1.0F, 9.0F, 1.0F)
                .texOffs(444, 158).addBox(-2.0F, -14.0F, -0.5F, 4.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -0.9F, 0.0F, 0.0F));
        PartDefinition neck7 = neck6.addOrReplaceChild("neck7", CubeListBuilder.create(),
                PartPose.offset(0.0F, -12.0F, 0.0F));
        PartDefinition ns7_0 = neck7.addOrReplaceChild("ns7_0", CubeListBuilder.create()
                .texOffs(492, 114).addBox(-2.0F, -14.0F, -2.0F, 4.0F, 14.0F, 4.0F),
                PartPose.offsetAndRotation(1.74F, 0.0F, 3.15F, 0.0566F, -1.081F, -0.33F));
        PartDefinition ns7_1 = neck7.addOrReplaceChild("ns7_1", CubeListBuilder.create()
                .texOffs(380, 116).addBox(-2.0F, -14.0F, -2.0F, 4.0F, 14.0F, 4.0F),
                PartPose.offsetAndRotation(-3.6F, 0.0F, -0.07F, -0.2284F, 3.0858F, 0.1533F));
        PartDefinition ns7_2 = neck7.addOrReplaceChild("ns7_2", CubeListBuilder.create()
                .texOffs(396, 116).addBox(-2.0F, -14.0F, -2.0F, 4.0F, 14.0F, 4.0F),
                PartPose.offsetAndRotation(1.86F, 0.0F, -3.08F, -0.4656F, 0.9732F, -0.2744F));
        PartDefinition fray7 = neck7.addOrReplaceChild("fray7", CubeListBuilder.create()
                .texOffs(0, 147).addBox(-0.75F, -10.0F, -0.75F, 2.0F, 10.0F, 2.0F),
                PartPose.offsetAndRotation(5.6F, -6.0F, 1.0F, -0.45F, 0.0F, 1.06F));
        PartDefinition frayf7 = fray7.addOrReplaceChild("frayf7", CubeListBuilder.create()
                .texOffs(244, 147).addBox(-0.5F, -9.0F, -0.5F, 1.0F, 9.0F, 1.0F)
                .texOffs(20, 159).addBox(-2.0F, -14.0F, -0.5F, 4.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -0.9F, 0.0F, 0.0F));
        PartDefinition head = neck7.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(224, 79).addBox(-6.0F, -6.0F, -3.0F, 12.0F, 12.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, -1.5708F, 0.0F, 0.0F));
        PartDefinition disc = head.addOrReplaceChild("disc", CubeListBuilder.create()
                .texOffs(338, 0).addBox(-20.0F, -20.0F, -3.0F, 40.0F, 40.0F, 3.0F),
                PartPose.offset(0.0F, 0.0F, -4.0F));
        PartDefinition halo = disc.addOrReplaceChild("halo", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-28.0F, -28.0F, 0.0F, 56.0F, 56.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, -0.5F));
        PartDefinition mask = disc.addOrReplaceChild("mask", CubeListBuilder.create()
                .texOffs(424, 0).addBox(-15.0F, -20.0F, -1.5F, 30.0F, 38.0F, 1.0F)
                .texOffs(114, 55).addBox(-24.0F, -39.0F, -1.0F, 48.0F, 23.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, -3.0F));
        PartDefinition crown0 = disc.addOrReplaceChild("crown0", CubeListBuilder.create()
                .texOffs(504, 74).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(4.66F, -17.39F, -1.0F, -2.5F, 0.0F, 0.2618F));
        PartDefinition crownf0 = crown0.addOrReplaceChild("crownf0", CubeListBuilder.create()
                .texOffs(508, 39).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(248, 156).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown1 = disc.addOrReplaceChild("crown1", CubeListBuilder.create()
                .texOffs(470, 76).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(12.73F, -12.73F, -1.0F, -2.5F, 0.0F, 0.7854F));
        PartDefinition crownf1 = crown1.addOrReplaceChild("crownf1", CubeListBuilder.create()
                .texOffs(508, 54).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(260, 156).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown2 = disc.addOrReplaceChild("crown2", CubeListBuilder.create()
                .texOffs(216, 93).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(17.39F, -4.66F, -1.0F, -2.5F, 0.0F, 1.309F));
        PartDefinition crownf2 = crown2.addOrReplaceChild("crownf2", CubeListBuilder.create()
                .texOffs(414, 98).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(272, 156).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown3 = disc.addOrReplaceChild("crown3", CubeListBuilder.create()
                .texOffs(444, 116).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(17.39F, 4.66F, -1.0F, -2.5F, 0.0F, 1.8326F));
        PartDefinition crownf3 = crown3.addOrReplaceChild("crownf3", CubeListBuilder.create()
                .texOffs(508, 114).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(484, 156).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown4 = disc.addOrReplaceChild("crown4", CubeListBuilder.create()
                .texOffs(452, 116).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(12.73F, 12.73F, -1.0F, -2.5F, 0.0F, 2.3562F));
        PartDefinition crownf4 = crown4.addOrReplaceChild("crownf4", CubeListBuilder.create()
                .texOffs(108, 134).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(496, 156).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown5 = disc.addOrReplaceChild("crown5", CubeListBuilder.create()
                .texOffs(20, 117).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(4.66F, 17.39F, -1.0F, -2.5F, 0.0F, 2.8798F));
        PartDefinition crownf5 = crown5.addOrReplaceChild("crownf5", CubeListBuilder.create()
                .texOffs(352, 134).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(232, 157).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown6 = disc.addOrReplaceChild("crown6", CubeListBuilder.create()
                .texOffs(28, 117).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(-4.66F, 17.39F, -1.0F, -2.5F, 0.0F, 3.4034F));
        PartDefinition crownf6 = crown6.addOrReplaceChild("crownf6", CubeListBuilder.create()
                .texOffs(356, 134).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(460, 157).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown7 = disc.addOrReplaceChild("crown7", CubeListBuilder.create()
                .texOffs(36, 117).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(-12.73F, 12.73F, -1.0F, -2.5F, 0.0F, 3.927F));
        PartDefinition crownf7 = crown7.addOrReplaceChild("crownf7", CubeListBuilder.create()
                .texOffs(360, 134).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(472, 157).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown8 = disc.addOrReplaceChild("crown8", CubeListBuilder.create()
                .texOffs(44, 117).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(-17.39F, 4.66F, -1.0F, -2.5F, 0.0F, 4.4506F));
        PartDefinition crownf8 = crown8.addOrReplaceChild("crownf8", CubeListBuilder.create()
                .texOffs(364, 134).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(76, 158).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown9 = disc.addOrReplaceChild("crown9", CubeListBuilder.create()
                .texOffs(52, 117).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(-17.39F, -4.66F, -1.0F, -2.5F, 0.0F, 4.9742F));
        PartDefinition crownf9 = crown9.addOrReplaceChild("crownf9", CubeListBuilder.create()
                .texOffs(368, 134).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(88, 158).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown10 = disc.addOrReplaceChild("crown10", CubeListBuilder.create()
                .texOffs(60, 117).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(-12.73F, -12.73F, -1.0F, -2.5F, 0.0F, 5.4978F));
        PartDefinition crownf10 = crown10.addOrReplaceChild("crownf10", CubeListBuilder.create()
                .texOffs(372, 134).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(100, 158).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown11 = disc.addOrReplaceChild("crown11", CubeListBuilder.create()
                .texOffs(68, 117).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(-4.66F, -17.39F, -1.0F, -2.5F, 0.0F, 6.0214F));
        PartDefinition crownf11 = crown11.addOrReplaceChild("crownf11", CubeListBuilder.create()
                .texOffs(376, 134).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(208, 158).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition arm_r = trunk.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(424, 39).addBox(-3.5F, -30.0F, -3.5F, 7.0F, 30.0F, 7.0F),
                PartPose.offset(-13.0F, -6.0F, -11.0F));
        PartDefinition fore_r = arm_r.addOrReplaceChild("fore_r", CubeListBuilder.create()
                .texOffs(114, 0).addBox(-2.5F, -50.0F, -2.5F, 5.0F, 50.0F, 5.0F),
                PartPose.offset(0.0F, -30.0F, 0.0F));
        PartDefinition hand_r = fore_r.addOrReplaceChild("hand_r", CubeListBuilder.create()
                .texOffs(380, 134).addBox(-6.0F, -8.0F, -2.0F, 12.0F, 8.0F, 4.0F)
                .texOffs(52, 147).addBox(-6.0F, -18.0F, -1.0F, 2.0F, 10.0F, 2.0F)
                .texOffs(12, 134).addBox(-2.7F, -21.0F, -1.0F, 2.0F, 13.0F, 2.0F)
                .texOffs(76, 134).addBox(0.7F, -21.0F, -1.0F, 2.0F, 13.0F, 2.0F)
                .texOffs(60, 147).addBox(4.0F, -18.0F, -1.0F, 2.0F, 10.0F, 2.0F)
                .texOffs(194, 51).addBox(-9.0F, -6.0F, -1.0F, 3.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -50.0F, 0.0F));
        PartDefinition arm_l = trunk.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(452, 39).addBox(-3.5F, -30.0F, -3.5F, 7.0F, 30.0F, 7.0F),
                PartPose.offset(13.0F, -6.0F, -11.0F));
        PartDefinition fore_l = arm_l.addOrReplaceChild("fore_l", CubeListBuilder.create()
                .texOffs(134, 0).addBox(-2.5F, -50.0F, -2.5F, 5.0F, 50.0F, 5.0F),
                PartPose.offset(0.0F, -30.0F, 0.0F));
        PartDefinition hand_l = fore_l.addOrReplaceChild("hand_l", CubeListBuilder.create()
                .texOffs(412, 134).addBox(-6.0F, -8.0F, -2.0F, 12.0F, 8.0F, 4.0F)
                .texOffs(68, 147).addBox(4.0F, -18.0F, -1.0F, 2.0F, 10.0F, 2.0F)
                .texOffs(84, 134).addBox(0.7F, -21.0F, -1.0F, 2.0F, 13.0F, 2.0F)
                .texOffs(92, 134).addBox(-2.7F, -21.0F, -1.0F, 2.0F, 13.0F, 2.0F)
                .texOffs(112, 147).addBox(-6.0F, -18.0F, -1.0F, 2.0F, 10.0F, 2.0F)
                .texOffs(104, 75).addBox(6.0F, -6.0F, -1.0F, 3.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -50.0F, 0.0F));
        PartDefinition mid_r = trunk.addOrReplaceChild("mid_r", CubeListBuilder.create()
                .texOffs(480, 39).addBox(-3.5F, -28.0F, -3.5F, 7.0F, 28.0F, 7.0F),
                PartPose.offset(-14.0F, -4.0F, 1.0F));
        PartDefinition midf_r = mid_r.addOrReplaceChild("midf_r", CubeListBuilder.create()
                .texOffs(194, 0).addBox(-2.5F, -46.0F, -2.5F, 5.0F, 46.0F, 5.0F),
                PartPose.offset(0.0F, -28.0F, 0.0F));
        PartDefinition midh_r = midf_r.addOrReplaceChild("midh_r", CubeListBuilder.create()
                .texOffs(20, 135).addBox(-6.0F, -8.0F, -2.0F, 12.0F, 8.0F, 4.0F)
                .texOffs(120, 147).addBox(-6.0F, -18.0F, -1.0F, 2.0F, 10.0F, 2.0F)
                .texOffs(100, 134).addBox(-2.7F, -21.0F, -1.0F, 2.0F, 13.0F, 2.0F)
                .texOffs(288, 134).addBox(0.7F, -21.0F, -1.0F, 2.0F, 13.0F, 2.0F)
                .texOffs(128, 147).addBox(4.0F, -18.0F, -1.0F, 2.0F, 10.0F, 2.0F)
                .texOffs(60, 159).addBox(-9.0F, -6.0F, -1.0F, 3.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -46.0F, 0.0F));
        PartDefinition mid_l = trunk.addOrReplaceChild("mid_l", CubeListBuilder.create()
                .texOffs(338, 43).addBox(-3.5F, -28.0F, -3.5F, 7.0F, 28.0F, 7.0F),
                PartPose.offset(14.0F, -4.0F, 1.0F));
        PartDefinition midf_l = mid_l.addOrReplaceChild("midf_l", CubeListBuilder.create()
                .texOffs(214, 0).addBox(-2.5F, -46.0F, -2.5F, 5.0F, 46.0F, 5.0F),
                PartPose.offset(0.0F, -28.0F, 0.0F));
        PartDefinition midh_l = midf_l.addOrReplaceChild("midh_l", CubeListBuilder.create()
                .texOffs(380, 146).addBox(-6.0F, -8.0F, -2.0F, 12.0F, 8.0F, 4.0F)
                .texOffs(136, 147).addBox(4.0F, -18.0F, -1.0F, 2.0F, 10.0F, 2.0F)
                .texOffs(296, 134).addBox(0.7F, -21.0F, -1.0F, 2.0F, 13.0F, 2.0F)
                .texOffs(304, 134).addBox(-2.7F, -21.0F, -1.0F, 2.0F, 13.0F, 2.0F)
                .texOffs(144, 147).addBox(-6.0F, -18.0F, -1.0F, 2.0F, 10.0F, 2.0F)
                .texOffs(112, 159).addBox(6.0F, -6.0F, -1.0F, 3.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -46.0F, 0.0F));
        PartDefinition leg_r = trunk.addOrReplaceChild("leg_r", CubeListBuilder.create()
                .texOffs(366, 43).addBox(-3.5F, -28.0F, -3.5F, 7.0F, 28.0F, 7.0F),
                PartPose.offset(-12.0F, -4.0F, 12.0F));
        PartDefinition shin_r = leg_r.addOrReplaceChild("shin_r", CubeListBuilder.create()
                .texOffs(154, 0).addBox(-2.5F, -48.0F, -2.5F, 5.0F, 48.0F, 5.0F),
                PartPose.offset(0.0F, -28.0F, 0.0F));
        PartDefinition foot_r = shin_r.addOrReplaceChild("foot_r", CubeListBuilder.create()
                .texOffs(412, 146).addBox(-6.0F, -8.0F, -2.0F, 12.0F, 8.0F, 4.0F)
                .texOffs(152, 147).addBox(-6.0F, -18.0F, -1.0F, 2.0F, 10.0F, 2.0F)
                .texOffs(312, 134).addBox(-2.7F, -21.0F, -1.0F, 2.0F, 13.0F, 2.0F)
                .texOffs(320, 134).addBox(0.7F, -21.0F, -1.0F, 2.0F, 13.0F, 2.0F)
                .texOffs(160, 147).addBox(4.0F, -18.0F, -1.0F, 2.0F, 10.0F, 2.0F)
                .texOffs(122, 159).addBox(-9.0F, -6.0F, -1.0F, 3.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -48.0F, 0.0F));
        PartDefinition leg_l = trunk.addOrReplaceChild("leg_l", CubeListBuilder.create()
                .texOffs(394, 43).addBox(-3.5F, -28.0F, -3.5F, 7.0F, 28.0F, 7.0F),
                PartPose.offset(12.0F, -4.0F, 12.0F));
        PartDefinition shin_l = leg_l.addOrReplaceChild("shin_l", CubeListBuilder.create()
                .texOffs(174, 0).addBox(-2.5F, -48.0F, -2.5F, 5.0F, 48.0F, 5.0F),
                PartPose.offset(0.0F, -28.0F, 0.0F));
        PartDefinition foot_l = shin_l.addOrReplaceChild("foot_l", CubeListBuilder.create()
                .texOffs(20, 147).addBox(-6.0F, -8.0F, -2.0F, 12.0F, 8.0F, 4.0F)
                .texOffs(168, 147).addBox(4.0F, -18.0F, -1.0F, 2.0F, 10.0F, 2.0F)
                .texOffs(328, 134).addBox(0.7F, -21.0F, -1.0F, 2.0F, 13.0F, 2.0F)
                .texOffs(336, 134).addBox(-2.7F, -21.0F, -1.0F, 2.0F, 13.0F, 2.0F)
                .texOffs(176, 147).addBox(-6.0F, -18.0F, -1.0F, 2.0F, 10.0F, 2.0F)
                .texOffs(132, 159).addBox(6.0F, -6.0F, -1.0F, 3.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -48.0F, 0.0F));
        PartDefinition tail0 = trunk.addOrReplaceChild("tail0", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 3.0F, 12.0F, -1.98F, 0.0F, 0.0F));
        PartDefinition ts0_0 = tail0.addOrReplaceChild("ts0_0", CubeListBuilder.create()
                .texOffs(420, 97).addBox(-2.5F, -14.0F, -2.5F, 5.0F, 14.0F, 5.0F),
                PartPose.offsetAndRotation(4.0F, 0.0F, 0.0F, -0.2441F, -0.0612F, -0.241F));
        PartDefinition ts0_1 = tail0.addOrReplaceChild("ts0_1", CubeListBuilder.create()
                .texOffs(440, 97).addBox(-2.5F, -14.0F, -2.5F, 5.0F, 14.0F, 5.0F),
                PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, -0.2441F, 3.0803F, 0.241F));
        PartDefinition tail1 = tail0.addOrReplaceChild("tail1", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, -0.44F, 0.0F, 0.0F));
        PartDefinition ts1_0 = tail1.addOrReplaceChild("ts1_0", CubeListBuilder.create()
                .texOffs(412, 116).addBox(-2.0F, -14.0F, -2.0F, 4.0F, 14.0F, 4.0F),
                PartPose.offsetAndRotation(0.86F, 0.0F, 3.08F, 0.5127F, -1.2646F, -0.7307F));
        PartDefinition ts1_1 = tail1.addOrReplaceChild("ts1_1", CubeListBuilder.create()
                .texOffs(428, 116).addBox(-2.0F, -14.0F, -2.0F, 4.0F, 14.0F, 4.0F),
                PartPose.offsetAndRotation(-0.86F, 0.0F, -3.08F, 0.5127F, 1.877F, 0.7307F));
        PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.33F, 0.0F, 0.0F));
        PartDefinition ts2_0 = tail2.addOrReplaceChild("ts2_0", CubeListBuilder.create()
                .texOffs(124, 130).addBox(-1.5F, -14.0F, -1.5F, 3.0F, 14.0F, 3.0F),
                PartPose.offsetAndRotation(-2.06F, 0.0F, 1.24F, -0.2334F, -2.6295F, 0.1784F));
        PartDefinition ts2_1 = tail2.addOrReplaceChild("ts2_1", CubeListBuilder.create()
                .texOffs(136, 130).addBox(-1.5F, -14.0F, -1.5F, 3.0F, 14.0F, 3.0F),
                PartPose.offsetAndRotation(2.06F, 0.0F, -1.24F, -0.2334F, 0.5121F, -0.1784F));
        PartDefinition tail3 = tail2.addOrReplaceChild("tail3", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.47F, 0.0F, 0.0F));
        PartDefinition ts3_0 = tail3.addOrReplaceChild("ts3_0", CubeListBuilder.create()
                .texOffs(148, 130).addBox(-1.5F, -14.0F, -1.5F, 3.0F, 14.0F, 3.0F),
                PartPose.offsetAndRotation(-1.31F, 0.0F, -1.24F, 0.0001F, 2.3766F, 0.1622F));
        PartDefinition ts3_1 = tail3.addOrReplaceChild("ts3_1", CubeListBuilder.create()
                .texOffs(160, 130).addBox(-1.5F, -14.0F, -1.5F, 3.0F, 14.0F, 3.0F),
                PartPose.offsetAndRotation(1.31F, 0.0F, 1.24F, 0.0001F, -0.765F, -0.1622F));
        PartDefinition tailend0 = tail3.addOrReplaceChild("tailend0", CubeListBuilder.create()
                .texOffs(184, 147).addBox(-0.75F, -10.0F, -0.75F, 2.0F, 10.0F, 2.0F)
                .texOffs(30, 159).addBox(-2.0F, -15.0F, -0.5F, 4.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.3F, 0.0F, 0.5F));
        PartDefinition tailend1 = tail3.addOrReplaceChild("tailend1", CubeListBuilder.create()
                .texOffs(192, 147).addBox(-0.75F, -10.0F, -0.75F, 2.0F, 10.0F, 2.0F)
                .texOffs(40, 159).addBox(-2.0F, -15.0F, -0.5F, 4.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, -0.2F, 0.0F, -0.45F));
        PartDefinition tailend2 = tail3.addOrReplaceChild("tailend2", CubeListBuilder.create()
                .texOffs(200, 147).addBox(-0.75F, -10.0F, -0.75F, 2.0F, 10.0F, 2.0F)
                .texOffs(50, 159).addBox(-2.0F, -15.0F, -0.5F, 4.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.5F, 0.0F, -0.1F));
        PartDefinition lesser_r0 = trunk.addOrReplaceChild("lesser_r0", CubeListBuilder.create()
                .texOffs(76, 117).addBox(-2.5F, -12.0F, -2.5F, 5.0F, 12.0F, 5.0F),
                PartPose.offset(-9.0F, -11.0F, -1.0F));
        PartDefinition lesser_r1 = lesser_r0.addOrReplaceChild("lesser_r1", CubeListBuilder.create()
                .texOffs(486, 23).addBox(-2.0F, -12.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 0.58F, 0.0F, 0.0F));
        PartDefinition lesser_r2 = lesser_r1.addOrReplaceChild("lesser_r2", CubeListBuilder.create()
                .texOffs(492, 132).addBox(-1.5F, -12.0F, -1.5F, 3.0F, 12.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 0.02F, 0.0F, 0.0F));
        PartDefinition lesser_r3 = lesser_r2.addOrReplaceChild("lesser_r3", CubeListBuilder.create()
                .texOffs(232, 99).addBox(-1.0F, -12.0F, -1.0F, 2.0F, 12.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 0.58F, 0.0F, 0.0F));
        PartDefinition lesser_r4 = lesser_r3.addOrReplaceChild("lesser_r4", CubeListBuilder.create()
                .texOffs(240, 99).addBox(-1.0F, -12.0F, -1.0F, 2.0F, 12.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 0.02F, 0.0F, 0.0F));
        PartDefinition lesser_r_end0 = lesser_r4.addOrReplaceChild("lesser_r_end0", CubeListBuilder.create()
                .texOffs(504, 147).addBox(-0.75F, -7.0F, -0.75F, 2.0F, 7.0F, 2.0F)
                .texOffs(220, 158).addBox(-2.5F, -14.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 0.5F, 0.0F, 0.0F));
        PartDefinition lesser_r_end1 = lesser_r4.addOrReplaceChild("lesser_r_end1", CubeListBuilder.create()
                .texOffs(344, 158).addBox(-0.75F, -7.0F, -0.75F, 2.0F, 7.0F, 2.0F)
                .texOffs(284, 158).addBox(-2.5F, -14.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, -0.3F, 0.0F, 0.6F));
        PartDefinition lesser_r_end2 = lesser_r4.addOrReplaceChild("lesser_r_end2", CubeListBuilder.create()
                .texOffs(352, 158).addBox(-0.75F, -7.0F, -0.75F, 2.0F, 7.0F, 2.0F)
                .texOffs(296, 158).addBox(-2.5F, -14.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, -0.3F, 0.0F, -0.6F));
        PartDefinition lesser_l0 = trunk.addOrReplaceChild("lesser_l0", CubeListBuilder.create()
                .texOffs(0, 100).addBox(-2.5F, -13.0F, -2.5F, 5.0F, 13.0F, 5.0F),
                PartPose.offset(8.0F, -11.0F, 3.0F));
        PartDefinition lesser_l1 = lesser_l0.addOrReplaceChild("lesser_l1", CubeListBuilder.create()
                .texOffs(96, 117).addBox(-2.0F, -13.0F, -2.0F, 4.0F, 13.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.08F, 0.0F, 0.0F));
        PartDefinition lesser_l2 = lesser_l1.addOrReplaceChild("lesser_l2", CubeListBuilder.create()
                .texOffs(268, 131).addBox(-1.5F, -13.0F, -1.5F, 3.0F, 13.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.52F, 0.0F, 0.0F));
        PartDefinition lesser_l3 = lesser_l2.addOrReplaceChild("lesser_l3", CubeListBuilder.create()
                .texOffs(344, 134).addBox(-1.0F, -13.0F, -1.0F, 2.0F, 13.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.08F, 0.0F, 0.0F));
        PartDefinition lesser_l_end0 = lesser_l3.addOrReplaceChild("lesser_l_end0", CubeListBuilder.create()
                .texOffs(360, 158).addBox(-0.75F, -7.0F, -0.75F, 2.0F, 7.0F, 2.0F)
                .texOffs(308, 158).addBox(-2.5F, -14.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.5F, 0.0F, 0.0F));
        PartDefinition lesser_l_end1 = lesser_l3.addOrReplaceChild("lesser_l_end1", CubeListBuilder.create()
                .texOffs(368, 158).addBox(-0.75F, -7.0F, -0.75F, 2.0F, 7.0F, 2.0F)
                .texOffs(320, 158).addBox(-2.5F, -14.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, -0.3F, 0.0F, 0.6F));
        PartDefinition lesser_l_end2 = lesser_l3.addOrReplaceChild("lesser_l_end2", CubeListBuilder.create()
                .texOffs(376, 158).addBox(-0.75F, -7.0F, -0.75F, 2.0F, 7.0F, 2.0F)
                .texOffs(332, 158).addBox(-2.5F, -14.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, -0.3F, 0.0F, -0.6F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 512, 512).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    /** The pose being put together becomes a pose of the sheet. */
    private void set(float[][] to) {
        for (int i = 0; i < this.now.length; i++) {
            System.arraycopy(to[i], 0, this.now[i], 0, 3);
        }
    }

    /** The pose being put together goes {@code amount} of the way to a pose of the sheet. */
    private void blend(float amount, float[][] to) {
        if (amount <= 0.0F) {
            return;
        }
        for (int i = 0; i < this.now.length; i++) {
            for (int k = 0; k < 3; k++) {
                this.now[i][k] += (to[i][k] - this.now[i][k]) * amount;
            }
        }
    }

    /** One of the four poses of its walk, and the one after it, by how far through its stride it is (0..1 round and round). */
    private void stride(float cycle) {
        float[][][] keys = {WALK0, WALKB, WALK1, WALKA};
        float at = (cycle - Mth.floor(cycle)) * 4.0F;
        int from = Mth.clamp((int) at, 0, 3);
        float f = at - from;
        f = f * f * (3.0F - 2.0F * f);
        float[][] a = keys[from];
        float[][] b = keys[(from + 1) % 4];
        for (int i = 0; i < this.walk.length; i++) {
            for (int k = 0; k < 3; k++) {
                this.walk[i][k] = a[i][k] + (b[i][k] - a[i][k]) * f;
            }
        }
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float t = state.attackTime;
        int move = state.attackId;
        boolean bare = state.phase >= 2;
        boolean coming = bare && move == ComingEntity.EMERGE;
        float broke = ComingEntity.EMERGE_BREAK;
        // how far the crown has opened and the ring shown; how far what crept out along the rock has been drawn back into it
        float open = bare ? (coming ? Anim.ramp(t, broke + 56.0F, broke + 80.0F) : 1.0F) : 0.0F;
        float drawn = bare ? (coming ? Anim.ramp(t, broke + 2.0F, broke + 34.0F) : 1.0F) : 0.0F;
        this.mask.visible = !bare;
        this.set(bare && !coming ? BARE : MASKED);
        float speed = bare && !coming ? Math.min(1.0F, state.walkAnimationSpeed * 2.0F) : 0.0F;
        if (speed > 0.01F) {
            this.stride(state.walkAnimationPos * 0.042F);
            this.blend(speed, this.walk);
        }

        // the crown: folded behind the disc under the mask, open round it without - the rays of it
        float spread = 0.0F;
        float curl = 0.0F;
        switch (move) {
            case ComingEntity.GAZE -> {
                float up = Anim.pulse(t, 2.0F, ComingEntity.GAZE_LOOK - 4.0F, ComingEntity.GAZE_DURATION - 24.0F, ComingEntity.GAZE_DURATION);
                this.blend(up, bare ? BARE_GAZE : GAZE);
            }
            case ComingEntity.SLAM -> {
                float impact = ComingEntity.SLAM_IMPACT;
                float back = Anim.pulse(t, 2.0F, impact - 14.0F, impact - 8.0F, impact - 2.0F);
                float down = Anim.pulse(t, impact - 8.0F, impact, ComingEntity.SLAM_LAIN, ComingEntity.SLAM_DURATION - 4.0F);
                this.blend(back, bare ? BARE_SLAM_BACK : SLAM_BACK);
                this.blend(down, bare ? BARE_SLAM : SLAM);
            }
            case ComingEntity.SWEEP -> {
                float impact = ComingEntity.SWEEP_IMPACT;
                float low = Anim.pulse(t, 4.0F, impact - 14.0F, impact + 14.0F, ComingEntity.SWEEP_DURATION);
                float through = Anim.ramp(t, impact - 7.0F, impact + 5.0F);
                this.blend(low, bare ? BARE_SWEEP_BACK : SWEEP_BACK);
                this.blend(low * through, bare ? BARE_SWEEP : SWEEP);
            }
            case ComingEntity.CRUSH -> {
                float impact = ComingEntity.CRUSH_IMPACT;
                float up = Anim.pulse(t, 0.0F, impact - 12.0F, impact - 6.0F, impact - 2.0F);
                float down = Anim.pulse(t, impact - 6.0F, impact, impact + 14.0F, ComingEntity.CRUSH_DURATION);
                this.blend(up, bare ? BARE_CRUSH_UP : CRUSH_UP);
                this.blend(down, bare ? BARE_CRUSH : CRUSH);
            }
            case ComingEntity.REACH -> {
                // its hands go into the floor, and stay in it while what it reaches with is up elsewhere
                float sunk = Anim.pulse(t, 2.0F, ComingEntity.REACH_CALL - 4.0F, ComingEntity.REACH_DURATION - 22.0F, ComingEntity.REACH_DURATION);
                this.blend(sunk, bare ? BARE_REACH : REACH);
            }
            case ComingEntity.EMERGE -> {
                // it works at the mask from behind it, the rope shaking; the mask goes, and it throws both hands out into
                // the hall and the rope up, and drags the knot out of the crack after them; and stands up, and the crown opens
                float strain = Anim.pulse(t, 6.0F, 20.0F, broke - 2.0F, broke + 2.0F);
                this.blend(strain, GAZE);
                this.blend(Anim.ramp(t, broke, broke + 16.0F), EMERGE_GRIP);
                this.blend(Anim.ramp(t, broke + 16.0F, broke + 62.0F), BARE);
                this.blend(Anim.pulse(t, broke + 58.0F, broke + 72.0F, ComingEntity.EMERGE_DURATION - 26.0F, ComingEntity.EMERGE_DURATION - 6.0F), RAIN);
                for (int i = 0; i < NECK; i++) {
                    this.now[NECK0 + i][2] += Mth.sin(t * 1.7F + i) * 0.035F * strain;
                    this.now[NECK0 + i][0] += Mth.sin(t * 1.3F + i * 0.7F) * 0.02F * strain;
                }
                spread = 0.35F * Anim.pulse(t, broke + 58.0F, broke + 72.0F, ComingEntity.EMERGE_DURATION - 26.0F, ComingEntity.EMERGE_DURATION - 6.0F);
            }
            case ComingEntity.PULL -> {
                float gape = Anim.pulse(t, 6.0F, ComingEntity.PULL_FROM, ComingEntity.PULL_BITE - 3.0F, ComingEntity.PULL_BITE + 1.0F);
                float bite = Anim.pulse(t, ComingEntity.PULL_BITE - 6.0F, ComingEntity.PULL_BITE, ComingEntity.PULL_BITE + 12.0F, ComingEntity.PULL_DURATION);
                this.blend(gape, PULL);
                this.blend(bite, BITE);
                this.now[TRUNK][2] += Mth.sin(t * 0.9F) * 0.03F * gape;
                spread = 0.25F * Anim.pulse(t, 4.0F, ComingEntity.PULL_FROM, ComingEntity.PULL_BITE, ComingEntity.PULL_DURATION);
            }
            case ComingEntity.RAIN -> {
                float up = Anim.pulse(t, 4.0F, ComingEntity.RAIN_FROM, ComingEntity.RAIN_UNTIL, ComingEntity.RAIN_DURATION);
                this.blend(up, RAIN);
                spread = 0.35F * up;
            }
            case ComingEntity.GLARE -> {
                // it bows down out of the way of what looks over it
                float low = Anim.pulse(t, 4.0F, ComingEntity.GLARE_FROM, ComingEntity.GLARE_UNTIL, ComingEntity.GLARE_DURATION);
                this.blend(low, GLARE);
                curl = low;
                spread = -0.9F * low;
            }
            case ComingEntity.STAMP -> {
                float impact = ComingEntity.STAMP_IMPACT;
                float up = Anim.pulse(t, 0.0F, impact - 8.0F, impact - 5.0F, impact - 1.0F);
                float down = Anim.pulse(t, impact - 5.0F, impact, ComingEntity.STAMP_LAIN, ComingEntity.STAMP_DURATION - 2.0F);
                this.blend(up, STAMP_UP);
                this.blend(down, STAMP);
            }
            case ComingEntity.CROSS -> {
                // it gathers itself low before it goes
                float low = Anim.pulse(t, 0.0F, ComingEntity.CROSS_GO - 4.0F, ComingEntity.CROSS_GO, ComingEntity.CROSS_GO + 10.0F);
                this.blend(low * (1.0F - speed), STAMP_UP);
            }
            default -> {
            }
        }
        for (int i = 0; i < POSED.length; i++) {
            PartPose rest = this.part[i].getInitialPose();
            this.part[i].xRot = rest.xRot() + this.now[i][0];
            this.part[i].yRot = rest.yRot() + this.now[i][1];
            this.part[i].zRot = rest.zRot() + this.now[i][2];
        }
        this.body.y += this.now[POSED.length][1];
        this.body.z += this.now[POSED.length][2];

        // in the crack it turns the whole of what is out of it to whoever it fights; out of it, the game turns it
        this.turn.yRot += bare ? 0.0F : state.extra;
        // it breathes; the rope sways, and what has come loose from it works; the lesser necks feel about
        this.part[TRUNK].xRot += Mth.sin(age * 0.035F) * 0.02F;
        this.part[TRUNK].zRot += Mth.sin(age * 0.027F + 1.0F) * 0.015F;
        for (int i = 0; i < NECK; i++) {
            this.part[NECK0 + i].zRot += Mth.sin(age * 0.031F - i * 0.6F) * 0.022F;
            this.part[NECK0 + i].yRot += Mth.sin(age * 0.023F + i * 0.9F) * 0.02F;
            this.fray[i].xRot += Mth.sin(age * (0.05F + i * 0.007F) + i * 1.9F) * 0.22F;
            this.frayFore[i].xRot += Math.max(0.0F, Mth.sin(age * (0.11F + i * 0.01F) + i)) * 0.5F;
        }
        this.disc.zRot += Mth.sin(age * 0.022F) * 0.03F;
        for (int side = 0; side < 2; side++) {
            for (int i = 0; i < this.lesser[side].length; i++) {
                this.lesser[side][i].xRot += Mth.sin(age * 0.041F + i * 0.9F + side * 2.0F) * 0.13F;
                this.lesser[side][i].zRot += Mth.cos(age * 0.033F + i * 1.3F + side) * 0.11F;
            }
        }
        float going = state.walkAnimationPos * 0.26F;
        for (int i = 0; i < DANGLE; i++) {
            this.dangle[i].xRot += Mth.sin(age * 0.045F + i * 1.7F) * 0.1F + Mth.sin(going + i) * 0.18F * speed + 0.3F * speed;
            this.dangle[i].zRot += Mth.cos(age * 0.037F + i * 2.3F) * 0.08F;
            this.dangleFore[i].xRot += Mth.sin(age * 0.06F + i * 1.1F) * 0.2F;
        }
        for (int i = 0; i < TAIL; i++) {
            this.tail[i].zRot += Mth.sin(age * 0.04F - i * 0.8F) * 0.09F + Mth.sin(going - i * 0.9F) * 0.12F * speed;
        }
        for (int i = 0; i < CROWN; i++) {
            Anim.pose(this.crown[i], open, 2.15F + spread + Mth.sin(age * 0.05F + i * 1.3F) * 0.08F, 0.0F, Mth.sin(age * 0.03F + i) * 0.06F);
            Anim.pose(this.crownFore[i], open, -1.9F - 0.6F * curl + Mth.sin(age * 0.07F + i * 2.1F) * 0.15F, 0.0F, 0.0F);
            if (!bare) {
                this.crownFore[i].xRot += Mth.sin(age * 0.1F + i * 1.7F) * 0.06F;       // (fingers working at the edge of the mask)
            }
        }
        // the ring of light round the disc turns, slowly, and is not there under the mask
        this.halo.visible = open > 0.02F;
        this.halo.xScale = open;
        this.halo.yScale = open;
        this.halo.zRot += age * 0.006F;
        // what crept out of the crack along the rock: its hands feel the rock; it is drawn back in when the rest comes out
        this.roots.visible = drawn < 0.98F;
        this.roots.z += drawn * 14.0F;
        for (int i = 0; i < ROOTS * 2; i++) {
            this.root[i].zRot += Mth.sin(age * 0.03F + i * 1.4F) * 0.05F - (i % 2 == 0 ? 1.0F : -1.0F) * drawn * 0.5F;
            this.rootFore[i].zRot += Mth.sin(age * 0.05F + i * 2.2F) * 0.12F;
        }
    }
}
