package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.RootEntity;

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
 * A root of the Weeping Mother coming up through the ground. The mesh between the markers is written by
 * tools/gen_root.py (which also paints the texture); the animation is written by hand.
 * <p>
 * There are five roots in it, each hanging from the ground by itself. While the ground is only warning, each is pushed
 * down under it by all but a few fingers of its height, and those few fingers tremble; when the blow comes they are let
 * up, the thick one first and the others a tick or two after it; then they stand, and at the end go back down.
 */
public class RootModel extends EntityModel<RootRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Dushi.id("root"), "main");
    /** Must match SCALE in tools/gen_root.py. */
    private static final float MODEL_SCALE = 1.5F;
    /** How far each root stands over the ground, in sixteenths (HEIGHTS in tools/gen_root.py, which measures them). */
    private static final float MAIN_HEIGHT = 33.0F;
    private static final float SIDE_HEIGHT = 20.0F;
    /** How much of a root shows while the ground is warning. */
    private static final float POKE = 3.0F;

    private final ModelPart clods;
    private final ModelPart main;
    private final ModelPart mainMid;
    private final ModelPart mainTip;
    private final ModelPart[] sides;
    private final ModelPart[] sideTips;

    public RootModel(ModelPart root) {
        super(root);
        this.clods = root.getChild("clods");
        this.main = root.getChild("main");
        this.mainMid = this.main.getChild("main_mid");
        this.mainTip = this.mainMid.getChild("main_tip");
        String[] names = {"east", "west", "south", "north"};
        this.sides = new ModelPart[names.length];
        this.sideTips = new ModelPart[names.length];
        for (int i = 0; i < names.length; i++) {
            this.sides[i] = root.getChild(names[i]);
            this.sideTips[i] = this.sides[i].getChild(names[i] + "_tip");
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // <generated mesh>
        PartDefinition clods = root.addOrReplaceChild("clods", CubeListBuilder.create()
                .texOffs(0, 28).addBox(-9.0F, -2.0F, -8.0F, 4.0F, 2.0F, 4.0F)
                .texOffs(32, 30).addBox(5.0F, -1.0F, -9.0F, 3.0F, 1.0F, 4.0F)
                .texOffs(46, 30).addBox(-8.0F, -1.0F, 5.0F, 4.0F, 1.0F, 3.0F)
                .texOffs(16, 28).addBox(4.0F, -2.0F, 4.0F, 4.0F, 2.0F, 4.0F)
                .texOffs(34, 35).addBox(-2.0F, -1.0F, -11.0F, 3.0F, 1.0F, 2.0F)
                .texOffs(0, 34).addBox(8.0F, -1.0F, -1.0F, 3.0F, 1.0F, 3.0F)
                .texOffs(12, 34).addBox(-12.0F, -1.0F, -1.0F, 3.0F, 1.0F, 3.0F)
                .texOffs(46, 34).addBox(-1.0F, -1.0F, 8.0F, 3.0F, 1.0F, 3.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition main = root.addOrReplaceChild("main", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0F, -11.0F, -3.0F, 6.0F, 11.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.07F, 0.0F, -0.05F));
        PartDefinition main_mid = main.addOrReplaceChild("main_mid", CubeListBuilder.create()
                .texOffs(24, 0).addBox(-2.0F, -12.0F, -2.0F, 4.0F, 12.0F, 4.0F)
                .texOffs(24, 35).addBox(2.0F, -8.0F, -1.0F, 3.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, -0.3F, 0.3F, 0.22F));
        PartDefinition main_tip = main_mid.addOrReplaceChild("main_tip", CubeListBuilder.create()
                .texOffs(24, 16).addBox(-1.0F, -10.0F, -1.0F, 2.0F, 10.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.5F, 0.0F, -0.34F));
        PartDefinition east = root.addOrReplaceChild("east", CubeListBuilder.create()
                .texOffs(40, 0).addBox(-1.5F, -12.0F, -1.5F, 3.0F, 12.0F, 3.0F),
                PartPose.offsetAndRotation(8.0F, 24.0F, 1.0F, 0.1F, 0.0F, 0.42F));
        PartDefinition east_tip = east.addOrReplaceChild("east_tip", CubeListBuilder.create()
                .texOffs(32, 16).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 9.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, -0.15F, 0.0F, -0.62F));
        PartDefinition west = root.addOrReplaceChild("west", CubeListBuilder.create()
                .texOffs(52, 0).addBox(-1.5F, -12.0F, -1.5F, 3.0F, 12.0F, 3.0F),
                PartPose.offsetAndRotation(-8.0F, 24.0F, -2.0F, -0.12F, 0.0F, -0.46F));
        PartDefinition west_tip = west.addOrReplaceChild("west_tip", CubeListBuilder.create()
                .texOffs(0, 17).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 9.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.2F, 0.0F, 0.6F));
        PartDefinition south = root.addOrReplaceChild("south", CubeListBuilder.create()
                .texOffs(40, 15).addBox(-1.5F, -12.0F, -1.5F, 3.0F, 12.0F, 3.0F),
                PartPose.offsetAndRotation(1.0F, 24.0F, 8.0F, 0.44F, 0.0F, 0.08F));
        PartDefinition south_tip = south.addOrReplaceChild("south_tip", CubeListBuilder.create()
                .texOffs(8, 17).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 9.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, -0.66F, 0.2F, 0.0F));
        PartDefinition north = root.addOrReplaceChild("north", CubeListBuilder.create()
                .texOffs(52, 15).addBox(-1.5F, -12.0F, -1.5F, 3.0F, 12.0F, 3.0F),
                PartPose.offsetAndRotation(-2.0F, 24.0F, -8.0F, -0.4F, 0.0F, -0.1F));
        PartDefinition north_tip = north.addOrReplaceChild("north_tip", CubeListBuilder.create()
                .texOffs(16, 17).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 9.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.6F, -0.2F, 0.0F));
        // </generated mesh>
        return LayerDefinition.create(mesh, 64, 64).apply(MeshTransformer.scaling(MODEL_SCALE));
    }

    /** How far out of the ground a root is, 0..1, {@code late} ticks behind the thick one. */
    private static float out(RootRenderState state, float late) {
        float t = state.age - state.warning - late;
        if (t < 0.0F) {
            return 0.0F;
        }
        float up = Anim.ramp(t, 0.0F, RootEntity.RISE);
        // (it comes up a little too far and settles)
        float over = Anim.pulse(t, RootEntity.RISE * 0.6F, RootEntity.RISE, RootEntity.RISE + 1.0F, RootEntity.RISE + 5.0F) * 0.07F;
        float down = Anim.ramp(t + late, state.stand, state.stand + RootEntity.SINK);
        return Math.max(0.0F, (up + over) * (1.0F - down));
    }

    @Override
    public void setupAnim(RootRenderState state) {
        super.setupAnim(state);
        float age = state.age;
        boolean warning = age < state.warning;
        // the earth is broken as soon as the ground begins to move, and stays broken
        float broken = Anim.ramp(age, 0.0F, 5.0F) * (1.0F - Anim.ramp(age, state.warning + state.stand + RootEntity.SINK - 4.0F, state.warning + state.stand + RootEntity.SINK));
        this.clods.xScale = broken;
        this.clods.yScale = broken * (warning ? 0.6F + 0.4F * Mth.sin(age * 1.9F) * Mth.sin(age * 1.9F) : 1.0F);
        this.clods.zScale = broken;
        this.clods.visible = broken > 0.02F;

        float poke = warning ? Anim.ramp(age, 1.0F, 6.0F) : 1.0F - Anim.ramp(age, state.warning + state.stand, state.warning + state.stand + 3.0F);
        float mainOut = out(state, 0.0F);
        float shown = Math.max(mainOut * MAIN_HEIGHT, poke * (POKE + Mth.sin(age * 1.7F) * 1.2F));
        this.main.y += MAIN_HEIGHT - shown;
        this.main.visible = shown > 0.05F;
        for (int i = 0; i < this.sides.length; i++) {
            float sideOut = out(state, 1.0F + i * 0.6F);
            float sideShown = Math.max(sideOut * SIDE_HEIGHT, poke * (POKE - 0.5F + Mth.sin(age * 1.5F + i * 1.9F) * 1.3F));
            this.sides[i].y += SIDE_HEIGHT - sideShown;
            this.sides[i].visible = sideShown > 0.05F;
            // standing, they feel about a little
            this.sideTips[i].xRot += Mth.sin(age * 0.33F + i * 1.3F) * 0.12F * sideOut;
            this.sideTips[i].zRot += Mth.cos(age * 0.29F + i * 2.1F) * 0.12F * sideOut;
        }
        this.mainMid.zRot += Mth.sin(age * 0.31F) * 0.07F * mainOut;
        this.mainTip.xRot += Mth.sin(age * 0.37F + 1.0F) * 0.14F * mainOut;
        this.mainTip.zRot += Mth.cos(age * 0.27F) * 0.1F * mainOut;
    }
}
