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

import _1ms.McOverTor.manager.TorOption;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NullMarked;

import static _1ms.McOverTor.Main.*;

@NullMarked
public class Settings extends Screen { //Non-changing parts of the buttons in the settings screen, pre-declared once the UI is opened for the 1st time.

//UI constructor, gets executed only when UI is opened, we set the tooltips here.
    public Settings() {
        super(Component.literal("McOverTor Settings"));
    }
//Override initialization of the UI, runs when the UI is opened but also every time it's resized, so we set the btn positions here responsively.
     @Override
     protected void init() {
         super.init();

         final int x = (this.width - 200) / 2;
         final int y = this.height / 2 - 50;

//Registed them as selectable but not drawable.
         this.addRenderableWidget(new SettCheckBox(x-30, y-100, Component.literal("Prevent non-Tor connections"), TorOption.torOnly)
                 .tooltip("Makes it so you cannot ping or connect to servers if Tor isn't turned on, to prevent accidents."));
         this.addRenderableWidget(new SettCheckBox(x-30, y-75, Component.literal("Stream separation"), TorOption.sepStreams)
                 .tooltip("Get a new IP every time you join a server."));
         this.addRenderableWidget(Button.builder(Component.literal("Done"), _ -> onClose()).bounds(x+40, y+200, 120, 20).build());
         this.addRenderableWidget(new SettCheckBox(x-30, y-30, Component.literal("Left"), "!"+TorOption.isRight));
         this.addRenderableWidget(new SettCheckBox(x+35, y-30, Component.literal("Right"), TorOption.isRight));
         this.addRenderableWidget(new SettCheckBox(x+115, y-30, Component.literal("Upper"), TorOption.isUpper));
         this.addRenderableWidget(new SettCheckBox(x+182, y-30, Component.literal("Lower"), "!"+TorOption.isUpper));
         this.addRenderableWidget(new SettCheckBox(x-30, y+10, Component.literal("Resolve DNS using Tor"), TorOption.useTorDNS)
                 .tooltip("§aPros: §fDNS queries won't leak your IP, it's more secure.\nYou can connect to .onion server addresses.\n§cCons: §fTor might fail to resolve some domains that use SRV, so it is turned off by default."));

         var txtW = this.font.width(madeByText);
         this.addRenderableWidget(new PlainTextButton(this.width-txtW-2,this.height-10,txtW,10, madeByText,
                 ConfirmLinkScreen.confirmLink(this, githubUrl), this.font));
     }
//Override the close func of the UI so it returns to the multiplayer screen when pressing ESC, not the title screen.
     @Override
     public void onClose() {
         this.minecraft.setScreenAndShow(new JoinMultiplayerScreen(new TitleScreen()));
     }
//Override the render func, so we can render the elements above the window.
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta)  {
        renderWindow(graphics, (this.width - 200) / 2-50, this.height / 2 - 170, 300, 350, "McOverTor Settings");
        super.extractRenderState(graphics,mouseX,mouseY,delta);

        graphics.text(this.font, verText,2, this.height-10, 0xFFFFFFFF);

        graphics.centeredText(this.minecraft.font, "Tor Buttons position:", (this.width - 200) / 2+100, this.height / 2 -100, 0xFFFFFFFF);

//Line between the vertical and horizontal pos settings.
        graphics.verticalLine(this.width/2, this.height/2-50, this.height/2-90, 0xFFFFFFFF);
    }

}
