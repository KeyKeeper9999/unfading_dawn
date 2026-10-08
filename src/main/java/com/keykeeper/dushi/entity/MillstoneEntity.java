package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The runner stone of the great mill: a millstone on edge at the end of a beam, rolling round the post in the middle
 * of the grinding floor, over a bed there has been no grain on for a long time. It is part of the Hollow Miller's
 * fight and nobody's enemy: it cannot be hurt, it goes where the beam takes it, and whoever is in its way when it
 * comes is run down and thrown.
 * <p>
 * The Miller is its master: while he fights it rolls at a walk; when he heaves on the beam it rolls faster than a man
 * runs, for a while; when he checks it, it slows, stands and comes back the other way. With no miller near it stands
 * still and harms nobody. It is a creature of the game only so as to be drawn and moved as one.
 */
public class MillstoneEntity extends SoulsMob {
    private static final EntityDataAccessor<Float> ANGLE = SynchedEntityData.defineId(MillstoneEntity.class, EntityDataSerializers.FLOAT);

    /** From the post to the middle of the stone, in blocks. */
    public static final double RADIUS = 6.0;
    /** Radians a tick: at a walk, and with the Miller's weight on the beam. */
    public static final double PACE = Math.PI * 2.0 / 250.0;
    public static final double HASTE = Math.PI * 2.0 / 120.0;
    /** Half the width of what it runs down. */
    public static final double BREADTH = 1.15;
    private static final int HASTE_TICKS = 170;

    private BlockPos post;
    private double angle;
    private double turning;
    private int way = 1;
    private int haste;
    private boolean driven;
    private int rumble;

