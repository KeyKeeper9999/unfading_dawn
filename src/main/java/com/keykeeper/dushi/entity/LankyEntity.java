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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * The lanky of the Mire - the thin one of the old stories, that walks the bog at night and looks in at upper windows.
 * Twice a man's height and no wider than his arm, with arms that hang to its shins. It stands among the dead trees and
 * is taken for one until it moves.
 * <p>
 * Water is nothing to it: it strides the bottom as fast as it strides the bank, and steps over what a man must climb.
 * It keeps whoever it is after at arm's length - its own arm: a straight thrust of one hand that reaches five blocks,
 * both arms brought down from over its head on the same line (no shield holds that), and, for whoever gets in under
 * them, both arms swept low and wide, which throws.
 */
public class LankyEntity extends SoulsMob {
    public static final int REACH = 1;
    public static final int REACH_DURATION = 34;
    public static final int REACH_IMPACT = 15;
    public static final int SWEEP = 2;
    public static final int SWEEP_DURATION = 40;
    public static final int SWEEP_IMPACT = 17;
    public static final int SLAM = 3;
    public static final int SLAM_DURATION = 48;
    public static final int SLAM_IMPACT = 22;

    public LankyEntity(EntityType<? extends LankyEntity> type, Level level) {
        super(type, level);
        this.xpReward = 14;
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 200.0).add(Attributes.ATTACK_DAMAGE, 18.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22).add(Attributes.ARMOR, 3.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 22.0).add(Attributes.STEP_HEIGHT, 1.6);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean isAffectedByFluids() {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public int soulReward() {
        return 950;
    }

    @Override
    protected int attackDuration(int id) {
        return id == SWEEP ? SWEEP_DURATION : id == SLAM ? SLAM_DURATION : REACH_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == SWEEP ? SWEEP_IMPACT - 7 : id == SLAM ? SLAM_IMPACT - 8 : REACH_IMPACT - 6;
    }

    @Override
    protected int attackPause(int finishedId) {
        return 16 + this.random.nextInt(12);
    }

    @Override
    public double stopDistance() {
        return 3.0;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance < 2.0) {
            return this.random.nextInt(4) < 3 ? SWEEP : SLAM;
        }
        if (distance < 4.9) {
            int roll = this.random.nextInt(10);
            return roll < 5 || distance > 4.0 ? REACH : roll < 8 ? SLAM : SWEEP;
        }
        return 0;
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (id == REACH) {
            if (time == REACH_IMPACT - 3) {
                this.playSound(Dushi.sound("entity.overseer.swing"), 1.0F, 1.5F);
            }
            if (time == REACH_IMPACT) {
                this.hitArc(level, 5.2, 13.0, damage, 0.5);
            }
        } else if (id == SWEEP) {
            if (time == SWEEP_IMPACT - 3) {
                this.playSound(Dushi.sound("entity.overseer.swing"), 1.2F, 1.2F);
            }
            if (time == SWEEP_IMPACT) {
                this.hitArc(level, 4.4, 95.0, damage * 0.8F, 1.2);
            }
        } else if (id == SLAM) {
            if (time == SLAM_IMPACT - 4) {
                this.playSound(Dushi.sound("player.weapon_swing_heavy"), 1.1F, 1.1F);
            }
            if (time == SLAM_IMPACT) {
                this.hitArc(level, this.facing(), 4.5, 20.0, damage * 1.4F, 0.3, true);
                Vec3 at = this.position().add(this.facing().scale(3.4));
                this.rubble(level, at, 14, 0.4);
                if (!level.getFluidState(BlockPos.containing(at.x, at.y + 0.3, at.z)).isEmpty()) {
                    level.sendParticles(ParticleTypes.SPLASH, at.x, at.y + 0.95, at.z, 40, 0.5, 0.1, 0.5, 0.0);
                }
                level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.lanky.slam"), this.getSoundSource(), 1.2F, 0.9F + this.random.nextFloat() * 0.2F);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.leave(level, Dushi.WITHE.get(), 0, 2);
        this.leaveMaybe(level, Dushi.NAMELESS_SOUL.get(), 0.3F);
        this.leaveMaybe(level, Dushi.GREAT_SOUL.get(), 0.12F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.lanky.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.lanky.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.lanky.death");
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(Dushi.sound("entity.lanky.step"), 0.5F, 0.9F + this.random.nextFloat() * 0.2F);
    }
}
