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
 * <p>
 * It has other lights than the sun's ({@link #hue}), and need not come from the sky: a ray ({@link #ray}) comes from a
 * place - the hole where the Coming's head should be, the eye behind it - down to the ground it stands on.
 */
public class SunShaftEntity extends Entity {
    /** How high the shaft reaches: higher than anything near it, so that it is seen to come from the sky. */
    public static final float HEIGHT = 48.0F;
    private static final EntityDataAccessor<Float> WIDTH = SynchedEntityData.defineId(SunShaftEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> RISE = SynchedEntityData.defineId(SunShaftEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STRIKE = SynchedEntityData.defineId(SunShaftEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LIFE = SynchedEntityData.defineId(SunShaftEntity.class, EntityDataSerializers.INT);
    /** 0: the sun's own light. 1: the light of the Sky Beneath, which is the colour of the hour after sunset. 2: the amber of the Founder's seals. */
    private static final EntityDataAccessor<Integer> HUE = SynchedEntityData.defineId(SunShaftEntity.class, EntityDataSerializers.INT);
    public static final int SUN = 0;
    public static final int DUSK = 1;
    public static final int AMBER = 2;
    /** Where it comes from, counted from the ground it stands on; all nothing for one that comes straight down from the sky. */
    private static final EntityDataAccessor<Float> FROM_X = SynchedEntityData.defineId(SunShaftEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> FROM_Y = SynchedEntityData.defineId(SunShaftEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> FROM_Z = SynchedEntityData.defineId(SunShaftEntity.class, EntityDataSerializers.FLOAT);

    public SunShaftEntity(EntityType<? extends SunShaftEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /** A shaft on {@code at}: as wide as {@code width} blocks across at the full, striking {@code strike} ticks from now. */
    public static SunShaftEntity shine(ServerLevel level, Vec3 at, float width, int rise, int strike, int life) {
        return make(level, at, null, width, rise, strike, life, SUN);
    }

    /** The same shaft in the light of the Sky Beneath: violet, for what the Coming looks at. */
    public static SunShaftEntity dusk(ServerLevel level, Vec3 at, float width, int rise, int strike, int life) {
        return make(level, at, null, width, rise, strike, life, DUSK);
    }

    /** And in the amber of the seals: what the works of the stopped clock strike with. */
    public static SunShaftEntity amber(ServerLevel level, Vec3 at, float width, int rise, int strike, int life) {
        return make(level, at, null, width, rise, strike, life, AMBER);
    }

    /** A ray from {@code from} to the ground at {@code at}, in the light {@code hue}. */
    public static SunShaftEntity ray(ServerLevel level, Vec3 at, Vec3 from, float width, int rise, int strike, int life, int hue) {
        return make(level, at, from, width, rise, strike, life, hue);
    }

    private static SunShaftEntity make(ServerLevel level, Vec3 at, Vec3 from, float width, int rise, int strike, int life, int hue) {
        SunShaftEntity shaft = new SunShaftEntity(Dushi.SUN_SHAFT.get(), level);
        shaft.setPos(at.x, at.y, at.z);
        shaft.entityData.set(WIDTH, width);
        shaft.entityData.set(RISE, Math.max(1, rise));
        shaft.entityData.set(STRIKE, strike);
        shaft.entityData.set(LIFE, Math.max(strike + 1, life));
        shaft.entityData.set(HUE, hue);
        if (from != null) {
            shaft.entityData.set(FROM_X, (float) (from.x - at.x));
            shaft.entityData.set(FROM_Y, (float) (from.y - at.y));
            shaft.entityData.set(FROM_Z, (float) (from.z - at.z));
        }
        level.addFreshEntity(shaft);
        return shaft;
    }

    /** Where it comes from, counted from where it stands; nothing at all if it comes down from the sky. */
    public float fromX() {
        return this.entityData.get(FROM_X);
    }

    public float fromY() {
        return this.entityData.get(FROM_Y);
    }

    public float fromZ() {
        return this.entityData.get(FROM_Z);
    }

    public int hue() {
        return this.entityData.get(HUE);
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
        builder.define(HUE, 0);
        builder.define(FROM_X, 0.0F);
        builder.define(FROM_Y, 0.0F);
        builder.define(FROM_Z, 0.0F);
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

    /** (only a summon gives it any: Width10 is its width in tenths of a block, FromX10.. where a ray comes from, in tenths too) */
    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.entityData.set(WIDTH, input.getIntOr("Width10", 10) / 10.0F);
        this.entityData.set(RISE, Math.max(1, input.getIntOr("Rise", 10)));
        this.entityData.set(STRIKE, input.getIntOr("Strike", 30));
        this.entityData.set(LIFE, input.getIntOr("Life", 40));
        this.entityData.set(HUE, input.getIntOr("Hue", 0));
        this.entityData.set(FROM_X, input.getIntOr("FromX10", 0) / 10.0F);
        this.entityData.set(FROM_Y, input.getIntOr("FromY10", 0) / 10.0F);
        this.entityData.set(FROM_Z, input.getIntOr("FromZ10", 0) / 10.0F);
    }
}
