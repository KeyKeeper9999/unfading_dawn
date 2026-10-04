package com.keykeeper.dushi.client;

import java.util.ArrayList;
import java.util.List;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.PlayerSoul;
import com.keykeeper.dushi.entity.ChaplainEntity;
import com.keykeeper.dushi.entity.OverseerEntity;
import com.keykeeper.dushi.entity.RustKnightEntity;
import com.keykeeper.dushi.entity.SoulsMob;
import com.keykeeper.dushi.entity.WitheredEntity;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Only runs in the automated build (when the DUSHI_CLIENTTEST environment variable is set): starts a flat world by itself,
 * lines the mobs, blocks and items up in front of the camera, takes screenshots and quits. The pictures are how the look
 * of the mod is checked without anybody at the keyboard.
 */
@EventBusSubscriber(modid = Dushi.MODID, value = Dist.CLIENT)
public final class ClientSelfTest {
    private static final boolean ENABLED = System.getenv("DUSHI_CLIENTTEST") != null;
    /** The flat world's grass is at y = -61; things stand on it at -60. */
    private static final int GROUND = -60;

    private static int stage;
    private static int wait;
    private static int waited;
    private static int step;
    private static final List<Runnable> STEPS = new ArrayList<>();

    private ClientSelfTest() {}

    private static void say(String text) {
        System.out.println("DUSHI CLIENTTEST " + text);
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        if (!ENABLED || stage < 0) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        try {
            tick(minecraft);
        } catch (RuntimeException e) {
            say("FAILED at stage " + stage + " step " + step + ": " + e);
            e.printStackTrace(System.out);
            stage = -1;
            minecraft.stop();
        }
    }

