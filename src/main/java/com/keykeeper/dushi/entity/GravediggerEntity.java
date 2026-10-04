package com.keykeeper.dushi.entity;

import java.util.List;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.block.FogGateBlock;
import com.keykeeper.dushi.block.MobMarkBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The Gravedigger of the borough: a gaunt giant in a long coat and a wide hat, a coffin strapped to his back, who went
 * on burying the town after the town was dead. The boss of the churchyard, and the first a traveller meets.
 * <p>
 * A wide sweep of the spade, an overhead blow that sticks in the ground and leaves him open, a spadeful of earth in the
 * eyes, and a shoulder-first rush across the yard. Below half health he strikes the ground and those he buried last
 * climb out to help him; he does it again whenever he is left alone.
 */
public class GravediggerEntity extends SoulsMob {
    public static final int SWEEP = 1;
    public static final int SWEEP_DURATION = 36;
    public static final int SWEEP_IMPACT = 16;
    public static final int SLAM = 2;
    public static final int SLAM_DURATION = 50;
    public static final int SLAM_IMPACT = 20;
    public static final int DIRT = 3;
    public static final int DIRT_DURATION = 38;
    public static final int DIRT_THROW = 18;
    public static final int RUSH = 4;
    public static final int RUSH_DURATION = 46;
    public static final int RUSH_START = 14;
    public static final int RUSH_END = 28;
    public static final int RAISE = 5;
    public static final int RAISE_DURATION = 56;
    public static final int RAISE_PEAK = 24;

    private static final double DIRT_RANGE = 7.5;

    private int dirtCooldown = 60;
    private int rushCooldown = 120;
    private int raiseCooldown;
    private boolean rushHit;

    public GravediggerEntity(EntityType<? extends GravediggerEntity> type, Level level) {
        super(type, level);
        this.xpReward = 60;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 130.0).add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.ARMOR, 2.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
                .add(Attributes.FOLLOW_RANGE, 36.0).add(Attributes.STEP_HEIGHT, 1.25);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 14.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public int soulReward() {
        return 1500;
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    protected int attackDuration(int id) {
        return switch (id) {
            case SWEEP -> SWEEP_DURATION;
            case SLAM -> SLAM_DURATION;
            case DIRT -> DIRT_DURATION;
            case RUSH -> RUSH_DURATION;
            default -> RAISE_DURATION;
        };
    }

    @Override
    protected int trackUntil(int id) {
        return switch (id) {
            case SWEEP -> SWEEP_IMPACT - 6;
            case SLAM -> SLAM_IMPACT - 7;
            case DIRT -> DIRT_THROW - 3;
            case RUSH -> RUSH_START;
            default -> 0;
        };
    }

    @Override
    protected int attackPause(int finishedId) {
        return this.phase() >= 2 ? 8 + this.random.nextInt(8) : 16 + this.random.nextInt(12);
    }

    @Override
    public double stopDistance() {
        return 2.0;
    }

    /** Those he has raised and who are still on their feet near him. */
    private int helpers(ServerLevel level) {
        return level.getEntitiesOfClass(WitheredEntity.class, this.getBoundingBox().inflate(20.0, 6.0, 20.0),
                mob -> mob.isAlive() && MobMarkBlockEntity.homeOf(mob) == null).size();
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (this.phase() >= 2 && this.raiseCooldown <= 0 && this.level() instanceof ServerLevel level && this.helpers(level) == 0) {
            this.raiseCooldown = 500;
            return RAISE;
        }
        if (distance > 6.0 && distance < 14.0 && this.rushCooldown <= 0 && this.hasLineOfSight(target)) {
            this.rushCooldown = 160;
            return RUSH;
        }
        if (distance > 3.0 && distance < DIRT_RANGE - 1.0 && this.dirtCooldown <= 0 && this.hasLineOfSight(target)) {
            this.dirtCooldown = 140;
            return DIRT;
        }
        if (distance < 3.4) {
            return this.random.nextInt(100) < 60 ? SWEEP : SLAM;
        }
        return 0;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.phase() == 1 && this.getHealth() < this.getMaxHealth() * 0.5F && this.attackId() == 0 && this.isAlive()) {
            this.setPhase(2);
            this.raiseCooldown = 500;
            this.startAttack(RAISE);
        }
        super.customServerAiStep(level);
        if (this.dirtCooldown > 0) {
            this.dirtCooldown--;
        }
        if (this.rushCooldown > 0) {
            this.rushCooldown--;
        }
        if (this.raiseCooldown > 0) {
            this.raiseCooldown--;
        }
    }

