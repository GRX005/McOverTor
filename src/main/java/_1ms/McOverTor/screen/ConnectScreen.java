/*
    This file is part of the McOverTor project, licensed under the
    GNU General Public License v3.0

    Copyright (C) 2024 _1ms

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
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

import java.util.Objects;

public class ConnectScreen extends Screen {
    //private static final Identifier IMAGE_ID = Identifier.of("mcovertor", "tor");

    public static int progress = 0;
    public static String message = "";
    private static final ButtonWidget closeButton = ButtonWidget.builder(Text.literal("Okay"), (buttonWidget) -> realClose())
            .dimensions(0, 0, 120, 20).build();

    private static final ButtonWidget cancelButton = ButtonWidget.builder(Text.literal("Cancel"), (buttonWidget) -> {
        if(progress < 5)
            TorManager.killTor(false);
        else
            TorManager.exitTor(true);
        realClose();
    }).dimensions(0, 0, 120, 20).build();

    public ConnectScreen() {
        super(Text.literal("Connect to Tor"));
    }

    @Override
    protected void init() {
        super.init();

        closeButton.setFocused(false);
        cancelButton.setFocused(false);
        closeButton.setPosition(this.width / 2 - 60, this.height / 2 + 30);
        if(progress < 100) {
            this.addSelectableChild(cancelButton);
            cancelButton.setPosition(this.width / 2 - 60, this.height / 2 + 30);
            return;
        }
        this.addSelectableChild(closeButton);
    }

    @Override
    public void close() {
        realClose();
    }

    public static void realClose() {
        Objects.requireNonNull(MinecraftClient.getInstance()).setScreen(new MultiplayerScreen(new TitleScreen()));
    }
    int i = 0;

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

        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(progress + "%"), xhalf, y-14, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(message), xhalf, y + barHeight - 10, 0xA0FFFFFF);
        if (progress < 100) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Connecting to Tor..."), xhalf, yhalf - 60, 0xFFFFFF);
            cancelButton.render(context, mouseX, mouseY, delta);
            return;
        }
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Successfully connected to Tor!"), xhalf, yhalf - 60, 0x00FF00);
        if(i == 0) {
            this.remove(cancelButton);
            this.addSelectableChild(closeButton);
            i++;
        }
        closeButton.render(context, mouseX, mouseY, delta);
    }
    public void renderProgressBar(DrawContext context, int x, int y, int barWidth, int barHeight) {
        context.drawBorder(x - 1, y - 1, barWidth + 2, barHeight + 2, 0xFFD3D3D3);
        context.fill(x, y, x + (progress * 2), y + barHeight, 0xFF00FF00);
        context.fill(x + (progress * 2), y, x + barWidth, y + barHeight, 0x80000000);
    }




}