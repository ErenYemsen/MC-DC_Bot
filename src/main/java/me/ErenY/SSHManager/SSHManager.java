package me.ErenY.SSHManager;

import com.jcraft.jsch.*;
import me.ErenY.DiscordBot;
import me.ErenY.GUI.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.io.InputStream;
import java.time.LocalDateTime;

public class SSHManager {
    private final String host;            //public ip
    private final String user;            //ubuntu
    private final String privateKeyPath;
    private Session session;
    private OutputStream out;
    private boolean isFRPStarted;

    private static final Logger logger = LoggerFactory.getLogger(SSHManager.class);

    public SSHManager() throws Exception {
        this.host = DiscordBot.getStaticDiscordBot().getOciManager().getPublic_ip();
        this.user = "ubuntu";
        this.privateKeyPath = View.pref.get("PRIVATE_KEY_PATH", "");
        this.isFRPStarted = false;
        this.connect();
        logger.info("Created SSHManager object");
    }

    public void connect() throws Exception {
        JSch jsch = new JSch();
        jsch.addIdentity(privateKeyPath);
        session = jsch.getSession(user, host, 22);

        //exposed to mim attacks
        session.setConfig("StrictHostKeyChecking", "no");

        session.connect();

        logger.info("Connected to ssh");
    }

    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            View.sshConsole.append(LocalDateTime.now() + " SSH Client Disconnected" + "\n");
        }
        logger.info("SSH disconnected");
    }

    private void executeCommand(String command) throws Exception {
        ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
        channelExec.setCommand(command);

        InputStream in = channelExec.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        channelExec.connect();

        Runnable runnableSTDOUT = new Runnable() {
            @Override
            public void run() {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        View.sshConsole.append(line + "\n");
                    }

                    logger.info("Exit status: {}", channelExec.getExitStatus());

                    reader.close();
                    channelExec.disconnect();
                }catch (Exception e){
                    logger.error("Exception in ssh execute command STDOUT thread");
                }
            }
        };


        InputStream ien = channelExec.getErrStream();
        BufferedReader ereader = new BufferedReader(new InputStreamReader(ien));
        Runnable runnableSTDERR = new Runnable() {
            @Override
            public void run() {
                try {
                    String line;
                    while ((line = ereader.readLine()) != null) {
                        View.sshConsole.append(line + "\n");
                    }

                    logger.info("STDERR Exit status: {}", channelExec.getExitStatus());

                    ereader.close();
                    channelExec.disconnect();
                }catch (Exception e){
                    logger.error("Exception in ssh execute command STDERR thread");
                }
            }
        };



        Thread thread = new Thread(runnableSTDOUT);
        Thread thread1 = new Thread(runnableSTDERR);
        thread.start();
        thread1.start();

        logger.info("executed command in ssh: {}", command);
    }

    public void killProcessCommand(int port) throws Exception {
        String command = "fuser -k " + port + "/tcp";
        executeCommand(command);
        logger.info("Killed process on port {}", port);
    }

    public void runFRPCommand() throws Exception {
        String command = "/home/ubuntu/frp/frps -c /home/ubuntu/frp/frps.toml";
        ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
        channelExec.setCommand(command);

        InputStream in = channelExec.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        this.out = channelExec.getOutputStream();

        channelExec.connect();

        Runnable runnableSTDOUT = new Runnable() {
            @Override
            public void run() {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        View.sshConsole.append(line + "\n");
                        logger.trace(line);
                        if (line.contains("frps started successfully")){
                            setFRPStarted(true);
                        }
                    }

                    View.sshConsole.append(LocalDateTime.now() + " FRP Server Stopped with exit code: " + channelExec.getExitStatus() + "\n");
                    logger.info("SSH Exit status: {}", channelExec.getExitStatus());

                    reader.close();
                    channelExec.disconnect();
                }catch (Exception e){
                    logger.error("Exception while running FRP Server", e);
                }
            }
        };

        InputStream ien = channelExec.getErrStream();
        BufferedReader ereader = new BufferedReader(new InputStreamReader(ien));

        Runnable runnableSTDERR = new Runnable() {
            @Override
            public void run() {
                try {
                    String line;
                    while ((line = ereader.readLine()) != null){
                        View.sshConsole.append("STDERR: " + line + "\n");
                        logger.error("STDERR: {}",line);
                    }

                    View.sshConsole.append(LocalDateTime.now() + " FRP Server Stopped with exit code: " + channelExec.getExitStatus() + "\n");
                    logger.info("SSH STDERR Exit status: {}", channelExec.getExitStatus());

                    ereader.close();
                    channelExec.disconnect();
                }catch (Exception e){
                    logger.error("Exception in FRPS process thread",e);
                }
            }
        };

        Thread thread = new Thread(runnableSTDOUT);
        Thread thread1 = new Thread(runnableSTDERR);
        thread.start();
        thread1.start();
        logger.info("Frps thread started");
    }

    public void stopFRPCommand() throws IOException {
        this.out.write(2);
        this.out.flush();
        this.isFRPStarted = false;
        logger.info("Stopped Frps");
    }

    private void uploadFrptoServer(InputStream localFile, String filename) throws Exception {
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();

        executeCommand("mkdir -p /home/ubuntu/frp");
        channelSftp.put(localFile, filename, ChannelSftp.OVERWRITE);

        channelSftp.exit();
        logger.info("Uploaded local file: {} to {}", localFile, filename);
    }

    public void uploadfrps() throws Exception {
        uploadFrptoServer(SSHManager.class.getResourceAsStream("/frps"), "/home/ubuntu/frp/frps");
        uploadFrptoServer(SSHManager.class.getResourceAsStream("/frps.toml"), "/home/ubuntu/frp/frps.toml");
        executeCommand("chmod 777 /home/ubuntu/frp/frps");
        logger.info("Uploaded frps");
    }

    public boolean isFRPStarted() {
        return isFRPStarted;
    }

    public void setFRPStarted(boolean FRPStarted) {
        isFRPStarted = FRPStarted;
    }

    public String getHost() {
        return host;
    }

    public String getUser() {
        return user;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public Session getSession() {
        return session;
    }
}