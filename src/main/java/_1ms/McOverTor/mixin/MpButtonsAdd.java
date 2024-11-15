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

package _1ms.McOverTor.mixin;

import _1ms.McOverTor.manager.TorManager;
import _1ms.McOverTor.screen.ChangeIPScreen;
import _1ms.McOverTor.screen.ConnectScreen;
import _1ms.McOverTor.screen.SettingsScreen;
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

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(MultiplayerScreen.class)
public abstract class MpButtonsAdd extends Screen {

    @Unique
    private static final Identifier settIcon = Identifier.of("mcovertor", "textures/settings.png");

    @Unique
    private static final ButtonWidget newIpButton = ButtonWidget.builder(
            Text.literal("Change IP"),
            (buttonWidget) -> Objects.requireNonNull(MinecraftClient.getInstance()).setScreen(new ChangeIPScreen())
    ).dimensions(0, 0, 95, 21).build();

    @Unique
    private static int i = 0;
    @Unique
    private static final ButtonWidget settButton = new ButtonWidget(0, 0, 26, 26, Text.empty(), button -> Objects.requireNonNull(MinecraftClient.getInstance()).setScreen(new SettingsScreen()), Supplier::get) {
        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
            try {
                //context.drawTexture(RenderLayer::getGuiTextured, settIcon, this.getX() + 2, this.getY() + 2, 0, 0, 22, 21, 22, 21);
                // Use the 1.21.3 method if available (Using runtime intermediary function name of the above)
                Method drawTextureMethod = DrawContext.class.getMethod(
                        "method_25290",
                        Function.class, Identifier.class, int.class, int.class, float.class, float.class, int.class, int.class, int.class, int.class
                );
                drawTextureMethod.invoke(
                        context,
                        (Function<Identifier, RenderLayer>) id -> RenderLayer.getGuiTextured(settIcon),
                        settIcon, this.getX() + 2, this.getY() + 2,
                        0.0f, 0.0f, 22, 21, 22, 21
                );
            } catch (ReflectiveOperationException e) {
                // Fall back to 1.21.1
                try {
                    Method drawTextureMethod = DrawContext.class.getMethod(
                            "method_25290",
                            Identifier.class, int.class, int.class, float.class, float.class, int.class, int.class, int.class, int.class
                    );
                    drawTextureMethod.invoke(
                            context,
                            settIcon, this.getX() + 2, this.getY() + 2,
                            0, 0, 22, 21, 22, 21
                    );
                } catch (ReflectiveOperationException ex) {
                    if(i==0) {
                        System.out.println("[McOverTor] Error while trying to render settings btn icon in compatibility mode: " + ex);
                        i++;
                    }
                }
            }
        }
    };

    protected MpButtonsAdd(Text title) {
        super(title);
    }

    @Inject(method = "init()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;updateButtonActivationStates()V"))
    public void multiplayerGuiOpen(CallbackInfo ci) {
        newIpButton.setPosition(10, this.height - 30);
        //settingsB.setPosition(110, this.height -50);
        settButton.setPosition(107, this.height -45);
        if(i!=0)
            i--;

        newIpButton.active = ConnectScreen.progress >= 100;
        newIpButton.setFocused(false);
        settButton.setFocused(false);

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Tor: " + (ConnectScreen.progress == 100 ? "§aON" : "§cOFF")),
                (buttonWidget) -> {
                    if (ConnectScreen.progress < 100) {
                        Objects.requireNonNull(MinecraftClient.getInstance()).setScreen(new ConnectScreen());
                        TorManager.launchTor();
                    } else {
                        TorManager.exitTor(true);
                        Objects.requireNonNull(MinecraftClient.getInstance()).setScreen(new MultiplayerScreen(new TitleScreen()));
                    }
                }
        ).dimensions(10, this.height - 55, 95, 21).build()); //We init this here otherwise it'll stay focused for some reason after turning it off.
        this.addDrawableChild(newIpButton);
        this.addDrawableChild(settButton);
    }
}