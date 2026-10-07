package com.keykeeper.dushi.item;

import java.util.function.Consumer;

import com.keykeeper.dushi.Dushi;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/**
 * A piece of another flask: right click with a flask of warmth anywhere in the inventory and it holds one gulp more.
 * Resting at a hearth does the same with every shard carried.
 */
public class FlaskShardItem extends Item {
    public FlaskShardItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            if (melt(serverLevel, player)) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            } else {
                player.sendOverlayMessage(Component.translatable("message.unfading_dawn.flask_shard_useless"));
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** Makes the first flask in the inventory that can still grow hold one gulp more. Returns false if there is none. */
    private static boolean melt(ServerLevel level, Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack flask = player.getInventory().getItem(slot);
            if (flask.getItem() instanceof FlaskItem && FlaskItem.enlarge(flask)) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), Dushi.sound("ui.level_up"), SoundSource.PLAYERS, 0.8F, 1.3F);
                player.sendOverlayMessage(Component.translatable("message.unfading_dawn.flask_enlarged", FlaskItem.capacity(flask)));
                return true;
            }
        }
        return false;
    }

    /** At a hearth every shard carried goes into the flask by itself. Returns how many were used. */
    public static int meltAll(ServerLevel level, Player player) {
        int used = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack shards = player.getInventory().getItem(slot);
            while (shards.getItem() instanceof FlaskShardItem && !shards.isEmpty() && melt(level, player)) {
                shards.shrink(1);
                used++;
            }
        }
        return used;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.unfading_dawn.flask_shard").withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("tooltip.unfading_dawn.flask_shard.hearth").withStyle(ChatFormatting.GRAY));
    }
}
