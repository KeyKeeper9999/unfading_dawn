package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.Spell;
import com.keykeeper.dushi.block.FogGateBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The Chaplain of the Stockade: a tall thin priest in rotten vestments who never stopped holding services for the withered.
 * Second boss of the mod, and the opposite of the overseer: he keeps his distance, throws spears of light,
 * answers anyone who comes close with a burst of wrath or simply is not there any more.
 * <p>
 * Below half health the censer on his chain burns black: the spears turn into bursting dark flame, he calls two guards
 * from the pews, and from time to time he starts a litany that heals him until somebody hits him.
 */
public class ChaplainEntity extends SoulsMob {
    public static final int SPEARS = 1;
    public static final int SPEARS_DURATION = 46;
    public static final int WRATH = 2;
    public static final int WRATH_DURATION = 52;
    public static final int WRATH_BURST = 30;
    public static final int BLINK = 3;
    public static final int BLINK_DURATION = 18;
    public static final int BLINK_AT = 7;
    public static final int CENSER = 4;
    public static final int CENSER_DURATION = 30;
    public static final int CENSER_IMPACT = 14;
    public static final int LITANY = 5;
    public static final int LITANY_DURATION = 90;
    public static final int CALL = 6;
    public static final int CALL_DURATION = 50;
    public static final int CALL_PEAK = 20;

    private static final double WRATH_RADIUS = 6.0;

    private int wrathCooldown = 80;
    private int blinkCooldown;
    private int litanyCooldown = 200;
    private int recentHits;
    private int recentHitsAge;

