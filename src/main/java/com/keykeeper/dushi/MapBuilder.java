package com.keykeeper.dushi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.keykeeper.dushi.world.Plan;
import com.keykeeper.dushi.world.PlanPiece;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.LevelChunk;
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
 * Only runs in the automated build, when DUSHI_BUILDMAP names a plan file (see tools/gen_world.py): sets the whole map down
 * in the world the server was started with - flat land under forest, whose top layer is the floor of the map's gorge -
 * makes it an adventure world (rules, time, where a newcomer appears), saves and stops. The world folder is then zipped
 * by the workflow: that is the map players download.
 */
@EventBusSubscriber(modid = Dushi.MODID)
public final class MapBuilder {
    private static final String PLAN_FILE = System.getenv("DUSHI_BUILDMAP");
    /**
     * Where the plan's origin cell lands: the place a newcomer wakes at, on the flat world's ground. The ground is low on
     * purpose: a flat world's sea is at -63, the game cools a biome with every block above its sea, and a forest of firs
     * 126 blocks over it grows up under snow - which the map, set down later, would not have.
     */
    public static final BlockPos ORIGIN = new BlockPos(0, -46, 0);
    /** The plan is drawn with the way forward to the north; turned, it leads west, into the sunset. */
    public static final Rotation ROTATION = Rotation.COUNTERCLOCKWISE_90;
    /** The hour the map begins at: the dead of night, in the town. (Every place sets its own hour: see PlaceNameBlock.) */
    private static final int MIDNIGHT = 18000;

