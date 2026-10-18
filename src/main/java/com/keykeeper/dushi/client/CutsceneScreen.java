package com.keykeeper.dushi.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

/**
 * What lies over the world while a cutscene plays ({@link Cinema}): nothing to be seen but the black of a cut and, at the beginning,
 * the way to skip. It holds the controls, and takes space, enter or escape to skip. The game goes on under it.
 */
public class CutsceneScreen extends Screen {
    public CutsceneScreen() {
        super(Component.empty());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        float black = Cinema.fade(partial);
        if (black > 0.004F) {
            graphics.fill(0, 0, this.width, this.height, (Math.min(255, Math.round(black * 255.0F)) << 24));
        }
        float hint = Cinema.hint(partial) * (1.0F - black);
        if (hint > 0.02F) {
            int alpha = Math.max(4, Math.min(255, Math.round(hint * 200.0F)));
            graphics.centeredText(this.font, Component.translatable("screen.unfading_dawn.cutscene.skip"), this.width / 2, this.height - 26, (alpha << 24) | 0xD8D0C0);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        if (key == InputConstants.KEY_SPACE || key == InputConstants.KEY_RETURN || key == InputConstants.KEY_ESCAPE) {
            Cinema.skip();
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
