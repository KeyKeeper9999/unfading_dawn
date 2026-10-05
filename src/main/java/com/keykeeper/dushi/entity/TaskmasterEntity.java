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
import net.minecraft.world.entity.ai.goal.FloatGoal;
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
 * The Taskmaster of the quarry: the foreman of every tenth man, who went on splitting stone when there was nobody left to
 * count. A broad man bent under a splitting maul as long as himself, a tallow lamp bound to his forehead, a belt of iron
 * wedges. The boss of the Wheel Hall.
 * <p>
 * Everything he does is quarry work. The maul comes down from over his head and no shield holds it; it goes round level
 * at the height of a man's ribs; he kneels, sets a wedge in the floor and drives it with one blow, and the rock splits
 * in a line that runs away from him faster than a man walks; he throws a wedge at whoever keeps their distance. Below
 * half health he strikes the wall of the hall instead, and the roof begins to come down - where the dust sifts down, a
 * block follows - and his cracks run three abreast.
 */
public class TaskmasterEntity extends SoulsMob {
    public static final int SLAM = 1;
    public static final int SLAM_DURATION = 54;
    public static final int SLAM_IMPACT = 22;
    public static final int SWEEP = 2;
    public static final int SWEEP_DURATION = 46;
    public static final int SWEEP_IMPACT = 19;
    /** Kneels and sets the wedge by tick 14, strikes it at 30; the crack then runs on by itself. */
    public static final int WEDGE = 3;
    public static final int WEDGE_DURATION = 68;
    public static final int WEDGE_SET = 14;
    public static final int WEDGE_STRIKE = 30;
    public static final int THROW = 4;
    public static final int THROW_DURATION = 36;
    public static final int THROW_RELEASE = 17;
    public static final int CAVE_IN = 5;
    public static final int CAVE_IN_DURATION = 62;
    public static final int CAVE_IN_STRIKE = 24;

    private static final double CRACK_LENGTH = 15.0;

    private int wedgeCooldown = 80;
    private int throwCooldown = 60;
    private int caveCooldown;

