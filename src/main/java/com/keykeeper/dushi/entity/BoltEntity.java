package com.keykeeper.dushi.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.block.FogGateBlock;
import com.keykeeper.dushi.block.MobMarkBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The Bolt: the bob of the great pendulum of the clock that was stopped with the day - a lens of amber glass in a ring
 * of black iron, a keyhole through the middle of it. It is what the Founder's rite shut tomorrow in with, and it is
 * the one thing of that clock still moving: it swings, so that nothing else may. The first lord of the Sky Beneath.
 * <p>
 * On its rod (phase 1) it goes from one side of its hall to the other and back, and whoever is in its way is run
 * down; it is slow only at the two ends of its swing, and those are the places to stand. It tolls - a ring goes out
 * from it along the floor, to be rolled through or jumped - and the weights of the clock come down out of the dark
 * where somebody stands.
 * <p>
 * Hurt enough it tears itself off the rod. The four hands that lay folded along it come down and are its legs (phase
 * 2): it walks, thrusts with one of them the length of three men, turns on the spot with all four out, and leaps on
 * whoever stands off from it.
 */
public class BoltEntity extends SoulsMob {
    private static final EntityDataAccessor<Float> SWING = SynchedEntityData.defineId(BoltEntity.class, EntityDataSerializers.FLOAT);

    public static final int TOLL = 1;
    public static final int TOLL_DURATION = 76;
    public static final int TOLL_RING = 22;
    public static final int TOLL_END = 58;
    public static final int WEIGHTS = 2;
    public static final int WEIGHTS_DURATION = 56;
    public static final int WEIGHTS_CALL = 18;
    public static final int BREAK = 3;
    public static final int BREAK_DURATION = 84;
    public static final int BREAK_SNAP = 36;
    public static final int STAB = 4;
    public static final int STAB_DURATION = 54;
    public static final int STAB_IMPACT = 25;
    public static final int SWEEP = 5;
    public static final int SWEEP_DURATION = 74;
    public static final int[] SWEEP_HITS = {30, 46};
    public static final int LEAP = 6;
    public static final int LEAP_DURATION = 86;
    public static final int LEAP_JUMP = 24;
    public static final int LEAP_LAND = 46;
    /** How far to either side of where it hangs it swings, in blocks; how long there and back takes; how long its rod is. */
    public static final double AMPLITUDE = 7.0;
    public static final int PERIOD = 116;
    public static final double ROD = 11.0;
    /** Half the width of what it runs down. */
    public static final double BREADTH = 1.5;
    public static final double STAB_REACH = 5.2;
    public static final double SWEEP_REACH = 4.0;
    public static final double WEIGHT_RADIUS = 1.9;
    public static final double LEAP_RADIUS = 3.2;
    /** The share of a blow that gets through to it (nothing has more than 1024 health; it needs more). */
    public static final float HIDE = 0.38F;
    private static final double WALK = 0.27;

    private Vec3 pivot;
    private float homeYaw;
    private double angle;
    private double amplitude = 0.6;
    private double swung;
    private int tollCooldown = 100;
    private int weightsCooldown = 60;
    private int leapCooldown = 80;
    private int sweepCooldown = 100;
    private Vec3 leapTo;
    private final Set<UUID> tolled = new HashSet<>();

    public BoltEntity(EntityType<? extends BoltEntity> type, Level level) {
        super(type, level);
        this.xpReward = 360;
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1000.0).add(Attributes.ATTACK_DAMAGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.ARMOR, 6.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 48.0).add(Attributes.STEP_HEIGHT, 1.25);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SWING, 0.0F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public int soulReward() {
        return 40000;
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    protected BossEvent.BossBarColor bossBarColor() {
        return BossEvent.BossBarColor.YELLOW;
    }

    /** Off its rod. */
    public boolean down() {
        return this.phase() >= 2;
    }

    /** How far it has swung from where it hangs, as a share of its rod's length: the model leans by it. */
    @Override
    public float renderExtra() {
        return this.down() ? 0.0F : (float) (this.entityData.get(SWING) / ROD);
    }

    @Override
    protected void setPhase(int phase) {
        super.setPhase(phase);
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(phase >= 2 ? WALK : 0.0);
        }
        this.setNoGravity(phase < 2);
    }

