package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.LongarmEntity;

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
 * The long-armed: a stain of dark on a wall, and the arms that come out of it. The mesh between the markers is written
 * by tools/gen_dusk.py (which also paints the texture and draws the poses below on a sheet); the animation is written
 * by hand. The small arms are always out and feel about; the two long ones lie folded behind the stain, show their
 * hands first, and then go out straight to take hold, or wide to rake.
 */
public class LongarmModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("longarm"), "main");
    /** Must match the scale in the generator. */
    private static final float MODEL_SCALE = 2.0F;
    private static final int FEELERS = 6;
    /** Right, left: which way each is turned out by a pose that turns the right one by a number. */
    private static final float[] SIDE = {1.0F, -1.0F};

    private final ModelPart pool;
    private final ModelPart[] arm = new ModelPart[2];
    private final ModelPart[] fore = new ModelPart[2];
    private final ModelPart[] wrist = new ModelPart[2];
    private final ModelPart[] hand = new ModelPart[2];
    private final ModelPart[] feeler = new ModelPart[FEELERS];
    private final ModelPart[] feelerHand = new ModelPart[FEELERS];

    public LongarmModel(ModelPart root) {
        super(root);
        this.pool = root.getChild("all").getChild("pool");
        String[] sides = {"right", "left"};
        for (int i = 0; i < 2; i++) {
            this.arm[i] = this.pool.getChild(sides[i] + "_arm");
            this.fore[i] = this.arm[i].getChild(sides[i] + "_fore");
            this.wrist[i] = this.fore[i].getChild(sides[i] + "_wrist");
            this.hand[i] = this.wrist[i].getChild(sides[i] + "_hand");
        }
        for (int i = 0; i < FEELERS; i++) {
            this.feeler[i] = this.pool.getChild("feel" + i);
            this.feelerHand[i] = this.feeler[i].getChild("feelh" + i);
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition pool = all.addOrReplaceChild("pool", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-12.0F, -27.0F, -0.6F, 24.0F, 27.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, -4.0F));
        PartDefinition right_arm = pool.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(50, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F),
                PartPose.offset(-2.6F, -17.0F, 2.4F));
        PartDefinition right_fore = right_arm.addOrReplaceChild("right_fore", CubeListBuilder.create()
                .texOffs(58, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 3.0F, 0.0F, 0.0F));
        PartDefinition right_wrist = right_fore.addOrReplaceChild("right_wrist", CubeListBuilder.create()
                .texOffs(82, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 9.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, -3.0F, 0.0F, 0.0F));
        PartDefinition right_hand = right_wrist.addOrReplaceChild("right_hand", CubeListBuilder.create()
                .texOffs(98, 0).addBox(-3.0F, 0.0F, -0.5F, 6.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 9.0F, 0.0F, 3.0F, 0.0F, 0.0F));
        PartDefinition left_arm = pool.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(66, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F),
                PartPose.offset(2.6F, -17.0F, 2.4F));
        PartDefinition left_fore = left_arm.addOrReplaceChild("left_fore", CubeListBuilder.create()
                .texOffs(74, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 3.0F, 0.0F, 0.0F));
        PartDefinition left_wrist = left_fore.addOrReplaceChild("left_wrist", CubeListBuilder.create()
                .texOffs(90, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 9.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, -3.0F, 0.0F, 0.0F));
        PartDefinition left_hand = left_wrist.addOrReplaceChild("left_hand", CubeListBuilder.create()
                .texOffs(112, 0).addBox(-3.0F, 0.0F, -0.5F, 6.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 9.0F, 0.0F, 3.0F, 0.0F, 0.0F));
        PartDefinition feel0 = pool.addOrReplaceChild("feel0", CubeListBuilder.create()
                .texOffs(102, 9).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(-7.5F, -19.0F, 0.0F, -1.0708F, 0.5F, 0.0F));
        PartDefinition feelh0 = feel0.addOrReplaceChild("feelh0", CubeListBuilder.create()
                .texOffs(82, 11).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, -0.4F, 0.0F, -0.3F));
        PartDefinition feel1 = pool.addOrReplaceChild("feel1", CubeListBuilder.create()
                .texOffs(98, 9).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 7.0F, 1.0F),
                PartPose.offsetAndRotation(6.5F, -21.0F, 0.0F, -2.1708F, -0.5F, 0.0F));
        PartDefinition feelh1 = feel1.addOrReplaceChild("feelh1", CubeListBuilder.create()
                .texOffs(90, 11).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 0.5F, 0.0F, 0.3F));
        PartDefinition feel2 = pool.addOrReplaceChild("feel2", CubeListBuilder.create()
                .texOffs(114, 9).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(-8.5F, -10.0F, 0.0F, -0.8708F, 0.9F, 0.0F));
        PartDefinition feelh2 = feel2.addOrReplaceChild("feelh2", CubeListBuilder.create()
                .texOffs(50, 12).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, -0.4F, 0.0F, -0.3F));
        PartDefinition feel3 = pool.addOrReplaceChild("feel3", CubeListBuilder.create()
                .texOffs(106, 9).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(8.0F, -12.0F, 0.0F, -1.3708F, -0.8F, 0.0F));
        PartDefinition feelh3 = feel3.addOrReplaceChild("feelh3", CubeListBuilder.create()
                .texOffs(58, 12).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.5F, 0.0F, 0.3F));
        PartDefinition feel4 = pool.addOrReplaceChild("feel4", CubeListBuilder.create()
                .texOffs(118, 9).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(1.5F, -24.5F, 0.0F, -2.4708F, 0.1F, 0.0F));
        PartDefinition feelh4 = feel4.addOrReplaceChild("feelh4", CubeListBuilder.create()
                .texOffs(66, 12).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, -0.4F, 0.0F, -0.3F));
        PartDefinition feel5 = pool.addOrReplaceChild("feel5", CubeListBuilder.create()
                .texOffs(110, 9).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(-2.5F, -5.5F, 0.0F, -0.6708F, 0.2F, 0.0F));
        PartDefinition feelh5 = feel5.addOrReplaceChild("feelh5", CubeListBuilder.create()
                .texOffs(74, 12).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.5F, 0.0F, 0.3F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 128, 64).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    /**
     * Both long arms {@code amount} of the way from where they are to a pose of the sheet: the turns of the shoulder,
     * the elbow, the wrist and the hand from where they lie folded, and how far the arms are turned out to the sides.
     */
    private void arms(float amount, float upper, float fore, float wrist, float hand, float yaw) {
        for (int i = 0; i < 2; i++) {
            Anim.pose(this.arm[i], amount, upper, SIDE[i] * yaw, 0.0F);
            Anim.pose(this.fore[i], amount, fore, 0.0F, 0.0F);
            Anim.pose(this.wrist[i], amount, wrist, 0.0F, 0.0F);
            Anim.pose(this.hand[i], amount, hand, 0.0F, 0.0F);
        }
    }

    /** The hands out of the stain, the arms behind them folded like a mantis's: what is seen before either blow. */
    private void peek(float amount) {
        this.arms(amount, -1.5708F, -0.42F, 0.42F, -3.0F, 0.0F);
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float t = state.attackTime;
        // the small arms feel about the edge of it, each in its own time
        for (int i = 0; i < FEELERS; i++) {
            this.feeler[i].xRot += Mth.sin(age * (0.045F + i * 0.011F) + i * 1.7F) * 0.3F;
            this.feeler[i].yRot += Mth.sin(age * (0.037F + i * 0.008F) + i * 2.3F) * 0.25F;
            this.feelerHand[i].xRot += Math.max(0.0F, Mth.sin(age * (0.13F + i * 0.02F) + i)) * 0.6F;
        }
        switch (state.attackId) {
            case LongarmEntity.GRAB -> {
                float impact = LongarmEntity.GRAB_IMPACT;
                float hands = Anim.ramp(t, 0.0F, impact - 9.0F) * (1.0F - Anim.ramp(t, LongarmEntity.GRAB_DURATION - 9.0F, LongarmEntity.GRAB_DURATION));
                float out = Anim.pulse(t, impact - 4.0F, impact, impact + 6.0F, impact + 24.0F);
                float pull = Anim.pulse(t, impact + 2.0F, impact + 12.0F, impact + 22.0F, LongarmEntity.GRAB_DURATION - 6.0F);
                this.peek(hands);
                this.arms(out, -1.5708F, -3.0F, 3.0F, -3.3F, 0.0F);
                // and back, with what it has: the elbows fold again
                for (int i = 0; i < 2; i++) {
                    this.fore[i].xRot += 1.1F * pull;
                    this.wrist[i].xRot -= 1.1F * pull;
                }
                // the small ones shrink from it
                for (int i = 0; i < FEELERS; i++) {
                    this.feelerHand[i].xRot += 0.8F * out;
                }
            }
            case LongarmEntity.RAKE -> {
                float impact = LongarmEntity.RAKE_IMPACT;
                float hands = Anim.ramp(t, 0.0F, impact - 14.0F) * (1.0F - Anim.ramp(t, LongarmEntity.RAKE_DURATION - 9.0F, LongarmEntity.RAKE_DURATION));
                float wide = Anim.pulse(t, impact - 14.0F, impact - 6.0F, impact + 12.0F, LongarmEntity.RAKE_DURATION - 8.0F);
                float through = Anim.ramp(t, impact - 4.0F, impact + 3.0F);
                this.peek(hands);
                this.arms(wide, -1.5708F, -3.0F, 3.0F, -3.2F, -1.0F + 1.55F * through);
            }
            default -> {
            }
        }
    }
}
