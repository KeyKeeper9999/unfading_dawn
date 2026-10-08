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
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The Hollow Miller: the master of the great mill on the hill above the Stubble. There has been no wind since the day
 * was stopped, and no sail of his has turned; so he turns the stone himself, and has, ever since - on nothing: the
 * corn that is cut stands again before it reaches him. He grinds all the same. The second lord of the Stubble.
 * <p>
 * His fight is on the grinding floor, and the stone is in it (see {@link MillstoneEntity}): it rolls round the post
 * the whole time, and he fights across its track with the bar he levers it with - round at arm's length, or down from
 * over his head. He puts his weight to the beam, and it rolls faster than a man runs; he checks it, and it comes back
 * the other way.
 * <p>
 * Hurt, he tears the stone off its axle and has it in his hands (phase 2). He rolls it at whoever is before him,
 * the length of the floor, and runs behind it - and when it meets the wall he is a while getting it round. He heaves
 * it up and lets it fall. He kicks the sacks open, so that the flour hangs in the air; and then he lifts his lantern.
 */
public class HollowMillerEntity extends SoulsMob {
    public static final int SWING = 1;
    public static final int SWING_DURATION = 56;
    public static final int SWING_IMPACT = 26;
    public static final int CLEAVE = 2;
    public static final int CLEAVE_DURATION = 68;
    public static final int CLEAVE_IMPACT = 33;
    public static final int HEAVE = 3;
    public static final int HEAVE_DURATION = 50;
    public static final int HEAVE_AT = 22;
    public static final int CHECK = 4;
    public static final int CHECK_DURATION = 44;
    public static final int CHECK_AT = 18;
    public static final int UNSHIP = 5;
    public static final int UNSHIP_DURATION = 92;
    public static final int UNSHIP_BREAK = 42;
    public static final int ROLL = 6;
    public static final int ROLL_DURATION = 118;
    public static final int ROLL_START = 28;
    public static final int ROLL_END = 72;
    public static final int DROP = 7;
    public static final int DROP_DURATION = 78;
    public static final int DROP_IMPACT = 38;
    public static final int SACKS = 8;
    public static final int SACKS_DURATION = 52;
    public static final int SACKS_BURST = 22;
    public static final int LANTERN = 9;
    public static final int LANTERN_DURATION = 64;
    public static final int LANTERN_LIT = 32;
    public static final double REACH = 4.6;
    public static final double DROP_RADIUS = 3.4;
    /** The share of a blow that gets through to him. */
    public static final float HIDE = 0.4F;

    private MillstoneEntity stone;
    private int heaveCooldown = 160;
    private int checkCooldown = 260;
    private int rollCooldown = 60;
    private int sacksCooldown = 140;
    private int lanternCooldown;
    private int rollStopped = -1;
    private final java.util.Set<Integer> rolledOver = new java.util.HashSet<>();

