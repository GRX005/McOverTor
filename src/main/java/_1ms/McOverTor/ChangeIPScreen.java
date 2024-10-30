package _1ms.McOverTor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ChangeIPScreen extends Screen {
    private static final ButtonWidget closeButton = ButtonWidget.builder(Text.literal("Okay"), (buttonWidget) -> RealClose())
            .dimensions(0, 0, 120, 20)
            .build();
    public static boolean isDone = false;

    @Override
    protected void init() {
        super.init();

        closeButton.setFocused(false);
        closeButton.setPosition(this.width / 2 - 60, this.height / 2 + 30);
        this.addSelectableChild(closeButton);
    }

    @Override
    public void close() {
        RealClose();
    }

    public static void RealClose() {
        MinecraftClient.getInstance().setScreen(new MultiplayerScreen(new TitleScreen()));
        isDone = false;
    }

    public ChangeIPScreen() {
        super(Text.literal("Change IP"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        final int x = (this.width - 200) / 2;
        final int yhalf = this.height / 2;
        final int y = yhalf - 10;

        Main.renderWindow(context, x-10, y-30, 220, 100, this.textRenderer, "McOverTor Connection");
        closeButton.render(context, mouseX, mouseY, delta);
        if(!isDone) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§aChanging IP..."), this.width / 2, this.height / 2 - 10, 0xFFFFFF);
            TorManager.changeCircuits();
            return;
        }
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§aYou've successfully changed IP."), this.width / 2, this.height / 2 - 10, 0xFFFFFF);
    }
}