    @Override
    protected int attackDuration(int id) {
        return switch (id) {
            case TOLL -> TOLL_DURATION;
            case WEIGHTS -> WEIGHTS_DURATION;
            case BREAK -> BREAK_DURATION;
            case STAB -> STAB_DURATION;
            case SWEEP -> SWEEP_DURATION;
            default -> LEAP_DURATION;
        };
    }

    @Override
    protected int trackUntil(int id) {
        if (!this.down()) {
            return 0;
        }
        return switch (id) {
            case STAB -> STAB_IMPACT - 9;
            case LEAP -> LEAP_JUMP;
            default -> 0;
        };
    }

    @Override
    protected int attackPause(int finishedId) {
        if (finishedId == BREAK) {
            return 6;
        }
        return this.down() ? 12 + this.random.nextInt(12) : 30 + this.random.nextInt(20);
    }

    @Override
    public double stopDistance() {
        return 2.6;
    }

    @Override
    protected void onReset() {
        super.onReset();
        this.tollCooldown = 100;
        this.weightsCooldown = 60;
        this.leapCooldown = 80;
        this.sweepCooldown = 100;
        this.amplitude = 0.6;
        this.angle = 0.0;
        this.tolled.clear();
        if (this.pivot != null) {
            this.teleportTo(this.pivot.x, this.pivot.y, this.pivot.z);
        }
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (!this.down()) {
            if (this.tollCooldown <= 0) {
                this.tollCooldown = 210 + this.random.nextInt(90);
                return TOLL;
            }
            if (this.weightsCooldown <= 0) {
                this.weightsCooldown = 150 + this.random.nextInt(70);
                return WEIGHTS;
            }
            return 0;
        }
        if (this.leapCooldown <= 0 && distance > 6.0 && distance < 18.0 && this.hasLineOfSight(target)) {
            this.leapCooldown = 190 + this.random.nextInt(90);
            return LEAP;
        }
        if (this.tollCooldown <= 0 && distance > 4.0) {
            this.tollCooldown = 260 + this.random.nextInt(120);
            return TOLL;
        }
        if (this.sweepCooldown <= 0 && distance < SWEEP_REACH - 0.6) {
            this.sweepCooldown = 150 + this.random.nextInt(80);
            return SWEEP;
        }
        return distance < STAB_REACH - 0.8 ? STAB : 0;
    }

    @Override
    public void startAttack(int id) {
        super.startAttack(id);
        this.tolled.clear();
        this.leapTo = null;
    }

    private static Vec3 ground(Level level, double x, double y, double z) {
        Vec3 found = RootEntity.ground(level, x, y, z);
        return found != null ? found : new Vec3(x, y, z);
    }

