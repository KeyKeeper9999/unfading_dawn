package com.keykeeper.dushi.entity;

import java.util.List;
import java.util.UUID;

import com.keykeeper.dushi.ArmorSet;
import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.block.MobMarkBlockEntity;

import net.minecraft.core.BlockPos;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Base of every hostile creature of the mod. Their blows are not instant: an attack is a move that lasts a number of ticks,
 * with a wind-up the player can read, an impact tick, and a recovery during which the mob is open. The move and its clock
 * are known on both sides, so the model can show it.
 */
public abstract class SoulsMob extends Monster {
    private static final EntityDataAccessor<Integer> ATTACK = SynchedEntityData.defineId(SoulsMob.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(SoulsMob.class, EntityDataSerializers.INT);

    /** Ticks since the current move began; counted on both sides. */
    public int attackTime;
    /** Ticks until the next move may begin. */
    protected int attackCooldown;
    /** Ticks without a target; a boss heals back up after a while. */
    private int idleTicks;
    private int blockedHits;
    private int blockedHitsAge;
    private int guardDownTicks;
    /** Length of the boss track in ticks (see tools/gen_sounds.py). */
    private static final int MUSIC_LENGTH = 960;
    /** How far from its mark a mob of the stockade strays before it turns back. */
    private static final double HOME_RANGE = 14.0;
    private static final double BAR_NEAR = 12.0;
    private static final double BAR_RANGE = 40.0;
    private int musicTicks;

    /** How much stronger the dead of a harder place are: health, the blows they deal, the souls they leave. */
    public static final float[] TIER_HEALTH = {1.0F, 1.6F, 2.3F, 3.0F};
    public static final float[] TIER_DAMAGE = {1.0F, 1.35F, 1.7F, 2.1F};
    public static final float[] TIER_SOULS = {1.0F, 2.5F, 4.5F, 7.0F};
    private static final Identifier TIER_MODIFIER = Dushi.id("tier");
    private int tier;

    private ServerBossEvent bossBar;

    protected SoulsMob(EntityType<? extends SoulsMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACK, 0);
        builder.define(PHASE, 1);
    }

    // ------------------------------------------------------------------ what a kind of mob defines

    /** Souls given for killing it. */
    public abstract int soulReward();

    /** Bosses have a health bar on the screen and reward everyone nearby. */
    public boolean isBoss() {
        return false;
    }

    /** Length of move {@code id} in ticks. */
    protected abstract int attackDuration(int id);

    /** Until which tick of move {@code id} the mob keeps turning towards its target. */
    protected int trackUntil(int id) {
        return this.attackDuration(id) / 3;
    }

    /** Called every tick when no move is running and the cooldown is over. Returns the move to start, or 0 to keep closing in. */
    public abstract int chooseAttack(LivingEntity target, double distance);

    /** Called every tick of a running move, with {@link #attackTime} already advanced. */
    protected abstract void attackTick(ServerLevel level, int id, int time);

    /** Ticks between two moves. */
    protected int attackPause(int finishedId) {
        return 12;
    }

    /** The mob stops walking when it is this close to its target. */
    public double stopDistance() {
        return 1.5;
    }

    // ------------------------------------------------------------------ the place it belongs to

    public int tier() {
        return this.tier;
    }

