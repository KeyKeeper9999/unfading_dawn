package com.keykeeper.dushi.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * The sky of the Sky Beneath: a block that is drawn as a piece of sky an hour after sunset - black overhead, with
 * stars; a dark blue towards the edge of the world - and at the same brightness whatever the light, so that a roof
 * and far walls made of it are not seen as a roof and walls. {@code tone} says how far down the sky the piece is: 0
 * is overhead, {@link #TONES} - 1 the horizon (the pictures are painted by tools/gen_blocks.py, several to a tone,
 * so that no two neighbours are alike).
 * <p>
 * The same block is the bottom of the pits of the place, which are sky too: whatever comes to stand on it has fallen
 * out of the world, and is gone.
 */
public class UnderSkyBlock extends Block {
    public static final int TONES = 12;
    public static final IntegerProperty TONE = IntegerProperty.create("tone", 0, TONES - 1);

    public UnderSkyBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TONE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TONE);
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (level instanceof ServerLevel server && entity instanceof LivingEntity living && living.isAlive()
                && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()))) {
            living.hurtServer(server, level.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
        }
        super.stepOn(level, pos, state, entity);
    }
}
