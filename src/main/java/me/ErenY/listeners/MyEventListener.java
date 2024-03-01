package me.ErenY.listeners;

import me.ErenY.DiscordBot;
import me.ErenY.ngrokmanager.NgrokManager;
import me.ErenY.servermanager.ServerManager;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


public class MyEventListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (!event.getAuthor().isBot()) {
            event.getChannel().sendMessage("üçbir").queue();
        }

        if (event.getMessage().getContentRaw().equals("!ngrok stop")){
            if (event.getMember().isOwner()){
                if (!NgrokManager.isStarted()){
                    event.getChannel().sendMessage("already stopped").queue();
                } else {
                    NgrokManager.StopTunnel();
                    event.getChannel().sendMessage("stooped ngrok").queue();
                }
            }else {
                event.getChannel().sendMessage("nah").queue();
            }
        }

        if (event.getMessage().getContentRaw().equals("!ngrok restart")){
            if (event.getMember().isOwner()) {
                if (NgrokManager.isStarted()) {
                    event.getChannel().sendMessage("emrolur").queue();
                    NgrokManager.StopTunnel();
                    int t = 0;
                    while (NgrokManager.isStarted()) {
                        if (t > Integer.parseInt(DiscordBot.config.get("COMMAND_TIMEOUT"))) {
                            event.getChannel().sendMessage("timed out while stopping").queue();
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        t++;
                    }
                    NgrokManager.StartTunnel();
                    int ts = 0;
                    while (NgrokManager.isStarted()) {
                        if (ts > Integer.parseInt(DiscordBot.config.get("COMMAND_TIMEOUT"))) {
                            event.getChannel().sendMessage("timed out while starting").queue();
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        ts++;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    event.getChannel().sendMessage("restarted? " + NgrokManager.getPublicURL()).queue();
                }else {
                    event.getChannel().sendMessage("alreaddy stopped").queue();
                }
            }else {
                event.getChannel().sendMessage("nah").queue();
            }
        }

        if (event.getMessage().getContentRaw().equals("!server switch lockdown")){
            if (event.getMember().isOwner()){
                if (!DiscordBot.lockdownMode){
                    DiscordBot.lockdownMode = true;
                    event.getChannel().sendMessage("ikinci bir emre kadar yönetime el konulmuştur").queue();
                    DiscordBot.getStaticDiscordBot().getShardManager().setActivity(Activity.watching("Big Brother"));

                }else {
                    DiscordBot.lockdownMode = false;
                    event.getChannel().sendMessage("yaşasın cumuriyed").queue();
                    DiscordBot.getStaticDiscordBot().getShardManager().setActivity(Activity.watching("Porno"));
                }


            }else {
                event.getChannel().sendMessage("sg").queue();
            }
        }

        if (event.getMessage().getContentRaw().equals("!server players")){
            if (event.getMember().isOwner()){
                if (ServerManager.isStarted()){
                    event.getChannel().sendMessage(ServerManager.getListofplayers().toString()).queue();
                }else {
                    event.getChannel().sendMessage("Server is offline").queue();
                }
            }else {
                event.getChannel().sendMessage("nah").queue();
            }
        }


    }

}