    /** Set by the mark that raises the mob: 0 for the first places of a map, up to 3 for its last. */
    public void setTier(int tier) {
        this.tier = Math.max(0, Math.min(TIER_HEALTH.length - 1, tier));
        AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.removeModifier(TIER_MODIFIER);
            if (this.tier > 0) {
                health.addPermanentModifier(new AttributeModifier(TIER_MODIFIER, TIER_HEALTH[this.tier] - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
            this.setHealth(this.getMaxHealth());
        }
    }

    public float damageScale() {
        return TIER_DAMAGE[this.tier];
    }

    /** Souls given for killing it where it stands. */
    public int reward() {
        return Math.round(this.soulReward() * TIER_SOULS[this.tier]);
    }

    /** A player resting by a hearth is not noticed; one who struck first is. */
    @Override
    public void setTarget(LivingEntity target) {
        if (target instanceof Player player && target != this.getTarget() && this.getLastHurtByMob() != target
                && com.keykeeper.dushi.PlayerSoul.of(player).atHearth) {
            return;
        }
        super.setTarget(target);
    }

    // ------------------------------------------------------------------ moves

    public int attackId() {
        return this.entityData.get(ATTACK);
    }

    public int phase() {
        return this.entityData.get(PHASE);
    }

    protected void setPhase(int phase) {
        this.entityData.set(PHASE, phase);
    }

    public boolean canStartAttack() {
        return this.attackId() == 0 && this.attackCooldown <= 0;
    }

    public void startAttack(int id) {
        this.entityData.set(ATTACK, id);
        this.attackTime = 0;
    }

    protected void endAttack() {
        int id = this.attackId();
        this.entityData.set(ATTACK, 0);
        this.attackTime = 0;
        this.attackCooldown = this.attackPause(id);
        // After its own blow a shield bearer needs a moment to bring the shield back up.
        if (this.guardBreakHits() > 0) {
            this.lowerGuard(16);
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (ATTACK.equals(key) && this.level().isClientSide()) {
            this.attackTime = 0;
        }
    }

    @Override
    public void aiStep() {
        if (this.attackId() != 0) {
            this.attackTime++;
        }
        super.aiStep();
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        int id = this.attackId();
        if (id != 0) {
            this.getNavigation().stop();
            LivingEntity target = this.getTarget();
            if (target != null && this.attackTime <= this.trackUntil(id)) {
                this.lookAt(target, 30.0F, 30.0F);
                this.setYBodyRot(this.getYRot());
            }
            this.attackTick(level, id, this.attackTime);
            if (this.attackId() == id && this.attackTime >= this.attackDuration(id)) {
                this.endAttack();
            }
        } else if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }
        if (this.guardBreakHits() > 0) {
            this.guardTick();
        }
        if (this.tickCount % 60 == 0) {
            this.homeTick();
        }
        if (this.isBoss()) {
            this.bossBar().setProgress(this.getHealth() / this.getMaxHealth());
            this.musicTick(level, this.getTarget() != null && this.isAlive());
            // A boss left alone recovers, so a failed attempt starts from the beginning.
            if (this.getTarget() == null) {
                if (++this.idleTicks > 200 && this.getHealth() < this.getMaxHealth()) {
                    this.heal(this.getMaxHealth() * 0.02F);
                    if (this.getHealth() >= this.getMaxHealth()) {
                        this.onReset();
                    }
                }
            } else {
                this.idleTicks = 0;
            }
        }
    }

    /**
     * A mob put there by a mark of the stockade stays around it: with nobody to fight it walks back, and one whose mark
     * has since raised another in its place is a leftover and goes.
     */
    private void homeTick() {
        if (MobMarkBlockEntity.disowned(this)) {
            this.discard();
            return;
        }
        BlockPos home = MobMarkBlockEntity.homeOf(this);
        if (home != null && this.getTarget() == null && this.attackId() == 0 && home.distSqr(this.blockPosition()) > HOME_RANGE * HOME_RANGE) {
            this.getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 1.0);
        }
    }

    /** A rest at a hearth sends a living boss back to where it waits, whole again. */
    public void returnHome(double x, double y, double z, float yaw) {
        this.recover();
        this.teleportTo(x, y, z);
        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);
    }

    /** Whole again and with nobody to fight, as if the fight had never begun: after a player's rest or death. */
    public void recover() {
        if (this.attackId() != 0) {
            this.endAttack();
        }
        this.setTarget(null);
        this.setLastHurtByMob(null);
        this.getNavigation().stop();
        this.setHealth(this.getMaxHealth());
        this.clearFire();
        this.removeAllEffects();
        this.onReset();
        if (this.isBoss() && this.level() instanceof ServerLevel level) {
            this.stopMusic(level);
        }
    }

    /** A boss has fully recovered after losing its target. */
    protected void onReset() {
        this.setPhase(1);
    }

    // ------------------------------------------------------------------ hitting

    /** Horizontal unit vector the body faces. */
    public Vec3 facing() {
        return Vec3.directionFromRotation(0.0F, this.getYRot());
    }

