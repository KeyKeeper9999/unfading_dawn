package com.keykeeper.dushi.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.SoulsMob;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Only runs in the automated build, when DUSHI_MAPTEST names a file of views (written by tools/gen_map.py): opens the saved
 * world of the map the way a player would, reports how a newcomer starts in it, then flies to every view and takes a picture.
 */
@EventBusSubscriber(modid = Dushi.MODID, value = Dist.CLIENT)
public final class MapSelfTest {
    private static final String VIEWS = System.getenv("DUSHI_MAPTEST");
    private static final String WORLD = "High_Stockade";
    /** What to photograph: empty for everything, or words of {@code .ci/mapshots} - a place ("depths", "borough", "lords") or the beginning of a view's name. */
    private static final String ONLY = System.getenv("DUSHI_MAPONLY") == null ? "" : System.getenv("DUSHI_MAPONLY").trim();
    private static final String[] PLACED = {"quarry", "mire", "firwood", "skete", "shore", "stubble", "depths", "near_the", "after_the"};

    /** The same sorting as the sheets' (tools/ci_sheets.py): a view without a place's word is the borough's or the stockade's by its number. */
    private static boolean wanted(String name, int number) {
        if (ONLY.isEmpty()) {
            return true;
        }
        boolean placed = false;
        for (String word : PLACED) {
            placed |= name.startsWith(word);
        }
        for (String word : ONLY.split("[\\s,]+")) {
            String prefixes = switch (word) {
                case "firwood" -> "firwood skete";
                case "wharf" -> "shore";
                case "lords" -> "near_the after_the";
                default -> word;
            };
            if (word.equals("borough") && !placed && number < 26 || word.equals("stockade") && !placed && number >= 26 && number < 190) {
                return true;
            }
            for (String prefix : prefixes.split(" ")) {
                if (!prefix.isEmpty() && name.startsWith(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    private record View(String name, double x, double y, double z, double toX, double toY, double toZ) {}

    private static int stage;
    private static int wait;
    private static int waited;
    private static int next;
    private static final List<View> LIST = new ArrayList<>();
    /** The hearths of the map, by name: the test rests at two of them and travels from one to the other. */
    private static final java.util.Map<String, BlockPos> HEARTHS = new java.util.LinkedHashMap<>();

    private MapSelfTest() {}

    private static void say(String text) {
        System.out.println("DUSHI MAPTEST " + text);
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        if (VIEWS == null || stage < 0) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        try {
            tick(minecraft);
        } catch (RuntimeException | IOException e) {
            say("FAILED at stage " + stage + ": " + e);
            e.printStackTrace(System.out);
            stage = -1;
            minecraft.stop();
        }
    }

    private static void tick(Minecraft minecraft) throws IOException {
        if (wait > 0) {
            wait--;
            return;
        }
        waited++;
        Screen screen = minecraft.gui.screen();
        switch (stage) {
            case 0 -> {
                if (screen != null && minecraft.isGameLoadFinished() && minecraft.gui.overlay() == null && waited > 40) {
                    for (String line : Files.readAllLines(Path.of(VIEWS))) {
                        String[] p = line.trim().split("\\s+");
                        if (p.length == 2 && p[0].equals("time")) {
                            LIST.add(new View("time", Double.parseDouble(p[1]), 0, 0, 0, 0, 0));
                        } else if (p.length == 5 && p[0].equals("hearth")) {
                            HEARTHS.put(p[1], BlockPos.containing(Double.parseDouble(p[2]), Double.parseDouble(p[3]), Double.parseDouble(p[4])));
                        } else if (p.length == 4 && p[0].equals("gate")) {
                            LIST.add(new View("gate", Double.parseDouble(p[1]), Double.parseDouble(p[2]), Double.parseDouble(p[3]), 0, 0, 0));
                        } else if (p.length == 5 && p[0].equals("kill")) {
                            LIST.add(new View("kill " + p[1], Double.parseDouble(p[2]), Double.parseDouble(p[3]), Double.parseDouble(p[4]), 0, 0, 0));
                        } else if (p.length == 7) {
                            LIST.add(new View(p[0], Double.parseDouble(p[1]), Double.parseDouble(p[2]), Double.parseDouble(p[3]),
                                    Double.parseDouble(p[4]), Double.parseDouble(p[5]), Double.parseDouble(p[6])));
                        }
                    }
                    say("menu reached, " + LIST.size() + " views, opening " + WORLD);
                    minecraft.options.pauseOnLostFocus = false;
                    minecraft.options.guiScale().set(2);
                    minecraft.options.tutorialStep = TutorialSteps.NONE;
                    minecraft.getTutorial().setStep(TutorialSteps.NONE);
                    minecraft.options.renderDistance().set(20);
                    minecraft.options.simulationDistance().set(8);
                    minecraft.getWindow().setWindowed(1280, 720);
                    minecraft.createWorldOpenFlows().openWorld(WORLD, () -> say("opening was cancelled"));
                    stage = 1;
                    waited = 0;
                } else if (waited > 2400) {
                    throw new IllegalStateException("never reached the title screen; screen is " + screen);
                }
            }
            case 1 -> {
                if (minecraft.player != null && minecraft.level != null && minecraft.getSingleplayerServer() != null && screen == null) {
                    MinecraftServer server = minecraft.getSingleplayerServer();
                    say("the world is called \"" + server.getWorldData().getLevelName() + "\"");
                    say("in the world: a newcomer plays " + minecraft.gameMode.getPlayerMode() + " at " + minecraft.player.blockPosition().toShortString()
                            + " looking " + Math.round(minecraft.player.getYRot()) + ", health " + minecraft.player.getHealth() + ", time "
                            + minecraft.level.getDefaultClockTime() + ", time moves " + server.overworld().getGameRules().get(GameRules.ADVANCE_TIME)
                            + ", commands allowed " + server.getPlayerList().isOp(server.getPlayerList().getPlayers().get(0).nameAndId()));
                    stage = 2;
                    wait = 300;
                } else if (screen instanceof DeathScreen && minecraft.player != null) {
                    say("FAILED: the newcomer died at " + minecraft.player.blockPosition().toShortString());
                    minecraft.player.respawn();
                    minecraft.gui.setScreen(null);
                } else if (waited % 100 == 99 && screen != null) {
                    say("waiting for the world, screen is " + screen.getClass().getName());
                    for (var child : screen.children()) {
                        if (child instanceof Button button) {
                            button.onPress(null);
                            break;
                        }
                    }
                } else if (waited > 4000) {
                    throw new IllegalStateException("the world never loaded; screen is " + screen);
                }
            }
            case 2 -> {
                // the first picture is what the newcomer sees; after it the camera is set free
                if (screen instanceof DeathScreen) {
                    say("FAILED: the newcomer died at " + minecraft.player.blockPosition().toShortString());
                    minecraft.player.respawn();
                    minecraft.gui.setScreen(null);
                    wait = 100;
                    return;
                }
                say("after a while: at " + minecraft.player.blockPosition().toShortString() + ", health " + minecraft.player.getHealth()
                        + ", on the ground " + minecraft.player.onGround());
                shot(minecraft, "m00_first_sight");
                stage = 21;
                wait = 5;
            }
            case 21 -> {
                // views named as_player_... are walked to as the newcomer is, with the screen as a player sees it
                MinecraftServer server = minecraft.getSingleplayerServer();
                while (next < LIST.size() && LIST.get(next).name.startsWith("as_player_") && !wanted("borough_" + LIST.get(next).name, 0)) {
                    next++;
                }
                if (next < LIST.size() && LIST.get(next).name.startsWith("as_player_")) {
                    View view = LIST.get(next);
                    server.execute(() -> {
                        ServerPlayer player = server.getPlayerList().getPlayers().get(0);
                        double dx = view.toX - view.x;
                        double dz = view.toZ - view.z;
                        player.connection.teleport(view.x, view.y - 1.62, view.z, (float) Math.toDegrees(Math.atan2(-dx, dz)), 0.0F);
                    });
                    stage = 22;
                    wait = 36;
                    return;
                }
                server.execute(() -> {
                    ServerPlayer player = server.getPlayerList().getPlayers().get(0);
                    say("on the server: game mode " + player.gameMode.getGameModeForPlayer() + ", respawn " + player.level().getRespawnData());
                    player.setGameMode(GameType.CREATIVE);
                    player.getAbilities().flying = true;
                    player.onUpdateAbilities();
                });
                stage = 3;
                wait = 20;
            }
            case 22 -> {
                say(LIST.get(next).name + ": at " + minecraft.player.blockPosition().toShortString() + ", health " + minecraft.player.getHealth());
                shot(minecraft, "m00_" + LIST.get(next).name);
                next++;
                stage = 21;
                wait = 4;
            }
            case 3 -> {
                // only some places asked for: the others' views, the levers and the lords are passed over, and so is the travelling after
                while (!ONLY.isEmpty() && next < LIST.size() && !LIST.get(next).name.equals("time")
                        && (LIST.get(next).name.equals("gate") || LIST.get(next).name.startsWith("kill ") || !wanted(LIST.get(next).name, next + 1))) {
                    next++;
                }
                if (next >= LIST.size()) {
                    stage = ONLY.isEmpty() ? 5 : 99;
                    return;
                }
                View view = LIST.get(next);
                MinecraftServer server = minecraft.getSingleplayerServer();
                if (view.name.startsWith("kill ")) {
                    // a lord of the place falls: the seal he kept must lift
                    String type = view.name.substring(5);
                    BlockPos seal = BlockPos.containing(view.x, view.y, view.z);
                    server.execute(() -> {
                        String before = server.overworld().getBlockState(seal).toString();
                        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), "kill @e[type=" + type + "]");
                        say("killed " + type + ": the seal at " + seal.toShortString() + " was " + before + ", is " + server.overworld().getBlockState(seal));
                    });
                    next++;
                    wait = 20;
                    return;
                }
                if (view.name.endsWith("_rouse")) {
                    // a lord is hurt to under half, so that what it is from then on can be looked at
                    net.minecraft.world.phys.AABB near = new net.minecraft.world.phys.AABB(view.x - 24.0, view.y - 12.0, view.z - 24.0, view.x + 24.0, view.y + 24.0, view.z + 24.0);
                    server.execute(() -> {
                        for (SoulsMob lord : server.overworld().getEntitiesOfClass(SoulsMob.class, near, SoulsMob::isBoss)) {
                            lord.setHealth(lord.getMaxHealth() * 0.4F);
                            say("roused " + lord.getType().toShortString() + " at " + lord.blockPosition().toShortString() + ", phase " + lord.phase());
                        }
                    });
                    next++;
                    wait = 5;
                    return;
                }
                if (view.name.equals("gate")) {
                    // a locked gate of the map: shut to one who has not its key, open - all of it - to one who has
                    BlockPos gate = BlockPos.containing(view.x, view.y, view.z);
                    server.execute(() -> {
                        ServerPlayer player = server.getPlayerList().getPlayers().get(0);
                        ServerLevel level = server.overworld();
                        BlockState state = level.getBlockState(gate);
                        if (!(state.getBlock() instanceof com.keykeeper.dushi.block.LockedGateBlock)) {
                            say("gate at " + gate.toShortString() + " is " + state + " FAILED");
                            return;
                        }
                        net.minecraft.world.level.GameType was = player.gameMode.getGameModeForPlayer();
                        player.setGameMode(net.minecraft.world.level.GameType.ADVENTURE);
                        net.minecraft.world.item.Item key = state.getValue(com.keykeeper.dushi.block.LockedGateBlock.LOCK).key();
                        player.getInventory().clearContent();
                        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(gate), Direction.EAST, gate, false);
                        state.useWithoutItem(level, player, hit);
                        boolean held = level.getBlockState(gate).getBlock() instanceof com.keykeeper.dushi.block.LockedGateBlock;
                        player.getInventory().add(new net.minecraft.world.item.ItemStack(key));
                        state.useWithoutItem(level, player, hit);
                        boolean open = level.getBlockState(gate).isAir() && level.getBlockState(gate.above(2)).isAir();
                        player.setGameMode(was);
                        say("gate at " + gate.toShortString() + ": holds without its key=" + held + ", opens with it=" + open + (held && open ? "" : " FAILED"));
                    });
                    next++;
                    wait = 10;
                    return;
                }
                if (view.name.equals("lever")) {
                    // a lever is thrown the way a player throws it: the door it bars must open
                    BlockPos lever = BlockPos.containing(view.x, view.y, view.z);
                    BlockPos door = BlockPos.containing(view.toX, view.toY, view.toZ);
                    server.execute(() -> {
                        ServerPlayer player = server.getPlayerList().getPlayers().get(0);
                        ServerLevel level = server.overworld();
                        BlockState state = level.getBlockState(lever);
                        String before = level.getBlockState(door).toString();
                        Object result = state.useWithoutItem(level, player, new BlockHitResult(Vec3.atCenterOf(lever), Direction.UP, lever, false));
                        say("lever at " + lever.toShortString() + " is " + state.getBlock() + ", thrown: " + result + "; the door at " + door.toShortString()
                                + " was " + before + ", is " + level.getBlockState(door));
                    });
                    next++;
                    wait = 5;
                    return;
                }
                if (view.name.equals("time")) {
                    long time = (long) view.x;
                    server.execute(() -> server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(),
                            "time set " + time));
                    say("time set to " + time);
                    next++;
                    wait = 60;
                    return;
                }
                server.execute(() -> {
                    ServerPlayer player = server.getPlayerList().getPlayers().get(0);
                    double dx = view.toX - view.x;
                    double dy = view.toY - view.y;
                    double dz = view.toZ - view.z;
                    float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                    float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
                    player.connection.teleport(view.x, view.y - 1.62, view.z, yaw, pitch);
                    player.getAbilities().flying = true;
                    player.onUpdateAbilities();
                });
                stage = 4;
                wait = 110;
            }
            case 4 -> {
                if (!minecraft.gui.hud.isHidden()) {
                    minecraft.gui.hud.toggle();
                    wait = 6;
                    return;
                }
                int mobs = 0;
                for (Entity entity : minecraft.level.entitiesForRendering()) {
                    if (entity instanceof SoulsMob && entity.distanceToSqr(minecraft.player) < 48.0 * 48.0) {
                        mobs++;
                    }
                }
                say(LIST.get(next).name + ": " + mobs + " of the dead within 48 blocks");
                if (LIST.get(next).name.startsWith("depths_tear")) {
                    // the Coming's hall: which way it is, and the eyes that are open in the rock round it
                    StringBuilder seen = new StringBuilder();
                    int eyes = 0;
                    for (Entity entity : minecraft.level.entitiesForRendering()) {
                        if (entity instanceof com.keykeeper.dushi.entity.WallEyeEntity eye) {
                            eyes++;
                            if (eye.great()) {
                                seen.append("; the great eye at ").append(String.format("%.2f %.2f %.2f", eye.getX(), eye.getY(), eye.getZ())).append(" looking ").append(Math.round(eye.getYRot()))
                                        .append(", ").append(eye.tickCount).append(" ticks old, open ").append(eye.lid(eye.tickCount));
                            }
                        } else if (entity instanceof com.keykeeper.dushi.entity.ComingEntity coming) {
                            seen.append("; the Coming at ").append(String.format("%.2f %.2f %.2f", coming.getX(), coming.getY(), coming.getZ())).append(" looking ").append(Math.round(coming.getYRot()))
                                    .append(", phase ").append(coming.phase()).append(", move ").append(coming.attackId()).append(", ").append(coming.getBbWidth()).append(" wide");
                        }
                    }
                    say(LIST.get(next).name + ": " + eyes + " eyes in the rock" + seen);
                }
                shot(minecraft, "m" + String.format("%02d", next + 1) + "_" + LIST.get(next).name);
                next++;
                stage = 3;
                wait = 4;
            }
            case 5 -> {
                // from hearth to hearth: a rest in the yard by night, a rest in the gatehouse by evening, and back
                if (minecraft.gui.hud.isHidden()) {
                    minecraft.gui.hud.toggle();
                }
                if (!HEARTHS.containsKey("yard") || !HEARTHS.containsKey("keeper")) {
                    stage = 56;
                    return;
                }
                sit(minecraft.getSingleplayerServer(), "yard", 18000);
                stage = 51;
                wait = 40;
            }
            case 51 -> {
                minecraft.gui.setScreen(null);
                sit(minecraft.getSingleplayerServer(), "keeper", 12350);
                stage = 52;
                wait = 40;
            }
            case 52 -> {
                say("travel: the client knows of " + HearthScreen.stops.size() + " hearths, screen is "
                        + (screen == null ? "none" : screen.getClass().getSimpleName()));
                minecraft.gui.setScreen(new TravelScreen());
                stage = 53;
                wait = 20;
            }
            case 53 -> {
                shot(minecraft, "m97_travel_screen");
                stage = 54;
                wait = 8;
            }
            case 54 -> {
                minecraft.gui.setScreen(null);
                MinecraftServer server = minecraft.getSingleplayerServer();
                server.execute(() -> {
                    ServerPlayer player = server.getPlayerList().getPlayers().get(0);
                    BlockPos before = player.blockPosition();
                    boolean went = com.keykeeper.dushi.block.HearthBlock.travel(player, 0);
                    say("travel: from " + before.toShortString() + ", went=" + went + ", now at " + player.blockPosition().toShortString() + ", "
                            + Math.round(Math.sqrt(player.blockPosition().distSqr(HEARTHS.get("yard")))) + " from the yard's hearth, time "
                            + Math.floorMod(server.overworld().getDefaultClockTime(), 24000L)
                            + (went && player.blockPosition().distSqr(HEARTHS.get("yard")) < 9.0
                            && Math.floorMod(server.overworld().getDefaultClockTime(), 24000L) == 18000L ? "" : " FAILED"));
                });
                stage = 55;
                wait = 60;
            }
            case 55 -> {
                shot(minecraft, "m98_after_travel");
                stage = 56;
                wait = 8;
            }
            case 56 -> {
                minecraft.gui.setScreen(new ForgeScreen());
                stage = 6;
                wait = 20;
            }
            case 6 -> {
                shot(minecraft, "m99_forge_screen");
                stage = 7;
                wait = 10;
            }
            default -> {
                say("done");
                stage = -1;
                minecraft.stop();
            }
        }
    }

    /** Sets the hour, puts the player beside the hearth of that name and has them rest at it. */
    private static void sit(MinecraftServer server, String name, int hour) {
        BlockPos hearth = HEARTHS.get(name);
        server.execute(() -> {
            ServerPlayer player = server.getPlayerList().getPlayers().get(0);
            ServerLevel level = server.overworld();
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), "time set " + hour);
            player.setGameMode(GameType.ADVENTURE);
            BlockPos seat = hearth.east();
            for (Direction side : Direction.Plane.HORIZONTAL) {
                if (level.getBlockState(hearth.relative(side)).isAir() && level.getBlockState(hearth.relative(side).above()).isAir()) {
                    seat = hearth.relative(side);
                    break;
                }
            }
            player.connection.teleport(seat.getX() + 0.5, seat.getY(), seat.getZ() + 0.5, 0.0F, 0.0F);
            BlockState state = level.getBlockState(hearth);
            Object result = state.useWithoutItem(level, player, new BlockHitResult(Vec3.atCenterOf(hearth), Direction.UP, hearth, false));
            say("travel: rested at " + name + " " + hearth.toShortString() + " (" + state.getBlock() + "), seat " + seat.toShortString() + ": " + result);
        });
    }

    private static void shot(Minecraft minecraft, String name) {
        Screenshot.grab(minecraft.gameDirectory, name + ".png", minecraft.gameRenderer.mainRenderTarget(), 1, message -> say("shot " + name));
    }
}
