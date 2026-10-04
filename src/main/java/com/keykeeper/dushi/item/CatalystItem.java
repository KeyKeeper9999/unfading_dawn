package com.keykeeper.dushi.item;

import java.util.function.Consumer;

import com.keykeeper.dushi.SpellSchool;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** Held in one hand, it lets the spells of its school in the other hand be cast. Does nothing by itself. */
public class CatalystItem extends Item {
    public final SpellSchool school;

    public CatalystItem(Item.Properties properties, SpellSchool school) {
        super(properties);
        this.school = school;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.vsratie_dushi.catalyst." + this.school.key()).withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("tooltip.vsratie_dushi.catalyst_use").withStyle(ChatFormatting.DARK_GRAY));
    }
}
