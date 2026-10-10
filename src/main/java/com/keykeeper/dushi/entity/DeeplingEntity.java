package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.PlayerSoul;
import com.keykeeper.dushi.block.MobMarkBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.phys.Vec3;

/**
 * A deepling: a fish of the bottom of a sea, blind, swimming in the air of the Sky Beneath as if it were water - which,
 * for what lives under a sky, it is. Over its head it carries a light on a stalk, violet, the very colour of the
 * crystals that are the only light of the place. It hangs still in the dark and lets the light be walked towards
 * (phase 1). Whoever comes near enough finds what is under the light (phase 2).
 * <p>
 * It bites, coming in fast and straight. And it opens its mouth as wide as it goes and holds it open: in the mouth,
 * where a throat should be, there are eyes, and to be looked at by them is to have the dark come down.
 */
public class DeeplingEntity extends SoulsMob {
    public static final int GULP = 1;
    public static final int GULP_DURATION = 42;
    public static final int GULP_START = 12;
    public static final int GULP_IMPACT = 19;
    public static final int GLARE = 2;
    public static final int GLARE_DURATION = 66;
    public static final int GLARE_FROM = 20;
    public static final int GLARE_UNTIL = 48;
    public static final double GLARE_RANGE = 9.0;
    /** How near somebody may come to one that is hanging in wait. */
    public static final double WAKES_AT = 4.5;
    /** How high over the floor it hangs. */
    private static final double HEIGHT = 1.5;

    private int glareCooldown = 60;
    private boolean bitten;

