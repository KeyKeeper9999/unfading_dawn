package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.block.FogGateBlock;
import com.keykeeper.dushi.block.MobMarkBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The Scorched Warden: the great scarecrow of the cut field, three men high, set up on its cross to keep the birds off
 * a harvest that is never in. It has hung in the full sun of one afternoon for longer than anybody has counted, and
 * the sun is in its keeping now. The first lord of the Stubble.
 * <p>
 * On the cross (phase 1) it cannot leave it: it turns after whoever comes, and strikes with the poles it has for
 * arms - one round at the height of a man, both down along the ground before it, or the whole of it round on its
 * stake. And the sun strikes for it: a patch of light gathers on the ground where somebody stands, brightens, and
 * burns - there is time to step out of it, and it does not reach under a roof of straw. The further off they stand,
 * the more of it there is.
 * <p>
 * Hurt enough, the sun takes the Warden itself. It comes down off the cross alight (phase 2) and walks: quicker,
 * burning whoever is close, throwing fire out of itself, falling on them full length - and burning down all the
 * while, which is the only thing in this field that is on anybody's side.
 */
public class ScorchedWardenEntity extends SoulsMob {
    public static final int RAKE = 1;
    public static final int RAKE_DURATION = 58;
    public static final int RAKE_IMPACT = 27;
    public static final int THRESH = 2;
    public static final int THRESH_DURATION = 72;
    public static final int THRESH_IMPACT = 35;
    public static final int ROUND = 3;
    public static final int ROUND_DURATION = 88;
    public static final int[] ROUND_HITS = {32, 48};
    public static final int KINDLE = 4;
    public static final int KINDLE_DURATION = 84;
    public static final int KINDLE_LIGHT = 10;
    public static final int KINDLE_BURST = 46;
    public static final int FLAIL = 5;
    public static final int FLAIL_DURATION = 62;
    public static final int FLAIL_FIRST = 20;
    public static final int FLAIL_SECOND = 35;
    public static final int FALL = 6;
    public static final int FALL_DURATION = 100;
    public static final int FALL_IMPACT = 30;
    /** Until this tick it lies where it fell. */
    public static final int FALL_LAIN = 72;
    public static final int EMBERS = 7;
    public static final int EMBERS_DURATION = 52;
    public static final int EMBERS_THROW = 22;
    public static final double REACH = 3.6;
    public static final double THRESH_REACH = 4.4;
    public static final double FALL_REACH = 4.6;
    public static final double SUN_RADIUS = 2.3;
    public static final int SUN_DELAY = 32;
    /** How wide the shaft of light stands over a place the sun will strike, and the one that kindles the Warden. */
    public static final float SHAFT_WIDTH = 2.2F;
    public static final float KINDLE_SHAFT_WIDTH = 5.0F;
    public static final double EMBER_RADIUS = 1.7;
    /** The share of a blow that gets through to it (nothing has more than 1024 health; it needs more). */
    public static final float HIDE = 0.42F;
    private static final double WALK = 0.29;

    private int sunCooldown = 60;
    private int roundCooldown = 120;
    private int fallCooldown = 100;
    private int embersCooldown = 60;

    public ScorchedWardenEntity(EntityType<? extends ScorchedWardenEntity> type, Level level) {
        super(type, level);
        this.xpReward = 320;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1000.0).add(Attributes.ATTACK_DAMAGE, 28.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.ARMOR, 4.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 48.0).add(Attributes.STEP_HEIGHT, 1.25);
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
        return 32000;
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    /** Down off the cross, alight. */
    public boolean down() {
        return this.phase() >= 2;
    }

