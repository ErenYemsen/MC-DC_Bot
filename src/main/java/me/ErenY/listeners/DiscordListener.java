package me.ErenY.listeners;

import me.ErenY.DiscordBot;
import me.ErenY.GUI.View;
import me.ErenY.servermanager.ServerManager;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscordListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(DiscordListener.class);
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();

        if (message.contains("!command ") && event.getMember() != null){
            if (event.getMember().equals(event.getGuild().getOwner()) || View.pref.get("COMMAND_PERMISSION", View.prop.getProperty("COMMAND_PERMISSION")).equalsIgnoreCase("Everyone")){
                String st = message.substring(message.indexOf("!command ") + 9);
                try {
                    ServerManager.SendMessageToServer(st);
                } catch (Exception e) {
                    logger.error("Failed to send command to server", e);
                }
                event.getMessage().reply("sent!").queue();
                logger.info("Send command to server: {}", st);
            }else {
                event.getMessage().reply("Permission denied").queue();
                logger.info("User {}'s permission denied on sending command to server command", event.getAuthor().getName());
            }
        }

        if (message.equals("!server switch lockdown") && event.getMember() != null){
            if (event.getMember().equals(event.getGuild().getOwner()) || View.pref.get("LOCKDOWN_PERMISSION", View.prop.getProperty("LOCKDOWN_PERMISSION")).equalsIgnoreCase("Everyone")){
                if (!DiscordBot.lockdownMode){
                    DiscordBot.lockdownMode = true;
                    event.getMessage().reply("The administration has been seized until further notice. No one will be able to use commands").queue();
                    DiscordBot.getStaticDiscordBot().getShardManager().setActivity(Activity.watching("Big Brother"));
                    logger.info("Lockdown mode on");
                }else {
                    DiscordBot.lockdownMode = false;
                    event.getMessage().reply("Freedom !!").queue();
                    View.refresh.doClick();
                    logger.info("Lockdown mode off");
                }


            }else {
                event.getMessage().reply("Permission denied").queue();
                logger.info("User {}'s permission denied on lockdown command", event.getAuthor().getName());
            }
        }

        if (message.equals("!server players") && event.getMember() != null){
            if (event.getMember().equals(event.getGuild().getOwner()) || View.pref.get("PLAYERS_PERMISSION", View.prop.getProperty("PLAYERS_PERMISSION")).equalsIgnoreCase("Everyone")){
                if (ServerManager.isStarted()){
                    event.getMessage().reply(ServerManager.getListofplayers().toString()).queue();
                    logger.info("Sent player list to discord");
                }else {
                    event.getMessage().reply("Server is offline").queue();
                    logger.info("Player list permission granted but server is offline");
                }
            }else {
                event.getMessage().reply("Permission denied").queue();
                logger.info("User {}'s permission denied on !server players command", event.getAuthor().getName());
            }
        }

    }

}
