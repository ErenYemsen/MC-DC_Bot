package me.ErenY.commands;

import io.github.cdimascio.dotenv.Dotenv;
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
    private final Dotenv config = Dotenv.configure().load();

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
                        if (!NgrokManager.isStarted() && config.get("ngrok").equals("true")){
                            event.getHook().sendMessage("ngrok açık değil sanki?").queue();
                        }
                        if (ServerManager.isStarted()){
                            event.reply("already started").queue();
                            break;
                        }
                        DiscordBot.getStaticDiscordBot().getShardManager().setStatus(OnlineStatus.IDLE);
                        event.reply("starting... waiting for " + ServerManager.getWaitTime() + "s").queue();
                        try {
                            ServerManager.StartServer(null);
                        } catch (IOException | InterruptedException e) {
                            event.reply("botun anası sikilmiş bulunmakta naptınız amk" + e).queue();
                        }
                        if (ServerManager.isStarted()) {
                            DiscordBot.getStaticDiscordBot().getShardManager().setStatus(OnlineStatus.ONLINE);
                            event.getHook().sendMessage("Started... i guess").queue();
                        }else {
                            event.getHook().sendMessage("açılmadı sanki ama bi deneyin bakayım").queue();
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
                        if (!ServerManager.isStarted()){
                            DiscordBot.getStaticDiscordBot().getShardManager().setStatus(OnlineStatus.DO_NOT_DISTURB);
                            event.getHook().sendMessage("stopped succesfully").queue();
                        }else {
                            event.getHook().sendMessage("kapanmadı gibi de anlayamadım çok").queue();
                        }
                        break;
                    case 2:
                        //status
                        event.reply("is server started: " + ServerManager.isStarted() + " is ngrok started: " + NgrokManager.isStarted()).queue();
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
