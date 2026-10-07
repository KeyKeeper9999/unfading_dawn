package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.block.FogGateBlock;
import com.keykeeper.dushi.block.MobMarkBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The Wallower: what grew in the mill pond of the Mire when nobody fished it any more. Half sheatfish and half newt,
 * as long as a barge and as flat, one wide mouth with four barbels to it. The boss of the Weir.
 * <p>
 * The Weir is a floor under a hand's depth of water, and that is enough for it. It snaps at what is before it and
 * clubs what is behind it with its tail. It spits clots of sludge that burst into pools one wades in - three at once
 * when it is hurt. It goes down into the water that is not deep enough to hide a rat and is gone; a ripple comes round
 * in a narrowing ring, and it comes up under the middle of the ring. And once hurt it opens its mouth and drinks: the
 * air and whoever stands before it are drawn in, and at the end of the breath the mouth shuts.
 */
public class WallowerEntity extends SoulsMob {
    public static final int SNAP = 1;
    public static final int SNAP_DURATION = 30;
    public static final int SNAP_IMPACT = 12;
    public static final int TAIL = 2;
    public static final int TAIL_DURATION = 38;
    public static final int TAIL_IMPACT = 15;
    public static final int SPIT = 3;
    public static final int SPIT_DURATION = 44;
    /** Hurt, it spits three times in one breath. */
    public static final int SPIT_DURATION_THREE = 60;
    public static final int SPIT_FIRST = 20;
    public static final int SPIT_EVERY = 8;
    public static final int DIVE = 4;
    public static final int DIVE_DURATION = 118;
    /** Under by this tick; */
    public static final int DIVE_UNDER = 18;
    /** the ring has closed and it lies still under its prey at this one - the water heaves there - */
    public static final int DIVE_STOP = 78;
    /** and comes up at this one. */
    public static final int DIVE_BREACH = 92;
    public static final int GULP = 5;
    public static final int GULP_DURATION = 76;
    public static final int GULP_FROM = 12;
    public static final int GULP_TO = 50;
    public static final int GULP_BITE = 55;

    /**
     * The game lets nothing have more than 1024 health. What it should have beyond that is in its hide: this much of
     * every blow gets through (tools/balance.py reads the number).
     */
    public static final float HIDE = 0.77F;

    private int spitCooldown = 60;
    private int diveCooldown = 180;
    private int gulpCooldown = 120;
    private int tailCooldown;
    private double diveAngle;

