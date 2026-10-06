package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * The Unsleeping: the people of the skete in the Black Firwood, who swore not to sleep until morning - and no morning
 * came. What they have in common: the same dry voice, the same white linen and candle wax when they fall.
 */
public abstract class UnsleepingEntity extends SoulsMob {
    protected UnsleepingEntity(EntityType<? extends UnsleepingEntity> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.leave(level, Dushi.LINEN.get(), 0, 2);
        this.leaveMaybe(level, Dushi.WAX.get(), 0.35F);
        this.leaveMaybe(level, Dushi.NAMELESS_SOUL.get(), 0.15F);
        this.leaveMaybe(level, Dushi.GREAT_SOUL.get(), 0.05F);
        this.leavePiece(level, com.keykeeper.dushi.ArmorSet.VIGIL, 0.04F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.sect.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.sect.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.sect.death");
    }
}
