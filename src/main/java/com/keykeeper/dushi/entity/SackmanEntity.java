package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A sackman of the mills: white from cap to shins, bowed under a sack he is bound to and has never set down, because
 * there is nowhere it is wanted - the flour is ground and no bread is baked of it. He has smaller sacks to spare.
 * <p>
 * From a way off he throws one: it bursts and its flour hangs where it burst, blinding, and waiting for a flame (see
 * {@link FlourCloudEntity}). Near, he swings one round by the neck.
 */
public class SackmanEntity extends FieldhandEntity {
    public static final int THROW = 1;
    public static final int THROW_DURATION = 46;
    public static final int THROW_RELEASE = 22;
    /** The tick by which he has another in his hand. */
    public static final int THROW_REARMED = 38;
    public static final int CLUB = 2;
    public static final int CLUB_DURATION = 48;
    public static final int CLUB_IMPACT = 22;
    public static final double REACH = 2.7;

    private int throwCooldown = 30;

    public SackmanEntity(EntityType<? extends SackmanEntity> type, Level level) {
        super(type, level);
        this.xpReward = 18;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 270.0).add(Attributes.ATTACK_DAMAGE, 22.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22).add(Attributes.ARMOR, 4.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.4)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    public int soulReward() {
        return 1300;
    }

    @Override
    protected int attackDuration(int id) {
        return id == THROW ? THROW_DURATION : CLUB_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == THROW ? THROW_RELEASE - 3 : CLUB_IMPACT - 8;
    }

    @Override
    protected int attackPause(int finishedId) {
        return 18 + this.random.nextInt(14);
    }

    @Override
    public double stopDistance() {
        return this.throwCooldown <= 20 ? 7.0 : 1.6;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance > 4.0 && distance < 15.0 && this.throwCooldown <= 0 && this.hasLineOfSight(target)) {
            this.throwCooldown = 110 + this.random.nextInt(60);
            return THROW;
        }
        return distance < REACH - 0.5 ? CLUB : 0;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.throwCooldown = Math.max(0, this.throwCooldown - 1);
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (id == THROW) {
            if (time == THROW_RELEASE) {
                LivingEntity target = this.getTarget();
                FlourSackEntity sack = new FlourSackEntity(level, this, damage * 1.1F);
                sack.setPos(this.getX(), this.getY() + this.getBbHeight() * 0.85, this.getZ());
                Vec3 to = target != null ? target.position().subtract(sack.position()) : this.facing().scale(7.0).add(0.0, -1.5, 0.0);
                // lobbed: it is in the air long enough to be seen coming (a hundredth of a thrown thing's speed goes every tick)
                double flight = Math.min(30.0, 16.0 + to.horizontalDistance() * 0.7);
                sack.setDeltaMovement(to.x / flight * 1.12, (to.y + 0.5 * 0.05 * flight * flight) / flight * 1.05, to.z / flight * 1.12);
                level.addFreshEntity(sack);
                this.playSound(Dushi.sound("entity.fieldhand.sack"), 1.0F, 1.2F);
            }
        } else if (time == CLUB_IMPACT) {
            this.playSound(Dushi.sound("entity.fieldhand.sack"), 1.3F, 0.8F);
            this.hitArc(level, REACH, 80.0, damage, 1.4);
            Vec3 at = this.position().add(this.facing().scale(1.8));
            level.sendParticles(ParticleTypes.CLOUD, at.x, at.y + 1.1, at.z, 16, 0.5, 0.4, 0.5, 0.03);
        }
    }

    @Override
    protected void struck(ServerLevel level, LivingEntity victim) {
        if (victim instanceof Player) {
            victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, false, false, true));     // a faceful of it
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.leave(level, Dushi.FLOUR_SACK.get(), 0, 2);
    }

    @Override
    public float getVoicePitch() {
        return 0.8F + this.random.nextFloat() * 0.1F;
    }
}
