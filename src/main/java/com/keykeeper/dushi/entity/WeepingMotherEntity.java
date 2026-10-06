package com.keykeeper.dushi.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.block.FogGateBlock;
import com.keykeeper.dushi.block.MobMarkBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.phys.Vec3;

/**
 * The Weeping Mother: the oldest willow of the Mire, on the island in the middle of the withy wood, and the woman who
 * was hanged from it and never taken down. They have grown into one another. The boss of the island.
 * <p>
 * She does not leave her roots: the whole fight is fought round one tree. What she has instead of legs is reach. An arm
 * as long as the rest of her comes round level, a hand's breadth over the ground; both come down together on one place
 * and go through a shield. Whoever stands off out of their reach has her roots come up under them - the ground lifts
 * and smokes green first, wherever they stood a breath ago. She wails, and the lights of the marsh come to her. Hurt,
 * she turns about on her bole with both arms out, twice round: there is no standing clear of that except far off, or
 * right against her, under the arms.
 */
public class WeepingMotherEntity extends SoulsMob {
    public static final int LASH_RIGHT = 1;
    public static final int LASH_LEFT = 2;
    public static final int LASH_DURATION = 40;
    public static final int LASH_IMPACT = 17;
    public static final int SLAM = 3;
    public static final int SLAM_DURATION = 52;
    public static final int SLAM_IMPACT = 24;
    public static final int ROOTS = 4;
    public static final int ROOTS_DURATION = 62;
    /** Hurt, she sends more of them, and for longer. */
    public static final int ROOTS_DURATION_MANY = 78;
    public static final int ROOTS_FIRST = 14;
    public static final int ROOTS_EVERY = 11;
    /** How long the ground smokes before a root comes up through it. */
    public static final int ROOT_WARNING = 19;
    public static final double ROOT_RADIUS = 1.5;
    public static final int WAIL = 5;
    public static final int WAIL_DURATION = 56;
    public static final int WAIL_CRY = 20;
    public static final int WHIRL = 6;
    public static final int WHIRL_DURATION = 66;
    public static final int WHIRL_FIRST = 22;
    public static final int WHIRL_SECOND = 37;
    /** How far her hands reach along the ground, and how near her bole they cannot come. */
    public static final double REACH = 6.4;
    /** Turning, her arms are out to the sides and do not reach so far - nor down to what stands against the bole. */
    public static final double WHIRL_REACH = 5.8;
    public static final double UNDER_ARMS = 2.3;
    /** She does nothing to whoever is further off than this: the island is twelve blocks from her to its edge. */
    public static final double HEARS = 14.5;

    /** What gets through her bark of every blow: the game lets nothing have more than 1024 health (see the Wallower). */
    public static final float HIDE = 0.65F;

    private record Root(Vec3 at, int due) {}

    private final List<Root> roots = new ArrayList<>();
    private final List<WispEntity> lights = new ArrayList<>();
    private int rootsCooldown = 70;
    private int wailCooldown = 140;
    private int whirlCooldown = 100;
    private int slamCooldown = 50;
    private int age;
    private boolean left;

    public WeepingMotherEntity(EntityType<? extends WeepingMotherEntity> type, Level level) {
        super(type, level);
        this.xpReward = 250;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1000.0).add(Attributes.ATTACK_DAMAGE, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.ARMOR, 6.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 30.0);
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
        return 24000;
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    public int rootsWaiting() {
        return this.roots.size();
    }

    public int lightsCalled() {
        this.lights.removeIf(light -> !light.isAlive());
        return this.lights.size();
    }

    @Override
    protected int attackDuration(int id) {
        return switch (id) {
            case LASH_RIGHT, LASH_LEFT -> LASH_DURATION;
            case SLAM -> SLAM_DURATION;
            case ROOTS -> this.phase() >= 2 ? ROOTS_DURATION_MANY : ROOTS_DURATION;
            case WAIL -> WAIL_DURATION;
            default -> WHIRL_DURATION;
        };
    }

    @Override
    protected int trackUntil(int id) {
        return switch (id) {
            case LASH_RIGHT, LASH_LEFT -> LASH_IMPACT - 7;
            case SLAM -> SLAM_IMPACT - 9;
            case ROOTS -> ROOTS_DURATION_MANY;
            case WAIL -> WAIL_DURATION;
            default -> 0;
        };
    }

    @Override
    protected int attackPause(int finishedId) {
        return this.phase() >= 2 ? 8 + this.random.nextInt(8) : 15 + this.random.nextInt(12);
    }

