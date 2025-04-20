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
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
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
    private static Identifier settIcon;

    @Unique
    private static Identifier regIcon;

    @Unique
    private final ButtonWidget regButton = new ButtonWidget(0,0,26,26,Text.empty(), btn->Objects.requireNonNull(MinecraftClient.getInstance()).setScreen(new Region()), Supplier::get) {
      @Override
      public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
          super.renderWidget(context, mouseX, mouseY, delta);
          setIcon(this, context,false);
      }
    };

    @Unique
    private final ButtonWidget settButton = new ButtonWidget(0, 0, 26, 26, Text.empty(), button -> Objects.requireNonNull(MinecraftClient.getInstance()).setScreen(new Settings()), Supplier::get) {
        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
            setIcon(this,context,true);
        }
    };

    protected MpButtonsAdd(Text title) {
        super(title);
    }

    @Inject(method = "init()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;updateButtonActivationStates()V"))
    public void multiplayerGuiOpen(CallbackInfo ci) {
        final boolean isUpper = SettingsMgr.get(TorOption.isUpper);
        final boolean isRight = SettingsMgr.get(TorOption.isRight);

        newIpButton.setPosition(calcX(isUpper, isRight, 205, 105, 110, 10), isUpper ? 5 : this.height-30);
        settButton.setPosition(calcX(isUpper, isRight, 235, 133, 210, 107), isUpper ? 3 : this.height-59);
        regButton.setPosition(calcX(isUpper, isRight, 265, 133, 240, 107), isUpper ? 3 : this.height-31);

        newIpButton.active = progress >= 100;
        newIpButton.setFocused(false);
        settButton.setFocused(false);
        regButton.setFocused(false);

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Tor: " + (progress == 100 ? "§aON" : "§cOFF")), MpButtonsAdd::TorBtnFunc).dimensions(isRight ? this.width-105 : 10, isUpper ? 5 : this.height - 55, 95, 21).build()); //We init this here otherwise it'll stay focused for some reason after turning it off.
        this.addDrawableChild(newIpButton);
        this.addDrawableChild(settButton);

        this.addDrawableChild(regButton);
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

    //Reflection to get private contructor, done this way as the .of() func's intermediary changes below 1.21.
    @Unique
    private static Identifier reflectIdent(boolean sett) {
        try {
            Constructor<?> identConst = Identifier.class.getDeclaredConstructor(String.class, String.class);
            identConst.setAccessible(true);
            String toRet = sett ? "textures/settings.png":"textures/globe.png";
            return (Identifier) identConst.newInstance("mcovertor", toRet);
        } catch (ReflectiveOperationException e) {
            logger.error("Identifier getter reflection error.");
            throw new RuntimeException(e);
        }
    }

    @Unique
    private static NativeImageBackedTexture reflectTexture(InputStream io){
        NativeImage image;
        try {//READ img
            image = NativeImage.read(io);
        } catch (Exception e) {
            logger.error("Failed to read settings IMG.");
            throw new RuntimeException(e);
        }
        try {
            // Try the modern constructor: Supplier<String> and NativeImage
            Constructor<?> constructor = NativeImageBackedTexture.class.getConstructor(Supplier.class, NativeImage.class);
            return (NativeImageBackedTexture) constructor.newInstance((Supplier<String>)() -> "TorSettingsIcon"+ new Random().nextInt(), image);
        } catch (NoSuchMethodException e) {
            // Older version: only NativeImage parameter
            try {
                Constructor<?> constructor = NativeImageBackedTexture.class.getConstructor(NativeImage.class);
                return (NativeImageBackedTexture) constructor.newInstance(image);
                // Use 'texture' as needed
            } catch (Exception ex) {
                throw new RuntimeException("Failed to create NativeImageBackedTexture with one parameter", ex);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create NativeImageBackedTexture", e);
        }
    }

    @Unique
    public void setIcon(ButtonWidget btn, DrawContext context, boolean sett) {
        try {
            if(settIcon == null && sett || regIcon == null && !sett) { //Load at first render try, once.
                String toReq = sett ? "assets/mcovertor/textures/settings.png" : "assets/mcovertor/textures/globe.png";
                try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(toReq)) {
                    assert inputStream != null;
                    if(sett) {
                        settIcon = reflectIdent(true);
                        MinecraftClient.getInstance().getTextureManager().registerTexture(settIcon, reflectTexture(inputStream));
                    }
                    else {
                        regIcon = reflectIdent(false);
                        MinecraftClient.getInstance().getTextureManager().registerTexture(regIcon, reflectTexture(inputStream));
                    }
                } catch (Exception e) {
                    logger.error("Error while loading icon native image.");
                    throw new RuntimeException(e);
                }
            }
            //context.drawTexture(RenderLayer::getGuiTextured, settIcon, this.getX() + 2, this.getY() + 2, 0, 0, 22, 21, 22, 21);
            // Use the 1.21.3 method if available (Using runtime intermediary function name of the above)
            Method drawTextureMethod = DrawContext.class.getMethod(
                    "method_25290",
                    Function.class, Identifier.class, int.class, int.class, float.class, float.class, int.class, int.class, int.class, int.class
            );
            if(sett)
                drawTextureMethod.invoke(
                        context,
                        (Function<Identifier, RenderLayer>) id -> RenderLayer.getGuiTextured(settIcon),
                        settIcon, btn.getX() + 2, btn.getY() + 2,
                        0.0f, 0.0f, 22, 21, 22, 21
                );
            else
                drawTextureMethod.invoke(
                        context,
                        (Function<Identifier, RenderLayer>) id -> RenderLayer.getGuiTextured(regIcon),
                        regIcon, btn.getX() + 2, btn.getY() + 2,
                        0.0f, 0.0f, 22, 21, 22, 21
                );
        } catch (ReflectiveOperationException e) {
            // Fall back to 1.21.1
            try {
                Method drawTextureMethod = DrawContext.class.getMethod(
                        "method_25290",
                        Identifier.class, int.class, int.class, float.class, float.class, int.class, int.class, int.class, int.class
                );
                if(sett)
                    drawTextureMethod.invoke(
                            context,
                            settIcon, btn.getX() + 2, btn.getY() + 2,
                            0, 0, 22, 21, 22, 21
                    );
                else
                    drawTextureMethod.invoke(
                            context,
                            regIcon, btn.getX() + 2, btn.getY() + 2,
                            0, 0, 22, 21, 22, 21
                    );
            } catch (ReflectiveOperationException ignored) {}
        }
    }

}