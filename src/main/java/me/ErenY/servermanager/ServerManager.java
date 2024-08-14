package me.ErenY.servermanager;

import me.ErenY.DiscordBot;
import me.ErenY.FRPManager.FRPManager;
import me.ErenY.GUI.View;
import me.ErenY.SSHManager.SSHManager;
import me.ErenY.commands.CommandManager;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class ServerManager {
    private static List<String> listofplayers = new ArrayList<>();
    private static int playerCount;
    private static int MaxplayerCount;
    private static boolean started = false;
    private static Process process;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> scheduledTask;


    private static final Logger logger = LoggerFactory.getLogger(ServerManager.class);

    public static List<String> getListofplayers() {
        return listofplayers;
    }

    public static String ServerStatus(){
        return ServerStatusPrivate();
    }

    public static boolean isStarted() {
        return started;
    }

    public static void StartServer(int g) throws IOException{
        StartServerPrivate(g);

    }
    public static void StopServer() throws IOException {
        StopProcess();
    }
    public static void SendMessageToServer(String message) throws IOException {
        SendMessageToServerPrivate(message);
    }

    private static void StartServerPrivate(int g) throws IOException {
        process = StartProcess(View.pref.get("SERVER_DIRECTORY", ""), g);
    }

    private static Process StartProcess(String directory, int g) throws IOException {
        final ProcessBuilder pb = new ProcessBuilder();

        List<String> commands = new ArrayList<>(Arrays.asList("java", "-Xms" + g + "G", "-Xmx" + g + "G", "-jar", "server.jar", "--nogui"));

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
                    while ((line = br.readLine()) != null) {
                        System.out.println(line);
                        View.serverConsole.append(line + "\n");
                        logger.trace(line);
                        if (line.contains("Done")){
                            started = true;
                            View.refresh.doClick();
                            DiscordBot.getStaticDiscordBot().getShardManager().setStatus(OnlineStatus.ONLINE);
                        } else if (line.contains("Stopping")) {
                            started = false;
                            View.refresh.doClick();
                            DiscordBot.getStaticDiscordBot().getShardManager().setStatus(OnlineStatus.DO_NOT_DISTURB);
                            listofplayers.clear();
                        }
                        if (line.contains("joined")){
                            View.refresh.doClick();
                            if (scheduledTask != null){
                                CancelTimer();
                            }
                        }
                        if (line.contains("left")){
                            View.refresh.doClick();
                        }
                        if (line.contains("!d ")){
                            List<String> listofwords = Arrays.asList(line.split(" "));
                            int i = listofwords.indexOf("!d");
                            int j = line.indexOf("!d");
                            String t;
                            if (!(t = View.pref.get("SERVER_TO_DISCORD_CHANNEL_ID", "")).isBlank()){
                                TextChannel tc = DiscordBot.getStaticDiscordBot().getShardManager().getTextChannelById(t);
                                if (tc != null){
                                    tc.sendMessage(listofwords.get(i) + " " + line.substring(j + 3)).queue();
                                }else {
                                    logger.error("failed to send message to discord because text channel is null");
                                }

                            }
                        }
                        if(line.contains("There are")){
                            String[] st = line.split(" ");
                            playerCount = Integer.parseInt(st[5]);
                            MaxplayerCount = Integer.parseInt(st[10]);
                            listofplayers = Arrays.asList(st).subList(13, st.length);

                            View.playerC.setText("Player Count: " + playerCount);
                            DiscordBot.getStaticDiscordBot().getShardManager().setActivity(Activity.playing(playerCount + " players"));

                            View.playersPanel.removeAll();
                            JList<Object> jList = new JList<>(listofplayers.toArray());
                            jList.setBackground(null);
                            jList.setBorder(null);
                            View.playersPanel.add(jList);
                            View.playersPanel.revalidate();
                            View.playersPanel.repaint();
                            if (listofplayers.isEmpty()){
                                StartTimer();
                            }
                            logger.info("Refreshed server info");
                        }
                    }
                } catch (Exception e) {
                    logger.error("Exception in server thread", e);
                }
            }
        };

        Thread t1 = new Thread(runnable);
        t1.start();
        logger.info("Started server thread");
        return p;
    }

    private static void StopProcess() throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        out.write("stop");
        out.write(System.lineSeparator());
        out.flush();
        listofplayers = new ArrayList<>();
        playerCount = 0;
        if (scheduledTask != null){
            CancelTimer();
        }
        logger.info("Stopped Server");
    }

    private static void SendMessageToServerPrivate(String message) throws IOException {
        BufferedWriter send = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        send.write(message);
        send.write(System.lineSeparator());
        send.flush();
        logger.info("Sent message to server {}",message);
    }

    private static String ServerStatusPrivate(){
        return "Server: " + (started ? "On":"Off") +
                "\nServer Ip: " + getPublicIpString() +
                "\nPlayer Count: " + playerCount;
    }

    private static void StartTimer(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    TimeoutServer();
                    logger.info("Server stopped after timer");
                } catch (Exception e) {
                    logger.error("Failed to stop server after waiting for a player", e);
                }
            }
        };

        scheduledTask = scheduler.schedule(runnable,
                Integer.parseInt(View.pref.get("SERVER_TIMEOUT_MIN", View.prop.getProperty("SERVER_TIMEOUT_MIN"))),
                TimeUnit.MINUTES);

        System.out.println("Timer Started");
        logger.info("Timer Started");
    }
    private static void CancelTimer(){
        scheduledTask.cancel(false);
        scheduledTask = null;
        System.out.println("Timer Canceled");
        logger.info("Timer Canceled");
    }
    private static void TimeoutServer(){
        logger.info("Timing out server");
        TextChannel textChannel = DiscordBot.getStaticDiscordBot().getShardManager().getTextChannelById(CommandManager.lastChannelID);
        if (textChannel != null){
            textChannel.sendMessage("Stopping server because there are no players").queue();
        }else {
            logger.error("Failed to send message to discord because text channel is null");
        }

        SSHManager sshManager = DiscordBot.getStaticDiscordBot().getSshManager();
        FRPManager frpManager = DiscordBot.getStaticDiscordBot().getFrpManager();

        if (View.pref.get("USE_OCI", View.prop.getProperty("USE_OCI")).equalsIgnoreCase("true")){
            if (frpManager != null) {
                int frptime = 0;
                while (frpManager.isConnected()) {
                    if (frptime < Integer.parseInt(View.pref.get("COMMAND_TIMEOUT", View.prop.getProperty("COMMAND_TIMEOUT")))) {
                        try {
                            frpManager.stopFRPClient();
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            logger.error("Exception while waiting for FRP Client to stop");
                            break;
                        }
                        frptime++;
                    }else {
                        logger.info("Timed out while stopping FRP Client");
                    }
                }
                logger.info("Stopped FRP Client");
            }

            if (sshManager != null){
                try {
                    sshManager.stopFRPCommand();
                    logger.info("Stopped FRP Server");
                } catch (Exception e) {
                    logger.error("Exception while stopping FRP Server", e);
                }
                sshManager.disconnect();
                logger.info("Disconnected SSH");
            }

        }

        if (!ServerManager.isStarted()){
            if (textChannel != null){
                textChannel.sendMessage("Already Stopped").queue();
            }else {
                logger.error("Failed to send message to discord because text channel is null");
            }
            logger.info("Server already stopped");
            DiscordBot.getStaticDiscordBot().getShardManager().setStatus(OnlineStatus.DO_NOT_DISTURB);
            return;
        }
        DiscordBot.getStaticDiscordBot().getShardManager().setStatus(OnlineStatus.IDLE);
        try {
            ServerManager.StopServer();
        } catch (Exception e) {
            if (textChannel != null){
                textChannel.sendMessage("Error occurred during stop process").queue();
            }else {
                logger.error("Failed to send message to discord because text channel is null");
            }
            logger.error("Exception while stopping server");
        }
        int timeStop = 0;
        while (ServerManager.isStarted()){
            if (timeStop > Integer.parseInt(View.pref.get("COMMAND_TIMEOUT", View.prop.getProperty("COMMAND_TIMEOUT")))){
                if (textChannel != null){
                    textChannel.sendMessage("Server stop timed out").queue();
                }else {
                    logger.error("failed to send message to discord because text channel is null");
                }
                logger.error("Server stop timed out");
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                logger.error("Exception while waiting for server to stop");
            }
            timeStop++;
        }
        if (!ServerManager.isStarted()) {
            DiscordBot.getStaticDiscordBot().getShardManager().setStatus(OnlineStatus.DO_NOT_DISTURB);
            if (textChannel != null){
                textChannel.sendMessage("Server stopped successfully").queue();
            }else {
                logger.error("Failed to send message to discord because text channel is null");
            }
            View.refresh.doClick();
            logger.info("Server stopped");
        }
    }

    public static String getPublicIpString(){
        if (DiscordBot.getStaticDiscordBot() != null){
            if (DiscordBot.getStaticDiscordBot().getOciManager() != null){
                return DiscordBot.getStaticDiscordBot().getOciManager().getPublic_ip() + ":" + View.pref.get("SERVER_PORT",View.prop.getProperty("SERVER_PORT"));
            }else {
                return View.pref.get("PUBLIC_IP",View.prop.getProperty("PUBLIC_IP")) + ":" + View.pref.get("SERVER_PORT",View.prop.getProperty("SERVER_PORT"));
            }
        }else {
            return "Off";
        }
    }

    public static int getMaxplayerCount() {
        return MaxplayerCount;
    }
}
