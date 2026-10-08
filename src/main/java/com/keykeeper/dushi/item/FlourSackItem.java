package com.keykeeper.dushi.item;

import com.keykeeper.dushi.entity.FlourSackEntity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A small sack of flour: thrown, it bursts and leaves its flour hanging in the air - which blinds whoever stands in it,
 * and goes up at the first flame (see {@link com.keykeeper.dushi.entity.FlourCloudEntity}).
 */
public class FlourSackItem extends Item {
    public FlourSackItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.6F, 0.5F);
        if (level instanceof ServerLevel serverLevel) {
            Projectile.spawnProjectileFromRotation(FlourSackEntity::new, serverLevel, stack, player, 0.0F, 0.9F, 1.0F);
            player.awardStat(Stats.ITEM_USED.get(this));
            stack.consume(1, player);
        }
        player.getCooldowns().addCooldown(stack, 20);
        return InteractionResult.SUCCESS;
    }
}
