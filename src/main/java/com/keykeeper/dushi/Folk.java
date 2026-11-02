package com.keykeeper.dushi;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The folk of the map: those who still live in the stopped day and talk. Each says what he has to say once to every
 * newcomer, a topic at a time (a click, a topic), the topics that the world allows by now in the order written here; when
 * they are all heard, a line of idle talk. What the world allows is read off the clock's memory ({@link ClockState}):
 * which lords have fallen, whether the clock runs, whether the tower stands, how the day ended.
 *
 * <p>The texts are {@code dialogue.unfading_dawn.<id>.<topic>.<n>} and {@code dialogue.unfading_dawn.<id>.idle.<n>}, written by
 * tools/gen_lang.py; tools/check_assets.py checks that every line counted here has its text. The story behind them is in
 * docs/story.md: who says "dawn", who says "sunset", and who neither.
 */
public enum Folk {
    /** The candle-woman of the churchyard lodge: she sings the dead to rest, and they get up again. */
    CANDLEWIFE("candlewife", Pose.STAND, 5,
            topic("first", 3),
            topic("gravedigger", 2, slain("gravedigger")),
            topic("chaplain", 2, slain("chaplain")),
            topic("mother", 2, slain("weeping_mother")),
            topic("elder", 2, slain("elder")),
            topic("coming", 2, slain("coming")),
            topic("started", 1, s -> s.started),
            topic("raised", 2, s -> s.raised),
            topic("seat", 2, s -> s.ending == ClockState.SEAT)),
    /** In the Lower Ward of the Stockade: a cell with its door open, and a man in it whose term has not begun to run. */
    DEBTOR("debtor", Pose.SIT, 5,
            topic("first", 3),
            topic("overseer", 2, slain("overseer")),
            topic("chaplain", 2, slain("chaplain")),
            topic("mother", 2, slain("weeping_mother")),
            topic("wharf", 1, slain("helmsman").or(slain("capsized"))),
            topic("coming", 2, slain("coming")),
            topic("started", 2, s -> s.started),
            topic("seat", 2, s -> s.ending == ClockState.SEAT)),
    /** In the carvers' shop of the White Pit: he carves the Founder's face from memory, and no longer remembers it. */
    CARVER("carver", Pose.WORK, 5,
            topic("first", 3),
            topic("taskmaster", 2, slain("taskmaster")),
            topic("stone_eater", 2, slain("stone_eater")),
            topic("coming", 3, slain("coming")),
            topic("started", 1, s -> s.started),
            topic("raised", 2, s -> s.raised),
            topic("seat", 1, s -> s.ending == ClockState.SEAT)),
    /** In his hut in the Mire: there is nobody to take across, and the far bank is where the near one is. */
    FERRYMAN("ferryman", Pose.LEAN, 5,
            topic("first", 3),
            topic("wallower", 2, slain("wallower")),
            topic("mother", 3, slain("weeping_mother")),
            topic("elder", 2, slain("elder")),
            topic("helmsman", 2, slain("helmsman")),
            topic("coming", 1, slain("coming")),
            topic("started", 1, s -> s.started),
            topic("seat", 1, s -> s.ending == ClockState.SEAT)),
    /**
     * On the gallows island of the Mire: he cannot finish the hanging while the Weeping Mother lives. When she has
     * fallen he says so, once, and goes (see {@link #leaves}).
     */
    HANGMAN("hangman", Pose.STAND, 4,
            topic("first", 3, slain("weeping_mother").negate()),
            topic("mother", 3, slain("weeping_mother"))),
    /** In the candle store of the skete: one of the Unsleeping, the one who still keeps a count of the days. It does not add up. */
    CELLARER("cellarer", Pose.SIT, 4,
            topic("first", 3),
            topic("shatun", 1, slain("shatun")),
            topic("mother", 2, slain("weeping_mother")),
            topic("elder", 2, slain("elder")),
            topic("wharf", 2, slain("helmsman").or(slain("capsized"))),
            topic("coming", 1, slain("coming")),
            topic("started", 1, s -> s.started),
            topic("seat", 1, s -> s.ending == ClockState.SEAT)),
    /** On the stair of the tower the clock raises, under the great bell: deaf, waiting to be told to strike. */
    BELLMAN("bellman", Pose.SIT, 5,
            topic("first", 3),
            topic("seat", 2, s -> s.ending == ClockState.SEAT));

    public enum Pose {
        /** On his feet, hands before him. */
        STAND,
        /** On the floor or a step, knees up. */
        SIT,
        /** At his stone, a chisel and a mallet. */
        WORK,
        /** Leaning on a pole. */
        LEAN
    }

    /** What one of the folk says at once, when the world allows it. */
    public record Topic(String id, int lines, Predicate<ClockState> when) {
    }

    public final String id;
    public final Pose pose;
    /** How many lines of idle talk there are. */
    public final int idle;
    public final List<Topic> topics;

    Folk(String id, Pose pose, int idle, Topic... topics) {
        this.id = id;
        this.pose = pose;
        this.idle = idle;
        this.topics = List.of(topics);
    }

    /** The first topic the world allows that this player has not heard yet, or null. */
    public Topic next(ClockState clock, Set<String> heard) {
        for (Topic topic : this.topics) {
            if (topic.when.test(clock) && !heard.contains(this.key(topic))) {
                return topic;
            }
        }
        return null;
    }

    /** How a heard topic is remembered by the player. */
    public String key(Topic topic) {
        return this.id + "." + topic.id;
    }

    public String line(Topic topic, int n) {
        return "dialogue." + Dushi.MODID + "." + this.id + "." + topic.id + "." + n;
    }

    public String idleLine(int n) {
        return "dialogue." + Dushi.MODID + "." + this.id + ".idle." + n;
    }

    /** The hangman goes once he has said the hanging is over: nothing keeps him on the island after that. */
    public boolean leaves(Topic topic) {
        return this == HANGMAN && topic.id.equals("mother");
    }

    private static Topic topic(String id, int lines) {
        return new Topic(id, lines, s -> true);
    }

    private static Topic topic(String id, int lines, Predicate<ClockState> when) {
        return new Topic(id, lines, when);
    }

    private static Predicate<ClockState> slain(String lord) {
        return s -> s.slain.contains(lord);
    }
}
