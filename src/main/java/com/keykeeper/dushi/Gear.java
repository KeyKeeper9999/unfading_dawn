package com.keykeeper.dushi;

import java.util.HashMap;
import java.util.Map;

import com.keykeeper.dushi.item.GuardShieldItem;
import com.keykeeper.dushi.item.WeaponItem;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * What the rules of the mod know about a piece of gear, whoever made it: the game's own swords, axes and armour get the same
 * kind of numbers as the mod's (stamina per swing, scaling with strength and dexterity, a blow in the back, weight), so that
 * everything works together.
 */
public final class Gear {
    private Gear() {}

    /** How a melee weapon behaves under the mod's rules. */
    public record Weapon(float stamina, float strength, float dexterity, float backstab) {}

    private static final Weapon SWORD = new Weapon(12.0F, 0.5F, 0.5F, 1.5F);
    private static final Weapon AXE = new Weapon(18.0F, 0.9F, 0.1F, 1.3F);
    private static final Weapon PICKAXE = new Weapon(14.0F, 0.7F, 0.1F, 1.2F);
    private static final Weapon SHOVEL = new Weapon(11.0F, 0.6F, 0.2F, 1.2F);
    private static final Weapon HOE = new Weapon(8.0F, 0.2F, 0.7F, 1.5F);
    private static final Weapon SPEAR = new Weapon(14.0F, 0.3F, 0.8F, 1.4F);
    private static final Weapon MACE = new Weapon(24.0F, 1.0F, 0.0F, 1.2F);
    private static final Weapon TRIDENT = new Weapon(16.0F, 0.4F, 0.6F, 1.5F);

    /** Extra arrow damage per point of dexterity, for any bow or crossbow. */
    public static final float ARROW_DEXTERITY = 0.03F;
    /** The grade shown on bows: it stands for {@link #ARROW_DEXTERITY}. */
    public static final float BOW_SCALING = 0.6F;
    /** Stability of a shield the mod does not know. */
    public static final float DEFAULT_STABILITY = 0.5F;

    /** The stats of the weapon in {@code stack}, or null if it is not one. */
    public static Weapon weapon(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        if (stack.getItem() instanceof WeaponItem weapon) {
            return new Weapon(weapon.staminaCost, weapon.strengthScaling, weapon.dexterityScaling, weapon.backstab);
        }
        if (stack.is(ItemTags.SWORDS)) {
            return SWORD;
        }
        if (stack.is(ItemTags.AXES)) {
            return AXE;
        }
        if (stack.is(ItemTags.SPEARS)) {
            return SPEAR;
        }
        if (stack.is(ItemTags.PICKAXES)) {
            return PICKAXE;
        }
        if (stack.is(ItemTags.SHOVELS)) {
            return SHOVEL;
        }
        if (stack.is(ItemTags.HOES)) {
            return HOE;
        }
        if (stack.is(Items.MACE)) {
            return MACE;
        }
        if (stack.is(Items.TRIDENT)) {
            return TRIDENT;
        }
        return null;
    }

    public static boolean isBow(ItemStack stack) {
        return stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
    }

    /** Share of a blocked blow that costs no stamina, or a negative number if {@code stack} is not a shield. */
    public static float stability(ItemStack stack) {
        if (stack.getItem() instanceof GuardShieldItem shield) {
            return shield.stability;
        }
        return stack.has(DataComponents.BLOCKS_ATTACKS) ? DEFAULT_STABILITY : -1.0F;
    }

    // ------------------------------------------------------------------ weight

    private static Map<Item, Float> armourWeights;

    /** A whole suit weighs {@code total}; the pieces share it the way they share the body. */
    private static void suit(Map<Item, Float> map, float total, Item helmet, Item chestplate, Item leggings, Item boots) {
        map.put(helmet, total * 0.2F);
        map.put(chestplate, total * 0.4F);
        map.put(leggings, total * 0.25F);
        map.put(boots, total * 0.15F);
    }

