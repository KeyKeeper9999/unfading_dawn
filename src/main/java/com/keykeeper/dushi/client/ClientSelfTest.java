package com.keykeeper.dushi.client;

import java.util.ArrayList;
import java.util.List;

import com.keykeeper.dushi.ArmorSet;
import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.Gear;
import com.keykeeper.dushi.PlayerSoul;
import com.keykeeper.dushi.block.MobMarkBlockEntity;
import com.keykeeper.dushi.entity.CarrionHoundEntity;
import com.keykeeper.dushi.entity.ChaplainEntity;
import com.keykeeper.dushi.entity.GravediggerEntity;
import com.keykeeper.dushi.entity.OverseerEntity;
import com.keykeeper.dushi.entity.RustKnightEntity;
import com.keykeeper.dushi.entity.SoulsMob;
import com.keykeeper.dushi.entity.WitheredEntity;
import com.keykeeper.dushi.world.Plan;
import com.keykeeper.dushi.world.PlanPiece;

import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Only runs in the automated build (when the DUSHI_CLIENTTEST environment variable is set): starts a world by itself,
 * lines the mobs, blocks and items up in front of the camera, takes screenshots and quits. The pictures are how the look
 * of the mod is checked without anybody at the keyboard.
 */
@EventBusSubscriber(modid = Dushi.MODID, value = Dist.CLIENT)
public final class ClientSelfTest {
    private static final boolean ENABLED = System.getenv("DUSHI_CLIENTTEST") != null;
    /** The stage is a stone floor built high in the air of an ordinary world; things stand on it at this height. */
    private static final int GROUND = 180;
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
                // Wait for the title screen, then make an ordinary creative world (the buildings of the mod must be found in it).
                // (On a first start the game shows its accessibility screen instead of the title; either will do.)
                if (screen != null && minecraft.isGameLoadFinished() && minecraft.gui.overlay() == null && waited > 40) {
                    say("menu reached (" + screen.getClass().getSimpleName() + "), creating a world");
                    minecraft.options.pauseOnLostFocus = false;
                    minecraft.options.guiScale().set(2);
                    minecraft.options.tutorialStep = TutorialSteps.NONE;
                    minecraft.getTutorial().setStep(TutorialSteps.NONE);
                    minecraft.options.renderDistance().set(8);
                    minecraft.options.simulationDistance().set(6);
                    minecraft.getWindow().setWindowed(1280, 720);
                    LevelSettings settings = new LevelSettings("dushi_test", GameType.CREATIVE,
                            new LevelSettings.DifficultySettings(Difficulty.NORMAL, false, false), true, WorldDataConfiguration.DEFAULT);
                    minecraft.createWorldOpenFlows().createFreshLevel("dushi_test", settings, new WorldOptions(20261004L, true, false),
                            registries -> registries.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.NORMAL).value().createWorldDimensions(),
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
        command("gamerule advance_time false", "forceload add -32 -16 32 40");
        pause(100);
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
        attack(ChaplainEntity.class, ChaplainEntity.SPEARS, 20);
        shot("21_chaplain_flames_gather_night");
        attack(ChaplainEntity.class, ChaplainEntity.SPEARS, ChaplainEntity.FLAMES_GATHERED + 14);
        shot("21_chaplain_flames_throw_night");
        rest();
        command("time set 6000");

        // 4a. the village: townsman, butcher, watchman and a hound; then the gravedigger
        command(gone, look(0.5, 0.5, 0, 5), summon("withered_townsman", -3.5, 4.5, held("pitchfork", null)), summon("withered_butcher", -1.0, 4.5, held("cleaver", null)),
                summon("withered_watchman", 1.5, 4.5, ",equipment:{mainhand:{id:\"vsratie_dushi:pitchfork\"},offhand:{id:\"minecraft:lantern\"}}"),
                summon("carrion_hound", 4.0, 4.0, ""));
        shot("v1_village", 30);
        attack(WitheredEntity.class, WitheredEntity.CLEAVE, 12);
        shot("v2_village_cleave_draw");
        attack(WitheredEntity.class, WitheredEntity.CLEAVE, 17);
        shot("v2_village_cleave_cut");
        attack(WitheredEntity.class, WitheredEntity.THRUST, 11);
        shot("v3_village_thrust_ready");
        attack(WitheredEntity.class, WitheredEntity.THRUST, 15);
        shot("v3_village_thrust");
        attack(WitheredEntity.class, WitheredEntity.ALARM, 14);
        attack(CarrionHoundEntity.class, CarrionHoundEntity.POUNCE, 13);
        shot("v4_village_alarm_pounce");
        command(look(-6.0, 6.5, -110, 5));
        attack(WitheredEntity.class, WitheredEntity.THRUST, 11);
        shot("v5_village_side_thrust_ready");
        attack(WitheredEntity.class, WitheredEntity.THRUST, 15);
        attack(CarrionHoundEntity.class, CarrionHoundEntity.BITE, 7);
        shot("v5_village_side_thrust");
        attack(WitheredEntity.class, WitheredEntity.CLEAVE, 12);
        shot("v5_village_side_cleave_draw");
        attack(WitheredEntity.class, WitheredEntity.CLEAVE, 17);
        shot("v5_village_side_cleave_cut");
        rest();
        command(look(0.5, 9.0, 180, 5));
        shot("v6_village_back");
        command(gone, look(0.5, 0.5, 0, -10), summon("gravedigger", 0.5, 6.0, ""), "time set 18000");
        shot("v7_gravedigger_night", 30);
        command("time set 6000");
        shot("v7_gravedigger", 20);
        attack(GravediggerEntity.class, GravediggerEntity.SWEEP, 15);
        shot("v8_gravedigger_sweep");
        attack(GravediggerEntity.class, GravediggerEntity.SLAM, 13);
        shot("v8_gravedigger_slam_raise");
        attack(GravediggerEntity.class, GravediggerEntity.SLAM, 21);
        shot("v8_gravedigger_slam_strike");
        attack(GravediggerEntity.class, GravediggerEntity.DIRT, 18);
        shot("v9_gravedigger_dirt");
        command(look(-6.0, 8.0, -110, -6));
        attack(GravediggerEntity.class, GravediggerEntity.RUSH, 20);
        shot("v9_gravedigger_rush_side");
        attack(GravediggerEntity.class, GravediggerEntity.RAISE, 24);
        shot("v9_gravedigger_raise_side");
        rest();
        command(look(0.5, 11.0, 180, -10));
        shot("v9_gravedigger_back");
        command("kill @e[type=vsratie_dushi:withered_townsman]");

        // 4b. the quarry: digger, barrowman, sapper - together, then each alone from the side; the crawler; the unhewn
        command(gone, look(0.5, 0.5, 0, 5), summon("withered_digger", -3.0, 4.5, held("quarry_pick", null)), summon("withered_barrowman", 0.0, 5.5, ""),
                summon("withered_sapper", 3.0, 4.5, held("powder_pot", null)));
        shot("q1_quarry_three", 30);
        command(look(0.5, 9.5, 180, 5));
        shot("q1_quarry_back");
        command(gone, look(-4.5, 4.5, -90, 5), summon("withered_digger", 0.0, 4.5, held("quarry_pick", null)));
        shot("q2_digger_side", 20);
        attack(WitheredEntity.class, WitheredEntity.PICK, 14);
        shot("q2_digger_pick_raised");
        attack(WitheredEntity.class, WitheredEntity.PICK, 19);
        shot("q2_digger_pick_struck");
        attack(WitheredEntity.class, WitheredEntity.PICK_STUCK, 21);
        shot("q2_digger_pick_stuck");
        attack(WitheredEntity.class, WitheredEntity.PICK_STUCK, 30);
        shot("q2_digger_pick_tug");
        attack(WitheredEntity.class, WitheredEntity.PICK_STUCK, 51);
        shot("q2_digger_pick_free");
        command(look(3.0, 1.5, 30, 8));
        attack(WitheredEntity.class, WitheredEntity.PICK_STUCK, 21);
        shot("q2_digger_pick_stuck_front_quarter");
        rest();
        command(gone, look(-5.0, 4.5, -90, 5), summon("withered_barrowman", 0.0, 4.5, ""));
        shot("q2_barrow_side", 20);
        attack(WitheredEntity.class, WitheredEntity.CHARGE, 10);
        shot("q2_barrow_set");
        attack(WitheredEntity.class, WitheredEntity.CHARGE, 25);
        shot("q2_barrow_run");
        attack(WitheredEntity.class, WitheredEntity.CHARGE, 42);
        shot("q2_barrow_fall");
        attack(WitheredEntity.class, WitheredEntity.RAM, 7);
        shot("q2_barrow_ram_draw");
        attack(WitheredEntity.class, WitheredEntity.RAM, 12);
        shot("q2_barrow_ram");
        rest();
        command(look(2.5, 1.5, 25, 10));
        shot("q2_barrow_front_quarter");
        command(gone, look(-4.5, 4.5, -90, 5), summon("withered_sapper", 0.0, 4.5, held("powder_pot", null)));
        shot("q2_sapper_side", 20);
        attack(WitheredEntity.class, WitheredEntity.LOB, 14);
        shot("q2_sapper_lighting");
        attack(WitheredEntity.class, WitheredEntity.LOB, 23);
        shot("q2_sapper_back");
        attack(WitheredEntity.class, WitheredEntity.LOB, 28);
        shot("q2_sapper_thrown");
        attack(WitheredEntity.class, WitheredEntity.FUSE, 9);
        shot("q2_sapper_fuse_reach");
        attack(WitheredEntity.class, WitheredEntity.FUSE, 30);
        shot("q2_sapper_fuse_run");
        rest();
        command(gone, look(0.5, 0.5, 0, 12), summon("blind_crawler", 0.5, 3.5, ""));
        shot("q3_crawler", 20);
        attack(com.keykeeper.dushi.entity.BlindCrawlerEntity.class, com.keykeeper.dushi.entity.BlindCrawlerEntity.SHRIEK, 10);
        shot("q3_crawler_shriek");
        attack(com.keykeeper.dushi.entity.BlindCrawlerEntity.class, com.keykeeper.dushi.entity.BlindCrawlerEntity.FLURRY, 8);
        shot("q3_crawler_rake_right");
        attack(com.keykeeper.dushi.entity.BlindCrawlerEntity.class, com.keykeeper.dushi.entity.BlindCrawlerEntity.FLURRY, 19);
        shot("q3_crawler_rake_both");
        command(look(-3.5, 3.5, -90, 10));
        rest();
        shot("q3_crawler_side");
        attack(com.keykeeper.dushi.entity.BlindCrawlerEntity.class, com.keykeeper.dushi.entity.BlindCrawlerEntity.SHRIEK, 10);
        shot("q3_crawler_side_shriek");
        attack(com.keykeeper.dushi.entity.BlindCrawlerEntity.class, com.keykeeper.dushi.entity.BlindCrawlerEntity.LEAP, 9);
        shot("q3_crawler_side_crouch");
        attack(com.keykeeper.dushi.entity.BlindCrawlerEntity.class, com.keykeeper.dushi.entity.BlindCrawlerEntity.LEAP, 14);
        shot("q3_crawler_side_leap");
        rest();
        command(gone, look(0.5, 0.5, 0, 0), summon("unhewn", -1.5, 5.0, ""), summon("unhewn", 2.5, 5.0, ",Phase:2"));
        shot("q3_statues", 30);
        attack(com.keykeeper.dushi.entity.UnhewnEntity.class, com.keykeeper.dushi.entity.UnhewnEntity.SMASH, 20);
        shot("q3_smash_raised");
        attack(com.keykeeper.dushi.entity.UnhewnEntity.class, com.keykeeper.dushi.entity.UnhewnEntity.SMASH, 27);
        shot("q3_smash_struck");
        attack(com.keykeeper.dushi.entity.UnhewnEntity.class, com.keykeeper.dushi.entity.UnhewnEntity.SHOVE, 17);
        shot("q3_shove");
        command(look(8.0, 5.0, 90, 0));
        shot("q3_side_shove");
        attack(com.keykeeper.dushi.entity.UnhewnEntity.class, com.keykeeper.dushi.entity.UnhewnEntity.SMASH, 27);
        shot("q3_side_smash_struck");
        rest();
        command(gone, look(0.5, 0.5, 0, -8), summon("taskmaster", 0.5, 6.0, ""), "time set 18000");
        shot("q4_taskmaster_night", 30);
        command("time set 6000");
        shot("q4_taskmaster", 20);
        attack(com.keykeeper.dushi.entity.TaskmasterEntity.class, com.keykeeper.dushi.entity.TaskmasterEntity.SLAM, 17);
        shot("q4_slam_raised");
        attack(com.keykeeper.dushi.entity.TaskmasterEntity.class, com.keykeeper.dushi.entity.TaskmasterEntity.SLAM, 24);
        shot("q4_slam_struck");
        attack(com.keykeeper.dushi.entity.TaskmasterEntity.class, com.keykeeper.dushi.entity.TaskmasterEntity.SWEEP, 15);
        shot("q4_sweep_drawn");
        attack(com.keykeeper.dushi.entity.TaskmasterEntity.class, com.keykeeper.dushi.entity.TaskmasterEntity.SWEEP, 21);
        shot("q4_sweep_cut");
        command(look(-6.5, 7.0, -100, -4));
        attack(com.keykeeper.dushi.entity.TaskmasterEntity.class, com.keykeeper.dushi.entity.TaskmasterEntity.SLAM, 17);
        shot("q5_side_slam_raised");
        attack(com.keykeeper.dushi.entity.TaskmasterEntity.class, com.keykeeper.dushi.entity.TaskmasterEntity.SLAM, 24);
        shot("q5_side_slam_struck");
        attack(com.keykeeper.dushi.entity.TaskmasterEntity.class, com.keykeeper.dushi.entity.TaskmasterEntity.WEDGE, 25);
        shot("q5_side_wedge_raised");
        attack(com.keykeeper.dushi.entity.TaskmasterEntity.class, com.keykeeper.dushi.entity.TaskmasterEntity.WEDGE, 32);
        shot("q5_side_wedge_struck");
        attack(com.keykeeper.dushi.entity.TaskmasterEntity.class, com.keykeeper.dushi.entity.TaskmasterEntity.THROW, 14);
        shot("q5_side_throw");
        attack(com.keykeeper.dushi.entity.TaskmasterEntity.class, com.keykeeper.dushi.entity.TaskmasterEntity.CAVE_IN, 40);
        shot("q5_side_cave_in_call");
        rest();
        command(look(0.5, 11.0, 180, -8));
        shot("q5_taskmaster_back");
        command(gone, look(0.5, 0.5, 0, 0), summon("stone_eater", 0.5, 6.0, ""), "time set 18000");
        shot("q6_stone_eater_night", 30);
        command("time set 6000");
        shot("q6_stone_eater", 20);
        attack(com.keykeeper.dushi.entity.StoneEaterEntity.class, com.keykeeper.dushi.entity.StoneEaterEntity.CLAW, 10);
        shot("q6_claw_raised");
        attack(com.keykeeper.dushi.entity.StoneEaterEntity.class, com.keykeeper.dushi.entity.StoneEaterEntity.CLAW, 15);
        shot("q6_claw_cut");
        attack(com.keykeeper.dushi.entity.StoneEaterEntity.class, com.keykeeper.dushi.entity.StoneEaterEntity.ROAR, 22);
        shot("q6_roar");
        command(look(-9.0, 6.5, -92, 0));
        shot("q7_side_roar");
        attack(com.keykeeper.dushi.entity.StoneEaterEntity.class, com.keykeeper.dushi.entity.StoneEaterEntity.LUNGE, 20);
        shot("q7_side_lunge");
        attack(com.keykeeper.dushi.entity.StoneEaterEntity.class, com.keykeeper.dushi.entity.StoneEaterEntity.BURROW, 12);
        shot("q7_side_burrow_digging");
        attack(com.keykeeper.dushi.entity.StoneEaterEntity.class, com.keykeeper.dushi.entity.StoneEaterEntity.BURROW, 40);
        shot("q7_side_burrow_under");
        attack(com.keykeeper.dushi.entity.StoneEaterEntity.class, com.keykeeper.dushi.entity.StoneEaterEntity.BURROW, 80);
        shot("q7_side_burrow_erupt");
        attack(com.keykeeper.dushi.entity.StoneEaterEntity.class, com.keykeeper.dushi.entity.StoneEaterEntity.TAIL, 12);
        shot("q7_side_tail_drawn");
        command(look(7.5, 11.5, 125, 5));
        attack(com.keykeeper.dushi.entity.StoneEaterEntity.class, com.keykeeper.dushi.entity.StoneEaterEntity.TAIL, 12);
        shot("q7_back_tail_drawn");
        attack(com.keykeeper.dushi.entity.StoneEaterEntity.class, com.keykeeper.dushi.entity.StoneEaterEntity.TAIL, 17);
        shot("q7_back_tail_lash");
        rest();
        command(gone);

        // 5. every item of the mod as a picture in the air
        List<String> items = new ArrayList<>();
        Dushi.ITEMS.getEntries().forEach(entry -> items.add(entry.getId().toString()));
        List<String> displays = new ArrayList<>();
        int columns = 13;
        for (int i = 0; i < items.size(); i++) {
            double x = 0.5 + 4.8 - (i % columns) * 0.8;
            double y = GROUND + 5.4 - (i / columns) * 0.8;
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

        // 7b. the four suits of armour on the player, and what the tooltips say about gear the mod did not make
        client(() -> Minecraft.getInstance().options.keyUse.setDown(false));
        for (ArmorSet set : ArmorSet.values()) {
            command("item replace entity @p weapon.mainhand with minecraft:air", "item replace entity @p weapon.offhand with minecraft:air",
                    "item replace entity @p armor.head with vsratie_dushi:" + set.names[0], "item replace entity @p armor.chest with vsratie_dushi:" + set.names[1],
                    "item replace entity @p armor.legs with vsratie_dushi:" + set.names[2], "item replace entity @p armor.feet with vsratie_dushi:" + set.names[3],
                    look(0.5, 4.5, 0, 8));
            camera(CameraType.THIRD_PERSON_FRONT);
            shot("31_armour_" + set.id + "_front", 20);
            camera(CameraType.THIRD_PERSON_BACK);
            shot("31_armour_" + set.id + "_back", 10);
        }
        client(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            for (ItemStack stack : new ItemStack[] {new ItemStack(Items.IRON_SWORD), new ItemStack(Items.DIAMOND_AXE), new ItemStack(Items.BOW),
                    new ItemStack(Items.SHIELD), new ItemStack(Items.IRON_CHESTPLATE), new ItemStack(ArmorSet.GUARD.piece(1)),
                    new ItemStack(ArmorSet.CHAPLAIN.piece(0)), new ItemStack(Dushi.FLASK_SHARD.get()), new ItemStack(Dushi.LONGSWORD.get())}) {
                StringBuilder text = new StringBuilder();
                for (Component line : stack.getTooltipLines(Item.TooltipContext.EMPTY, player, TooltipFlag.NORMAL)) {
                    text.append(" | ").append(line.getString());
                }
                say("tooltip" + text);
            }
            say("burden in " + ArmorSet.CHAPLAIN.id + ": " + Gear.load(player) + "/" + Gear.maxLoad(PlayerSoul.of(player)) + " " + Gear.burden(player).id);
        });
        command("item replace entity @p armor.head with minecraft:netherite_helmet", "item replace entity @p armor.chest with minecraft:netherite_chestplate",
                "item replace entity @p armor.legs with minecraft:netherite_leggings", "item replace entity @p armor.feet with minecraft:netherite_boots",
                "item replace entity @p weapon.mainhand with vsratie_dushi:great_key", "item replace entity @p weapon.offhand with vsratie_dushi:tower_shield");
        pause(10);
        client(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            say("burden in netherite with the great key and the tower shield: " + Gear.load(player) + "/" + Gear.maxLoad(PlayerSoul.of(player)) + " "
                    + Gear.burden(player).id);
        });
        command("item replace entity @p armor.head with vsratie_dushi:guard_hat", "item replace entity @p armor.chest with vsratie_dushi:guard_hauberk",
                "item replace entity @p armor.legs with vsratie_dushi:guard_leggings", "item replace entity @p armor.feet with vsratie_dushi:guard_boots",
                "item replace entity @p weapon.mainhand with vsratie_dushi:longsword", "item replace entity @p weapon.offhand with vsratie_dushi:guard_shield");

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