    private static void tick(Minecraft minecraft) {
        if (wait > 0) {
            wait--;
            return;
        }
        waited++;
        Screen screen = minecraft.gui.screen();
        switch (stage) {
            case 0 -> {
                // Wait for the title screen, then make a flat creative world.
                // (On a first start the game shows its accessibility screen instead of the title; either will do.)
                if (screen != null && minecraft.isGameLoadFinished() && minecraft.gui.overlay() == null && waited > 40) {
                    say("menu reached (" + screen.getClass().getSimpleName() + "), creating a world");
                    minecraft.options.pauseOnLostFocus = false;
                    minecraft.options.guiScale().set(2);
                    LevelSettings settings = new LevelSettings("dushi_test", GameType.CREATIVE,
                            new LevelSettings.DifficultySettings(Difficulty.NORMAL, false, false), true, WorldDataConfiguration.DEFAULT);
                    minecraft.createWorldOpenFlows().createFreshLevel("dushi_test", settings, new WorldOptions(20261004L, false, false),
                            registries -> registries.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
                            screen);
                    stage = 1;
                    waited = 0;
                } else if (waited > 2400) {
                    throw new IllegalStateException("never reached the title screen; screen is " + screen);
                }
            }
            case 1 -> {
                if (minecraft.player != null && minecraft.level != null && minecraft.getSingleplayerServer() != null && screen == null) {
                    say("in the world");
                    buildSteps(minecraft);
                    stage = 2;
                    wait = 60;
                } else if (waited % 100 == 99 && screen != null) {
                    // Some confirmation is in the way: press its first button.
                    say("waiting for the world, screen is " + screen.getClass().getName());
                    for (var child : screen.children()) {
                        if (child instanceof Button button) {
                            button.onPress(null);
                            break;
                        }
                    }
                } else if (waited > 3000) {
                    throw new IllegalStateException("the world never loaded; screen is " + screen);
                }
            }
            case 2 -> {
                if (step >= STEPS.size()) {
                    say("done");
                    stage = -1;
                    minecraft.stop();
                    return;
                }
                STEPS.get(step++).run();
            }
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------ helpers

    private static void pause(int ticks) {
        STEPS.add(() -> wait = ticks);
    }

    private static void command(String... commands) {
        STEPS.add(() -> {
            MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
            server.execute(() -> {
                for (String command : commands) {
                    try {
                        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
                    } catch (RuntimeException e) {
                        say("command failed: " + command + " : " + e);
                    }
                }
            });
        });
    }

    /** Runs something on the server thread with the overworld. */
    private static void server(java.util.function.Consumer<ServerLevel> action) {
        STEPS.add(() -> {
            MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
            server.execute(() -> {
                try {
                    action.accept(server.overworld());
                } catch (RuntimeException e) {
                    say("server action failed: " + e);
                    e.printStackTrace(System.out);
                }
            });
        });
    }

    private static void client(Runnable action) {
        STEPS.add(action);
    }

    private static void shot(String name) {
        pause(12);
        STEPS.add(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            Screenshot.grab(minecraft.gameDirectory, name + ".png", minecraft.gameRenderer.mainRenderTarget(), 1, message -> say("shot " + name));
        });
        pause(6);
    }

    private static <T extends SoulsMob> void attack(Class<T> type, int id) {
        server(level -> {
            for (T mob : level.getEntitiesOfClass(type, new AABB(-40, GROUND - 5, -40, 40, GROUND + 30, 40))) {
                mob.startAttack(id);
            }
        });
    }

    private static String summon(String id, double x, double z, String extra) {
        return "summon vsratie_dushi:" + id + " " + x + " " + GROUND + " " + z + " {NoAI:1b,PersistenceRequired:1b,Rotation:[180f,0f]" + extra + "}";
    }

    private static String held(String main, String off) {
        StringBuilder s = new StringBuilder(",equipment:{");
        if (main != null) {
            s.append("mainhand:{id:\"vsratie_dushi:").append(main).append("\"}");
        }
        if (off != null) {
            s.append(main != null ? "," : "").append("offhand:{id:\"vsratie_dushi:").append(off).append("\"}");
        }
        return s.append('}').toString();
    }

    // ------------------------------------------------------------------ the script

    private static void buildSteps(Minecraft minecraft) {
        String clear = "kill @e[type=!minecraft:player]";
        command("time set 6000", "weather clear", "fill -14 " + (GROUND - 1) + " -6 14 " + (GROUND - 1) + " 22 minecraft:stone_bricks",
                "fill -14 " + GROUND + " 16 14 " + (GROUND + 9) + " 16 minecraft:deepslate_bricks",
                "tp @p 0.5 " + GROUND + " -2.5 0 2");
        pause(40);

        // 1. the ordinary enemies and the keykeeper
        command(summon("withered_prisoner", -5.5, 4, held("broken_sword", null)), summon("withered_guard", -3, 4, held("shortsword", "guard_shield")),
                summon("withered_archer", -0.5, 4, held("short_bow", null)), summon("rust_knight", 2.5, 4.5, held("rust_greatsword", "tower_shield")),
                summon("keykeeper", 5.5, 4, ""));
        pause(30);
        shot("01_mobs");
        attack(WitheredEntity.class, WitheredEntity.SWING);
        attack(RustKnightEntity.class, RustKnightEntity.CHOP);
        pause(8);
        shot("02_mobs_windup");
        command("tp @p 3.5 " + GROUND + " -0.5 -150 5");
        attack(RustKnightEntity.class, RustKnightEntity.SWEEP);
        pause(13);
        shot("03_mobs_side");

        // 2. the bosses, at rest and in the middle of their moves, in both phases
        command(clear, "tp @p 0.5 " + GROUND + " -4.5 0 -8", summon("overseer", -3.5, 6, ""), summon("chaplain", 4, 6, ""));
        pause(30);
        shot("04_bosses");
        attack(OverseerEntity.class, OverseerEntity.SLAM);
        attack(ChaplainEntity.class, ChaplainEntity.WRATH);
        pause(14);
        shot("05_bosses_windup");
        pause(6);
        shot("06_bosses_strike");
        pause(40);
        attack(OverseerEntity.class, OverseerEntity.SWEEP);
        attack(ChaplainEntity.class, ChaplainEntity.SPEARS);
        pause(15);
        shot("07_bosses_sweep");
        pause(40);
        command("data merge entity @e[type=vsratie_dushi:overseer,limit=1] {Phase:2}", "data merge entity @e[type=vsratie_dushi:chaplain,limit=1] {Phase:2}",
                "time set 18000");
        attack(OverseerEntity.class, OverseerEntity.ROAR);
        attack(ChaplainEntity.class, ChaplainEntity.CALL);
        pause(22);
        shot("08_bosses_phase2_night");
        command("time set 6000");

        // 3. blocks, every item of the mod as a picture in the air, the things on the HUD
        List<String> items = new ArrayList<>();
        Dushi.ITEMS.getEntries().forEach(entry -> items.add(entry.getId().toString()));
        List<String> displays = new ArrayList<>();
        int columns = 12;
        for (int i = 0; i < items.size(); i++) {
            double x = -6.6 + (i % columns) * 1.2;
            double y = GROUND + 6.3 - (i / columns) * 1.2;
            displays.add("summon minecraft:item_display " + x + " " + y + " 5 {item:{id:\"" + items.get(i) + "\"},item_display:\"gui\",billboard:\"center\"}");
        }
        command(clear, "tp @p 0.5 " + GROUND + " -4.5 0 -14", "setblock -3 " + GROUND + " 3 vsratie_dushi:hearth[lit=true]",
                "setblock -1 " + GROUND + " 3 vsratie_dushi:hearth[lit=false]",
                "fill 1 " + GROUND + " 3 3 " + (GROUND + 2) + " 3 vsratie_dushi:fog_gate[axis=x]");
        command(displays.toArray(String[]::new));
        pause(40);
        shot("09_items_blocks");

        // 4. the player from outside with sword and shield, then blocking; the HUD of a survivor
        command(clear, "gamemode survival @p", "item replace entity @p weapon.mainhand with vsratie_dushi:longsword",
                "item replace entity @p weapon.offhand with vsratie_dushi:guard_shield", "item replace entity @p hotbar.1 with vsratie_dushi:warmth_flask",
                "item replace entity @p hotbar.2 with vsratie_dushi:great_key", "item replace entity @p hotbar.3 with vsratie_dushi:tower_shield",
                "tp @p -2.5 " + GROUND + " 0.5 0 10");
        client(() -> {
            PlayerSoul soul = PlayerSoul.of(Minecraft.getInstance().player);
            soul.souls = 1234;
            soul.stamina = 55.0F;
            SoulsHud.showBanner("banner.vsratie_dushi.hearth_lit", 1);
        });
        pause(20);
        shot("10_first_person_hud");
        client(() -> Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_FRONT));
        pause(10);
        shot("11_third_person_front");
        client(() -> Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK));
        pause(10);
        shot("12_third_person_back");
        client(() -> Minecraft.getInstance().options.keyUse.setDown(true));
        pause(12);
        shot("13_blocking_back");
        client(() -> Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_FRONT));
        pause(8);
        shot("14_blocking_front");
        client(() -> Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON));
        pause(8);
        shot("15_blocking_first_person");
        client(() -> Minecraft.getInstance().options.keyUse.setDown(false));
        command("item replace entity @p weapon.mainhand with vsratie_dushi:great_key", "item replace entity @p weapon.offhand with vsratie_dushi:tower_shield");
        client(() -> Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_FRONT));
        pause(12);
        shot("16_great_key_tower_shield");
        client(() -> Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON));
        pause(8);
        shot("17_great_key_first_person");

        // 5. the hearth screen
        client(() -> Minecraft.getInstance().gui.setScreen(new HearthScreen()));
        pause(10);
        shot("18_hearth_screen");
        client(() -> Minecraft.getInstance().gui.setScreen(null));
        pause(5);
    }
}
