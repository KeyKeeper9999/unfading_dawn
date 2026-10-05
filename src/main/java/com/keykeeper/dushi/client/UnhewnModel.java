package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.UnhewnEntity;

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
 * The Unhewn. The mesh between the markers is written by tools/gen_unhewn.py (which also paints the textures); the
 * animation is written by hand. While it sleeps (phase 1) nothing of it moves at all: it is a statue.
 */
public class UnhewnModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("unhewn"), "main");
    /** Must match SCALE in tools/gen_unhewn.py. */
    private static final float MODEL_SCALE = 1.1F;
    private static final float DEG = (float) (Math.PI / 180.0);

    private final ModelPart base;
    private final ModelPart torso;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart rightForearm;
    private final ModelPart leftArm;

    public UnhewnModel(ModelPart root) {
        super(root);
        this.base = root.getChild("base");
        this.torso = this.base.getChild("torso");
        this.head = this.torso.getChild("head");
        this.rightArm = this.torso.getChild("right_arm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftArm = this.torso.getChild("left_arm");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition base = root.addOrReplaceChild("base", CubeListBuilder.create()
                .texOffs(28, 24).addBox(-8.0F, -9.0F, -7.0F, 16.0F, 9.0F, 14.0F)
                .texOffs(76, 0).addBox(-7.0F, -15.0F, -6.0F, 14.0F, 6.0F, 12.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition torso = base.addOrReplaceChild("torso", CubeListBuilder.create()
                .texOffs(28, 0).addBox(-7.0F, -14.0F, -5.0F, 14.0F, 14.0F, 10.0F),
                PartPose.offset(0.0F, -15.0F, 0.0F));
        PartDefinition head = torso.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(88, 18).addBox(-4.0F, -9.0F, -4.5F, 8.0F, 9.0F, 9.0F)
                .texOffs(88, 36).addBox(-7.0F, -13.0F, 4.0F, 14.0F, 14.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -14.0F, 0.0F, 0.12F, 0.0F, 0.0F));
        PartDefinition right_arm = torso.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 30).addBox(-4.0F, -1.5F, -2.5F, 4.0F, 11.0F, 5.0F),
                PartPose.offsetAndRotation(-7.0F, -12.0F, 0.0F, -0.25F, 0.0F, 0.08F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(0, 46).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 8.0F, 3.0F)
                .texOffs(12, 46).addBox(-2.0F, 8.0F, -1.0F, 4.0F, 5.0F, 2.0F),
                PartPose.offsetAndRotation(-2.0F, 9.0F, 0.0F, -2.1F, 0.0F, 0.0F));
        PartDefinition left_arm = torso.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0.0F, -2.5F, -3.5F, 7.0F, 23.0F, 7.0F),
                PartPose.offsetAndRotation(7.0F, -12.0F, 0.0F, 0.0F, 0.0F, -0.06F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 128, 128).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        if (state.phase < 2) {
            return;
        }
        float age = state.ageInTicks;
        float speed = Math.min(1.0F, state.walkAnimationSpeed * 2.5F);
        float walk = state.walkAnimationPos * 2.2F;
        float t = state.attackTime;
        // how much of it is awake yet: the waking move brings it up from nothing
        float life = state.attackId == UnhewnEntity.WAKE ? Anim.ramp(t, 4.0F, 30.0F) : 1.0F;

        // Awake it never stands quite still: the block arm drags the shoulder down, the head follows whoever it wants.
        float sway = Mth.sin(age * 0.05F) * life;
        this.torso.zRot += 0.05F * life + sway * 0.02F;
        this.leftArm.xRot += sway * 0.04F;
        this.head.xRot -= 0.12F * life;
        this.head.yRot += Mth.clamp(state.yRot, -45.0F, 45.0F) * DEG * 0.8F * life;

        // It has no legs: it walks its plinth, corner by corner, the way masons walk a block.
        float rock = Mth.sin(walk);
        this.base.zRot += rock * 0.13F * speed;
        this.base.yRot += Mth.cos(walk) * 0.16F * speed;
        this.base.y -= Math.abs(rock) * 1.2F * speed;
        this.torso.zRot -= rock * 0.1F * speed;
        this.leftArm.xRot += Mth.cos(walk) * 0.22F * speed;
        this.leftArm.zRot -= Math.max(0.0F, rock) * 0.12F * speed;

        switch (state.attackId) {
            case UnhewnEntity.WAKE -> {
                // A shudder runs up it; it tears loose from its bed and the head comes up.
                float shake = Anim.pulse(t, 0.0F, 6.0F, 22.0F, 32.0F);
                this.base.zRot += Mth.sin(t * 2.1F) * 0.05F * shake;
                this.base.xRot += Mth.sin(t * 1.3F) * 0.03F * shake;
                this.torso.yRot += Mth.sin(t * 0.9F) * 0.12F * shake;
                this.leftArm.xRot -= 0.35F * Anim.pulse(t, 10.0F, 20.0F, 26.0F, 35.0F);
                this.head.xRot -= 0.3F * Anim.pulse(t, 14.0F, 22.0F, 28.0F, 35.0F);
            }
            case UnhewnEntity.SMASH -> {
                // The block goes up over the head - slowly, it is a block - hangs, and comes down with the whole trunk behind it.
                float raise = Anim.pulse(t, 0.0F, 17.0F, 21.0F, 25.0F);
                float strike = Anim.pulse(t, 21.0F, 25.0F, 42.0F, 57.0F);
                this.leftArm.xRot = Anim.lerp(raise, this.leftArm.xRot, -2.85F);
                this.leftArm.xRot = Anim.lerp(strike, this.leftArm.xRot, -1.0F);
                this.leftArm.zRot += 0.1F * Math.max(raise, strike);
                this.torso.xRot += -0.25F * raise + 0.5F * strike;
                this.torso.yRot += 0.2F * raise - 0.1F * strike;
                this.head.xRot += -0.2F * raise + 0.1F * strike;
                this.rightArm.xRot += 0.5F * raise + 0.6F * strike;
                this.base.xRot += 0.08F * strike;
            }
            case UnhewnEntity.SHOVE -> {
                // The blessing hand is drawn back to the shoulder and put out flat, with the trunk turning in behind it.
                float draw = Anim.pulse(t, 0.0F, 9.0F, 12.0F, 15.0F);
                float push = Anim.pulse(t, 12.0F, 15.0F, 22.0F, 37.0F);
                this.rightArm.xRot += 0.5F * draw - 1.05F * push;
                this.rightForearm.xRot += -0.4F * draw + 1.9F * push;
                this.torso.yRot += -0.35F * draw + 0.35F * push;
                this.torso.xRot += 0.2F * push;
                this.leftArm.xRot += 0.4F * push;
            }
            default -> {
            }
        }
    }
}
