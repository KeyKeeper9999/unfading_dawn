package com.keykeeper.dushi;

/** The three kinds of magic; each needs its own catalyst and grows with its own stats. */
public enum SpellSchool {
    SORCERY, MIRACLE, PYROMANCY;

    public String key() {
        return this.name().toLowerCase(java.util.Locale.ROOT);
    }
}