    public WallowerEntity(EntityType<? extends WallowerEntity> type, Level level) {
        super(type, level);
        this.xpReward = 200;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1000.0).add(Attributes.ATTACK_DAMAGE, 19.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.ARMOR, 5.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 44.0).add(Attributes.STEP_HEIGHT, 1.1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 14.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public boolean isAffectedByFluids() {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public int soulReward() {
        return 12000;
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    /** Whether it is under the water just now, where nothing reaches it. */
    public boolean under() {
        return this.attackId() == DIVE && this.attackTime >= DIVE_UNDER && this.attackTime < DIVE_BREACH;
    }

    @Override
    protected int attackDuration(int id) {
        return switch (id) {
            case SNAP -> SNAP_DURATION;
            case TAIL -> TAIL_DURATION;
            case SPIT -> this.phase() >= 2 ? SPIT_DURATION_THREE : SPIT_DURATION;
            case DIVE -> DIVE_DURATION;
            default -> GULP_DURATION;
        };
    }

    @Override
    protected int trackUntil(int id) {
        return switch (id) {
            case SNAP -> SNAP_IMPACT - 5;
            case SPIT -> SPIT_DURATION_THREE;
            case GULP -> GULP_FROM;
            default -> 0;
        };
    }

    @Override
    protected int attackPause(int finishedId) {
        return this.phase() >= 2 ? 9 + this.random.nextInt(8) : 16 + this.random.nextInt(12);
    }

    @Override
    public double stopDistance() {
        return 2.2;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        Vec3 to = new Vec3(target.getX() - this.getX(), 0.0, target.getZ() - this.getZ());
        boolean behind = to.lengthSqr() > 0.5 && to.normalize().dot(this.facing()) < -0.2;
        if (behind && distance < 5.2 && this.tailCooldown <= 0) {
            this.tailCooldown = 44;
            return TAIL;
        }
        if (this.phase() >= 2 && this.gulpCooldown <= 0 && distance > 2.0 && distance < 9.5 && this.hasLineOfSight(target)) {
            this.gulpCooldown = 260 + this.random.nextInt(100);
            return GULP;
        }
        if (this.diveCooldown <= 0 && this.onGround() && distance > 3.0) {
            this.diveCooldown = (this.phase() >= 2 ? 280 : 400) + this.random.nextInt(120);
            return DIVE;
        }
        if (this.spitCooldown <= 0 && distance > 3.6 && this.hasLineOfSight(target)) {
            this.spitCooldown = (this.phase() >= 2 ? 110 : 150) + this.random.nextInt(60);
            return SPIT;
        }
        return distance < 3.4 ? SNAP : 0;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.phase() == 1 && this.getHealth() < this.getMaxHealth() * 0.5F && this.attackId() == 0 && this.isAlive()) {
            this.setPhase(2);
            this.spitCooldown = 150;
            this.gulpCooldown = 100;
            this.startAttack(SPIT);
            this.playSound(Dushi.sound("entity.wallower.roar"), 4.0F, 1.0F);
        }
        super.customServerAiStep(level);
        this.spitCooldown = Math.max(0, this.spitCooldown - 1);
        this.diveCooldown = Math.max(0, this.diveCooldown - 1);
        this.gulpCooldown = Math.max(0, this.gulpCooldown - 1);
        this.tailCooldown = Math.max(0, this.tailCooldown - 1);
    }

    private void splash(ServerLevel level, Vec3 at, int count, double spread) {
        level.sendParticles(ParticleTypes.SPLASH, at.x, at.y + 0.2, at.z, count, spread, 0.1, spread, 0.0);
    }

    /** Where its mouth is. */
    private Vec3 mouth() {
        return this.position().add(this.facing().scale(2.8)).add(0.0, 1.0, 0.0);
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        switch (id) {
            case SNAP -> {
                if (time == SNAP_IMPACT - 4) {
                    Vec3 step = this.facing().scale(0.4);
                    this.setDeltaMovement(step.x, this.getDeltaMovement().y, step.z);
                }
                if (time == SNAP_IMPACT) {
                    this.playSound(Dushi.sound("entity.wallower.bite"), 2.2F, 1.0F);
                    this.hitArc(level, 3.6, 52.0, damage, 0.8);
                    this.splash(level, this.position().add(this.facing().scale(3.0)), 30, 0.8);
                }
            }
            case TAIL -> {
                if (time == TAIL_IMPACT - 4) {
                    this.playSound(Dushi.sound("entity.overseer.swing"), 1.8F, 0.7F);
                }
                if (time == TAIL_IMPACT) {
                    Vec3 back = this.facing().scale(-1.0);
                    this.hitArc(level, back, 5.4, 100.0, damage * 0.9F, 1.7, false);
                    this.splash(level, this.position().add(back.scale(3.6)), 60, 1.4);
                    this.playSound(Dushi.sound("entity.wallower.slap"), 2.2F, 0.9F + this.random.nextFloat() * 0.2F);
                }
            }
            case SPIT -> {
                if (time == 3) {
                    this.playSound(Dushi.sound("entity.wallower.spit"), 2.4F, this.phase() >= 2 ? 0.85F : 1.0F);
                }
                int shots = this.phase() >= 2 ? 3 : 1;
                for (int i = 0; i < shots; i++) {
                    if (time == SPIT_FIRST + i * SPIT_EVERY) {
                        this.spit(level, damage, i);
                    }
                }
            }
            case DIVE -> this.diveTick(level, time, damage);
            default -> this.gulpTick(level, time, damage);
        }
    }

    /** One clot: the first at its prey, the second and third to either side of where the prey is making for. */
    private void spit(ServerLevel level, float damage, int which) {
        LivingEntity target = this.getTarget();
        Vec3 from = this.mouth();
        Vec3 aim = target != null ? target.position() : this.position().add(this.facing().scale(8.0));
        if (target != null && which > 0) {
            Vec3 going = target.getKnownMovement().multiply(1.0, 0.0, 1.0);
            Vec3 side = new Vec3(-this.facing().z, 0.0, this.facing().x).scale(which == 1 ? 3.0 : -3.0);
            aim = aim.add(going.scale(14.0)).add(going.lengthSqr() > 0.004 ? Vec3.ZERO : side);
        }
        MudGlobEntity glob = new MudGlobEntity(level, this, damage * 0.4F);
        glob.setPos(from.x, from.y, from.z);
        double dx = aim.x - from.x;
        double dz = aim.z - from.z;
        double flat = Math.max(1.5, Math.sqrt(dx * dx + dz * dz));
        glob.shoot(dx, aim.y - from.y + flat * 0.42, dz, (float) Math.min(1.15, 0.21 * Math.sqrt(flat)), 1.0F);
        level.addFreshEntity(glob);
        this.playSound(Dushi.sound("entity.wallower.plop"), 1.6F, 1.3F);
    }

    private void diveTick(ServerLevel level, int time, float damage) {
        if (time == 1) {
            this.playSound(Dushi.sound("entity.wallower.dive"), 3.0F, 1.0F);
            this.diveAngle = this.random.nextDouble() * Math.PI * 2.0;
        }
        if (time < DIVE_UNDER) {
            this.splash(level, this.position(), 24, 1.6);
        } else if (time < DIVE_STOP) {
            // under the water it comes round its prey in a ring that narrows; the ripple over it is all that shows
            LivingEntity target = this.getTarget();
            if (target != null) {
                double closing = (time - DIVE_UNDER) / (double) (DIVE_STOP - DIVE_UNDER);
                double ring = 7.5 * (1.0 - closing) * (1.0 - closing);
                this.diveAngle += 0.16;
                Vec3 want = target.position().add(Math.cos(this.diveAngle) * ring, 0.0, Math.sin(this.diveAngle) * ring);
                Vec3 to = new Vec3(want.x - this.getX(), 0.0, want.z - this.getZ());
                if (to.lengthSqr() > 0.04) {
                    Vec3 go = to.normalize().scale(Math.min(0.85, to.length() * 0.6));
                    this.setDeltaMovement(go.x, this.getDeltaMovement().y, go.z);
                } else {
                    this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
                }
            }
            this.splash(level, this.position(), 5, 0.5);
            level.sendParticles(ParticleTypes.BUBBLE_POP, this.getX(), this.getY() + 0.2, this.getZ(), 3, 0.6, 0.0, 0.6, 0.0);
            if (time % 14 == 0) {
                this.playSound(Dushi.sound("entity.wallower.wake"), 1.6F, 0.9F + this.random.nextFloat() * 0.3F);
            }
        } else if (time < DIVE_BREACH) {
            this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
            this.splash(level, this.position(), 10 + (time - DIVE_STOP) * 3, 1.5);
            level.sendParticles(ParticleTypes.BUBBLE_POP, this.getX(), this.getY() + 0.2, this.getZ(), 8, 1.4, 0.0, 1.4, 0.0);
        } else if (time == DIVE_BREACH) {
            Vec3 at = this.position();
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(3.5, 1.5, 3.5), this::isFoe)) {
                double distance = Math.sqrt(victim.distanceToSqr(at.x, victim.getY(), at.z)) - victim.getBbWidth() * 0.5;
                if (distance <= 4.2 && victim.hurtServer(level, this.damageSources().mobAttack(this), damage * 1.5F)) {
                    Vec3 away = new Vec3(victim.getX() - at.x, 0.0, victim.getZ() - at.z);
                    away = away.lengthSqr() < 1.0E-4 ? Vec3.ZERO : away.normalize().scale(0.55);
                    shove(victim, new Vec3(away.x, 0.8, away.z));
                }
            }
            this.splash(level, at, 220, 2.4);
            level.sendParticles(ParticleTypes.CLOUD, at.x, at.y + 0.8, at.z, 30, 1.6, 0.6, 1.6, 0.05);
            this.playSound(Dushi.sound("entity.wallower.breach"), 4.0F, 1.0F);
        }
    }

