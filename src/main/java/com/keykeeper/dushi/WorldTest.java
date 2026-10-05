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
        // the hour of a place: only where time stands still
        long was = level.getDefaultClockTime();
        SoulMechanics.hour(level, 18000);
        boolean untouched = level.getDefaultClockTime() == was;
        level.getGameRules().set(net.minecraft.world.level.gamerules.GameRules.ADVANCE_TIME, false, level.getServer());
        SoulMechanics.hour(level, 18000);
        boolean set = Math.floorMod(level.getDefaultClockTime(), 24000L) == 18000L && SoulMechanics.hourNow(level) == 18000;
        level.getGameRules().set(net.minecraft.world.level.gamerules.GameRules.ADVANCE_TIME, true, level.getServer());
        say("rules: the hour of a place: left alone where time moves=" + untouched + ", set where it stands=" + set + (untouched && set ? "" : " FAILED"));
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
