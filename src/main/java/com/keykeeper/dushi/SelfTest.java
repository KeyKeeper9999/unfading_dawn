package com.keykeeper.dushi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.keykeeper.dushi.block.HearthBlock;
import com.keykeeper.dushi.entity.SoulStainEntity;
import com.keykeeper.dushi.entity.SoulsMob;
import com.keykeeper.dushi.item.FlaskItem;
import com.keykeeper.dushi.item.SpellItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Only runs in the automated build (when the DUSHI_SELFTEST environment variable is set). Builds a stone floor in the sky,
 * puts every mob of the mod on it with iron golems to fight, drives a stand-in player through the items and the hearth,
 * and prints what happened, so that a crash or a mechanic that does nothing shows up in the build log. Then stops the server.
 */
@EventBusSubscriber(modid = Dushi.MODID)
public final class SelfTest {
    private static final boolean ENABLED = System.getenv("DUSHI_SELFTEST") != null;
    private static final int FLOOR_Y = 200;
    /** Half the side of the floor the mobs are tried on. */
    private static final int HALF = 40;
    private static final int RUN_TICKS = 500;

    private static final Map<String, Mob> MOBS = new LinkedHashMap<>();
    /** For every mob, how many times each of its moves was started. */
    private static final Map<String, Map<Integer, Integer>> MOVES = new LinkedHashMap<>();
    private static final Map<String, Integer> LAST_MOVE = new LinkedHashMap<>();
    private static final int DRILL_TICKS = 780;
    private static final List<Drill> DRILL = new ArrayList<>();
    private static int ticks = -1;

    private SelfTest() {}

    private static void say(String text) {
        System.out.println("DUSHI SELFTEST " + text);
    }

    @SubscribeEvent
    static void onStarted(ServerStartedEvent event) {
        if (!ENABLED) {
            return;
        }
        try {
            start(event.getServer().overworld());
        } catch (RuntimeException e) {
            say("FAILED in start: " + e);
            e.printStackTrace(System.out);
        }
        ticks = 0;
    }

