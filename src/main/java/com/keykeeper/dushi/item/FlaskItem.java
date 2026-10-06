package com.keykeeper.dushi.item;

import java.util.function.Consumer;

import com.keykeeper.dushi.Dushi;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/**
 * The flask of warmth: a few gulps of the hearth's fire that heal at once. It is never used up, only emptied;
 * resting at a hearth fills it again. The gulps left are kept as the item's wear, the last point of which is never spent.
 */
public class FlaskItem extends Item {
    public static final int BASE_CHARGES = 5;
    public static final int MAX_CHARGES = 12;
    private static final int DRINK_TICKS = 24;

    public FlaskItem(Item.Properties properties) {
        super(properties);
    }

    public static int charges(ItemStack stack) {
        return Math.max(0, stack.getMaxDamage() - 1 - stack.getDamageValue());
    }

    public static int capacity(ItemStack stack) {
        return stack.getMaxDamage() - 1;
    }

    /** One more gulp fits in from now on. Returns false if the flask is already as big as it gets. */
    public static boolean enlarge(ItemStack stack) {
        if (capacity(stack) >= MAX_CHARGES) {
            return false;
        }
        stack.set(DataComponents.MAX_DAMAGE, stack.getMaxDamage() + 1);
        return true;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (charges(stack) <= 0) {
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), Dushi.sound("item.flask.empty"), SoundSource.PLAYERS, 0.8F, 1.0F);
                player.sendOverlayMessage(Component.translatable("message.vsratie_dushi.flask_empty"));
            }
            player.getCooldowns().addCooldown(stack, 20);
            return InteractionResult.FAIL;
        }
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), Dushi.sound("item.flask.drink"), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level instanceof ServerLevel serverLevel && charges(stack) > 0) {
            // A gulp restores half of the health, but never less than five hearts.
            float gulp = Math.max(10.0F, entity.getMaxHealth() * 0.5F);
            entity.heal(entity instanceof Player drinker ? gulp * com.keykeeper.dushi.Charm.ASH_KNOT.more(drinker) : gulp);
            serverLevel.sendParticles(ParticleTypes.FLAME, entity.getX(), entity.getY() + 1.0, entity.getZ(), 14, 0.3, 0.5, 0.3, 0.01);
            if (!(entity instanceof Player player) || !player.getAbilities().instabuild) {
                stack.setDamageValue(stack.getDamageValue() + 1);
            }
            if (entity instanceof Player player) {
                player.awardStat(Stats.ITEM_USED.get(this));
                player.getCooldowns().addCooldown(stack, 12);
            }
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return DRINK_TICKS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * charges(stack) / Math.max(1, capacity(stack)));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFF9A2E;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.vsratie_dushi.flask", charges(stack), capacity(stack)).withStyle(ChatFormatting.GOLD));
        builder.accept(Component.translatable("tooltip.vsratie_dushi.flask_hint").withStyle(ChatFormatting.GRAY));
    }
}
