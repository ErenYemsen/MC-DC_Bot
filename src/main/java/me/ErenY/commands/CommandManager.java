package me.ErenY.commands;

import me.ErenY.DiscordBot;
import me.ErenY.myaudiomanager.MySendHandler;
import me.ErenY.ngrokmanager.NgrokManager;
import me.ErenY.servermanager.ServerManager;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.AudioManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommandManager extends ListenerAdapter {
    AudioManager audioManager;
    VoiceChannel voiceChannel;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();
        long time = System.currentTimeMillis();

        switch (command){
            case "ping":
                event.reply("pong" + "\n" + "ping: " + (System.currentTimeMillis() - time)).setEphemeral(true).queue();
                break;
            case "join":
                audioManager = event.getGuild().getAudioManager();
                voiceChannel = (VoiceChannel) event.getMember().getVoiceState().getChannel();
                if (voiceChannel == null){
                    event.reply("join a voice channel first").setEphemeral(true).queue();
                    break;
                }

                audioManager.setSendingHandler(new MySendHandler());

                audioManager.openAudioConnection(voiceChannel);

                event.reply("joined").setEphemeral(true).queue();
                break;
            case "leave":
                if (audioManager.isConnected()){
                    audioManager.closeAudioConnection();
                    event.reply("leaved").setEphemeral(true).queue();
                }else {
                    event.reply("bot is not connected").setEphemeral(true).queue();
                }
                break;
            case "server":
                OptionMapping optionMapping = event.getOption("options");
                int i = optionMapping.getAsInt();

                switch (i){
                    case 0:
                        //start server
                        if (DiscordBot.config.get("NGROK").equals("true")){
                            NgrokManager.StartTunnel();
                            int timeNgrok = 0;
                            while (!NgrokManager.isStarted()){
                                if (timeNgrok>60) break;
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
                            if (timeStart>60) {
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
                            event.getHook().sendMessage("durdururken anası sikildi umarım world siki tutumaz").queue();
                        }
                        int timeStop = 0;
                        while (ServerManager.isStarted()){
                            if (timeStop>60){
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
                        event.reply("is server started: " + ServerManager.isStarted() +
                                "\n is ngrok started: " + NgrokManager.isStarted() +
                                "\n server ip: " + NgrokManager.getPublicURL()).queue();
                        break;
                }

                break;

        }

    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commands = new ArrayList<>();
        commands.add(Commands.slash("ping", "pong"));
        commands.add(Commands.slash("join", "joins voice"));
        commands.add(Commands.slash("leave", "leaves"));

        OptionData option1 = new OptionData(OptionType.INTEGER, "options", "select option", true)
                .addChoice("Start", 0L)
                .addChoice("Stop", 1L)
                .addChoice("Status", 2L);

        commands.add(Commands.slash("server", "manage server").addOptions(option1));

        event.getGuild().updateCommands().addCommands(commands).queue();
    }
}
