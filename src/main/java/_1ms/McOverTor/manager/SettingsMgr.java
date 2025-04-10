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

package _1ms.McOverTor.manager;

import _1ms.McOverTor.Main;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import static _1ms.McOverTor.Main.confPath;
import static _1ms.McOverTor.Main.logger;

public class SettingsMgr {
    private static final String settConf = confPath+File.separator+"config.cfg";
    private static final Gson gson = new Gson();
    private static HashMap<TorOption, Boolean> settings = new HashMap<>();
    private final static String ver = "CONFIG_VERSION: 1.3";

    //Save cfg, and load def settings if needed.
    static void saveConfig(boolean first) {
        if(first) //Def values, all false
            for (TorOption opt : TorOption.values())
                settings.put(opt, false);
        try (FileWriter writer = new FileWriter(settConf)) {
            // Write version header
            writer.write(ver+"\n");
            // Write JSON config
            gson.toJson(settings, writer);
        } catch (IOException e) {
            logger.error("Failed to save config.");
            throw new RuntimeException(e);
        }
    }
    //Check if config exists, if not, load default settings, create it, and unpack Tor files, otherwise load it's options to the hashmap
    public static void initAndCheckConf() {
        Runtime.getRuntime().addShutdownHook(Thread.ofVirtual().name("CfgSave").unstarted(() -> saveConfig(false)));
        if(Files.notExists(Paths.get(settConf))) {
            saveConfig(true);
            AllTorExtract();
            return;
        }
        settings = loadConfig();
    }
    //Extract all Tor files.
    static void AllTorExtract() {
        logger.info("Extracting Tor files...");
        TorManager.extractTor(Main.isLinux ? "/tor/lnx/tor" : "/tor/tor.exe", TorManager.torFile.getAbsolutePath(), false);
        if(Main.isLinux) {
            TorManager.extractTor("/tor/lnx/libcrypto.so.3", confPath+File.separator+"libcrypto.so.3", false);
            TorManager.extractTor("/tor/lnx/libevent-2.1.so.7", confPath+File.separator+"libevent-2.1.so.7", false);
            TorManager.extractTor("/tor/lnx/libssl.so.3", confPath+File.separator+"libssl.so.3", false);
            TorManager.extractTor("/tor/lnx/libstdc++.so.6", confPath+File.separator+"libstdc++.so.6", false);
        }
        TorManager.extractTor("/tor/geoip", confPath+File.separator+"geoip", false);
        TorManager.extractTor("/tor/geoip6", confPath+File.separator+"geoip6", false);
    }
//Overloads bc why not
    public static void flip(TorOption val) {
        settings.replace(val, !settings.get(val));
    }

    public static void flip(String val) {
        TorOption valE = TorOption.valueOf(val.substring(1));
        settings.replace(valE, !settings.get(valE));
    }

    public static boolean get(TorOption val) {
        return settings.get(val);
    }

    public static boolean get(String val) {
        return !settings.get(TorOption.valueOf(val.substring(1)));
    }

    static HashMap<TorOption, Boolean> loadConfig() {
        try (BufferedReader reader = new BufferedReader(new FileReader(settConf))) {
            final String version = reader.readLine(); // Skip the version line
            if(!version.equals(ver)) {
                saveConfig(true);
                AllTorExtract(); //Way to update Tor files, by changing cfg version.
                logger.info("Updated config.");
                return settings;
            }
            logger.info("LOADING {}", version);
            return gson.fromJson(reader, new TypeToken<HashMap<TorOption, Boolean>>() {}.getType());
        } catch (IOException e) {
            logger.error("Failed to load config.");
            throw new RuntimeException(e);
        }
    }
}
