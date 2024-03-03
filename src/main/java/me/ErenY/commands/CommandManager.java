package me.ErenY.commands;

import me.ErenY.DiscordBot;
import me.ErenY.ngrokmanager.NgrokManager;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommandManager extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();
        long time = System.currentTimeMillis();

        switch (command){
            case "ping":
                event.reply("pong" + "\n" + "ping: " + (System.currentTimeMillis() - time)).setEphemeral(true).queue();
                break;
            case "server":
                if (DiscordBot.lockdownMode && !event.getMember().isOwner()){
                    event.reply("ikinci bir emre kadar server yönetimine el konulmuştur").queue();
                    break;
                }
                OptionMapping optionMapping = event.getOption("options");
                int i = optionMapping.getAsInt();

                switch (i){
                    case 0:
                        //start server
                        if (DiscordBot.config.get("NGROK").equals("true")){
                            NgrokManager.StartTunnel();
                            int timeNgrok = 0;
                            while (!NgrokManager.isStarted()){
                                if (timeNgrok > Integer.parseInt(DiscordBot.config.get("COMMAND_TIMEOUT"))) break;
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                timeNgrok++;
                            }
                        }

                        if (!NgrokManager.isStarted() && DiscordBot.config.get("NGROK").equals("true")){
                            event.getHook().sendMessage("ngrok timed out").queue();
                        }
                        if (ServerManager.isStarted()){
                            event.reply("already started").queue();
                            break;
                        }
                        DiscordBot.getStaticDiscordBot().getShardManager().setStatus(OnlineStatus.IDLE);
                        event.reply("starting... wait...").queue();
                        try {
                            ServerManager.StartServer(new String[]{});
                        } catch (IOException | InterruptedException e) {
                            event.getHook().sendMessage("botun anası sikilmiş bulunmakta naptınız amk" + e).queue();
                        }
                        int timeStart = 0;
                        while (!ServerManager.isStarted()){
                            if (timeStart > Integer.parseInt(DiscordBot.config.get("COMMAND_TIMEOUT"))) {
                                event.getHook().sendMessage("timed out").queue();
                                break;
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            timeStart++;
                        }
                        if (ServerManager.isStarted()) {
                            DiscordBot.getStaticDiscordBot().getShardManager().setStatus(OnlineStatus.ONLINE);
                            event.getHook().sendMessage("Started... i guess").queue();
                            if (NgrokManager.isStarted()) event.getHook().sendMessage(NgrokManager.getPublicURL().substring(6)).queue();
                        }
                        break;
                    case 1:
                        //stop server
                        if (NgrokManager.isStarted()){
                            NgrokManager.StopTunnel();
                        }

                        if (!ServerManager.isStarted()){
                            event.reply("already stopped").queue();
                            break;
                        }
                        DiscordBot.getStaticDiscordBot().getShardManager().setStatus(OnlineStatus.IDLE);
                        event.reply("stopping").queue();
                        try {
                            ServerManager.StopServer();
                        } catch (Exception e) {
                            event.getHook().sendMessage("durdururken anası sikildi umarım world siki tutmaz " + e).queue();
                        }
                        int timeStop = 0;
                        while (ServerManager.isStarted()){
                            if (timeStop > Integer.parseInt(DiscordBot.config.get("COMMAND_TIMEOUT"))){
                                event.getHook().sendMessage("timed out").queue();
                                break;
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            timeStop++;
                        }
                        DiscordBot.getStaticDiscordBot().getShardManager().setStatus(OnlineStatus.DO_NOT_DISTURB);
                        event.getHook().sendMessage("stopped succesfully").queue();
                        break;
                    case 2:
                        //status
                        event.reply(ServerManager.ServerStatus()).queue();
                        break;
                }
                break;
            case "say":
                if (!ServerManager.isStarted()){
                    event.reply("Server is offline").queue();
                    break;
                }
                OptionMapping optionMapping1 = event.getOption("message");
                String message = optionMapping1.getAsString();

                try {
                    ServerManager.SendMessageToServer("say [from Discord]" + event.getUser().getName() + ": " + message);
                } catch (IOException e) {
                    throw new RuntimeException(e);
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
