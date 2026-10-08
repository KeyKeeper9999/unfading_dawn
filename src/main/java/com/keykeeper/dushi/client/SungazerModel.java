package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.SungazerEntity;

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
 * The sungazer of the Stubble. The mesh between the markers is written by tools/gen_sungazer.py (which also paints the
 * texture and draws the poses below on a sheet); the animation is written by hand. It stands with its head bowed to
 * whoever is before it; to gaze, the neck straightens and the face comes up level; it beats with a leaf by turning
 * its whole stalk.
 */
public class SungazerModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("sungazer"), "main");
    /** Must match SCALE in tools/gen_sungazer.py. */
    private static final float MODEL_SCALE = 1.375F;

    private final ModelPart all;
    private final ModelPart stem;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart leafRight;
    private final ModelPart leafLeft;
    private final ModelPart rootFront;
    private final ModelPart rootRight;
    private final ModelPart rootLeft;

    public SungazerModel(ModelPart root) {
        super(root);
        this.all = root.getChild("all");
        this.stem = this.all.getChild("stem");
        this.neck = this.stem.getChild("neck");
        this.head = this.neck.getChild("head");
        this.leafRight = this.stem.getChild("leaf_r");
        this.leafLeft = this.stem.getChild("leaf_l");
        this.rootFront = this.all.getChild("root_f");
        this.rootRight = this.all.getChild("root_r");
        this.rootLeft = this.all.getChild("root_l");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition root_f = all.addOrReplaceChild("root_f", CubeListBuilder.create()
                .texOffs(54, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -4.0F, -1.0F, -0.6F, 0.0F, 0.0F));
        PartDefinition root_r = all.addOrReplaceChild("root_r", CubeListBuilder.create()
                .texOffs(54, 8).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offsetAndRotation(-1.0F, -4.0F, 1.0F, 0.45F, 0.0F, 0.6F));
        PartDefinition root_l = all.addOrReplaceChild("root_l", CubeListBuilder.create()
                .texOffs(16, 19).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offsetAndRotation(1.0F, -4.0F, 1.0F, 0.45F, 0.0F, -0.6F));
        PartDefinition stem = all.addOrReplaceChild("stem", CubeListBuilder.create()
                .texOffs(38, 0).addBox(-1.0F, -15.0F, -1.0F, 2.0F, 15.0F, 2.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));
        PartDefinition leaf_r = stem.addOrReplaceChild("leaf_r", CubeListBuilder.create()
                .texOffs(0, 27).addBox(-9.0F, -0.5F, -3.0F, 9.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(-1.0F, -9.0F, 0.0F, 0.0F, 0.0F, -0.35F));
        PartDefinition leaf_l = stem.addOrReplaceChild("leaf_l", CubeListBuilder.create()
                .texOffs(30, 30).addBox(0.0F, -0.5F, -3.0F, 9.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(1.0F, -9.0F, 0.0F, 0.0F, 0.0F, 0.35F));
        PartDefinition small_r = stem.addOrReplaceChild("small_r", CubeListBuilder.create()
                .texOffs(0, 34).addBox(-6.0F, -0.5F, -2.0F, 6.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(-1.0F, -4.0F, 0.0F, 0.0F, 0.5F, -0.5F));
        PartDefinition small_l = stem.addOrReplaceChild("small_l", CubeListBuilder.create()
                .texOffs(20, 37).addBox(0.0F, -0.5F, -2.0F, 6.0F, 1.0F, 4.0F),
                PartPose.offsetAndRotation(1.0F, -4.0F, 0.0F, 0.0F, -0.5F, 0.5F));
        PartDefinition neck = stem.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(46, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 8.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -15.0F, 0.0F, 0.35F, 0.0F, 0.0F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(38, 17).addBox(-5.0F, -5.0F, -2.5F, 10.0F, 10.0F, 3.0F)
                .texOffs(0, 0).addBox(-9.0F, -9.0F, -1.5F, 18.0F, 18.0F, 1.0F)
                .texOffs(0, 19).addBox(-3.0F, -3.0F, 0.5F, 6.0F, 6.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, -0.2F, 0.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 64).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float t = state.attackTime;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float step = state.walkAnimationPos * 1.2F;
        // it sways, as a thing on a stalk does; its leaves lift and fall
        this.stem.zRot += Mth.sin(age * 0.05F) * 0.04F;
        this.stem.xRot += Mth.sin(age * 0.037F) * 0.03F;
        this.leafRight.zRot += Mth.sin(age * 0.08F) * 0.08F;
        this.leafLeft.zRot -= Mth.sin(age * 0.08F + 0.7F) * 0.08F;
        // it goes on three roots, one after another
        this.rootFront.xRot -= Math.max(0.0F, Mth.sin(step)) * 0.5F * speed;
        this.rootRight.xRot += Math.max(0.0F, Mth.sin(step + 2.1F)) * 0.4F * speed;
        this.rootLeft.xRot += Math.max(0.0F, Mth.sin(step + 4.2F)) * 0.4F * speed;
        this.all.zRot += Mth.sin(step) * 0.06F * speed;
        switch (state.attackId) {
            case SungazerEntity.GAZE -> {
                // the neck straightens until the face is level with what it looks at, and is held there, trembling
                float up = Anim.pulse(t, 0.0F, SungazerEntity.GAZE_START - 2.0F, SungazerEntity.GAZE_END, SungazerEntity.GAZE_DURATION);
                Anim.pose(this.neck, up, -0.75F, 0.0F, Mth.sin(t * 1.7F) * 0.03F);
                Anim.pose(this.head, up, 0.25F, 0.0F, 0.0F);
                this.stem.xRot -= 0.08F * up;
                this.leafRight.zRot += 0.5F * up;
                this.leafLeft.zRot -= 0.5F * up;
            }
            case SungazerEntity.LASH -> {
                float impact = SungazerEntity.LASH_IMPACT;
                float back = Anim.pulse(t, 0.0F, 9.0F, impact - 4.0F, impact - 1.0F);
                float through = Anim.pulse(t, impact - 4.0F, impact + 1.0F, impact + 8.0F, SungazerEntity.LASH_DURATION);
                this.stem.yRot += 0.9F * back - 1.0F * through;
                this.stem.xRot += 0.15F * through;
                this.neck.yRot += -0.7F * back + 0.7F * through;
                this.leafRight.yRot += 0.5F * back - 0.4F * through;
                this.leafRight.zRot += 0.2F * back + 0.3F * through;
            }
            default -> {
            }
        }
    }
}
