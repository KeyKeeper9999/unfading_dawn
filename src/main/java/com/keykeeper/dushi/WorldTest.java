package com.keykeeper.dushi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.keykeeper.dushi.block.MobMarkBlock;
import com.keykeeper.dushi.block.MobMarkBlockEntity;
import com.keykeeper.dushi.entity.SoulsMob;
import com.keykeeper.dushi.world.Plan;
import com.keykeeper.dushi.world.PlanPiece;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/**
 * The second half of the automated check (see {@link SelfTest}): gear numbers, recipes and chest loot, the buildings set
 * down from their plans with everything that should be in them, the marks raising, losing and raising their mobs again,
 * and the buildings found in the world the server made.
 */
final class WorldTest {
    private WorldTest() {}

    private static void say(String text) {
        System.out.println("DUSHI SELFTEST " + text);
    }

    static void run(ServerLevel level) {
        gear(level);
        try {
            charms(level);
        } catch (RuntimeException e) {
            say("charms: FAILED: " + e);
            e.printStackTrace(System.out);
        }
        data(level);
        BlockPos stockade = new BlockPos(0, 240, 8);
        built(level, "stockade", stockade, Rotation.NONE);
        marks(level, stockade);
        built(level, "outpost", new BlockPos(-22, 280, -22), Rotation.CLOCKWISE_90);
        try {
            rules(level);
        } catch (RuntimeException e) {
            say("rules: FAILED: " + e);
            e.printStackTrace(System.out);
        }
        try {
            firwood(level);
            shore(level);
        } catch (RuntimeException e) {
            say("firwood: FAILED: " + e);
            e.printStackTrace(System.out);
        }
        try {
            magic(level);
        } catch (RuntimeException e) {
            say("magic: FAILED: " + e);
            e.printStackTrace(System.out);
        }
        for (String name : new String[] {"outpost", "stockade"}) {
            try {
                found(level, name);
            } catch (RuntimeException e) {
                say("world: " + name + " in the wild: FAILED: " + e);
                e.printStackTrace(System.out);
            }
        }
        say("world done");
    }

    /** What a map leans on: the tier of a mark, the shard in the ash, peace at a hearth, the hour of a place. */
    private static void rules(ServerLevel level) {
        BlockPos at = new BlockPos(40, 240, 40);
        for (BlockPos floor : BlockPos.betweenClosed(at.offset(-8, -1, -8), at.offset(8, -1, 8))) {
            level.setBlock(floor, Blocks.STONE.defaultBlockState(), 3);
        }
        level.setBlock(at, Dushi.MOB_MARK.get().defaultBlockState().setValue(MobMarkBlock.MOB, MobMarkBlock.Kind.TOWNSMAN).setValue(MobMarkBlock.TIER, 2), 3);
        SoulsMob risen = null;
        if (level.getBlockEntity(at) instanceof MobMarkBlockEntity mark && mark.wake(level) && level.getEntity(mark.mob()) instanceof SoulsMob mob) {
            risen = mob;
            say("rules: a townsman of tier " + mob.tier() + " has " + mob.getMaxHealth() + " health, blows x" + mob.damageScale() + ", leaves " + mob.reward()
                    + " souls (by birth " + mob.soulReward() + ")" + (mob.tier() == 2 && mob.getMaxHealth() > 13.0F ? "" : " FAILED"));
        } else {
            say("rules: the mark of tier 2 raised nothing FAILED");
        }
        FakePlayer player = FakePlayerFactory.getMinecraft(level);
        player.setPos(at.getX() + 4.5, at.getY(), at.getZ() + 0.5);
        PlayerSoul soul = PlayerSoul.of(player);
        BlockPos heap = at.offset(4, 0, 2);
        level.setBlock(heap, Dushi.SHARD_HEAP.get().defaultBlockState(), 3);
        int before = Forge.count(player, Dushi.FLASK_SHARD.get());
        level.getBlockState(heap).useWithoutItem(level, player, new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(heap),
                net.minecraft.core.Direction.UP, heap, false));
        boolean taken = Forge.count(player, Dushi.FLASK_SHARD.get()) == before + 1 && level.getBlockState(heap).isAir();
        say("rules: the shard in the ash: taken=" + taken + (taken ? "" : " FAILED"));
        player.getInventory().clearContent();
        if (risen != null) {
            // (a stand-in player is not in the world, so the mob cannot hold it as a target: the choice itself is asked)
            boolean far = SoulMechanics.nearHearth(player);
            level.setBlock(at.offset(7, 0, 0), Dushi.HEARTH.get().defaultBlockState(), 3);
            boolean near = SoulMechanics.nearHearth(player);
            soul.atHearth = near;
            boolean spared = risen.overlooks(player);
            soul.atHearth = false;
            boolean noticed = !risen.overlooks(player);
            say("rules: by a hearth: none near before=" + !far + ", one near after=" + near + ", not noticed there=" + spared + ", noticed away from it=" + noticed
                    + (!far && near && spared && noticed ? "" : " FAILED"));
            risen.discard();
        }
        level.setBlock(at, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(at.offset(7, 0, 0), Blocks.AIR.defaultBlockState(), 3);
        boolean first = soul.discover("market");
        boolean again = soul.discover("market");
        boolean other = soul.discover("shambles");
        say("rules: the name of a place: shown on finding it=" + first + ", not again=" + !again + ", another place still=" + other + (first && !again && other ? "" : " FAILED"));
        // the hour of a place: only where time stands still
        long was = level.getDefaultClockTime();
        SoulMechanics.hour(level, 18000);
        boolean untouched = level.getDefaultClockTime() == was;
        level.getGameRules().set(net.minecraft.world.level.gamerules.GameRules.ADVANCE_TIME, false, level.getServer());
        SoulMechanics.hour(level, 18000);
        boolean set = Math.floorMod(level.getDefaultClockTime(), 24000L) == 18000L && SoulMechanics.hourNow(level) == 18000;
        level.getGameRules().set(net.minecraft.world.level.gamerules.GameRules.ADVANCE_TIME, true, level.getServer());
        say("rules: the hour of a place: left alone where time moves=" + untouched + ", set where it stands=" + set + (untouched && set ? "" : " FAILED"));
        quarry(level, at, player);
    }

