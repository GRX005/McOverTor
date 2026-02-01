/*
    This file is part of the McOverTor project, licensed under the
    GNU General Public License v3.0

    Copyright (C) 2024-2026 _1ms

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

package _1ms.McOverTor;

import _1ms.McOverTor.manager.SettingsMgr;
import _1ms.McOverTor.manager.TorManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ServerAddress;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main implements ModInitializer {
    public static final Path confPath = FabricLoader.getInstance().getGameDir().resolve("mcovertor");
    public static final boolean isLinux = System.getProperty("os.name").toLowerCase().contains("linux");
    public static final ThreadLocal<ServerAddress> connIP = new ThreadLocal<>();
    public static final Logger logger = LogManager.getLogger("McOverTor");
    @Override
    public void onInitialize() {
        Thread.ofVirtual().name("TorInit").start(()-> {
            if(isLinux)
                logger.info("Linux detected!");
            else
                logger.info("Windows detected!");

            SettingsMgr.initAndCheckConf();
            final Path torrc = confPath.resolve("torrc");
            if(!Files.exists(torrc))
                createTorConf(torrc);
            //LocationMgr.getCtr();
            logger.info("McOverTor Loaded!");
        });
    }

    //Create the torrc config file
    static void createTorConf(Path torrc) {
        try {
            final String sep = File.separator.replace("\\", "\\\\"); //For intercompatibility windows -> \\ lnx -> /
            Files.writeString(torrc, "GeoIPFile \"mcovertor"+sep+"geoip\"\nGeoIPv6File \"mcovertor"+sep+"geoip6\"");
        } catch (IOException e) {
            logger.warn("Error while writing Tor config file!");
            throw new RuntimeException(e);
        }
    }

    //Render the window around the mod's UI elements in different shapes.
    public static void renderWindow(DrawContext context, int x, int y, int windowWidth, int windowHeight, String text) {
        final int color = 0xFFFFFFFF;
        //Background
        context.fill(x, y, x + windowWidth, y + windowHeight, 0x80000000);


        final int bor1 = x + windowWidth / 2 - 65;
        final int bor2 = x + windowWidth / 2 + 65;

        context.fill(x, y, bor1, y + 1, color);               // Top borders
        context.fill(x+windowWidth, y, bor2, y + 1, color);
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, text, bor1 + (bor2 - bor1)/2, y-5, color); //Top text
        context.fill(x, y + windowHeight - 1, x + windowWidth, y + windowHeight, color);  // Bottom border
        context.fill(x, y, x + 1, y + windowHeight, color);              // left border
        context.fill(x + windowWidth - 1, y, x + windowWidth, y + windowHeight, color);   // right border
    }

    //Extract all Tor files.
    public static void AllTorExtract() {
        logger.info("Extracting Tor files...");
        TorManager.extractTor(isLinux ? "/tor/lnx/tor" : "/tor/tor", "tor");
        if(isLinux) {
            TorManager.extractTor("/tor/lnx/libcrypto.so.3", "libcrypto.so.3");
            TorManager.extractTor("/tor/lnx/libevent-2.1.so.7", "libevent-2.1.so.7");
            TorManager.extractTor("/tor/lnx/libssl.so.3", "libssl.so.3");
        }
        TorManager.extractTor("/tor/geoip", "geoip");
        TorManager.extractTor("/tor/geoip6", "geoip6");
    }

    public static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y + 1, x + 1, y + height - 1, color);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

}