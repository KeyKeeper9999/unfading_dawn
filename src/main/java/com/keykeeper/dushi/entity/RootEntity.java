package com.keykeeper.dushi.entity;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * A root of the Weeping Mother coming up under somebody. It stands on the ground: first the earth is broken and the
 * ends of the roots show through it and tremble - that is the warning, and the place to step out of - then they come
 * up all at once, as high as a man and half again, and whoever is still there is struck and thrown; they stand a
 * moment and go back down. The blow is hers (she is told when it lands); a root nobody sent (one summoned by hand)
 * strikes nobody and stands a long time, to be looked at.
 */
public class RootEntity extends Entity {
    private static final EntityDataAccessor<Integer> WARNING = SynchedEntityData.defineId(RootEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STAND = SynchedEntityData.defineId(RootEntity.class, EntityDataSerializers.INT);
    /** Ticks it takes to come up, and to go back down. */
    public static final float RISE = 3.0F;
    public static final float SINK = 12.0F;
    private static final int STANDS = 18;
    private static final int STANDS_ALONE = 400;

    private WeepingMotherEntity sender;

    public RootEntity(EntityType<? extends RootEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /** A root of {@code sender}'s on the ground at {@code at}; it comes up after {@code warning} ticks. */
    public static RootEntity rise(ServerLevel level, WeepingMotherEntity sender, Vec3 at, int warning) {
        RootEntity root = new RootEntity(Dushi.ROOT.get(), level);
        root.sender = sender;
        root.entityData.set(WARNING, warning);
        root.entityData.set(STAND, STANDS);
        root.setPos(at.x, at.y, at.z);
        root.setYRot(level.getRandom().nextFloat() * 360.0F);
        level.addFreshEntity(root);
        return root;
    }

    /**
     * Where a root can come up for somebody at ({@code x}, {@code y}, {@code z}): the ground straight under them - they may
     * be in the air, thrown there by the root before - or null if there is none within a few blocks, or no room over it.
     */
    public static Vec3 ground(Level level, double x, double y, double z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int bx = Mth.floor(x);
        int bz = Mth.floor(z);
        int top = Mth.floor(y + 0.4);
        for (int by = top; by >= top - 7; by--) {
            pos.set(bx, by - 1, bz);
            BlockState below = level.getBlockState(pos);
            net.minecraft.world.phys.shapes.VoxelShape floor = below.getCollisionShape(level, pos);
            if (floor.isEmpty() || floor.max(net.minecraft.core.Direction.Axis.Y) < 0.8) {
                continue;                                        // (mud is a floor too, though it is not a whole block high)
            }
            for (int h = 0; h < 2; h++) {
                pos.set(bx, by + h, bz);
                if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                    return null;                                 // a wall, a bough: nothing comes up into that
                }
            }
            return new Vec3(x, by, z);
        }
        return null;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(WARNING, 19);
        builder.define(STAND, STANDS_ALONE);
    }

    public int warning() {
        return this.entityData.get(WARNING);
    }

    public int stand() {
        return this.entityData.get(STAND);
    }

    /** Whether it is out of the ground: for the test. */
    public boolean isUp() {
        return this.tickCount >= this.warning() + (int) RISE;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        int warning = this.warning();
        if (this.tickCount < warning) {
            // the earth jumps over the whole of the place it will strike: the edge of it is where the earth stops
            if (this.tickCount % 2 == 0) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState()), this.getX(), this.getY() + 0.1, this.getZ(), 5,
                        WeepingMotherEntity.ROOT_RADIUS * 0.45, 0.05, WeepingMotherEntity.ROOT_RADIUS * 0.45, 0.0);
            }
            if (this.tickCount % 4 == 0) {
                for (int i = 0; i < 8; i++) {
                    double angle = i * Math.PI / 4.0 + this.tickCount * 0.11;
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ROOTED_DIRT.defaultBlockState()),
                            this.getX() + Math.cos(angle) * WeepingMotherEntity.ROOT_RADIUS, this.getY() + 0.1, this.getZ() + Math.sin(angle) * WeepingMotherEntity.ROOT_RADIUS,
                            1, 0.05, 0.02, 0.05, 0.0);
                }
            }
        } else if (this.tickCount == warning) {
            if (this.sender != null && this.sender.isAlive()) {
                this.sender.rootStrikes(level, this.position());
            }
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MANGROVE_ROOTS.defaultBlockState()), this.getX(), this.getY() + 0.8, this.getZ(), 30,
                    WeepingMotherEntity.ROOT_RADIUS * 0.35, 0.9, WeepingMotherEntity.ROOT_RADIUS * 0.35, 0.1);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState()), this.getX(), this.getY() + 0.2, this.getZ(), 24,
                    WeepingMotherEntity.ROOT_RADIUS * 0.5, 0.2, WeepingMotherEntity.ROOT_RADIUS * 0.5, 0.1);
            level.playSound(null, this.getX(), this.getY(), this.getZ(), Dushi.sound("entity.weeping_mother.root"), SoundSource.HOSTILE, 1.5F,
                    0.85F + level.getRandom().nextFloat() * 0.3F);
        } else if (this.tickCount > warning + this.stand() + (int) SINK) {
            this.discard();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;                                            // (its blow belongs to a fight that will not be there when the world is opened again)
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }
}
