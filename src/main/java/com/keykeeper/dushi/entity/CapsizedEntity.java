package com.keykeeper.dushi.entity;

import java.util.List;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.PlayerSoul;
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
 * The Capsized: a boat lying keel up on the shoal under the lighthouse, as boats are left for the winter - and what has
 * lived under her since the morning the tide stopped going out. It has grown to her; she is its house and its back. The
 * second lord of the shore.
 * <p>
 * It lies there as a boat (phase 1) until somebody comes near or strikes her planks. Then she is lifted on six legs. The
 * great claw is shot straight out, or carried across low; or the boat is stood on her stern and let fall on whoever is
 * before her, which no shield holds - and her bow stays dug into the sand a while. Hurt much in a short time it sits
 * down: there is nothing to strike then but a boat, and when it has sat long enough it throws itself up and everything
 * round it off its feet. Hurt by half (phase 3) it raps on its own planks, and what the sand keeps comes up to the
 * knocking; and it draws its legs short and goes round like a top, and is giddy after.
 */
public class CapsizedEntity extends SoulsMob {
    public static final int RISE = 1;
    public static final int RISE_DURATION = 40;
    public static final int RISE_UP = 20;
    public static final int SNAP = 2;
    public static final int SNAP_DURATION = 42;
    public static final int SNAP_IMPACT = 19;
    public static final int SWEEP = 3;
    public static final int SWEEP_DURATION = 48;
    public static final int SWEEP_IMPACT = 23;
    public static final int SLAM = 4;
    public static final int SLAM_DURATION = 84;
    public static final int SLAM_IMPACT = 36;
    /** Until this tick her bow is fast in the sand. */
    public static final int SLAM_STUCK = 70;
    public static final int SPIN = 5;
    public static final int SPIN_DURATION = 90;
    public static final int[] SPIN_HITS = {26, 38, 50, 62};
    public static final int TUCK = 6;
    public static final int TUCK_DURATION = 96;
    public static final int TUCK_DOWN = 14;
    public static final int TUCK_BURST = 72;
    public static final int BROOD = 7;
    public static final int BROOD_DURATION = 56;
    public static final int[] BROOD_KNOCKS = {20, 34};
    public static final double SNAP_REACH = 5.2;
    public static final double SWEEP_REACH = 4.6;
    public static final double SPIN_RADIUS = 4.4;
    public static final double BURST_RADIUS = 4.8;
    /** How near somebody comes before the boat gets up. */
    public static final double WAKES_AT = 7.5;
    /** The share of a blow that gets through to it (nothing has more than 1024 health; it needs more). */
    public static final float HIDE = 0.38F;
    /** What gets through planks: while it lies as a boat, and while it sits. */
    public static final float PLANKS = 0.08F;
    /** Hurt by this share of its health within {@link #HURT_WITHIN} ticks, it sits down. */
    private static final float SITS_AFTER = 0.14F;
    private static final int HURT_WITHIN = 160;
    private static final int BROOD_MOST = 3;

    private int spinCooldown = 80;
    private int broodCooldown;
    private int tuckCooldown = 200;
    private float hurtLately;
    private int hurtSince;

