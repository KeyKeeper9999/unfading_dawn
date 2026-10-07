package com.keykeeper.dushi.item;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * A melee weapon of the mod. Damage, speed and reach are ordinary item attributes; on top of that every swing costs stamina,
 * the damage grows with the wielder's strength and dexterity, and a blow in the back hurts more.
 * The effects themselves are applied in {@link com.keykeeper.dushi.SoulMechanics}.
 */
public class WeaponItem extends Item {
    private static final String[] GRADES = {"-", "E", "D", "C", "B", "A", "S"};

    public final float staminaCost;
    public final float strengthScaling;
    public final float dexterityScaling;
    public final float backstab;

    public WeaponItem(Item.Properties properties, float staminaCost, float strengthScaling, float dexterityScaling, float backstab) {
        super(properties);
        this.staminaCost = staminaCost;
        this.strengthScaling = strengthScaling;
        this.dexterityScaling = dexterityScaling;
        this.backstab = backstab;
    }

    /** 0 is "-", 1.0 is "S". */
    public static String grade(float scaling) {
        return scaling <= 0.0F ? GRADES[0] : GRADES[Math.min(6, 1 + (int) Math.floor(scaling * 5.0F + 0.001F))];
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.unfading_dawn.scaling", grade(this.strengthScaling), grade(this.dexterityScaling))
                .withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("tooltip.unfading_dawn.stamina", Math.round(this.staminaCost)).withStyle(ChatFormatting.DARK_GREEN));
        builder.accept(Component.translatable("tooltip.unfading_dawn.backstab", String.valueOf(this.backstab)).withStyle(ChatFormatting.DARK_RED));
    }
}
