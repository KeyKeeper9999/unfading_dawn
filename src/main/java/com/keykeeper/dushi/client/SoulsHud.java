package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Dushi;
import com.keykeeper.dushi.PlayerSoul;
import com.keykeeper.dushi.SoulMechanics;
import com.keykeeper.dushi.item.FlaskItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * What the mod adds to the screen: the stamina bar in the top left corner, the souls carried and the flask in the bottom
 * right one, and the big lines of text that mark a victory or a lit hearth.
 */
@EventBusSubscriber(modid = Dushi.MODID, value = Dist.CLIENT)
public final class SoulsHud {
    private static final int BAR_X = 10;
    private static final int BAR_Y = 10;
    private static final int BAR_HEIGHT = 5;
    /** Pixels of bar per point of stamina, and the longest the bar may get. */
    private static final float BAR_SCALE = 0.9F;
    private static final int BAR_MAX_WIDTH = 240;
    private static final int FRAME = 0xFF16120E;
    private static final int FRAME_LIGHT = 0xFF6B5A40;
    private static final int BAR_BACK = 0xC0100C08;
    private static final int STAMINA = 0xFF4E9E3C;
    private static final int STAMINA_LIGHT = 0xFF7FCB5E;
    private static final int STAMINA_LOW = 0xFFB8862B;
    private static final int STAMINA_TRAIL = 0xFFD9C36A;

    private static final int[] BANNER_COLORS = {0xE8C873, 0xD2ECFF, 0xB01A1A};
    private static final int BANNER_IN = 8;
    private static final int BANNER_HOLD = 56;
    private static final int BANNER_OUT = 24;

    private static String bannerKey;
    private static int bannerStyle;
    private static int bannerAge;

    /** The yellow tail that shows how much stamina has just been spent. */
    private static float trail;
    private static int shownSouls = -1;
    private static int gained;
    private static int gainedAge;

    private SoulsHud() {}