        // 11. the buildings, found in the world as it was generated
        command("gamemode creative @p", "time set 6000");
        building("outpost", 40);
        view("40_outpost_front", 6, 6, 26, 6, 4, 8);
        view("41_outpost_hearth", 12, 5, -6, 6, 1, 3);
        view("42_outpost_inside", 6, 2, 10, 5, 2, 6);
        building("stockade", 43);
        view("43_stockade_from_south", 36, 24, 80, 22, 2, 30);
        view("44_stockade_gate", 23, 9, 70, 22, 3, 56);
        view("45_stockade_keeper_room", 16.5, 2.6, 51.5, 10, 1.5, 53.5);
        view("46_stockade_yard", 22.5, 4, 48, 22, 3, 20);
        view("47_stockade_cells", 10.5, 2.6, 42, 8, 2, 24);
        view("48_stockade_roof", 12, 8, 43, 6, 6, 22);
        view("49_stockade_hall", 22.5, 3, 14.5, 22, 4, 4);
        view("50_stockade_chapel", 36.5, 3, 44, 36.5, 4, 24);
        view("51_stockade_from_above", 22, 60, 60, 22, 0, 26);
        player(player -> {
            int marks = MobMarkBlockEntity.resetAround((ServerLevel) player.level(), player.blockPosition(), 96);
            say("rest near the stockade: " + marks + " marks reset");
        });
    }

    /** The piece of the building the views are taken of, or null if none was found. */
    private static PlanPiece piece;

    /** Finds the nearest building of the kind in the world and stands the player next to it. */
    private static void building(String name, int number) {
        server(level -> {
            piece = null;
            ServerPlayer player = level.players().get(0);
            Holder<Structure> structure = level.registryAccess().lookupOrThrow(Registries.STRUCTURE)
                    .getOrThrow(ResourceKey.create(Registries.STRUCTURE, Dushi.id(name)));
            var nearest = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(structure), BlockPos.ZERO, 100, false);
            if (nearest == null) {
                say(name + ": none within 100 chunks");
                return;
            }
            BlockPos at = nearest.getFirst();
            StructureStart start = level.structureManager().getStartForStructure(structure.value(), level.getChunk(at.getX() >> 4, at.getZ() >> 4));
            if (start == null || !start.isValid()) {
                say(name + ": located at " + at.toShortString() + " but there is no start");
                return;
            }
            piece = (PlanPiece) start.getPieces().get(0);
            say(name + ": found at " + piece.origin().toShortString() + " turned " + piece.rotation().getSerializedName());
            player.connection.teleport(piece.origin().getX() + 0.5, piece.origin().getY() + 30.0, piece.origin().getZ() + 0.5, 0.0F, 60.0F);
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        });
        // the land round it has to be made and drawn before the first picture; drawing is slow here
        pause(500);
    }

    /** A picture taken from one cell of the building's plan, looking at another. */
    private static void view(String name, double fromX, double fromY, double fromZ, double toX, double toY, double toZ) {
        server(level -> {
            if (piece == null) {
                return;
            }
            Plan plan = Plan.get(piece.planName());
            Vec3 from = turned(plan, fromX, fromY, fromZ);
            Vec3 to = turned(plan, toX, toY, toZ);
            double dx = to.x - from.x;
            double dy = to.y - from.y;
            double dz = to.z - from.z;
            float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
            ServerPlayer player = level.players().get(0);
            // the eyes are 1.62 above the feet
            player.connection.teleport(from.x, from.y - 1.62, from.z, yaw, pitch);
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        });
        shot(name, 50);
    }

    /** A point given in the plan's own coordinates (y = 1 is where feet stand), where it is in the world. */
    private static Vec3 turned(Plan plan, double x, double y, double z) {
        BlockPos corner = PlanPiece.toWorld(plan, piece.origin(), piece.rotation(), 0, plan.originY, 0);
        BlockPos along = PlanPiece.toWorld(plan, piece.origin(), piece.rotation(), 1, plan.originY, 0);
        BlockPos across = PlanPiece.toWorld(plan, piece.origin(), piece.rotation(), 0, plan.originY, 1);
        double wx = corner.getX() + 0.5 + (along.getX() - corner.getX()) * x + (across.getX() - corner.getX()) * z;
        double wz = corner.getZ() + 0.5 + (along.getZ() - corner.getZ()) * x + (across.getZ() - corner.getZ()) * z;
        return new Vec3(wx, corner.getY() + y - 1.0, wz);
    }

    private static void camera(CameraType type) {
        client(() -> Minecraft.getInstance().options.setCameraType(type));
    }
}
