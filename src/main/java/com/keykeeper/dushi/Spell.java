package com.keykeeper.dushi;

import java.util.List;

import com.keykeeper.dushi.entity.SpellBoltEntity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Every spell a player can learn. Each has a limited number of casts that only a hearth restores. */
public enum Spell {
    /** Fast bolt of pale blue light. */
    SOUL_SHARD("soul_shard", SpellSchool.SORCERY, 30, 8.0F, 14, 0),
    /** Slow and heavy: twice the wound. */
    HEAVY_SOUL_SHARD("heavy_soul_shard", SpellSchool.SORCERY, 12, 14.0F, 28, 0),
    /** Heals after a short prayer. */
    MEND("mend", SpellSchool.MIRACLE, 5, 12.0F, 20, 36),
    /** A burst of force that throws everything nearby back. */
    REBUKE("rebuke", SpellSchool.MIRACLE, 20, 16.0F, 26, 0),
    /** A lobbed ball of fire that bursts where it lands. */
    FIRE_ORB("fire_orb", SpellSchool.PYROMANCY, 8, 14.0F, 26, 0),
    /** A short tongue of flame straight from the hand. */
    FLARE("flare", SpellSchool.PYROMANCY, 16, 12.0F, 16, 0);

    public final String id;
    public final SpellSchool school;
    public final int casts;
    public final float stamina;
    public final int cooldown;
    /** Ticks the caster must hold still before the spell goes off; 0 for instant spells. */
    public final int channel;

    Spell(String id, SpellSchool school, int casts, float stamina, int cooldown, int channel) {
        this.id = id;
        this.school = school;
        this.casts = casts;
        this.stamina = stamina;
        this.cooldown = cooldown;
        this.channel = channel;
    }

    /** Server side: the spell takes effect. {@code power} is 1 for an untrained caster. */
    public void cast(ServerLevel level, Player caster, float power) {
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        switch (this) {
            case SOUL_SHARD -> {
                SpellBoltEntity.shoot(level, caster, SpellBoltEntity.Kind.SHARD, look, 1.9F, 6.0F * power);
                sound(level, caster, "item.spell.shard", 1.0F, 1.15F);
            }
            case HEAVY_SOUL_SHARD -> {
                SpellBoltEntity.shoot(level, caster, SpellBoltEntity.Kind.HEAVY_SHARD, look, 1.3F, 13.0F * power);
                sound(level, caster, "item.spell.shard", 1.0F, 0.7F);
            }
            case MEND -> {
                caster.heal(8.0F + 4.0F * power);
                caster.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, caster.getX(), caster.getY() + 1.0, caster.getZ(), 24, 0.5, 0.8, 0.5, 0.0);
                level.sendParticles(ParticleTypes.END_ROD, caster.getX(), caster.getY() + 0.2, caster.getZ(), 16, 0.6, 0.1, 0.6, 0.02);
                sound(level, caster, "item.spell.mend", 1.0F, 1.0F);
            }
            case REBUKE -> {
                DamageSource source = caster.damageSources().indirectMagic(caster, caster);
                for (LivingEntity target : nearby(level, caster, 4.5)) {
                    target.hurtServer(level, source, 3.0F * power);
                    target.knockback(1.6, caster.getX() - target.getX(), caster.getZ() - target.getZ(), source, 3.0F);
                }
                for (int i = 0; i < 28; i++) {
                    double angle = i * Math.PI * 2.0 / 28.0;
                    level.sendParticles(ParticleTypes.CLOUD, caster.getX() + Math.cos(angle) * 1.2, caster.getY() + 1.0, caster.getZ() + Math.sin(angle) * 1.2,
                            0, Math.cos(angle), 0.05, Math.sin(angle), 0.45);
                }
                level.sendParticles(ParticleTypes.FLASH, caster.getX(), caster.getY() + 1.0, caster.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                sound(level, caster, "item.spell.rebuke", 1.2F, 1.0F);
            }
            case FIRE_ORB -> {
                SpellBoltEntity.shoot(level, caster, SpellBoltEntity.Kind.FIRE_ORB, look.add(0.0, 0.12, 0.0), 1.05F, 9.0F * power);
                sound(level, caster, "item.spell.fire", 1.0F, 1.0F);
            }
            case FLARE -> {
                DamageSource source = caster.damageSources().indirectMagic(caster, caster);
                for (LivingEntity target : nearby(level, caster, 4.0)) {
                    Vec3 to = target.getBoundingBox().getCenter().subtract(eye);
                    if (to.length() < 1.2 || to.normalize().dot(look) > 0.6) {
                        target.hurtServer(level, source, 8.0F * power);
                        target.igniteForSeconds(4.0F);
                    }
                }
                for (int i = 1; i <= 7; i++) {
                    Vec3 at = eye.add(look.scale(0.5 * i));
                    level.sendParticles(ParticleTypes.FLAME, at.x, at.y - 0.2, at.z, 6, 0.08 * i, 0.08 * i, 0.08 * i, 0.02);
                }
                sound(level, caster, "item.spell.fire_burst", 1.0F, 1.2F);
            }
        }
    }

    private static List<LivingEntity> nearby(ServerLevel level, Player caster, double radius) {
        return level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(radius),
                e -> e != caster && e.isAlive() && !(e instanceof ArmorStand) && e.distanceToSqr(caster) <= radius * radius
                        && !(e instanceof Player other && !caster.canHarmPlayer(other)));
    }

    private static void sound(ServerLevel level, Player caster, String name, float volume, float pitch) {
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), Dushi.sound(name), SoundSource.PLAYERS, volume,
                pitch * (0.95F + level.getRandom().nextFloat() * 0.1F));
    }
}
