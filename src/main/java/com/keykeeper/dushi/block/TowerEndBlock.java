package com.keykeeper.dushi.block;

import com.keykeeper.dushi.Clock;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The two ways the day can end, both at the top of the bell tower: the rope of the great bell, and the seat of the
 * Founder. Either is used once, by anyone, for the whole world: see {@link Clock#pullRope} and {@link Clock#takeSeat}.
 * No item; the tower's plan sets them.
 */
public class TowerEndBlock extends Block {
    public enum Kind implements StringRepresentable {
        ROPE("rope"),
        SEAT("seat");

        public final String id;

        Kind(String id) {
            this.id = id;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }
    }

    public static final EnumProperty<Kind> KIND = EnumProperty.create("kind", Kind.class);
    private static final VoxelShape ROPE = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
    private static final VoxelShape SEAT = Block.box(0.0, 0.0, 0.0, 16.0, 9.0, 16.0);

    public TowerEndBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(KIND, Kind.ROPE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(KIND);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(KIND) == Kind.ROPE ? ROPE : SEAT;
    }

    /** A rope is walked through; a seat is a seat. */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(KIND) == Kind.ROPE ? Shapes.empty() : SEAT;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            if (state.getValue(KIND) == Kind.ROPE) {
                Clock.pullRope(serverLevel, serverPlayer, pos);
            } else {
                Clock.takeSeat(serverLevel, serverPlayer, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
