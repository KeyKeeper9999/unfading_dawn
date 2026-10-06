package com.keykeeper.dushi.entity;

import java.util.EnumSet;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.block.MobMarkBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The bone pike of the Mire: a fish as long as a man with a shield of bone for a head and two blades of it for jaws. It
 * lies in water to the knee with its back fin out, and that fin is all there is to see of it.
 * <p>
 * It does not leave the water to walk: it comes as near as the water goes and bites whoever wades. At somebody on the
 * boards it throws itself - the water boils where it gathers - and whether it bit or not it lands where it lands. On
 * the boards it can do nothing but thrash, takes half as much again from every blow, and in a few breaths flings itself
 * back at the nearest water.
 */
public class BonePikeEntity extends SoulsMob {
    public static final int BITE = 1;
    public static final int BITE_DURATION = 22;
    public static final int BITE_IMPACT = 9;
    public static final int LEAP = 2;
    public static final int LEAP_DURATION = 40;
    public static final int LEAP_JUMP = 11;
    /** Out of the water: on its side, thrashing. */
    public static final int FLOUNDER = 3;
    public static final int FLOUNDER_DURATION = 64;
    /** The tick of it at which it throws itself back at the water. */
    public static final int FLOUNDER_BACK = 50;

    private int leapCooldown = 30;
    private boolean leapHit;

    public BonePikeEntity(EntityType<? extends BonePikeEntity> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 70.0).add(Attributes.ATTACK_DAMAGE, 15.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ARMOR, 4.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.3)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new Hunt(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Water is its ground: it neither floats in it nor is slowed or carried by it, and it needs no air. */
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
        return 380;
    }

    @Override
    protected int attackDuration(int id) {
        return id == LEAP ? LEAP_DURATION : id == FLOUNDER ? FLOUNDER_DURATION : BITE_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == LEAP ? LEAP_JUMP : id == FLOUNDER ? 0 : BITE_IMPACT - 3;
    }

    @Override
    protected int attackPause(int finishedId) {
        return finishedId == FLOUNDER ? 4 : 14 + this.random.nextInt(10);
    }

    @Override
    public double stopDistance() {
        return 1.1;
    }

