package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The Helmsman's anchor, cast: it goes up and comes down like the iron it is, and where it comes down everything
 * within a couple of paces is struck and thrown. It is seen the whole way.
 */
public class AnchorEntity extends ThrowableItemProjectile {
    public static final double RADIUS = 2.8;
    private float damage = 20.0F;

    public AnchorEntity(EntityType<? extends AnchorEntity> type, Level level) {
        super(type, level);
    }

    public AnchorEntity(Level level, LivingEntity owner, float damage) {
        super(Dushi.ANCHOR_ENTITY.get(), owner, level, new ItemStack(Dushi.SHIP_ANCHOR.get()));
        this.damage = damage;
    }

    @Override
    protected Item getDefaultItem() {
        return Dushi.SHIP_ANCHOR.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.06;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return !(entity instanceof SoulsMob) && super.canHitEntity(entity);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 at = hitResult.getLocation();
        Entity owner = this.getOwner();
        net.minecraft.world.damagesource.DamageSource source = this.damageSources().thrown(this, owner);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(at, at).inflate(RADIUS + 1.0, 3.0, RADIUS + 1.0),
                e -> e != owner && e.isAlive() && !(e instanceof SoulsMob) && !(e instanceof ArmorStand) && !(e instanceof Player p && (p.isCreative() || p.isSpectator())))) {
            double far = Math.sqrt(victim.distanceToSqr(at.x, victim.getY(), at.z)) - victim.getBbWidth() * 0.5;
            if (far <= RADIUS && victim.hurtServer(level, source, this.damage)) {
                victim.knockback(1.0, at.x - victim.getX(), at.z - victim.getZ(), source, this.damage);
            }
        }
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SPRUCE_PLANKS.defaultBlockState()), at.x, at.y + 0.2, at.z, 50, 1.0, 0.3, 1.0, 0.2);
        level.sendParticles(ParticleTypes.POOF, at.x, at.y + 0.2, at.z, 12, 0.9, 0.1, 0.9, 0.03);
        level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.helmsman.anchor"), SoundSource.HOSTILE, 2.6F, 0.9F + level.getRandom().nextFloat() * 0.15F);
        this.discard();
    }
}
