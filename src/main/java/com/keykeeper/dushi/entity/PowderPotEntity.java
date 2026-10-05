package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A quarryman's pot of blasting powder with a lit match in its neck: it goes off where it lands. It breaks no blocks, and
 * it does not care whose side those near it are on - the dead of the quarry are hurt by their own sappers.
 */
public class PowderPotEntity extends ThrowableItemProjectile {
    private static final double RADIUS = 3.2;
    private static final float DAMAGE = 10.0F;

    public PowderPotEntity(EntityType<? extends PowderPotEntity> type, Level level) {
        super(type, level);
    }

    public PowderPotEntity(Level level, LivingEntity owner, ItemStack stack) {
        super(Dushi.POWDER_POT_ENTITY.get(), owner, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return Dushi.POWDER_POT.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel level && this.tickCount % 2 == 0) {
            level.sendParticles(ParticleTypes.SMALL_FLAME, this.getX(), this.getY() + 0.2, this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (this.level() instanceof ServerLevel level) {
            blast(level, this, this.getOwner() instanceof LivingEntity owner ? owner : null, hitResult.getLocation(), RADIUS, DAMAGE);
            this.discard();
        }
    }

    /**
     * Powder going off at a point: everything living within {@code radius} is hurt (less towards the edge) and thrown
     * back, except {@code direct} and {@code owner}. The dead take less of it from one of their own.
     */
    public static void blast(ServerLevel level, Entity direct, LivingEntity owner, Vec3 at, double radius, float damage) {
        DamageSource source = level.damageSources().explosion(direct, owner != null ? owner : direct);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(radius + 1.0),
                e -> e != owner && e != direct && e.isAlive() && !(e instanceof ArmorStand) && !(e instanceof Player p && (p.isCreative() || p.isSpectator())))) {
            Vec3 centre = victim.getBoundingBox().getCenter();
            double distance = Math.max(0.0, centre.distanceTo(at) - victim.getBbWidth() * 0.5);
            if (distance > radius) {
                continue;
            }
            float dealt = damage * (float) (1.0 - 0.5 * distance / radius);
            if (victim instanceof SoulsMob && !(owner instanceof Player)) {
                dealt *= 0.6F;
            }
            if (victim.hurtServer(level, source, dealt)) {
                Vec3 away = new Vec3(centre.x - at.x, 0.0, centre.z - at.z);
                away = away.lengthSqr() < 1.0E-4 ? Vec3.ZERO : away.normalize().scale(0.9 * (1.0 - 0.5 * distance / radius));
                SoulsMob.shove(victim, victim.getDeltaMovement().add(away.x, 0.32, away.z));
            }
        }
        level.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y + 0.5, at.z, 5, radius * 0.3, 0.4, radius * 0.3, 0.0);
        level.sendParticles(ParticleTypes.FLAME, at.x, at.y + 0.4, at.z, 40, radius * 0.25, 0.4, radius * 0.25, 0.12);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, at.x, at.y + 0.6, at.z, 24, radius * 0.3, 0.5, radius * 0.3, 0.04);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.CALCITE.defaultBlockState()), at.x, at.y + 0.2, at.z, 30, radius * 0.3, 0.3,
                radius * 0.3, 0.3);
        level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.quarry.blast"), SoundSource.HOSTILE, 3.0F, 0.9F + level.getRandom().nextFloat() * 0.2F);
    }
}
