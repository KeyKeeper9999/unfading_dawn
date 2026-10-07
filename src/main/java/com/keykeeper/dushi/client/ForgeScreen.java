package com.keykeeper.dushi.client;

import java.util.ArrayList;
import java.util.List;

import com.keykeeper.dushi.Forge;
import com.keykeeper.dushi.Net;
import com.keykeeper.dushi.PlayerSoul;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * The anvil's list: one line per thing that can be made, with what it takes (red where the player is short) and a button.
 * The mouse wheel or the arrows move through the list.
 */
public class ForgeScreen extends Screen {
    private static final int PANEL_WIDTH = 336;
    private static final int ROW_HEIGHT = 24;
    /** Where the list begins under the title, the souls and the tabs. */
    private static final int LIST_TOP = 62;
    private static final int FRAME = 0xFF16120E;
    private static final int FRAME_LIGHT = 0xFF6B5A40;
    private static final int BACK = 0xE61A1510;
    private static final int GOLD = 0xFFE8C873;
    private static final int PALE = 0xFFE6DCC8;
    private static final int DIM = 0xFF9A8F7C;
    private static final int SOULS = 0xFF9FE3FF;
    private static final int RED = 0xFFD06A5A;

    private final List<Forge.Recipe> all = Forge.recipes();
    /** The recipes of the open shelf, as indices into the whole list (which is what the server is told). */
    private final List<Integer> recipes = new ArrayList<>();
    private final ItemStack[] results;
    private Button[] buttons = new Button[0];
    private final Button[] tabs = new Button[Forge.Category.values().length];
    private static Forge.Category shelf = Forge.Category.WEAPONS;
    private int scroll;

    public ForgeScreen() {
        super(Component.translatable("screen.unfading_dawn.forge"));
        this.results = new ItemStack[this.all.size()];
        for (int i = 0; i < this.results.length; i++) {
            this.results[i] = this.all.get(i).result().get();
        }
        this.open(shelf);
    }

    private void open(Forge.Category category) {
        shelf = category;
        this.recipes.clear();
        for (int i = 0; i < this.all.size(); i++) {
            if (this.all.get(i).category() == category) {
                this.recipes.add(i);
            }
        }
        this.scroll = 0;
        this.updateButtons();
    }

    private int visible() {
        return Math.max(3, Math.min(8, (this.height - 110) / ROW_HEIGHT));
    }

    private int panelHeight() {
        return this.visible() * ROW_HEIGHT + 98;
    }

    private int left() {
        return (this.width - PANEL_WIDTH) / 2;
    }

    private int top() {
        return Math.max(4, (this.height - this.panelHeight()) / 2);
    }

    private int maxScroll() {
        return Math.max(0, this.recipes.size() - this.visible());
    }

    private void scrollBy(int rows) {
        this.scroll = Math.max(0, Math.min(this.maxScroll(), this.scroll + rows));
        this.updateButtons();
    }

