package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The blind crawler of the lower workings: what became of the diggers who were left below when the lamps went out. It
 * has no eyes and does not need them: it hears. Whoever walks upright is heard from far off; whoever creeps (sneaks)
 * can pass within a few steps of it. When it hears somebody it shrieks, and every other crawler in earshot comes too.
 * <p>
 * Three quick rakes of its long arms, each a step closer, or a spring from a distance.
 */
public class BlindCrawlerEntity extends SoulsMob {
    /** Three rakes: left, right, and both together. */
    public static final int FLURRY = 1;
    public static final int FLURRY_DURATION = 32;
    private static final int[] FLURRY_HITS = {8, 13, 19};
    public static final int LEAP = 2;
    public static final int LEAP_DURATION = 34;
    public static final int LEAP_JUMP = 12;
    /** The shriek of one that has heard something: the others answer it. */
    public static final int SHRIEK = 3;
    public static final int SHRIEK_DURATION = 22;
    private static final int SHRIEK_SOUND = 6;

    /** How far off it hears somebody walking, and somebody creeping. */
    public static final double HEARS_WALKING = 13.0;
    public static final double HEARS_CREEPING = 3.5;
    private static final double PACK_RANGE = 12.0;

    private int leapCooldown = 30;
    private boolean leapHit;
    private boolean shrieked;

    public BlindCrawlerEntity(EntityType<? extends BlindCrawlerEntity> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 38.0).add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.33).add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.5));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 6, false, false, (entity, level) -> this.hears(entity)));
    }

    /** Whether it can hear this one from where it is. */
    public boolean hears(LivingEntity entity) {
        return this.hears(entity.isShiftKeyDown() || entity.isCrouching(), this.distanceTo(entity));
    }

    /** The rule of its ears, by itself (the self-test asks it directly). */
    public boolean hears(boolean creeping, double distance) {
        return distance <= (creeping ? HEARS_CREEPING : HEARS_WALKING);
    }

    @Override
    public int soulReward() {
        return 240;
    }

    @Override
    protected int attackDuration(int id) {
        return id == LEAP ? LEAP_DURATION : id == SHRIEK ? SHRIEK_DURATION : FLURRY_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == LEAP ? LEAP_JUMP : id == SHRIEK ? SHRIEK_DURATION : FLURRY_HITS[2] - 2;
    }

    @Override
    protected int attackPause(int finishedId) {
        return finishedId == SHRIEK ? 2 : finishedId == LEAP ? 16 + this.random.nextInt(8) : 14 + this.random.nextInt(10);
    }

    @Override
    public double stopDistance() {
        return 1.2;
    }

    @Override
    protected void onReset() {
        super.onReset();
        this.shrieked = false;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (!this.shrieked) {
            this.shrieked = true;
            return SHRIEK;
        }
        if (distance > 3.2 && distance < 8.0 && this.leapCooldown <= 0 && this.onGround() && this.hasLineOfSight(target)) {
            this.leapCooldown = 80 + this.random.nextInt(40);
            return LEAP;
        }
        return distance < 2.0 ? FLURRY : 0;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.leapCooldown > 0) {
            this.leapCooldown--;
        }
        if (this.getTarget() == null && this.attackId() == 0 && this.tickCount % 40 == 0) {
            this.shrieked = false;
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
        if (id == SHRIEK) {
            if (time == SHRIEK_SOUND) {
                this.playSound(Dushi.sound("entity.crawler.screech"), 2.2F, 0.9F + this.random.nextFloat() * 0.2F);
                LivingEntity target = this.getTarget();
                if (target != null) {
                    for (BlindCrawlerEntity other : level.getEntitiesOfClass(BlindCrawlerEntity.class, this.getBoundingBox().inflate(PACK_RANGE, 5.0, PACK_RANGE),
                            mob -> mob != this && mob.isAlive() && mob.getTarget() == null)) {
                        other.shrieked = true;                    // one shriek is enough for the pack
                        other.setTarget(target);
                    }
                }
            }
        } else if (id == FLURRY) {
            for (int i = 0; i < FLURRY_HITS.length; i++) {
                if (time == FLURRY_HITS[i] - 2) {
                    Vec3 step = this.facing().scale(0.28);
                    this.setDeltaMovement(step.x, this.getDeltaMovement().y, step.z);
                }
                if (time == FLURRY_HITS[i]) {
                    this.playSound(Dushi.sound("entity.hound.bite"), 0.8F, 1.5F + 0.15F * i);
                    this.hitArc(level, 2.2, 60.0, damage * (i == 2 ? 1.0F : 0.6F), i == 2 ? 0.6 : 0.1);
                }
            }
        } else if (id == LEAP) {
            if (time == 2) {
                this.playSound(Dushi.sound("entity.crawler.ambient"), 1.4F, 1.4F);
            }
            if (time == LEAP_JUMP) {
                LivingEntity target = this.getTarget();
                Vec3 to = target == null ? this.facing().scale(5.0) : target.position().subtract(this.position());
                Vec3 flat = new Vec3(to.x, 0.0, to.z);
                double length = Math.min(8.0, flat.length() + 0.5);
                Vec3 jump = flat.lengthSqr() < 1.0E-4 ? Vec3.ZERO : flat.normalize().scale(length / 8.5);
                this.setDeltaMovement(jump.x, 0.44, jump.z);
                this.needsSync = true;
                this.playSound(Dushi.sound("entity.crawler.screech"), 1.2F, 1.5F);
            }
            if (!this.leapHit && time > LEAP_JUMP && time < LEAP_JUMP + 12 && this.hitArc(level, 1.6, 75.0, damage * 1.3F, 0.8) > 0) {
                this.leapHit = true;
            }
        }
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.leaveMaybe(level, Dushi.NAMELESS_SOUL.get(), 0.12F);
        this.leaveMaybe(level, Dushi.TATTERED_CLOTH.get(), 0.3F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.crawler.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.crawler.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.crawler.death");
    }
}
