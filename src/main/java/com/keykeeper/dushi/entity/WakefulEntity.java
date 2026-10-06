package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.PlayerSoul;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * One of the wakeful: on its knees at its vigil, a candlestick as long as a spear upright before it in both hands,
 * candle ends burning in a ring on its head-cloth - looking the way the morning will come from, as it has since it
 * knelt (phase 1). It does not turn its head. Whoever crosses before its eyes it sees from ten paces; whoever walks up
 * to it, it feels; whoever comes from behind, creeping, it knows nothing of until the blow - and a blow in the back is
 * what a kneeling man is for. Beaten awake by a waker's board, they all get up at once.
 * <p>
 * Up, it brings the candlestick down from over its head with both hands, or drives the burning end in level: the
 * second is quicker, reaches further, and sets alight.
 */
public class WakefulEntity extends UnsleepingEntity {
    /** Getting up off its knees: its first move, in which it does nothing else. */
    public static final int RISE = 1;
    public static final int RISE_DURATION = 26;
    public static final int STRIKE = 2;
    public static final int STRIKE_DURATION = 40;
    public static final int STRIKE_IMPACT = 20;
    public static final int THRUST = 3;
    public static final int THRUST_DURATION = 30;
    public static final int THRUST_IMPACT = 15;

    /** How far it sees before it, how wide (half the angle, in degrees), and how far when the one it looks at creeps. */
    public static final double SEES = 10.0;
    public static final double SEES_CREEPING = 5.0;
    public static final double SEES_ANGLE = 55.0;
    /** Whoever walks this near it is felt, from any side. Whoever creeps is not. */
    public static final double FEELS = 2.2;

    public WakefulEntity(EntityType<? extends WakefulEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 130.0).add(Attributes.ATTACK_DAMAGE, 17.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.ARMOR, 2.0).add(Attributes.FOLLOW_RANGE, 22.0);
    }

    @Override
    protected void registerGoals() {
        // (nothing that would make a kneeling man look about or wander)
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false, (entity, level) -> this.awake()));
    }

    public boolean awake() {
        return this.phase() >= 2;
    }

    /**
     * The rule of its eyes, by itself (the self-test asks it directly): it faces {@code yaw}; somebody is ({@code dx},
     * {@code dz}) from it along the ground, creeping or not.
     */
    public static boolean sees(float yaw, double dx, double dz, boolean creeping) {
        double far = Math.sqrt(dx * dx + dz * dz);
        if (!creeping && far <= FEELS) {
            return true;
        }
        if (far > (creeping ? SEES_CREEPING : SEES) || far < 1.0E-4) {
            return false;
        }
        Vec3 facing = Vec3.directionFromRotation(0.0F, yaw);
        return (dx * facing.x + dz * facing.z) / far >= Math.cos(Math.toRadians(SEES_ANGLE));
    }

    @Override
    public int soulReward() {
        return 600;
    }

    @Override
    protected int attackDuration(int id) {
        return id == STRIKE ? STRIKE_DURATION : id == THRUST ? THRUST_DURATION : RISE_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == STRIKE ? STRIKE_IMPACT - 8 : id == THRUST ? THRUST_IMPACT - 5 : RISE_DURATION;
    }

    @Override
    protected int attackPause(int finishedId) {
        return finishedId == RISE ? 4 : 16 + this.random.nextInt(12);
    }

    @Override
    public double stopDistance() {
        return 2.2;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance < 2.4) {
            return this.random.nextInt(10) < 6 ? STRIKE : THRUST;
        }
        return distance < 3.7 ? THRUST : 0;
    }

    @Override
    public boolean overlooks(LivingEntity target) {
        return !this.awake() || super.overlooks(target);
    }

    /** It gets up, and knows whom it is getting up for (if anybody). */
    public void rouse(LivingEntity cause) {
        if (!this.awake() && this.isAlive()) {
            this.setPhase(2);
            this.startAttack(RISE);
            this.playSound(Dushi.sound("entity.wakeful.rise"), 1.2F, 0.9F + this.random.nextFloat() * 0.2F);
            if (cause != null) {
                this.setTarget(cause);
            }
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (!this.awake()) {
            this.getNavigation().stop();
            if (this.tickCount % 4 == 0) {
                for (Player player : level.players()) {
                    if (player.isCreative() || player.isSpectator() || !player.isAlive() || PlayerSoul.of(player).atHearth || Math.abs(player.getY() - this.getY()) > 3.0) {
                        continue;
                    }
                    if (sees(this.yBodyRot, player.getX() - this.getX(), player.getZ() - this.getZ(), player.isShiftKeyDown() || player.isCrouching())
                            && this.hasLineOfSight(player)) {
                        this.rouse(player);
                        break;
                    }
                }
            }
        }
        super.customServerAiStep(level);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && !this.awake()) {
            this.rouse(source.getEntity() instanceof LivingEntity attacker ? attacker : null);
        }
        return hurt;
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (id == STRIKE) {
            if (time == STRIKE_IMPACT - 3) {
                this.playSound(Dushi.sound("player.weapon_swing_heavy"), 0.9F, 1.2F);
            }
            if (time == STRIKE_IMPACT) {
                this.hitArc(level, 3.3, 26.0, damage * 1.3F, 0.6);
                Vec3 at = this.position().add(this.facing().scale(2.6));
                level.sendParticles(ParticleTypes.FLAME, at.x, at.y + 0.3, at.z, 8, 0.25, 0.1, 0.25, 0.02);
                level.sendParticles(ParticleTypes.SMOKE, at.x, at.y + 0.3, at.z, 6, 0.3, 0.1, 0.3, 0.01);
            }
        } else if (id == THRUST) {
            if (time == THRUST_IMPACT - 2) {
                // the step that carries the thrust
                Vec3 step = this.facing().scale(0.45);
                this.setDeltaMovement(step.x, this.getDeltaMovement().y, step.z);
                this.playSound(Dushi.sound("player.weapon_swing_heavy"), 0.6F, 1.7F);
            }
            if (time == THRUST_IMPACT) {
                this.hitArc(level, 4.0, 15.0, damage, 0.5);
                Vec3 at = this.position().add(this.facing().scale(3.2)).add(0.0, 1.0, 0.0);
                level.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, 6, 0.15, 0.15, 0.15, 0.02);
            }
        }
    }

    /** The candle at the end of the stick: what it is driven into, burns. */
    @Override
    protected void struck(ServerLevel level, LivingEntity victim) {
        if (this.attackId() == THRUST && !victim.isBlocking()) {
            victim.igniteForSeconds(3.0F);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.awake() ? super.getAmbientSound() : Dushi.sound("entity.wakeful.vigil");
    }
}
