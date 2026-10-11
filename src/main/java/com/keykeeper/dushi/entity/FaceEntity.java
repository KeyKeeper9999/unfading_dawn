package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The disc of the Coming, as something that can be struck: the disc is on the end of a rope a dozen blocks long, far
 * from where the Coming stands, and a blow has to land where the disc is. Nothing is drawn of it (the model draws the
 * disc); the Coming puts it where its disc is every tick, and a blow on it is a blow on the Coming
 * ({@link ComingEntity#hurtFace}). It is not kept with the world: the Coming makes another.
 */
public class FaceEntity extends Entity {
    private ComingEntity owner;

    public FaceEntity(EntityType<? extends FaceEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static FaceEntity of(ServerLevel level, ComingEntity owner) {
        FaceEntity face = new FaceEntity(Dushi.COMING_FACE.get(), level);
        face.owner = owner;
        face.setPos(owner.getX(), owner.getY() + 6.0, owner.getZ());
        level.addFreshEntity(face);
        return face;
    }

    /** Whose disc it is: for what is done to a player who strikes it (a swing at it is a swing at the Coming). */
    public ComingEntity owner() {
        return this.owner;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && (this.owner == null || !this.owner.isAlive() || this.owner.isRemoved())) {
            this.discard();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return this.owner != null && this.owner.isAlive() && this.owner.hurtFace(level, source, damage);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
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
