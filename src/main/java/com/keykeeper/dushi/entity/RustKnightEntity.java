package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The rust knight: a head taller than a man, in armour eaten by rust, with a greatsword and a tower shield.
 * Slow and readable, but every blow hurts. Three moves: an overhead chop, a wide sweep and a lunge from a distance.
 */
public class RustKnightEntity extends SoulsMob {
    public static final int CHOP = 1;
    public static final int CHOP_DURATION = 34;
    private static final int CHOP_IMPACT = 17;
    public static final int SWEEP = 2;
    public static final int SWEEP_DURATION = 32;
    private static final int SWEEP_IMPACT = 15;
    public static final int LUNGE = 3;
    public static final int LUNGE_DURATION = 32;
    private static final int LUNGE_DASH_FROM = 10;
    private static final int LUNGE_IMPACT = 15;

    private int lungeCooldown = 60;

    public RustKnightEntity(EntityType<? extends RustKnightEntity> type, Level level) {
        super(type, level);
        this.xpReward = 30;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 90.0).add(Attributes.ATTACK_DAMAGE, 9.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24).add(Attributes.ARMOR, 12.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.7)
                .add(Attributes.FOLLOW_RANGE, 28.0);
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
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Dushi.RUST_GREATSWORD.get()));
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Dushi.TOWER_SHIELD.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.25F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.15F);
        return data;
    }

    @Override
    public int soulReward() {
        return 600;
    }

    @Override
    protected int guardBreakHits() {
        return 4;
    }

    @Override
    protected int attackDuration(int id) {
        return switch (id) {
            case CHOP -> CHOP_DURATION;
            case SWEEP -> SWEEP_DURATION;
            default -> LUNGE_DURATION;
        };
    }

    @Override
    protected int trackUntil(int id) {
        return switch (id) {
            case CHOP -> CHOP_IMPACT - 5;
            case SWEEP -> SWEEP_IMPACT - 5;
            default -> LUNGE_DASH_FROM;
        };
    }

    @Override
    protected int attackPause(int finishedId) {
        return 16 + this.random.nextInt(12);
    }

    @Override
    public double stopDistance() {
        return 2.0;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance < 2.9) {
            return this.random.nextBoolean() ? CHOP : SWEEP;
        }
        if (distance > 4.0 && distance < 8.0 && this.lungeCooldown <= 0 && this.hasLineOfSight(target)) {
            this.lungeCooldown = 120;
            return LUNGE;
        }
        return 0;
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
        switch (id) {
            case CHOP -> {
                if (time == CHOP_IMPACT - 4) {
                    this.playSound(Dushi.sound("player.weapon_swing_heavy"), 1.2F, 0.8F);
                }
                if (time == CHOP_IMPACT) {
                    this.swingArm();
                    this.hitArc(level, 3.4, 40.0, damage * 1.35F, 0.6);
                    Vec3 at = this.position().add(this.facing().scale(2.4));
                    level.sendParticles(ParticleTypes.POOF, at.x, at.y + 0.1, at.z, 10, 0.4, 0.05, 0.4, 0.02);
                }
            }
            case SWEEP -> {
                if (time == SWEEP_IMPACT - 3) {
                    this.playSound(Dushi.sound("player.weapon_swing_heavy"), 1.2F, 1.0F);
                }
                if (time == SWEEP_IMPACT) {
                    this.swingArm();
                    this.hitArc(level, 3.6, 100.0, damage, 1.1);
                    level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + this.facing().x * 2.0, this.getY() + 1.4, this.getZ() + this.facing().z * 2.0,
                            1, 0.0, 0.0, 0.0, 0.0);
                }
            }
            default -> {
                if (time >= LUNGE_DASH_FROM && time < LUNGE_IMPACT) {
                    Vec3 dash = this.facing().scale(0.85);
                    this.setDeltaMovement(dash.x, this.getDeltaMovement().y, dash.z);
                }
                if (time == LUNGE_DASH_FROM) {
                    this.playSound(Dushi.sound("player.weapon_swing_heavy"), 1.2F, 1.2F);
                }
                if (time == LUNGE_IMPACT) {
                    this.swingArm();
                    this.hitArc(level, 3.2, 35.0, damage * 1.2F, 0.9);
                }
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.spawnAtLocation(level, new ItemStack(Dushi.GREAT_SOUL.get()));
        if (this.random.nextInt(3) == 0) {
            this.spawnAtLocation(level, new ItemStack(Dushi.TAR_POT.get(), 2 + this.random.nextInt(3)));
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.rust_knight.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.rust_knight.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.rust_knight.death");
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(Dushi.sound("entity.rust_knight.step"), 0.5F, 0.9F + this.random.nextFloat() * 0.2F);
    }
}
