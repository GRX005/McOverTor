/*
    This file is part of the McOverTor project, licensed under the
    GNU General Public License v3.0

    Copyright (C) 2024-2025 _1ms

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package _1ms.McOverTor.screen;

import _1ms.McOverTor.Main;
import _1ms.McOverTor.manager.TorManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.Objects;

import static _1ms.McOverTor.manager.TorManager.progress;

public class TorConnect extends Screen {
    //private static final Identifier IMAGE_ID = Identifier.of("mcovertor", "tor");
    public static boolean fail = false;
    private final static ButtonWidget closeButton = ButtonWidget.builder(Text.literal("Okay"), Btn -> realClose())
            .dimensions(0, 0, 120, 20).build();
    private final static ButtonWidget cancelButton = ButtonWidget.builder(Text.literal("Cancel"), Btn -> cancelBtnFunc())
            .dimensions(0, 0, 120, 20).build();

    public TorConnect() {
        super(Text.literal("Connect to Tor"));
        fail = false;
    }

    @Override
    protected void init() {
        super.init();
        cancelButton.setFocused(false);
        closeButton.setFocused(false);

//We start with setting the pos for both, but only adding cancel btn for first.
        cancelButton.setPosition(this.width / 2 - 60, this.height / 2 + 30);
        closeButton.setPosition(this.width / 2 - 60, this.height / 2 + 30);
        this.addSelectableChild(cancelButton);
    }
//Shouldn't be closed like this, while it's loading.
    @Override
    public boolean shouldCloseOnEsc() {
        return progress == 100;
    }

    @Override
    public void close() {
        realClose();
    }
//After 5% we estabilish a control port conn with Tor so we can close it gracefully.
    private static void cancelBtnFunc() {
        if(progress < 5)
            TorManager.killTor(false, true);
        else
            TorManager.exitTor(true);
        realClose();
        fail = false;
    }

    private static void realClose() { Objects.requireNonNull(MinecraftClient.getInstance()).setScreen(new MultiplayerScreen(new TitleScreen())); }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        final int barWidth = 200;
        final int barHeight = 20;
        final int x = (this.width - barWidth) / 2;

        final int yhalf = this.height / 2;
        final int xhalf = this.width / 2;
        final int y = yhalf - 10;
        //context.drawGuiTexture(IMAGE_ID, x, y-270, 179, 108);
        Main.renderWindow(context, x-110, y-80, barWidth+220, barHeight+140, "McOverTor Connection");

        renderProgressBar(context, x, y-20, barWidth, barHeight);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(progress + "%"), xhalf, y-14, 0xFFFFFF); //Progress in %
//Render the fail msg and ret if the conn failed.
        if(fail) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Failed to launch Tor, check logs."), xhalf, y + barHeight - 10, Color.RED.getRGB());
            cancelButton.render(context, mouseX, mouseY, delta);
            return;
        }
//Otherwise the tor status msgs.
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(TorManager.message), xhalf, y + barHeight - 10, 0xA0FFFFFF);

        if (progress < 100) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Connecting to Tor..."), xhalf, yhalf - 60, 0xFFFFFF);
            cancelButton.render(context, mouseX, mouseY, delta);
            return;
        }
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Successfully connected to Tor!"), xhalf, yhalf - 60, Color.GREEN.getRGB());

        closeButton.render(context, mouseX, mouseY, delta);
    }
//When the conn reaches 100%, remove the cancelBtn and add close.
    public void connCallback() {
        this.remove(cancelButton);
        this.addSelectableChild(closeButton);
    }

    public void renderProgressBar(DrawContext context, int x, int y, int barWidth, int barHeight) {
        context.drawBorder(x - 2, y - 2, barWidth + 4, barHeight + 4, 0xFFFFFFFF);
        context.fill(x, y, x + (progress * 2), y + barHeight, 0xFF00FF00);
        context.fill(x + (progress * 2), y, x + barWidth, y + barHeight, 0x80000000);
    }

}