    @Override
    public void startAttack(int id) {
        super.startAttack(id);
        this.rushHit = false;
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        switch (id) {
            case SWEEP -> {
                if (time == SWEEP_IMPACT - 4) {
                    this.playSound(Dushi.sound("entity.overseer.swing"), 1.6F, 1.25F);
                }
                if (time == SWEEP_IMPACT) {
                    this.hitArc(level, 4.2, 105.0, damage, 1.2);
                    for (int i = -2; i <= 2; i++) {
                        Vec3 dir = this.facing().yRot((float) Math.toRadians(i * 32.0));
                        level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + dir.x * 3.0, this.getY() + 1.4, this.getZ() + dir.z * 3.0, 1, 0.0, 0.0, 0.0, 0.0);
                    }
                }
            }
            case SLAM -> {
                if (time == SLAM_IMPACT - 6) {
                    this.playSound(Dushi.sound("entity.overseer.swing"), 1.6F, 1.0F);
                }
                if (time == SLAM_IMPACT) {
                    this.hitArc(level, 4.4, 30.0, damage * 1.5F, 0.8);
                    Vec3 at = this.position().add(this.facing().scale(3.2));
                    this.earth(level, at, 40, 1.0);
                    level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.gravedigger.dig"), this.getSoundSource(), 2.0F, 0.8F);
                }
            }
            case DIRT -> {
                if (time == DIRT_THROW - 10) {
                    this.playSound(Dushi.sound("entity.gravedigger.dig"), 1.6F, 1.2F);
                }
                if (time == DIRT_THROW) {
                    this.throwDirt(level, damage);
                }
            }
            case RUSH -> {
                if (time == RUSH_START) {
                    this.playSound(Dushi.sound("entity.gravedigger.ambient"), 2.5F, 0.7F);
                }
                if (time >= RUSH_START && time < RUSH_END) {
                    Vec3 run = this.facing().scale(0.62);
                    this.setDeltaMovement(run.x, this.getDeltaMovement().y, run.z);
                    if (time % 3 == 0) {
                        this.earth(level, this.position(), 6, 0.4);
                    }
                    if (!this.rushHit && this.hitArc(level, 1.6, 70.0, damage * 1.2F, 2.2) > 0) {
                        this.rushHit = true;
                    }
                }
                if (time == RUSH_END) {
                    this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
                }
            }
            default -> {
                if (time == RAISE_PEAK) {
                    this.playSound(Dushi.sound("entity.gravedigger.raise"), 3.0F, 1.0F);
                    this.earth(level, this.position().add(this.facing().scale(1.5)), 50, 1.4);
                    int count = 2 + (this.tier() > 0 ? 1 : 0);
                    for (int i = 0; i < count; i++) {
                        double angle = this.random.nextDouble() * Math.PI * 2.0;
                        double far = 3.5 + this.random.nextDouble() * 2.5;
                        BlockPos at = BlockPos.containing(this.getX() + Math.cos(angle) * far, this.getY(), this.getZ() + Math.sin(angle) * far);
                        WitheredEntity helper = Dushi.WITHERED.get(WitheredEntity.Kind.TOWNSMAN).get().spawn(level, at, EntitySpawnReason.MOB_SUMMONED);
                        if (helper != null) {
                            helper.setTier(this.tier());
                            helper.setTarget(this.getTarget());
                            this.earth(level, helper.position(), 24, 0.5);
                        }
                    }
                }
            }
        }
    }

    /** Earth thrown up from the ground. */
    private void earth(ServerLevel level, Vec3 at, int count, double spread) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COARSE_DIRT.defaultBlockState()), at.x, at.y + 0.2, at.z, count,
                spread, 0.2, spread, 0.15);
        level.sendParticles(ParticleTypes.POOF, at.x, at.y + 0.2, at.z, Math.max(1, count / 5), spread * 0.6, 0.1, spread * 0.6, 0.02);
    }

    /** A spadeful of grave earth in a cone ahead: it hurts little, but whoever it catches sees nothing and moves like in a dream. */
    private void throwDirt(ServerLevel level, float damage) {
        Vec3 from = this.position().add(0.0, 1.6, 0.0);
        Vec3 dir = this.facing();
        for (double d = 1.5; d <= DIRT_RANGE; d += 1.0) {
            Vec3 at = from.add(dir.scale(d));
            this.earth(level, at.add(0.0, -0.4, 0.0), 10, 0.25 + d * 0.12);
        }
        double minDot = Math.cos(Math.toRadians(28.0));
        DamageSource source = this.damageSources().mobAttack(this);
        List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, new AABB(from, from).inflate(DIRT_RANGE + 1.0, 3.0, DIRT_RANGE + 1.0), this::isFoe);
        for (LivingEntity victim : victims) {
            Vec3 to = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ());
            if (to.length() > DIRT_RANGE || to.lengthSqr() < 1.0E-4 || to.normalize().dot(dir) < minDot || !this.hasLineOfSight(victim)) {
                continue;
            }
            if (victim.hurtServer(level, source, damage * 0.4F)) {
                victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1));
            }
        }
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING) || super.isInvulnerableTo(level, source);
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.spawnAtLocation(level, new ItemStack(Dushi.GRAVEDIGGER_SOUL.get()));
        this.spawnAtLocation(level, new ItemStack(Dushi.FLASK_SHARD.get()));
        this.leave(level, Dushi.RUST_SCRAP.get(), 3, 5);
        this.leave(level, Dushi.TATTERED_CLOTH.get(), 2, 4);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            BlockPos home = MobMarkBlockEntity.homeOf(this);
            FogGateBlock.lift(level, home != null ? home : this.blockPosition(), 40);
            // those he raised lie down with him
            for (WitheredEntity helper : level.getEntitiesOfClass(WitheredEntity.class, this.getBoundingBox().inflate(30.0, 8.0, 30.0),
                    mob -> mob.isAlive() && MobMarkBlockEntity.homeOf(mob) == null)) {
                helper.kill(level);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.gravedigger.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.gravedigger.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.gravedigger.death");
    }

    @Override
    protected float getSoundVolume() {
        return 2.0F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(Dushi.sound("entity.rust_knight.step"), 0.7F, 0.5F);
    }
}
