package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
 * A gut: a mound the length of two men, with no head and no legs, shut in a shell of two halves that meet along its
 * back; it goes on hands, a row of them down each side, and no faster than they can shuffle. There are two or three
 * of them in the whole of the Sky Beneath, and each has a stretch of it to itself.
 * <p>
 * It is slow, and nothing turns it once it has begun a thing. Shut, most of a blow is lost on the shell
 * ({@link #SHELL}); and the shell is its mouth: it has to open it to do anything, and what is under it is soft. It
 * gathers itself and heaves its whole weight forward a few paces (and stands open if that ends against a wall); near,
 * it rears and comes down, and stands open a moment after; and whoever gets beside it or behind it, it opens for, and
 * the arms that lie in its mouth come out over the edges of the shell and take what they find.
 */
public class GutEntity extends SoulsMob {
    public static final int HEAVE = 1;
    public static final int HEAVE_DURATION = 86;
    public static final int HEAVE_START = 34;
    public static final int HEAVE_END = 50;
    public static final int SLAM = 2;
    public static final int SLAM_DURATION = 88;
    public static final int SLAM_IMPACT = 34;
    /** Until this tick after a slam the shell stands open. */
    public static final int SLAM_SHUT = 68;
    public static final int GAPE = 3;
    public static final int GAPE_DURATION = 80;
    public static final int GAPE_IMPACT = 30;
    public static final int GAPE_SHUT = 64;
    /** What a heave ends in when it meets something that does not give: it stands with its shell open. */
    public static final int STAGGER = 4;
    public static final int STAGGER_DURATION = 64;
    public static final double SLAM_REACH = 3.6;
    public static final double GAPE_REACH = 2.9;
    /** The share of a blow that gets through the shell while it is shut, and how much more one tells while it is open. */
    public static final float SHELL = 0.5F;
    public static final float SOFT = 1.3F;
    private static final double HEAVE_SPEED = 0.44;

    private int heaveCooldown = 50;
    private boolean heaveHit;

    public GutEntity(EntityType<? extends GutEntity> type, Level level) {
        super(type, level);
        this.xpReward = 60;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 540.0).add(Attributes.ATTACK_DAMAGE, 36.0)
                .add(Attributes.MOVEMENT_SPEED, 0.14).add(Attributes.ARMOR, 4.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
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
        return 4200;
    }

    /** Its shell open: when a blow tells. */
    public boolean open() {
        int id = this.attackId();
        int time = this.attackTime;
        return id == STAGGER || (id == SLAM && time > SLAM_IMPACT && time < SLAM_SHUT) || (id == GAPE && time > GAPE_IMPACT - 10 && time < GAPE_SHUT);
    }

    @Override
    protected int attackDuration(int id) {
        return switch (id) {
            case HEAVE -> HEAVE_DURATION;
            case SLAM -> SLAM_DURATION;
            case GAPE -> GAPE_DURATION;
            default -> STAGGER_DURATION;
        };
    }

    /** Nothing turns it once it has begun: it looks where it means to go only while it gathers itself. */
    @Override
    protected int trackUntil(int id) {
        return switch (id) {
            case HEAVE -> HEAVE_START - 14;
            case SLAM -> SLAM_IMPACT - 22;
            default -> 0;
        };
    }

    @Override
    protected int attackPause(int finishedId) {
        return finishedId == STAGGER ? 10 : 22 + this.random.nextInt(16);
    }

    @Override
    public double stopDistance() {
        return 2.0;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        Vec3 to = new Vec3(target.getX() - this.getX(), 0.0, target.getZ() - this.getZ());
        boolean before = to.lengthSqr() < 0.3 || to.normalize().dot(this.facing()) > 0.45;
        if (distance > 3.6 && distance < 11.0 && this.heaveCooldown <= 0 && before && this.hasLineOfSight(target)) {
            this.heaveCooldown = 130 + this.random.nextInt(80);
            return HEAVE;
        }
        if (distance < SLAM_REACH - 0.6 && before) {
            return SLAM;
        }
        return distance < GAPE_REACH - 0.3 ? GAPE : 0;
    }

    @Override
    public void startAttack(int id) {
        super.startAttack(id);
        this.heaveHit = false;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.heaveCooldown = Math.max(0, this.heaveCooldown - 1);
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        switch (id) {
            case HEAVE -> {
                if (time == 2) {
                    this.playSound(Dushi.sound("entity.gut.bellow"), 2.0F, 0.85F);
                }
                if (time < HEAVE_START && time % 6 == 3) {
                    // its hands dig in under it
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState()), this.getX(), this.getY() + 0.2, this.getZ(), 12, 1.1, 0.1, 1.1, 0.1);
                }
                if (time >= HEAVE_START && time < HEAVE_END) {
                    Vec3 run = this.facing().scale(HEAVE_SPEED);
                    this.setDeltaMovement(run.x, this.getDeltaMovement().y, run.z);
                    if (time % 3 == 0) {
                        this.playSound(Dushi.sound("entity.gut.step"), 1.8F, 0.75F + this.random.nextFloat() * 0.15F);
                    }
                    if (!this.heaveHit && this.hitArc(level, this.facing(), 1.6, 60.0, damage * 1.5F, 2.4, true) > 0) {
                        this.heaveHit = true;
                    }
                    if (this.horizontalCollision && time > HEAVE_START + 3) {
                        Vec3 at = this.position().add(this.facing().scale(1.9)).add(0.0, 1.0, 0.0);
                        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState()), at.x, at.y, at.z, 40, 0.6, 0.8, 0.6, 0.2);
                        this.playSound(Dushi.sound("entity.gut.crash"), 2.2F, 1.0F);
                        this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
                        this.startAttack(STAGGER);
                    }
                }
                if (time == HEAVE_END) {
                    this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
                    this.rubble(level, this.position().add(this.facing().scale(1.4)), 18, 0.8);
                }
            }
            case SLAM -> {
                if (time == 4) {
                    this.playSound(Dushi.sound("entity.gut.bellow"), 1.6F, 1.1F);
                }
                if (time == SLAM_IMPACT) {
                    Vec3 at = this.position().add(this.facing().scale(2.6));
                    this.hitArc(level, this.facing(), SLAM_REACH, 34.0, damage * 1.4F, 0.6, true);
                    this.hitAround(level, at, 2.6, damage * 0.7F, 1.0, this.damageSources().mobAttack(this));
                    this.rubble(level, at, 36, 1.1);
                    this.playSound(Dushi.sound("entity.gut.slam"), 2.2F, 0.9F);
                }
            }
            case GAPE -> {
                if (time == GAPE_IMPACT - 16) {
                    this.playSound(Dushi.sound("entity.gut.gape"), 1.6F, 0.9F);
                }
                if (time == GAPE_IMPACT) {
                    this.hitAround(level, this.position(), GAPE_REACH + this.getBbWidth() * 0.5, damage, 0.0, this.damageSources().mobAttack(this));
                }
            }
            default -> {
            }
        }
    }

    @Override
    protected void struck(ServerLevel level, LivingEntity victim) {
        if (this.attackId() == GAPE) {
            // taken by what came out of the mouth: held where they are, and drawn in a little
            victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 3, false, false, true), this);
            Vec3 to = new Vec3(this.getX() - victim.getX(), 0.0, this.getZ() - victim.getZ());
            if (to.lengthSqr() > 1.0) {
                to = to.normalize();
                shove(victim, new Vec3(to.x * 0.35, 0.12, to.z * 0.35));
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (damage == Float.MAX_VALUE || source.getDirectEntity() == null) {
            return super.hurtServer(level, source, damage);
        }
        if (this.open()) {
            return super.hurtServer(level, source, damage * SOFT);
        }
        // the shell takes it: chips of it, and a knock
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BLACKSTONE.defaultBlockState()), this.getX(), this.getY() + this.getBbHeight() * 0.8, this.getZ(),
                6, 0.6, 0.2, 0.6, 0.05);
        this.playSound(Dushi.sound("entity.gut.shell"), 1.2F, 0.9F + this.random.nextFloat() * 0.2F);
        return super.hurtServer(level, source, damage * SHELL);
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
