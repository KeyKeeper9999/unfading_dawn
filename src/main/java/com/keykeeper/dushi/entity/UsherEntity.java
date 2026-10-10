package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.PlayerSoul;

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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * An usher of the Sky Beneath: three men and a half high, in a coat of the night of the place, with hands folded over
 * where its face should be. There is one to a stretch of the place. It knows who has come in from the moment they do,
 * and comes for them - at a walk, always at a walk, through whatever is between; it does not run and it does not
 * stop, and a blow does not check it. Where it is, the light there is goes down.
 * <p>
 * Near enough, the hands come off its face, and it takes hold: whoever is before it then is lifted and thrown. Out
 * of arm's reach of that it brings one arm down from its full height, and no shield holds it.
 */
public class UsherEntity extends SoulsMob {
    public static final int GRASP = 1;
    public static final int GRASP_DURATION = 70;
    /** The hands are off its face by now, */
    public static final int GRASP_OPEN = 16;
    /** it closes them on whoever is there, */
    public static final int GRASP_IMPACT = 32;
    /** and has lifted and let go. */
    public static final int GRASP_THROW = 44;
    public static final int REACH = 2;
    public static final int REACH_DURATION = 58;
    public static final int REACH_IMPACT = 28;
    public static final double GRASP_RANGE = 3.1;
    public static final double REACH_RANGE = 4.8;
    /** How far its dark goes. */
    public static final double DIM = 9.0;

    private LivingEntity held;

    public UsherEntity(EntityType<? extends UsherEntity> type, Level level) {
        super(type, level);
        this.xpReward = 60;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 620.0).add(Attributes.ATTACK_DAMAGE, 34.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ARMOR, 4.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 64.0).add(Attributes.STEP_HEIGHT, 1.1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 24.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // (it does not need to see them)
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public int soulReward() {
        return 4200;
    }

    @Override
    protected int attackDuration(int id) {
        return id == GRASP ? GRASP_DURATION : REACH_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == GRASP ? GRASP_IMPACT - 8 : REACH_IMPACT - 9;
    }

    @Override
    protected int attackPause(int finishedId) {
        return 22 + this.random.nextInt(14);
    }

    @Override
    public double stopDistance() {
        return 2.0;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance < GRASP_RANGE - 0.6) {
            return this.random.nextInt(4) == 0 ? REACH : GRASP;
        }
        return distance < REACH_RANGE - 0.6 ? REACH : 0;
    }

    @Override
    public void startAttack(int id) {
        super.startAttack(id);
        this.held = null;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.tickCount % 20 == 0 && this.isAlive() && this.getTarget() != null) {
            // the light goes down round it
            for (Player player : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(DIM, 4.0, DIM), this::isFoe)) {
                if (!PlayerSoul.of(player).atHearth && player.distanceToSqr(this) < DIM * DIM) {
                    player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, false, false), this);
                }
            }
        }
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (id == REACH) {
            if (time == REACH_IMPACT - 6) {
                this.playSound(Dushi.sound("entity.usher.reach"), 1.4F, 1.0F);
            }
            if (time == REACH_IMPACT) {
                this.hitArc(level, this.facing(), REACH_RANGE, 20.0, damage * 1.2F, 0.5, true);
                Vec3 at = this.position().add(this.facing().scale(REACH_RANGE - 0.6));
                level.sendParticles(ParticleTypes.SMOKE, at.x, at.y + 0.2, at.z, 14, 0.4, 0.1, 0.4, 0.02);
            }
            return;
        }
        if (time == 2) {
            this.playSound(Dushi.sound("entity.usher.open"), 1.6F, 1.0F);
        }
        if (time == GRASP_IMPACT) {
            // whoever is before it and near enough is taken hold of: one of them
            Vec3 facing = this.facing();
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(GRASP_RANGE + 1.0, 1.0, GRASP_RANGE + 1.0), this::isFoe)) {
                Vec3 to = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ());
                double far = to.length() - victim.getBbWidth() * 0.5 - this.getBbWidth() * 0.5;
                if (far > GRASP_RANGE || (to.lengthSqr() > 0.5 && to.normalize().dot(facing) < 0.55)) {
                    continue;
                }
                if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage * 0.5F)) {
                    this.held = victim;
                    this.playSound(Dushi.sound("entity.usher.take"), 1.6F, 1.0F);
                    break;
                }
            }
        }
        if (this.held != null && this.held.isAlive() && time > GRASP_IMPACT && time < GRASP_THROW) {
            // lifted: held before its face, off the ground
            Vec3 hold = this.position().add(this.facing().scale(1.5)).add(0.0, 2.6, 0.0);
            Vec3 pull = hold.subtract(this.held.position());
            shove(this.held, new Vec3(pull.x * 0.4, Math.max(-0.2, Math.min(0.5, pull.y * 0.4)), pull.z * 0.4));
        }
        if (time == GRASP_THROW && this.held != null && this.held.isAlive()) {
            Vec3 away = this.facing();
            this.held.hurtServer(level, this.damageSources().mobAttack(this), damage * 0.7F);
            shove(this.held, new Vec3(away.x * 1.25, 0.45, away.z * 1.25));
            this.playSound(Dushi.sound("entity.usher.reach"), 1.4F, 0.8F);
            this.held = null;
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && this.held != null && this.getHealth() < this.getMaxHealth() * 0.25F) {
            this.held = null;                                      // (near its end it cannot keep hold)
        }
        return hurt;
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.leave(level, Dushi.GREAT_SOUL.get(), 1, 2);
        this.leaveMaybe(level, Dushi.RETURN_EMBER.get(), 0.3F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.usher.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.usher.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.usher.death");
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(Dushi.sound("entity.usher.step"), 0.7F, 0.9F + this.random.nextFloat() * 0.2F);
    }
}
