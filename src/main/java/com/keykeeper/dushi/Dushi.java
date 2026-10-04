package com.keykeeper.dushi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.keykeeper.dushi.block.FogGateBlock;
import com.keykeeper.dushi.block.ForgeBlock;
import com.keykeeper.dushi.block.HearthBlock;
import com.keykeeper.dushi.block.MobMarkBlock;
import com.keykeeper.dushi.block.MobMarkBlockEntity;
import com.keykeeper.dushi.block.PlaceNameBlock;
import com.keykeeper.dushi.block.ShardBlock;
import com.keykeeper.dushi.entity.CarrionHoundEntity;
import com.keykeeper.dushi.entity.ChaplainEntity;
import com.keykeeper.dushi.entity.GravediggerEntity;
import com.keykeeper.dushi.entity.KeykeeperEntity;
import com.keykeeper.dushi.entity.OverseerEntity;
import com.keykeeper.dushi.entity.RustKnightEntity;
import com.keykeeper.dushi.entity.SoulStainEntity;
import com.keykeeper.dushi.entity.SpellBoltEntity;
import com.keykeeper.dushi.entity.TarPotEntity;
import com.keykeeper.dushi.entity.WitheredEntity;
import com.keykeeper.dushi.item.CatalystItem;
import com.keykeeper.dushi.item.FlaskItem;
import com.keykeeper.dushi.item.ReturnEmberItem;
import com.keykeeper.dushi.item.ShortBowItem;
import com.keykeeper.dushi.item.SoulItem;
import com.keykeeper.dushi.item.SpellItem;
import com.keykeeper.dushi.item.TarPotItem;
import com.keykeeper.dushi.item.WeaponItem;
import com.keykeeper.dushi.world.PlanPiece;
import com.keykeeper.dushi.world.PlanStructure;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(Dushi.MODID)
public class Dushi {
    public static final String MODID = "vsratie_dushi";

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, MODID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, MODID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    // ------------------------------------------------------------------ player data

    /** Only the player's own client is told about their souls and stamina. */
    public static final Supplier<AttachmentType<PlayerSoul>> PLAYER_SOUL = ATTACHMENTS.register("player_soul",
            () -> AttachmentType.builder(PlayerSoul::new).serialize(PlayerSoul.CODEC).copyOnDeath()
                    .sync((holder, to) -> holder == to, PlayerSoul.STREAM_CODEC).build());

    // ------------------------------------------------------------------ sounds (made by tools/gen_sounds.py)

    private static final Map<String, DeferredHolder<SoundEvent, SoundEvent>> SOUND_EVENTS = new LinkedHashMap<>();
    public static final String[] SOUND_NAMES = {
            "entity.withered.ambient", "entity.withered.hurt", "entity.withered.death",
            "entity.rust_knight.ambient", "entity.rust_knight.hurt", "entity.rust_knight.death", "entity.rust_knight.step",
            "entity.overseer.ambient", "entity.overseer.hurt", "entity.overseer.death", "entity.overseer.roar", "entity.overseer.slam",
            "entity.overseer.swing", "entity.overseer.chain",
            "entity.chaplain.ambient", "entity.chaplain.hurt", "entity.chaplain.death", "entity.chaplain.cast", "entity.chaplain.bell",
            "entity.chaplain.blink",
            "entity.keykeeper.ambient", "entity.keykeeper.give",
            "ui.death", "ui.boss_defeated", "ui.hearth_lit", "ui.level_up", "ui.souls_gain", "ui.souls_retrieve", "ui.place",
            "entity.hound.ambient", "entity.hound.hurt", "entity.hound.death", "entity.hound.bite", "entity.watchman.alarm",
            "entity.gravedigger.ambient", "entity.gravedigger.hurt", "entity.gravedigger.death", "entity.gravedigger.dig", "entity.gravedigger.raise",
            "block.hearth.rest", "block.fog_gate.pass",
            "item.flask.drink", "item.flask.empty", "item.spell.shard", "item.spell.shard_hit", "item.spell.mend", "item.spell.rebuke",
            "item.spell.fire", "item.spell.fire_burst", "item.spell.fizzle",
            "player.roll", "player.backstab", "player.guard_break", "player.weapon_swing_heavy",
            "music.boss"
    };

