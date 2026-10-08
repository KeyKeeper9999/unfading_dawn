package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * The people of the Stubble: those who were sent out to get the corn in before sunset, on the afternoon the day was
 * stopped. The sun has not moved since, and what they cut stands again behind them. What they have in common: the same
 * dry voice, the same straw when they fall, and the way they go about a field.
 */
public abstract class FieldhandEntity extends SoulsMob {
    protected FieldhandEntity(EntityType<? extends FieldhandEntity> type, Level level) {
        super(type, level);
        this.xpReward = 14;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.leave(level, Dushi.RYE_STRAW.get(), 0, 2);
        this.leaveMaybe(level, Dushi.NAMELESS_SOUL.get(), 0.15F);
        this.leaveMaybe(level, Dushi.GREAT_SOUL.get(), 0.07F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.fieldhand.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.fieldhand.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.fieldhand.death");
    }
}
