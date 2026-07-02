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

package _1ms.McOverTor.mixin;

import _1ms.McOverTor.manager.SettingsMgr;
import _1ms.McOverTor.manager.TorManager;
import _1ms.McOverTor.manager.TorOption;
import _1ms.McOverTor.screen.ChangeIP;
import _1ms.McOverTor.screen.Region;
import _1ms.McOverTor.screen.Settings;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static _1ms.McOverTor.manager.TorManager.progress;

@Mixin(MultiplayerScreen.class)
abstract class MpButtonsAdd extends Screen {

    @Unique
    private ButtonWidget newIpButton;

    @Unique
    private TextIconButtonWidget settButton;

    @Unique
    private TextIconButtonWidget regButton;

    @Unique
    private ButtonWidget torButton;

    protected MpButtonsAdd(Text title) {
        super(title);
    }

    @Inject(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;refreshWidgetPositions()V"
            )
    )
    public void init(CallbackInfo ci) {
        newIpButton = ButtonWidget.builder(Text.literal("Change IP"), b -> this.client.setScreen(new ChangeIP()))
                .size(95,21).build();

        newIpButton.active = progress >= 100;

        settButton = TextIconButtonWidget.builder(Text.literal("Tor options"),b -> this.client.setScreen(new Settings()),true)
                .dimension(26,26).texture(Identifier.of("mcovertor","settings"),22,22).build();

        regButton = TextIconButtonWidget.builder(Text.literal("Tor regions"),b -> this.client.setScreen(new Region()),true)
                .dimension(26,26).texture(Identifier.of("mcovertor", "globe"),22,22).build();

        torButton = ButtonWidget.builder(Text.literal("Tor: "+(progress==100?"§aON":"§cOFF")),b -> TorBtnFunc()).size(95,21).build();

        this.addDrawableChild(newIpButton);
        this.addDrawableChild(settButton);
        this.addDrawableChild(regButton);
        this.addDrawableChild(torButton);
    }

    @Inject(method = "refreshWidgetPositions()V", at = @At("HEAD"))
    public void refresh(CallbackInfo ci) {
        final boolean isUpper = SettingsMgr.get(TorOption.isUpper);
        final boolean isRight = SettingsMgr.get(TorOption.isRight);

        newIpButton.setPosition(calcX(isUpper, isRight, 205, 105, 110, 10), isUpper ? 5 : this.height-27);
        settButton.setPosition(calcX(isUpper, isRight, 235, 133, 210, 107), isUpper ? 3 : this.height-56);
        regButton.setPosition(calcX(isUpper, isRight, 265, 133, 240, 107), isUpper ? 3 : this.height-28);
        torButton.setPosition(isRight ? this.width-105 : 10, isUpper ? 5 : this.height - 52);
    }

    @Unique
    private void TorBtnFunc() {
        if (progress < 100) {
            TorManager.startTor();
        } else {
            TorManager.exitTorAsync(true)
                    .thenAcceptAsync(a->this.client.setScreen(new MultiplayerScreen(new TitleScreen())),this.client);
        }
    }

    @Unique
    private int calcX(boolean isUpper, boolean isRight, int upRightOff, int lowRightOff, int upLeftOff, int lowLeftOff) {
        if (isRight)
            return isUpper ? this.width - upRightOff : this.width - lowRightOff;
        else
            return isUpper ? upLeftOff : lowLeftOff;
    }

}