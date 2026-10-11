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
import com.keykeeper.dushi.block.LockedGateBlock;
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
    public static final String MODID = "unfading_dawn";

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
            "entity.quarry.pick", "entity.quarry.blast", "entity.quarry.fuse", "entity.quarry.rockfall", "entity.quarry.barrow",
            "entity.crawler.ambient", "entity.crawler.hurt", "entity.crawler.death", "entity.crawler.screech",
            "entity.unhewn.ambient", "entity.unhewn.hurt", "entity.unhewn.death", "entity.unhewn.wake", "entity.unhewn.slam", "entity.unhewn.step",
            "entity.taskmaster.ambient", "entity.taskmaster.hurt", "entity.taskmaster.death", "entity.taskmaster.shout", "entity.taskmaster.maul",
            "entity.taskmaster.wedge", "entity.taskmaster.crack",
            "entity.stone_eater.ambient", "entity.stone_eater.hurt", "entity.stone_eater.death", "entity.stone_eater.roar", "entity.stone_eater.burrow",
            "entity.stone_eater.erupt", "entity.stone_eater.bite",
            "entity.pike.ambient", "entity.pike.hurt", "entity.pike.death", "entity.pike.bite", "entity.pike.leap", "entity.pike.flop",
            "entity.lanky.ambient", "entity.lanky.hurt", "entity.lanky.death", "entity.lanky.slam", "entity.lanky.step",
            "entity.wisp.ambient", "entity.wisp.hurt", "entity.wisp.death", "entity.wisp.cast",
            "entity.wallower.ambient", "entity.wallower.hurt", "entity.wallower.death", "entity.wallower.roar", "entity.wallower.bite",
            "entity.wallower.slap", "entity.wallower.spit", "entity.wallower.plop", "entity.wallower.dive", "entity.wallower.wake",
            "entity.wallower.breach", "entity.wallower.gulp",
            "entity.weeping_mother.ambient", "entity.weeping_mother.hurt", "entity.weeping_mother.death", "entity.weeping_mother.wail",
            "entity.weeping_mother.lash", "entity.weeping_mother.slam", "entity.weeping_mother.creak", "entity.weeping_mother.root",
            "entity.sect.ambient", "entity.sect.hurt", "entity.sect.death", "entity.wakeful.rise", "entity.wakeful.vigil", "entity.waker.knock",
            "entity.sleeper.cry", "entity.sleeper.fall", "entity.sleeper.snore",
            "entity.waxen.ambient", "entity.waxen.hurt", "entity.waxen.death", "entity.waxen.slam", "entity.waxen.gather", "entity.waxen.spew",
            "entity.sleep_moth.flutter", "entity.sleep_moth.hurt", "entity.sleep_moth.death", "entity.sleep_moth.dust",
            "entity.shatun.ambient", "entity.shatun.hurt", "entity.shatun.death", "entity.shatun.roar", "entity.shatun.swipe", "entity.shatun.slam",
            "entity.shatun.bite", "entity.shatun.step", "entity.shatun.crash",
            "entity.elder.ambient", "entity.elder.hurt", "entity.elder.death", "entity.elder.sweep", "entity.elder.chant", "entity.elder.knock",
            "entity.elder.call", "entity.elder.slumber", "entity.elder.dream", "entity.elder.snore",
            "entity.sailor.ambient", "entity.sailor.hurt", "entity.sailor.death", "entity.sailor.hook", "entity.sailor.throw", "entity.sailor.oar",
            "entity.harpoon.hit", "entity.pilot.whirl", "entity.pilot.lead",
            "entity.sand_crab.dig", "entity.sand_crab.snap", "entity.sand_crab.click", "entity.sand_crab.hurt", "entity.sand_crab.death",
            "entity.skua.cry", "entity.skua.stoop", "entity.skua.hurt", "entity.skua.death",
            "entity.helmsman.ambient", "entity.helmsman.hurt", "entity.helmsman.death", "entity.helmsman.swing", "entity.helmsman.anchor",
            "entity.helmsman.chain", "entity.helmsman.shout",
            "entity.capsized.ambient", "entity.capsized.hurt", "entity.capsized.death", "entity.capsized.rise", "entity.capsized.snap",
            "entity.capsized.creak", "entity.capsized.slam", "entity.capsized.knock",
            "ambient.shore.sea", "player.undertow",
            "entity.fieldhand.ambient", "entity.fieldhand.hurt", "entity.fieldhand.death", "entity.fieldhand.sickle", "entity.fieldhand.scythe",
            "entity.fieldhand.sack", "entity.scorched.ambient", "entity.scorched.hurt", "entity.scorched.death", "entity.scorched.flare",
            "entity.scarecrow.creak", "entity.scarecrow.hurt", "entity.scarecrow.death", "entity.scarecrow.wake", "entity.scarecrow.spin",
            "entity.stook.rustle", "entity.stook.hurt", "entity.stook.death", "entity.stook.fall", "entity.stook.shed",
            "entity.sungazer.hum", "entity.sungazer.hurt", "entity.sungazer.death", "entity.sungazer.gaze", "entity.sungazer.lash",
            "entity.warden.ambient", "entity.warden.hurt", "entity.warden.death", "entity.warden.sweep", "entity.warden.slam",
            "entity.warden.kindle", "entity.warden.roar", "entity.sun.gather", "entity.sun.burn",
            "entity.miller.ambient", "entity.miller.hurt", "entity.miller.death", "entity.miller.swing", "entity.miller.slam",
            "entity.miller.shout", "entity.miller.lantern", "entity.millstone.rumble", "entity.millstone.check", "entity.millstone.crash",
            "entity.flour.puff", "entity.flour.blast",
            "entity.usher.ambient", "entity.usher.hurt", "entity.usher.death", "entity.usher.open", "entity.usher.take", "entity.usher.reach", "entity.usher.step",
            "entity.shadow.ambient", "entity.shadow.hurt", "entity.shadow.death", "entity.shadow.rise", "entity.shadow.claw", "entity.shadow.snatch",
            "entity.longarm.ambient", "entity.longarm.hurt", "entity.longarm.death", "entity.longarm.reach", "entity.longarm.take",
            "entity.handwalker.ambient", "entity.handwalker.hurt", "entity.handwalker.death", "entity.handwalker.rear", "entity.handwalker.whip",
            "entity.handwalker.step",
            "entity.deepling.ambient", "entity.deepling.hurt", "entity.deepling.death", "entity.deepling.wake", "entity.deepling.gulp", "entity.deepling.glare",
            "entity.gut.ambient", "entity.gut.hurt", "entity.gut.death", "entity.gut.bellow", "entity.gut.step", "entity.gut.crash", "entity.gut.slam", "entity.gut.gape", "entity.gut.shell",
            "entity.clockhand.hurt", "entity.clockhand.death", "entity.clockhand.tick", "entity.clockhand.stab", "entity.clockhand.sweep", "entity.clockhand.wind",
            "entity.bolt.ambient", "entity.bolt.hurt", "entity.bolt.death", "entity.bolt.tock", "entity.bolt.strike", "entity.bolt.toll", "entity.bolt.chain",
            "entity.bolt.weight", "entity.bolt.strain", "entity.bolt.snap", "entity.bolt.stab", "entity.bolt.sweep", "entity.bolt.land", "entity.bolt.step",
            "entity.coming.ambient", "entity.coming.hurt", "entity.coming.death", "entity.coming.gaze", "entity.coming.rear", "entity.coming.slam",
            "entity.coming.sweep", "entity.coming.strain", "entity.coming.unmask", "entity.coming.pull", "entity.coming.bite", "entity.coming.star",
            "entity.coming.burst",
            "entity.gear.wind", "entity.gear.roll", "entity.gear.catch", "entity.tendril.open", "entity.tendril.rise", "entity.tendril.slap",
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
    /** An iron grate that opens to whoever carries its key: see {@link LockedGateBlock}. It has no item; maps set it. */
    public static final DeferredBlock<LockedGateBlock> LOCKED_GATE = BLOCKS.registerBlock("locked_gate", LockedGateBlock::new,
            props -> props.mapColor(MapColor.METAL).strength(-1.0F, 3600000.0F).noOcclusion().noLootTable()
                    .pushReaction(PushReaction.IMMOVEABLE).sound(SoundType.METAL));
    /** A hand's depth of water over a floor, which is a floor: see {@link com.keykeeper.dushi.block.ShallowsBlock}. No item; maps set it. */
    public static final DeferredBlock<com.keykeeper.dushi.block.ShallowsBlock> SHALLOWS = BLOCKS.registerBlock("shallows", com.keykeeper.dushi.block.ShallowsBlock::new,
            props -> props.mapColor(MapColor.WATER).strength(-1.0F, 3600000.0F).noCollision().noOcclusion().noLootTable()
                    .pushReaction(PushReaction.IMMOVEABLE).sound(SoundType.WET_GRASS));
    /** The same thing on the sands of the Morning Wharf: the sea's own grey, with foam on it. */
    public static final DeferredBlock<com.keykeeper.dushi.block.ShallowsBlock> SEA_SHALLOWS = BLOCKS.registerBlock("sea_shallows", com.keykeeper.dushi.block.ShallowsBlock::new,
            props -> props.mapColor(MapColor.WATER).strength(-1.0F, 3600000.0F).noCollision().noOcclusion().noLootTable()
                    .pushReaction(PushReaction.IMMOVEABLE).sound(SoundType.WET_GRASS));
    /** The same thing over the black floors of the Sky Beneath: all but black, with stars in it that are not over it. It shines, a very little. */
    public static final DeferredBlock<com.keykeeper.dushi.block.ShallowsBlock> NIGHT_SHALLOWS = BLOCKS.registerBlock("night_shallows", com.keykeeper.dushi.block.ShallowsBlock::new,
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(-1.0F, 3600000.0F).noCollision().noOcclusion().noLootTable()
                    .pushReaction(PushReaction.IMMOVEABLE).sound(SoundType.WET_GRASS).emissiveRendering(state -> true));
    /** The sky of the Sky Beneath: what its roof, its far walls and its pits are made of. See {@link com.keykeeper.dushi.block.UnderSkyBlock}. No item; maps set it. */
    public static final DeferredBlock<com.keykeeper.dushi.block.UnderSkyBlock> UNDER_SKY = BLOCKS.registerBlock("under_sky", com.keykeeper.dushi.block.UnderSkyBlock::new,
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(-1.0F, 3600000.0F).noLootTable().pushReaction(PushReaction.IMMOVEABLE)
                    .sound(SoundType.STONE).emissiveRendering(state -> true));
    /** A sheet of mist with stars in it, over the great hollows of the Sky Beneath: see {@link com.keykeeper.dushi.block.SkyVeilBlock}. No item; maps set it. */
    public static final DeferredBlock<com.keykeeper.dushi.block.SkyVeilBlock> SKY_VEIL = BLOCKS.registerBlock("sky_veil", com.keykeeper.dushi.block.SkyVeilBlock::new,
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(-1.0F, 3600000.0F).noCollision().noOcclusion().noLootTable()
                    .pushReaction(PushReaction.IMMOVEABLE).sound(SoundType.WOOL).emissiveRendering(state -> true));
    /** The sky over the last of those sheets: see {@link com.keykeeper.dushi.block.SkyLidBlock}. No item; maps set it. */
    public static final DeferredBlock<com.keykeeper.dushi.block.SkyLidBlock> SKY_LID = BLOCKS.registerBlock("sky_lid", com.keykeeper.dushi.block.SkyLidBlock::new,
            props -> props.mapColor(MapColor.COLOR_BLACK).strength(-1.0F, 3600000.0F).noCollision().noOcclusion().noLootTable()
                    .pushReaction(PushReaction.IMMOVEABLE).sound(SoundType.STONE));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.keykeeper.dushi.block.SkyLidBlockEntity>> SKY_LID_ENTITY = BLOCK_ENTITIES.register("sky_lid",
            () -> new BlockEntityType<>(com.keykeeper.dushi.block.SkyLidBlockEntity::new, SKY_LID.get()));
    /** A pool of sludge that lies a while and is gone: see {@link com.keykeeper.dushi.block.SludgeBlock}. No item; the Wallower spits it. */
    public static final DeferredBlock<com.keykeeper.dushi.block.SludgeBlock> SLUDGE = BLOCKS.registerBlock("sludge", com.keykeeper.dushi.block.SludgeBlock::new,
            props -> props.mapColor(MapColor.COLOR_BROWN).strength(-1.0F, 3600000.0F).noCollision().noOcclusion().noLootTable()
                    .pushReaction(PushReaction.POPPED).sound(SoundType.MUD));
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
    /** The quarry: a digger's pick, the Taskmaster's maul, and a blade of the Stone-Eater's claw bound to a haft. */
    public static final DeferredItem<WeaponItem> QUARRY_PICK = weapon("quarry_pick", 9.0F, 1.1F, 0.25F, 22.0F, 0.9F, 0.2F, 1.5F, 500, Rarity.UNCOMMON);
    public static final DeferredItem<WeaponItem> GREAT_MAUL = weapon("great_maul", 16.0F, 0.65F, 1.0F, 42.0F, 1.0F, 0.0F, 1.5F, 900, Rarity.EPIC);
    public static final DeferredItem<WeaponItem> EATER_CLAW = weapon("eater_claw", 9.5F, 1.9F, 0.0F, 14.0F, 0.2F, 0.9F, 2.0F, 800, Rarity.EPIC);
    /** A stonecutter's own hammer, for one hand: what the heads of the Founder were roughed out with. */
    public static final DeferredItem<WeaponItem> STONE_HAMMER = weapon("stone_hammer", 9.5F, 1.15F, 0.0F, 20.0F, 1.0F, 0.0F, 1.5F, 500, Rarity.UNCOMMON);

    /** The Mire: a ferryman's boat hook, a tooth of the Wallower on a grip of its hide, and a scourge plaited of the Weeping Mother's withes. */
    public static final DeferredItem<WeaponItem> BOAT_HOOK = weapon("boat_hook", 8.0F, 1.4F, 1.0F, 18.0F, 0.4F, 0.7F, 1.4F, 400, Rarity.UNCOMMON);
    public static final DeferredItem<WeaponItem> WALLOWER_FANG = weapon("wallower_fang", 11.0F, 1.7F, 0.0F, 16.0F, 0.5F, 0.8F, 2.0F, 800, Rarity.EPIC);
    public static final DeferredItem<WeaponItem> WITHE_SCOURGE = weapon("withe_scourge", 9.5F, 1.5F, 1.75F, 20.0F, 0.1F, 1.0F, 1.5F, 800, Rarity.EPIC);
    /** A fisher's knife, long and narrow, for opening what was caught: quick, and at its best in a back. */
    public static final DeferredItem<WeaponItem> FISH_KNIFE = weapon("fish_knife", 6.5F, 2.5F, -0.5F, 8.0F, 0.1F, 0.9F, 3.0F, 400, Rarity.UNCOMMON);

    /**
     * The Black Firwood: the staff a wakeful one holds his candle on, the mallet a waker beats his board with, the spear
     * the bear was kept awake with, and the Elder's great candle on its pole (it sets alight what it strikes).
     */
    public static final DeferredItem<WeaponItem> VIGIL_STAFF = weapon("vigil_staff", 10.0F, 1.35F, 0.75F, 19.0F, 0.5F, 0.6F, 1.5F, 600, Rarity.UNCOMMON);
    public static final DeferredItem<WeaponItem> WAKER_MALLET = weapon("waker_mallet", 12.0F, 1.05F, 0.0F, 25.0F, 1.0F, 0.0F, 1.5F, 600, Rarity.UNCOMMON);
    public static final DeferredItem<WeaponItem> BEAR_SPEAR = weapon("bear_spear", 12.5F, 1.25F, 1.5F, 24.0F, 0.7F, 0.6F, 1.6F, 900, Rarity.EPIC);
    public static final DeferredItem<WeaponItem> GREAT_CANDLE = weapon("great_candle", 12.0F, 1.1F, 1.0F, 26.0F, 0.5F, 0.5F, 1.5F, 900, Rarity.EPIC);

    /**
     * The Morning Wharf: a stevedore's hook, a harpoon, an oar of a karbas (two hands), the pilot's sounding lead on its
     * line (it reaches far), the anchor of the Daybreak (two hands, and the slowest thing there is) and the lesser claw of
     * the Capsized on a grip of tarred rope.
     */
    public static final DeferredItem<WeaponItem> CARGO_HOOK = weapon("cargo_hook", 11.0F, 1.75F, -0.25F, 14.0F, 0.5F, 0.6F, 2.0F, 600, Rarity.UNCOMMON);
    public static final DeferredItem<WeaponItem> HARPOON = weapon("harpoon", 12.0F, 1.35F, 1.25F, 20.0F, 0.4F, 0.8F, 1.5F, 600, Rarity.UNCOMMON);
    public static final DeferredItem<WeaponItem> OAR = weapon("oar", 15.0F, 0.85F, 1.25F, 34.0F, 1.0F, 0.0F, 1.5F, 600, Rarity.UNCOMMON);
    public static final DeferredItem<WeaponItem> LEAD_LINE = weapon("lead_line", 12.5F, 1.3F, 2.0F, 22.0F, 0.3F, 0.9F, 1.5F, 900, Rarity.EPIC);
    public static final DeferredItem<WeaponItem> SHIP_ANCHOR = weapon("ship_anchor", 19.5F, 0.6F, 1.0F, 46.0F, 1.0F, 0.0F, 1.5F, 900, Rarity.EPIC);
    public static final DeferredItem<WeaponItem> CAPSIZED_CLAW = weapon("capsized_claw", 13.5F, 1.6F, 0.0F, 17.0F, 0.6F, 0.6F, 1.75F, 900, Rarity.EPIC);

    /**
     * The Stubble: a reaper's sickle (quick, and at its best in a back), a mower's scythe (two hands, and as wide a
     * swathe as there is), the flail they threshed with, and one of the poles the Scorched Warden had for arms (two
     * hands; it is still alight, and what it strikes burns).
     */
    public static final DeferredItem<WeaponItem> REAPER_SICKLE = weapon("reaper_sickle", 12.0F, 2.2F, -0.25F, 10.0F, 0.2F, 0.9F, 2.5F, 600, Rarity.UNCOMMON);
    public static final DeferredItem<WeaponItem> SCYTHE = weapon("scythe", 17.0F, 0.9F, 1.5F, 32.0F, 0.6F, 0.6F, 1.5F, 600, Rarity.UNCOMMON);
    public static final DeferredItem<WeaponItem> THRESH_FLAIL = weapon("thresh_flail", 13.0F, 1.1F, 0.75F, 23.0F, 0.9F, 0.2F, 1.5F, 600, Rarity.UNCOMMON);
    public static final DeferredItem<WeaponItem> WARDEN_POLE = weapon("warden_pole", 18.0F, 0.95F, 1.75F, 30.0F, 0.6F, 0.6F, 1.5F, 900, Rarity.EPIC);

    public static final DeferredItem<ShortBowItem> SHORT_BOW = ITEMS.registerItem("short_bow", props -> new ShortBowItem(props.stacksTo(1)));

    // ------------------------------------------------------------------ shields

    public static final List<DeferredItem<Item>> SHIELDS = new ArrayList<>();

    /**
     * @param reduction share of the blocked blow the shield takes, 0..1
     * @param stability share of the blow's force that does NOT cost stamina, 0..1
     */
    private static DeferredItem<Item> shield(String name, float reduction, float stability, int durability, Rarity rarity) {
        return shield(name, reduction, stability, -1.0F, null, rarity);
    }

    /**
     * @param weight what it weighs, or a negative number for the weight its stability gives it
     * @param trait  what it does that other shields do not (the key of its line; the doing is in SoulMechanics), or null
     */
    private static DeferredItem<Item> shield(String name, float reduction, float stability, float weight, String trait, Rarity rarity) {
        DeferredItem<Item> item = ITEMS.registerItem(name, props -> new com.keykeeper.dushi.item.GuardShieldItem(props.stacksTo(1)
                .rarity(rarity).equippableUnswappable(net.minecraft.world.entity.EquipmentSlot.OFFHAND).setNoCombineRepair()
                .delayedComponent(DataComponents.BLOCKS_ATTACKS, lookup -> new BlocksAttacks(0.15F, 1.0F,
                        List.of(new BlocksAttacks.DamageReduction(100.0F, Optional.empty(), 0.0F, reduction)),
                        new BlocksAttacks.ItemDamageFunction(3.0F, 0.0F, 0.0F),
                        Optional.of(lookup.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                        Optional.of(SoundEvents.SHIELD_BLOCK), Optional.of(SoundEvents.SHIELD_BREAK))), stability, weight, trait));
        SHIELDS.add(item);
        return item;
    }

    public static final DeferredItem<Item> PLANK_SHIELD = shield("plank_shield", 0.6F, 0.3F, 120, Rarity.COMMON);
    public static final DeferredItem<Item> ROUND_SHIELD = shield("round_shield", 0.75F, 0.45F, 220, Rarity.COMMON);
    public static final DeferredItem<Item> GUARD_SHIELD = shield("guard_shield", 0.9F, 0.6F, 400, Rarity.COMMON);
    public static final DeferredItem<Item> TOWER_SHIELD = shield("tower_shield", 1.0F, 0.8F, 700, Rarity.UNCOMMON);
    /** A carver's buckler, no bigger than a plate: it stops little, but an arm that holds it up does not tire - stamina comes back behind it as if it were down. */
    public static final DeferredItem<Item> CARVER_BUCKLER = shield("carver_buckler", 0.7F, 0.4F, 3.0F, "buckler", Rarity.UNCOMMON);
    /** The Mire's: pike scales on a frame of withe. Nearly a guard's shield, at half the weight. */
    public static final DeferredItem<Item> SCALE_SHIELD = shield("scale_shield", 0.85F, 0.55F, 4.0F, "light", Rarity.UNCOMMON);
    /** A waker's board, carried on the arm: whoever strikes it is thrown back by it. */
    public static final DeferredItem<Item> WAKER_BOARD = shield("waker_board", 0.9F, 0.65F, -1.0F, "board", Rarity.UNCOMMON);
    /** The iron door of a drying barn's kiln: fire caught on it does nothing, and whoever holds it up does not burn. */
    public static final DeferredItem<Item> KILN_DOOR = shield("kiln_door", 0.95F, 0.7F, -1.0F, "door", Rarity.UNCOMMON);
    /** The runner stone of the Great Mill, carried as a shield: nothing gets through it, and it weighs what it weighs. */
    public static final DeferredItem<Item> MILLSTONE_SHIELD = shield("millstone_shield", 1.0F, 0.92F, -1.0F, "stone", Rarity.EPIC);

    // ------------------------------------------------------------------ armour

    static {
        for (ArmorSet set : ArmorSet.values()) {
            set.register(ITEMS);
        }
    }

    /** What the dead of the stockade leave behind; the mod's armour is made and mended with these. */
    public static final DeferredItem<Item> TATTERED_CLOTH = ITEMS.registerSimpleItem("tattered_cloth");
    public static final DeferredItem<Item> RUST_SCRAP = ITEMS.registerSimpleItem("rust_scrap");
    /** A quarryman's splitting wedge: the iron of the quarry, for what is forged from it. */
    public static final DeferredItem<Item> IRON_WEDGE = ITEMS.registerSimpleItem("iron_wedge");
    /** The Mire's two: a scale of a bone pike, and a withe of the willows that walk. */
    public static final DeferredItem<Item> BONE_SCALE = ITEMS.registerSimpleItem("bone_scale");
    public static final DeferredItem<Item> WITHE = ITEMS.registerSimpleItem("withe");
    /** The Firwood's: the bleached linen of the Unsleeping, the wax of their candles, and a piece of the hide of the bear they would not let sleep. */
    public static final DeferredItem<Item> LINEN = ITEMS.registerSimpleItem("bleached_linen");
    public static final DeferredItem<Item> WAX = ITEMS.registerSimpleItem("candle_wax");
    public static final DeferredItem<Item> SHATUN_HIDE = ITEMS.registerSimpleItem("shatun_hide");
    /** The shore's: sailcloth, rope that was tarred against the sea, and the shell of what lives in the sand. */
    public static final DeferredItem<Item> SAILCLOTH = ITEMS.registerSimpleItem("sailcloth");
    /** The Stubble's: the straw of a harvest that is never in, and flour nobody bakes with - a sack of it, to be thrown. */
    public static final DeferredItem<Item> RYE_STRAW = ITEMS.registerSimpleItem("rye_straw");
    public static final DeferredItem<com.keykeeper.dushi.item.FlourSackItem> FLOUR_SACK = ITEMS.registerItem("flour_sack",
            props -> new com.keykeeper.dushi.item.FlourSackItem(props.stacksTo(16)));
    public static final DeferredItem<Item> TARRED_ROPE = ITEMS.registerSimpleItem("tarred_rope");
    public static final DeferredItem<Item> CRAB_SHELL = ITEMS.registerSimpleItem("crab_shell");
    /** Only the picture of what the Wallower spits; not in the creative tab. */
    public static final DeferredItem<Item> MUD_GLOB = ITEMS.registerSimpleItem("mud_glob");

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

    /** The charms: one is worn at a time (see {@link Charm}). */
    public static final Map<Charm, DeferredItem<com.keykeeper.dushi.item.CharmItem>> CHARMS = new LinkedHashMap<>();

    static {
        for (Charm charm : Charm.values()) {
            CHARMS.put(charm, ITEMS.registerItem(charm.id, props -> new com.keykeeper.dushi.item.CharmItem(props.stacksTo(1).rarity(Rarity.UNCOMMON), charm)));
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
    public static final DeferredItem<SoulItem> TASKMASTER_SOUL = ITEMS.registerItem("taskmaster_soul",
            props -> new SoulItem(props.stacksTo(1).rarity(Rarity.EPIC).fireResistant(), 10000));
    public static final DeferredItem<SoulItem> STONE_EATER_SOUL = ITEMS.registerItem("stone_eater_soul",
            props -> new SoulItem(props.stacksTo(1).rarity(Rarity.EPIC).fireResistant(), 14000));
    public static final DeferredItem<SoulItem> WALLOWER_SOUL = ITEMS.registerItem("wallower_soul",
            props -> new SoulItem(props.stacksTo(1).rarity(Rarity.EPIC).fireResistant(), 18000));
    public static final DeferredItem<SoulItem> MOTHER_SOUL = ITEMS.registerItem("mother_soul",
            props -> new SoulItem(props.stacksTo(1).rarity(Rarity.EPIC).fireResistant(), 24000));
    public static final DeferredItem<SoulItem> SHATUN_SOUL = ITEMS.registerItem("shatun_soul",
            props -> new SoulItem(props.stacksTo(1).rarity(Rarity.EPIC).fireResistant(), 30000));
    public static final DeferredItem<SoulItem> ELDER_SOUL = ITEMS.registerItem("elder_soul",
            props -> new SoulItem(props.stacksTo(1).rarity(Rarity.EPIC).fireResistant(), 38000));
    public static final DeferredItem<SoulItem> HELMSMAN_SOUL = ITEMS.registerItem("helmsman_soul",
            props -> new SoulItem(props.stacksTo(1).rarity(Rarity.EPIC).fireResistant(), 46000));
    public static final DeferredItem<SoulItem> CAPSIZED_SOUL = ITEMS.registerItem("capsized_soul",
            props -> new SoulItem(props.stacksTo(1).rarity(Rarity.EPIC).fireResistant(), 54000));
    public static final DeferredItem<SoulItem> WARDEN_SOUL = ITEMS.registerItem("warden_soul",
            props -> new SoulItem(props.stacksTo(1).rarity(Rarity.EPIC).fireResistant(), 62000));
    public static final DeferredItem<SoulItem> MILLER_SOUL = ITEMS.registerItem("miller_soul",
            props -> new SoulItem(props.stacksTo(1).rarity(Rarity.EPIC).fireResistant(), 70000));
    public static final DeferredItem<SoulItem> BOLT_SOUL = ITEMS.registerItem("bolt_soul",
            props -> new SoulItem(props.stacksTo(1).rarity(Rarity.EPIC).fireResistant(), 78000));
    public static final DeferredItem<SoulItem> COMING_SOUL = ITEMS.registerItem("coming_soul",
            props -> new SoulItem(props.stacksTo(1).rarity(Rarity.EPIC).fireResistant(), 90000));
    /** Opens the Quarrymen's Gate of the borough: see {@link LockedGateBlock}. */
    public static final DeferredItem<Item> QUARRY_KEY = ITEMS.registerItem("quarry_key", props -> new Item(props.stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant()));
    /** Opens the Mire Gate, at the other end of the same street. */
    public static final DeferredItem<Item> MIRE_KEY = ITEMS.registerItem("mire_key", props -> new Item(props.stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant()));
    /** Opens the wicket in the cliff of the fortress road: the way to the Black Firwood. */
    public static final DeferredItem<Item> FIRWOOD_KEY = ITEMS.registerItem("firwood_key", props -> new Item(props.stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant()));
    /** Opens the Sea Gate, the old south gate of the yard of the first hearth: the way down to the Morning Wharf. */
    public static final DeferredItem<Item> SHORE_KEY = ITEMS.registerItem("shore_key", props -> new Item(props.stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant()));
    /** Opens the Reapers' Gate, in the cliff of the yard before the town's north gate: the way out to the Stubble. */
    public static final DeferredItem<Item> STUBBLE_KEY = ITEMS.registerItem("stubble_key", props -> new Item(props.stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant()));
    /** Opens the Lower Grate, in the south wall of the quarry's sump: the way down to the Sky Beneath. It lies in the Miller's granary. */
    public static final DeferredItem<Item> DEPTHS_KEY = ITEMS.registerItem("depths_key", props -> new Item(props.stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant()));
    public static final DeferredItem<com.keykeeper.dushi.item.PowderPotItem> POWDER_POT = ITEMS.registerItem("powder_pot",
            props -> new com.keykeeper.dushi.item.PowderPotItem(props.stacksTo(16)));
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

    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.PowderPotEntity>> POWDER_POT_ENTITY = ENTITIES.registerEntityType(
            "powder_pot", com.keykeeper.dushi.entity.PowderPotEntity::new, MobCategory.MISC, b -> b.sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.WedgeEntity>> WEDGE_ENTITY = ENTITIES.registerEntityType(
            "thrown_wedge", com.keykeeper.dushi.entity.WedgeEntity::new, MobCategory.MISC, b -> b.sized(0.3F, 0.3F).clientTrackingRange(6).updateInterval(5));

    /** The undead of the stockade, see {@link WitheredEntity.Kind}. */
    public static final Map<WitheredEntity.Kind, DeferredHolder<EntityType<?>, EntityType<WitheredEntity>>> WITHERED = new LinkedHashMap<>();

    static {
        for (WitheredEntity.Kind kind : WitheredEntity.Kind.values()) {
            WITHERED.put(kind, ENTITIES.registerEntityType(kind.id, (type, level) -> new WitheredEntity(type, level, kind), MobCategory.MONSTER,
                    b -> (kind == WitheredEntity.Kind.BUTCHER ? b.sized(0.75F, 1.99F).eyeHeight(1.9F) : b.sized(0.6F, 1.95F).eyeHeight(1.74F)).clientTrackingRange(8)));
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

    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.BlindCrawlerEntity>> BLIND_CRAWLER = ENTITIES.registerEntityType(
            "blind_crawler", com.keykeeper.dushi.entity.BlindCrawlerEntity::new, MobCategory.MONSTER, b -> b.sized(0.9F, 0.9F).eyeHeight(0.6F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.UnhewnEntity>> UNHEWN = ENTITIES.registerEntityType(
            "unhewn", com.keykeeper.dushi.entity.UnhewnEntity::new, MobCategory.MONSTER, b -> b.sized(1.1F, 2.7F).eyeHeight(2.4F).clientTrackingRange(10).fireImmune());
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.TaskmasterEntity>> TASKMASTER = ENTITIES.registerEntityType(
            "taskmaster", com.keykeeper.dushi.entity.TaskmasterEntity::new, MobCategory.MONSTER, b -> b.sized(1.5F, 3.4F).eyeHeight(2.9F).clientTrackingRange(12));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.StoneEaterEntity>> STONE_EATER = ENTITIES.registerEntityType(
            "stone_eater", com.keykeeper.dushi.entity.StoneEaterEntity::new, MobCategory.MONSTER, b -> b.sized(3.0F, 2.7F).eyeHeight(1.9F).clientTrackingRange(12).fireImmune());

    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.BonePikeEntity>> BONE_PIKE = ENTITIES.registerEntityType(
            "bone_pike", com.keykeeper.dushi.entity.BonePikeEntity::new, MobCategory.MONSTER, b -> b.sized(0.9F, 0.8F).eyeHeight(0.5F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.LankyEntity>> LANKY = ENTITIES.registerEntityType(
            "lanky", com.keykeeper.dushi.entity.LankyEntity::new, MobCategory.MONSTER, b -> b.sized(0.7F, 3.3F).eyeHeight(3.0F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.WispEntity>> WISP = ENTITIES.registerEntityType(
            "wisp", com.keykeeper.dushi.entity.WispEntity::new, MobCategory.MONSTER, b -> b.sized(0.6F, 0.8F).eyeHeight(0.5F).clientTrackingRange(10).fireImmune());
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.WallowerEntity>> WALLOWER = ENTITIES.registerEntityType(
            "wallower", com.keykeeper.dushi.entity.WallowerEntity::new, MobCategory.MONSTER, b -> b.sized(3.2F, 1.9F).eyeHeight(1.3F).clientTrackingRange(12));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.WeepingMotherEntity>> WEEPING_MOTHER = ENTITIES.registerEntityType(
            "weeping_mother", com.keykeeper.dushi.entity.WeepingMotherEntity::new, MobCategory.MONSTER, b -> b.sized(2.2F, 5.4F).eyeHeight(4.8F).clientTrackingRange(12));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.MudGlobEntity>> MUD_GLOB_ENTITY = ENTITIES.registerEntityType(
            "mud_glob", com.keykeeper.dushi.entity.MudGlobEntity::new, MobCategory.MISC, b -> b.sized(0.4F, 0.4F).clientTrackingRange(6).updateInterval(5));

    /** The Unsleeping of the Black Firwood and what lives beside them. */
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.WakefulEntity>> WAKEFUL = ENTITIES.registerEntityType(
            "wakeful", com.keykeeper.dushi.entity.WakefulEntity::new, MobCategory.MONSTER, b -> b.sized(0.6F, 1.95F).eyeHeight(1.74F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.WakerEntity>> WAKER = ENTITIES.registerEntityType(
            "waker", com.keykeeper.dushi.entity.WakerEntity::new, MobCategory.MONSTER, b -> b.sized(0.6F, 1.95F).eyeHeight(1.74F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.SleeperEntity>> SLEEPER = ENTITIES.registerEntityType(
            "sleeper", com.keykeeper.dushi.entity.SleeperEntity::new, MobCategory.MONSTER, b -> b.sized(0.6F, 1.95F).eyeHeight(1.6F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.WaxenEntity>> WAXEN = ENTITIES.registerEntityType(
            "waxen", com.keykeeper.dushi.entity.WaxenEntity::new, MobCategory.MONSTER, b -> b.sized(1.3F, 2.3F).eyeHeight(1.9F).clientTrackingRange(10).fireImmune());
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.SleepMothEntity>> SLEEP_MOTH = ENTITIES.registerEntityType(
            "sleep_moth", com.keykeeper.dushi.entity.SleepMothEntity::new, MobCategory.MONSTER, b -> b.sized(0.9F, 1.3F).eyeHeight(0.9F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.ShatunEntity>> SHATUN = ENTITIES.registerEntityType(
            "shatun", com.keykeeper.dushi.entity.ShatunEntity::new, MobCategory.MONSTER, b -> b.sized(2.2F, 2.4F).eyeHeight(1.8F).clientTrackingRange(12));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.ElderEntity>> ELDER = ENTITIES.registerEntityType(
            "elder", com.keykeeper.dushi.entity.ElderEntity::new, MobCategory.MONSTER, b -> b.sized(1.0F, 3.2F).eyeHeight(2.75F).clientTrackingRange(12).fireImmune());

    /** The Morning Wharf: those who were to sail that morning, what lives in the sand and over it, and the two lords. */
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.DockhandEntity>> DOCKHAND = ENTITIES.registerEntityType(
            "dockhand", com.keykeeper.dushi.entity.DockhandEntity::new, MobCategory.MONSTER, b -> b.sized(0.6F, 1.95F).eyeHeight(1.74F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.HarpoonerEntity>> HARPOONER = ENTITIES.registerEntityType(
            "harpooner", com.keykeeper.dushi.entity.HarpoonerEntity::new, MobCategory.MONSTER, b -> b.sized(0.6F, 1.95F).eyeHeight(1.74F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.OarsmanEntity>> OARSMAN = ENTITIES.registerEntityType(
            "oarsman", com.keykeeper.dushi.entity.OarsmanEntity::new, MobCategory.MONSTER, b -> b.sized(0.8F, 2.35F).eyeHeight(2.1F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.PilotEntity>> PILOT = ENTITIES.registerEntityType(
            "pilot", com.keykeeper.dushi.entity.PilotEntity::new, MobCategory.MONSTER, b -> b.sized(0.8F, 2.35F).eyeHeight(2.1F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.SandCrabEntity>> SAND_CRAB = ENTITIES.registerEntityType(
            "sand_crab", com.keykeeper.dushi.entity.SandCrabEntity::new, MobCategory.MONSTER, b -> b.sized(1.0F, 0.7F).eyeHeight(0.5F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.SkuaEntity>> SKUA = ENTITIES.registerEntityType(
            "skua", com.keykeeper.dushi.entity.SkuaEntity::new, MobCategory.MONSTER, b -> b.sized(0.7F, 0.7F).eyeHeight(0.5F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.HelmsmanEntity>> HELMSMAN = ENTITIES.registerEntityType(
            "helmsman", com.keykeeper.dushi.entity.HelmsmanEntity::new, MobCategory.MONSTER, b -> b.sized(1.1F, 2.95F).eyeHeight(2.6F).clientTrackingRange(12));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.CapsizedEntity>> CAPSIZED = ENTITIES.registerEntityType(
            "capsized", com.keykeeper.dushi.entity.CapsizedEntity::new, MobCategory.MONSTER, b -> b.sized(2.6F, 2.0F).eyeHeight(1.2F).clientTrackingRange(12));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.HarpoonEntity>> HARPOON_ENTITY = ENTITIES.registerEntityType(
            "thrown_harpoon", com.keykeeper.dushi.entity.HarpoonEntity::new, MobCategory.MISC, b -> b.sized(0.3F, 0.3F).clientTrackingRange(8).updateInterval(3));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.AnchorEntity>> ANCHOR_ENTITY = ENTITIES.registerEntityType(
            "thrown_anchor", com.keykeeper.dushi.entity.AnchorEntity::new, MobCategory.MISC, b -> b.sized(0.9F, 0.9F).clientTrackingRange(8).updateInterval(1));

    /** The Stubble: those who were to have the corn in by sunset, what stands in the field and has begun to walk, and the two lords. */
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.ReaperEntity>> REAPER = ENTITIES.registerEntityType(
            "reaper", com.keykeeper.dushi.entity.ReaperEntity::new, MobCategory.MONSTER, b -> b.sized(0.6F, 1.75F).eyeHeight(1.5F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.MowerEntity>> MOWER = ENTITIES.registerEntityType(
            "mower", com.keykeeper.dushi.entity.MowerEntity::new, MobCategory.MONSTER, b -> b.sized(0.8F, 2.35F).eyeHeight(2.1F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.SackmanEntity>> SACKMAN = ENTITIES.registerEntityType(
            "sackman", com.keykeeper.dushi.entity.SackmanEntity::new, MobCategory.MONSTER, b -> b.sized(0.8F, 2.1F).eyeHeight(1.8F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.ScorchedEntity>> SCORCHED = ENTITIES.registerEntityType(
            "scorched", com.keykeeper.dushi.entity.ScorchedEntity::new, MobCategory.MONSTER, b -> b.sized(0.9F, 2.9F).eyeHeight(2.6F).clientTrackingRange(8).fireImmune());
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.ScarecrowEntity>> SCARECROW = ENTITIES.registerEntityType(
            "scarecrow", com.keykeeper.dushi.entity.ScarecrowEntity::new, MobCategory.MONSTER, b -> b.sized(0.7F, 2.3F).eyeHeight(2.0F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.StookEntity>> STOOK = ENTITIES.registerEntityType(
            "stook", com.keykeeper.dushi.entity.StookEntity::new, MobCategory.MONSTER, b -> b.sized(1.3F, 2.1F).eyeHeight(1.5F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.SungazerEntity>> SUNGAZER = ENTITIES.registerEntityType(
            "sungazer", com.keykeeper.dushi.entity.SungazerEntity::new, MobCategory.MONSTER, b -> b.sized(0.85F, 3.0F).eyeHeight(2.65F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.ScorchedWardenEntity>> SCORCHED_WARDEN = ENTITIES.registerEntityType(
            "scorched_warden", com.keykeeper.dushi.entity.ScorchedWardenEntity::new, MobCategory.MONSTER, b -> b.sized(1.8F, 5.4F).eyeHeight(4.6F).clientTrackingRange(12).fireImmune());
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.HollowMillerEntity>> HOLLOW_MILLER = ENTITIES.registerEntityType(
            "hollow_miller", com.keykeeper.dushi.entity.HollowMillerEntity::new, MobCategory.MONSTER, b -> b.sized(1.1F, 2.95F).eyeHeight(2.6F).clientTrackingRange(12));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.MillstoneEntity>> MILLSTONE = ENTITIES.registerEntityType(
            "millstone", com.keykeeper.dushi.entity.MillstoneEntity::new, MobCategory.MONSTER, b -> b.sized(1.6F, 1.85F).eyeHeight(0.9F).clientTrackingRange(10).updateInterval(1).fireImmune());
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.FlourSackEntity>> FLOUR_SACK_ENTITY = ENTITIES.registerEntityType(
            "thrown_flour_sack", com.keykeeper.dushi.entity.FlourSackEntity::new, MobCategory.MISC, b -> b.sized(0.4F, 0.4F).clientTrackingRange(8).updateInterval(3));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.SunPatchEntity>> SUN_PATCH = ENTITIES.registerEntityType(
            "sun_patch", com.keykeeper.dushi.entity.SunPatchEntity::new, MobCategory.MISC, b -> b.sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(20).fireImmune());
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.SunShaftEntity>> SUN_SHAFT = ENTITIES.registerEntityType(
            "sun_shaft", com.keykeeper.dushi.entity.SunShaftEntity::new, MobCategory.MISC, b -> b.sized(0.5F, 48.0F).clientTrackingRange(10).updateInterval(40).fireImmune());
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.FlourCloudEntity>> FLOUR_CLOUD = ENTITIES.registerEntityType(
            "flour_cloud", com.keykeeper.dushi.entity.FlourCloudEntity::new, MobCategory.MISC, b -> b.sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(20).fireImmune());

    /**
     * The Sky Beneath, under the black hill: what a day kept too long in the dark has turned into, the two kinds of thing
     * the Founder's rite set there to keep it, and the two lords.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.UsherEntity>> USHER = ENTITIES.registerEntityType(
            "usher", com.keykeeper.dushi.entity.UsherEntity::new, MobCategory.MONSTER, b -> b.sized(0.8F, 3.5F).eyeHeight(3.2F).clientTrackingRange(12));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.StrayShadowEntity>> STRAY_SHADOW = ENTITIES.registerEntityType(
            "stray_shadow", com.keykeeper.dushi.entity.StrayShadowEntity::new, MobCategory.MONSTER, b -> b.sized(0.8F, 1.7F).eyeHeight(0.4F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.LongarmEntity>> LONGARM = ENTITIES.registerEntityType(
            "longarm", com.keykeeper.dushi.entity.LongarmEntity::new, MobCategory.MONSTER, b -> b.sized(0.9F, 2.2F).eyeHeight(1.8F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.HandwalkerEntity>> HANDWALKER = ENTITIES.registerEntityType(
            "handwalker", com.keykeeper.dushi.entity.HandwalkerEntity::new, MobCategory.MONSTER, b -> b.sized(1.3F, 0.9F).eyeHeight(0.6F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.DeeplingEntity>> DEEPLING = ENTITIES.registerEntityType(
            "deepling", com.keykeeper.dushi.entity.DeeplingEntity::new, MobCategory.MONSTER, b -> b.sized(1.0F, 1.0F).eyeHeight(0.6F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.GutEntity>> GUT = ENTITIES.registerEntityType(
            "gut", com.keykeeper.dushi.entity.GutEntity::new, MobCategory.MONSTER, b -> b.sized(2.3F, 2.0F).eyeHeight(1.4F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.ClockhandEntity>> CLOCKHAND = ENTITIES.registerEntityType(
            "clockhand", com.keykeeper.dushi.entity.ClockhandEntity::new, MobCategory.MONSTER, b -> b.sized(0.7F, 2.3F).eyeHeight(2.0F).clientTrackingRange(8).fireImmune());
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.BoltEntity>> BOLT = ENTITIES.registerEntityType(
            "bolt", com.keykeeper.dushi.entity.BoltEntity::new, MobCategory.MONSTER, b -> b.sized(2.2F, 2.6F).eyeHeight(1.4F).clientTrackingRange(12).updateInterval(1).fireImmune());
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.ComingEntity>> COMING = ENTITIES.registerEntityType(
            "coming", com.keykeeper.dushi.entity.ComingEntity::new, MobCategory.MONSTER, b -> b.sized(3.2F, 9.0F).eyeHeight(6.5F).clientTrackingRange(14).fireImmune());
    /** The works of the stopped clock - the Bolt's hall, which is its body: see {@link com.keykeeper.dushi.entity.GearEntity}. */
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.GearEntity>> GEAR = ENTITIES.registerEntityType(
            "gear", com.keykeeper.dushi.entity.GearEntity::new, MobCategory.MISC, b -> b.sized(2.0F, 3.2F).clientTrackingRange(10).updateInterval(1).fireImmune());
    /** What the Coming reaches into its hall with: see {@link com.keykeeper.dushi.entity.TendrilEntity}. */
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.TendrilEntity>> TENDRIL = ENTITIES.registerEntityType(
            "tendril", com.keykeeper.dushi.entity.TendrilEntity::new, MobCategory.MISC, b -> b.sized(1.6F, 7.0F).clientTrackingRange(10).updateInterval(20).fireImmune());
    /** The Coming's disc, where it can be struck: see {@link com.keykeeper.dushi.entity.FaceEntity}. */
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.FaceEntity>> COMING_FACE = ENTITIES.registerEntityType(
            "coming_face", com.keykeeper.dushi.entity.FaceEntity::new, MobCategory.MISC, b -> b.sized(4.2F, 4.2F).clientTrackingRange(10).updateInterval(1).fireImmune());
    /** The eyes that open in the rock when the Coming is out of its crack: see {@link com.keykeeper.dushi.entity.WallEyeEntity}. */
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.WallEyeEntity>> WALL_EYE = ENTITIES.registerEntityType(
            "wall_eye", com.keykeeper.dushi.entity.WallEyeEntity::new, MobCategory.MISC, b -> b.sized(1.0F, 1.0F).clientTrackingRange(10).updateInterval(20).fireImmune());

    /** A root of the Weeping Mother, out of the ground: see {@link com.keykeeper.dushi.entity.RootEntity}. */
    public static final DeferredHolder<EntityType<?>, EntityType<com.keykeeper.dushi.entity.RootEntity>> ROOT = ENTITIES.registerEntityType(
            "root", com.keykeeper.dushi.entity.RootEntity::new, MobCategory.MISC, b -> b.sized(1.2F, 2.6F).clientTrackingRange(8).updateInterval(20).fireImmune());

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
        egg("blind_crawler", BLIND_CRAWLER);
        egg("unhewn", UNHEWN);
        egg("taskmaster", TASKMASTER);
        egg("stone_eater", STONE_EATER);
        egg("bone_pike", BONE_PIKE);
        egg("lanky", LANKY);
        egg("wisp", WISP);
        egg("wallower", WALLOWER);
        egg("weeping_mother", WEEPING_MOTHER);
        egg("wakeful", WAKEFUL);
        egg("waker", WAKER);
        egg("sleeper", SLEEPER);
        egg("waxen", WAXEN);
        egg("sleep_moth", SLEEP_MOTH);
        egg("shatun", SHATUN);
        egg("elder", ELDER);
        egg("dockhand", DOCKHAND);
        egg("harpooner", HARPOONER);
        egg("oarsman", OARSMAN);
        egg("pilot", PILOT);
        egg("sand_crab", SAND_CRAB);
        egg("skua", SKUA);
        egg("helmsman", HELMSMAN);
        egg("capsized", CAPSIZED);
        egg("reaper", REAPER);
        egg("mower", MOWER);
        egg("sackman", SACKMAN);
        egg("scorched", SCORCHED);
        egg("scarecrow", SCARECROW);
        egg("stook", STOOK);
        egg("sungazer", SUNGAZER);
        egg("scorched_warden", SCORCHED_WARDEN);
        egg("hollow_miller", HOLLOW_MILLER);
        egg("usher", USHER);
        egg("stray_shadow", STRAY_SHADOW);
        egg("longarm", LONGARM);
        egg("handwalker", HANDWALKER);
        egg("deepling", DEEPLING);
        egg("gut", GUT);
        egg("clockhand", CLOCKHAND);
        egg("bolt", BOLT);
        egg("coming", COMING);
        egg("keykeeper", KEYKEEPER);
        egg("overseer", OVERSEER);
        egg("chaplain", CHAPLAIN);
    }

    // ------------------------------------------------------------------ creative tab

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.unfading_dawn"))
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
                output.accept(POWDER_POT.get());
                output.accept(IRON_WEDGE.get());
                output.accept(QUARRY_KEY.get());
                output.accept(MIRE_KEY.get());
                output.accept(FIRWOOD_KEY.get());
                output.accept(SHORE_KEY.get());
                output.accept(STUBBLE_KEY.get());
                output.accept(DEPTHS_KEY.get());
                output.accept(NAMELESS_SOUL.get());
                output.accept(GREAT_SOUL.get());
                output.accept(OVERSEER_SOUL.get());
                output.accept(CHAPLAIN_SOUL.get());
                output.accept(GRAVEDIGGER_SOUL.get());
                output.accept(TASKMASTER_SOUL.get());
                output.accept(STONE_EATER_SOUL.get());
                output.accept(WALLOWER_SOUL.get());
                output.accept(MOTHER_SOUL.get());
                output.accept(SHATUN_SOUL.get());
                output.accept(ELDER_SOUL.get());
                output.accept(HELMSMAN_SOUL.get());
                output.accept(CAPSIZED_SOUL.get());
                output.accept(WARDEN_SOUL.get());
                output.accept(MILLER_SOUL.get());
                output.accept(BOLT_SOUL.get());
                output.accept(COMING_SOUL.get());
                WEAPONS.forEach(item -> output.accept(item.get()));
                output.accept(SHORT_BOW.get());
                SHIELDS.forEach(item -> output.accept(item.get()));
                for (ArmorSet set : ArmorSet.values()) {
                    set.pieces().forEach(item -> output.accept(item.get()));
                }
                output.accept(TATTERED_CLOTH.get());
                output.accept(RUST_SCRAP.get());
                output.accept(BONE_SCALE.get());
                output.accept(WITHE.get());
                output.accept(LINEN.get());
                output.accept(WAX.get());
                output.accept(SHATUN_HIDE.get());
                output.accept(SAILCLOTH.get());
                output.accept(RYE_STRAW.get());
                output.accept(FLOUR_SACK.get());
                output.accept(TARRED_ROPE.get());
                output.accept(CRAB_SHELL.get());
                output.accept(SCHOLAR_STAFF.get());
                output.accept(PRAYER_BEADS.get());
                output.accept(EMBER_GLOVE.get());
                SPELLS.values().forEach(item -> output.accept(item.get()));
                CHARMS.values().forEach(item -> output.accept(item.get()));
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
            event.put(BLIND_CRAWLER.get(), com.keykeeper.dushi.entity.BlindCrawlerEntity.createAttributes().build());
            event.put(UNHEWN.get(), com.keykeeper.dushi.entity.UnhewnEntity.createAttributes().build());
            event.put(TASKMASTER.get(), com.keykeeper.dushi.entity.TaskmasterEntity.createAttributes().build());
            event.put(STONE_EATER.get(), com.keykeeper.dushi.entity.StoneEaterEntity.createAttributes().build());
            event.put(BONE_PIKE.get(), com.keykeeper.dushi.entity.BonePikeEntity.createAttributes().build());
            event.put(LANKY.get(), com.keykeeper.dushi.entity.LankyEntity.createAttributes().build());
            event.put(WISP.get(), com.keykeeper.dushi.entity.WispEntity.createAttributes().build());
            event.put(WALLOWER.get(), com.keykeeper.dushi.entity.WallowerEntity.createAttributes().build());
            event.put(WEEPING_MOTHER.get(), com.keykeeper.dushi.entity.WeepingMotherEntity.createAttributes().build());
            event.put(WAKEFUL.get(), com.keykeeper.dushi.entity.WakefulEntity.createAttributes().build());
            event.put(WAKER.get(), com.keykeeper.dushi.entity.WakerEntity.createAttributes().build());
            event.put(SLEEPER.get(), com.keykeeper.dushi.entity.SleeperEntity.createAttributes().build());
            event.put(WAXEN.get(), com.keykeeper.dushi.entity.WaxenEntity.createAttributes().build());
            event.put(SLEEP_MOTH.get(), com.keykeeper.dushi.entity.SleepMothEntity.createAttributes().build());
            event.put(SHATUN.get(), com.keykeeper.dushi.entity.ShatunEntity.createAttributes().build());
            event.put(ELDER.get(), com.keykeeper.dushi.entity.ElderEntity.createAttributes().build());
            event.put(DOCKHAND.get(), com.keykeeper.dushi.entity.DockhandEntity.createAttributes().build());
            event.put(HARPOONER.get(), com.keykeeper.dushi.entity.HarpoonerEntity.createAttributes().build());
            event.put(OARSMAN.get(), com.keykeeper.dushi.entity.OarsmanEntity.createAttributes().build());
            event.put(PILOT.get(), com.keykeeper.dushi.entity.PilotEntity.createAttributes().build());
            event.put(SAND_CRAB.get(), com.keykeeper.dushi.entity.SandCrabEntity.createAttributes().build());
            event.put(SKUA.get(), com.keykeeper.dushi.entity.SkuaEntity.createAttributes().build());
            event.put(HELMSMAN.get(), com.keykeeper.dushi.entity.HelmsmanEntity.createAttributes().build());
            event.put(CAPSIZED.get(), com.keykeeper.dushi.entity.CapsizedEntity.createAttributes().build());
            event.put(REAPER.get(), com.keykeeper.dushi.entity.ReaperEntity.createAttributes().build());
            event.put(MOWER.get(), com.keykeeper.dushi.entity.MowerEntity.createAttributes().build());
            event.put(SACKMAN.get(), com.keykeeper.dushi.entity.SackmanEntity.createAttributes().build());
            event.put(SCORCHED.get(), com.keykeeper.dushi.entity.ScorchedEntity.createAttributes().build());
            event.put(SCARECROW.get(), com.keykeeper.dushi.entity.ScarecrowEntity.createAttributes().build());
            event.put(STOOK.get(), com.keykeeper.dushi.entity.StookEntity.createAttributes().build());
            event.put(SUNGAZER.get(), com.keykeeper.dushi.entity.SungazerEntity.createAttributes().build());
            event.put(SCORCHED_WARDEN.get(), com.keykeeper.dushi.entity.ScorchedWardenEntity.createAttributes().build());
            event.put(HOLLOW_MILLER.get(), com.keykeeper.dushi.entity.HollowMillerEntity.createAttributes().build());
            event.put(MILLSTONE.get(), com.keykeeper.dushi.entity.MillstoneEntity.createAttributes().build());
            event.put(USHER.get(), com.keykeeper.dushi.entity.UsherEntity.createAttributes().build());
            event.put(STRAY_SHADOW.get(), com.keykeeper.dushi.entity.StrayShadowEntity.createAttributes().build());
            event.put(LONGARM.get(), com.keykeeper.dushi.entity.LongarmEntity.createAttributes().build());
            event.put(HANDWALKER.get(), com.keykeeper.dushi.entity.HandwalkerEntity.createAttributes().build());
            event.put(DEEPLING.get(), com.keykeeper.dushi.entity.DeeplingEntity.createAttributes().build());
            event.put(GUT.get(), com.keykeeper.dushi.entity.GutEntity.createAttributes().build());
            event.put(CLOCKHAND.get(), com.keykeeper.dushi.entity.ClockhandEntity.createAttributes().build());
            event.put(BOLT.get(), com.keykeeper.dushi.entity.BoltEntity.createAttributes().build());
            event.put(COMING.get(), com.keykeeper.dushi.entity.ComingEntity.createAttributes().build());
            event.put(KEYKEEPER.get(), KeykeeperEntity.createAttributes().build());
            event.put(OVERSEER.get(), OverseerEntity.createAttributes().build());
            event.put(CHAPLAIN.get(), ChaplainEntity.createAttributes().build());
        });
        modEventBus.addListener((RegisterPayloadHandlersEvent event) -> {
            var registrar = event.registrar("1");
            registrar.playToServer(Net.Roll.TYPE, Net.Roll.CODEC, Net.Roll::handle);
            registrar.playToServer(Net.LevelUp.TYPE, Net.LevelUp.CODEC, Net.LevelUp::handle);
            registrar.playToServer(Net.Travel.TYPE, Net.Travel.CODEC, Net.Travel::handle);
            registrar.playToClient(Net.OpenHearth.TYPE, Net.OpenHearth.CODEC);
            registrar.playToClient(Net.Banner.TYPE, Net.Banner.CODEC);
            registrar.playToClient(Net.OpenShop.TYPE, Net.OpenShop.CODEC);
            registrar.playToServer(Net.Buy.TYPE, Net.Buy.CODEC, Net.Buy::handle);
            registrar.playToClient(Net.OpenForge.TYPE, Net.OpenForge.CODEC);
            registrar.playToServer(Net.Make.TYPE, Net.Make.CODEC, Net.Make::handle);
            registrar.playToServer(Net.Unwear.TYPE, Net.Unwear.CODEC, Net.Unwear::handle);
        });
    }
}
