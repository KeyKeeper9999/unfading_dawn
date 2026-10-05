package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** An iron splitting wedge thrown by the Taskmaster: a pound of iron, flat and fast. */
public class WedgeEntity extends ThrowableItemProjectile {
    private float damage = 9.0F;

    public WedgeEntity(EntityType<? extends WedgeEntity> type, Level level) {
        super(type, level);
    }

    public WedgeEntity(Level level, LivingEntity owner, float damage) {
        super(Dushi.WEDGE_ENTITY.get(), owner, level, new ItemStack(Dushi.IRON_WEDGE.get()));
        this.damage = damage;
    }

    @Override
    protected Item getDefaultItem() {
        return Dushi.IRON_WEDGE.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.02;
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        Entity victim = hit.getEntity();
        if (this.level() instanceof ServerLevel level && !(victim instanceof SoulsMob)) {
            net.minecraft.world.damagesource.DamageSource source = this.damageSources().thrown(this, this.getOwner());
            if (victim.hurtServer(level, source, this.damage) && victim instanceof LivingEntity living) {
                Vec3 push = this.getDeltaMovement().multiply(1.0, 0.0, 1.0);
                if (push.lengthSqr() > 1.0E-4) {
                    living.knockback(0.6, -push.x, -push.z, source, this.damage);
                }
            }
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (this.level() instanceof ServerLevel level) {
            Vec3 at = hitResult.getLocation();
            level.sendParticles(ParticleTypes.CRIT, at.x, at.y, at.z, 8, 0.15, 0.15, 0.15, 0.2);
            level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.taskmaster.wedge"), SoundSource.HOSTILE, 0.9F, 1.5F);
            this.discard();
        }
    }
}
