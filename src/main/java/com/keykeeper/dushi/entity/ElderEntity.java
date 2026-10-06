package com.keykeeper.dushi.entity;

import java.util.Comparator;
import java.util.List;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.block.FogGateBlock;
import com.keykeeper.dushi.block.MobMarkBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
import net.minecraft.world.phys.Vec3;

/**
 * The Elder of the Unsleeping: the one who made them swear. He stands in the white tent in the middle of the skete as
 * he has stood since the oath - half as tall again as a man, a ring of candles alight on his head, a candlestick taller
 * than himself in his hand - and has not shut his eyes. The boss of the tent.
 * <p>
 * Awake he is slow and exact. The candlestick comes round level, the whole length of it; or its burning end is driven
 * in straight, which reaches further than it looks and sets alight. He holds the great candle up and shakes it, and
 * hot wax comes down wherever his enemy stands and is about to stand. And he strikes the foot of the candlestick on
 * the ground and calls: the nearest of those kneeling round the walls of the tent get up and come.
 * <p>
 * Hurt badly enough, the thing happens that he swore would not: he falls asleep, on his feet, in the middle of the
 * fight. The candles of his crown go out. What fights on is a sleepwalker with a pole - no waiting between blows,
 * four sweeps to and fro, a thrust from the far side of the tent; and from time to time he stands still, folded over
 * the candle, and what he is dreaming spreads out from him: whoever it reaches, it takes the sight and the legs of.
 * Rolled through, like any blow, it does nothing.
 */
public class ElderEntity extends SoulsMob {
    public static final int SWEEP = 1;
    public static final int SWEEP_DURATION = 44;
    public static final int SWEEP_IMPACT = 20;
    public static final int THRUST = 2;
    public static final int THRUST_DURATION = 38;
    public static final int THRUST_IMPACT = 18;
    public static final int WAX_RAIN = 3;
    public static final int RAIN_DURATION = 62;
    public static final int RAIN_FROM = 16;
    public static final int RAIN_UNTIL = 46;
    private static final int RAIN_EVERY = 6;
    /** How long the place a drop will fall on is shown before it falls. */
    public static final int RAIN_WARNING = 22;
    public static final int CALL = 4;
    public static final int CALL_DURATION = 56;
    public static final int CALL_KNOCK = 24;
    /** How far his call carries, and how many get up for one call. */
    public static final double CALL_RANGE = 26.0;
    public static final int CALLED = 2;
    /** He falls asleep: the move in which the second phase begins. */
    public static final int SLUMBER = 5;
    public static final int SLUMBER_DURATION = 58;
    public static final int FLURRY = 6;
    public static final int FLURRY_DURATION = 62;
    public static final int[] FLURRY_HITS = {12, 22, 32, 44};
    public static final int LUNGE = 7;
    public static final int LUNGE_DURATION = 44;
    public static final int LUNGE_START = 11;
    public static final int LUNGE_IMPACT = 19;
    public static final int DREAM = 8;
    public static final int DREAM_DURATION = 66;
    public static final int DREAM_SPREADS = 28;
    public static final double DREAM_RADIUS = 6.5;
    /** How long whoever the dream reached is without sight and slow, in ticks. */
    public static final int DREAM_HOLDS = 80;

    public static final double STAFF_REACH = 4.4;
    /** What gets through of every blow: the game lets nothing have more than 1024 health (see the Wallower). */
    public static final float HIDE = 0.5F;
    private static final Identifier ASLEEP_SPEED = Dushi.id("asleep");

    private int rainCooldown = 100;
    private int callCooldown = 160;
    private int lungeCooldown = 60;
    private int dreamCooldown = 120;
    private boolean left;

    public ElderEntity(EntityType<? extends ElderEntity> type, Level level) {
        super(type, level);
        this.xpReward = 320;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1000.0).add(Attributes.ATTACK_DAMAGE, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23).add(Attributes.ARMOR, 6.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 44.0);
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
        return 38000;
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    public boolean asleep() {
        return this.phase() >= 2;
    }

