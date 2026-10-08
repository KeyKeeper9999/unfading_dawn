package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * A scarecrow of the Stubble: a sack for a head under somebody's hat, a coat stuffed with straw, arms bound out along
 * a crossbar with a bundle of rods for each hand - and standing on its stake in the corn like every other one (phase
 * 1). Some of them are only that. The others let somebody go by, and come off the stake behind them.
 * <p>
 * It moves while nobody is looking at it. Looked at, it stands as it stood - until it is struck: then for a while it
 * does not care who sees. Its arms do not bend, so it fights by turning: once round with a hand at the height of a
 * face, or round and round, coming on.
 */
public class ScarecrowEntity extends SoulsMob {
    public static final int WAKE = 1;
    public static final int WAKE_DURATION = 22;
    public static final int WAKE_DROP = 8;
    public static final int TURN = 2;
    public static final int TURN_DURATION = 30;
    public static final int TURN_IMPACT = 11;
    public static final int WHIRL = 3;
    public static final int WHIRL_DURATION = 56;
    public static final int[] WHIRL_HITS = {12, 22, 32};
    public static final double REACH = 1.9;
    /** How near somebody comes before one that is more than straw gets down, once they have gone by or stopped looking. */
    public static final double WAKES_AT = 7.0;
    /** A man's eyes are on it when it is within this cosine of where he looks. */
    private static final double SEEN = 0.55;
    /** Struck, it does not care who is looking for this long. */
    private static final int HEEDLESS = 110;

    /** More than straw: gets down by itself. The others only if they are struck. Chosen anew every time it is raised. */
    private boolean restless;
    private int heedless;

