package com.keykeeper.dushi.entity;

import java.util.ArrayList;
import java.util.List;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.block.FogGateBlock;
import com.keykeeper.dushi.block.MobMarkBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The Coming: what was to arrive on the day that was not let begin. It is in the Tear - a crack in the wall of the last
 * hollow of the Sky Beneath, with some other sky behind it. It is a weaving of several things: three necks twisted
 * into a rope, and on the end of the rope a black disc with a rim of light and nothing in the middle of it; the rope
 * grows out of a knot of the same necks that has a mouth in it and six arms a house long. The Founder's rite nailed a
 * mask of gold over the disc. The second lord of the place, and the last.
 * <p>
 * Masked (phase 1) it looks out of the crack: the knot is in the dark behind the face of the rock, the rope hangs out
 * over the hall with the mask on the end of it, two arms are out to the floor. It does not leave the crack and does
 * not turn in it: it turns what is out of it to whoever it fights ({@link #aim}), about the foot of the crack
 * ({@link #rift}). Rays come out of the slits of the mask to the floor, in a line through whoever it looks at; it
 * throws the rope down along the floor, and the mask lies at the end of it a moment (the time to strike it); it sweeps
 * an arm round, low; whoever hides at the foot of the crack, it brings both arms down on; and it puts its hands into
 * the floor, and what it reaches with comes up out of holes elsewhere ({@link TendrilEntity}).
 * <p>
 * Hurt enough, the mask goes, and all of it comes out ({@link #EMERGE}): the crown opens round the disc, an eye opens
 * in the crack it left, as tall as the crack, and lesser eyes open in the rock all round the hall
 * ({@link WallEyeEntity}). From then (phase 2) it walks: it goes about its hall on its six arms after whoever it
 * fights ({@link #roam}), and now and again goes from one side of the hall to the other over anybody in its way
 * ({@link #CROSS}). Everything it did it does from where it stands; the rays are three lines; whoever gets under it,
 * it lets the knot down on ({@link #STAMP}); an arm comes up out of the floor now and again whatever else it is doing;
 * it draws everything in the hall towards the mouth in the knot, and bites; the sky of the place comes down wherever
 * anybody stands; and the eyes look - either the great one, a fan of rays from the crack that goes across the whole
 * hall, or the lesser ones, one after another, each at where whoever it fights is going.
 * <p>
 * Its disc is far from where it stands, and can be struck where it is ({@link FaceEntity}); a blow there tells for more.
 */
public class ComingEntity extends SoulsMob {
    /** How far round from straight out of the crack what is out of it is turned, in radians: synced, for the model. */
    private static final EntityDataAccessor<Float> AIM = SynchedEntityData.defineId(ComingEntity.class, EntityDataSerializers.FLOAT);

    public static final int GAZE = 1;
    public static final int GAZE_DURATION = 92;
    public static final int GAZE_LOOK = 26;
    public static final int SLAM = 2;
    public static final int SLAM_DURATION = 112;
    public static final int SLAM_IMPACT = 40;
    /** Until this tick the disc lies where it fell. */
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
    public static final int EMERGE_DURATION = 156;
    public static final int EMERGE_BREAK = 52;
    /** When the eyes open: it is out of the crack by then. */
    public static final int EMERGE_EYES = EMERGE_BREAK + 66;
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
    public static final int STAMP = 10;
    public static final int STAMP_DURATION = 78;
    public static final int STAMP_IMPACT = 24;
    /** Until this tick the knot lies where it was let down. */
    public static final int STAMP_LAIN = 56;
    public static final int CROSS = 11;
    public static final int CROSS_DURATION = 130;
    public static final int CROSS_GO = 16;

    // <generated places>
    private static final double[] DISC_REST = {7.77, 9.12};
    private static final double[] DISC_GAZE = {8.27, 7.1};
    private static final double[] DISC_SLAM = {9.31, 3.38};
    private static final double[] DISC_BARE = {3.45, 15.3};
    private static final double[] DISC_BARE_GAZE = {8.51, 11.41};
    private static final double[] DISC_BARE_SLAM = {10.36, 3.36};
    private static final double[] DISC_BOWED = {14.68, 5.03};
    private static final double[] DISC_RAIN = {1.32, 21.93};
    private static final double[] DISC_STAMP = {7.07, 9.23};
    // </generated places>

    /** The face of the rock is so far behind where it stands in the crack. */
    public static final double FACE = 1.5;
    public static final double SLAM_RADIUS = 3.5;
    /** How far an arm brought round reaches: from the foot of the crack, and from the middle of it when it is out. */
    public static final double SWEEP_REACH = 8.6;
    public static final double SWEEP_REACH_BARE = 10.5;
    /** Where both arms come down, before where it stands, and how wide. */
    public static final double CRUSH_FAR = 4.2;
    public static final double CRUSH_FAR_BARE = 8.0;
    public static final double CRUSH_REACH = 4.3;
    public static final double STAMP_RADIUS = 5.2;
    public static final double PULL_RANGE = 38.0;
    public static final double STAR_RADIUS = 1.9;
    public static final double GLARE_RADIUS = 2.0;
    public static final int STAR_DELAY = 26;
    /** The middle of its hall is so far straight out of the crack, and the hall so far across from its middle. */
    public static final double CENTRE = 18.0;
    public static final double HALL = 17.0;
    /** Out of the crack it keeps this far inside the rim of its hall; how fast it walks and crosses, in blocks a tick; how near it comes; how fast it turns. */
    private static final double ROAM = HALL - 5.5;
    private static final double WALK = 0.15;
    private static final double RUN = 0.36;
    private static final double NEAR = 8.5;
    private static final float TURN = 4.5F;
    /** The share of a blow that gets through to it (nothing has more than 1024 health; it needs more). */
    public static final float HIDE = 0.3F;
    private static final float AIM_MAX = 1.0F;
    /** Its box when it is out of the crack: the knot on its arms. */
    public static final EntityDimensions OUT = EntityDimensions.scalable(5.0F, 7.0F).withEyeHeight(5.0F);
    /** The lesser eyes: so many spokes from the middle of the hall an eye is looked for along, and so many heights tried on each. */
    private static final int EYES = 26;
    private static final int EYE_TRIES = 4;

    private float homeYaw = Float.NaN;
    private Vec3 home;
    /** The way it faces when it is out of the crack: it turns slowly. */
    private float yaw;
    private int gazeCooldown = 70;
    private int sweepCooldown = 110;
    private int reachCooldown = 120;
    private int pullCooldown = 160;
    private int rainCooldown = 90;
    private int glareCooldown = 200;
    private int stampCooldown = 40;
    private int crossCooldown = 220;
    private int armClock = 140;
    private int stepClock;
    private boolean glareTurn;
    private boolean glareWalls;
    private Vec3 crossTo;
    private boolean faceStruck;
    private Boolean wasOut;
    private FaceEntity face;
    private WallEyeEntity greatEye;
    private final List<WallEyeEntity> eyes = new ArrayList<>();

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

    /** The mask is off, and it is out of the crack (or coming out). */
    public boolean bare() {
        return this.phase() >= 2;
    }

    /** Out, and done coming out: it goes where it will. */
    private boolean loose() {
        return this.bare() && this.attackId() != EMERGE;
    }

    /** The disc lying where it came down, or the knot where it was let down: when a blow tells for double. */
    public boolean open() {
        int id = this.attackId();
        return id == SLAM && this.attackTime > SLAM_IMPACT + 2 && this.attackTime < SLAM_LAIN || id == STAMP && this.attackTime > STAMP_IMPACT + 2 && this.attackTime < STAMP_LAIN;
    }

    /** How far round what is out of the crack is turned from straight out of it, in radians. */
    public float aim() {
        return this.entityData.get(AIM);
    }

    @Override
    public float renderExtra() {
        return this.aim();
    }

    private float homeYaw() {
        return Float.isNaN(this.homeYaw) ? this.getYRot() : this.homeYaw;
    }

    /** Where it stands in the crack. */
    private Vec3 home() {
        return this.home != null ? this.home : this.position();
    }

    /** Straight out of the crack. */
    public Vec3 outward() {
        return Vec3.directionFromRotation(0.0F, this.homeYaw());
    }

    /** The foot of the crack: the middle of it, in the face of the rock. What is out of the crack turns about this. */
    public Vec3 rift() {
        Vec3 out = this.outward();
        Vec3 home = this.home();
        return new Vec3(home.x - out.x * FACE, home.y, home.z - out.z * FACE);
    }

    /** The middle of its hall. */
    public Vec3 centre() {
        Vec3 out = this.outward();
        Vec3 home = this.home();
        return new Vec3(home.x + out.x * CENTRE, home.y, home.z + out.z * CENTRE);
    }

    /** The way it looks: out of the crack, turned to whoever it fights; or, out of it, the way it stands. */
    @Override
    public Vec3 facing() {
        return this.loose() ? Vec3.directionFromRotation(0.0F, this.getYRot()) : Vec3.directionFromRotation(0.0F, this.homeYaw() + (float) Math.toDegrees(this.aim()));
    }

    /** Where its blows are counted from: the foot of the crack, or where it stands when it is out. */
    private Vec3 origin() {
        return this.loose() ? this.position() : this.rift();
    }

    /** The middle of the disc in a pose of the model's: {@code place} is how far out before where it stands the disc is, and how high. */
    private Vec3 discAt(double out, double up) {
        Vec3 way = this.facing();
        if (this.loose()) {
            return new Vec3(this.getX() + way.x * out, this.getY() + up, this.getZ() + way.z * out);
        }
        Vec3 rift = this.rift();
        return new Vec3(rift.x + way.x * (out + FACE), rift.y + up, rift.z + way.z * (out + FACE));
    }

    private Vec3 discAt(double[] place) {
        return this.discAt(place[0], place[1]);
    }

    /** Where the hole in the middle of its disc is while it looks (the rays come from it): the model's `gaze` poses. */
    public Vec3 head() {
        return this.discAt(this.bare() ? DISC_BARE_GAZE : DISC_GAZE);
    }

    private static float ramp(float t, float from, float to) {
        float x = Mth.clamp((t - from) / (to - from), 0.0F, 1.0F);
        return x * x * (3.0F - 2.0F * x);
    }

    private static float pulse(float t, float up0, float up1, float down0, float down1) {
        return ramp(t, up0, up1) * (1.0F - ramp(t, down0, down1));
    }

    /** Where the middle of the disc is just now, about: the model's poses, gone through as the model goes through them. */
    public Vec3 discNow() {
        boolean bare = this.bare();
        double[] rest = this.loose() ? DISC_BARE : DISC_REST;
        double[] to = rest;
        float t = this.attackTime;
        float share = 0.0F;
        switch (this.attackId()) {
            case GAZE -> {
                to = bare ? DISC_BARE_GAZE : DISC_GAZE;
                share = pulse(t, 2.0F, GAZE_LOOK - 4.0F, GAZE_DURATION - 24.0F, GAZE_DURATION);
            }
            case SLAM -> {
                to = bare ? DISC_BARE_SLAM : DISC_SLAM;
                share = pulse(t, SLAM_IMPACT - 8.0F, SLAM_IMPACT, SLAM_LAIN, SLAM_DURATION - 4.0F);
            }
            case GLARE -> {
                to = DISC_BOWED;
                share = pulse(t, 4.0F, GLARE_FROM, GLARE_UNTIL, GLARE_DURATION);
            }
            case RAIN -> {
                to = DISC_RAIN;
                share = pulse(t, 4.0F, RAIN_FROM, RAIN_UNTIL, RAIN_DURATION);
            }
            case STAMP -> {
                to = DISC_STAMP;
                share = pulse(t, STAMP_IMPACT - 5.0F, STAMP_IMPACT, STAMP_LAIN, STAMP_DURATION - 2.0F);
            }
            default -> {
            }
        }
        return this.discAt(rest[0] + (to[0] - rest[0]) * share, rest[1] + (to[1] - rest[1]) * share);
    }

    /** Where the eye in the crack is. */
    public Vec3 eye() {
        Vec3 out = this.outward();
        Vec3 rift = this.rift();
        return new Vec3(rift.x + out.x * 0.08, rift.y + 6.7, rift.z + out.z * 0.08);
    }

    /** Whether a place is on the floor of its hall. */
    private boolean inHall(double x, double z) {
        Vec3 centre = this.centre();
        double dx = x - centre.x;
        double dz = z - centre.z;
        return dx * dx + dz * dz <= (HALL + 0.5) * (HALL + 0.5);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.loose() ? OUT : super.getDefaultDimensions(pose);
    }

    /** Its box is the knot's: at the foot of the crack while it is in it, and wide when it is out (known on both sides). */
    @Override
    public void tick() {
        super.tick();
        boolean out = this.loose();
        if (this.wasOut == null || this.wasOut != out) {
            this.wasOut = out;
            this.refreshDimensions();
        }
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
            case STAMP -> STAMP_DURATION;
            case CROSS -> CROSS_DURATION;
            default -> GLARE_DURATION;
        };
    }

    /** (It turns itself: {@link #turnUntil}.) */
    @Override
    protected int trackUntil(int id) {
        return -1;
    }

    /** Until this tick of a move it goes on turning to whoever it fights. */
    private int turnUntil(int id) {
        return switch (id) {
            case GAZE -> GAZE_LOOK - 4;
            case SLAM -> SLAM_IMPACT - 14;
            case SWEEP -> SWEEP_IMPACT - 14;
            case CRUSH -> CRUSH_IMPACT - 10;
            case PULL -> PULL_UNTIL;
            case REACH, RAIN -> 200;
            default -> 0;
        };
    }

    @Override
    protected int attackPause(int finishedId) {
        if (finishedId == EMERGE || finishedId == CROSS) {
            return 8;
        }
        return this.bare() ? 12 + this.random.nextInt(13) : 18 + this.random.nextInt(15);
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
        this.stampCooldown = 40;
        this.crossCooldown = 220;
        this.armClock = 140;
        this.crossTo = null;
        this.entityData.set(AIM, 0.0F);
        this.shutEyes();
        BlockPos mark = MobMarkBlockEntity.homeOf(this);
        Vec3 to = mark != null ? new Vec3(mark.getX() + 0.5, mark.getY(), mark.getZ() + 0.5) : this.home;
        if (to != null) {
            this.teleportTo(to.x, to.y, to.z);
        }
        if (!Float.isNaN(this.homeYaw)) {
            this.yaw = this.homeYaw;
            this.setYRot(this.homeYaw);
            this.setYBodyRot(this.homeYaw);
            this.setYHeadRot(this.homeYaw);
        }
    }

    private static float yawOf(double dx, double dz) {
        return (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        Vec3 from = this.origin();
        double far = Math.hypot(target.getX() - from.x, target.getZ() - from.z);
        if (!this.bare()) {
            if (far < FACE + CRUSH_FAR + 0.8) {
                return CRUSH;
            }
            if (this.reachCooldown <= 0 && far > 9.0) {
                this.reachCooldown = 170 + this.random.nextInt(70);
                return REACH;
            }
            if (this.sweepCooldown <= 0 && far < SWEEP_REACH - 0.8) {
                this.sweepCooldown = 150 + this.random.nextInt(70);
                return SWEEP;
            }
            boolean underDisc = Math.abs(far - (DISC_SLAM[0] + FACE)) < SLAM_RADIUS - 0.6;
            if (underDisc && this.random.nextInt(3) != 0) {
                return SLAM;
            }
            if (this.gazeCooldown <= 0 && far > 6.0) {
                this.gazeCooldown = 110 + this.random.nextInt(55);
                return GAZE;
            }
            if (this.reachCooldown <= 0) {
                this.reachCooldown = 170 + this.random.nextInt(70);
                return REACH;
            }
            return underDisc ? SLAM : 0;
        }
        if (far < STAMP_RADIUS - 1.0 && this.stampCooldown <= 0) {
            this.stampCooldown = 80 + this.random.nextInt(50);
            return STAMP;
        }
        if (this.crossCooldown <= 0 && (far < 7.0 || this.random.nextInt(3) == 0)) {
            this.crossCooldown = 300 + this.random.nextInt(160);
            return CROSS;
        }
        if (this.pullCooldown <= 0 && far > 9.0) {
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
        // what it strikes with from where it stands goes the way it faces: it has to have turned to them first
        if (Mth.degreesDifferenceAbs(this.yaw, yawOf(target.getX() - this.getX(), target.getZ() - this.getZ())) < 28.0F) {
            boolean underDisc = Math.abs(far - DISC_BARE_SLAM[0]) < SLAM_RADIUS - 0.6;
            if (underDisc && this.random.nextInt(3) != 0) {
                return SLAM;
            }
            if (this.sweepCooldown <= 0 && far < SWEEP_REACH_BARE - 0.8) {
                this.sweepCooldown = 110 + this.random.nextInt(70);
                return SWEEP;
            }
            if (far > CRUSH_FAR_BARE - CRUSH_REACH + 0.6 && far < CRUSH_FAR_BARE + CRUSH_REACH - 0.8 && this.random.nextInt(3) != 0) {
                return CRUSH;
            }
            if (this.gazeCooldown <= 0 && far > 7.0) {
                this.gazeCooldown = 80 + this.random.nextInt(55);
                return GAZE;
            }
            if (underDisc) {
                return SLAM;
            }
        }
        if (this.reachCooldown <= 0) {
            this.reachCooldown = 120 + this.random.nextInt(70);
            return REACH;
        }
        return 0;
    }

    @Override
    public void startAttack(int id) {
        super.startAttack(id);
        if (id == GLARE) {
            this.glareTurn = this.random.nextBoolean();
            this.glareWalls = this.liveEyes() >= 4 && this.random.nextBoolean();
        }
        if (id == CROSS && !this.level().isClientSide()) {
            this.crossTo = this.acrossTheHall();
        }
    }

    private static Vec3 ground(Level level, double x, double y, double z) {
        Vec3 found = RootEntity.ground(level, x, y, z);
        return found != null ? found : new Vec3(x, y, z);
    }

    /** The place in its hall furthest from whoever it fights - and, if it is there already, a third of the way round from it. */
    private Vec3 acrossTheHall() {
        Vec3 centre = this.centre();
        LivingEntity target = this.getTarget();
        double dx = target != null ? centre.x - target.getX() : this.random.nextDouble() - 0.5;
        double dz = target != null ? centre.z - target.getZ() : this.random.nextDouble() - 0.5;
        double far = Math.hypot(dx, dz);
        if (far < 1.0) {
            double a = this.random.nextDouble() * Math.PI * 2.0;
            dx = Math.cos(a);
            dz = Math.sin(a);
            far = 1.0;
        }
        Vec3 to = new Vec3(centre.x + dx / far * ROAM, this.getY(), centre.z + dz / far * ROAM);
        if (Math.hypot(to.x - this.getX(), to.z - this.getZ()) < 7.0) {
            Vec3 way = new Vec3(dx / far, 0.0, dz / far).yRot(this.random.nextBoolean() ? 2.1F : -2.1F);
            to = new Vec3(centre.x + way.x * ROAM, this.getY(), centre.z + way.z * ROAM);
        }
        return to;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.home == null) {
            BlockPos mark = MobMarkBlockEntity.homeOf(this);
            this.home = mark != null ? new Vec3(mark.getX() + 0.5, mark.getY(), mark.getZ() + 0.5) : this.position();
        }
        if (Float.isNaN(this.homeYaw)) {
            this.homeYaw = this.getYRot();
            this.yaw = this.homeYaw;
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
        this.stampCooldown = Math.max(0, this.stampCooldown - 1);
        this.crossCooldown = Math.max(0, this.crossCooldown - 1);
        LivingEntity target = this.getTarget();
        int id = this.attackId();
        if (this.loose()) {
            this.roam(level, target, id);
        } else {
            this.keep(target, id);
        }
        if (this.isAlive()) {
            this.tend(level);
        }
        // out of its mask, an arm comes up out of the floor under whoever it fights now and again, whatever else it is doing
        if (this.bare() && target != null && id != EMERGE && --this.armClock <= 0) {
            this.armClock = 150 + this.random.nextInt(80);
            Vec3 lead = target.getKnownMovement().multiply(1.0, 0.0, 1.0).scale(14.0);
            this.arm(level, target.getX() + lead.x, target.getZ() + lead.z, 26);
        }
    }

    /** In the crack: it neither goes anywhere nor turns, and looks by turning what is out of it. */
    private void keep(LivingEntity target, int id) {
        Vec3 motion = this.getDeltaMovement();
        if (motion.x != 0.0 || motion.z != 0.0) {
            this.setDeltaMovement(0.0, motion.y, 0.0);
        }
        float aim = this.aim();
        float want = aim;
        if (target == null || id == EMERGE) {
            want = 0.0F;
        } else if (id == 0 || this.attackTime <= this.turnUntil(id)) {
            Vec3 rift = this.rift();
            want = Mth.clamp((float) Math.toRadians(Mth.wrapDegrees(yawOf(target.getX() - rift.x, target.getZ() - rift.z) - this.homeYaw)), -AIM_MAX, AIM_MAX);
        }
        float next = aim + Mth.clamp(want - aim, -0.06F, 0.06F);
        if (Math.abs(next - aim) > 0.004F) {
            this.entityData.set(AIM, next);
        }
        this.yaw = this.homeYaw;
        this.face(this.homeYaw);
    }

    private void face(float yaw) {
        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);
    }

    /**
     * Out of the crack: it goes after whoever it fights until it is near, or across its hall, on the floor and no
     * further than {@link #ROAM} from the middle; and turns, slowly, to whoever it fights or the way it goes.
     */
    private void roam(ServerLevel level, LivingEntity target, int id) {
        if (this.aim() != 0.0F) {
            this.entityData.set(AIM, 0.0F);
        }
        Vec3 go = null;
        double speed = 0.0;
        if (id == CROSS) {
            if (this.attackTime >= CROSS_GO && this.crossTo != null) {
                go = this.crossTo;
                speed = RUN;
                if (Math.hypot(go.x - this.getX(), go.z - this.getZ()) < 1.5) {
                    this.crossTo = null;
                    go = null;
                }
            }
        } else if (id == 0 && target != null && Math.hypot(target.getX() - this.getX(), target.getZ() - this.getZ()) > NEAR) {
            go = target.position();
            speed = WALK;
        }
        double vx = 0.0;
        double vz = 0.0;
        if (go != null) {
            Vec3 centre = this.centre();
            double cx = go.x - centre.x;
            double cz = go.z - centre.z;
            double off = Math.hypot(cx, cz);
            if (off > ROAM) {
                go = new Vec3(centre.x + cx / off * ROAM, go.y, centre.z + cz / off * ROAM);
            }
            double dx = go.x - this.getX();
            double dz = go.z - this.getZ();
            double far = Math.hypot(dx, dz);
            if (far > 0.3) {
                double step = Math.min(speed, far);
                vx = dx / far * step;
                vz = dz / far * step;
            }
        }
        this.setDeltaMovement(vx, this.getDeltaMovement().y, vz);
        boolean going = vx != 0.0 || vz != 0.0;
        float want = this.yaw;
        if (id == CROSS && going) {
            want = yawOf(vx, vz);
        } else if (target != null && (id == 0 || this.attackTime <= this.turnUntil(id))) {
            want = yawOf(target.getX() - this.getX(), target.getZ() - this.getZ());
        }
        this.yaw = Mth.approachDegrees(this.yaw, want, id == CROSS ? TURN * 2.5F : TURN);
        this.face(this.yaw);
        if (going && --this.stepClock <= 0) {
            // a hand comes down: on the floor, and going across the hall on whoever is under it
            this.stepClock = speed > WALK ? 5 : 11;
            Vec3 side = this.facing().yRot((this.tickCount / 5) % 2 == 0 ? 1.2F : -1.2F).scale(6.0);
            Vec3 at = this.position().add(side);
            level.sendParticles(ParticleTypes.SPLASH, at.x, at.y + 0.2, at.z, 24, 0.8, 0.1, 0.8, 0.2);
            level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.tendril.slap"), this.getSoundSource(), speed > WALK ? 2.2F : 1.4F, 0.55F + this.random.nextFloat() * 0.15F);
            if (id == CROSS) {
                this.hitAround(level, this.position(), 4.4, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.8F, 1.4, this.damageSources().mobAttack(this));
            }
        }
    }

    // ------------------------------------------------------------------ what is not itself

    /** Its disc can be struck where it is; the eyes in the rock are opened again if they are gone (none of this is kept with the world). */
    private void tend(ServerLevel level) {
        if (this.face == null || !this.face.isAlive()) {
            this.face = FaceEntity.of(level, this);
        }
        Vec3 disc = this.discNow();
        this.face.setPos(disc.x, disc.y - this.face.getBbHeight() * 0.5, disc.z);
        if (this.loose() && this.tickCount % 40 == 7 && (this.greatEye == null || !this.greatEye.isAlive())) {
            this.openEyes(level);
        }
    }

    private int liveEyes() {
        int count = 0;
        for (WallEyeEntity eye : this.eyes) {
            if (eye.isAlive()) {
                count++;
            }
        }
        return count;
    }

    private void shutEyes() {
        if (this.greatEye != null) {
            this.greatEye.shut();
            this.greatEye = null;
        }
        for (WallEyeEntity eye : this.eyes) {
            eye.shut();
        }
        this.eyes.clear();
    }

    private static boolean rock(ServerLevel level, BlockPos pos) {
        return !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    /**
     * The eyes open: the great one in the crack it came out of, and lesser ones in the rock round the hall - one
     * looked for along every spoke from the middle of it but those to the crack, at some height, where the rock has a
     * face to the hall with rock over and under it and nothing before it.
     */
    private void openEyes(ServerLevel level) {
        this.shutEyes();
        this.greatEye = WallEyeEntity.open(level, this, this.eye(), this.homeYaw(), true, 1.0F, 0.0F, 0);
        Vec3 centre = this.centre();
        Vec3 out = this.outward();
        double back = Math.atan2(-out.z, -out.x);
        for (int i = 2; i < EYES - 2; i++) {
            double a = back + (i + 0.5) * Math.PI * 2.0 / EYES;
            for (int k = 0; k < EYE_TRIES; k++) {
                Vec3 from = centre.add(0.0, 2.5 + (i * 5 + k * 3) % 9, 0.0);
                Vec3 to = from.add(Math.cos(a) * (HALL + 9.0), 0.0, Math.sin(a) * (HALL + 9.0));
                BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                if (hit.getType() != HitResult.Type.BLOCK) {
                    continue;
                }
                Direction side = hit.getDirection();
                BlockPos pos = hit.getBlockPos();
                BlockPos before = pos.relative(side);
                if (side.getAxis().isVertical() || !rock(level, pos.above()) || !rock(level, pos.below()) || rock(level, before) || rock(level, before.above())
                        || rock(level, before.below())) {
                    continue;
                }
                Vec3 at = Vec3.atCenterOf(pos).add(side.getStepX() * 0.56, 0.0, side.getStepZ() * 0.56);
                this.eyes.add(WallEyeEntity.open(level, this, at, side.toYRot(), false, 1.3F + this.random.nextFloat() * 0.7F, (this.random.nextFloat() - 0.5F) * 50.0F, 4 + i * 2));
                break;
            }
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
        Vec3 at = RootEntity.ground(level, x, this.home().y, z);
        if (at == null) {
            return false;
        }
        TendrilEntity.rise(level, this, at, warning, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.9F);
        return true;
    }

    /** An arm brought round, low: everybody within {@code reach} of {@code from} and before it. */
    private void sweep(ServerLevel level, Vec3 from, double reach, float damage) {
        Vec3 way = this.facing();
        DamageSource source = this.damageSources().mobAttack(this);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(from, from).inflate(reach + 2.0, 3.0, reach + 2.0), this::isFoe)) {
            Vec3 to = new Vec3(victim.getX() - from.x, 0.0, victim.getZ() - from.z);
            if (to.length() - victim.getBbWidth() * 0.5 > reach || to.lengthSqr() > 4.0 && to.normalize().dot(way) < -0.26) {
                continue;
            }
            if (victim.hurtServer(level, source, damage)) {
                victim.knockback(1.7 * com.keykeeper.dushi.Gear.thrown(victim), from.x - victim.getX(), from.z - victim.getZ(), source, damage);
            }
        }
        for (int i = -3; i <= 3; i++) {
            Vec3 at = from.add(way.yRot(i * 0.5F).scale(reach - 1.5));
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y + 0.9, at.z, 1, 0.3, 0.0, 0.3, 0.0);
        }
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        boolean bare = this.bare();
        double floor = this.home().y;
        switch (id) {
            case GAZE -> {
                if (time == 4) {
                    this.playSound(Dushi.sound("entity.coming.gaze"), 4.0F, 1.0F);
                }
                if (time == GAZE_LOOK) {
                    // where it looks: rays from the hole in its disc to the floor, in a line out from it past whoever it is
                    // looking at; three lines with the mask off
                    Vec3 forward = this.facing();
                    Vec3 head = this.head();
                    Vec3 from = this.origin();
                    double[] turns = bare ? new double[] {0.0, 0.4, -0.4} : new double[] {0.0};
                    for (double turn : turns) {
                        Vec3 line = forward.yRot((float) turn);
                        for (int i = 0; i < 13; i++) {
                            double far = 6.0 + i * 2.7;
                            double x = from.x + line.x * far;
                            double z = from.z + line.z * far;
                            if (this.inHall(x, z)) {
                                this.star(level, ground(level, x, floor, z), 20 + i * 3, STAR_RADIUS, damage * 0.8F, head);
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
                    Vec3 disc = this.discAt(bare ? DISC_BARE_SLAM : DISC_SLAM);
                    Vec3 at = new Vec3(disc.x, floor, disc.z);
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
                    // (and the rope, all the way from it to the disc, on whoever stands under that)
                    Vec3 from = this.origin();
                    Vec3 way = this.facing();
                    double length = Math.hypot(at.x - from.x, at.z - from.z);
                    for (double far = bare ? 4.0 : 2.5; far < length - SLAM_RADIUS; far += 2.0) {
                        Vec3 on = new Vec3(from.x + way.x * far, floor, from.z + way.z * far);
                        this.hitAround(level, on, 1.5, damage * 1.1F, 0.8, this.damageSources().mobAttack(this));
                        level.sendParticles(ParticleTypes.SPLASH, on.x, on.y + 0.2, on.z, 20, 0.6, 0.1, 0.6, 0.2);
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
                    this.sweep(level, this.origin(), bare ? SWEEP_REACH_BARE : SWEEP_REACH, damage);
                }
            }
            case CRUSH -> {
                if (time == CRUSH_IMPACT - 10) {
                    this.playSound(Dushi.sound("entity.coming.sweep"), 3.0F, 0.8F);
                }
                if (time == CRUSH_IMPACT) {
                    Vec3 from = this.origin();
                    Vec3 way = this.facing();
                    double far = bare ? CRUSH_FAR_BARE : CRUSH_FAR + FACE;
                    Vec3 at = new Vec3(from.x + way.x * far, floor, from.z + way.z * far);
                    this.hitAround(level, at, CRUSH_REACH, damage * 0.9F, 2.0, this.damageSources().mobAttack(this));
                    level.sendParticles(ParticleTypes.SPLASH, at.x, at.y + 0.2, at.z, 100, 3.0, 0.2, 3.0, 0.3);
                    level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.coming.slam"), this.getSoundSource(), 3.0F, 1.2F);
                }
            }
            case REACH -> {
                if (time == 4) {
                    this.playSound(Dushi.sound("entity.coming.strain"), 3.0F, 1.3F);
                }
                if (time == REACH_CALL) {
                    // under whoever it fights, where they are going, and round them
                    LivingEntity target = this.getTarget();
                    Vec3 aim = target != null ? target.position() : this.centre();
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
                Vec3 head = this.discAt(DISC_GAZE);
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
                    Vec3 rift = this.rift();
                    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(rift, rift).inflate(14.0, 4.0, 14.0), this::isFoe)) {
                        Vec3 away = new Vec3(victim.getX() - rift.x, 0.0, victim.getZ() - rift.z);
                        double far = away.length();
                        if (far < 14.0) {
                            away = far < 1.0E-3 ? this.outward() : away.scale(1.0 / far);
                            victim.hurtServer(level, this.damageSources().mobAttack(this), damage * 0.5F);
                            shove(victim, new Vec3(away.x * 1.1, 0.4, away.z * 1.1));
                        }
                    }
                    this.playSound(Dushi.sound("entity.coming.unmask"), 5.0F, 1.0F);
                }
                if (time == EMERGE_BREAK + 20) {
                    this.playSound(Dushi.sound("entity.coming.rear"), 5.0F, 0.8F);
                }
                if (time == EMERGE_EYES) {
                    // the eyes open
                    this.openEyes(level);
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
                        Vec3 centre = this.centre();
                        double a = this.random.nextDouble() * Math.PI * 2.0;
                        double far = this.random.nextDouble() * HALL;
                        level.sendParticles(ParticleTypes.REVERSE_PORTAL, centre.x + Math.cos(a) * far, floor + 0.6 + this.random.nextDouble() * 2.0, centre.z + Math.sin(a) * far, 4, 0.3, 0.3, 0.3,
                                0.02);
                    }
                }
                if (time == PULL_BITE - 6) {
                    this.playSound(Dushi.sound("entity.coming.rear"), 4.0F, 1.3F);
                }
                if (time == PULL_BITE) {
                    // the mouth comes down: under the knot, and before it
                    this.hitAround(level, this.position().add(this.facing().scale(2.6)), 4.4, damage * 1.7F, 1.6, this.damageSources().mobAttack(this));
                    this.hitAround(level, this.position().add(this.facing().scale(7.0)), 3.2, damage * 1.7F, 1.6, this.damageSources().mobAttack(this));
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
                    Vec3 centre = this.centre();
                    Vec3 at;
                    int kind = (time / 3) % 3;
                    if (target != null && kind == 0) {
                        at = target.position();
                    } else if (target != null && kind == 1) {
                        at = target.position().add(target.getKnownMovement().multiply(1.0, 0.0, 1.0).scale(16.0));
                    } else {
                        double a = this.random.nextDouble() * Math.PI * 2.0;
                        double far = Math.sqrt(this.random.nextDouble()) * HALL;
                        at = centre.add(Math.cos(a) * far, 0.0, Math.sin(a) * far);
                    }
                    if (this.inHall(at.x, at.z)) {
                        this.star(level, ground(level, at.x, floor, at.z), STAR_DELAY, STAR_RADIUS, damage * 0.75F, null);
                    }
                }
            }
            case STAMP -> {
                if (time == 4) {
                    this.playSound(Dushi.sound("entity.coming.strain"), 3.0F, 0.8F);
                }
                if (time == STAMP_IMPACT) {
                    // the knot let down on whoever is under it, and no shield holds it
                    Vec3 at = this.position();
                    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(STAMP_RADIUS + 1.0, 3.0, STAMP_RADIUS + 1.0), this::isFoe)) {
                        if (Math.hypot(victim.getX() - at.x, victim.getZ() - at.z) - victim.getBbWidth() * 0.5 > STAMP_RADIUS) {
                            continue;
                        }
                        if (victim.isBlocking()) {
                            breakGuard(level, victim);
                        }
                        if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage * 1.4F)) {
                            Vec3 away = new Vec3(victim.getX() - at.x, 0.0, victim.getZ() - at.z);
                            away = away.lengthSqr() < 1.0E-3 ? this.facing() : away.normalize();
                            shove(victim, new Vec3(away.x * 0.9, 0.45, away.z * 0.9));
                        }
                    }
                    level.sendParticles(ParticleTypes.SPLASH, at.x, at.y + 0.2, at.z, 160, 3.0, 0.2, 3.0, 0.3);
                    level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.coming.slam"), this.getSoundSource(), 4.0F, 0.8F);
                }
            }
            case CROSS -> {
                if (time == 2) {
                    this.playSound(Dushi.sound("entity.coming.rear"), 4.0F, 1.15F);
                }
                // (it goes: see roam) - and where it comes to, it has done with this and turns
                if (time > CROSS_GO + 6 && this.crossTo == null && time < CROSS_DURATION - 12) {
                    this.attackTime = CROSS_DURATION - 12;
                    this.gazeCooldown = Math.min(this.gazeCooldown, 12);
                }
            }
            default -> {
                if (time == 6) {
                    this.playSound(Dushi.sound("entity.coming.gaze"), 5.0F, 0.55F);
                }
                int step = time - GLARE_FROM;
                if (step < 0 || step >= GLARE_STEP * GLARE_RAYS || step % GLARE_STEP != 0) {
                    break;
                }
                int ray = step / GLARE_STEP;
                if (this.glareWalls) {
                    // the lesser eyes look, one after another: each at where whoever it fights is going
                    LivingEntity target = this.getTarget();
                    WallEyeEntity eye = null;
                    for (int i = 0; i < this.eyes.size() && eye == null; i++) {
                        WallEyeEntity next = this.eyes.get((ray * 5 + i) % this.eyes.size());
                        eye = next.isAlive() ? next : null;
                    }
                    if (eye != null && target != null) {
                        Vec3 at = target.position().add(target.getKnownMovement().multiply(1.0, 0.0, 1.0).scale(ray % 2 == 0 ? 0.0 : 12.0));
                        if (this.inHall(at.x, at.z)) {
                            this.star(level, ground(level, at.x, floor, at.z), 18, GLARE_RADIUS, damage * 0.85F, eye.position());
                            eye.look();
                        }
                    }
                } else {
                    // the great eye looks: one ray of a fan that goes across the hall from one side of the crack to the other
                    double turn = (-1.05 + ray * 2.1 / (GLARE_RAYS - 1)) * (this.glareTurn ? 1.0 : -1.0);
                    Vec3 line = this.outward().yRot((float) turn);
                    Vec3 eye = this.eye();
                    Vec3 rift = this.rift();
                    for (double far = 5.0; far < 38.0; far += 3.4) {
                        double x = rift.x + line.x * far;
                        double z = rift.z + line.z * far;
                        if (this.inHall(x, z)) {
                            this.star(level, ground(level, x, floor, z), 18, GLARE_RADIUS, damage * 0.85F, eye);
                        }
                    }
                    if (this.greatEye != null && ray == 0) {
                        this.greatEye.look();
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

    /** A blow on its disc ({@link FaceEntity}): it is what it is, and a blow there tells for half as much again. */
    public boolean hurtFace(ServerLevel level, DamageSource source, float damage) {
        this.faceStruck = true;
        try {
            return this.hurtServer(level, source, damage);
        } finally {
            this.faceStruck = false;
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        float through = HIDE * (this.open() ? 2.0F : 1.0F) * (this.faceStruck ? 1.5F : 1.0F);
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
        this.shutEyes();
        if (this.level() instanceof ServerLevel level) {
            BlockPos mark = MobMarkBlockEntity.homeOf(this);
            FogGateBlock.lift(level, mark != null ? mark : BlockPos.containing(this.home()), 40);
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
        if (this.home != null) {
            output.putDouble("HomeX", this.home.x);
            output.putDouble("HomeY", this.home.y);
            output.putDouble("HomeZ", this.home.z);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        float yaw = input.getFloatOr("HomeYaw", Float.NaN);
        if (!Float.isNaN(yaw)) {
            this.homeYaw = yaw;
            this.yaw = yaw;
        }
        double y = input.getDoubleOr("HomeY", Double.NaN);
        if (!Double.isNaN(y)) {
            this.home = new Vec3(input.getDoubleOr("HomeX", 0.0), y, input.getDoubleOr("HomeZ", 0.0));
        }
    }
}
