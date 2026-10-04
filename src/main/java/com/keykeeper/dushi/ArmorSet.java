package com.keykeeper.dushi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The four suits of armour of the mod. Besides protection every suit has a weight (see {@link Gear}) and a small gift of its
 * own that grows with every piece worn.
 */
public enum ArmorSet {
    /** Rags: almost no protection, almost no weight, stamina comes back faster. */
    PRISONER("prisoner", new String[] {"prisoner_hood", "prisoner_rags", "prisoner_breeches", "prisoner_wraps"}, 8.0F, 0.05F,
            6, new int[] {1, 2, 1, 1}, 0.0F, 0.0F, SoundEvents.ARMOR_EQUIP_LEATHER, "repairs_cloth", Rarity.COMMON),
    /** Mail and a kettle hat: middling in everything, a blocked blow costs less stamina. */
    GUARD("guard", new String[] {"guard_hat", "guard_hauberk", "guard_leggings", "guard_boots"}, 26.0F, 0.06F,
            15, new int[] {2, 5, 4, 1}, 0.0F, 0.0F, SoundEvents.ARMOR_EQUIP_CHAIN, "repairs_scrap", Rarity.COMMON),
    /** Rusted plate: the best protection here, hard to knock back, and very heavy. */
    RUST("rust", new String[] {"rust_helm", "rust_cuirass", "rust_tassets", "rust_sabatons"}, 46.0F, 0.0F,
            28, new int[] {3, 7, 5, 3}, 1.0F, 0.1F, SoundEvents.ARMOR_EQUIP_IRON, "repairs_scrap", Rarity.UNCOMMON),
    /** Vestments: light, little protection, every spell is stronger. */
    CHAPLAIN("chaplain", new String[] {"chaplain_mitre", "chaplain_cassock", "chaplain_skirt", "chaplain_shoes"}, 9.0F, 0.06F,
            12, new int[] {1, 3, 2, 1}, 0.0F, 0.0F, SoundEvents.ARMOR_EQUIP_LEATHER, "repairs_cloth", Rarity.UNCOMMON);

    public static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final ArmorType[] TYPES = {ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS};

    public final String id;
    public final String[] names;
    /** Weight of the whole suit. */
    public final float weight;
    /** The suit's gift per piece worn, as a share (0.05 is 5%); what it applies to depends on the suit. */
    public final float bonus;
    public final ArmorMaterial material;
    private final Rarity rarity;
    private final List<DeferredItem<Item>> pieces = new ArrayList<>();

    ArmorSet(String id, String[] names, float weight, float bonus, int durability, int[] defense, float toughness, float knockbackResistance,
            Holder<SoundEvent> sound, String repairTag, Rarity rarity) {
        this.id = id;
        this.names = names;
        this.weight = weight;
        this.bonus = bonus;
        this.rarity = rarity;
        this.material = new ArmorMaterial(durability,
                Map.of(ArmorType.HELMET, defense[0], ArmorType.CHESTPLATE, defense[1], ArmorType.LEGGINGS, defense[2], ArmorType.BOOTS, defense[3],
                        ArmorType.BODY, defense[1]),
                12, sound, toughness, knockbackResistance, TagKey.create(Registries.ITEM, Dushi.id(repairTag)),
                ResourceKey.create(EquipmentAssets.ROOT_ID, Dushi.id(id)));
    }

    /** Called once by {@link Dushi} while it fills its item register. */
    void register(DeferredRegister.Items items) {
        for (int i = 0; i < 4; i++) {
            ArmorType type = TYPES[i];
            this.pieces.add(items.registerItem(this.names[i], props -> new Item(props.humanoidArmor(this.material, type).rarity(this.rarity))));
        }
    }

    public List<DeferredItem<Item>> pieces() {
        return this.pieces;
    }

    /** 0 helmet, 1 chest, 2 legs, 3 boots. */
    public Item piece(int index) {
        return this.pieces.get(index).get();
    }

    /** The suit {@code item} belongs to, or null. */
    public static ArmorSet of(Item item) {
        for (ArmorSet set : values()) {
            for (DeferredItem<Item> piece : set.pieces) {
                if (piece.get() == item) {
                    return set;
                }
            }
        }
        return null;
    }
}