    public CapsizedEntity(EntityType<? extends CapsizedEntity> type, Level level) {
        super(type, level);
        this.xpReward = 320;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1000.0).add(Attributes.ATTACK_DAMAGE, 27.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26).add(Attributes.ARMOR, 8.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 44.0).add(Attributes.STEP_HEIGHT, 1.25);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false, (entity, level) -> this.up()));
    }

    /** On its legs. */
    public boolean up() {
        return this.phase() >= 2;
    }

    /** Nothing of it to be struck but the boat: lying as one, or sat down under her. */
    public boolean housed() {
        return !this.up() || this.attackId() == TUCK && this.attackTime > TUCK_DOWN - 4 && this.attackTime < TUCK_BURST
                || this.attackId() == RISE && this.attackTime < RISE_UP - 8;
    }

    /** Her bow fast in the sand, or giddy from going round: when it can do nothing about a blow. */
    public boolean open() {
        int id = this.attackId();
        return id == SLAM && this.attackTime > SLAM_IMPACT && this.attackTime < SLAM_STUCK
                || id == SPIN && this.attackTime > SPIN_HITS[SPIN_HITS.length - 1] + 4;
    }

    @Override
    public int soulReward() {
        return 27000;
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    public boolean overlooks(LivingEntity target) {
        return !this.up() || super.overlooks(target);
    }

    @Override
    protected int attackDuration(int id) {
        return switch (id) {
            case RISE -> RISE_DURATION;
            case SNAP -> SNAP_DURATION;
            case SWEEP -> SWEEP_DURATION;
            case SLAM -> SLAM_DURATION;
            case SPIN -> SPIN_DURATION;
            case TUCK -> TUCK_DURATION;
            default -> BROOD_DURATION;
        };
    }

    @Override
    protected int trackUntil(int id) {
        return switch (id) {
            case RISE -> RISE_DURATION;
            case SNAP -> SNAP_IMPACT - 6;
            case SWEEP -> SWEEP_IMPACT - 9;
            case SLAM -> SLAM_IMPACT - 14;
            case SPIN -> SPIN_HITS[SPIN_HITS.length - 1];
            case TUCK -> 0;
            default -> BROOD_DURATION;
        };
    }

    @Override
    protected int attackPause(int finishedId) {
        if (finishedId == RISE || finishedId == TUCK || finishedId == SPIN || finishedId == SLAM) {
            return 6;
        }
        return this.phase() >= 3 ? 9 + this.random.nextInt(8) : 15 + this.random.nextInt(12);
    }

    @Override
    public double stopDistance() {
        return 3.0;
    }

    @Override
    protected void onReset() {
        super.onReset();
        this.spinCooldown = 80;
        this.broodCooldown = 0;
        this.tuckCooldown = 200;
        this.hurtLately = 0.0F;
        if (this.level() instanceof ServerLevel level) {
            for (SandCrabEntity crab : this.brood(level)) {
                crab.discard();
            }
        }
    }

    private List<SandCrabEntity> brood(ServerLevel level) {
        return level.getEntitiesOfClass(SandCrabEntity.class, this.getBoundingBox().inflate(24.0, 6.0, 24.0), crab -> crab.isAlive() && crab.isCalled());
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (this.phase() >= 3) {
            if (this.broodCooldown <= 0 && this.level() instanceof ServerLevel level && this.brood(level).isEmpty()) {
                this.broodCooldown = 520 + this.random.nextInt(160);
                return BROOD;
            }
            if (this.spinCooldown <= 0 && distance < 7.5) {
                this.spinCooldown = 240 + this.random.nextInt(100);
                return SPIN;
            }
        }
        if (distance < SWEEP_REACH - 0.8) {
            int roll = this.random.nextInt(10);
            return roll < 4 ? SWEEP : roll < 7 ? SNAP : SLAM;
        }
        if (distance < SNAP_REACH - 0.3) {
            return this.random.nextInt(3) == 0 ? SLAM : SNAP;
        }
        return 0;
    }

    /** It gets up, for {@code cause} if there is one. */
    public void rise(LivingEntity cause) {
        if (!this.up() && this.isAlive()) {
            this.setPhase(2);
            this.startAttack(RISE);
            if (cause != null) {
                this.setTarget(cause);
            }
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.phase() == 2 && this.getHealth() < this.getMaxHealth() * 0.5F && this.attackId() == 0 && this.isAlive()) {
            this.setPhase(3);
            this.spinCooldown = 120;
            this.broodCooldown = 520;
            this.startAttack(BROOD);
        } else if (this.up() && this.attackId() == 0 && this.tuckCooldown <= 0 && this.hurtLately > this.getMaxHealth() * SITS_AFTER && this.isAlive()) {
            this.tuckCooldown = 420 + this.random.nextInt(120);
            this.hurtLately = 0.0F;
            this.startAttack(TUCK);
        }
        super.customServerAiStep(level);
        this.spinCooldown = Math.max(0, this.spinCooldown - 1);
        this.broodCooldown = Math.max(0, this.broodCooldown - 1);
        this.tuckCooldown = Math.max(0, this.tuckCooldown - 1);
        if (this.hurtLately > 0.0F && ++this.hurtSince > HURT_WITHIN) {
            this.hurtLately = 0.0F;
        }
        if (!this.up()) {
            this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
            if (this.tickCount % 5 == 0) {
                Player near = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), WAKES_AT, entity -> entity instanceof Player player
                        && !player.isCreative() && !player.isSpectator() && !PlayerSoul.of(player).atHearth);
                if (near != null) {
                    this.rise(near);
                }
            }
        }
    }

    private void sand(ServerLevel level, Vec3 at, int count, double spread) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()), at.x, at.y + 0.2, at.z, count, spread, 0.3, spread, 0.2);
        level.sendParticles(ParticleTypes.SPLASH, at.x, at.y + 0.2, at.z, count, spread, 0.2, spread, 0.2);
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        switch (id) {
            case RISE -> {
                if (time == 2) {
                    this.playSound(Dushi.sound("entity.capsized.rise"), 3.0F, 1.0F);
                }
                if (time < RISE_UP && time % 2 == 0) {
                    this.sand(level, this.position(), 14, 1.4);
                }
                if (time == RISE_UP - 6) {
                    this.hitAround(level, this.position(), 3.4, damage * 0.5F, 1.4, this.damageSources().mobAttack(this));
                }
            }
            case SNAP -> {
                if (time == SNAP_IMPACT) {
                    this.playSound(Dushi.sound("entity.capsized.snap"), 2.4F, 0.9F + this.random.nextFloat() * 0.15F);
                    this.hitArc(level, this.facing(), SNAP_REACH, 20.0, damage * 1.2F, 1.0, false);
                }
            }
            case SWEEP -> {
                if (time == SWEEP_IMPACT - 5) {
                    this.playSound(Dushi.sound("entity.capsized.creak"), 2.0F, 1.15F);
                }
                if (time == SWEEP_IMPACT) {
                    this.hitArc(level, SWEEP_REACH, 90.0, damage, 1.6);
                    Vec3 at = this.position().add(this.facing().scale(3.0));
                    level.sendParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y + 0.7, at.z, 2, 0.7, 0.0, 0.7, 0.0);
                    this.sand(level, at, 16, 1.0);
                }
            }
            case SLAM -> {
                if (time == 4) {
                    this.playSound(Dushi.sound("entity.capsized.creak"), 2.6F, 0.8F);
                }
                if (time == SLAM_IMPACT) {
                    this.hitArc(level, this.facing(), 5.4, 24.0, damage * 1.7F, 0.6, true);
                    Vec3 at = this.position().add(this.facing().scale(3.6));
                    this.hitAround(level, at, 2.6, damage * 0.6F, 0.9, this.damageSources().mobAttack(this));
                    this.sand(level, at, 60, 1.2);
                    level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.capsized.slam"), this.getSoundSource(), 3.0F, 0.9F + this.random.nextFloat() * 0.1F);
                }
            }
            case SPIN -> {
                int last = SPIN_HITS[SPIN_HITS.length - 1];
                if (time == 6) {
                    this.playSound(Dushi.sound("entity.capsized.creak"), 2.6F, 1.3F);
                }
                if (time > SPIN_HITS[0] - 12 && time <= last) {
                    Vec3 step = this.facing().scale(0.16);
                    this.setDeltaMovement(step.x, this.getDeltaMovement().y, step.z);
                    if (time % 3 == 0) {
                        this.sand(level, this.position(), 8, 1.8);
                    }
                }
                for (int hit : SPIN_HITS) {
                    if (time == hit) {
                        this.hitAround(level, this.position(), SPIN_RADIUS, damage * 0.85F, 1.4, this.damageSources().mobAttack(this));
                        this.playSound(Dushi.sound("entity.capsized.snap"), 1.6F, 1.2F);
                    }
                }
                if (time == last + 1) {
                    this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
                }
            }
            case TUCK -> {
                if (time == 2) {
                    this.playSound(Dushi.sound("entity.capsized.creak"), 2.4F, 0.7F);
                }
                if (time == TUCK_DOWN) {
                    this.sand(level, this.position(), 30, 1.6);
                }
                if (time > TUCK_BURST - 16 && time < TUCK_BURST && time % 4 == 0) {
                    this.playSound(Dushi.sound("entity.capsized.knock"), 1.4F, 1.3F);
                    this.sand(level, this.position(), 6, 1.6);
                }
                if (time == TUCK_BURST) {
                    this.hitAround(level, this.position(), BURST_RADIUS, damage * 1.1F, 2.0, this.damageSources().mobAttack(this));
                    this.sand(level, this.position(), 90, 2.4);
                    this.playSound(Dushi.sound("entity.capsized.slam"), 3.0F, 1.1F);
                }
            }
            default -> {
                for (int knock : BROOD_KNOCKS) {
                    if (time == knock) {
                        this.playSound(Dushi.sound("entity.capsized.knock"), 3.0F, 0.9F + this.random.nextFloat() * 0.1F);
                    }
                }
                if (time == BROOD_KNOCKS[BROOD_KNOCKS.length - 1] + 4) {
                    this.call(level);
                }
            }
        }
    }

    /** What the sand keeps comes up to the knocking: round whoever it is fighting, not under them. */
    private void call(ServerLevel level) {
        LivingEntity target = this.getTarget();
        Vec3 centre = target != null ? target.position() : this.position();
        int called = 0;
        for (int i = 0; i < 16 && called < BROOD_MOST; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            double far = 3.0 + this.random.nextDouble() * 3.0;
            Vec3 at = RootEntity.ground(level, centre.x + Math.cos(angle) * far, centre.y + 1.0, centre.z + Math.sin(angle) * far);
            if (at == null || at.distanceToSqr(this.position()) < 9.0) {
                continue;
            }
            SandCrabEntity crab = Dushi.SAND_CRAB.get().create(level, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
            if (crab == null) {
                return;
            }
            crab.setPos(at.x, at.y, at.z);
            crab.setYRot(this.random.nextFloat() * 360.0F);
            crab.called();
            level.addFreshEntity(crab);
            crab.emerge(target);
            called++;
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (damage == Float.MAX_VALUE) {
            return super.hurtServer(level, source, damage);
        }
        boolean housed = this.housed();
        float through = housed ? PLANKS : this.open() ? Math.min(1.0F, HIDE * 2.0F) : HIDE;
        boolean hurt = super.hurtServer(level, source, damage * through);
        if (hurt) {
            if (housed) {
                this.playSound(Dushi.sound("entity.capsized.knock"), 1.6F, 0.8F + this.random.nextFloat() * 0.2F);
            } else {
                if (this.hurtLately <= 0.0F) {
                    this.hurtSince = 0;
                }
                this.hurtLately += damage * through;
            }
            if (!this.up()) {
                this.rise(source.getEntity() instanceof LivingEntity attacker ? attacker : null);
            }
        }
        return hurt;
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
        this.spawnAtLocation(level, new ItemStack(Dushi.CAPSIZED_SOUL.get()));
        this.spawnAtLocation(level, new ItemStack(Dushi.FLASK_SHARD.get()));
        this.spawnAtLocation(level, new ItemStack(Dushi.CHARMS.get(com.keykeeper.dushi.Charm.TIDE_PEARL).get()));
        this.leave(level, Dushi.GREAT_SOUL.get(), 2, 3);
        this.leave(level, Dushi.CRAB_SHELL.get(), 4, 6);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            for (SandCrabEntity crab : this.brood(level)) {
                crab.discard();
            }
            BlockPos home = MobMarkBlockEntity.homeOf(this);
            FogGateBlock.lift(level, home != null ? home : this.blockPosition(), 40);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.up() ? Dushi.sound("entity.capsized.ambient") : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.capsized.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.capsized.death");
    }

    @Override
    protected float getSoundVolume() {
        return 2.2F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(Dushi.sound("entity.sand_crab.click"), 0.8F, 0.55F + this.random.nextFloat() * 0.15F);
    }
}
