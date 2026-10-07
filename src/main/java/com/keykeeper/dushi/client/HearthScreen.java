package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Gear;
import com.keykeeper.dushi.Net;
import com.keykeeper.dushi.PlayerSoul;

import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/** Opened by resting at a hearth: shows the player's level and stats and lets them spend souls on a point of any stat. */
public class HearthScreen extends Screen {
    private static final int PANEL_WIDTH = 248;
    private static final int ROW_HEIGHT = 22;
    /** The line of the charm worn, under the stats. */
    private static final int CHARM_ROW = 38;
    private static final int PANEL_HEIGHT = 88 + PlayerSoul.STAT_COUNT * ROW_HEIGHT + CHARM_ROW + 34;
    private static final int FRAME = 0xFF16120E;
    private static final int FRAME_LIGHT = 0xFF6B5A40;
    private static final int BACK = 0xE61A1510;
    private static final int GOLD = 0xFFE8C873;
    private static final int PALE = 0xFFD8D0C0;
    private static final int DIM = 0xFF9A8F7C;
    private static final int SOULS = 0xFF9FE3FF;
    private static final int RED = 0xFFD06A5A;

    /** The hearths the player has rested at, as the server told them when this one's screen was opened. */
    public static List<Net.Stop> stops = List.of();

    private final Button[] plus = new Button[PlayerSoul.STAT_COUNT];
    private Button charmOff;

    public HearthScreen() {
        super(Component.translatable("screen.unfading_dawn.hearth"));
    }

    private int left() {
        return (this.width - PANEL_WIDTH) / 2;
    }

    private int top() {
        return Math.max(4, (this.height - PANEL_HEIGHT) / 2);
    }

    private PlayerSoul soul() {
        return PlayerSoul.of(this.minecraft.player);
    }

    private int charmY() {
        return this.top() + 80 + PlayerSoul.STAT_COUNT * ROW_HEIGHT + 16;
    }

    @Override
    protected void init() {
        int left = this.left();
        int top = this.top();
        for (int stat = 0; stat < PlayerSoul.STAT_COUNT; stat++) {
            int index = stat;
            this.plus[stat] = this.addRenderableWidget(Button.builder(Component.literal("+"),
                    button -> ClientPacketDistributor.sendToServer(new Net.LevelUp(index)))
                    .bounds(left + PANEL_WIDTH - 32, top + 80 + stat * ROW_HEIGHT, 20, 20).build());
        }
        this.charmOff = this.addRenderableWidget(Button.builder(Component.translatable("screen.unfading_dawn.hearth.charm.off"),
                button -> ClientPacketDistributor.sendToServer(new Net.Unwear())).bounds(left + PANEL_WIDTH - 62, this.charmY() - 1, 50, 18).build());
        // from one hearth to another: only when there is another to go to
        Button travel = this.addRenderableWidget(Button.builder(Component.translatable("screen.unfading_dawn.hearth.travel"),
                button -> this.minecraft.gui.setScreen(new TravelScreen())).bounds(left + 12, top + PANEL_HEIGHT - 28, 108, 20).build());
        travel.active = stops.size() > 1;
        this.addRenderableWidget(Button.builder(Component.translatable("screen.unfading_dawn.hearth.leave"), button -> this.onClose())
                .bounds(left + PANEL_WIDTH - 120, top + PANEL_HEIGHT - 28, 108, 20).build());
        this.updateButtons();
    }

