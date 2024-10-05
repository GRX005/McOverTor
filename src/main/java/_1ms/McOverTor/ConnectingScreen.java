package _1ms.McOverTor;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.Objects;

public class ConnectingScreen extends Screen {
    public static volatile int progress = 0;
    public static String message = "";
    private ButtonWidget closeButton;

    public ConnectingScreen() {
        super(Text.literal("Connecting to Tor"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int barWidth = 200;
        int barHeight = 20;
        int x = (this.width - barWidth) / 2;
        int y = this.height / 2 - 10;

        if (progress < 100) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Connecting to Tor..."), this.width / 2, this.height / 2 - 60, 0xFFFFFF);
            renderProgressBar(context, x, y, barWidth, barHeight);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(progress + "%"), this.width / 2, y - 30, 0xFFFFFF);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(message), this.width / 2, y + barHeight + 10, 0xA0FFFFFF);
            return;
        }

        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Successfully connected to Tor!"), this.width / 2, this.height / 2 - 60, 0x00FF00);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("100%"), this.width / 2, this.height / 2 - 30, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(message), this.width / 2, y + barHeight + 10, 0xA0FFFFFF);
        renderProgressBar(context, x, y, barWidth, barHeight);

        if (closeButton == null) {
            closeButton = ButtonWidget.builder(Text.literal("Okay"),
                            (buttonWidget) -> Objects.requireNonNull(this.client).setScreen(new MultiplayerScreen(new TitleScreen())))
                    .dimensions(this.width / 2 - 60, this.height / 2 + 30, 120, 20)
                    .build();
        }
        this.addDrawableChild(closeButton);
    }
    public void renderProgressBar(DrawContext context, int x, int y, int barWidth, int barHeight) {
        context.drawBorder(x - 1, y - 1, barWidth + 2, barHeight + 2, 0xFFD3D3D3);
        context.fill(x, y, x + (progress * 2), y + barHeight, 0xFF00FF00);
        context.fill(x + (progress * 2), y, x + barWidth, y + barHeight, 0x80000000);
    }

    public static void setProgress(int progress1) {
        progress = progress1;
    }

    public static void setProgMessage(String msg) {
        message = msg;
    }
}