package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * One of the scorched: a reaper who stayed in the open field. The sun that does not move has stood on him until there
 * was nothing left to dry, and then he took: a cinder in the shape of a man, split, and alight in the splits. He is
 * slow, and holds his arms off himself.
 * <p>
 * He reaches with both hands, and what he takes hold of burns. Whoever strikes him from within arm's length is burnt
 * for it. Crowded, he flares - the splits open and the fire in him comes out all round. And when he is killed he
 * goes up: there is the space of a breath to step back in.
 */
public class ScorchedEntity extends FieldhandEntity {
    public static final int GRASP = 1;
    public static final int GRASP_DURATION = 46;
    public static final int GRASP_IMPACT = 21;
    public static final int FLARE = 2;
    public static final int FLARE_DURATION = 56;
    public static final int FLARE_BURST = 28;
    public static final double REACH = 2.4;
    public static final double FLARE_RADIUS = 3.0;
    /** Ticks between his death and his going up. */
    public static final int PYRE = 12;

    private int flareCooldown = 80;
    private boolean wentUp;

    public ScorchedEntity(EntityType<? extends ScorchedEntity> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 250.0).add(Attributes.ATTACK_DAMAGE, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ARMOR, 2.0).add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    public int soulReward() {
        return 1400;
    }

    @Override
    protected int attackDuration(int id) {
        return id == GRASP ? GRASP_DURATION : FLARE_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == GRASP ? GRASP_IMPACT - 7 : 0;
    }

    @Override
    protected int attackPause(int finishedId) {
        return 20 + this.random.nextInt(16);
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance < 2.6 && this.flareCooldown <= 0) {
            this.flareCooldown = 200 + this.random.nextInt(100);
            return FLARE;
        }
        return distance < REACH - 0.5 ? GRASP : 0;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.flareCooldown = Math.max(0, this.flareCooldown - 1);
        if (this.tickCount % 6 == 0) {
            level.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.4, this.getZ(), 1, 0.25, 0.4, 0.25, 0.005);
        }
    }

    /** The fire in him, out all round: at his death, or when he is crowded. */
    private void burst(ServerLevel level, float damage) {
        Vec3 at = this.position();
        this.hitAround(level, at, FLARE_RADIUS, damage, 0.8, this.damageSources().mobAttack(this));
        FlourCloudEntity.kindle(level, at, FLARE_RADIUS);
        level.sendParticles(ParticleTypes.FLAME, at.x, at.y + 1.0, at.z, 70, 1.3, 0.8, 1.3, 0.1);
        level.sendParticles(ParticleTypes.LAVA, at.x, at.y + 1.0, at.z, 10, 0.8, 0.5, 0.8, 0.0);
        level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.scorched.flare"), this.getSoundSource(), 1.6F, 0.9F + this.random.nextFloat() * 0.2F);
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (id == GRASP) {
            if (time == GRASP_IMPACT) {
                this.hitArc(level, REACH, 50.0, damage, 0.3);
            }
        } else {
            if (time < FLARE_BURST && time % 2 == 0) {
                // the splits open: it is seen coming for more than a second
                level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.0, this.getZ(), 4 + time / 3, 0.3, 0.6, 0.3, 0.01);
                level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.6, this.getZ(), 1, 0.3, 0.3, 0.3, 0.01);
            }
            if (time == 4) {
                this.playSound(Dushi.sound("entity.scorched.ambient"), 1.6F, 1.5F);
            }
            if (time == FLARE_BURST) {
                this.burst(level, damage * 1.1F);
            }
        }
    }

    @Override
    protected void struck(ServerLevel level, LivingEntity victim) {
        victim.igniteForSeconds(4.0F);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.is(DamageTypeTags.IS_FIRE)) {
            return false;
        }
        boolean hurt = super.hurtServer(level, source, damage);
        // a blow from within arm's length is paid for
        if (hurt && source.getDirectEntity() instanceof LivingEntity attacker && attacker == source.getEntity() && this.isFoe(attacker)
                && attacker.distanceToSqr(this) < 3.4 * 3.4) {
            attacker.igniteForSeconds(3.0F);
        }
        return hurt;
    }

    @Override
    protected void tickDeath() {
        super.tickDeath();
        if (this.level() instanceof ServerLevel level) {
            if (this.deathTime < PYRE) {
                level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 0.8, this.getZ(), 8, 0.3, 0.5, 0.3, 0.02);
            } else if (!this.wentUp) {
                this.wentUp = true;
                this.burst(level, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.9F);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.scorched.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.scorched.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.scorched.death");
    }
}