    /** The line it swings along: to its own left and right. */
    private Vec3 axis() {
        double yaw = Math.toRadians(this.homeYaw);
        return new Vec3(Math.cos(yaw), 0.0, Math.sin(yaw));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.pivot == null) {
            BlockPos home = MobMarkBlockEntity.homeOf(this);
            this.pivot = home != null ? new Vec3(home.getX() + 0.5, home.getY(), home.getZ() + 0.5) : this.position();
            this.homeYaw = this.getYRot();
        }
        if (this.phase() == 1 && this.getHealth() < this.getMaxHealth() * 0.5F && this.attackId() == 0 && this.isAlive()) {
            this.startAttack(BREAK);
        }
        super.customServerAiStep(level);
        this.tollCooldown = Math.max(0, this.tollCooldown - 1);
        this.weightsCooldown = Math.max(0, this.weightsCooldown - 1);
        this.leapCooldown = Math.max(0, this.leapCooldown - 1);
        this.sweepCooldown = Math.max(0, this.sweepCooldown - 1);
        if (this.down() || !this.isAlive()) {
            return;
        }
        // on the rod: it goes where the rod takes it
        boolean fighting = this.getTarget() != null && this.attackId() != BREAK;
        double wanted = fighting ? AMPLITUDE : (this.attackId() == BREAK ? 0.0 : 0.6);
        this.amplitude += Math.max(-0.12, Math.min(0.05, wanted - this.amplitude));
        double before = this.swung;
        this.angle += Math.PI * 2.0 / PERIOD;
        this.swung = this.amplitude * Math.sin(this.angle);
        double speed = this.swung - before;
        Vec3 axis = this.axis();
        this.setDeltaMovement(Vec3.ZERO);
        this.setPos(this.pivot.x + axis.x * this.swung, this.pivot.y, this.pivot.z + axis.z * this.swung);
        this.setYRot(this.homeYaw);
        this.setYBodyRot(this.homeYaw);
        this.setYHeadRot(this.homeYaw);
        this.entityData.set(SWING, (float) this.swung);
        if (before * this.swung < 0.0 && this.amplitude > 2.0) {
            this.playSound(Dushi.sound("entity.bolt.tock"), 2.0F, speed > 0.0 ? 1.0F : 0.9F);
        }
        if (Math.abs(speed) > 0.1) {
            this.run(level, axis.scale(Math.signum(speed)), Math.abs(speed));
        }
    }

    /** Whoever is in its way is run down, and thrown out of it. */
    private void run(ServerLevel level, Vec3 along, double speed) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * (speed > 0.3 ? 1.0F : 0.75F);
        Vec3 out = new Vec3(-along.z, 0.0, along.x);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.8, 0.3, 0.8), this::isFoe)) {
            double dx = victim.getX() - this.getX();
            double dz = victim.getZ() - this.getZ();
            double reach = BREADTH + victim.getBbWidth() * 0.5;
            if (dx * dx + dz * dz > reach * reach || victim.getY() > this.getY() + 2.6) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                double side = dx * out.x + dz * out.z >= 0.0 ? 1.0 : -1.0;
                shove(victim, new Vec3(along.x * 0.5 + out.x * side * 0.7, 0.36, along.z * 0.5 + out.z * side * 0.7));
                this.playSound(Dushi.sound("entity.bolt.strike"), 1.6F, 1.0F);
            }
        }
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        switch (id) {
            case TOLL -> {
                if (time == TOLL_RING - 12) {
                    this.playSound(Dushi.sound("entity.bolt.toll"), 4.0F, 1.0F);
                }
                if (time >= TOLL_RING && time <= TOLL_END) {
                    // a ring going out along the floor from where it tolled
                    double radius = 1.6 + (time - TOLL_RING) * 0.34;
                    int points = 12 + (int) (radius * 5.0);
                    for (int i = 0; i < points; i++) {
                        double a = i * Math.PI * 2.0 / points + time * 0.05;
                        level.sendParticles(i % 3 == 0 ? ParticleTypes.END_ROD : ParticleTypes.ELECTRIC_SPARK, this.getX() + Math.cos(a) * radius, this.getY() + 0.25,
                                this.getZ() + Math.sin(a) * radius, 1, 0.0, 0.05, 0.0, 0.0);
                    }
                    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius + 1.0, 1.0, radius + 1.0), this::isFoe)) {
                        double far = Math.hypot(victim.getX() - this.getX(), victim.getZ() - this.getZ());
                        if (Math.abs(far - radius) > 0.7 || victim.getY() > this.getY() + 0.9 || this.tolled.contains(victim.getUUID())) {
                            continue;                              // (it runs along the floor: it is jumped, or rolled through)
                        }
                        if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage * 0.8F)) {
                            this.tolled.add(victim.getUUID());
                            victim.knockback(1.0 * com.keykeeper.dushi.Gear.thrown(victim), this.getX() - victim.getX(), this.getZ() - victim.getZ(),
                                    this.damageSources().mobAttack(this), damage * 0.8F);
                        }
                    }
                }
            }
            case WEIGHTS -> {
                if (time == WEIGHTS_CALL - 8) {
                    this.playSound(Dushi.sound("entity.bolt.chain"), 3.0F, 1.0F);
                }
                if (time == WEIGHTS_CALL) {
                    LivingEntity target = this.getTarget();
                    Vec3 aim = target != null ? target.position() : this.position().add(this.facing().scale(6.0));
                    Vec3 lead = target != null ? target.getKnownMovement().multiply(1.0, 0.0, 1.0).scale(10.0) : Vec3.ZERO;
                    this.strikeLater(ground(level, aim.x + lead.x, aim.y, aim.z + lead.z), 30, WEIGHT_RADIUS, damage * 0.9F, true);
                    int more = this.getHealth() < this.getMaxHealth() * 0.75F ? 4 : 2;
                    for (int i = 0; i < more; i++) {
                        double a = this.random.nextDouble() * Math.PI * 2.0;
                        double far = 2.6 + this.random.nextDouble() * 3.4;
                        this.strikeLater(ground(level, aim.x + Math.cos(a) * far, aim.y, aim.z + Math.sin(a) * far), 38 + i * 6, WEIGHT_RADIUS, damage * 0.9F, true);
                    }
                }
            }
            case BREAK -> {
                if (time == 4) {
                    this.playSound(Dushi.sound("entity.bolt.strain"), 4.0F, 1.0F);
                }
                if (time < BREAK_SNAP && time % 3 == 0) {
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 3.2 + this.random.nextDouble() * 4.0, this.getZ(), 4, 0.2, 0.6, 0.2, 0.1);
                }
                if (time == BREAK_SNAP) {
                    this.setPhase(2);
                    this.entityData.set(SWING, 0.0F);
                    Vec3 at = this.position();
                    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0, 2.0, 6.0), this::isFoe)) {
                        Vec3 away = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ());
                        double far = away.length();
                        if (far < 6.5) {
                            away = far < 1.0E-3 ? this.facing() : away.scale(1.0 / far);
                            victim.hurtServer(level, this.damageSources().mobAttack(this), damage * 0.7F);
                            shove(victim, new Vec3(away.x * 0.9, 0.36, away.z * 0.9));
                        }
                    }
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.IRON_BLOCK.defaultBlockState()), at.x, at.y + 4.0, at.z, 120, 0.4, 3.0, 0.4, 0.2);
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y + 2.0, at.z, 80, 2.0, 1.5, 2.0, 0.3);
                    this.playSound(Dushi.sound("entity.bolt.snap"), 4.0F, 1.0F);
                }
            }
            case STAB -> {
                if (time == STAB_IMPACT - 6) {
                    this.playSound(Dushi.sound("entity.bolt.stab"), 2.0F, 1.0F);
                }
                if (time == STAB_IMPACT) {
                    this.hitArc(level, this.facing(), STAB_REACH, 14.0, damage * 1.3F, 0.8, true);
                    Vec3 at = this.position().add(this.facing().scale(STAB_REACH - 0.5));
                    this.rubble(level, at, 14, 0.4);
                }
            }
            case SWEEP -> {
                for (int hit : SWEEP_HITS) {
                    if (time == hit - 5) {
                        this.playSound(Dushi.sound("entity.bolt.sweep"), 2.0F, hit == SWEEP_HITS[0] ? 1.0F : 1.15F);
                    }
                    if (time == hit) {
                        this.hitAround(level, this.position(), SWEEP_REACH + this.getBbWidth() * 0.5, damage, 1.5, this.damageSources().mobAttack(this));
                        for (int i = 0; i < 8; i++) {
                            double a = i * Math.PI / 4.0;
                            level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + Math.cos(a) * 3.6, this.getY() + 1.0, this.getZ() + Math.sin(a) * 3.6, 1, 0.0, 0.0, 0.0, 0.0);
                        }
                    }
                }
            }
            default -> {
                if (time == LEAP_JUMP - 10) {
                    this.playSound(Dushi.sound("entity.bolt.strain"), 2.0F, 1.5F);
                }
                if (time == LEAP_JUMP) {
                    LivingEntity target = this.getTarget();
                    Vec3 aim = target != null ? target.position() : this.position().add(this.facing().scale(6.0));
                    Vec3 to = aim.subtract(this.position()).multiply(1.0, 0.0, 1.0);
                    if (to.length() > 15.0) {
                        to = to.scale(15.0 / to.length());
                    }
                    this.leapTo = ground(level, this.getX() + to.x, aim.y, this.getZ() + to.z);
                    this.strikeLater(this.leapTo, LEAP_LAND - LEAP_JUMP, LEAP_RADIUS, damage * 1.4F, false);
                    this.setDeltaMovement(to.x / (LEAP_LAND - LEAP_JUMP), 0.95, to.z / (LEAP_LAND - LEAP_JUMP));
                    this.playSound(Dushi.sound("entity.bolt.sweep"), 2.4F, 0.8F);
                }
                if (this.leapTo != null && time > LEAP_JUMP && time < LEAP_LAND) {
                    // (the air stops whatever flies through it: it is carried on to where it means to come down)
                    int left = LEAP_LAND - time;
                    this.setDeltaMovement((this.leapTo.x - this.getX()) / left, this.getDeltaMovement().y, (this.leapTo.z - this.getZ()) / left);
                }
                if (time == LEAP_LAND) {
                    this.setDeltaMovement(0.0, Math.min(0.0, this.getDeltaMovement().y), 0.0);
                    this.rubble(level, this.position(), 50, 1.6);
                    this.playSound(Dushi.sound("entity.bolt.land"), 3.0F, 1.0F);
                }
            }
        }
    }

    @Override
    protected void strikeComing(ServerLevel level, Vec3 at, double radius, int left, boolean fromAbove) {
        if (!fromAbove) {
            // where it will come down: the ring of the place, in sparks
            if (this.strikeClock() % 2 == 0) {
                for (int i = 0; i < 10; i++) {
                    double a = i * Math.PI / 5.0 + this.strikeClock() * 0.09;
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x + Math.cos(a) * radius, at.y + 0.15, at.z + Math.sin(a) * radius, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
            return;
        }
        // a weight on its chain, coming down out of the dark: amber dripping where it will land, and a ring
        if (this.strikeClock() % 2 == 0) {
            level.sendParticles(ParticleTypes.FALLING_HONEY, at.x, at.y + 5.0, at.z, 2, radius * 0.3, 0.8, radius * 0.3, 0.0);
            level.sendParticles(ParticleTypes.LANDING_HONEY, at.x, at.y + 0.1, at.z, 2, radius * 0.4, 0.0, radius * 0.4, 0.0);
        }
        if (this.strikeClock() % 3 == 0) {
            for (int i = 0; i < 8; i++) {
                double a = i * Math.PI / 4.0 + this.strikeClock() * 0.08;
                level.sendParticles(left < 10 ? ParticleTypes.END_ROD : ParticleTypes.ELECTRIC_SPARK, at.x + Math.cos(a) * radius, at.y + 0.1, at.z + Math.sin(a) * radius, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
        if (left == 6) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.IRON_BLOCK.defaultBlockState()), at.x, at.y + 4.0, at.z, 30, radius * 0.3, 2.0, radius * 0.3, 0.0);
        }
    }

    @Override
    protected void strikeLands(ServerLevel level, Vec3 at, double radius, float damage, boolean fromAbove) {
        this.hitAround(level, at, radius, damage, 0.5, this.damageSources().mobAttack(this));
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.IRON_BLOCK.defaultBlockState()), at.x, at.y + 0.3, at.z, fromAbove ? 40 : 20, radius * 0.4, 0.3, radius * 0.4, 0.2);
        level.sendParticles(ParticleTypes.POOF, at.x, at.y + 0.2, at.z, 8, radius * 0.4, 0.1, radius * 0.4, 0.03);
        if (fromAbove) {
            level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.bolt.weight"), this.getSoundSource(), 2.2F, 0.9F + this.random.nextFloat() * 0.2F);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return super.hurtServer(level, source, damage == Float.MAX_VALUE ? damage : damage * HIDE);
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
    protected void pushEntities() {
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.spawnAtLocation(level, new ItemStack(Dushi.BOLT_SOUL.get()));
        this.spawnAtLocation(level, new ItemStack(Dushi.FLASK_SHARD.get()));
        this.leave(level, Dushi.GREAT_SOUL.get(), 2, 3);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        this.setNoGravity(false);
        if (this.level() instanceof ServerLevel level) {
            BlockPos home = MobMarkBlockEntity.homeOf(this);
            FogGateBlock.lift(level, home != null ? home : (this.pivot != null ? BlockPos.containing(this.pivot.x, this.pivot.y, this.pivot.z) : this.blockPosition()), 40);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.down() ? Dushi.sound("entity.bolt.ambient") : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.bolt.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.bolt.death");
    }

    @Override
    protected float getSoundVolume() {
        return 2.4F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(Dushi.sound("entity.bolt.step"), 1.2F, 0.9F + this.random.nextFloat() * 0.2F);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.pivot != null) {
            output.putLong("Pivot", BlockPos.containing(this.pivot.x, this.pivot.y, this.pivot.z).asLong());
            output.putFloat("HomeYaw", this.homeYaw);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        long at = input.getLongOr("Pivot", Long.MIN_VALUE);
        if (at != Long.MIN_VALUE) {
            BlockPos pos = BlockPos.of(at);
            this.pivot = new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            this.homeYaw = input.getFloatOr("HomeYaw", 0.0F);
        }
        this.setNoGravity(this.phase() < 2);
    }
}