    public MillstoneEntity(EntityType<? extends MillstoneEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.setPersistenceRequired();
        this.setSilent(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 100.0).add(Attributes.ATTACK_DAMAGE, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    /** A stone on its beam round {@code post} (the block the Miller's fight is centred on), standing still. */
    public static MillstoneEntity set(ServerLevel level, BlockPos post) {
        MillstoneEntity stone = new MillstoneEntity(Dushi.MILLSTONE.get(), level);
        stone.post = post;
        stone.place();
        level.addFreshEntity(stone);
        return stone;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANGLE, 0.0F);
    }

    @Override
    protected void registerGoals() {
    }

    public BlockPos post() {
        return this.post;
    }

    /** How far round it has gone, in radians: the model rolls the stone by it. */
    public float angle() {
        return this.entityData.get(ANGLE);
    }

    /** Radians a tick, signed: for the tests. */
    public double turning() {
        return this.turning;
    }

    /** The Miller fights: it rolls. */
    public void drive(boolean driven) {
        this.driven = driven;
    }

    /** The Miller's weight on the beam: faster than a man runs, for a while. */
    public void hasten() {
        this.haste = HASTE_TICKS;
    }

    /** Checked: it slows, stands, and comes back the other way. */
    public void turnBack() {
        this.way = -this.way;
        this.playSound(Dushi.sound("entity.millstone.check"), 2.4F, 1.0F);
    }

    /** Torn off its axle: the beam goes, and the stone is the Miller's to carry. */
    public void unship(ServerLevel level) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState()), this.getX(), this.getY() + 1.0, this.getZ(), 60, 1.2, 0.6, 1.2, 0.2);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), Dushi.sound("entity.millstone.crash"), this.getSoundSource(), 3.0F, 1.0F);
        this.discard();
    }

    private void place() {
        double x = this.post.getX() + 0.5 + Math.cos(this.angle) * RADIUS;
        double z = this.post.getZ() + 0.5 + Math.sin(this.angle) * RADIUS;
        this.setPos(x, this.post.getY(), z);
        // it faces the way the angle grows; the beam is on its right, towards the post
        float yaw = (float) (Math.atan2(Math.sin(this.angle), Math.cos(this.angle)) * (180.0 / Math.PI));
        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);
        this.yRotO = yaw;
        this.yBodyRotO = yaw;
        this.yHeadRotO = yaw;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            // (the model reads the angle out of the clock of the moves, which a stone has no other use for)
            this.attackTime = Math.round(this.angle() * 1000.0F);
            return;
        }
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.post == null) {
            this.post = BlockPos.containing(this.getX() - RADIUS, this.getY(), this.getZ());
        }
        if (this.tickCount % 10 == 0) {
            // no miller, no milling
            if (level.getEntitiesOfClass(HollowMillerEntity.class, new AABB(this.post).inflate(24.0, 8.0, 24.0), LivingEntity::isAlive).isEmpty()) {
                this.driven = false;
            }
        }
        this.haste = Math.max(0, this.haste - 1);
        double wanted = this.driven ? this.way * (this.haste > 0 ? HASTE : PACE) : 0.0;
        this.turning += Math.max(-HASTE / 30.0, Math.min(HASTE / 30.0, wanted - this.turning));
        this.angle += this.turning;
        this.entityData.set(ANGLE, (float) this.angle);
        this.setDeltaMovement(Vec3.ZERO);
        this.place();
        if (Math.abs(this.turning) > PACE * 0.3) {
            this.run(level);
            if (--this.rumble <= 0) {
                this.rumble = this.haste > 0 ? 14 : 26;
                this.playSound(Dushi.sound("entity.millstone.rumble"), this.haste > 0 ? 2.0F : 1.2F, this.haste > 0 ? 1.25F : 0.9F);
            }
            if (this.tickCount % 3 == 0) {
                level.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 0.1, this.getZ(), 1, 0.3, 0.02, 0.3, 0.01);
            }
        }
    }

    /** Whoever is in its way is run down: struck, and thrown on ahead of it and out. */
    private void run(ServerLevel level) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * (Math.abs(this.turning) > PACE * 1.4 ? 1.35F : 1.0F);
        double sign = Math.signum(this.turning);
        Vec3 along = new Vec3(-Math.sin(this.angle) * sign, 0.0, Math.cos(this.angle) * sign);
        Vec3 out = new Vec3(Math.cos(this.angle), 0.0, Math.sin(this.angle));
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(1.0, 0.5, 1.0), this::isFoe)) {
            double dx = victim.getX() - this.getX();
            double dz = victim.getZ() - this.getZ();
            if (dx * dx + dz * dz > (BREADTH + victim.getBbWidth() * 0.5) * (BREADTH + victim.getBbWidth() * 0.5) || victim.getY() > this.getY() + 2.2) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().mobAttack(this), damage)) {
                // out of the track, whichever side of it they were nearer
                double side = dx * out.x + dz * out.z >= 0.0 ? 1.0 : -1.0;
                shove(victim, new Vec3(along.x * 0.55 + out.x * side * 0.6, 0.38, along.z * 0.55 + out.z * side * 0.6));
            }
        }
    }

    @Override
    public int soulReward() {
        return 0;
    }

    @Override
    protected int attackDuration(int id) {
        return 1;
    }

    @Override
    public int chooseAttack(LivingEntity target, double distance) {
        return 0;
    }

    @Override
    protected void attackTick(ServerLevel level, int id, int time) {
    }

    @Override
    public void setTarget(LivingEntity target) {
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (damage == Float.MAX_VALUE) {
            return super.hurtServer(level, source, damage);         // (the kill command still clears it away)
        }
        if (source.getDirectEntity() != null) {
            this.playSound(Dushi.sound("entity.unhewn.hurt"), 0.8F, 1.4F);
        }
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.post != null) {
            output.putLong("Post", this.post.asLong());
        }
        output.putFloat("Angle", (float) this.angle);
        output.putInt("Way", this.way);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        long post = input.getLongOr("Post", Long.MIN_VALUE);
        this.post = post == Long.MIN_VALUE ? null : BlockPos.of(post);
        this.angle = input.getFloatOr("Angle", 0.0F);
        this.way = input.getIntOr("Way", 1) < 0 ? -1 : 1;
    }
}
