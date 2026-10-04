package com.keykeeper.dushi.block;

import com.keykeeper.dushi.Dushi;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A threshold: nothing to see and nothing to bump into, but a player who steps through it is shown the name of the place
 * they have come to (see SoulMechanics). Set across doorways by a map's builder. It can only be seen and touched with
 * a mob mark in hand, which is how a builder in creative finds and removes it.
 */
public class PlaceNameBlock extends Block {
    public enum Place implements StringRepresentable {
        STOCKADE("stockade"),
        WARD("ward"),
        BRIDGE("bridge"),
        HALL("hall"),
        CITY("city"),
        GARDEN("garden"),
        BELFRY("belfry"),
        CATHEDRAL("cathedral"),
        CRYPT("crypt"),
        EDGE("edge");

        public final String id;

        Place(String id) {
            this.id = id;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }
    }

    public static final EnumProperty<Place> PLACE = EnumProperty.create("place", Place.class);

    public PlaceNameBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PLACE, Place.STOCKADE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PLACE);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return context.isHoldingItem(Dushi.MOB_MARK_ITEM.get()) ? Shapes.block() : Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }
}
