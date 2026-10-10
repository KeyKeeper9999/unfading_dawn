package com.keykeeper.dushi.block;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Holds nothing: it is there so that the sky of {@link SkyLidBlock} has something to be drawn by. */
public class SkyLidBlockEntity extends BlockEntity {
    public SkyLidBlockEntity(BlockPos pos, BlockState state) {
        super(Dushi.SKY_LID_ENTITY.get(), pos, state);
    }
}
