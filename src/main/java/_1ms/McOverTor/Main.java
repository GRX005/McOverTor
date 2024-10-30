package _1ms.McOverTor;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main implements ModInitializer {

    static File confDir;
    @Override
    public void onInitialize() {
        confDir = new File(FabricLoader.getInstance().getGameDir().toFile(), "mcovertor");
        if(!confDir.exists()) {
            if(confDir.mkdir()) {
                System.out.println("[McOverTor] Config directory created.");
            } else {
                System.err.println("[McOverTor] Failed to create config directory.");
            }
        }
        final File torrc = new File(confDir, "torrc");
        if(!torrc.exists()) {
            try {
                if(!torrc.createNewFile()) {
                    System.err.println("[McOverTor] Failed to create config file.");
                    return;
                }
                try(FileWriter fw = new FileWriter(torrc)) {
                    fw.write("ControlPort 9051\nHashedControlPassword 16:5CC34EC2B16C1DA260CE40B1D139DA73AAFAFF5EA46E17D2E20191BA76");
                }
                System.out.println("[McOverTor] Config file created.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("McOverTor Loaded!");
    }

    public static void renderWindow(DrawContext context, int x, int y, int windowWidth, int windowHeight, TextRenderer textRenderer, String text) {
        //Background
        context.fill(x, y, x + windowWidth, y + windowHeight, 0x80000000);

        final int color = 0xFFFFFFFF;

        final int bor1 = x + windowWidth / 2 - 65;
        final int bor2 = x + windowWidth / 2 + 65;

        context.fill(x, y, bor1, y + 1, color);               // Top borders
        context.fill(x+windowWidth, y, bor2, y + 1, color);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(text), bor1 + (bor2 - bor1)/2, y-5, 0xFFFFFF);
        context.fill(x, y + windowHeight - 1, x + windowWidth, y + windowHeight, color);  // Bottom border
        context.fill(x, y, x + 1, y + windowHeight, color);              // Left border
        context.fill(x + windowWidth - 1, y, x + windowWidth, y + windowHeight, color);   // Right border
    }

}