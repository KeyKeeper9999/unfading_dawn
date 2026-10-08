package com.keykeeper.dushi.client;

import java.util.List;

import com.keykeeper.dushi.ArmorSet;
import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.Gear;
import com.keykeeper.dushi.PlayerSoul;
import com.keykeeper.dushi.item.GuardShieldItem;
import com.keykeeper.dushi.item.WeaponItem;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Shows what the mod's rules make of any piece of gear: the game's own weapons, bows and shields get the lines the mod's
 * have, armour says what it weighs, and the mod's suits say what they give.
 */
@EventBusSubscriber(modid = Dushi.MODID, value = Dist.CLIENT)
public final class GearTooltips {
    private GearTooltips() {}

    private static String number(double value) {
        double rounded = Math.round(value * 10.0) / 10.0;
        return rounded == Math.rint(rounded) ? String.valueOf((long) rounded) : String.valueOf(rounded);
    }

    @SubscribeEvent
    static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> lines = event.getToolTip();
        Gear.Weapon weapon = Gear.weapon(stack);
        if (!(stack.getItem() instanceof WeaponItem)) {
            if (weapon != null) {
                lines.add(Component.translatable("tooltip.unfading_dawn.scaling", WeaponItem.grade(weapon.strength()), WeaponItem.grade(weapon.dexterity()))
                        .withStyle(ChatFormatting.GRAY));
                lines.add(Component.translatable("tooltip.unfading_dawn.stamina", Math.round(weapon.stamina())).withStyle(ChatFormatting.DARK_GREEN));
                lines.add(Component.translatable("tooltip.unfading_dawn.backstab", String.valueOf(weapon.backstab())).withStyle(ChatFormatting.DARK_RED));
            }
        }
        // What the weapon does in these hands: its own damage and the damage with this player's strength and dexterity.
        Player player = event.getEntity();
        if (weapon != null && player != null) {
            double base = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)
                    .compute(Attributes.ATTACK_DAMAGE, 1.0, EquipmentSlot.MAINHAND);
            double mine = base * PlayerSoul.of(player).weaponBonus(weapon.strength(), weapon.dexterity());
            lines.add(Component.translatable("tooltip.unfading_dawn.damage", number(base), number(mine), number(mine * Gear.backstab(player, weapon)))
                    .withStyle(ChatFormatting.GOLD));
        }
        if (stack.is(com.keykeeper.dushi.Dushi.GREAT_CANDLE.get())) {
            lines.add(Component.translatable("tooltip.unfading_dawn.great_candle", Math.round(Gear.CANDLE_BURNS)).withStyle(ChatFormatting.DARK_AQUA));
        }
        if (stack.is(com.keykeeper.dushi.Dushi.WARDEN_POLE.get())) {
            lines.add(Component.translatable("tooltip.unfading_dawn.great_candle", Math.round(Gear.POLE_BURNS)).withStyle(ChatFormatting.DARK_AQUA));
        }
        if (Gear.isBow(stack)) {
            lines.add(Component.translatable("tooltip.unfading_dawn.bow", WeaponItem.grade(Gear.BOW_SCALING)).withStyle(ChatFormatting.GRAY));
            if (player != null) {
                int percent = Math.round(100.0F * Gear.ARROW_DEXTERITY * PlayerSoul.of(player).stats[PlayerSoul.DEXTERITY]);
                lines.add(Component.translatable("tooltip.unfading_dawn.bow.mine", percent).withStyle(ChatFormatting.GOLD));
            }
        }
        if (!(stack.getItem() instanceof GuardShieldItem)) {
            float stability = Gear.stability(stack);
            if (stability >= 0.0F) {
                lines.add(Component.translatable("tooltip.unfading_dawn.stability", Math.round(stability * 100.0F)).withStyle(ChatFormatting.GRAY));
            }
        }
        ArmorSet set = ArmorSet.of(stack.getItem());
        if (set != null && set.bonus > 0.0F) {
            lines.add(Component.translatable("tooltip.unfading_dawn.set." + set.id, Math.round(set.bonus * 100.0F)).withStyle(ChatFormatting.DARK_AQUA));
        }
        float weight = Gear.weight(stack);
        if (weight > 0.0F) {
            lines.add(Component.translatable("tooltip.unfading_dawn.weight", String.valueOf(Math.round(weight * 10.0F) / 10.0F)).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