    public ChaplainEntity(EntityType<? extends ChaplainEntity> type, Level level) {
        super(type, level);
        this.xpReward = 150;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 240.0).add(Attributes.ATTACK_DAMAGE, 9.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27).add(Attributes.ARMOR, 2.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulsCombatGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public int soulReward() {
        return 5000;
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    protected BossEvent.BossBarColor bossBarColor() {
        return BossEvent.BossBarColor.YELLOW;
    }

    @Override
    protected int attackDuration(int id) {
        return switch (id) {
            case SPEARS -> SPEARS_DURATION;
            case WRATH -> WRATH_DURATION;
            case BLINK -> BLINK_DURATION;
            case CENSER -> CENSER_DURATION;
            case LITANY -> LITANY_DURATION;
            default -> CALL_DURATION;
        };
    }

    @Override
    protected int trackUntil(int id) {
        return switch (id) {
            case SPEARS -> SPEARS_DURATION;
            case CENSER -> CENSER_IMPACT - 5;
            default -> 0;
        };
    }

    @Override
    protected int attackPause(int finishedId) {
        return this.phase() >= 2 ? 10 + this.random.nextInt(10) : 20 + this.random.nextInt(14);
    }

    /** He does not walk up to his target: ten blocks is close enough for a spear. */
    @Override
    public double stopDistance() {
        return 10.0;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        if (distance < 3.2) {
            if (this.blinkCooldown <= 0 && this.random.nextBoolean()) {
                return BLINK;
            }
            return CENSER;
        }
        if (this.phase() >= 2 && this.litanyCooldown <= 0 && this.getHealth() < this.getMaxHealth() * 0.45F && distance > 6.0) {
            this.litanyCooldown = 420;
            return LITANY;
        }
        if (distance < WRATH_RADIUS + 1.0 && this.wrathCooldown <= 0) {
            this.wrathCooldown = 170;
            return WRATH;
        }
        if (distance < 26.0 && this.hasLineOfSight(target)) {
            return SPEARS;
        }
        return 0;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.phase() == 1 && this.getHealth() < this.getMaxHealth() * 0.5F && this.attackId() != CALL && this.isAlive()) {
            this.setPhase(2);
            this.startAttack(CALL);
        }
        super.customServerAiStep(level);
        if (this.wrathCooldown > 0) {
            this.wrathCooldown--;
        }
        if (this.blinkCooldown > 0) {
            this.blinkCooldown--;
        }
        if (this.litanyCooldown > 0) {
            this.litanyCooldown--;
        }
        if (this.recentHits > 0 && ++this.recentHitsAge > 80) {
            this.recentHits = 0;
        }
        if (this.tickCount % 5 == 0) {
            level.sendParticles(this.phase() >= 2 ? ParticleTypes.SMOKE : ParticleTypes.END_ROD, this.getX(), this.getY() + 0.3, this.getZ(), 1, 0.3, 0.1, 0.3, 0.0);
        }
    }

    @Override
    public void startAttack(int id) {
        super.startAttack(id);
        if (id == WRATH) {
            this.playSound(Dushi.sound("entity.chaplain.bell"), 3.0F, 1.0F);
        } else if (id == LITANY) {
            this.playSound(Dushi.sound("entity.chaplain.ambient"), 3.0F, 0.8F);
        }
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
        LivingEntity target = this.getTarget();
        boolean dark = this.phase() >= 2;
        switch (id) {
            case SPEARS -> {
                boolean shot = dark ? (time == 12 || time == 18 || time == 24 || time == 30) : (time == 14 || time == 22 || time == 30);
                if (shot && target != null) {
                    Vec3 from = this.getEyePosition();
                    Vec3 aim = target.getBoundingBox().getCenter().subtract(from);
                    // Lead the target a little, so standing still is not the answer but strafing is.
                    aim = aim.add(target.getDeltaMovement().multiply(4.0, 0.0, 4.0));
                    SpellBoltEntity.shoot(level, this, dark ? SpellBoltEntity.Kind.DARK_FLAME : SpellBoltEntity.Kind.LIGHT_SPEAR, aim, dark ? 1.0F : 1.35F,
                            dark ? 6.0F : 7.0F);
                    this.playSound(Dushi.sound("entity.chaplain.cast"), 2.0F, dark ? 0.7F : 1.0F + this.random.nextFloat() * 0.2F);
                }
            }
            case WRATH -> {
                if (time < WRATH_BURST && time % 3 == 0) {
                    // The ring on the floor shows how far the burst will reach.
                    for (int i = 0; i < 24; i++) {
                        double angle = i * Math.PI * 2.0 / 24.0 + time * 0.1;
                        level.sendParticles(dark ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.END_ROD, this.getX() + Math.cos(angle) * WRATH_RADIUS,
                                this.getY() + 0.15, this.getZ() + Math.sin(angle) * WRATH_RADIUS, 1, 0.0, 0.0, 0.0, 0.0);
                    }
                    level.sendParticles(ParticleTypes.ENCHANT, this.getX(), this.getY() + 2.0, this.getZ(), 8, 0.6, 0.8, 0.6, 0.6);
                }
                if (time == WRATH_BURST) {
                    this.hitAround(level, this.position(), WRATH_RADIUS, 12.0F, 2.0, this.damageSources().indirectMagic(this, this));
                    level.sendParticles(net.minecraft.core.particles.ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFFFFF), this.getX(), this.getY() + 1.5, this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                    for (int i = 0; i < 36; i++) {
                        double angle = i * Math.PI * 2.0 / 36.0;
                        level.sendParticles(ParticleTypes.CLOUD, this.getX() + Math.cos(angle) * 1.5, this.getY() + 1.0, this.getZ() + Math.sin(angle) * 1.5,
                                0, Math.cos(angle), 0.03, Math.sin(angle), 0.7);
                    }
                    level.playSound(null, this.getX(), this.getY(), this.getZ(), Dushi.sound("item.spell.rebuke"), this.getSoundSource(), 3.0F, 0.7F);
                }
            }
            case BLINK -> {
                if (time == BLINK_AT) {
                    this.blink(level, target);
                    this.blinkCooldown = 90;
                }
            }
            case CENSER -> {
                if (time == CENSER_IMPACT - 4) {
                    this.playSound(Dushi.sound("entity.overseer.chain"), 1.5F, 1.4F);
                }
                if (time == CENSER_IMPACT) {
                    float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    DamageSource before = this.damageSources().mobAttack(this);
                    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0, 1.5, 4.0), this::isFoe)) {
                        Vec3 to = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ());
                        if (to.length() - victim.getBbWidth() * 0.5 <= 3.4 && (to.lengthSqr() < 0.6 || to.normalize().dot(this.facing()) > 0.2)
                                && victim.hurtServer(level, before, damage)) {
                            victim.igniteForSeconds(3.0F);
                            victim.knockback(0.8, this.getX() - victim.getX(), this.getZ() - victim.getZ(), before, damage);
                        }
                    }
                    Vec3 at = this.position().add(this.facing().scale(2.2));
                    level.sendParticles(dark ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME, at.x, at.y + 1.2, at.z, 30, 0.8, 0.4, 0.8, 0.03);
                }
            }
            case LITANY -> {
                if (time % 10 == 0) {
                    this.heal(this.getMaxHealth() * 0.02F);
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.5, this.getZ(), 10, 0.5, 1.0, 0.5, 0.0);
                    level.sendParticles(ParticleTypes.ENCHANT, this.getX(), this.getY() + 2.5, this.getZ(), 14, 0.6, 0.6, 0.6, 0.8);
                }
                if (time % 30 == 1) {
                    this.playSound(Dushi.sound("entity.chaplain.ambient"), 3.0F, 0.8F);
                }
            }
            default -> {
                if (time == CALL_PEAK) {
                    this.playSound(Dushi.sound("entity.chaplain.bell"), 4.0F, 0.7F);
                    level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.5, this.getZ(), 60, 0.8, 1.2, 0.8, 0.05);
                    for (int i = 0; i < 2; i++) {
                        Vec3 side = this.facing().yRot((float) Math.toRadians(i == 0 ? 80.0 : -80.0)).scale(3.0);
                        BlockPos at = BlockPos.containing(this.getX() + side.x, this.getY(), this.getZ() + side.z);
                        WitheredEntity helper = Dushi.WITHERED.get(WitheredEntity.Kind.GUARD).get().spawn(level, at, EntitySpawnReason.MOB_SUMMONED);
                        if (helper != null) {
                            helper.setTarget(target);
                            level.sendParticles(ParticleTypes.LARGE_SMOKE, helper.getX(), helper.getY() + 1.0, helper.getZ(), 12, 0.3, 0.6, 0.3, 0.01);
                        }
                    }
                }
            }
        }
    }

    /** Vanishes and appears eight to eleven blocks from his target, somewhere he can stand and see it from. */
    private void blink(ServerLevel level, LivingEntity target) {
        Vec3 centre = target == null ? this.position() : target.position();
        level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.5, this.getZ(), 30, 0.4, 1.0, 0.4, 0.02);
        this.playSound(Dushi.sound("entity.chaplain.blink"), 2.0F, 1.0F);
        for (int attempt = 0; attempt < 16; attempt++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            double radius = 8.0 + this.random.nextDouble() * 3.0;
            double x = centre.x + Math.cos(angle) * radius;
            double z = centre.z + Math.sin(angle) * radius;
            if (this.randomTeleport(x, centre.y + 1.0, z, false, state -> true)) {
                if (target == null || this.hasLineOfSight(target) || attempt >= 12) {
                    break;
                }
            }
        }
        level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.5, this.getZ(), 30, 0.4, 1.0, 0.4, 0.02);
        if (target != null) {
            this.lookAt(target, 180.0F, 180.0F);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && this.isAlive()) {
            // A blow silences the litany.
            if (this.attackId() == LITANY) {
                this.endAttack();
                this.attackCooldown = 30;
                level.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 2.0, this.getZ(), 20, 0.4, 0.6, 0.4, 0.02);
            }
            // Three blows in a row and he is gone.
            this.recentHitsAge = 0;
            if (++this.recentHits >= 3 && this.attackId() == 0 && source.getEntity() != null) {
                this.recentHits = 0;
                this.startAttack(BLINK);
            }
        }
        return hurt;
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING) || (this.attackId() == CALL && !source.is(DamageTypes.GENERIC_KILL))
                || super.isInvulnerableTo(level, source);
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        this.spawnAtLocation(level, new ItemStack(Dushi.CHAPLAIN_SOUL.get()));
        this.spawnAtLocation(level, new ItemStack(Dushi.CENSER_FLAIL.get()));
        this.spawnAtLocation(level, new ItemStack(Dushi.PRAYER_BEADS.get()));
        this.spawnAtLocation(level, new ItemStack(Dushi.SPELLS.get(Spell.MEND).get()));
        this.spawnAtLocation(level, new ItemStack(Dushi.FLASK_SHARD.get()));
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            FogGateBlock.lift(level, this.blockPosition(), 28);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return Dushi.sound("entity.chaplain.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return Dushi.sound("entity.chaplain.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return Dushi.sound("entity.chaplain.death");
    }

    @Override
    protected float getSoundVolume() {
        return 2.0F;
    }
}
