package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.ChaplainEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * The Chaplain of the Stockade. The mesh between the markers is written by tools/gen_chaplain.py (which also paints
 * the textures); the animation is written by hand.
 */
public class ChaplainModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("chaplain"), "main");
    private static final float DEG = (float) (Math.PI / 180.0);

    private final ModelPart robe;
    private final ModelPart torso;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart censerChain;
    private final ModelPart censer;

    public ChaplainModel(ModelPart root) {
        super(root);
        this.robe = root.getChild("robe");
        this.torso = root.getChild("torso");
        this.head = this.torso.getChild("head");
        this.rightArm = this.torso.getChild("right_arm");
        this.leftArm = this.torso.getChild("left_arm");
        this.censerChain = this.rightArm.getChild("censer_chain");
        this.censer = this.censerChain.getChild("censer");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 96);
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);

        // He does not walk: he hangs a hand's breadth above the floor and drifts.
        float bob = Mth.sin(age * 0.08F) * 0.8F - 1.5F;
        this.robe.y += bob;
        this.torso.y += bob;
        this.robe.xRot += 0.18F * speed + Mth.sin(age * 0.11F) * 0.02F;
        this.torso.xRot += 0.1F * speed;
        this.robe.zRot += Mth.sin(age * 0.06F) * 0.025F;

        this.head.yRot += Mth.clamp(state.yRot, -70.0F, 70.0F) * DEG;
        this.head.xRot += Mth.clamp(state.xRot, -40.0F, 40.0F) * DEG;

        // Idle: one hand holds the censer out to the side, the other is folded on the chest.
        this.rightArm.zRot += 0.3F;
        this.rightArm.xRot += -0.35F;
        this.leftArm.xRot += -0.75F;
        this.leftArm.yRot += -0.5F;
        float pendulum = Mth.sin(age * 0.12F) * (0.35F + 0.3F * speed);
        this.censerChain.zRot += -0.3F + pendulum * 0.4F;
        this.censerChain.xRot += 0.35F + pendulum;
        this.censer.xRot += pendulum * 0.5F;

        float t = state.attackTime;
        switch (state.attackId) {
            case ChaplainEntity.SPEARS -> {
                // The free hand points at the target and flicks with every spear.
                float aim = Anim.pulse(t, 0.0F, 9.0F, 34.0F, 45.0F);
                float flick = Math.max(0.0F, Mth.sin(t * (float) Math.PI / 4.0F)) * aim;
                this.leftArm.xRot = Anim.lerp(aim, this.leftArm.xRot, -1.5F - 0.3F * flick);
                this.leftArm.yRot = Anim.lerp(aim, this.leftArm.yRot, 0.1F);
                this.torso.yRot += 0.2F * aim;
                this.head.xRot -= 0.1F * aim;
            }
            case ChaplainEntity.WRATH -> {
                // Both arms rise to the ceiling while the ring closes, and fall as the burst goes off.
                float rise = Anim.pulse(t, 0.0F, 22.0F, 29.0F, 32.0F);
                float fall = Anim.pulse(t, 29.0F, 32.0F, 40.0F, 51.0F);
                this.rightArm.xRot = Anim.lerp(rise, this.rightArm.xRot, -2.8F);
                this.leftArm.xRot = Anim.lerp(rise, this.leftArm.xRot, -2.8F);
                this.leftArm.yRot = Anim.lerp(Math.max(rise, fall), this.leftArm.yRot, 0.0F);
                this.rightArm.zRot += 0.3F * rise + 0.9F * fall;
                this.leftArm.zRot -= 0.6F * rise + 1.2F * fall;
                this.rightArm.xRot = Anim.lerp(fall, this.rightArm.xRot, -0.2F);
                this.leftArm.xRot = Anim.lerp(fall, this.leftArm.xRot, -0.2F);
                this.torso.xRot += -0.25F * rise + 0.3F * fall;
                this.head.xRot += -0.5F * rise + 0.3F * fall;
                this.robe.y -= 2.0F * rise;
                this.torso.y -= 2.0F * rise;
            }
            case ChaplainEntity.BLINK -> {
                // Folds into a line and unfolds somewhere else.
                float gone = Anim.pulse(t, 0.0F, 6.0F, 8.0F, 16.0F);
                float width = 1.0F - 0.95F * gone;
                this.root.xScale = width;
                this.root.zScale = width;
                this.root.yScale = 1.0F + 0.25F * gone;
                this.root.y -= 6.0F * gone;
            }
            case ChaplainEntity.CENSER -> {
                // The censer goes back over the shoulder and comes round on the full length of its chain.
                float draw = Anim.pulse(t, 0.0F, 9.0F, 11.0F, 14.5F);
                float swing = Anim.pulse(t, 11.0F, 14.5F, 20.0F, 29.0F);
                this.rightArm.xRot = Anim.lerp(draw, this.rightArm.xRot, 0.9F);
                this.rightArm.xRot = Anim.lerp(swing, this.rightArm.xRot, -1.6F);
                this.rightArm.yRot += -0.7F * draw + 0.8F * swing;
                this.censerChain.xRot = Anim.lerp(Math.max(draw, swing), this.censerChain.xRot, 0.1F - 1.2F * draw);
                this.censerChain.zRot = Anim.lerp(swing, this.censerChain.zRot, 0.0F);
                this.torso.yRot += -0.5F * draw + 0.55F * swing;
                this.torso.xRot += 0.25F * swing;
            }
            case ChaplainEntity.LITANY -> {
                // Hands together, head bowed, swaying.
                float pray = Anim.pulse(t, 0.0F, 12.0F, 78.0F, 89.0F);
                this.rightArm.xRot = Anim.lerp(pray, this.rightArm.xRot, -1.25F);
                this.leftArm.xRot = Anim.lerp(pray, this.leftArm.xRot, -1.25F);
                this.rightArm.yRot += -0.55F * pray;
                this.leftArm.yRot = Anim.lerp(pray, this.leftArm.yRot, 0.55F);
                this.rightArm.zRot -= 0.3F * pray;
                this.head.xRot += 0.45F * pray;
                this.torso.zRot += Mth.sin(age * 0.2F) * 0.06F * pray;
                this.torso.xRot += 0.12F * pray;
            }
            case ChaplainEntity.CALL -> {
                // Arms wide, face to the ceiling.
                float open = Anim.pulse(t, 0.0F, 14.0F, 36.0F, 49.0F);
                this.rightArm.xRot = Anim.lerp(open, this.rightArm.xRot, -0.4F);
                this.leftArm.xRot = Anim.lerp(open, this.leftArm.xRot, -0.4F);
                this.leftArm.yRot = Anim.lerp(open, this.leftArm.yRot, 0.0F);
                this.rightArm.zRot += 1.5F * open;
                this.leftArm.zRot -= 1.8F * open;
                this.head.xRot -= 0.6F * open;
                this.torso.xRot -= 0.2F * open;
                this.robe.y -= 3.0F * open;
                this.torso.y -= 3.0F * open;
            }
            default -> {
            }
        }
    }
}