    /** What the quarry leans on: a grate that wants its key, a crawler that hears and does not see, a statue that wakes. */
    private static void quarry(ServerLevel level, BlockPos at, FakePlayer player) {
        BlockPos gate = at.offset(-5, 0, -5);
        for (BlockPos pos : BlockPos.betweenClosed(gate, gate.offset(2, 2, 0))) {
            level.setBlock(pos, Dushi.LOCKED_GATE.get().defaultBlockState(), 3);
        }
        net.minecraft.world.phys.BlockHitResult hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(gate),
                net.minecraft.core.Direction.SOUTH, gate, false);
        player.getInventory().clearContent();
        level.getBlockState(gate).useWithoutItem(level, player, hit);
        boolean held = level.getBlockState(gate.offset(2, 2, 0)).is(Dushi.LOCKED_GATE.get());
        player.getInventory().add(new ItemStack(Dushi.QUARRY_KEY.get()));
        level.getBlockState(gate).useWithoutItem(level, player, hit);
        boolean opened = level.getBlockState(gate).isAir() && level.getBlockState(gate.offset(2, 2, 0)).isAir();
        boolean kept = Forge.count(player, Dushi.QUARRY_KEY.get()) == 1;
        player.getInventory().clearContent();
        say("rules: the locked grate: holds without the key=" + held + ", opens whole with it=" + opened + ", the key is kept=" + kept
                + (held && opened && kept ? "" : " FAILED"));

        boolean ears = com.keykeeper.dushi.entity.BlindCrawlerEntity.hears(false, 10.0, 0.0, 0.0) && !com.keykeeper.dushi.entity.BlindCrawlerEntity.hears(false, 16.0, 0.0, 0.0)
                && !com.keykeeper.dushi.entity.BlindCrawlerEntity.hears(true, 10.0, 0.0, 0.0) && !com.keykeeper.dushi.entity.BlindCrawlerEntity.hears(true, 2.0, 0.0, 0.0)
                && !com.keykeeper.dushi.entity.BlindCrawlerEntity.hears(true, 0.0, 0.0, -1.6) && com.keykeeper.dushi.entity.BlindCrawlerEntity.hears(true, 1.4, 0.0, -1.4);
        say("rules: the blind crawler: hears a walker at ten blocks and a creeper only on the blocks that touch its own=" + ears + (ears ? "" : " FAILED"));

        com.keykeeper.dushi.entity.UnhewnEntity statue = Dushi.UNHEWN.get().spawn(level, at.offset(-4, 0, 4), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (statue == null) {
            say("rules: the unhewn did not spawn FAILED");
            return;
        }
        boolean asleep = !statue.awake() && statue.overlooks(player);
        statue.hurtServer(level, level.damageSources().generic(), 1.0F);
        boolean woken = statue.awake() && statue.attackId() == com.keykeeper.dushi.entity.UnhewnEntity.WAKE;
        statue.recover();
        boolean again = !statue.awake();
        statue.discard();
        say("rules: the unhewn: a statue until touched=" + asleep + ", wakes when struck=" + woken + ", stone again after a rest=" + again
                + (asleep && woken && again ? "" : " FAILED"));
        mire(level, at, player);
    }

    /**
     * What the Mire leans on: a film of water that is a floor and not water, sludge that lies on it and on bare floor and
     * goes again, a mark that stands in water, a grate that wants the Mire's key, and a tree that does not move.
     */
    private static void mire(ServerLevel level, BlockPos at, FakePlayer player) {
        BlockPos film = at.offset(-6, 0, 2);
        for (BlockPos pos : BlockPos.betweenClosed(film.offset(-1, 0, -1), film.offset(1, 0, 1))) {
            level.setBlock(pos, Dushi.SHALLOWS.get().defaultBlockState(), 3);
        }
        boolean dry = level.getFluidState(film).isEmpty() && level.getBlockState(film).getCollisionShape(level, film).isEmpty();
        int pools = com.keykeeper.dushi.block.SludgeBlock.spill(level, net.minecraft.world.phys.Vec3.atBottomCenterOf(film.offset(2, 0, 0)), 2.4);
        BlockState onFilm = level.getBlockState(film);
        BlockState onFloor = level.getBlockState(film.offset(3, 0, 0));
        boolean lies = onFilm.is(Dushi.SLUDGE.get()) && onFilm.getValue(com.keykeeper.dushi.block.SludgeBlock.WET)
                && onFloor.is(Dushi.SLUDGE.get()) && !onFloor.getValue(com.keykeeper.dushi.block.SludgeBlock.WET);
        onFilm.tick(level, film, level.getRandom());
        onFloor.tick(level, film.offset(3, 0, 0), level.getRandom());
        boolean gone = level.getBlockState(film).is(Dushi.SHALLOWS.get()) && level.getBlockState(film.offset(3, 0, 0)).isAir();
        say("rules: the shallows and the sludge: the film is a floor and no water=" + dry + ", " + pools + " pools from one clot, wet on the film and dry beside it=" + lies
                + ", gone again and the film back=" + gone + (dry && pools >= 12 && lies && gone ? "" : " FAILED"));
        for (BlockPos pos : BlockPos.betweenClosed(film.offset(-3, 0, -3), film.offset(5, 0, 3))) {
            if (level.getBlockState(pos).is(Dushi.SHALLOWS.get()) || level.getBlockState(pos).is(Dushi.SLUDGE.get())) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }

        BlockPos pond = at.offset(4, 0, -5);
        level.setBlock(pond, Dushi.MOB_MARK.get().defaultBlockState().setValue(MobMarkBlock.MOB, MobMarkBlock.Kind.PIKE).setValue(MobMarkBlock.WATERLOGGED, true), 3);
        boolean wet = level.getFluidState(pond).getType() == net.minecraft.world.level.material.Fluids.WATER;
        boolean fish = level.getBlockEntity(pond) instanceof MobMarkBlockEntity mark && mark.wake(level)
                && level.getEntity(mark.mob()) instanceof com.keykeeper.dushi.entity.BonePikeEntity;
        say("rules: a mark in water: holds water=" + wet + ", raises a pike=" + fish + (wet && fish ? "" : " FAILED"));
        if (level.getBlockEntity(pond) instanceof MobMarkBlockEntity mark && level.getEntity(mark.mob()) != null) {
            level.getEntity(mark.mob()).discard();
        }
        level.setBlock(pond, Blocks.AIR.defaultBlockState(), 3);

        BlockPos gate = at.offset(-5, 0, -7);
        BlockState grate = Dushi.LOCKED_GATE.get().defaultBlockState().setValue(com.keykeeper.dushi.block.LockedGateBlock.LOCK, com.keykeeper.dushi.block.LockedGateBlock.Lock.MIRE);
        for (BlockPos pos : BlockPos.betweenClosed(gate, gate.offset(2, 2, 0))) {
            level.setBlock(pos, grate, 3);
        }
        net.minecraft.world.phys.BlockHitResult hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(gate),
                net.minecraft.core.Direction.SOUTH, gate, false);
        player.getInventory().clearContent();
        player.getInventory().add(new ItemStack(Dushi.QUARRY_KEY.get()));
        level.getBlockState(gate).useWithoutItem(level, player, hit);
        boolean held = level.getBlockState(gate.offset(2, 2, 0)).is(Dushi.LOCKED_GATE.get());
        player.getInventory().add(new ItemStack(Dushi.MIRE_KEY.get()));
        level.getBlockState(gate).useWithoutItem(level, player, hit);
        boolean opened = level.getBlockState(gate).isAir() && level.getBlockState(gate.offset(2, 2, 0)).isAir();
        player.getInventory().clearContent();
        say("rules: the Mire Gate: holds against the quarry's key=" + held + ", opens to its own=" + opened + (held && opened ? "" : " FAILED"));

