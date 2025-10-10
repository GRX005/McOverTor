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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static _1ms.McOverTor.Main.confPath;
import static _1ms.McOverTor.Main.logger;

public class SettingsMgr {
    private static final Path settConf = confPath.resolve("config.cfg");
    private static final Gson gson = new Gson();
    private static HashMap<TorOption, Boolean> settings = new HashMap<>();
    private final static String ver = "CONFIG_VERSION: 1.4";

    //Save cfg, and load def settings if needed.
    static void saveConfig(boolean first) {
        if(first) //Def values, all false
            for (TorOption opt : TorOption.values())
                settings.put(opt, false);
        try {
            Files.writeString(settConf, ver+System.lineSeparator()+gson.toJson(settings));
        } catch (IOException e) {
            logger.error("Failed to save config.");
            throw new RuntimeException(e);
        }
    }
    //Check if config exists, if not, load default settings, create it, and unpack Tor files, otherwise load it's options to the hashmap
    public static void initAndCheckConf() {
        Runtime.getRuntime().addShutdownHook(Thread.ofVirtual().name("TorCfgSave").unstarted(() -> saveConfig(false)));
        if(!Files.exists(confPath)) {
            try { //Create McOverTor dir.
                Files.createDirectory(confPath);
            } catch (IOException e) {
                logger.error("Failed to initialize the config");
                throw new RuntimeException(e);
            }
            reConf();
            return;
        }
        settings = loadConfig();
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
//Load the mod's cfg, to upd the tor client used -> upd the cfg version
    static HashMap<TorOption, Boolean> loadConfig() {
        try (BufferedReader reader = Files.newBufferedReader(settConf)) {
            if(!reader.readLine().equals(ver)) { // Skip the version line
                FileUtils.deleteDirectory(confPath.toFile());
                Files.createDirectory(confPath);
                reConf();
                return settings;
            }
            logger.info("LOADING {}", ver);
            return gson.fromJson(reader, new TypeToken<HashMap<TorOption, Boolean>>(){}.getType());
        } catch (IOException e) {
            logger.error("Failed to load config.");
            throw new RuntimeException(e);
        }
    }

    static void reConf() {
        saveConfig(true);
        Main.AllTorExtract();
        logger.info("Updated config.");
    }
}