    public HollowMillerEntity(EntityType<? extends HollowMillerEntity> type, Level level) {
        super(type, level);
        this.xpReward = 340;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1000.0).add(Attributes.ATTACK_DAMAGE, 29.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.ARMOR, 6.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 44.0).add(Attributes.STEP_HEIGHT, 1.25);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public int soulReward() {
        return 35500;
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    /** The stone is off its axle and in his hands. */
    public boolean carrying() {
        return this.phase() >= 2;
    }

    @Override
    protected int attackDuration(int id) {
        return switch (id) {
            case SWING -> SWING_DURATION;
            case CLEAVE -> CLEAVE_DURATION;
            case HEAVE -> HEAVE_DURATION;
            case CHECK -> CHECK_DURATION;
            case UNSHIP -> UNSHIP_DURATION;
            case ROLL -> ROLL_DURATION;
            case DROP -> DROP_DURATION;
            case SACKS -> SACKS_DURATION;
            default -> LANTERN_DURATION;
        };
    }

    @Override
    protected int trackUntil(int id) {
        return switch (id) {
            case SWING -> SWING_IMPACT - 9;
            case CLEAVE -> CLEAVE_IMPACT - 12;
            case ROLL -> ROLL_START - 5;
            case DROP -> DROP_IMPACT - 14;
            case UNSHIP -> 0;
            default -> this.attackDuration(id);
        };
    }

    @Override
    protected int attackPause(int finishedId) {
        if (finishedId == UNSHIP || finishedId == SACKS) {
            return 6;
        }
        return this.carrying() ? 10 + this.random.nextInt(10) : 16 + this.random.nextInt(12);
    }

    @Override
    public double stopDistance() {
        return 2.6;
    }

    /** The post his floor is centred on: where his mark is, or where he stood when he was first seen. */
    private BlockPos post() {
        BlockPos home = MobMarkBlockEntity.homeOf(this);
        if (home == null) {
            String key = "MillPost";
            long kept = this.getPersistentData().getLongOr(key, Long.MIN_VALUE);
            if (kept == Long.MIN_VALUE) {
                kept = this.blockPosition().asLong();
                this.getPersistentData().putLong(key, kept);
            }
            home = BlockPos.of(kept);
        }
        return home;
    }

    /** The stone on its beam, if it is still on it: looked for, and set up if there is none. */
    public MillstoneEntity stone(ServerLevel level) {
        if (this.carrying()) {
            return null;
        }
        if (this.stone == null || !this.stone.isAlive()) {
            BlockPos post = this.post();
            List<MillstoneEntity> found = level.getEntitiesOfClass(MillstoneEntity.class, new AABB(post).inflate(MillstoneEntity.RADIUS + 4.0, 4.0, MillstoneEntity.RADIUS + 4.0),
                    Entity::isAlive);
            this.stone = found.isEmpty() ? MillstoneEntity.set(level, post) : found.get(0);
        }
        return this.stone;
    }

    @Override
    protected void onReset() {
        super.onReset();
        this.heaveCooldown = 160;
        this.checkCooldown = 260;
        this.rollCooldown = 60;
        this.sacksCooldown = 140;
        this.lanternCooldown = 0;
        if (this.level() instanceof ServerLevel level) {
            for (FlourCloudEntity cloud : level.getEntitiesOfClass(FlourCloudEntity.class, this.getBoundingBox().inflate(24.0, 8.0, 24.0))) {
                cloud.discard();
            }
        }
    }

    /** Getting the stone round after it has met the wall: when he can do nothing about a blow. */
    public boolean open() {
        return this.attackId() == ROLL && this.rollStopped >= 0 && this.attackTime > this.rollStopped + 2 && this.attackTime < ROLL_DURATION - 10;
    }

    private List<FlourCloudEntity> clouds(ServerLevel level) {
        return level.getEntitiesOfClass(FlourCloudEntity.class, this.getBoundingBox().inflate(16.0, 6.0, 16.0), cloud -> cloud.isAlive() && !cloud.lit());
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (!this.carrying()) {
            if (this.heaveCooldown <= 0) {
                this.heaveCooldown = 320 + this.random.nextInt(120);
                return HEAVE;
            }
            if (this.checkCooldown <= 0) {
                this.checkCooldown = 300 + this.random.nextInt(160);
                return CHECK;
            }
            if (distance < REACH - 0.6) {
                return this.random.nextInt(10) < 6 ? SWING : CLEAVE;
            }
            return 0;
        }
        if (this.lanternCooldown <= 0 && this.level() instanceof ServerLevel level && !this.clouds(level).isEmpty()) {
            this.lanternCooldown = 200;
            return LANTERN;
        }
        if (this.sacksCooldown <= 0) {
            this.sacksCooldown = 360 + this.random.nextInt(160);
            this.lanternCooldown = 30;
            return SACKS;
        }
        if (this.rollCooldown <= 0 && distance > 3.0 && distance < 16.0 && this.hasLineOfSight(target)) {
            this.rollCooldown = 150 + this.random.nextInt(90);
            return ROLL;
        }
        if (distance < DROP_RADIUS) {
            return DROP;
        }
        return 0;
    }

    @Override
    public void startAttack(int id) {
        super.startAttack(id);
        this.rollStopped = -1;
        this.rolledOver.clear();
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.phase() == 1 && this.getHealth() < this.getMaxHealth() * 0.5F && this.attackId() == 0 && this.isAlive()) {
            this.startAttack(UNSHIP);
        }
        super.customServerAiStep(level);
        this.heaveCooldown = Math.max(0, this.heaveCooldown - 1);
        this.checkCooldown = Math.max(0, this.checkCooldown - 1);
        this.rollCooldown = Math.max(0, this.rollCooldown - 1);
        this.sacksCooldown = Math.max(0, this.sacksCooldown - 1);
        this.lanternCooldown = Math.max(0, this.lanternCooldown - 1);
        if (this.tickCount % 5 == 0) {
            MillstoneEntity mill = this.stone(level);
            if (mill != null) {
                mill.drive(this.getTarget() != null && this.isAlive());
            }
        }
    }

