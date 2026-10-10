package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.block.FogGateBlock;
import com.keykeeper.dushi.block.MobMarkBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The Coming: what was to arrive on the day that was not let begin - or as much of it as had got through when the day
 * was stopped. It stands in a tear in the floor of the Sky Beneath and cannot leave it, because the rest of it is on
 * the other side: a neck like a tower, a black disc with a rim of light where a head would be, two arms out of the
 * same tear. The Founder's rite nailed a mask of gold over the disc. The second lord of the place, and the last.
 * <p>
 * Masked (phase 1), it looks through the slits of the mask, and where it looks light stands on the ground, in a line,
 * and strikes down along it; it brings its head down full length on whoever stands off from it, and lies so a moment
 * (the time to strike it); it sweeps an arm round, low; and whoever hides against its foot, it brings both arms down on.
 * <p>
 * Hurt enough, the mask goes. The arms folded behind the disc open into a crown round it, and the front of the neck
 * opens too (phase 2): it draws everything in the hall towards the mouth, and bites; and the sky of the place, which
 * is under it and over it, comes down in pieces wherever anybody stands.
 */
public class ComingEntity extends SoulsMob {
    public static final int GAZE = 1;
    public static final int GAZE_DURATION = 92;
    public static final int GAZE_LOOK = 26;
    public static final int SLAM = 2;
    public static final int SLAM_DURATION = 112;
    public static final int SLAM_IMPACT = 40;
    /** Until this tick the head lies where it fell. */
    public static final int SLAM_LAIN = 84;
    public static final int SWEEP = 3;
    public static final int SWEEP_DURATION = 82;
    public static final int SWEEP_IMPACT = 36;
    public static final int CRUSH = 4;
    public static final int CRUSH_DURATION = 66;
    public static final int CRUSH_IMPACT = 28;
    public static final int UNMASK = 5;
    public static final int UNMASK_DURATION = 104;
    public static final int UNMASK_BREAK = 52;
    public static final int PULL = 6;
    public static final int PULL_DURATION = 124;
    public static final int PULL_FROM = 22;
    public static final int PULL_UNTIL = 82;
    public static final int PULL_BITE = 92;
    public static final int RAIN = 7;
    public static final int RAIN_DURATION = 104;
    public static final int RAIN_FROM = 22;
    public static final int RAIN_UNTIL = 74;
    /** How far from its foot the head comes down, and how wide. */
    public static final double SLAM_FAR = 7.0;
    public static final double SLAM_RADIUS = 3.0;
    public static final double SWEEP_REACH = 7.5;
    public static final double CRUSH_REACH = 4.4;
    public static final double PULL_RANGE = 16.0;
    public static final double STAR_RADIUS = 1.9;
    public static final int STAR_DELAY = 26;
    /** The share of a blow that gets through to it (nothing has more than 1024 health; it needs more). */
    public static final float HIDE = 0.3F;

    private int gazeCooldown = 70;
    private int sweepCooldown = 110;
    private int pullCooldown = 160;
    private int rainCooldown = 90;

