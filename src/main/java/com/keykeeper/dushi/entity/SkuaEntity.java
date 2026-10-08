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
 * A skua of the Morning Wharf: a sea bird the colour of smoke that lives on what others have caught. It sits on a
 * pile, a rail or the gunwale of a boat with its wings folded (phase 1) and is taken for part of it, until somebody
 * comes within a stone's throw; then it goes up.
 * <p>
 * It goes round over whoever it has chosen, out of reach. From time to time it hangs in the air, cries - that is the
 * warning - and stoops: straight down the line to where they stood when it cried, beak first, and up again. It does
 * not turn in its fall. A blow or two kills it; the place to give them is where its stoop ends.
 */
public class SkuaEntity extends SoulsMob {
    public static final int STOOP = 1;
    public static final int STOOP_DURATION = 56;
    /** It hangs and cries until this tick, falls until the next, and has climbed away by the end. */
    public static final int STOOP_FALLS = 18;
    public static final int STOOP_RISES = 38;
    public static final double GOES_UP_AT = 10.0;
    private static final double HEIGHT = 5.0;
    private static final double RING = 5.5;

    private Vec3 fall = Vec3.ZERO;
    private boolean struck;
    private int stoopCooldown = 50;
    private float round;

    public SkuaEntity(EntityType<? extends SkuaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 6;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 36.0).add(Attributes.ATTACK_DAMAGE, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.FOLLOW_RANGE, 24.0);
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
        return 250;
    }

    @Override
    protected int attackDuration(int id) {
        return STOOP_DURATION;
    }

    @Override
    protected int trackUntil(int id) {
        return 0;
    }

    @Override
    protected int attackPause(int finishedId) {
        return 10;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        return STOOP;
    }

    @Override
    public boolean overlooks(LivingEntity target) {
        return !this.flying() || super.overlooks(target);
    }

    /** It goes up. */
    public void startle(LivingEntity cause) {
        if (!this.flying() && this.isAlive()) {
            this.setPhase(2);
            this.setNoGravity(true);
            this.setDeltaMovement(0.0, 0.35, 0.0);
            this.round = this.random.nextFloat() * 6.28F;
            this.playSound(Dushi.sound("entity.skua.cry"), 1.6F, 1.0F);
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
    public void startAttack(int id) {
        super.startAttack(id);
        this.struck = false;
        this.fall = Vec3.ZERO;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (!this.flying()) {
            this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
            if (this.tickCount % 5 == 0) {
                Player near = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), GOES_UP_AT, entity -> entity instanceof Player player
                        && !player.isCreative() && !player.isSpectator() && !PlayerSoul.of(player).atHearth);
                if (near != null) {
                    this.startle(near);
                }
            }
            return;
        }
        this.stoopCooldown = Math.max(0, this.stoopCooldown - 1);
        if (this.attackId() != 0) {
            return;                                              // (the stoop flies itself: see attackTick)
        }
        LivingEntity target = this.getTarget();
        if (target != null && lordNear(level, target)) {
            this.setTarget(null);                                // (a gull does not follow anybody into a lord's ground)
            target = null;
        }
        Vec3 want;
        if (target != null && target.isAlive()) {
            // round over their head, out of reach
            this.round += 0.045F;
            want = target.position().add(Math.cos(this.round) * RING, HEIGHT, Math.sin(this.round) * RING);
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double flat = Math.hypot(this.getX() - target.getX(), this.getZ() - target.getZ());
            if (this.canStartAttack() && this.stoopCooldown <= 0 && flat < RING + 2.5 && this.getY() > target.getY() + 3.0 && this.hasLineOfSight(target)) {
                this.stoopCooldown = 90 + this.random.nextInt(60);
                this.startAttack(STOOP);
                return;
            }
        } else {
            BlockPos home = MobMarkBlockEntity.homeOf(this);
            want = home != null ? new Vec3(home.getX() + 0.5, home.getY() + HEIGHT, home.getZ() + 0.5) : this.position();
        }
        Vec3 to = want.subtract(this.position());
        double far = to.length();
        Vec3 pull = far < 0.05 ? Vec3.ZERO : to.scale(Math.min(0.34, far * 0.12) / far);
        this.setDeltaMovement(this.getDeltaMovement().scale(0.8).add(pull.scale(0.4)));
        this.face(this.getDeltaMovement());
    }

    /** How near a living lord whoever it hunts may come before it lets them go and flies back. */
    public static final double LORD_GROUND = 24.0;

    private static boolean lordNear(ServerLevel level, LivingEntity target) {
        return !level.getEntitiesOfClass(SoulsMob.class, target.getBoundingBox().inflate(LORD_GROUND, 10.0, LORD_GROUND),
                mob -> mob.isBoss() && mob.isAlive()).isEmpty();
    }

    private void face(Vec3 way) {
        if (way.horizontalDistanceSqr() > 1.0E-4) {
            float yaw = (float) (Math.atan2(way.z, way.x) * (180.0 / Math.PI)) - 90.0F;
            this.setYRot(net.minecraft.util.Mth.approachDegrees(this.getYRot(), yaw, 20.0F));
            this.setYBodyRot(this.getYRot());
        }
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        LivingEntity target = this.getTarget();
        if (time == 2) {
            this.playSound(Dushi.sound("entity.skua.cry"), 2.0F, 1.25F);
        }
        if (time < STOOP_FALLS) {
            // hanging where it is, turned to what it has chosen
            this.setDeltaMovement(this.getDeltaMovement().scale(0.6));
            if (target != null) {
                this.face(target.position().subtract(this.position()));
            }
        } else if (time == STOOP_FALLS) {
            // the line is fixed now: through where they stand at this moment
            Vec3 at = target != null ? target.position().add(0.0, target.getBbHeight() * 0.6, 0.0) : this.position().add(0.0, -HEIGHT, 0.0);
            Vec3 to = at.subtract(this.position());
            this.fall = to.lengthSqr() < 1.0E-4 ? new Vec3(0.0, -1.0, 0.0) : to.normalize();
            this.playSound(Dushi.sound("entity.skua.stoop"), 1.4F, 1.0F);
        } else if (time < STOOP_RISES) {
            this.setDeltaMovement(this.fall.scale(0.82));
            this.face(this.fall);
            if (!this.struck) {
                float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
                if (this.hitAround(level, this.position().add(this.fall.scale(0.5)), 1.1, damage, 0.5, this.damageSources().mobAttack(this)) > 0) {
                    this.struck = true;
                }
            }
            if (this.onGround() || this.verticalCollision || this.horizontalCollision) {
                this.attackTime = STOOP_RISES - 1;               // it has met the ground: no further down
                this.setDeltaMovement(0.0, 0.1, 0.0);
            }
        } else {
            // up and away, the way it was going
            Vec3 away = new Vec3(this.fall.x, 0.0, this.fall.z).scale(0.25).add(0.0, 0.3, 0.0);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.7).add(away.scale(0.5)));
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
        this.setNoGravity(false);
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 0.3, this.getZ(), 10, 0.3, 0.3, 0.3, 0.02);
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.leaveMaybe(level, Dushi.NAMELESS_SOUL.get(), 0.1F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.skua.cry");
    }

    @Override
    public int getAmbientSoundInterval() {
        return this.flying() ? 90 : 320;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.skua.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.skua.death");
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
    }
}