    public ScarecrowEntity(EntityType<? extends ScarecrowEntity> type, Level level) {
        super(type, level);
        this.xpReward = 14;
        this.restless = this.random.nextInt(5) < 3;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 190.0).add(Attributes.ATTACK_DAMAGE, 22.0)
                .add(Attributes.MOVEMENT_SPEED, 0.36).add(Attributes.ARMOR, 0.0).add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false, (entity, level) -> this.down()));
    }

    /** Off its stake. */
    public boolean down() {
        return this.phase() >= 2;
    }

    public boolean restless() {
        return this.restless;
    }

    /** For the tests: one that gets down by itself, or one that is only straw until struck. */
    public void setRestless(boolean restless) {
        this.restless = restless;
    }

    @Override
    public int soulReward() {
        return 1100;
    }

    @Override
    protected int attackDuration(int id) {
        return id == WAKE ? WAKE_DURATION : id == TURN ? TURN_DURATION : WHIRL_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == WAKE ? WAKE_DURATION : id == TURN ? TURN_IMPACT - 5 : WHIRL_HITS[WHIRL_HITS.length - 1];
    }

    @Override
    protected int attackPause(int finishedId) {
        return finishedId == WAKE ? 4 : 12 + this.random.nextInt(12);
    }

    @Override
    public double stopDistance() {
        return 1.0;
    }

    @Override
    public boolean overlooks(LivingEntity target) {
        return !this.down() || super.overlooks(target);
    }

    /** Whether {@code player} has it before their eyes. */
    public boolean seenBy(Player player) {
        Vec3 to = this.position().add(0.0, this.getBbHeight() * 0.6, 0.0).subtract(player.getEyePosition());
        double far = to.length();
        return far < 1.0E-3 || far < 32.0 && to.scale(1.0 / far).dot(player.getLookAngle()) > SEEN && player.hasLineOfSight(this);
    }

    /** Looked at, and not past caring: it stands as it stood. */
    public boolean held() {
        return this.down() && this.heedless <= 0 && this.attackId() == 0 && this.getTarget() instanceof Player player && this.seenBy(player);
    }

    @Override
    public boolean canStartAttack() {
        return !this.held() && super.canStartAttack();
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance < REACH - 0.4) {
            return this.random.nextInt(3) == 0 ? WHIRL : TURN;
        }
        return 0;
    }

    /** It gets down, for {@code cause} if there is one. */
    public void wake(LivingEntity cause) {
        if (!this.down() && this.isAlive()) {
            this.setPhase(2);
            this.startAttack(WAKE);
            if (cause != null) {
                this.setTarget(cause);
            }
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (!this.down()) {
            this.getNavigation().stop();
            this.setDeltaMovement(0.0, Math.min(0.0, this.getDeltaMovement().y), 0.0);
            if (this.restless && this.tickCount % 5 == 0) {
                Player near = level.getNearestPlayer(this, WAKES_AT);
                // not while they are looking at it: when they have turned away, or walked on with it behind them
                if (near != null && this.isFoe(near) && !com.keykeeper.dushi.PlayerSoul.of(near).atHearth && this.hasLineOfSight(near) && !this.seenBy(near)) {
                    this.wake(near);
                }
            }
        }
        super.customServerAiStep(level);
        this.heedless = Math.max(0, this.heedless - 1);
        if (this.held()) {
            this.getNavigation().stop();
            this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide() && this.held()) {
            // (the goal that walks it has run by now: what it asked for is taken back)
            this.getNavigation().stop();
            this.zza = 0.0F;
            this.xxa = 0.0F;
        }
    }

    private void straw(ServerLevel level, Vec3 at, int count) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HAY_BLOCK.defaultBlockState()), at.x, at.y, at.z, count, 0.5, 0.4, 0.5, 0.1);
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (id == WAKE) {
            if (time == 2) {
                this.playSound(Dushi.sound("entity.scarecrow.wake"), 1.2F, 0.9F + this.random.nextFloat() * 0.2F);
            }
            if (time == WAKE_DROP) {
                this.straw(level, this.position().add(0.0, 0.4, 0.0), 12);
            }
        } else if (id == TURN) {
            if (time == TURN_IMPACT - 3) {
                this.playSound(Dushi.sound("entity.scarecrow.spin"), 1.0F, 1.2F);
            }
            if (time == TURN_IMPACT) {
                this.hitArc(level, REACH, 110.0, damage, 0.7);
            }
        } else {
            if (time == WHIRL_HITS[0] - 6) {
                this.playSound(Dushi.sound("entity.scarecrow.spin"), 1.3F, 0.9F);
            }
            if (time > 4 && time <= WHIRL_HITS[WHIRL_HITS.length - 1]) {
                Vec3 step = this.facing().scale(0.16);
                this.setDeltaMovement(step.x, this.getDeltaMovement().y, step.z);
            }
            for (int hit : WHIRL_HITS) {
                if (time == hit) {
                    this.hitAround(level, this.position(), REACH, damage * 0.75F, 0.6, this.damageSources().mobAttack(this));
                }
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        // straw: fire has it at once
        boolean hurt = super.hurtServer(level, source, source.is(DamageTypeTags.IS_FIRE) ? damage * 2.5F : damage);
        if (hurt && this.isAlive()) {
            this.heedless = HEEDLESS;
            this.straw(level, this.position().add(0.0, 1.2, 0.0), 6);
            if (!this.down()) {
                this.wake(source.getEntity() instanceof LivingEntity attacker && this.isFoe(attacker) ? attacker : null);
            }
        }
        return hurt;
    }

    @Override
    public void recover() {
        super.recover();
        this.heedless = 0;
    }

    @Override
    public boolean isPushable() {
        return this.down();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Restless", this.restless);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.restless = input.getBooleanOr("Restless", this.restless);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.leave(level, Dushi.RYE_STRAW.get(), 1, 3);
        this.leaveMaybe(level, Dushi.NAMELESS_SOUL.get(), 0.15F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.down() ? Dushi.sound("entity.scarecrow.creak") : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.scarecrow.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.scarecrow.death");
    }
}