    public TaskmasterEntity(EntityType<? extends TaskmasterEntity> type, Level level) {
        super(type, level);
        this.xpReward = 120;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 720.0).add(Attributes.ATTACK_DAMAGE, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24).add(Attributes.ARMOR, 4.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 40.0).add(Attributes.STEP_HEIGHT, 1.25);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 14.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public int soulReward() {
        return 9000;
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
            case WEDGE -> WEDGE_DURATION;
            case THROW -> THROW_DURATION;
            default -> CAVE_IN_DURATION;
        };
    }

    @Override
    protected int trackUntil(int id) {
        return switch (id) {
            case SLAM -> SLAM_IMPACT - 8;
            case SWEEP -> SWEEP_IMPACT - 7;
            case WEDGE -> WEDGE_SET - 2;
            case THROW -> THROW_RELEASE;
            default -> 0;
        };
    }

    @Override
    protected int attackPause(int finishedId) {
        return this.phase() >= 2 ? 9 + this.random.nextInt(9) : 16 + this.random.nextInt(12);
    }

    @Override
    public double stopDistance() {
        return 2.4;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (this.phase() >= 2 && this.caveCooldown <= 0) {
            this.caveCooldown = 300 + this.random.nextInt(80);
            return CAVE_IN;
        }
        boolean seen = this.hasLineOfSight(target);
        if (distance > 4.0 && distance < CRACK_LENGTH && this.wedgeCooldown <= 0 && seen && Math.abs(target.getY() - this.getY()) < 2.5) {
            this.wedgeCooldown = (this.phase() >= 2 ? 130 : 170) + this.random.nextInt(50);
            return WEDGE;
        }
        if (distance > 6.5 && this.throwCooldown <= 0 && seen) {
            this.throwCooldown = 90 + this.random.nextInt(50);
            return THROW;
        }
        if (distance < 3.9) {
            return this.random.nextInt(100) < 55 ? SWEEP : SLAM;
        }
        return 0;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.phase() == 1 && this.getHealth() < this.getMaxHealth() * 0.5F && this.attackId() == 0 && this.isAlive()) {
            this.setPhase(2);
            this.caveCooldown = 320;
            this.startAttack(CAVE_IN);
        }
        super.customServerAiStep(level);
        if (this.wedgeCooldown > 0) {
            this.wedgeCooldown--;
        }
        if (this.throwCooldown > 0) {
            this.throwCooldown--;
        }
        if (this.caveCooldown > 0) {
            this.caveCooldown--;
        }
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        switch (id) {
            case SLAM -> {
                if (time == SLAM_IMPACT - 6) {
                    this.playSound(Dushi.sound("entity.overseer.swing"), 1.8F, 0.85F);
                }
                if (time == SLAM_IMPACT) {
                    Vec3 at = this.position().add(this.facing().scale(3.3));
                    this.hitArc(level, this.facing(), 4.8, 26.0, damage * 1.5F, 0.9, true);
                    this.hitAround(level, at, 2.8, damage * 0.55F, 0.6, this.damageSources().mobAttack(this));
                    this.rubble(level, at, 60, 1.2);
                    level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.taskmaster.maul"), this.getSoundSource(), 3.0F, 0.9F + this.random.nextFloat() * 0.15F);
                }
            }
            case SWEEP -> {
                if (time == SWEEP_IMPACT - 5) {
                    this.playSound(Dushi.sound("entity.overseer.swing"), 1.8F, 1.1F);
                }
                if (time == SWEEP_IMPACT) {
                    this.hitArc(level, 4.9, 110.0, damage, 1.5);
                    for (int i = -2; i <= 2; i++) {
                        Vec3 dir = this.facing().yRot((float) Math.toRadians(i * 34.0));
                        level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + dir.x * 3.4, this.getY() + 1.3, this.getZ() + dir.z * 3.4, 1, 0.0, 0.0, 0.0, 0.0);
                    }
                }
            }
            case WEDGE -> {
                if (time == WEDGE_SET) {
                    Vec3 at = this.position().add(this.facing().scale(1.6));
                    level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.taskmaster.wedge"), this.getSoundSource(), 1.6F, 1.25F);
                    this.rubble(level, at, 6, 0.15);
                }
                if (time == WEDGE_STRIKE) {
                    Vec3 at = this.position().add(this.facing().scale(1.6));
                    level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.taskmaster.wedge"), this.getSoundSource(), 3.0F, 0.8F);
                    level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.taskmaster.crack"), this.getSoundSource(), 3.0F, 1.0F);
                    this.rubble(level, at, 30, 0.5);
                    this.crack(this.facing(), damage * 1.15F);
                    if (this.phase() >= 2) {
                        this.crack(this.facing().yRot((float) Math.toRadians(24.0)), damage * 1.15F);
                        this.crack(this.facing().yRot((float) Math.toRadians(-24.0)), damage * 1.15F);
                    }
                }
            }
            case THROW -> {
                LivingEntity target = this.getTarget();
                if (time == THROW_RELEASE && target != null) {
                    WedgeEntity wedge = new WedgeEntity(level, this, damage * 0.65F);
                    wedge.setPos(this.getX(), this.getY() + this.getBbHeight() * 0.72, this.getZ());
                    Vec3 to = target.getBoundingBox().getCenter().subtract(wedge.position());
                    wedge.shoot(to.x, to.y + to.horizontalDistance() * 0.035, to.z, 1.7F, 1.5F);
                    level.addFreshEntity(wedge);
                    this.playSound(Dushi.sound("player.weapon_swing_heavy"), 1.4F, 1.5F);
                }
            }
            default -> {
                if (time == 2) {
                    this.playSound(Dushi.sound("entity.taskmaster.shout"), 3.0F, 1.0F);
                }
                if (time == CAVE_IN_STRIKE) {
                    Vec3 at = this.position().add(this.facing().scale(2.6));
                    this.rubble(level, at, 50, 1.0);
                    level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.taskmaster.maul"), this.getSoundSource(), 3.0F, 0.7F);
                    level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.taskmaster.crack"), this.getSoundSource(), 3.0F, 0.7F);
                    this.hitAround(level, at, 2.6, damage * 0.8F, 1.0, this.damageSources().mobAttack(this));
                    this.caveIn(damage * 1.2F);
                }
            }
        }
    }

    /** The rock splits in a line from his wedge: each stretch of it goes a moment after the one before. */
    private void crack(Vec3 direction, float damage) {
        Vec3 from = this.position().add(this.facing().scale(1.6));
        int step = 0;
        for (double d = 1.2; d <= CRACK_LENGTH; d += 1.15) {
            this.strikeLater(from.add(direction.scale(d)), 3 + step * 2, 1.05, damage, false);
            step++;
        }
    }

    /** The roof comes down in a dozen places: some where those he fights stand now, the rest scattered about them. */
    private void caveIn(float damage) {
        LivingEntity target = this.getTarget();
        Vec3 centre = target != null ? target.position() : this.position();
        double floor = this.getY();
        int rocks = 11 + (this.tier() > 0 ? 3 : 0);
        for (int i = 0; i < rocks; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            double far = i < 3 ? this.random.nextDouble() * 1.2 : 2.5 + this.random.nextDouble() * 7.5;
            Vec3 at = new Vec3(centre.x + Math.cos(angle) * far, floor, centre.z + Math.sin(angle) * far);
            this.strikeLater(at, 26 + i * 6 + this.random.nextInt(6), 1.9, damage, true);
        }
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING) || source.is(DamageTypes.EXPLOSION) && source.getEntity() instanceof SoulsMob
                || super.isInvulnerableTo(level, source);
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.spawnAtLocation(level, new ItemStack(Dushi.TASKMASTER_SOUL.get()));
        this.spawnAtLocation(level, new ItemStack(Dushi.FLASK_SHARD.get()));
        this.leave(level, Dushi.IRON_WEDGE.get(), 4, 6);
        this.leave(level, Dushi.RUST_SCRAP.get(), 3, 5);
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
        return Dushi.sound("entity.taskmaster.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.taskmaster.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.taskmaster.death");
    }

    @Override
    protected float getSoundVolume() {
        return 2.0F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(Dushi.sound("entity.rust_knight.step"), 0.8F, 0.55F);
    }
}
