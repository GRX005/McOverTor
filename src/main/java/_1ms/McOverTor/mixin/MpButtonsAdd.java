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

package _1ms.McOverTor.mixin;

import _1ms.McOverTor.manager.SettingsMgr;
import _1ms.McOverTor.manager.TorManager;
import _1ms.McOverTor.manager.TorOption;
import _1ms.McOverTor.screen.ChangeIPScreen;
import _1ms.McOverTor.screen.SettingsScreen;
import _1ms.McOverTor.screen.TorScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.function.Supplier;

import static _1ms.McOverTor.manager.TorManager.progress;

@Mixin(MultiplayerScreen.class)
abstract class MpButtonsAdd extends Screen {
    @Unique
    private static final ButtonWidget newIpButton = ButtonWidget.builder(
            Text.literal("Change IP"),
            buttonWidget -> Objects.requireNonNull(MinecraftClient.getInstance()).setScreen(new ChangeIPScreen())
    ).dimensions(0, 0, 95, 21).build();

    @Unique
    private static final ButtonWidget settButton = new ButtonWidget(0, 0, 26, 26, Text.empty(), button -> Objects.requireNonNull(MinecraftClient.getInstance()).setScreen(new SettingsScreen()), Supplier::get) {
        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
            final Identifier settIcon = Identifier.of("mcovertor", "textures/settings.png");
            context.drawTexture(RenderLayer::getGuiTextured, settIcon, this.getX() + 2, this.getY() + 2, 0, 0, 22, 21, 22, 21);
        }
    };

    protected MpButtonsAdd(Text title) {
        super(title);
    }

    @Inject(method = "init()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;updateButtonActivationStates()V"))
    public void multiplayerGuiOpen(CallbackInfo ci) {
        final boolean isUpper = SettingsMgr.get(TorOption.isUpper);
        final boolean isRight = SettingsMgr.get(TorOption.isRight);

        newIpButton.setPosition(calcX(isUpper, isRight, 205, 105, 110, 10), isUpper ? 5 : this.height - 30);
        settButton.setPosition(calcX(isUpper, isRight, 235, 133, 210, 107), isUpper ? 3 : this.height -45);

        newIpButton.active = progress >= 100;
        newIpButton.setFocused(false);
        settButton.setFocused(false);

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Tor: " + (progress == 100 ? "§aON" : "§cOFF")),
                buttonWidget -> {
                    if (progress < 100) {
                        Objects.requireNonNull(MinecraftClient.getInstance()).setScreen(new TorScreen());
                        TorManager.launchTor();
                    } else {
                        TorManager.exitTor(true);
                        Objects.requireNonNull(MinecraftClient.getInstance()).setScreen(new MultiplayerScreen(new TitleScreen()));
                    }
                }
        ).dimensions(isRight ? this.width-105 : 10, isUpper ? 5 : this.height - 55, 95, 21).build()); //We init this here otherwise it'll stay focused for some reason after turning it off.
        this.addDrawableChild(newIpButton);
        this.addDrawableChild(settButton);
    }

    @Unique
    private int calcX(boolean isUpper, boolean isRight, int upRightOff, int lowRightOff, int upLeftOff, int lowLeftOff) {
        if (isRight)
            return isUpper ? this.width - upRightOff : this.width - lowRightOff;
        else
            return isUpper ? upLeftOff : lowLeftOff;
    }
}