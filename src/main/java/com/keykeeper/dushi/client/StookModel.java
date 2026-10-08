package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.StookEntity;

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
 * The stook of the Stubble. The mesh between the markers is written by tools/gen_stook.py (which also paints the
 * texture and draws the poses below on a sheet); the animation is written by hand. Standing (phase 1) it does not
 * move at all. Walking, it rocks from one pair of sheaves to the other; it rears and falls its length; it shakes
 * itself open.
 */
public class StookModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("stook"), "main");
    /** Must match SCALE in tools/gen_stook.py. */
    private static final float MODEL_SCALE = 1.25F;

    private final ModelPart all;
    private final ModelPart cap;
    private final ModelPart front;
    private final ModelPart back;
    private final ModelPart right;
    private final ModelPart left;

    public StookModel(ModelPart root) {
        super(root);
        this.all = root.getChild("all");
        ModelPart body = this.all.getChild("body");
        this.cap = body.getChild("cap");
        this.front = body.getChild("sheaf_f");
        this.back = body.getChild("sheaf_b");
        this.right = body.getChild("sheaf_r");
        this.left = body.getChild("sheaf_l");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(88, 0).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 13.0F, 7.0F),
                PartPose.offset(0.0F, -19.0F, 0.0F));
        PartDefinition sheaf_f = body.addOrReplaceChild("sheaf_f", CubeListBuilder.create()
                .texOffs(44, 0).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 19.0F, 5.0F)
                .texOffs(100, 30).addBox(-3.5F, -2.0F, -3.5F, 7.0F, 3.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 1.0F, -2.0F, -0.27F, 0.0F, 0.0F));
        PartDefinition sheaf_b = body.addOrReplaceChild("sheaf_b", CubeListBuilder.create()
                .texOffs(66, 0).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 19.0F, 5.0F)
                .texOffs(100, 39).addBox(-3.5F, -2.0F, -2.5F, 7.0F, 3.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 1.0F, 2.0F, 0.27F, 0.0F, 0.0F));
        PartDefinition sheaf_r = body.addOrReplaceChild("sheaf_r", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0F, 0.0F, -3.0F, 5.0F, 19.0F, 6.0F)
                .texOffs(100, 20).addBox(-3.5F, -2.0F, -3.5F, 6.0F, 3.0F, 7.0F),
                PartPose.offsetAndRotation(-2.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.27F));
        PartDefinition sheaf_l = body.addOrReplaceChild("sheaf_l", CubeListBuilder.create()
                .texOffs(22, 0).addBox(-2.0F, 0.0F, -3.0F, 5.0F, 19.0F, 6.0F)
                .texOffs(0, 25).addBox(-2.5F, -2.0F, -3.5F, 6.0F, 3.0F, 7.0F),
                PartPose.offsetAndRotation(2.0F, 1.0F, 0.0F, 0.0F, 0.0F, -0.27F));
        PartDefinition cap = body.addOrReplaceChild("cap", CubeListBuilder.create()
                .texOffs(48, 40).addBox(-2.5F, -6.0F, -2.5F, 5.0F, 4.0F, 5.0F)
                .texOffs(0, 40).addBox(-6.0F, -2.5F, -6.0F, 12.0F, 4.0F, 12.0F)
                .texOffs(44, 24).addBox(-7.0F, 1.5F, -7.0F, 14.0F, 2.0F, 14.0F),
                PartPose.ZERO);
        // </generated mesh>
        return LayerDefinition.create(mesh, 128, 64).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        if (state.phase < 2) {
            return;                                              // a stook
        }
        float t = state.attackTime;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float rock = Mth.sin(state.walkAnimationPos * 0.9F) * speed;
        // it goes on the butts of its sheaves, two and two
        this.all.zRot += rock * 0.14F;
        this.right.zRot += Math.max(0.0F, rock) * 0.3F - Math.max(0.0F, -rock) * 0.1F;
        this.left.zRot += Math.max(0.0F, rock) * 0.1F - Math.max(0.0F, -rock) * 0.3F;
        this.front.xRot -= rock * 0.3F;
        this.back.xRot -= rock * 0.3F;
        this.cap.yRot += Mth.sin(state.ageInTicks * 0.07F) * 0.05F;
        switch (state.attackId) {
            case StookEntity.RISE -> {
                float lift = Anim.pulse(t, 0.0F, 6.0F, 10.0F, StookEntity.RISE_DURATION);
                this.all.y -= 2.5F * lift;
                this.front.xRot -= 0.3F * lift;
                this.back.xRot += 0.3F * lift;
                this.right.zRot += 0.3F * lift;
                this.left.zRot -= 0.3F * lift;
            }
            case StookEntity.FALL -> {
                float impact = StookEntity.FALL_IMPACT;
                float rear = Anim.pulse(t, 0.0F, 14.0F, impact - 6.0F, impact - 2.0F);
                float lain = Anim.pulse(t, impact - 6.0F, impact, StookEntity.FALL_LAIN, StookEntity.FALL_DURATION - 4.0F);
                this.all.xRot += -0.5F * rear + 1.4F * lain;
                this.all.y -= 2.0F * lain;
                this.all.z -= 4.0F * lain;
                this.front.xRot += -0.5F * rear + 0.15F * lain;
                this.back.xRot += 0.3F * rear;
                this.right.zRot += 0.25F * lain;
                this.left.zRot -= 0.25F * lain;
                this.cap.xRot += -0.2F * rear + 0.2F * lain;
            }
            case StookEntity.SHED -> {
                float burst = StookEntity.SHED_BURST;
                float gather = Anim.pulse(t, 0.0F, 12.0F, burst - 3.0F, burst);
                float open = Anim.pulse(t, burst - 3.0F, burst, burst + 10.0F, StookEntity.SHED_DURATION);
                float shake = Mth.sin(t * 2.6F) * 0.08F * gather;
                this.all.yRot += shake * 4.0F;
                this.all.zRot += shake;
                this.front.xRot += 0.2F * gather - 0.45F * open;
                this.back.xRot += -0.2F * gather + 0.45F * open;
                this.right.zRot += -0.2F * gather + 0.45F * open;
                this.left.zRot += 0.2F * gather - 0.45F * open;
                this.cap.y -= 3.0F * open;
            }
            default -> {
            }
        }
    }
}
