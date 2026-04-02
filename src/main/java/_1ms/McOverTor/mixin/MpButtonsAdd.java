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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

import static _1ms.McOverTor.manager.TorManager.progress;

@Mixin(JoinMultiplayerScreen.class)
abstract class MpButtonsAdd extends Screen {

    @Unique
    private final static Button newIpButton = Button.builder(
            Component.literal("Change IP"),
            buttonWidget -> Objects.requireNonNull(Minecraft.getInstance()).setScreen(new ChangeIP())
    ).bounds(0, 0, 95, 21).build();

    @Unique
    private final SpriteIconButton settButton = SpriteIconButton.builder(Component.literal("Tor options"),
                btn->Objects.requireNonNull(Minecraft.getInstance()).setScreen(new Settings()),false)
        .size(26,26).sprite(Identifier.fromNamespaceAndPath("mcovertor", "settings"),22,22).build();

    @Unique
    private final SpriteIconButton regButton = SpriteIconButton.builder(Component.literal("Tor regions"),
                    btn->Objects.requireNonNull(Minecraft.getInstance()).setScreen(new Region()),true)
            .size(26,26).sprite(Identifier.fromNamespaceAndPath("mcovertor", "globe"),22,22).build();

    protected MpButtonsAdd(Component title) {
        super(title);
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    public void multiplayerGuiOpen(CallbackInfo ci) {
        newIpButton.active = progress >= 100;
        //without this it'll stay focused after click for some reason
        newIpButton.setFocused(false);
        this.addRenderableWidget(newIpButton);
        this.addRenderableWidget(settButton);
        this.addRenderableWidget(regButton);
    }
    @Unique
    private Button torButton;
//TODO REMOVE UNIQUE INTO SEPARATE CLASS?
    @Inject(method = "repositionElements()V", at = @At("HEAD"))
    public void refresh(CallbackInfo ci) {
        final boolean isUpper = SettingsMgr.get(TorOption.isUpper);
        final boolean isRight = SettingsMgr.get(TorOption.isRight);

        newIpButton.setPosition(calcX(isUpper, isRight, 205, 105, 110, 10), isUpper ? 5 : this.height-27);
        settButton.setPosition(calcX(isUpper, isRight, 235, 133, 210, 107), isUpper ? 3 : this.height-56);
        regButton.setPosition(calcX(isUpper, isRight, 265, 133, 240, 107), isUpper ? 3 : this.height-28);
        if(torButton!=null)
            this.removeWidget(torButton);
        //We init this here and re-add every time, otherwise it'll stay focused for some reason after turning it off.
        torButton = Button.builder(Component.literal("Tor: " + (progress == 100 ? "§aON" : "§cOFF")), MpButtonsAdd::TorBtnFunc).bounds(isRight ? this.width-105 : 10, isUpper ? 5 : this.height - 52, 95, 21).build();

        this.addRenderableWidget(torButton);
    }

    @Unique
    private static void TorBtnFunc(Button ignored) {
        if (progress < 100) {
            TorManager.startTor();
        } else {
            TorManager.exitTor(true);
            Objects.requireNonNull(Minecraft.getInstance()).setScreen(new JoinMultiplayerScreen(new TitleScreen()));
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