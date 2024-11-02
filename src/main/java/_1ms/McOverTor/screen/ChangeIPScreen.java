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

public class ChangeIPScreen extends Screen {
    private static final ButtonWidget closeButton = ButtonWidget.builder(Text.literal("Okay"), (buttonWidget) -> RealClose())
            .dimensions(0, 0, 120, 20)
            .build();
    public static boolean isDone = false;

    @Override
    protected void init() {
        super.init();

        closeButton.setFocused(false);
        closeButton.setPosition(this.width / 2 - 60, this.height / 2 + 30);
        this.addSelectableChild(closeButton);
    }

    @Override
    public void close() {
        RealClose();
    }

    public static void RealClose() {
        MinecraftClient.getInstance().setScreen(new MultiplayerScreen(new TitleScreen()));
        isDone = false;
    }

    public ChangeIPScreen() {
        super(Text.literal("Change IP"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        final int centerY = this.height / 2-10;
        final int centerX = this.width / 2;

        Main.renderWindow(context, (this.width - 200) / 2-10, centerY - 30, 220, 100, "McOverTor Connection");
        closeButton.render(context, mouseX, mouseY, delta);

        if(!isDone) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§aChanging IP..."), centerX, centerY, 0xFFFFFF);
            TorManager.changeCircuits();
            return;
        }
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§aYou've successfully changed IP."), centerX, centerY, 0xFFFFFF);
    }
}
