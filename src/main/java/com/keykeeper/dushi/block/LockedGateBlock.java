package com.keykeeper.dushi.block;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.Forge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * An iron grate across a way that a map keeps shut until its key is found somewhere else: whoever touches it with the key
 * among their things opens the whole gate, for good and for everybody (the key is kept). Without the key it only rattles.
 * (A state property must have two values at least: the second lock is the one that has no key.)
 * Which key is the {@code lock} of the state, so that one block serves every such gate of a map: a new gate is a new
 * entry in {@link Lock} and a new key item.
 */
public class LockedGateBlock extends Block {
    public enum Lock implements StringRepresentable {
        /** The Quarrymen's Gate at the west end of Low Street; its key lies in the treasury of the High Stockade. */
        QUARRY("quarry", () -> Dushi.QUARRY_KEY.get()),
        /** The Mire Gate at the east end of the same street; its key lies in the crack behind the Stone-Eater. */
        MIRE("mire", () -> Dushi.MIRE_KEY.get()),
        /** The Wood Wicket in the cliff of the yard before the town's north gate; its key lies in the grove behind the Weeping Mother. */
        FIRWOOD("firwood", () -> Dushi.FIRWOOD_KEY.get()),
        /** The Sea Gate, the old south gate of the yard of the first hearth; its key lies in the candle store behind the Elder. */
        SHORE("shore", () -> Dushi.SHORE_KEY.get()),
        /** The Reapers' Gate, in the west cliff of the yard before the town's north gate; its key hangs in the lighthouse, by the lamp. */
        STUBBLE("stubble", () -> Dushi.STUBBLE_KEY.get()),
        /** The Lower Grate, in the south wall of the quarry's sump: the way down to the Sky Beneath. Its key lies in the granary behind the Hollow Miller. */
        DEPTHS("depths", () -> Dushi.DEPTHS_KEY.get()),
        /** A gate whose key is not in the map yet: a way that is being kept for a place still to come. Nothing opens it. */
        NONE("none", () -> null);

        public final String id;
        private final Supplier<Item> key;

        Lock(String id, Supplier<Item> key) {
            this.id = id;
            this.key = key;
        }

        public Item key() {
            return this.key.get();
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }
    }

    /** The axis the grate runs along. */
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final EnumProperty<Lock> LOCK = EnumProperty.create("lock", Lock.class);
    private static final VoxelShape ALONG_X = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    private static final VoxelShape ALONG_Z = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);
    /** No gate is bigger than this many blocks. */
    private static final int LIMIT = 256;

    public LockedGateBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X).setValue(LOCK, Lock.QUARRY));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, LOCK);
    }

    /** A plan is set down turned: the grate turns with its gateway. */
    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) {
            return state.setValue(AXIS, state.getValue(AXIS) == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X);
        }
        return state;
    }

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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level instanceof ServerLevel serverLevel) {
            Lock lock = state.getValue(LOCK);
            if (player.isCreative() || lock.key() != null && Forge.count(player, lock.key()) > 0) {
                open(serverLevel, pos);
                serverLevel.playSound(null, pos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.4F, 0.6F);
                serverLevel.playSound(null, pos, SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS, 1.2F, 0.7F);
                player.sendOverlayMessage(Component.translatable("message.unfading_dawn.gate_opened." + lock.id));
            } else {
                serverLevel.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 0.7F);
                player.sendOverlayMessage(Component.translatable("message.unfading_dawn.gate_locked." + lock.id));
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** Takes away the gate this block is part of: every block of the same lock joined to it. Returns how many there were. */
    public static int open(ServerLevel level, BlockPos start) {
        BlockState first = level.getBlockState(start);
        if (!(first.getBlock() instanceof LockedGateBlock)) {
            return 0;
        }
        Lock lock = first.getValue(LOCK);
        Deque<BlockPos> open = new ArrayDeque<>();
        open.add(start.immutable());
        int count = 0;
        while (!open.isEmpty() && count < LIMIT) {
            BlockPos pos = open.poll();
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof LockedGateBlock) || state.getValue(LOCK) != lock) {
                continue;
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2, 0.3, 0.3, 0.3, 0.05);
            count++;
            for (Direction direction : Direction.values()) {
                open.add(pos.relative(direction));
            }
        }
        return count;
    }
}
