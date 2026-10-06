package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.block.FogGateBlock;
import com.keykeeper.dushi.block.MobMarkBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The Shatun: a bear that was not let lie down. The Unsleeping kept it on a chain at the gate of their skete and beat
 * it awake whenever it began to nod - if they were not to sleep, nothing of theirs was - until it was what a bear
 * becomes that has missed its winter: skin on a frame, out of its mind, and unable to stop walking. The chain broke
 * long ago. It keeps to the hollow under the fallen fir where its den would have been. The boss of the hollow.
 * <p>
 * A paw across, from either side; the jaws, with the whole of it thrown after them; up on its hind legs and down on
 * both paws, which no shield holds. From far off it rakes the ground and comes straight, bounding - whoever is in the
 * way is thrown, but it cannot turn, and if what it meets is a tree it sits down stunned. Hurt, it roars, stops
 * waiting between its blows, and strings three of them together.
 */
public class ShatunEntity extends SoulsMob {
    public static final int SWIPE = 1;
    public static final int SWIPE_LEFT = 2;
    public static final int SWIPE_DURATION = 36;
    public static final int SWIPE_IMPACT = 15;
    public static final int BITE = 3;
    public static final int BITE_DURATION = 40;
    public static final int BITE_LUNGE = 14;
    public static final int BITE_IMPACT = 19;
    public static final int REAR = 4;
    public static final int REAR_DURATION = 72;
    public static final int REAR_IMPACT = 34;
    public static final int CHARGE = 5;
    public static final int CHARGE_DURATION = 70;
    public static final int CHARGE_START = 22;
    public static final int CHARGE_END = 52;
    public static final int ROAR = 6;
    public static final int ROAR_DURATION = 50;
    public static final int ROAR_SOUND = 14;
    /** Hurt: right paw, left paw, and down on both. */
    public static final int MAUL = 7;
    public static final int MAUL_DURATION = 84;
    public static final int MAUL_FIRST = 14;
    public static final int MAUL_SECOND = 30;
    public static final int MAUL_THIRD = 54;
    /** What a charge ends in when it meets something that does not give. */
    public static final int CRASH = 8;
    public static final int CRASH_DURATION = 64;

    public static final double PAW_REACH = 3.4;
    /** What gets through its hide of every blow: the game lets nothing have more than 1024 health (see the Wallower). */
    public static final float HIDE = 0.62F;

    private int chargeCooldown = 120;
    private int rearCooldown = 60;
    private int maulCooldown = 60;
    private boolean chargeHit;

    public ShatunEntity(EntityType<? extends ShatunEntity> type, Level level) {
        super(type, level);
        this.xpReward = 260;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1000.0).add(Attributes.ATTACK_DAMAGE, 23.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ARMOR, 4.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 44.0).add(Attributes.STEP_HEIGHT, 1.25);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public int soulReward() {
        return 30000;
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    protected int attackDuration(int id) {
        return switch (id) {
            case SWIPE, SWIPE_LEFT -> SWIPE_DURATION;
            case BITE -> BITE_DURATION;
            case REAR -> REAR_DURATION;
            case CHARGE -> CHARGE_DURATION;
            case ROAR -> ROAR_DURATION;
            case MAUL -> MAUL_DURATION;
            default -> CRASH_DURATION;
        };
    }

    @Override
    protected int trackUntil(int id) {
        return switch (id) {
            case SWIPE, SWIPE_LEFT -> SWIPE_IMPACT - 6;
            case BITE -> BITE_LUNGE - 2;
            case REAR -> REAR_IMPACT - 12;
            case CHARGE -> CHARGE_START - 4;
            case ROAR -> ROAR_DURATION;
            case MAUL -> MAUL_THIRD - 10;
            default -> 0;
        };
    }

    @Override
    protected int attackPause(int finishedId) {
        if (finishedId == CRASH) {
            return 4;
        }
        return this.phase() >= 2 ? 7 + this.random.nextInt(8) : 16 + this.random.nextInt(12);
    }

    @Override
    public double stopDistance() {
        return 2.2;
    }

    @Override
    protected void onReset() {
        super.onReset();
        this.chargeCooldown = 120;
        this.rearCooldown = 60;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance > 7.0 && distance < 22.0 && this.chargeCooldown <= 0 && this.hasLineOfSight(target)) {
            this.chargeCooldown = (this.phase() >= 2 ? 180 : 260) + this.random.nextInt(100);
            return CHARGE;
        }
        if (distance < PAW_REACH - 0.4) {
            if (this.phase() >= 2 && this.maulCooldown <= 0) {
                this.maulCooldown = 170 + this.random.nextInt(80);
                return MAUL;
            }
            if (this.rearCooldown <= 0 && this.random.nextInt(3) == 0) {
                this.rearCooldown = 140 + this.random.nextInt(80);
                return REAR;
            }
            if (this.random.nextInt(5) == 0) {
                return BITE;
            }
            // the paw of the side they stand on
            Vec3 to = new Vec3(target.getX() - this.getX(), 0.0, target.getZ() - this.getZ());
            Vec3 right = this.facing().yRot((float) (-Math.PI / 2.0));
            return to.dot(right) >= 0.0 ? SWIPE : SWIPE_LEFT;
        }
        if (distance < 5.0 && this.random.nextInt(3) != 0) {
            return BITE;
        }
        return 0;
    }

