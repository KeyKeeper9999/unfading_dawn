package com.keykeeper.dushi.world;

import java.util.Arrays;
import java.util.Optional;

import com.keykeeper.dushi.Dushi;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/** A structure that is one {@link Plan} set on the surface, turned one of four ways. Found with /locate like any other. */
public class PlanStructure extends Structure {
    public static final MapCodec<PlanStructure> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            settingsCodec(i), Codec.STRING.fieldOf("plan").forGetter(structure -> structure.plan)).apply(i, PlanStructure::new));
    /** The ground under the building may differ by this much at most; steeper places are passed over. */
    private static final int MAX_SLOPE = 10;

    private final String plan;

    public PlanStructure(Structure.StructureSettings settings, String plan) {
        super(settings);
        this.plan = plan;
    }

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        Plan plan = Plan.get(this.plan);
        int x = context.chunkPos().getMiddleBlockX();
        int z = context.chunkPos().getMiddleBlockZ();
        long seed = context.seed() ^ (x * 341873128712L + z * 132897987541L);
        Rotation rotation = Rotation.values()[(int) Math.floorMod(seed >> 12, 4L)];
        // The ground at the middle and at the four corners: no water under the walls, no cliff through the yard.
        int[][] corners = {{plan.originX, plan.originZ}, {0, 0}, {plan.width - 1, 0}, {0, plan.depth - 1}, {plan.width - 1, plan.depth - 1}};
        int[] heights = new int[corners.length];
        for (int i = 0; i < corners.length; i++) {
            BlockPos at = PlanPiece.toWorld(plan, new BlockPos(x, 0, z), rotation, corners[i][0], plan.originY, corners[i][1]);
            heights[i] = context.chunkGenerator().getFirstFreeHeight(at.getX(), at.getZ(), Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(),
                    context.randomState());
            int floor = context.chunkGenerator().getFirstFreeHeight(at.getX(), at.getZ(), Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(),
                    context.randomState());
            if (floor < context.chunkGenerator().getSeaLevel()) {
                return Optional.empty();
            }
        }
        Arrays.sort(heights);
        if (heights[heights.length - 1] - heights[0] > MAX_SLOPE) {
            return Optional.empty();
        }
        BlockPos origin = new BlockPos(x, heights[1], z);
        return Optional.of(new Structure.GenerationStub(origin, builder -> builder.addPiece(new PlanPiece(this.plan, origin, rotation))));
    }

    @Override
    public StructureType<?> type() {
        return Dushi.PLAN_STRUCTURE.get();
    }
}
