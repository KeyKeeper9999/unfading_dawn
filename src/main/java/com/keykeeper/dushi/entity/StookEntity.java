package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * A stook: sheaves stood together to dry, with one laid over them for a cap, as they stand all over a cut field - and
 * this one has stood so long in a day that does not end that it has begun to walk (phase 1: standing, a stook). It
 * goes on the butts of its sheaves, rocking, and there is nothing in it but straw and whatever nested there.
 * <p>
 * It falls on whoever is before it, with all its weight, and takes a moment to get up. It shakes itself, and the chaff
 * that comes out of it blinds. And it is straw: alight, it burns down fast - but it runs while it burns, and whatever
 * it falls on then is alight too.
 */
public class StookEntity extends SoulsMob {
    public static final int RISE = 1;
    public static final int RISE_DURATION = 20;
    public static final int FALL = 2;
    public static final int FALL_DURATION = 64;
    public static final int FALL_IMPACT = 24;
    /** Until this tick it lies where it fell. */
    public static final int FALL_LAIN = 44;
    public static final int SHED = 3;
    public static final int SHED_DURATION = 44;
    public static final int SHED_BURST = 20;
    public static final double REACH = 2.9;
    public static final double SHED_RADIUS = 3.2;
    public static final double WAKES_AT = 4.5;

    private int shedCooldown = 60;

    public StookEntity(EntityType<? extends StookEntity> type, Level level) {
        super(type, level);
        this.xpReward = 14;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 290.0).add(Attributes.ATTACK_DAMAGE, 25.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24).add(Attributes.ARMOR, 0.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 18.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false, (entity, level) -> this.up()));
    }

    /** Walking. */
    public boolean up() {
        return this.phase() >= 2;
    }

    @Override
    public int soulReward() {
        return 1200;
    }

    @Override
    protected int attackDuration(int id) {
        return id == RISE ? RISE_DURATION : id == FALL ? FALL_DURATION : SHED_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == RISE ? RISE_DURATION : id == FALL ? FALL_IMPACT - 9 : 0;
    }

    @Override
    protected int attackPause(int finishedId) {
        return finishedId == RISE ? 4 : 16 + this.random.nextInt(14);
    }

    @Override
    public double stopDistance() {
        return 1.6;
    }

    @Override
    public boolean overlooks(LivingEntity target) {
        return !this.up() || super.overlooks(target);
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance < SHED_RADIUS - 0.6 && this.shedCooldown <= 0 && !this.isOnFire()) {
            this.shedCooldown = 180 + this.random.nextInt(100);
            return SHED;
        }
        return distance < REACH - 0.6 ? FALL : 0;
    }

    public void rise(LivingEntity cause) {
        if (!this.up() && this.isAlive()) {
            this.setPhase(2);
            this.startAttack(RISE);
            if (cause != null) {
                this.setTarget(cause);
            }
        }
    }

    private void straw(ServerLevel level, Vec3 at, int count, double spread) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HAY_BLOCK.defaultBlockState()), at.x, at.y, at.z, count, spread, 0.4, spread, 0.12);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (!this.up()) {
            this.getNavigation().stop();
            this.setDeltaMovement(0.0, Math.min(0.0, this.getDeltaMovement().y), 0.0);
            if (this.tickCount % 5 == 0) {
                Player near = level.getNearestPlayer(this, WAKES_AT);
                if (near != null && this.isFoe(near) && !com.keykeeper.dushi.PlayerSoul.of(near).atHearth && this.hasLineOfSight(near)) {
                    this.rise(near);
                }
            }
        }
        super.customServerAiStep(level);
        this.shedCooldown = Math.max(0, this.shedCooldown - 1);
        if (this.isOnFire() && this.up()) {
            // it runs while it burns
            if (this.attackId() == 0 && this.getTarget() != null) {
                this.getNavigation().setSpeedModifier(1.7);
            }
            if (this.tickCount % 4 == 0) {
                FlourCloudEntity.kindle(level, this.position(), 1.0);
            }
        }
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (id == RISE) {
            if (time == 3) {
                this.playSound(Dushi.sound("entity.stook.rustle"), 1.4F, 0.8F);
                this.straw(level, this.position().add(0.0, 0.5, 0.0), 14, 0.5);
            }
        } else if (id == FALL) {
            if (time == FALL_IMPACT - 6) {
                this.playSound(Dushi.sound("entity.stook.rustle"), 1.2F, 1.2F);
            }
            if (time == FALL_IMPACT) {
                this.hitArc(level, this.facing(), REACH, 40.0, damage * 1.3F, 0.6, true);
                Vec3 at = this.position().add(this.facing().scale(1.8));
                this.straw(level, at.add(0.0, 0.3, 0.0), 30, 0.8);
                level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.stook.fall"), this.getSoundSource(), 1.5F, 0.9F + this.random.nextFloat() * 0.2F);
            }
        } else {
            if (time == SHED_BURST - 14) {
                this.playSound(Dushi.sound("entity.stook.shed"), 1.4F, 1.0F);
            }
            if (time == SHED_BURST) {
                Vec3 at = this.position();
                this.hitAround(level, at, SHED_RADIUS, damage * 0.4F, 0.3, this.damageSources().mobAttack(this));
                level.sendParticles(ParticleTypes.WHITE_ASH, at.x, at.y + 1.2, at.z, 120, 1.6, 0.8, 1.6, 0.02);
                this.straw(level, at.add(0.0, 1.2, 0.0), 50, 1.4);
            }
        }
    }

    @Override
    protected void struck(ServerLevel level, LivingEntity victim) {
        if (this.attackId() == SHED) {
            if (victim instanceof Player) {
                victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 0, false, false, true));
            }
        } else if (this.isOnFire()) {
            victim.igniteForSeconds(4.0F);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean fire = source.is(DamageTypeTags.IS_FIRE);
        // lying where it fell, it is a heap of straw and is cut as one
        boolean lying = this.attackId() == FALL && this.attackTime > FALL_IMPACT && this.attackTime < FALL_LAIN;
        boolean hurt = super.hurtServer(level, source, fire ? damage * 3.0F : lying ? damage * 1.5F : damage);
        if (hurt && this.isAlive()) {
            if (!fire) {
                this.straw(level, this.position().add(0.0, 1.0, 0.0), 6, 0.4);
            }
            if (!this.up()) {
                this.rise(source.getEntity() instanceof LivingEntity attacker && this.isFoe(attacker) ? attacker : null);
            }
        }
        return hurt;
    }

    @Override
    public boolean isPushable() {
        return this.up();
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        if (!this.isOnFire()) {
            this.leave(level, Dushi.RYE_STRAW.get(), 2, 4);                // (one that burnt down leaves ash)
        }
        this.leaveMaybe(level, Dushi.NAMELESS_SOUL.get(), 0.2F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.up() ? Dushi.sound("entity.stook.rustle") : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.stook.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.stook.death");
    }
}
