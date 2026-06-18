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
import static _1ms.McOverTor.manager.TorManager.*;

public class TorConnect extends Screen {
    //private static final Identifier IMAGE_ID = Identifier.of("mcovertor", "tor");

    private Button cancelBtn;
    private Button closeBtn;

    public TorConnect() {
        super(Component.literal("Connect to Tor"));
        failToStart = false;
        failToConn = false;
    }

    @Override
    protected void init() {
        super.init();
        cancelBtn=Button.builder(Component.literal("Cancel"),_ -> cancelBtnFunc()).bounds(this.width/2-60,this.height/2+30,120,20).build();
        closeBtn=Button.builder(Component.literal("Okay"),_ -> onClose()).bounds(this.width/2-60,this.height/2+30,120,20).build();
        if (progress!=100)
            this.addRenderableWidget(cancelBtn);
        else
            this.addRenderableWidget(closeBtn);

        var txtW = this.font.width(madeByText);
        this.addRenderableWidget(new PlainTextButton(this.width-txtW-2,this.height-10,txtW,10, madeByText,
                ConfirmLinkScreen.confirmLink(this, githubUrl), this.font));
    }
//Shouldn't be closed like this, while it's loading.
    @Override
    public boolean shouldCloseOnEsc() {
        return progress == 100;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(new JoinMultiplayerScreen(new TitleScreen()));
    }
//After 5% we estabilish a control port conn with Tor so we can close it gracefully.
    private void cancelBtnFunc() {
        if(progress < 5)
            TorManager.killTor(false, true);
        else
            TorManager.exitTor(true);
        onClose();
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

        final int barWidth = 200;
        final int barHeight = 20;
        final int x = (this.width - barWidth) / 2;

        final int yhalf = this.height / 2;
        final int xhalf = this.width / 2;
        final int y = yhalf - 10;
        //context.drawGuiTexture(IMAGE_ID, x, y-270, 179, 108);
        renderWindow(graphics, x-110, y-80, barWidth+220, barHeight+140, "McOverTor Connection");
        super.extractRenderState(graphics,mouseX,mouseY,a);

        graphics.text(this.font, verText,2, this.height-10, 0xFFFFFFFF);

        renderProgressBar(graphics, x, y-20);
        graphics.centeredText(this.font, Component.literal(progress + "%"), xhalf, y-14, 0xFFFFFFFF); //Progress in %
//Render the fail msg and ret if the conn failed.
        if(failToStart) {
            graphics.centeredText(this.font, Component.literal("Failed to launch Tor, check logs."), xhalf, y + barHeight - 10, 0xFFFF0000);
            graphics.centeredText(this.font, Component.literal("Error occurred!"), xhalf, yhalf - 60, 0xFFFF5555);
            cancelBtn.extractRenderState(graphics, mouseX, mouseY, a);
            return;
        }
        if (failToConn)
            graphics.centeredText(this.font, Component.literal("Tor might be failing to connect because of your internet or country selections."), xhalf, y + barHeight +5, 0xFFFF0000);

//Otherwise the tor status msgs.
        graphics.centeredText(this.font, Component.literal(TorManager.message), xhalf, y + barHeight - 10, 0xA0FFFFFF);

        if (progress < 100) {
            graphics.centeredText(this.font, Component.literal("Connecting to Tor..."), xhalf, yhalf - 60, 0xFFFFFFFF);
            return;
        }
        graphics.centeredText(this.font, Component.literal("Successfully connected to Tor!"), xhalf, yhalf - 60, 0xFF00FF00);

    }
//When the conn reaches 100%, remove the cancelBtn and add close.
    public void connCallback() {
        this.removeWidget(cancelBtn);
        this.addRenderableWidget(closeBtn);
    }

    private void renderProgressBar(GuiGraphicsExtractor graphics, int x, int y) {
        drawBorder(graphics, x - 2, y - 2, 200 + 4, 20 + 4, 0xFFFFFFFF);
        graphics.fill(x, y, x + (progress * 2), y + 20, 0xFF00FF00);
        graphics.fill(x + (progress * 2), y, x + 200, y + 20, 0x80000000);
    }

}