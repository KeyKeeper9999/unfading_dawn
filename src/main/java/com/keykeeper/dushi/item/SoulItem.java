package com.keykeeper.dushi.item;

import java.util.function.Consumer;

import com.keykeeper.dushi.SoulMechanics;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/** Somebody's soul, found or won: crush it in the hand to take its souls. Unlike carried souls, it is not lost on death. */
public class SoulItem extends Item {
    public final int souls;

    public SoulItem(Item.Properties properties, int souls) {
        super(properties);
        this.souls = souls;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            SoulMechanics.giveSouls(serverPlayer, this.souls);
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        player.getCooldowns().addCooldown(stack, 8);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return this.souls >= 5000;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.unfading_dawn.soul_item", this.souls).withStyle(ChatFormatting.AQUA));
    }
}