    private static Map<Item, Float> armourWeights() {
        if (armourWeights == null) {
            Map<Item, Float> map = new HashMap<>();
            suit(map, 10.0F, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS);
            suit(map, 24.0F, Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS);
            suit(map, 28.0F, Items.COPPER_HELMET, Items.COPPER_CHESTPLATE, Items.COPPER_LEGGINGS, Items.COPPER_BOOTS);
            suit(map, 34.0F, Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS);
            suit(map, 36.0F, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS);
            suit(map, 44.0F, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);
            suit(map, 54.0F, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);
            map.put(Items.TURTLE_HELMET, 3.0F);
            for (ArmorSet set : ArmorSet.values()) {
                suit(map, set.weight, set.piece(0), set.piece(1), set.piece(2), set.piece(3));
            }
            armourWeights = map;
        }
        return armourWeights;
    }

    /** Weight of one item when worn or held; 0 for things that do not burden. */
    public static float weight(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0F;
        }
        Float armour = armourWeights().get(stack.getItem());
        if (armour != null) {
            return armour;
        }
        Weapon weapon = weapon(stack);
        if (weapon != null) {
            return Math.round(weapon.stamina * 3.0F) / 10.0F;
        }
        if (stack.getItem() instanceof GuardShieldItem shield && shield.weight >= 0.0F) {
            return shield.weight;
        }
        float stability = stability(stack);
        if (stability >= 0.0F) {
            return 2.0F + Math.round(stability * 100.0F) / 10.0F;
        }
        return isBow(stack) ? 2.0F : 0.0F;
    }

    /** Everything worn and held. */
    public static float load(Player player) {
        float load = 0.0F;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR || slot.getType() == EquipmentSlot.Type.HAND) {
                load += weight(player.getItemBySlot(slot));
            }
        }
        return load;
    }

    public static float maxLoad(PlayerSoul soul) {
        return 70.0F + 2.5F * soul.stats[PlayerSoul.ENDURANCE] + (Charm.FETTER_LINK.id.equals(soul.charm) ? Charm.FETTER_LINK.amount : 0.0F);
    }

    /** How the load carried changes the roll and the recovery of stamina. */
    public enum Burden {
        /** Under 30% of what the player can carry. */
        LIGHT("light", 1.18F, 9, 16.0F, 14, 1.1F),
        /** Under 70%. */
        MEDIUM("medium", 1.0F, 8, 20.0F, 16, 1.0F),
        /** Up to 100%. */
        HEAVY("heavy", 0.72F, 5, 26.0F, 22, 0.85F),
        /** More than can be carried: no roll at all, and a slow walk. */
        OVER("over", 0.0F, 0, 0.0F, 0, 0.7F);

        public final String id;
        /** Multiplier of the distance a roll covers. */
        public final float rollSpeed;
        /** Ticks of the roll during which blows pass through. */
        public final int safeTicks;
        public final float rollCost;
        public final int rollCooldown;
        public final float staminaRegen;

        Burden(String id, float rollSpeed, int safeTicks, float rollCost, int rollCooldown, float staminaRegen) {
            this.id = id;
            this.rollSpeed = rollSpeed;
            this.safeTicks = safeTicks;
            this.rollCost = rollCost;
            this.rollCooldown = rollCooldown;
            this.staminaRegen = staminaRegen;
        }

        public boolean canRoll() {
            return this != OVER;
        }
    }

    public static Burden burden(Player player) {
        float share = load(player) / maxLoad(PlayerSoul.of(player));
        if (share > 1.0F) {
            return Burden.OVER;
        }
        return share > 0.7F ? Burden.HEAVY : share > 0.3F ? Burden.MEDIUM : Burden.LIGHT;
    }

    // ------------------------------------------------------------------ what the mod's own armour adds

    /** Pieces of {@code set} the player wears. */
    public static int worn(Player player, ArmorSet set) {
        int count = 0;
        for (int piece = 0; piece < 4; piece++) {
            if (player.getItemBySlot(ArmorSet.SLOTS[piece]).is(set.piece(piece))) {
                count++;
            }
        }
        return count;
    }

    /** Stamina recovery: the burden, the prisoner's rags that weigh next to nothing, and the weeping herb. */
    public static float staminaRegen(Player player) {
        return burden(player).staminaRegen * (1.0F + ArmorSet.PRISONER.bonus * worn(player, ArmorSet.PRISONER)) * Charm.WEEPING_HERB.more(player);
    }

    /** The chaplain's vestments strengthen every spell; so does the stub of a candle from his cathedral. */
    public static float spellBonus(Player player) {
        return (1.0F + ArmorSet.CHAPLAIN.bonus * worn(player, ArmorSet.CHAPLAIN)) * Charm.CANDLE_STUB.more(player);
    }

    /** The guard's armour makes a blocked blow cheaper; so does a quarryman's wedge on a cord. */
    public static float blockCost(Player player) {
        return (1.0F - ArmorSet.GUARD.bonus * worn(player, ArmorSet.GUARD)) * Charm.WEDGE_CORD.less(player);
    }

    /** Whether holding this shield up slows the return of stamina: every shield but the carver's buckler. */
    public static boolean tiresTheArm(ItemStack shield) {
        return shield == null || !shield.is(Dushi.CARVER_BUCKLER.get());
    }

    /** How hard a waker's board throws back whoever struck it. */
    public static final double BOARD_THROWS = 0.9;

    /** The share of a blast, or of rock coming down, that reaches somebody in a stonecutter's felt and leather. */
    public static float blastShare(Player player) {
        return 1.0F - ArmorSet.MASON.bonus * worn(player, ArmorSet.MASON);
    }

    /**
     * How much of the water's hold the scales of the Mire's pike take off the wearer's legs (the game's own measure: at 1
     * water is walked through as if it were not there).
     */
    public static float wading(Player player) {
        return ArmorSet.SCALE.bonus * worn(player, ArmorSet.SCALE);
    }

    /** The share of a throw that still moves somebody in a seaman's oilskins: they were made to stand on a wet deck in. */
    public static float thrown(LivingEntity victim) {
        return victim instanceof Player player ? 1.0F - ArmorSet.OILSKIN.bonus * worn(player, ArmorSet.OILSKIN) : 1.0F;
    }

    /** The share of a fire's hurt that reaches somebody in a reaper's harvest clothes. */
    public static float fireShare(Player player) {
        return 1.0F - ArmorSet.REAPER.bonus * worn(player, ArmorSet.REAPER);
    }

    /** Behind the door of a kiln, held up, a fire does this much of what it would. */
    public static final float DOOR_FIRE = 0.25F;

    /** For how many seconds the Warden's pole sets alight what it strikes. */
    public static final float POLE_BURNS = 5.0F;

    /** What a swing costs: the weapon's own, less what a seaman's knot takes off. */
    public static float swingCost(Player player, float cost) {
        return cost * Charm.SEA_KNOT.less(player);
    }

    /** For how many seconds the Elder's great candle sets alight what it strikes. */
    public static final float CANDLE_BURNS = 4.0F;

    /**
     * Sleep as the Firwood gives it: the dark over the eyes and the weight in the legs, for {@code ticks}. Every piece of the
     * linen of the Unsleeping takes a share off the time; nothing under half a second is felt at all.
     *
     * @param weight how heavy the legs are: the level of slowness, from 0
     */
    public static void drowse(LivingEntity victim, LivingEntity from, int ticks, int weight) {
        if (victim instanceof Player player) {
            ticks = Math.round(ticks * (1.0F - ArmorSet.VIGIL.bonus * worn(player, ArmorSet.VIGIL)));
        }
        if (ticks < 10) {
            return;
        }
        victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DARKNESS, ticks, 0, false, false, true), from);
        victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOWNESS, ticks, weight, false, false, true), from);
    }

    /** What a roll costs: the burden decides, and the wax of the vigil takes a share off. */
    public static float rollCost(Player player, Burden burden) {
        return burden.rollCost * Charm.VIGIL_WAX.less(player);
    }

    /** A blow in the back, as the weapon makes it and a pike's tooth sharpens it. */
    public static float backstab(Player player, Weapon weapon) {
        return weapon.backstab() * Charm.PIKE_TOOTH.more(player);
    }
}
