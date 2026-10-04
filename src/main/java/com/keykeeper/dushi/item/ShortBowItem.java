package com.keykeeper.dushi.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A small bow that is drawn half again as fast as an ordinary one. */
public class ShortBowItem extends BowItem {
    public ShortBowItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
        int held = this.getUseDuration(stack, entity) - remainingTime;
        return super.releaseUsing(stack, level, entity, remainingTime - held / 2);
    }
}
