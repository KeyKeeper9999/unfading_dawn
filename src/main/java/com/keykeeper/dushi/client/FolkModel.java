package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.Folk;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;

/**
 * The folk ({@link Folk}): one humanoid mesh for all seven, and on it the things each of them wears - a part whose name
 * begins with his prefix is shown on him alone. The mesh between the markers and the textures are written by
 * tools/gen_folk.py, which also holds the same poses for its preview (POSES there).
 */
public class FolkModel extends HumanoidModel<HumanoidRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("folk"), "main");
    /** Those who sit are lowered by this much, so that they sit on what they stand on. */
    private static final float SIT_DROP = 10.0F;

    private final Folk who;
    /** The carver's mallet hand and the hangman's noose, which move a little. */
    private final ModelPart mallet;
    private final ModelPart noose;

    public FolkModel(ModelPart root, Folk who) {
        super(root);
        this.who = who;
        String mine = prefix(who);
        ModelPart[][] owners = {
                {this.body, null}, {this.head, null}, {this.rightArm, null}, {this.leftArm, null}, {this.rightLeg, null}, {root, null}};
        String[][] parts = {
                {"cw_shawl", "cw_skirt", "cv_apron", "fm_coat", "hm_bulk", "bm_coil"},
                {"db_beard", "cv_cap", "fm_hat", "hm_peak", "cl_stubs", "bm_beard"},
                {"cw_candle", "cv_mallet", "fm_pole", "hm_brawn_r"},
                {"cv_chisel", "hm_brawn_l", "hm_noose"},
                {"db_fetter"},
                {"cl_book"}};
        for (int i = 0; i < owners.length; i++) {
            for (String name : parts[i]) {
                owners[i][0].getChild(name).visible = name.startsWith(mine);
            }
        }
        this.mallet = this.rightArm.getChild("cv_mallet");
        this.noose = this.leftArm.getChild("hm_noose");
    }

    private static String prefix(Folk who) {
        return switch (who) {
            case CANDLEWIFE -> "cw_";
            case DEBTOR -> "db_";
            case CARVER -> "cv_";
            case FERRYMAN -> "fm_";
            case HANGMAN -> "hm_";
            case CELLARER -> "cl_";
            case BELLMAN -> "bm_";
        };
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        PartDefinition body = root.getChild("body");
        // <generated mesh>
        PartDefinition right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(1.9F, 12.0F, 0.0F));
        PartDefinition cw_shawl = body.addOrReplaceChild("cw_shawl", CubeListBuilder.create()
                .texOffs(36, 109).addBox(-4.5F, -0.5F, -2.5F, 9.0F, 6.0F, 5.0F, new CubeDeformation(0.3F)),
                PartPose.ZERO);
        PartDefinition cw_skirt = body.addOrReplaceChild("cw_skirt", CubeListBuilder.create()
                .texOffs(16, 32).addBox(-4.5F, 0.0F, -2.5F, 9.0F, 10.0F, 5.0F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        PartDefinition cw_candle = right_arm.addOrReplaceChild("cw_candle", CubeListBuilder.create()
                .texOffs(60, 23).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 5.0F, 1.0F)
                .texOffs(60, 45).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-0.5F, 9.5F, -1.5F, 0.95F, 0.0F, 0.0F));
        PartDefinition db_fetter = right_leg.addOrReplaceChild("db_fetter", CubeListBuilder.create()
                .texOffs(36, 120).addBox(-2.5F, 9.0F, -2.5F, 5.0F, 2.0F, 5.0F)
                .texOffs(44, 44).addBox(-0.5F, 11.0F, -4.5F, 1.0F, 1.0F, 3.0F),
                PartPose.ZERO);
        PartDefinition db_beard = head.addOrReplaceChild("db_beard", CubeListBuilder.create()
                .texOffs(36, 127).addBox(-3.5F, -3.0F, -4.6F, 7.0F, 6.0F, 1.0F),
                PartPose.ZERO);
        PartDefinition cv_cap = head.addOrReplaceChild("cv_cap", CubeListBuilder.create()
                .texOffs(0, 121).addBox(-4.5F, -9.2F, -4.5F, 9.0F, 2.0F, 9.0F),
                PartPose.ZERO);
        PartDefinition cv_apron = body.addOrReplaceChild("cv_apron", CubeListBuilder.create()
                .texOffs(32, 64).addBox(-4.0F, 4.0F, -2.8F, 8.0F, 13.0F, 1.0F),
                PartPose.ZERO);
        PartDefinition cv_mallet = right_arm.addOrReplaceChild("cv_mallet", CubeListBuilder.create()
                .texOffs(50, 61).addBox(-0.5F, -0.5F, -5.0F, 1.0F, 1.0F, 6.0F)
                .texOffs(44, 32).addBox(-1.5F, -1.5F, -8.0F, 3.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(-1.0F, 9.5F, 0.0F, 0.7F, 0.0F, 0.0F));
        PartDefinition cv_chisel = left_arm.addOrReplaceChild("cv_chisel", CubeListBuilder.create()
                .texOffs(60, 29).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F),
                PartPose.offset(1.0F, 9.5F, 0.0F));
        PartDefinition fm_hat = head.addOrReplaceChild("fm_hat", CubeListBuilder.create()
                .texOffs(0, 82).addBox(-6.5F, -8.6F, -6.5F, 13.0F, 1.0F, 13.0F)
                .texOffs(0, 109).addBox(-4.5F, -11.6F, -4.5F, 9.0F, 3.0F, 9.0F),
                PartPose.ZERO);
        PartDefinition fm_coat = body.addOrReplaceChild("fm_coat", CubeListBuilder.create()
                .texOffs(40, 96).addBox(-4.0F, 12.0F, -2.0F, 8.0F, 8.0F, 4.0F, new CubeDeformation(0.45F)),
                PartPose.ZERO);
        PartDefinition fm_pole = right_arm.addOrReplaceChild("fm_pole", CubeListBuilder.create()
                .texOffs(56, 16).addBox(-0.5F, -28.0F, -0.5F, 1.0F, 44.0F, 1.0F)
                .texOffs(60, 39).addBox(-0.5F, -30.0F, -0.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(48, 48).addBox(-0.5F, -27.0F, -2.5F, 1.0F, 1.0F, 2.0F),
                PartPose.offsetAndRotation(-1.0F, 9.5F, 0.0F, 0.45F, 0.0F, 0.0F));
        PartDefinition hm_bulk = body.addOrReplaceChild("hm_bulk", CubeListBuilder.create()
                .texOffs(0, 64).addBox(-5.0F, 0.0F, -3.0F, 10.0F, 12.0F, 6.0F, new CubeDeformation(0.25F)),
                PartPose.ZERO);
        PartDefinition hm_peak = head.addOrReplaceChild("hm_peak", CubeListBuilder.create()
                .texOffs(0, 132).addBox(-2.0F, -10.5F, -1.5F, 4.0F, 3.0F, 4.0F),
                PartPose.ZERO);
        PartDefinition hm_brawn_r = right_arm.addOrReplaceChild("hm_brawn_r", CubeListBuilder.create()
                .texOffs(0, 96).addBox(-3.5F, -2.5F, -2.5F, 5.0F, 8.0F, 5.0F),
                PartPose.ZERO);
        PartDefinition hm_brawn_l = left_arm.addOrReplaceChild("hm_brawn_l", CubeListBuilder.create()
                .texOffs(20, 96).addBox(-1.5F, -2.5F, -2.5F, 5.0F, 8.0F, 5.0F),
                PartPose.ZERO);
        PartDefinition hm_noose = left_arm.addOrReplaceChild("hm_noose", CubeListBuilder.create()
                .texOffs(44, 38).addBox(-2.0F, 0.0F, -0.5F, 4.0F, 5.0F, 1.0F)
                .texOffs(60, 16).addBox(-0.5F, 5.0F, -0.5F, 1.0F, 6.0F, 1.0F),
                PartPose.offset(1.0F, 10.0F, 0.0F));
        PartDefinition cl_stubs = head.addOrReplaceChild("cl_stubs", CubeListBuilder.create()
                .texOffs(60, 42).addBox(-3.0F, -10.0F, -1.0F, 1.0F, 2.0F, 1.0F)
                .texOffs(60, 35).addBox(-0.5F, -11.0F, -2.0F, 1.0F, 3.0F, 1.0F)
                .texOffs(52, 44).addBox(2.0F, -10.0F, 0.5F, 1.0F, 2.0F, 1.0F),
                PartPose.ZERO);
        PartDefinition cl_book = root.addOrReplaceChild("cl_book", CubeListBuilder.create()
                .texOffs(16, 134).addBox(-4.0F, -0.5F, -2.5F, 8.0F, 1.0F, 5.0F),
                PartPose.offset(0.0F, 19.4F, -5.5F));
        PartDefinition bm_beard = head.addOrReplaceChild("bm_beard", CubeListBuilder.create()
                .texOffs(0, 54).addBox(-3.0F, -2.5F, -4.7F, 6.0F, 7.0F, 1.0F),
                PartPose.ZERO);
        PartDefinition bm_coil = body.addOrReplaceChild("bm_coil", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-1.0F, -8.0F, -2.7F, 2.0F, 16.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.0F, 0.0F, 0.7F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 192);
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float breath = Mth.sin(t * 0.05F);
        this.leftLeg.xRot = 0.0F;
        this.rightLeg.xRot = 0.0F;
        switch (this.who) {
            case CANDLEWIFE -> {
                // Both hands round the candle before her; her lips move.
                this.arms(-0.95F + breath * 0.02F, -0.35F, 0.1F, -0.85F + breath * 0.02F, 0.45F, -0.1F);
                this.head.xRot = this.head.xRot * 0.5F + 0.2F;
            }
            case CARVER -> {
                // Bent to his stone: the chisel held to it, the mallet coming down on it now and then.
                float tap = Math.max(0.0F, Mth.sin(t * 0.35F)) * (Mth.sin(t * 0.05F) > -0.3F ? 1.0F : 0.0F);
                this.arms(-1.05F - tap * 0.35F, -0.1F, 0.0F, -1.0F, 0.25F, 0.0F);
                this.head.xRot = this.head.xRot * 0.3F + 0.35F;
                this.mallet.xRot = -tap * 0.3F;
            }
            case FERRYMAN -> this.arms(-0.45F, 0.0F, 0.05F, 0.08F + breath * 0.02F, 0.0F, -0.06F);
            case HANGMAN -> {
                this.arms(0.05F + breath * 0.02F, 0.0F, 0.08F, -0.25F, 0.0F, -0.08F);
                this.noose.zRot = Mth.sin(t * 0.07F) * 0.08F;
            }
            case DEBTOR -> {
                this.sit(breath);
                this.arms(-0.85F, 0.0F, -0.25F, -0.8F, 0.0F, 0.25F);
                this.lap(-1.2F);
            }
            case CELLARER -> {
                // His eyes on the book in his lap, the right hand marking it.
                this.sit(breath);
                this.arms(-1.15F + Mth.sin(t * 0.21F) * 0.04F, 0.15F, -0.1F, -1.1F, -0.2F, 0.1F);
                this.lap(-1.0F);
                this.head.xRot = 0.55F;
            }
            case BELLMAN -> {
                // A hand cupped behind his ear: he is listening for the bell.
                this.sit(breath);
                this.arms(-0.85F, 0.0F, -0.25F, -2.6F, -0.3F, -0.35F);
                this.rightArm.z -= 1.2F;
            }
        }
    }

    private void arms(float rx, float ry, float rz, float lx, float ly, float lz) {
        this.rightArm.xRot = rx;
        this.rightArm.yRot = ry;
        this.rightArm.zRot = rz;
        this.leftArm.xRot = lx;
        this.leftArm.yRot = ly;
        this.leftArm.zRot = lz;
    }

    /** On the floor with the legs straight out before him, a little apart, the back rounded. */
    private void sit(float breath) {
        for (ModelPart part : new ModelPart[] {this.head, this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg}) {
            part.y += SIT_DROP;
        }
        this.rightLeg.xRot = -1.5F;
        this.leftLeg.xRot = -1.45F;
        this.rightLeg.yRot = 0.2F;
        this.leftLeg.yRot = -0.18F;
        this.body.xRot = 0.12F + breath * 0.012F;
        this.head.xRot = this.head.xRot * 0.5F + 0.25F;
        this.head.z -= 0.8F;
    }

    /** Both hands brought forward into the lap. */
    private void lap(float dz) {
        this.rightArm.z += dz;
        this.leftArm.z += dz;
    }
}
