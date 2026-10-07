package com.keykeeper.dushi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/**
 * What can be made at the old anvil: a plain list, each thing with what it takes - materials the dead leave behind and souls.
 * The same list on both sides; a job is named by its index. This is how gear is made where there is no crafting table and
 * nothing to put on one (an adventure map); in an ordinary world the crafting recipes work as well.
 */
public final class Forge {
    public record Part(Supplier<? extends ItemLike> item, int count) {}

    /** The shelves of the anvil's list. */
    public enum Category {
        WEAPONS("weapons"), SHIELDS("shields"), ARMOUR("armour"), MAGIC("magic"), SUPPLIES("supplies");

        public final String id;

        Category(String id) {
            this.id = id;
        }
    }

    public record Recipe(Supplier<ItemStack> result, List<Part> parts, int souls, Category category) {}

    /** The shelf the recipes being listed go on. */
    private static Category shelf = Category.WEAPONS;

    private static List<Recipe> recipes;

    private Forge() {}

    private static Part cloth(int count) {
        return new Part(Dushi.TATTERED_CLOTH, count);
    }

    private static Part scrap(int count) {
        return new Part(Dushi.RUST_SCRAP, count);
    }

    private static void add(List<Recipe> list, Supplier<? extends ItemLike> item, int count, int souls, Part... parts) {
        list.add(new Recipe(() -> new ItemStack(item.get(), count), List.of(parts), souls, shelf));
    }

    private static void add(List<Recipe> list, Supplier<? extends ItemLike> item, int souls, Part... parts) {
        add(list, item, 1, souls, parts);
    }

