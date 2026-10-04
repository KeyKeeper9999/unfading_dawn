package com.keykeeper.dushi.item;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** A shield of the mod: blocking with it costs stamina, less the more stable the shield is. */
public class GuardShieldItem extends Item {
    /** Share of a blocked blow's force that does not cost stamina, 0..1. */
    public final float stability;

    public GuardShieldItem(Item.Properties properties, float stability) {
        super(properties);
        this.stability = stability;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.vsratie_dushi.stability", Math.round(this.stability * 100.0F)).withStyle(ChatFormatting.GRAY));
    }
}