    public ComingEntity(EntityType<? extends ComingEntity> type, Level level) {
        super(type, level);
        this.xpReward = 500;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1000.0).add(Attributes.ATTACK_DAMAGE, 36.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.ARMOR, 6.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 24.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public int soulReward() {
        return 45000;
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    protected BossEvent.BossBarColor bossBarColor() {
        return BossEvent.BossBarColor.PURPLE;
    }

    /** The mask is off. */
    public boolean bare() {
        return this.phase() >= 2;
    }

    /** Its head lying where it came down: when a blow tells for double. */
    public boolean open() {
        return this.attackId() == SLAM && this.attackTime > SLAM_IMPACT + 2 && this.attackTime < SLAM_LAIN;
    }

    @Override
    protected int attackDuration(int id) {
        return switch (id) {
            case GAZE -> GAZE_DURATION;
            case SLAM -> SLAM_DURATION;
            case SWEEP -> SWEEP_DURATION;
            case CRUSH -> CRUSH_DURATION;
            case UNMASK -> UNMASK_DURATION;
            case PULL -> PULL_DURATION;
            default -> RAIN_DURATION;
        };
    }

    @Override
    protected int trackUntil(int id) {
        return switch (id) {
            case GAZE -> GAZE_LOOK - 4;
            case SLAM -> SLAM_IMPACT - 14;
            case SWEEP -> SWEEP_IMPACT - 14;
            case PULL -> PULL_UNTIL;
            default -> 0;
        };
    }

    @Override
    protected int attackPause(int finishedId) {
        if (finishedId == UNMASK) {
            return 10;
        }
        return this.bare() ? 12 + this.random.nextInt(12) : 22 + this.random.nextInt(16);
    }

    @Override
    public double stopDistance() {
        return 64.0;
    }

    @Override
    protected void onReset() {
        super.onReset();
        this.gazeCooldown = 70;
        this.sweepCooldown = 110;
        this.pullCooldown = 160;
        this.rainCooldown = 90;
        BlockPos home = MobMarkBlockEntity.homeOf(this);
        if (home != null) {
            this.teleportTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);
        }
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (this.bare()) {
            if (this.pullCooldown <= 0 && distance > 5.0) {
                this.pullCooldown = 320 + this.random.nextInt(140);
                return PULL;
            }
            if (this.rainCooldown <= 0) {
                this.rainCooldown = 230 + this.random.nextInt(110);
                return RAIN;
            }
        }
        if (distance < CRUSH_REACH - 1.2) {
            return CRUSH;
        }
        if (this.sweepCooldown <= 0 && distance < SWEEP_REACH - 0.8) {
            this.sweepCooldown = (this.bare() ? 130 : 180) + this.random.nextInt(80);
            return SWEEP;
        }
        if (distance > SLAM_FAR - SLAM_RADIUS + 0.4 && distance < SLAM_FAR + SLAM_RADIUS - 0.6 && this.random.nextInt(3) != 0) {
            return SLAM;
        }
        if (this.gazeCooldown <= 0 && distance > 4.0) {
            this.gazeCooldown = (this.bare() ? 90 : 130) + this.random.nextInt(60);
            return GAZE;
        }
        return distance < SLAM_FAR + SLAM_RADIUS - 0.6 && distance > CRUSH_REACH - 1.2 ? SLAM : 0;
    }

    private static Vec3 ground(Level level, double x, double y, double z) {
        Vec3 found = RootEntity.ground(level, x, y, z);
        return found != null ? found : new Vec3(x, y, z);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.phase() == 1 && this.getHealth() < this.getMaxHealth() * 0.5F && this.attackId() == 0 && this.isAlive()) {
            this.startAttack(UNMASK);
        }
        super.customServerAiStep(level);
        this.gazeCooldown = Math.max(0, this.gazeCooldown - 1);
        this.sweepCooldown = Math.max(0, this.sweepCooldown - 1);
        this.pullCooldown = Math.max(0, this.pullCooldown - 1);
        this.rainCooldown = Math.max(0, this.rainCooldown - 1);
        // it stands in the tear, and only turns
        Vec3 motion = this.getDeltaMovement();
        if (motion.x != 0.0 || motion.z != 0.0) {
            this.setDeltaMovement(0.0, motion.y, 0.0);
        }
        LivingEntity target = this.getTarget();
        if (target != null && this.attackId() == 0) {
            float want = (float) (Mth.atan2(target.getZ() - this.getZ(), target.getX() - this.getX()) * (180.0 / Math.PI)) - 90.0F;
            float yaw = Mth.approachDegrees(this.getYRot(), want, this.bare() ? 5.0F : 3.5F);
            this.setYRot(yaw);
            this.setYBodyRot(yaw);
        }
    }

