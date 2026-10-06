package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.BonePikeEntity;

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
 * The bone pike. The mesh between the markers is written by tools/gen_pike.py (which also paints the textures); the
 * animation is written by hand. Everything hangs from the part {@code all}, which is the middle of the fish: it pitches
 * about that when it leaps, and lies over on it when it is on the boards.
 */
public class BonePikeModel extends EntityModel<BossRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("bone_pike"), "main");

    private final ModelPart all;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart rightFin;
    private final ModelPart leftFin;
    private final ModelPart tail;
    private final ModelPart tailTip;

    public BonePikeModel(ModelPart root) {
        super(root);
        this.all = root.getChild("all");
        this.body = this.all.getChild("body");
        this.head = this.all.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.rightFin = this.body.getChild("right_fin");
        this.leftFin = this.body.getChild("left_fin");
        this.tail = this.all.getChild("tail");
        this.tailTip = this.tail.getChild("tail_tip");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(),
                PartPose.offset(0.0F, 18.0F, 0.0F));
        PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, -4.5F, -7.0F, 7.0F, 9.0F, 12.0F)
                .texOffs(24, 21).addBox(-4.0F, -5.5F, -7.0F, 8.0F, 2.0F, 5.0F)
                .texOffs(90, 23).addBox(-3.5F, -5.5F, -1.0F, 7.0F, 2.0F, 5.0F)
                .texOffs(110, 0).addBox(-0.5F, -10.5F, -2.0F, 1.0F, 5.0F, 7.0F),
                PartPose.ZERO);
        PartDefinition head = all.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(54, 0).addBox(-4.0F, -4.5F, -7.0F, 8.0F, 8.0F, 7.0F)
                .texOffs(0, 21).addBox(-3.0F, -2.5F, -13.0F, 6.0F, 4.0F, 6.0F),
                PartPose.offset(0.0F, 0.0F, -7.0F));
        PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(54, 15).addBox(-3.0F, 0.0F, -12.0F, 6.0F, 2.0F, 12.0F)
                .texOffs(114, 23).addBox(-3.0F, -2.0F, -12.5F, 6.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 2.5F, -1.0F, 0.1F, 0.0F, 0.0F));
        PartDefinition right_fin = body.addOrReplaceChild("right_fin", CubeListBuilder.create()
                .texOffs(24, 28).addBox(-6.0F, 0.0F, -2.0F, 6.0F, 1.0F, 5.0F),
                PartPose.offsetAndRotation(-3.5F, 2.5F, -4.0F, 0.0F, 0.2F, 0.45F));
        PartDefinition left_fin = body.addOrReplaceChild("left_fin", CubeListBuilder.create()
                .texOffs(46, 29).addBox(0.0F, 0.0F, -2.0F, 6.0F, 1.0F, 5.0F),
                PartPose.offsetAndRotation(3.5F, 2.5F, -4.0F, 0.0F, -0.2F, -0.45F));
        PartDefinition tail = all.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(84, 0).addBox(-2.5F, -3.5F, 0.0F, 5.0F, 7.0F, 8.0F)
                .texOffs(90, 15).addBox(-2.0F, -4.5F, 1.0F, 4.0F, 1.0F, 5.0F),
                PartPose.offset(0.0F, 0.0F, 5.0F));
        PartDefinition tail_tip = tail.addOrReplaceChild("tail_tip", CubeListBuilder.create()
                .texOffs(110, 12).addBox(-1.5F, -2.5F, 0.0F, 3.0F, 5.0F, 6.0F)
                .texOffs(38, 0).addBox(-0.5F, -7.0F, 4.0F, 1.0F, 14.0F, 7.0F),
                PartPose.offset(0.0F, 0.0F, 8.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;
        float speed = Math.min(1.0F, state.walkAnimationSpeed);

        // Lying in wait it only keeps its place: the tail goes slowly from side to side, the fins fan, the jaws work.
        // Swimming, the whole of it bends, and the bend runs down it from the head to the tail.
        float beat = age * 0.16F + state.walkAnimationPos * 1.3F;
        float wide = 0.22F + 0.4F * speed;
        this.head.yRot += Mth.sin(beat + 1.6F) * wide * 0.25F;
        this.body.yRot += Mth.sin(beat + 0.8F) * wide * 0.2F;
        this.tail.yRot += Mth.sin(beat) * wide;
        this.tailTip.yRot += Mth.sin(beat - 0.9F) * wide * 1.4F;
        this.rightFin.zRot += Mth.sin(age * 0.23F) * 0.25F;
        this.leftFin.zRot -= Mth.sin(age * 0.23F) * 0.25F;
        this.jaw.xRot += Math.max(0.0F, Mth.sin(age * 0.09F)) * 0.12F;

        float t = state.attackTime;
        switch (state.attackId) {
            case BonePikeEntity.BITE -> {
                // The head goes back a hand's breadth and the jaws come open; then all of it darts and they shut.
                float open = Anim.pulse(t, 0.0F, 6.0F, 8.5F, 10.0F);
                float dart = Anim.pulse(t, 6.0F, 9.0F, 11.0F, 20.0F);
                this.all.z += 3.0F * open * (1.0F - dart) - 6.0F * dart;
                this.jaw.xRot += 0.95F * open;
                this.head.xRot -= 0.25F * open;
                this.tail.yRot += 0.5F * dart;
            }
            case BonePikeEntity.LEAP -> {
                // It bends itself like a bow under the water - then it is in the air nose first with its jaws wide, and
                // comes down nose first.
                float coil = Anim.pulse(t, 0.0F, 8.0F, 10.0F, 12.0F);
                float fly = Anim.ramp(t, 10.0F, 13.0F);
                float pitch = Anim.lerp(Anim.ramp(t, 12.0F, 26.0F), -0.85F, 0.75F);
                this.tail.yRot = Anim.lerp(coil, this.tail.yRot, 1.1F);
                this.tailTip.yRot = Anim.lerp(coil, this.tailTip.yRot, 0.9F);
                this.head.yRot -= 0.3F * coil;
                this.all.y += 2.0F * coil;
                this.all.xRot += pitch * fly;
                this.jaw.xRot += 0.95F * fly * (1.0F - Anim.ramp(t, 24.0F, 27.0F));
                this.head.xRot -= 0.2F * fly;
                this.rightFin.zRot += 0.6F * fly;
                this.leftFin.zRot -= 0.6F * fly;
            }
            case BonePikeEntity.FLOUNDER -> {
                // On its side on the boards: the tail beats them, the head comes up and falls back, the jaws gape.
                float lie = Anim.ramp(t, 0.0F, 4.0F) * (1.0F - Anim.ramp(t, BonePikeEntity.FLOUNDER_BACK - 4.0F, BonePikeEntity.FLOUNDER_BACK + 1.0F));
                float thrash = Mth.sin(t * 0.85F);
                this.all.zRot += 1.45F * lie;
                this.all.y += 2.5F * lie;
                this.tail.yRot = Anim.lerp(lie, this.tail.yRot, thrash * 0.95F);
                this.tailTip.yRot = Anim.lerp(lie, this.tailTip.yRot, Mth.sin(t * 0.85F - 0.8F) * 1.0F);
                this.head.yRot = Anim.lerp(lie, this.head.yRot, -thrash * 0.4F);
                this.jaw.xRot += (0.4F + 0.3F * Math.max(0.0F, Mth.sin(t * 0.4F))) * lie;
                float back = Anim.ramp(t, BonePikeEntity.FLOUNDER_BACK - 4.0F, BonePikeEntity.FLOUNDER_BACK + 2.0F);
                this.all.xRot += -0.5F * back;
            }
            default -> {
            }
        }
    }
}
