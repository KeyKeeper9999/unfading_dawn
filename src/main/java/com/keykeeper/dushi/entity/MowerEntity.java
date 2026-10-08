package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * A mower of the Stubble: upright where the reapers are bent, a straw hat as wide as his shoulders, and the scythe.
 * He does not hurry. He takes a swathe as wide as the scythe is long, at the height of a man's belt, and then the
 * same swathe back; and what a shield is to a scythe brought down from over the head is nothing.
 */
public class MowerEntity extends FieldhandEntity {
    public static final int SWEEP = 1;
    public static final int SWEEP_DURATION = 52;
    public static final int SWEEP_IMPACT = 24;
    public static final int BACK = 2;
    public static final int BACK_DURATION = 44;
    public static final int BACK_IMPACT = 17;
    public static final int CLEAVE = 3;
    public static final int CLEAVE_DURATION = 60;
    public static final int CLEAVE_IMPACT = 30;
    public static final double REACH = 4.2;

    private boolean back;

    public MowerEntity(EntityType<? extends MowerEntity> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 330.0).add(Attributes.ATTACK_DAMAGE, 26.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23).add(Attributes.ARMOR, 4.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
                .add(Attributes.FOLLOW_RANGE, 22.0);
    }

    @Override
    public int soulReward() {
        return 1500;
    }

    @Override
    protected int attackDuration(int id) {
        return id == SWEEP ? SWEEP_DURATION : id == BACK ? BACK_DURATION : CLEAVE_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == SWEEP ? SWEEP_IMPACT - 9 : id == BACK ? BACK_IMPACT - 8 : CLEAVE_IMPACT - 11;
    }

    @Override
    protected int attackPause(int finishedId) {
        // (the swathe back follows the swathe out without a breath between)
        return this.back ? 1 : 20 + this.random.nextInt(14);
    }

    @Override
    public double stopDistance() {
        return 2.5;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (this.back) {
            this.back = false;
            return BACK;
        }
        if (distance > REACH - 0.4) {
            return 0;
        }
        int roll = this.random.nextInt(10);
        if (roll < 7) {
            this.back = this.random.nextInt(5) < 3;
            return SWEEP;
        }
        return CLEAVE;
    }

    private void swathe(ServerLevel level, float damage) {
        this.hitArc(level, REACH, 105.0, damage, 1.2);
        Vec3 at = this.position().add(this.facing().scale(2.8));
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y + 1.0, at.z, 2, 0.5, 0.0, 0.5, 0.0);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HAY_BLOCK.defaultBlockState()), at.x, at.y + 0.6, at.z, 14, 1.2, 0.2, 1.2, 0.1);
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (id == SWEEP) {
            if (time == SWEEP_IMPACT - 4) {
                this.playSound(Dushi.sound("entity.fieldhand.scythe"), 1.3F, 0.9F);
            }
            if (time == SWEEP_IMPACT) {
                this.swathe(level, damage);
            }
        } else if (id == BACK) {
            if (time == BACK_IMPACT - 4) {
                this.playSound(Dushi.sound("entity.fieldhand.scythe"), 1.3F, 1.1F);
            }
            if (time == BACK_IMPACT) {
                this.swathe(level, damage * 0.9F);
            }
        } else {
            if (time == CLEAVE_IMPACT - 5) {
                this.playSound(Dushi.sound("entity.fieldhand.scythe"), 1.3F, 0.7F);
            }
            if (time == CLEAVE_IMPACT) {
                this.hitArc(level, this.facing(), REACH, 18.0, damage * 1.5F, 0.4, true);
                Vec3 at = this.position().add(this.facing().scale(3.2));
                level.sendParticles(ParticleTypes.POOF, at.x, at.y + 0.2, at.z, 8, 0.4, 0.1, 0.4, 0.02);
            }
        }
    }

    @Override
    public void recover() {
        super.recover();
        this.back = false;
    }

    @Override
    public float getVoicePitch() {
        return 0.75F + this.random.nextFloat() * 0.1F;
    }
}