    public static List<Recipe> recipes() {
        if (recipes == null) {
            List<Recipe> list = new ArrayList<>();
            shelf = Category.WEAPONS;
            // blades and blunt things, from the cheapest up
            add(list, Dushi.SHIV, 0, scrap(1));
            add(list, Dushi.DAGGER, 100, scrap(2));
            add(list, Dushi.SHORTSWORD, 150, scrap(3));
            add(list, Dushi.PITCHFORK, 150, scrap(2), cloth(1));
            add(list, Dushi.HATCHET, 200, scrap(3), cloth(1));
            add(list, Dushi.CLEAVER, 350, scrap(4));
            add(list, Dushi.LONGSWORD, 400, scrap(5));
            add(list, Dushi.SABRE, 500, scrap(5), cloth(1));
            add(list, Dushi.BROADSWORD, 500, scrap(7));
            add(list, Dushi.FLANGED_MACE, 500, scrap(7));
            add(list, Dushi.BATTLE_AXE, 600, scrap(8));
            add(list, Dushi.RUST_GREATSWORD, 0, scrap(12), new Part(Dushi.GREAT_SOUL, 1));
            // what the lords of the stockade leave can be beaten into what they carried
            add(list, Dushi.GRAVE_SPADE, 0, new Part(Dushi.GRAVEDIGGER_SOUL, 1), scrap(4));
            add(list, Dushi.GREAT_KEY, 0, new Part(Dushi.OVERSEER_SOUL, 1), scrap(6));
            add(list, Dushi.CENSER_FLAIL, 0, new Part(Dushi.CHAPLAIN_SOUL, 1), scrap(6));
            // the quarry's iron
            add(list, Dushi.QUARRY_PICK, 600, new Part(Dushi.IRON_WEDGE, 3), scrap(3));
            add(list, Dushi.STONE_HAMMER, 600, new Part(Dushi.IRON_WEDGE, 2), scrap(4));
            add(list, Dushi.GREAT_MAUL, 0, new Part(Dushi.TASKMASTER_SOUL, 1), new Part(Dushi.IRON_WEDGE, 6));
            add(list, Dushi.EATER_CLAW, 0, new Part(Dushi.STONE_EATER_SOUL, 1), new Part(Dushi.IRON_WEDGE, 2));
            // what the Mire grows
            add(list, Dushi.FISH_KNIFE, 600, new Part(Dushi.BONE_SCALE, 2), scrap(2));
            add(list, Dushi.BOAT_HOOK, 700, new Part(Dushi.WITHE, 3), scrap(3));
            add(list, Dushi.WALLOWER_FANG, 0, new Part(Dushi.WALLOWER_SOUL, 1), new Part(Dushi.BONE_SCALE, 4));
            add(list, Dushi.WITHE_SCOURGE, 0, new Part(Dushi.MOTHER_SOUL, 1), new Part(Dushi.WITHE, 6));
            // what the Unsleeping kept themselves awake with, and their bear
            add(list, Dushi.VIGIL_STAFF, 800, new Part(Dushi.LINEN, 3), new Part(Dushi.WAX, 2));
            add(list, Dushi.WAKER_MALLET, 800, new Part(Dushi.LINEN, 2), scrap(4));
            add(list, Dushi.BEAR_SPEAR, 0, new Part(Dushi.SHATUN_SOUL, 1), new Part(Dushi.SHATUN_HIDE, 2));
            add(list, Dushi.GREAT_CANDLE, 0, new Part(Dushi.ELDER_SOUL, 1), new Part(Dushi.WAX, 6));
            shelf = Category.SUPPLIES;
            add(list, () -> Items.ARROW, 8, 0, scrap(1));
            // shields
            shelf = Category.SHIELDS;
            add(list, Dushi.ROUND_SHIELD, 100, scrap(2), cloth(2));
            add(list, Dushi.GUARD_SHIELD, 400, scrap(6));
            add(list, Dushi.TOWER_SHIELD, 0, scrap(12), new Part(Dushi.GREAT_SOUL, 1));
            add(list, Dushi.CARVER_BUCKLER, 500, new Part(Dushi.IRON_WEDGE, 2), scrap(2));
            add(list, Dushi.SCALE_SHIELD, 700, new Part(Dushi.BONE_SCALE, 5), new Part(Dushi.WITHE, 2));
            add(list, Dushi.WAKER_BOARD, 900, new Part(Dushi.LINEN, 2), new Part(Dushi.WAX, 2));
            shelf = Category.ARMOUR;
            // armour: rags of cloth, mail of scrap, plate hammered over the mail, vestments of cloth and a soul
            int[] shape = {2, 4, 3, 2};
            for (int piece = 0; piece < 4; piece++) {
                add(list, ArmorSet.PRISONER.pieces().get(piece), 0, cloth(shape[piece]));
            }
            for (int piece = 0; piece < 4; piece++) {
                add(list, ArmorSet.GUARD.pieces().get(piece), 100 * shape[piece], scrap(shape[piece] + 1));
            }
            for (int piece = 0; piece < 4; piece++) {
                add(list, ArmorSet.RUST.pieces().get(piece), 150 * shape[piece], new Part(ArmorSet.GUARD.pieces().get(piece), 1), scrap(shape[piece] + 2));
            }
            for (int piece = 0; piece < 4; piece++) {
                add(list, ArmorSet.CHAPLAIN.pieces().get(piece), 150 * shape[piece], cloth(shape[piece] + 2), new Part(Dushi.NAMELESS_SOUL, 1));
            }
            // a stonecutter's felt and leather: cloth, and the iron of his trade for the caps and the nails
            for (int piece = 0; piece < 4; piece++) {
                add(list, ArmorSet.MASON.pieces().get(piece), 200 * shape[piece], cloth(shape[piece] + 2), new Part(Dushi.IRON_WEDGE, 1));
            }
            // the fishers' coat: the pike's scales, bound with withe
            for (int piece = 0; piece < 4; piece++) {
                add(list, ArmorSet.SCALE.pieces().get(piece), 220 * shape[piece], new Part(Dushi.BONE_SCALE, shape[piece] + 1), new Part(Dushi.WITHE, 1));
            }
            // the white linen of the Unsleeping, stiffened with their wax
            for (int piece = 0; piece < 4; piece++) {
                add(list, ArmorSet.VIGIL.pieces().get(piece), 250 * shape[piece], new Part(Dushi.LINEN, shape[piece] + 2), new Part(Dushi.WAX, 1));
            }
            shelf = Category.MAGIC;
            // catalysts and spells: written with a soul
            add(list, Dushi.SCHOLAR_STAFF, 300, cloth(2), scrap(1));
            add(list, Dushi.PRAYER_BEADS, 300, cloth(2), new Part(Dushi.NAMELESS_SOUL, 1));
            add(list, Dushi.EMBER_GLOVE, 300, cloth(3));
            add(list, Dushi.SPELLS.get(Spell.HEAVY_SOUL_SHARD), 500, new Part(Dushi.GREAT_SOUL, 1));
            add(list, Dushi.SPELLS.get(Spell.REBUKE), 300, new Part(Dushi.NAMELESS_SOUL, 2));
            add(list, Dushi.SPELLS.get(Spell.FIRE_ORB), 500, new Part(Dushi.GREAT_SOUL, 1));
            // what the later places taught
            add(list, Dushi.SPELLS.get(Spell.SOUL_SCATTER), 800, new Part(Dushi.GREAT_SOUL, 1), new Part(Dushi.IRON_WEDGE, 1));
            add(list, Dushi.SPELLS.get(Spell.ROOT_SNARE), 900, new Part(Dushi.WITHE, 4), new Part(Dushi.NAMELESS_SOUL, 2));
            add(list, Dushi.SPELLS.get(Spell.MARSH_FIRE), 900, new Part(Dushi.WITHE, 2), new Part(Dushi.GREAT_SOUL, 1));
            add(list, Dushi.SPELLS.get(Spell.HOT_WAX), 1000, new Part(Dushi.WAX, 4), new Part(Dushi.GREAT_SOUL, 1));
            shelf = Category.SUPPLIES;
            add(list, Dushi.TAR_POT, 2, 50, cloth(1));
            add(list, Dushi.POWDER_POT, 2, 150, cloth(1), new Part(Dushi.IRON_WEDGE, 1));
            add(list, Dushi.RETURN_EMBER, 2, 0, new Part(Dushi.NAMELESS_SOUL, 1));
            recipes = List.copyOf(list);
        }
        return recipes;
    }

    /** How many of {@code item} the player carries. */
    public static int count(Player player, Item item) {
        Inventory inventory = player.getInventory();
        int count = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static boolean canMake(Player player, Recipe recipe) {
        if (player.isCreative()) {
            return true;
        }
        if (PlayerSoul.of(player).souls < recipe.souls()) {
            return false;
        }
        for (Part part : recipe.parts()) {
            if (count(player, part.item().get().asItem()) < part.count()) {
                return false;
            }
        }
        return true;
    }

    /** Takes {@code count} of {@code item} out of the player's inventory. */
    static void take(Player player, Item item, int count) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize() && count > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) {
                int taken = Math.min(count, stack.getCount());
                stack.shrink(taken);
                count -= taken;
            }
        }
    }
}