    public DeeplingEntity(EntityType<? extends DeeplingEntity> type, Level level) {
        super(type, level);
        this.xpReward = 16;
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 150.0).add(Attributes.ATTACK_DAMAGE, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false, (entity, level) -> this.hunting()));
    }

    public boolean hunting() {
        return this.phase() >= 2;
    }

    @Override
    public int soulReward() {
        return 1400;
    }

    @Override
    protected int attackDuration(int id) {
        return id == GULP ? GULP_DURATION : GLARE_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return id == GULP ? GULP_START - 2 : GLARE_UNTIL;
    }

    @Override
    protected int attackPause(int finishedId) {
        return 24 + this.random.nextInt(20);
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        // (asked by its own flying, below)
        if (distance < 3.2) {
            return GULP;
        }
        if (this.glareCooldown <= 0 && distance < GLARE_RANGE - 1.5 && this.hasLineOfSight(target)) {
            this.glareCooldown = 140 + this.random.nextInt(80);
            return GLARE;
        }
        return 0;
    }

    @Override
    public boolean overlooks(LivingEntity target) {
        return !this.hunting() || super.overlooks(target);
    }

    @Override
    public void startAttack(int id) {
        super.startAttack(id);
        this.bitten = false;
    }

    /** What is under the light comes for them. */
    public void wake(LivingEntity cause) {
        if (!this.hunting() && this.isAlive()) {
            this.setPhase(2);
            this.playSound(Dushi.sound("entity.deepling.wake"), 1.4F, 1.0F);
            if (cause != null) {
                this.setTarget(cause);
            }
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.setNoGravity(true);
        this.glareCooldown = Math.max(0, this.glareCooldown - 1);
        BlockPos home = MobMarkBlockEntity.homeOf(this);
        LivingEntity target = this.hunting() ? this.getTarget() : null;
        Vec3 want;
        if (target != null && target.isAlive()) {
            // before them, at the height of their head, a little way off; nearer when it means to bite
            Vec3 from = new Vec3(this.getX() - target.getX(), 0.0, this.getZ() - target.getZ());
            double far = from.length();
            from = far < 1.0E-3 ? this.facing().scale(-1.0) : from.scale(1.0 / far);
            double keep = this.attackId() == GULP && this.attackTime >= GULP_START ? 0.6 : 2.4;
            want = target.position().add(from.scale(keep)).add(0.0, 0.8, 0.0);
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distance = Math.max(0.0, this.distanceTo(target) - target.getBbWidth() * 0.5 - this.getBbWidth() * 0.5);
            if (this.canStartAttack()) {
                int id = this.chooseAttack(target, distance);
                if (id != 0) {
                    this.startAttack(id);
                }
            }
        } else {
            want = home != null ? new Vec3(home.getX() + 0.5, home.getY() + HEIGHT, home.getZ() + 0.5) : this.position();
            if (!this.hunting() && this.tickCount % 5 == 0) {
                Player near = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), WAKES_AT, entity -> entity instanceof Player player
                        && !player.isCreative() && !player.isSpectator() && !PlayerSoul.of(player).atHearth);
                if (near != null) {
                    this.wake(near);
                }
            }
        }
        // it hangs, and rises and falls a little, as a thing in water does
        want = want.add(0.0, Math.sin(this.tickCount * 0.07) * 0.18, 0.0);
        Vec3 to = want.subtract(this.position());
        double far = to.length();
        double pace = this.attackId() == GULP && this.attackTime >= GULP_START && this.attackTime <= GULP_IMPACT + 2 ? 0.5 : (this.attackId() == GLARE ? 0.03 : 0.17);
        Vec3 pull = far < 0.05 ? Vec3.ZERO : to.scale(Math.min(pace, far * 0.12) / far);
        this.setDeltaMovement(this.getDeltaMovement().scale(0.72).add(pull.scale(0.5)));
        if (target != null && this.attackId() == 0) {
            float yaw = (float) (Mth.atan2(target.getZ() - this.getZ(), target.getX() - this.getX()) * (180.0 / Math.PI)) - 90.0F;
            this.setYRot(Mth.approachDegrees(this.getYRot(), yaw, 10.0F));
            this.setYBodyRot(this.getYRot());
        }
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (id == GULP) {
            if (time == GULP_START - 4) {
                this.playSound(Dushi.sound("entity.deepling.gulp"), 1.2F, 1.0F);
            }
            if (!this.bitten && time >= GULP_START + 3 && time <= GULP_IMPACT + 3 && this.hitArc(level, 1.5, 60.0, damage, 0.7) > 0) {
                this.bitten = true;
            }
            return;
        }
        if (time == 4) {
            this.playSound(Dushi.sound("entity.deepling.glare"), 1.4F, 1.0F);
        }
        if (time >= GLARE_FROM && time <= GLARE_UNTIL && time % 4 == 0) {
            Vec3 facing = this.facing();
            Vec3 mouth = this.position().add(facing.scale(0.9)).add(0.0, 0.5, 0.0);
            level.sendParticles(ParticleTypes.WITCH, mouth.x, mouth.y, mouth.z, 3, 0.2, 0.2, 0.2, 0.0);
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(GLARE_RANGE, 4.0, GLARE_RANGE), this::isFoe)) {
                Vec3 to = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ());
                if (to.length() > GLARE_RANGE || (to.lengthSqr() > 0.5 && to.normalize().dot(facing) < 0.82) || !this.hasLineOfSight(victim)) {
                    continue;
                }
                // looked at: the dark comes down (somebody behind a raised shield is not seen by it)
                if (victim.isBlocking()) {
                    continue;
                }
                victim.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 90, 0, false, false, true), this);
                if (time % 8 == 0) {
                    victim.hurtServer(level, this.damageSources().indirectMagic(this, this), damage * 0.15F);
                }
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && !this.hunting()) {
            this.wake(source.getEntity() instanceof LivingEntity attacker ? attacker : null);
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
        this.leaveMaybe(level, Dushi.NAMELESS_SOUL.get(), 0.3F);
        this.leaveMaybe(level, Dushi.GREAT_SOUL.get(), 0.07F);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        this.setNoGravity(false);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.hunting() ? Dushi.sound("entity.deepling.ambient") : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.deepling.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.deepling.death");
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
    }
}
