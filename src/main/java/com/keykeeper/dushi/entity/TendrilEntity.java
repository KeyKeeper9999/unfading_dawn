package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * What the Coming reaches into its hall with: a hole opens in the floor - a round of the same night it is made of,
 * with a rim of light - and for {@code warning} ticks only fingers show in it; then an arm as tall as a house comes
 * straight up out of it (whoever stands on the hole is struck and thrown), stands, leans back, and comes down full
 * length along the floor towards whoever is nearest ({@link #LENGTH} blocks, and no shield holds it); it lies a
 * moment, is drawn up and goes back down, and the hole shuts.
 * <p>
 * The blows are the Coming's; an arm nobody sent (one summoned by hand) strikes nobody and stands a long time, to be
 * looked at.
 */
public class TendrilEntity extends Entity {
    private static final EntityDataAccessor<Integer> WARNING = SynchedEntityData.defineId(TendrilEntity.class, EntityDataSerializers.INT);
    /** Which way it comes down, in degrees: known when it is up. */
    private static final EntityDataAccessor<Float> FALL_YAW = SynchedEntityData.defineId(TendrilEntity.class, EntityDataSerializers.FLOAT);
    /** How long it stands before it comes down, in ticks: STAND for one that was sent, a long time for one summoned by hand. */
    private static final EntityDataAccessor<Integer> STANDS = SynchedEntityData.defineId(TendrilEntity.class, EntityDataSerializers.INT);
    /** Ticks: coming up; standing (the last of them leaning back); coming down; lying; getting up again; going back down. */
    public static final int RISE = 4;
    public static final int STAND = 16;
    public static final int FALL = 5;
    public static final int LIE = 14;
    public static final int BACK = 7;
    public static final int SINK = 9;
    /** What is left of its time once it has stood. */
    public static final int REST = FALL + LIE + BACK + SINK;
    public static final double HOLE = 1.5;
    public static final double LENGTH = 6.4;
    public static final double BREADTH = 1.1;
    private static final int SHOWN = 400;

    private SoulsMob owner;
    private float damage;

    public TendrilEntity(EntityType<? extends TendrilEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /** An arm of {@code owner}'s out of the floor at {@code at}; it comes up after {@code warning} ticks. */
    public static TendrilEntity rise(ServerLevel level, SoulsMob owner, Vec3 at, int warning, float damage) {
        TendrilEntity arm = new TendrilEntity(Dushi.TENDRIL.get(), level);
        arm.owner = owner;
        arm.damage = damage;
        arm.entityData.set(WARNING, warning);
        arm.entityData.set(FALL_YAW, level.getRandom().nextFloat() * 360.0F);
        arm.setPos(at.x, at.y, at.z);
        level.addFreshEntity(arm);
        return arm;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(WARNING, 24);
        builder.define(FALL_YAW, 0.0F);
        builder.define(STANDS, STAND);
    }

    public int warning() {
        return this.entityData.get(WARNING);
    }

    public float fallYaw() {
        return this.entityData.get(FALL_YAW);
    }

    /** How long it stands before it comes down. */
    public int stands() {
        return this.entityData.get(STANDS);
    }

    /** Whether it is out of the floor: for the test. */
    public boolean isUp() {
        return this.tickCount >= this.warning() + RISE;
    }

    private static boolean foe(LivingEntity entity) {
        return entity.isAlive() && !(entity instanceof SoulsMob) && !(entity instanceof ArmorStand)
                && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    /** The way it comes down: a unit vector along the floor. */
    private Vec3 fall() {
        return Vec3.directionFromRotation(0.0F, this.fallYaw());
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        int warning = this.warning();
        int time = this.tickCount - warning;
        int stood = RISE + this.stands();
        boolean sent = this.owner != null && this.owner.isAlive();
        if (time < 0) {
            // the hole is the warning: it is seen opening, and its rim creeps
            if (this.tickCount % 3 == 0) {
                double a = this.tickCount * 0.7;
                level.sendParticles(ParticleTypes.WITCH, this.getX() + Math.cos(a) * HOLE, this.getY() + 0.15, this.getZ() + Math.sin(a) * HOLE, 1, 0.0, 0.0, 0.0, 0.0);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 0.2, this.getZ(), 2, HOLE * 0.4, 0.05, HOLE * 0.4, 0.01);
            }
            if (this.tickCount == 1) {
                level.playSound(null, this.getX(), this.getY(), this.getZ(), Dushi.sound("entity.tendril.open"), SoundSource.HOSTILE, 1.6F, 0.85F + level.getRandom().nextFloat() * 0.3F);
            }
            return;
        }
        if (time == 0) {
            if (sent) {
                DamageSource source = this.owner.damageSources().mobAttack(this.owner);
                for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(this.position(), this.position()).inflate(HOLE + 1.0, 4.0, HOLE + 1.0), TendrilEntity::foe)) {
                    double far = Math.hypot(victim.getX() - this.getX(), victim.getZ() - this.getZ()) - victim.getBbWidth() * 0.5;
                    if (far <= HOLE && victim.getY() < this.getY() + 5.0 && victim.hurtServer(level, source, this.damage * 0.75F)) {
                        SoulsMob.shove(victim, new Vec3((victim.getX() - this.getX()) * 0.2, 0.62, (victim.getZ() - this.getZ()) * 0.2));
                    }
                }
            }
            level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0, this.getZ(), 24, 0.4, 1.4, 0.4, 0.04);
            level.playSound(null, this.getX(), this.getY(), this.getZ(), Dushi.sound("entity.tendril.rise"), SoundSource.HOSTILE, 1.8F, 0.85F + level.getRandom().nextFloat() * 0.3F);
        }
        if (time == RISE + 3 && sent) {
            // up, it finds whoever is nearest, and that is the way it will come down
            LivingEntity nearest = null;
            double best = 11.0 * 11.0;
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(this.position(), this.position()).inflate(11.0, 5.0, 11.0), TendrilEntity::foe)) {
                double far = victim.distanceToSqr(this);
                if (far < best) {
                    best = far;
                    nearest = victim;
                }
            }
            if (nearest != null && best > 0.5) {
                this.entityData.set(FALL_YAW, (float) (Math.toDegrees(Math.atan2(nearest.getZ() - this.getZ(), nearest.getX() - this.getX())) - 90.0));
            }
        }
        if (time == stood + FALL) {
            Vec3 way = this.fall();
            if (sent) {
                DamageSource source = this.owner.damageSources().mobAttack(this.owner);
                Vec3 side = new Vec3(-way.z, 0.0, way.x);
                for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(this.position(), this.position()).inflate(LENGTH + 1.5, 3.0, LENGTH + 1.5), TendrilEntity::foe)) {
                    double dx = victim.getX() - this.getX();
                    double dz = victim.getZ() - this.getZ();
                    double along = dx * way.x + dz * way.z;
                    double across = dx * side.x + dz * side.z;
                    if (along < -0.6 || along > LENGTH + victim.getBbWidth() * 0.5 || Math.abs(across) > BREADTH + victim.getBbWidth() * 0.5 || victim.getY() > this.getY() + 2.5) {
                        continue;
                    }
                    if (victim.isBlocking()) {
                        SoulsMob.breakGuard(level, victim);
                    }
                    if (victim.hurtServer(level, source, this.damage)) {
                        double out = across >= 0.0 ? 1.0 : -1.0;
                        SoulsMob.shove(victim, new Vec3(side.x * out * 0.6, 0.3, side.z * out * 0.6));
                    }
                }
            }
            for (double far = 1.0; far <= LENGTH; far += 1.1) {
                level.sendParticles(ParticleTypes.SPLASH, this.getX() + way.x * far, this.getY() + 0.2, this.getZ() + way.z * far, 10, 0.5, 0.1, 0.5, 0.2);
                level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX() + way.x * far, this.getY() + 0.3, this.getZ() + way.z * far, 3, 0.4, 0.1, 0.4, 0.02);
            }
            level.playSound(null, this.getX() + way.x * 3.0, this.getY(), this.getZ() + way.z * 3.0, Dushi.sound("entity.tendril.slap"), SoundSource.HOSTILE, 2.2F,
                    0.85F + level.getRandom().nextFloat() * 0.3F);
        }
        if (time > stood + REST) {
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
        return false;                                            // (its blow belongs to a fight that will not be there when the world is opened again)
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.entityData.set(WARNING, input.getIntOr("Warning", 24));
        this.entityData.set(FALL_YAW, (float) input.getIntOr("Yaw", 0));
        this.entityData.set(STANDS, input.getIntOr("Stand", SHOWN));
    }
}
