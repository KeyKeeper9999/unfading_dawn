package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.CarrionHoundEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * The carrion hound. The mesh between the markers is written by tools/gen_hound.py (which also paints the texture);
 * the animation is written by hand.
 */
public class CarrionHoundModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("carrion_hound"), "main");
    private static final float DEG = (float) (Math.PI / 180.0);

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart tail;
    private final ModelPart frontRightLeg;
    private final ModelPart frontLeftLeg;
    private final ModelPart hindRightLeg;
    private final ModelPart hindLeftLeg;

    public CarrionHoundModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.tail = this.body.getChild("tail");
        this.frontRightLeg = root.getChild("front_right_leg");
        this.frontLeftLeg = root.getChild("front_left_leg");
        this.hindRightLeg = root.getChild("hind_right_leg");
        this.hindLeftLeg = root.getChild("hind_left_leg");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F)
                .texOffs(0, 16).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 6.0F, 9.0F)
                .texOffs(32, 0).addBox(-0.5F, -5.0F, -7.0F, 1.0F, 1.0F, 15.0F),
                PartPose.offset(0.0F, 13.0F, 0.0F));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 31).addBox(-3.0F, -3.0F, -5.0F, 6.0F, 6.0F, 5.0F)
                .texOffs(42, 40).addBox(-1.5F, -0.5F, -9.0F, 3.0F, 2.0F, 4.0F)
                .texOffs(54, 16).addBox(-3.0F, -5.0F, -2.0F, 2.0F, 2.0F, 1.0F)
                .texOffs(54, 19).addBox(1.0F, -5.0F, -2.0F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -2.0F, -8.0F));
        PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(0, 42).addBox(-1.5F, 0.0F, -4.0F, 3.0F, 1.0F, 4.0F),
                PartPose.offset(0.0F, 1.5F, -5.0F));
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(22, 40).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, -2.0F, 9.0F, -1.0F, 0.0F, 0.0F));
        PartDefinition front_right_leg = root.addOrReplaceChild("front_right_leg", CubeListBuilder.create()
                .texOffs(30, 16).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 9.0F, 3.0F),
                PartPose.offset(-2.5F, 15.0F, -5.5F));
        PartDefinition front_left_leg = root.addOrReplaceChild("front_left_leg", CubeListBuilder.create()
                .texOffs(42, 16).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 9.0F, 3.0F),
                PartPose.offset(2.5F, 15.0F, -5.5F));
        PartDefinition hind_right_leg = root.addOrReplaceChild("hind_right_leg", CubeListBuilder.create()
                .texOffs(30, 28).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 9.0F, 3.0F),
                PartPose.offset(-2.5F, 15.0F, 6.5F));
        PartDefinition hind_left_leg = root.addOrReplaceChild("hind_left_leg", CubeListBuilder.create()
                .texOffs(42, 28).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 9.0F, 3.0F),
                PartPose.offset(2.5F, 15.0F, 6.5F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float walk = state.walkAnimationPos * 0.9F;

        // Idle: the flanks heave, the head hangs low, the tail hangs and twitches.
        float breath = Mth.sin(age * 0.16F);
        this.body.y += breath * 0.15F;
        this.head.xRot += 0.18F + breath * 0.02F;
        this.jaw.xRot += 0.12F + Math.max(0.0F, breath) * 0.12F;
        this.tail.zRot += Mth.sin(age * 0.11F) * 0.12F;
        this.head.yRot += Mth.clamp(state.yRot, -60.0F, 60.0F) * DEG * 0.8F;
        this.head.xRot += Mth.clamp(state.xRot, -30.0F, 30.0F) * DEG * 0.6F;

        // Running: a lope, the two pairs of legs out of step, the back rocking.
        float step = Mth.cos(walk);
        this.frontRightLeg.xRot += step * 1.0F * speed;
        this.frontLeftLeg.xRot -= step * 0.8F * speed;
        this.hindRightLeg.xRot -= step * 1.0F * speed;
        this.hindLeftLeg.xRot += step * 0.8F * speed;
        this.body.xRot += Mth.sin(walk) * 0.06F * speed;
        this.tail.xRot += 0.5F * speed;

        float t = state.attackTime;
        if (state.attackId == CarrionHoundEntity.BITE) {
            // The jaws open as the head goes out, and shut.
            float open = Anim.pulse(t, 0.0F, 5.0F, 6.5F, 8.0F);
            float snap = Anim.pulse(t, 5.0F, 7.0F, 9.0F, 15.0F);
            this.head.z -= 2.5F * snap;
            this.head.xRot -= 0.25F * open;
            this.jaw.xRot += 0.9F * open;
            this.body.xRot += 0.08F * snap;
        } else if (state.attackId == CarrionHoundEntity.POUNCE) {
            // Sinks on its haunches with the head low - the warning - then goes through the air stretched out.
            float crouch = Anim.pulse(t, 0.0F, 7.0F, 10.0F, 12.0F);
            float air = state.onGround && t > 14.0F ? 0.0F : Anim.ramp(t, 10.0F, 13.0F);
            float land = state.onGround && t > 14.0F ? 1.0F - Anim.ramp(t, 20.0F, 31.0F) : 0.0F;
            this.body.y += 3.0F * crouch + 1.5F * land - 2.0F * air;
            this.body.xRot += 0.25F * crouch - 0.3F * air + 0.1F * land;
            this.frontRightLeg.xRot += -0.9F * crouch - 1.2F * air;
            this.frontLeftLeg.xRot += -0.9F * crouch - 1.2F * air;
            this.frontRightLeg.y += 2.0F * crouch - 3.0F * air;
            this.frontLeftLeg.y += 2.0F * crouch - 3.0F * air;
            this.hindRightLeg.xRot += 0.5F * crouch + 1.1F * air;
            this.hindLeftLeg.xRot += 0.5F * crouch + 1.1F * air;
            this.hindRightLeg.y += 2.0F * crouch;
            this.hindLeftLeg.y += 2.0F * crouch;
            this.head.xRot += -0.3F * crouch - 0.2F * air;
            this.jaw.xRot += 0.7F * air;
            this.tail.xRot += 0.9F * air;
        }
    }
}
