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

import _1ms.McOverTor.manager.SettingsMgr;
import _1ms.McOverTor.manager.TorOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import static _1ms.McOverTor.Main.drawBorder;

public class SettCheckBox extends AbstractWidget {
    private final TorOption val;
    private final String strVal;

    public SettCheckBox(int x, int y, Component text, TorOption val) {
        super(x, y, 16, 15, text);
        this.val = val;
        this.strVal = null;
    }
    public SettCheckBox(int x, int y, Component text, String val) {
        super(x, y, 16, 15, text);
        this.strVal = val;
        this.val = null;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        final int borderColor = 0xFFFFFFFF;

        final int x = this.getX();
        final int y = this.getY();
        //Background
        graphics.fill(x, y, x + this.width, y + this.height, 0x80000000);

        //Border
        drawBorder(graphics, x, y, this.width, this.height, borderColor);

        // Draw the "X" if checked
        boolean stuff;
        if(strVal == null)
            stuff = SettingsMgr.get(val);
        else
            stuff = SettingsMgr.get(strVal);
        if (stuff) {
            final int x1 = x + 4;
            final int y1 = y + 4;
            final int size = 7;
            final int thickness = 2;

            for (int i = 0; i < size; i++) {
                // Top-left to bottom-right diagonal with thickness
                for (int t = 0; t < thickness; t++) {
                    graphics.verticalLine(x1 + i + t, y1 + i, y1 + i, borderColor);
                }

                // Bottom-left to top-right diagonal with thickness
                for (int t = 0; t < thickness; t++) {
                    graphics.verticalLine(x1 + i + t, y1 + size - i - 1, y1 + size - i - 1, borderColor);
                }
            }
        }
        //Text
        graphics.text(Minecraft.getInstance().font, this.getMessage(), x + 24, y + 4, borderColor);
    }

    @Override
    public void onClick(@NonNull MouseButtonEvent click, boolean doubled) {
        if(strVal == null)
            SettingsMgr.flip(val);
        else
            SettingsMgr.flip(strVal);
    }

    public SettCheckBox tooltip(String tip) {
        this.setTooltip(Tooltip.create(Component.literal(tip)));
        return this;
    }

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput builder) {
        this.defaultButtonNarrationText(builder);
    }

}
