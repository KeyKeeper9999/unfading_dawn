package com.keykeeper.dushi.block;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.SoulsMob;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/** What a {@link MobMarkBlock} remembers: which mob is its own right now, and whether that mob has been killed. */
public class MobMarkBlockEntity extends BlockEntity {
    /** A rest at a hearth brings back the dead of every mark this close. */
    public static final int RESET_RADIUS = 96;
    /** A mark wakes when a player is this close. */
    private static final double WAKE_RANGE = 40.0;
    /** The key under which a mob keeps the position of its mark. */
    private static final String HOME = "DushiHome";

    private UUID mob;
    private boolean dead;

    public MobMarkBlockEntity(BlockPos pos, BlockState state) {
        super(Dushi.MOB_MARK_ENTITY.get(), pos, state);
    }

    public boolean dead() {
        return this.dead;
    }

    public UUID mob() {
        return this.mob;
    }

    void serverTick(Level level, BlockPos pos, BlockState state) {
        if (this.dead || this.mob != null || !(level instanceof ServerLevel serverLevel) || Math.floorMod(level.getGameTime() + pos.hashCode(), 20L) != 0) {
            return;
        }
        if (level.getNearestPlayer(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, WAKE_RANGE, false) == null) {
            return;
        }
        this.spawn(serverLevel, pos, state);
    }

    /** Raises the mark's mob now if it is due, whether or not anybody is near. Returns true if one rose. */
    public boolean wake(ServerLevel level) {
        if (this.dead || this.mob != null) {
            return false;
        }
        this.spawn(level, this.getBlockPos(), this.getBlockState());
        return this.mob != null;
    }

    private void spawn(ServerLevel level, BlockPos pos, BlockState state) {
        MobMarkBlock.Kind kind = state.getValue(MobMarkBlock.MOB);
        Mob spawned = kind.type().create(level, EntitySpawnReason.STRUCTURE);
        if (spawned == null || (spawned instanceof Enemy && level.getDifficulty() == Difficulty.PEACEFUL)) {
            return;
        }
        float yaw = state.getValue(MobMarkBlock.FACING).toYRot();
        spawned.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0.0F);
        spawned.setYBodyRot(yaw);
        spawned.setYHeadRot(yaw);
        spawned.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.STRUCTURE, null);
        if (spawned instanceof SoulsMob souls) {
            souls.setTier(state.getValue(MobMarkBlock.TIER));
        }
        spawned.setPersistenceRequired();
        spawned.getPersistentData().putLong(HOME, pos.asLong());
        level.addFreshEntityWithPassengers(spawned);
        this.mob = spawned.getUUID();
        this.setChanged();
    }

    /** Lets go of the current mob without touching it (the mark was changed by hand). */
    void forget() {
        this.mob = null;
        this.dead = false;
        this.setChanged();
    }

    /**
     * A rest: an ordinary mob is taken away and rises again at its mark; a living boss goes back to its place and heals,
     * a slain one stays slain. The keykeeper is left alone unless somebody killed him.
     */
    private void reset(ServerLevel level) {
        MobMarkBlock.Kind kind = this.getBlockState().getValue(MobMarkBlock.MOB);
        Entity current = this.mob != null ? level.getEntity(this.mob) : null;
        if (kind.boss || (kind == MobMarkBlock.Kind.KEYKEEPER && !this.dead)) {
            if (!this.dead && current instanceof SoulsMob boss) {
                BlockPos pos = this.getBlockPos();
                boss.returnHome(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, this.getBlockState().getValue(MobMarkBlock.FACING).toYRot());
            }
            return;
        }
        if (current != null) {
            current.discard();
        }
        this.mob = null;
        this.dead = false;
        this.setChanged();
    }

    /** Brings back the dead of every mark within {@code radius} blocks that is loaded, and heals every enemy there that lived. */
    public static int resetAround(ServerLevel level, BlockPos centre, int radius) {
        List<MobMarkBlockEntity> marks = new ArrayList<>();
        int minX = (centre.getX() - radius) >> 4;
        int maxX = (centre.getX() + radius) >> 4;
        int minZ = (centre.getZ() - radius) >> 4;
        int maxZ = (centre.getZ() + radius) >> 4;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity entity : chunk.getBlockEntities().values()) {
                    if (entity instanceof MobMarkBlockEntity mark && mark.getBlockPos().distSqr(centre) <= (double) radius * radius) {
                        marks.add(mark);
                    }
                }
            }
        }
        marks.forEach(mark -> mark.reset(level));
        // Those that no mark answers for (hatched from an egg, or called by a boss) stay, but are whole again.
        for (SoulsMob mob : level.getEntitiesOfClass(SoulsMob.class, new AABB(centre).inflate(radius), SoulsMob::isAlive)) {
            if (homeOf(mob) == null) {
                mob.recover();
            }
        }
        return marks.size();
    }

    /** The mark {@code entity} came from, if it came from one that still counts it as its own. */
    private static MobMarkBlockEntity markOf(LivingEntity entity) {
        long home = entity.getPersistentData().getLongOr(HOME, Long.MIN_VALUE);
        if (home == Long.MIN_VALUE) {
            return null;
        }
        BlockPos pos = BlockPos.of(home);
        if (entity.level().isLoaded(pos) && entity.level().getBlockEntity(pos) instanceof MobMarkBlockEntity mark && entity.getUUID().equals(mark.mob)) {
            return mark;
        }
        return null;
    }

    /** Tells the mark of a mob that has just been killed. */
    public static void mobDied(LivingEntity entity) {
        MobMarkBlockEntity mark = markOf(entity);
        if (mark != null) {
            mark.dead = true;
            mark.setChanged();
        }
    }

    /** The position of the mark a mob belongs to, or null if it was not put there by one. */
    public static BlockPos homeOf(LivingEntity entity) {
        long home = entity.getPersistentData().getLongOr(HOME, Long.MIN_VALUE);
        return home == Long.MIN_VALUE ? null : BlockPos.of(home);
    }

    /**
     * True if {@code entity} was put there by a mark that is loaded and no longer counts it as its own - a leftover
     * from before a rest, which should go.
     */
    public static boolean disowned(LivingEntity entity) {
        BlockPos home = homeOf(entity);
        return home != null && entity.level().isLoaded(home) && markOf(entity) == null;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Mob", this.mob == null ? "" : this.mob.toString());
        output.putBoolean("Dead", this.dead);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String id = input.getStringOr("Mob", "");
        try {
            this.mob = id.isEmpty() ? null : UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            this.mob = null;
        }
        this.dead = input.getBooleanOr("Dead", false);
    }
}