    /** She has nowhere to go; the distance only decides what she does. */
    @Override
    public double stopDistance() {
        return 64.0;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        double far = distance + (this.getBbWidth() + target.getBbWidth()) * 0.5;
        if (far > HEARS) {
            return 0;                                            // still outside her ring of roots: she waits
        }
        int called = this.lightsCalled();
        if (this.wailCooldown <= 0 && called < (this.phase() >= 2 ? 3 : 2)) {
            this.wailCooldown = (this.phase() >= 2 ? 420 : 560) + this.random.nextInt(160);
            return WAIL;
        }
        if (far > REACH - 0.4) {
            if (this.rootsCooldown <= 0) {
                this.rootsCooldown = (this.phase() >= 2 ? 30 : 50) + this.random.nextInt(30);
                return ROOTS;
            }
            return 0;
        }
        if (this.phase() >= 2 && this.whirlCooldown <= 0) {
            this.whirlCooldown = 200 + this.random.nextInt(120);
            return WHIRL;
        }
        if (this.rootsCooldown <= 0 && this.random.nextInt(4) == 0) {
            this.rootsCooldown = 120 + this.random.nextInt(60);
            return ROOTS;
        }
        if (this.slamCooldown <= 0 && this.random.nextInt(3) == 0) {
            this.slamCooldown = 90 + this.random.nextInt(50);
            return SLAM;
        }
        this.left = !this.left;
        return this.left ? LASH_LEFT : LASH_RIGHT;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.phase() == 1 && this.getHealth() < this.getMaxHealth() * 0.5F && this.attackId() == 0 && this.isAlive()) {
            this.setPhase(2);
            this.wailCooldown = 300;
            this.whirlCooldown = 70;
            this.startAttack(WAIL);
        }
        super.customServerAiStep(level);
        this.rootsCooldown = Math.max(0, this.rootsCooldown - 1);
        this.wailCooldown = Math.max(0, this.wailCooldown - 1);
        this.whirlCooldown = Math.max(0, this.whirlCooldown - 1);
        this.slamCooldown = Math.max(0, this.slamCooldown - 1);
        this.age++;
        Vec3 motion = this.getDeltaMovement();
        if (motion.x != 0.0 || motion.z != 0.0) {
            this.setDeltaMovement(0.0, motion.y, 0.0);           // a blast moves what knockback does not: not her
        }
        if (!this.roots.isEmpty()) {
            this.rootsTick(level);
        }
        // she turns on her bole after whoever she is watching, about as fast as a man runs round her at arm's length
        LivingEntity target = this.getTarget();
        if (target != null && this.attackId() == 0) {
            float want = (float) (Mth.atan2(target.getZ() - this.getZ(), target.getX() - this.getX()) * (180.0 / Math.PI)) - 90.0F;
            float yaw = Mth.approachDegrees(this.getYRot(), want, 7.0F);
            this.setYRot(yaw);
            this.setYBodyRot(yaw);
        }
    }

    // ------------------------------------------------------------------ the roots

    /** The ground at {@code at} smokes green, and a root comes up through it after the warning. */
    private void root(Vec3 at, int delay) {
        this.roots.add(new Root(at, this.age + delay));
    }

    private void rootsTick(ServerLevel level) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        Iterator<Root> all = this.roots.iterator();
        while (all.hasNext()) {
            Root root = all.next();
            Vec3 at = root.at();
            if (this.age < root.due()) {
                // the place is drawn on the ground: a ring of green, and earth jumping inside it
                if (this.age % 2 == 0) {
                    for (int i = 0; i < 10; i++) {
                        double angle = i * Math.PI / 5.0 + this.age * 0.12;
                        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, at.x + Math.cos(angle) * ROOT_RADIUS, at.y + 0.15, at.z + Math.sin(angle) * ROOT_RADIUS,
                                1, 0.0, 0.0, 0.0, 0.0);
                    }
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState()), at.x, at.y + 0.1, at.z, 3,
                            ROOT_RADIUS * 0.4, 0.05, ROOT_RADIUS * 0.4, 0.0);
                }
                continue;
            }
            all.remove();
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class,
                    new net.minecraft.world.phys.AABB(at, at).inflate(ROOT_RADIUS + 1.0, 3.0, ROOT_RADIUS + 1.0), this::isFoe)) {
                double distance = Math.sqrt(victim.distanceToSqr(at.x, victim.getY(), at.z)) - victim.getBbWidth() * 0.5;
                if (distance <= ROOT_RADIUS && victim.hurtServer(level, this.damageSources().mobAttack(this), damage * 0.9F)) {
                    shove(victim, new Vec3(0.0, 0.62, 0.0));
                }
            }
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MANGROVE_ROOTS.defaultBlockState()), at.x, at.y + 0.8, at.z, 46,
                    ROOT_RADIUS * 0.35, 0.9, ROOT_RADIUS * 0.35, 0.1);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState()), at.x, at.y + 0.2, at.z, 20,
                    ROOT_RADIUS * 0.5, 0.2, ROOT_RADIUS * 0.5, 0.1);
            level.sendParticles(ParticleTypes.COMPOSTER, at.x, at.y + 1.2, at.z, 8, 0.4, 0.8, 0.4, 0.0);
            level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.weeping_mother.root"), this.getSoundSource(), 1.5F, 0.85F + this.random.nextFloat() * 0.3F);
        }
    }

    // ------------------------------------------------------------------ the moves

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        switch (id) {
            case LASH_RIGHT, LASH_LEFT -> {
                if (time == LASH_IMPACT - 5) {
                    this.playSound(Dushi.sound("entity.weeping_mother.lash"), 2.0F, 0.9F + this.random.nextFloat() * 0.2F);
                }
                if (time == LASH_IMPACT) {
                    this.hitArc(level, REACH, 78.0, damage, 1.2);
                    this.sweep(level, 78.0, 16);
                }
            }
            case SLAM -> {
                if (time == SLAM_IMPACT - 6) {
                    this.playSound(Dushi.sound("entity.weeping_mother.lash"), 2.2F, 0.7F);
                }
                if (time == SLAM_IMPACT) {
                    this.hitArc(level, this.facing(), REACH + 0.3, 21.0, damage * 1.5F, 0.6, true);
                    Vec3 at = this.position().add(this.facing().scale(6.0));
                    this.rubble(level, at, 26, 0.9);
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState()), at.x, at.y + 0.2, at.z, 50, 0.9, 0.2, 0.9, 0.1);
                    this.playSound(Dushi.sound("entity.weeping_mother.slam"), 3.0F, 1.0F);
                }
            }
            case ROOTS -> {
                if (time == 2) {
                    this.playSound(Dushi.sound("entity.weeping_mother.creak"), 2.6F, 1.0F);
                }
                LivingEntity target = this.getTarget();
                int count = this.phase() >= 2 ? 5 : 3;
                for (int i = 0; i < count && target != null; i++) {
                    if (time == ROOTS_FIRST + i * ROOTS_EVERY) {
                        // under where they stand - and, every other one, under where they are making for
                        Vec3 going = target.getKnownMovement().multiply(1.0, 0.0, 1.0);
                        Vec3 at = target.position().add(i % 2 == 1 ? going.scale(ROOT_WARNING * 0.8) : Vec3.ZERO);
                        this.root(new Vec3(at.x, target.getY(), at.z), ROOT_WARNING);
                        if (this.phase() >= 2 && i % 2 == 0) {
                            double angle = this.random.nextDouble() * Math.PI * 2.0;
                            this.root(new Vec3(at.x + Math.cos(angle) * 3.4, target.getY(), at.z + Math.sin(angle) * 3.4), ROOT_WARNING + 4);
                        }
                    }
                }
            }
            case WAIL -> {
                if (time == WAIL_CRY - 14) {
                    this.playSound(Dushi.sound("entity.weeping_mother.wail"), 4.0F, this.phase() >= 2 ? 0.9F : 1.0F);
                }
                if (time == WAIL_CRY) {
                    this.wail(level);
                }
                if (time >= WAIL_CRY && time < WAIL_CRY + 16 && time % 2 == 0) {
                    level.sendParticles(ParticleTypes.GLOW, this.getX(), this.getY() + 4.4, this.getZ(), 6, 1.6, 0.8, 1.6, 0.02);
                }
            }
            default -> {
                if (time == 6) {
                    this.playSound(Dushi.sound("entity.weeping_mother.creak"), 2.6F, 1.3F);
                }
                if (time == WHIRL_FIRST || time == WHIRL_SECOND) {
                    this.whirl(level, damage * 1.2F);
                    this.playSound(Dushi.sound("entity.weeping_mother.lash"), 2.4F, time == WHIRL_FIRST ? 0.8F : 0.95F);
                }
            }
        }
    }

    /** Leaves and water thrown up along the path of a hand. */
    private void sweep(ServerLevel level, double halfAngle, int count) {
        float yaw = this.getYRot();
        for (int i = 0; i < count; i++) {
            float angle = yaw + (float) ((i / (count - 1.0) * 2.0 - 1.0) * halfAngle);
            Vec3 at = this.position().add(Vec3.directionFromRotation(0.0F, angle).scale(REACH - 1.0));
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, at.x, at.y + 0.8, at.z, 2, 0.3, 0.3, 0.3, 0.0);
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y + 0.9, at.z, i % 4 == 0 ? 1 : 0, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /** Everything about her is thrown back, and the lights come. */
    private void wail(ServerLevel level) {
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0, 2.0, 8.0), this::isFoe)) {
            Vec3 away = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ());
            double far = away.length();
            if (far < 8.5) {
                away = far < 1.0E-3 ? this.facing() : away.scale(1.0 / far);
                shove(victim, new Vec3(away.x * 1.1, 0.42, away.z * 1.1));
            }
        }
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX(), this.getY() + 3.0, this.getZ(), 120, 3.0, 2.0, 3.0, 0.0);
        int want = (this.phase() >= 2 ? 3 : 2) - this.lightsCalled();
        for (int i = 0; i < want; i++) {
            WispEntity light = Dushi.WISP.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (light == null) {
                break;
            }
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            light.setPos(this.getX() + Math.cos(angle) * 9.0, this.getY() + 2.4, this.getZ() + Math.sin(angle) * 9.0);
            light.called();
            light.setTarget(this.getTarget());
            level.addFreshEntity(light);
            level.sendParticles(ParticleTypes.GLOW, light.getX(), light.getY() + 0.4, light.getZ(), 16, 0.3, 0.3, 0.3, 0.05);
            this.lights.add(light);
        }
    }

    /** One turn of her arms: everything within their reach, all round, but not what stands right against the bole. */
    private void whirl(ServerLevel level, float damage) {
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(WHIRL_REACH + 1.0, 1.0, WHIRL_REACH + 1.0), this::isFoe)) {
            Vec3 away = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ());
            double far = away.length();
            if (far - victim.getBbWidth() * 0.5 > WHIRL_REACH || far < UNDER_ARMS) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                away = away.scale(1.0 / far);
                shove(victim, new Vec3(away.x * 0.9, 0.35, away.z * 0.9));
            }
        }
        for (int i = 0; i < 28; i++) {
            double angle = i * Math.PI / 14.0;
            double far = WHIRL_REACH - 0.6 - (i % 3);
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX() + Math.cos(angle) * far, this.getY() + 0.9, this.getZ() + Math.sin(angle) * far,
                    2, 0.3, 0.3, 0.3, 0.0);
            if (i % 4 == 0) {
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + Math.cos(angle) * (WHIRL_REACH - 1.2), this.getY() + 1.0,
                        this.getZ() + Math.sin(angle) * (WHIRL_REACH - 1.2), 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }

    // ------------------------------------------------------------------ a tree

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    /** Her bole is a bole: it is walked round, not through. The lights she calls pass. */
    @Override
    public boolean canBeCollidedWith(Entity other) {
        return this.isAlive() && !(other instanceof SoulsMob);
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

    private void lightsOut() {
        for (WispEntity light : this.lights) {
            if (light.isAlive()) {
                if (light.level() instanceof ServerLevel level) {
                    level.sendParticles(ParticleTypes.GLOW, light.getX(), light.getY() + 0.4, light.getZ(), 12, 0.3, 0.3, 0.3, 0.05);
                }
                light.discard();
            }
        }
        this.lights.clear();
    }

    @Override
    protected void onReset() {
        super.onReset();
        this.roots.clear();
        this.lightsOut();
        this.rootsCooldown = 70;
        this.wailCooldown = 140;
    }

    @Override
    public void remove(RemovalReason reason) {
        this.lightsOut();
        super.remove(reason);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.spawnAtLocation(level, new ItemStack(Dushi.MOTHER_SOUL.get()));
        this.spawnAtLocation(level, new ItemStack(Dushi.FLASK_SHARD.get()));
        this.leave(level, Dushi.GREAT_SOUL.get(), 2, 3);
        this.leave(level, Dushi.WITHE.get(), 3, 5);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        this.roots.clear();
        this.lightsOut();
        if (this.level() instanceof ServerLevel level) {
            BlockPos home = MobMarkBlockEntity.homeOf(this);
            FogGateBlock.lift(level, home != null ? home : this.blockPosition(), 40);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.weeping_mother.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.weeping_mother.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.weeping_mother.death");
    }

    @Override
    protected float getSoundVolume() {
        return 2.5F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
    }
}
