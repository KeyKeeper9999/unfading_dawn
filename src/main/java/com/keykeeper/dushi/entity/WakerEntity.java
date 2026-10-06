package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A waker of the skete: the one whose office is that the others do not sleep. He walks his round among them in a dark
 * cowl, a board as long as himself on his left shoulder and the mallet he beats it with in his right hand, a lantern
 * at his belt - and he, unlike those on their knees, looks about him.
 * <p>
 * Whoever he sees, he first beats the board: three strokes, and at the third everything of the skete within earshot is
 * on its feet and coming - the kneeling ones too. He is the one to strike first, and from behind. After that he fights
 * with the mallet: round level, or down from over his head, which no shield holds.
 */
public class WakerEntity extends UnsleepingEntity {
    public static final int KNOCK = 1;
    public static final int KNOCK_DURATION = 44;
    /** The ticks of the three strokes; at the last the others are up. */
    public static final int[] KNOCKS = {10, 20, 30};
    public static final int SWING = 2;
    public static final int SWING_DURATION = 36;
    public static final int SWING_IMPACT = 16;
    public static final int SMASH = 3;
    public static final int SMASH_DURATION = 46;
    public static final int SMASH_IMPACT = 24;
    /** How far the board is heard. */
    public static final double HEARD = 18.0;

    /** He beats the board once for every time he takes notice of somebody. */
    private boolean knocked;

    public WakerEntity(EntityType<? extends WakerEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 170.0).add(Attributes.ATTACK_DAMAGE, 19.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26).add(Attributes.ARMOR, 4.0).add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int soulReward() {
        return 850;
    }

    @Override
    protected int attackDuration(int id) {
        return id == SWING ? SWING_DURATION : id == SMASH ? SMASH_DURATION : KNOCK_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == SWING ? SWING_IMPACT - 6 : id == SMASH ? SMASH_IMPACT - 9 : KNOCK_DURATION;
    }

    @Override
    protected int attackPause(int finishedId) {
        return finishedId == KNOCK ? 4 : finishedId == SMASH ? 22 + this.random.nextInt(10) : 15 + this.random.nextInt(10);
    }

    @Override
    public double stopDistance() {
        return 1.7;
    }

    @Override
    protected void onReset() {
        super.onReset();
        this.knocked = false;
    }

    @Override
    public void setTarget(LivingEntity target) {
        if (target == null) {
            this.knocked = false;                                // whoever he has lost, he will announce again
        }
        super.setTarget(target);
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (!this.knocked && distance < 16.0 && this.hasLineOfSight(target)) {
            this.knocked = true;
            return KNOCK;
        }
        if (distance < 2.5) {
            return this.random.nextInt(10) < 6 ? SWING : SMASH;
        }
        return 0;
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (id == KNOCK) {
            for (int i = 0; i < KNOCKS.length; i++) {
                if (time == KNOCKS[i]) {
                    this.playSound(Dushi.sound("entity.waker.knock"), 3.0F, 0.95F + 0.06F * i);
                    level.sendParticles(ParticleTypes.NOTE, this.getX(), this.getY() + this.getBbHeight() + 0.5, this.getZ(), 1, 0.2, 0.1, 0.2, 0.0);
                    if (i == KNOCKS.length - 1) {
                        this.wake(level);
                    }
                }
            }
        } else if (id == SWING) {
            if (time == SWING_IMPACT - 3) {
                this.playSound(Dushi.sound("player.weapon_swing_heavy"), 0.9F, 1.1F);
            }
            if (time == SWING_IMPACT) {
                this.hitArc(level, 2.9, 80.0, damage, 1.0);
            }
        } else if (id == SMASH) {
            if (time == SMASH_IMPACT - 4) {
                this.playSound(Dushi.sound("player.weapon_swing_heavy"), 1.0F, 0.85F);
            }
            if (time == SMASH_IMPACT) {
                this.hitArc(level, this.facing(), 3.1, 22.0, damage * 1.5F, 0.5, true);
                Vec3 at = this.position().add(this.facing().scale(2.2));
                this.rubble(level, at, 10, 0.3);
                level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.waker.knock"), this.getSoundSource(), 1.0F, 0.6F);
            }
        }
    }

    /** The third stroke: every one of the skete within earshot that has nobody to fight gets up and comes for his. */
    public int wake(ServerLevel level) {
        LivingEntity target = this.getTarget();
        int woken = 0;
        for (SoulsMob other : level.getEntitiesOfClass(SoulsMob.class, this.getBoundingBox().inflate(HEARD, 6.0, HEARD),
                mob -> mob != this && mob.isAlive() && !mob.isBoss() && mob.getTarget() == null)) {
            if (other instanceof WakefulEntity kneeling) {
                kneeling.rouse(target);
                woken++;
            } else if (other instanceof SleeperEntity) {
                continue;                                        // (those nothing wakes)
            } else if (target != null) {
                other.setTarget(target);
                woken++;
            }
        }
        return woken;
    }
}
