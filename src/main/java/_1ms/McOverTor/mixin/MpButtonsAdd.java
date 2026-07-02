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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static _1ms.McOverTor.manager.TorManager.progress;

@Mixin(JoinMultiplayerScreen.class)
abstract class MpButtonsAdd extends Screen {

    @Unique
    private Button newIpButton;
    @Unique
    private SpriteIconButton settButton;
    @Unique
    private SpriteIconButton regButton;
    @Unique
    private Button torButton;

    protected MpButtonsAdd(Component title) {
        super(title);
    }
    @Inject(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/multiplayer/JoinMultiplayerScreen;repositionElements()V"
            )
    )
    public void init(CallbackInfo ci) {
        newIpButton = Button.builder(Component.literal("Change IP"),_ -> this.minecraft.gui.setScreen(new ChangeIP()))
                .size(95,21).build();
        newIpButton.active = progress >= 100;

        settButton = SpriteIconButton.builder(Component.literal("Tor options"),_ -> this.minecraft.gui.setScreen(new Settings()),true)
                .size(26,26).sprite(Identifier.fromNamespaceAndPath("mcovertor","settings"),22,22).build();

        regButton = SpriteIconButton.builder(Component.literal("Tor regions"),_ -> this.minecraft.gui.setScreen(new Region()),true)
                .size(26,26).sprite(Identifier.fromNamespaceAndPath("mcovertor", "globe"),22,22).build();

        torButton = Button.builder(Component.literal("Tor: "+(progress==100?"§aON":"§cOFF")),_ -> TorBtnFunc()).size(95,21).build();

        this.addRenderableWidget(newIpButton);
        this.addRenderableWidget(settButton);
        this.addRenderableWidget(regButton);
        this.addRenderableWidget(torButton);
    }

    @Inject(method = "repositionElements()V", at = @At("HEAD"))
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
                    .thenAcceptAsync(_->this.minecraft.gui.setScreen(new JoinMultiplayerScreen(new TitleScreen())),this.minecraft);
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
