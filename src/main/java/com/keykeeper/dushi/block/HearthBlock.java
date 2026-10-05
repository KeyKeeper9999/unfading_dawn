package com.keykeeper.dushi.block;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.Net;
import com.keykeeper.dushi.PlayerSoul;
import com.keykeeper.dushi.SoulMechanics;
import com.keykeeper.dushi.item.FlaskShardItem;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
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
 * A player who has rested at a hearth can come back to it from any other (see {@link #travel}).
 */
public class HearthBlock extends Block {
    /** What a hearth of a map is called in the list of hearths to travel to (the text is hearth.vsratie_dushi.ID). */
    public enum Site implements StringRepresentable {
        /** A hearth somebody set down themselves: it is told from others by where it stands. */
        NONE("none"),
        YARD("yard"), LODGE("lodge"), KEEPER("keeper"), SMITHY("smithy"), SACRISTY("sacristy"), TERRACE("terrace"), MAW("maw"), CAGE("cage");

        public final String id;

        Site(String id) {
            this.id = id;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }
    }

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final EnumProperty<Site> SITE = EnumProperty.create("site", Site.class);
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 11.0, 15.0);

    public HearthBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false).setValue(SITE, Site.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, SITE);
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
            rest(serverLevel, pos, serverPlayer, true);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Carries the player from the hearth they sit by to another they have rested at: {@code index} is its place in
     * {@link PlayerSoul#rests}. They arrive as if they had sat down there: it becomes the place they come back to, the
     * dead round it rise, and the hour is the one they left it at. Returns whether they went.
     */
    public static boolean travel(ServerPlayer player, int index) {
        PlayerSoul soul = PlayerSoul.of(player);
        if (index < 0 || index >= soul.rests.size() || !SoulMechanics.nearHearth(player)) {
            return false;
        }
        PlayerSoul.Rest rest = soul.rests.get(index);
        ServerLevel to = player.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse(rest.dimension())));
        BlockPos hearth = BlockPos.of(rest.hearth());
        if (to == null || !to.getBlockState(hearth).is(Dushi.HEARTH.get())) {
            soul.rests.remove(index);                      // somebody took it away
            player.sendOverlayMessage(Component.translatable("message.vsratie_dushi.hearth_gone"));
            return false;
        }
        ServerLevel from = player.level();
        from.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.3, 0.8, 0.3, 0.02);
        from.playSound(null, player.getX(), player.getY(), player.getZ(), Dushi.sound("block.fog_gate.pass"), SoundSource.PLAYERS, 0.9F, 0.8F);
        player.teleport(new TeleportTransition(to, Vec3.atBottomCenterOf(BlockPos.of(rest.seat())), Vec3.ZERO, rest.yaw(), 0.0F, TeleportTransition.DO_NOTHING));
        player.resetFallDistance();
        SoulMechanics.hour(to, rest.hour());
        to.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.3, 0.8, 0.3, 0.03);
        rest(to, hearth, player, false);
        return true;
    }

    private static void rest(ServerLevel level, BlockPos pos, ServerPlayer player, boolean open) {
        PlayerSoul soul = PlayerSoul.of(player);
        player.setHealth(player.getMaxHealth());
        player.removeAllEffects();
        player.clearFire();
        player.getFoodData().eat(20, 1.0F);
        soul.stamina = soul.maxStamina();
        soul.regenPause = 0;
        // The first hearth a traveller rests at gives them their flask; shards carried are melted into it.
        if (!soul.flaskGiven) {
            soul.flaskGiven = true;
            if (!SoulMechanics.hasFlask(player)) {
                ItemStack flask = new ItemStack(Dushi.WARMTH_FLASK.get());
                if (!player.getInventory().add(flask)) {
                    player.spawnAtLocation(level, flask);
                }
                player.sendSystemMessage(Component.translatable("message.vsratie_dushi.flask_given"));
            }
        }
        FlaskShardItem.meltAll(level, player);
        soul.hearthHour = SoulMechanics.hourNow(level);
        SoulMechanics.refill(player);
        MobMarkBlockEntity.resetAround(level, pos, MobMarkBlockEntity.RESET_RADIUS);
        // The player comes back to where they sat, not into the fire.
        player.setRespawnPosition(new ServerPlayer.RespawnConfig(
                LevelData.RespawnData.of(level.dimension(), player.blockPosition(), player.getYRot(), 0.0F), true), false);
        player.syncData(Dushi.PLAYER_SOUL);
        level.playSound(null, pos, Dushi.sound("block.hearth.rest"), SoundSource.BLOCKS, 1.0F, 1.0F);
        BlockState state = level.getBlockState(pos);
        String site = state.is(Dushi.HEARTH.get()) ? state.getValue(SITE).id : Site.NONE.id;
        int here = soul.remember(new PlayerSoul.Rest(level.dimension().identifier().toString(), pos.asLong(), player.blockPosition().asLong(), player.getYRot(),
                site, soul.hearthHour));
        if (open) {
            List<Net.Stop> stops = new ArrayList<>();
            for (int i = 0; i < soul.rests.size(); i++) {
                PlayerSoul.Rest rest = soul.rests.get(i);
                stops.add(new Net.Stop(rest.site(), BlockPos.of(rest.hearth()), i == here));
            }
            PacketDistributor.sendToPlayer(player, new Net.OpenHearth(pos, stops));
        }
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
