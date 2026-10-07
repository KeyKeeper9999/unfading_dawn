package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A harpooner of the Morning Wharf, in his sou'wester, with spare irons on his back: he stands where he can see - a
 * boat's thwart, the end of a pier, a roof - and does not come down. Whoever is in sight and not yet upon him gets an
 * iron, thrown level and hard: it is seen leaving his hand, and stepped out of the way of. Whoever gets close he
 * stabs at, or jumps back from to throw again.
 */
public class HarpoonerEntity extends ShoremanEntity {
    public static final int THROW = 1;
    public static final int THROW_DURATION = 46;
    public static final int THROW_RELEASE = 24;
    public static final int STAB = 2;
    public static final int STAB_DURATION = 30;
    public static final int STAB_IMPACT = 13;
    public static final int HOP = 3;
    public static final int HOP_DURATION = 14;
    /** He throws at whoever is no nearer than this and no further than that. */
    public static final double THROWS_FROM = 4.0;
    public static final double THROWS_TO = 20.0;

    private int hopCooldown;

    public HarpoonerEntity(EntityType<? extends HarpoonerEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 140.0).add(Attributes.ATTACK_DAMAGE, 19.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27).add(Attributes.ARMOR, 2.0).add(Attributes.FOLLOW_RANGE, 26.0);
    }

    @Override
    protected void registerGoals() {
        // (nothing that would walk him off the place he was stood on)
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int soulReward() {
        return 950;
    }

    @Override
    protected int attackDuration(int id) {
        return id == THROW ? THROW_DURATION : id == STAB ? STAB_DURATION : HOP_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == THROW ? THROW_RELEASE - 5 : id == STAB ? STAB_IMPACT - 5 : 0;
    }

    @Override
    protected int attackPause(int finishedId) {
        return finishedId == THROW ? 26 + this.random.nextInt(24) : finishedId == HOP ? 2 : 14 + this.random.nextInt(10);
    }

    /** He keeps his distance: he is content wherever he can throw from. */
    @Override
    public double stopDistance() {
        return 9.0;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance < 3.0) {
            if (this.hopCooldown <= 0 && this.random.nextInt(3) != 0 && this.groundBehind()) {
                this.hopCooldown = 120;
                return HOP;
            }
            return STAB;
        }
        if (distance >= THROWS_FROM && distance <= THROWS_TO && this.hasLineOfSight(target)) {
            return THROW;
        }
        return 0;
    }

    /** Whether there is something to land on three paces behind him: he does not jump off the pier. */
    private boolean groundBehind() {
        Vec3 back = this.position().subtract(this.facing().scale(3.0));
        BlockPos under = BlockPos.containing(back.x, this.getY() - 0.5, back.z);
        return !this.level().getBlockState(under).getCollisionShape(this.level(), under).isEmpty();
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.hopCooldown = Math.max(0, this.hopCooldown - 1);
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        LivingEntity target = this.getTarget();
        if (id == THROW) {
            if (time == THROW_RELEASE && target != null) {
                HarpoonEntity iron = new HarpoonEntity(level, this, damage);
                iron.setPos(this.getX(), this.getY() + this.getBbHeight() * 0.8, this.getZ());
                Vec3 to = target.getBoundingBox().getCenter().subtract(iron.position());
                iron.shoot(to.x, to.y + to.horizontalDistance() * 0.03, to.z, 1.9F, 1.0F);
                level.addFreshEntity(iron);
                this.playSound(Dushi.sound("entity.sailor.throw"), 1.4F, 0.95F + this.random.nextFloat() * 0.1F);
            }
        } else if (id == STAB) {
            if (time == STAB_IMPACT - 2) {
                this.playSound(Dushi.sound("player.weapon_swing_heavy"), 0.9F, 0.9F);
            }
            if (time == STAB_IMPACT) {
                this.hitArc(level, 3.3, 18.0, damage * 1.1F, 0.6);
            }
        } else if (time == 1) {
            Vec3 back = this.facing().scale(-0.72);
            this.setDeltaMovement(back.x, 0.34, back.z);
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.leaveMaybe(level, Dushi.HARPOON.get(), 0.05F);
    }
}
