package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Flour hanging in the air where a sack burst: whoever stands in it sees nothing; and flour in the air is as good as
 * powder. Anything alight that comes into it - somebody burning, a spell of fire, a miller's lantern - sets it off: it
 * sparkles for the time it takes to get out of it, and goes up. One cloud sets off the next.
 */
public class FlourCloudEntity extends Entity {
    public static final double RADIUS = 2.6;
    public static final double BLAST = 3.6;
    public static final int LIFE = 220;
    public static final int FUSE = 14;

    private LivingEntity owner;
    private float damage = 24.0F;
    private int age;
    private int fuse = -1;

    public FlourCloudEntity(EntityType<? extends FlourCloudEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /** A cloud at {@code at}; {@code owner} (who may be null) is whose blow it is when it goes up. */
    public static FlourCloudEntity burst(ServerLevel level, LivingEntity owner, Vec3 at, float damage) {
        FlourCloudEntity cloud = new FlourCloudEntity(Dushi.FLOUR_CLOUD.get(), level);
        cloud.owner = owner;
        cloud.damage = damage;
        cloud.setPos(at.x, at.y, at.z);
        level.addFreshEntity(cloud);
        level.sendParticles(ParticleTypes.CLOUD, at.x, at.y + 0.8, at.z, 40, RADIUS * 0.4, 0.5, RADIUS * 0.4, 0.06);
        level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.flour.puff"), SoundSource.HOSTILE, 1.2F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        return cloud;
    }

    /** Fire at {@code at}: every cloud that reaches within {@code reach} of it is set off. Returns how many were. */
    public static int kindle(ServerLevel level, Vec3 at, double reach) {
        int lit = 0;
        for (FlourCloudEntity cloud : level.getEntitiesOfClass(FlourCloudEntity.class, new AABB(at, at).inflate(RADIUS + reach + 1.0), Entity::isAlive)) {
            if (cloud.fuse < 0 && cloud.position().distanceTo(at) <= RADIUS + reach) {
                cloud.light();
                lit++;
            }
        }
        return lit;
    }

    public boolean lit() {
        return this.fuse >= 0;
    }

    public void light() {
        if (this.fuse < 0) {
            this.fuse = FUSE;
            this.playSound(Dushi.sound("item.spell.fire"), 1.0F, 1.6F);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    private boolean within(Entity entity) {
        double dx = entity.getX() - this.getX();
        double dz = entity.getZ() - this.getZ();
        return dx * dx + dz * dz <= RADIUS * RADIUS && entity.getY() > this.getY() - 2.0 && entity.getY() < this.getY() + 3.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        this.age++;
        if (this.age % 2 == 0) {
            level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 1.0, this.getZ(), 5, RADIUS * 0.45, 0.6, RADIUS * 0.45, 0.005);
            level.sendParticles(ParticleTypes.WHITE_ASH, this.getX(), this.getY() + 1.0, this.getZ(), 8, RADIUS * 0.5, 0.8, RADIUS * 0.5, 0.0);
        }
        if (this.fuse < 0) {
            for (LivingEntity inside : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(RADIUS, 2.0, RADIUS), this::within)) {
                if (inside.isOnFire()) {
                    this.light();
                } else if (inside instanceof Player player && !player.isCreative() && !player.isSpectator()) {
                    player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, false, true));
                }
            }
            if (this.age >= LIFE) {
                this.discard();
            }
            return;
        }
        level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.0, this.getZ(), 10, RADIUS * 0.45, 0.6, RADIUS * 0.45, 0.02);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 1.0, this.getZ(), 6, RADIUS * 0.45, 0.6, RADIUS * 0.45, 0.1);
        if (--this.fuse <= 0) {
            this.blast(level);
        }
    }

    private void blast(ServerLevel level) {
        Vec3 at = this.position();
        DamageSource source = level.damageSources().explosion(this, this.owner != null ? this.owner : this);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(BLAST + 1.0, 3.0, BLAST + 1.0),
                e -> e.isAlive() && !(e instanceof ArmorStand) && !(e instanceof SoulsMob mob && mob.isBoss())
                        && !(e instanceof Player p && (p.isCreative() || p.isSpectator())))) {
            double far = Math.sqrt(victim.distanceToSqr(at.x, victim.getY(), at.z)) - victim.getBbWidth() * 0.5;
            if (far <= BLAST && victim.hurtServer(level, source, this.damage)) {
                victim.igniteForSeconds(3.0F);
                victim.knockback(1.2 * com.keykeeper.dushi.Gear.thrown(victim), at.x - victim.getX(), at.z - victim.getZ(), source, this.damage);
            }
        }
        level.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y + 1.0, at.z, 6, 1.4, 0.6, 1.4, 0.0);
        level.sendParticles(ParticleTypes.FLAME, at.x, at.y + 1.0, at.z, 80, 1.8, 0.8, 1.8, 0.12);
        level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.flour.blast"), SoundSource.HOSTILE, 2.4F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        this.discard();
        kindle(level, at, BLAST);
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
