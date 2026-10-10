package com.keykeeper.dushi.client;

import com.keykeeper.dushi.block.SkyLidBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

/**
 * The sky of the Sky Beneath: one level square round a SkyLidBlock, drawn the way the game draws the gate to its End
 * (its own picture of stars at several depths, which no light and no fog touches). One block draws a square
 * {@code 2 * REACH + 1} cells wide, so a roof of a hundred cells is a few dozen of these and not thousands; the
 * square is seen from both sides.
 */
public class SkyLidRenderer implements BlockEntityRenderer<SkyLidBlockEntity, BlockEntityRenderState> {
    /** How many cells the square reaches from the block, every way (tools/gen_depths.py: LID_TILE). */
    public static final int REACH = 8;

    public SkyLidRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public BlockEntityRenderState createRenderState() {
        return new BlockEntityRenderState();
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        collector.submitCustomGeometry(poseStack, RenderTypes.endPortal(), (pose, consumer) -> {
            float a = -REACH;
            float c = REACH + 1;
            float y = 0.5F;
            consumer.addVertex(pose, a, y, a);
            consumer.addVertex(pose, c, y, a);
            consumer.addVertex(pose, c, y, c);
            consumer.addVertex(pose, a, y, c);
            consumer.addVertex(pose, a, y, c);
            consumer.addVertex(pose, c, y, c);
            consumer.addVertex(pose, c, y, a);
            consumer.addVertex(pose, a, y, a);
        });
    }

    /** The block itself is seldom in sight when its square is. */
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 192;
    }

    @Override
    public AABB getRenderBoundingBox(SkyLidBlockEntity lid) {
        BlockPos pos = lid.getBlockPos();
        return new AABB(pos.getX() - REACH, pos.getY(), pos.getZ() - REACH, pos.getX() + REACH + 1, pos.getY() + 1, pos.getZ() + REACH + 1);
    }
}
