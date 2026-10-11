package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * An eye open in the rock: when the Coming is out of its crack, a great one opens in the crack it left, as tall as the
 * crack, and lesser ones in the walls all round its hall. An eye stands on end, flat on the face of the rock it is in,
 * looking out of it the way the entity faces; it opens {@code delay} ticks after it is there, follows whoever is
 * near with its ring (WallEyeModel), and widens for a moment when a ray comes out of it ({@link #look}). The rays are
 * the Coming's; an eye does nothing itself and cannot be struck. It shuts and is gone when the Coming is dead, or back
 * in its crack; one nobody opened (one summoned by hand) stays open, to be looked at.
 */
public class WallEyeEntity extends Entity {
    private static final EntityDataAccessor<Boolean> GREAT = SynchedEntityData.defineId(WallEyeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(WallEyeEntity.class, EntityDataSerializers.FLOAT);
    /** How far it is turned from standing straight on end, in degrees. */
    private static final EntityDataAccessor<Float> ROLL = SynchedEntityData.defineId(WallEyeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DELAY = SynchedEntityData.defineId(WallEyeEntity.class, EntityDataSerializers.INT);
    /** The tick of its own life it began to shut at; none while it is open. */
    private static final EntityDataAccessor<Integer> SHUT_AT = SynchedEntityData.defineId(WallEyeEntity.class, EntityDataSerializers.INT);
    /** The tick of its own life it last looked at (a ray came out of it). */
    private static final EntityDataAccessor<Integer> LOOKED_AT = SynchedEntityData.defineId(WallEyeEntity.class, EntityDataSerializers.INT);
    /** How long it takes to open, and to shut. */
    public static final int OPENING = 16;
    public static final int SHUTTING = 10;

    private ComingEntity owner;
    private boolean summoned = true;

    public WallEyeEntity(EntityType<? extends WallEyeEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /** An eye of {@code owner}'s at {@code at}, looking the way {@code yaw}: its middle is where the entity is. */
    public static WallEyeEntity open(ServerLevel level, ComingEntity owner, Vec3 at, float yaw, boolean great, float size, float roll, int delay) {
        WallEyeEntity eye = new WallEyeEntity(Dushi.WALL_EYE.get(), level);
        eye.owner = owner;
        eye.summoned = false;
        eye.entityData.set(GREAT, great);
        eye.entityData.set(SIZE, size);
        eye.entityData.set(ROLL, roll);
        eye.entityData.set(DELAY, delay);
        eye.setPos(at.x, at.y, at.z);
        eye.setYRot(yaw);
        level.addFreshEntity(eye);
        return eye;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(GREAT, false);
        builder.define(SIZE, 1.3F);
        builder.define(ROLL, 0.0F);
        builder.define(DELAY, 0);
        builder.define(SHUT_AT, -1);
        builder.define(LOOKED_AT, -1000);
    }

    public boolean great() {
        return this.entityData.get(GREAT);
    }

    public float size() {
        return this.entityData.get(SIZE);
    }

    public float roll() {
        return this.entityData.get(ROLL);
    }

    public int delay() {
        return this.entityData.get(DELAY);
    }

    public int shutAt() {
        return this.entityData.get(SHUT_AT);
    }

    public int lookedAt() {
        return this.entityData.get(LOOKED_AT);
    }

    /** How far open it is {@code age} ticks into its life: nothing, to wide open. */
    public float lid(float age) {
        float open = Math.max(0.0F, Math.min(1.0F, (age - this.delay()) / OPENING));
        int shut = this.shutAt();
        if (shut >= 0) {
            open *= Math.max(0.0F, 1.0F - (age - shut) / SHUTTING);
        }
        return open * open * (3.0F - 2.0F * open);
    }

    /** A ray comes out of it: it widens. */
    public void look() {
        this.entityData.set(LOOKED_AT, this.tickCount);
    }

    /** It shuts, and is gone. */
    public void shut() {
        if (this.shutAt() < 0) {
            this.entityData.set(SHUT_AT, this.tickCount);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        int shut = this.shutAt();
        if (shut >= 0) {
            if (this.tickCount > shut + SHUTTING) {
                this.discard();
            }
        } else if (!this.summoned && this.tickCount % 10 == 0 && (this.owner == null || !this.owner.isAlive() || this.owner.isRemoved() || !this.owner.bare())) {
            this.shut();
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
        return false;                                            // (the Coming opens them again)
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }

    /** (only a summon gives it any: Great, Size10 - its size in tenths - and Roll in degrees; the way it looks is its Rotation) */
    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.entityData.set(GREAT, input.getBooleanOr("Great", false));
        this.entityData.set(SIZE, input.getIntOr("Size10", 13) / 10.0F);
        this.entityData.set(ROLL, (float) input.getIntOr("Roll", 0));
    }
}
