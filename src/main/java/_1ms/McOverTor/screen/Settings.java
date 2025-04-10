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
import _1ms.McOverTor.manager.TorOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.Objects;

public class Settings extends Screen { //Non-changing parts of the buttons in the settings screen, pre-declared once the UI is opened for the 1st time.
    private final static SettCheckBox preventNonTor = new SettCheckBox(0, 0, Text.literal("Prevent non-Tor connections"), TorOption.torOnly);
    private final static SettCheckBox sepStr = new SettCheckBox(0, 0, Text.literal("Stream separation"), TorOption.sepStreams);
    private final static SettCheckBox left = new SettCheckBox(0, 0, Text.literal("Left"), "!"+TorOption.isRight);
    private final static SettCheckBox right = new SettCheckBox(0, 0, Text.literal("Right"), TorOption.isRight);
    private final static SettCheckBox upper = new SettCheckBox(0, 0, Text.literal("Upper"), TorOption.isUpper);
    private final static SettCheckBox lower = new SettCheckBox(0, 0, Text.literal("Lower"), "!"+TorOption.isUpper);
    private final static SettCheckBox torDNS = new SettCheckBox(0,0, Text.literal("Resolve DNS using Tor"), TorOption.useTorDNS);
    private final static ButtonWidget doneBtn = ButtonWidget.builder(Text.literal("Done"), btn -> CloseR()).dimensions(0, 0, 120, 20).build();
//UI constructor, gets executed only when UI is opened, we set the tooltips here.
    public Settings() {
        super(Text.literal("McOverTor Settings"));
        preventNonTor.setTooltip(Tooltip.of(Text.literal("Makes it so you cannot ping or connect to servers if Tor isn't turned on, to prevent accidents.")));
        sepStr.setTooltip(Tooltip.of(Text.literal("Get a new IP every time you join a server.")));
        torDNS.setTooltip(Tooltip.of(Text.literal("§aPros: §fDNS queries won't leak your IP, more secure.\nYou can connect to .onion server addresses.\n§cCons: §fTor might fail to resolve some domains, so it is turned off by default.")));
    }
//Override initialization of the UI, runs when the UI is opened but also every time it's resized, so we set the btn positions here responsively.
     @Override
     protected void init() {
         super.init();
         doneBtn.setFocused(false);//Set the btn unfocused upon opening to avoid the mc bug of it staying focused after previously being clicked.

         final int x = (this.width - 200) / 2;
         final int y = this.height / 2 - 50;

         preventNonTor.setPosition(x-30, y-100);
         sepStr.setPosition(x-30, y-75);
         doneBtn.setPosition(x+40, y+200);
         left.setPosition(x-30, y-30);
         right.setPosition(x+35, y-30);
         upper.setPosition(x+115, y-30);
         lower.setPosition(x+182, y-30);
         torDNS.setPosition(x-30, y+10);
//Registed them as selectable but not drawable.
         this.addSelectableChild(preventNonTor);
         this.addSelectableChild(sepStr);
         this.addSelectableChild(doneBtn);
         this.addSelectableChild(left);
         this.addSelectableChild(right);
         this.addSelectableChild(upper);
         this.addSelectableChild(lower);
         this.addSelectableChild(torDNS);
     }
//Override the close func of the UI so it returns to the multiplayer screen when pressing ESC, not the title screen.
     @Override
     public void close() {
         CloseR();
     }
//Return to the mp screen correctly by setting it's parent as the TitleScreen, so it goes there after closing it.
     public static void CloseR() {
        Objects.requireNonNull(MinecraftClient.getInstance()).setScreen(new MultiplayerScreen(new TitleScreen()));
     }
//Override the render func, so we can render the elements above the window.
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        Main.renderWindow(context, (this.width - 200) / 2-50, this.height / 2 - 170, 300, 350, "McOverTor Settings");
        preventNonTor.render(context, mouseX, mouseY, delta);
        sepStr.render(context, mouseX, mouseY, delta);
        doneBtn.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal("Tor Buttons position:"), (this.width - 200) / 2+100, this.height / 2 -100, 0xFFFFFF);
        left.render(context, mouseX, mouseY, delta);
        right.render(context, mouseX, mouseY, delta);
        upper.render(context, mouseX, mouseY, delta);
        lower.render(context, mouseX, mouseY, delta);
        torDNS.render(context, mouseX, mouseY, delta);
//Line between the vertical and horizontal pos settings.
        context.drawVerticalLine(this.width/2, this.height/2-50, this.height/2-90, Color.WHITE.getRGB());
    }

}