    private static void start(ServerLevel level) {
        for (int cx = -3; cx <= 2; cx++) {
            for (int cz = -3; cz <= 2; cz++) {
                level.setChunkForced(cx, cz, true);
            }
        }
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(-HALF, FLOOR_Y, -HALF), new BlockPos(HALF, FLOOR_Y, HALF))) {
            level.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
        }
        // A wall around the floor so nobody walks off.
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(-HALF - 1, FLOOR_Y, -HALF - 1), new BlockPos(HALF + 1, FLOOR_Y + 5, HALF + 1))) {
            if (Math.abs(pos.getX()) == HALF + 1 || Math.abs(pos.getZ()) == HALF + 1) {
                level.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
            }
        }

        // Every mob in its own corner, with golems for company: golems attack monsters, monsters hit back.
        List<EntityType<? extends Mob>> types = new ArrayList<>();
        Dushi.WITHERED.values().forEach(type -> types.add(type.get()));
        types.add(Dushi.RUST_KNIGHT.get());
        types.add(Dushi.CARRION_HOUND.get());
        types.add(Dushi.GRAVEDIGGER.get());
        types.add(Dushi.BLIND_CRAWLER.get());
        types.add(Dushi.UNHEWN.get());
        types.add(Dushi.TASKMASTER.get());
        types.add(Dushi.STONE_EATER.get());
        types.add(Dushi.BONE_PIKE.get());
        types.add(Dushi.LANKY.get());
        types.add(Dushi.WISP.get());
        types.add(Dushi.WALLOWER.get());
        types.add(Dushi.WEEPING_MOTHER.get());
        types.add(Dushi.WAKEFUL.get());
        types.add(Dushi.WAKER.get());
        types.add(Dushi.SLEEPER.get());
        types.add(Dushi.WAXEN.get());
        types.add(Dushi.SLEEP_MOTH.get());
        types.add(Dushi.SHATUN.get());
        types.add(Dushi.ELDER.get());
        types.add(Dushi.DOCKHAND.get());
        types.add(Dushi.HARPOONER.get());
        types.add(Dushi.OARSMAN.get());
        types.add(Dushi.PILOT.get());
        types.add(Dushi.SAND_CRAB.get());
        types.add(Dushi.SKUA.get());
        types.add(Dushi.HELMSMAN.get());
        types.add(Dushi.CAPSIZED.get());
        types.add(Dushi.REAPER.get());
        types.add(Dushi.MOWER.get());
        types.add(Dushi.SACKMAN.get());
        types.add(Dushi.SCORCHED.get());
        types.add(Dushi.SCARECROW.get());
        types.add(Dushi.STOOK.get());
        types.add(Dushi.SUNGAZER.get());
        types.add(Dushi.SCORCHED_WARDEN.get());
        types.add(Dushi.HOLLOW_MILLER.get());
        types.add(Dushi.KEYKEEPER.get());
        types.add(Dushi.OVERSEER.get());
        types.add(Dushi.CHAPLAIN.get());
        int index = 0;
        for (EntityType<? extends Mob> type : types) {
            int x = -30 + (index % 7) * 10;
            int z = -30 + (index / 7) * 12;
            BlockPos at = new BlockPos(x, FLOOR_Y + 1, z);
            Mob mob = type.spawn(level, at, EntitySpawnReason.COMMAND);
            String name = EntityType.getKey(type).getPath();
            if (mob == null) {
                say("FAILED to spawn " + name);
            } else {
                mob.setPersistenceRequired();
                MOBS.put(name, mob);
                MOVES.put(name, new TreeMap<>());
                int golems = mob instanceof SoulsMob souls && souls.isBoss() ? 3 : 1;
                for (int g = 0; g < golems; g++) {
                    EntityTypes.IRON_GOLEM.spawn(level, at.offset(3 + g, 0, 2), EntitySpawnReason.COMMAND);
                }
            }
            index++;
        }
        say("spawned " + MOBS.size() + " of " + types.size() + " mobs: " + MOBS.keySet());
        say("spawn eggs: " + Dushi.SPAWN_EGGS.size() + ", weapons: " + Dushi.WEAPONS.size() + ", shields: " + Dushi.SHIELDS.size()
                + ", spells: " + Dushi.SPELLS.size() + ", sounds: " + Dushi.SOUND_NAMES.length);

        player(level);
    }

    /** A stand-in player: souls, levelling, the flask, every spell, the hearth and death. */
    private static void player(ServerLevel level) {
        FakePlayer player = FakePlayerFactory.getMinecraft(level);
        player.setPos(0.5, FLOOR_Y + 1, 0.5);
        PlayerSoul soul = PlayerSoul.of(player);
        say("fresh soul: level=" + soul.level() + " cost=" + soul.levelCost() + " stamina=" + soul.stamina + "/" + soul.maxStamina());

        SoulMechanics.giveSouls(player, 5000);
        int before = soul.souls;
        BlockPos hearth = new BlockPos(2, FLOOR_Y + 1, 0);
        level.setBlock(hearth, Dushi.HEARTH.get().defaultBlockState(), 3);
        say("hearth placed lit=" + level.getBlockState(hearth).getValue(HearthBlock.LIT));

        // The flask: drink twice, then rest at the hearth and see it full again.
        ItemStack flask = new ItemStack(Dushi.WARMTH_FLASK.get());
        player.getInventory().setItem(5, flask);
        ItemStack held = player.getInventory().getItem(5);
        player.setHealth(4.0F);
        held.finishUsingItem(level, player);
        float healed = player.getHealth();
        held.finishUsingItem(level, player);
        int afterDrinking = FlaskItem.charges(held);

        // Every spell with its catalyst in the other hand.
        StringBuilder spells = new StringBuilder();
        for (Map.Entry<Spell, net.neoforged.neoforge.registries.DeferredItem<SpellItem>> entry : Dushi.SPELLS.entrySet()) {
            Spell spell = entry.getKey();
            ItemStack scroll = new ItemStack(entry.getValue().get());
            ItemStack catalyst = new ItemStack(switch (spell.school) {
                case SORCERY -> Dushi.SCHOLAR_STAFF.get();
                case MIRACLE -> Dushi.PRAYER_BEADS.get();
                case PYROMANCY -> Dushi.EMBER_GLOVE.get();
            });
            player.setItemInHand(InteractionHand.MAIN_HAND, scroll);
            player.setItemInHand(InteractionHand.OFF_HAND, catalyst);
            soul.stamina = soul.maxStamina();
            try {
                var result = scroll.use(level, player, InteractionHand.MAIN_HAND);
                if (spell.channel > 0) {
                    scroll.finishUsingItem(level, player);
                }
                spells.append(' ').append(spell.id).append('=').append(result.consumesAction() ? "ok" : "no").append('(')
                        .append(SpellItem.castsLeft(scroll)).append('/').append(spell.casts).append(')');
            } catch (RuntimeException e) {
                spells.append(' ').append(spell.id).append("=FAILED:").append(e);
                e.printStackTrace(System.out);
            }
        }
        say("spells:" + spells);
        // Without the catalyst a spell must refuse.
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        ItemStack lone = new ItemStack(Dushi.SPELLS.get(Spell.SOUL_SHARD).get());
        player.setItemInHand(InteractionHand.MAIN_HAND, lone);
        say("spell without catalyst refused=" + !lone.use(level, player, InteractionHand.MAIN_HAND).consumesAction());
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

        level.getBlockState(hearth).useWithoutItem(level, player, new BlockHitResult(Vec3.atCenterOf(hearth), Direction.UP, hearth, false));
        say("hearth: lit=" + level.getBlockState(hearth).getValue(HearthBlock.LIT) + " health " + healed + " -> " + player.getHealth() + "/" + player.getMaxHealth()
                + " flask " + afterDrinking + " -> " + FlaskItem.charges(held) + "/" + FlaskItem.capacity(held)
                + " respawn=" + (player.getRespawnConfig() != null));

        // Levelling next to the lit hearth: one point of everything.
        StringBuilder levels = new StringBuilder();
        for (int stat = 0; stat < PlayerSoul.STAT_COUNT; stat++) {
            int cost = soul.levelCost();
            int had = soul.souls;
            Net.LevelUp.apply(player, stat);
            levels.append(' ').append(PlayerSoul.STAT_KEYS[stat]).append('=').append(soul.stats[stat]).append("(-").append(had - soul.souls).append('/')
                    .append(cost).append(')');
        }
        say("level up:" + levels + " level=" + soul.level() + " souls " + before + " -> " + soul.souls + " maxHealth=" + player.getMaxHealth()
                + " maxStamina=" + soul.maxStamina());

        // The keykeeper's shop: no keykeeper near, no deal; with one, the price is paid and the thing arrives.
        boolean refused = !Net.Buy.apply(player, 0);
        var keeper = Dushi.KEYKEEPER.get().spawn(level, hearth.offset(2, 0, 0), EntitySpawnReason.COMMAND);
        int purse = soul.souls;
        boolean bought = Net.Buy.apply(player, 0);
        say("shop: " + Shop.wares().size() + " wares, refused without a keykeeper=" + refused + " bought=" + bought + " paid=" + (purse - soul.souls)
                + " has it=" + player.getInventory().contains(stack -> stack.is(Dushi.CLUB.get())));
        if (keeper != null) {
            keeper.discard();
        }

        // Stamina: spending, the pause, recovery.
        soul.stamina = soul.maxStamina();
        boolean enough = soul.spend(30.0F);
        float spent = soul.stamina;
        for (int i = 0; i < PlayerSoul.REGEN_PAUSE + 5; i++) {
            soul.tickStamina(false, false, 1.0F);
        }
        say("stamina: spend ok=" + enough + " " + soul.maxStamina() + " -> " + spent + " -> " + soul.stamina + " after the pause; exhausted spend ok="
                + new PlayerSoul().spend(500.0F));

        // Death leaves the souls in a stain; coming back picks them up.
        int carried = soul.souls;
        SoulMechanics.onPlayerDeath(level, player);
        List<SoulStainEntity> stains = level.getEntitiesOfClass(SoulStainEntity.class, new AABB(hearth).inflate(8.0));
        say("death: souls " + carried + " -> " + soul.souls + " lost=" + soul.lostSouls + " stains=" + stains.size());
    }

    @SubscribeEvent
    static void onTick(ServerTickEvent.Post event) {
        if (!ENABLED || ticks < 0) {
            return;
        }
        ticks++;
        ServerLevel level = event.getServer().overworld();
        try {
            for (Map.Entry<String, Mob> entry : MOBS.entrySet()) {
                if (entry.getValue() instanceof SoulsMob mob && mob.isAlive()) {
                    int move = mob.attackId();
                    Integer last = LAST_MOVE.put(entry.getKey(), move);
                    if (move != 0 && (last == null || last != move)) {
                        MOVES.get(entry.getKey()).merge(move, 1, Integer::sum);
                    }
                    // Half-way through, the bosses are brought below half health so that their second phase shows up too.
                    if (ticks == RUN_TICKS / 2 && mob.isBoss() && mob.getHealth() > mob.getMaxHealth() * 0.45F) {
                        mob.setHealth(mob.getMaxHealth() * 0.45F);
                    }
                }
            }
            if (ticks == RUN_TICKS) {
                report(level);
                startDrill(level);
            }
            if (ticks > RUN_TICKS) {
                drill();
            }
            if (ticks == RUN_TICKS + DRILL_TICKS) {
                reportDrill();
                try {
                    WorldTest.run(level);
                } catch (RuntimeException e) {
                    say("world: FAILED: " + e);
                    e.printStackTrace(System.out);
                }
                event.getServer().halt(false);
                ticks = -1;
            }
        } catch (RuntimeException e) {
            say("FAILED in tick " + ticks + ": " + e);
            e.printStackTrace(System.out);
            ticks = -1;
            event.getServer().halt(false);
        }
    }

    /**
     * The fight with the golems only shows the moves the mobs choose at arm's length. The drill makes fresh ones go through
     * every move they have, one after another, against a golem that just stands there.
     */
    private static void startDrill(ServerLevel level) {
        AABB arena = new AABB(-HALF - 2, FLOOR_Y - 2, -HALF - 2, HALF + 2, FLOOR_Y + 20, HALF + 2);
        for (Entity entity : level.getEntitiesOfClass(Entity.class, arena)) {
            entity.discard();
        }
        int index = 0;
        for (Map.Entry<EntityType<? extends SoulsMob>, int[]> entry : drillPlan().entrySet()) {
            // eight to a row: six rows are all the floor holds, and one set down past its edge falls and finishes nothing
            BlockPos at = new BlockPos(-35 + (index % 8) * 10, FLOOR_Y + 1, -34 + (index / 8) * 13);
            if (entry.getKey() == Dushi.BONE_PIKE.get()) {
                // a pike is drilled in water: on a dry floor all it does is flounder
                for (BlockPos pos : BlockPos.betweenClosed(at.offset(-2, -2, -2), at.offset(2, -1, 9))) {
                    boolean rim = Math.abs(pos.getX() - at.getX()) == 2 || pos.getZ() == at.getZ() - 2 || pos.getZ() == at.getZ() + 9 || pos.getY() == at.getY() - 2;
                    level.setBlock(pos, rim ? Blocks.STONE.defaultBlockState() : Blocks.WATER.defaultBlockState(), 3);
                }
                at = at.below();
            }
            SoulsMob mob = entry.getKey().spawn(level, at, EntitySpawnReason.COMMAND);
            Mob dummy = EntityTypes.IRON_GOLEM.spawn(level, at.offset(0, 0, entry.getKey() == Dushi.BONE_PIKE.get() ? 3 : 7), EntitySpawnReason.COMMAND);
            if (mob == null || dummy == null) {
                say("drill: FAILED to spawn " + EntityType.getKey(entry.getKey()).getPath());
                continue;
            }
            dummy.setNoAi(true);
            if (mob instanceof com.keykeeper.dushi.entity.UnhewnEntity statue) {
                statue.rouse();                                  // (it would stand through the whole drill otherwise)
            }
            if (mob instanceof com.keykeeper.dushi.entity.WakefulEntity kneeling) {
                kneeling.rouse(dummy);                           // (and he would kneel through it)
            }
            if (mob instanceof com.keykeeper.dushi.entity.SleepMothEntity moth) {
                moth.startle(dummy);
            }
            if (mob instanceof com.keykeeper.dushi.entity.SandCrabEntity crab) {
                crab.emerge(dummy);                              // (it would lie in the sand through it)
            }
            if (mob instanceof com.keykeeper.dushi.entity.SkuaEntity skua) {
                skua.startle(dummy);
            }
            if (mob instanceof com.keykeeper.dushi.entity.CapsizedEntity boat) {
                boat.rise(dummy);
            }
            if (mob instanceof com.keykeeper.dushi.entity.ScarecrowEntity scarecrow) {
                scarecrow.wake(dummy);                           // (it would hang on its stake through it)
            }
            if (mob instanceof com.keykeeper.dushi.entity.StookEntity stook) {
                stook.rise(dummy);
            }
            mob.setTarget(dummy);
            DRILL.add(new Drill(EntityType.getKey(entry.getKey()).getPath(), mob, dummy, entry.getValue()));
            index++;
        }
    }

    private static Map<EntityType<? extends SoulsMob>, int[]> drillPlan() {
        Map<EntityType<? extends SoulsMob>, int[]> plan = new LinkedHashMap<>();
        plan.put(Dushi.WITHERED.get(com.keykeeper.dushi.entity.WitheredEntity.Kind.ARCHER).get(), new int[] {1, 2});
        plan.put(Dushi.RUST_KNIGHT.get(), new int[] {1, 2, 3});
        plan.put(Dushi.OVERSEER.get(), new int[] {1, 2, 3, 4, 5});
        plan.put(Dushi.CHAPLAIN.get(), new int[] {1, 2, 3, 4, 5, 6});
        plan.put(Dushi.WITHERED.get(com.keykeeper.dushi.entity.WitheredEntity.Kind.BUTCHER).get(), new int[] {1, 4});
        plan.put(Dushi.WITHERED.get(com.keykeeper.dushi.entity.WitheredEntity.Kind.WATCHMAN).get(), new int[] {5, 6});
        plan.put(Dushi.CARRION_HOUND.get(), new int[] {1, 2});
        plan.put(Dushi.GRAVEDIGGER.get(), new int[] {1, 2, 3, 4, 5});
        // the quarry (the sapper's last move is his end: he is not alive after it)
        plan.put(Dushi.WITHERED.get(com.keykeeper.dushi.entity.WitheredEntity.Kind.DIGGER).get(), new int[] {7, 12, 1});
        plan.put(Dushi.WITHERED.get(com.keykeeper.dushi.entity.WitheredEntity.Kind.BARROWMAN).get(), new int[] {8, 11});
        plan.put(Dushi.WITHERED.get(com.keykeeper.dushi.entity.WitheredEntity.Kind.SAPPER).get(), new int[] {9, 10});
        plan.put(Dushi.BLIND_CRAWLER.get(), new int[] {3, 1, 2});
        plan.put(Dushi.UNHEWN.get(), new int[] {1, 2, 3});
        plan.put(Dushi.TASKMASTER.get(), new int[] {1, 2, 3, 4, 5});
        plan.put(Dushi.STONE_EATER.get(), new int[] {1, 2, 3, 4, 5});
        // the Mire (the Weeping Mother's whirl is hers only when she is hurt, but the move itself can be asked for)
        plan.put(Dushi.BONE_PIKE.get(), new int[] {1, 2, 3});
        plan.put(Dushi.LANKY.get(), new int[] {1, 2, 3});
        plan.put(Dushi.WISP.get(), new int[] {1});
        plan.put(Dushi.WALLOWER.get(), new int[] {1, 2, 3, 4, 5});
        plan.put(Dushi.WEEPING_MOTHER.get(), new int[] {1, 2, 3, 4, 5, 6});
        // the Black Firwood (the wakeful one's first move is his rising; the Elder falls asleep in his fifth, and the
        // moves after it are the ones he makes sleeping)
        plan.put(Dushi.WAKEFUL.get(), new int[] {2, 3});
        plan.put(Dushi.WAKER.get(), new int[] {1, 2, 3});
        plan.put(Dushi.SLEEPER.get(), new int[] {1, 2, 3});
        plan.put(Dushi.WAXEN.get(), new int[] {1, 2});
        plan.put(Dushi.SLEEP_MOTH.get(), new int[] {1});
        plan.put(Dushi.SHATUN.get(), new int[] {1, 2, 3, 4, 5, 6, 7});
        plan.put(Dushi.ELDER.get(), new int[] {1, 2, 3, 4, 5, 6, 7, 8});
        // the Morning Wharf (the crab's first move is its coming up, the Capsized's its getting up)
        plan.put(Dushi.DOCKHAND.get(), new int[] {1, 2, 3});
        plan.put(Dushi.HARPOONER.get(), new int[] {1, 2, 3});
        plan.put(Dushi.OARSMAN.get(), new int[] {1, 2, 3});
        plan.put(Dushi.PILOT.get(), new int[] {1, 2, 3});
        plan.put(Dushi.SAND_CRAB.get(), new int[] {2, 3});
        plan.put(Dushi.SKUA.get(), new int[] {1});
        plan.put(Dushi.HELMSMAN.get(), new int[] {1, 2, 3, 4, 5, 6});
        plan.put(Dushi.CAPSIZED.get(), new int[] {2, 3, 4, 5, 6, 7});
        // the Stubble (the scarecrow's first move is its getting down, the stook's its getting up; the Warden's first three
        // are made on the cross and the rest after it has caught, the Miller's first four with the stone on its axle)
        plan.put(Dushi.REAPER.get(), new int[] {1, 2, 3});
        plan.put(Dushi.MOWER.get(), new int[] {1, 2, 3});
        plan.put(Dushi.SACKMAN.get(), new int[] {1, 2});
        plan.put(Dushi.SCORCHED.get(), new int[] {1, 2});
        plan.put(Dushi.SCARECROW.get(), new int[] {2, 3});
        plan.put(Dushi.STOOK.get(), new int[] {2, 3});
        plan.put(Dushi.SUNGAZER.get(), new int[] {1, 2});
        plan.put(Dushi.SCORCHED_WARDEN.get(), new int[] {1, 2, 3, 4, 5, 6, 7});
        plan.put(Dushi.HOLLOW_MILLER.get(), new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9});
        return plan;
    }

    private static void drill() {
        for (Drill d : DRILL) {
            if (!d.mob.isAlive()) {
                continue;
            }
            d.mob.setTarget(d.dummy);
            if (d.mob.attackId() == 0 && d.next < d.moves.length) {
                if (d.next > 0) {
                    d.done.append(d.moves[d.next - 1]).append(' ');
                }
                d.mob.startAttack(d.moves[d.next++]);
            } else if (d.mob.attackId() == 0 && d.next == d.moves.length) {
                d.done.append(d.moves[d.next - 1]).append(' ');
                d.next++;
            }
        }
    }

    private static void reportDrill() {
        for (Drill d : DRILL) {
            say("drill " + d.name + ": finished moves [" + d.done.toString().trim() + "] of " + d.moves.length + ", alive=" + d.mob.isAlive()
                    + " phase=" + d.mob.phase() + " dummy health=" + Math.round(d.dummy.getHealth()) + "/" + Math.round(d.dummy.getMaxHealth()));
        }
        say("drill done");
    }

    private static final class Drill {
        final String name;
        final SoulsMob mob;
        final Mob dummy;
        final int[] moves;
        final StringBuilder done = new StringBuilder();
        int next;

        Drill(String name, SoulsMob mob, Mob dummy, int[] moves) {
            this.name = name;
            this.mob = mob;
            this.dummy = dummy;
            this.moves = moves;
        }
    }

    private static void report(ServerLevel level) {
        for (Map.Entry<String, Mob> entry : MOBS.entrySet()) {
            Mob mob = entry.getValue();
            StringBuilder line = new StringBuilder(entry.getKey()).append(": alive=").append(mob.isAlive()).append(" health=")
                    .append(Math.round(mob.getHealth())).append('/').append(Math.round(mob.getMaxHealth())).append(" ticked=").append(mob.tickCount);
            if (mob instanceof SoulsMob souls) {
                line.append(" phase=").append(souls.phase()).append(" moves=").append(MOVES.get(entry.getKey())).append(" souls=").append(souls.soulReward())
                        .append(" main=").append(mob.getMainHandItem().getItem()).append(" off=").append(mob.getOffhandItem().getItem());
            }
            say(line.toString());
        }
        AABB arena = new AABB(-HALF - 2, FLOOR_Y - 2, -HALF - 2, HALF + 2, FLOOR_Y + 20, HALF + 2);
        Map<String, Integer> census = new TreeMap<>();
        for (Entity entity : level.getEntitiesOfClass(Entity.class, arena)) {
            census.merge(EntityType.getKey(entity.getType()).getPath(), 1, Integer::sum);
        }
        say("on the floor after " + RUN_TICKS + " ticks: " + census);
        // Killing a boss: drops, and the fog gate next to it lifts.
        for (String boss : new String[] {"overseer", "chaplain"}) {
            Mob mob = MOBS.get(boss);
            if (mob == null || !mob.isAlive()) {
                say(boss + " was already dead");
                continue;
            }
            BlockPos gate = mob.blockPosition().offset(3, 0, 0);
            level.setBlock(gate, Dushi.FOG_GATE.get().defaultBlockState(), 3);
            mob.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
            List<String> drops = new ArrayList<>();
            for (var item : level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new AABB(mob.blockPosition()).inflate(4.0))) {
                drops.add(item.getItem().getItem().toString());
            }
            say(boss + " killed: alive=" + mob.isAlive() + " drops=" + drops + " fog gate lifted=" + !level.getBlockState(gate).is(Dushi.FOG_GATE.get()));
        }
        say("done");
    }
}
