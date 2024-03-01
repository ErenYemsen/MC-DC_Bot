package me.ErenY.servermanager;


import me.ErenY.DiscordBot;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ServerManager {
    private static boolean started = false;
    private static Process process;

    public static void main(String[] args) throws IOException, InterruptedException {

    }

    public static boolean isStarted() {
        return started;
    }

    public static void StartServer(String[] args) throws IOException, InterruptedException {
        StartServerPrivate(args);
    }
    public static void StopServer() throws IOException, InterruptedException {
        StopProcess();
    }
    public static void SendMessageToServer(String message, String sender) throws IOException {
        SendMessageToServerPrivate(message, sender);
    }

    private static void StartServerPrivate(String[] args) throws IOException, InterruptedException {
        process = StartProcess(DiscordBot.config.get("SERVER_DIRECTORY"), Integer.parseInt(DiscordBot.config.get("XMX")), args);
    }

    private static Process StartProcess(String directory, int g, String... args) throws IOException {
        final ProcessBuilder pb = new ProcessBuilder();

        List<String> commands = new ArrayList<>(Arrays.asList("java", "-Xms" + g + "G", "-Xmx" + g + "G", "-jar", "server.jar"));

        pb.directory(new File(directory));
        pb.command(commands);

        Process p = pb.start();

        InputStream is = p.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    String line;
                    System.out.println(Arrays.toString(args));
                    while ((line = br.readLine()) != null) {
                        //todo add something to count players
                        // -maybe send mc chat messages to dc(if wanted?) ?
                        if (line.contains("Done")){
                            started = true;
                        } else if (line.contains("Stopping")) {
                            started = false;
                        }
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    // Do something when the Exception is thrown
                }
            }
        };

        Thread t1 = new Thread(runnable);
        t1.start();

        return p;
    }

    private static void StopProcess() throws IOException, InterruptedException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        out.write("stop");
        out.write(System.lineSeparator());
        out.flush();
    }

    //todo add a method to be able to send messages to minecraft chat from discord chat
    // (maybe connect discord with minecraft or just print dc name)
    private static void SendMessageToServerPrivate(String message, String sender) throws IOException {
        BufferedWriter send = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        send.write("say [Discord] " + sender + ": " + message);
        send.write(System.lineSeparator());
        send.flush();
    }


}
