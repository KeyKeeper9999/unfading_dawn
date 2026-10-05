package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.TaskmasterEntity;

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
 * The Taskmaster of the quarry. The mesh between the markers is written by tools/gen_taskmaster.py (which also paints
 * the textures); the animation is written by hand.
 * <p>
 * The maul's pitch is the sum of the arm's, the forearm's, the 1.5 it is set at in the hand and its own: at 0 it stands
 * straight up, at a quarter turn it lies level and forward, at half a turn it hangs. Every move below is thought out in
 * that sum - where the iron is - and the arm follows.
 */
public class TaskmasterModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("taskmaster"), "main");
    /** Must match SCALE in tools/gen_taskmaster.py. */
    private static final float MODEL_SCALE = 1.25F;
    private static final float DEG = (float) (Math.PI / 180.0);

    private final ModelPart body;
    private final ModelPart apron;
    private final ModelPart chest;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightForearm;
    private final ModelPart leftForearm;
    private final ModelPart maul;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightShin;
    private final ModelPart leftShin;

    public TaskmasterModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.apron = this.body.getChild("apron");
        this.chest = this.body.getChild("chest");
        this.head = this.chest.getChild("head");
        this.rightArm = this.chest.getChild("right_arm");
        this.leftArm = this.chest.getChild("left_arm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.maul = this.rightForearm.getChild("maul");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.rightShin = this.rightLeg.getChild("right_shin");
        this.leftShin = this.leftLeg.getChild("left_shin");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(48, 38).addBox(-7.0F, -4.0F, -4.5F, 14.0F, 6.0F, 9.0F),
                PartPose.offset(0.0F, 9.0F, 0.0F));
        PartDefinition apron = body.addOrReplaceChild("apron", CubeListBuilder.create()
                .texOffs(94, 49).addBox(-6.0F, 0.0F, -0.5F, 12.0F, 11.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 2.0F, -4.5F, -0.06F, 0.0F, 0.0F));
        PartDefinition wedges = body.addOrReplaceChild("wedges", CubeListBuilder.create()
                .texOffs(48, 23).addBox(-8.5F, -1.0F, -2.0F, 2.0F, 5.0F, 2.0F)
                .texOffs(118, 36).addBox(-8.5F, -1.0F, 1.0F, 2.0F, 4.0F, 2.0F)
                .texOffs(48, 30).addBox(6.5F, -1.0F, -3.0F, 2.0F, 5.0F, 2.0F)
                .texOffs(32, 39).addBox(6.5F, -1.0F, 0.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition chest = body.addOrReplaceChild("chest", CubeListBuilder.create()
                .texOffs(8, 0).addBox(-8.5F, -13.0F, -5.0F, 17.0F, 13.0F, 10.0F)
                .texOffs(0, 52).addBox(-6.5F, -15.5F, -2.0F, 13.0F, 3.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.3F, 0.0F, 0.0F));
        PartDefinition head = chest.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(62, 22).addBox(-4.0F, -8.0F, -4.5F, 8.0F, 8.0F, 8.0F)
                .texOffs(118, 42).addBox(-1.5F, -9.0F, -6.5F, 3.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -12.5F, -3.5F, -0.22F, 0.0F, 0.0F));
        PartDefinition right_arm = chest.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(98, 0).addBox(-6.0F, -2.5F, -3.0F, 6.0F, 12.0F, 6.0F),
                PartPose.offset(-8.5F, -11.0F, 0.0F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(8, 23).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 11.0F, 5.0F)
                .texOffs(20, 64).addBox(-3.0F, 10.0F, -3.0F, 6.0F, 4.0F, 6.0F),
                PartPose.offset(-3.0F, 9.5F, 0.0F));
        PartDefinition maul = right_forearm.addOrReplaceChild("maul", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.0F, -30.0F, -1.0F, 2.0F, 40.0F, 2.0F)
                .texOffs(62, 0).addBox(-3.5F, -41.0F, -5.5F, 7.0F, 11.0F, 11.0F)
                .texOffs(32, 45).addBox(-1.5F, -31.0F, -1.5F, 3.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 1.5F, 0.0F, 0.0F));
        PartDefinition left_arm = chest.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(98, 18).addBox(0.0F, -2.5F, -3.0F, 6.0F, 12.0F, 6.0F),
                PartPose.offset(8.5F, -11.0F, 0.0F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(28, 23).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 11.0F, 5.0F)
                .texOffs(44, 64).addBox(-3.0F, 10.0F, -3.0F, 6.0F, 4.0F, 6.0F),
                PartPose.offset(3.0F, 9.5F, 0.0F));
        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(94, 36).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 7.0F, 6.0F),
                PartPose.offset(-4.0F, 11.0F, 0.0F));
        PartDefinition right_shin = right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(98, 61).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 6.0F, 5.0F)
                .texOffs(42, 53).addBox(-3.0F, 3.0F, -5.0F, 6.0F, 3.0F, 8.0F),
                PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(8, 39).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 7.0F, 6.0F),
                PartPose.offset(4.0F, 11.0F, 0.0F));
        PartDefinition left_shin = left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(0, 63).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 6.0F, 5.0F)
                .texOffs(70, 61).addBox(-3.0F, 3.0F, -5.0F, 6.0F, 3.0F, 8.0F),
                PartPose.offset(0.0F, 7.0F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 128, 128).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float walk = state.walkAnimationPos * 0.55F;
        boolean roused = state.phase >= 2;

        // Idle: the maul stood upright before him in his right hand like a staff of office, the left hanging; he breathes
        // like a bellows, faster once he is hurt.
        float breath = Mth.sin(age * (roused ? 0.13F : 0.065F));
        this.chest.xRot += breath * 0.03F;
        this.head.xRot += breath * 0.02F;
        this.rightArm.xRot += -0.45F;
        this.rightArm.zRot += 0.12F;
        this.rightForearm.xRot += -1.75F;
        this.maul.xRot += -0.1F + breath * 0.02F;
        this.leftArm.xRot += 0.1F;
        this.leftArm.zRot -= 0.12F;
        this.leftForearm.xRot += -0.3F;
        this.head.yRot += Mth.clamp(state.yRot, -50.0F, 50.0F) * DEG * 0.8F;
        this.head.xRot += Mth.clamp(state.xRot, -30.0F, 30.0F) * DEG * 0.6F;

        // Walking: short heavy steps, the trunk rolling over each of them.
        float step = Mth.cos(walk);
        this.rightLeg.xRot += step * 0.65F * speed;
        this.leftLeg.xRot -= step * 0.65F * speed;
        this.rightShin.xRot += Math.max(0.0F, Mth.sin(walk)) * 0.6F * speed;
        this.leftShin.xRot += Math.max(0.0F, -Mth.sin(walk)) * 0.6F * speed;
        this.body.y += Math.abs(Mth.sin(walk)) * 0.9F * speed;
        this.chest.zRot += step * 0.06F * speed;
        this.chest.yRot += step * 0.08F * speed;
        this.leftArm.xRot += step * 0.4F * speed;
        this.apron.xRot -= Math.abs(step) * 0.3F * speed;

        float t = state.attackTime;
        switch (state.attackId) {
            case TaskmasterEntity.SLAM -> this.slam(t, 14.0F, 18.0F, 22.0F, 40.0F, 53.0F, 0.0F);
            case TaskmasterEntity.SWEEP -> {
                // Both hands on the shaft; the maul is laid level far out to his right and brought round at the height of the ribs.
                float draw = Anim.pulse(t, 0.0F, 13.0F, 16.0F, 19.0F);
                float sweep = Anim.pulse(t, 16.0F, 19.0F, 27.0F, 45.0F);
                float out = Math.max(draw, sweep);
                this.rightArm.xRot = Anim.lerp(out, this.rightArm.xRot, -1.35F);
                this.rightForearm.xRot = Anim.lerp(out, this.rightForearm.xRot, -0.15F);
                this.maul.xRot = Anim.lerp(out, this.maul.xRot, 1.1F);
                this.rightArm.yRot += -1.1F * draw + 0.9F * sweep;
                this.chest.yRot += -0.7F * draw + 0.75F * sweep;
                this.chest.xRot += -0.2F * out;
                this.leftArm.xRot = Anim.lerp(out, this.leftArm.xRot, -1.1F);
                this.leftArm.yRot += 0.5F * draw - 0.2F * sweep;
                this.leftForearm.xRot = Anim.lerp(out, this.leftForearm.xRot, -0.5F);
                this.rightLeg.xRot += 0.25F * draw - 0.3F * sweep;
                this.leftLeg.xRot += -0.25F * draw + 0.3F * sweep;
            }
            case TaskmasterEntity.WEDGE -> {
                // Down on one knee; the left hand stands a wedge on the floor and steadies it, the right swings the maul up
                // one-handed and drops it on the wedge's head. He stays down while the crack runs.
                float kneel = Anim.pulse(t, 0.0F, 10.0F, 48.0F, 66.0F);
                float set = Anim.pulse(t, 4.0F, 12.0F, 27.0F, 32.0F);
                float raise = Anim.pulse(t, 13.0F, 24.0F, 27.0F, 30.0F);
                float strike = Anim.pulse(t, 27.0F, 30.0F, 44.0F, 58.0F);
                this.body.y += 5.0F * kneel;
                this.chest.xRot += 0.45F * kneel - 0.15F * raise + 0.15F * strike;
                this.head.xRot -= 0.3F * kneel;
                this.rightLeg.xRot -= 1.3F * kneel;
                this.rightShin.xRot += 1.5F * kneel;
                this.leftLeg.xRot -= 0.2F * kneel;
                this.leftShin.xRot += 1.4F * kneel;
                this.apron.xRot -= 0.9F * kneel;
                this.leftArm.xRot = Anim.lerp(set, this.leftArm.xRot, -0.9F);
                this.leftArm.zRot += 0.3F * set;
                this.leftForearm.xRot = Anim.lerp(set, this.leftForearm.xRot, -0.5F);
                this.rightArm.xRot = Anim.lerp(raise, this.rightArm.xRot, -2.3F);
                this.rightForearm.xRot = Anim.lerp(raise, this.rightForearm.xRot, -0.6F);
                this.maul.xRot = Anim.lerp(raise, this.maul.xRot, 0.2F);
                this.rightArm.xRot = Anim.lerp(strike, this.rightArm.xRot, -0.75F);
                this.rightForearm.xRot = Anim.lerp(strike, this.rightForearm.xRot, -0.3F);
                this.maul.xRot = Anim.lerp(strike, this.maul.xRot, 1.05F);
            }
            case TaskmasterEntity.THROW -> {
                // The left hand takes a wedge from the belt, goes back over the shoulder and whips forward.
                float take = Anim.pulse(t, 0.0F, 6.0F, 8.0F, 11.0F);
                float back = Anim.pulse(t, 7.0F, 13.0F, 15.0F, 17.5F);
                float cast = Anim.pulse(t, 15.0F, 17.5F, 23.0F, 35.0F);
                this.leftArm.xRot += 0.35F * take;
                this.leftArm.zRot += 0.2F * take;
                this.leftArm.xRot = Anim.lerp(back, this.leftArm.xRot, -2.6F);
                this.leftForearm.xRot = Anim.lerp(back, this.leftForearm.xRot, -1.1F);
                this.leftArm.xRot = Anim.lerp(cast, this.leftArm.xRot, -1.0F);
                this.leftForearm.xRot = Anim.lerp(cast, this.leftForearm.xRot, -0.1F);
                this.chest.yRot += 0.4F * back - 0.35F * cast;
                this.chest.xRot += -0.1F * back + 0.2F * cast;
            }
            case TaskmasterEntity.CAVE_IN -> {
                // The same overhead blow, but at the floor of the hall itself, and then his head goes back and he bellows at the roof.
                this.slam(t, 16.0F, 20.0F, 24.0F, 30.0F, 44.0F, 0.2F);
                float call = Anim.pulse(t, 27.0F, 34.0F, 50.0F, 61.0F);
                float shake = call * Mth.sin(age * 2.4F) * 0.04F;
                this.chest.xRot += -0.6F * call + shake;
                this.head.xRot -= 0.5F * call;
                this.head.zRot += shake * 2.0F;
                this.leftArm.xRot = Anim.lerp(call, this.leftArm.xRot, -0.4F);
                this.leftArm.zRot -= 0.9F * call;
                this.rightArm.zRot += 0.5F * call;
            }
            default -> {
            }
        }
    }

    /**
     * The overhead blow: both hands take the maul back over the head until it hangs behind him, hold it there, and bring
     * it down in front; it stays where it fell while he straightens.
     */
    private void slam(float t, float up, float hold, float impact, float stay, float end, float turn) {
        float raise = Anim.pulse(t, 0.0F, up, hold, impact);
        float strike = Anim.pulse(t, hold, impact, stay, end);
        float stuck = Anim.pulse(t, impact + 3.0F, impact + 6.0F, stay - 3.0F, stay);
        this.rightArm.xRot = Anim.lerp(raise, this.rightArm.xRot, -2.85F);
        this.rightArm.xRot = Anim.lerp(strike, this.rightArm.xRot, -0.95F);
        this.rightForearm.xRot = Anim.lerp(Math.max(raise, strike), this.rightForearm.xRot, -0.22F);
        this.leftArm.xRot = Anim.lerp(raise, this.leftArm.xRot, -2.6F);
        this.leftArm.xRot = Anim.lerp(strike, this.leftArm.xRot, -0.9F);
        this.leftArm.zRot -= 0.25F * Math.max(raise, strike);
        this.leftForearm.xRot = Anim.lerp(Math.max(raise, strike), this.leftForearm.xRot, -0.35F);
        this.maul.xRot = Anim.lerp(raise, this.maul.xRot, 0.0F);
        this.maul.xRot = Anim.lerp(strike, this.maul.xRot, 0.75F);
        this.chest.xRot += -0.28F * raise + 0.55F * strike + Mth.sin(t * 0.9F) * 0.04F * stuck;
        this.chest.yRot += turn * raise - turn * strike;
        this.head.xRot += -0.2F * raise + 0.2F * strike;
        this.body.y += 2.5F * strike;
        this.rightShin.xRot += 0.5F * strike;
        this.leftShin.xRot += 0.35F * strike;
        this.rightLeg.xRot -= 0.35F * strike;
        this.leftLeg.xRot -= 0.2F * strike;
    }
}