    private void updateButtons() {
        if (this.minecraft.player == null) {
            return;
        }
        PlayerSoul soul = this.soul();
        boolean enough = soul.souls >= soul.levelCost() || this.minecraft.player.isCreative();
        for (int stat = 0; stat < PlayerSoul.STAT_COUNT; stat++) {
            if (this.plus[stat] != null) {
                this.plus[stat].active = enough && soul.stats[stat] < PlayerSoul.STAT_CAP;
            }
        }
        if (this.charmOff != null) {
            this.charmOff.visible = com.keykeeper.dushi.Charm.byId(soul.charm) != null;
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.updateButtons();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        if (this.minecraft.player == null) {
            return;
        }
        PlayerSoul soul = this.soul();
        int left = this.left();
        int top = this.top();
        int right = left + PANEL_WIDTH;
        graphics.fill(left - 2, top - 2, right + 2, top + PANEL_HEIGHT + 2, FRAME);
        graphics.fill(left - 1, top - 1, right + 1, top + PANEL_HEIGHT + 1, FRAME_LIGHT);
        graphics.fill(left, top, right, top + PANEL_HEIGHT, BACK);

        graphics.centeredText(this.font, this.title, this.width / 2, top + 8, GOLD);
        graphics.fill(left + 10, top + 21, right - 10, top + 22, FRAME_LIGHT);

        int cost = soul.levelCost();
        graphics.text(this.font, Component.translatable("screen.unfading_dawn.hearth.level", soul.level()), left + 12, top + 28, PALE);
        Component souls = Component.translatable("screen.unfading_dawn.hearth.souls", soul.souls);
        graphics.text(this.font, souls, right - 12 - this.font.width(souls), top + 28, SOULS);
        Component price = Component.translatable("screen.unfading_dawn.hearth.cost", cost);
        graphics.text(this.font, price, left + 12, top + 42, soul.souls >= cost ? PALE : RED);
        // What is worn and held against what can be carried: it decides how the roll goes.
        Gear.Burden burden = Gear.burden(this.minecraft.player);
        Component load = Component.translatable("screen.unfading_dawn.hearth.load", Math.round(Gear.load(this.minecraft.player)),
                Math.round(Gear.maxLoad(soul)), Component.translatable("burden.unfading_dawn." + burden.id));
        graphics.text(this.font, load, left + 12, top + 56, burden == Gear.Burden.OVER ? RED : burden == Gear.Burden.HEAVY ? GOLD : DIM);
        graphics.fill(left + 10, top + 71, right - 10, top + 72, FRAME_LIGHT);

        int hovered = -1;
        for (int stat = 0; stat < PlayerSoul.STAT_COUNT; stat++) {
            int y = top + 80 + stat * ROW_HEIGHT;
            if (mouseX >= left + 6 && mouseX < right - 6 && mouseY >= y - 1 && mouseY < y + 21) {
                hovered = stat;
                graphics.fill(left + 6, y - 1, right - 6, y + 21, 0x30FFFFFF);
            }
            graphics.text(this.font, Component.translatable("stat.unfading_dawn." + PlayerSoul.STAT_KEYS[stat]), left + 12, y + 6, PALE);
            String value = String.valueOf(soul.stats[stat]);
            graphics.text(this.font, value, right - 42 - this.font.width(value), y + 6, soul.stats[stat] >= PlayerSoul.STAT_CAP ? GOLD : PALE);
        }
        // What the stat under the cursor does, in the space above the button.
        String key = hovered >= 0 ? "stat.unfading_dawn." + PlayerSoul.STAT_KEYS[hovered] + ".desc" : "screen.unfading_dawn.hearth.hint";
        graphics.centeredText(this.font, Component.translatable(key), this.width / 2, top + 80 + PlayerSoul.STAT_COUNT * ROW_HEIGHT + 2, DIM);
        // The charm worn: its picture, its name and what it gives; one is worn at a time, and here it can be taken off.
        int charmY = this.charmY();
        graphics.fill(left + 10, charmY - 3, right - 10, charmY - 2, FRAME_LIGHT);
        com.keykeeper.dushi.Charm charm = com.keykeeper.dushi.Charm.byId(soul.charm);
        if (charm == null) {
            graphics.text(this.font, Component.translatable("screen.unfading_dawn.hearth.charm.none"), left + 12, charmY + 4, DIM);
            graphics.text(this.font, Component.translatable("screen.unfading_dawn.hearth.charm.hint"), left + 12, charmY + 21, DIM);
        } else {
            net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(com.keykeeper.dushi.Dushi.CHARMS.get(charm).get());
            graphics.fakeItem(stack, left + 10, charmY);
            graphics.text(this.font, stack.getHoverName(), left + 30, charmY + 4, GOLD);
            graphics.text(this.font, Component.translatable("tooltip.unfading_dawn.charm." + charm.id, charm.amount), left + 12, charmY + 21, DIM);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