    protected boolean isFoe(LivingEntity entity) {
        return entity != this && entity.isAlive() && !(entity instanceof SoulsMob) && !(entity instanceof ArmorStand)
                && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    /**
     * Hurts everything within {@code range} blocks (measured to the edge of the victim) inside a cone in front of the mob.
     *
     * @param halfAngle half of the cone's opening in degrees; 180 hits all around
     * @return how many were hit
     */
    protected int hitArc(ServerLevel level, double range, double halfAngle, float damage, double knockback) {
        Vec3 facing = this.facing();
        double minDot = Math.cos(Math.toRadians(halfAngle));
        DamageSource source = this.damageSources().mobAttack(this);
        int hits = 0;
        List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(range + 2.0, 2.0, range + 2.0), this::isFoe);
        for (LivingEntity victim : victims) {
            Vec3 to = new Vec3(victim.getX() - this.getX(), 0.0, victim.getZ() - this.getZ());
            double distance = to.length() - victim.getBbWidth() * 0.5 - this.getBbWidth() * 0.5;
            if (distance > range || Math.abs(victim.getY() - this.getY()) > 3.0 + this.getBbHeight() * 0.5) {
                continue;
            }
            if (halfAngle < 180.0 && to.lengthSqr() > 0.6 && to.normalize().dot(facing) < minDot) {
                continue;
            }
            if (victim.hurtServer(level, source, damage)) {
                hits++;
                if (knockback > 0.0) {
                    victim.knockback(knockback, this.getX() - victim.getX(), this.getZ() - victim.getZ(), source, damage);
                }
            }
        }
        return hits;
    }

