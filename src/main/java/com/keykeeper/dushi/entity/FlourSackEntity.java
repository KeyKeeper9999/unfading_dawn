package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** A small sack of flour, thrown: it bursts where it lands and leaves its flour hanging there (see {@link FlourCloudEntity}). */
public class FlourSackEntity extends ThrowableItemProjectile {
    private float blast = 24.0F;

    public FlourSackEntity(EntityType<? extends FlourSackEntity> type, Level level) {
        super(type, level);
    }

    /** Thrown by a player: see {@link com.keykeeper.dushi.item.FlourSackItem}. */
    public FlourSackEntity(Level level, LivingEntity owner, ItemStack stack) {
        super(Dushi.FLOUR_SACK_ENTITY.get(), owner, level, stack);
    }

    public FlourSackEntity(Level level, LivingEntity owner, float blast) {
        super(Dushi.FLOUR_SACK_ENTITY.get(), owner, level, new ItemStack(Dushi.FLOUR_SACK.get()));
        this.blast = blast;
    }

    @Override
    protected Item getDefaultItem() {
        return Dushi.FLOUR_SACK.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return !(entity instanceof SoulsMob && this.getOwner() instanceof SoulsMob) && super.canHitEntity(entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level() instanceof ServerLevel level) {
            result.getEntity().hurtServer(level, this.damageSources().thrown(this, this.getOwner()), 6.0F);
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (this.level() instanceof ServerLevel level) {
            FlourCloudEntity.burst(level, this.getOwner() instanceof LivingEntity living ? living : null, hitResult.getLocation(), this.blast);
            this.discard();
        }
    }
}