    private void chips(ServerLevel level, Vec3 at, int count, double spread) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState()), at.x, at.y + 0.2, at.z, count, spread, 0.3, spread, 0.2);
        level.sendParticles(ParticleTypes.CLOUD, at.x, at.y + 0.3, at.z, Math.max(1, count / 5), spread, 0.2, spread, 0.03);
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        switch (id) {
            case SWING -> {
                if (time == SWING_IMPACT - 5) {
                    this.playSound(Dushi.sound("entity.miller.swing"), 2.0F, 0.95F + this.random.nextFloat() * 0.1F);
                }
                if (time == SWING_IMPACT) {
                    this.hitArc(level, REACH, 100.0, damage, 1.5);
                    Vec3 at = this.position().add(this.facing().scale(3.2));
                    level.sendParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y + 1.3, at.z, 2, 0.6, 0.0, 0.6, 0.0);
                }
            }
            case CLEAVE -> {
                if (time == CLEAVE_IMPACT - 6) {
                    this.playSound(Dushi.sound("entity.miller.swing"), 2.0F, 0.75F);
                }
                if (time == CLEAVE_IMPACT) {
                    this.hitArc(level, this.facing(), REACH + 0.2, 20.0, damage * 1.6F, 0.5, true);
                    Vec3 at = this.position().add(this.facing().scale(3.6));
                    this.hitAround(level, at, 2.2, damage * 0.6F, 0.8, this.damageSources().mobAttack(this));
                    this.chips(level, at, 40, 0.8);
                    level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.miller.slam"), this.getSoundSource(), 2.6F, 1.0F);
                }
            }
            case HEAVE -> {
                if (time == HEAVE_AT - 12) {
                    this.playSound(Dushi.sound("entity.miller.shout"), 3.0F, 1.0F);
                }
                if (time == HEAVE_AT) {
                    MillstoneEntity mill = this.stone(level);
                    if (mill != null) {
                        mill.drive(true);
                        mill.hasten();
                    }
                }
            }
            case CHECK -> {
                if (time == CHECK_AT - 10) {
                    this.playSound(Dushi.sound("entity.miller.shout"), 3.0F, 0.8F);
                }
                if (time == CHECK_AT) {
                    MillstoneEntity mill = this.stone(level);
                    if (mill != null) {
                        mill.turnBack();
                    }
                }
            }
            case UNSHIP -> {
                if (time == 6) {
                    this.playSound(Dushi.sound("entity.miller.shout"), 4.0F, 0.7F);
                }
                if (time == UNSHIP_BREAK) {
                    MillstoneEntity mill = this.stone(level);
                    if (mill != null) {
                        mill.unship(level);
                    }
                    this.stone = null;
                    this.setPhase(2);
                    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.0, 2.0, 5.0), this::isFoe)) {
                        Vec3 away = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ());
                        double far = away.length();
                        if (far < 5.5) {
                            away = far < 1.0E-3 ? this.facing() : away.scale(1.0 / far);
                            shove(victim, new Vec3(away.x * 0.8, 0.34, away.z * 0.8));
                        }
                    }
                    this.chips(level, this.position(), 60, 1.6);
                }
            }
            case ROLL -> {
                if (time == ROLL_START - 14) {
                    this.playSound(Dushi.sound("entity.miller.shout"), 3.0F, 1.1F);
                }
                if (time >= ROLL_START && this.rollStopped < 0) {
                    Vec3 run = this.facing().scale(0.52);
                    this.setDeltaMovement(run.x, this.getDeltaMovement().y, run.z);
                    if (time % 6 == 0) {
                        this.playSound(Dushi.sound("entity.millstone.rumble"), 2.2F, 1.3F);
                    }
                    Vec3 before = this.position().add(this.facing().scale(1.6));
                    level.sendParticles(ParticleTypes.CLOUD, before.x, before.y + 0.1, before.z, 2, 0.4, 0.05, 0.4, 0.02);
                    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(before, before).inflate(1.5, 1.6, 1.5), this::isFoe)) {
                        if (this.rolledOver.add(victim.getId()) && victim.hurtServer(level, this.damageSources().mobAttack(this), damage * 1.5F)) {
                            Vec3 side = new Vec3(-this.facing().z, 0.0, this.facing().x);
                            double way = (victim.getX() - this.getX()) * side.x + (victim.getZ() - this.getZ()) * side.z >= 0.0 ? 1.0 : -1.0;
                            shove(victim, new Vec3(side.x * way * 0.7 + this.facing().x * 0.5, 0.42, side.z * way * 0.7 + this.facing().z * 0.5));
                        }
                    }
                    if (time >= ROLL_END || (this.horizontalCollision && time > ROLL_START + 4)) {
                        this.rollStopped = time;
                        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
                        if (this.horizontalCollision) {
                            this.chips(level, before, 40, 0.8);
                            this.playSound(Dushi.sound("entity.millstone.crash"), 2.6F, 1.2F);
                        }
                    }
                }
                // (a short run leaves him open as long as a long one: the move ends that much sooner)
                if (this.rollStopped >= 0 && time >= this.rollStopped + 40) {
                    this.endAttack();
                }
            }
            case DROP -> {
                if (time == DROP_IMPACT - 8) {
                    this.playSound(Dushi.sound("entity.miller.swing"), 2.0F, 0.6F);
                }
                if (time == DROP_IMPACT) {
                    Vec3 at = this.position().add(this.facing().scale(1.8));
                    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(DROP_RADIUS + 1.0, 3.0, DROP_RADIUS + 1.0), this::isFoe)) {
                        if (victim.isBlocking() && Math.sqrt(victim.distanceToSqr(at.x, victim.getY(), at.z)) - victim.getBbWidth() * 0.5 <= DROP_RADIUS) {
                            breakGuard(level, victim);
                        }
                    }
                    this.hitAround(level, at, DROP_RADIUS, damage * 1.4F, 1.4, this.damageSources().mobAttack(this));
                    this.chips(level, at, 70, 1.4);
                    level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.millstone.crash"), this.getSoundSource(), 3.0F, 0.8F);
                    FlourCloudEntity.burst(level, this, at, damage * 0.9F);
                }
            }
            case SACKS -> {
                if (time == SACKS_BURST - 10) {
                    this.playSound(Dushi.sound("entity.miller.shout"), 3.0F, 1.2F);
                }
                if (time == SACKS_BURST) {
                    LivingEntity target = this.getTarget();
                    Vec3 aim = target != null ? target.position() : this.position().add(this.facing().scale(5.0));
                    FlourCloudEntity.burst(level, this, aim, damage * 0.9F);
                    for (int i = 0; i < 3; i++) {
                        double angle = this.random.nextDouble() * Math.PI * 2.0;
                        double far = 3.5 + this.random.nextDouble() * 3.0;
                        FlourCloudEntity.burst(level, this, new Vec3(aim.x + Math.cos(angle) * far, aim.y, aim.z + Math.sin(angle) * far), damage * 0.9F);
                    }
                }
            }
            default -> {
                // the lantern off his belt and up, where everybody can see what it is going to do
                if (time == 4) {
                    this.playSound(Dushi.sound("entity.miller.lantern"), 3.0F, 1.0F);
                }
                if (time < LANTERN_LIT && time % 2 == 0) {
                    level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + this.getBbHeight() + 0.6, this.getZ(), 3, 0.15, 0.15, 0.15, 0.01);
                }
                if (time == LANTERN_LIT) {
                    for (FlourCloudEntity cloud : this.clouds(level)) {
                        cloud.light();
                    }
                }
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        float through = this.open() ? Math.min(1.0F, HIDE * 2.0F) : HIDE;
        return super.hurtServer(level, source, damage == Float.MAX_VALUE ? damage : damage * through);
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING) || source.is(DamageTypes.DROWN) || super.isInvulnerableTo(level, source);
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
        this.spawnAtLocation(level, new ItemStack(Dushi.MILLER_SOUL.get()));
        this.spawnAtLocation(level, new ItemStack(Dushi.FLASK_SHARD.get()));
        this.leave(level, Dushi.GREAT_SOUL.get(), 2, 3);
        this.leave(level, Dushi.FLOUR_SACK.get(), 3, 5);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            BlockPos home = MobMarkBlockEntity.homeOf(this);
            FogGateBlock.lift(level, home != null ? home : this.blockPosition(), 40);
            MillstoneEntity mill = this.stone;
            if (mill != null && mill.isAlive()) {
                mill.drive(false);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.miller.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.miller.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.miller.death");
    }

    @Override
    protected float getSoundVolume() {
        return 2.2F;
    }
}
