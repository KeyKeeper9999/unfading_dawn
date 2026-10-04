package com.keykeeper.dushi.world;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Sets a {@link Plan} into the world. The game calls {@link #postProcess} once for every chunk the building touches,
 * and each time only the cells inside that chunk are placed.
 */
public class PlanPiece extends StructurePiece {
    /** How far down the ground under the building is filled in where the land falls away. */
    private static final int FOUNDATION = 14;

    private final String name;
    private final BlockPos origin;
    private final Rotation rotation;

    public PlanPiece(String name, BlockPos origin, Rotation rotation) {
        super(Dushi.PLAN_PIECE.get(), 0, box(Plan.get(name), origin, rotation));
        this.name = name;
        this.origin = origin;
        this.rotation = rotation;
    }

    public PlanPiece(CompoundTag tag) {
        super(Dushi.PLAN_PIECE.get(), tag);
        this.name = tag.getStringOr("Plan", "outpost");
        this.origin = new BlockPos(tag.getIntOr("OX", 0), tag.getIntOr("OY", 64), tag.getIntOr("OZ", 0));
        this.rotation = Rotation.values()[Math.floorMod(tag.getIntOr("Rot", 0), 4)];
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("Plan", this.name);
        tag.putInt("OX", this.origin.getX());
        tag.putInt("OY", this.origin.getY());
        tag.putInt("OZ", this.origin.getZ());
        tag.putInt("Rot", this.rotation.ordinal());
    }

    public String planName() {
        return this.name;
    }

    public BlockPos origin() {
        return this.origin;
    }

    public Rotation rotation() {
        return this.rotation;
    }

    /** Where cell (x, y, z) of the plan lands when the plan's origin cell is put on {@code origin}. */
    public static BlockPos toWorld(Plan plan, BlockPos origin, Rotation rotation, int x, int y, int z) {
        int dx = x - plan.originX;
        int dz = z - plan.originZ;
        int wx;
        int wz;
        switch (rotation) {
            case CLOCKWISE_90 -> {
                wx = -dz;
                wz = dx;
            }
            case CLOCKWISE_180 -> {
                wx = -dx;
                wz = -dz;
            }
            case COUNTERCLOCKWISE_90 -> {
                wx = dz;
                wz = -dx;
            }
            default -> {
                wx = dx;
                wz = dz;
            }
        }
        return new BlockPos(origin.getX() + wx, origin.getY() + y - plan.originY, origin.getZ() + wz);
    }

    /** From the floor layer (the one under the feet) to the top, so that the land around is shaped to meet the floor. */
    private static BoundingBox box(Plan plan, BlockPos origin, Rotation rotation) {
        BlockPos a = toWorld(plan, origin, rotation, 0, Math.max(0, plan.originY - 1), 0);
        BlockPos b = toWorld(plan, origin, rotation, plan.width - 1, plan.height - 1, plan.depth - 1);
        return new BoundingBox(Math.min(a.getX(), b.getX()), a.getY(), Math.min(a.getZ(), b.getZ()),
                Math.max(a.getX(), b.getX()), b.getY(), Math.max(a.getZ(), b.getZ()));
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource chunkRandom,
            BoundingBox chunkBox, ChunkPos chunkPos, BlockPos referencePos) {
        Plan plan = Plan.get(this.name);
        for (int y = 0; y < plan.height; y++) {
            for (int z = 0; z < plan.depth; z++) {
                for (int x = 0; x < plan.width; x++) {
                    short index = plan.cell(x, y, z);
                    if (index == Plan.SKIP) {
                        continue;
                    }
                    BlockPos pos = toWorld(plan, this.origin, this.rotation, x, y, z);
                    if (!chunkBox.isInside(pos)) {
                        continue;
                    }
                    // Bars and walls come from the plan already joined to their neighbours; turning the state turns the joints.
                    BlockState state = plan.palette[index].rotate(this.rotation);
                    level.setBlock(pos, state, 2);
                    if (plan.loot[index] != null) {
                        RandomizableContainer.setBlockEntityLootTable(level, RandomSource.create(pos.asLong() ^ level.getSeed()), pos, plan.loot[index]);
                    }
                    if (y == 0 && !state.isAir()) {
                        this.foundation(level, pos);
                    }
                }
            }
        }
    }

    /** Stone under a floor block for as far as there is nothing to stand on. */
    private void foundation(WorldGenLevel level, BlockPos floor) {
        BlockPos.MutableBlockPos pos = floor.mutable();
        for (int i = 0; i < FOUNDATION; i++) {
            pos.move(0, -1, 0);
            BlockState below = level.getBlockState(pos);
            if (!below.isAir() && !below.liquid() && !below.canBeReplaced()) {
                return;
            }
            level.setBlock(pos, (i + pos.getX() + pos.getZ()) % 3 == 0 ? Blocks.COBBLESTONE.defaultBlockState() : Blocks.STONE_BRICKS.defaultBlockState(), 2);
        }
    }
}
