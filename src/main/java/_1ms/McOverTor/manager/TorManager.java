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

import _1ms.McOverTor.screen.TorConnect;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Objects;
import java.util.Random;

import static _1ms.McOverTor.Main.confPath;
import static _1ms.McOverTor.Main.isLinux;

public class TorManager {
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static Process torP;
    private static Thread torStopThread;
    static final Path tor = confPath.resolve("tor");

    public static volatile int progress = 0;
    public static volatile String message = "(starting): Starting";
    public static String sPort = "9050";
    private static String cPort = "9051";
    private static final Logger logger = LogManager.getLogger("McOverTor/TorControl");
    public static TorConnect connScrn;

    public static void startTor() {
        TorConnect scrn = new TorConnect();
        Objects.requireNonNull(MinecraftClient.getInstance()).setScreen(scrn);
        connScrn = scrn;
        launchTor();
    }

    //Extract the files located in the plugin to the desired path.
    public static void extractTor(String input, String output) {
        Thread.ofVirtual().name("TorFileExtract").start(()-> { //Async so all the unpackings can run concurrently, and it won't slow down the client's starting.
            try (BufferedInputStream in = new BufferedInputStream(Objects.requireNonNull(TorManager.class.getResourceAsStream(input))); BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(confPath.resolve(output)))) {
                in.transferTo(out);
            } catch (IOException e) {
                logger.error("Couldn't extract tor");
                throw new RuntimeException(e);
            }
        });
    }

    //Multiplatform Tor client launcher, also reads it's output and supplies it to the loading screen, and extracts the client if it's not found in windows.
    private static void launchTor() {
        if(isLinux){
            try {
                Files.setPosixFilePermissions(tor, PosixFilePermissions.fromString("rwxr-xr-x")); //Perm so ./tor can be ran
            } catch (IOException e) {
                logger.error("Failed to set Tor PosixFilePermissions.");
                throw new RuntimeException(e);
            }
        }
        final ProcessBuilder pb = new ProcessBuilder(tor.toAbsolutePath().toString(), "-f", confPath+File.separator+"torrc", "--DataDirectory", confPath.toString(), "--SocksPort", sPort, "--ControlPort", cPort, "--HashedControlPassword", "16:5CC34EC2B16C1DA260CE40B1D139DA73AAFAFF5EA46E17D2E20191BA76");
        if(isLinux)
            pb.environment().put("LD_LIBRARY_PATH", ":"+ tor.getParent());
        try {
            torP = pb.start();
            Thread.ofVirtual().name("TorOutputReader").start(TorManager::readTorOutput);
            Runtime.getRuntime().addShutdownHook(torStopThread = Thread.ofVirtual().name("TorStopper").unstarted(() -> exitTor(false))); //The shutdown hook should always be null at this point.
            logger.info("Tor successfully launched.");
        } catch (IOException e) {
            logger.error("Failed to launch Tor!");
            TorConnect.fail = true;
            logger.error(e);
        }
    }

    private static void readTorOutput(){
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(torP.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.contains("Address already in use")) { //Kill if already running.
                    killTor(true, false);
                    break;
                }
                if(line.contains("Failed")) {
                    message = "§4"+line.substring(29);
                    logger.info("Error: {}", message);
                }
                if (line.contains("Bootstrapped")) { //Progress msg parsing.
                    progress = Integer.parseInt(line.substring(line.indexOf("Bootstrapped") + 12, line.indexOf("%")).trim());
                    message = line.substring(line.indexOf("%") + 1).trim();
                    logger.info("Progress: {}%, Status: {}", progress, message);
                    //Itt meg tudsz hívni egy funkciót ami előrébb viszi a progress bars progress százalékra
                    if (message.contains("(starting)")) //First bootstrapped msg, init control as soon as possible.
                        authControl();
                    if(progress == 100) { //Shut down reader after Tor is Loaded.
                        logsAdjust();
                        connScrn.connCallback();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error while reading Tor output!");
            TorConnect.fail = true;
            logger.error(e);
        }
    }

    //Set the UI to Tor OFF state.
    private static void resetProg() {
        progress = 0;
        message = "(starting): Starting";
    }

    //Forcefully shut down the Tor client when needed.
    public static void killTor(boolean relaunch, boolean linuxKill) {
        try {
            if(torStopThread != null) {
                Runtime.getRuntime().removeShutdownHook(torStopThread); //Remove shutdown hook as the Tor client is stopped here.
                torStopThread = null;
            }
            if (isLinux)
                if(linuxKill)
                    new ProcessBuilder("killall", "tor").start().waitFor();
                else {
                    final Random rand = new Random();
                    sPort = String.valueOf(rand.nextInt(61001,65535));
                    do cPort = String.valueOf(rand.nextInt(61001,65535)); while (Objects.equals(cPort, sPort)); //Gen and check if we somehow gened the same num.
                    logger.info("Default ports already occupied, switching to Socks: {}, Control: {}", sPort,cPort);
                }
            else
                new ProcessBuilder("taskkill", "/F", "/IM", "tor").start().waitFor();
            if(relaunch) //If tor was already running on the system before the mod starting it.
                launchTor();
            else
                resetProg();
            logger.info("Killed already running Tor.");
        } catch (InterruptedException | IOException ignored) {
            TorConnect.fail = true;
        }
    }

    //Connect to the Tor client control port.
    private static void authControl() {
        try {
            socket = new Socket("127.0.0.1", Integer.parseInt(cPort));
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("AUTHENTICATE \"TorControlPs01\"");
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            progress = 2;
            message = "(control_auth): Authenticating control port";
            logger.info("Authenticating Tor Control Port..");
            final String resp = in.readLine();
            if(resp.contains("250")) {// Doesn't ret here if except or wrong ans.
                logger.info("Tor Control Port Successfully Authenticated.");
                progress = 4;
                return;
            }
        } catch (IOException e) {
            logger.error("Tor couldn't be started, control port auth error.");
            TorConnect.fail = true;
        }
        torP.destroy();
    }
    //Properly close Tor.
    public static void exitTor(boolean remHook) {
        try {
            out.println("SIGNAL SHUTDOWN");
            final String resp = in.readLine();//If the Tor client freezes, this will make the game freeze too, but it shouldn't
            if(resp.contains("250")) {
                resetProg();
                socket.close();
                if(remHook) { //Don't remove when the shutdown thread has been triggered
                    Runtime.getRuntime().removeShutdownHook(torStopThread);
                    torStopThread = null;
                }
                logger.info("Tor has been closed.");
                return;
            }
        } catch (IOException ignored) {}
        logger.warn("Failed to close Tor.");
        killTor(false, true);//Kill tor if it couldn't be closed.
    }
    //Change circuits without restarting, using the control port.
    public static int changeCircuits() {
        try {
            out.println("SIGNAL NEWNYM");
            final String resp = in.readLine();
            if (resp.contains("250")) {
                logger.info("Circuits changed.");
                return 1;
            }
        } catch (IOException ignored) {}
        logger.warn("Failed to change circuits.");
        return 2;
    }
    //Only log errors, otherwise the stdout buffer fills up, it shouldn't throw errors :)
    private static void logsAdjust() {
        try {
            out.println("SETCONF Log=\"err\"");
            final String resp = in.readLine();
            if (resp.contains("250")) {
                logger.info("Adjusted logging levels");
                return;
            }
        } catch (IOException ignored) {}
        logger.warn("Failed to adjust Tor logging levels, potential crash might happen.");
    }
}