package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.block.FogGateBlock;
import com.keykeeper.dushi.block.MobMarkBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The Stone-Eater: what the lowest drift broke into. A beast the size of a wagon that lived in the rock before there was
 * a quarry - eyeless, plated with stone that pale lichen grows on, with two digging claws as long as a man and a tail
 * that ends in a knot of rock. The boss of the Breach.
 * <p>
 * It rakes with a claw (with both, one after the other, once it is hurt), springs with its jaws open, and clubs
 * whoever gets behind it with its tail. Its own trick is the rock: it digs itself under in a few strokes - nothing
 * reaches it there - follows its prey below the floor with the dust jumping over it, and comes up underneath. When it
 * rears and roars the roof answers.
 */
public class StoneEaterEntity extends SoulsMob {
    public static final int CLAW = 1;
    public static final int CLAW_DURATION = 32;
    public static final int CLAW_IMPACT = 13;
    /** The second claw of the same move, once it is hurt. */
    public static final int CLAW_SECOND = 23;
    public static final int CLAW_DURATION_TWO = 42;
    public static final int LUNGE = 2;
    public static final int LUNGE_DURATION = 46;
    public static final int LUNGE_START = 15;
    public static final int LUNGE_END = 26;
    public static final int BURROW = 3;
    public static final int BURROW_DURATION = 100;
    /** Under by this tick; */
    public static final int BURROW_UNDER = 20;
    /** stops under its prey at this one - the floor shakes there - */
    public static final int BURROW_STOP = 62;
    /** and comes up at this one. */
    public static final int BURROW_ERUPT = 76;
    public static final int ROAR = 4;
    public static final int ROAR_DURATION = 58;
    public static final int ROAR_SOUND = 16;
    public static final int TAIL = 5;
    public static final int TAIL_DURATION = 36;
    public static final int TAIL_IMPACT = 14;

    private int lungeCooldown = 80;
    private int burrowCooldown = 200;
    private int roarCooldown = 240;
    private int tailCooldown;
    private boolean lungeHit;

    public StoneEaterEntity(EntityType<? extends StoneEaterEntity> type, Level level) {
        super(type, level);
        this.xpReward = 200;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 950.0).add(Attributes.ATTACK_DAMAGE, 15.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27).add(Attributes.ARMOR, 6.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 44.0).add(Attributes.STEP_HEIGHT, 1.25);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 14.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public int soulReward() {
        return 14000;
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    /** Whether it is under the floor just now, where nothing reaches it. */
    public boolean under() {
        return this.attackId() == BURROW && this.attackTime >= BURROW_UNDER && this.attackTime < BURROW_ERUPT;
    }

    @Override
    protected int attackDuration(int id) {
        return switch (id) {
            case CLAW -> this.phase() >= 2 ? CLAW_DURATION_TWO : CLAW_DURATION;
            case LUNGE -> LUNGE_DURATION;
            case BURROW -> BURROW_DURATION;
            case ROAR -> ROAR_DURATION;
            default -> TAIL_DURATION;
        };
    }

    @Override
    protected int trackUntil(int id) {
        return switch (id) {
            case CLAW -> CLAW_IMPACT - 5;
            case LUNGE -> LUNGE_START - 2;
            case BURROW -> BURROW_STOP;
            default -> 0;
        };
    }

    @Override
    protected int attackPause(int finishedId) {
        return this.phase() >= 2 ? 8 + this.random.nextInt(8) : 15 + this.random.nextInt(12);
    }

    @Override
    public double stopDistance() {
        return 2.2;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        Vec3 to = new Vec3(target.getX() - this.getX(), 0.0, target.getZ() - this.getZ());
        boolean behind = to.lengthSqr() > 0.5 && to.normalize().dot(this.facing()) < -0.25;
        if (behind && distance < 4.6 && this.tailCooldown <= 0) {
            this.tailCooldown = 40;
            return TAIL;
        }
        if (this.phase() >= 2 && this.roarCooldown <= 0) {
            this.roarCooldown = 340 + this.random.nextInt(100);
            return ROAR;
        }
        if (this.burrowCooldown <= 0 && this.onGround() && distance > 3.0) {
            this.burrowCooldown = (this.phase() >= 2 ? 260 : 380) + this.random.nextInt(120);
            return BURROW;
        }
        if (distance > 5.0 && distance < 12.0 && this.lungeCooldown <= 0 && this.hasLineOfSight(target)) {
            this.lungeCooldown = 130 + this.random.nextInt(60);
            return LUNGE;
        }
        return distance < 3.6 ? CLAW : 0;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.phase() == 1 && this.getHealth() < this.getMaxHealth() * 0.5F && this.attackId() == 0 && this.isAlive()) {
            this.setPhase(2);
            this.roarCooldown = 360;
            this.startAttack(ROAR);
        }
        super.customServerAiStep(level);
        if (this.lungeCooldown > 0) {
            this.lungeCooldown--;
        }
        if (this.burrowCooldown > 0) {
            this.burrowCooldown--;
        }
        if (this.roarCooldown > 0) {
            this.roarCooldown--;
        }
        if (this.tailCooldown > 0) {
            this.tailCooldown--;
        }
    }