    @Override
    protected void init() {
        int left = this.left();
        int top = this.top();
        int visible = this.visible();
        this.scroll = Math.min(this.scroll, this.maxScroll());
        this.buttons = new Button[visible];
        for (int row = 0; row < visible; row++) {
            int line = row;
            this.buttons[row] = this.addRenderableWidget(Button.builder(Component.translatable("screen.unfading_dawn.forge.make"),
                    button -> {
                        if (this.scroll + line < this.recipes.size()) {
                            ClientPacketDistributor.sendToServer(new Net.Make(this.recipes.get(this.scroll + line)));
                        }
                    }).bounds(left + PANEL_WIDTH - 62, top + LIST_TOP + row * ROW_HEIGHT + 2, 54, 20).build());
        }
        Forge.Category[] categories = Forge.Category.values();
        int tabWidth = (PANEL_WIDTH - 16) / categories.length;
        for (int i = 0; i < categories.length; i++) {
            Forge.Category category = categories[i];
            this.tabs[i] = this.addRenderableWidget(Button.builder(Component.translatable("screen.unfading_dawn.forge.tab." + category.id),
                    button -> this.open(category)).bounds(left + 8 + i * tabWidth, top + 34, tabWidth - 2, 20).build());
        }
        int bottom = top + this.panelHeight();
        this.addRenderableWidget(Button.builder(Component.literal("▲"), button -> this.scrollBy(-visible + 1)).bounds(left + 8, bottom - 26, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("▼"), button -> this.scrollBy(visible - 1)).bounds(left + 30, bottom - 26, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.unfading_dawn.shop.leave"), button -> this.onClose())
                .bounds(this.width / 2 - 60, bottom - 26, 120, 20).build());
        this.updateButtons();
    }

    private void updateButtons() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        for (int row = 0; row < this.buttons.length; row++) {
            int index = this.scroll + row;
            this.buttons[row].visible = index < this.recipes.size();
            this.buttons[row].active = index < this.recipes.size() && Forge.canMake(this.minecraft.player, this.all.get(this.recipes.get(index)));
        }
        for (int i = 0; i < this.tabs.length; i++) {
            if (this.tabs[i] != null) {
                this.tabs[i].active = Forge.Category.values()[i] != shelf;
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.updateButtons();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0.0) {
            this.scrollBy(scrollY > 0.0 ? -1 : 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int left = this.left();
        int top = this.top();
        int right = left + PANEL_WIDTH;
        int bottom = top + this.panelHeight();
        graphics.fill(left - 2, top - 2, right + 2, bottom + 2, FRAME);
        graphics.fill(left - 1, top - 1, right + 1, bottom + 1, FRAME_LIGHT);
        graphics.fill(left, top, right, bottom, BACK);
        graphics.centeredText(this.font, this.title, this.width / 2, top + 8, GOLD);
        if (this.minecraft.player != null) {
            Component souls = Component.translatable("screen.unfading_dawn.hearth.souls", PlayerSoul.of(this.minecraft.player).souls);
            graphics.text(this.font, souls, right - 10 - this.font.width(souls), top + 21, SOULS);
        }
        String place = (this.scroll + 1) + "-" + Math.min(this.recipes.size(), this.scroll + this.visible()) + " / " + this.recipes.size();
        graphics.text(this.font, place, left + 10, top + 21, DIM);
        graphics.fill(left + 8, top + 57, right - 8, top + 58, FRAME_LIGHT);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        if (this.minecraft.player == null) {
            return;
        }
        int left = this.left();
        int top = this.top();
        boolean free = this.minecraft.player.isCreative();
        int have = PlayerSoul.of(this.minecraft.player).souls;
        ItemStack hovered = ItemStack.EMPTY;
        for (int row = 0; row < this.buttons.length; row++) {
            int index = this.scroll + row;
            if (index >= this.recipes.size()) {
                break;
            }
            Forge.Recipe recipe = this.all.get(this.recipes.get(index));
            int y = top + LIST_TOP + row * ROW_HEIGHT;
            if (row % 2 == 1) {
                graphics.fill(left + 6, y, left + PANEL_WIDTH - 6, y + ROW_HEIGHT, 0x18FFFFFF);
            }
            ItemStack result = this.results[this.recipes.get(index)];
            graphics.item(result, left + 10, y + 4);
            graphics.itemDecorations(this.font, result, left + 10, y + 4);
            if (mouseX >= left + 8 && mouseX < left + 30 && mouseY >= y && mouseY < y + ROW_HEIGHT) {
                hovered = result;
            }
            // what it takes, from the button leftwards: souls first, then the materials
            int x = left + PANEL_WIDTH - 68;
            if (recipe.souls() > 0) {
                String souls = String.valueOf(recipe.souls());
                x -= this.font.width(souls);
                graphics.text(this.font, souls, x, y + 8, free || have >= recipe.souls() ? SOULS : RED);
                x -= 6;
            }
            for (int i = recipe.parts().size() - 1; i >= 0; i--) {
                Forge.Part part = recipe.parts().get(i);
                ItemStack stack = new ItemStack(part.item().get());
                String count = "×" + part.count();
                x -= this.font.width(count);
                boolean enough = free || Forge.count(this.minecraft.player, stack.getItem()) >= part.count();
                graphics.text(this.font, count, x, y + 8, enough ? PALE : RED);
                x -= 17;
                graphics.item(stack, x, y + 4);
                if (mouseX >= x && mouseX < x + 16 && mouseY >= y + 4 && mouseY < y + 20) {
                    hovered = stack;
                }
                x -= 5;
            }
            String name = this.font.plainSubstrByWidth(result.getHoverName().getString(), Math.max(20, x - (left + 32)), false);
            graphics.text(this.font, name, left + 32, y + 8, PALE);
        }
        if (!hovered.isEmpty()) {
            graphics.setTooltipForNextFrame(this.font, hovered, mouseX, mouseY);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
