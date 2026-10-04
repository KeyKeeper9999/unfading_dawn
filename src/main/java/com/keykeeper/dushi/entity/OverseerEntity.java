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
import net.minecraft.world.entity.EntitySpawnReason;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The Overseer of the Stockade: a jailer twice the height of a man, his head locked in an iron cage, with the great key
 * of the stockade for a hammer and a lantern on a chain. First boss of the mod.
 * <p>
 * Phase one: an overhead slam, a wide sweep and a leap that shakes the ground. Below half health he roars, the lantern
 * and the cage catch fire, prisoners crawl out to help him, his blows come faster, the slam leaves fire and he starts
 * throwing the chain to drag his target back in.
 */
public class OverseerEntity extends SoulsMob {
    public static final int SLAM = 1;
    public static final int SLAM_DURATION = 40;
    public static final int SLAM_IMPACT = 20;
    public static final int SWEEP = 2;
    public static final int SWEEP_DURATION = 36;
    public static final int SWEEP_IMPACT = 16;
    public static final int LEAP = 3;
    public static final int LEAP_DURATION = 56;
    public static final int LEAP_JUMP = 12;
    public static final int CHAIN = 4;
    public static final int CHAIN_DURATION = 44;
    public static final int CHAIN_THROW = 18;
    public static final int ROAR = 5;
    public static final int ROAR_DURATION = 60;
    public static final int ROAR_PEAK = 20;

    private static final double CHAIN_RANGE = 12.0;

    private int leapCooldown = 100;
    private int chainCooldown = 80;
    private boolean leapLanded;

