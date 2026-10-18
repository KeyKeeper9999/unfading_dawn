package com.keykeeper.dushi.entity;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * What stands up out of the sea when the bell is struck: about three hundred blocks high, farther from the tower than the game
 * will draw an entity or send one to a client, and for the looking only - it cannot be struck, touched or hurt, and it strikes
 * nobody. So it exists on the client alone ({@code client/Cinema} makes it when the server says: {@code Net.Colossus}); the
 * server never has one. It does not tick on any clock of its own: its age is read off the level's game time (so that a client
 * that comes late finds it standing), and {@link #begun} is the game time it began to rise at.
 */
public class ColossusEntity extends Entity {
    /** The tick, after it begins to rise, by which it stands (ColossusModel's animation, tools/gen_colossus.py). */
    public static final int RISE = 560;

    private long begun;

    public ColossusEntity(EntityType<? extends ColossusEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /** The game time at which it began to rise (or will: in the future). */
    public void begin(long gameTime) {
        this.begun = gameTime;
    }

    public long begun() {
        return this.begun;
    }

    /** Ticks since it began to rise, with the partial tick added: negative before. */
    public float age(float partialTicks) {
        return this.level().getGameTime() - this.begun + partialTicks;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
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

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }
}