    /** Hurts everything standing within {@code radius} of a point. */
    protected int hitAround(ServerLevel level, Vec3 centre, double radius, float damage, double knockback, DamageSource source) {
        int hits = 0;
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class,
                new net.minecraft.world.phys.AABB(centre, centre).inflate(radius + 1.0, 3.0, radius + 1.0), this::isFoe)) {
            double distance = Math.sqrt(victim.distanceToSqr(centre.x, victim.getY(), centre.z)) - victim.getBbWidth() * 0.5;
            if (distance > radius) {
                continue;
            }
            if (victim.hurtServer(level, source, damage)) {
                hits++;
                if (knockback > 0.0) {
                    victim.knockback(knockback, centre.x - victim.getX(), centre.z - victim.getZ(), source, damage);
                }
            }
        }
        return hits;
    }

    /** Throws {@code victim} with the given velocity; a player's own client is told at once. */
    protected static void shove(LivingEntity victim, Vec3 velocity) {
        victim.setDeltaMovement(velocity);
        victim.needsSync = true;
        if (victim instanceof ServerPlayer player) {
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
        }
    }

    /** Boss music: starts the track for everyone near, and again every time it has played through. */
    protected void musicTick(ServerLevel level, boolean fighting) {
        if (fighting) {
            if (this.musicTicks-- <= 0) {
                this.musicTicks = MUSIC_LENGTH;
                for (ServerPlayer player : level.players()) {
                    if (player.distanceToSqr(this) < 64.0 * 64.0) {
                        player.connection.send(new ClientboundStopSoundPacket(null, SoundSource.MUSIC));
                        player.connection.send(new ClientboundSoundPacket(Holder.direct(Dushi.sound("music.boss")), SoundSource.MUSIC,
                                player.getX(), player.getY(), player.getZ(), 1.0F, 1.0F, this.random.nextLong()));
                    }
                }
            }
        } else if (this.musicTicks > 0) {
            this.stopMusic(level);
        }
    }

    protected void stopMusic(ServerLevel level) {
        this.musicTicks = 0;
        for (ServerPlayer player : level.players()) {
            player.connection.send(new ClientboundStopSoundPacket(Dushi.id("music.boss"), SoundSource.MUSIC));
        }
    }

    // ------------------------------------------------------------------ what is left behind

    /** Drops between {@code min} and {@code max} of an item. */
    protected void leave(ServerLevel level, ItemLike item, int min, int max) {
        int count = min + this.random.nextInt(max - min + 1);
        if (count > 0) {
            this.spawnAtLocation(level, new ItemStack(item, count));
        }
    }

    /** With the given chance, drops one of an item. */
    protected void leaveMaybe(ServerLevel level, ItemLike item, float chance) {
        if (this.random.nextFloat() < chance) {
            this.spawnAtLocation(level, new ItemStack(item));
        }
    }

    /** With the given chance, drops one piece of a suit of armour, picked at random. */
    protected void leavePiece(ServerLevel level, ArmorSet set, float chance) {
        if (this.random.nextFloat() < chance) {
            this.spawnAtLocation(level, new ItemStack(set.piece(this.random.nextInt(4))));
        }
    }

    protected void swingArm() {
        this.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT);
    }

    // ------------------------------------------------------------------ shield

    /** Blows a shield takes in a row before the arm holding it drops; 0 for mobs that do not carry one. */
    protected int guardBreakHits() {
        return 0;
    }

    /** The shield stays down for this many more ticks. */
    protected void lowerGuard(int ticks) {
        this.guardDownTicks = Math.max(this.guardDownTicks, ticks);
        if (this.isUsingItem() && this.getUsedItemHand() == InteractionHand.OFF_HAND) {
            this.stopUsingItem();
        }
    }

    /** The shield is up whenever the mob has someone to fight and is not in the middle of a blow of its own. */
    private void guardTick() {
        if (this.blockedHits > 0 && ++this.blockedHitsAge > 100) {
            this.blockedHits = 0;
        }
        if (this.guardDownTicks > 0) {
            this.guardDownTicks--;
        }
        boolean wantsGuard = this.getTarget() != null && this.attackId() == 0 && this.guardDownTicks == 0 && !this.getOffhandItem().isEmpty();
        boolean guarding = this.isUsingItem() && this.getUsedItemHand() == InteractionHand.OFF_HAND;
        if (wantsGuard && !this.isUsingItem()) {
            this.startUsingItem(InteractionHand.OFF_HAND);
        } else if (!wantsGuard && guarding) {
            this.stopUsingItem();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean guarding = this.guardBreakHits() > 0 && this.isBlocking();
        float before = this.getHealth();
        boolean hurt = super.hurtServer(level, source, damage);
        if (guarding && this.isAlive() && this.getHealth() >= before && source.getDirectEntity() != null) {
            this.blockedHitsAge = 0;
            if (++this.blockedHits >= this.guardBreakHits()) {
                this.blockedHits = 0;
                this.lowerGuard(50);
                this.playSound(Dushi.sound("player.guard_break"), 1.0F, 0.9F);
                level.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + this.getBbHeight() * 0.6, this.getZ(), 10, 0.3, 0.3, 0.3, 0.2);
            }
        }
        return hurt;
    }

    // ------------------------------------------------------------------ boss bar

    private ServerBossEvent bossBar() {
        if (this.bossBar == null) {
            this.bossBar = new ServerBossEvent(UUID.randomUUID(), this.getDisplayName(), this.bossBarColor(), BossEvent.BossBarOverlay.PROGRESS);
            this.bossBar.setDarkenScreen(true);
        }
        return this.bossBar;
    }

    protected BossEvent.BossBarColor bossBarColor() {
        return BossEvent.BossBarColor.RED;
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        if (this.bossBar != null) {
            this.bossBar.setName(this.getDisplayName());
        }
    }

    /**
     * The bar is shown to those the boss is fighting or who stand in front of it, not to everyone who can see the building:
     * to players within {@link #BAR_RANGE} while it has a target, and to anyone within {@link #BAR_NEAR} at any time.
     */
    private void updateBossBar(ServerLevel level) {
        ServerBossEvent bar = this.bossBar();
        boolean fighting = this.getTarget() != null && this.isAlive();
        for (ServerPlayer player : level.players()) {
            double distance = player.distanceToSqr(this);
            boolean show = this.isAlive() && (distance < BAR_NEAR * BAR_NEAR || (fighting && distance < BAR_RANGE * BAR_RANGE));
            if (show != bar.getPlayers().contains(player)) {
                if (show) {
                    bar.addPlayer(player);
                } else {
                    bar.removePlayer(player);
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isBoss() && this.tickCount % 10 == 1 && this.level() instanceof ServerLevel level) {
            this.updateBossBar(level);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        if (this.isBoss()) {
            this.bossBar().removePlayer(player);
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.isBoss() && this.level() instanceof ServerLevel level) {
            this.stopMusic(level);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return !this.isBoss() && super.removeWhenFarAway(distSqr);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Phase", this.phase());
        output.putInt("Tier", this.tier);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setPhase(input.getIntOr("Phase", 1));
        this.tier = Math.max(0, Math.min(TIER_HEALTH.length - 1, input.getIntOr("Tier", 0)));
    }
}
