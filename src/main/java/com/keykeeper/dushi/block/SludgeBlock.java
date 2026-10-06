package com.keykeeper.dushi.block;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.SoulsMob;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A pool of the bog's own sludge, as the Wallower spits it: it lies a hand deep on the floor for some seconds and then
 * is gone. Whoever stands in it wades - walks at two fifths of their pace, rolls no further than that, and does not
 * jump out. The dead of the Mire do not mind it. {@code wet} says what it lies in: when it is gone, a pool that fell on
 * the shallows leaves the shallows there again.
 */
public class SludgeBlock extends Block {
    public static final BooleanProperty WET = BooleanProperty.create("wet");
    /** Ticks a pool lies. */
    public static final int LASTS = 170;
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0);
    private static final Vec3 DRAG = new Vec3(0.42, 0.9, 0.42);

    public SludgeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WET, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WET);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return context.isHoldingItem(Dushi.MOB_MARK_ITEM.get()) ? SHAPE : Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, LASTS + level.getRandom().nextInt(12));
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.setBlock(pos, state.getValue(WET) ? Dushi.SHALLOWS.get().defaultBlockState() : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.sendParticles(ParticleTypes.BUBBLE_POP, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5, 3, 0.3, 0.0, 0.3, 0.0);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean precise) {
        if (entity instanceof LivingEntity && !(entity instanceof SoulsMob) && entity.getY() < pos.getY() + 0.3) {
            entity.makeStuckInBlock(state, DRAG);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(12) == 0) {
            level.addParticle(ParticleTypes.BUBBLE_POP, pos.getX() + random.nextDouble(), pos.getY() + 0.22, pos.getZ() + random.nextDouble(), 0.0, 0.02, 0.0);
        }
    }

    /**
     * Sludge lands at a place: every cell of floor within {@code radius} of it that is bare or under the shallows gets a
     * pool. Returns how many did.
     */
    public static int spill(ServerLevel level, Vec3 at, double radius) {
        int pools = 0;
        BlockPos centre = BlockPos.containing(at.x, at.y + 0.3, at.z);
        int reach = (int) Math.ceil(radius);
        for (BlockPos column : BlockPos.betweenClosed(centre.offset(-reach, 0, -reach), centre.offset(reach, 0, reach))) {
            double dx = column.getX() + 0.5 - at.x;
            double dz = column.getZ() + 0.5 - at.z;
            if (dx * dx + dz * dz > radius * radius) {
                continue;
            }
            for (int dy = 1; dy >= -2; dy--) {                   // the floor may be a step up or down from where it burst
                BlockPos pos = column.offset(0, dy, 0);
                BlockState there = level.getBlockState(pos);
                boolean wet = there.is(Dushi.SHALLOWS.get());
                if ((wet || there.isAir()) && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
                    level.setBlock(pos, Dushi.SLUDGE.get().defaultBlockState().setValue(WET, wet), Block.UPDATE_ALL);
                    pools++;
                    break;
                }
                if (there.is(Dushi.SLUDGE.get())) {
                    level.scheduleTick(pos, Dushi.SLUDGE.get(), LASTS);   // (the game keeps the earlier tick: the pool is simply not laid twice)
                    break;
                }
            }
        }
        return pools;
    }
}
