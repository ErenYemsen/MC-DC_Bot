package me.ErenY.listeners;

import me.ErenY.ngrokmanager.NgrokManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MyEventListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (!event.getAuthor().isBot()) {
            event.getChannel().sendMessage("üçbir").queue();
        }
        if (NgrokManager.isStarted() && event.getMember().isOwner() && event.getMessage().getContentRaw().equals("!ngrok restart")){
            NgrokManager.StopTunnel();
            int t = 0;
            while (NgrokManager.isStarted()){
                if (t>60) break;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                t++;
            }
            NgrokManager.StartTunnel();
            event.getChannel().sendMessage("restarted?").queue();
        } else if (!NgrokManager.isStarted()) {
            event.getChannel().sendMessage("already stopped").queue();
        }
    }

}
