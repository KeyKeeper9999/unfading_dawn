package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A stray shadow: one that has outlived whoever cast it, and gone on. It lies on the floor, where a shadow lies, and
 * goes about there quickly and without a sound; a blade passes through most of it. It comes to lie under whoever it
 * has found, and then one of two things: it takes them by the ankles from where it is, or it stands up out of the
 * floor - a man's outline, a hand's breadth thick - and claws. Standing it can be struck like anything else; that is
 * the time to strike it.
 */
public class StrayShadowEntity extends SoulsMob {
    public static final int RISE = 1;
    public static final int RISE_DURATION = 50;
    /** It is up by now, */
    public static final int RISE_UP = 13;
    public static final int RISE_IMPACT = 21;
    /** and begins to go back down. */
    public static final int RISE_SINK = 36;
    public static final int SNATCH = 2;
    public static final int SNATCH_DURATION = 34;
    public static final int SNATCH_IMPACT = 14;
    public static final double REACH = 2.3;
    /** The share of a blow that finds anything in it while it lies. */
    public static final float FLAT = 0.3F;

    public StrayShadowEntity(EntityType<? extends StrayShadowEntity> type, Level level) {
        super(type, level);
        this.xpReward = 14;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 170.0).add(Attributes.ATTACK_DAMAGE, 22.0)
                .add(Attributes.MOVEMENT_SPEED, 0.31).add(Attributes.KNOCKBACK_RESISTANCE, 0.8).add(Attributes.FOLLOW_RANGE, 22.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.5));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int soulReward() {
        return 1500;
    }

    /** Up out of the floor: when a blow lands on it whole. */
    public boolean risen() {
        return this.attackId() == RISE && this.attackTime >= RISE_UP - 4 && this.attackTime <= RISE_SINK + 6;
    }

    @Override
    protected int attackDuration(int id) {
        return id == RISE ? RISE_DURATION : SNATCH_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == RISE ? RISE_IMPACT - 6 : SNATCH_IMPACT - 4;
    }

    @Override
    protected int attackPause(int finishedId) {
        return 14 + this.random.nextInt(14);
    }

    @Override
    public double stopDistance() {
        return 0.9;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance > REACH - 0.7) {
            return 0;
        }
        return this.random.nextInt(10) < 3 ? SNATCH : RISE;
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (id == SNATCH) {
            if (time == SNATCH_IMPACT - 5) {
                this.playSound(Dushi.sound("entity.shadow.snatch"), 1.0F, 1.0F);
            }
            if (time == SNATCH_IMPACT) {
                this.hitAround(level, this.position().add(this.facing().scale(0.8)), 1.7, damage * 0.6F, 0.0, this.damageSources().mobAttack(this));
            }
            return;
        }
        if (time == 2) {
            this.playSound(Dushi.sound("entity.shadow.rise"), 1.2F, 0.9F + this.random.nextFloat() * 0.2F);
        }
        if (time < RISE_UP && time % 2 == 0) {
            level.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.1 + time * 0.12, this.getZ(), 3, 0.3, 0.2, 0.3, 0.0);
        }
        if (time == RISE_IMPACT) {
            this.hitArc(level, REACH, 70.0, damage, 0.6);
            this.playSound(Dushi.sound("entity.shadow.claw"), 1.0F, 1.0F);
        }
    }

    @Override
    protected void struck(ServerLevel level, LivingEntity victim) {
        if (this.attackId() == SNATCH) {
            // held by the ankles, for a breath
            victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 36, 4, false, false, true), this);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (damage != Float.MAX_VALUE && !this.risen() && source.getDirectEntity() != null) {
            level.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.2, this.getZ(), 6, 0.3, 0.05, 0.3, 0.01);
            return super.hurtServer(level, source, damage * FLAT);
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.leaveMaybe(level, Dushi.NAMELESS_SOUL.get(), 0.25F);
        this.leaveMaybe(level, Dushi.GREAT_SOUL.get(), 0.06F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.shadow.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.shadow.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.shadow.death");
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
    }
}
