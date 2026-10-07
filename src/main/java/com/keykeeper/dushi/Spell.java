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
    /** A short tongue of flame straight from the hand: three paces and a half, and round a shield. */
    FLARE("flare", SpellSchool.PYROMANCY, 16, 12.0F, 16, 0),
    /** The quarry's scholars': five shards in a fan. Up close all of them land on one; further off they pass either side. */
    SOUL_SCATTER("soul_scatter", SpellSchool.SORCERY, 14, 14.0F, 22, 0),
    /** The Mire's: a root comes up under whoever the caster looks at - struck, thrown, and held a moment. */
    ROOT_SNARE("root_snare", SpellSchool.MIRACLE, 8, 14.0F, 30, 0),
    /** The Mire's: a slow green flame that bends its way to whatever is near it and burns on it a long time. */
    MARSH_FIRE("marsh_fire", SpellSchool.PYROMANCY, 10, 12.0F, 24, 0),
    /** The Firwood's: hot wax from the palm. It burns little; it sets on the legs. */
    HOT_WAX("hot_wax", SpellSchool.PYROMANCY, 12, 12.0F, 20, 0);

    /** How far the flare reaches, measured to the nearest of its prey, not to the middle of it. */
    public static final double FLARE_REACH = 3.5;
    public static final double WAX_REACH = 4.5;
    /** How far off somebody can be picked out for a root. */
    public static final double SNARE_RANGE = 14.0;
    public static final int SNARE_WARNING = 12;

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
                level.sendParticles(net.minecraft.core.particles.ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFFFFF), caster.getX(), caster.getY() + 1.0, caster.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                sound(level, caster, "item.spell.rebuke", 1.2F, 1.0F);
            }
            case FIRE_ORB -> {
                SpellBoltEntity.shoot(level, caster, SpellBoltEntity.Kind.FIRE_ORB, look.add(0.0, 0.12, 0.0), 1.05F, 9.0F * power);
                sound(level, caster, "item.spell.fire", 1.0F, 1.0F);
            }
            case FLARE -> {
                DamageSource source = caster.damageSources().indirectMagic(caster, caster);
                for (LivingEntity target : inJet(level, caster, FLARE_REACH)) {
                    lick(target);
                    target.hurtServer(level, source, 8.0F * power);
                    target.igniteForSeconds(4.0F);
                }
                for (int i = 1; i <= 7; i++) {
                    Vec3 at = eye.add(look.scale(0.5 * i));
                    level.sendParticles(ParticleTypes.FLAME, at.x, at.y - 0.2, at.z, 6, 0.08 * i, 0.08 * i, 0.08 * i, 0.02);
                }
                sound(level, caster, "item.spell.fire_burst", 1.0F, 1.2F);
            }
            case SOUL_SCATTER -> {
                Vec3 side = look.cross(new Vec3(0.0, 1.0, 0.0));
                side = side.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
                for (int i = -2; i <= 2; i++) {
                    SpellBoltEntity.shoot(level, caster, SpellBoltEntity.Kind.SHARD, look.add(side.scale(0.13 * i)), 1.7F, 5.0F * power);
                }
                sound(level, caster, "item.spell.shard", 1.0F, 1.4F);
            }
            case ROOT_SNARE -> {
                LivingEntity aimed = aimed(level, caster, SNARE_RANGE);
                Vec3 want = aimed != null ? aimed.position() : eye.add(look.scale(5.0));
                Vec3 ground = com.keykeeper.dushi.entity.RootEntity.ground(level, want.x, want.y, want.z);
                if (ground != null) {
                    com.keykeeper.dushi.entity.RootEntity.rise(level, caster, ground, SNARE_WARNING, 11.0F * power);
                }
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, caster.getX(), caster.getY() + 0.2, caster.getZ(), 12, 0.5, 0.1, 0.5, 0.0);
                sound(level, caster, "item.spell.mend", 1.0F, 0.7F);
            }
            case MARSH_FIRE -> {
                SpellBoltEntity.shoot(level, caster, SpellBoltEntity.Kind.WISP_FLAME, look, 0.55F, 7.0F * power);
                sound(level, caster, "item.spell.fire", 1.0F, 0.7F);
            }
            case HOT_WAX -> {
                DamageSource source = caster.damageSources().indirectMagic(caster, caster);
                for (LivingEntity target : inJet(level, caster, WAX_REACH)) {
                    lick(target);
                    target.hurtServer(level, source, 5.0F * power);
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 90, 3, false, true, true), caster);
                }
                for (int i = 1; i <= 9; i++) {
                    Vec3 at = eye.add(look.scale(0.5 * i));
                    level.sendParticles(ParticleTypes.CLOUD, at.x, at.y - 0.2 - 0.02 * i * i, at.z, 3, 0.05 * i, 0.05 * i, 0.05 * i, 0.01);
                    level.sendParticles(ParticleTypes.SMALL_FLAME, at.x, at.y - 0.2 - 0.02 * i * i, at.z, 1, 0.05 * i, 0.05 * i, 0.05 * i, 0.0);
                }
                sound(level, caster, "item.spell.fire", 1.0F, 1.5F);
            }
        }
    }

    private static List<LivingEntity> nearby(ServerLevel level, Player caster, double radius) {
        return level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(radius),
                e -> e != caster && e.isAlive() && !(e instanceof ArmorStand) && e.distanceToSqr(caster) <= radius * radius
                        && !(e instanceof Player other && !caster.canHarmPlayer(other)));
    }

    /**
     * Whether a jet from {@code eye} along {@code look}, {@code reach} long and widening as it goes, touches {@code box}:
     * asked of the nearest point of the box at every quarter of a pace along the jet, so that a thing as wide as a wagon
     * is touched where it begins and not where its middle is.
     */
    public static boolean jetTouches(Vec3 eye, Vec3 look, double reach, net.minecraft.world.phys.AABB box) {
        for (double along = 0.0; along <= reach + 1.0E-6; along += 0.25) {
            Vec3 at = eye.add(look.scale(along));
            // from this place on the jet to the nearest place of the box: how far ahead or behind that is, and how far aside
            Vec3 to = new Vec3(Math.max(box.minX, Math.min(at.x, box.maxX)) - at.x, Math.max(box.minY, Math.min(at.y, box.maxY)) - at.y,
                    Math.max(box.minZ, Math.min(at.z, box.maxZ)) - at.z);
            double ahead = to.dot(look);
            double aside = to.subtract(look.scale(ahead)).length();
            if (Math.abs(ahead) <= 0.25 && aside <= 0.35 + 0.3 * along) {
                return true;                                     // (so the jet ends where it is said to, however wide it has grown)
            }
        }
        return false;
    }

    private static List<LivingEntity> inJet(ServerLevel level, Player caster, double reach) {
        Vec3 eye = caster.getEyePosition().add(0.0, -0.2, 0.0);
        Vec3 look = caster.getLookAngle();
        return level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(reach + 2.0),
                e -> e != caster && e.isAlive() && !(e instanceof ArmorStand) && !(e instanceof Player other && !caster.canHarmPlayer(other))
                        && jetTouches(eye, look, reach, e.getBoundingBox()));
    }

    /** What pours out of a palm goes round a shield, and is not put off by a blow that landed a moment ago. */
    private static void lick(LivingEntity target) {
        if (target.isBlocking()) {
            target.stopUsingItem();
        }
        target.damageCooldownTime = 0;                           // (the game's moment of grace after any blow: not setInvulnerableTime, which is another thing)
    }

    /** Whoever the caster is looking at: the living thing nearest to the line of their eyes, within {@code range}. */
    public static LivingEntity aimed(ServerLevel level, Player caster, double range) {
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        LivingEntity best = null;
        double bestOff = Double.MAX_VALUE;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(range),
                c -> c != caster && c.isAlive() && !(c instanceof ArmorStand) && !(c instanceof Player other && !caster.canHarmPlayer(other)))) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            double along = to.dot(look);
            if (along < 0.5 || along > range) {
                continue;
            }
            double off = to.subtract(look.scale(along)).length() - e.getBbWidth() * 0.5;
            if (off < 1.5 + along * 0.12 && off < bestOff) {
                bestOff = off;
                best = e;
            }
        }
        return best;
    }

    private static void sound(ServerLevel level, Player caster, String name, float volume, float pitch) {
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), Dushi.sound(name), SoundSource.PLAYERS, volume,
                pitch * (0.95F + level.getRandom().nextFloat() * 0.1F));
    }
}