    @Override
    public void startAttack(int id) {
        super.startAttack(id);
        this.lungeHit = false;
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        switch (id) {
            case CLAW -> {
                if (time == CLAW_IMPACT - 4 || this.phase() >= 2 && time == CLAW_SECOND - 4) {
                    this.playSound(Dushi.sound("entity.overseer.swing"), 1.6F, 1.3F);
                    Vec3 step = this.facing().scale(0.3);
                    this.setDeltaMovement(step.x, this.getDeltaMovement().y, step.z);
                }
                if (time == CLAW_IMPACT || this.phase() >= 2 && time == CLAW_SECOND) {
                    this.hitArc(level, 4.2, 72.0, damage * 0.9F, 0.7);
                    this.rubble(level, this.position().add(this.facing().scale(2.6)), 14, 0.7);
                }
            }
            case LUNGE -> {
                if (time == 2) {
                    this.playSound(Dushi.sound("entity.stone_eater.ambient"), 2.5F, 1.3F);
                }
                if (time >= LUNGE_START && time < LUNGE_END) {
                    Vec3 run = this.facing().scale(0.82);
                    this.setDeltaMovement(run.x, this.getDeltaMovement().y, run.z);
                    if (!this.lungeHit && this.hitArc(level, 2.2, 55.0, damage * 1.4F, 1.6) > 0) {
                        this.lungeHit = true;
                        this.playSound(Dushi.sound("entity.stone_eater.bite"), 2.2F, 1.0F);
                    }
                }
                if (time == LUNGE_END) {
                    this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
                    if (!this.lungeHit) {
                        this.playSound(Dushi.sound("entity.stone_eater.bite"), 2.0F, 0.8F);
                    }
                }
            }
            case BURROW -> this.burrowTick(level, time, damage);
            case ROAR -> {
                if (time == ROAR_SOUND) {
                    this.playSound(Dushi.sound("entity.stone_eater.roar"), 4.0F, 1.0F);
                    this.hitArc(level, 3.2, 180.0, damage * 0.3F, 1.6);
                    LivingEntity target = this.getTarget();
                    Vec3 centre = target != null ? target.position() : this.position();
                    int rocks = this.phase() >= 2 ? 13 : 8;
                    for (int i = 0; i < rocks; i++) {
                        double angle = this.random.nextDouble() * Math.PI * 2.0;
                        double far = i < 3 ? this.random.nextDouble() * 1.4 : 2.5 + this.random.nextDouble() * 8.0;
                        this.strikeLater(new Vec3(centre.x + Math.cos(angle) * far, this.getY(), centre.z + Math.sin(angle) * far),
                                24 + i * 5 + this.random.nextInt(6), 1.9, damage * 1.1F, true);
                    }
                }
            }
            default -> {
                if (time == TAIL_IMPACT - 4) {
                    this.playSound(Dushi.sound("entity.overseer.swing"), 1.8F, 0.8F);
                }
                if (time == TAIL_IMPACT) {
                    Vec3 back = this.facing().scale(-1.0);
                    this.hitArc(level, back, 4.6, 80.0, damage, 1.9, false);
                    this.rubble(level, this.position().add(back.scale(3.2)), 24, 0.8);
                }
            }
        }
    }

    private void burrowTick(ServerLevel level, int time, float damage) {
        if (time == 1) {
            this.playSound(Dushi.sound("entity.stone_eater.burrow"), 3.0F, 1.0F);
        }
        if (time < BURROW_UNDER) {
            this.rubble(level, this.position(), 12, 1.2);
        } else if (time < BURROW_STOP) {
            // under the floor it goes straight for its prey; the dust jumping over it is all that shows
            LivingEntity target = this.getTarget();
            if (target != null) {
                Vec3 to = new Vec3(target.getX() - this.getX(), 0.0, target.getZ() - this.getZ());
                if (to.lengthSqr() > 0.6) {
                    Vec3 go = to.normalize().scale(Math.min(0.46, to.length() * 0.5));
                    this.setDeltaMovement(go.x, this.getDeltaMovement().y, go.z);
                } else {
                    this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
                }
            }
            if (time % 2 == 0) {
                this.rubble(level, this.position(), 6, 0.7);
            }
            if (time % 14 == 0) {
                this.playSound(Dushi.sound("entity.stone_eater.burrow"), 1.6F, 1.3F);
            }
        } else if (time < BURROW_ERUPT) {
            this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
            this.rubble(level, this.position(), 8 + (time - BURROW_STOP) * 2, 1.4);
        } else if (time == BURROW_ERUPT) {
            Vec3 at = this.position();
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(3.0, 1.5, 3.0), this::isFoe)) {
                double distance = Math.sqrt(victim.distanceToSqr(at.x, victim.getY(), at.z)) - victim.getBbWidth() * 0.5;
                if (distance <= 3.4 && victim.hurtServer(level, this.damageSources().mobAttack(this), damage * 1.5F)) {
                    Vec3 away = new Vec3(victim.getX() - at.x, 0.0, victim.getZ() - at.z);
                    away = away.lengthSqr() < 1.0E-4 ? Vec3.ZERO : away.normalize().scale(0.5);
                    shove(victim, new Vec3(away.x, 0.85, away.z));
                }
            }
            this.rubble(level, at, 120, 1.8);
            this.playSound(Dushi.sound("entity.stone_eater.erupt"), 4.0F, 1.0F);
        }
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return this.under() || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING) || super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean isPushable() {
        return !this.under() && super.isPushable();
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.spawnAtLocation(level, new ItemStack(Dushi.STONE_EATER_SOUL.get()));
        this.spawnAtLocation(level, new ItemStack(Dushi.FLASK_SHARD.get()));
        this.leave(level, Dushi.GREAT_SOUL.get(), 1, 2);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            BlockPos home = MobMarkBlockEntity.homeOf(this);
            FogGateBlock.lift(level, home != null ? home : this.blockPosition(), 40);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.under() ? null : Dushi.sound("entity.stone_eater.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.stone_eater.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.stone_eater.death");
    }

    @Override
    protected float getSoundVolume() {
        return 2.5F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(Dushi.sound("entity.unhewn.step"), 1.2F, 0.6F);
    }
}
