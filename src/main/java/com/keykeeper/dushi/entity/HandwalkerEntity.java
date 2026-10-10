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
import net.minecraft.world.entity.ai.goal.FloatGoal;
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
 * A handwalker: a thing of many joints, four paces long, that runs on hands - a pair of a man's hands to every joint,
 * flat on the floor, and two more for jaws. It is the quickest thing in the Sky Beneath and is heard before it is
 * seen: a great many palms on stone.
 * <p>
 * From a little way off it rears the front third of itself and throws it forward, the whole length following; close
 * to, it brings its tail round, all the way round, at the height of a shin.
 */
public class HandwalkerEntity extends SoulsMob {
    public static final int LUNGE = 1;
    public static final int LUNGE_DURATION = 46;
    public static final int LUNGE_START = 15;
    public static final int LUNGE_END = 25;
    public static final int WHIP = 2;
    public static final int WHIP_DURATION = 42;
    public static final int WHIP_IMPACT = 19;
    public static final double WHIP_REACH = 3.1;

    private int lungeCooldown = 30;
    private boolean lungeHit;

    public HandwalkerEntity(EntityType<? extends HandwalkerEntity> type, Level level) {
        super(type, level);
        this.xpReward = 18;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 220.0).add(Attributes.ATTACK_DAMAGE, 23.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34).add(Attributes.KNOCKBACK_RESISTANCE, 0.4).add(Attributes.FOLLOW_RANGE, 24.0).add(Attributes.STEP_HEIGHT, 1.1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int soulReward() {
        return 1900;
    }

    @Override
    protected int attackDuration(int id) {
        return id == LUNGE ? LUNGE_DURATION : WHIP_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == LUNGE ? LUNGE_START - 3 : 0;
    }

    @Override
    protected int attackPause(int finishedId) {
        return finishedId == LUNGE ? 18 + this.random.nextInt(10) : 12 + this.random.nextInt(10);
    }

    @Override
    public double stopDistance() {
        return this.lungeCooldown <= 0 ? 4.0 : 1.3;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (this.lungeCooldown <= 0 && distance > 2.4 && distance < 6.5 && this.hasLineOfSight(target)) {
            this.lungeCooldown = 70 + this.random.nextInt(50);
            return LUNGE;
        }
        return distance < WHIP_REACH - 1.0 ? WHIP : 0;
    }

    @Override
    public void startAttack(int id) {
        super.startAttack(id);
        this.lungeHit = false;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.lungeCooldown = Math.max(0, this.lungeCooldown - 1);
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (id == WHIP) {
            if (time == WHIP_IMPACT - 6) {
                this.playSound(Dushi.sound("entity.handwalker.whip"), 1.2F, 1.0F);
            }
            if (time == WHIP_IMPACT) {
                this.hitAround(level, this.position(), WHIP_REACH, damage * 0.85F, 1.3, this.damageSources().mobAttack(this));
                for (int i = 0; i < 6; i++) {
                    double angle = i * Math.PI / 3.0;
                    level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + Math.cos(angle) * 2.4, this.getY() + 0.5, this.getZ() + Math.sin(angle) * 2.4, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
            return;
        }
        if (time == 3) {
            this.playSound(Dushi.sound("entity.handwalker.rear"), 1.2F, 1.0F);
        }
        if (time >= LUNGE_START && time < LUNGE_END) {
            Vec3 run = this.facing().scale(0.72);
            this.setDeltaMovement(run.x, this.getDeltaMovement().y, run.z);
            if (!this.lungeHit && this.hitArc(level, 1.6, 55.0, damage * 1.2F, 1.0) > 0) {
                this.lungeHit = true;
            }
        }
        if (time == LUNGE_END) {
            this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
        }
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.leaveMaybe(level, Dushi.NAMELESS_SOUL.get(), 0.3F);
        this.leaveMaybe(level, Dushi.GREAT_SOUL.get(), 0.08F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.handwalker.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.handwalker.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.handwalker.death");
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(Dushi.sound("entity.handwalker.step"), 0.5F, 0.9F + this.random.nextFloat() * 0.3F);
    }
}
