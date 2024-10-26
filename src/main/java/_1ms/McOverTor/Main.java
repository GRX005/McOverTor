package _1ms.McOverTor;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

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
}