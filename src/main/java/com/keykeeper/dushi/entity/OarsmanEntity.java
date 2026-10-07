package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * An oarsman of the Morning Wharf: one of those who rowed the boats out to bring the ships' people ashore - half as
 * broad again as other men, a knitted cap, a beard like a mat, and the oar in both hands. He is slow. The oar is long.
 * <p>
 * Round level with the whole length of it: whoever it meets is thrown - off the pier, if there is a pier. The blade
 * driven out straight, which is a shove more than a blow. And up over his head and down flat, which no shield holds.
 */
public class OarsmanEntity extends ShoremanEntity {
    public static final int SWEEP = 1;
    public static final int SWEEP_DURATION = 50;
    public static final int SWEEP_IMPACT = 24;
    public static final int PROD = 2;
    public static final int PROD_DURATION = 38;
    public static final int PROD_IMPACT = 18;
    public static final int SLAM = 3;
    public static final int SLAM_DURATION = 58;
    public static final int SLAM_IMPACT = 30;
    public static final double REACH = 4.4;

    public OarsmanEntity(EntityType<? extends OarsmanEntity> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 300.0).add(Attributes.ATTACK_DAMAGE, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22).add(Attributes.ARMOR, 4.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 22.0);
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
    public int soulReward() {
        return 1500;
    }

    @Override
    protected int attackDuration(int id) {
        return id == SWEEP ? SWEEP_DURATION : id == PROD ? PROD_DURATION : SLAM_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == SWEEP ? SWEEP_IMPACT - 9 : id == PROD ? PROD_IMPACT - 6 : SLAM_IMPACT - 11;
    }

    @Override
    protected int attackPause(int finishedId) {
        return 20 + this.random.nextInt(14);
    }

    @Override
    public double stopDistance() {
        return 2.6;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance > REACH - 0.4) {
            return 0;
        }
        int roll = this.random.nextInt(10);
        if (distance > 3.0) {
            return roll < 6 ? PROD : SWEEP;
        }
        return roll < 5 ? SWEEP : roll < 8 ? SLAM : PROD;
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (id == SWEEP) {
            if (time == SWEEP_IMPACT - 4) {
                this.playSound(Dushi.sound("player.weapon_swing_heavy"), 1.3F, 0.7F);
            }
            if (time == SWEEP_IMPACT) {
                this.hitArc(level, REACH, 100.0, damage, 1.9);
                Vec3 at = this.position().add(this.facing().scale(3.0));
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y + 1.2, at.z, 1, 0.0, 0.0, 0.0, 0.0);
            }
        } else if (id == PROD) {
            if (time == PROD_IMPACT - 2) {
                this.playSound(Dushi.sound("player.weapon_swing_heavy"), 1.0F, 1.0F);
            }
            if (time == PROD_IMPACT) {
                this.hitArc(level, REACH + 0.4, 16.0, damage * 0.7F, 1.6);
            }
        } else {
            if (time == SLAM_IMPACT - 5) {
                this.playSound(Dushi.sound("player.weapon_swing_heavy"), 1.3F, 0.6F);
            }
            if (time == SLAM_IMPACT) {
                this.hitArc(level, this.facing(), REACH, 18.0, damage * 1.5F, 0.4, true);
                Vec3 at = this.position().add(this.facing().scale(3.2));
                level.sendParticles(ParticleTypes.POOF, at.x, at.y + 0.2, at.z, 8, 0.5, 0.1, 0.5, 0.02);
                level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.sailor.oar"), this.getSoundSource(), 1.6F, 0.9F + this.random.nextFloat() * 0.2F);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.leaveMaybe(level, Dushi.OAR.get(), 0.06F);
        this.leaveMaybe(level, Dushi.GREAT_SOUL.get(), 0.1F);
    }

    @Override
    public float getVoicePitch() {
        return 0.7F + this.random.nextFloat() * 0.1F;
    }
}
