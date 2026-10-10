package com.keykeeper.dushi.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The sky over a great hollow of the Sky Beneath, above the last sheet of mist ({@link SkyVeilBlock}): the block
 * itself is nothing - not seen, not felt - but where it stands a square of sky is drawn round it, level, in the
 * middle of its layer, the way the game draws the gate to its End: stars at several depths that keep their places
 * as one walks under them (client/SkyLidRenderer; the square reaches {@code SkyLidRenderer.REACH} cells every way).
 * A map sets one in the middle of every such square of a hollow's roof, in the rock too if that is where the middle
 * is: what of the square lies inside rock is not seen. No item.
 */
public class SkyLidBlock extends Block implements EntityBlock {
    public SkyLidBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SkyLidBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }
}
