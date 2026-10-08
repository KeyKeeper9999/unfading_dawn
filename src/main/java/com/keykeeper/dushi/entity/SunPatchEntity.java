package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
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
 * A patch of the Stubble's sun, called down by somebody who learnt it from a sungazer: light gathers on the ground
 * where they looked, brightens for a second, and burns whatever is still standing in it. It is seen coming, by the one
 * it is meant for as well.
 */
public class SunPatchEntity extends Entity {
    public static final double RADIUS = 2.2;
    public static final int GATHERS = 22;

    private LivingEntity caster;
    private float damage;
    private int age;

    public SunPatchEntity(EntityType<? extends SunPatchEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static SunPatchEntity call(ServerLevel level, LivingEntity caster, Vec3 at, float damage) {
        SunPatchEntity patch = new SunPatchEntity(Dushi.SUN_PATCH.get(), level);
        patch.caster = caster;
        patch.damage = damage;
        patch.setPos(at.x, at.y, at.z);
        level.addFreshEntity(patch);
        level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.sun.gather"), SoundSource.PLAYERS, 1.2F, 1.3F);
        return patch;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        this.age++;
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        if (this.age < GATHERS) {
            double bright = this.age / (double) GATHERS;
            level.sendParticles(ParticleTypes.END_ROD, x, y + 0.1, z, 1 + (int) (bright * 7), RADIUS * 0.4, 0.02, RADIUS * 0.4, 0.0);
            if (this.age % 3 == 0) {
                for (int i = 0; i < 10; i++) {
                    double angle = i * Math.PI / 5.0 + this.age * 0.07;
                    level.sendParticles(this.age > GATHERS - 8 ? ParticleTypes.FLAME : ParticleTypes.SMALL_FLAME, x + Math.cos(angle) * RADIUS, y + 0.1,
                            z + Math.sin(angle) * RADIUS, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
            return;
        }
        DamageSource source = this.damageSources().indirectMagic(this, this.caster);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(x, y, z, x, y, z).inflate(RADIUS + 1.0, 3.0, RADIUS + 1.0),
                e -> e != this.caster && e.isAlive() && !(e instanceof ArmorStand))) {
            double far = Math.sqrt(victim.distanceToSqr(x, victim.getY(), z)) - victim.getBbWidth() * 0.5;
            if (far > RADIUS || (this.caster instanceof Player player && victim instanceof Player other && !player.canHarmPlayer(other))) {
                continue;
            }
            if (victim.hurtServer(level, source, this.damage) && !victim.fireImmune()) {
                victim.igniteForSeconds(4.0F);
            }
        }
        FlourCloudEntity.kindle(level, this.position(), RADIUS);
        level.sendParticles(ParticleTypes.FLAME, x, y + 0.3, z, 60, RADIUS * 0.5, 0.3, RADIUS * 0.5, 0.06);
        level.sendParticles(net.minecraft.core.particles.ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFF0C0), x, y + 0.5, z, 1, 0.0, 0.0, 0.0, 0.0);
        level.playSound(null, x, y, z, Dushi.sound("entity.sun.burn"), SoundSource.PLAYERS, 1.4F, 1.0F + level.getRandom().nextFloat() * 0.2F);
        this.discard();
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
