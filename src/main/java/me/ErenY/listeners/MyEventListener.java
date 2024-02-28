package me.ErenY.listeners;

import me.ErenY.DiscordBot;
import me.ErenY.ngrokmanager.NgrokManager;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


public class MyEventListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (!event.getAuthor().isBot()) {
            event.getChannel().sendMessage("üçbir").queue();
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
                    event.getChannel().sendMessage("ikinci bir emre kadar kontrole el konulmuştur").queue();
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

    }

}
