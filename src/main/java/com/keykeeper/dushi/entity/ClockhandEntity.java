package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * A clockhand: one of those that were set in the Sky Beneath when the day was stopped, to tend the clock that was
 * stopped with it. It is made of that clock - a dial for a chest, the two hands for arms - and it wears the Founder's
 * amber. Of everything down here it alone is at its work, and its work is that nothing moves.
 * <p>
 * It moves as a hand moves round a dial: it stands for most of a second, and then is a step further on. Its blows
 * keep the same time, which is the way to fight it - the long hand straight out, the length of two men; the short
 * hand round. Now and then it stops to wind itself, and for a while after that its seconds are shorter.
 */
public class ClockhandEntity extends SoulsMob {
    public static final int STAB = 1;
    public static final int STAB_DURATION = 40;
    public static final int STAB_IMPACT = 20;
    public static final int SWEEP = 2;
    public static final int SWEEP_DURATION = 50;
    public static final int SWEEP_IMPACT = 20;
    public static final int WIND = 3;
    public static final int WIND_DURATION = 40;
    public static final double STAB_REACH = 4.0;
    public static final double SWEEP_REACH = 2.9;
    /** How long a winding lasts, in ticks. */
    public static final int WOUND = 240;
    /** The length of its second: it stands for the first part of it and goes in the rest. */
    public static final int SECOND = 20;

    private int wound;
    private int windCooldown = 200;

    public ClockhandEntity(EntityType<? extends ClockhandEntity> type, Level level) {
        super(type, level);
        this.xpReward = 18;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 240.0).add(Attributes.ATTACK_DAMAGE, 26.0)
                .add(Attributes.MOVEMENT_SPEED, 0.36).add(Attributes.ARMOR, 8.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.6).add(Attributes.FOLLOW_RANGE, 22.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int soulReward() {
        return 2100;
    }

    public boolean wound() {
        return this.wound > 0;
    }

    @Override
    protected int attackDuration(int id) {
        return switch (id) {
            case STAB -> STAB_DURATION;
            case SWEEP -> SWEEP_DURATION;
            default -> WIND_DURATION;
        };
    }

    @Override
    protected int trackUntil(int id) {
        // (it sets itself at the start of a move and does not turn again: a hand does not)
        return id == WIND ? 0 : 6;
    }

    @Override
    protected int attackPause(int finishedId) {
        return this.wound() ? 10 : 20;
    }

    @Override
    public double stopDistance() {
        return 2.2;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (this.windCooldown <= 0 && distance > 5.0 && !this.wound()) {
            this.windCooldown = 500 + this.random.nextInt(200);
            return WIND;
        }
        if (distance < SWEEP_REACH - 0.5) {
            return this.random.nextInt(3) == 0 ? STAB : SWEEP;
        }
        return distance < STAB_REACH - 0.5 ? STAB : 0;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.wound = Math.max(0, this.wound - 1);
        this.windCooldown = Math.max(0, this.windCooldown - 1);
        if (this.attackId() == 0) {
            // it stands for the first part of every second
            int second = this.wound() ? SECOND / 2 : SECOND;
            int beat = this.tickCount % second;
            if (beat < second * 11 / 20) {
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(0.0, motion.y, 0.0);
            } else if (beat == second * 11 / 20 && this.getTarget() != null) {
                this.playSound(Dushi.sound("entity.clockhand.tick"), 0.8F, this.wound() ? 1.3F : 1.0F);
            }
        }
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        switch (id) {
            case STAB -> {
                if (time == STAB_IMPACT - 4) {
                    this.playSound(Dushi.sound("entity.clockhand.stab"), 1.2F, 1.0F);
                }
                if (time == STAB_IMPACT) {
                    this.hitArc(level, this.facing(), STAB_REACH, 13.0, damage * 1.2F, 0.6, false);
                }
            }
            case SWEEP -> {
                if (time == SWEEP_IMPACT - 4) {
                    this.playSound(Dushi.sound("entity.clockhand.sweep"), 1.2F, 1.0F);
                }
                if (time == SWEEP_IMPACT) {
                    this.hitArc(level, SWEEP_REACH, 110.0, damage * 0.9F, 1.1);
                    Vec3 at = this.position().add(this.facing().scale(2.0));
                    level.sendParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y + 1.2, at.z, 2, 0.6, 0.0, 0.6, 0.0);
                }
            }
            default -> {
                if (time % 8 == 2) {
                    this.playSound(Dushi.sound("entity.clockhand.wind"), 1.2F, 0.9F + time * 0.01F);
                }
                if (time == WIND_DURATION - 2) {
                    this.wound = WOUND;
                }
            }
        }
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.leaveMaybe(level, Dushi.NAMELESS_SOUL.get(), 0.3F);
        this.leaveMaybe(level, Dushi.GREAT_SOUL.get(), 0.1F);
        this.leaveMaybe(level, Dushi.RUST_SCRAP.get(), 0.4F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.clockhand.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.clockhand.death");
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
    }
}
