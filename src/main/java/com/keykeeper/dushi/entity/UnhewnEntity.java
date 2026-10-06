package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.PlayerSoul;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The Unhewn: a statue of a saint the masons never finished. The face and the blessing right hand are done; the left arm
 * is still the squared block it was to be cut from, and below the waist there is only the rough of the quarry bed. It
 * stands among the other statues of the workshop as one of them (phase 1) until somebody comes within a few steps or
 * strikes it; then it tears itself off its bed and comes, rocking from corner to corner like a block being walked.
 * <p>
 * The block arm comes down from over its head and the floor jumps round where it lands; the finished hand, open, pushes -
 * it hurts little and throws far, which is worst beside a drop. It is slow to turn: the place to stand is behind it.
 */
public class UnhewnEntity extends SoulsMob {
    public static final int WAKE = 1;
    public static final int WAKE_DURATION = 36;
    public static final int SMASH = 2;
    public static final int SMASH_DURATION = 58;
    public static final int SMASH_IMPACT = 25;
    public static final int SHOVE = 3;
    public static final int SHOVE_DURATION = 38;
    public static final int SHOVE_IMPACT = 15;

    /** How near somebody may come before it wakes. */
    public static final double WAKES_AT = 4.5;

    public UnhewnEntity(EntityType<? extends UnhewnEntity> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 170.0).add(Attributes.ATTACK_DAMAGE, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.19).add(Attributes.ARMOR, 10.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void registerGoals() {
        // (nothing that would make a statue look about or wander)
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false, (entity, level) -> this.awake()));
    }

    public boolean awake() {
        return this.phase() >= 2;
    }

    @Override
    public int soulReward() {
        return 700;
    }

    @Override
    protected int attackDuration(int id) {
        return id == SMASH ? SMASH_DURATION : id == SHOVE ? SHOVE_DURATION : WAKE_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == SMASH ? SMASH_IMPACT - 11 : id == SHOVE ? SHOVE_IMPACT - 6 : 0;
    }

    @Override
    protected int attackPause(int finishedId) {
        return finishedId == WAKE ? 4 : 18 + this.random.nextInt(12);
    }

    @Override
    public double stopDistance() {
        return 2.0;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance < 3.0) {
            return this.random.nextInt(10) < 6 ? SMASH : SHOVE;
        }
        return 0;
    }

    @Override
    public boolean overlooks(LivingEntity target) {
        return !this.awake() || super.overlooks(target);
    }

    /** Tears itself off its bed. */
    public void rouse() {
        if (!this.awake() && this.isAlive()) {
            this.setPhase(2);
            this.startAttack(WAKE);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (!this.awake()) {
            this.getNavigation().stop();
            if (this.tickCount % 5 == 0) {
                Player near = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), WAKES_AT, entity -> entity instanceof Player player
                        && !player.isCreative() && !player.isSpectator() && !PlayerSoul.of(player).atHearth && Math.abs(player.getY() - this.getY()) < 3.0);
                if (near != null) {
                    this.rouse();
                }
            }
        }
        super.customServerAiStep(level);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && !this.awake()) {
            this.rouse();
        }
        return hurt;
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        switch (id) {
            case WAKE -> {
                if (time == 1) {
                    this.playSound(Dushi.sound("entity.unhewn.wake"), 2.0F, 1.0F);
                }
                if (time % 4 == 1 && time < 26) {
                    this.rubble(level, this.position(), 10, 0.5);
                }
            }
            case SMASH -> {
                if (time == 4) {
                    this.playSound(Dushi.sound("entity.unhewn.ambient"), 1.6F, 0.8F);
                }
                if (time == SMASH_IMPACT) {
                    Vec3 at = this.position().add(this.facing().scale(2.4));
                    this.hitArc(level, 3.7, 34.0, damage * 1.5F, 1.0);
                    this.hitAround(level, at, 2.7, damage * 0.6F, 0.7, this.damageSources().mobAttack(this));
                    this.rubble(level, at, 50, 1.1);
                    level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.unhewn.slam"), this.getSoundSource(), 2.4F, 0.9F + this.random.nextFloat() * 0.15F);
                }
            }
            default -> {
                if (time == SHOVE_IMPACT - 5) {
                    this.playSound(Dushi.sound("entity.unhewn.ambient"), 1.4F, 1.3F);
                }
                if (time == SHOVE_IMPACT) {
                    Vec3 push = this.facing().scale(0.8).add(0.0, 0.38, 0.0);
                    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0, 1.0, 4.0), this::isFoe)) {
                        Vec3 to = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ());
                        double distance = to.length() - victim.getBbWidth() * 0.5 - this.getBbWidth() * 0.5;
                        if (distance > 2.7 || to.lengthSqr() > 0.3 && to.normalize().dot(this.facing()) < 0.62) {
                            continue;
                        }
                        // a raised shield takes the hurt of it, not the push
                        victim.hurtServer(level, this.damageSources().mobAttack(this), damage * 0.5F);
                        shove(victim, push);
                    }
                }
            }
        }
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING) || super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            this.rubble(level, this.position().add(0.0, 1.2, 0.0), 80, 0.6);
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.leave(level, Dushi.IRON_WEDGE.get(), 0, 2);
        this.leaveMaybe(level, Dushi.GREAT_SOUL.get(), 0.12F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.awake() ? Dushi.sound("entity.unhewn.ambient") : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.unhewn.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.unhewn.death");
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(Dushi.sound("entity.unhewn.step"), 0.9F, 0.9F + this.random.nextFloat() * 0.2F);
    }
}
