package me.ErenY.servermanager;


import me.ErenY.DiscordBot;
import me.ErenY.ngrokmanager.NgrokManager;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import java.io.*;
import java.sql.Time;
import java.util.*;


public class ServerManager {
    private static Timer timer;
    private static TimerTask task;
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

    public static void StartServer(int g, String[] args) throws IOException, InterruptedException {
        StartServerPrivate(g, args);
    }
    public static void StopServer() throws IOException, InterruptedException {
        StopProcess();
    }
    public static void SendMessageToServer(String message) throws IOException {
        SendMessageToServerPrivate(message);
    }

    private static void StartServerPrivate(int g, String[] args) throws IOException, InterruptedException {
        process = StartProcess(DiscordBot.config.get("SERVER_DIRECTORY"), g, args);
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
                            if (NgrokManager.isStarted()){
                            NgrokManager.StopTunnel();
                            }
                            DiscordBot.getStaticDiscordBot().getShardManager().setStatus(OnlineStatus.DO_NOT_DISTURB);
                            listofplayers.clear();
                        }
                        if (line.contains("joined")){
                            List<String> listofwords = Arrays.asList(line.split(" "));
                            listofplayers.add(listofwords.get(listofwords.indexOf("joined")-1));
                            if (timer != null){
                                CancelTimer();
                            }
                            DiscordBot.getStaticDiscordBot().getShardManager().setActivity(Activity.playing(listofplayers.size() + " kişi"));
                        }
                        if (line.contains("left")){
                            List<String> listofwords = Arrays.asList(line.split(" "));
                            listofplayers.remove(listofwords.get(listofwords.indexOf("left")-1));
                            if (listofplayers.isEmpty()){
                                StartTimer();
                                DiscordBot.getStaticDiscordBot().getShardManager().setActivity(Activity.watching("porno"));
                            }
                        }
                        if (line.contains("!d ")){
                            List<String> listofwords = Arrays.asList(line.split(" "));
                            int i = listofwords.indexOf("!d");
                            int j = line.indexOf("!d");
                            DiscordBot.getStaticDiscordBot().getShardManager()
                                    .getTextChannelById(DiscordBot.config.get("SERVER_TO_DISCORD_CHANNEL_ID"))
                                    .sendMessage(listofwords.get(i) + " " + line.substring(j + 3)).queue();
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

    private static void SendMessageToServerPrivate(String message) throws IOException {
        BufferedWriter send = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        send.write(message);
        send.write(System.lineSeparator());
        send.flush();
    }

    private static String ServerStatusPrivate(){
        return "is server started: " + started +
                "\nis ngrok started: " + NgrokManager.isStarted() +
                "\nserver ip: " + NgrokManager.getPublicURL() +
                "\nplayer count: " + listofplayers.size();
    }

    private static void StartTimer(){
        task = new TimerTask() {
            @Override
            public void run() {
                try {
                    StopServer();
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                DiscordBot.getStaticDiscordBot().getShardManager()
                        .getTextChannelById(DiscordBot.config.get("SERVER_TO_DISCORD_CHANNEL_ID"))
                        .sendMessage("olm çıkarken kapatsanıza").queue();
            }
        };

        timer.schedule(task, Long.parseLong(DiscordBot.config.get("SERVER_TIMEOUT_MIN"))*1000*60);

        System.out.println("Timer Started");
    }
    private static void CancelTimer(){
        timer.cancel();
        System.out.println("Timer Canceled");
    }
}
