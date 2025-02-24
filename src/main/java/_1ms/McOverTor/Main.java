/*
    This file is part of the McOverTor project, licensed under the
    GNU General Public License v3.0

    Copyright (C) 2024-2025 _1ms

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
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main implements ModInitializer {

    public static final String confPath = FabricLoader.getInstance().getGameDir().resolve("mcovertor").toString();
    public static final boolean isLinux = System.getProperty("os.name").toLowerCase().contains("linux");
    public static final ThreadLocal<ServerAddress> connIP = new ThreadLocal<>();
    public static final Logger logger = LogManager.getLogger("McOverTor");
    @Override
    public void onInitialize() {
        if(isLinux)
            logger.info("Linux detected!");
        else
            logger.info("Windows detected!");
        checkAndCreateConfDir();
        SettingsMgr.initAndCheckConf();
        final File torrc = new File(confPath, "torrc");
        if(!torrc.exists())
            Thread.ofVirtual().name("ConfigWriter").start(()->createTorConf(torrc));
        logger.info("McOverTor Loaded!");
    }
    //Make mcovertor folder in .minecraft if it doesnt exist
    static void checkAndCreateConfDir() {
        final File confDir = new File(confPath);
        if(!confDir.exists()) {
            if(confDir.mkdir()) {
                logger.info("Config directory created.");
                return;
            }
            System.err.println("Failed to create config directory.");
        }
    }
    //Create the torrc config file
    static void createTorConf(File torrc) {
        try (BufferedWriter fw = new BufferedWriter(new FileWriter(torrc))) {
            final String sep = File.separator.replace("\\", "\\\\");
            fw.write("ControlPort 9051\nHashedControlPassword 16:5CC34EC2B16C1DA260CE40B1D139DA73AAFAFF5EA46E17D2E20191BA76\nGeoIPFile \"mcovertor"+sep+"geoip\"\nGeoIPv6File \"mcovertor"+sep+"geoip6\"");
            logger.info("Config file created.");
        } catch (IOException e) {
            throw new RuntimeException("Error while writing Tor config file!", e);
        }
    }

    //Render the window around the mod's UI elements in different shapes.
    public static void renderWindow(DrawContext context, int x, int y, int windowWidth, int windowHeight, String text) {
        //Background
        context.fill(x, y, x + windowWidth, y + windowHeight, 0x80000000);

        final int color = 0xFFFFFFFF;

        final int bor1 = x + windowWidth / 2 - 65;
        final int bor2 = x + windowWidth / 2 + 65;

        context.fill(x, y, bor1, y + 1, color);               // Top borders
        context.fill(x+windowWidth, y, bor2, y + 1, color);
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal(text), bor1 + (bor2 - bor1)/2, y-5, 0xFFFFFF); //Top text
        context.fill(x, y + windowHeight - 1, x + windowWidth, y + windowHeight, color);  // Bottom border
        context.fill(x, y, x + 1, y + windowHeight, color);              // left border
        context.fill(x + windowWidth - 1, y, x + windowWidth, y + windowHeight, color);   // right border
    }

}