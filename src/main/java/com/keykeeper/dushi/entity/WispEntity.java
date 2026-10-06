package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.block.MobMarkBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * A wisp of the Mire: a light at the height of a man's face that is seen a long way off across the water, and, close
 * to, the skull of something small with a candle-end of green flame in its crown.
 * <p>
 * It does not come near. It keeps its distance and drifts round whoever it has seen, and every few breaths the flame
 * stands up tall and it throws a slow tongue of the same fire. Two blows put it out; struck, it darts away. Alone it is
 * a nuisance. It is seldom alone, and what walks under it is not slow.
 */
public class WispEntity extends SoulsMob {
    public static final int BOLT = 1;
    public static final int BOLT_DURATION = 42;
    public static final int BOLT_RELEASE = 22;
    /** How far from its prey it keeps, and how high over the ground. */
    private static final double KEEP = 6.5;
    private static final double HEIGHT = 1.7;

    /** Which way round it drifts. */
    private final double turn;
    private double angle;
    /** One the Weeping Mother called burns for so long and no longer, and leaves nothing: 0 for a wisp of the marsh. */
    private int burns;

    public WispEntity(EntityType<? extends WispEntity> type, Level level) {
        super(type, level);
        this.xpReward = 6;
        this.setNoGravity(true);
        this.turn = this.random.nextBoolean() ? 1.0 : -1.0;
        this.angle = this.random.nextDouble() * Math.PI * 2.0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 45.0).add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public int soulReward() {
        return this.burns > 0 ? 0 : 300;
    }

    /** It was called, and will go out by itself. */
    public void called() {
        this.burns = 900;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Burns", this.burns);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.burns = input.getIntOr("Burns", 0);
    }

    @Override
    protected int attackDuration(int id) {
        return BOLT_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return BOLT_RELEASE;
    }

    @Override
    protected int attackPause(int finishedId) {
        return 50 + this.random.nextInt(35);
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        return distance < 15.0 && this.hasLineOfSight(target) ? BOLT : 0;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.burns > 0 && --this.burns == 0) {
            level.sendParticles(ParticleTypes.GLOW, this.getX(), this.getY() + 0.5, this.getZ(), 12, 0.3, 0.3, 0.3, 0.05);
            this.discard();
            return;
        }
        LivingEntity target = this.getTarget();
        Vec3 want;
        if (target != null && target.isAlive()) {
            this.angle += 0.022 * this.turn;
            want = target.position().add(Math.cos(this.angle) * KEEP, HEIGHT + 0.3, Math.sin(this.angle) * KEEP);
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (this.canStartAttack()) {
                int id = this.chooseAttack(target, this.distanceTo(target));
                if (id != 0) {
                    this.startAttack(id);
                }
            }
        } else {
            BlockPos home = MobMarkBlockEntity.homeOf(this);
            want = home != null ? new Vec3(home.getX() + 0.5, home.getY() + HEIGHT, home.getZ() + 0.5) : this.position();
        }
        want = want.add(0.0, Math.sin(this.tickCount * 0.13) * 0.25, 0.0);
        Vec3 to = want.subtract(this.position());
        double far = to.length();
        Vec3 pull = far < 0.05 ? Vec3.ZERO : to.scale(Math.min(0.17, far * 0.08) / far);
        if (this.attackId() != 0) {
            pull = pull.scale(0.25);                             // it hangs nearly still while the flame stands up
        }
        this.setDeltaMovement(this.getDeltaMovement().scale(0.78).add(pull.scale(0.4)));
        if (this.tickCount % 4 == 0) {
            level.sendParticles(ParticleTypes.GLOW, this.getX(), this.getY() + 0.5, this.getZ(), 1, 0.15, 0.15, 0.15, 0.0);
        }
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        if (time == 2) {
            this.playSound(Dushi.sound("entity.wisp.cast"), 1.2F, 0.9F + this.random.nextFloat() * 0.2F);
        }
        if (time < BOLT_RELEASE && time % 2 == 0) {
            level.sendParticles(ParticleTypes.GLOW, this.getX(), this.getY() + 0.9, this.getZ(), 2, 0.25, 0.25, 0.25, 0.0);
        }
        if (time == BOLT_RELEASE) {
            LivingEntity target = this.getTarget();
            Vec3 aim = target == null ? this.getLookAngle() : target.position().add(0.0, target.getBbHeight() * 0.6, 0.0).subtract(this.getEyePosition());
            SpellBoltEntity.shoot(level, this, SpellBoltEntity.Kind.WISP_FLAME, aim, 0.42F, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        }
    }

    /** Struck, it is ten paces off before the second blow comes. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && this.isAlive() && source.getEntity() != null) {
            Vec3 away = new Vec3(this.getX() - source.getEntity().getX(), 0.0, this.getZ() - source.getEntity().getZ());
            if (away.lengthSqr() > 1.0E-4) {
                away = away.normalize();
                this.setDeltaMovement(away.x * 0.7, 0.25, away.z * 0.7);
                this.needsSync = true;
            }
        }
        return hurt;
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
        if (this.burns == 0) {
            this.leaveMaybe(level, Dushi.NAMELESS_SOUL.get(), 0.2F);
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.GLOW, this.getX(), this.getY() + 0.5, this.getZ(), 24, 0.3, 0.3, 0.3, 0.06);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.wisp.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.wisp.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.wisp.death");
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
    }
}
