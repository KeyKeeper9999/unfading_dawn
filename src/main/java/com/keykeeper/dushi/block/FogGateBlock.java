package com.keykeeper.dushi.block;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.WeakHashMap;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A wall of white fog across a doorway. Players push through it; monsters, arrows and everything else are stopped.
 * It cannot be broken in survival: it lifts by itself when the boss behind it dies (see {@link #lift}).
 */
public class FogGateBlock extends Block {
    /** The axis the wall runs along. */
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    private static final VoxelShape ALONG_X = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    private static final VoxelShape ALONG_Z = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);
    private static final Map<Entity, Long> LAST_PASS = new WeakHashMap<>();

    public FogGateBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    /** The wall stands across the way the player is looking. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction.Axis looking = context.getHorizontalDirection().getAxis();
        return this.defaultBlockState().setValue(AXIS, looking == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.X ? ALONG_X : ALONG_Z;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext && entityContext.getEntity() instanceof Player) {
            return Shapes.empty();
        }
        return this.getShape(state, level, pos, context);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean isPrecise) {
        if (level instanceof ServerLevel serverLevel && entity instanceof Player) {
            long now = serverLevel.getGameTime();
            Long last = LAST_PASS.get(entity);
            if (last == null || now - last > 30 || now < last) {
                serverLevel.playSound(null, pos, Dushi.sound("block.fog_gate.pass"), SoundSource.BLOCKS, 1.0F, 0.9F + serverLevel.getRandom().nextFloat() * 0.2F);
                serverLevel.sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY() + 1.0, entity.getZ(), 12, 0.3, 0.6, 0.3, 0.02);
            }
            LAST_PASS.put(entity, now);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(6) == 0) {
            level.addParticle(ParticleTypes.WHITE_SMOKE, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(),
                    0.0, 0.01, 0.0);
        }
    }

    /**
     * Lifts one fog gate: the one nearest to {@code centre} within {@code radius} blocks, with every block of fog joined to it.
     * Another gate in the same building, across another doorway, stays.
     */
    public static void lift(ServerLevel level, BlockPos centre, int radius) {
        BlockPos nearest = null;
        for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-radius, -8, -radius), centre.offset(radius, 12, radius))) {
            if (level.getBlockState(pos).is(Dushi.FOG_GATE.get()) && (nearest == null || pos.distSqr(centre) < nearest.distSqr(centre))) {
                nearest = pos.immutable();
            }
        }
        if (nearest == null) {
            return;
        }
        Deque<BlockPos> open = new ArrayDeque<>();
        open.add(nearest);
        int lifted = 0;
        while (!open.isEmpty() && lifted < 512) {
            BlockPos pos = open.poll();
            if (!level.getBlockState(pos).is(Dushi.FOG_GATE.get())) {
                continue;
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.02);
            lifted++;
            for (Direction direction : Direction.values()) {
                open.add(pos.relative(direction));
            }
        }
    }
}