    @Override
    public void startAttack(int id) {
        super.startAttack(id);
        this.chargeHit = false;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.phase() == 1 && this.getHealth() < this.getMaxHealth() * 0.5F && this.attackId() == 0 && this.isAlive()) {
            this.setPhase(2);
            this.maulCooldown = 40;
            this.startAttack(ROAR);
        }
        super.customServerAiStep(level);
        this.chargeCooldown = Math.max(0, this.chargeCooldown - 1);
        this.rearCooldown = Math.max(0, this.rearCooldown - 1);
        this.maulCooldown = Math.max(0, this.maulCooldown - 1);
    }

    private void paw(ServerLevel level, float damage) {
        this.hitArc(level, PAW_REACH, 72.0, damage, 1.0);
        Vec3 at = this.position().add(this.facing().scale(2.6));
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y + 1.2, at.z, 1, 0.0, 0.0, 0.0, 0.0);
        this.playSound(Dushi.sound("entity.shatun.swipe"), 1.6F, 0.9F + this.random.nextFloat() * 0.2F);
    }

    /** Both paws on the ground before it: no shield holds that, and the ground jumps well beyond the paws. */
    private void slam(ServerLevel level, float damage) {
        this.hitArc(level, this.facing(), 4.6, 55.0, damage, 1.2, true);
        Vec3 at = this.position().add(this.facing().scale(3.0));
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PODZOL.defaultBlockState()), at.x, at.y + 0.2, at.z, 60, 1.3, 0.3, 1.3, 0.2);
        level.sendParticles(ParticleTypes.POOF, at.x, at.y + 0.2, at.z, 10, 1.0, 0.1, 1.0, 0.03);
        level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.shatun.slam"), this.getSoundSource(), 2.6F, 0.9F + this.random.nextFloat() * 0.15F);
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        switch (id) {
            case SWIPE, SWIPE_LEFT -> {
                if (time == SWIPE_IMPACT) {
                    this.paw(level, damage);
                }
            }
            case BITE -> {
                if (time >= BITE_LUNGE && time < BITE_IMPACT) {
                    Vec3 lunge = this.facing().scale(0.5);
                    this.setDeltaMovement(lunge.x, this.getDeltaMovement().y, lunge.z);
                }
                if (time == BITE_IMPACT) {
                    this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
                    this.hitArc(level, 3.0, 26.0, damage * 1.3F, 0.4);
                    this.playSound(Dushi.sound("entity.shatun.bite"), 1.8F, 0.9F + this.random.nextFloat() * 0.2F);
                }
            }
            case REAR -> {
                if (time == 4) {
                    this.playSound(Dushi.sound("entity.shatun.ambient"), 2.4F, 0.8F);
                }
                if (time == REAR_IMPACT) {
                    this.slam(level, damage * 1.7F);
                }
            }
            case CHARGE -> {
                if (time == 2) {
                    this.playSound(Dushi.sound("entity.shatun.roar"), 2.0F, 1.25F);
                }
                if (time < CHARGE_START && time % 6 == 3) {
                    Vec3 at = this.position().add(this.facing().scale(1.8));
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PODZOL.defaultBlockState()), at.x, at.y + 0.2, at.z, 12, 0.4, 0.1, 0.4, 0.1);
                }
                if (time >= CHARGE_START && time < CHARGE_END) {
                    Vec3 run = this.facing().scale(0.62);
                    this.setDeltaMovement(run.x, this.getDeltaMovement().y, run.z);
                    if (time % 5 == 0) {
                        this.playSound(Dushi.sound("entity.shatun.step"), 1.6F, 0.9F + this.random.nextFloat() * 0.2F);
                    }
                    if (!this.chargeHit && this.hitArc(level, 2.2, 50.0, damage * 1.5F, 2.4) > 0) {
                        this.chargeHit = true;
                    }
                    if (this.horizontalCollision && time > CHARGE_START + 4) {
                        // a tree: it does not give
                        Vec3 at = this.position().add(this.facing().scale(1.8)).add(0.0, 1.2, 0.0);
                        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SPRUCE_LOG.defaultBlockState()), at.x, at.y, at.z, 40, 0.6, 0.8, 0.6, 0.2);
                        this.playSound(Dushi.sound("entity.shatun.crash"), 2.4F, 1.0F);
                        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
                        this.startAttack(CRASH);
                    }
                }
                if (time == CHARGE_END) {
                    this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
                }
            }
            case ROAR -> {
                if (time == ROAR_SOUND - 6) {
                    this.playSound(Dushi.sound("entity.shatun.roar"), 4.0F, 1.0F);
                }
                if (time == ROAR_SOUND) {
                    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(7.0, 2.0, 7.0), this::isFoe)) {
                        Vec3 away = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ());
                        double far = away.length();
                        if (far < 7.5) {
                            away = far < 1.0E-3 ? this.facing() : away.scale(1.0 / far);
                            shove(victim, new Vec3(away.x * 0.9, 0.36, away.z * 0.9));
                        }
                    }
                    level.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 1.5, this.getZ(), 30, 2.0, 0.6, 2.0, 0.08);
                }
            }
            case MAUL -> {
                if (time == MAUL_FIRST - 3 || time == MAUL_SECOND - 3 || time == MAUL_THIRD - 8) {
                    Vec3 step = this.facing().scale(0.42);
                    this.setDeltaMovement(step.x, this.getDeltaMovement().y, step.z);
                }
                if (time == MAUL_FIRST || time == MAUL_SECOND) {
                    this.paw(level, damage * 0.9F);
                }
                if (time == MAUL_THIRD) {
                    this.slam(level, damage * 1.5F);
                }
            }
            default -> this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        // (stunned, it is all open: the hide does not help it then)
        float through = this.attackId() == CRASH ? 1.0F : HIDE;
        return super.hurtServer(level, source, damage == Float.MAX_VALUE ? damage : damage * through);
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING) || super.isInvulnerableTo(level, source);
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.spawnAtLocation(level, new ItemStack(Dushi.SHATUN_SOUL.get()));
        this.spawnAtLocation(level, new ItemStack(Dushi.FLASK_SHARD.get()));
        this.leave(level, Dushi.GREAT_SOUL.get(), 2, 3);
        this.leave(level, Dushi.SHATUN_HIDE.get(), 3, 5);
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
        return Dushi.sound("entity.shatun.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.shatun.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.shatun.death");
    }

    @Override
    protected float getSoundVolume() {
        return 2.2F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(Dushi.sound("entity.shatun.step"), 0.8F, 0.9F + this.random.nextFloat() * 0.2F);
    }
}
