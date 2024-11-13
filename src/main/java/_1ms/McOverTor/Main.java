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

package _1ms.McOverTor;

import _1ms.McOverTor.manager.SettingsMgr;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main implements ModInitializer {

    public static final String confPath = FabricLoader.getInstance().getGameDir().resolve("mcovertor").toString();

    @Override
    public void onInitialize() {
        checkAndCreateConfDir();
        SettingsMgr.check();
        final File torrc = new File(confPath, "torrc");
        if(!torrc.exists()) {
            try {
                if(!torrc.createNewFile()) {
                    System.err.println("[McOverTor] Failed to create config file.");
                    return;
                }
                try(FileWriter fw = new FileWriter(torrc)) {
                    fw.write("ControlPort 9051\nHashedControlPassword 16:5CC34EC2B16C1DA260CE40B1D139DA73AAFAFF5EA46E17D2E20191BA76\nGeoIPFile \"mcovertor"+File.separator+"geoip\"\nGeoIPv6File \"mcovertor"+File.separator+"geoip6\"");
                }
                System.out.println("[McOverTor] Config file created.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("McOverTor Loaded!");
    }

    public static void checkAndCreateConfDir() {
        final File confDir = new File(confPath);
        if(!confDir.exists()) {
            if(confDir.mkdir()) {
                System.out.println("[McOverTor] Config directory created.");
                return;
            }
            System.err.println("[McOverTor] Failed to create config directory.");
        }
    }

    public static void renderWindow(DrawContext context, int x, int y, int windowWidth, int windowHeight, String text) {
        //Background
        context.fill(x, y, x + windowWidth, y + windowHeight, 0x80000000);

        final int color = 0xFFFFFFFF;

        final int bor1 = x + windowWidth / 2 - 65;
        final int bor2 = x + windowWidth / 2 + 65;

        context.fill(x, y, bor1, y + 1, color);               // Top borders
        context.fill(x+windowWidth, y, bor2, y + 1, color);
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal(text), bor1 + (bor2 - bor1)/2, y-5, 0xFFFFFF);
        context.fill(x, y + windowHeight - 1, x + windowWidth, y + windowHeight, color);  // Bottom border
        context.fill(x, y, x + 1, y + windowHeight, color);              // Left border
        context.fill(x + windowWidth - 1, y, x + windowWidth, y + windowHeight, color);   // Right border
    }

}