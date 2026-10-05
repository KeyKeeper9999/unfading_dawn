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
    /** The sun half under the edge of the land: the hour of the stockade. */
    public static final int DUSK = 12350;
    /** Midnight under a full moon: the hour of the town below. */
    public static final int NIGHT = 18000;

    public enum Place implements StringRepresentable {
        STOCKADE("stockade", DUSK, true),
        WARD("ward", DUSK, true),
        BRIDGE("bridge", DUSK, true),
        HALL("hall", DUSK, true),
        CITY("city", DUSK, true),
        GARDEN("garden", DUSK, true),
        BELFRY("belfry", DUSK, true),
        CATHEDRAL("cathedral", DUSK, true),
        CRYPT("crypt", DUSK, true),
        EDGE("edge", DUSK, true),
        POSAD("posad", NIGHT, true),
        MARKET("market", NIGHT, true),
        SHAMBLES("shambles", NIGHT, true),
        CHURCHYARD("churchyard", NIGHT, true),
        ROAD("road", NIGHT, true),
        /** The quarry under the plateau, west of the borough: the same night, and then no sky at all. */
        QUARRY("quarry", NIGHT, true),
        DRIFTS("drifts", NIGHT, true),
        CARVERS("carvers", NIGHT, true),
        WHEEL("wheel", NIGHT, true),
        WORKINGS("workings", NIGHT, true),
        BREACH("breach", NIGHT, true),
        /** Says nothing: only turns the hour, in a bend of a passage from which no sky is seen. */
        TO_NIGHT("to_night", NIGHT, false),
        TO_DUSK("to_dusk", DUSK, false);

        public final String id;
        /** The hour of the day at this place; the world's clock is set to it when a player comes. */
        public final int hour;
        /** Whether the name of the place is shown. */
        public final boolean named;

        Place(String id, int hour, boolean named) {
            this.id = id;
            this.hour = hour;
            this.named = named;
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
