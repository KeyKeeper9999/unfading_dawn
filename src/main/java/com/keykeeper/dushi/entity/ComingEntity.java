package com.keykeeper.dushi.entity;

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
 * The Coming: what was to arrive on the day that was not let begin. It is in the Tear - a crack in the wall of the last
 * hollow of the Sky Beneath, with some other sky behind it - and it is coming out of it: a breast it crawls on, arms it
 * drags itself by, a neck like a tower, and where a head would be a black disc with a rim of light and nothing in the
 * middle of it. The Founder's rite nailed a mask of gold over the disc. The second lord of the place, and the last.
 * <p>
 * It does not leave the crack and does not turn in it: it stands where its mark is, its back to the rock, and looks
 * at whoever it fights with its neck ({@link #aim}). All of its hall is in its reach.
 * <p>
 * Masked (phase 1) it has got no further than its neck, and looks out and down. Rays come out of the slits of the
 * mask to the floor, in a line that runs out from the crack through whoever it is looking at; it brings its head down
 * full length before the crack, and lies so a moment (the time to strike it); it sweeps an arm round, low; whoever
 * hides at the foot of the crack, it brings both arms down on; and it puts its hands into the floor, and what it
 * reaches with comes up out of holes elsewhere - under whoever stands off from it - stands, and comes down along the
 * floor ({@link TendrilEntity}).
 * <p>
 * Hurt enough, the mask goes, and it comes out to the breast ({@link #EMERGE}): a second pair of arms, a crown of them
 * round the disc, and an eye opens in the crack behind it, as tall as the crack (phase 2). Everything it did it does
 * from further out and oftener; the rays are three lines; an arm comes up out of the floor now and again whatever
 * else it is doing; it draws everything in the hall towards the mouth in its neck, and bites; the sky of the place
 * comes down wherever anybody stands; and the eye looks - a fan of rays from the crack that goes across the whole
 * hall from one side to the other, to be gone through and not run from.
 */
public class ComingEntity extends SoulsMob {
    /** How far round from straight out of the crack its neck is turned, in radians: synced, for the model. */
    private static final EntityDataAccessor<Float> AIM = SynchedEntityData.defineId(ComingEntity.class, EntityDataSerializers.FLOAT);

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
    public static final int REACH = 5;
    public static final int REACH_DURATION = 96;
    public static final int REACH_CALL = 24;
    public static final int EMERGE = 6;
    public static final int EMERGE_DURATION = 124;
    public static final int EMERGE_BREAK = 52;
    public static final int PULL = 7;
    public static final int PULL_DURATION = 124;
    public static final int PULL_FROM = 22;
    public static final int PULL_UNTIL = 82;
    public static final int PULL_BITE = 92;
    public static final int RAIN = 8;
    public static final int RAIN_DURATION = 104;
    public static final int RAIN_FROM = 22;
    public static final int RAIN_UNTIL = 74;
    public static final int GLARE = 9;
    public static final int GLARE_DURATION = 152;
    public static final int GLARE_FROM = 30;
    public static final int GLARE_STEP = 8;
    public static final int GLARE_RAYS = 11;
    public static final int GLARE_UNTIL = GLARE_FROM + GLARE_STEP * GLARE_RAYS;
    /** How far before the crack the head comes down, masked and bare (where the model's head is in those poses), and how wide. */
    public static final double SLAM_FAR = 4.0;
    public static final double SLAM_FAR_BARE = 7.2;
    public static final double SLAM_RADIUS = 3.1;
    public static final double SWEEP_REACH = 6.5;
    public static final double SWEEP_REACH_BARE = 8.5;
    public static final double CRUSH_REACH = 4.4;
    public static final double PULL_RANGE = 38.0;
    public static final double STAR_RADIUS = 1.9;
    public static final double GLARE_RADIUS = 2.0;
    public static final int STAR_DELAY = 26;
    /** The middle of its hall is so far straight out of the crack, and the hall so far across from its middle. */
    public static final double CENTRE = 18.0;
    public static final double HALL = 17.0;
    /** The share of a blow that gets through to it (nothing has more than 1024 health; it needs more). */
    public static final float HIDE = 0.3F;
    private static final float AIM_MAX = 1.3F;

    private float homeYaw = Float.NaN;
    private int gazeCooldown = 70;
    private int sweepCooldown = 110;
    private int reachCooldown = 120;
    private int pullCooldown = 160;
    private int rainCooldown = 90;
    private int glareCooldown = 200;
    private int armClock = 140;
    private boolean glareTurn;

    public ComingEntity(EntityType<? extends ComingEntity> type, Level level) {
        super(type, level);
        this.xpReward = 500;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1000.0).add(Attributes.ATTACK_DAMAGE, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.ARMOR, 6.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.FOLLOW_RANGE, 56.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(AIM, 0.0F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
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

    /** The mask is off, and it is out to the breast. */
    public boolean bare() {
        return this.phase() >= 2;
    }

    /** Its head lying where it came down: when a blow tells for double. */
    public boolean open() {
        return this.attackId() == SLAM && this.attackTime > SLAM_IMPACT + 2 && this.attackTime < SLAM_LAIN;
    }

    /** How far round its neck is turned from straight out of the crack, in radians. */
    public float aim() {
        return this.entityData.get(AIM);
    }

    @Override
    public float renderExtra() {
        return this.aim();
    }

    private float home() {
        return Float.isNaN(this.homeYaw) ? this.getYRot() : this.homeYaw;
    }

    /** Straight out of the crack. */
    public Vec3 outward() {
        return Vec3.directionFromRotation(0.0F, this.home());
    }

    /** The way it looks: out of the crack, turned by its neck. */
    @Override
    public Vec3 facing() {
        return Vec3.directionFromRotation(0.0F, this.home() + (float) Math.toDegrees(this.aim()));
    }

    /** Where the hole in its head is, about (the rays come from it): higher and further back while it looks, lower out of its mask. */
    public Vec3 head() {
        Vec3 way = this.facing();
        double far = this.bare() ? 1.7 : 2.4;
        return this.position().add(way.x * far, this.bare() ? 10.4 : 8.1, way.z * far);
    }

    /** Where the eye in the crack behind it is. */
    public Vec3 eye() {
        Vec3 out = this.outward();
        return this.position().add(-out.x * 1.3, 7.8, -out.z * 1.3);
    }

    /** Whether a place is on the floor of its hall. */
    private boolean inHall(double x, double z) {
        Vec3 out = this.outward();
        double dx = x - (this.getX() + out.x * CENTRE);
        double dz = z - (this.getZ() + out.z * CENTRE);
        return dx * dx + dz * dz <= (HALL + 0.5) * (HALL + 0.5);
    }

    @Override
    protected int attackDuration(int id) {
        return switch (id) {
            case GAZE -> GAZE_DURATION;
            case SLAM -> SLAM_DURATION;
            case SWEEP -> SWEEP_DURATION;
            case CRUSH -> CRUSH_DURATION;
            case REACH -> REACH_DURATION;
            case EMERGE -> EMERGE_DURATION;
            case PULL -> PULL_DURATION;
            case RAIN -> RAIN_DURATION;
            default -> GLARE_DURATION;
        };
    }

    @Override
    protected int trackUntil(int id) {
        return switch (id) {
            case GAZE -> GAZE_LOOK - 4;
            case SLAM -> SLAM_IMPACT - 14;
            case SWEEP -> SWEEP_IMPACT - 14;
            case PULL -> PULL_UNTIL;
            case REACH, RAIN -> 200;
            default -> 0;
        };
    }

    @Override
    protected int attackPause(int finishedId) {
        if (finishedId == EMERGE) {
            return 10;
        }
        return this.bare() ? 10 + this.random.nextInt(11) : 18 + this.random.nextInt(15);
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
        this.reachCooldown = 120;
        this.pullCooldown = 160;
        this.rainCooldown = 90;
        this.glareCooldown = 200;
        this.armClock = 140;
        this.entityData.set(AIM, 0.0F);
        BlockPos home = MobMarkBlockEntity.homeOf(this);
        if (home != null) {
            this.teleportTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);
        }
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        boolean bare = this.bare();
        if (bare) {
            if (this.pullCooldown <= 0 && distance > 7.0) {
                this.pullCooldown = 300 + this.random.nextInt(140);
                return PULL;
            }
            if (this.glareCooldown <= 0) {
                this.glareCooldown = 260 + this.random.nextInt(120);
                return GLARE;
            }
            if (this.rainCooldown <= 0) {
                this.rainCooldown = 220 + this.random.nextInt(100);
                return RAIN;
            }
        }
        if (distance < CRUSH_REACH - 1.4) {
            return CRUSH;
        }
        if (this.reachCooldown <= 0 && distance > (bare ? 9.0 : 6.5)) {
            this.reachCooldown = (bare ? 120 : 170) + this.random.nextInt(70);
            return REACH;
        }
        if (this.sweepCooldown <= 0 && distance < (bare ? SWEEP_REACH_BARE : SWEEP_REACH) - 0.8) {
            this.sweepCooldown = (bare ? 110 : 150) + this.random.nextInt(70);
            return SWEEP;
        }
        double far = (bare ? SLAM_FAR_BARE : SLAM_FAR) - this.getBbWidth() * 0.5;
        boolean underHead = distance > far - SLAM_RADIUS + 0.4 && distance < far + SLAM_RADIUS - 0.6;
        if (underHead && this.random.nextInt(3) != 0) {
            return SLAM;
        }
        if (this.gazeCooldown <= 0 && distance > 4.0) {
            this.gazeCooldown = (bare ? 80 : 110) + this.random.nextInt(55);
            return GAZE;
        }
        if (this.reachCooldown <= 0) {
            this.reachCooldown = (bare ? 120 : 170) + this.random.nextInt(70);
            return REACH;
        }
        return underHead ? SLAM : 0;
    }

    @Override
    public void startAttack(int id) {
        super.startAttack(id);
        if (id == GLARE) {
            this.glareTurn = this.random.nextBoolean();
        }
    }

    private static Vec3 ground(Level level, double x, double y, double z) {
        Vec3 found = RootEntity.ground(level, x, y, z);
        return found != null ? found : new Vec3(x, y, z);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (Float.isNaN(this.homeYaw)) {
            this.homeYaw = this.getYRot();
        }
        if (this.phase() == 1 && this.getHealth() < this.getMaxHealth() * 0.5F && this.attackId() == 0 && this.isAlive()) {
            this.startAttack(EMERGE);
        }
        super.customServerAiStep(level);
        this.gazeCooldown = Math.max(0, this.gazeCooldown - 1);
        this.sweepCooldown = Math.max(0, this.sweepCooldown - 1);
        this.reachCooldown = Math.max(0, this.reachCooldown - 1);
        this.pullCooldown = Math.max(0, this.pullCooldown - 1);
        this.rainCooldown = Math.max(0, this.rainCooldown - 1);
        this.glareCooldown = Math.max(0, this.glareCooldown - 1);
        // it is in the crack: it neither goes anywhere nor turns, and looks with its neck
        Vec3 motion = this.getDeltaMovement();
        if (motion.x != 0.0 || motion.z != 0.0) {
            this.setDeltaMovement(0.0, motion.y, 0.0);
        }
        LivingEntity target = this.getTarget();
        int id = this.attackId();
        float aim = this.aim();
        float want = aim;
        if (target == null) {
            want = 0.0F;
        } else if (id == 0 || this.attackTime <= this.trackUntil(id)) {
            float yaw = (float) (Mth.atan2(target.getZ() - this.getZ(), target.getX() - this.getX()) * (180.0 / Math.PI)) - 90.0F;
            want = Mth.clamp((float) Math.toRadians(Mth.wrapDegrees(yaw - this.homeYaw)), -AIM_MAX, AIM_MAX);
        }
        float step = this.bare() ? 0.085F : 0.06F;
        float next = aim + Mth.clamp(want - aim, -step, step);
        if (Math.abs(next - aim) > 0.004F) {
            this.entityData.set(AIM, next);
        }
        this.setYRot(this.homeYaw);
        this.setYBodyRot(this.homeYaw);
        this.setYHeadRot(this.homeYaw);
        // out of its mask, an arm comes up out of the floor under whoever it fights now and again, whatever else it is doing
        if (this.bare() && target != null && id != EMERGE && --this.armClock <= 0) {
            this.armClock = 150 + this.random.nextInt(80);
            Vec3 lead = target.getKnownMovement().multiply(1.0, 0.0, 1.0).scale(14.0);
            this.arm(level, target.getX() + lead.x, target.getZ() + lead.z, 26);
        }
    }

    /** Light standing on the ground where it will strike, {@code delay} ticks on: down from the sky, or a ray from {@code from}. */
    private void star(ServerLevel level, Vec3 at, int delay, double radius, float damage, Vec3 from) {
        this.strikeLater(at, delay, radius, damage, true);
        if (from == null) {
            SunShaftEntity.dusk(level, at, (float) radius * 0.9F, delay, delay, delay + 9);
        } else {
            SunShaftEntity.ray(level, at, from, (float) radius * 0.9F, delay, delay, delay + 9, SunShaftEntity.DUSK);
        }
    }

    /** One of its arms out of the floor at a place of the hall, after {@code warning} ticks; nothing, if the place is not floor. */
    private boolean arm(ServerLevel level, double x, double z, int warning) {
        if (!this.inHall(x, z)) {
            return false;
        }
        Vec3 at = RootEntity.ground(level, x, this.getY(), z);
        if (at == null) {
            return false;
        }
        TendrilEntity.rise(level, this, at, warning, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.9F);
        return true;
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        boolean bare = this.bare();
        switch (id) {
            case GAZE -> {
                if (time == 4) {
                    this.playSound(Dushi.sound("entity.coming.gaze"), 4.0F, 1.0F);
                }
                if (time == GAZE_LOOK) {
                    // where it looks: rays from the hole in its head to the floor, in a line out from the crack past whoever it
                    // is looking at; three lines with the mask off
                    Vec3 forward = this.facing();
                    Vec3 head = this.head();
                    double[] turns = bare ? new double[] {0.0, 0.4, -0.4} : new double[] {0.0};
                    for (double turn : turns) {
                        Vec3 line = forward.yRot((float) turn);
                        for (int i = 0; i < 12; i++) {
                            double far = 5.0 + i * 2.7;
                            double x = this.getX() + line.x * far;
                            double z = this.getZ() + line.z * far;
                            if (this.inHall(x, z)) {
                                this.star(level, ground(level, x, this.getY(), z), 20 + i * 3, STAR_RADIUS, damage * 0.8F, head);
                            }
                        }
                    }
                }
            }
            case SLAM -> {
                if (time == 6) {
                    this.playSound(Dushi.sound("entity.coming.rear"), 4.0F, 1.0F);
                }
                if (time == SLAM_IMPACT) {
                    double far = bare ? SLAM_FAR_BARE : SLAM_FAR;
                    Vec3 at = this.position().add(this.facing().scale(far));
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
                    // and along the neck, from the crack to the head
                    for (double on = 1.5; on < far - SLAM_RADIUS + 0.5; on += 1.5) {
                        this.hitAround(level, this.position().add(this.facing().scale(on)), 1.5, damage, 0.9, this.damageSources().mobAttack(this));
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
                    double reach = bare ? SWEEP_REACH_BARE : SWEEP_REACH;
                    this.hitArc(level, this.facing(), reach, 105.0, damage, 1.7, false);
                    for (int i = -3; i <= 3; i++) {
                        Vec3 at = this.position().add(this.facing().yRot(i * 0.5F).scale(reach - 1.5));
                        level.sendParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y + 0.9, at.z, 1, 0.3, 0.0, 0.3, 0.0);
                    }
                }
            }
            case CRUSH -> {
                if (time == CRUSH_IMPACT - 10) {
                    this.playSound(Dushi.sound("entity.coming.sweep"), 3.0F, 0.8F);
                }
                if (time == CRUSH_IMPACT) {
                    Vec3 at = this.position().add(this.outward().scale(1.5));
                    this.hitAround(level, at, CRUSH_REACH, damage * 0.9F, 2.0, this.damageSources().mobAttack(this));
                    level.sendParticles(ParticleTypes.SPLASH, at.x, at.y + 0.2, at.z, 100, 3.0, 0.2, 3.0, 0.3);
                    this.playSound(Dushi.sound("entity.coming.slam"), 3.0F, 1.2F);
                }
            }
            case REACH -> {
                if (time == 4) {
                    this.playSound(Dushi.sound("entity.coming.strain"), 3.0F, 1.3F);
                }
                if (time == REACH_CALL) {
                    // under whoever it fights, where they are going, and round them
                    LivingEntity target = this.getTarget();
                    Vec3 aim = target != null ? target.position() : this.position().add(this.outward().scale(CENTRE));
                    Vec3 lead = target != null ? target.getKnownMovement().multiply(1.0, 0.0, 1.0).scale(14.0) : Vec3.ZERO;
                    this.arm(level, aim.x + lead.x, aim.z + lead.z, 22);
                    int more = bare ? 4 : 2;
                    double turn = this.random.nextDouble() * Math.PI * 2.0;
                    for (int i = 0; i < more; i++) {
                        double a = turn + i * Math.PI * 2.0 / more + (this.random.nextDouble() - 0.5) * 0.8;
                        double far = 3.5 + this.random.nextDouble() * 3.5;
                        this.arm(level, aim.x + Math.cos(a) * far, aim.z + Math.sin(a) * far, 29 + i * 7);
                    }
                }
            }
            case EMERGE -> {
                Vec3 head = this.head();
                if (time == 6) {
                    this.playSound(Dushi.sound("entity.coming.strain"), 4.0F, 1.0F);
                }
                if (time > 10 && time < EMERGE_BREAK && time % 3 == 0) {
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GOLD_BLOCK.defaultBlockState()), head.x, head.y, head.z, 4, 0.8, 0.6, 0.8, 0.05);
                }
                if (time == EMERGE_BREAK) {
                    this.setPhase(2);
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GOLD_BLOCK.defaultBlockState()), head.x, head.y, head.z, 160, 1.2, 1.2, 1.2, 0.3);
                    level.sendParticles(net.minecraft.core.particles.ColorParticleOption.create(ParticleTypes.FLASH, 0xFFD8C8FF), head.x, head.y, head.z, 1, 0.0, 0.0, 0.0, 0.0);
                    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(12.0, 2.0, 12.0), this::isFoe)) {
                        Vec3 away = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ());
                        double far = away.length();
                        if (far < 12.5) {
                            away = far < 1.0E-3 ? this.outward() : away.scale(1.0 / far);
                            victim.hurtServer(level, this.damageSources().mobAttack(this), damage * 0.5F);
                            shove(victim, new Vec3(away.x * 1.1, 0.4, away.z * 1.1));
                        }
                    }
                    this.playSound(Dushi.sound("entity.coming.unmask"), 5.0F, 1.0F);
                }
                if (time == EMERGE_BREAK + 30) {
                    // the eye opens
                    Vec3 eye = this.eye();
                    level.sendParticles(ParticleTypes.END_ROD, eye.x, eye.y, eye.z, 60, 1.2, 2.6, 1.2, 0.05);
                    this.playSound(Dushi.sound("entity.coming.gaze"), 5.0F, 0.6F);
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
                        if (far > PULL_RANGE || far < 2.6) {
                            continue;
                        }
                        Vec3 pull = to.scale(0.06 / far);
                        victim.setDeltaMovement(victim.getDeltaMovement().add(pull.x, 0.0, pull.z));
                        victim.needsSync = true;
                        if (victim instanceof ServerPlayer player && time % 2 == 0) {
                            player.connection.send(new ClientboundSetEntityMotionPacket(player));
                        }
                    }
                    if (time % 2 == 0) {
                        Vec3 out = this.outward();
                        double a = this.random.nextDouble() * Math.PI * 2.0;
                        double far = this.random.nextDouble() * HALL;
                        level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX() + out.x * CENTRE + Math.cos(a) * far, this.getY() + 0.6 + this.random.nextDouble() * 2.0,
                                this.getZ() + out.z * CENTRE + Math.sin(a) * far, 4, 0.3, 0.3, 0.3, 0.02);
                    }
                }
                if (time == PULL_BITE - 6) {
                    this.playSound(Dushi.sound("entity.coming.rear"), 4.0F, 1.3F);
                }
                if (time == PULL_BITE) {
                    // the mouth comes down before the crack: from its foot out to where the head ends
                    this.hitAround(level, this.position().add(this.facing().scale(2.6)), 3.4, damage * 1.7F, 1.6, this.damageSources().mobAttack(this));
                    this.hitAround(level, this.position().add(this.facing().scale(6.2)), 3.2, damage * 1.7F, 1.6, this.damageSources().mobAttack(this));
                    this.playSound(Dushi.sound("entity.coming.bite"), 4.0F, 1.0F);
                }
            }
            case RAIN -> {
                if (time == 4) {
                    this.playSound(Dushi.sound("entity.coming.gaze"), 4.0F, 0.7F);
                }
                if (time >= RAIN_FROM && time <= RAIN_UNTIL && time % 3 == 2) {
                    // the sky of the place comes down: where they stand, where they are going, and anywhere in the hall
                    LivingEntity target = this.getTarget();
                    Vec3 out = this.outward();
                    Vec3 at;
                    int kind = (time / 3) % 3;
                    if (target != null && kind == 0) {
                        at = target.position();
                    } else if (target != null && kind == 1) {
                        at = target.position().add(target.getKnownMovement().multiply(1.0, 0.0, 1.0).scale(16.0));
                    } else {
                        double a = this.random.nextDouble() * Math.PI * 2.0;
                        double far = Math.sqrt(this.random.nextDouble()) * HALL;
                        at = this.position().add(out.x * CENTRE + Math.cos(a) * far, 0.0, out.z * CENTRE + Math.sin(a) * far);
                    }
                    if (this.inHall(at.x, at.z)) {
                        this.star(level, ground(level, at.x, this.getY(), at.z), STAR_DELAY, STAR_RADIUS, damage * 0.75F, null);
                    }
                }
            }
            default -> {
                if (time == 6) {
                    this.playSound(Dushi.sound("entity.coming.gaze"), 5.0F, 0.55F);
                }
                int step = time - GLARE_FROM;
                if (step >= 0 && step < GLARE_STEP * GLARE_RAYS && step % GLARE_STEP == 0) {
                    // the eye looks: one ray of a fan that goes across the hall from one side of the crack to the other
                    int ray = step / GLARE_STEP;
                    double turn = (-1.05 + ray * 2.1 / (GLARE_RAYS - 1)) * (this.glareTurn ? 1.0 : -1.0);
                    Vec3 line = this.outward().yRot((float) turn);
                    Vec3 eye = this.eye();
                    for (double far = 4.5; far < 38.0; far += 3.4) {
                        double x = this.getX() + line.x * far;
                        double z = this.getZ() + line.z * far;
                        if (this.inHall(x, z)) {
                            this.star(level, ground(level, x, this.getY(), z), 18, GLARE_RADIUS, damage * 0.85F, eye);
                        }
                    }
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
        if (left == 17) {
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

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (!Float.isNaN(this.homeYaw)) {
            output.putFloat("HomeYaw", this.homeYaw);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        float yaw = input.getFloatOr("HomeYaw", Float.NaN);
        if (!Float.isNaN(yaw)) {
            this.homeYaw = yaw;
        }
    }
}
