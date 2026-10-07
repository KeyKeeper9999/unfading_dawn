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
 * The Helmsman of the Daybreak: the master of the ship that lies at the end of the long pier and cannot cast off. On
 * the morning the day was stopped she was to go out and bring the others in; the anchor would not come up. He has it
 * now - he tore it out of the bottom, and carries it by the ring, the chain across his chest - and the ship is where
 * she was. He walks her deck. The boss of the wharf.
 * <p>
 * The anchor comes off his shoulder and round, the width of the deck; or up and down on the planks, which no shield
 * holds; or he drives his shoulder in. From further off it is cast - it goes up and comes down where his enemy stood
 * when it left his hand - and then he must get it back, hand over hand along the chain, a long time, and what strikes
 * him then strikes him fairly. Hurt, he shouts the order nobody has obeyed since that morning, and after that he goes
 * round with the anchor at arm's length, four times, coming on - and stands giddy when it is done.
 */
public class HelmsmanEntity extends SoulsMob {
    public static final int SWEEP = 1;
    public static final int SWEEP_DURATION = 54;
    public static final int SWEEP_IMPACT = 25;
    public static final int SLAM = 2;
    public static final int SLAM_DURATION = 66;
    public static final int SLAM_IMPACT = 32;
    public static final int CAST = 3;
    public static final int CAST_DURATION = 104;
    public static final int CAST_RELEASE = 28;
    /** The tick by which the anchor is back in his hand. */
    public static final int CAST_HOME = 90;
    public static final int SHOULDER = 4;
    public static final int SHOULDER_DURATION = 38;
    public static final int SHOULDER_START = 12;
    public static final int SHOULDER_END = 24;
    public static final int BELLOW = 5;
    public static final int BELLOW_DURATION = 50;
    public static final int BELLOW_SOUND = 14;
    public static final int WHIRL = 6;
    public static final int WHIRL_DURATION = 100;
    public static final int[] WHIRL_HITS = {28, 42, 56, 70};
    public static final double REACH = 4.8;
    public static final double WHIRL_RADIUS = 4.6;
    /** The share of a blow that gets through to him (nothing has more than 1024 health; he needs more). */
    public static final float HIDE = 0.44F;

    private int castCooldown = 100;
    private int whirlCooldown = 60;
    private int shoulderCooldown = 80;
    private boolean shouldered;

    public HelmsmanEntity(EntityType<? extends HelmsmanEntity> type, Level level) {
        super(type, level);
        this.xpReward = 300;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1000.0).add(Attributes.ATTACK_DAMAGE, 26.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.ARMOR, 6.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
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
        return 24000;
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    protected int attackDuration(int id) {
        return switch (id) {
            case SWEEP -> SWEEP_DURATION;
            case SLAM -> SLAM_DURATION;
            case CAST -> CAST_DURATION;
            case SHOULDER -> SHOULDER_DURATION;
            case BELLOW -> BELLOW_DURATION;
            default -> WHIRL_DURATION;
        };
    }

    @Override
    protected int trackUntil(int id) {
        return switch (id) {
            case SWEEP -> SWEEP_IMPACT - 9;
            case SLAM -> SLAM_IMPACT - 12;
            case CAST -> CAST_RELEASE - 4;
            case SHOULDER -> SHOULDER_START - 3;
            case BELLOW -> BELLOW_DURATION;
            default -> WHIRL_HITS[WHIRL_HITS.length - 1];
        };
    }

    @Override
    protected int attackPause(int finishedId) {
        if (finishedId == CAST || finishedId == WHIRL) {
            return 6;
        }
        return this.phase() >= 2 ? 9 + this.random.nextInt(8) : 16 + this.random.nextInt(12);
    }

    @Override
    public double stopDistance() {
        return 2.6;
    }

    @Override
    protected void onReset() {
        super.onReset();
        this.castCooldown = 100;
        this.whirlCooldown = 60;
        this.shoulderCooldown = 80;
    }

    /** Getting the anchor back, or giddy after going round with it: when he can do nothing about a blow. */
    public boolean open() {
        int id = this.attackId();
        return id == CAST && this.attackTime > CAST_RELEASE + 8 && this.attackTime < CAST_HOME
                || id == WHIRL && this.attackTime > WHIRL_HITS[WHIRL_HITS.length - 1] + 4;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance > 6.0 && distance < 20.0 && this.castCooldown <= 0 && this.hasLineOfSight(target)) {
            this.castCooldown = (this.phase() >= 2 ? 200 : 280) + this.random.nextInt(100);
            return CAST;
        }
        if (this.phase() >= 2 && this.whirlCooldown <= 0 && distance < 7.0) {
            this.whirlCooldown = 260 + this.random.nextInt(100);
            return WHIRL;
        }
        if (distance < REACH - 0.6) {
            int roll = this.random.nextInt(10);
            if (distance < 2.4 && this.shoulderCooldown <= 0 && roll < 4) {
                this.shoulderCooldown = 120 + this.random.nextInt(60);
                return SHOULDER;
            }
            return roll < 6 ? SWEEP : SLAM;
        }
        if (distance < 6.5 && this.shoulderCooldown <= 0 && this.random.nextInt(3) == 0) {
            this.shoulderCooldown = 120 + this.random.nextInt(60);
            return SHOULDER;
        }
        return 0;
    }

