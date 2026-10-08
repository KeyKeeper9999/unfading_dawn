package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A sungazer: a sunflower as tall as a man, which has looked at a sun that does not move for so long that it has
 * the whole of the afternoon in its face. It has pulled its roots up and walks on them, slowly, and what it turns its
 * face to, it burns.
 * <p>
 * It lifts its head and a patch of light falls on the ground before it - and creeps towards whoever it is looking at,
 * at something less than a walk, scorching what it passes over; it follows them as long as the flower can hold its
 * head up. Near, it has only its leaves to beat with. It is best killed from behind its face.
 */
public class SungazerEntity extends SoulsMob {
    public static final int GAZE = 1;
    public static final int GAZE_DURATION = 110;
    /** The patch falls at this tick, and is gone at {@link #GAZE_END}. */
    public static final int GAZE_START = 22;
    public static final int GAZE_END = 92;
    public static final int LASH = 2;
    public static final int LASH_DURATION = 36;
    public static final int LASH_IMPACT = 15;
    public static final double REACH = 3.2;
    public static final double SPOT_RADIUS = 1.15;
    /** Blocks a tick: a man walks a little faster than this, and runs much faster. */
    public static final double SPOT_SPEED = 0.19;
    public static final double GAZE_RANGE = 16.0;

    private Vec3 spot;
    private int gazeCooldown = 40;

