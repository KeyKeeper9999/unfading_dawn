package com.keykeeper.dushi.client;

import java.util.List;

import com.keykeeper.dushi.Net;
import com.keykeeper.dushi.PlayerSoul;
import com.keykeeper.dushi.Shop;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/** The keykeeper's wares: a grid of things, each with its price in souls. A click buys. */
public class ShopScreen extends Screen {
    private static final int COLUMNS = 7;
    private static final int CELL_WIDTH = 34;
    private static final int CELL_HEIGHT = 36;
    private static final int FRAME = 0xFF16120E;
    private static final int FRAME_LIGHT = 0xFF6B5A40;
    private static final int BACK = 0xE61A1510;
    private static final int GOLD = 0xFFE8C873;
    private static final int SOULS = 0xFF9FE3FF;
    private static final int RED = 0xFFD06A5A;

    private final List<Shop.Ware> wares = Shop.wares();
    private Button[] buttons;
    private ItemStack[] stacks;

    public ShopScreen() {
        super(Component.translatable("screen.vsratie_dushi.shop"));
    }

    private int rows() {
        return (this.wares.size() + COLUMNS - 1) / COLUMNS;
    }

    private int panelWidth() {
        return COLUMNS * CELL_WIDTH + 16;
    }

    private int panelHeight() {
        return this.rows() * CELL_HEIGHT + 70;
    }

    private int left() {
        return (this.width - this.panelWidth()) / 2;
    }

    private int top() {
        return Math.max(4, (this.height - this.panelHeight()) / 2);
    }

    @Override
    protected void init() {
        this.buttons = new Button[this.wares.size()];
        this.stacks = new ItemStack[this.wares.size()];
        for (int i = 0; i < this.wares.size(); i++) {
            int index = i;
            this.stacks[i] = this.wares.get(i).stack().get();
            this.buttons[i] = this.addRenderableWidget(Button.builder(Component.empty(),
                    button -> ClientPacketDistributor.sendToServer(new Net.Buy(index)))
                    .bounds(this.left() + 8 + (i % COLUMNS) * CELL_WIDTH + 1, this.top() + 36 + (i / COLUMNS) * CELL_HEIGHT, CELL_WIDTH - 2, CELL_HEIGHT - 2).build());
        }
        this.addRenderableWidget(Button.builder(Component.translatable("screen.vsratie_dushi.shop.leave"), button -> this.onClose())
                .bounds(this.width / 2 - 60, this.top() + this.panelHeight() - 26, 120, 20).build());
        this.updateButtons();
    }

    private void updateButtons() {
        if (this.minecraft.player == null || this.buttons == null) {
            return;
        }
        int souls = PlayerSoul.of(this.minecraft.player).souls;
        boolean free = this.minecraft.player.isCreative();
        for (int i = 0; i < this.buttons.length; i++) {
            this.buttons[i].active = free || souls >= this.wares.get(i).price();
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
        int left = this.left();
        int top = this.top();
        int right = left + this.panelWidth();
        int bottom = top + this.panelHeight();
        graphics.fill(left - 2, top - 2, right + 2, bottom + 2, FRAME);
        graphics.fill(left - 1, top - 1, right + 1, bottom + 1, FRAME_LIGHT);
        graphics.fill(left, top, right, bottom, BACK);
        graphics.centeredText(this.font, this.title, this.width / 2, top + 8, GOLD);
        if (this.minecraft.player != null) {
            Component souls = Component.translatable("screen.vsratie_dushi.hearth.souls", PlayerSoul.of(this.minecraft.player).souls);
            graphics.text(this.font, souls, right - 10 - this.font.width(souls), top + 21, SOULS);
        }
        graphics.fill(left + 8, top + 32, right - 8, top + 33, FRAME_LIGHT);
    }

    /** The wares are drawn over their buttons: the thing itself, and its price under it. */
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        if (this.buttons == null || this.minecraft.player == null) {
            return;
        }
        int souls = PlayerSoul.of(this.minecraft.player).souls;
        for (int i = 0; i < this.buttons.length; i++) {
            Button button = this.buttons[i];
            int x = button.getX() + (button.getWidth() - 16) / 2;
            int y = button.getY() + 3;
            graphics.item(this.stacks[i], x, y);
            graphics.itemDecorations(this.font, this.stacks[i], x, y);
            String price = String.valueOf(this.wares.get(i).price());
            boolean enough = souls >= this.wares.get(i).price() || this.minecraft.player.isCreative();
            graphics.text(this.font, price, button.getX() + (button.getWidth() - this.font.width(price)) / 2, button.getY() + 23, enough ? SOULS : RED, true);
            if (button.isHovered()) {
                graphics.setTooltipForNextFrame(this.font, this.stacks[i], mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
