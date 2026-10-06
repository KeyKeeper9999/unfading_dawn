package com.keykeeper.dushi;

import net.minecraft.world.entity.player.Player;

/**
 * The charms: small things worn on a cord, one at a time, each with one small lasting gift. A charm is put on by using
 * it (see {@link com.keykeeper.dushi.item.CharmItem}); the one worn is kept in {@link PlayerSoul#charm}, with the
 * player through death like everything else, and is taken off at a hearth or by putting on another.
 */
public enum Charm {
    /** The borough: a nail of a coffin nobody could be laid in. The dead leave more souls. */
    COFFIN_NAIL("coffin_nail", 10),
    /** The borough: a knot of ash from a hearth. A gulp of the flask heals more. */
    ASH_KNOT("ash_knot", 20),
    /** The stockade: a link of a prisoner's fetters. More can be carried. */
    FETTER_LINK("fetter_link", 15),
    /** The cathedral: the stub of a candle that burned before the last light. Spells are stronger. */
    CANDLE_STUB("candle_stub", 10),
    /** The quarry: a splitting wedge on a cord. A blow caught on a shield costs less stamina. */
    WEDGE_CORD("wedge_cord", 20),
    /** The Mire: a tooth of a bone pike. A blow in the back is harder. */
    PIKE_TOOTH("pike_tooth", 25),
    /** The Mire: the herb that grows where the Mother wept. Stamina comes back faster. */
    WEEPING_HERB("weeping_herb", 12),
    /** The Firwood: wax of a candle of the vigil. A roll costs less stamina. */
    VIGIL_WAX("vigil_wax", 20),
    /** The Firwood: a claw of the bear that could not sleep. Three hearts more. */
    SHATUN_CLAW("shatun_claw", 3);

    public final String id;
    /** What it gives, as its tooltip says it: a percentage for most, a number for the link and the claw. */
    public final int amount;

    Charm(String id, int amount) {
        this.id = id;
        this.amount = amount;
    }

    public static Charm byId(String id) {
        for (Charm charm : values()) {
            if (charm.id.equals(id)) {
                return charm;
            }
        }
        return null;
    }

    /** The charm the player wears, or null. */
    public static Charm worn(Player player) {
        return byId(PlayerSoul.of(player).charm);
    }

    public boolean on(Player player) {
        return this.id.equals(PlayerSoul.of(player).charm);
    }

    /** 1 + the charm's share if the player wears it, else 1. */
    public float more(Player player) {
        return this.on(player) ? 1.0F + this.amount / 100.0F : 1.0F;
    }

    /** 1 - the charm's share if the player wears it, else 1. */
    public float less(Player player) {
        return this.on(player) ? 1.0F - this.amount / 100.0F : 1.0F;
    }
}
