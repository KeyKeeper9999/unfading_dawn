package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.PlayerSoul;

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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * A sand crab of the Long Spit: the size of a dog and the colour of the sand it lies under, with only its eyes out
 * (phase 1) - two pebbles, until somebody walks up to them. Then it comes up all at once, throwing sand, and whoever
 * stands over it is thrown too.
 * <p>
 * Up, it does not come straight: it goes round its prey sideways, as crabs go, and comes in from the side to pinch
 * with the great claw - once, or twice. There are seldom fewer than three of them.
 */
public class SandCrabEntity extends SoulsMob {
    public static final int EMERGE = 1;
    public static final int EMERGE_DURATION = 18;
    public static final int EMERGE_BURST = 6;
    public static final int PINCH = 2;
    public static final int PINCH_DURATION = 24;
    public static final int PINCH_IMPACT = 11;
    public static final int SNIPS = 3;
    public static final int SNIPS_DURATION = 38;
    public static final int SNIPS_FIRST = 11;
    public static final int SNIPS_SECOND = 24;
    /** How near somebody walks before it comes up; whoever creeps gets nearer. */
    public static final double COMES_UP_AT = 4.2;
    public static final double COMES_UP_AT_CREEPING = 1.8;
    /** One that a lord called lives this long and no longer. */
    private static final int CALLED_FOR = 600;

    private int side = 1;
    private int sideTicks;
    private boolean called;
    private int calledTicks;

    public SandCrabEntity(EntityType<? extends SandCrabEntity> type, Level level) {
        super(type, level);
        this.xpReward = 6;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 80.0).add(Attributes.ATTACK_DAMAGE, 15.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ARMOR, 6.0).add(Attributes.FOLLOW_RANGE, 18.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false, (entity, level) -> this.up()));
    }

    /** Out of the sand. */
    public boolean up() {
        return this.phase() >= 2;
    }

    /** One that a lord of the shore called out of the sand: it leaves nothing, and goes back down by itself. */
    public void called() {
        this.called = true;
    }

    public boolean isCalled() {
        return this.called;
    }

    @Override
    public int soulReward() {
        return this.called ? 0 : 420;
    }

    @Override
    protected int attackDuration(int id) {
        return id == EMERGE ? EMERGE_DURATION : id == PINCH ? PINCH_DURATION : SNIPS_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == EMERGE ? EMERGE_DURATION : id == PINCH ? PINCH_IMPACT - 4 : SNIPS_SECOND - 4;
    }

    @Override
    protected int attackPause(int finishedId) {
        return finishedId == EMERGE ? 4 : 18 + this.random.nextInt(16);
    }

    @Override
    public double stopDistance() {
        return 1.3;
    }

    @Override
    public boolean overlooks(LivingEntity target) {
        return !this.up() || super.overlooks(target);
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance < 1.9) {
            return this.random.nextInt(3) == 0 ? SNIPS : PINCH;
        }
        return 0;
    }

    /** It comes up, for {@code cause} if there is one. */
    public void emerge(LivingEntity cause) {
        if (!this.up() && this.isAlive()) {
            this.setPhase(2);
            this.startAttack(EMERGE);
            if (cause != null) {
                this.setTarget(cause);
            }
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.called && ++this.calledTicks > CALLED_FOR && this.attackId() == 0) {
            this.sand(level, 30);
            this.discard();
            return;
        }
        if (!this.up()) {
            this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
            if (this.tickCount % 5 == 0) {
                Player near = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), COMES_UP_AT, entity -> entity instanceof Player player
                        && !player.isCreative() && !player.isSpectator() && !PlayerSoul.of(player).atHearth
                        && !(player.isShiftKeyDown() && this.distanceToSqr(player) > COMES_UP_AT_CREEPING * COMES_UP_AT_CREEPING));
                if (near != null) {
                    this.emerge(near);
                }
            }
            return;
        }
        // sideways round its prey, one way and then the other
        LivingEntity target = this.getTarget();
        if (target != null && this.attackId() == 0 && this.onGround()) {
            if (--this.sideTicks <= 0) {
                this.sideTicks = 30 + this.random.nextInt(30);
                this.side = -this.side;
            }
            double far = this.distanceTo(target);
            if (far < 7.0 && far > 1.6) {
                Vec3 to = new Vec3(target.getX() - this.getX(), 0.0, target.getZ() - this.getZ()).normalize();
                Vec3 across = new Vec3(-to.z, 0.0, to.x).scale(0.075 * this.side);
                this.setDeltaMovement(this.getDeltaMovement().add(across));
            }
        }
    }

    private void sand(ServerLevel level, int count) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()), this.getX(), this.getY() + 0.2, this.getZ(), count, 0.5, 0.25, 0.5, 0.15);
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (id == EMERGE) {
            if (time == 1) {
                this.playSound(Dushi.sound("entity.sand_crab.dig"), 1.2F, 0.9F + this.random.nextFloat() * 0.2F);
            }
            if (time <= EMERGE_BURST) {
                this.sand(level, 10);
            }
            if (time == EMERGE_BURST) {
                this.hitAround(level, this.position(), 1.6, damage * 0.6F, 0.7, this.damageSources().mobAttack(this));
            }
        } else if (time == PINCH_IMPACT && id == PINCH || id == SNIPS && (time == SNIPS_FIRST || time == SNIPS_SECOND)) {
            this.playSound(Dushi.sound("entity.sand_crab.snap"), 1.0F, 0.9F + this.random.nextFloat() * 0.3F);
            this.hitArc(level, 2.1, 55.0, damage * (id == SNIPS ? 0.75F : 1.0F), 0.3);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && !this.up()) {
            this.emerge(source.getEntity() instanceof LivingEntity attacker ? attacker : null);
        }
        return hurt;
    }

    @Override
    public boolean isPushable() {
        return this.up();
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        if (!this.called) {
            this.leaveMaybe(level, Dushi.CRAB_SHELL.get(), 0.5F);
            this.leaveMaybe(level, Dushi.NAMELESS_SOUL.get(), 0.08F);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.up() ? Dushi.sound("entity.sand_crab.click") : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.sand_crab.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.sand_crab.death");
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        if (this.tickCount % 3 == 0) {
            this.playSound(Dushi.sound("entity.sand_crab.click"), 0.25F, 1.5F + this.random.nextFloat() * 0.3F);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Called", this.called);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.called = input.getBooleanOr("Called", false);
    }
}
