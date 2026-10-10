package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * One of the long-armed: something that is in the wall. A hump of it shows in a niche, and two arms folded before it;
 * the rest, if there is any, is in the rock. It does not come out and it does not turn. It waits for whoever passes
 * its niche within four paces - and then the arms are four paces long: it takes hold and draws them in to the wall,
 * or, if they are off to one side, rakes the whole width of the way.
 * <p>
 * It is a thing of narrow ways: the place to be when one is passed is the far wall, or past it at a run.
 */
public class LongarmEntity extends SoulsMob {
    public static final int GRAB = 1;
    public static final int GRAB_DURATION = 58;
    public static final int GRAB_IMPACT = 22;
    public static final int RAKE = 2;
    public static final int RAKE_DURATION = 62;
    public static final int RAKE_IMPACT = 26;
    public static final double REACH = 5.0;
    public static final double RAKE_REACH = 4.4;

    /** The way out of its niche: it is set with its back to the wall, and keeps it there. */
    private float homeYaw = Float.NaN;

    public LongarmEntity(EntityType<? extends LongarmEntity> type, Level level) {
        super(type, level);
        this.xpReward = 16;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 260.0).add(Attributes.ATTACK_DAMAGE, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.FOLLOW_RANGE, 9.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int soulReward() {
        return 1400;
    }

    @Override
    protected int attackDuration(int id) {
        return id == GRAB ? GRAB_DURATION : RAKE_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return 0;
    }

    @Override
    protected int attackPause(int finishedId) {
        return 26 + this.random.nextInt(16);
    }

    @Override
    public double stopDistance() {
        return REACH;
    }

    /** The way it faces, out of the wall: it does not turn from it. */
    public Vec3 outward() {
        return Vec3.directionFromRotation(0.0F, Float.isNaN(this.homeYaw) ? this.getYRot() : this.homeYaw);
    }

    @Override
    public Vec3 facing() {
        return this.outward();
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance > REACH - 0.4) {
            return 0;
        }
        Vec3 to = new Vec3(target.getX() - this.getX(), 0.0, target.getZ() - this.getZ());
        double before = to.lengthSqr() < 0.3 ? 1.0 : to.normalize().dot(this.outward());
        if (before < 0.2) {
            return 0;                                              // (beside its niche, along the wall: it cannot get at them)
        }
        if (before > 0.86) {
            return GRAB;
        }
        return distance < RAKE_REACH - 0.3 ? RAKE : 0;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (Float.isNaN(this.homeYaw)) {
            this.homeYaw = this.getYRot();
        }
        super.customServerAiStep(level);
        Vec3 motion = this.getDeltaMovement();
        if (motion.x != 0.0 || motion.z != 0.0) {
            this.setDeltaMovement(0.0, motion.y, 0.0);
        }
        this.setYRot(this.homeYaw);
        this.setYBodyRot(this.homeYaw);
        this.setYHeadRot(this.homeYaw);
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (id == RAKE) {
            if (time == RAKE_IMPACT - 6) {
                this.playSound(Dushi.sound("entity.longarm.reach"), 1.2F, 1.15F);
            }
            if (time == RAKE_IMPACT) {
                this.hitArc(level, RAKE_REACH, 80.0, damage * 0.85F, 1.2);
            }
            return;
        }
        if (time == GRAB_IMPACT - 8) {
            this.playSound(Dushi.sound("entity.longarm.reach"), 1.2F, 0.95F);
        }
        if (time == GRAB_IMPACT) {
            this.hitArc(level, REACH, 24.0, damage, 0.0);
        }
    }

    @Override
    protected void struck(ServerLevel level, LivingEntity victim) {
        if (this.attackId() == GRAB) {
            // drawn in to the wall
            Vec3 to = new Vec3(this.getX() - victim.getX(), 0.0, this.getZ() - victim.getZ());
            double far = to.length();
            if (far > 1.6) {
                to = to.scale(Math.min(1.1, far * 0.28) / far);
                shove(victim, new Vec3(to.x, 0.22, to.z));
            }
            this.playSound(Dushi.sound("entity.longarm.take"), 1.2F, 1.0F);
        }
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING) || source.is(DamageTypeTags.IS_FALL) || super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.leaveMaybe(level, Dushi.NAMELESS_SOUL.get(), 0.3F);
        this.leaveMaybe(level, Dushi.GREAT_SOUL.get(), 0.08F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.longarm.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.longarm.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.longarm.death");
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (!Float.isNaN(this.homeYaw)) {
            output.putFloat("HomeYaw", this.homeYaw);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        float yaw = input.getFloatOr("HomeYaw", Float.NaN);
        if (!Float.isNaN(yaw)) {
            this.homeYaw = yaw;
        }
    }
}
