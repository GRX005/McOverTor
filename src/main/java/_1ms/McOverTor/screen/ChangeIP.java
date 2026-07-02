/*
    This file is part of the McOverTor project, licensed under the
    GNU General Public License v3.0

    Copyright (C) 2024-2026 _1ms (GRX005)

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

import _1ms.McOverTor.manager.TorManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.text.Text;

import static _1ms.McOverTor.Main.*;

public class ChangeIP extends Screen {
    private final ButtonWidget closeButton = ButtonWidget.builder(Text.literal("Okay"), buttonWidget -> close())
            .dimensions(0, 0, 120, 20)
            .build();
    private int status = 0;
    private static volatile long cooldownEndsAt = 0;

    @Override
    protected void init() {
        super.init();

        closeButton.setFocused(false);
        closeButton.setPosition(this.width / 2 - 60, this.height / 2 + 30);
        this.addSelectableChild(closeButton);

        var txtW = this.textRenderer.getWidth(madeByText);
        this.addDrawableChild(new PressableTextWidget(this.width-txtW-2,this.height-10,txtW,10, madeByText,
                ConfirmLinkScreen.opening(this, githubUrl), this.textRenderer));
    }

    @Override
    public void close() {
        this.client.setScreen(new MultiplayerScreen(new TitleScreen()));
    }

    public ChangeIP() {
        super(Text.literal("Change IP"));
        long now = System.currentTimeMillis();
        if (now >= cooldownEndsAt) {
            TorManager.changeCircuits().thenAccept(i -> status = i);
            cooldownEndsAt = now + 10_000;
        } else {
            status = 3;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawTextWithShadow(this.textRenderer,verText,2, this.height-10, 0xFFFFFFFF);

        final int centerY = this.height / 2-10;
        final int centerX = this.width / 2;

        renderWindow(context, (this.width - 200) / 2-10, centerY - 30, 220, 100, "McOverTor Connection");
        closeButton.render(context, mouseX, mouseY, delta);
        switch (status) {
            case 0-> context.drawCenteredTextWithShadow(this.textRenderer, "Changing IP...", centerX, centerY, 0xFFFFFFFF); //Will be 0 at first
            case 1-> context.drawCenteredTextWithShadow(this.textRenderer, "You've successfully changed IP.", centerX, centerY, 0xFF00FF00);
            case 2-> context.drawCenteredTextWithShadow(this.textRenderer, "Failed to change IP!", centerX, centerY, 0xFFFF0000);
            case 3-> {
                long remaining = Math.max(0, (cooldownEndsAt - System.currentTimeMillis() + 999) / 1000);
                context.drawCenteredTextWithShadow(this.textRenderer, "Waiting for Tor's IP rate limit: " + remaining, centerX, centerY, 0xFFFF0000);
            }
        }
    }

    @Override
    public void tick() {
        if (status == 3 && System.currentTimeMillis() >= cooldownEndsAt) {
            status = 0;
            TorManager.changeCircuits().thenAccept(i -> status = i);
            cooldownEndsAt = System.currentTimeMillis() + 10_000;
        }
    }
}
