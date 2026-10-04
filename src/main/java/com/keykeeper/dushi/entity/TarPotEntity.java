package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** A thrown clay pot of burning tar: bursts into flame where it lands. */
public class TarPotEntity extends ThrowableItemProjectile {
    private static final double RADIUS = 2.6;
    private static final float DAMAGE = 7.0F;

    public TarPotEntity(EntityType<? extends TarPotEntity> type, Level level) {
        super(type, level);
    }

    public TarPotEntity(Level level, LivingEntity owner, ItemStack stack) {
        super(Dushi.TAR_POT_ENTITY.get(), owner, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return Dushi.TAR_POT.get();
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (this.level() instanceof ServerLevel level) {
            Vec3 at = hitResult.getLocation();
            Entity owner = this.getOwner();
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(RADIUS),
                    e -> e != owner && e.isAlive() && !(e instanceof ArmorStand))) {
                if (target.hurtServer(level, this.damageSources().thrown(this, owner), DAMAGE)) {
                    target.igniteForSeconds(5.0F);
                }
            }
            level.sendParticles(ParticleTypes.FLAME, at.x, at.y + 0.2, at.z, 50, 1.0, 0.4, 1.0, 0.05);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, at.x, at.y + 0.4, at.z, 14, 0.8, 0.4, 0.8, 0.02);
            level.playSound(null, at.x, at.y, at.z, SoundEvents.DECORATED_POT_SHATTER, SoundSource.NEUTRAL, 1.0F, 0.9F);
            level.playSound(null, at.x, at.y, at.z, Dushi.sound("item.spell.fire_burst"), SoundSource.NEUTRAL, 1.0F, 1.1F);
            this.discard();
        }
    }
}
