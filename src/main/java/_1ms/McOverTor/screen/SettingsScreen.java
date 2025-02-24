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

public class SettingsScreen extends Screen {
    private record Buttons(SettCheckBox preventNonTor, SettCheckBox sepStr, SettCheckBox left, SettCheckBox right, SettCheckBox upper, SettCheckBox lower,
                           SettCheckBox torDNS, ButtonWidget doneBtn){}
    private static final Buttons btns = new Buttons(
            new SettCheckBox(0, 0, Text.literal("Prevent non-Tor connections"), TorOption.torOnly),
            new SettCheckBox(0, 0, Text.literal("Stream separation"), TorOption.sepStreams),
            new SettCheckBox(0, 0, Text.literal("Left"), "!"+TorOption.isRight),
            new SettCheckBox(0, 0, Text.literal("Right"), TorOption.isRight),
            new SettCheckBox(0, 0, Text.literal("Upper"), TorOption.isUpper),
            new SettCheckBox(0, 0, Text.literal("Lower"), "!"+TorOption.isUpper),
            new SettCheckBox(0,0, Text.literal("Resolve DNS using Tor"), TorOption.useTorDNS),
            ButtonWidget.builder(Text.literal("Done"), btn -> CloseR()).dimensions(0, 0, 120, 20).build()
    );

    public SettingsScreen() {
        super(Text.literal("McOverTor Settings"));
    }

     @Override
     protected void init() {
         super.init();
         btns.doneBtn.setFocused(false);
         btns.preventNonTor.setTooltip(Tooltip.of(Text.literal("Makes it so you cannot ping or connect to servers if Tor isn't turned on, to prevent accidents.")));
         btns.sepStr.setTooltip(Tooltip.of(Text.literal("Get a new IP every time you join a server.")));
         btns.torDNS.setTooltip(Tooltip.of(Text.literal("§aPros: §fDNS queries won't leak your IP, more secure.\nYou can connect to .onion server addresses.\n§cCons: §fTor might fail to resolve some domains, so it is turned off by default.")));

         final int x = (this.width - 200) / 2;
         final int y = this.height / 2 - 10;

         btns.preventNonTor.setPosition(x-30, y-100);
         btns.sepStr.setPosition(x-30, y-75);
         btns.doneBtn.setPosition(x+40, y+200);
         btns.left.setPosition(x-30, y-30);
         btns.right.setPosition(x+35, y-30);
         btns.upper.setPosition(x+115, y-30);
         btns.lower.setPosition(x+182, y-30);
         btns.torDNS.setPosition(x-30, y+10);

         this.addSelectableChild(btns.preventNonTor);
         this.addSelectableChild(btns.sepStr);
         this.addSelectableChild(btns.doneBtn);
         this.addSelectableChild(btns.left);
         this.addSelectableChild(btns.right);
         this.addSelectableChild(btns.upper);
         this.addSelectableChild(btns.lower);
         this.addSelectableChild(btns.torDNS);
     }

     @Override
     public void close() {
         CloseR();
     }

     public static void CloseR() {
        Objects.requireNonNull(MinecraftClient.getInstance()).setScreen(new MultiplayerScreen(new TitleScreen()));
     }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        Main.renderWindow(context, (this.width - 200) / 2-50, this.height / 2 - 130, 300, 350, "McOverTor Settings");
        btns.preventNonTor.render(context, mouseX, mouseY, delta);
        btns.sepStr.render(context, mouseX, mouseY, delta);
        btns.doneBtn.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal("Tor Buttons position:"), (this.width - 200) / 2+100, (this.height / 2 - 10)-50, 0xFFFFFF);
        btns.left.render(context, mouseX, mouseY, delta);
        btns.right.render(context, mouseX, mouseY, delta);
        btns.upper.render(context, mouseX, mouseY, delta);
        btns.lower.render(context, mouseX, mouseY, delta);
        btns.torDNS.render(context, mouseX, mouseY, delta);

        context.drawVerticalLine(this.width/2, this.height/2-10, this.height/2-50, Color.WHITE.getRGB());
    }

}
