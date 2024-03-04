package me.ErenY.ngrokmanager;

import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Proto;
import com.github.alexdlaird.ngrok.protocol.Tunnel;
import me.ErenY.DiscordBot;

public class NgrokManager { //fixme does not work on windows? or on my pc?
    private static boolean Started = false;

    private static NgrokClient ngrokClient;
    private static CreateTunnel createTunnel;
    private static Tunnel tunnel;
    private static String PublicURL;

    public static boolean isStarted() {
        return Started;
    }

    public static NgrokClient getNgrokClient() {
        return ngrokClient;
    }

    public static CreateTunnel getCreateTunnel() {
        return createTunnel;
    }

    public static Tunnel getTunnel() {
        return tunnel;
    }

    public static String getPublicURL() {
        return PublicURL;
    }

    public static void setIsStarted(boolean isStarted) {
        NgrokManager.Started = isStarted;
    }

    public static void main(String[] args) throws InterruptedException {



    }
    public static void StartTunnel(){
            Tunnel start = StartTunnelPrivate();
    }
    public static void StopTunnel(){
        StopTunnelPrivate();
    }

    private static Tunnel StartTunnelPrivate(){
            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    ngrokClient = new NgrokClient.Builder().build();
                    createTunnel = new CreateTunnel.Builder().withName("ngrok").withProto(Proto.TCP).withAddr(DiscordBot.config.get("SERVER_PORT")).build();
                    tunnel = ngrokClient.connect(createTunnel);
                    PublicURL = tunnel.getPublicUrl();

                    Started = true;

                }
            };

            Thread t2 = new Thread(runnable);
            t2.start();
            return tunnel;
    }

    private static void StopTunnelPrivate(){
        ngrokClient.kill();
        Started = false;
    }



}