    public OverseerEntity(EntityType<? extends OverseerEntity> type, Level level) {
        super(type, level);
        this.xpReward = 120;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 260.0).add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.ARMOR, 6.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 40.0).add(Attributes.STEP_HEIGHT, 1.25);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public int soulReward() {
        return 3000;
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    protected int attackDuration(int id) {
        return switch (id) {
            case SLAM -> SLAM_DURATION;
            case SWEEP -> SWEEP_DURATION;
            case LEAP -> LEAP_DURATION;
            case CHAIN -> CHAIN_DURATION;
            default -> ROAR_DURATION;
        };
    }

    @Override
    protected int trackUntil(int id) {
        return switch (id) {
            case SLAM -> SLAM_IMPACT - 7;
            case SWEEP -> SWEEP_IMPACT - 6;
            case LEAP -> LEAP_JUMP;
            case CHAIN -> CHAIN_THROW - 2;
            default -> 0;
        };
    }

    @Override
    protected int attackPause(int finishedId) {
        return this.phase() >= 2 ? 6 + this.random.nextInt(8) : 14 + this.random.nextInt(12);
    }

    @Override
    public double stopDistance() {
        return 2.2;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (this.phase() >= 2 && distance > 5.0 && distance < CHAIN_RANGE - 1.0 && this.chainCooldown <= 0 && this.hasLineOfSight(target)) {
            this.chainCooldown = 130;
            return CHAIN;
        }
        if (distance > 6.0 && distance < 15.0 && this.leapCooldown <= 0 && this.onGround()) {
            this.leapCooldown = 150;
            return LEAP;
        }
        if (distance < 3.6) {
            return this.random.nextInt(100) < 55 ? SLAM : SWEEP;
        }
        return 0;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.phase() == 1 && this.getHealth() < this.getMaxHealth() * 0.5F && this.attackId() != ROAR && this.isAlive()) {
            this.setPhase(2);
            this.startAttack(ROAR);
            var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) {
                speed.setBaseValue(0.3);
            }
        }
        super.customServerAiStep(level);
        if (this.leapCooldown > 0) {
            this.leapCooldown--;
        }
        if (this.chainCooldown > 0) {
            this.chainCooldown--;
        }
        if (this.phase() >= 2 && this.tickCount % 4 == 0) {
            level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + this.getBbHeight() * 0.93, this.getZ(), 1, 0.25, 0.2, 0.25, 0.01);
        }
    }

    @Override
    protected void onReset() {
        super.onReset();
        var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(0.25);
        }
    }

    @Override
    public void startAttack(int id) {
        super.startAttack(id);
        this.leapLanded = false;
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        boolean enraged = this.phase() >= 2;
        switch (id) {
            case SLAM -> {
                if (time == SLAM_IMPACT - 6) {
                    this.playSound(Dushi.sound("entity.overseer.swing"), 2.0F, 0.8F);
                }
                if (time == SLAM_IMPACT) {
                    this.hitArc(level, 4.8, 38.0, damage * 1.2F, 1.0);
                    Vec3 at = this.position().add(this.facing().scale(3.6));
                    this.quake(level, at, 2.2);
                    if (enraged) {
                        DamageSource fire = this.damageSources().mobAttack(this);
                        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(at, at).inflate(3.2, 2.0, 3.2), this::isFoe)) {
                            if (victim.hurtServer(level, fire, 5.0F)) {
                                victim.igniteForSeconds(4.0F);
                            }
                        }
                        level.sendParticles(ParticleTypes.FLAME, at.x, at.y + 0.2, at.z, 70, 1.4, 0.2, 1.4, 0.05);
                    }
                }
            }
            case SWEEP -> {
                if (time == SWEEP_IMPACT - 4) {
                    this.playSound(Dushi.sound("entity.overseer.swing"), 2.0F, 1.0F);
                }
                if (time == SWEEP_IMPACT) {
                    this.hitArc(level, 5.0, 110.0, damage, 1.7);
                    for (int i = -2; i <= 2; i++) {
                        Vec3 dir = this.facing().yRot((float) Math.toRadians(i * 35.0));
                        level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + dir.x * 3.5, this.getY() + 1.6, this.getZ() + dir.z * 3.5, 1, 0.0, 0.0, 0.0, 0.0);
                    }
                }
            }
            case LEAP -> {
                LivingEntity target = this.getTarget();
                if (time == LEAP_JUMP) {
                    Vec3 to = target == null ? this.facing().scale(6.0) : target.position().subtract(this.position());
                    Vec3 flat = new Vec3(to.x, 0.0, to.z);
                    double length = Math.min(13.0, flat.length());
                    // About sixteen ticks in the air.
                    Vec3 jump = flat.lengthSqr() < 1.0E-4 ? Vec3.ZERO : flat.normalize().scale(length / 16.0);
                    this.setDeltaMovement(jump.x, 0.95, jump.z);
                    this.needsSync = true;
                    this.playSound(Dushi.sound("entity.overseer.swing"), 2.0F, 0.6F);
                }
                if (!this.leapLanded && time > LEAP_JUMP + 6 && (this.onGround() || time >= LEAP_DURATION - 14)) {
                    this.leapLanded = true;
                    this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
                    this.hitAround(level, this.position(), 5.5, damage, 1.5, this.damageSources().mobAttack(this));
                    this.quake(level, this.position(), 5.5);
                    // The landing leaves him open for a moment.
                    this.attackTime = Math.max(this.attackTime, LEAP_DURATION - 14);
                }
            }
            case CHAIN -> {
                if (time == CHAIN_THROW - 8) {
                    this.playSound(Dushi.sound("entity.overseer.chain"), 2.0F, 1.0F);
                }
                if (time == CHAIN_THROW) {
                    this.throwChain(level);
                }
            }
            default -> {
                if (time == ROAR_PEAK) {
                    this.playSound(Dushi.sound("entity.overseer.roar"), 4.0F, 1.0F);
                    level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + this.getBbHeight() * 0.9, this.getZ(), 80, 0.6, 0.6, 0.6, 0.12);
                    level.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY() + this.getBbHeight() * 0.9, this.getZ(), 16, 0.5, 0.5, 0.5, 0.0);
                    // Everyone close is thrown back, unharmed.
                    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(7.0, 3.0, 7.0), this::isFoe)) {
                        Vec3 away = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ());
                        away = away.lengthSqr() < 1.0E-4 ? this.facing() : away.normalize();
                        shove(victim, new Vec3(away.x * 1.3, 0.45, away.z * 1.3));
                    }
                    for (int i = 0; i < 3; i++) {
                        double angle = this.random.nextDouble() * Math.PI * 2.0;
                        BlockPos at = BlockPos.containing(this.getX() + Math.cos(angle) * 4.0, this.getY(), this.getZ() + Math.sin(angle) * 4.0);
                        WitheredEntity helper = Dushi.WITHERED.get(WitheredEntity.Kind.PRISONER).get().spawn(level, at, EntitySpawnReason.MOB_SUMMONED);
                        if (helper != null) {
                            helper.setTarget(this.getTarget());
                            level.sendParticles(ParticleTypes.LARGE_SMOKE, helper.getX(), helper.getY() + 1.0, helper.getZ(), 12, 0.3, 0.6, 0.3, 0.01);
                        }
                    }
                }
            }
        }
    }

    /** Dust and a thud where something heavy hits the floor. */
    private void quake(ServerLevel level, Vec3 at, double radius) {
        for (int i = 0; i < 18; i++) {
            double angle = i * Math.PI * 2.0 / 18.0;
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, at.x + Math.cos(angle) * radius * 0.5, at.y + 0.15, at.z + Math.sin(angle) * radius * 0.5,
                    0, Math.cos(angle), 0.1, Math.sin(angle), 0.08);
        }
        level.sendParticles(ParticleTypes.POOF, at.x, at.y + 0.2, at.z, 24, radius * 0.35, 0.1, radius * 0.35, 0.03);
        level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.overseer.slam"), this.getSoundSource(), 3.0F, 0.9F + this.random.nextFloat() * 0.2F);
    }

    /** The lantern chain flies straight ahead; whoever it reaches is burned and dragged to the overseer's feet. */
    private void throwChain(ServerLevel level) {
        Vec3 from = this.position().add(0.0, this.getBbHeight() * 0.6, 0.0);
        Vec3 dir = this.facing();
        LivingEntity target = this.getTarget();
        if (target != null) {
            Vec3 to = target.getBoundingBox().getCenter().subtract(from);
            if (to.lengthSqr() > 1.0E-4) {
                dir = to.normalize();
            }
        }
        for (double d = 1.5; d <= CHAIN_RANGE; d += 0.5) {
            Vec3 at = from.add(dir.scale(d));
            level.sendParticles(ParticleTypes.CRIT, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
            if (d % 2.0 == 0.0) {
                level.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, 1, 0.05, 0.05, 0.05, 0.0);
            }
        }
        DamageSource source = this.damageSources().mobAttack(this);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(CHAIN_RANGE + 1.0), this::isFoe)) {
            Vec3 to = victim.getBoundingBox().getCenter().subtract(from);
            double along = to.dot(dir);
            if (along < 1.0 || along > CHAIN_RANGE) {
                continue;
            }
            double off = to.subtract(dir.scale(along)).length();
            if (off > 0.9 + victim.getBbWidth() * 0.5 || !this.hasLineOfSight(victim)) {
                continue;
            }
            if (victim.hurtServer(level, source, 6.0F)) {
                victim.igniteForSeconds(3.0F);
                Vec3 pull = this.position().subtract(victim.position());
                double length = pull.length();
                if (length > 3.0) {
                    Vec3 flat = new Vec3(pull.x, 0.0, pull.z).normalize();
                    double speed = Math.min(2.2, (length - 2.5) * 0.28 + 0.5);
                    shove(victim, new Vec3(flat.x * speed, 0.42, flat.z * speed));
                }
            }
        }
    }

    /** The cage on his head makes him deaf to everything while he roars. */
    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING) || (this.attackId() == ROAR && !source.is(DamageTypes.GENERIC_KILL))
                || super.isInvulnerableTo(level, source);
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.spawnAtLocation(level, new ItemStack(Dushi.OVERSEER_SOUL.get()));
        this.spawnAtLocation(level, new ItemStack(Dushi.FLASK_SHARD.get()));
        this.leave(level, Dushi.RUST_SCRAP.get(), 4, 7);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            // the gate of the room it kept, even if it was led out of it to die
            BlockPos home = MobMarkBlockEntity.homeOf(this);
            FogGateBlock.lift(level, home != null ? home : this.blockPosition(), 28);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.overseer.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.overseer.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.overseer.death");
    }

    @Override
    protected float getSoundVolume() {
        return 2.5F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(Dushi.sound("entity.rust_knight.step"), 1.2F, 0.6F);
    }
}
