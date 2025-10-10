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

import _1ms.McOverTor.Main;
import _1ms.McOverTor.manager.SettingsMgr;
import _1ms.McOverTor.manager.TorManager;
import _1ms.McOverTor.manager.TorOption;
import _1ms.McOverTor.screen.ChangeIP;
import _1ms.McOverTor.screen.Region;
import _1ms.McOverTor.screen.Settings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.function.Supplier;

import static _1ms.McOverTor.Main.logger;
import static _1ms.McOverTor.manager.TorManager.progress;

@Mixin(MultiplayerScreen.class)
abstract class MpButtonsAdd extends Screen {

    @Unique
    private final static ButtonWidget newIpButton = ButtonWidget.builder(
            Text.literal("Change IP"),
            buttonWidget -> Objects.requireNonNull(MinecraftClient.getInstance()).setScreen(new ChangeIP())
    ).dimensions(0, 0, 95, 21).build();

    @Unique
    private static final Identifier settIcon = Identifier.of("mcovertor","textures/settings.png");

    @Unique
    private static final Identifier regIcon = Identifier.of("mcovertor","textures/globe.png");
//Init and register sett and reg btn textures the first time this class is called.
    static {
        for (int i = 0; i < 2; i++) {
            String toReq = "assets/mcovertor/textures/" + (i==0 ? "settings.png" : "globe.png");
            try (var inpStr=Main.class.getClassLoader().getResourceAsStream(toReq); var natImg=NativeImage.read(Objects.requireNonNull(inpStr))) {
                MinecraftClient.getInstance().getTextureManager().registerTexture(i==0 ? settIcon : regIcon, new NativeImageBackedTexture(()->"TorIcon", natImg));
            } catch (Exception e) {
                logger.error("Error while loading icon native image.\n{}", e.toString());
            }
        }
    }
//Render custom icons
    @Unique
    private final ButtonWidget regButton = new ButtonWidget(0,0,26,26,Text.empty(), btn->Objects.requireNonNull(MinecraftClient.getInstance()).setScreen(new Region()), Supplier::get) {
      @Override
      public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
          super.renderWidget(context, mouseX, mouseY, delta);
          context.drawTexture(RenderPipelines.GUI_TEXTURED, regIcon, this.getX() + 2, this.getY() + 2, 0, 0, 22, 21, 22, 21);
      }
    };

    @Unique
    private final ButtonWidget settButton = new ButtonWidget(0, 0, 26, 26, Text.empty(), button -> Objects.requireNonNull(MinecraftClient.getInstance()).setScreen(new Settings()), Supplier::get) {
        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, settIcon, this.getX() + 2, this.getY() + 2, 0, 0, 22, 21, 22, 21);
        }
    };

    protected MpButtonsAdd(Text title) {
        super(title);
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    public void multiplayerGuiOpen(CallbackInfo ci) {
        newIpButton.active = progress >= 100;
        //without this it'll stay focused after click for some reason
        newIpButton.setFocused(false);
//        settButton.setFocused(false);
//        regButton.setFocused(false);
        this.addDrawableChild(newIpButton);
        this.addDrawableChild(settButton);
        this.addDrawableChild(regButton);
        //regButton.setAlpha(1);
       // this.addDrawableChild(torButton);
        
    }
    @Unique
    private ButtonWidget torButton;

    @Inject(method = "refreshWidgetPositions()V", at = @At("HEAD"))
    public void refresh(CallbackInfo ci) {
        final boolean isUpper = SettingsMgr.get(TorOption.isUpper);
        final boolean isRight = SettingsMgr.get(TorOption.isRight);

        newIpButton.setPosition(calcX(isUpper, isRight, 205, 105, 110, 10), isUpper ? 5 : this.height-27);
        settButton.setPosition(calcX(isUpper, isRight, 235, 133, 210, 107), isUpper ? 3 : this.height-56);
        regButton.setPosition(calcX(isUpper, isRight, 265, 133, 240, 107), isUpper ? 3 : this.height-28);
        if(torButton!=null)
            this.remove(torButton);
        //We init this here and re-add every time, otherwise it'll stay focused for some reason after turning it off.
        torButton = ButtonWidget.builder(Text.literal("Tor: " + (progress == 100 ? "§aON" : "§cOFF")), MpButtonsAdd::TorBtnFunc).dimensions(isRight ? this.width-105 : 10, isUpper ? 5 : this.height - 52, 95, 21).build();

        this.addDrawableChild(torButton);
    }

    @Unique
    private static void TorBtnFunc(ButtonWidget ignored) {
        if (progress < 100) {
            TorManager.startTor();
        } else {
            TorManager.exitTor(true);
            Objects.requireNonNull(MinecraftClient.getInstance()).setScreen(new MultiplayerScreen(new TitleScreen()));
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