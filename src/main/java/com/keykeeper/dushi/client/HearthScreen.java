package com.keykeeper.dushi.client;

import com.keykeeper.dushi.Gear;
import com.keykeeper.dushi.Net;
import com.keykeeper.dushi.PlayerSoul;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/** Opened by resting at a hearth: shows the player's level and stats and lets them spend souls on a point of any stat. */
public class HearthScreen extends Screen {
    private static final int PANEL_WIDTH = 248;
    private static final int ROW_HEIGHT = 22;
    private static final int PANEL_HEIGHT = 74 + PlayerSoul.STAT_COUNT * ROW_HEIGHT + 34;
    private static final int FRAME = 0xFF16120E;
    private static final int FRAME_LIGHT = 0xFF6B5A40;
    private static final int BACK = 0xE61A1510;
    private static final int GOLD = 0xFFE8C873;
    private static final int PALE = 0xFFD8D0C0;
    private static final int DIM = 0xFF9A8F7C;
    private static final int SOULS = 0xFF9FE3FF;
    private static final int RED = 0xFFD06A5A;

    private final Button[] plus = new Button[PlayerSoul.STAT_COUNT];

    public HearthScreen() {
        super(Component.translatable("screen.vsratie_dushi.hearth"));
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

    @Override
    protected void init() {
        int left = this.left();
        int top = this.top();
        for (int stat = 0; stat < PlayerSoul.STAT_COUNT; stat++) {
            int index = stat;
            this.plus[stat] = this.addRenderableWidget(Button.builder(Component.literal("+"),
                    button -> ClientPacketDistributor.sendToServer(new Net.LevelUp(index)))
                    .bounds(left + PANEL_WIDTH - 32, top + 66 + stat * ROW_HEIGHT, 20, 20).build());
        }
        this.addRenderableWidget(Button.builder(Component.translatable("screen.vsratie_dushi.hearth.leave"), button -> this.onClose())
                .bounds(left + PANEL_WIDTH / 2 - 60, top + PANEL_HEIGHT - 28, 120, 20).build());
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
        graphics.text(this.font, Component.translatable("screen.vsratie_dushi.hearth.level", soul.level()), left + 12, top + 28, PALE);
        Component souls = Component.translatable("screen.vsratie_dushi.hearth.souls", soul.souls);
        graphics.text(this.font, souls, right - 12 - this.font.width(souls), top + 28, SOULS);
        Component price = Component.translatable("screen.vsratie_dushi.hearth.cost", cost);
        graphics.text(this.font, price, left + 12, top + 42, soul.souls >= cost ? PALE : RED);
        // What is worn and held against what can be carried: it decides how the roll goes.
        Gear.Burden burden = Gear.burden(this.minecraft.player);
        Component load = Component.translatable("screen.vsratie_dushi.hearth.load", Math.round(Gear.load(this.minecraft.player)),
                Math.round(Gear.maxLoad(soul)), Component.translatable("burden.vsratie_dushi." + burden.id));
        graphics.text(this.font, load, right - 12 - this.font.width(load), top + 42, burden == Gear.Burden.OVER ? RED : burden == Gear.Burden.HEAVY ? GOLD : DIM);
        graphics.fill(left + 10, top + 57, right - 10, top + 58, FRAME_LIGHT);

        int hovered = -1;
        for (int stat = 0; stat < PlayerSoul.STAT_COUNT; stat++) {
            int y = top + 66 + stat * ROW_HEIGHT;
            if (mouseX >= left + 6 && mouseX < right - 6 && mouseY >= y - 1 && mouseY < y + 21) {
                hovered = stat;
                graphics.fill(left + 6, y - 1, right - 6, y + 21, 0x30FFFFFF);
            }
            graphics.text(this.font, Component.translatable("stat.vsratie_dushi." + PlayerSoul.STAT_KEYS[stat]), left + 12, y + 6, PALE);
            String value = String.valueOf(soul.stats[stat]);
            graphics.text(this.font, value, right - 42 - this.font.width(value), y + 6, soul.stats[stat] >= PlayerSoul.STAT_CAP ? GOLD : PALE);
        }
        // What the stat under the cursor does, in the space above the button.
        String key = hovered >= 0 ? "stat.vsratie_dushi." + PlayerSoul.STAT_KEYS[hovered] + ".desc" : "screen.vsratie_dushi.hearth.hint";
        graphics.centeredText(this.font, Component.translatable(key), this.width / 2, top + 66 + PlayerSoul.STAT_COUNT * ROW_HEIGHT + 2, DIM);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
