package com.keykeeper.dushi.item;

import java.util.function.Consumer;

import com.keykeeper.dushi.Charm;
import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.PlayerSoul;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
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
 * A charm: used, it is put on - it leaves the hand for the one place a charm is worn, and the charm that was worn
 * before comes back into the hand. What each gives is in {@link Charm}.
 */
public class CharmItem extends Item {
    public final Charm charm;

    public CharmItem(Item.Properties properties, Charm charm) {
        super(properties);
        this.charm = charm;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = player.getItemInHand(hand);
            Charm before = Charm.worn(player);
            Component name = stack.getHoverName();
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            wear(serverPlayer, this.charm);
            if (before != null && !player.getAbilities().instabuild) {
                give(serverLevel, player, hand, new ItemStack(Dushi.CHARMS.get(before).get()));
            }
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_CHAIN.value(), SoundSource.PLAYERS, 0.8F, 1.3F);
            player.sendOverlayMessage(Component.translatable("message.unfading_dawn.charm_on", name));
        }
        return InteractionResult.SUCCESS;
    }

    /** Puts {@code charm} in the place where the player's charm is worn (null takes it off) and makes what follows from it so. */
    public static void wear(ServerPlayer player, Charm charm) {
        PlayerSoul soul = PlayerSoul.of(player);
        soul.charm = charm == null ? "" : charm.id;
        soul.applyAttributes(player);
        player.syncData(Dushi.PLAYER_SOUL);
    }

    /** Takes the charm the player wears off and gives it back to them as a thing. Returns whether there was one. */
    public static boolean takeOff(ServerPlayer player) {
        Charm worn = Charm.worn(player);
        if (worn == null) {
            return false;
        }
        wear(player, null);
        ItemStack stack = new ItemStack(Dushi.CHARMS.get(worn).get());
        Component name = stack.getHoverName();
        if (!player.getInventory().add(stack)) {
            player.spawnAtLocation(player.level(), stack);
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_CHAIN.value(), SoundSource.PLAYERS, 0.8F, 0.9F);
        player.sendOverlayMessage(Component.translatable("message.unfading_dawn.charm_off", name));
        return true;
    }

    private static void give(ServerLevel level, Player player, InteractionHand hand, ItemStack stack) {
        if (player.getItemInHand(hand).isEmpty()) {
            player.setItemInHand(hand, stack);
        } else if (!player.getInventory().add(stack)) {
            player.spawnAtLocation(level, stack);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.unfading_dawn.charm." + this.charm.id, this.charm.amount).withStyle(ChatFormatting.DARK_AQUA));
        builder.accept(Component.translatable("tooltip.unfading_dawn.charm_use").withStyle(ChatFormatting.GRAY));
    }
}
