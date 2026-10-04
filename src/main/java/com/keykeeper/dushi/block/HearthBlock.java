package com.keykeeper.dushi.block;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.Net;
import com.keykeeper.dushi.PlayerSoul;
import com.keykeeper.dushi.SoulMechanics;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The hearth: an iron brazier on a ring of stones. The first touch lights it; every touch is a rest that heals, fills the flask,
 * restores the spells, makes this the place to come back to after death and opens the screen where souls are spent on stats.
 */
public class HearthBlock extends Block {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 11.0, 15.0);

    public HearthBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            if (!state.getValue(LIT)) {
                level.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL);
                serverLevel.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, 40, 0.25, 0.3, 0.25, 0.04);
                serverLevel.sendParticles(ParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, 6, 0.2, 0.1, 0.2, 0.0);
                PacketDistributor.sendToPlayer(serverPlayer, new Net.Banner("banner.vsratie_dushi.hearth_lit", Net.Banner.PALE, "ui.hearth_lit"));
            }
            rest(serverLevel, pos, serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    private static void rest(ServerLevel level, BlockPos pos, ServerPlayer player) {
        PlayerSoul soul = PlayerSoul.of(player);
        player.setHealth(player.getMaxHealth());
        player.removeAllEffects();
        player.clearFire();
        player.getFoodData().eat(20, 1.0F);
        soul.stamina = soul.maxStamina();
        soul.regenPause = 0;
        SoulMechanics.refill(player);
        // The player comes back to where they sat, not into the fire.
        player.setRespawnPosition(new ServerPlayer.RespawnConfig(
                LevelData.RespawnData.of(level.dimension(), player.blockPosition(), player.getYRot(), 0.0F), true), false);
        player.syncData(Dushi.PLAYER_SOUL);
        level.playSound(null, pos, Dushi.sound("block.hearth.rest"), SoundSource.BLOCKS, 1.0F, 1.0F);
        PacketDistributor.sendToPlayer(player, new Net.OpenHearth(pos));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.75;
        double z = pos.getZ() + 0.5;
        if (random.nextInt(8) == 0) {
            level.playLocalSound(x, y, z, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.6F + random.nextFloat() * 0.3F, 0.6F + random.nextFloat() * 0.5F, false);
        }
        for (int i = 0; i < 2; i++) {
            level.addParticle(ParticleTypes.FLAME, x + (random.nextDouble() - 0.5) * 0.4, y + random.nextDouble() * 0.2, z + (random.nextDouble() - 0.5) * 0.4,
                    0.0, 0.01 + random.nextDouble() * 0.02, 0.0);
        }
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.SMOKE, x + (random.nextDouble() - 0.5) * 0.3, y + 0.4, z + (random.nextDouble() - 0.5) * 0.3, 0.0, 0.03, 0.0);
        }
        if (random.nextInt(12) == 0) {
            level.addParticle(ParticleTypes.LAVA, x, y, z, 0.0, 0.0, 0.0);
        }
    }
}
