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
    public static final KeyMapping ROLL_KEY = new KeyMapping("key.vsratie_dushi.roll", InputConstants.KEY_R, KEYS);

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
        event.registerEntityRenderer(Dushi.SOUL_STAIN.get(), context -> new ThrownItemRenderer<>(context, 2.2F, true));
        event.registerEntityRenderer(Dushi.SPELL_BOLT.get(), context -> new ThrownItemRenderer<>(context, 1.6F, true));
        event.registerEntityRenderer(Dushi.TAR_POT_ENTITY.get(), ThrownItemRenderer::new);
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
        event.registerLayerDefinition(KeykeeperModel.LAYER, KeykeeperModel::createLayer);
        event.registerLayerDefinition(OverseerModel.LAYER, OverseerModel::createLayer);
        event.registerLayerDefinition(ChaplainModel.LAYER, ChaplainModel::createLayer);
    }

    @SubscribeEvent
    static void registerPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(Net.OpenHearth.TYPE, (payload, context) -> Minecraft.getInstance().gui.setScreen(new HearthScreen()));
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
                player.sendOverlayMessage(Component.translatable("message.vsratie_dushi.overburdened"));
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
                soul.spend(burden.rollCost);
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
