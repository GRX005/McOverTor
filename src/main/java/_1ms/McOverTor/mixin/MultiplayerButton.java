package _1ms.McOverTor.mixin;

import _1ms.McOverTor.ChangeIPScreen;
import _1ms.McOverTor.ConnectingScreen;
import _1ms.McOverTor.SettingsScreen;
import _1ms.McOverTor.TorManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.function.Supplier;

@Mixin(MultiplayerScreen.class)
public class MultiplayerButton extends Screen {

    @Unique
    private static final Identifier settIcon = Identifier.of("mcovertor", "textures/settings.png");

    @Unique
    private static final ButtonWidget newIpButton = ButtonWidget.builder(
            Text.literal("Change IP"),
            (buttonWidget) -> Objects.requireNonNull(MinecraftClient.getInstance()).setScreen(new ChangeIPScreen())
    ).dimensions(0, 0, 95, 21).build();

    @Unique
    private static final ButtonWidget iconButton = new ButtonWidget(0, 0, 26, 26, Text.empty(), button -> Objects.requireNonNull(MinecraftClient.getInstance()).setScreen(new SettingsScreen()), Supplier::get) {
        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            super.renderWidget(context, mouseX, mouseY, delta);
            context.drawTexture(settIcon, this.getX()+2, this.getY()+2, 0, 0, 22, 21, 22, 21);
        }
    };

    protected MultiplayerButton(Text title) {
        super(title);
    }

    @Inject(method = "init()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;updateButtonActivationStates()V"))
    public void multiplayerGuiOpen(CallbackInfo ci) {
        newIpButton.setPosition(10, this.height - 30);
        //settingsB.setPosition(110, this.height -50);
        iconButton.setPosition(107, this.height -45);

        newIpButton.active = ConnectingScreen.progress >= 100;
        newIpButton.setFocused(false);

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Tor: " + (ConnectingScreen.progress == 100 ? "§aON" : "§cOFF")),
                (buttonWidget) -> {
                    if (ConnectingScreen.progress < 100) {
                        Objects.requireNonNull(MinecraftClient.getInstance()).setScreen(new ConnectingScreen());
                        TorManager.launchTor();
                    } else {
                        TorManager.exitTor();
                        Objects.requireNonNull(MinecraftClient.getInstance()).setScreen(new MultiplayerScreen(new TitleScreen()));
                    }
                }
        ).dimensions(10, this.height - 55, 95, 21).build());
        this.addDrawableChild(newIpButton); //We init this here otherwise it'll stay focused for some reason after turning it off.
        this.addDrawableChild(iconButton);
    }

//    @Override
//    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
//        super.render(context, mouseX, mouseY, delta);
//
//        // TODO: Ide majd a settings icont ha túl kicsi lenne
//    }
}