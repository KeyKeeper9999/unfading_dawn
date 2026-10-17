package com.keykeeper.dushi.client;

import java.util.List;

import com.keykeeper.dushi.ClockState;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * The end of the day, in the dark: a few lines one after another, each coming out of the black and going back into it,
 * then the title of the ending and a way back into the world (which stays as the ending left it). Escape leaves at any time.
 * The lines are {@code ending.unfading_dawn.<ending>.<n>}; how many there are is {@link #LINES}.
 */
public class EndingScreen extends Screen {
    /** How many lines each ending has: the bell's, the seat's. */
    private static final int[] LINES = {0, 8, 7};
    private static final long FADE = 1800L;
    private static final long HOLD = 4600L;
    private static final long GAP = 700L;

    private final int ending;
    private final long opened = System.currentTimeMillis();
    private Button next;

    public EndingScreen(int ending) {
        super(Component.translatable("ending.unfading_dawn." + ending + ".title"));
        this.ending = ending == ClockState.SEAT ? ClockState.SEAT : ClockState.BELL;
    }

    private long slot() {
        return FADE + HOLD + FADE + GAP;
    }

    private long total() {
        return 1500L + LINES[this.ending] * this.slot();
    }

    @Override
    protected void init() {
        // The way back is offered only when the last line has gone.
        this.next = this.addRenderableWidget(Button.builder(Component.translatable("screen.unfading_dawn.ending.continue"), button -> this.onClose())
                .bounds(this.width / 2 - 60, this.height / 2 + 34, 120, 20).build());
        this.next.visible = false;
        this.next.active = false;
    }

    private static int shade(int rgb, float alpha) {
        int a = Math.max(4, Math.min(255, Math.round(alpha * 255.0F)));
        return (a << 24) | rgb;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        long now = System.currentTimeMillis() - this.opened;
        graphics.fill(0, 0, this.width, this.height, 0xFF000000);
        long into = now - 1500L;
        int cx = this.width / 2;
        if (into >= 0 && into < LINES[this.ending] * this.slot()) {
            int line = (int) (into / this.slot());
            long within = into % this.slot();
            float alpha = within < FADE ? within / (float) FADE
                    : within < FADE + HOLD ? 1.0F
                    : within < FADE + HOLD + FADE ? 1.0F - (within - FADE - HOLD) / (float) FADE : 0.0F;
            if (alpha > 0.02F) {
                List<FormattedCharSequence> rows = this.font.split(Component.translatable("ending.unfading_dawn." + this.ending + "." + line), Math.min(this.width - 40, 320));
                int y = this.height / 2 - rows.size() * 6;
                for (FormattedCharSequence row : rows) {
                    graphics.text(this.font, row, cx - this.font.width(row) / 2, y, shade(0xE8D8B0, alpha));
                    y += 12;
                }
            }
        } else if (into >= LINES[this.ending] * this.slot()) {
            float alpha = Math.min(1.0F, (into - LINES[this.ending] * this.slot()) / (float) FADE);
            graphics.centeredText(this.font, this.title, cx, this.height / 2 - 10, shade(0xE8C873, alpha));
            graphics.centeredText(this.font, Component.translatable("ending.unfading_dawn." + this.ending + ".subtitle"), cx, this.height / 2 + 6, shade(0x9A8F7C, alpha));
            if (this.next != null) {
                this.next.visible = true;
                this.next.active = true;
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
