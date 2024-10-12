package _1ms.McOverTor;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.Objects;

public class SettingsScreen extends Screen {
    public SettingsScreen() {
        super(Text.literal("Settings"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;


        final ButtonWidget positionButton = ButtonWidget.builder(
                Text.literal("Position: " + OptionsManager.buttonPosition),
                (button) -> {
                    OptionsManager.saveOptions();
                    button.setMessage(Text.literal("Position: " + OptionsManager.buttonPosition));
                }
        ).dimensions(centerX - 100, centerY - 40, 200, 20).build();
        final ButtonWidget torConnectionButton = ButtonWidget.builder(
                Text.literal("Only Tor Connection: " + (OptionsManager.onlyTorConnection ? "On" : "Off")),
                (button) -> {
                    OptionsManager.onlyTorConnection = !OptionsManager.onlyTorConnection;
                    button.setMessage(Text.literal("Only Tor Connection: " + (OptionsManager.onlyTorConnection ? "On" : "Off")));
                    OptionsManager.saveOptions();
                }
        ).dimensions(centerX - 100, centerY - 10, 200, 20).build();

        final ButtonWidget updateCheckerButton = ButtonWidget.builder(
                Text.literal("Update Checker: " + (OptionsManager.updateCheckerEnabled ? "On" : "Off")),
                (button) -> {
                    OptionsManager.updateCheckerEnabled = !OptionsManager.updateCheckerEnabled;
                    button.setMessage(Text.literal("Update Checker: " + (OptionsManager.updateCheckerEnabled ? "On" : "Off")));
                    OptionsManager.saveOptions();
                }
        ).dimensions(centerX - 100, centerY + 20, 200, 20).build();

        final ButtonWidget ipCheckerButton = ButtonWidget.builder(
                Text.literal("IP/Connection Checker: " + (OptionsManager.ipConnectionCheckerEnabled ? "Enabled" : "Disabled")),
                (button) -> {
                    OptionsManager.ipConnectionCheckerEnabled = !OptionsManager.ipConnectionCheckerEnabled;
                    button.setMessage(Text.literal("IP/Connection Checker: " + (OptionsManager.ipConnectionCheckerEnabled ? "Enabled" : "Disabled")));
                    OptionsManager.saveOptions();
                }
        ).dimensions(centerX - 100, centerY + 50, 200, 20).build();

        final ButtonWidget backButton = ButtonWidget.builder(
                Text.literal("Back"),
                (button) -> Objects.requireNonNull(this.client).setScreen(new MultiplayerScreen(new TitleScreen()))
        ).dimensions(centerX - 100, centerY + 80, 200, 20).build();

        this.addDrawableChild(positionButton);
        this.addDrawableChild(torConnectionButton);
        this.addDrawableChild(updateCheckerButton);
        this.addDrawableChild(ipCheckerButton);
        this.addDrawableChild(backButton);
        this.addSelectableChild(positionButton);
        this.addSelectableChild(torConnectionButton);
        this.addSelectableChild(updateCheckerButton);
        this.addSelectableChild(ipCheckerButton);
        this.addSelectableChild(backButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        RenderSystem.disableBlend();
    }
}
