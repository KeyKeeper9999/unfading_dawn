package com.keykeeper.dushi.item;

import java.util.function.Consumer;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.Gear;
import com.keykeeper.dushi.PlayerSoul;
import com.keykeeper.dushi.Spell;

import net.minecraft.ChatFormatting;
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
 * A learned spell. Right click casts it if the catalyst of its school is in the other hand.
 * The casts left are kept as the item's wear (the last point is never used, so the spell never breaks); a hearth restores them.
 */
public class SpellItem extends Item {
    public final Spell spell;

    public SpellItem(Item.Properties properties, Spell spell) {
        super(properties);
        this.spell = spell;
    }

    public static int castsLeft(ItemStack stack) {
        return Math.max(0, stack.getMaxDamage() - 1 - stack.getDamageValue());
    }

    private boolean hasCatalyst(Player player, InteractionHand hand) {
        ItemStack other = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        return other.getItem() instanceof CatalystItem catalyst && catalyst.school == this.spell.school;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!this.hasCatalyst(player, hand)) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("message.unfading_dawn.need_catalyst." + this.spell.school.key()));
            }
            return InteractionResult.FAIL;
        }
        if (castsLeft(stack) <= 0 || PlayerSoul.of(player).stamina <= 0.0F) {
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), Dushi.sound("item.spell.fizzle"), SoundSource.PLAYERS, 0.8F, 1.0F);
                serverLevel.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getEyeY() - 0.3, player.getZ(), 6, 0.2, 0.2, 0.2, 0.01);
            }
            player.getCooldowns().addCooldown(stack, 10);
            return InteractionResult.FAIL;
        }
        if (this.spell.channel > 0) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        if (level instanceof ServerLevel serverLevel) {
            this.release(serverLevel, player, stack);
        }
        player.getCooldowns().addCooldown(stack, this.spell.cooldown);
        return InteractionResult.SUCCESS;
    }

    private void release(ServerLevel level, Player player, ItemStack stack) {
        PlayerSoul soul = PlayerSoul.of(player);
        soul.spend(this.spell.stamina);
        player.syncData(Dushi.PLAYER_SOUL);
        this.spell.cast(level, player, soul.spellBonus(this.spell.school) * Gear.spellBonus(player));
        if (!player.getAbilities().instabuild) {
            stack.setDamageValue(stack.getDamageValue() + 1);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level instanceof ServerLevel serverLevel && entity instanceof Player player && castsLeft(stack) > 0) {
            this.release(serverLevel, player, stack);
            player.getCooldowns().addCooldown(stack, this.spell.cooldown);
        }
        return stack;
    }

    /** While praying, sparks rise around the caster. */
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int ticksRemaining) {
        if (level instanceof ServerLevel serverLevel && ticksRemaining % 4 == 0) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, entity.getX(), entity.getY() + 0.1, entity.getZ(), 2, 0.5, 0.0, 0.5, 0.03);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return this.spell.channel;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return this.spell.channel > 0 ? ItemUseAnimation.BOW : ItemUseAnimation.NONE;
    }

    /** The bar under the icon shows the casts left in the colour of the school. */
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * castsLeft(stack) / Math.max(1, stack.getMaxDamage() - 1));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return switch (this.spell.school) {
            case SORCERY -> 0x6FC7FF;
            case MIRACLE -> 0xFFE27A;
            case PYROMANCY -> 0xFF7A2E;
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("spell.unfading_dawn." + this.spell.id + ".desc").withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("tooltip.unfading_dawn.school." + this.spell.school.key()).withStyle(ChatFormatting.DARK_AQUA));
        builder.accept(Component.translatable("tooltip.unfading_dawn.casts", castsLeft(stack), stack.getMaxDamage() - 1).withStyle(ChatFormatting.DARK_GREEN));
    }
}
