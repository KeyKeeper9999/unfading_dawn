package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.PlayerSoul;
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
import net.minecraft.world.phys.Vec3;

/**
 * A sleep-moth of the Black Firwood: a moth the size of a dog and the colour of old linen, with an open eye on either
 * fore wing. It sits on the ground with its wings spread and is taken for a rag somebody dropped (phase 1), until
 * somebody comes near; then it goes up, and comes.
 * <p>
 * It does not bite. It hangs over whoever it has found and shakes the dust off its wings on them, and the dust is
 * sleep: for a few breaths everything goes dark and slow. In a wood where something is always a few steps behind,
 * that is enough. One blow kills it, and the dust is easy to step out from under - if it is seen coming.
 */
public class SleepMothEntity extends SoulsMob {
    public static final int SHED = 1;
    public static final int SHED_DURATION = 50;
    public static final int SHED_FROM = 16;
    public static final int SHED_UNTIL = 36;
    /** How wide the dust falls, and how far under the moth. */
    public static final double DUST_RADIUS = 2.1;
    /** How long the sleep of it lasts, in ticks. */
    public static final int DROWSE = 70;
    /** How near somebody may come to one that is sitting. */
    public static final double STARTLES_AT = 5.5;
    private static final double HEIGHT = 2.6;

    private boolean dusted;

    public SleepMothEntity(EntityType<? extends SleepMothEntity> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.FOLLOW_RANGE, 18.0);
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false, (entity, level) -> this.flying()));
    }

    public boolean flying() {
        return this.phase() >= 2;
    }

    @Override
    public int soulReward() {
        return 260;
    }

    @Override
    protected int attackDuration(int id) {
        return SHED_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return 0;
    }

    @Override
    protected int attackPause(int finishedId) {
        return 60 + this.random.nextInt(40);
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        // (asked when it hangs over them)
        return SHED;
    }

    @Override
    public boolean overlooks(LivingEntity target) {
        return !this.flying() || super.overlooks(target);
    }

    @Override
    public void startAttack(int id) {
        super.startAttack(id);
        this.dusted = false;
    }

    /** It goes up. */
    public void startle(LivingEntity cause) {
        if (!this.flying() && this.isAlive()) {
            this.setPhase(2);
            this.setNoGravity(true);
            this.setDeltaMovement(0.0, 0.3, 0.0);
            this.playSound(Dushi.sound("entity.sleep_moth.flutter"), 1.2F, 1.0F);
            if (cause != null) {
                this.setTarget(cause);
            }
        }
    }

    @Override
    protected void onReset() {
        super.onReset();
        this.setNoGravity(false);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (!this.flying()) {
            this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
            if (this.tickCount % 5 == 0) {
                Player near = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), STARTLES_AT, entity -> entity instanceof Player player
                        && !player.isCreative() && !player.isSpectator() && !PlayerSoul.of(player).atHearth && !(player.isShiftKeyDown() && this.distanceToSqr(player) > 4.0));
                if (near != null) {
                    this.startle(near);
                }
            }
            return;
        }
        LivingEntity target = this.getTarget();
        Vec3 want;
        if (target != null && target.isAlive()) {
            // straight over their head, a little ahead of where they are going
            Vec3 going = target.getKnownMovement().multiply(1.0, 0.0, 1.0);
            want = target.position().add(going.scale(6.0)).add(0.0, HEIGHT, 0.0);
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double flat = Math.hypot(this.getX() - target.getX(), this.getZ() - target.getZ());
            if (this.canStartAttack() && flat < 1.4 && this.getY() > target.getY() + 1.4) {
                this.startAttack(SHED);
            }
        } else {
            BlockPos home = MobMarkBlockEntity.homeOf(this);
            want = home != null ? new Vec3(home.getX() + 0.5, home.getY() + HEIGHT, home.getZ() + 0.5) : this.position();
        }
        want = want.add(Math.sin(this.tickCount * 0.21) * 0.5, Math.sin(this.tickCount * 0.33) * 0.3, Math.cos(this.tickCount * 0.17) * 0.5);
        Vec3 to = want.subtract(this.position());
        double far = to.length();
        Vec3 pull = far < 0.05 ? Vec3.ZERO : to.scale(Math.min(0.24, far * 0.1) / far);
        if (this.attackId() != 0) {
            pull = pull.scale(0.3);                              // it hangs where it is while it shakes
        }
        this.setDeltaMovement(this.getDeltaMovement().scale(0.76).add(pull.scale(0.45)));
        float yaw = (float) (Math.atan2(to.z, to.x) * (180.0 / Math.PI)) - 90.0F;
        if (far > 0.4 && this.attackId() == 0) {
            this.setYRot(net.minecraft.util.Mth.approachDegrees(this.getYRot(), yaw, 12.0F));
            this.setYBodyRot(this.getYRot());
        }
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        if (time == 2) {
            this.playSound(Dushi.sound("entity.sleep_moth.flutter"), 1.0F, 1.4F);
        }
        if (time >= SHED_FROM && time <= SHED_UNTIL) {
            // the dust comes down in a column under it: seen before it is felt
            for (int i = 0; i < 4; i++) {
                level.sendParticles(ParticleTypes.WHITE_ASH, this.getX(), this.getY() - 0.4 - i * 0.6, this.getZ(), 6, DUST_RADIUS * 0.4, 0.2, DUST_RADIUS * 0.4, 0.0);
            }
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() - 0.3, this.getZ(), 1, DUST_RADIUS * 0.3, 0.1, DUST_RADIUS * 0.3, 0.0);
            if (!this.dusted && time >= SHED_FROM + 6) {
                float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
                Vec3 under = new Vec3(this.getX(), this.getY() - HEIGHT, this.getZ());
                for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class,
                        new net.minecraft.world.phys.AABB(under, under).inflate(DUST_RADIUS, 3.0, DUST_RADIUS), this::isFoe)) {
                    if (Math.hypot(victim.getX() - this.getX(), victim.getZ() - this.getZ()) <= DUST_RADIUS && victim.getY() < this.getY()
                            && victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                        com.keykeeper.dushi.Gear.drowse(victim, this, DROWSE, 1);
                        this.playSound(Dushi.sound("entity.sleep_moth.dust"), 1.0F, 1.0F);
                        this.dusted = true;
                    }
                }
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && !this.flying()) {
            this.startle(source.getEntity() instanceof LivingEntity attacker ? attacker : null);
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
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.WHITE_ASH, this.getX(), this.getY() + 0.3, this.getZ(), 40, 0.5, 0.3, 0.5, 0.02);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.flying() ? Dushi.sound("entity.sleep_moth.flutter") : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.sleep_moth.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.sleep_moth.death");
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
    }
}
