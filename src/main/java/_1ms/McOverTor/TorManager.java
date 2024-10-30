package _1ms.McOverTor;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TorManager {
    static Socket socket;
    static PrintWriter out;
    static BufferedReader in;
    static Process torP;
    static final File torFile = new File(System.getProperty("java.io.tmpdir"), "tor.exe");

    public static void extractTor() {
        try (InputStream in = TorManager.class.getResourceAsStream("/tor.exe");
             final OutputStream out = new FileOutputStream(torFile)) {
            if (in == null) {
                throw new FileNotFoundException("Couldn't start Tor, file not found.");
            }
            final byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            throw new RuntimeException("Couldn't extract tor", e);
        }
        launchTor();
    }

    public static void killTor(final boolean relaunch) {
        try {
            new ProcessBuilder("taskkill", "/F", "/IM", "tor.exe").start().waitFor();
            if(relaunch)
                launchTor();
            System.out.println("[McTorControl] Killed already running Tor.");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to kill Tor.", e);
        }
    }

    static int i = 0;
    public static void launchTor() {
        final ProcessBuilder pb = new ProcessBuilder(torFile.getAbsolutePath(), "-f", Main.confDir.getAbsolutePath()+"\\torrc", "--DataDirectory", Main.confDir.getAbsolutePath());
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
                        ConnectingScreen.progress = progress;
                        ConnectingScreen.message = message;
                        if (message.contains("(starting)"))
                            authControl();
                    }
                }
            });
            executor.shutdown();
            if(i == 0) {
                Runtime.getRuntime().addShutdownHook(new Thread(TorManager::exitTor));
                i++;
            }
            System.out.println("[McTorControl] Tor successfully started.");
        } catch (IOException e) {
            System.out.println("[McTorControl] Extracting...");
            extractTor();
        }
    }

    public static void authControl() {
        try {
            socket = new Socket("127.0.0.1", 9051);
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("AUTHENTICATE \"TorControlPs01\"");
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ConnectingScreen.progress = 2;
            ConnectingScreen.message = "(control_auth): Authenticating control port";
            System.out.println("[McTorControl] Authenticating Tor Control Port..");
            final String resp = in.readLine();
            if(resp.contains("250")) {
                System.out.println("[McTorControl] Tor Control Port Successfully Authenticated.");
                ConnectingScreen.progress = 4;
                return;
            }
        } catch (IOException ignored) {}
        System.err.println("[McTorControl] Tor couldn't be started, control port auth error.");
        torP.destroy();
    }

    public static void exitTor() {
        System.out.println("[McTorControl] Shutting down Tor...");
        try {
            out.println("SIGNAL SHUTDOWN");
            final String resp = in.readLine();
            if(resp.contains("250")) {
                ConnectingScreen.progress = 0;
                socket.close();
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