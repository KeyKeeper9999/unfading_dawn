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

/** A piece of another flask: right click with a flask of warmth anywhere in the inventory and it holds one gulp more. */
public class FlaskShardItem extends Item {
    public FlaskShardItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack flask = player.getInventory().getItem(slot);
                if (flask.getItem() instanceof FlaskItem && FlaskItem.enlarge(flask)) {
                    serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), Dushi.sound("ui.level_up"), SoundSource.PLAYERS, 0.8F, 1.3F);
                    player.sendOverlayMessage(Component.translatable("message.vsratie_dushi.flask_enlarged", FlaskItem.capacity(flask)));
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
            player.sendOverlayMessage(Component.translatable("message.vsratie_dushi.flask_shard_useless"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.vsratie_dushi.flask_shard").withStyle(ChatFormatting.GRAY));
    }
}
