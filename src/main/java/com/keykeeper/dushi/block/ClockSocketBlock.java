package com.keykeeper.dushi.block;

import java.util.function.Supplier;

import com.keykeeper.dushi.Clock;
import com.keykeeper.dushi.Dushi;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * One of the seven sockets of the clock under the Sky Beneath, each with the sign of its place cut into it. Whoever
 * touches an empty one with the matching key among their things puts the key in, for good (the key stays in the clock).
 * Seven keys in, and the clock runs: see {@link Clock}. Nothing breaks it.
 */
public class ClockSocketBlock extends Block {
    public enum Socket implements StringRepresentable {
        /** The quarry's: the Quarrymen's Gate. */
        QUARRY("quarry", 0, () -> Dushi.QUARRY_KEY.get()),
        MIRE("mire", 1, () -> Dushi.MIRE_KEY.get()),
        FIRWOOD("firwood", 2, () -> Dushi.FIRWOOD_KEY.get()),
        SHORE("shore", 3, () -> Dushi.SHORE_KEY.get()),
        STUBBLE("stubble", 4, () -> Dushi.STUBBLE_KEY.get()),
        DEPTHS("depths", 5, () -> Dushi.DEPTHS_KEY.get()),
        /** The seventh: the keykeeper's own, the one he keeps for the borough. */
        KEEPER("keeper", 6, () -> Dushi.CLOCK_KEY.get());

        public final String id;
        /** The bit of this socket in the clock's reckoning of its keys. */
        public final int bit;
        private final Supplier<Item> key;

        Socket(String id, int bit, Supplier<Item> key) {
            this.id = id;
            this.bit = bit;
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

    public static final EnumProperty<Socket> SOCKET = EnumProperty.create("socket", Socket.class);
    public static final BooleanProperty FILLED = BooleanProperty.create("filled");

    public ClockSocketBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SOCKET, Socket.QUARRY).setValue(FILLED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SOCKET, FILLED);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            Clock.touchSocket(serverLevel, serverPlayer, pos, state);
        }
        return InteractionResult.SUCCESS;
    }
}
