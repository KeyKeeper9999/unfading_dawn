package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.block.SludgeBlock;

import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * What the Wallower spits: a clot of sludge the size of a man's head, lobbed high. Where it lands it bursts into a pool
 * (see {@link SludgeBlock}); whoever it lands on is hurt a little and stands in the pool.
 */
public class MudGlobEntity extends ThrowableItemProjectile {
    private static final double POOL = 2.4;
    private float damage = 6.0F;

    public MudGlobEntity(EntityType<? extends MudGlobEntity> type, Level level) {
        super(type, level);
    }

    public MudGlobEntity(Level level, LivingEntity owner, float damage) {
        super(Dushi.MUD_GLOB_ENTITY.get(), owner, level, new ItemStack(Dushi.MUD_GLOB.get()));
        this.damage = damage;
    }

    @Override
    protected Item getDefaultItem() {
        return Dushi.MUD_GLOB.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel level && this.tickCount % 2 == 0) {
            level.sendParticles(ParticleTypes.ITEM_SLIME, this.getX(), this.getY() + 0.1, this.getZ(), 1, 0.05, 0.05, 0.05, 0.0);
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return !(entity instanceof SoulsMob) && super.canHitEntity(entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (this.level() instanceof ServerLevel level) {
            hitResult.getEntity().hurtServer(level, this.damageSources().thrown(this, this.getOwner()), this.damage);
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (this.level() instanceof ServerLevel level) {
            Vec3 at = hitResult.getLocation();
            SludgeBlock.spill(level, at, POOL);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState()), at.x, at.y + 0.2, at.z, 40, POOL * 0.35, 0.3, POOL * 0.35, 0.2);
            level.sendParticles(ParticleTypes.ITEM_SLIME, at.x, at.y + 0.3, at.z, 16, POOL * 0.3, 0.2, POOL * 0.3, 0.0);
            level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.wallower.plop"), SoundSource.HOSTILE, 1.6F, 0.85F + level.getRandom().nextFloat() * 0.3F);
            this.discard();
        }
    }
}
