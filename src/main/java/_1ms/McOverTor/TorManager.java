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
             OutputStream out = new FileOutputStream(torFile)) { // Move OutputStream to try-with-resources

            if (in == null) {
                throw new FileNotFoundException("Couldn't start Tor, file not found.");
            }

            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        launchTor();
    }

    public static void launchTor() {
        ProcessBuilder pb;
        pb = new ProcessBuilder(torFile.getAbsolutePath(), "-f", Main.confDir.getAbsolutePath()+"\\torrc", "--DataDirectory", Main.confDir.getAbsolutePath());
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            torP = pb.start();
            executor.submit(() -> {
                BufferedReader reader = new BufferedReader(new InputStreamReader(torP.getInputStream()));
                String line;
                while (true) {
                    try {
                        if ((line = reader.readLine()) == null) break;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if(line.contains("WSAEADDRINUSE")) {
                        try {
                            new ProcessBuilder("taskkill", "/F", "/IM", "tor.exe").start().waitFor();
                            System.out.println("[McTorControl] Killed already running Tor, relaunching..");
                            launchTor();
                            return;
                        } catch (IOException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (line.contains("Bootstrapped")) {
                        final int progress = Integer.parseInt(line.substring(line.indexOf("Bootstrapped") + 12, line.indexOf("%")).trim());
                        final String message = line.substring(line.indexOf("%") + 1).trim();
                        System.out.println("Progress: " + progress + "%, Status: " + message);
                        //Itt meg tudsz hívni egy funkciót ami előrébb viszi a progress bars progress százalékra
                        ConnectingScreen.setProgress(progress);
                        ConnectingScreen.setProgMessage(message);
                        if (message.contains("(starting)"))
                            authControl();
                    }
                }
            });
            executor.shutdown();
            Runtime.getRuntime().addShutdownHook(new Thread(()-> {
                try {
                    exitTor();
                    socket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
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
            ConnectingScreen.setProgress(2);
            System.out.println("[McTorControl] Authenticating Tor Control Port..");
            final String resp = in.readLine();
            if(resp.contains("250")) {
                System.out.println("[McTorControl] Tor Control Port Successfully Authenticated.");
                ConnectingScreen.setProgress(4);
                return;
            }
            torP.destroy();
            System.err.println("[McTorControl] Tor couldn't be started.");
        } catch (IOException e) {
            torP.destroy();
            System.err.println("[McTorControl] Tor couldn't be started.");
        }
    }

    public static void exitTor() {
        System.out.println("[McTorControl] Shutting down Tor...");
        try {
            out.println("SIGNAL SHUTDOWN");
            final String resp = in.readLine();
            if(resp.contains("250")) {
                ConnectingScreen.progress = 0;
                System.out.println("[McTorControl] Tor has been closed.");
                return;
            }
            System.err.println("[McTorControl] Failed to close Tor.");
        } catch (IOException e) {
            System.err.println("[McTorControl] Failed to close Tor.");
        }
    }

    public static void changeCircuits() {
        System.out.println("[McTorControl] Changing circuits...");
        out.println("SIGNAL NEWNYM");
        try {
            String response = in.readLine();
            if (response.contains("250")) {
                System.out.println("[McTorControl] Circuit changed successfully.");
                ChangeIPScreen.isDone = true;
                return;
            }
            System.err.println("[McTorControl] Failed to change circuits.");
        } catch (IOException e) {
            System.err.println("[McTorControl] Failed to change circuits.");
        }
    }
}