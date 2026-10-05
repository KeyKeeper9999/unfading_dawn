package com.keykeeper.dushi;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * Everything the mod remembers about one player: the souls carried, the six trained stats and the stamina.
 * Saved with the player, kept through death and sent to the player's own client.
 */
public final class PlayerSoul {
    public static final int VITALITY = 0;
    public static final int ENDURANCE = 1;
    public static final int STRENGTH = 2;
    public static final int DEXTERITY = 3;
    public static final int INTELLECT = 4;
    public static final int FAITH = 5;
    public static final int STAT_COUNT = 6;
    public static final String[] STAT_KEYS = {"vitality", "endurance", "strength", "dexterity", "intellect", "faith"};
    /** No stat can be trained beyond this. */
    public static final int STAT_CAP = 40;

    public static final float BASE_STAMINA = 100.0F;
    public static final float STAMINA_PER_ENDURANCE = 5.0F;
    /** Stamina that comes back every tick once the pause after the last effort is over. */
    public static final float STAMINA_REGEN = 2.2F;
    /** Ticks without recovery after stamina has been spent. */
    public static final int REGEN_PAUSE = 22;
    /** Hearts (two health points) per point of vitality. */
    private static final double HEALTH_PER_VITALITY = 3.0;
    /** Extra damage per point of strength or dexterity, for a weapon that scales fully with it. */
    public static final float WEAPON_BONUS = 0.05F;
    private static final Identifier VITALITY_MODIFIER = Identifier.fromNamespaceAndPath(Dushi.MODID, "vitality");

