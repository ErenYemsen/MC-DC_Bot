package me.ErenY.servermanager;


import me.ErenY.DiscordBot;
import me.ErenY.ngrokmanager.NgrokManager;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.user.UserActivityEndEvent;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ServerManager {
    private static final List<String> listofplayers = new ArrayList<>();
    private static boolean started = false;
    private static Process process;

    public static void main(String[] args) throws IOException, InterruptedException {

    }
    public static List<String> getListofplayers() {
        return listofplayers;
    }

    public static String ServerStatus(){
        return ServerStatusPrivate();
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
                        if (line.contains("Done")){
                            started = true;
                            DiscordBot.getStaticDiscordBot().getShardManager().setStatus(OnlineStatus.ONLINE);
                        } else if (line.contains("Stopping")) {
                            started = false;
                            NgrokManager.StopTunnel();
                            DiscordBot.getStaticDiscordBot().getShardManager().setStatus(OnlineStatus.DO_NOT_DISTURB);
                        }
                        if (line.contains("joined")){
                            List<String> listofwords = Arrays.asList(line.split(" "));
                            listofplayers.add(listofwords.get(listofwords.indexOf("joined")-1));
                        }
                        if (line.contains("left")){
                            List<String> listofwords = Arrays.asList(line.split(" "));
                            listofplayers.remove(listofwords.get(listofwords.indexOf("left")-1));
                        }
                        if (line.contains("!d ")){
                            List<String> listofwords = Arrays.asList(line.split(" "));
                            int i = listofwords.indexOf("!d");
                            DiscordBot.getStaticDiscordBot().getShardManager()
                                    .getTextChannelById(DiscordBot.config.get("SERVER_TO_DISCORD_CHANNEL_ID"))
                                    .sendMessage(listofwords.get(i-1) + " " + listofwords.get(i+1)).queue();
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

    private static void SendMessageToServerPrivate(String message, String sender) throws IOException {
        BufferedWriter send = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        send.write("say [from Discord]" + sender + ": " + message);
        send.write(System.lineSeparator());
        send.flush();
    }

    private static String ServerStatusPrivate(){
        return "is server started: " + started +
                "\nis ngrok started: " + NgrokManager.isStarted() +
                "\nserver ip: " + NgrokManager.getPublicURL() +
                "\nplayer count: " + listofplayers.size();
    }
}
