package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * The people of the Morning Wharf: those who went down to the water to meet the ships on the morning the day was
 * stopped, and have stood there since - the ships are still a cable's length off. What they have in common: the same
 * wet voice, the same sailcloth and tarred rope when they fall.
 */
public abstract class ShoremanEntity extends SoulsMob {
    protected ShoremanEntity(EntityType<? extends ShoremanEntity> type, Level level) {
        super(type, level);
        this.xpReward = 12;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.leave(level, Dushi.SAILCLOTH.get(), 0, 2);
        this.leaveMaybe(level, Dushi.TARRED_ROPE.get(), 0.4F);
        this.leaveMaybe(level, Dushi.NAMELESS_SOUL.get(), 0.15F);
        this.leaveMaybe(level, Dushi.GREAT_SOUL.get(), 0.06F);
        this.leavePiece(level, com.keykeeper.dushi.ArmorSet.OILSKIN, 0.05F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.sailor.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.sailor.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.sailor.death");
    }
}
