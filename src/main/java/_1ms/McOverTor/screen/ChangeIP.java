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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import static _1ms.McOverTor.Main.*;

public class ChangeIP extends Screen {
    private volatile int status = 0;

    public ChangeIP() {
        super(Component.literal("Change IP"));
    }

    @Override
    protected void init() {
        super.init();

        if (status==0)
            TorManager.changeCircuits().thenAccept(i->status=i);

        this.addRenderableWidget(Button.builder(Component.literal("Okay"), _ -> onClose())
                .bounds(this.width / 2 - 60, this.height / 2 + 30, 120, 20)
                .build());

        var txtW = this.font.width(madeByText);
        this.addRenderableWidget(new PlainTextButton(this.width-txtW-2,this.height-10,txtW,10, madeByText,
                ConfirmLinkScreen.confirmLink(this, githubUrl), this.font));
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(new JoinMultiplayerScreen(new TitleScreen()));
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        final int centerY = this.height / 2-10;
        final int centerX = this.width / 2;
        renderWindow(graphics, (this.width - 200) / 2-10, centerY - 30, 220, 100, "McOverTor Connection");

        super.extractRenderState(graphics,mouseX,mouseY,a);

        graphics.text(this.font,verText,2, this.height-10, 0xFFFFFFFF);

        switch (status) {
            case 0 -> graphics.centeredText(this.font, "Changing IP...", centerX, centerY, 0xFFFFFFFF); //Will be 0 at first
            case 1 -> graphics.centeredText(this.font, "You've successfully changed IP.", centerX, centerY, 0xFF00FF00);
            case 2 -> graphics.centeredText(this.font, "Failed to change IP!", centerX, centerY, 0xFFFF0000);
        }
    }
}
