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

import _1ms.McOverTor.Main;
import _1ms.McOverTor.screen.ChangeIPScreen;
import _1ms.McOverTor.screen.ConnectScreen;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TorManager {
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static Process torP;
    private static Thread torStopThread;
    static final File torFile = new File(System.getProperty("java.io.tmpdir"), "tor.exe");

    public static void extractTor(String input, String output, boolean launch) {
        System.out.println("[McTorControl] Extracting...");
        try (final InputStream in = TorManager.class.getResourceAsStream(input);
             final OutputStream out = new FileOutputStream(output)) {
            if (in == null)
                throw new FileNotFoundException("Couldn't start Tor, file not found.");
            final byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            throw new RuntimeException("Couldn't extract tor", e);
        }
        if(launch)
            launchTor();
    }

    public static void killTor(final boolean relaunch) {
        try {
            new ProcessBuilder("taskkill", "/F", "/IM", "tor.exe").start().waitFor();
            if(torStopThread != null)
                Runtime.getRuntime().removeShutdownHook(torStopThread);
            if(relaunch)
                launchTor();
            System.out.println("[McTorControl] Killed already running Tor.");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to kill Tor.", e);
        }
    }

    public static void launchTor() {
        final ProcessBuilder pb = new ProcessBuilder(torFile.getAbsolutePath(), "-f", Main.confPath+"\\torrc", "--DataDirectory", Main.confPath);
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            torP = pb.start();
            executor.submit(() -> {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(torP.getInputStream()));
                String line;
                while (true) {
                    try {
                        if ((line = reader.readLine()) == null) break;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if(line.contains("WSAEADDRINUSE")) {
                        killTor(true);
                        break;
                    }
                    if (line.contains("Bootstrapped")) {
                        final int progress = Integer.parseInt(line.substring(line.indexOf("Bootstrapped") + 12, line.indexOf("%")).trim());
                        final String message = line.substring(line.indexOf("%") + 1).trim();
                        System.out.println("Progress: " + progress + "%, Status: " + message);
                        //Itt meg tudsz hívni egy funkciót ami előrébb viszi a progress bars progress százalékra
                        ConnectScreen.progress = progress;
                        ConnectScreen.message = message;
                        if (message.contains("(starting)"))
                            authControl();
                    }
                }
            });
            executor.shutdown();
            if(torStopThread == null) {
                Runtime.getRuntime().addShutdownHook(torStopThread = new Thread(() -> TorManager.exitTor(false)));
            }
            System.out.println("[McTorControl] Tor successfully started.");
        } catch (IOException e) {
            TorManager.extractTor("/tor.exe", torFile.getAbsolutePath(), false);
            TorManager.extractTor("/geoip", Main.confPath, false);
            TorManager.extractTor("/geoip6", Main.confPath, true);
        }
    }

    public static void authControl() {
        try {
            socket = new Socket("127.0.0.1", 9051);
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("AUTHENTICATE \"TorControlPs01\"");
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ConnectScreen.progress = 2;
            ConnectScreen.message = "(control_auth): Authenticating control port";
            System.out.println("[McTorControl] Authenticating Tor Control Port..");
            final String resp = in.readLine();
            if(resp.contains("250")) {
                System.out.println("[McTorControl] Tor Control Port Successfully Authenticated.");
                ConnectScreen.progress = 4;
                return;
            }
        } catch (IOException ignored) {}
        System.err.println("[McTorControl] Tor couldn't be started, control port auth error.");
        torP.destroy();
    }

    public static void exitTor(boolean remHook) {
        System.out.println("[McTorControl] Shutting down Tor...");
        try {
            out.println("SIGNAL SHUTDOWN");
            final String resp = in.readLine();
            if(resp.contains("250")) {
                ConnectScreen.progress = 0;
                socket.close();
                if(remHook)
                    Runtime.getRuntime().removeShutdownHook(torStopThread);
                System.out.println("[McTorControl] Tor has been closed.");
                return;
            }
        } catch (IOException ignored) {}
        System.err.println("[McTorControl] Failed to close Tor.");
    }

    public static void changeCircuits() {
        System.out.println("[McTorControl] Changing circuits...");
        try {
            out.println("SIGNAL NEWNYM");
            final String resp = in.readLine();
            if (resp.contains("250")) {
                ChangeIPScreen.isDone = true;
                System.out.println("[McTorControl] Circuits changed successfully.");
                return;
            }
        } catch (IOException ignored) {}
        System.err.println("[McTorControl] Failed to change circuits.");
    }
}