package me.ErenY.servermanager;


import io.github.cdimascio.dotenv.Dotenv;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class ServerManager {
    private static final Dotenv config = Dotenv.configure().load();
    private static final int waitTime = 10;
    private static boolean started = false;
    private static Process process;

    public static void main(String[] args) throws IOException, InterruptedException {

    }

    public static boolean isStarted() {
        return started;
    }
    public static int getWaitTime(){
        return waitTime;
    }

    public static void StartServer(String[] args) throws IOException, InterruptedException {
        StartServerPrivate(args);
    }
    public static void StopServer() throws IOException, InterruptedException {
        StopProcess();
    }

    private static void StartServerPrivate(String[] args) throws IOException, InterruptedException {
        process = StartProcess(config.get("SERVER_DIRECTORY"), Integer.parseInt(config.get("XMX")), args);
        process.waitFor(waitTime, TimeUnit.SECONDS);
    }

    private static Process StartProcess(String directory, int g, String... args) throws IOException {
        final ProcessBuilder pb = new ProcessBuilder();

        List<String> commands = new ArrayList<>(Arrays.asList("java", "-Xms" + g + "G", "-Xmx" + g + "G", "-jar", "server.jar"));

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
                    System.out.println(Arrays.toString(args));
                    while ((line = br.readLine()) != null) {
                        if (line.contains("Done")){
                            started = true;
                        }
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    // Do something when the Exception is thrown
                }
            }
        };

        Thread t1 = new Thread(runnable);
        t1.start();

        return p;
    }

    private static void StopProcess() throws IOException, InterruptedException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        out.write("stop");
        out.write(System.lineSeparator());
        out.flush();
        started = false;
    }

}
