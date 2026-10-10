package com.keykeeper.dushi.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A sheet of mist with stars in it: what hangs over the great hollows of the Sky Beneath instead of a roof. It is one
 * flat picture in the middle of its cell, seen through, drawn at the same brightness whatever the light, and it is
 * nothing to the touch. Sheets hang one over another with room between them, each with thinner and thicker places
 * ({@code mist}: 0 is a breath, {@link #MISTS} - 1 all but hides what is over it), so that looking up one looks into
 * a depth; over the last of them is the sky itself, {@link SkyLidBlock}. The pictures are painted by
 * tools/gen_blocks.py; maps set the block (tools/gen_depths.py), it has no item.
 */
public class SkyVeilBlock extends Block {
    public static final int MISTS = 6;
    public static final IntegerProperty MIST = IntegerProperty.create("mist", 0, MISTS - 1);

    public SkyVeilBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MIST, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MIST);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }
}
