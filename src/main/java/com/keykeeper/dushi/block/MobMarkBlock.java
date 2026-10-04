package com.keykeeper.dushi.block;

import java.util.function.Supplier;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.entity.WitheredEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A ring of ash on the floor where one of the dead keeps rising: it puts its mob there when somebody comes near, and puts
 * it there again whenever a player rests at a hearth. A boss rises only once. Cannot be broken in survival.
 * In creative, a click with an empty hand changes which mob it is for.
 */
public class MobMarkBlock extends Block implements EntityBlock {
    public enum Kind implements StringRepresentable {
        PRISONER("prisoner", () -> Dushi.WITHERED.get(WitheredEntity.Kind.PRISONER).get(), false),
        GUARD("guard", () -> Dushi.WITHERED.get(WitheredEntity.Kind.GUARD).get(), false),
        ARCHER("archer", () -> Dushi.WITHERED.get(WitheredEntity.Kind.ARCHER).get(), false),
        KNIGHT("knight", () -> Dushi.RUST_KNIGHT.get(), false),
        ACOLYTE("acolyte", () -> Dushi.WITHERED.get(WitheredEntity.Kind.ACOLYTE).get(), false),
        GILDED("gilded", () -> Dushi.GILDED_KNIGHT.get(), false),
        KEYKEEPER("keykeeper", () -> Dushi.KEYKEEPER.get(), false),
        OVERSEER("overseer", () -> Dushi.OVERSEER.get(), true),
        CHAPLAIN("chaplain", () -> Dushi.CHAPLAIN.get(), true),
        TOWNSMAN("townsman", () -> Dushi.WITHERED.get(WitheredEntity.Kind.TOWNSMAN).get(), false),
        BUTCHER("butcher", () -> Dushi.WITHERED.get(WitheredEntity.Kind.BUTCHER).get(), false),
        WATCHMAN("watchman", () -> Dushi.WITHERED.get(WitheredEntity.Kind.WATCHMAN).get(), false),
        HOUND("hound", () -> Dushi.CARRION_HOUND.get(), false),
        GRAVEDIGGER("gravedigger", () -> Dushi.GRAVEDIGGER.get(), true);

        public final String id;
        private final Supplier<EntityType<? extends Mob>> type;
        /** A boss does not come back once slain. */
        public final boolean boss;

        Kind(String id, Supplier<EntityType<? extends Mob>> type, boolean boss) {
            this.id = id;
            this.type = type;
            this.boss = boss;
        }

        public EntityType<? extends Mob> type() {
            return this.type.get();
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }
    }

    public static final EnumProperty<Kind> MOB = EnumProperty.create("mob", Kind.class);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** How hard the place is: see SoulsMob.setTier. */
    public static final IntegerProperty TIER = IntegerProperty.create("tier", 0, 3);
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);

    public MobMarkBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MOB, Kind.PRISONER).setValue(FACING, Direction.SOUTH).setValue(TIER, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MOB, FACING, TIER);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.isCreative()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            Kind next = Kind.values()[(state.getValue(MOB).ordinal() + 1) % Kind.values().length];
            level.setBlock(pos, state.setValue(MOB, next), Block.UPDATE_ALL);
            if (level.getBlockEntity(pos) instanceof MobMarkBlockEntity mark) {
                mark.forget();
            }
            player.sendOverlayMessage(Component.translatable("message.vsratie_dushi.mob_mark", next.type().getDescription()));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MobMarkBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != Dushi.MOB_MARK_ENTITY.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> ((MobMarkBlockEntity) entity).serverTick(tickLevel, pos, tickState);
    }
}
