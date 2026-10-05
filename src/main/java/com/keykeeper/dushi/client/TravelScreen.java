package com.keykeeper.dushi.client;

import java.util.List;

import com.keykeeper.dushi.Net;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/** Opened from the hearth's screen: the hearths the player has rested at, and a way to any of them. */
public class TravelScreen extends Screen {
    private static final int PANEL_WIDTH = 248;
    private static final int ROW_HEIGHT = 22;
    private static final int ROWS = 7;
    private static final int PANEL_HEIGHT = 34 + ROWS * ROW_HEIGHT + 50;
    private static final int FRAME = 0xFF16120E;
    private static final int FRAME_LIGHT = 0xFF6B5A40;
    private static final int BACK = 0xE61A1510;
    private static final int GOLD = 0xFFE8C873;
    private static final int PALE = 0xFFD8D0C0;
    private static final int DIM = 0xFF9A8F7C;

    private final List<Net.Stop> stops = HearthScreen.stops;
    private int first;

    public TravelScreen() {
        super(Component.translatable("screen.vsratie_dushi.travel"));
    }

    private int left() {
        return (this.width - PANEL_WIDTH) / 2;
    }

    private int top() {
        return Math.max(4, (this.height - PANEL_HEIGHT) / 2);
    }

    /** What a hearth is called: the name its place has, or, for one somebody set down, where it stands. */
    static Component name(Net.Stop stop) {
        if (stop.site().equals("none")) {
            return Component.translatable("hearth.vsratie_dushi.none", stop.pos().getX(), stop.pos().getY(), stop.pos().getZ());
        }
        return Component.translatable("hearth.vsratie_dushi." + stop.site());
    }

    @Override
    protected void init() {
        int left = this.left();
        int top = this.top();
        for (int row = 0; row < ROWS && this.first + row < this.stops.size(); row++) {
            int index = this.first + row;
            Button go = this.addRenderableWidget(Button.builder(Component.translatable("screen.vsratie_dushi.travel.go"), button -> {
                ClientPacketDistributor.sendToServer(new Net.Travel(index));
                this.onClose();
            }).bounds(left + PANEL_WIDTH - 62, top + 30 + row * ROW_HEIGHT, 50, 20).build());
            go.active = !this.stops.get(index).here();
        }
        if (this.stops.size() > ROWS) {
            Button up = this.addRenderableWidget(Button.builder(Component.literal("\u25B2"), button -> this.page(-ROWS))
                    .bounds(left + 12, top + PANEL_HEIGHT - 28, 20, 20).build());
            Button down = this.addRenderableWidget(Button.builder(Component.literal("\u25BC"), button -> this.page(ROWS))
                    .bounds(left + 36, top + PANEL_HEIGHT - 28, 20, 20).build());
            up.active = this.first > 0;
            down.active = this.first + ROWS < this.stops.size();
        }
        this.addRenderableWidget(Button.builder(Component.translatable("screen.vsratie_dushi.travel.back"), button -> this.minecraft.gui.setScreen(new HearthScreen()))
                .bounds(left + PANEL_WIDTH - 120, top + PANEL_HEIGHT - 28, 108, 20).build());
    }

    private void page(int by) {
        this.first = Math.max(0, Math.min(this.first + by, Math.max(0, this.stops.size() - 1) / ROWS * ROWS));
        this.rebuildWidgets();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int left = this.left();
        int top = this.top();
        int right = left + PANEL_WIDTH;
        graphics.fill(left - 2, top - 2, right + 2, top + PANEL_HEIGHT + 2, FRAME);
        graphics.fill(left - 1, top - 1, right + 1, top + PANEL_HEIGHT + 1, FRAME_LIGHT);
        graphics.fill(left, top, right, top + PANEL_HEIGHT, BACK);
        graphics.centeredText(this.font, this.title, this.width / 2, top + 8, GOLD);
        graphics.fill(left + 10, top + 21, right - 10, top + 22, FRAME_LIGHT);
        for (int row = 0; row < ROWS && this.first + row < this.stops.size(); row++) {
            Net.Stop stop = this.stops.get(this.first + row);
            int y = top + 30 + row * ROW_HEIGHT;
            if (row % 2 == 1) {
                graphics.fill(left + 6, y - 1, right - 6, y + 21, 0x18FFFFFF);
            }
            graphics.text(this.font, name(stop), left + 12, y + 6, stop.here() ? GOLD : PALE);
            if (stop.here()) {
                Component here = Component.translatable("screen.vsratie_dushi.travel.here");
                graphics.text(this.font, here, right - 68 - this.font.width(here), y + 6, DIM);
            }
        }
        graphics.centeredText(this.font, Component.translatable("screen.vsratie_dushi.travel.hint"), this.width / 2, top + 34 + ROWS * ROW_HEIGHT, DIM);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