    public static final MapCodec<PlayerSoul> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("souls").forGetter(soul -> soul.souls),
            Codec.INT.fieldOf("vitality").forGetter(soul -> soul.stats[VITALITY]),
            Codec.INT.fieldOf("endurance").forGetter(soul -> soul.stats[ENDURANCE]),
            Codec.INT.fieldOf("strength").forGetter(soul -> soul.stats[STRENGTH]),
            Codec.INT.fieldOf("dexterity").forGetter(soul -> soul.stats[DEXTERITY]),
            Codec.INT.fieldOf("intellect").forGetter(soul -> soul.stats[INTELLECT]),
            Codec.INT.fieldOf("faith").forGetter(soul -> soul.stats[FAITH]),
            Codec.INT.fieldOf("stain").forGetter(soul -> soul.stainGeneration),
            Codec.INT.optionalFieldOf("lost", 0).forGetter(soul -> soul.lostSouls),
            Codec.BOOL.optionalFieldOf("flask", false).forGetter(soul -> soul.flaskGiven),
            Codec.BOOL.optionalFieldOf("keykeeper", false).forGetter(soul -> soul.metKeykeeper),
            Codec.INT.optionalFieldOf("hour", -1).forGetter(soul -> soul.hearthHour))
            .apply(instance, (souls, vit, end, str, dex, intel, faith, stain, lost, flask, keykeeper, hour) -> {
                PlayerSoul soul = new PlayerSoul();
                soul.souls = souls;
                soul.stats[VITALITY] = vit;
                soul.stats[ENDURANCE] = end;
                soul.stats[STRENGTH] = str;
                soul.stats[DEXTERITY] = dex;
                soul.stats[INTELLECT] = intel;
                soul.stats[FAITH] = faith;
                soul.stainGeneration = stain;
                soul.lostSouls = lost;
                soul.flaskGiven = flask;
                soul.metKeykeeper = keykeeper;
                soul.hearthHour = hour;
                soul.stamina = soul.maxStamina();
                return soul;
            }));

    public static final StreamCodec<ByteBuf, PlayerSoul> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PlayerSoul decode(ByteBuf buffer) {
            PlayerSoul soul = new PlayerSoul();
            soul.souls = ByteBufCodecs.VAR_INT.decode(buffer);
            for (int i = 0; i < STAT_COUNT; i++) {
                soul.stats[i] = ByteBufCodecs.VAR_INT.decode(buffer);
            }
            soul.stamina = ByteBufCodecs.FLOAT.decode(buffer);
            soul.regenPause = ByteBufCodecs.VAR_INT.decode(buffer);
            soul.lostSouls = ByteBufCodecs.VAR_INT.decode(buffer);
            // The roll travels too: the copy that arrives replaces the client's own, and must not cut its roll short.
            soul.rollTicks = ByteBufCodecs.VAR_INT.decode(buffer);
            soul.rollCooldown = ByteBufCodecs.VAR_INT.decode(buffer);
            return soul;
        }

        @Override
        public void encode(ByteBuf buffer, PlayerSoul soul) {
            ByteBufCodecs.VAR_INT.encode(buffer, soul.souls);
            for (int i = 0; i < STAT_COUNT; i++) {
                ByteBufCodecs.VAR_INT.encode(buffer, soul.stats[i]);
            }
            ByteBufCodecs.FLOAT.encode(buffer, soul.stamina);
            ByteBufCodecs.VAR_INT.encode(buffer, soul.regenPause);
            ByteBufCodecs.VAR_INT.encode(buffer, soul.lostSouls);
            ByteBufCodecs.VAR_INT.encode(buffer, soul.rollTicks);
            ByteBufCodecs.VAR_INT.encode(buffer, soul.rollCooldown);
        }
    };

    public int souls;
    public final int[] stats = new int[STAT_COUNT];
    public float stamina = BASE_STAMINA;
    /** Ticks left before stamina starts to come back. */
    public int regenPause;
    /** Counts the player's soul stains: only the stain with this number is still theirs. */
    public int stainGeneration;
    /** Souls lying in the player's current stain (0 if there is none); only for the hint on the screen. */
    public int lostSouls;

    /** Whether a hearth has already given this player their flask. */
    public boolean flaskGiven;
    /** Whether the keykeeper has already given this player his gift. */
    public boolean metKeykeeper;

    /** Whether the player stands by a hearth: the dead do not take notice of them there. Not saved, server only. */
    public boolean atHearth;

    /** The place whose name was last shown to the player (see PlaceNameBlock), -1 for none yet. Not saved, server only. */
    public int place = -1;
    /** The hour of the place where the player last rested, in a world whose time stands still (see {@link SoulMechanics#hour}); -1 if none. */
    public int hearthHour = -1;

    /** Where the player last stood on something, for a stain left by a fall into nothing. Not saved, server only. */
    public double groundX;
    public double groundY;
    public double groundZ;
    public boolean hasGround;

    /** Ticks of the roll still to go; the first part of it cannot be hurt. Not saved. */
    public int rollTicks;
    /** Ticks until the next roll is allowed. Not saved. */
    public int rollCooldown;
    /** Whether the swing in progress was started without stamina. Not saved. */
    public boolean exhaustedSwing;

    public static PlayerSoul of(Player player) {
        return player.getData(Dushi.PLAYER_SOUL);
    }

    public int level() {
        int level = 1;
        for (int stat : this.stats) {
            level += stat;
        }
        return level;
    }

    /** Souls needed to train one more point at the current level. */
    public int levelCost() {
        int level = this.level();
        return (int) Math.floor(100.0 * Math.pow(1.1, level - 1)) + 25 * (level - 1);
    }

    public float maxStamina() {
        return BASE_STAMINA + STAMINA_PER_ENDURANCE * this.stats[ENDURANCE];
    }

    /** Takes stamina away and pauses its recovery. Returns false (but still empties the bar) if there was not enough. */
    public boolean spend(float amount) {
        boolean enough = this.stamina >= amount;
        this.stamina = Math.max(0.0F, this.stamina - amount);
        this.regenPause = REGEN_PAUSE;
        return enough;
    }

    /**
     * One tick of recovery; runs the same way on the server and on the player's client.
     *
     * @param regen multiplier of the recovery, from what the player wears (see {@link Gear#staminaRegen})
     */
    public void tickStamina(boolean sprinting, boolean blocking, float regen) {
        if (this.rollTicks > 0) {
            this.rollTicks--;
        }
        if (this.rollCooldown > 0) {
            this.rollCooldown--;
        }
        if (sprinting) {
            this.stamina = Math.max(0.0F, this.stamina - 0.18F);
            this.regenPause = Math.max(this.regenPause, 8);
            return;
        }
        if (this.regenPause > 0) {
            this.regenPause--;
            return;
        }
        float max = this.maxStamina();
        if (this.stamina < max) {
            this.stamina = Math.min(max, this.stamina + (blocking ? STAMINA_REGEN * 0.25F : STAMINA_REGEN) * regen);
        }
    }

    /** Extra damage for a weapon that scales with strength and dexterity (each 0..1). */
    public float weaponBonus(float strengthScaling, float dexterityScaling) {
        return 1.0F + WEAPON_BONUS * (strengthScaling * this.stats[STRENGTH] + dexterityScaling * this.stats[DEXTERITY]);
    }

    public float spellBonus(SpellSchool school) {
        float stat = switch (school) {
            case SORCERY -> this.stats[INTELLECT];
            case MIRACLE -> this.stats[FAITH];
            case PYROMANCY -> (this.stats[INTELLECT] + this.stats[FAITH]) * 0.5F;
        };
        return 1.0F + 0.05F * stat;
    }

    /** Makes the player's maximum health match their vitality. */
    public void applyAttributes(Player player) {
        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.removeModifier(VITALITY_MODIFIER);
            if (this.stats[VITALITY] > 0) {
                health.addPermanentModifier(new AttributeModifier(VITALITY_MODIFIER, HEALTH_PER_VITALITY * this.stats[VITALITY],
                        AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }
}
