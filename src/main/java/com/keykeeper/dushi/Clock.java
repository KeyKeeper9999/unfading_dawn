package com.keykeeper.dushi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.keykeeper.dushi.block.ClockSocketBlock;
import com.keykeeper.dushi.world.Plan;
import com.keykeeper.dushi.world.PlanPiece;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The clock under the Sky Beneath, and what it does to the borough above. Seven keys in seven sockets start it
 * ({@link #touchSocket}); a started clock raises the bell tower out of the market of the Dead Borough as soon as a player
 * comes near ({@link #tickRise}: a long rumble, the ground and the screen shaking, the tower coming up layer by layer);
 * at the top of the tower the day is ended one of two ways: the rope of the great bell ({@link #pullRope}: the bell
 * tolls three times, everything of the mod's that lived lies down, a figure rises on the horizon) or the Founder's seat
 * ({@link #takeSeat}: the day goes on). What was done is kept in {@link ClockState}, with the overworld, for everybody.
 *
 * <p>Where the tower and the figure stand is in {@code data/unfading_dawn/clock_sites.txt}, written by tools/gen_world.py
 * (the world coordinates of the plan's origin cell; the plans are set down turned as the map is, {@link MapBuilder#ROTATION}).
 */
@EventBusSubscriber(modid = Dushi.MODID)
public final class Clock {
    public static final Rotation ROTATION = MapBuilder.ROTATION;
    public static final String TOWER_PLAN = "clock_tower";
    public static final String FIGURE_PLAN = "horizon_figure";

    /** Ticks of rumbling before the first layer of the tower comes up. */
    static final int INTRO = 80;
    /** Ticks for each layer of the tower. */
    static final int PER_LAYER = 2;
    /** Ticks the tower stands shaking after its last layer. */
    static final int SETTLE = 60;
    /** How near a player must come for a started clock to raise the tower. */
    static final double TRIGGER = 90.0;
    /** While the tower rises a player must be at least this near, or it waits: the ground under it must be loaded. */
    static final double HOLD = 150.0;
    /** The shaking is felt this far from where it comes from. */
    static final double QUAKE_RANGE = 240.0;
    /** The bell tolls at 0, 70 and 140 ticks after the rope. */
    static final int TOLL_GAP = 70;
    /** Everything that lived lies down at this tick. */
    static final int LIE_DOWN = 150;
    /** The figure begins to rise at this tick, if the place it stands at is loaded. */
    static final int FIGURE_AT = 190;
    /** The screen of the ending comes this many ticks after the bell is struck. */
    static final int SCREEN_AT = 360;
    static final int SEAT_SCREEN_AT = 100;

    private static Map<String, BlockPos> sites;

    private Clock() {}

    // ------------------------------------------------------------------ state and sites

    public static ClockState state(ServerLevel level) {
        return level.getServer().overworld().getData(Dushi.CLOCK);
    }

    /** The bell has been struck: the day is over, and nothing of the mod's lives. */
    public static boolean ended(ServerLevel level) {
        return state(level).ending == ClockState.BELL;
    }

    /** A lord has fallen: the world remembers it, for the talk of those who live in it. */
    public static void bossSlain(ServerLevel level, Entity boss) {
        ClockState clock = state(level);
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(boss.getType()).getPath();
        clock.slain.add(id);
    }

    public static boolean slain(ServerLevel level, String id) {
        return state(level).slain.contains(id);
    }

    private static synchronized BlockPos site(String name) {
        if (sites == null) {
            sites = new HashMap<>();
            try (InputStream stream = Dushi.class.getResourceAsStream("/data/" + Dushi.MODID + "/clock_sites.txt")) {
                if (stream != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] part = line.trim().split(" ");
                        if (part.length == 4) {
                            sites.put(part[0], new BlockPos(Integer.parseInt(part[1]), Integer.parseInt(part[2]), Integer.parseInt(part[3])));
                        }
                    }
                }
            } catch (IOException | NumberFormatException e) {
                throw new IllegalStateException("cannot read the clock's sites", e);
            }
        }
        return sites.get(name);
    }

    /** Where the tower stands (the plan's origin cell), or null in a world whose map has no tower. */
    public static BlockPos towerSite() {
        return site("tower");
    }

    // ------------------------------------------------------------------ the sockets

    /** A hand on a socket: if it is empty and the key of its place is among the player's things, the key goes in for good. */
    public static void touchSocket(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state) {
        ClockState clock = state(level);
        ClockSocketBlock.Socket socket = state.getValue(ClockSocketBlock.SOCKET);
        if (state.getValue(ClockSocketBlock.FILLED) || clock.has(socket.bit)) {
            player.sendOverlayMessage(Component.translatable("message.unfading_dawn.clock.filled"));
            return;
        }
        Item key = socket.key();
        boolean creative = player.isCreative();
        if (!creative && Forge.count(player, key) <= 0) {
            level.playSound(null, pos, Dushi.sound("block.clock.empty"), SoundSource.BLOCKS, 1.0F, 0.8F);
            player.sendOverlayMessage(Component.translatable("message.unfading_dawn.clock.empty." + socket.id));
            return;
        }
        if (!creative) {
            Forge.take(player, key, 1);
        }
        level.setBlock(pos, state.setValue(ClockSocketBlock.FILLED, true), Block.UPDATE_ALL);
        clock.keys |= 1 << socket.bit;
        level.playSound(null, pos, Dushi.sound("block.clock.set"), SoundSource.BLOCKS, 1.6F, 0.9F + 0.04F * socket.bit);
        level.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 14, 0.3, 0.3, 0.3, 0.1);
        player.sendOverlayMessage(Component.translatable("message.unfading_dawn.clock.set", clock.count(), ClockState.SOCKETS));
        if (clock.keys == ClockState.ALL && !clock.started) {
            start(level, pos);
        }
    }

    /** The seventh key is in: the clock runs. Everybody in the world hears it, deep under the ground or not. */
    private static void start(ServerLevel level, BlockPos at) {
        ClockState clock = state(level);
        clock.started = true;
        clock.rise = -1;
        level.playSound(null, at, Dushi.sound("block.clock.start"), SoundSource.BLOCKS, 14.0F, 1.0F);
        for (ServerPlayer player : level.players()) {
            PacketDistributor.sendToPlayer(player, new Net.Banner("banner.unfading_dawn.clock_started", Net.Banner.GOLD, "ui.boss_defeated"));
            PacketDistributor.sendToPlayer(player, new Net.Quake(160, 0.45F));
            player.sendSystemMessage(Component.translatable("message.unfading_dawn.clock.started"));
        }
    }

    // ------------------------------------------------------------------ the tower rises

    @SubscribeEvent
    static void onTick(ServerTickEvent.Post event) {
        ServerLevel level = event.getServer().overworld();
        if (level == null) {
            return;
        }
        ClockState clock = level.getData(Dushi.CLOCK);
        if (!clock.started) {
            return;
        }
        if (!clock.raised) {
            tickRise(level, clock);
        } else if (clock.ending == ClockState.BELL) {
            tickBell(level, clock);
        } else if (clock.ending == ClockState.SEAT) {
            tickSeat(level, clock);
        }
    }

    private static boolean playerNear(ServerLevel level, BlockPos pos, double range) {
        for (ServerPlayer player : level.players()) {
            if (player.isAlive() && !player.isSpectator() && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= range * range) {
                return true;
            }
        }
        return false;
    }

    /** The tower's footprint in the world, whole height: a box. */
    private static AABB footprint(Plan plan, BlockPos site) {
        BlockPos a = PlanPiece.toWorld(plan, site, ROTATION, 0, 0, 0);
        BlockPos b = PlanPiece.toWorld(plan, site, ROTATION, plan.width - 1, plan.height - 1, plan.depth - 1);
        return new AABB(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()),
                Math.max(a.getX(), b.getX()) + 1, Math.max(a.getY(), b.getY()) + 1, Math.max(a.getZ(), b.getZ()) + 1);
    }

    private static void tickRise(ServerLevel level, ClockState clock) {
        BlockPos site = site("tower");
        if (site == null) {
            return;
        }
        if (clock.rise < 0) {
            if (!playerNear(level, site, TRIGGER)) {
                return;
            }
            clock.rise = 0;
        } else if (!playerNear(level, site, HOLD)) {
            return;
        }
        Plan plan = Plan.get(TOWER_PLAN);
        AABB box = footprint(plan, site);
        double cx = (box.minX + box.maxX) * 0.5;
        double cz = (box.minZ + box.maxZ) * 0.5;
        int t = clock.rise++;
        int layers = plan.height;
        int last = INTRO + layers * PER_LAYER;
        BlockPos centre = BlockPos.containing(cx, site.getY(), cz);
        if (t == 0) {
            // The great sound: heard far over the borough, and in every chest.
            level.playSound(null, centre, Dushi.sound("block.tower.rumble"), SoundSource.BLOCKS, 16.0F, 1.0F);
            level.playSound(null, centre, Dushi.sound("block.tower.crack"), SoundSource.BLOCKS, 12.0F, 0.6F);
        }
        if (t % 4 == 0) {
            float ramp = t < INTRO ? 0.45F + 0.35F * t / INTRO : 0.9F;
            quake(level, cx, cz, 10, ramp);
        }
        if (t < INTRO) {
            if (t % 8 == 0) {
                level.playSound(null, centre, Dushi.sound("block.tower.crack"), SoundSource.BLOCKS, 6.0F, 0.5F + level.getRandom().nextFloat() * 0.5F);
            }
            ground(level, box, site, 4 + t / 10);
        } else if (t < last) {
            int layer = (t - INTRO) / PER_LAYER;
            if ((t - INTRO) % PER_LAYER == 0) {
                evict(level, box, site.getY() + layer - plan.originY, site.getY());
                layer(level, plan, site, layer);
                if (layer % 6 == 0) {
                    level.playSound(null, centre, Dushi.sound("block.tower.crack"), SoundSource.BLOCKS, 10.0F, 0.7F + level.getRandom().nextFloat() * 0.5F);
                }
                ground(level, box, site, 10);
            }
        } else if (t == last) {
            level.playSound(null, centre, Dushi.sound("block.tower.settle"), SoundSource.BLOCKS, 16.0F, 1.0F);
            quake(level, cx, cz, 50, 1.2F);
            ground(level, box, site, 60);
        } else if (t >= last + SETTLE) {
            clock.raised = true;
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(cx, site.getY(), cz) < QUAKE_RANGE * QUAKE_RANGE) {
                    PacketDistributor.sendToPlayer(player, new Net.Banner("place.unfading_dawn.clock_tower", Net.Banner.PALE, "ui.place"));
                }
            }
        }
    }

    /** Sets down one layer of the plan, turned as the map is. */
    static void layer(ServerLevel level, Plan plan, BlockPos site, int y) {
        for (int z = 0; z < plan.depth; z++) {
            for (int x = 0; x < plan.width; x++) {
                short index = plan.cell(x, y, z);
                if (index == Plan.SKIP) {
                    continue;
                }
                BlockPos pos = PlanPiece.toWorld(plan, site, ROTATION, x, y, z);
                level.setBlock(pos, plan.palette[index].rotate(ROTATION), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
    }

    /** Dust and chips thrown up around the foot of the tower. */
    private static void ground(ServerLevel level, AABB box, BlockPos site, int count) {
        BlockState floor = Blocks.STONE_BRICKS.defaultBlockState();
        for (int i = 0; i < count; i++) {
            double x = box.minX - 2.0 + level.getRandom().nextDouble() * (box.maxX - box.minX + 4.0);
            double z = box.minZ - 2.0 + level.getRandom().nextDouble() * (box.maxZ - box.minZ + 4.0);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, floor), x, site.getY() + 0.2, z, 3, 0.4, 0.2, 0.4, 0.15);
            if (i % 3 == 0) {
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, site.getY() + 0.3, z, 1, 0.2, 0.6, 0.2, 0.02);
            }
        }
    }

    /** Whatever stands where a layer is about to come is put outside the foot of the tower: players and the dead alike. */
    private static void evict(ServerLevel level, AABB box, int layerY, int groundY) {
        AABB zone = new AABB(box.minX - 0.6, layerY - 2.0, box.minZ - 0.6, box.maxX + 0.6, layerY + 2.5, box.maxZ + 0.6);
        for (Entity entity : level.getEntities((Entity) null, zone, e -> !e.isSpectator() && (e instanceof ServerPlayer || e instanceof Mob))) {
            double left = entity.getX() - box.minX;
            double right = box.maxX - entity.getX();
            double north = entity.getZ() - box.minZ;
            double south = box.maxZ - entity.getZ();
            double least = Math.min(Math.min(left, right), Math.min(north, south));
            double x = entity.getX();
            double z = entity.getZ();
            if (least == left) {
                x = box.minX - 1.5;
            } else if (least == right) {
                x = box.maxX + 1.5;
            } else if (least == north) {
                z = box.minZ - 1.5;
            } else {
                z = box.maxZ + 1.5;
            }
            entity.teleportTo(level, x, groundY, z, Set.of(), entity.getYRot(), entity.getXRot(), true);
        }
    }

    /** Shakes the screens of everyone within reach, the less the farther. */
    static void quake(ServerLevel level, double x, double z, int ticks, float strength) {
        for (ServerPlayer player : level.players()) {
            double distance = Math.sqrt(player.distanceToSqr(x, player.getY(), z));
            if (distance >= QUAKE_RANGE) {
                continue;
            }
            float scaled = (float) (strength * Math.pow(1.0 - distance / QUAKE_RANGE, 0.6));
            if (scaled > 0.03F) {
                PacketDistributor.sendToPlayer(player, new Net.Quake(ticks, scaled));
            }
        }
    }

    // ------------------------------------------------------------------ the ending

    /** The rope of the great bell: pull it and the day ends. */
    public static void pullRope(ServerLevel level, ServerPlayer player, BlockPos pos) {
        ClockState clock = state(level);
        if (!clock.raised || clock.ending != ClockState.NO_ENDING) {
            player.sendOverlayMessage(Component.translatable("message.unfading_dawn.clock.rope_slack"));
            return;
        }
        clock.ending = ClockState.BELL;
        clock.endingTick = 0;
        clock.figure = 0;
        player.sendSystemMessage(Component.translatable("message.unfading_dawn.clock.rope"));
    }

    /** The Founder's seat: sit in it and the day goes on. */
    public static void takeSeat(ServerLevel level, ServerPlayer player, BlockPos pos) {
        ClockState clock = state(level);
        if (!clock.raised || clock.ending != ClockState.NO_ENDING) {
            player.sendOverlayMessage(Component.translatable("message.unfading_dawn.clock.seat_taken"));
            return;
        }
        clock.ending = ClockState.SEAT;
        clock.endingTick = 0;
        level.playSound(null, pos, Dushi.sound("block.tower.seat"), SoundSource.BLOCKS, 8.0F, 0.7F);
        player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 500, 3, false, false, true));
        player.sendSystemMessage(Component.translatable("message.unfading_dawn.clock.seat"));
        quake(level, pos.getX(), pos.getZ(), 30, 0.5F);
    }

    private static void tickBell(ServerLevel level, ClockState clock) {
        BlockPos site = site("tower");
        if (site == null) {
            return;
        }
        if (clock.endingTick > SCREEN_AT + 20 && clock.figure >= figureHeight()) {
            return;
        }
        int t = clock.endingTick++;
        Plan plan = Plan.get(TOWER_PLAN);
        AABB box = footprint(plan, site);
        double cx = (box.minX + box.maxX) * 0.5;
        double cz = (box.minZ + box.maxZ) * 0.5;
        BlockPos top = BlockPos.containing(cx, box.maxY - 4, cz);
        if (t <= 2 * TOLL_GAP && t % TOLL_GAP == 0) {
            level.playSound(null, top, Dushi.sound("block.tower.toll"), SoundSource.BLOCKS, 20.0F, 0.9F - 0.08F * (t / TOLL_GAP));
            quake(level, cx, cz, 60, 0.7F + 0.25F * (t / TOLL_GAP));
            level.sendParticles(ParticleTypes.POOF, cx, box.maxY - 3, cz, 40, 3.0, 1.0, 3.0, 0.05);
        }
        if (t == LIE_DOWN) {
            lieDown(level);
            for (ServerPlayer player : level.players()) {
                player.sendSystemMessage(Component.translatable("message.unfading_dawn.clock.silence"));
            }
        }
        if (t >= FIGURE_AT && clock.figure < figureHeight()) {
            raiseFigure(level, clock);
        }
        if (t == SCREEN_AT) {
            for (ServerPlayer player : level.players()) {
                PacketDistributor.sendToPlayer(player, new Net.EndScreen(ClockState.BELL));
            }
        }
    }

    private static int figureHeight() {
        return site("figure") == null ? 0 : Plan.get(FIGURE_PLAN).height;
    }

    /** One more layer of the figure on the horizon, each time the whole of its foot is loaded; the ground shakes where it stands. */
    private static void raiseFigure(ServerLevel level, ClockState clock) {
        BlockPos site = site("figure");
        Plan plan = Plan.get(FIGURE_PLAN);
        if (clock.endingTick % 2 != 0) {
            return;
        }
        BlockPos a = PlanPiece.toWorld(plan, site, ROTATION, 0, 0, 0);
        BlockPos b = PlanPiece.toWorld(plan, site, ROTATION, plan.width - 1, 0, plan.depth - 1);
        if (!level.isLoaded(a) || !level.isLoaded(b) || !level.isLoaded(BlockPos.containing((a.getX() + b.getX()) * 0.5, a.getY(), (a.getZ() + b.getZ()) * 0.5))) {
            return;
        }
        if (clock.figure == 0) {
            level.playSound(null, site, Dushi.sound("block.tower.rumble"), SoundSource.BLOCKS, 24.0F, 0.6F);
        }
        layer(level, plan, site, clock.figure++);
        if (clock.figure % 4 == 0) {
            quake(level, site.getX(), site.getZ(), 12, 0.5F);
        }
    }

    /** Everything of the mod's that lived lies down: the dead, the lords and those who talked alike. */
    static void lieDown(ServerLevel level) {
        List<Mob> mobs = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof Mob mob && lives(mob)) {
                mobs.add(mob);
            }
        }
        for (Mob mob : mobs) {
            lieDown(level, mob);
        }
    }

    private static void lieDown(ServerLevel level, Mob mob) {
        level.sendParticles(ParticleTypes.SOUL, mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(), 6, 0.3, 0.4, 0.3, 0.03);
        mob.discard();
    }

    private static boolean lives(Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace().equals(Dushi.MODID);
    }

    /** What walks into the world after the bell does not stay: the chunks that were asleep wake to an empty field. */
    @SubscribeEvent
    static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof Mob mob && lives(mob)) {
            ClockState clock = level.getServer().overworld().getData(Dushi.CLOCK);
            if (clock.ending == ClockState.BELL && clock.endingTick > LIE_DOWN) {
                event.setCanceled(true);
            }
        }
    }

    private static void tickSeat(ServerLevel level, ClockState clock) {
        BlockPos site = site("tower");
        if (site == null) {
            return;
        }
        int t = clock.endingTick++;
        if (t == SEAT_SCREEN_AT) {
            Plan plan = Plan.get(TOWER_PLAN);
            AABB box = footprint(plan, site);
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr((box.minX + box.maxX) * 0.5, player.getY(), (box.minZ + box.maxZ) * 0.5) < 60.0 * 60.0) {
                    PacketDistributor.sendToPlayer(player, new Net.EndScreen(ClockState.SEAT));
                }
            }
        }
    }

    /** Whether the player has already had the bell's last word or the seat's (used by the folk's talk). */
    public static int ending(ServerLevel level) {
        return state(level).ending;
    }
}
