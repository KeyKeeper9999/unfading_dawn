package com.keykeeper.dushi;

import com.keykeeper.dushi.block.HearthBlock;

import java.util.List;

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

    /** Length of a roll in ticks. What it costs, how far it goes and how long it protects depends on the load, see {@link Gear.Burden}. */
    public static final int ROLL_TICKS = 11;

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
            Gear.Burden burden = Gear.burden(player);
            if (soul.rollCooldown > 0 || !burden.canRoll() || (soul.stamina <= 0.0F && !player.isCreative())) {
                return;
            }
            if (!player.isCreative()) {
                soul.spend(Gear.rollCost(player, burden));
            }
            soul.rollTicks = ROLL_TICKS;
            soul.rollCooldown = burden.rollCooldown;
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

    /** A hearth the player may travel to: what it is called (HearthBlock.Site), where it stands, whether it is the one they sit by. */
    public record Stop(String site, BlockPos pos, boolean here) {
        public static final StreamCodec<ByteBuf, Stop> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, Stop::site, BlockPos.STREAM_CODEC, Stop::pos,
                ByteBufCodecs.BOOL, Stop::here, Stop::new);
    }

    /** Server to client: the player sat down at a hearth, show the level-up screen; with it, every hearth they have rested at. */
    public record OpenHearth(BlockPos pos, List<Stop> stops) implements CustomPacketPayload {
        public static final Type<OpenHearth> TYPE = new Type<>(Dushi.id("open_hearth"));
        public static final StreamCodec<ByteBuf, OpenHearth> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, OpenHearth::pos,
                Stop.CODEC.apply(ByteBufCodecs.list()), OpenHearth::stops, OpenHearth::new);

        @Override
        public Type<OpenHearth> type() {
            return TYPE;
        }
    }

    /** Client to server: go from this hearth to another the player has rested at (its place in the list they were sent). */
    public record Travel(int index) implements CustomPacketPayload {
        public static final Type<Travel> TYPE = new Type<>(Dushi.id("travel"));
        public static final StreamCodec<ByteBuf, Travel> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Travel::index, Travel::new);

        @Override
        public Type<Travel> type() {
            return TYPE;
        }

        public static void handle(Travel payload, IPayloadContext context) {
            if (context.player() instanceof ServerPlayer player) {
                HearthBlock.travel(player, payload.index());
            }
        }
    }

    /** Client to server: take off the charm worn; it comes back as a thing. */
    public record Unwear() implements CustomPacketPayload {
        public static final Type<Unwear> TYPE = new Type<>(Dushi.id("unwear"));
        public static final StreamCodec<ByteBuf, Unwear> CODEC = StreamCodec.unit(new Unwear());

        @Override
        public Type<Unwear> type() {
            return TYPE;
        }

        public static void handle(Unwear payload, IPayloadContext context) {
            if (context.player() instanceof ServerPlayer player && player.isAlive()) {
                com.keykeeper.dushi.item.CharmItem.takeOff(player);
            }
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

    /** Server to client: the player touched an anvil, show what can be made. */
    public record OpenForge(BlockPos pos) implements CustomPacketPayload {
        public static final Type<OpenForge> TYPE = new Type<>(Dushi.id("open_forge"));
        public static final StreamCodec<ByteBuf, OpenForge> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, OpenForge::pos, OpenForge::new);

        @Override
        public Type<OpenForge> type() {
            return TYPE;
        }
    }

    /** Client to server: make thing number {@code index} of {@link Forge#recipes()} at an anvil standing close by. */
    public record Make(int index) implements CustomPacketPayload {
        public static final Type<Make> TYPE = new Type<>(Dushi.id("make"));
        public static final StreamCodec<ByteBuf, Make> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Make::index, Make::new);

        @Override
        public Type<Make> type() {
            return TYPE;
        }

        public static void handle(Make payload, IPayloadContext context) {
            if (context.player() instanceof ServerPlayer player) {
                apply(player, payload.index());
            }
        }

        /** Returns whether the thing was made. */
        public static boolean apply(ServerPlayer player, int index) {
            if (index < 0 || index >= Forge.recipes().size() || !player.isAlive()) {
                return false;
            }
            ServerLevel level = player.level();
            BlockPos centre = player.blockPosition();
            boolean near = false;
            for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-6, -3, -6), centre.offset(6, 3, 6))) {
                if (level.getBlockState(pos).is(Dushi.FORGE.get())) {
                    near = true;
                    break;
                }
            }
            Forge.Recipe recipe = Forge.recipes().get(index);
            if (!near || !Forge.canMake(player, recipe)) {
                return false;
            }
            if (!player.isCreative()) {
                PlayerSoul.of(player).souls -= recipe.souls();
                for (Forge.Part part : recipe.parts()) {
                    Forge.take(player, part.item().get().asItem(), part.count());
                }
            }
            ItemStack stack = recipe.result().get();
            if (!player.getInventory().add(stack)) {
                player.spawnAtLocation(level, stack);
            }
            player.syncData(Dushi.PLAYER_SOUL);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sounds.SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.6F, 1.0F);
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

    /**
     * Server to client: the ground shakes. The screen is rocked for {@code ticks} ticks, by {@code strength} (1.0 is a hard
     * shake; the bell tower's rising is about 0.9 at its foot). A new one replaces the one running if it is stronger.
     */
    public record Quake(int ticks, float strength) implements CustomPacketPayload {
        public static final Type<Quake> TYPE = new Type<>(Dushi.id("quake"));
        public static final StreamCodec<ByteBuf, Quake> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Quake::ticks,
                ByteBufCodecs.FLOAT, Quake::strength, Quake::new);

        @Override
        public Type<Quake> type() {
            return TYPE;
        }
    }

    /** Server to client: the screen of an ending (1 the bell, 2 the seat), see {@code EndingScreen}. */
    /**
     * The figure that stands up out of the sea when the bell is struck: where (the middle of the patch of sea it stands in; x, y, z in
     * the world), which way it faces, and how many ticks ago it began to rise - less than nothing while it has not begun, more than the
     * rising takes for a client that comes late. The client makes it ({@code client/Cinema}); the server has no such entity.
     */
    public record Colossus(int x, int y, int z, float yaw, int age) implements CustomPacketPayload {
        public static final Type<Colossus> TYPE = new Type<>(Dushi.id("colossus"));
        public static final StreamCodec<ByteBuf, Colossus> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Colossus::x, ByteBufCodecs.VAR_INT, Colossus::y,
                ByteBufCodecs.VAR_INT, Colossus::z, ByteBufCodecs.FLOAT, Colossus::yaw, ByteBufCodecs.VAR_INT, Colossus::age, Colossus::new);

        @Override
        public Type<Colossus> type() {
            return TYPE;
        }
    }

    /** The camera is taken from the player for the ending ({@link ClockState#BELL} or {@link ClockState#SEAT}); x, y, z is the rope or the seat. */
    public record Cutscene(int ending, int x, int y, int z) implements CustomPacketPayload {
        public static final Type<Cutscene> TYPE = new Type<>(Dushi.id("cutscene"));
        public static final StreamCodec<ByteBuf, Cutscene> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Cutscene::ending, ByteBufCodecs.VAR_INT, Cutscene::x,
                ByteBufCodecs.VAR_INT, Cutscene::y, ByteBufCodecs.VAR_INT, Cutscene::z, Cutscene::new);

        @Override
        public Type<Cutscene> type() {
            return TYPE;
        }
    }

    public record EndScreen(int ending) implements CustomPacketPayload {
        public static final Type<EndScreen> TYPE = new Type<>(Dushi.id("end_screen"));
        public static final StreamCodec<ByteBuf, EndScreen> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, EndScreen::ending, EndScreen::new);

        @Override
        public Type<EndScreen> type() {
            return TYPE;
        }
    }
}
