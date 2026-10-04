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
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
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
    private static final AABB ARENA = new AABB(-40, GROUND - 5, -40, 40, GROUND + 30, 40);

    private static int stage;
    private static int wait;
    private static int waited;
    private static int step;
    /** While not negative, every mob of the mod is kept at this tick of its move, so a picture of it cannot be missed. */
    private static int holdTime = -1;
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
        if (holdTime >= 0 && minecraft.level != null) {
            for (Entity entity : minecraft.level.entitiesForRendering()) {
                if (entity instanceof SoulsMob mob && mob.attackId() != 0) {
                    mob.attackTime = holdTime;
                }
            }
        }
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
                    minecraft.options.tutorialStep = TutorialSteps.NONE;
                    minecraft.getTutorial().setStep(TutorialSteps.NONE);
                    minecraft.getWindow().setWindowed(1280, 720);
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
                        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), command);
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

    /** Waits {@code settle} ticks, then saves what is on the screen. */
    private static void shot(String name, int settle) {
        pause(settle);
        STEPS.add(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            Screenshot.grab(minecraft.gameDirectory, name + ".png", minecraft.gameRenderer.mainRenderTarget(), 1, message -> say("shot " + name));
        });
        pause(3);
    }

    private static void shot(String name) {
        shot(name, 14);
    }

    /** Puts the player on the test floor, looking at the given angles. */
    private static String look(double x, double z, float yaw, float pitch) {
        return "tp @p " + x + " " + GROUND + " " + z + " " + yaw + " " + pitch;
    }

    /** Starts move {@code id} of every mob of the type and freezes it at tick {@code time}. */
    private static <T extends SoulsMob> void attack(Class<T> type, int id, int time) {
        client(() -> holdTime = time);
        server(level -> {
            for (T mob : level.getEntitiesOfClass(type, ARENA)) {
                mob.startAttack(id);
            }
        });
    }

    /** Lets every mob go back to standing. */
    private static void rest() {
        server(level -> {
            for (SoulsMob mob : level.getEntitiesOfClass(SoulsMob.class, ARENA)) {
                mob.startAttack(0);
                mob.stopUsingItem();
            }
        });
        client(() -> holdTime = -1);
    }

    /** Runs something on the server thread with the player. */
    private static void player(java.util.function.Consumer<ServerPlayer> action) {
        server(level -> level.players().forEach(action));
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
        String gone = "tp @e[type=!minecraft:player] 0 -200 0";
        command("time set 6000", "weather clear", "fill -16 " + (GROUND - 1) + " -8 16 " + (GROUND - 1) + " 26 minecraft:stone_bricks",
                "fill -16 " + GROUND + " 12 16 " + (GROUND + 9) + " 12 minecraft:deepslate_bricks", look(0.5, 0.5, 0, 5));
        pause(40);

        // 1. the three withered: at rest, the arm raised, the blow, then shield up and bow drawn
        command(summon("withered_prisoner", -2.0, 4.5, held("broken_sword", null)), summon("withered_guard", 0.5, 4.5, held("shortsword", "guard_shield")),
                summon("withered_archer", 3.0, 4.5, held("short_bow", null)));
        shot("01_withered", 30);
        attack(WitheredEntity.class, WitheredEntity.SWING, 8);
        shot("02_withered_raise");
        attack(WitheredEntity.class, WitheredEntity.SWING, 12);
        shot("02_withered_strike");
        rest();
        server(level -> {
            for (WitheredEntity mob : level.getEntitiesOfClass(WitheredEntity.class, ARENA)) {
                if (mob.kind() == WitheredEntity.Kind.GUARD) {
                    mob.startUsingItem(InteractionHand.OFF_HAND);
                } else if (mob.kind() == WitheredEntity.Kind.ARCHER) {
                    mob.startAttack(WitheredEntity.SHOOT);
                }
            }
        });
        shot("03_withered_guard_and_bow", 30);
        command(look(-4.0, 6.5, -110, 5));
        shot("03_withered_side");
        rest();

        // 2. the rust knight and the keykeeper
        command(gone, look(0.5, 0.5, 0, 0), summon("rust_knight", -1.0, 4.5, held("rust_greatsword", "tower_shield")), summon("keykeeper", 2.5, 4.0, ""));
        shot("04_knight_keykeeper", 30);
        attack(RustKnightEntity.class, RustKnightEntity.CHOP, 13);
        shot("05_knight_chop_raise");
        attack(RustKnightEntity.class, RustKnightEntity.CHOP, 19);
        shot("05_knight_chop_strike");
        attack(RustKnightEntity.class, RustKnightEntity.LUNGE, 16);
        shot("05_knight_lunge");
        command(look(-5.0, 6.0, -115, 0));
        attack(RustKnightEntity.class, RustKnightEntity.SWEEP, 18);
        shot("06_knight_sweep_side");
        rest();
        server(level -> level.getEntitiesOfClass(RustKnightEntity.class, ARENA).forEach(mob -> mob.startUsingItem(InteractionHand.OFF_HAND)));
        shot("06_knight_guard_side", 20);
        command(look(0.5, 0.5, 0, 0));
        shot("06_knight_guard_front");
        rest();
        command(look(-1.0, 8.5, 180, 0));
        shot("07_knight_back");

        // 3. the overseer: at rest, key raised, key down, sweeping, and enraged at night
        command(gone, look(0.5, 0.5, 0, -12), summon("overseer", 0.5, 7.0, ""));
        shot("08_overseer", 30);
        attack(OverseerEntity.class, OverseerEntity.SLAM, 15);
        shot("09_overseer_raise");
        attack(OverseerEntity.class, OverseerEntity.SLAM, 23);
        shot("10_overseer_strike");
        attack(OverseerEntity.class, OverseerEntity.SWEEP, 19);
        shot("11_overseer_sweep");
        command(look(-6.0, 9.0, -110, -8));
        attack(OverseerEntity.class, OverseerEntity.SLAM, 23);
        shot("12_overseer_strike_side");
        rest();
        command(look(0.5, 12.0, 180, -12));
        shot("13_overseer_back");
        command(look(0.5, 0.5, 0, -12), "data merge entity @e[type=vsratie_dushi:overseer,limit=1] {Phase:2}", "time set 18000");
        attack(OverseerEntity.class, OverseerEntity.ROAR, 25);
        shot("14_overseer_roar_night", 20);
        attack(OverseerEntity.class, OverseerEntity.CHAIN, 24);
        shot("15_overseer_chain_night");
        rest();
        command("time set 6000");

        // 4. the chaplain: at rest, spears, wrath, censer, litany, and with the black fire at night
        command(gone, look(0.5, 0.5, 0, -8), summon("chaplain", 0.5, 5.5, ""));
        shot("16_chaplain", 30);
        attack(ChaplainEntity.class, ChaplainEntity.SPEARS, 20);
        shot("17_chaplain_spears");
        attack(ChaplainEntity.class, ChaplainEntity.WRATH, 25);
        shot("18_chaplain_wrath");
        attack(ChaplainEntity.class, ChaplainEntity.CENSER, 17);
        shot("19_chaplain_censer");
        command(look(-4.0, 7.0, -110, -5));
        attack(ChaplainEntity.class, ChaplainEntity.LITANY, 40);
        shot("20_chaplain_litany_side");
        command(look(0.5, 0.5, 0, -8), "data merge entity @e[type=vsratie_dushi:chaplain,limit=1] {Phase:2}", "time set 18000");
        attack(ChaplainEntity.class, ChaplainEntity.CALL, 25);
        shot("21_chaplain_call_night", 20);
        rest();
        command("time set 6000");

        // 5. every item of the mod as a picture in the air
        List<String> items = new ArrayList<>();
        Dushi.ITEMS.getEntries().forEach(entry -> items.add(entry.getId().toString()));
        List<String> displays = new ArrayList<>();
        int columns = 11;
        for (int i = 0; i < items.size(); i++) {
            double x = 0.5 + 4.0 - (i % columns) * 0.8;
            double y = GROUND + 4.0 - (i / columns) * 0.8;
            displays.add("summon minecraft:item_display " + x + " " + y + " 5 {item:{id:\"" + items.get(i) + "\"},item_display:\"gui\",billboard:\"center\","
                    + "transformation:{translation:[0f,0f,0f],left_rotation:[0f,0f,0f,1f],scale:[0.7f,0.7f,0.7f],right_rotation:[0f,0f,0f,1f]}}");
        }
        command(gone, clear, look(0.5, 0.5, 0, -12));
        command(displays.toArray(String[]::new));
        shot("22_items", 40);

        // 6. the blocks
        command(clear, "setblock -1 " + GROUND + " 4 vsratie_dushi:hearth[lit=true]", "setblock 1 " + GROUND + " 4 vsratie_dushi:hearth[lit=false]",
                "fill 3 " + GROUND + " 4 4 " + (GROUND + 2) + " 4 vsratie_dushi:fog_gate[axis=x]", look(0.5, 1.0, 0, 18));
        shot("23_blocks", 40);
        command("time set 18000");
        shot("24_blocks_night", 20);
        command("time set 6000", "fill -1 " + GROUND + " 4 4 " + (GROUND + 2) + " 4 minecraft:air");

        // 7. a survivor with sword and shield: the HUD, then from outside, then blocking
        command("gamemode survival @p", "item replace entity @p weapon.mainhand with vsratie_dushi:longsword",
                "item replace entity @p weapon.offhand with vsratie_dushi:guard_shield", "item replace entity @p hotbar.1 with vsratie_dushi:warmth_flask",
                "item replace entity @p hotbar.2 with vsratie_dushi:great_key", "item replace entity @p hotbar.3 with vsratie_dushi:scholar_staff",
                look(0.5, 4.5, 0, 8));
        player(player -> {
            PlayerSoul soul = PlayerSoul.of(player);
            soul.souls = 1234;
            soul.stamina = 55.0F;
            soul.regenPause = 400;
            player.syncData(Dushi.PLAYER_SOUL);
        });
        client(() -> SoulsHud.showBanner("banner.vsratie_dushi.hearth_lit", 1));
        shot("25_first_person_hud", 20);
        player(player -> {
            PlayerSoul.of(player).regenPause = 0;
            player.syncData(Dushi.PLAYER_SOUL);
        });
        pause(90);
        camera(CameraType.THIRD_PERSON_FRONT);
        shot("26_player_front");
        camera(CameraType.THIRD_PERSON_BACK);
        shot("27_player_back");
        client(() -> Minecraft.getInstance().options.keyUse.setDown(true));
        shot("28_blocking_back", 20);
        camera(CameraType.THIRD_PERSON_FRONT);
        shot("29_blocking_front");
        camera(CameraType.FIRST_PERSON);
        shot("30_blocking_first_person");
        client(() -> Minecraft.getInstance().options.keyUse.setDown(false));
        for (String[] set : new String[][] {{"great_key", "tower_shield"}, {"rust_greatsword", "round_shield"}, {"scholar_staff", "soul_shard"},
                {"battle_axe", "plank_shield"}, {"short_bow", "prayer_beads"}}) {
            command("item replace entity @p weapon.mainhand with vsratie_dushi:" + set[0], "item replace entity @p weapon.offhand with vsratie_dushi:" + set[1]);
            camera(CameraType.THIRD_PERSON_FRONT);
            shot("31_held_" + set[0] + "_front", 20);
            camera(CameraType.FIRST_PERSON);
            shot("32_held_" + set[0] + "_first_person", 10);
        }
        command("item replace entity @p weapon.offhand with vsratie_dushi:tower_shield");
        client(() -> Minecraft.getInstance().options.keyUse.setDown(true));
        shot("32_tower_shield_blocking_first_person", 20);
        camera(CameraType.THIRD_PERSON_FRONT);
        shot("32_tower_shield_blocking_front");
        client(() -> Minecraft.getInstance().options.keyUse.setDown(false));

        // 8. the roll, by the real key: how far it carries, and that the player stands up again afterwards
        camera(CameraType.THIRD_PERSON_BACK);
        command(look(0.5, 0.5, 0, 0));
        pause(20);
        double[] start = new double[3];
        client(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            start[0] = player.getX();
            start[1] = player.getZ();
            start[2] = PlayerSoul.of(player).stamina;
            Minecraft.getInstance().options.keyUp.setDown(true);
        });
        pause(2);
        client(() -> KeyMapping.click(DushiClient.ROLL_KEY.getKey()));
        pause(3);
        client(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            say("roll, 4 ticks in: pose=" + player.getPose() + " rollTicks=" + PlayerSoul.of(player).rollTicks);
            Minecraft.getInstance().options.keyUp.setDown(false);
        });
        shot("35_roll", 1);
        pause(30);
        client(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            PlayerSoul soul = PlayerSoul.of(player);
            say("roll: moved " + String.format("%.2f", Math.hypot(player.getX() - start[0], player.getZ() - start[1])) + " blocks, pose=" + player.getPose()
                    + " forced=" + player.getForcedPose() + " rollTicks=" + soul.rollTicks + " stamina " + start[2] + " -> " + soul.stamina);
        });
        player(player -> say("roll, server side: pose=" + player.getPose() + " forced=" + player.getForcedPose()
                + " rollTicks=" + PlayerSoul.of(player).rollTicks));

        // 9. the screens
        camera(CameraType.FIRST_PERSON);
        client(() -> Minecraft.getInstance().gui.setScreen(new HearthScreen()));
        shot("33_hearth_screen");
        client(() -> Minecraft.getInstance().gui.setScreen(new ShopScreen()));
        shot("34_shop_screen");
        client(() -> Minecraft.getInstance().gui.setScreen(null));
        pause(5);

        // 10. dying: the title, the souls left behind as a stain, and getting them back
        command(look(0.5, 0.5, 0, 10));
        player(player -> {
            PlayerSoul.of(player).souls = 777;
            player.syncData(Dushi.PLAYER_SOUL);
        });
        command("kill @p");
        shot("36_death_screen", 50);
        client(() -> {
            Minecraft.getInstance().player.respawn();
            Minecraft.getInstance().gui.setScreen(null);
        });
        pause(40);
        command("gamemode survival @p", look(0.5, -3.5, 0, 15));
        shot("37_soul_stain", 30);
        client(() -> {
            PlayerSoul soul = PlayerSoul.of(Minecraft.getInstance().player);
            LocalPlayer player = Minecraft.getInstance().player;
            say("after death: souls=" + soul.souls + " lost=" + soul.lostSouls + " main hand=" + player.getMainHandItem().getItem()
                    + " off hand=" + player.getOffhandItem().getItem() + " flask=" + player.getInventory().getItem(1).getItem());
        });
        command(look(0.5, 0.5, 0, 15));
        pause(30);
        client(() -> {
            PlayerSoul soul = PlayerSoul.of(Minecraft.getInstance().player);
            say("after walking into the stain: souls=" + soul.souls + " lost=" + soul.lostSouls);
        });
        shot("38_souls_back", 5);
    }

    private static void camera(CameraType type) {
        client(() -> Minecraft.getInstance().options.setCameraType(type));
    }
}