    public static void showBanner(String key, int style) {
        bannerKey = key;
        bannerStyle = Mth.clamp(style, 0, BANNER_COLORS.length - 1);
        bannerAge = 0;
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            shownSouls = -1;
            bannerKey = null;
            return;
        }
        if (minecraft.isPaused()) {
            return;
        }
        if (bannerKey != null && ++bannerAge > BANNER_IN + BANNER_HOLD + BANNER_OUT) {
            bannerKey = null;
        }
        PlayerSoul soul = PlayerSoul.of(player);
        trail = soul.stamina >= trail ? soul.stamina : Math.max(soul.stamina, trail - 2.5F);
        // The counter runs up to the real number instead of jumping, and the last gain floats above it for a while.
        if (shownSouls < 0) {
            shownSouls = soul.souls;
        } else if (shownSouls != soul.souls) {
            int difference = soul.souls - shownSouls;
            if (difference > 0) {
                if (gainedAge > 40) {
                    gained = 0;
                }
                gained += Math.max(1, Math.min(difference, Math.max(1, difference / 6)));
                gainedAge = 0;
            } else {
                gained = 0;
            }
            int step = Math.max(1, Math.abs(difference) / 6);
            shownSouls += difference > 0 ? Math.min(step, difference) : Math.max(-step, difference);
        }
        if (gained > 0 && ++gainedAge > 60 && shownSouls == soul.souls) {
            gained = 0;
        }
    }

    @SubscribeEvent
    static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.gui.hud.isHidden()) {
            return;
        }
        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        PlayerSoul soul = PlayerSoul.of(player);
        if (SoulMechanics.plays(player)) {
            staminaBar(graphics, soul);
        }
        if (!player.isSpectator()) {
            souls(graphics, minecraft.font, player, soul);
        }
        if (bannerKey != null) {
            banner(graphics, minecraft.font, event.getPartialTick().getGameTimeDeltaPartialTick(true));
        }
    }

    private static void staminaBar(GuiGraphicsExtractor graphics, PlayerSoul soul) {
        float max = soul.maxStamina();
        int width = Math.min(BAR_MAX_WIDTH, Math.round(max * BAR_SCALE));
        int filled = Math.round(width * Mth.clamp(soul.stamina / max, 0.0F, 1.0F));
        int tail = Math.round(width * Mth.clamp(trail / max, 0.0F, 1.0F));
        graphics.fill(BAR_X - 2, BAR_Y - 2, BAR_X + width + 2, BAR_Y + BAR_HEIGHT + 2, FRAME);
        graphics.fill(BAR_X - 1, BAR_Y - 1, BAR_X + width + 1, BAR_Y + BAR_HEIGHT + 1, FRAME_LIGHT);
        graphics.fill(BAR_X, BAR_Y, BAR_X + width, BAR_Y + BAR_HEIGHT, BAR_BACK);
        if (tail > filled) {
            graphics.fill(BAR_X + filled, BAR_Y, BAR_X + tail, BAR_Y + BAR_HEIGHT, STAMINA_TRAIL);
        }
        if (filled > 0) {
            boolean low = soul.stamina < 20.0F;
            graphics.fill(BAR_X, BAR_Y, BAR_X + filled, BAR_Y + BAR_HEIGHT, low ? STAMINA_LOW : STAMINA);
            graphics.fill(BAR_X, BAR_Y, BAR_X + filled, BAR_Y + 1, low ? STAMINA_TRAIL : STAMINA_LIGHT);
        }
    }

    private static void souls(GuiGraphicsExtractor graphics, Font font, LocalPlayer player, PlayerSoul soul) {
        int right = graphics.guiWidth() - 8;
        int bottom = graphics.guiHeight() - 8;
        String text = String.valueOf(Math.max(0, shownSouls));
        int boxWidth = Math.max(54, font.width(text) + 26);
        int left = right - boxWidth;
        int top = bottom - 16;
        graphics.fill(left - 1, top - 1, right + 1, bottom + 1, FRAME);
        graphics.fill(left, top, right, bottom, 0xB0100C08);
        graphics.fill(left, top, right, top + 1, FRAME_LIGHT);
        graphics.fill(left, bottom - 1, right, bottom, FRAME_LIGHT);
        graphics.fakeItem(new ItemStack(Dushi.SOUL_STAIN_ITEM.get()), left + 1, top);
        graphics.text(font, text, right - 4 - font.width(text), top + 4, 0xFFE8F4FF, true);
        if (gained > 0) {
            String plus = "+" + gained;
            int alpha = gainedAge < 45 ? 255 : Math.max(0, 255 - (gainedAge - 45) * 17);
            if (alpha > 8) {
                graphics.text(font, plus, right - 4 - font.width(plus), top - 11, (alpha << 24) | 0x9FE3FF, true);
            }
        }
        // The charm worn, to the left of where the flask is.
        com.keykeeper.dushi.Charm charm = com.keykeeper.dushi.Charm.byId(soul.charm);
        if (charm != null) {
            int x = left - 44;
            graphics.fill(x - 2, top - 1, x + 18, bottom + 1, FRAME);
            graphics.fill(x - 1, top, x + 17, bottom, 0xB0100C08);
            graphics.fakeItem(new ItemStack(Dushi.CHARMS.get(charm).get()), x, top);
        }
        // The flask and its gulps, to the left of the souls.
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof FlaskItem) {
                int x = left - 22;
                graphics.fill(x - 2, top - 1, x + 18, bottom + 1, FRAME);
                graphics.fill(x - 1, top, x + 17, bottom, 0xB0100C08);
                graphics.fakeItem(stack, x, top);
                String count = String.valueOf(FlaskItem.charges(stack));
                graphics.text(font, count, x + 17 - font.width(count), top + 8, FlaskItem.charges(stack) > 0 ? 0xFFFFC870 : 0xFF8A8A8A, true);
                break;
            }
        }
        if (soul.lostSouls > 0) {
            String lost = Component.translatable("hud.vsratie_dushi.lost", soul.lostSouls).getString();
            graphics.text(font, lost, right - font.width(lost), bottom + 0 - 28 - (gained > 0 ? 10 : 0), 0xC0B8C8D8, true);
        }
    }

    private static void banner(GuiGraphicsExtractor graphics, Font font, float partialTick) {
        float age = bannerAge + partialTick;
        float alpha;
        if (age < BANNER_IN) {
            alpha = age / BANNER_IN;
        } else if (age < BANNER_IN + BANNER_HOLD) {
            alpha = 1.0F;
        } else {
            alpha = 1.0F - (age - BANNER_IN - BANNER_HOLD) / BANNER_OUT;
        }
        alpha = Mth.clamp(alpha, 0.0F, 1.0F);
        if (alpha <= 0.03F) {
            return;
        }
        int width = graphics.guiWidth();
        int centreY = graphics.guiHeight() * 2 / 5;
        int shade = (int) (alpha * 150.0F);
        // A dark band that fades out towards its upper and lower edges.
        graphics.fillGradient(0, centreY - 30, width, centreY - 8, 0x00000000, shade << 24);
        graphics.fill(0, centreY - 8, width, centreY + 14, shade << 24);
        graphics.fillGradient(0, centreY + 14, width, centreY + 36, shade << 24, 0x00000000);
        Component text = Component.translatable(bannerKey);
        float scale = 2.5F + 0.25F * Math.min(1.0F, age / (BANNER_IN + BANNER_HOLD));
        int color = ((int) (alpha * 255.0F) << 24) | BANNER_COLORS[bannerStyle];
        graphics.pose().pushMatrix();
        graphics.pose().translate(width / 2.0F, centreY + 3.0F);
        graphics.pose().scale(scale, scale);
        graphics.text(font, text, -font.width(text) / 2, -font.lineHeight / 2, color, true);
        graphics.pose().popMatrix();
    }
}