    @Override
    protected void setPhase(int phase) {
        super.setPhase(phase);
        // asleep, he is quicker on his feet than he ever was awake
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(ASLEEP_SPEED);
            if (phase >= 2) {
                speed.addTransientModifier(new AttributeModifier(ASLEEP_SPEED, 0.35, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }
    }

    @Override
    protected int attackDuration(int id) {
        return switch (id) {
            case SWEEP -> SWEEP_DURATION;
            case THRUST -> THRUST_DURATION;
            case WAX_RAIN -> RAIN_DURATION;
            case CALL -> CALL_DURATION;
            case SLUMBER -> SLUMBER_DURATION;
            case FLURRY -> FLURRY_DURATION;
            case LUNGE -> LUNGE_DURATION;
            default -> DREAM_DURATION;
        };
    }

    @Override
    protected int trackUntil(int id) {
        return switch (id) {
            case SWEEP -> SWEEP_IMPACT - 8;
            case THRUST -> THRUST_IMPACT - 6;
            case WAX_RAIN -> RAIN_DURATION;
            case CALL -> CALL_DURATION;
            case FLURRY -> FLURRY_HITS[3] - 4;
            case LUNGE -> LUNGE_START - 2;
            default -> 0;
        };
    }

    @Override
    protected int attackPause(int finishedId) {
        if (finishedId == SLUMBER) {
            return 2;
        }
        return this.asleep() ? 5 + this.random.nextInt(11) : 18 + this.random.nextInt(14);
    }

    @Override
    public double stopDistance() {
        return 3.0;
    }

    @Override
    protected void onReset() {
        super.onReset();
        this.rainCooldown = 100;
        this.callCooldown = 160;
    }

    /** Those on their knees within his call who are not up yet, the nearest first. */
    private List<WakefulEntity> kneeling(ServerLevel level) {
        List<WakefulEntity> all = level.getEntitiesOfClass(WakefulEntity.class, this.getBoundingBox().inflate(CALL_RANGE, 6.0, CALL_RANGE),
                wakeful -> wakeful.isAlive() && !wakeful.awake());
        all.sort(Comparator.comparingDouble(this::distanceToSqr));
        return all;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (this.asleep()) {
            if (distance > 5.0 && distance < 14.0 && this.lungeCooldown <= 0 && this.hasLineOfSight(target)) {
                this.lungeCooldown = 110 + this.random.nextInt(70);
                return LUNGE;
            }
            if (this.dreamCooldown <= 0 && distance < DREAM_RADIUS - 1.0) {
                this.dreamCooldown = 280 + this.random.nextInt(120);
                return DREAM;
            }
            if (distance < STAFF_REACH - 0.6) {
                return this.random.nextInt(10) < 7 ? FLURRY : SWEEP;
            }
            return distance < 5.2 ? THRUST : 0;
        }
        if (this.callCooldown <= 0 && this.level() instanceof ServerLevel level && !this.kneeling(level).isEmpty()) {
            this.callCooldown = 520 + this.random.nextInt(160);
            return CALL;
        }
        if (distance > 5.5 && this.rainCooldown <= 0 && this.hasLineOfSight(target)) {
            this.rainCooldown = 200 + this.random.nextInt(100);
            return WAX_RAIN;
        }
        if (distance < STAFF_REACH - 0.6) {
            if (this.rainCooldown <= 0 && this.random.nextInt(5) == 0) {
                this.rainCooldown = 260 + this.random.nextInt(100);
                return WAX_RAIN;
            }
            return this.random.nextInt(10) < 6 ? SWEEP : THRUST;
        }
        return distance < 5.2 ? THRUST : 0;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.phase() == 1 && this.getHealth() < this.getMaxHealth() * 0.5F && this.attackId() == 0 && this.isAlive()) {
            this.startAttack(SLUMBER);
        }
        super.customServerAiStep(level);
        this.rainCooldown = Math.max(0, this.rainCooldown - 1);
        this.callCooldown = Math.max(0, this.callCooldown - 1);
        this.lungeCooldown = Math.max(0, this.lungeCooldown - 1);
        this.dreamCooldown = Math.max(0, this.dreamCooldown - 1);
        if (this.asleep() && this.tickCount % 12 == 0) {
            level.sendParticles(ParticleTypes.WHITE_SMOKE, this.getX(), this.getY() + this.getBbHeight() + 0.5, this.getZ(), 1, 0.2, 0.1, 0.2, 0.01);
        }
    }

    /** One sweep of the candlestick, the whole length of it, from one side to the other. */
    private void sweep(ServerLevel level, float damage) {
        this.hitArc(level, STAFF_REACH, 78.0, damage, 1.0);
        float yaw = this.getYRot();
        for (int i = 0; i < 9; i++) {
            Vec3 at = this.position().add(Vec3.directionFromRotation(0.0F, yaw + (i - 4) * 18.0F).scale(STAFF_REACH - 0.6));
            level.sendParticles(ParticleTypes.SMALL_FLAME, at.x, at.y + 1.3, at.z, 1, 0.1, 0.1, 0.1, 0.0);
        }
        this.playSound(Dushi.sound("entity.elder.sweep"), 1.8F, 0.9F + this.random.nextFloat() * 0.2F);
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        LivingEntity target = this.getTarget();
        switch (id) {
            case SWEEP -> {
                if (time == SWEEP_IMPACT) {
                    this.sweep(level, damage);
                }
            }
            case THRUST -> {
                if (time == THRUST_IMPACT - 2) {
                    Vec3 step = this.facing().scale(0.4);
                    this.setDeltaMovement(step.x, this.getDeltaMovement().y, step.z);
                    this.playSound(Dushi.sound("entity.elder.sweep"), 1.4F, 1.4F);
                }
                if (time == THRUST_IMPACT) {
                    this.hitArc(level, 5.4, 12.0, damage * 1.2F, 0.6);
                    Vec3 at = this.position().add(this.facing().scale(4.6)).add(0.0, 1.3, 0.0);
                    level.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, 10, 0.2, 0.2, 0.2, 0.03);
                }
            }
            case WAX_RAIN -> {
                if (time == 4) {
                    this.playSound(Dushi.sound("entity.elder.chant"), 2.4F, 1.0F);
                }
                if (time >= RAIN_FROM - 6 && time <= RAIN_UNTIL && time % 2 == 0) {
                    level.sendParticles(ParticleTypes.FALLING_HONEY, this.getX(), this.getY() + this.getBbHeight() + 2.6, this.getZ(), 3, 0.5, 0.2, 0.5, 0.0);
                }
                if (target != null && time >= RAIN_FROM && time <= RAIN_UNTIL && (time - RAIN_FROM) % RAIN_EVERY == 0) {
                    // where they stand - and, every other drop, where they are making for; and one more, somewhere near
                    int n = (time - RAIN_FROM) / RAIN_EVERY;
                    Vec3 going = target.getKnownMovement().multiply(1.0, 0.0, 1.0);
                    Vec3 at = target.position().add(n % 2 == 1 ? going.scale(RAIN_WARNING * 0.8) : Vec3.ZERO);
                    this.strikeLater(new Vec3(at.x, target.getY(), at.z), RAIN_WARNING, 1.7, damage * 0.7F, true);
                    double angle = this.random.nextDouble() * Math.PI * 2.0;
                    double far = 2.5 + this.random.nextDouble() * 2.5;
                    this.strikeLater(new Vec3(at.x + Math.cos(angle) * far, target.getY(), at.z + Math.sin(angle) * far), RAIN_WARNING + 5, 1.7, damage * 0.7F, true);
                }
            }
            case CALL -> {
                if (time == CALL_KNOCK) {
                    this.playSound(Dushi.sound("entity.elder.knock"), 3.5F, 1.0F);
                    this.rubble(level, this.position().add(this.facing().scale(0.8)), 16, 0.4);
                    int called = 0;
                    for (WakefulEntity wakeful : this.kneeling(level)) {
                        if (called++ >= CALLED) {
                            break;
                        }
                        wakeful.rouse(target);
                        level.sendParticles(ParticleTypes.SMALL_FLAME, wakeful.getX(), wakeful.getY() + 2.2, wakeful.getZ(), 8, 0.2, 0.2, 0.2, 0.02);
                    }
                }
                if (time == CALL_KNOCK + 4) {
                    this.playSound(Dushi.sound("entity.elder.call"), 3.0F, 1.0F);
                }
            }
            case SLUMBER -> {
                if (time == 6) {
                    this.playSound(Dushi.sound("entity.elder.slumber"), 3.0F, 1.0F);
                }
                if (time == SLUMBER_DURATION - 8) {
                    // the candles of his crown go out, all at once
                    this.setPhase(2);
                    level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + this.getBbHeight() + 0.4, this.getZ(), 14, 0.35, 0.15, 0.35, 0.01);
                    this.lungeCooldown = 40;
                    this.dreamCooldown = 160;
                }
            }
            case FLURRY -> {
                for (int hit : FLURRY_HITS) {
                    if (time == hit - 3) {
                        Vec3 step = this.facing().scale(0.38);
                        this.setDeltaMovement(step.x, this.getDeltaMovement().y, step.z);
                    }
                    if (time == hit) {
                        this.sweep(level, damage * 0.8F);
                    }
                }
            }
            case LUNGE -> {
                if (time == 3) {
                    this.playSound(Dushi.sound("entity.elder.hurt"), 1.6F, 0.7F);
                }
                if (time >= LUNGE_START && time < LUNGE_IMPACT) {
                    Vec3 run = this.facing().scale(0.85);
                    this.setDeltaMovement(run.x, this.getDeltaMovement().y, run.z);
                }
                if (time == LUNGE_IMPACT) {
                    this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
                    this.hitArc(level, 5.4, 14.0, damage * 1.3F, 1.0);
                    Vec3 at = this.position().add(this.facing().scale(4.6)).add(0.0, 1.3, 0.0);
                    level.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, 10, 0.2, 0.2, 0.2, 0.03);
                    this.playSound(Dushi.sound("entity.elder.sweep"), 1.6F, 1.5F);
                }
            }
            default -> {
                // the dream: a ring of pale motes far out that closes on him, and then goes out from him all at once
                if (time == 2) {
                    this.playSound(Dushi.sound("entity.elder.dream"), 3.0F, 1.0F);
                }
                if (time < DREAM_SPREADS && time % 2 == 0) {
                    double r = DREAM_RADIUS * (1.0 - (double) time / DREAM_SPREADS);
                    for (int i = 0; i < 16; i++) {
                        double angle = i * Math.PI / 8.0 + time * 0.07;
                        level.sendParticles(ParticleTypes.END_ROD, this.getX() + Math.cos(angle) * r, this.getY() + 0.3, this.getZ() + Math.sin(angle) * r, 1, 0.0, 0.05, 0.0, 0.0);
                    }
                }
                if (time == DREAM_SPREADS) {
                    for (int ring = 1; ring <= 3; ring++) {
                        double r = DREAM_RADIUS * ring / 3.0;
                        for (int i = 0; i < 8 * ring; i++) {
                            double angle = i * Math.PI / (4.0 * ring);
                            level.sendParticles(ParticleTypes.WHITE_ASH, this.getX() + Math.cos(angle) * r, this.getY() + 0.8, this.getZ() + Math.sin(angle) * r, 4, 0.2, 0.5, 0.2, 0.0);
                        }
                    }
                    this.hitAround(level, this.position(), DREAM_RADIUS, damage * 0.3F, 0.0, this.damageSources().mobAttack(this));
                }
            }
        }
    }

    @Override
    protected void struck(ServerLevel level, LivingEntity victim) {
        int id = this.attackId();
        if ((id == THRUST || id == LUNGE) && !victim.isBlocking()) {
            victim.igniteForSeconds(4.0F);                       // the great candle, end on
        } else if (id == DREAM) {
            com.keykeeper.dushi.Gear.drowse(victim, this, DREAM_HOLDS, 2);
        }
    }

    // ------------------------------------------------------------------ the wax

    /** Where a drop will fall: a ring of small flames on the ground and the wax already coming down thin over it. */
    @Override
    protected void strikeComing(ServerLevel level, Vec3 at, double radius, int left, boolean fromAbove) {
        int clock = this.strikeClock();
        if (clock % 2 == 0) {
            level.sendParticles(ParticleTypes.FALLING_HONEY, at.x, at.y + 4.5, at.z, 2, radius * 0.4, 0.5, radius * 0.4, 0.0);
        }
        if (clock % 3 == 0) {
            for (int i = 0; i < 8; i++) {
                double angle = i * Math.PI / 4.0 + clock * 0.09;
                level.sendParticles(ParticleTypes.SMALL_FLAME, at.x + Math.cos(angle) * radius, at.y + 0.12, at.z + Math.sin(angle) * radius, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
        if (left == 5) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HONEYCOMB_BLOCK.defaultBlockState()), at.x, at.y + 4.0, at.z, 30, radius * 0.35, 2.0,
                    radius * 0.35, 0.0);
        }
    }

    @Override
    protected void strikeLands(ServerLevel level, Vec3 at, double radius, float damage, boolean fromAbove) {
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(at, at).inflate(radius + 1.0, 3.0, radius + 1.0), this::isFoe)) {
            double distance = Math.sqrt(victim.distanceToSqr(at.x, victim.getY(), at.z)) - victim.getBbWidth() * 0.5;
            if (distance <= radius && victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, WaxenEntity.SETS_FOR, 1, false, true, true), this);
            }
        }
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HONEYCOMB_BLOCK.defaultBlockState()), at.x, at.y + 0.3, at.z, 26, radius * 0.5, 0.3, radius * 0.5, 0.15);
        level.sendParticles(ParticleTypes.LANDING_HONEY, at.x, at.y + 0.1, at.z, 8, radius * 0.5, 0.05, radius * 0.5, 0.0);
        level.sendParticles(ParticleTypes.SMOKE, at.x, at.y + 0.4, at.z, 6, radius * 0.4, 0.2, radius * 0.4, 0.01);
        level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.waxen.slam"), this.getSoundSource(), 1.0F, 1.2F + this.random.nextFloat() * 0.3F);
    }

    // ------------------------------------------------------------------ the rest

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return super.hurtServer(level, source, damage == Float.MAX_VALUE ? damage : damage * HIDE);
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING) || super.isInvulnerableTo(level, source);
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
        this.spawnAtLocation(level, new ItemStack(Dushi.ELDER_SOUL.get()));
        this.spawnAtLocation(level, new ItemStack(Dushi.FLASK_SHARD.get()));
        this.leave(level, Dushi.GREAT_SOUL.get(), 3, 4);
        this.leave(level, Dushi.WAX.get(), 4, 6);
        this.leave(level, Dushi.LINEN.get(), 3, 5);
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
        return Dushi.sound(this.asleep() ? "entity.elder.snore" : "entity.elder.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.elder.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.elder.death");
    }

    @Override
    protected float getSoundVolume() {
        return 2.0F;
    }
}