    /** Light standing on the ground where it will strike, {@code delay} ticks on. */
    private void star(ServerLevel level, Vec3 at, int delay, float damage) {
        this.strikeLater(at, delay, STAR_RADIUS, damage, true);
        SunShaftEntity.dusk(level, at, 1.7F, delay, delay, delay + 9);
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        switch (id) {
            case GAZE -> {
                if (time == 4) {
                    this.playSound(Dushi.sound("entity.coming.gaze"), 4.0F, 1.0F);
                }
                if (time == GAZE_LOOK) {
                    // where it looks: a line of light from its foot out past whoever it is looking at; three lines with the mask off
                    LivingEntity target = this.getTarget();
                    Vec3 forward = target != null ? new Vec3(target.getX() - this.getX(), 0.0, target.getZ() - this.getZ()) : this.facing();
                    forward = forward.lengthSqr() < 1.0E-4 ? this.facing() : forward.normalize();
                    double[] turns = this.bare() ? new double[] {0.0, 0.42, -0.42} : new double[] {0.0};
                    for (double turn : turns) {
                        Vec3 line = forward.yRot((float) turn);
                        for (int i = 0; i < 7; i++) {
                            double far = 4.0 + i * 2.7;
                            this.star(level, ground(level, this.getX() + line.x * far, this.getY(), this.getZ() + line.z * far), 22 + i * 4, damage * 0.8F);
                        }
                    }
                }
            }
            case SLAM -> {
                if (time == 6) {
                    this.playSound(Dushi.sound("entity.coming.rear"), 4.0F, 1.0F);
                }
                if (time == SLAM_IMPACT) {
                    Vec3 at = this.position().add(this.facing().scale(SLAM_FAR));
                    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(SLAM_RADIUS + 1.0, 3.0, SLAM_RADIUS + 1.0), this::isFoe)) {
                        if (Math.hypot(victim.getX() - at.x, victim.getZ() - at.z) - victim.getBbWidth() * 0.5 > SLAM_RADIUS) {
                            continue;
                        }
                        if (victim.isBlocking()) {
                            breakGuard(level, victim);
                        }
                        if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage * 1.6F)) {
                            Vec3 away = new Vec3(victim.getX() - at.x, 0.0, victim.getZ() - at.z);
                            away = away.lengthSqr() < 1.0E-3 ? this.facing() : away.normalize();
                            shove(victim, new Vec3(away.x * 0.7, 0.4, away.z * 0.7));
                        }
                    }
                    // and along the neck, from the foot to the head
                    for (double far = 2.5; far < SLAM_FAR - 1.5; far += 1.5) {
                        Vec3 on = this.position().add(this.facing().scale(far));
                        this.hitAround(level, on, 1.4, damage, 0.9, this.damageSources().mobAttack(this));
                    }
                    level.sendParticles(ParticleTypes.SPLASH, at.x, at.y + 0.2, at.z, 120, 2.0, 0.2, 2.0, 0.3);
                    level.sendParticles(ParticleTypes.END_ROD, at.x, at.y + 0.5, at.z, 40, 2.0, 0.5, 2.0, 0.08);
                    level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.coming.slam"), this.getSoundSource(), 4.0F, 1.0F);
                }
            }
            case SWEEP -> {
                if (time == SWEEP_IMPACT - 12) {
                    this.playSound(Dushi.sound("entity.coming.sweep"), 3.0F, 1.0F);
                }
                if (time == SWEEP_IMPACT) {
                    this.hitArc(level, SWEEP_REACH, 105.0, damage, 1.7);
                    for (int i = -3; i <= 3; i++) {
                        Vec3 at = this.position().add(this.facing().yRot(i * 0.5F).scale(5.5));
                        level.sendParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y + 0.9, at.z, 1, 0.3, 0.0, 0.3, 0.0);
                    }
                }
            }
            case CRUSH -> {
                if (time == CRUSH_IMPACT - 10) {
                    this.playSound(Dushi.sound("entity.coming.sweep"), 3.0F, 0.8F);
                }
                if (time == CRUSH_IMPACT) {
                    this.hitAround(level, this.position(), CRUSH_REACH, damage * 0.9F, 2.0, this.damageSources().mobAttack(this));
                    level.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 0.2, this.getZ(), 100, 3.0, 0.2, 3.0, 0.3);
                    this.playSound(Dushi.sound("entity.coming.slam"), 3.0F, 1.2F);
                }
            }
            case UNMASK -> {
                if (time == 6) {
                    this.playSound(Dushi.sound("entity.coming.strain"), 4.0F, 1.0F);
                }
                if (time > 10 && time < UNMASK_BREAK && time % 3 == 0) {
                    Vec3 head = this.position().add(0.0, this.getBbHeight() * 0.9, 0.0);
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GOLD_BLOCK.defaultBlockState()), head.x, head.y, head.z, 4, 0.8, 0.6, 0.8, 0.05);
                }
                if (time == UNMASK_BREAK) {
                    this.setPhase(2);
                    Vec3 head = this.position().add(0.0, this.getBbHeight() * 0.9, 0.0);
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GOLD_BLOCK.defaultBlockState()), head.x, head.y, head.z, 160, 1.2, 1.2, 1.2, 0.3);
                    level.sendParticles(net.minecraft.core.particles.ColorParticleOption.create(ParticleTypes.FLASH, 0xFFD8C8FF), head.x, head.y, head.z, 1, 0.0, 0.0, 0.0, 0.0);
                    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(11.0, 2.0, 11.0), this::isFoe)) {
                        Vec3 away = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ());
                        double far = away.length();
                        if (far < 11.5) {
                            away = far < 1.0E-3 ? this.facing() : away.scale(1.0 / far);
                            victim.hurtServer(level, this.damageSources().mobAttack(this), damage * 0.5F);
                            shove(victim, new Vec3(away.x * 1.1, 0.4, away.z * 1.1));
                        }
                    }
                    this.playSound(Dushi.sound("entity.coming.unmask"), 5.0F, 1.0F);
                }
            }
            case PULL -> {
                if (time == 4) {
                    this.playSound(Dushi.sound("entity.coming.pull"), 5.0F, 1.0F);
                }
                if (time >= PULL_FROM && time <= PULL_UNTIL) {
                    // everything in the hall goes towards the mouth: it is walked against, or run from
                    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(PULL_RANGE, 3.0, PULL_RANGE), this::isFoe)) {
                        Vec3 to = new Vec3(this.getX() - victim.getX(), 0.0, this.getZ() - victim.getZ());
                        double far = to.length();
                        if (far > PULL_RANGE || far < 2.2) {
                            continue;
                        }
                        Vec3 pull = to.scale(0.058 / far);
                        victim.setDeltaMovement(victim.getDeltaMovement().add(pull.x, 0.0, pull.z));
                        victim.needsSync = true;
                        if (victim instanceof ServerPlayer player && time % 2 == 0) {
                            player.connection.send(new ClientboundSetEntityMotionPacket(player));
                        }
                    }
                    if (time % 2 == 0) {
                        double a = this.random.nextDouble() * Math.PI * 2.0;
                        double far = 5.0 + this.random.nextDouble() * 9.0;
                        level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX() + Math.cos(a) * far, this.getY() + 0.6 + this.random.nextDouble() * 2.0,
                                this.getZ() + Math.sin(a) * far, 4, 0.3, 0.3, 0.3, 0.02);
                    }
                }
                if (time == PULL_BITE - 6) {
                    this.playSound(Dushi.sound("entity.coming.rear"), 4.0F, 1.3F);
                }
                if (time == PULL_BITE) {
                    Vec3 at = this.position().add(this.facing().scale(2.4));
                    this.hitAround(level, at, 3.8, damage * 1.7F, 1.6, this.damageSources().mobAttack(this));
                    this.playSound(Dushi.sound("entity.coming.bite"), 4.0F, 1.0F);
                }
            }
            default -> {
                if (time == 4) {
                    this.playSound(Dushi.sound("entity.coming.gaze"), 4.0F, 0.7F);
                }
                if (time >= RAIN_FROM && time <= RAIN_UNTIL && time % 4 == 2) {
                    // the sky of the place comes down: where they stand, where they are going, and anywhere
                    LivingEntity target = this.getTarget();
                    Vec3 at;
                    int kind = (time / 4) % 3;
                    if (target != null && kind == 0) {
                        at = target.position();
                    } else if (target != null && kind == 1) {
                        at = target.position().add(target.getKnownMovement().multiply(1.0, 0.0, 1.0).scale(16.0));
                    } else {
                        double a = this.random.nextDouble() * Math.PI * 2.0;
                        double far = 4.0 + this.random.nextDouble() * 12.0;
                        at = this.position().add(Math.cos(a) * far, 0.0, Math.sin(a) * far);
                    }
                    this.star(level, ground(level, at.x, this.getY(), at.z), STAR_DELAY, damage * 0.75F);
                }
            }
        }
    }

    @Override
    protected void strikeComing(ServerLevel level, Vec3 at, double radius, int left, boolean fromAbove) {
        if (this.strikeClock() % 3 == 0) {
            int points = 8;
            for (int i = 0; i < points; i++) {
                double a = i * Math.PI * 2.0 / points + this.strikeClock() * 0.07;
                level.sendParticles(left < 8 ? ParticleTypes.END_ROD : ParticleTypes.WITCH, at.x + Math.cos(a) * radius, at.y + 0.12, at.z + Math.sin(a) * radius, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
        if (left == STAR_DELAY - 2 || left == 20) {
            level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.coming.star"), this.getSoundSource(), 1.0F, 0.9F + this.random.nextFloat() * 0.3F);
        }
    }

    @Override
    protected void strikeLands(ServerLevel level, Vec3 at, double radius, float damage, boolean fromAbove) {
        this.hitAround(level, at, radius, damage, 0.3, this.damageSources().indirectMagic(this, this));
        level.sendParticles(ParticleTypes.END_ROD, at.x, at.y + 0.4, at.z, 24, radius * 0.4, 0.5, radius * 0.4, 0.06);
        level.sendParticles(ParticleTypes.WITCH, at.x, at.y + 0.3, at.z, 16, radius * 0.5, 0.2, radius * 0.5, 0.0);
        level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.coming.burst"), this.getSoundSource(), 1.4F, 0.9F + this.random.nextFloat() * 0.2F);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        float through = this.open() ? HIDE * 2.0F : HIDE;
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
    protected void pushEntities() {
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.spawnAtLocation(level, new ItemStack(Dushi.COMING_SOUL.get()));
        this.spawnAtLocation(level, new ItemStack(Dushi.FLASK_SHARD.get()));
        this.leave(level, Dushi.GREAT_SOUL.get(), 3, 4);
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
        return Dushi.sound("entity.coming.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.coming.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.coming.death");
    }

    @Override
    protected float getSoundVolume() {
        return 3.0F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
    }
}
