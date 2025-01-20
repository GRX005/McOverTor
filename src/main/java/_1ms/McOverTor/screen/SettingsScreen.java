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

    private static final SettCheckBox preventNonTor = new SettCheckBox(0, 0, Text.literal("Prevent non-Tor connections"), "torOnly");
    private static final SettCheckBox sepStr = new SettCheckBox(0, 0, Text.literal("Stream separation"), "separateStreams");
    private static final SettCheckBox left = new SettCheckBox(0, 0, Text.literal("Left"), "!isRight");
    private static final SettCheckBox right = new SettCheckBox(0, 0, Text.literal("Right"), "isRight");
    private static final SettCheckBox upper = new SettCheckBox(0, 0, Text.literal("Upper"), "isUpper");
    private static final SettCheckBox lower = new SettCheckBox(0, 0, Text.literal("Lower"), "!isUpper");
    private static final ButtonWidget doneBtn = ButtonWidget.builder(Text.literal("Done"), btn -> CloseR()).dimensions(0,0,120, 20).build();

    public SettingsScreen() {
        super(Text.literal("McOverTor Settings"));
    }

     @Override
     protected void init() {
         super.init();
         doneBtn.setFocused(false);
         sepStr.setTooltip(Tooltip.of(Text.literal("Get a new IP every time you join a server.")));

         final int x = (this.width - 200) / 2;
         final int y = this.height / 2 - 10;

         preventNonTor.setPosition(x-30, y-100);
         sepStr.setPosition(x-30, y-75);
         doneBtn.setPosition(x+40, y+200);
         left.setPosition(x-30, y-30);
         right.setPosition(x+40, y-30);
         upper.setPosition(x+115, y-30);
         lower.setPosition(x+182, y-30);

         this.addSelectableChild(preventNonTor);
         this.addSelectableChild(sepStr);
         this.addSelectableChild(doneBtn);
         this.addSelectableChild(left);
         this.addSelectableChild(right);
         this.addSelectableChild(upper);
         this.addSelectableChild(lower);
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
        preventNonTor.render(context, mouseX, mouseY, delta);
        sepStr.render(context, mouseX, mouseY, delta);
        doneBtn.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal("Tor Buttons position:"), (this.width - 200) / 2+100, (this.height / 2 - 10)-50, 0xFFFFFF);
        left.render(context, mouseX, mouseY, delta);
        right.render(context, mouseX, mouseY, delta);
        upper.render(context, mouseX, mouseY, delta);
        lower.render(context, mouseX, mouseY, delta);

        context.drawVerticalLine(this.width/2, this.height/2-10, this.height/2-50, Color.WHITE.getRGB());
    }

}
