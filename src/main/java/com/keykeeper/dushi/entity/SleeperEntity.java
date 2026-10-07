package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A sleeper: one of the Unsleeping who fell asleep. They tied such to a stake, standing, so that he could not lie
 * down; he slept on the stake. Then they put him out of the skete, and he tore the stake out of the ground and walks
 * the wood with it still lashed along his back - asleep, the head on one shoulder, the arms out before him.
 * <p>
 * He knows of nobody until they are close. Then he fights the way a man fights in a dream: three wild blows one after
 * another, each a step nearer - or he sinks on his haunches and throws himself the length of a room. Out of every
 * few such leaps one ends flat on his face, where the stake pins him a long moment: that is when to strike.
 */
public class SleeperEntity extends UnsleepingEntity {
    public static final int FLURRY = 1;
    public static final int FLURRY_DURATION = 36;
    public static final int[] FLURRY_HITS = {9, 16, 23};
    public static final int LUNGE = 2;
    public static final int LUNGE_DURATION = 42;
    /** The same leap when he does not land on his feet. */
    public static final int LUNGE_FALL = 3;
    public static final int LUNGE_FALL_DURATION = 84;
    public static final int LUNGE_LEAP = 15;
    public static final int LUNGE_LAND = 25;
    /** Until which tick of a fall he lies where he fell. */
    public static final int LIES_UNTIL = 66;
    /** How many leaps in a hundred end on his face. */
    private static final int FALLS = 38;

    private int lungeCooldown = 30;
    private boolean lungeHit;

    public SleeperEntity(EntityType<? extends SleeperEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 110.0).add(Attributes.ATTACK_DAMAGE, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.31).add(Attributes.FOLLOW_RANGE, 13.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.45));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int soulReward() {
        return 780;
    }

    @Override
    protected int attackDuration(int id) {
        return id == LUNGE ? LUNGE_DURATION : id == LUNGE_FALL ? LUNGE_FALL_DURATION : FLURRY_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == FLURRY ? FLURRY_HITS[2] - 2 : LUNGE_LEAP - 3;
    }

    @Override
    protected int attackPause(int finishedId) {
        return finishedId == FLURRY ? 18 + this.random.nextInt(14) : 10 + this.random.nextInt(8);
    }

    @Override
    public double stopDistance() {
        return 1.3;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance > 3.5 && distance < 8.5 && this.lungeCooldown <= 0 && this.hasLineOfSight(target)) {
            this.lungeCooldown = 70 + this.random.nextInt(50);
            return this.random.nextInt(100) < FALLS ? LUNGE_FALL : LUNGE;
        }
        return distance < 2.1 ? FLURRY : 0;
    }

    @Override
    public void startAttack(int id) {
        super.startAttack(id);
        this.lungeHit = false;
        if (id == LUNGE || id == LUNGE_FALL) {
            this.playSound(Dushi.sound("entity.sleeper.cry"), 1.4F, 0.9F + this.random.nextFloat() * 0.2F);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.lungeCooldown > 0) {
            this.lungeCooldown--;
        }
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (id == FLURRY) {
            for (int hit : FLURRY_HITS) {
                if (time == hit - 2) {
                    Vec3 step = this.facing().scale(0.3);
                    this.setDeltaMovement(step.x, this.getDeltaMovement().y, step.z);
                }
                if (time == hit) {
                    this.hitArc(level, 2.2, 60.0, damage * 0.75F, 0.3);
                    this.playSound(Dushi.sound("entity.sect.hurt"), 0.5F, 1.5F);
                }
            }
            return;
        }
        // the leap: straight, low, and with no turning once he has gone
        if (time == LUNGE_LEAP) {
            Vec3 leap = this.facing().scale(0.95);
            this.setDeltaMovement(leap.x, 0.3, leap.z);
            this.needsSync = true;
        }
        if (time > LUNGE_LEAP && time <= LUNGE_LAND && !this.lungeHit && this.hitArc(level, 1.9, 50.0, damage * 1.4F, 1.2) > 0) {
            this.lungeHit = true;
        }
        if (time == LUNGE_LAND) {
            this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
            if (id == LUNGE_FALL) {
                this.rubble(level, this.position().add(this.facing().scale(0.8)), 10, 0.4);
                this.playSound(Dushi.sound("entity.sleeper.fall"), 1.2F, 1.0F);
            }
        }
        if (id == LUNGE_FALL && time > LUNGE_LAND && time < LIES_UNTIL) {
            this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.sleeper.snore");
    }
}
