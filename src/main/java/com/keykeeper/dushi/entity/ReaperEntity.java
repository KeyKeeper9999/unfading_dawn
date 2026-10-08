package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A reaper of the Stubble: bent double, as one is who has cut low since noon, a sheaf bound on the back and the sickle
 * before - in the standing corn there is nothing to see of them but the corn moving.
 * <p>
 * The sickle across, quick; across and back; and, from a few paces, a spring with it reached out as far as it goes.
 */
public class ReaperEntity extends FieldhandEntity {
    public static final int SLASH = 1;
    public static final int SLASH_DURATION = 30;
    public static final int SLASH_IMPACT = 12;
    public static final int DOUBLE = 2;
    public static final int DOUBLE_DURATION = 46;
    public static final int DOUBLE_FIRST = 12;
    public static final int DOUBLE_SECOND = 25;
    public static final int LUNGE = 3;
    public static final int LUNGE_DURATION = 44;
    public static final int LUNGE_SPRING = 15;
    public static final int LUNGE_IMPACT = 21;
    public static final double REACH = 2.5;

    private int lungeCooldown = 60;

    public ReaperEntity(EntityType<? extends ReaperEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 200.0).add(Attributes.ATTACK_DAMAGE, 22.0)
                .add(Attributes.MOVEMENT_SPEED, 0.31).add(Attributes.ARMOR, 2.0).add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    public int soulReward() {
        return 1000;
    }

    @Override
    protected int attackDuration(int id) {
        return id == SLASH ? SLASH_DURATION : id == DOUBLE ? DOUBLE_DURATION : LUNGE_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == SLASH ? SLASH_IMPACT - 4 : id == DOUBLE ? DOUBLE_SECOND - 5 : LUNGE_SPRING - 2;
    }

    @Override
    protected int attackPause(int finishedId) {
        return 14 + this.random.nextInt(14);
    }

    @Override
    public double stopDistance() {
        return 1.4;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance > 3.0 && distance < 6.0 && this.lungeCooldown <= 0 && this.hasLineOfSight(target)) {
            this.lungeCooldown = 120 + this.random.nextInt(80);
            return LUNGE;
        }
        if (distance < REACH - 0.5) {
            return this.random.nextInt(5) < 2 ? DOUBLE : SLASH;
        }
        return 0;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.lungeCooldown = Math.max(0, this.lungeCooldown - 1);
    }

    private void cut(ServerLevel level, float damage) {
        this.playSound(Dushi.sound("entity.fieldhand.sickle"), 1.0F, 0.9F + this.random.nextFloat() * 0.3F);
        this.hitArc(level, REACH, 70.0, damage, 0.5);
        Vec3 at = this.position().add(this.facing().scale(1.6));
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y + 0.9, at.z, 1, 0.0, 0.0, 0.0, 0.0);
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (id == SLASH) {
            if (time == SLASH_IMPACT) {
                this.cut(level, damage);
            }
        } else if (id == DOUBLE) {
            if (time == DOUBLE_FIRST || time == DOUBLE_SECOND) {
                this.cut(level, damage * 0.8F);
            }
        } else {
            if (time == LUNGE_SPRING) {
                Vec3 go = this.facing().scale(0.95);
                this.setDeltaMovement(go.x, 0.3, go.z);
                this.playSound(Dushi.sound("entity.fieldhand.sickle"), 1.2F, 0.7F);
            }
            if (time == LUNGE_IMPACT) {
                this.hitArc(level, REACH + 0.5, 40.0, damage * 1.3F, 0.8);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, net.minecraft.world.damagesource.DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.leaveMaybe(level, Dushi.RYE_STRAW.get(), 0.5F);
    }
}
