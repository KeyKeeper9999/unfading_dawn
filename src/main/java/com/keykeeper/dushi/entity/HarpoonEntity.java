package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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

/** A harpoon on its way: thrown level by a harpooner of the wharf. Whoever it finds is hurt, thrown back and lamed a moment. */
public class HarpoonEntity extends ThrowableItemProjectile {
    private float damage = 14.0F;

    public HarpoonEntity(EntityType<? extends HarpoonEntity> type, Level level) {
        super(type, level);
    }

    public HarpoonEntity(Level level, LivingEntity owner, float damage) {
        super(Dushi.HARPOON_ENTITY.get(), owner, level, new ItemStack(Dushi.HARPOON.get()));
        this.damage = damage;
    }

    @Override
    protected Item getDefaultItem() {
        return Dushi.HARPOON.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.025;
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
                    living.knockback(0.5, -push.x, -push.z, source, this.damage);
                }
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 1, false, false, true));
            }
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (this.level() instanceof ServerLevel level) {
            Vec3 at = hitResult.getLocation();
            level.sendParticles(ParticleTypes.CRIT, at.x, at.y, at.z, 8, 0.15, 0.15, 0.15, 0.2);
            level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.harpoon.hit"), SoundSource.HOSTILE, 1.0F, 0.9F + level.getRandom().nextFloat() * 0.2F);
            this.discard();
        }
    }
}
