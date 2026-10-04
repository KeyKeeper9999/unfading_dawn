package com.keykeeper.dushi.block;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A shard of a flask still glowing in a heap of ash: whoever takes it gets one flask shard, and the heap is gone. This is
 * how a map hands the shards out one by one at places of its choosing, where a chest would hide them among other things.
 */
public class ShardBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 7.0, 13.0);

    public ShardBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel serverLevel) {
            ItemStack shard = new ItemStack(Dushi.FLASK_SHARD.get());
            if (!player.getInventory().add(shard)) {
                player.spawnAtLocation(serverLevel, shard);
            }
            level.removeBlock(pos, false);
            serverLevel.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 24, 0.2, 0.2, 0.2, 0.03);
            level.playSound(null, pos, Dushi.sound("ui.souls_retrieve"), SoundSource.BLOCKS, 0.9F, 1.2F);
            player.sendOverlayMessage(Component.translatable("message.vsratie_dushi.shard_found"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.SMALL_FLAME, pos.getX() + 0.35 + random.nextDouble() * 0.3, pos.getY() + 0.45 + random.nextDouble() * 0.2,
                    pos.getZ() + 0.35 + random.nextDouble() * 0.3, 0.0, 0.01, 0.0);
        }
        if (random.nextInt(10) == 0) {
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5,
                    (random.nextDouble() - 0.5) * 0.02, 0.03, (random.nextDouble() - 0.5) * 0.02);
        }
    }
}