    @Override
    protected void setPhase(int phase) {
        super.setPhase(phase);
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(phase >= 2 ? WALK : 0.0);
        }
    }

    @Override
    protected int attackDuration(int id) {
        return switch (id) {
            case RAKE -> RAKE_DURATION;
            case THRESH -> THRESH_DURATION;
            case ROUND -> ROUND_DURATION;
            case KINDLE -> KINDLE_DURATION;
            case FLAIL -> FLAIL_DURATION;
            case FALL -> FALL_DURATION;
            default -> EMBERS_DURATION;
        };
    }

    @Override
    protected int trackUntil(int id) {
        return switch (id) {
            case RAKE -> RAKE_IMPACT - 10;
            case THRESH -> THRESH_IMPACT - 13;
            case FLAIL -> FLAIL_SECOND - 6;
            case FALL -> FALL_IMPACT - 11;
            case EMBERS -> EMBERS_THROW;
            default -> 0;
        };
    }

    @Override
    protected int attackPause(int finishedId) {
        if (finishedId == KINDLE) {
            return 4;
        }
        return this.down() ? 10 + this.random.nextInt(10) : 18 + this.random.nextInt(14);
    }

    @Override
    public double stopDistance() {
        return 2.4;
    }

    @Override
    protected void onReset() {
        super.onReset();
        this.sunCooldown = 60;
        this.roundCooldown = 120;
        this.fallCooldown = 100;
        this.embersCooldown = 60;
        // back up on the cross, if it has one to go back to
        BlockPos home = MobMarkBlockEntity.homeOf(this);
        if (home != null) {
            this.teleportTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);
        }
    }

    /** Lying where it fell: when it can do nothing about a blow. */
    public boolean open() {
        return this.attackId() == FALL && this.attackTime > FALL_IMPACT + 2 && this.attackTime < FALL_LAIN;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (!this.down()) {
            if (distance > THRESH_REACH) {
                return 0;
            }
            if (this.roundCooldown <= 0 && distance < REACH) {
                this.roundCooldown = 200 + this.random.nextInt(100);
                return ROUND;
            }
            if (distance > REACH - 0.3) {
                return THRESH;
            }
            return this.random.nextInt(10) < 6 ? RAKE : THRESH;
        }
        if (this.embersCooldown <= 0 && distance > 4.0 && distance < 18.0 && this.hasLineOfSight(target)) {
            this.embersCooldown = 150 + this.random.nextInt(80);
            return EMBERS;
        }
        if (this.fallCooldown <= 0 && distance > 2.5 && distance < FALL_REACH - 0.4) {
            this.fallCooldown = 220 + this.random.nextInt(120);
            return FALL;
        }
        if (distance < REACH - 0.4) {
            return FLAIL;
        }
        return 0;
    }

    private static Vec3 ground(Level level, double x, double y, double z) {
        Vec3 found = RootEntity.ground(level, x, y, z);
        return found != null ? found : new Vec3(x, y, z);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.phase() == 1 && this.getHealth() < this.getMaxHealth() * 0.55F && this.attackId() == 0 && this.isAlive()) {
            this.startAttack(KINDLE);
        }
        super.customServerAiStep(level);
        this.roundCooldown = Math.max(0, this.roundCooldown - 1);
        this.fallCooldown = Math.max(0, this.fallCooldown - 1);
        this.embersCooldown = Math.max(0, this.embersCooldown - 1);
        LivingEntity target = this.getTarget();
        if (!this.down()) {
            Vec3 motion = this.getDeltaMovement();
            if (motion.x != 0.0 || motion.z != 0.0) {
                this.setDeltaMovement(0.0, motion.y, 0.0);
            }
            // it turns on its stake after whoever it is watching
            if (target != null && this.attackId() == 0) {
                float want = (float) (Mth.atan2(target.getZ() - this.getZ(), target.getX() - this.getX()) * (180.0 / Math.PI)) - 90.0F;
                float yaw = Mth.approachDegrees(this.getYRot(), want, 6.0F);
                this.setYRot(yaw);
                this.setYBodyRot(yaw);
            }
            // the sun strikes for it
            if (target != null && this.attackId() != KINDLE && --this.sunCooldown <= 0) {
                boolean far = this.distanceTo(target) > THRESH_REACH + 1.5;
                float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.75F;
                Vec3 lead = target.getDeltaMovement().multiply(1.0, 0.0, 1.0).scale(far ? 14.0 : 8.0);
                this.sunStrike(level, ground(level, target.getX() + lead.x, target.getY(), target.getZ() + lead.z), SUN_DELAY, damage);
                if (far) {
                    this.sunStrike(level, ground(level, target.getX(), target.getY(), target.getZ()), SUN_DELAY + 12, damage);
                }
                this.sunCooldown = far ? 50 + this.random.nextInt(20) : 90 + this.random.nextInt(40);
            }
        } else if (this.isAlive()) {
            // alight: whoever is close burns with it, and it burns down
            if (this.tickCount % 10 == 0) {
                for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(1.3, 0.5, 1.3), this::isFoe)) {
                    near.igniteForSeconds(2.0F);
                    near.hurtServer(level, this.damageSources().onFire(), 3.0F);
                }
                FlourCloudEntity.kindle(level, this.position(), 2.0);
            }
            if (this.tickCount % 20 == 0 && this.getHealth() > this.getMaxHealth() * 0.12F) {
                this.setHealth(this.getHealth() - this.getMaxHealth() * 0.0012F);
            }
            if (this.tickCount % 2 == 0) {
                level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + this.getBbHeight() * 0.55, this.getZ(), 3, 0.6, 1.4, 0.6, 0.01);
                level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + this.getBbHeight(), this.getZ(), 1, 0.5, 0.3, 0.5, 0.01);
            }
        }
    }

    /** The sun strikes where it is shown, {@code delay} ticks on: a shaft of its light stands there until then, and flares. */
    private void sunStrike(ServerLevel level, Vec3 at, int delay, float damage) {
        this.strikeLater(at, delay, SUN_RADIUS, damage, true);
        SunShaftEntity.shine(level, at, SHAFT_WIDTH, delay, delay, delay + 10);
    }

    private void straw(ServerLevel level, Vec3 at, int count, double spread) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HAY_BLOCK.defaultBlockState()), at.x, at.y + 0.3, at.z, count, spread, 0.3, spread, 0.15);
        level.sendParticles(ParticleTypes.POOF, at.x, at.y + 0.2, at.z, Math.max(1, count / 6), spread, 0.1, spread, 0.03);
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        switch (id) {
            case RAKE -> {
                if (time == RAKE_IMPACT - 6) {
                    this.playSound(Dushi.sound("entity.warden.sweep"), 2.0F, 0.95F + this.random.nextFloat() * 0.1F);
                }
                if (time == RAKE_IMPACT) {
                    this.hitArc(level, REACH, 110.0, damage, 1.6);
                    Vec3 at = this.position().add(this.facing().scale(3.0));
                    level.sendParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y + 1.4, at.z, 3, 0.9, 0.0, 0.9, 0.0);
                }
            }
            case THRESH -> {
                if (time == THRESH_IMPACT - 7) {
                    this.playSound(Dushi.sound("entity.warden.sweep"), 2.0F, 0.75F);
                }
                if (time == THRESH_IMPACT) {
                    this.hitArc(level, this.facing(), THRESH_REACH, 24.0, damage * 1.5F, 0.5, true);
                    Vec3 at = this.position().add(this.facing().scale(4.2));
                    this.hitAround(level, at, 2.0, damage * 0.6F, 0.8, this.damageSources().mobAttack(this));
                    this.straw(level, at, 50, 1.0);
                    level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.warden.slam"), this.getSoundSource(), 2.6F, 0.9F + this.random.nextFloat() * 0.1F);
                }
            }
            case ROUND -> {
                if (time == ROUND_HITS[0] - 10) {
                    this.playSound(Dushi.sound("entity.warden.roar"), 2.4F, 1.1F);
                }
                for (int hit : ROUND_HITS) {
                    if (time == hit - 4) {
                        this.playSound(Dushi.sound("entity.warden.sweep"), 2.0F, 1.1F);
                    }
                    if (time == hit) {
                        this.hitAround(level, this.position(), REACH + this.getBbWidth() * 0.5, damage * 0.9F, 1.5, this.damageSources().mobAttack(this));
                        for (int i = 0; i < 6; i++) {
                            double angle = i * Math.PI / 3.0;
                            level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + Math.cos(angle) * 3.2, this.getY() + 1.4, this.getZ() + Math.sin(angle) * 3.2,
                                    1, 0.0, 0.0, 0.0, 0.0);
                        }
                    }
                }
            }
            case KINDLE -> {
                if (time == KINDLE_LIGHT) {
                    this.playSound(Dushi.sound("entity.sun.gather"), 3.0F, 0.7F);
                    // the sun itself comes down on it: a shaft wider than the Warden, from now until it catches
                    SunShaftEntity.shine(level, this.position(), KINDLE_SHAFT_WIDTH, 8, KINDLE_BURST - KINDLE_LIGHT, KINDLE_BURST - KINDLE_LIGHT + 16);
                }
                if (time >= KINDLE_LIGHT && time < KINDLE_BURST && time % 2 == 0) {
                    // the light gathers on the Warden itself, and it begins to smoke
                    level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + this.getBbHeight() * 0.6, this.getZ(), 6 + time / 4, 0.9, 1.6, 0.9, 0.02);
                    level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + this.getBbHeight() * 0.8, this.getZ(), 2, 0.6, 0.8, 0.6, 0.02);
                }
                if (time == KINDLE_BURST) {
                    this.setPhase(2);
                    Vec3 at = this.position();
                    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0, 2.0, 6.0), this::isFoe)) {
                        Vec3 away = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ());
                        double far = away.length();
                        if (far < 6.5) {
                            away = far < 1.0E-3 ? this.facing() : away.scale(1.0 / far);
                            if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage * 0.8F)) {
                                victim.igniteForSeconds(4.0F);
                            }
                            shove(victim, new Vec3(away.x * 0.9, 0.36, away.z * 0.9));
                        }
                    }
                    level.sendParticles(ParticleTypes.FLAME, at.x, at.y + 2.2, at.z, 200, 2.2, 2.0, 2.2, 0.16);
                    level.sendParticles(ParticleTypes.LAVA, at.x, at.y + 2.0, at.z, 24, 1.4, 1.4, 1.4, 0.0);
                    this.playSound(Dushi.sound("entity.warden.kindle"), 4.0F, 1.0F);
                    this.playSound(Dushi.sound("entity.warden.roar"), 4.0F, 0.8F);
                    FlourCloudEntity.kindle(level, at, 6.0);
                }
            }
            case FLAIL -> {
                if (time > 6 && time < FLAIL_SECOND) {
                    Vec3 step = this.facing().scale(0.14);
                    this.setDeltaMovement(step.x, this.getDeltaMovement().y, step.z);
                }
                if (time == FLAIL_FIRST - 5 || time == FLAIL_SECOND - 5) {
                    this.playSound(Dushi.sound("entity.warden.sweep"), 2.0F, 1.05F + this.random.nextFloat() * 0.1F);
                }
                if (time == FLAIL_FIRST || time == FLAIL_SECOND) {
                    this.hitArc(level, REACH, 115.0, damage * 0.9F, 1.3);
                    Vec3 at = this.position().add(this.facing().scale(3.0));
                    level.sendParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y + 1.4, at.z, 3, 0.9, 0.0, 0.9, 0.0);
                    level.sendParticles(ParticleTypes.FLAME, at.x, at.y + 1.4, at.z, 20, 1.2, 0.3, 1.2, 0.03);
                }
                if (time == FLAIL_SECOND) {
                    this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
                }
            }
            case FALL -> {
                if (time == FALL_IMPACT - 10) {
                    this.playSound(Dushi.sound("entity.warden.roar"), 2.4F, 0.9F);
                }
                if (time == FALL_IMPACT) {
                    this.hitArc(level, this.facing(), FALL_REACH, 22.0, damage * 1.6F, 0.6, true);
                    for (double far = 1.5; far <= FALL_REACH; far += 1.3) {
                        Vec3 at = this.position().add(this.facing().scale(far));
                        this.hitAround(level, at, 1.6, damage * 0.7F, 0.8, this.damageSources().mobAttack(this));
                        level.sendParticles(ParticleTypes.FLAME, at.x, at.y + 0.4, at.z, 18, 0.7, 0.2, 0.7, 0.05);
                        this.straw(level, at, 16, 0.7);
                    }
                    FlourCloudEntity.kindle(level, this.position().add(this.facing().scale(3.0)), 3.0);
                    this.playSound(Dushi.sound("entity.warden.slam"), 3.0F, 0.8F);
                }
            }
            default -> {
                if (time == EMBERS_THROW - 8) {
                    this.playSound(Dushi.sound("entity.warden.sweep"), 2.0F, 1.3F);
                }
                if (time == EMBERS_THROW) {
                    LivingEntity target = this.getTarget();
                    Vec3 aim = target != null ? target.position() : this.position().add(this.facing().scale(8.0));
                    for (int i = 0; i < 6; i++) {
                        double angle = this.random.nextDouble() * Math.PI * 2.0;
                        double far = i == 0 ? 0.0 : 1.6 + this.random.nextDouble() * 3.2;
                        this.strikeLater(ground(level, aim.x + Math.cos(angle) * far, aim.y, aim.z + Math.sin(angle) * far), 24 + i * 5, EMBER_RADIUS, damage * 0.6F, false);
                    }
                    level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + this.getBbHeight() * 0.7, this.getZ(), 40, 0.8, 0.8, 0.8, 0.2);
                }
            }
        }
    }

    /** Whether the sun reaches somebody: not under a roof, of straw or of anything. */
    public static boolean sunlit(Level level, LivingEntity entity) {
        return level.canSeeSky(BlockPos.containing(entity.getX(), entity.getY() + entity.getBbHeight() + 0.2, entity.getZ()));
    }

    @Override
    protected void strikeComing(ServerLevel level, Vec3 at, double radius, int left, boolean fromAbove) {
        if (!fromAbove) {
            // a brand in the air over the place it will come down on
            if (this.strikeClock() % 2 == 0) {
                level.sendParticles(ParticleTypes.FLAME, at.x, at.y + 0.6 + left * 0.22, at.z, 2, 0.1, 0.1, 0.1, 0.0);
                level.sendParticles(ParticleTypes.SMALL_FLAME, at.x, at.y + 0.1, at.z, 2, radius * 0.4, 0.0, radius * 0.4, 0.0);
            }
            return;
        }
        // light gathering on the ground: thin at first, then a patch too bright to look at, ringed
        int total = SUN_DELAY + 12;
        double bright = 1.0 - Math.min(1.0, left / (double) total);
        if (this.strikeClock() % 2 == 0) {
            level.sendParticles(ParticleTypes.END_ROD, at.x, at.y + 0.1, at.z, 1 + (int) (bright * 8), radius * 0.4, 0.02, radius * 0.4, 0.0);
        }
        if (this.strikeClock() % 3 == 0) {
            int points = 10;
            for (int i = 0; i < points; i++) {
                double angle = i * Math.PI * 2.0 / points + this.strikeClock() * 0.07;
                level.sendParticles(left < 10 ? ParticleTypes.FLAME : ParticleTypes.SMALL_FLAME, at.x + Math.cos(angle) * radius, at.y + 0.1, at.z + Math.sin(angle) * radius,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }
        if (left == SUN_DELAY - 2) {
            level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.sun.gather"), this.getSoundSource(), 1.4F, 1.2F);
        }
    }

    @Override
    protected void strikeLands(ServerLevel level, Vec3 at, double radius, float damage, boolean fromAbove) {
        DamageSource source = this.damageSources().mobAttack(this);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(radius + 1.0, 3.0, radius + 1.0), this::isFoe)) {
            double far = Math.sqrt(victim.distanceToSqr(at.x, victim.getY(), at.z)) - victim.getBbWidth() * 0.5;
            if (far > radius || fromAbove && !sunlit(level, victim)) {
                continue;
            }
            if (victim.hurtServer(level, source, damage)) {
                victim.igniteForSeconds(fromAbove ? 3.0F : 4.0F);
            }
        }
        FlourCloudEntity.kindle(level, at, radius);
        level.sendParticles(ParticleTypes.FLAME, at.x, at.y + 0.3, at.z, fromAbove ? 60 : 30, radius * 0.5, 0.3, radius * 0.5, 0.06);
        if (fromAbove) {
            level.sendParticles(net.minecraft.core.particles.ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFF0C0), at.x, at.y + 0.5, at.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        level.playSound(null, at.x, at.y, at.z, Dushi.sound("entity.sun.burn"), this.getSoundSource(), 1.6F, 0.9F + this.random.nextFloat() * 0.2F);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.is(DamageTypeTags.IS_FIRE)) {
            return false;
        }
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
        this.spawnAtLocation(level, new ItemStack(Dushi.WARDEN_SOUL.get()));
        this.spawnAtLocation(level, new ItemStack(Dushi.FLASK_SHARD.get()));
        this.leave(level, Dushi.GREAT_SOUL.get(), 2, 3);
        this.leave(level, Dushi.RYE_STRAW.get(), 4, 6);
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
        return Dushi.sound("entity.warden.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.warden.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.warden.death");
    }

    @Override
    protected float getSoundVolume() {
        return 2.4F;
    }
}
