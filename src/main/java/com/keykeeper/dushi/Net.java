package com.keykeeper.dushi;

import com.keykeeper.dushi.block.HearthBlock;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import com.keykeeper.dushi.entity.KeykeeperEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Messages between client and server. */
public final class Net {
    private Net() {}

    public static final float ROLL_COST = 20.0F;
    /** Length of a roll in ticks, and how many of its first ticks cannot be hurt. */
    public static final int ROLL_TICKS = 11;
    public static final int ROLL_SAFE_TICKS = 8;
    public static final int ROLL_COOLDOWN = 16;

    /** Client to server: the player rolls. The movement itself is done by the client. */
    public record Roll() implements CustomPacketPayload {
        public static final Type<Roll> TYPE = new Type<>(Dushi.id("roll"));
        public static final StreamCodec<ByteBuf, Roll> CODEC = StreamCodec.unit(new Roll());

        @Override
        public Type<Roll> type() {
            return TYPE;
        }

        public static void handle(Roll payload, IPayloadContext context) {
            if (!(context.player() instanceof ServerPlayer player) || player.isSpectator() || !player.isAlive()) {
                return;
            }
            PlayerSoul soul = PlayerSoul.of(player);
            if (soul.rollCooldown > 0 || (soul.stamina <= 0.0F && !player.isCreative())) {
                return;
            }
            if (!player.isCreative()) {
                soul.spend(ROLL_COST);
            }
            soul.rollTicks = ROLL_TICKS;
            soul.rollCooldown = ROLL_COOLDOWN;
            player.setForcedPose(Pose.SWIMMING);
            player.stopUsingItem();
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), Dushi.sound("player.roll"), SoundSource.PLAYERS, 0.7F,
                    0.9F + player.getRandom().nextFloat() * 0.2F);
            player.level().sendParticles(ParticleTypes.POOF, player.getX(), player.getY() + 0.1, player.getZ(), 2, 0.2, 0.05, 0.2, 0.01);
            player.syncData(Dushi.PLAYER_SOUL);
        }
    }

    /** Client to server: spend souls on one point of a stat. Only works next to a lit hearth. */
    public record LevelUp(int stat) implements CustomPacketPayload {
        public static final Type<LevelUp> TYPE = new Type<>(Dushi.id("level_up"));
        public static final StreamCodec<ByteBuf, LevelUp> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, LevelUp::stat, LevelUp::new);

        @Override
        public Type<LevelUp> type() {
            return TYPE;
        }

        public static void handle(LevelUp payload, IPayloadContext context) {
            if (context.player() instanceof ServerPlayer player) {
                apply(player, payload.stat());
            }
        }

        /** Trains one point of {@code stat} if the player stands by a lit hearth and can pay. */
        public static void apply(ServerPlayer player, int stat) {
            if (stat < 0 || stat >= PlayerSoul.STAT_COUNT || !nearLitHearth(player)) {
                return;
            }
            PlayerSoul soul = PlayerSoul.of(player);
            int cost = soul.levelCost();
            if (soul.stats[stat] >= PlayerSoul.STAT_CAP || (soul.souls < cost && !player.isCreative())) {
                return;
            }
            if (!player.isCreative()) {
                soul.souls -= cost;
            }
            soul.stats[stat]++;
            soul.applyAttributes(player);
            if (stat == PlayerSoul.VITALITY) {
                player.heal(2.0F);
            }
            soul.stamina = soul.maxStamina();
            player.syncData(Dushi.PLAYER_SOUL);
            PacketDistributor.sendToPlayer(player, new Banner("", 0, "ui.level_up"));
            player.level().sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 12, 0.3, 0.6, 0.3, 0.02);
        }

        private static boolean nearLitHearth(ServerPlayer player) {
            ServerLevel level = player.level();
            BlockPos centre = player.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-5, -3, -5), centre.offset(5, 3, 5))) {
                var state = level.getBlockState(pos);
                if (state.is(Dushi.HEARTH.get()) && state.getValue(HearthBlock.LIT)) {
                    return true;
                }
            }
            return false;
        }
    }

    /** Server to client: the player sat down at a hearth, show the level-up screen. */
    public record OpenHearth(BlockPos pos) implements CustomPacketPayload {
        public static final Type<OpenHearth> TYPE = new Type<>(Dushi.id("open_hearth"));
        public static final StreamCodec<ByteBuf, OpenHearth> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, OpenHearth::pos, OpenHearth::new);

        @Override
        public Type<OpenHearth> type() {
            return TYPE;
        }
    }

    /** Server to client: the keykeeper shows his wares. */
    public record OpenShop(int entityId) implements CustomPacketPayload {
        public static final Type<OpenShop> TYPE = new Type<>(Dushi.id("open_shop"));
        public static final StreamCodec<ByteBuf, OpenShop> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, OpenShop::entityId, OpenShop::new);

        @Override
        public Type<OpenShop> type() {
            return TYPE;
        }
    }

    /** Client to server: buy ware number {@code index} from a keykeeper standing close by. */
    public record Buy(int index) implements CustomPacketPayload {
        public static final Type<Buy> TYPE = new Type<>(Dushi.id("buy"));
        public static final StreamCodec<ByteBuf, Buy> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Buy::index, Buy::new);

        @Override
        public Type<Buy> type() {
            return TYPE;
        }

        public static void handle(Buy payload, IPayloadContext context) {
            if (context.player() instanceof ServerPlayer player) {
                apply(player, payload.index());
            }
        }

        /** Returns whether the purchase went through. */
        public static boolean apply(ServerPlayer player, int index) {
            if (index < 0 || index >= Shop.wares().size() || !player.isAlive()) {
                return false;
            }
            ServerLevel level = player.level();
            if (level.getEntitiesOfClass(KeykeeperEntity.class, player.getBoundingBox().inflate(6.0), Entity::isAlive).isEmpty()) {
                return false;
            }
            Shop.Ware ware = Shop.wares().get(index);
            PlayerSoul soul = PlayerSoul.of(player);
            if (soul.souls < ware.price() && !player.isCreative()) {
                return false;
            }
            if (!player.isCreative()) {
                soul.souls -= ware.price();
            }
            ItemStack stack = ware.stack().get();
            if (!player.getInventory().add(stack)) {
                player.spawnAtLocation(level, stack);
            }
            player.syncData(Dushi.PLAYER_SOUL);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), Dushi.sound("entity.keykeeper.give"), SoundSource.NEUTRAL, 0.8F, 1.1F);
            return true;
        }
    }

    /**
     * Server to client: a big line of text across the screen and/or a sound only this player hears.
     *
     * @param key   translation key of the text, empty for none
     * @param style 0 gold (victory), 1 pale (souls and hearths), 2 red (death)
     * @param sound name of one of the mod's sounds, empty for none
     */
    public record Banner(String key, int style, String sound) implements CustomPacketPayload {
        public static final int GOLD = 0;
        public static final int PALE = 1;
        public static final int RED = 2;
        public static final Type<Banner> TYPE = new Type<>(Dushi.id("banner"));
        public static final StreamCodec<ByteBuf, Banner> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, Banner::key,
                ByteBufCodecs.VAR_INT, Banner::style, ByteBufCodecs.STRING_UTF8, Banner::sound, Banner::new);

        @Override
        public Type<Banner> type() {
            return TYPE;
        }
    }
}
