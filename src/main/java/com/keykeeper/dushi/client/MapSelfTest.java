package com.keykeeper.dushi.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.keykeeper.dushi.Dushi;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
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

    private record View(String name, double x, double y, double z, double toX, double toY, double toZ) {}

    private static int stage;
    private static int wait;
    private static int waited;
    private static int next;
    private static final List<View> LIST = new ArrayList<>();

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
                        if (p.length == 7) {
                            LIST.add(new View(p[0], Double.parseDouble(p[1]), Double.parseDouble(p[2]), Double.parseDouble(p[3]),
                                    Double.parseDouble(p[4]), Double.parseDouble(p[5]), Double.parseDouble(p[6])));
                        }
                    }
                    say("menu reached, " + LIST.size() + " views, opening " + WORLD);
                    minecraft.options.pauseOnLostFocus = false;
                    minecraft.options.guiScale().set(2);
                    minecraft.options.tutorialStep = TutorialSteps.NONE;
                    minecraft.getTutorial().setStep(TutorialSteps.NONE);
                    minecraft.options.renderDistance().set(12);
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
                    say("in the world: a newcomer plays " + minecraft.gameMode.getPlayerMode() + " at " + minecraft.player.blockPosition().toShortString()
                            + " looking " + Math.round(minecraft.player.getYRot()) + ", health " + minecraft.player.getHealth() + ", time "
                            + minecraft.level.getDefaultClockTime() + ", time moves " + server.overworld().getGameRules().get(GameRules.ADVANCE_TIME)
                            + ", commands allowed " + server.getPlayerList().isOp(server.getPlayerList().getPlayers().get(0).nameAndId()));
                    stage = 2;
                    wait = 300;
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
                shot(minecraft, "m00_first_sight");
                MinecraftServer server = minecraft.getSingleplayerServer();
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
            case 3 -> {
                if (next >= LIST.size()) {
                    stage = 5;
                    return;
                }
                View view = LIST.get(next);
                MinecraftServer server = minecraft.getSingleplayerServer();
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
                shot(minecraft, "m" + String.format("%02d", next + 1) + "_" + LIST.get(next).name);
                next++;
                stage = 3;
                wait = 4;
            }
            case 5 -> {
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

    private static void shot(Minecraft minecraft, String name) {
        Screenshot.grab(minecraft.gameDirectory, name + ".png", minecraft.gameRenderer.mainRenderTarget(), 1, message -> say("shot " + name));
    }
}
