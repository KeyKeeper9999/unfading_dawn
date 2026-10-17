package com.keykeeper.dushi;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * What the clock under the ground remembers, for the whole world: which of its seven sockets hold a key, whether it
 * runs, how far the tower it raises has come, and how the day ended. Kept with the overworld (a level attachment), so
 * it is the same for everybody and survives a restart in the middle of the tower's rising.
 */
public final class ClockState {
    /** One socket for each of the six keys of the gates and the keykeeper's own. */
    public static final int SOCKETS = 7;
    public static final int ALL = (1 << SOCKETS) - 1;

    public static final int NO_ENDING = 0;
    /** The great bell was struck: the day is over and everything that was dead lies down. */
    public static final int BELL = 1;
    /** The Founder's seat was taken: the day goes on. */
    public static final int SEAT = 2;

    public static final MapCodec<ClockState> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("keys", 0).forGetter(state -> state.keys),
            Codec.BOOL.optionalFieldOf("started", false).forGetter(state -> state.started),
            Codec.INT.optionalFieldOf("rise", -1).forGetter(state -> state.rise),
            Codec.BOOL.optionalFieldOf("raised", false).forGetter(state -> state.raised),
            Codec.INT.optionalFieldOf("ending", NO_ENDING).forGetter(state -> state.ending),
            Codec.INT.optionalFieldOf("ending_tick", 0).forGetter(state -> state.endingTick),
            Codec.INT.optionalFieldOf("figure", 0).forGetter(state -> state.figure),
            Codec.STRING.listOf().optionalFieldOf("slain", List.of()).forGetter(state -> List.copyOf(state.slain)))
            .apply(instance, (keys, started, rise, raised, ending, endingTick, figure, slain) -> {
                ClockState state = new ClockState();
                state.keys = keys;
                state.started = started;
                state.rise = rise;
                state.raised = raised;
                state.ending = ending;
                state.endingTick = endingTick;
                state.figure = figure;
                state.slain.addAll(slain);
                return state;
            }));

    /** A bit for each socket that holds its key. */
    public int keys;
    /** All seven keys are in and the clock runs. */
    public boolean started;
    /** -1 until the tower begins to rise; then the ticks since it began. */
    public int rise = -1;
    /** The tower stands. */
    public boolean raised;
    public int ending = NO_ENDING;
    /** Ticks since the bell was struck, while the figure on the horizon is still rising. */
    public int endingTick;
    /** How many layers of the figure on the horizon stand. */
    public int figure;
    /** The lords that have fallen, by the id of their kind: what the folk of the borough talk about. */
    public final Set<String> slain = new HashSet<>();

    public boolean has(int socket) {
        return (this.keys & (1 << socket)) != 0;
    }

    public int count() {
        return Integer.bitCount(this.keys);
    }
}
