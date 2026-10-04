package com.keykeeper.dushi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/** What the blind keykeeper sells for souls. The same list on both sides; a purchase is named by its index. */
public final class Shop {
    public record Ware(Supplier<ItemStack> stack, int price) {}

    private static List<Ware> wares;

    private Shop() {}

    public static List<Ware> wares() {
        if (wares == null) {
            List<Ware> list = new ArrayList<>();
            // Only what a newcomer needs to start with. Better arms are taken from the dead, found, or made at an anvil.
            add(list, Dushi.CLUB, 1, 150);
            add(list, Dushi.SHORTSWORD, 1, 300);
            add(list, Dushi.SHORT_BOW, 1, 500);
            add(list, () -> Items.ARROW, 16, 100);
            add(list, Dushi.PLANK_SHIELD, 1, 200);
            add(list, Dushi.ROUND_SHIELD, 1, 500);
            int[] rags = {100, 150, 120, 80};
            for (int piece = 0; piece < 4; piece++) {
                add(list, ArmorSet.PRISONER.pieces().get(piece), 1, rags[piece]);
            }
            add(list, Dushi.SCHOLAR_STAFF, 1, 500);
            add(list, Dushi.SPELLS.get(Spell.SOUL_SHARD), 1, 600);
            add(list, Dushi.PRAYER_BEADS, 1, 500);
            add(list, Dushi.SPELLS.get(Spell.MEND), 1, 1000);
            add(list, Dushi.EMBER_GLOVE, 1, 500);
            add(list, Dushi.SPELLS.get(Spell.FLARE), 1, 700);
            add(list, Dushi.TAR_POT, 3, 150);
            add(list, Dushi.RETURN_EMBER, 1, 200);
            add(list, Dushi.TATTERED_CLOTH, 2, 120);
            add(list, Dushi.RUST_SCRAP, 2, 200);
            wares = List.copyOf(list);
        }
        return wares;
    }

    private static void add(List<Ware> list, Supplier<? extends ItemLike> item, int count, int price) {
        list.add(new Ware(() -> new ItemStack(item.get(), count), price));
    }
}
