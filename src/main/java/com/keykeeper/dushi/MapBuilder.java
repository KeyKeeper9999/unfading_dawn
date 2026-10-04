package com.keykeeper.dushi;

import java.nio.file.Path;

import com.keykeeper.dushi.world.Plan;
import com.keykeeper.dushi.world.PlanPiece;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.LevelData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Only runs in the automated build, when DUSHI_BUILDMAP names a plan file (see tools/gen_map.py): sets the whole map down
 * in the empty world the server was started with, makes it an adventure world (rules, time, where a newcomer appears),
 * saves and stops. The world folder is then zipped by the workflow: that is the map players download.
 */
@EventBusSubscriber(modid = Dushi.MODID)
public final class MapBuilder {
    private static final String PLAN_FILE = System.getenv("DUSHI_BUILDMAP");
    /** Where the plan's origin cell lands. High up: the clouds are below the walls. */
    public static final BlockPos ORIGIN = new BlockPos(0, 200, 0);
    /** The plan is drawn with the way forward to the north; turned, it leads west, into the sunset. */
    public static final Rotation ROTATION = Rotation.COUNTERCLOCKWISE_90;
    /** Just before the sun touches the horizon, for ever. */
    private static final int SUNSET = 11800;

    private static int ticks = -1;

    private MapBuilder() {}

    private static void say(String text) {
        System.out.println("DUSHI MAP " + text);
    }

    @SubscribeEvent
    static void onStarted(ServerStartedEvent event) {
        if (PLAN_FILE == null) {
            return;
        }
        try {
            build(event.getServer());
            ticks = 0;
        } catch (RuntimeException e) {
            say("FAILED: " + e);
            e.printStackTrace(System.out);
            event.getServer().halt(false);
        }
    }

    private static void build(MinecraftServer server) {
        ServerLevel level = server.overworld();
        Plan plan = Plan.fromFile("map", Path.of(PLAN_FILE));
        PlanPiece piece = new PlanPiece("map", ORIGIN, ROTATION);
        BoundingBox box = piece.getBoundingBox();
        say("plan " + plan.width + "x" + plan.height + "x" + plan.depth + ", box " + box + ", world is empty at the origin: "
                + level.getBlockState(ORIGIN.below(40)).isAir());
        long started = System.currentTimeMillis();
        for (int cx = (box.minX() >> 4) - 1; cx <= (box.maxX() >> 4) + 1; cx++) {
            for (int cz = (box.minZ() >> 4) - 1; cz <= (box.maxZ() >> 4) + 1; cz++) {
                level.getChunk(cx, cz);
            }
        }
        piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), level.getRandom(),
                new BoundingBox(-30000000, -2048, -30000000, 30000000, 2048, 30000000), new ChunkPos(0, 0), ORIGIN);
        say("blocks set in " + (System.currentTimeMillis() - started) + " ms");

        // Where a newcomer appears: the plan's origin cell, looking the way the map leads.
        level.setRespawnData(LevelData.RespawnData.of(level.dimension(), ORIGIN, ROTATION.rotate(net.minecraft.core.Direction.NORTH).toYRot(), 0.0F));
        GameRules rules = level.getGameRules();
        rules.set(GameRules.ADVANCE_TIME, false, server);
        rules.set(GameRules.ADVANCE_WEATHER, false, server);
        rules.set(GameRules.NATURAL_HEALTH_REGENERATION, false, server);
        rules.set(GameRules.RESPAWN_RADIUS, 0, server);
        rules.set(GameRules.SPAWN_PHANTOMS, false, server);
        rules.set(GameRules.SPAWN_PATROLS, false, server);
        rules.set(GameRules.SPAWN_WANDERING_TRADERS, false, server);
        rules.set(GameRules.MOB_GRIEFING, false, server);
        rules.set(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER, 0, server);
        rules.set(GameRules.LOCATOR_BAR, false, server);
        rules.set(GameRules.SHOW_ADVANCEMENT_MESSAGES, false, server);
        server.setDefaultGameType(GameType.ADVENTURE);
        server.setDifficulty(Difficulty.NORMAL, true);
        for (String command : new String[] {"time set " + SUNSET, "weather clear"}) {
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), command);
        }
        say("rules set: time " + level.getDefaultClockTime() + ", regeneration " + rules.get(GameRules.NATURAL_HEALTH_REGENERATION) + ", game type "
                + server.getDefaultGameType() + ", spawn " + level.getRespawnData());
    }

    @SubscribeEvent
    static void onTick(ServerTickEvent.Post event) {
        if (ticks < 0) {
            return;
        }
        // Light is worked out over the next ticks; give it time before saving.
        if (++ticks == 300) {
            MinecraftServer server = event.getServer();
            boolean saved = server.saveEverything(true, true, true);
            say("saved=" + saved + ", done");
            ticks = -1;
            server.halt(false);
        }
    }
}
