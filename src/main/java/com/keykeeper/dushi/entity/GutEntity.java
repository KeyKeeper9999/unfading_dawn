package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * A gut: a beast the size of a bull, with no head - the neck ends in a stump that healed long ago. Its back opens
 * from end to end, and that is its mouth. There are two or three of them in the whole of the Sky Beneath, and each
 * has a stretch of it to itself.
 * <p>
 * It runs whoever it finds down, in a straight line it cannot leave; what it meets at the end of the line it meets
 * with the stump, and stands a while with its back open after (the time to strike it). Near, it rears and comes down
 * with both fists; and whoever gets beside it, or on it, is in reach of the back.
 */
public class GutEntity extends SoulsMob {
    public static final int CHARGE = 1;
    public static final int CHARGE_DURATION = 78;
    public static final int CHARGE_START = 24;
    public static final int CHARGE_END = 58;
    public static final int SLAM = 2;
    public static final int SLAM_DURATION = 64;
    public static final int SLAM_IMPACT = 31;
    public static final int GAPE = 3;
    public static final int GAPE_DURATION = 52;
    public static final int GAPE_IMPACT = 23;
    /** What a charge ends in when it meets something that does not give: it stands with its back open. */
    public static final int STAGGER = 4;
    public static final int STAGGER_DURATION = 56;
    public static final double SLAM_REACH = 3.2;
    public static final double GAPE_REACH = 2.5;

    private int chargeCooldown = 60;
    private boolean chargeHit;

    public GutEntity(EntityType<? extends GutEntity> type, Level level) {
        super(type, level);
        this.xpReward = 50;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 520.0).add(Attributes.ATTACK_DAMAGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24).add(Attributes.ARMOR, 2.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
                .add(Attributes.FOLLOW_RANGE, 28.0).add(Attributes.STEP_HEIGHT, 1.1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.5));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int soulReward() {
        return 3600;
    }

    /** Standing with its back open after a charge that met a wall: a blow tells for more. */
    public boolean open() {
        return this.attackId() == STAGGER;
    }

    @Override
    protected int attackDuration(int id) {
        return switch (id) {
            case CHARGE -> CHARGE_DURATION;
            case SLAM -> SLAM_DURATION;
            case GAPE -> GAPE_DURATION;
            default -> STAGGER_DURATION;
        };
    }

    @Override
    protected int trackUntil(int id) {
        return switch (id) {
            case CHARGE -> CHARGE_START - 4;
            case SLAM -> SLAM_IMPACT - 10;
            default -> 0;
        };
    }

    @Override
    protected int attackPause(int finishedId) {
        return finishedId == STAGGER ? 6 : 16 + this.random.nextInt(14);
    }

    @Override
    public double stopDistance() {
        return 1.9;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance > 6.0 && distance < 20.0 && this.chargeCooldown <= 0 && this.hasLineOfSight(target)) {
            this.chargeCooldown = 150 + this.random.nextInt(90);
            return CHARGE;
        }
        if (distance < SLAM_REACH - 0.4) {
            Vec3 to = new Vec3(target.getX() - this.getX(), 0.0, target.getZ() - this.getZ());
            boolean before = to.lengthSqr() < 0.3 || to.normalize().dot(this.facing()) > 0.35;
            return before ? SLAM : (distance < GAPE_REACH ? GAPE : 0);
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
        super.customServerAiStep(level);
        this.chargeCooldown = Math.max(0, this.chargeCooldown - 1);
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        switch (id) {
            case CHARGE -> {
                if (time == 2) {
                    this.playSound(Dushi.sound("entity.gut.bellow"), 2.0F, 1.0F);
                }
                if (time < CHARGE_START && time % 7 == 3) {
                    Vec3 at = this.position().add(this.facing().scale(1.6));
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState()), at.x, at.y + 0.2, at.z, 10, 0.4, 0.1, 0.4, 0.1);
                }
                if (time >= CHARGE_START && time < CHARGE_END) {
                    Vec3 run = this.facing().scale(0.66);
                    this.setDeltaMovement(run.x, this.getDeltaMovement().y, run.z);
                    if (time % 5 == 0) {
                        this.playSound(Dushi.sound("entity.gut.step"), 1.6F, 0.9F + this.random.nextFloat() * 0.2F);
                    }
                    if (!this.chargeHit && this.hitArc(level, 1.9, 50.0, damage * 1.5F, 2.2) > 0) {
                        this.chargeHit = true;
                    }
                    if (this.horizontalCollision && time > CHARGE_START + 4) {
                        Vec3 at = this.position().add(this.facing().scale(1.6)).add(0.0, 1.0, 0.0);
                        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState()), at.x, at.y, at.z, 40, 0.6, 0.8, 0.6, 0.2);
                        this.playSound(Dushi.sound("entity.gut.crash"), 2.2F, 1.0F);
                        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
                        this.startAttack(STAGGER);
                    }
                }
                if (time == CHARGE_END) {
                    this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
                }
            }
            case SLAM -> {
                if (time == 4) {
                    this.playSound(Dushi.sound("entity.gut.bellow"), 1.6F, 1.25F);
                }
                if (time == SLAM_IMPACT) {
                    Vec3 at = this.position().add(this.facing().scale(2.2));
                    this.hitArc(level, this.facing(), SLAM_REACH, 30.0, damage * 1.3F, 0.5, true);
                    this.hitAround(level, at, 2.3, damage * 0.7F, 0.9, this.damageSources().mobAttack(this));
                    this.rubble(level, at, 30, 0.9);
                    this.playSound(Dushi.sound("entity.gut.slam"), 2.2F, 1.0F);
                }
            }
            case GAPE -> {
                if (time == GAPE_IMPACT - 8) {
                    this.playSound(Dushi.sound("entity.gut.gape"), 1.6F, 1.0F);
                }
                if (time == GAPE_IMPACT) {
                    this.hitAround(level, this.position(), GAPE_REACH + this.getBbWidth() * 0.5, damage, 1.4, this.damageSources().mobAttack(this));
                }
            }
            default -> {
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return super.hurtServer(level, source, damage != Float.MAX_VALUE && this.open() ? damage * 1.5F : damage);
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
        this.leave(level, Dushi.GREAT_SOUL.get(), 1, 2);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.gut.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.gut.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.gut.death");
    }

    @Override
    protected float getSoundVolume() {
        return 1.6F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(Dushi.sound("entity.gut.step"), 0.8F, 0.9F + this.random.nextFloat() * 0.2F);
    }
}
