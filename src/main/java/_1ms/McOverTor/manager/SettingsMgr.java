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

package _1ms.McOverTor.manager;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import static _1ms.McOverTor.Main.confPath;

public class SettingsMgr {


    private static final String settConf = confPath+"\\config.cfg";
    private static final Gson gson = new Gson();
    private static HashMap<String, Boolean> settings = new HashMap<>();

    private static void saveConfig(boolean first) {
        if(first) {
            settings.put("torOnly", false);
            settings.put("separateStreams", false);
        }
        try (FileWriter writer = new FileWriter(settConf)) {
            // Write version header
            writer.write("CONFIG_VERSION: 1.0\n");
            // Write JSON config
            gson.toJson(settings, writer);
        } catch (IOException e) {
            throw new RuntimeException("[McOverTor] Failed to save config.",e);
        }
    }

    public static void check() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> saveConfig(false)));
        if(Files.notExists(Paths.get(settConf))) {
            saveConfig(true);
            TorManager.extractTor("/tor.exe", TorManager.torFile.getAbsolutePath(), false);
            TorManager.extractTor("/geoip", new File(confPath, "geoip").getAbsolutePath(), false);
            TorManager.extractTor("/geoip6", new File(confPath, "geoip6").getAbsolutePath(), false);
            return;
        }
        settings = loadConfig();
    }

    public static void flip(String szart) {
        settings.replace(szart, !settings.get(szart));
    }

    public static boolean get(String szart) {
        return settings.get(szart);
    }

    private static HashMap<String, Boolean> loadConfig() {
        try (final BufferedReader reader = new BufferedReader(new FileReader(settConf))) {
            System.out.println("LOADING " +reader.readLine()); // Skip the version line
            return gson.fromJson(reader, new TypeToken<HashMap<String, Boolean>>() {}.getType());
        } catch (IOException e) {
            throw new RuntimeException("[McOverTor] Failed to load config." ,e);
        }
    }

}