package _1ms.McOverTor.mixin;

import _1ms.McOverTor.ChangeIPScreen;
import _1ms.McOverTor.ConnectingScreen;
import _1ms.McOverTor.TorManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(MultiplayerScreen.class)
public class MultiplayerButton extends Screen {

    protected MultiplayerButton(Text title) {
        super(title);
    }

    @Inject(method = "init()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;updateButtonActivationStates()V"))
    public void multiplayerGuiOpen(CallbackInfo ci) {
        final MultiplayerScreen screen = (MultiplayerScreen) (Object) this;

        final ButtonWidget torStatusButton = ButtonWidget.builder(
                Text.literal("Tor: " + (ConnectingScreen.progress == 100 ? "§aON" : "§cOFF")),
                (buttonWidget) -> {
                    if (ConnectingScreen.progress < 100) {
                        Objects.requireNonNull(this.client).setScreen(new ConnectingScreen());
                        TorManager.launchTor();
                    } else {
                        TorManager.exitTor();
                        Objects.requireNonNull(this.client).setScreen(new MultiplayerScreen(new TitleScreen()));
                    }
                }
        ).dimensions(10, screen.height - 55, 95, 20).build();

        final ButtonWidget newIpButton = ButtonWidget.builder(
                Text.literal("Change IP"),
                (buttonWidget) -> Objects.requireNonNull(this.client).setScreen(new ChangeIPScreen())
        ).dimensions(10, screen.height - 30, 95, 20).build();

        //final ButtonWidget settingsButton = ButtonWidget.builder(Text.literal(""), (buttonWidget) -> {
        //System.out.println("Settings clicked!");
        //    // TODO: Ide majd be kell rakni a SettingsScreen-t ha kész
        //}).dimensions(140, screen.height - 55, 43, 45).build();

        this.addDrawableChild(torStatusButton);
        this.addSelectableChild(torStatusButton);
        this.addDrawableChild(newIpButton);
        this.addSelectableChild(newIpButton);
        //this.addDrawableChild(settingsButton);
        //this.addSelectableChild(settingsButton);
    }

//    @Override
//    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
//        super.render(context, mouseX, mouseY, delta);
//
//        // TODO: Ide majd a settings icont ha túl kicsi lenne
//    }
}