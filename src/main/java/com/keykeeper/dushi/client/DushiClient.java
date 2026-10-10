package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.Gear;
import com.keykeeper.dushi.Net;
import com.keykeeper.dushi.PlayerSoul;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

/** Everything that only exists on the client: renderers, the roll key, the answers to the server's messages. */
@EventBusSubscriber(modid = Dushi.MODID, value = Dist.CLIENT)
public final class DushiClient {
    private static final KeyMapping.Category KEYS = new KeyMapping.Category(Dushi.id("main"));
    public static final KeyMapping ROLL_KEY = new KeyMapping("key.unfading_dawn.roll", InputConstants.KEY_R, KEYS);

    /** Blocks per tick while rolling, and while stepping back (a roll with no direction held). */
    private static final double ROLL_SPEED = 0.46;
    private static final double BACKSTEP_SPEED = 0.36;

    private static Vec3 rollVelocity = Vec3.ZERO;

    private DushiClient() {}

    @SubscribeEvent
    static void registerKeys(RegisterKeyMappingsEvent event) {
        event.registerCategory(KEYS);
        event.register(ROLL_KEY);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Dushi.SKY_LID_ENTITY.get(), SkyLidRenderer::new);
        event.registerEntityRenderer(Dushi.SOUL_STAIN.get(), context -> new ThrownItemRenderer<>(context, 2.2F, true));
        event.registerEntityRenderer(Dushi.SPELL_BOLT.get(), context -> new ThrownItemRenderer<>(context, 1.6F, true));
        event.registerEntityRenderer(Dushi.TAR_POT_ENTITY.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(Dushi.POWDER_POT_ENTITY.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(Dushi.WEDGE_ENTITY.get(), context -> new ThrownItemRenderer<>(context, 1.3F, false));
        event.registerEntityRenderer(Dushi.BLIND_CRAWLER.get(),
                context -> new QuarryRenderer<>(context, new BlindCrawlerModel(context.bakeLayer(BlindCrawlerModel.LAYER)), "blind_crawler", 0.5F, false, false));
        event.registerEntityRenderer(Dushi.UNHEWN.get(),
                context -> new QuarryRenderer<>(context, new UnhewnModel(context.bakeLayer(UnhewnModel.LAYER)), "unhewn", 0.8F, false, true));
        event.registerEntityRenderer(Dushi.TASKMASTER.get(),
                context -> new QuarryRenderer<>(context, new TaskmasterModel(context.bakeLayer(TaskmasterModel.LAYER)), "taskmaster", 1.0F, true, true));
        event.registerEntityRenderer(Dushi.STONE_EATER.get(),
                context -> new QuarryRenderer<>(context, new StoneEaterModel(context.bakeLayer(StoneEaterModel.LAYER)), "stone_eater", 2.0F, true, true));
        event.registerEntityRenderer(Dushi.MUD_GLOB_ENTITY.get(), context -> new ThrownItemRenderer<>(context, 1.8F, false));
        event.registerEntityRenderer(Dushi.ROOT.get(), RootRenderer::new);
        event.registerEntityRenderer(Dushi.BONE_PIKE.get(),
                context -> new QuarryRenderer<>(context, new BonePikeModel(context.bakeLayer(BonePikeModel.LAYER)), "bone_pike", 0.5F, true, true));
        event.registerEntityRenderer(Dushi.LANKY.get(),
                context -> new QuarryRenderer<>(context, new LankyModel(context.bakeLayer(LankyModel.LAYER)), "lanky", 0.5F, true, true));
        event.registerEntityRenderer(Dushi.WISP.get(),
                context -> new QuarryRenderer<>(context, new WispModel(context.bakeLayer(WispModel.LAYER)), "wisp", 0.0F, true, true));
        event.registerEntityRenderer(Dushi.WALLOWER.get(),
                context -> new QuarryRenderer<>(context, new WallowerModel(context.bakeLayer(WallowerModel.LAYER)), "wallower", 2.0F, true, true));
        event.registerEntityRenderer(Dushi.WEEPING_MOTHER.get(),
                context -> new QuarryRenderer<>(context, new WeepingMotherModel(context.bakeLayer(WeepingMotherModel.LAYER)), "weeping_mother", 1.6F, true, true));
        event.registerEntityRenderer(Dushi.WAKEFUL.get(), context -> new QuarryRenderer<>(context,
                new SectModel(context.bakeLayer(SectModel.LAYER), SectModel.Kind.WAKEFUL), "wakeful", 0.5F, true, true));
        event.registerEntityRenderer(Dushi.WAKER.get(), context -> new QuarryRenderer<>(context,
                new SectModel(context.bakeLayer(SectModel.LAYER), SectModel.Kind.WAKER), "waker", 0.5F, true, true));
        event.registerEntityRenderer(Dushi.SLEEPER.get(), context -> new QuarryRenderer<>(context,
                new SectModel(context.bakeLayer(SectModel.LAYER), SectModel.Kind.SLEEPER), "sleeper", 0.5F, false, false));
        event.registerEntityRenderer(Dushi.WAXEN.get(),
                context -> new QuarryRenderer<>(context, new WaxenModel(context.bakeLayer(WaxenModel.LAYER)), "waxen", 0.9F, true, true));
        event.registerEntityRenderer(Dushi.SLEEP_MOTH.get(),
                context -> new QuarryRenderer<>(context, new MothModel(context.bakeLayer(MothModel.LAYER)), "sleep_moth", 0.0F, true, true));
        event.registerEntityRenderer(Dushi.SHATUN.get(),
                context -> new QuarryRenderer<>(context, new ShatunModel(context.bakeLayer(ShatunModel.LAYER)), "shatun", 1.6F, true, true));
        event.registerEntityRenderer(Dushi.ELDER.get(),
                context -> new QuarryRenderer<>(context, new ElderModel(context.bakeLayer(ElderModel.LAYER)), "elder", 0.8F, "elder_glow", "elder_asleep_glow"));
        event.registerEntityRenderer(Dushi.DOCKHAND.get(), context -> new QuarryRenderer<>(context,
                new SailorModel(context.bakeLayer(SailorModel.LAYER), SailorModel.Kind.DOCKHAND), "dockhand", 0.5F, false, false));
        event.registerEntityRenderer(Dushi.HARPOONER.get(), context -> new QuarryRenderer<>(context,
                new SailorModel(context.bakeLayer(SailorModel.LAYER), SailorModel.Kind.HARPOONER), "harpooner", 0.5F, false, false));
        event.registerEntityRenderer(Dushi.OARSMAN.get(), context -> new QuarryRenderer<>(context,
                new SailorModel(context.bakeLayer(SailorModel.BIG), SailorModel.Kind.OARSMAN), "oarsman", 0.65F, false, false));
        event.registerEntityRenderer(Dushi.PILOT.get(), context -> new QuarryRenderer<>(context,
                new SailorModel(context.bakeLayer(SailorModel.BIG), SailorModel.Kind.PILOT), "pilot", 0.65F, true, true));
        event.registerEntityRenderer(Dushi.HELMSMAN.get(), context -> new QuarryRenderer<>(context,
                new SailorModel(context.bakeLayer(SailorModel.HUGE), SailorModel.Kind.HELMSMAN), "helmsman", 0.9F, true, true));
        event.registerEntityRenderer(Dushi.SAND_CRAB.get(),
                context -> new QuarryRenderer<>(context, new CrabModel(context.bakeLayer(CrabModel.LAYER)), "sand_crab", 0.5F, false, false));
        event.registerEntityRenderer(Dushi.SKUA.get(),
                context -> new QuarryRenderer<>(context, new SkuaModel(context.bakeLayer(SkuaModel.LAYER)), "skua", 0.3F, false, false));
        event.registerEntityRenderer(Dushi.CAPSIZED.get(),
                context -> new QuarryRenderer<>(context, new CapsizedModel(context.bakeLayer(CapsizedModel.LAYER)), "capsized", 2.0F, false, true));
        event.registerEntityRenderer(Dushi.REAPER.get(), context -> new QuarryRenderer<>(context,
                new FieldhandModel(context.bakeLayer(FieldhandModel.LAYER), FieldhandModel.Kind.REAPER), "reaper", 0.5F, false, false));
        event.registerEntityRenderer(Dushi.MOWER.get(), context -> new QuarryRenderer<>(context,
                new FieldhandModel(context.bakeLayer(FieldhandModel.BIG), FieldhandModel.Kind.MOWER), "mower", 0.65F, false, false));
        event.registerEntityRenderer(Dushi.SACKMAN.get(), context -> new QuarryRenderer<>(context,
                new FieldhandModel(context.bakeLayer(FieldhandModel.BIG), FieldhandModel.Kind.SACKMAN), "sackman", 0.65F, false, false));
        event.registerEntityRenderer(Dushi.SCORCHED.get(), context -> new QuarryRenderer<>(context,
                new FieldhandModel(context.bakeLayer(FieldhandModel.HUGE), FieldhandModel.Kind.SCORCHED), "scorched", 0.75F, true, true));
        event.registerEntityRenderer(Dushi.HOLLOW_MILLER.get(), context -> new QuarryRenderer<>(context,
                new FieldhandModel(context.bakeLayer(FieldhandModel.HUGE), FieldhandModel.Kind.MILLER), "hollow_miller", 0.9F, true, true));
        event.registerEntityRenderer(Dushi.SCARECROW.get(),
                context -> new QuarryRenderer<>(context, new ScarecrowModel(context.bakeLayer(ScarecrowModel.LAYER), false), "scarecrow", 0.4F, false, false));
        event.registerEntityRenderer(Dushi.SCORCHED_WARDEN.get(),
                context -> new QuarryRenderer<>(context, new ScarecrowModel(context.bakeLayer(ScarecrowModel.GIANT), true), "scorched_warden", 1.4F, false, true));
        event.registerEntityRenderer(Dushi.STOOK.get(),
                context -> new QuarryRenderer<>(context, new StookModel(context.bakeLayer(StookModel.LAYER)), "stook", 0.9F, false, false));
        event.registerEntityRenderer(Dushi.SUNGAZER.get(),
                context -> new QuarryRenderer<>(context, new SungazerModel(context.bakeLayer(SungazerModel.LAYER)), "sungazer", 0.45F, true, true));
        event.registerEntityRenderer(Dushi.USHER.get(),
                context -> new QuarryRenderer<>(context, new UsherModel(context.bakeLayer(UsherModel.LAYER)), "usher", 0.4F, true, true));
        event.registerEntityRenderer(Dushi.STRAY_SHADOW.get(),
                context -> new QuarryRenderer<>(context, new StrayShadowModel(context.bakeLayer(StrayShadowModel.LAYER)), "stray_shadow", 0.0F, true, true));
        event.registerEntityRenderer(Dushi.LONGARM.get(),
                context -> new QuarryRenderer<>(context, new LongarmModel(context.bakeLayer(LongarmModel.LAYER)), "longarm", 0.0F, true, true));
        event.registerEntityRenderer(Dushi.HANDWALKER.get(),
                context -> new QuarryRenderer<>(context, new HandwalkerModel(context.bakeLayer(HandwalkerModel.LAYER)), "handwalker", 0.7F, true, true));
        event.registerEntityRenderer(Dushi.DEEPLING.get(),
                context -> new QuarryRenderer<>(context, new DeeplingModel(context.bakeLayer(DeeplingModel.LAYER)), "deepling", 0.4F, true, true));
        event.registerEntityRenderer(Dushi.GUT.get(),
                context -> new QuarryRenderer<>(context, new GutModel(context.bakeLayer(GutModel.LAYER)), "gut", 1.4F, true, true));
        event.registerEntityRenderer(Dushi.CLOCKHAND.get(),
                context -> new QuarryRenderer<>(context, new ClockhandModel(context.bakeLayer(ClockhandModel.LAYER)), "clockhand", 0.4F, true, true));
        event.registerEntityRenderer(Dushi.BOLT.get(),
                context -> new QuarryRenderer<>(context, new BoltModel(context.bakeLayer(BoltModel.LAYER)), "bolt", 1.2F, true, true));
        event.registerEntityRenderer(Dushi.COMING.get(),
                context -> new QuarryRenderer<>(context, new ComingModel(context.bakeLayer(ComingModel.LAYER)), "coming", 0.0F, true, true));
        event.registerEntityRenderer(Dushi.MILLSTONE.get(),
                context -> new QuarryRenderer<>(context, new MillstoneModel(context.bakeLayer(MillstoneModel.LAYER)), "millstone", 0.8F, false, false));
        event.registerEntityRenderer(Dushi.FLOUR_SACK_ENTITY.get(), context -> new ThrownItemRenderer<>(context, 1.4F, false));
        event.registerEntityRenderer(Dushi.FLOUR_CLOUD.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
        event.registerEntityRenderer(Dushi.SUN_PATCH.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
        event.registerEntityRenderer(Dushi.SUN_SHAFT.get(), SunShaftRenderer::new);
        event.registerEntityRenderer(Dushi.GEAR.get(), GearRenderer::new);
        event.registerEntityRenderer(Dushi.TENDRIL.get(), TendrilRenderer::new);
        event.registerEntityRenderer(Dushi.HARPOON_ENTITY.get(), context -> new ThrownItemRenderer<>(context, 1.6F, false));
        event.registerEntityRenderer(Dushi.ANCHOR_ENTITY.get(), AnchorRenderer::new);
        Dushi.WITHERED.forEach((kind, type) -> event.registerEntityRenderer(type.get(), context -> new WitheredRenderer(context, kind)));
        event.registerEntityRenderer(Dushi.RUST_KNIGHT.get(), context -> new RustKnightRenderer(context, "rust_knight"));
        event.registerEntityRenderer(Dushi.GILDED_KNIGHT.get(), context -> new RustKnightRenderer(context, "gilded_knight"));
        event.registerEntityRenderer(Dushi.CARRION_HOUND.get(), CarrionHoundRenderer::new);
        event.registerEntityRenderer(Dushi.GRAVEDIGGER.get(), GravediggerRenderer::new);
        event.registerEntityRenderer(Dushi.KEYKEEPER.get(), KeykeeperRenderer::new);
        event.registerEntityRenderer(Dushi.OVERSEER.get(), OverseerRenderer::new);
        event.registerEntityRenderer(Dushi.CHAPLAIN.get(), ChaplainRenderer::new);
    }

    @SubscribeEvent
    static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WitheredModel.LAYER, WitheredModel::createLayer);
        event.registerLayerDefinition(RustKnightModel.LAYER, RustKnightModel::createLayer);
        event.registerLayerDefinition(CarrionHoundModel.LAYER, CarrionHoundModel::createLayer);
        event.registerLayerDefinition(GravediggerModel.LAYER, GravediggerModel::createLayer);
        event.registerLayerDefinition(BlindCrawlerModel.LAYER, BlindCrawlerModel::createLayer);
        event.registerLayerDefinition(UnhewnModel.LAYER, UnhewnModel::createLayer);
        event.registerLayerDefinition(TaskmasterModel.LAYER, TaskmasterModel::createLayer);
        event.registerLayerDefinition(StoneEaterModel.LAYER, StoneEaterModel::createLayer);
        event.registerLayerDefinition(BonePikeModel.LAYER, BonePikeModel::createLayer);
        event.registerLayerDefinition(LankyModel.LAYER, LankyModel::createLayer);
        event.registerLayerDefinition(WispModel.LAYER, WispModel::createLayer);
        event.registerLayerDefinition(WallowerModel.LAYER, WallowerModel::createLayer);
        event.registerLayerDefinition(WeepingMotherModel.LAYER, WeepingMotherModel::createLayer);
        event.registerLayerDefinition(RootModel.LAYER, RootModel::createLayer);
        event.registerLayerDefinition(AnchorModel.LAYER, AnchorModel::createLayer);
        event.registerLayerDefinition(SunShaftModel.LAYER, SunShaftModel::createLayer);
        event.registerLayerDefinition(SectModel.LAYER, SectModel::createLayer);
        event.registerLayerDefinition(WaxenModel.LAYER, WaxenModel::createLayer);
        event.registerLayerDefinition(MothModel.LAYER, MothModel::createLayer);
        event.registerLayerDefinition(ShatunModel.LAYER, ShatunModel::createLayer);
        event.registerLayerDefinition(ElderModel.LAYER, ElderModel::createLayer);
        event.registerLayerDefinition(SailorModel.LAYER, SailorModel::createLayer);
        event.registerLayerDefinition(SailorModel.BIG, SailorModel::createBigLayer);
        event.registerLayerDefinition(SailorModel.HUGE, SailorModel::createHugeLayer);
        event.registerLayerDefinition(CrabModel.LAYER, CrabModel::createLayer);
        event.registerLayerDefinition(SkuaModel.LAYER, SkuaModel::createLayer);
        event.registerLayerDefinition(CapsizedModel.LAYER, CapsizedModel::createLayer);
        event.registerLayerDefinition(FieldhandModel.LAYER, FieldhandModel::createLayer);
        event.registerLayerDefinition(FieldhandModel.BIG, FieldhandModel::createBigLayer);
        event.registerLayerDefinition(FieldhandModel.HUGE, FieldhandModel::createHugeLayer);
        event.registerLayerDefinition(ScarecrowModel.LAYER, ScarecrowModel::createLayer);
        event.registerLayerDefinition(ScarecrowModel.GIANT, ScarecrowModel::createGiantLayer);
        event.registerLayerDefinition(StookModel.LAYER, StookModel::createLayer);
        event.registerLayerDefinition(SungazerModel.LAYER, SungazerModel::createLayer);
        event.registerLayerDefinition(UsherModel.LAYER, UsherModel::createLayer);
        event.registerLayerDefinition(StrayShadowModel.LAYER, StrayShadowModel::createLayer);
        event.registerLayerDefinition(LongarmModel.LAYER, LongarmModel::createLayer);
        event.registerLayerDefinition(HandwalkerModel.LAYER, HandwalkerModel::createLayer);
        event.registerLayerDefinition(DeeplingModel.LAYER, DeeplingModel::createLayer);
        event.registerLayerDefinition(GutModel.LAYER, GutModel::createLayer);
        event.registerLayerDefinition(ClockhandModel.LAYER, ClockhandModel::createLayer);
        event.registerLayerDefinition(BoltModel.LAYER, BoltModel::createLayer);
        event.registerLayerDefinition(ComingModel.LAYER, ComingModel::createLayer);
        event.registerLayerDefinition(GearModel.LAYER, GearModel::createLayer);
        event.registerLayerDefinition(TendrilModel.LAYER, TendrilModel::createLayer);
        event.registerLayerDefinition(MillstoneModel.LAYER, MillstoneModel::createLayer);
        event.registerLayerDefinition(KeykeeperModel.LAYER, KeykeeperModel::createLayer);
        event.registerLayerDefinition(OverseerModel.LAYER, OverseerModel::createLayer);
        event.registerLayerDefinition(ChaplainModel.LAYER, ChaplainModel::createLayer);
    }

    @SubscribeEvent
    static void registerPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(Net.OpenHearth.TYPE, (payload, context) -> {
            HearthScreen.stops = payload.stops();
            Minecraft.getInstance().gui.setScreen(new HearthScreen());
        });
        event.register(Net.OpenShop.TYPE, (payload, context) -> Minecraft.getInstance().gui.setScreen(new ShopScreen()));
        event.register(Net.OpenForge.TYPE, (payload, context) -> Minecraft.getInstance().gui.setScreen(new ForgeScreen()));
        event.register(Net.Banner.TYPE, (payload, context) -> {
            if (!payload.key().isEmpty()) {
                SoulsHud.showBanner(payload.key(), payload.style());
            }
            if (!payload.sound().isEmpty()) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(Dushi.sound(payload.sound()), 1.0F, 1.0F));
            }
        });
    }

    /** The roll: the player's own client moves them, the server is only told so it can charge stamina and ignore blows. */
    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }
        PlayerSoul soul = PlayerSoul.of(player);
        while (ROLL_KEY.consumeClick()) {
            Gear.Burden burden = Gear.burden(player);
            if (minecraft.gui.screen() != null || player.isSpectator() || player.isPassenger() || !player.onGround() || player.isInWater()
                    || soul.rollCooldown > 0 || (soul.stamina <= 0.0F && !player.isCreative())) {
                continue;
            }
            if (!burden.canRoll()) {
                player.sendOverlayMessage(Component.translatable("message.unfading_dawn.overburdened"));
                continue;
            }
            Vec2 input = player.input.getMoveVector();
            float yaw = player.getYRot() * (float) (Math.PI / 180.0);
            double sin = Math.sin(yaw);
            double cos = Math.cos(yaw);
            Vec3 direction = new Vec3(input.x * cos - input.y * sin, 0.0, input.y * cos + input.x * sin);
            if (direction.lengthSqr() < 1.0E-4) {
                rollVelocity = new Vec3(sin, 0.0, -cos).scale(BACKSTEP_SPEED * burden.rollSpeed);
            } else {
                rollVelocity = direction.normalize().scale(ROLL_SPEED * burden.rollSpeed);
            }
            if (!player.isCreative()) {
                soul.spend(Gear.rollCost(player, burden));
            }
            soul.rollTicks = Net.ROLL_TICKS;
            soul.rollCooldown = burden.rollCooldown;
            player.setForcedPose(Pose.SWIMMING);
            player.setSprinting(false);
            ClientPacketDistributor.sendToServer(new Net.Roll());
        }
        if (soul.rollTicks > 2) {
            player.setDeltaMovement(rollVelocity.x, player.getDeltaMovement().y, rollVelocity.z);
        }
    }
}
