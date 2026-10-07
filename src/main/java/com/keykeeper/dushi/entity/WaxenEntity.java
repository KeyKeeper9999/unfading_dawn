package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The waxen: what the wax of the skete's candles ran together into. Every night that would not end they burned
 * candles to stay awake, and the wax of all of them lies where it fell, by the vats where they poured more; some of
 * the heaps have got up. A heap as high as a man, a head with two pits in it, arms like posts - and the stubs still
 * alight where they stuck, so that it is seen a long way off among the firs, a walking rack of small flames.
 * <p>
 * It is slow and there is a great deal of it. Near, both arms come down together on the ground before it. A little
 * further off it draws itself up and spews what is in it, hot: a rope of wax five paces long that it holds on whoever
 * it is after - not much hurt in it, but it sets on them and they walk as if in a dream. When it is killed it bursts.
 */
public class WaxenEntity extends SoulsMob {
    public static final int SLAM = 1;
    public static final int SLAM_DURATION = 44;
    public static final int SLAM_IMPACT = 22;
    public static final int SPEW = 2;
    public static final int SPEW_DURATION = 62;
    public static final int SPEW_FROM = 18;
    public static final int SPEW_UNTIL = 44;
    public static final double SPEW_REACH = 5.2;
    /** How long wax that has set on somebody holds them, in ticks. */
    public static final int SETS_FOR = 50;

    private int spewCooldown = 40;

    public WaxenEntity(EntityType<? extends WaxenEntity> type, Level level) {
        super(type, level);
        this.xpReward = 16;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 260.0).add(Attributes.ATTACK_DAMAGE, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.17).add(Attributes.ARMOR, 4.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int soulReward() {
        return 1300;
    }

    @Override
    protected int attackDuration(int id) {
        return id == SPEW ? SPEW_DURATION : SLAM_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == SPEW ? SPEW_FROM : SLAM_IMPACT - 8;
    }

    @Override
    protected int attackPause(int finishedId) {
        return 20 + this.random.nextInt(14);
    }

    @Override
    public double stopDistance() {
        return 2.0;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance < SPEW_REACH - 0.6 && distance > 1.6 && this.spewCooldown <= 0 && this.hasLineOfSight(target)) {
            this.spewCooldown = 110 + this.random.nextInt(60);
            return SPEW;
        }
        return distance < 2.6 ? SLAM : 0;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.spewCooldown > 0) {
            this.spewCooldown--;
        }
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (id == SLAM) {
            if (time == SLAM_IMPACT - 4) {
                this.playSound(Dushi.sound("player.weapon_swing_heavy"), 1.0F, 0.8F);
            }
            if (time == SLAM_IMPACT) {
                Vec3 at = this.position().add(this.facing().scale(2.0));
                this.hitAround(level, at, 2.3, damage * 1.25F, 0.8, this.damageSources().mobAttack(this));
                splash(level, at, 30, 0.9);
                level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.waxen.slam"), this.getSoundSource(), 1.5F, 0.9F + this.random.nextFloat() * 0.2F);
            }
            return;
        }
        if (time == 4) {
            this.playSound(Dushi.sound("entity.waxen.gather"), 1.6F, 1.0F);
        }
        if (time == SPEW_FROM) {
            this.playSound(Dushi.sound("entity.waxen.spew"), 1.8F, 1.0F);
        }
        if (time >= SPEW_FROM && time <= SPEW_UNTIL) {
            // it turns after whoever it is spewing at, but slowly: running round it gets one out of the rope
            LivingEntity target = this.getTarget();
            if (target != null) {
                float want = (float) (net.minecraft.util.Mth.atan2(target.getZ() - this.getZ(), target.getX() - this.getX()) * (180.0 / Math.PI)) - 90.0F;
                float yaw = net.minecraft.util.Mth.approachDegrees(this.getYRot(), want, 2.2F);
                this.setYRot(yaw);
                this.setYBodyRot(yaw);
                this.setYHeadRot(yaw);
            }
            Vec3 mouth = this.position().add(0.0, this.getBbHeight() * 0.8, 0.0).add(this.facing().scale(0.6));
            for (int i = 1; i <= 6; i++) {
                Vec3 at = mouth.add(this.facing().scale(i * SPEW_REACH / 6.5)).add(0.0, -0.18 * i, 0.0);
                level.sendParticles(ParticleTypes.FALLING_HONEY, at.x, at.y, at.z, 2, 0.12 + 0.03 * i, 0.1, 0.12 + 0.03 * i, 0.0);
                if (i % 2 == 0) {
                    level.sendParticles(ParticleTypes.SMOKE, at.x, at.y, at.z, 1, 0.1, 0.1, 0.1, 0.01);
                }
            }
            if ((time - SPEW_FROM) % 5 == 0) {
                this.hitArc(level, SPEW_REACH, 20.0, damage * 0.3F, 0.0);
            }
        }
    }

    /** Hot wax sets on whoever it lands on: they are slow until it cracks off. */
    @Override
    protected void struck(ServerLevel level, LivingEntity victim) {
        if (this.attackId() == SPEW) {
            victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SETS_FOR, 1, false, true, true), this);
        }
    }

    private static void splash(ServerLevel level, Vec3 at, int count, double spread) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HONEYCOMB_BLOCK.defaultBlockState()), at.x, at.y + 0.3, at.z, count, spread, 0.3, spread, 0.15);
        level.sendParticles(ParticleTypes.LANDING_HONEY, at.x, at.y + 0.1, at.z, count / 3, spread, 0.05, spread, 0.0);
        level.sendParticles(ParticleTypes.SMOKE, at.x, at.y + 0.4, at.z, count / 4, spread * 0.7, 0.2, spread * 0.7, 0.01);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            // what was holding it up lets go: it goes down all at once, and what is near is spattered
            Vec3 at = this.position();
            splash(level, at.add(0.0, 0.6, 0.0), 70, 1.2);
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(2.6, 1.0, 2.6), this::isFoe)) {
                if (victim.hurtServer(level, this.damageSources().mobAttack(this), 6.0F)) {
                    victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SETS_FOR, 1, false, true, true), this);
                }
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.leave(level, Dushi.WAX.get(), 1, 3);
        this.leaveMaybe(level, Dushi.NAMELESS_SOUL.get(), 0.3F);
        this.leaveMaybe(level, Dushi.GREAT_SOUL.get(), 0.12F);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.waxen.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.waxen.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.waxen.death");
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
    }
}