        com.keykeeper.dushi.entity.WeepingMotherEntity tree = Dushi.WEEPING_MOTHER.get().spawn(level, at.offset(0, 0, 5), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (tree == null) {
            say("rules: the Weeping Mother did not spawn FAILED");
            return;
        }
        tree.knockback(3.0, 1.0, 0.0, level.damageSources().generic(), 1.0F);
        boolean rooted = tree.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED) == 0.0 && !tree.isPushable()
                && tree.getDeltaMovement().horizontalDistanceSqr() < 1.0E-6;
        say("rules: the Weeping Mother: neither walks nor is pushed nor knocked back=" + rooted + (rooted ? "" : " FAILED"));
        // her roots: out of the ground only, straight under whoever is over it; one blow, and no second while they are still
        // in the air from the first; nothing for whoever is higher than a root reaches
        net.minecraft.world.phys.Vec3 under = com.keykeeper.dushi.entity.RootEntity.ground(level, at.getX() + 2.5, at.getY() + 3.0, at.getZ() + 0.5);
        net.minecraft.world.phys.Vec3 nowhere = com.keykeeper.dushi.entity.RootEntity.ground(level, at.getX() + 30.5, at.getY() + 3.0, at.getZ() + 0.5);
        boolean grounded = under != null && under.y == at.getY() && nowhere == null;
        var struck = net.minecraft.world.entity.EntityTypes.IRON_GOLEM.spawn(level, at.offset(2, 0, 0), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        var high = net.minecraft.world.entity.EntityTypes.IRON_GOLEM.spawn(level, at.offset(-3, 4, 0), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (struck == null || high == null || under == null) {
            say("rules: the roots: grounded=" + grounded + ", nothing to strike FAILED");
            tree.discard();
            return;
        }
        struck.setNoAi(true);
        high.setNoAi(true);
        float whole = struck.getHealth();
        tree.rootStrikes(level, under);
        float once = struck.getHealth();
        struck.setInvulnerableTime(0);
        tree.rootStrikes(level, under);
        float twice = struck.getHealth();
        float above = high.getHealth();
        tree.rootStrikes(level, new net.minecraft.world.phys.Vec3(high.getX(), at.getY(), high.getZ()));
        boolean spared = high.getHealth() == above;
        com.keykeeper.dushi.entity.RootEntity root = com.keykeeper.dushi.entity.RootEntity.rise(level, tree, under, 19);
        boolean risen = root.isAlive() && tree.rootsWaiting() == 0 && root.warning() == 19;
        say("rules: the roots: come up from the ground under one in the air and nowhere over nothing=" + grounded + ", strike once (" + whole + " -> " + once + " -> " + twice
                + ")=" + (once < whole && twice == once) + ", not one who is over their reach=" + spared + ", a root is a thing in the world=" + risen
                + (grounded && once < whole && twice == once && spared && risen ? "" : " FAILED"));
        root.discard();
        struck.discard();
        high.discard();
        tree.discard();
    }

    /** The Black Firwood: whom a kneeling one sees, whom a waker wakes, what the hides, the linen and the great candle do. */
    private static void firwood(ServerLevel level) {
        BlockPos at = new BlockPos(40, 240, 40);
        // one of the wakeful, kneeling with his face to the south
        boolean seen = com.keykeeper.dushi.entity.WakefulEntity.sees(0.0F, 0.0, 8.0, false) && com.keykeeper.dushi.entity.WakefulEntity.sees(0.0F, 3.0, 4.0, true);
        boolean unseen = !com.keykeeper.dushi.entity.WakefulEntity.sees(0.0F, 0.0, -5.0, false) && !com.keykeeper.dushi.entity.WakefulEntity.sees(0.0F, 0.0, 8.0, true)
                && !com.keykeeper.dushi.entity.WakefulEntity.sees(0.0F, 0.0, 12.0, false) && !com.keykeeper.dushi.entity.WakefulEntity.sees(0.0F, 6.0, 0.0, false);
        boolean felt = com.keykeeper.dushi.entity.WakefulEntity.sees(0.0F, 0.0, -2.0, false) && !com.keykeeper.dushi.entity.WakefulEntity.sees(0.0F, 0.0, -1.0, true);
        say("firwood: the wakeful: sees who is before him=" + seen + ", not who is behind, far, beside, or creeping at a distance=" + unseen
                + ", feels who walks at his back and not who creeps there=" + felt + (seen && unseen && felt ? "" : " FAILED"));
        com.keykeeper.dushi.entity.WakerEntity waker = Dushi.WAKER.get().spawn(level, at, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        com.keykeeper.dushi.entity.WakefulEntity near = Dushi.WAKEFUL.get().spawn(level, at.offset(7, 0, 0), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        com.keykeeper.dushi.entity.WakefulEntity far = Dushi.WAKEFUL.get().spawn(level, at.offset(-28, 0, 0), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        com.keykeeper.dushi.entity.ShatunEntity bear = Dushi.SHATUN.get().spawn(level, at.offset(0, 0, 6), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        com.keykeeper.dushi.entity.ElderEntity elder = Dushi.ELDER.get().spawn(level, at.offset(0, 0, -6), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (waker == null || near == null || far == null || bear == null || elder == null) {
            say("firwood: something did not spawn FAILED");
            return;
        }
        boolean knelt = !near.awake() && !far.awake();
        waker.wake(level);
        boolean woken = near.awake() && near.attackId() == com.keykeeper.dushi.entity.WakefulEntity.RISE && !far.awake();
        say("firwood: the waker: they kneel=" + knelt + ", his board wakes the one who hears it and not the one who does not=" + woken + (knelt && woken ? "" : " FAILED"));
        float bearWhole = bear.getHealth();
        bear.hurtServer(level, level.damageSources().generic(), 100.0F);
        float elderWhole = elder.getHealth();
        elder.hurtServer(level, level.damageSources().generic(), 100.0F);
        boolean hides = Math.abs(bearWhole - bear.getHealth() - 100.0F * com.keykeeper.dushi.entity.ShatunEntity.HIDE) < 0.01F
                && Math.abs(elderWhole - elder.getHealth() - 100.0F * com.keykeeper.dushi.entity.ElderEntity.HIDE) < 0.01F;
        say("firwood: of a blow of 100 the Shatun takes " + (bearWhole - bear.getHealth()) + " and the Elder " + (elderWhole - elder.getHealth()) + (hides ? "" : " FAILED"));
        // sleep, and the linen that keeps it off
        FakePlayer player = FakePlayerFactory.getMinecraft(level);
        player.setPos(at.getX() + 0.5, at.getY(), at.getZ() + 3.5);
        wear(player, null, null, null, null, null, null);
        player.removeAllEffects();
        Gear.drowse(player, waker, 100, 1);
        var dark = player.getEffect(net.minecraft.world.effect.MobEffects.DARKNESS);
        int bare = dark == null ? 0 : dark.getDuration();
        player.removeAllEffects();
        ArmorSet set = ArmorSet.VIGIL;
        wear(player, set.piece(0), set.piece(1), set.piece(2), set.piece(3), null, null);
        Gear.drowse(player, waker, 100, 1);
        dark = player.getEffect(net.minecraft.world.effect.MobEffects.DARKNESS);
        int clothed = dark == null ? 0 : dark.getDuration();
        player.removeAllEffects();
        Gear.drowse(player, waker, 40, 1);
        boolean shrugged = player.getEffect(net.minecraft.world.effect.MobEffects.DARKNESS) == null;
        player.removeAllEffects();
        boolean linen = bare == 100 && clothed == Math.round(100 * (1.0F - 4 * set.bonus)) && shrugged;
        say("firwood: sleep holds " + bare + " ticks, in the linen of the Unsleeping " + clothed + ", and a short one not at all=" + shrugged + (linen ? "" : " FAILED"));
        // the great candle sets alight what it strikes
        wear(player, null, null, null, null, Dushi.GREAT_CANDLE.get(), null);
        near.setInvulnerableTime(0);
        float before = near.getHealth();
        player.attack(near);
        boolean burns = near.getRemainingFireTicks() > 0;
        say("firwood: the great candle: a blow of it took " + (before - near.getHealth()) + " and left its mark burning=" + burns + (burns ? "" : " FAILED"));
        wear(player, null, null, null, null, null, null);
        waker.discard();
        near.discard();
        far.discard();
        bear.discard();
        elder.discard();
    }

    /**
     * What pours from a palm reaches as far as it says and whatever is that near, a wide thing as well as a narrow one; a
     * root comes up for whoever is looked at; the shields that are more than their numbers; what a level costs.
     */
    private static void magic(ServerLevel level) {
        BlockPos at = new BlockPos(40, 240, 40);
        FakePlayer player = FakePlayerFactory.getMinecraft(level);
        player.setPos(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
        player.setYRot(0.0F);
        player.setXRot(0.0F);                                    // looking south, level
        var spawn = net.minecraft.world.entity.EntitySpawnReason.COMMAND;
        var near = net.minecraft.world.entity.EntityTypes.IRON_GOLEM.spawn(level, at.offset(0, 0, 3), spawn);
        var beyond = net.minecraft.world.entity.EntityTypes.IRON_GOLEM.spawn(level, at.offset(0, 0, 6), spawn);
        var beside = net.minecraft.world.entity.EntityTypes.IRON_GOLEM.spawn(level, at.offset(4, 0, 0), spawn);
        var behind = net.minecraft.world.entity.EntityTypes.IRON_GOLEM.spawn(level, at.offset(0, 0, -2), spawn);
        if (near == null || beyond == null || beside == null || behind == null) {
            say("magic: something did not spawn FAILED");
            return;
        }
        for (var golem : new net.minecraft.world.entity.Mob[] {near, beyond, beside, behind}) {
            golem.setNoAi(true);
        }
        float whole = near.getHealth();
        Spell.FLARE.cast(level, player, 1.0F);
        float once = near.getHealth();
        Spell.FLARE.cast(level, player, 1.0F);                   // at once: a blow a moment ago does not put it off
        float twice = near.getHealth();
        boolean reaches = once < whole && twice < once && beyond.getHealth() == beyond.getMaxHealth() && beside.getHealth() == beside.getMaxHealth()
                && behind.getHealth() == behind.getMaxHealth();
        // by its own rule: a thing a pace wide whose near side is at 3.4, and at 3.9; one as wide as a wagon whose middle is 5 off
        net.minecraft.world.phys.Vec3 eye = new net.minecraft.world.phys.Vec3(0.0, 1.4, 0.0);
        net.minecraft.world.phys.Vec3 south = new net.minecraft.world.phys.Vec3(0.0, 0.0, 1.0);
        boolean rule = Spell.jetTouches(eye, south, Spell.FLARE_REACH, new net.minecraft.world.phys.AABB(-0.5, 0.0, 3.4, 0.5, 2.0, 4.4))
                && !Spell.jetTouches(eye, south, Spell.FLARE_REACH, new net.minecraft.world.phys.AABB(-0.5, 0.0, 3.9, 0.5, 2.0, 4.9))
                && Spell.jetTouches(eye, south, Spell.FLARE_REACH, new net.minecraft.world.phys.AABB(-1.6, 0.0, 3.4, 1.6, 2.0, 6.6))
                && Spell.jetTouches(eye, south, Spell.FLARE_REACH, new net.minecraft.world.phys.AABB(0.8, 0.0, 2.5, 1.8, 2.0, 3.5))
                && !Spell.jetTouches(eye, south, Spell.FLARE_REACH, new net.minecraft.world.phys.AABB(2.5, 0.0, 2.5, 3.5, 2.0, 3.5))
                && !Spell.jetTouches(eye, south, Spell.FLARE_REACH, new net.minecraft.world.phys.AABB(-0.5, 0.0, -2.5, 0.5, 2.0, -1.5));
        say("magic: the palm's flame: of the one two paces off " + whole + " -> " + once + " -> " + twice + ", none of those beyond, beside or behind=" + reaches
                + ", its reach by rule=" + rule + (reaches && rule ? "" : " FAILED"));
        // hot wax: further, weaker, and it sets
        near.setHealth(near.getMaxHealth());
        Spell.HOT_WAX.cast(level, player, 1.0F);
        boolean waxed = near.getHealth() < near.getMaxHealth() && near.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS)
                && beside.getHealth() == beside.getMaxHealth();
        // the scatter: five shards
        Spell.SOUL_SCATTER.cast(level, player, 1.0F);
        int shards = level.getEntitiesOfClass(com.keykeeper.dushi.entity.SpellBoltEntity.class, player.getBoundingBox().inflate(4.0)).size();
        level.getEntitiesOfClass(com.keykeeper.dushi.entity.SpellBoltEntity.class, player.getBoundingBox().inflate(4.0)).forEach(net.minecraft.world.entity.Entity::discard);
        // the root: for whoever is looked at, out of the ground under them; it strikes them and not its caster
        near.removeAllEffects();
        near.setHealth(near.getMaxHealth());
        near.damageCooldownTime = 0;
        boolean picked = Spell.aimed(level, player, Spell.SNARE_RANGE) == near;
        net.minecraft.world.phys.Vec3 ground = com.keykeeper.dushi.entity.RootEntity.ground(level, near.getX(), near.getY(), near.getZ());
        boolean snared = false;
        if (ground != null) {
            com.keykeeper.dushi.entity.RootEntity root = com.keykeeper.dushi.entity.RootEntity.rise(level, player, ground, Spell.SNARE_WARNING, 11.0F);
            root.tickCount = Spell.SNARE_WARNING;
            root.tick();
            snared = near.getHealth() == near.getMaxHealth() - 11.0F && near.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS);
            root.discard();
        }
        say("magic: hot wax burns and sets=" + waxed + ", the scatter is of " + shards + " shards, the root: picks out whom the eyes are on=" + picked
                + ", strikes and holds them=" + snared + (waxed && shards == 5 && picked && snared ? "" : " FAILED"));
        for (var golem : new net.minecraft.world.entity.Mob[] {near, beyond, beside, behind}) {
            golem.discard();
        }
        // the shields that are more than their numbers
        float guard = Gear.weight(new ItemStack(Dushi.GUARD_SHIELD.get()));
        float scale = Gear.weight(new ItemStack(Dushi.SCALE_SHIELD.get()));
        boolean shields = scale == 4.0F && guard == 8.0F && !Gear.tiresTheArm(new ItemStack(Dushi.CARVER_BUCKLER.get()))
                && Gear.tiresTheArm(new ItemStack(Dushi.GUARD_SHIELD.get())) && Gear.tiresTheArm(null);
        say("magic: shields: the scale shield weighs " + scale + " to the guard's " + guard + ", the buckler alone does not tire the arm=" + shields
                + (shields ? "" : " FAILED"));
        // what a level costs
        int first = PlayerSoul.levelCost(1);
        int thirty = PlayerSoul.levelCost(30);
        int sixty = PlayerSoul.levelCost(60);
        int eighty = PlayerSoul.levelCost(80);
        boolean costs = first == 100 && thirty == 2362 && sixty == 8242 && eighty == 14162;
        say("magic: a level costs " + first + " at 1, " + thirty + " at 30, " + sixty + " at 60, " + eighty + " at 80" + (costs ? "" : " FAILED"));
    }

    // ------------------------------------------------------------------ gear

    private static void wear(FakePlayer player, Item head, Item chest, Item legs, Item feet, Item main, Item off) {
        Item[] items = {head, chest, legs, feet, main, off};
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};
        for (int i = 0; i < slots.length; i++) {
            player.setItemSlot(slots[i], items[i] == null ? ItemStack.EMPTY : new ItemStack(items[i]));
        }
    }

    private static String burden(FakePlayer player) {
        return Math.round(Gear.load(player)) + "/" + Math.round(Gear.maxLoad(PlayerSoul.of(player))) + " " + Gear.burden(player).id;
    }

    private static void gear(ServerLevel level) {
        FakePlayer player = FakePlayerFactory.getMinecraft(level);
        StringBuilder line = new StringBuilder("gear: ");
        wear(player, null, null, null, null, null, null);
        line.append("bare ").append(burden(player));
        wear(player, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS, Items.IRON_SWORD, null);
        line.append("; leather+sword ").append(burden(player));
        wear(player, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS, Items.IRON_SWORD, Items.SHIELD);
        line.append("; iron+sword+shield ").append(burden(player));
        wear(player, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS, Items.MACE, Dushi.TOWER_SHIELD.get());
        line.append("; netherite+mace+tower shield ").append(burden(player));
        for (ArmorSet set : ArmorSet.values()) {
            wear(player, set.piece(0), set.piece(1), set.piece(2), set.piece(3), null, null);
            line.append("; ").append(set.id).append(' ').append(burden(player)).append(" armour=").append(player.getArmorValue())
                    .append(" worn=").append(Gear.worn(player, set));
        }
        say(line.toString());
        say("gear: regen in rags x" + rounded(regenIn(player, ArmorSet.PRISONER)) + ", spells in vestments x" + rounded(spellsIn(player))
                + ", block cost in mail x" + rounded(blockIn(player)));
        // the stonecutter's gear against a blast, the scale coat against water
        ArmorSet mason = ArmorSet.MASON;
        wear(player, mason.piece(0), mason.piece(1), mason.piece(2), mason.piece(3), null, null);
        float blast = Gear.blastShare(player);
        ArmorSet scale = ArmorSet.SCALE;
        var water = net.minecraft.world.entity.ai.attributes.Attributes.WATER_MOVEMENT_EFFICIENCY;
        wear(player, scale.piece(0), scale.piece(1), null, null, null, null);
        SoulMechanics.wade(player);
        double two = player.getAttributeValue(water);
        wear(player, scale.piece(0), scale.piece(1), scale.piece(2), scale.piece(3), null, null);
        SoulMechanics.wade(player);
        double four = player.getAttributeValue(water);
        wear(player, null, null, null, null, null, null);
        SoulMechanics.wade(player);
        double none = player.getAttributeValue(water);
        boolean gifts = Math.abs(blast - (1.0F - 4 * mason.bonus)) < 1.0E-4 && Gear.blastShare(player) == 1.0F
                && Math.abs(two - 2 * scale.bonus) < 1.0E-4 && Math.abs(four - 4 * scale.bonus) < 1.0E-4 && none == 0.0;
        say("gear: of a blast the stonecutter's gear lets through x" + rounded(blast) + "; the water's hold taken off by two pieces of the scale coat "
                + two + ", by four " + four + ", by none " + none + (gifts ? "" : " FAILED"));
        StringBuilder weapons = new StringBuilder("gear: weapons");
        for (Item item : new Item[] {Items.WOODEN_SWORD, Items.DIAMOND_AXE, Items.IRON_PICKAXE, Items.IRON_SPEAR, Items.MACE, Items.TRIDENT,
                Dushi.LONGSWORD.get(), Items.STICK}) {
            Gear.Weapon weapon = Gear.weapon(new ItemStack(item));
            weapons.append(' ').append(BuiltInRegistries.ITEM.getKey(item).getPath()).append('=')
                    .append(weapon == null ? "none" : Math.round(weapon.stamina()) + "/" + weapon.strength() + "/" + weapon.dexterity() + "/x" + weapon.backstab())
                    .append(" w").append(Gear.weight(new ItemStack(item)));
        }
        weapons.append(" bow w").append(Gear.weight(new ItemStack(Items.BOW))).append(" shield stability ").append(Gear.stability(new ItemStack(Items.SHIELD)));
        say(weapons.toString());
        wear(player, null, null, null, null, null, null);
    }

    /** The Morning Wharf: the lords' hides, what lies as a boat and what lies in the sand, the oilskins, and how deep the sea is. */
    private static void shore(ServerLevel level) {
        BlockPos at = new BlockPos(-40, 240, 40);
        for (BlockPos pos : BlockPos.betweenClosed(at.offset(-12, -1, -12), at.offset(12, -1, 12))) {
            level.setBlock(pos, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
        }
        com.keykeeper.dushi.entity.HelmsmanEntity helmsman = Dushi.HELMSMAN.get().spawn(level, at.offset(-6, 0, 0), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        com.keykeeper.dushi.entity.CapsizedEntity boat = Dushi.CAPSIZED.get().spawn(level, at.offset(6, 0, 0), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        com.keykeeper.dushi.entity.SandCrabEntity crab = Dushi.SAND_CRAB.get().spawn(level, at.offset(0, 0, 8), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        com.keykeeper.dushi.entity.SkuaEntity skua = Dushi.SKUA.get().spawn(level, at.offset(0, 0, -8), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (helmsman == null || boat == null || crab == null || skua == null) {
            say("shore: something did not spawn FAILED");
            return;
        }
        float whole = helmsman.getHealth();
        helmsman.hurtServer(level, level.damageSources().generic(), 100.0F);
        float helm = whole - helmsman.getHealth();
        boolean lay = !boat.up() && boat.housed() && !crab.up() && skua.phase() == 1;
        whole = boat.getHealth();
        boat.hurtServer(level, level.damageSources().generic(), 100.0F);
        float planks = whole - boat.getHealth();
        boolean rose = boat.up() && boat.attackId() == com.keykeeper.dushi.entity.CapsizedEntity.RISE;
        crab.hurtServer(level, level.damageSources().generic(), 1.0F);
        skua.hurtServer(level, level.damageSources().generic(), 1.0F);
        boolean woke = crab.up() && skua.phase() >= 2;
        boolean hides = Math.abs(helm - 100.0F * com.keykeeper.dushi.entity.HelmsmanEntity.HIDE) < 0.01F
                && Math.abs(planks - 100.0F * com.keykeeper.dushi.entity.CapsizedEntity.PLANKS) < 0.01F;
        say("shore: of a blow of 100 the Helmsman takes " + helm + " and the boat on the sand " + planks + "; they lay still=" + lay + ", struck, the boat gets up="
                + rose + ", the crab and the skua too=" + woke + (hides && lay && rose && woke ? "" : " FAILED"));
        helmsman.discard();
        boat.discard();
        crab.discard();
        skua.discard();
        // the oilskins, and the sea
        FakePlayer player = FakePlayerFactory.getMinecraft(level);
        ArmorSet oilskin = ArmorSet.OILSKIN;
        wear(player, oilskin.piece(0), oilskin.piece(1), oilskin.piece(2), oilskin.piece(3), null, null);
        float thrown = Gear.thrown(player);
        wear(player, null, null, null, null, null, null);
        BlockPos pool = at.offset(0, 6, 0);
        for (BlockPos pos : BlockPos.betweenClosed(pool.offset(-1, -1, -1), pool.offset(2, 3, 1))) {
            level.setBlock(pos, net.minecraft.world.level.block.Blocks.GLASS.defaultBlockState(), 3);
        }
        for (int y = 0; y < 3; y++) {
            level.setBlock(pool.above(y), net.minecraft.world.level.block.Blocks.WATER.defaultBlockState(), 3);
        }
        level.setBlock(pool.offset(1, 0, 0), net.minecraft.world.level.block.Blocks.WATER.defaultBlockState(), 3);
        level.setBlock(pool.offset(1, 1, 0), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
        int deep = SoulMechanics.waterDepth(level, pool.above());
        int knee = SoulMechanics.waterDepth(level, pool.offset(1, 0, 0));
        int dry = SoulMechanics.waterDepth(level, pool.offset(1, 1, 0));
        boolean sea = Math.abs(thrown - (1.0F - 4 * oilskin.bonus)) < 1.0E-4 && Gear.thrown(player) == 1.0F && deep == 3 && knee == 1 && dry == 0;
        say("shore: a blow throws somebody in oilskins x" + rounded(thrown) + "; the sea is " + deep + " deep where it takes them, " + knee + " where it is waded, "
                + dry + " over it" + (sea ? "" : " FAILED"));
        for (BlockPos pos : BlockPos.betweenClosed(pool.offset(-1, -1, -1), pool.offset(2, 3, 1))) {
            level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
        }
    }

    /** The charms: one is worn, by using it; the next takes its place and gives the first back; each gives what it says. */
    private static void charms(ServerLevel level) {
        FakePlayer player = FakePlayerFactory.getMinecraft(level);
        PlayerSoul soul = PlayerSoul.of(player);
        wear(player, null, null, null, null, null, null);
        player.getInventory().clearContent();
        soul.charm = "";
        soul.applyAttributes(player);
        float load = Gear.maxLoad(soul);
        float health = player.getMaxHealth();
        Item link = Dushi.CHARMS.get(Charm.FETTER_LINK).get();
        Item claw = Dushi.CHARMS.get(Charm.SHATUN_CLAW).get();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(link));
        player.getMainHandItem().use(level, player, net.minecraft.world.InteractionHand.MAIN_HAND);
        boolean on = Charm.worn(player) == Charm.FETTER_LINK && player.getMainHandItem().isEmpty() && Gear.maxLoad(soul) == load + Charm.FETTER_LINK.amount;
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(claw));
        player.getMainHandItem().use(level, player, net.minecraft.world.InteractionHand.MAIN_HAND);
        boolean swapped = Charm.worn(player) == Charm.SHATUN_CLAW && player.getMainHandItem().is(link) && Gear.maxLoad(soul) == load
                && player.getMaxHealth() == health + 2.0F * Charm.SHATUN_CLAW.amount;
        boolean off = com.keykeeper.dushi.item.CharmItem.takeOff(player) && Charm.worn(player) == null && player.getMaxHealth() == health
                && Forge.count(player, claw) == 1 && !com.keykeeper.dushi.item.CharmItem.takeOff(player);
        say("charms: " + Dushi.CHARMS.size() + " of them; put on by using it=" + on + ", the next takes its place and the first comes back=" + swapped
                + ", taken off and given back=" + off + (on && swapped && off ? "" : " FAILED"));
        player.getInventory().clearContent();
        Gear.Weapon sword = Gear.weapon(new ItemStack(Dushi.LONGSWORD.get()));
        float[] bare = {Gear.staminaRegen(player), Gear.spellBonus(player), Gear.blockCost(player), Gear.rollCost(player, Gear.Burden.MEDIUM), Gear.backstab(player, sword),
                Gear.swingCost(player, 20.0F)};
        StringBuilder gifts = new StringBuilder("charms:");
        boolean each = true;
        for (Charm charm : Charm.values()) {
            soul.charm = charm.id;
            float[] with = {Gear.staminaRegen(player), Gear.spellBonus(player), Gear.blockCost(player), Gear.rollCost(player, Gear.Burden.MEDIUM), Gear.backstab(player, sword),
                    Gear.swingCost(player, 20.0F)};
            int changed = 0;
            for (int i = 0; i < bare.length; i++) {
                if (with[i] != bare[i]) {
                    changed++;
                    gifts.append(' ').append(charm.id).append('=').append(new String[] {"regen x", "spells x", "block x", "roll ", "backstab x", "swing "}[i]).append(rounded(with[i]));
                }
            }
            boolean other = charm == Charm.COFFIN_NAIL || charm == Charm.ASH_KNOT || charm == Charm.FETTER_LINK || charm == Charm.SHATUN_CLAW || charm == Charm.TIDE_PEARL;
            each &= changed == (other ? 0 : 1);
        }
        soul.charm = Charm.COFFIN_NAIL.id;
        gifts.append(" coffin_nail=souls x").append(rounded(Charm.COFFIN_NAIL.more(player))).append(" ash_knot=flask x");
        soul.charm = Charm.ASH_KNOT.id;
        gifts.append(rounded(Charm.ASH_KNOT.more(player)));
        soul.charm = "";
        say(gifts + (each ? "" : " FAILED"));
    }

    private static float regenIn(FakePlayer player, ArmorSet set) {
        wear(player, set.piece(0), set.piece(1), set.piece(2), set.piece(3), null, null);
        return Gear.staminaRegen(player);
    }

    private static float spellsIn(FakePlayer player) {
        ArmorSet set = ArmorSet.CHAPLAIN;
        wear(player, set.piece(0), set.piece(1), set.piece(2), set.piece(3), null, null);
        return Gear.spellBonus(player);
    }

    private static float blockIn(FakePlayer player) {
        ArmorSet set = ArmorSet.GUARD;
        wear(player, set.piece(0), set.piece(1), set.piece(2), set.piece(3), null, null);
        return Gear.blockCost(player);
    }

    private static String rounded(float value) {
        return String.valueOf(Math.round(value * 100.0F) / 100.0F);
    }

    // ------------------------------------------------------------------ recipes and loot

    private static void data(ServerLevel level) {
        long recipes = level.getServer().getRecipeManager().getRecipes().stream().filter(holder -> holder.id().identifier().getNamespace().equals(Dushi.MODID)).count();
        say("data: recipes of the mod loaded: " + recipes);
        for (String name : new String[] {"chests/stockade_gate", "chests/stockade_cell", "chests/stockade_hall", "chests/stockade_chapel",
                "chests/stockade_treasure", "chests/outpost", "blocks/hearth"}) {
            ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, Dushi.id(name));
            LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
            if (table == LootTable.EMPTY) {
                say("data: loot " + name + ": MISSING");
                continue;
            }
            // three rolls, so that counts above one and the rarer entries have a chance to show
            Map<String, Integer> got = new TreeMap<>();
            for (int roll = 0; roll < 3; roll++) {
                for (ItemStack stack : table.getRandomItems(new LootParams.Builder(level).create(LootContextParamSets.EMPTY), 1000L + roll)) {
                    got.merge(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath(), stack.getCount(), Integer::sum);
                }
            }
            say("data: loot " + name + " x3: " + got);
        }
    }

    // ------------------------------------------------------------------ a building set down from its plan

    private static BoundingBox everything() {
        return new BoundingBox(-30000000, -2048, -30000000, 30000000, 2048, 30000000);
    }

    private static void built(ServerLevel level, String name, BlockPos origin, Rotation rotation) {
        PlanPiece piece = new PlanPiece(name, origin, rotation);
        piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), level.getRandom(), everything(), new ChunkPos(0, 0), origin);
        say("world: " + name + " built at " + origin.toShortString() + " turned " + rotation.getSerializedName() + ": " + census(level, piece.getBoundingBox()));
    }

    /** What stands inside a box, as far as the mod cares. */
    private static String census(ServerLevel level, BoundingBox box) {
        Map<String, Integer> marks = new TreeMap<>();
        int hearths = 0;
        int fog = 0;
        int chests = 0;
        int chestsWithLoot = 0;
        int bars = 0;
        int barsJoined = 0;
        int solid = 0;
        for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY() - 1, box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            solid++;
            if (state.is(Dushi.MOB_MARK.get())) {
                marks.merge(state.getValue(MobMarkBlock.MOB).id, 1, Integer::sum);
            } else if (state.is(Dushi.HEARTH.get())) {
                hearths++;
            } else if (state.is(Dushi.FOG_GATE.get())) {
                fog++;
            } else if (state.is(Blocks.CHEST)) {
                chests++;
                if (level.getBlockEntity(pos) instanceof RandomizableContainer container && container.getLootTable() != null) {
                    chestsWithLoot++;
                }
            } else if (state.is(Blocks.IRON_BARS)) {
                bars++;
                if (state.getValue(IronBarsBlock.NORTH) || state.getValue(IronBarsBlock.SOUTH) || state.getValue(IronBarsBlock.EAST) || state.getValue(IronBarsBlock.WEST)) {
                    barsJoined++;
                }
            }
        }
        return "blocks=" + solid + " marks=" + marks + " hearths=" + hearths + " fog=" + fog + " chests=" + chestsWithLoot + "/" + chests + " with loot, bars joined="
                + barsJoined + "/" + bars;
    }

    // ------------------------------------------------------------------ the marks

    private static List<MobMarkBlockEntity> marksAround(ServerLevel level, BlockPos centre) {
        List<MobMarkBlockEntity> marks = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-40, -8, -40), centre.offset(40, 20, 40))) {
            if (level.getBlockEntity(pos) instanceof MobMarkBlockEntity mark) {
                marks.add(mark);
            }
        }
        return marks;
    }

    private static MobMarkBlockEntity markOf(List<MobMarkBlockEntity> marks, MobMarkBlock.Kind kind) {
        for (MobMarkBlockEntity mark : marks) {
            if (mark.getBlockState().getValue(MobMarkBlock.MOB) == kind) {
                return mark;
            }
        }
        throw new IllegalStateException("no mark for " + kind.id);
    }

    private static Map<String, Integer> living(ServerLevel level, BlockPos centre) {
        Map<String, Integer> count = new TreeMap<>();
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(centre).inflate(48.0, 30.0, 48.0), LivingEntity::isAlive)) {
            if (MobMarkBlockEntity.homeOf(entity) != null) {
                count.merge(EntityType.getKey(entity.getType()).getPath(), 1, Integer::sum);
            }
        }
        return count;
    }

    private static void slay(ServerLevel level, MobMarkBlockEntity mark) {
        Entity entity = level.getEntity(mark.mob());
        if (entity instanceof LivingEntity living) {
            living.hurtServer(level, level.damageSources().genericKill(), 100000.0F);
        }
    }

    private static void marks(ServerLevel level, BlockPos centre) {
        List<MobMarkBlockEntity> marks = marksAround(level, centre);
        int woke = 0;
        for (MobMarkBlockEntity mark : marks) {
            if (mark.wake(level)) {
                woke++;
            }
        }
        say("marks: " + marks.size() + " marks, " + woke + " woke: " + living(level, centre));

        // An ordinary mob: killed, its mark knows; a rest brings it back. A leftover of an earlier life goes by itself.
        MobMarkBlockEntity cell = markOf(marks, MobMarkBlock.Kind.PRISONER);
        slay(level, cell);
        boolean knew = cell.dead();
        // A boss: killed, stays dead through a rest, and the fog in its door lifts.
        MobMarkBlockEntity hall = markOf(marks, MobMarkBlock.Kind.OVERSEER);
        int fogBefore = count(level, centre, Dushi.FOG_GATE.get().defaultBlockState());
        slay(level, hall);
        int fogAfter = count(level, centre, Dushi.FOG_GATE.get().defaultBlockState());
        // The other boss is hurt and lured away, and must be back in its place and whole after the rest.
        MobMarkBlockEntity chapel = markOf(marks, MobMarkBlock.Kind.CHAPLAIN);
        SoulsMob chaplain = (SoulsMob) level.getEntity(chapel.mob());
        chaplain.setHealth(chaplain.getMaxHealth() * 0.5F);
        chaplain.teleportTo(chaplain.getX() + 6.0, chaplain.getY(), chaplain.getZ() + 12.0);

        int reset = MobMarkBlockEntity.resetAround(level, centre, MobMarkBlockEntity.RESET_RADIUS);
        boolean cleared = !cell.dead() && cell.mob() == null;
        int rose = 0;
        for (MobMarkBlockEntity mark : marks) {
            if (mark.wake(level)) {
                rose++;
            }
        }
        say("marks: prisoner killed, mark knew=" + knew + "; rest reset " + reset + " marks, cleared=" + cleared + ", rose again " + rose
                + "; overseer stays dead=" + hall.dead() + ", fog " + fogBefore + " -> " + fogAfter + "; chaplain back home="
                + (chaplain.blockPosition().distManhattan(chapel.getBlockPos()) <= 1) + " health " + Math.round(chaplain.getHealth()) + "/"
                + Math.round(chaplain.getMaxHealth()) + "; living now " + living(level, centre));
    }

    private static int count(ServerLevel level, BlockPos centre, BlockState like) {
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-40, -8, -40), centre.offset(40, 20, 40))) {
            if (level.getBlockState(pos).is(like.getBlock())) {
                count++;
            }
        }
        return count;
    }

    // ------------------------------------------------------------------ the buildings in the world as generated

    private static void found(ServerLevel level, String name) {
        Holder<Structure> structure = level.registryAccess().lookupOrThrow(Registries.STRUCTURE)
                .getOrThrow(ResourceKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(Dushi.MODID, name)));
        Pair<BlockPos, Holder<Structure>> nearest = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(structure),
                BlockPos.ZERO, 100, false);
        if (nearest == null) {
            say("world: " + name + " in the wild: none within 100 chunks of the origin");
            return;
        }
        BlockPos at = nearest.getFirst();
        ChunkAccess chunk = level.getChunk(at.getX() >> 4, at.getZ() >> 4);
        StructureStart start = level.structureManager().getStartForStructure(structure.value(), chunk);
        if (start == null || !start.isValid()) {
            say("world: " + name + " in the wild: located at " + at.toShortString() + " but no start there");
            return;
        }
        BoundingBox box = start.getBoundingBox();
        for (int cx = box.minX() >> 4; cx <= box.maxX() >> 4; cx++) {
            for (int cz = box.minZ() >> 4; cz <= box.maxZ() >> 4; cz++) {
                level.getChunk(cx, cz);
            }
        }
        PlanPiece piece = (PlanPiece) start.getPieces().get(0);
        Plan plan = Plan.get(name);
        // how the land lies round the walls: the height of the ground just outside each corner, against the floor
        StringBuilder land = new StringBuilder();
        for (int[] corner : new int[][] {{-2, -2}, {plan.width + 1, -2}, {-2, plan.depth + 1}, {plan.width + 1, plan.depth + 1}}) {
            BlockPos outside = PlanPiece.toWorld(plan, piece.origin(), piece.rotation(), corner[0], plan.originY, corner[1]);
            land.append(' ').append(level.getHeight(Heightmap.Types.WORLD_SURFACE, outside.getX(), outside.getZ()) - piece.origin().getY());
        }
        say("world: " + name + " in the wild: " + Math.round(Math.sqrt(at.distSqr(BlockPos.ZERO))) + " blocks from the origin, floor at y="
                + piece.origin().getY() + " at " + piece.origin().toShortString() + " turned " + piece.rotation().getSerializedName() + ", ground outside the corners"
                + land + " above the floor; " + census(level, box));
    }
}
