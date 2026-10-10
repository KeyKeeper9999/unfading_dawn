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
    /**
     * The hour of the Mire: a noon that does not pass. Its green and its gloom are the biome's
     * (worldgen/biome/mire.json). The hour is one of full day because the game darkens a biome's fog and sky by the
     * hour and paints them orange towards a low sun: by day it leaves them as the biome names them - and at noon the
     * sun is overhead, where the willows are, and not in a player's eyes.
     */
    public static final int MURK = 6000;

    /**
     * The hour of the Black Firwood: the sky has only just begun to pale and there is no sun yet (it comes up at
     * 23000 or so; the hour with the sun on the edge of the world is being kept for another place).
     */
    public static final int FOREDAWN = 22600;

    /**
     * The hour of the Morning Wharf: the sun just clear of the sea, and it does not climb. This is the dawn the country
     * is named for - and the only place in it where it is one. Its mist is the biome's (worldgen/biome/shore.json).
     */
    public static final int SUNRISE = 23600;

    /**
     * The hour of the Stubble: the afternoon of a harvest day, the sun high and a little past its height, white, with
     * no cloud near it and no wind. Its haze and the colour of its corn are the biome's (worldgen/biome/stubble.json).
     */
    public static final int AFTERNOON = 8000;

    /**
     * The hour of the Sky Beneath. No sun is seen there, and its dusk is its biome's (worldgen/biome/under_sky.json):
     * the hour is noon only because the game leaves a biome's colours alone at noon and blackens them at night.
     */
    public static final int BLUE_HOUR = 6000;

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
        /** The bog east of the borough, and what it took of the town. */
        MIRE("mire", MURK, true),
        LOG_ROADS("log_roads", MURK, true),
        WEIR("weir", MURK, true),
        WITHY("withy", MURK, true),
        ISLE("isle", MURK, true),
        /** The Black Firwood, in a hollow of the plateau north-east of the town: the hour before a dawn that does not come. */
        FIRWOOD("firwood", FOREDAWN, true),
        DEN("den", FOREDAWN, true),
        SKETE("skete", FOREDAWN, true),
        VIGIL("vigil", FOREDAWN, true),
        WHITE_TENT("white_tent", FOREDAWN, true),
        /** Says nothing: only turns the hour, in a bend of a passage from which no sky is seen. */
        TO_NIGHT("to_night", NIGHT, false),
        TO_DUSK("to_dusk", DUSK, false),
        TO_MURK("to_murk", MURK, false),
        TO_FOREDAWN("to_foredawn", FOREDAWN, false),
        WHARF("wharf", SUNRISE, true),
        SHIP("ship", SUNRISE, true),
        MOLE("mole", SUNRISE, true),
        SPIT("spit", SUNRISE, true),
        BOULDERS("boulders", SUNRISE, true),
        SHOAL("shoal", SUNRISE, true),
        LIGHTHOUSE("lighthouse", SUNRISE, true),
        TO_SUNRISE("to_sunrise", SUNRISE, false),
        /** The cut and the uncut fields west of the borough's wall, where the harvest is never in. */
        STUBBLE("stubble", AFTERNOON, true),
        WATCH_FIELD("watch_field", AFTERNOON, true),
        THRESHING("threshing", AFTERNOON, true),
        GREAT_MILL("great_mill", AFTERNOON, true),
        TO_AFTERNOON("to_afternoon", AFTERNOON, false),
        /** Under the black hill south of the quarry: where the day that was not let begin has been kept. */
        UNDER_SKY("under_sky", BLUE_HOUR, true),
        STILL_WATER("still_water", BLUE_HOUR, true),
        CLOCKWORKS("clockworks", BLUE_HOUR, true),
        CAUSEWAYS("causeways", BLUE_HOUR, true),
        TEAR("tear", BLUE_HOUR, true),
        TO_BLUE_HOUR("to_blue_hour", BLUE_HOUR, false);

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
