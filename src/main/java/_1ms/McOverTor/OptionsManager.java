package _1ms.McOverTor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class OptionsManager {
    private static final String OPTIONS_FILE_PATH = "options.txt";
    public static String buttonPosition = "BOTTOM; LEFT;";
    public static boolean onlyTorConnection = false;
    public static boolean updateCheckerEnabled = true;
    public static boolean ipConnectionCheckerEnabled = false;

    public static void loadOptions() {
        try (BufferedReader reader = new BufferedReader(new FileReader(OPTIONS_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(": ");
                switch (parts[0]) {
                    case "button_position":
                        buttonPosition = parts[1];
                        break;
                    case "only_tor_connection":
                        onlyTorConnection = Boolean.parseBoolean(parts[1]);
                        break;
                    case "update_checker_enabled":
                        updateCheckerEnabled = Boolean.parseBoolean(parts[1]);
                        break;
                    case "ip_connection_checker_enabled":
                        ipConnectionCheckerEnabled = Boolean.parseBoolean(parts[1]);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveOptions() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OPTIONS_FILE_PATH))) {
            writer.write("button_position: " + buttonPosition);
            writer.newLine();
            writer.write("only_tor_connection: " + onlyTorConnection);
            writer.newLine();
            writer.write("update_checker_enabled: " + updateCheckerEnabled);
            writer.newLine();
            writer.write("ip_connection_checker_enabled: " + ipConnectionCheckerEnabled);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
