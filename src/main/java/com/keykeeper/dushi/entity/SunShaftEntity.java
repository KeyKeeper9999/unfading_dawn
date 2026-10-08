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
 * A shaft of the sun's light standing on the ground, as a lighthouse's beam stands in a fog: it shows where the sun
 * will strike for the Scorched Warden, and the strike that kindles the Warden itself. It does nothing; it is only
 * seen (SunShaftRenderer). Its width grows to the full over {@code rise} ticks, holds until {@code strike}, flares
 * then and is gone by {@code life}.
 */
public class SunShaftEntity extends Entity {
    /** How high the shaft reaches: higher than anything near it, so that it is seen to come from the sky. */
    public static final float HEIGHT = 48.0F;
    private static final EntityDataAccessor<Float> WIDTH = SynchedEntityData.defineId(SunShaftEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> RISE = SynchedEntityData.defineId(SunShaftEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STRIKE = SynchedEntityData.defineId(SunShaftEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LIFE = SynchedEntityData.defineId(SunShaftEntity.class, EntityDataSerializers.INT);

    public SunShaftEntity(EntityType<? extends SunShaftEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /** A shaft on {@code at}: as wide as {@code width} blocks across at the full, striking {@code strike} ticks from now. */
    public static SunShaftEntity shine(ServerLevel level, Vec3 at, float width, int rise, int strike, int life) {
        SunShaftEntity shaft = new SunShaftEntity(Dushi.SUN_SHAFT.get(), level);
        shaft.setPos(at.x, at.y, at.z);
        shaft.entityData.set(WIDTH, width);
        shaft.entityData.set(RISE, Math.max(1, rise));
        shaft.entityData.set(STRIKE, strike);
        shaft.entityData.set(LIFE, Math.max(strike + 1, life));
        level.addFreshEntity(shaft);
        return shaft;
    }

    public float width() {
        return this.entityData.get(WIDTH);
    }

    public int rise() {
        return this.entityData.get(RISE);
    }

    public int strike() {
        return this.entityData.get(STRIKE);
    }

    public int life() {
        return this.entityData.get(LIFE);
    }

    /** How wide it is {@code age} ticks after it began, as a share of its full width (more than one when it flares). */
    public static float share(float age, int rise, int strike, int life) {
        if (age < 0.0F || age > life) {
            return 0.0F;
        }
        if (age < strike) {
            return 0.2F + 0.8F * Math.min(1.0F, age / rise);
        }
        float after = (age - strike) / Math.max(1.0F, life - strike);
        return after < 0.25F ? 1.0F + after * 1.6F : 1.4F * (1.0F - (after - 0.25F) / 0.75F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(WIDTH, 1.0F);
        builder.define(RISE, 10);
        builder.define(STRIKE, 30);
        builder.define(LIFE, 40);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount > this.life()) {
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
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }

    /** (only a summon gives it any: Width10 is its width in tenths of a block) */
    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.entityData.set(WIDTH, input.getIntOr("Width10", 10) / 10.0F);
        this.entityData.set(RISE, Math.max(1, input.getIntOr("Rise", 10)));
        this.entityData.set(STRIKE, input.getIntOr("Strike", 30));
        this.entityData.set(LIFE, input.getIntOr("Life", 40));
    }
}
