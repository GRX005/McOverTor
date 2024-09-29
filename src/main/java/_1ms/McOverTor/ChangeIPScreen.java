package _1ms.McOverTor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ChangeIPScreen extends Screen {
    private ButtonWidget closeButton;
    private ButtonWidget closeButton1;
    public static boolean isDone = false;

    public ChangeIPScreen() {
        super(Text.literal("Changing IP"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (ConnectingScreen.progress < 100) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§cYou are not connected yet."), this.width / 2, this.height / 2 - 30, 0xFFFFFF);

            if (closeButton == null) {
                closeButton = ButtonWidget.builder(Text.literal("Okay"), (buttonWidget) -> MinecraftClient.getInstance()
                                .setScreen(new MultiplayerScreen(new TitleScreen())))
                        .dimensions(this.width / 2 - 60, this.height / 2 + 30, 120, 20)
                        .build();
            }
            this.addDrawableChild(closeButton);
            renderProgressBar(context);
            return;
        }

        if (isDone) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§aYou've successfully changed IP."), this.width / 2, this.height / 2 - 30, 0xFFFFFF);

            if (closeButton1 == null) {
                closeButton1 = ButtonWidget.builder(Text.literal("Okay"), (buttonWidget) -> {
                            MinecraftClient.getInstance().setScreen(new MultiplayerScreen(new TitleScreen()));
                            isDone = false;
                        })
                        .dimensions(this.width / 2 - 60, this.height / 2 + 30, 120, 20)
                        .build();
            }
            this.addDrawableChild(closeButton1);
            return;
        }

        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§aChanging IP..."), this.width / 2, this.height / 2 - 30, 0xFFFFFF);
        TorManager.changeCircuits();
    }

    public void renderProgressBar(DrawContext context) {
        int barWidth = 200;
        int barHeight = 20;
        int x = (this.width - barWidth) / 2;
        int y = this.height / 2 - 10;

        context.fill(x + 5, y + 5, x + barWidth + 5, y + barHeight + 5, 0x66000000);
        context.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xFFD3D3D3);
        context.fill(x, y, x + (ConnectingScreen.progress * 2), y + barHeight, 0xFF00FF00);
        context.fill(x + (ConnectingScreen.progress * 2), y, x + barWidth, y + barHeight, 0xFF000000);
    }
}
