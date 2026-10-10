package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.HandwalkerEntity;

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
 * The handwalker. The mesh between the markers is written by tools/gen_dusk.py (which also paints the texture and
 * draws the poses below on a sheet); the animation is written by hand. It hangs from 'mid', the joint in the middle of
 * it: the front of it rears from there, and the rear of it is what it whips with. Running, the whole length of it goes
 * in a wave, and the pairs of hands come down one after another along it.
 */
public class HandwalkerModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("handwalker"), "main");
    /** Must match the scale in the generator. */
    private static final float MODEL_SCALE = 1.15F;

    private final ModelPart all;
    private final ModelPart mid;
    private final ModelPart front1;
    private final ModelPart front0;
    private final ModelPart head;
    private final ModelPart jawRight;
    private final ModelPart jawLeft;
    private final ModelPart rear0;
    private final ModelPart rear1;
    private final ModelPart rear2;
    private final ModelPart rear3;
    private final ModelPart tail;

    private final ModelPart[] legRight = new ModelPart[6];
    private final ModelPart[] legLeft = new ModelPart[6];

    public HandwalkerModel(ModelPart root) {
        super(root);
        this.all = root.getChild("all");
        this.mid = this.all.getChild("mid");
        this.front1 = this.mid.getChild("front1");
        this.front0 = this.front1.getChild("front0");
        this.head = this.front0.getChild("head");
        this.jawRight = this.head.getChild("jaw_r");
        this.jawLeft = this.head.getChild("jaw_l");
        this.rear0 = this.mid.getChild("rear0");
        this.rear1 = this.rear0.getChild("rear1");
        this.rear2 = this.rear1.getChild("rear2");
        this.rear3 = this.rear2.getChild("rear3");
        this.tail = this.rear3.getChild("tail");
        ModelPart[] joints = {this.front0, this.front1, this.mid, this.rear0, this.rear1, this.rear2};
        for (int i = 0; i < 6; i++) {
            this.legRight[i] = joints[i].getChild("leg_r_" + i);
            this.legLeft[i] = joints[i].getChild("leg_l_" + i);
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition mid = all.addOrReplaceChild("mid", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, -2.5F, -3.5F, 7.0F, 5.0F, 7.0F),
                PartPose.offset(0.0F, -6.0F, 0.0F));
        PartDefinition leg_r_2 = mid.addOrReplaceChild("leg_r_2", CubeListBuilder.create()
                .texOffs(44, 42).addBox(-5.0F, -1.0F, -1.0F, 5.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-3.0F, 0.5F, 0.0F, 0.0F, 0.0F, -0.6F));
        PartDefinition hand_r_2 = leg_r_2.addOrReplaceChild("hand_r_2", CubeListBuilder.create()
                .texOffs(96, 12).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F)
                .texOffs(32, 23).addBox(-2.5F, 2.5F, -5.0F, 5.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.6F));
        PartDefinition leg_l_2 = mid.addOrReplaceChild("leg_l_2", CubeListBuilder.create()
                .texOffs(58, 42).addBox(0.0F, -1.0F, -1.0F, 5.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(3.0F, 0.5F, 0.0F, 0.0F, 0.0F, 0.6F));
        PartDefinition hand_l_2 = leg_l_2.addOrReplaceChild("hand_l_2", CubeListBuilder.create()
                .texOffs(120, 18).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F)
                .texOffs(96, 26).addBox(-2.5F, 2.5F, -5.0F, 5.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.6F));
        PartDefinition front1 = mid.addOrReplaceChild("front1", CubeListBuilder.create()
                .texOffs(28, 0).addBox(-3.5F, -2.5F, -7.0F, 7.0F, 5.0F, 7.0F),
                PartPose.offset(0.0F, 0.0F, -3.5F));
        PartDefinition leg_r_1 = front1.addOrReplaceChild("leg_r_1", CubeListBuilder.create()
                .texOffs(22, 44).addBox(-5.0F, -1.0F, -1.0F, 5.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-3.0F, 0.5F, -3.5F, 0.0F, 0.0F, -0.6F));
        PartDefinition hand_r_1 = leg_r_1.addOrReplaceChild("hand_r_1", CubeListBuilder.create()
                .texOffs(54, 23).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F)
                .texOffs(54, 28).addBox(-2.5F, 2.5F, -5.0F, 5.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.6F));
        PartDefinition leg_l_1 = front1.addOrReplaceChild("leg_l_1", CubeListBuilder.create()
                .texOffs(0, 45).addBox(0.0F, -1.0F, -1.0F, 5.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(3.0F, 0.5F, -3.5F, 0.0F, 0.0F, 0.6F));
        PartDefinition hand_l_1 = leg_l_1.addOrReplaceChild("hand_l_1", CubeListBuilder.create()
                .texOffs(62, 23).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F)
                .texOffs(32, 30).addBox(-2.5F, 2.5F, -5.0F, 5.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.6F));
        PartDefinition front0 = front1.addOrReplaceChild("front0", CubeListBuilder.create()
                .texOffs(56, 0).addBox(-3.0F, -2.5F, -7.0F, 6.0F, 5.0F, 7.0F),
                PartPose.offset(0.0F, 0.0F, -7.0F));
        PartDefinition leg_r_0 = front0.addOrReplaceChild("leg_r_0", CubeListBuilder.create()
                .texOffs(36, 46).addBox(-5.0F, -1.0F, -1.0F, 5.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-3.0F, 0.5F, -3.5F, 0.0F, 0.0F, -0.6F));
        PartDefinition hand_r_0 = leg_r_0.addOrReplaceChild("hand_r_0", CubeListBuilder.create()
                .texOffs(120, 23).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F)
                .texOffs(0, 31).addBox(-2.5F, 2.5F, -5.0F, 5.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.6F));
        PartDefinition leg_l_0 = front0.addOrReplaceChild("leg_l_0", CubeListBuilder.create()
                .texOffs(50, 46).addBox(0.0F, -1.0F, -1.0F, 5.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(3.0F, 0.5F, -3.5F, 0.0F, 0.0F, 0.6F));
        PartDefinition hand_l_0 = leg_l_0.addOrReplaceChild("hand_l_0", CubeListBuilder.create()
                .texOffs(76, 28).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F)
                .texOffs(76, 33).addBox(-2.5F, 2.5F, -5.0F, 5.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.6F));
        PartDefinition head = front0.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(108, 0).addBox(-2.5F, -2.0F, -5.0F, 5.0F, 4.0F, 5.0F)
                .texOffs(0, 23).addBox(-2.0F, -3.5F, -9.0F, 1.0F, 1.0F, 7.0F)
                .texOffs(16, 23).addBox(1.0F, -3.5F, -9.0F, 1.0F, 1.0F, 7.0F),
                PartPose.offset(0.0F, 0.0F, -7.0F));
        PartDefinition jaw_r = head.addOrReplaceChild("jaw_r", CubeListBuilder.create()
                .texOffs(72, 12).addBox(-2.5F, -0.5F, -7.0F, 5.0F, 1.0F, 7.0F),
                PartPose.offsetAndRotation(-2.0F, 1.5F, -5.0F, 0.0F, 0.45F, 0.0F));
        PartDefinition jaw_l = head.addOrReplaceChild("jaw_l", CubeListBuilder.create()
                .texOffs(96, 18).addBox(-2.5F, -0.5F, -7.0F, 5.0F, 1.0F, 7.0F),
                PartPose.offsetAndRotation(2.0F, 1.5F, -5.0F, 0.0F, -0.45F, 0.0F));
        PartDefinition rear0 = mid.addOrReplaceChild("rear0", CubeListBuilder.create()
                .texOffs(82, 0).addBox(-3.0F, -2.5F, 0.0F, 6.0F, 5.0F, 7.0F),
                PartPose.offset(0.0F, 0.0F, 3.5F));
        PartDefinition leg_r_3 = rear0.addOrReplaceChild("leg_r_3", CubeListBuilder.create()
                .texOffs(64, 47).addBox(-5.0F, -1.0F, -1.0F, 5.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-3.0F, 0.5F, 3.5F, 0.0F, 0.0F, -0.6F));
        PartDefinition hand_r_3 = leg_r_3.addOrReplaceChild("hand_r_3", CubeListBuilder.create()
                .texOffs(84, 28).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F)
                .texOffs(98, 33).addBox(-2.5F, 2.5F, -5.0F, 5.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.6F));
        PartDefinition leg_l_3 = rear0.addOrReplaceChild("leg_l_3", CubeListBuilder.create()
                .texOffs(78, 47).addBox(0.0F, -1.0F, -1.0F, 5.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(3.0F, 0.5F, 3.5F, 0.0F, 0.0F, 0.6F));
        PartDefinition hand_l_3 = leg_l_3.addOrReplaceChild("hand_l_3", CubeListBuilder.create()
                .texOffs(118, 28).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F)
                .texOffs(54, 35).addBox(-2.5F, 2.5F, -5.0F, 5.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.6F));
        PartDefinition rear1 = rear0.addOrReplaceChild("rear1", CubeListBuilder.create()
                .texOffs(0, 12).addBox(-3.0F, -2.0F, 0.0F, 6.0F, 4.0F, 7.0F),
                PartPose.offset(0.0F, 0.0F, 7.0F));
        PartDefinition leg_r_4 = rear1.addOrReplaceChild("leg_r_4", CubeListBuilder.create()
                .texOffs(92, 47).addBox(-5.0F, -1.0F, -1.0F, 5.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-3.0F, 0.5F, 3.5F, 0.0F, 0.0F, -0.6F));
        PartDefinition hand_r_4 = leg_r_4.addOrReplaceChild("hand_r_4", CubeListBuilder.create()
                .texOffs(22, 31).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F)
                .texOffs(22, 37).addBox(-2.5F, 2.5F, -5.0F, 5.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.6F));
        PartDefinition leg_l_4 = rear1.addOrReplaceChild("leg_l_4", CubeListBuilder.create()
                .texOffs(106, 47).addBox(0.0F, -1.0F, -1.0F, 5.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(3.0F, 0.5F, 3.5F, 0.0F, 0.0F, 0.6F));
        PartDefinition hand_l_4 = leg_l_4.addOrReplaceChild("hand_l_4", CubeListBuilder.create()
                .texOffs(120, 33).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F)
                .texOffs(0, 38).addBox(-2.5F, 2.5F, -5.0F, 5.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.6F));
        PartDefinition rear2 = rear1.addOrReplaceChild("rear2", CubeListBuilder.create()
                .texOffs(26, 12).addBox(-2.5F, -2.0F, 0.0F, 5.0F, 4.0F, 7.0F),
                PartPose.offset(0.0F, 0.0F, 7.0F));
        PartDefinition leg_r_5 = rear2.addOrReplaceChild("leg_r_5", CubeListBuilder.create()
                .texOffs(14, 48).addBox(-5.0F, -1.0F, -1.0F, 5.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-3.0F, 0.5F, 3.5F, 0.0F, 0.0F, -0.6F));
        PartDefinition hand_r_5 = leg_r_5.addOrReplaceChild("hand_r_5", CubeListBuilder.create()
                .texOffs(44, 37).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F)
                .texOffs(76, 40).addBox(-2.5F, 2.5F, -5.0F, 5.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.6F));
        PartDefinition leg_l_5 = rear2.addOrReplaceChild("leg_l_5", CubeListBuilder.create()
                .texOffs(0, 49).addBox(0.0F, -1.0F, -1.0F, 5.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(3.0F, 0.5F, 3.5F, 0.0F, 0.0F, 0.6F));
        PartDefinition hand_l_5 = leg_l_5.addOrReplaceChild("hand_l_5", CubeListBuilder.create()
                .texOffs(120, 38).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F)
                .texOffs(98, 40).addBox(-2.5F, 2.5F, -5.0F, 5.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.6F));
        PartDefinition rear3 = rear2.addOrReplaceChild("rear3", CubeListBuilder.create()
                .texOffs(50, 12).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 7.0F),
                PartPose.offset(0.0F, 0.0F, 7.0F));
        PartDefinition tail = rear3.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(108, 9).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 7.0F)
                .texOffs(72, 20).addBox(-2.5F, -0.5F, 6.0F, 5.0F, 1.0F, 7.0F),
                PartPose.offset(0.0F, 0.0F, 7.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 128, 64).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float t = state.attackTime;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);
        float step = state.walkAnimationPos * 1.5F;
        ModelPart[] chain = {this.front1, this.front0, this.rear0, this.rear1, this.rear2, this.rear3, this.tail};
        // the wave down its length: a little of it always, all of it when it runs
        for (int i = 0; i < chain.length; i++) {
            chain[i].yRot += Mth.sin(i * 1.1F + step + age * 0.05F) * (0.05F + 0.2F * speed);
        }
        // the hands, a pair after a pair
        for (int i = 0; i < 6; i++) {
            float beat = Mth.sin(step * 2.0F - i * 1.2F);
            this.legRight[i].zRot += Math.max(0.0F, beat) * 0.35F * speed;
            this.legRight[i].yRot += beat * 0.3F * speed;
            this.legLeft[i].zRot -= Math.max(0.0F, -beat) * 0.35F * speed;
            this.legLeft[i].yRot += beat * 0.3F * speed;
        }
        this.jawRight.yRot += Mth.sin(age * 0.2F) * 0.12F;
        this.jawLeft.yRot -= Mth.sin(age * 0.2F) * 0.12F;
        switch (state.attackId) {
            case HandwalkerEntity.LUNGE -> {
                float start = HandwalkerEntity.LUNGE_START;
                float reared = Anim.pulse(t, 0.0F, start - 4.0F, start - 1.0F, start + 3.0F);
                float flat = Anim.pulse(t, start - 1.0F, start + 3.0F, HandwalkerEntity.LUNGE_END + 4.0F, HandwalkerEntity.LUNGE_DURATION);
                Anim.pose(this.front1, reared, -0.55F, 0.0F, 0.0F);
                Anim.pose(this.front0, reared, -0.45F, 0.0F, 0.0F);
                Anim.pose(this.head, reared, 0.5F, 0.0F, 0.0F);
                Anim.pose(this.jawRight, reared, 0.0F, -0.6F, 0.0F);
                Anim.pose(this.jawLeft, reared, 0.0F, 0.6F, 0.0F);
                Anim.pose(this.front1, flat, 0.1F, 0.0F, 0.0F);
                Anim.pose(this.front0, flat, 0.05F, 0.0F, 0.0F);
                Anim.pose(this.jawRight, flat, 0.0F, 0.3F, 0.0F);
                Anim.pose(this.jawLeft, flat, 0.0F, -0.3F, 0.0F);
            }
            case HandwalkerEntity.WHIP -> {
                float impact = HandwalkerEntity.WHIP_IMPACT;
                float coil = Anim.pulse(t, 0.0F, impact - 6.0F, impact - 4.0F, impact);
                float whip = Anim.pulse(t, impact - 4.0F, impact + 2.0F, impact + 8.0F, HandwalkerEntity.WHIP_DURATION);
                float turn = 0.58F * coil - 0.55F * whip;
                this.rear0.yRot += turn;
                this.rear1.yRot += turn;
                this.rear2.yRot += turn;
                this.rear3.yRot += turn;
                this.tail.yRot += turn;
                this.tail.xRot -= 0.3F * coil;
                this.front1.yRot -= 0.3F * coil;
            }
            default -> {
            }
        }
    }
}
