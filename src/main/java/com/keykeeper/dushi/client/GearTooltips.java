package com.keykeeper.dushi.client;

import java.util.List;

import com.keykeeper.dushi.ArmorSet;
import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.Gear;
import com.keykeeper.dushi.item.GuardShieldItem;
import com.keykeeper.dushi.item.WeaponItem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
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

    @SubscribeEvent
    static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> lines = event.getToolTip();
        if (!(stack.getItem() instanceof WeaponItem)) {
            Gear.Weapon weapon = Gear.weapon(stack);
            if (weapon != null) {
                lines.add(Component.translatable("tooltip.vsratie_dushi.scaling", WeaponItem.grade(weapon.strength()), WeaponItem.grade(weapon.dexterity()))
                        .withStyle(ChatFormatting.GRAY));
                lines.add(Component.translatable("tooltip.vsratie_dushi.stamina", Math.round(weapon.stamina())).withStyle(ChatFormatting.DARK_GREEN));
                lines.add(Component.translatable("tooltip.vsratie_dushi.backstab", String.valueOf(weapon.backstab())).withStyle(ChatFormatting.DARK_RED));
            }
        }
        if (Gear.isBow(stack)) {
            lines.add(Component.translatable("tooltip.vsratie_dushi.bow", WeaponItem.grade(Gear.BOW_SCALING)).withStyle(ChatFormatting.GRAY));
        }
        if (!(stack.getItem() instanceof GuardShieldItem)) {
            float stability = Gear.stability(stack);
            if (stability >= 0.0F) {
                lines.add(Component.translatable("tooltip.vsratie_dushi.stability", Math.round(stability * 100.0F)).withStyle(ChatFormatting.GRAY));
            }
        }
        ArmorSet set = ArmorSet.of(stack.getItem());
        if (set != null && set.bonus > 0.0F) {
            lines.add(Component.translatable("tooltip.vsratie_dushi.set." + set.id, Math.round(set.bonus * 100.0F)).withStyle(ChatFormatting.DARK_AQUA));
        }
        float weight = Gear.weight(stack);
        if (weight > 0.0F) {
            lines.add(Component.translatable("tooltip.vsratie_dushi.weight", String.valueOf(Math.round(weight * 10.0F) / 10.0F)).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