    private static int ticks = -1;
    /** 0: the blocks are down; 1: the light engine is being told about every chunk; 2: light is being spread; 3: done. */
    private static int stage;
    private static final List<LevelChunk> CHUNKS = new ArrayList<>();
    private static final List<CompletableFuture<?>> WAITING = new ArrayList<>();

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
        say("plan " + plan.width + "x" + plan.height + "x" + plan.depth + ", box " + box + ", the world's ground under the origin is "
                + level.getBlockState(ORIGIN.below()).getBlock() + ", over it " + level.getBlockState(ORIGIN).getBlock());
        long started = System.currentTimeMillis();
        for (int cx = (box.minX() >> 4) - 2; cx <= (box.maxX() >> 4) + 2; cx++) {
            for (int cz = (box.minZ() >> 4) - 2; cz <= (box.maxZ() >> 4) + 2; cz++) {
                level.setChunkForced(cx, cz, true);            // kept loaded until the light is done
                CHUNKS.add(level.getChunk(cx, cz));
            }
        }
        // The forest the world grew stands where the map goes: everything over the ground is taken away first.
        int felled = 0;
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int z = box.minZ(); z <= box.maxZ(); z++) {
                for (int y = ORIGIN.getY(); y < ORIGIN.getY() + 40; y++) {
                    at.set(x, y, z);
                    if (!level.getBlockState(at).isAir()) {
                        level.setBlock(at, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2 | 16);
                        felled++;
                    }
                }
            }
        }
        say("cleared " + felled + " blocks of forest in " + (System.currentTimeMillis() - started) + " ms");
        piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), level.getRandom(),
                new BoundingBox(-30000000, -2048, -30000000, 30000000, 2048, 30000000), new ChunkPos(0, 0), ORIGIN);
        say("blocks set in " + (System.currentTimeMillis() - started) + " ms");
        biomes(server, level);

        // Where a newcomer appears: the plan's origin cell, looking the way the map leads.
        level.setRespawnData(LevelData.RespawnData.of(level.dimension(), ORIGIN, ROTATION.rotate(net.minecraft.core.Direction.NORTH).toYRot(), 0.0F));
        GameRules rules = level.getGameRules();
        rules.set(GameRules.ADVANCE_TIME, false, server);
        rules.set(GameRules.ADVANCE_WEATHER, false, server);
        rules.set(GameRules.NATURAL_HEALTH_REGENERATION, false, server);
        rules.set(GameRules.RESPAWN_RADIUS, 0, server);
        rules.set(GameRules.SPAWN_MOBS, false, server);              // the dead of the map rise from their marks, nothing else walks it
        rules.set(GameRules.SPAWN_PHANTOMS, false, server);
        rules.set(GameRules.SPAWN_PATROLS, false, server);
        rules.set(GameRules.SPAWN_WANDERING_TRADERS, false, server);
        rules.set(GameRules.MOB_GRIEFING, false, server);
        rules.set(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER, 0, server);
        rules.set(GameRules.LOCATOR_BAR, false, server);
        rules.set(GameRules.SHOW_ADVANCEMENT_MESSAGES, false, server);
        server.setDefaultGameType(GameType.ADVENTURE);
        server.setDifficulty(Difficulty.NORMAL, true);
        for (String command : new String[] {"time set " + MIDNIGHT, "weather clear"}) {
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), command);
        }
        say("rules set: time " + level.getDefaultClockTime() + ", regeneration " + rules.get(GameRules.NATURAL_HEALTH_REGENERATION) + ", game type "
                + server.getDefaultGameType() + ", spawn " + level.getRespawnData());
    }

    /**
     * A place of the map may have a biome of its own - its water, its leaves, its fog and sky (the Mire). The file
     * beside the plan lists boxes of the world, a line each: the biome, then x and z of two corners. Every box is filled
     * from the bottom of the world to its top.
     */
    private static void biomes(MinecraftServer server, ServerLevel level) {
        Path file = Path.of(PLAN_FILE).resolveSibling("biomes.txt");
        if (!Files.exists(file)) {
            return;
        }
        GameRules rules = level.getGameRules();
        int limit = rules.get(GameRules.MAX_BLOCK_MODIFICATIONS);
        rules.set(GameRules.MAX_BLOCK_MODIFICATIONS, Integer.MAX_VALUE, server);
        int boxes = 0;
        int cells = 0;
        int failed = 0;
        String why = "";
        try {
            for (String line : Files.readAllLines(file)) {
                String[] p = line.trim().split("\\s+");
                if (p.length != 5) {
                    continue;
                }
                var biome = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BIOME).getOrThrow(
                        net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BIOME, net.minecraft.resources.Identifier.parse(p[0])));
                var result = net.minecraft.server.commands.FillBiomeCommand.fill(level,
                        new BlockPos(Integer.parseInt(p[1]), level.getMinY(), Integer.parseInt(p[2])),
                        new BlockPos(Integer.parseInt(p[3]), level.getMaxY(), Integer.parseInt(p[4])), biome);
                boxes++;
                if (result.left().isPresent()) {
                    cells += result.left().get();
                } else {
                    failed++;
                    why = result.right().map(Throwable::getMessage).orElse("?");
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            rules.set(GameRules.MAX_BLOCK_MODIFICATIONS, limit, server);
        }
        say("biomes: " + boxes + " boxes, " + cells + " cells changed, " + failed + " failed" + (failed > 0 ? " (" + why + ")" : ""));
    }

    @SubscribeEvent
    static void onTick(ServerTickEvent.Post event) {
        if (ticks < 0) {
            return;
        }
        ticks++;
        MinecraftServer server = event.getServer();
        ServerLevel level = server.overworld();
        try {
            light(server, level);
        } catch (RuntimeException | IOException e) {
            say("FAILED: " + e);
            e.printStackTrace(System.out);
            ticks = -1;
            server.halt(false);
        }
    }

    /**
     * Setting several hundred thousand blocks one by one into an empty world leaves its light in pieces (whole storeys came
     * out black). So the light of every chunk is thrown away and worked out again, the way the game does it for a chunk it
     * has just generated: first every chunk is announced to the light engine, then light is spread in every chunk.
     */
    private static void light(MinecraftServer server, ServerLevel level) throws IOException {
        ThreadedLevelLightEngine engine = level.getChunkSource().getLightEngine();
        boolean idle = WAITING.stream().allMatch(CompletableFuture::isDone);
        if (stage == 0 && ticks >= 40) {
            for (LevelChunk chunk : CHUNKS) {
                WAITING.add(engine.initializeLight(chunk, false));
            }
            stage = 1;
        } else if (stage == 1 && idle) {
            WAITING.clear();
            for (LevelChunk chunk : CHUNKS) {
                WAITING.add(engine.lightChunk(chunk, false));
            }
            say("light: " + CHUNKS.size() + " chunks announced by tick " + ticks);
            stage = 2;
        } else if (stage == 2 && idle) {
            WAITING.clear();
            say("light: spread by tick " + ticks);
            Path probes = Path.of(PLAN_FILE).resolveSibling("light.txt");
            if (Files.exists(probes)) {
                for (String line : Files.readAllLines(probes)) {
                    String[] p = line.trim().split("\\s+");
                    if (p.length == 4) {
                        BlockPos pos = new BlockPos(Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
                        say("light at " + p[0] + " " + pos.toShortString() + ": sky " + level.getBrightness(LightLayer.SKY, pos) + ", block "
                                + level.getBrightness(LightLayer.BLOCK, pos) + (level.getBlockState(pos).isAir() ? "" : " (in " + level.getBlockState(pos).getBlock() + ")"));
                    }
                }
            }
            java.util.Map<String, Integer> census = new java.util.TreeMap<>();
            for (LevelChunk chunk : CHUNKS) {
                for (var entity : chunk.getBlockEntities().values()) {
                    census.merge(entity.getClass().getSimpleName(), 1, Integer::sum);
                }
            }
            say("what stands in the world: " + census);
            for (LevelChunk chunk : CHUNKS) {
                chunk.markUnsaved();
                level.setChunkForced(chunk.getPos().x(), chunk.getPos().z(), false);
            }
            stage = 3;
            ticks = 100000;
        } else if (stage == 3 && ticks == 100040) {
            boolean saved = server.saveEverything(true, true, true);
            say("saved=" + saved + ", done");
            ticks = -1;
            server.halt(false);
        } else if (stage < 3 && ticks > 9000) {
            throw new IllegalStateException("the light was not ready after " + ticks + " ticks (stage " + stage + ")");
        }
    }
}