    static {
        for (String name : SOUND_NAMES) {
            SOUND_EVENTS.put(name, SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id(name))));
        }
    }

    public static SoundEvent sound(String name) {
        return SOUND_EVENTS.get(name).get();
    }

    // ------------------------------------------------------------------ blocks

    /** The checkpoint: rest here to heal, refill the flask, set the respawn point and spend souls. */
    public static final DeferredBlock<HearthBlock> HEARTH = BLOCKS.registerBlock("hearth", HearthBlock::new,
            props -> props.mapColor(MapColor.COLOR_GRAY).strength(3.0F, 1200.0F).noOcclusion().sound(SoundType.STONE)
                    .lightLevel(state -> state.getValue(HearthBlock.LIT) ? 13 : 0));
    public static final DeferredItem<BlockItem> HEARTH_ITEM = ITEMS.registerSimpleBlockItem(HEARTH);

    /** A wall of fog: players walk through it, nothing else does. */
    public static final DeferredBlock<FogGateBlock> FOG_GATE = BLOCKS.registerBlock("fog_gate", FogGateBlock::new,
            props -> props.mapColor(MapColor.SNOW).strength(-1.0F, 3600000.0F).noOcclusion().noLootTable().sound(SoundType.WOOL)
                    .lightLevel(state -> 6).isSuffocating((state, level, pos) -> false));
    public static final DeferredItem<BlockItem> FOG_GATE_ITEM = ITEMS.registerSimpleBlockItem(FOG_GATE);

    /** The old anvil: gear is made at it from scrap, cloth and souls. */
    public static final DeferredBlock<ForgeBlock> FORGE = BLOCKS.registerBlock("forge", ForgeBlock::new,
            props -> props.mapColor(MapColor.METAL).strength(5.0F, 1200.0F).noOcclusion().sound(SoundType.ANVIL));
    public static final DeferredItem<BlockItem> FORGE_ITEM = ITEMS.registerSimpleBlockItem(FORGE);

    /** Where one of the dead keeps rising: see {@link MobMarkBlock}. */
    public static final DeferredBlock<MobMarkBlock> MOB_MARK = BLOCKS.registerBlock("mob_mark", MobMarkBlock::new,
            props -> props.mapColor(MapColor.COLOR_GRAY).strength(-1.0F, 3600000.0F).noCollision().noOcclusion().noLootTable()
                    .pushReaction(PushReaction.IMMOVEABLE).sound(SoundType.SAND));
    public static final DeferredItem<BlockItem> MOB_MARK_ITEM = ITEMS.registerSimpleBlockItem(MOB_MARK);

    /** A flask shard glowing in a heap of ash, for whoever takes it: see {@link ShardBlock}. No item; maps set it. */
    public static final DeferredBlock<ShardBlock> SHARD_HEAP = BLOCKS.registerBlock("shard_heap", ShardBlock::new,
            props -> props.mapColor(MapColor.COLOR_ORANGE).strength(-1.0F, 3600000.0F).noOcclusion().noLootTable()
                    .pushReaction(PushReaction.IMMOVEABLE).lightLevel(state -> 10).sound(SoundType.SAND));

    /** A threshold that names the place beyond it: see {@link PlaceNameBlock}. It has no item; maps set it with their builder. */
    public static final DeferredBlock<PlaceNameBlock> PLACE_NAME = BLOCKS.registerBlock("place_name", PlaceNameBlock::new,
            props -> props.mapColor(MapColor.NONE).strength(-1.0F, 3600000.0F).noCollision().noOcclusion().noLootTable()
                    .pushReaction(PushReaction.IMMOVEABLE));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MobMarkBlockEntity>> MOB_MARK_ENTITY = BLOCK_ENTITIES.register("mob_mark",
            () -> new BlockEntityType<>(MobMarkBlockEntity::new, MOB_MARK.get()));

    // ------------------------------------------------------------------ structures (built from the plans of tools/gen_structures.py)

    public static final DeferredHolder<StructureType<?>, StructureType<PlanStructure>> PLAN_STRUCTURE = STRUCTURE_TYPES.register("plan",
            () -> () -> PlanStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> PLAN_PIECE = STRUCTURE_PIECES.register("plan",
            () -> (StructurePieceType.ContextlessType) PlanPiece::new);

    // ------------------------------------------------------------------ weapons

    /** All melee weapons in creative tab order. */
    public static final List<DeferredItem<WeaponItem>> WEAPONS = new ArrayList<>();

    /**
     * @param damage     attack damage shown on the item (a fist is 1)
     * @param speed      attacks per second
     * @param reach      extra reach in blocks
     * @param stamina    stamina one swing costs
     * @param strength   how much it gains from strength, 0..1
     * @param dexterity  how much it gains from dexterity, 0..1
     * @param backstab   damage multiplier for a blow in the back
     * @param durability unused: nothing of the mod wears out, because dying over and over must not cost the player their gear
     */
    private static DeferredItem<WeaponItem> weapon(String name, float damage, float speed, float reach, float stamina, float strength,
            float dexterity, float backstab, int durability, Rarity rarity) {
        DeferredItem<WeaponItem> item = ITEMS.registerItem(name, props -> {
            ItemAttributeModifiers.Builder attributes = ItemAttributeModifiers.builder()
                    .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, damage - 1.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, speed - 4.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.MAINHAND);
            if (reach != 0.0F) {
                attributes.add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(id("weapon_reach"), reach, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND);
            }
            return new WeaponItem(props.stacksTo(1).rarity(rarity).attributes(attributes.build())
                    .component(DataComponents.WEAPON, new Weapon(1)).setNoCombineRepair(), stamina, strength, dexterity, backstab);
        });
        WEAPONS.add(item);
        return item;
    }

    public static final DeferredItem<WeaponItem> BROKEN_SWORD = weapon("broken_sword", 3.0F, 1.8F, -0.5F, 9.0F, 0.3F, 0.3F, 1.5F, 90, Rarity.COMMON);
    public static final DeferredItem<WeaponItem> SHIV = weapon("shiv", 3.5F, 2.6F, -0.5F, 7.0F, 0.0F, 0.9F, 3.0F, 160, Rarity.COMMON);
    public static final DeferredItem<WeaponItem> DAGGER = weapon("dagger", 4.5F, 2.4F, -0.5F, 8.0F, 0.1F, 0.8F, 3.0F, 260, Rarity.COMMON);
    public static final DeferredItem<WeaponItem> SHORTSWORD = weapon("shortsword", 5.5F, 1.9F, 0.0F, 12.0F, 0.5F, 0.5F, 1.5F, 300, Rarity.COMMON);
    public static final DeferredItem<WeaponItem> LONGSWORD = weapon("longsword", 6.5F, 1.6F, 0.25F, 15.0F, 0.6F, 0.5F, 1.5F, 400, Rarity.COMMON);
    public static final DeferredItem<WeaponItem> BROADSWORD = weapon("broadsword", 7.5F, 1.4F, 0.0F, 17.0F, 0.8F, 0.3F, 1.5F, 450, Rarity.COMMON);
    public static final DeferredItem<WeaponItem> SABRE = weapon("sabre", 6.0F, 2.0F, 0.0F, 12.0F, 0.2F, 0.9F, 1.75F, 320, Rarity.COMMON);
    public static final DeferredItem<WeaponItem> HATCHET = weapon("hatchet", 6.5F, 1.5F, -0.25F, 15.0F, 0.8F, 0.2F, 1.5F, 300, Rarity.COMMON);
    public static final DeferredItem<WeaponItem> BATTLE_AXE = weapon("battle_axe", 8.5F, 1.1F, 0.0F, 21.0F, 1.0F, 0.1F, 1.5F, 420, Rarity.COMMON);
    public static final DeferredItem<WeaponItem> CLUB = weapon("club", 6.0F, 1.3F, 0.0F, 16.0F, 0.9F, 0.0F, 1.5F, 200, Rarity.COMMON);
    public static final DeferredItem<WeaponItem> FLANGED_MACE = weapon("flanged_mace", 8.0F, 1.2F, 0.0F, 19.0F, 1.0F, 0.0F, 1.5F, 450, Rarity.COMMON);
    public static final DeferredItem<WeaponItem> PITCHFORK = weapon("pitchfork", 5.0F, 1.5F, 0.75F, 14.0F, 0.3F, 0.7F, 1.4F, 240, Rarity.COMMON);
    public static final DeferredItem<WeaponItem> CLEAVER = weapon("cleaver", 6.5F, 1.7F, -0.25F, 14.0F, 0.8F, 0.2F, 1.5F, 300, Rarity.COMMON);
    public static final DeferredItem<WeaponItem> RUST_GREATSWORD = weapon("rust_greatsword", 11.0F, 0.9F, 1.0F, 30.0F, 1.0F, 0.3F, 1.5F, 600, Rarity.UNCOMMON);
    public static final DeferredItem<WeaponItem> GREAT_KEY = weapon("great_key", 14.0F, 0.7F, 1.0F, 38.0F, 1.0F, 0.0F, 1.5F, 900, Rarity.EPIC);
    public static final DeferredItem<WeaponItem> GRAVE_SPADE = weapon("grave_spade", 10.0F, 1.0F, 0.75F, 26.0F, 1.0F, 0.1F, 1.5F, 600, Rarity.EPIC);
    public static final DeferredItem<WeaponItem> CENSER_FLAIL = weapon("censer_flail", 9.0F, 1.2F, 1.0F, 22.0F, 0.4F, 0.6F, 1.5F, 700, Rarity.EPIC);

    public static final DeferredItem<ShortBowItem> SHORT_BOW = ITEMS.registerItem("short_bow", props -> new ShortBowItem(props.stacksTo(1)));

    // ------------------------------------------------------------------ shields

    public static final List<DeferredItem<Item>> SHIELDS = new ArrayList<>();

    /**
     * @param reduction share of the blocked blow the shield takes, 0..1
     * @param stability share of the blow's force that does NOT cost stamina, 0..1
     */
    private static DeferredItem<Item> shield(String name, float reduction, float stability, int durability, Rarity rarity) {
        DeferredItem<Item> item = ITEMS.registerItem(name, props -> new com.keykeeper.dushi.item.GuardShieldItem(props.stacksTo(1)
                .rarity(rarity).equippableUnswappable(net.minecraft.world.entity.EquipmentSlot.OFFHAND).setNoCombineRepair()
                .delayedComponent(DataComponents.BLOCKS_ATTACKS, lookup -> new BlocksAttacks(0.15F, 1.0F,
                        List.of(new BlocksAttacks.DamageReduction(100.0F, Optional.empty(), 0.0F, reduction)),
                        new BlocksAttacks.ItemDamageFunction(3.0F, 0.0F, 0.0F),
                        Optional.of(lookup.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                        Optional.of(SoundEvents.SHIELD_BLOCK), Optional.of(SoundEvents.SHIELD_BREAK))), stability));
        SHIELDS.add(item);
        return item;
    }

    public static final DeferredItem<Item> PLANK_SHIELD = shield("plank_shield", 0.6F, 0.3F, 120, Rarity.COMMON);
    public static final DeferredItem<Item> ROUND_SHIELD = shield("round_shield", 0.75F, 0.45F, 220, Rarity.COMMON);
    public static final DeferredItem<Item> GUARD_SHIELD = shield("guard_shield", 0.9F, 0.6F, 400, Rarity.COMMON);
    public static final DeferredItem<Item> TOWER_SHIELD = shield("tower_shield", 1.0F, 0.8F, 700, Rarity.UNCOMMON);

    // ------------------------------------------------------------------ armour

    static {
        for (ArmorSet set : ArmorSet.values()) {
            set.register(ITEMS);
        }
    }

    /** What the dead of the stockade leave behind; the mod's armour is made and mended with these. */
    public static final DeferredItem<Item> TATTERED_CLOTH = ITEMS.registerSimpleItem("tattered_cloth");
    public static final DeferredItem<Item> RUST_SCRAP = ITEMS.registerSimpleItem("rust_scrap");

    // ------------------------------------------------------------------ magic

    public static final DeferredItem<CatalystItem> SCHOLAR_STAFF = ITEMS.registerItem("scholar_staff",
            props -> new CatalystItem(props.stacksTo(1), SpellSchool.SORCERY));
    public static final DeferredItem<CatalystItem> PRAYER_BEADS = ITEMS.registerItem("prayer_beads",
            props -> new CatalystItem(props.stacksTo(1), SpellSchool.MIRACLE));
    public static final DeferredItem<CatalystItem> EMBER_GLOVE = ITEMS.registerItem("ember_glove",
            props -> new CatalystItem(props.stacksTo(1), SpellSchool.PYROMANCY));

    public static final Map<Spell, DeferredItem<SpellItem>> SPELLS = new LinkedHashMap<>();

    static {
        for (Spell spell : Spell.values()) {
            SPELLS.put(spell, ITEMS.registerItem(spell.id, props -> new SpellItem(props.durability(spell.casts + 1).setNoCombineRepair()
                    .rarity(Rarity.UNCOMMON), spell)));
        }
    }

    /** Only the pictures of flying spells; not in the creative tab. */
    public static final Map<SpellBoltEntity.Kind, DeferredItem<Item>> BOLT_ITEMS = new LinkedHashMap<>();

    static {
        for (SpellBoltEntity.Kind kind : SpellBoltEntity.Kind.values()) {
            BOLT_ITEMS.put(kind, ITEMS.registerSimpleItem(kind.id + "_bolt"));
        }
    }

    /** Only the picture of a soul stain; not in the creative tab. */
    public static final DeferredItem<Item> SOUL_STAIN_ITEM = ITEMS.registerSimpleItem("soul_stain");

    // ------------------------------------------------------------------ other items

    public static final DeferredItem<FlaskItem> WARMTH_FLASK = ITEMS.registerItem("warmth_flask",
            props -> new FlaskItem(props.durability(FlaskItem.BASE_CHARGES + 1).setNoCombineRepair().rarity(Rarity.RARE)));
    public static final DeferredItem<SoulItem> NAMELESS_SOUL = ITEMS.registerItem("nameless_soul", props -> new SoulItem(props.stacksTo(64), 200));
    public static final DeferredItem<SoulItem> GREAT_SOUL = ITEMS.registerItem("great_soul",
            props -> new SoulItem(props.stacksTo(64).rarity(Rarity.UNCOMMON), 1000));
    public static final DeferredItem<SoulItem> GRAVEDIGGER_SOUL = ITEMS.registerItem("gravedigger_soul",
            props -> new SoulItem(props.stacksTo(1).rarity(Rarity.EPIC).fireResistant(), 2500));
    public static final DeferredItem<SoulItem> OVERSEER_SOUL = ITEMS.registerItem("overseer_soul",
            props -> new SoulItem(props.stacksTo(1).rarity(Rarity.EPIC).fireResistant(), 5000));
    public static final DeferredItem<SoulItem> CHAPLAIN_SOUL = ITEMS.registerItem("chaplain_soul",
            props -> new SoulItem(props.stacksTo(1).rarity(Rarity.EPIC).fireResistant(), 8000));
    public static final DeferredItem<ReturnEmberItem> RETURN_EMBER = ITEMS.registerItem("return_ember", props -> new ReturnEmberItem(props.stacksTo(16)));
    public static final DeferredItem<TarPotItem> TAR_POT = ITEMS.registerItem("tar_pot", props -> new TarPotItem(props.stacksTo(16)));
    public static final DeferredItem<com.keykeeper.dushi.item.FlaskShardItem> FLASK_SHARD = ITEMS.registerItem("flask_shard",
            props -> new com.keykeeper.dushi.item.FlaskShardItem(props.stacksTo(16).rarity(Rarity.RARE)));

    // ------------------------------------------------------------------ entities

    public static final DeferredHolder<EntityType<?>, EntityType<SoulStainEntity>> SOUL_STAIN = ENTITIES.registerEntityType(
            "soul_stain", SoulStainEntity::new, MobCategory.MISC, b -> b.sized(0.8F, 0.8F).clientTrackingRange(10).updateInterval(20).fireImmune());
    public static final DeferredHolder<EntityType<?>, EntityType<SpellBoltEntity>> SPELL_BOLT = ENTITIES.registerEntityType(
            "spell_bolt", SpellBoltEntity::new, MobCategory.MISC, b -> b.sized(0.35F, 0.35F).clientTrackingRange(6).updateInterval(2));
    public static final DeferredHolder<EntityType<?>, EntityType<TarPotEntity>> TAR_POT_ENTITY = ENTITIES.registerEntityType(
            "tar_pot", TarPotEntity::new, MobCategory.MISC, b -> b.sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));

    /** The undead of the stockade, see {@link WitheredEntity.Kind}. */
    public static final Map<WitheredEntity.Kind, DeferredHolder<EntityType<?>, EntityType<WitheredEntity>>> WITHERED = new LinkedHashMap<>();

    static {
        for (WitheredEntity.Kind kind : WitheredEntity.Kind.values()) {
            WITHERED.put(kind, ENTITIES.registerEntityType(kind.id, (type, level) -> new WitheredEntity(type, level, kind), MobCategory.MONSTER,
                    b -> b.sized(0.6F, 1.95F).eyeHeight(1.74F).clientTrackingRange(8)));
        }
    }

    public static final DeferredHolder<EntityType<?>, EntityType<RustKnightEntity>> RUST_KNIGHT = ENTITIES.registerEntityType(
            "rust_knight", RustKnightEntity::new, MobCategory.MONSTER, b -> b.sized(0.85F, 2.6F).eyeHeight(2.3F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<RustKnightEntity>> GILDED_KNIGHT = ENTITIES.registerEntityType(
            "gilded_knight", RustKnightEntity::new, MobCategory.MONSTER, b -> b.sized(0.85F, 2.6F).eyeHeight(2.3F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<KeykeeperEntity>> KEYKEEPER = ENTITIES.registerEntityType(
            "keykeeper", KeykeeperEntity::new, MobCategory.MISC, b -> b.sized(0.7F, 1.3F).eyeHeight(1.0F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<OverseerEntity>> OVERSEER = ENTITIES.registerEntityType(
            "overseer", OverseerEntity::new, MobCategory.MONSTER, b -> b.sized(2.0F, 4.2F).eyeHeight(3.6F).clientTrackingRange(12).fireImmune());
    public static final DeferredHolder<EntityType<?>, EntityType<ChaplainEntity>> CHAPLAIN = ENTITIES.registerEntityType(
            "chaplain", ChaplainEntity::new, MobCategory.MONSTER, b -> b.sized(0.9F, 3.0F).eyeHeight(2.6F).clientTrackingRange(12));

    public static final DeferredHolder<EntityType<?>, EntityType<CarrionHoundEntity>> CARRION_HOUND = ENTITIES.registerEntityType(
            "carrion_hound", CarrionHoundEntity::new, MobCategory.MONSTER, b -> b.sized(0.75F, 0.95F).eyeHeight(0.8F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<GravediggerEntity>> GRAVEDIGGER = ENTITIES.registerEntityType(
            "gravedigger", GravediggerEntity::new, MobCategory.MONSTER, b -> b.sized(1.5F, 3.6F).eyeHeight(3.1F).clientTrackingRange(12));

    public static final List<DeferredItem<SpawnEggItem>> SPAWN_EGGS = new ArrayList<>();

    private static void egg(String name, Supplier<? extends EntityType<? extends Mob>> type) {
        SPAWN_EGGS.add(ITEMS.registerItem(name + "_spawn_egg", props -> new SpawnEggItem(props.spawnEgg(type.get()))));
    }

    static {
        WITHERED.forEach((kind, type) -> egg(kind.id, type));
        egg("rust_knight", RUST_KNIGHT);
        egg("gilded_knight", GILDED_KNIGHT);
        egg("carrion_hound", CARRION_HOUND);
        egg("gravedigger", GRAVEDIGGER);
        egg("keykeeper", KEYKEEPER);
        egg("overseer", OVERSEER);
        egg("chaplain", CHAPLAIN);
    }

    // ------------------------------------------------------------------ creative tab

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.vsratie_dushi"))
            .icon(() -> WARMTH_FLASK.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(HEARTH_ITEM.get());
                output.accept(FORGE_ITEM.get());
                output.accept(FOG_GATE_ITEM.get());
                output.accept(MOB_MARK_ITEM.get());
                output.accept(WARMTH_FLASK.get());
                output.accept(FLASK_SHARD.get());
                output.accept(RETURN_EMBER.get());
                output.accept(TAR_POT.get());
                output.accept(NAMELESS_SOUL.get());
                output.accept(GREAT_SOUL.get());
                output.accept(OVERSEER_SOUL.get());
                output.accept(CHAPLAIN_SOUL.get());
                WEAPONS.forEach(item -> output.accept(item.get()));
                output.accept(SHORT_BOW.get());
                SHIELDS.forEach(item -> output.accept(item.get()));
                for (ArmorSet set : ArmorSet.values()) {
                    set.pieces().forEach(item -> output.accept(item.get()));
                }
                output.accept(TATTERED_CLOTH.get());
                output.accept(RUST_SCRAP.get());
                output.accept(SCHOLAR_STAFF.get());
                output.accept(PRAYER_BEADS.get());
                output.accept(EMBER_GLOVE.get());
                SPELLS.values().forEach(item -> output.accept(item.get()));
                SPAWN_EGGS.forEach(item -> output.accept(item.get()));
            }).build());

    public Dushi(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);
        TABS.register(modEventBus);
        SOUNDS.register(modEventBus);
        ATTACHMENTS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        STRUCTURE_TYPES.register(modEventBus);
        STRUCTURE_PIECES.register(modEventBus);
        modEventBus.addListener((EntityAttributeCreationEvent event) -> {
            WITHERED.forEach((kind, type) -> event.put(type.get(), WitheredEntity.createAttributes(kind).build()));
            event.put(RUST_KNIGHT.get(), RustKnightEntity.createAttributes().build());
            event.put(GILDED_KNIGHT.get(), RustKnightEntity.createGildedAttributes().build());
            event.put(CARRION_HOUND.get(), CarrionHoundEntity.createAttributes().build());
            event.put(GRAVEDIGGER.get(), GravediggerEntity.createAttributes().build());
            event.put(KEYKEEPER.get(), KeykeeperEntity.createAttributes().build());
            event.put(OVERSEER.get(), OverseerEntity.createAttributes().build());
            event.put(CHAPLAIN.get(), ChaplainEntity.createAttributes().build());
        });
        modEventBus.addListener((RegisterPayloadHandlersEvent event) -> {
            var registrar = event.registrar("1");
            registrar.playToServer(Net.Roll.TYPE, Net.Roll.CODEC, Net.Roll::handle);
            registrar.playToServer(Net.LevelUp.TYPE, Net.LevelUp.CODEC, Net.LevelUp::handle);
            registrar.playToClient(Net.OpenHearth.TYPE, Net.OpenHearth.CODEC);
            registrar.playToClient(Net.Banner.TYPE, Net.Banner.CODEC);
            registrar.playToClient(Net.OpenShop.TYPE, Net.OpenShop.CODEC);
            registrar.playToServer(Net.Buy.TYPE, Net.Buy.CODEC, Net.Buy::handle);
            registrar.playToClient(Net.OpenForge.TYPE, Net.OpenForge.CODEC);
            registrar.playToServer(Net.Make.TYPE, Net.Make.CODEC, Net.Make::handle);
        });
    }
}
