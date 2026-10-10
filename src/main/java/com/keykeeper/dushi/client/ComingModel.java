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
 * It has no head and no neck: it is a mass of shoulders with a great disc set in the breast of it, a ring of light
 * and a crown of arms round the disc, two arms a house long, lesser ones down its flanks, a belly that opens. It does
 * not turn: it is in a crack of the wall behind it, and a pose says how far back in the crack the whole of it is.
 * Masked, all of it but the disc and the two arms is in the rock; with the mask off it is out to the waist, the crown
 * is open, and the eye in the crack behind it is open. It turns the mass on its waist to whoever it fights
 * ({@code extra} of the state is how far round, see ComingEntity.renderExtra).
 */
public class ComingModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("coming"), "main");
    /** Must match the scale in the generator. */
    private static final float MODEL_SCALE = 2.4F;
    /** The arms round the disc and the lesser arms down each flank: CROWN and RIBS in tools/gen_coming.py. */
    private static final int CROWN = 12;
    private static final int RIBS = 5;
    /** The parts a pose turns, in the order of the rows of a pose: POSED in the generator. */
    private static final String[][] POSED = {{"waist"}, {"waist", "torso"}, {"waist", "torso", "disc"}, {"waist", "torso", "arm_r"},
            {"waist", "torso", "arm_r", "fore_r"}, {"waist", "torso", "arm_r", "fore_r", "hand_r"}, {"waist", "torso", "arm_l"}, {"waist", "torso", "arm_l", "fore_l"},
            {"waist", "torso", "arm_l", "fore_l", "hand_l"}, {"waist", "maw_r"}, {"waist", "maw_l"}};
    private static final int WAIST = 0;
    private static final int TORSO = 1;
    private static final int DISC = 2;
    private static final int ARM_R = 3;
    private static final int ARM_L = 6;

    // <generated poses>
    private static final float[][] MASKED = {{-0.200F, 0.000F, 0.000F}, {-0.250F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.894F, 0.000F, -2.642F}, {-0.226F, 0.000F, -0.920F}, {1.100F, 0.000F, 0.000F}, {0.894F, 0.000F, 2.642F}, {-0.226F, 0.000F, 0.920F}, {1.100F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] GAZE = {{-0.050F, 0.000F, 0.000F}, {-0.050F, 0.000F, 0.000F}, {0.300F, 0.000F, 0.000F}, {1.170F, 0.000F, -2.264F}, {0.123F, 0.000F, -0.934F}, {1.100F, 0.000F, 0.000F}, {1.170F, 0.000F, 2.264F}, {0.123F, 0.000F, 0.934F}, {1.100F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] SLAM_BACK = {{-0.400F, 0.000F, 0.000F}, {-0.400F, 0.000F, 0.000F}, {-0.200F, 0.000F, 0.000F}, {0.575F, 0.000F, -2.776F}, {-0.381F, 0.000F, -0.882F}, {1.100F, 0.000F, 0.000F}, {0.575F, 0.000F, 2.776F}, {-0.381F, 0.000F, 0.882F}, {1.100F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] SLAM = {{0.750F, 0.000F, 0.000F}, {0.450F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.150F, 0.000F, -0.861F}, {0.437F, 0.000F, 0.458F}, {0.600F, 0.000F, 0.000F}, {0.150F, 0.000F, 0.861F}, {0.437F, 0.000F, -0.458F}, {0.600F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] SWEEP_BACK = {{-0.200F, 0.000F, 0.000F}, {-0.250F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.434F, 0.000F, -1.681F}, {-0.009F, 0.000F, -0.257F}, {0.400F, 0.000F, 0.000F}, {0.894F, 0.000F, 2.642F}, {-0.226F, 0.000F, 0.920F}, {1.100F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] SWEEP = {{-0.200F, 0.000F, 0.000F}, {-0.250F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.778F, 0.000F, 2.257F}, {-0.296F, 0.000F, -0.208F}, {0.400F, 0.000F, 0.000F}, {0.894F, 0.000F, 2.642F}, {-0.226F, 0.000F, 0.920F}, {1.100F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] CRUSH_UP = {{-0.300F, 0.000F, 0.000F}, {-0.300F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.629F, 0.000F, -0.592F}, {-0.026F, 0.000F, 0.347F}, {0.300F, 0.000F, 0.000F}, {0.629F, 0.000F, 0.592F}, {-0.026F, 0.000F, -0.347F}, {0.300F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] CRUSH = {{-0.050F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {1.204F, 0.000F, -2.369F}, {-0.149F, 0.000F, -0.601F}, {1.400F, 0.000F, 0.000F}, {1.204F, 0.000F, 2.369F}, {-0.149F, 0.000F, 0.601F}, {1.400F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] REACH = {{0.000F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {0.200F, 0.000F, 0.000F}, {0.921F, 0.000F, -2.162F}, {0.258F, 0.000F, -0.845F}, {1.500F, 0.000F, 0.000F}, {0.921F, 0.000F, 2.162F}, {0.258F, 0.000F, 0.845F}, {1.500F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] BARE = {{0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.748F, 0.000F, -2.392F}, {0.343F, 0.000F, -0.532F}, {1.100F, 0.000F, 0.000F}, {0.748F, 0.000F, 2.392F}, {0.343F, 0.000F, 0.532F}, {1.100F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] BARE_GAZE = {{-0.150F, 0.000F, 0.000F}, {-0.150F, 0.000F, 0.000F}, {0.200F, 0.000F, 0.000F}, {0.514F, 0.000F, -2.531F}, {0.218F, 0.000F, -0.589F}, {1.100F, 0.000F, 0.000F}, {0.514F, 0.000F, 2.531F}, {0.218F, 0.000F, 0.589F}, {1.100F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] BARE_SLAM_BACK = {{-0.300F, 0.000F, 0.000F}, {-0.350F, 0.000F, 0.000F}, {-0.200F, 0.000F, 0.000F}, {0.307F, 0.000F, -2.148F}, {0.257F, 0.000F, -0.685F}, {0.800F, 0.000F, 0.000F}, {0.307F, 0.000F, 2.148F}, {0.257F, 0.000F, 0.685F}, {0.800F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] BARE_SLAM = {{0.750F, 0.000F, 0.000F}, {0.420F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.224F, 0.000F, -0.959F}, {0.341F, 0.000F, 0.488F}, {0.600F, 0.000F, 0.000F}, {0.224F, 0.000F, 0.959F}, {0.341F, 0.000F, -0.488F}, {0.600F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] BARE_SWEEP_BACK = {{0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.436F, 0.000F, -1.468F}, {0.113F, 0.000F, -0.231F}, {0.400F, 0.000F, 0.000F}, {0.748F, 0.000F, 2.392F}, {0.343F, 0.000F, 0.532F}, {1.100F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] BARE_SWEEP = {{0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.976F, 0.000F, 1.752F}, {-0.359F, 0.000F, 0.019F}, {0.400F, 0.000F, 0.000F}, {0.748F, 0.000F, 2.392F}, {0.343F, 0.000F, 0.532F}, {1.100F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] BARE_CRUSH_UP = {{-0.200F, 0.000F, 0.000F}, {-0.200F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.459F, 0.000F, -0.527F}, {-0.068F, 0.000F, 0.341F}, {0.300F, 0.000F, 0.000F}, {0.459F, 0.000F, 0.527F}, {-0.068F, 0.000F, -0.341F}, {0.300F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] BARE_CRUSH = {{0.100F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {1.317F, 0.000F, -1.642F}, {0.281F, 0.000F, -0.557F}, {1.400F, 0.000F, 0.000F}, {1.317F, 0.000F, 1.642F}, {0.281F, 0.000F, 0.557F}, {1.400F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] BARE_REACH = {{0.200F, 0.000F, 0.000F}, {0.150F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {1.025F, 0.000F, -1.823F}, {0.548F, 0.000F, -0.720F}, {1.500F, 0.000F, 0.000F}, {1.025F, 0.000F, 1.823F}, {0.548F, 0.000F, 0.720F}, {1.500F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] PULL = {{0.000F, 0.000F, 0.000F}, {-0.200F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {0.595F, 0.000F, -2.495F}, {0.256F, 0.000F, -0.575F}, {1.100F, 0.000F, 0.000F}, {0.595F, 0.000F, 2.495F}, {0.256F, 0.000F, 0.575F}, {1.100F, 0.000F, 0.000F}, {0.000F, 1.350F, 0.000F}, {0.000F, -1.350F, 0.000F}};
    private static final float[][] BITE = {{0.550F, 0.000F, 0.000F}, {0.300F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {1.020F, 0.000F, -1.266F}, {0.567F, 0.000F, 0.275F}, {1.100F, 0.000F, 0.000F}, {1.020F, 0.000F, 1.266F}, {0.567F, 0.000F, -0.275F}, {1.100F, 0.000F, 0.000F}, {0.000F, 0.200F, 0.000F}, {0.000F, -0.200F, 0.000F}};
    private static final float[][] RAIN = {{-0.200F, 0.000F, 0.000F}, {-0.300F, 0.000F, 0.000F}, {-0.350F, 0.000F, 0.000F}, {0.545F, 0.000F, -0.555F}, {-0.048F, 0.000F, 0.344F}, {0.300F, 0.000F, 0.000F}, {0.545F, 0.000F, 0.555F}, {-0.048F, 0.000F, -0.344F}, {0.300F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float[][] GLARE = {{0.600F, 0.000F, 0.000F}, {0.450F, 0.000F, 0.000F}, {0.100F, 0.000F, 0.000F}, {0.934F, 0.000F, -0.996F}, {0.451F, 0.000F, 0.446F}, {1.100F, 0.000F, 0.000F}, {0.934F, 0.000F, 0.996F}, {0.451F, 0.000F, -0.446F}, {1.100F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}, {0.000F, 0.000F, 0.000F}};
    private static final float BACK_MASKED = 14.0F;
    private static final float BACK_GAZE = 6.0F;
    private static final float BACK_SLAM_BACK = 22.0F;
    private static final float BACK_SLAM = 0.0F;
    private static final float BACK_SWEEP_BACK = 14.0F;
    private static final float BACK_SWEEP = 14.0F;
    private static final float BACK_CRUSH_UP = 14.0F;
    private static final float BACK_CRUSH = 14.0F;
    private static final float BACK_REACH = 8.0F;
    private static final float OUT_FRONT = 8.0F;
    // </generated poses>

    private final ModelPart eye;
    private final ModelPart body;
    private final ModelPart mask;
    private final ModelPart halo;
    private final ModelPart[] part = new ModelPart[POSED.length];
    private final ModelPart[] crown = new ModelPart[CROWN];
    private final ModelPart[] crownFore = new ModelPart[CROWN];
    private final ModelPart[] rib = new ModelPart[RIBS * 2];
    private final ModelPart[] ribFore = new ModelPart[RIBS * 2];
    /** How far back in the crack the whole of it is in the pose being put together, in model units. */
    private float back;

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
        this.mask = this.part[DISC].getChild("mask");
        this.halo = this.part[DISC].getChild("halo");
        for (int i = 0; i < CROWN; i++) {
            this.crown[i] = this.part[DISC].getChild("crown" + i);
            this.crownFore[i] = this.crown[i].getChild("crownf" + i);
        }
        for (int i = 0; i < RIBS * 2; i++) {
            int k = i / 2;
            String side = i % 2 == 0 ? "r" : "l";
            this.rib[i] = this.part[k < 3 ? TORSO : WAIST].getChild("rib_" + side + k);
            this.ribFore[i] = this.rib[i].getChild("ribf_" + side + k);
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition eye = all.addOrReplaceChild("eye", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-19.0F, -34.0F, 0.0F, 38.0F, 68.0F, 1.0F),
                PartPose.offset(0.0F, -56.0F, 9.5F));
        PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(272, 43).addBox(-9.0F, -16.0F, 10.0F, 18.0F, 16.0F, 26.0F),
                PartPose.ZERO);
        PartDefinition waist = body.addOrReplaceChild("waist", CubeListBuilder.create()
                .texOffs(192, 0).addBox(-12.0F, -24.0F, -8.0F, 24.0F, 32.0F, 16.0F)
                .texOffs(0, 69).addBox(-8.0F, -21.0F, -8.6F, 16.0F, 19.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -6.0F, 8.0F, 0.2F, 0.0F, 0.0F));
        PartDefinition maw_r = waist.addOrReplaceChild("maw_r", CubeListBuilder.create()
                .texOffs(248, 48).addBox(0.0F, 0.0F, -1.0F, 8.0F, 19.0F, 1.0F),
                PartPose.offset(-8.0F, -21.0F, -9.0F));
        PartDefinition maw_l = waist.addOrReplaceChild("maw_l", CubeListBuilder.create()
                .texOffs(34, 69).addBox(-8.0F, 0.0F, -1.0F, 8.0F, 19.0F, 1.0F),
                PartPose.offset(8.0F, -21.0F, -9.0F));
        PartDefinition torso = waist.addOrReplaceChild("torso", CubeListBuilder.create()
                .texOffs(272, 0).addBox(-26.0F, -36.0F, -11.0F, 52.0F, 21.0F, 22.0F)
                .texOffs(78, 57).addBox(-18.0F, -15.0F, -9.0F, 36.0F, 17.0F, 18.0F)
                .texOffs(186, 85).addBox(-14.0F, -42.0F, -7.0F, 28.0F, 6.0F, 15.0F)
                .texOffs(468, 81).addBox(-0.5F, -46.0F, -5.0F, 1.0F, 4.0F, 14.0F)
                .texOffs(360, 43).addBox(-0.5F, -38.0F, 11.0F, 1.0F, 36.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -24.0F, 0.0F, 0.25F, 0.0F, 0.0F));
        PartDefinition disc = torso.addOrReplaceChild("disc", CubeListBuilder.create()
                .texOffs(420, 0).addBox(-20.0F, -20.0F, -3.0F, 40.0F, 40.0F, 3.0F),
                PartPose.offset(0.0F, -20.0F, -12.0F));
        PartDefinition halo = disc.addOrReplaceChild("halo", CubeListBuilder.create()
                .texOffs(78, 0).addBox(-28.0F, -28.0F, 0.0F, 56.0F, 56.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, -0.5F));
        PartDefinition mask = disc.addOrReplaceChild("mask", CubeListBuilder.create()
                .texOffs(370, 43).addBox(-15.0F, -20.0F, -1.5F, 30.0F, 38.0F, 1.0F)
                .texOffs(370, 82).addBox(-24.0F, -39.0F, -1.0F, 48.0F, 23.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, -3.0F));
        PartDefinition crown0 = disc.addOrReplaceChild("crown0", CubeListBuilder.create()
                .texOffs(496, 43).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(4.66F, -17.39F, -1.0F, -2.5F, 0.0F, 0.2618F));
        PartDefinition crownf0 = crown0.addOrReplaceChild("crownf0", CubeListBuilder.create()
                .texOffs(506, 0).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(152, 92).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown1 = disc.addOrReplaceChild("crown1", CubeListBuilder.create()
                .texOffs(504, 43).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(12.73F, -12.73F, -1.0F, -2.5F, 0.0F, 0.7854F));
        PartDefinition crownf1 = crown1.addOrReplaceChild("crownf1", CubeListBuilder.create()
                .texOffs(506, 15).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(164, 92).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown2 = disc.addOrReplaceChild("crown2", CubeListBuilder.create()
                .texOffs(496, 61).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(17.39F, -4.66F, -1.0F, -2.5F, 0.0F, 1.309F));
        PartDefinition crownf2 = crown2.addOrReplaceChild("crownf2", CubeListBuilder.create()
                .texOffs(266, 48).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(332, 97).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown3 = disc.addOrReplaceChild("crown3", CubeListBuilder.create()
                .texOffs(504, 61).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(17.39F, 4.66F, -1.0F, -2.5F, 0.0F, 1.8326F));
        PartDefinition crownf3 = crown3.addOrReplaceChild("crownf3", CubeListBuilder.create()
                .texOffs(186, 57).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(344, 97).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown4 = disc.addOrReplaceChild("crown4", CubeListBuilder.create()
                .texOffs(52, 69).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(12.73F, 12.73F, -1.0F, -2.5F, 0.0F, 2.3562F));
        PartDefinition crownf4 = crown4.addOrReplaceChild("crownf4", CubeListBuilder.create()
                .texOffs(506, 79).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(498, 97).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown5 = disc.addOrReplaceChild("crown5", CubeListBuilder.create()
                .texOffs(60, 69).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(4.66F, 17.39F, -1.0F, -2.5F, 0.0F, 2.8798F));
        PartDefinition crownf5 = crown5.addOrReplaceChild("crownf5", CubeListBuilder.create()
                .texOffs(304, 85).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(468, 99).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown6 = disc.addOrReplaceChild("crown6", CubeListBuilder.create()
                .texOffs(68, 69).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(-4.66F, 17.39F, -1.0F, -2.5F, 0.0F, 3.4034F));
        PartDefinition crownf6 = crown6.addOrReplaceChild("crownf6", CubeListBuilder.create()
                .texOffs(308, 85).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(480, 99).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown7 = disc.addOrReplaceChild("crown7", CubeListBuilder.create()
                .texOffs(498, 79).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(-12.73F, 12.73F, -1.0F, -2.5F, 0.0F, 3.927F));
        PartDefinition crownf7 = crown7.addOrReplaceChild("crownf7", CubeListBuilder.create()
                .texOffs(312, 85).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(296, 100).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown8 = disc.addOrReplaceChild("crown8", CubeListBuilder.create()
                .texOffs(360, 83).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(-17.39F, 4.66F, -1.0F, -2.5F, 0.0F, 4.4506F));
        PartDefinition crownf8 = crown8.addOrReplaceChild("crownf8", CubeListBuilder.create()
                .texOffs(316, 85).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(308, 100).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown9 = disc.addOrReplaceChild("crown9", CubeListBuilder.create()
                .texOffs(272, 85).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(-17.39F, -4.66F, -1.0F, -2.5F, 0.0F, 4.9742F));
        PartDefinition crownf9 = crown9.addOrReplaceChild("crownf9", CubeListBuilder.create()
                .texOffs(320, 85).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(320, 100).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown10 = disc.addOrReplaceChild("crown10", CubeListBuilder.create()
                .texOffs(280, 85).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(-12.73F, -12.73F, -1.0F, -2.5F, 0.0F, 5.4978F));
        PartDefinition crownf10 = crown10.addOrReplaceChild("crownf10", CubeListBuilder.create()
                .texOffs(324, 85).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(0, 101).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition crown11 = disc.addOrReplaceChild("crown11", CubeListBuilder.create()
                .texOffs(288, 85).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F),
                PartPose.offsetAndRotation(-4.66F, -17.39F, -1.0F, -2.5F, 0.0F, 6.0214F));
        PartDefinition crownf11 = crown11.addOrReplaceChild("crownf11", CubeListBuilder.create()
                .texOffs(328, 85).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 14.0F, 1.0F)
                .texOffs(12, 101).addBox(-2.5F, -21.0F, -0.5F, 5.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 2.4F, 0.0F, 0.0F));
        PartDefinition arm_r = torso.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(432, 43).addBox(-4.0F, -30.0F, -4.0F, 8.0F, 30.0F, 8.0F),
                PartPose.offset(-25.0F, -27.0F, 0.0F));
        PartDefinition fore_r = arm_r.addOrReplaceChild("fore_r", CubeListBuilder.create()
                .texOffs(192, 48).addBox(-3.5F, -30.0F, -3.5F, 7.0F, 30.0F, 7.0F),
                PartPose.offset(0.0F, -30.0F, 0.0F));
        PartDefinition hand_r = fore_r.addOrReplaceChild("hand_r", CubeListBuilder.create()
                .texOffs(0, 89).addBox(-6.0F, -8.0F, -2.0F, 12.0F, 8.0F, 4.0F)
                .texOffs(332, 85).addBox(-6.0F, -18.0F, -1.0F, 2.0F, 10.0F, 2.0F)
                .texOffs(248, 68).addBox(-2.7F, -21.0F, -1.0F, 2.0F, 13.0F, 2.0F)
                .texOffs(256, 68).addBox(0.7F, -21.0F, -1.0F, 2.0F, 13.0F, 2.0F)
                .texOffs(340, 85).addBox(4.0F, -18.0F, -1.0F, 2.0F, 10.0F, 2.0F)
                .texOffs(102, 103).addBox(-9.0F, -6.0F, -1.0F, 3.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -30.0F, 0.0F));
        PartDefinition arm_l = torso.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(464, 43).addBox(-4.0F, -30.0F, -4.0F, 8.0F, 30.0F, 8.0F),
                PartPose.offset(25.0F, -27.0F, 0.0F));
        PartDefinition fore_l = arm_l.addOrReplaceChild("fore_l", CubeListBuilder.create()
                .texOffs(220, 48).addBox(-3.5F, -30.0F, -3.5F, 7.0F, 30.0F, 7.0F),
                PartPose.offset(0.0F, -30.0F, 0.0F));
        PartDefinition hand_l = fore_l.addOrReplaceChild("hand_l", CubeListBuilder.create()
                .texOffs(32, 89).addBox(-6.0F, -8.0F, -2.0F, 12.0F, 8.0F, 4.0F)
                .texOffs(348, 85).addBox(4.0F, -18.0F, -1.0F, 2.0F, 10.0F, 2.0F)
                .texOffs(264, 68).addBox(0.7F, -21.0F, -1.0F, 2.0F, 13.0F, 2.0F)
                .texOffs(296, 85).addBox(-2.7F, -21.0F, -1.0F, 2.0F, 13.0F, 2.0F)
                .texOffs(64, 87).addBox(-6.0F, -18.0F, -1.0F, 2.0F, 10.0F, 2.0F)
                .texOffs(112, 103).addBox(6.0F, -6.0F, -1.0F, 3.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -30.0F, 0.0F));
        PartDefinition rib_r0 = torso.addOrReplaceChild("rib_r0", CubeListBuilder.create()
                .texOffs(72, 92).addBox(-0.75F, -9.0F, -0.75F, 2.0F, 9.0F, 2.0F),
                PartPose.offsetAndRotation(-18.0F, -12.0F, -1.0F, -0.7F, 0.0F, -1.9F));
        PartDefinition ribf_r0 = rib_r0.addOrReplaceChild("ribf_r0", CubeListBuilder.create()
                .texOffs(506, 30).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F)
                .texOffs(28, 101).addBox(-2.0F, -13.0F, -0.5F, 4.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -9.0F, 0.0F, -0.9F, 0.0F, 0.0F));
        PartDefinition rib_l0 = torso.addOrReplaceChild("rib_l0", CubeListBuilder.create()
                .texOffs(80, 92).addBox(-0.75F, -9.0F, -0.75F, 2.0F, 9.0F, 2.0F),
                PartPose.offsetAndRotation(18.0F, -12.0F, -1.0F, -0.7F, 0.0F, 1.9F));
        PartDefinition ribf_l0 = rib_l0.addOrReplaceChild("ribf_l0", CubeListBuilder.create()
                .texOffs(186, 72).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F)
                .texOffs(38, 101).addBox(-2.0F, -13.0F, -0.5F, 4.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -9.0F, 0.0F, -0.9F, 0.0F, 0.0F));
        PartDefinition rib_r1 = torso.addOrReplaceChild("rib_r1", CubeListBuilder.create()
                .texOffs(88, 92).addBox(-0.75F, -9.0F, -0.75F, 2.0F, 9.0F, 2.0F),
                PartPose.offsetAndRotation(-18.0F, -6.0F, -1.0F, -0.7F, 0.0F, -1.78F));
        PartDefinition ribf_r1 = rib_r1.addOrReplaceChild("ribf_r1", CubeListBuilder.create()
                .texOffs(356, 85).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F)
                .texOffs(48, 101).addBox(-2.0F, -13.0F, -0.5F, 4.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -9.0F, 0.0F, -0.9F, 0.0F, 0.0F));
        PartDefinition rib_l1 = torso.addOrReplaceChild("rib_l1", CubeListBuilder.create()
                .texOffs(96, 92).addBox(-0.75F, -9.0F, -0.75F, 2.0F, 9.0F, 2.0F),
                PartPose.offsetAndRotation(18.0F, -6.0F, -1.0F, -0.7F, 0.0F, 1.78F));
        PartDefinition ribf_l1 = rib_l1.addOrReplaceChild("ribf_l1", CubeListBuilder.create()
                .texOffs(176, 92).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F)
                .texOffs(152, 101).addBox(-2.0F, -13.0F, -0.5F, 4.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -9.0F, 0.0F, -0.9F, 0.0F, 0.0F));
        PartDefinition rib_r2 = torso.addOrReplaceChild("rib_r2", CubeListBuilder.create()
                .texOffs(104, 92).addBox(-0.75F, -9.0F, -0.75F, 2.0F, 9.0F, 2.0F),
                PartPose.offsetAndRotation(-18.0F, 0.0F, -1.0F, -0.7F, 0.0F, -1.66F));
        PartDefinition ribf_r2 = rib_r2.addOrReplaceChild("ribf_r2", CubeListBuilder.create()
                .texOffs(180, 92).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F)
                .texOffs(162, 101).addBox(-2.0F, -13.0F, -0.5F, 4.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -9.0F, 0.0F, -0.9F, 0.0F, 0.0F));
        PartDefinition rib_l2 = torso.addOrReplaceChild("rib_l2", CubeListBuilder.create()
                .texOffs(112, 92).addBox(-0.75F, -9.0F, -0.75F, 2.0F, 9.0F, 2.0F),
                PartPose.offsetAndRotation(18.0F, 0.0F, -1.0F, -0.7F, 0.0F, 1.66F));
        PartDefinition ribf_l2 = rib_l2.addOrReplaceChild("ribf_l2", CubeListBuilder.create()
                .texOffs(356, 94).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F)
                .texOffs(172, 101).addBox(-2.0F, -13.0F, -0.5F, 4.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -9.0F, 0.0F, -0.9F, 0.0F, 0.0F));
        PartDefinition rib_r3 = waist.addOrReplaceChild("rib_r3", CubeListBuilder.create()
                .texOffs(120, 92).addBox(-0.75F, -9.0F, -0.75F, 2.0F, 9.0F, 2.0F),
                PartPose.offsetAndRotation(-12.0F, -20.0F, -1.0F, -0.7F, 0.0F, -1.54F));
        PartDefinition ribf_r3 = rib_r3.addOrReplaceChild("ribf_r3", CubeListBuilder.create()
                .texOffs(64, 99).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F)
                .texOffs(360, 101).addBox(-2.0F, -13.0F, -0.5F, 4.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -9.0F, 0.0F, -0.9F, 0.0F, 0.0F));
        PartDefinition rib_l3 = waist.addOrReplaceChild("rib_l3", CubeListBuilder.create()
                .texOffs(128, 92).addBox(-0.75F, -9.0F, -0.75F, 2.0F, 9.0F, 2.0F),
                PartPose.offsetAndRotation(12.0F, -20.0F, -1.0F, -0.7F, 0.0F, 1.54F));
        PartDefinition ribf_l3 = rib_l3.addOrReplaceChild("ribf_l3", CubeListBuilder.create()
                .texOffs(68, 99).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F)
                .texOffs(72, 103).addBox(-2.0F, -13.0F, -0.5F, 4.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -9.0F, 0.0F, -0.9F, 0.0F, 0.0F));
        PartDefinition rib_r4 = waist.addOrReplaceChild("rib_r4", CubeListBuilder.create()
                .texOffs(136, 92).addBox(-0.75F, -9.0F, -0.75F, 2.0F, 9.0F, 2.0F),
                PartPose.offsetAndRotation(-12.0F, -11.0F, -1.0F, -0.7F, 0.0F, -1.42F));
        PartDefinition ribf_r4 = rib_r4.addOrReplaceChild("ribf_r4", CubeListBuilder.create()
                .texOffs(492, 99).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F)
                .texOffs(82, 103).addBox(-2.0F, -13.0F, -0.5F, 4.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -9.0F, 0.0F, -0.9F, 0.0F, 0.0F));
        PartDefinition rib_l4 = waist.addOrReplaceChild("rib_l4", CubeListBuilder.create()
                .texOffs(144, 92).addBox(-0.75F, -9.0F, -0.75F, 2.0F, 9.0F, 2.0F),
                PartPose.offsetAndRotation(12.0F, -11.0F, -1.0F, -0.7F, 0.0F, 1.42F));
        PartDefinition ribf_l4 = rib_l4.addOrReplaceChild("ribf_l4", CubeListBuilder.create()
                .texOffs(24, 101).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F)
                .texOffs(92, 103).addBox(-2.0F, -13.0F, -0.5F, 4.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -9.0F, 0.0F, -0.9F, 0.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 512, 256).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    /** Every part {@code amount} of the way from where it is to a pose of the sheet, which is {@code toBack} back in the crack. */
    private void pose(float amount, float[][] to, float toBack) {
        if (amount <= 0.0F) {
            return;
        }
        for (int i = 0; i < this.part.length; i++) {
            Anim.pose(this.part[i], amount, to[i][0], to[i][1], to[i][2]);
        }
        this.back = Anim.lerp(amount, this.back, toBack);
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
        this.back = BACK_MASKED;
        this.pose(1.0F, MASKED, BACK_MASKED);
        this.pose(out, BARE, -OUT_FRONT);

        // the crown: folded behind the disc under the mask, open round it without - the rays of it
        float spread = 0.0F;
        float curl = 0.0F;
        if (move == ComingEntity.RAIN) {
            spread = 0.35F * Anim.pulse(t, 4.0F, ComingEntity.RAIN_FROM, ComingEntity.RAIN_UNTIL, ComingEntity.RAIN_DURATION);
        } else if (move == ComingEntity.PULL) {
            spread = 0.25F * Anim.pulse(t, 4.0F, ComingEntity.PULL_FROM, ComingEntity.PULL_BITE, ComingEntity.PULL_DURATION);
        } else if (move == ComingEntity.GLARE) {
            curl = Anim.pulse(t, 4.0F, ComingEntity.GLARE_FROM, ComingEntity.GLARE_UNTIL, ComingEntity.GLARE_DURATION);
            spread = -0.9F * curl;
        }
        for (int i = 0; i < CROWN; i++) {
            Anim.pose(this.crown[i], out, 2.15F + spread + Mth.sin(age * 0.05F + i * 1.3F) * 0.08F, 0.0F, Mth.sin(age * 0.03F + i) * 0.06F);
            Anim.pose(this.crownFore[i], out, -1.9F - 0.6F * curl + Mth.sin(age * 0.07F + i * 2.1F) * 0.15F, 0.0F, 0.0F);
            if (!bare) {
                this.crownFore[i].xRot += Mth.sin(age * 0.1F + i * 1.7F) * 0.06F;       // (fingers working at the edge of the mask)
            }
        }

        switch (move) {
            case ComingEntity.GAZE -> {
                float up = Anim.pulse(t, 2.0F, ComingEntity.GAZE_LOOK - 4.0F, ComingEntity.GAZE_DURATION - 24.0F, ComingEntity.GAZE_DURATION);
                this.pose(up, bare ? BARE_GAZE : GAZE, bare ? -OUT_FRONT : BACK_GAZE);
            }
            case ComingEntity.SLAM -> {
                float impact = ComingEntity.SLAM_IMPACT;
                float drawn = Anim.pulse(t, 2.0F, impact - 14.0F, impact - 8.0F, impact - 2.0F);
                float down = Anim.pulse(t, impact - 8.0F, impact, ComingEntity.SLAM_LAIN, ComingEntity.SLAM_DURATION - 4.0F);
                this.pose(drawn, bare ? BARE_SLAM_BACK : SLAM_BACK, bare ? -OUT_FRONT : BACK_SLAM_BACK);
                this.pose(down, bare ? BARE_SLAM : SLAM, bare ? -OUT_FRONT : BACK_SLAM);
            }
            case ComingEntity.SWEEP -> {
                float impact = ComingEntity.SWEEP_IMPACT;
                float low = Anim.pulse(t, 4.0F, impact - 14.0F, impact + 14.0F, ComingEntity.SWEEP_DURATION);
                float through = Anim.ramp(t, impact - 7.0F, impact + 5.0F);
                this.pose(low, bare ? BARE_SWEEP_BACK : SWEEP_BACK, bare ? -OUT_FRONT : BACK_SWEEP_BACK);
                this.pose(low * through, bare ? BARE_SWEEP : SWEEP, bare ? -OUT_FRONT : BACK_SWEEP);
                this.part[TORSO].yRot += (0.3F - 0.6F * through) * low;
            }
            case ComingEntity.CRUSH -> {
                float impact = ComingEntity.CRUSH_IMPACT;
                float up = Anim.pulse(t, 0.0F, impact - 12.0F, impact - 6.0F, impact - 2.0F);
                float down = Anim.pulse(t, impact - 6.0F, impact, impact + 14.0F, ComingEntity.CRUSH_DURATION);
                this.pose(up, bare ? BARE_CRUSH_UP : CRUSH_UP, bare ? -OUT_FRONT : BACK_CRUSH_UP);
                this.pose(down, bare ? BARE_CRUSH : CRUSH, bare ? -OUT_FRONT : BACK_CRUSH);
            }
            case ComingEntity.REACH -> {
                // its hands go into the floor, and stay in it while what it reaches with is up elsewhere
                float sunk = Anim.pulse(t, 2.0F, ComingEntity.REACH_CALL - 4.0F, ComingEntity.REACH_DURATION - 22.0F, ComingEntity.REACH_DURATION);
                this.pose(sunk, bare ? BARE_REACH : REACH, bare ? -OUT_FRONT : BACK_REACH);
                this.part[ARM_R].xRot += Mth.sin(t * 0.8F) * 0.04F * sunk;
                this.part[ARM_L].xRot += Mth.sin(t * 0.8F + 1.0F) * 0.04F * sunk;
            }
            case ComingEntity.EMERGE -> {
                float strain = Anim.pulse(t, 6.0F, 20.0F, ComingEntity.EMERGE_BREAK - 2.0F, ComingEntity.EMERGE_BREAK + 2.0F);
                float thrown = Anim.pulse(t, ComingEntity.EMERGE_BREAK, ComingEntity.EMERGE_BREAK + 6.0F, ComingEntity.EMERGE_DURATION - 30.0F, ComingEntity.EMERGE_DURATION - 6.0F);
                // it works at the mask from behind it, the whole mass shaking in the crack; and when the mask goes it throws itself up and comes
                this.pose(strain, GAZE, BACK_GAZE);
                this.part[DISC].zRot += Mth.sin(t * 1.7F) * 0.12F * strain;
                this.part[TORSO].zRot += Mth.sin(t * 1.3F) * 0.07F * strain;
                this.part[TORSO].xRot += Mth.sin(t * 1.1F) * 0.06F * strain;
                this.pose(thrown, RAIN, -OUT_FRONT);
            }
            case ComingEntity.PULL -> {
                float gape = Anim.pulse(t, 6.0F, ComingEntity.PULL_FROM, ComingEntity.PULL_BITE - 3.0F, ComingEntity.PULL_BITE + 1.0F);
                float bite = Anim.pulse(t, ComingEntity.PULL_BITE - 6.0F, ComingEntity.PULL_BITE, ComingEntity.PULL_BITE + 12.0F, ComingEntity.PULL_DURATION);
                this.pose(gape, PULL, -OUT_FRONT);
                this.pose(bite, BITE, -OUT_FRONT);
                this.part[TORSO].zRot += Mth.sin(t * 0.9F) * 0.03F * gape;
            }
            case ComingEntity.RAIN -> {
                float up = Anim.pulse(t, 4.0F, ComingEntity.RAIN_FROM, ComingEntity.RAIN_UNTIL, ComingEntity.RAIN_DURATION);
                this.pose(up, RAIN, -OUT_FRONT);
            }
            case ComingEntity.GLARE -> {
                // it bows down out of the way of what looks over it
                float low = Anim.pulse(t, 4.0F, ComingEntity.GLARE_FROM, ComingEntity.GLARE_UNTIL, ComingEntity.GLARE_DURATION);
                this.pose(low, GLARE, -OUT_FRONT);
                lid *= 1.0F + 0.25F * low;
            }
            default -> {
            }
        }
        this.body.z += this.back;

        // it turns the mass on its waist to whoever it fights: what is in the crack does not turn. And it breathes.
        float look = state.extra;
        this.part[WAIST].yRot += look * 0.55F;
        this.part[TORSO].yRot += look * 0.45F;
        this.part[TORSO].xRot += Mth.sin(age * 0.035F) * 0.025F;
        this.part[TORSO].zRot += Mth.sin(age * 0.027F + 1.0F) * 0.02F;
        this.part[DISC].zRot += Mth.sin(age * 0.022F) * 0.03F;
        this.part[ARM_R].zRot += Mth.sin(age * 0.035F) * 0.02F;
        this.part[ARM_L].zRot -= Mth.sin(age * 0.031F + 1.5F) * 0.02F;
        for (int i = 0; i < RIBS * 2; i++) {
            this.rib[i].xRot += Mth.sin(age * (0.05F + i * 0.007F) + i * 1.9F) * 0.2F;
            this.ribFore[i].xRot += Math.max(0.0F, Mth.sin(age * (0.11F + i * 0.01F) + i)) * 0.5F;
        }

        // the ring of light round the disc turns, slowly, and is not there under the mask
        this.halo.visible = out > 0.02F;
        this.halo.xScale = out;
        this.halo.yScale = out;
        this.halo.zRot += age * 0.006F;
        // the eye in the crack: shut under the mask; open, it narrows and widens a little, slowly
        this.eye.visible = lid > 0.02F;
        this.eye.xScale = lid * (1.0F + Mth.sin(age * 0.045F) * 0.05F);
    }
}
