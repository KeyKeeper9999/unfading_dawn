package com.keykeeper.dushi.item;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * A shield of the mod: blocking with it costs stamina, less the more stable the shield is.
 * It is a ShieldItem so that the game holds it like a shield in first person instead of like a blocking sword.
 */
public class GuardShieldItem extends ShieldItem {
    /** Share of a blocked blow's force that does not cost stamina, 0..1. */
    public final float stability;
    /** What it weighs, or a negative number for a shield that weighs what its stability says (see Gear.weight). */
    public final float weight;
    /** The key of the line that says what this shield does that others do not, or null. */
    public final String trait;

    public GuardShieldItem(Item.Properties properties, float stability) {
        this(properties, stability, -1.0F, null);
    }

    public GuardShieldItem(Item.Properties properties, float stability, float weight, String trait) {
        super(properties);
        this.stability = stability;
        this.weight = weight;
        this.trait = trait;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.vsratie_dushi.stability", Math.round(this.stability * 100.0F)).withStyle(ChatFormatting.GRAY));
        if (this.trait != null) {
            builder.accept(Component.translatable("tooltip.vsratie_dushi.shield." + this.trait).withStyle(ChatFormatting.DARK_AQUA));
        }
    }
}
