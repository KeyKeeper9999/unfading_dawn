package com.keykeeper.dushi.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A piece of the works of the stopped clock - the hall the Bolt hangs in is that clock, and the Bolt's body as much as
 * the lens is. Three kinds of piece:
 * <ul>
 * <li>a wheel in the wall ({@link #MOUNT}): it stands behind the rim of the floor and turns, slowly while nothing is
 * the matter and fast in a fight; it does nothing else, and is there as long as the Bolt is;</li>
 * <li>a wheel let go ({@link #ROLL}): it turns where it stands for {@code warning} ticks, throwing sparks along the
 * line it will take, then runs along the floor to the other side of the hall, and whoever is in its way is run down
 * and thrown aside (once: it is stepped out of, or rolled through);</li>
 * <li>a weight of the clock on its chain ({@link #WEIGHT}): it comes down out of the dark over {@code warning} ticks,
 * the last of them fast, stands a moment and is drawn up again. The blow is the Bolt's own (a place marked on the
 * floor, see BoltEntity); the weight is what is seen to deal it.</li>
 * </ul>
 * None of them can be struck, and none is kept when the world is saved: the Bolt hangs its wheels again.
 */
public class GearEntity extends Entity {
    public static final int MOUNT = 0;
    public static final int ROLL = 1;
    public static final int WEIGHT = 2;
    private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(GearEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WARNING = SynchedEntityData.defineId(GearEntity.class, EntityDataSerializers.INT);
    /** How many times its drawn size it is (the wheel of the model is {@link #RADIUS} blocks to the tips of its teeth). */
    private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(GearEntity.class, EntityDataSerializers.FLOAT);
    /** Degrees a tick a wheel in the wall turns; blocks a tick a wheel let go runs; how high over the floor a weight begins. */
    private static final EntityDataAccessor<Float> SPEED = SynchedEntityData.defineId(GearEntity.class, EntityDataSerializers.FLOAT);
    public static final float RADIUS = 1.6F;
    /** How long a weight stands where it fell, and how long it takes to be drawn up out of sight. */
    public static final int STANDS = 16;
    public static final int HAULED = 26;
    /** The last ticks of a weight's warning are its fall. */
    public static final int FALL = 7;
    private static final double BREADTH = 1.2;

    private SoulsMob owner;
    private float damage;
    private Vec3 way = Vec3.ZERO;
    private double left;
    private final Set<UUID> struck = new HashSet<>();

    public GearEntity(EntityType<? extends GearEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    private static GearEntity make(ServerLevel level, SoulsMob owner, int kind, Vec3 at, float yaw, float size, float speed, int warning) {
        GearEntity gear = new GearEntity(Dushi.GEAR.get(), level);
        gear.owner = owner;
        gear.entityData.set(KIND, kind);
        gear.entityData.set(SIZE, size);
        gear.entityData.set(SPEED, speed);
        gear.entityData.set(WARNING, warning);
        gear.setPos(at.x, at.y, at.z);
        gear.setYRot(yaw);
        level.addFreshEntity(gear);
        return gear;
    }

    /** A wheel in the wall at {@code at} (its lowest point), its axle pointing along {@code yaw}. */
    public static GearEntity mount(ServerLevel level, SoulsMob owner, Vec3 at, float yaw, float size) {
        return make(level, owner, MOUNT, at, yaw, size, 0.6F, 0);
    }

    /** A wheel let go at {@code from} to run to {@code to} along the floor, {@code warning} ticks from now. */
    public static GearEntity roll(ServerLevel level, SoulsMob owner, Vec3 from, Vec3 to, float speed, int warning, float damage) {
        Vec3 way = new Vec3(to.x - from.x, 0.0, to.z - from.z);
        double far = way.length();
        way = far < 1.0E-3 ? new Vec3(0.0, 0.0, 1.0) : way.scale(1.0 / far);
        GearEntity gear = make(level, owner, ROLL, from, (float) Math.toDegrees(Math.atan2(-way.x, way.z)), 1.0F, speed, warning);
        gear.way = way;
        gear.left = far;
        gear.damage = damage;
        return gear;
    }

    /** A weight that will stand on the floor at {@code at} {@code warning} ticks from now, having come down from {@code height} over it. */
    public static GearEntity weight(ServerLevel level, SoulsMob owner, Vec3 at, float height, int warning) {
        return make(level, owner, WEIGHT, at, level.getRandom().nextFloat() * 360.0F, 1.0F, height, warning);
    }

    public int kind() {
        return this.entityData.get(KIND);
    }

    public int warning() {
        return this.entityData.get(WARNING);
    }

    public float size() {
        return this.entityData.get(SIZE);
    }

    public float speed() {
        return this.entityData.get(SPEED);
    }

    /** A wheel in the wall turns so fast: the Bolt says (slowly asleep, fast in a fight). */
    public void turn(float degreesATick) {
        if (Math.abs(this.speed() - degreesATick) > 0.01F) {
            this.entityData.set(SPEED, degreesATick);
        }
    }

    /** How high over the floor a weight hangs {@code age} ticks after it began to come: it creeps down, and falls the last of the way. */
    public static float hang(float age, int warning, float height) {
        float fall = warning - FALL;
        if (age < fall) {
            return height * (1.0F - 0.25F * Math.max(0.0F, age) / Math.max(1.0F, fall));
        }
        if (age < warning) {
            float t = (age - fall) / FALL;
            return height * 0.75F * (1.0F - t * t);
        }
        float after = age - warning - STANDS;
        if (after <= 0.0F) {
            return 0.0F;
        }
        float t = Math.min(1.0F, after / HAULED);
        return height * t * t;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(KIND, MOUNT);
        builder.define(WARNING, 0);
        builder.define(SIZE, 1.0F);
        builder.define(SPEED, 0.6F);
    }

    private static boolean foe(LivingEntity entity) {
        return entity.isAlive() && !(entity instanceof SoulsMob) && !(entity instanceof ArmorStand)
                && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        int kind = this.kind();
        if (this.owner == null ? this.tickCount > 600 : !this.owner.isAlive()) {
            // nobody's any more (or summoned by hand, to be looked at): a wheel stops, and is gone
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.IRON_BLOCK.defaultBlockState()), this.getX(), this.getY() + 1.0, this.getZ(), 16, 0.6, 0.8, 0.6, 0.1);
            this.discard();
            return;
        }
        if (kind == MOUNT) {
            return;
        }
        int warning = this.warning();
        if (kind == WEIGHT) {
            if (this.tickCount > warning + STANDS + HAULED) {
                this.discard();
            }
            return;
        }
        if (this.tickCount < warning) {
            // it turns where it stands, and the line it will take is marked in sparks: the place to be out of
            if (this.tickCount % 2 == 0) {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 0.3, this.getZ(), 3, 0.3, 0.2, 0.3, 0.05);
                double step = (this.tickCount % 12) / 12.0;
                for (double far = step * 3.0; far < this.left; far += 3.0) {
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX() + this.way.x * far, this.getY() + 0.15, this.getZ() + this.way.z * far, 1, 0.1, 0.0, 0.1, 0.0);
                }
            }
            if (this.tickCount == 1) {
                level.playSound(null, this.getX(), this.getY(), this.getZ(), Dushi.sound("entity.gear.wind"), SoundSource.HOSTILE, 2.4F, 0.9F + level.getRandom().nextFloat() * 0.2F);
            }
            return;
        }
        if (this.tickCount == warning) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), Dushi.sound("entity.gear.roll"), SoundSource.HOSTILE, 2.6F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        }
        double speed = this.speed();
        this.setPos(this.getX() + this.way.x * speed, this.getY(), this.getZ() + this.way.z * speed);
        this.left -= speed;
        if (this.tickCount % 2 == 0) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 0.1, this.getZ(), 4, 0.2, 0.05, 0.2, 0.12);
        }
        if (this.owner != null && this.owner.isAlive()) {
            DamageSource source = this.owner.damageSources().mobAttack(this.owner);
            Vec3 side = new Vec3(-this.way.z, 0.0, this.way.x);
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(this.position(), this.position()).inflate(RADIUS + 1.0, 3.0, RADIUS + 1.0), GearEntity::foe)) {
                double dx = victim.getX() - this.getX();
                double dz = victim.getZ() - this.getZ();
                double across = dx * side.x + dz * side.z;
                double along = dx * this.way.x + dz * this.way.z;
                if (Math.abs(across) > BREADTH * 0.5 + victim.getBbWidth() * 0.5 || Math.abs(along) > RADIUS * 0.8 || victim.getY() > this.getY() + RADIUS * 2.0
                        || this.struck.contains(victim.getUUID())) {
                    continue;
                }
                if (victim.hurtServer(level, source, this.damage)) {
                    this.struck.add(victim.getUUID());
                    double out = across >= 0.0 ? 1.0 : -1.0;
                    SoulsMob.shove(victim, new Vec3(this.way.x * 0.45 + side.x * out * 0.75, 0.38, this.way.z * 0.45 + side.z * out * 0.75));
                    level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), Dushi.sound("entity.bolt.strike"), SoundSource.HOSTILE, 1.6F, 1.1F);
                }
            }
        }
        if (this.left <= 0.0) {
            // into the works of the far wall
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.IRON_BLOCK.defaultBlockState()), this.getX(), this.getY() + 1.2, this.getZ(), 30, 0.5, 0.9, 0.5, 0.2);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 1.2, this.getZ(), 20, 0.6, 0.9, 0.6, 0.2);
            level.playSound(null, this.getX(), this.getY(), this.getZ(), Dushi.sound("entity.gear.catch"), SoundSource.HOSTILE, 2.4F, 0.9F + level.getRandom().nextFloat() * 0.2F);
            this.discard();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;                                            // (the Bolt hangs its wheels again when the world is opened)
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }

    /** (only a summon gives it any: which kind it is, to be looked at) */
    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.entityData.set(KIND, input.getIntOr("Kind", MOUNT));
        this.entityData.set(WARNING, input.getIntOr("Warning", 0));
        this.entityData.set(SIZE, input.getIntOr("Size10", 10) / 10.0F);
        this.entityData.set(SPEED, input.getIntOr("Speed10", 6) / 10.0F);
    }
}