    public SungazerEntity(EntityType<? extends SungazerEntity> type, Level level) {
        super(type, level);
        this.xpReward = 16;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 190.0).add(Attributes.ATTACK_DAMAGE, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.17).add(Attributes.ARMOR, 0.0).add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        // it goes about the corn slowly, its head over the ears, looking for whoever is in it
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.5));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int soulReward() {
        return 1300;
    }

    @Override
    protected int attackDuration(int id) {
        return id == GAZE ? GAZE_DURATION : LASH_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == GAZE ? GAZE_END : LASH_IMPACT - 5;
    }

    @Override
    protected int attackPause(int finishedId) {
        return finishedId == GAZE ? 30 + this.random.nextInt(20) : 16 + this.random.nextInt(10);
    }

    @Override
    public double stopDistance() {
        return this.gazeCooldown <= 30 ? 8.0 : 1.6;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance < REACH - 0.5) {
            return LASH;
        }
        if (distance < GAZE_RANGE && this.gazeCooldown <= 0 && this.hasLineOfSight(target)) {
            this.gazeCooldown = 70 + this.random.nextInt(50);
            return GAZE;
        }
        return 0;
    }

    @Override
    public void startAttack(int id) {
        super.startAttack(id);
        this.spot = null;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.gazeCooldown = Math.max(0, this.gazeCooldown - 1);
    }

    /** The ground under ({@code x}, {@code z}) near the height {@code y}. */
    private double floor(ServerLevel level, double x, double y, double z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = 2; dy >= -4; dy--) {
            pos.set(net.minecraft.util.Mth.floor(x), net.minecraft.util.Mth.floor(y) + dy, net.minecraft.util.Mth.floor(z));
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty() && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()) {
                return pos.getY() + 1.0;
            }
        }
        return y;
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (id == LASH) {
            if (time == LASH_IMPACT - 3) {
                this.playSound(Dushi.sound("entity.sungazer.lash"), 1.0F, 1.0F);
            }
            if (time == LASH_IMPACT) {
                this.hitArc(level, REACH, 80.0, damage, 1.5);
            }
            return;
        }
        if (time == 2) {
            this.playSound(Dushi.sound("entity.sungazer.gaze"), 1.4F, 1.0F);
        }
        if (time < GAZE_START) {
            // the face lifts and fills with light
            if (time % 2 == 0) {
                Vec3 face = this.position().add(this.facing().scale(0.5)).add(0.0, this.getBbHeight() * 0.9, 0.0);
                level.sendParticles(ParticleTypes.END_ROD, face.x, face.y, face.z, 2, 0.2, 0.2, 0.2, 0.01);
            }
            return;
        }
        if (time > GAZE_END) {
            this.spot = null;
            return;
        }
        LivingEntity target = this.getTarget();
        if (this.spot == null) {
            Vec3 start = this.position().add(this.facing().scale(2.0));
            this.spot = new Vec3(start.x, this.floor(level, start.x, this.getY(), start.z), start.z);
        }
        if (target != null) {
            Vec3 to = new Vec3(target.getX() - this.spot.x, 0.0, target.getZ() - this.spot.z);
            double far = to.length();
            if (far > 1.0E-3) {
                Vec3 step = to.scale(Math.min(SPOT_SPEED, far) / far);
                double x = this.spot.x + step.x;
                double z = this.spot.z + step.z;
                this.spot = new Vec3(x, this.floor(level, x, this.spot.y, z), z);
            }
        }
        // it does not reach further than the flower sees
        Vec3 out = this.spot.subtract(this.position());
        if (out.horizontalDistance() > GAZE_RANGE) {
            double k = GAZE_RANGE / out.horizontalDistance();
            this.spot = new Vec3(this.getX() + out.x * k, this.spot.y, this.getZ() + out.z * k);
        }
        level.sendParticles(ParticleTypes.END_ROD, this.spot.x, this.spot.y + 0.1, this.spot.z, 3, SPOT_RADIUS * 0.45, 0.02, SPOT_RADIUS * 0.45, 0.0);
        level.sendParticles(ParticleTypes.FLAME, this.spot.x, this.spot.y + 0.1, this.spot.z, 2, SPOT_RADIUS * 0.4, 0.02, SPOT_RADIUS * 0.4, 0.005);
        if (time % 3 == 0) {
            // the edge of the patch, and the beam it comes down by
            for (int i = 0; i < 8; i++) {
                double angle = i * Math.PI / 4.0 + time * 0.2;
                level.sendParticles(ParticleTypes.SMALL_FLAME, this.spot.x + Math.cos(angle) * SPOT_RADIUS, this.spot.y + 0.1, this.spot.z + Math.sin(angle) * SPOT_RADIUS,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
            Vec3 face = this.position().add(0.0, this.getBbHeight() * 0.9, 0.0);
            Vec3 ray = this.spot.subtract(face);
            for (int i = 1; i < 6; i++) {
                Vec3 at = face.add(ray.scale(i / 6.0));
                level.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, 1, 0.02, 0.02, 0.02, 0.0);
            }
        }
        if (time % 5 == 0) {
            DamageSource source = this.damageSources().mobAttack(this);
            this.hitAround(level, this.spot, SPOT_RADIUS, damage * 0.4F, 0.0, source);
            FlourCloudEntity.kindle(level, this.spot, SPOT_RADIUS);
        }
    }

    @Override
    protected void struck(ServerLevel level, LivingEntity victim) {
        if (this.attackId() == GAZE) {
            victim.igniteForSeconds(2.0F);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        // from behind its face it is a stalk
        float dealt = damage;
        if (source.getEntity() != null && source.getDirectEntity() != null) {
            Vec3 from = new Vec3(source.getEntity().getX() - this.getX(), 0.0, source.getEntity().getZ() - this.getZ());
            if (from.lengthSqr() > 0.2 && from.normalize().dot(this.facing()) < -0.3) {
                dealt = damage * 1.5F;
            }
        }
        return super.hurtServer(level, source, dealt);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.leaveMaybe(level, Dushi.NAMELESS_SOUL.get(), 0.25F);
        this.leaveMaybe(level, Dushi.GREAT_SOUL.get(), 0.08F);
        this.leaveMaybe(level, Dushi.SPELLS.get(com.keykeeper.dushi.Spell.SUN_PATCH).get(), 0.05F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.sungazer.hum");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.sungazer.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.sungazer.death");
    }
}