    /** Whether there is water where it would be a step further on that line. */
    private boolean waterAhead(Vec3 direction, double far) {
        BlockPos ahead = BlockPos.containing(this.getX() + direction.x * far, this.getY() + 0.3, this.getZ() + direction.z * far);
        return this.level().getFluidState(ahead).is(FluidTags.WATER);
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (!this.isInWater()) {
            return 0;
        }
        if (distance < 1.9 && Math.abs(target.getY() - this.getY()) < 1.7) {
            return BITE;
        }
        if (this.leapCooldown <= 0 && distance > 2.2 && distance < 7.5 && this.hasLineOfSight(target)) {
            this.leapCooldown = 70 + this.random.nextInt(50);
            return LEAP;
        }
        return 0;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.leapCooldown > 0) {
            this.leapCooldown--;
        }
        if (this.attackId() == 0 && !this.isInWater() && this.onGround() && this.isAlive()) {
            this.startAttack(FLOUNDER);                          // thrown out of the water, by itself or by a blow
        }
        if (this.getTarget() == null && this.attackId() == 0 && this.isInWater() && this.tickCount % 20 == 0) {
            BlockPos home = MobMarkBlockEntity.homeOf(this);     // with nobody to hunt it goes back to where it lay
            if (home != null && home.distSqr(this.blockPosition()) > 9.0) {
                this.getMoveControl().setWantedPosition(home.getX() + 0.5, this.getY(), home.getZ() + 0.5, 0.6);
            }
        }
        if (this.isInWater() && this.tickCount % 5 == 0 && this.getDeltaMovement().horizontalDistanceSqr() > 0.004) {
            // the wake of the fin
            level.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 0.95, this.getZ(), 2, 0.2, 0.0, 0.2, 0.0);
        }
    }

    @Override
    public void startAttack(int id) {
        super.startAttack(id);
        this.leapHit = false;
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (id == BITE) {
            if (time == BITE_IMPACT - 3) {
                Vec3 dart = this.facing().scale(0.4);
                if (this.waterAhead(this.facing(), 1.2)) {
                    this.setDeltaMovement(dart.x, this.getDeltaMovement().y, dart.z);
                }
            }
            if (time == BITE_IMPACT) {
                this.playSound(Dushi.sound("entity.pike.bite"), 1.2F, 0.9F + this.random.nextFloat() * 0.2F);
                this.hitArc(level, 2.2, 55.0, damage, 0.4);
                level.sendParticles(ParticleTypes.SPLASH, this.getX() + this.facing().x, this.getY() + 0.9, this.getZ() + this.facing().z, 14, 0.3, 0.1, 0.3, 0.0);
            }
        } else if (id == LEAP) {
            if (time < LEAP_JUMP) {
                // it gathers under the water: the water boils over it
                level.sendParticles(ParticleTypes.BUBBLE_POP, this.getX(), this.getY() + 0.95, this.getZ(), 3, 0.35, 0.0, 0.35, 0.0);
                level.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 0.95, this.getZ(), 2, 0.35, 0.0, 0.35, 0.0);
            }
            if (time == LEAP_JUMP) {
                LivingEntity target = this.getTarget();
                Vec3 to = target == null ? this.facing().scale(4.0) : target.position().subtract(this.position());
                Vec3 flat = new Vec3(to.x, 0.0, to.z);
                double length = Math.min(8.0, flat.length() + 0.6);
                Vec3 jump = flat.lengthSqr() < 1.0E-4 ? Vec3.ZERO : flat.normalize().scale(length / 11.5);
                this.setDeltaMovement(jump.x, 0.5 + Math.max(0.0, Math.min(2.0, to.y)) * 0.07, jump.z);
                this.needsSync = true;
                this.playSound(Dushi.sound("entity.pike.leap"), 1.4F, 0.9F + this.random.nextFloat() * 0.2F);
                level.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 0.9, this.getZ(), 40, 0.5, 0.2, 0.5, 0.0);
            }
            if (time > LEAP_JUMP && time < LEAP_JUMP + 16 && !this.leapHit && this.hitArc(level, 1.5, 80.0, damage * 1.3F, 0.7) > 0) {
                this.leapHit = true;
                this.playSound(Dushi.sound("entity.pike.bite"), 1.3F, 0.8F);
            }
            if (time > LEAP_JUMP + 5 && (this.onGround() || this.isInWater())) {
                if (this.isInWater()) {
                    level.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 0.9, this.getZ(), 30, 0.5, 0.1, 0.5, 0.0);
                }
                this.endAttack();                                // (if it is on the boards the next tick finds it there)
                this.attackCooldown = this.isInWater() ? 20 : 0;
            }
        } else if (id == FLOUNDER) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 1.0, 0.5));
            if (this.isInWater() && time > 4) {
                this.endAttack();
                return;
            }
            if (time % 9 == 4 && this.onGround() && time < FLOUNDER_BACK - 6) {
                this.setDeltaMovement((this.random.nextDouble() - 0.5) * 0.16, 0.26, (this.random.nextDouble() - 0.5) * 0.16);
                this.needsSync = true;
                this.playSound(Dushi.sound("entity.pike.flop"), 0.9F, 0.9F + this.random.nextFloat() * 0.3F);
            }
            if (time == FLOUNDER_BACK) {
                Vec3 water = this.nearestWater(level);
                Vec3 to = water.subtract(this.position());
                Vec3 flat = new Vec3(to.x, 0.0, to.z);
                Vec3 jump = flat.lengthSqr() < 1.0E-4 ? Vec3.ZERO : flat.normalize().scale(Math.min(0.75, (flat.length() + 0.6) / 10.0));
                this.setDeltaMovement(jump.x, 0.46, jump.z);
                this.needsSync = true;
                this.playSound(Dushi.sound("entity.pike.flop"), 1.2F, 0.7F);
            }
        }
    }

    /** The middle of the nearest water within a few blocks; failing that, the place it rose at. */
    private Vec3 nearestWater(ServerLevel level) {
        BlockPos here = this.blockPosition();
        BlockPos best = null;
        for (BlockPos pos : BlockPos.betweenClosed(here.offset(-7, -2, -7), here.offset(7, 0, 7))) {
            if (level.getFluidState(pos).is(FluidTags.WATER) && level.getFluidState(pos.above()).isEmpty()
                    && (best == null || pos.distSqr(here) < best.distSqr(here))) {
                best = pos.immutable();
            }
        }
        if (best == null) {
            best = MobMarkBlockEntity.homeOf(this);
        }
        if (best == null) {
            return this.position().add(this.facing().scale(-3.0));
        }
        // (a little past its edge, so that the whole of it comes down in the water)
        Vec3 centre = Vec3.atBottomCenterOf(best);
        Vec3 on = centre.subtract(this.position());
        return on.lengthSqr() < 1.0E-4 ? centre : centre.add(new Vec3(on.x, 0.0, on.z).normalize().scale(1.2));
    }

    /** On the boards it is helpless: every blow counts for half as much again. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return super.hurtServer(level, source, this.attackId() == FLOUNDER ? damage * 1.5F : damage);
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.leaveMaybe(level, Dushi.BONE_SCALE.get(), 0.6F);
        this.leaveMaybe(level, Dushi.NAMELESS_SOUL.get(), 0.12F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isInWater() ? Dushi.sound("entity.pike.ambient") : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.pike.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.pike.death");
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
    }

    /** It swims straight at its prey for as long as there is water before it, and no further. */
    private static class Hunt extends Goal {
        private final BonePikeEntity pike;

        Hunt(BonePikeEntity pike) {
            this.pike = pike;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.pike.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse() || this.pike.attackId() != 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.pike.getTarget();
            if (target == null || this.pike.attackId() != 0) {
                return;
            }
            this.pike.getLookControl().setLookAt(target, 30.0F, 30.0F);
            Vec3 to = new Vec3(target.getX() - this.pike.getX(), 0.0, target.getZ() - this.pike.getZ());
            double flat = to.length();
            double distance = Math.max(0.0, flat - target.getBbWidth() * 0.5 - this.pike.getBbWidth() * 0.5);
            if (this.pike.isInWater() && flat > 0.3) {
                Vec3 direction = to.scale(1.0 / flat);
                if (distance > this.pike.stopDistance() && this.pike.waterAhead(direction, 0.9)) {
                    this.pike.getMoveControl().setWantedPosition(this.pike.getX() + direction.x * 2.0, this.pike.getY(), this.pike.getZ() + direction.z * 2.0, 1.0);
                } else {
                    this.pike.getMoveControl().setWantedPosition(this.pike.getX(), this.pike.getY(), this.pike.getZ(), 0.0);
                    this.pike.setYRot((float) (Math.atan2(direction.z, direction.x) * 180.0 / Math.PI) - 90.0F);
                    this.pike.setYBodyRot(this.pike.getYRot());
                }
            }
            if (this.pike.canStartAttack()) {
                int id = this.pike.chooseAttack(target, distance);
                if (id != 0) {
                    this.pike.startAttack(id);
                }
            }
        }
    }
}