    @Override
    public void startAttack(int id) {
        super.startAttack(id);
        this.shouldered = false;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.phase() == 1 && this.getHealth() < this.getMaxHealth() * 0.5F && this.attackId() == 0 && this.isAlive()) {
            this.setPhase(2);
            this.whirlCooldown = 30;
            this.startAttack(BELLOW);
        }
        super.customServerAiStep(level);
        this.castCooldown = Math.max(0, this.castCooldown - 1);
        this.whirlCooldown = Math.max(0, this.whirlCooldown - 1);
        this.shoulderCooldown = Math.max(0, this.shoulderCooldown - 1);
    }

    private void planks(ServerLevel level, Vec3 at, int count, double spread) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SPRUCE_PLANKS.defaultBlockState()), at.x, at.y + 0.2, at.z, count, spread, 0.3, spread, 0.2);
        level.sendParticles(ParticleTypes.POOF, at.x, at.y + 0.2, at.z, Math.max(1, count / 6), spread, 0.1, spread, 0.03);
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        switch (id) {
            case SWEEP -> {
                if (time == SWEEP_IMPACT - 5) {
                    this.playSound(Dushi.sound("entity.helmsman.swing"), 2.0F, 0.95F + this.random.nextFloat() * 0.1F);
                }
                if (time == SWEEP_IMPACT) {
                    this.hitArc(level, REACH, 100.0, damage, 1.5);
                    Vec3 at = this.position().add(this.facing().scale(3.2));
                    level.sendParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y + 1.3, at.z, 2, 0.6, 0.0, 0.6, 0.0);
                }
            }
            case SLAM -> {
                if (time == SLAM_IMPACT - 6) {
                    this.playSound(Dushi.sound("entity.helmsman.swing"), 2.0F, 0.75F);
                }
                if (time == SLAM_IMPACT) {
                    this.hitArc(level, this.facing(), REACH + 0.2, 20.0, damage * 1.6F, 0.5, true);
                    Vec3 at = this.position().add(this.facing().scale(3.6));
                    this.hitAround(level, at, 2.4, damage * 0.6F, 0.8, this.damageSources().mobAttack(this));
                    this.planks(level, at, 50, 0.9);
                    level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.helmsman.anchor"), this.getSoundSource(), 2.8F, 0.85F + this.random.nextFloat() * 0.1F);
                }
            }
            case CAST -> {
                LivingEntity target = this.getTarget();
                if (time == CAST_RELEASE - 8) {
                    this.playSound(Dushi.sound("entity.helmsman.swing"), 2.0F, 0.85F);
                }
                if (time == CAST_RELEASE) {
                    AnchorEntity anchor = new AnchorEntity(level, this, damage * 1.3F);
                    anchor.setPos(this.getX(), this.getY() + this.getBbHeight() * 0.8, this.getZ());
                    Vec3 to = target != null ? target.position().subtract(anchor.position()) : this.facing().scale(8.0).add(0.0, -2.0, 0.0);
                    // thrown high, to come down where they stand now: it is in the air long enough to be walked out from
                    // under (the game takes a hundredth off a thrown thing's speed every tick: a little is added for that)
                    double flight = Math.min(36.0, 24.0 + to.horizontalDistance() * 0.5);
                    anchor.setDeltaMovement(to.x / flight * 1.14, (to.y + 0.5 * 0.06 * flight * flight) / flight * 1.06, to.z / flight * 1.14);
                    level.addFreshEntity(anchor);
                }
                if (time > CAST_RELEASE + 8 && time < CAST_HOME && time % 9 == 0) {
                    this.playSound(Dushi.sound("entity.helmsman.chain"), 1.6F, 0.9F + this.random.nextFloat() * 0.2F);
                }
            }
            case SHOULDER -> {
                if (time >= SHOULDER_START && time < SHOULDER_END) {
                    Vec3 run = this.facing().scale(0.5);
                    this.setDeltaMovement(run.x, this.getDeltaMovement().y, run.z);
                    if (!this.shouldered && this.hitArc(level, 1.9, 60.0, damage * 0.8F, 2.2) > 0) {
                        this.shouldered = true;
                    }
                }
                if (time == SHOULDER_END) {
                    this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
                }
            }
            case BELLOW -> {
                if (time == BELLOW_SOUND - 8) {
                    this.playSound(Dushi.sound("entity.helmsman.shout"), 4.0F, 1.0F);
                }
                if (time == BELLOW_SOUND) {
                    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0, 2.0, 6.0), this::isFoe)) {
                        Vec3 away = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ());
                        double far = away.length();
                        if (far < 6.5) {
                            away = far < 1.0E-3 ? this.facing() : away.scale(1.0 / far);
                            shove(victim, new Vec3(away.x * 0.8, 0.34, away.z * 0.8));
                        }
                    }
                    level.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 1.8, this.getZ(), 30, 2.0, 0.6, 2.0, 0.08);
                }
            }
            default -> {
                if (time == 6) {
                    this.playSound(Dushi.sound("entity.helmsman.shout"), 3.0F, 1.2F);
                }
                int last = WHIRL_HITS[WHIRL_HITS.length - 1];
                if (time > WHIRL_HITS[0] - 12 && time <= last) {
                    Vec3 step = this.facing().scale(0.13);
                    this.setDeltaMovement(step.x, this.getDeltaMovement().y, step.z);
                }
                for (int hit : WHIRL_HITS) {
                    if (time == hit) {
                        this.hitAround(level, this.position(), WHIRL_RADIUS, damage * 0.9F, 1.3, this.damageSources().mobAttack(this));
                        this.playSound(Dushi.sound("entity.helmsman.swing"), 2.0F, 1.1F);
                        Vec3 at = this.position().add(this.facing().scale(3.0));
                        level.sendParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y + 1.0, at.z, 2, 0.8, 0.0, 0.8, 0.0);
                    }
                }
                if (time == last + 1) {
                    this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
                }
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        // (with his hands full of chain, or giddy, he is open: his coat does not help him then)
        float through = this.open() ? Math.min(1.0F, HIDE * 2.0F) : HIDE;
        return super.hurtServer(level, source, damage == Float.MAX_VALUE ? damage : damage * through);
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING) || source.is(DamageTypes.DROWN) || super.isInvulnerableTo(level, source);
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
        this.spawnAtLocation(level, new ItemStack(Dushi.HELMSMAN_SOUL.get()));
        this.spawnAtLocation(level, new ItemStack(Dushi.FLASK_SHARD.get()));
        this.leave(level, Dushi.GREAT_SOUL.get(), 2, 3);
        this.leave(level, Dushi.TARRED_ROPE.get(), 3, 5);
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
        return Dushi.sound("entity.helmsman.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.helmsman.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.helmsman.death");
    }

    @Override
    protected float getSoundVolume() {
        return 2.2F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(Dushi.sound("entity.helmsman.chain"), 0.5F, 0.8F + this.random.nextFloat() * 0.2F);
    }
}
