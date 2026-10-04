package com.keykeeper.dushi.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;

/** How a mob of the mod holds its arms: a raised shield and a drawn bow look the way they do on a player. */
final class ArmPoses {
    private ArmPoses() {}

    static HumanoidModel.ArmPose of(LivingEntity mob, HumanoidArm arm) {
        boolean main = mob.getMainArm() == arm;
        ItemStack held = main ? mob.getMainHandItem() : mob.getOffhandItem();
        if (held.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        }
        if (mob.isUsingItem() && (mob.getUsedItemHand() == InteractionHand.MAIN_HAND) == main) {
            ItemUseAnimation animation = held.getUseAnimation();
            if (animation == ItemUseAnimation.BLOCK) {
                return HumanoidModel.ArmPose.BLOCK;
            }
            if (animation == ItemUseAnimation.BOW) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
        }
        return HumanoidModel.ArmPose.ITEM;
    }
}