    private void gulpTick(ServerLevel level, int time, float damage) {
        if (time == 2) {
            this.playSound(Dushi.sound("entity.wallower.gulp"), 3.5F, 1.0F);
        }
        Vec3 mouth = this.mouth();
        if (time >= GULP_FROM && time < GULP_TO) {
            // the air goes in, and whoever stands before the mouth goes with it
            for (int i = 0; i < 4; i++) {
                double far = 3.0 + this.random.nextDouble() * 7.0;
                double off = (this.random.nextDouble() - 0.5) * far * 0.9;
                Vec3 at = mouth.add(this.facing().scale(far)).add(-this.facing().z * off, this.random.nextDouble() * 1.6 - 0.6, this.facing().x * off);
                Vec3 in = mouth.subtract(at).normalize().scale(0.6);
                level.sendParticles(ParticleTypes.SNEEZE, at.x, at.y, at.z, 0, in.x, in.y, in.z, 1.0);
            }
            if (time % 2 == 0) {
                for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(11.0, 2.0, 11.0), this::isFoe)) {
                    Vec3 to = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ());
                    double far = to.length();
                    if (far < 1.0 || far > 11.5 || to.normalize().dot(this.facing()) < 0.6) {
                        continue;
                    }
                    Vec3 pull = new Vec3(mouth.x - victim.getX(), 0.0, mouth.z - victim.getZ()).normalize().scale(0.3);
                    shove(victim, new Vec3(pull.x, victim.onGround() ? 0.0 : victim.getDeltaMovement().y, pull.z));
                }
            }
        }
        if (time == GULP_BITE - 3) {
            Vec3 step = this.facing().scale(0.5);
            this.setDeltaMovement(step.x, this.getDeltaMovement().y, step.z);
        }
        if (time == GULP_BITE) {
            this.playSound(Dushi.sound("entity.wallower.bite"), 2.6F, 0.8F);
            this.hitArc(level, 4.0, 46.0, damage * 1.7F, 1.0);
            this.splash(level, this.position().add(this.facing().scale(3.0)), 50, 1.0);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return super.hurtServer(level, source, damage == Float.MAX_VALUE ? damage : damage * HIDE);
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return this.under() || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING) || super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean isPushable() {
        return !this.under() && super.isPushable();
    }

    /** Under the water it shoves nobody: there is only the ripple to go by. */
    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {
        if (!this.under()) {
            super.doPush(entity);
        }
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.spawnAtLocation(level, new ItemStack(Dushi.WALLOWER_SOUL.get()));
        this.spawnAtLocation(level, new ItemStack(Dushi.FLASK_SHARD.get()));
        this.leave(level, Dushi.GREAT_SOUL.get(), 1, 2);
        this.leave(level, Dushi.BONE_SCALE.get(), 2, 4);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            BlockPos home = MobMarkBlockEntity.homeOf(this);
            FogGateBlock.lift(level, home != null ? home : this.blockPosition(), 40);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.under() ? null : Dushi.sound("entity.wallower.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.wallower.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.wallower.death");
    }

    @Override
    protected float getSoundVolume() {
        return 2.5F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(Dushi.sound("entity.wallower.wake"), 0.8F, 1.3F);
    }
}
