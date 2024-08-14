package me.ErenY.commands;

import me.ErenY.DiscordBot;
import me.ErenY.FRPManager.FRPManager;
import me.ErenY.GUI.View;
import me.ErenY.SSHManager.SSHManager;
import me.ErenY.ocimanager.OCIManager;
import me.ErenY.servermanager.ServerManager;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class CommandManager extends ListenerAdapter {
    OCIManager ociManager;
    SSHManager sshManager;
    FRPManager frpManager;

    private static final Logger logger = LoggerFactory.getLogger(CommandManager.class);

    public static long lastChannelID = 0L;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();
        long time = System.currentTimeMillis();

        switch (command){
            case "ping":
                lastChannelID = event.getChannel().getIdLong();
                event.reply("pong" + "\n" + "ping: " + (System.currentTimeMillis() - time)).setEphemeral(true).queue();
                logger.info("Ping command used by user: {}", event.getUser().getName());
                break;
            case "server":
                lastChannelID = event.getChannel().getIdLong();
                if (DiscordBot.lockdownMode && event.getMember() != null && !event.getMember().isOwner()){
                    event.reply("The administration has been seized until further notice. No one will be able to use commands").queue();
                    logger.info("User {} tried server command on lockdown", event.getUser().getName());
                    break;
                }
                OptionMapping optionMapping = event.getOption("options");
                int i = optionMapping != null ? optionMapping.getAsInt() : 0;

                switch (i){
                    case 0:
                        //start server
                        logger.info("Start server command taken");
                        event.reply("starting... wait...").queue();

                        if (View.pref.get("USE_OCI", View.prop.getProperty("USE_OCI")).equalsIgnoreCase("true")) {
                            logger.info("Using oci as its preferred");
                            try {
                                frpManager = DiscordBot.getStaticDiscordBot().getFrpManager();
                            } catch (Exception e) {
                                logger.error("Failed to get FRPManager", e);
                            }
                            sshManager = DiscordBot.getStaticDiscordBot().getSshManager();
                            ociManager = DiscordBot.getStaticDiscordBot().getOciManager();
                            String OCIstatus = ociManager.getInstanceStatus();
                            logger.info("Got oci with instance status: {}", OCIstatus);

                            if (OCIstatus.equalsIgnoreCase("STOPPED")){
                                ociManager.startInstance();
                                logger.info("Starting instance because its stopped");
                            } else if (!(OCIstatus.equalsIgnoreCase("RUNNING")
                                        || OCIstatus.equalsIgnoreCase("STARTING"))){
                                event.getChannel().sendMessage("OCI is not stopped nor started").queue();
                                logger.warn("OCI is not stopped nor started");
                            }
                            int timeOCI = 0;
                            while (!OCIstatus.equalsIgnoreCase("RUNNING")
                                    && timeOCI < Integer.parseInt(View.pref.get("COMMAND_TIMEOUT", View.prop.getProperty("COMMAND_TIMEOUT")))){
                                logger.info("Started waiting for oci instance to start");
                                if (timeOCI > Integer.parseInt(View.pref.get("OCI_TIMEOUT", View.prop.getProperty("OCI_TIMEOUT")))) {
                                    logger.warn("OCI timed out while starting");
                                    break;
                                }
                                try {
                                    Thread.sleep(1000);
                                } catch (Exception e) {
                                    logger.error("Exception while waiting for oci to start", e);
                                }
                                timeOCI++;
                            }
                            if (!OCIstatus.equalsIgnoreCase("RUNNING")){
                                event.getChannel().sendMessage("OCI did not start, manually starting can solve the problem").queue();
                                logger.error("OCI did not start, waiting more or manually starting can solve the problem");
                            }

                            //ssh to oci, kill listen and run frps

                            try {
                                sshManager.connect();
                                logger.info("SSH'd into OCI");
                                Thread.sleep(500);
                            } catch (Exception e) {
                                logger.error("Exception while SSH'ing into OCI", e);
                            }
                            try {
                                sshManager.killProcessCommand((int) frpManager.getServerPort());
                            } catch (Exception e) {
                                logger.error("Exception while killing process listening port {}", frpManager.getServerPort());
                            }

                            try {
                                sshManager.runFRPCommand();
                                logger.info("Ran FRP Server");
                            } catch (Exception e) {
                                logger.error("Exception while running FRP Server", e);
                            }
                            int frpstime = 0;
                            while (!sshManager.isFRPStarted()){
                                if (frpstime < Integer.parseInt(View.pref.get("COMMAND_TIMEOUT", View.prop.getProperty("COMMAND_TIMEOUT")))) {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (Exception e) {
                                        logger.error("Exception while waiting for FRP Server to start", e);
                                    }
                                    frpstime++;
                                }else {
                                    logger.warn("Timed out while waiting for FRP Server to start");
                                }
                            }
                            //run frpc on local

                            try {
                                frpManager.setTomlPublicIP(ociManager.getPublic_ip());
                                logger.info("Set toml public ip");
                            } catch (Exception e) {
                                logger.error("Exception while setting toml public ip", e);
                            }
                            try {
                                frpManager.startFRPClient();
                                logger.info("Started FRP Client");
                            } catch (Exception e) {
                                logger.error("Exception while starting FRP Client", e);
                            }

                        }

                        if (ServerManager.isStarted()){
                            event.getChannel().sendMessage("Already started").queue();
                            logger.info("Minecraft Server is already started");
                            break;
                        }
                        DiscordBot.getStaticDiscordBot().getShardManager().setStatus(OnlineStatus.IDLE);

                        try {

                            ServerManager.StartServer(Integer.parseInt(View.pref.get("XMX", View.prop.getProperty("XMX"))));

                        } catch (Exception e) {
                            event.getChannel().sendMessage("Failed to start server").queue();
                            logger.error("Exception while starting server",e);
                        }

                        int timeStart = 0;
                        while (!ServerManager.isStarted()){
                            if (timeStart > Integer.parseInt(View.pref.get("COMMAND_TIMEOUT", View.prop.getProperty("COMMAND_TIMEOUT")))) {
                                event.getChannel().sendMessage("Server timed out").queue();
                                logger.warn("Server timed out while starting");
                                break;
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e) {
                                logger.error("Exception while starting server", e);
                            }
                            timeStart++;
                        }
                        if (ServerManager.isStarted()) {
                            logger.info("Server is started");
                            DiscordBot.getStaticDiscordBot().getShardManager().setStatus(OnlineStatus.ONLINE);
                            String st = ServerManager.getPublicIpString();
                            if (View.pref.get("USE_OCI", View.prop.getProperty("USE_OCI")).equalsIgnoreCase("true")){
                                DiscordBot.getStaticDiscordBot().getShardManager().getGuilds().getFirst()
                                        .getSelfMember().modifyNickname(st).queue();
                                logger.info("Set discord bot nickname to public ip: {}", st);
                            }
                            event.getChannel().sendMessage("Server Started").queue();
                            if (View.pref.get("USE_OCI", View.prop.getProperty("USE_OCI")).equalsIgnoreCase("true")){
                                event.getChannel().sendMessage(st).queue();
                                logger.info("Sent discord public ip: {}", st);
                            }
                        }
                        break;
                    case 1:
                        //stop server
                        logger.info("Server stopping command taken");
                        event.reply("Stopping").queue();

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
                            event.reply("Already Stopped").queue();
                            logger.info("Server already stopped");
                            DiscordBot.getStaticDiscordBot().getShardManager().setStatus(OnlineStatus.DO_NOT_DISTURB);
                            break;
                        }
                        DiscordBot.getStaticDiscordBot().getShardManager().setStatus(OnlineStatus.IDLE);
                        try {
                            ServerManager.StopServer();
                        } catch (Exception e) {
                            event.getChannel().sendMessage("Error occurred during stop process").queue();
                            logger.error("Exception while stopping server");
                        }
                        int timeStop = 0;
                        while (ServerManager.isStarted()){
                            if (timeStop > Integer.parseInt(View.pref.get("COMMAND_TIMEOUT", View.prop.getProperty("COMMAND_TIMEOUT")))){
                                event.getChannel().sendMessage("Server stop timed out").queue();
                                logger.error("Server timed out while stopping");
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
                            event.getChannel().sendMessage("Server stopped successfully").queue();
                            View.refresh.doClick();
                            logger.info("Server stopped");
                        }
                        break;
                    case 2:
                        //status
                        event.reply(ServerManager.ServerStatus()).queue();
                        logger.info("Server status requested by user: {}", event.getUser().getName());
                        break;
                }
                break;
            case "say":
                lastChannelID = event.getChannel().getIdLong();
                logger.info("Say command taken");
                if (!ServerManager.isStarted()){
                    event.reply("Server is offline").queue();
                    logger.info("Say command taken but server is offline");
                    break;
                }
                OptionMapping optionMapping1 = event.getOption("message");
                String message = optionMapping1 != null ? optionMapping1.getAsString() : null;

                try {
                    ServerManager.SendMessageToServer("say [from Discord]" + event.getUser().getName() + ": " + message);
                    logger.info("Sent to server from discord message: {}", message);
                } catch (Exception e) {
                    logger.error("Exception while sending message to server from discord");
                }
                event.reply("[to Server]" + event.getUser().getName() + ": " + message).queue();
                break;
        }

    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commands = new ArrayList<>();
        commands.add(Commands.slash("ping", "pong"));

        OptionData option1 = new OptionData(OptionType.INTEGER, "options", "select option", true)
                .addChoice("Start", 0L)
                .addChoice("Stop", 1L)
                .addChoice("Status", 2L);
        commands.add(Commands.slash("server", "manage server").addOptions(option1));

        OptionData option2 = new OptionData(OptionType.STRING, "message", "send message to server", true);

        commands.add(Commands.slash("say", "send message to the server").addOptions(option2));

        event.getGuild().updateCommands().addCommands(commands).queue();
    }
}
