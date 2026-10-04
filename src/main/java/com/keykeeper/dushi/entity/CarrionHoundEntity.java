package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The carrion hound: what the dogs of the borough became once there was nobody left to feed them. Fast and frail; it
 * snaps when close and, from a few steps away, crouches and springs. They keep to the alleys in twos and threes.
 */
public class CarrionHoundEntity extends SoulsMob {
    public static final int BITE = 1;
    public static final int BITE_DURATION = 16;
    private static final int BITE_IMPACT = 7;
    /** Crouches for half a second, then springs. */
    public static final int POUNCE = 2;
    public static final int POUNCE_DURATION = 32;
    public static final int POUNCE_LEAP = 11;

    private int pounceCooldown = 40;
    private boolean pounceHit;

    public CarrionHoundEntity(EntityType<? extends CarrionHoundEntity> type, Level level) {
        super(type, level);
        this.xpReward = 4;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 11.0).add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34).add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int soulReward() {
        return 25;
    }

    @Override
    protected int attackDuration(int id) {
        return id == POUNCE ? POUNCE_DURATION : BITE_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == POUNCE ? POUNCE_LEAP : BITE_IMPACT - 2;
    }

    @Override
    protected int attackPause(int finishedId) {
        return finishedId == POUNCE ? 18 + this.random.nextInt(10) : 8 + this.random.nextInt(8);
    }

    @Override
    public double stopDistance() {
        return 1.1;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance > 2.8 && distance < 7.0 && this.pounceCooldown <= 0 && this.onGround() && this.hasLineOfSight(target)) {
            this.pounceCooldown = 70 + this.random.nextInt(40);
            return POUNCE;
        }
        return distance < 1.7 ? BITE : 0;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.pounceCooldown > 0) {
            this.pounceCooldown--;
        }
    }

    @Override
    public void startAttack(int id) {
        super.startAttack(id);
        this.pounceHit = false;
        if (id == POUNCE) {
            this.playSound(Dushi.sound("entity.hound.ambient"), 1.0F, 1.2F);
        }
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (id == BITE && time == BITE_IMPACT) {
            this.playSound(Dushi.sound("entity.hound.bite"), 1.0F, 0.9F + this.random.nextFloat() * 0.3F);
            this.hitArc(level, 1.9, 50.0, damage, 0.2);
        } else if (id == POUNCE) {
            if (time == POUNCE_LEAP) {
                LivingEntity target = this.getTarget();
                Vec3 to = target == null ? this.facing().scale(4.0) : target.position().subtract(this.position());
                Vec3 flat = new Vec3(to.x, 0.0, to.z);
                double length = Math.min(7.0, flat.length() + 0.6);
                Vec3 jump = flat.lengthSqr() < 1.0E-4 ? Vec3.ZERO : flat.normalize().scale(length / 9.0);
                this.setDeltaMovement(jump.x, 0.42, jump.z);
                this.needsSync = true;
                this.playSound(Dushi.sound("entity.hound.bite"), 1.0F, 1.3F);
            }
            if (!this.pounceHit && time > POUNCE_LEAP && time < POUNCE_LEAP + 12 && this.hitArc(level, 1.5, 75.0, damage * 1.4F, 0.5) > 0) {
                this.pounceHit = true;
            }
        }
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.leaveMaybe(level, Dushi.TATTERED_CLOTH.get(), 0.25F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.hound.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.hound.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.hound.death");
    }
}
