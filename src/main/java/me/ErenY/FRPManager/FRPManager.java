package me.ErenY.FRPManager;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import me.ErenY.GUI.View;
import me.ErenY.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class FRPManager {
    private String clientPath;
    private String clientFilePath;
    private String clientTomlPath;
    private int localPort;
    private int remotePort;
    private String serverAddr;
    private final Toml toml;
    private boolean isConnected;
    private Process process;

    private static final Logger logger = LoggerFactory.getLogger(FRPManager.class);

    public FRPManager() throws IOException {
        switch ((String) Objects.requireNonNull(View.os.getSelectedItem())){
            case "Windows AMD64":
                try {
                    setFrpcPathWin("frpcWinAMD.exe");
                    logger.info("Succesfully created frpc winamd");
                } catch (Exception ex) {
                    logger.error("Failed to create frpc winamd", ex);
                }
                break;
            case "Windows ARM":
                try {
                    setFrpcPathWin("frpcWinARM.exe");
                    logger.info("Succesfully created frpc winarm");
                } catch (Exception ex) {
                    logger.error("Failed to create frpc winarm", ex);
                }
                break;
            case "MacOS AMD64":
                try {
                    setFrpcPathMac("frpcMacOSAMD");
                    logger.info("Succesfully created frpc macosamd");
                } catch (Exception ex) {
                    logger.error("Failed to create frpc macosamd", ex);
                }
                break;
            case "MacOS ARM":
                try {
                    setFrpcPathMac("frpcMacOSARM");
                    logger.info("Succesfully created frpc macosarm");
                } catch (Exception ex) {
                    logger.error("Failed to create frpc macosarm", ex);
                }
                break;
            case "Other":
                logger.info("Selected other as OS");
                View.pref.put("FRPC_PATH", View.frpcPath.getText());
                this.clientTomlPath = View.pref.get("FRPC_PATH", "");
                logger.info("Pref frpc_path saved and clientTomlPath set to as {}", View.pref.get("FRPC_PATH", ""));
                break;
            default:
                logger.warn("OS selection switch dropped to default");
                View.pref.put("FRPC_PATH", View.frpcPath.getText());
                this.clientTomlPath = View.pref.get("FRPC_PATH", "");
                logger.info("[Default branch] Pref frpc_path saved and clientTomlPath set to as {}", View.pref.get("FRPC_PATH", ""));
                break;
        }
        this.isConnected = false;
        this.toml = new Toml().read(new File(this.clientTomlPath));
        setTomlLocalPort(Integer.parseInt(View.pref.get("SERVER_PORT", View.prop.getProperty("SERVER_PORT"))));
        setTomlRemotePort(Integer.parseInt(View.pref.get("SERVER_PORT", View.prop.getProperty("SERVER_PORT"))));
        logger.info("set remoteport on toml to {}", Integer.parseInt(View.pref.get("SERVER_PORT", View.prop.getProperty("SERVER_PORT"))));

        //set name
        setTomlName();
        logger.info("Created FRPManager object");
    }

    public void startFRPClient() throws IOException {
        final ProcessBuilder pb = new ProcessBuilder();

        List<String> commands = new ArrayList<>(Arrays.asList(this.clientFilePath, "-c", this.clientTomlPath));


        pb.directory(new File(this.clientPath));
        pb.command(commands);

        Process p = pb.start();
        this.process = p;

        InputStream is = p.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        Runnable runnableSTDOUT = new Runnable() {
            @Override
            public void run() {
                try {
                    String line;
                    while ((line = br.readLine()) !=  null){
                        View.frpConsole.append(line + "\n");
                        logger.trace(line);
                        if (line.contains("[MCDC-bot] start proxy success")){
                            setConnected(true);
                        }
                    }

                    int exitCode = process.waitFor();
                    logger.debug("\nProcess STDOUT exited with error code : {}", exitCode);

                } catch (IOException e) {
                    logger.error("IOException while running frp client or it may just stopped", e);
                } catch (Exception e) {
                    logger.error("Exception while running frp client or it may just stopped", e);
                }
            }
        };

        InputStream ies = p.getErrorStream();
        InputStreamReader iesr = new InputStreamReader(ies);
        BufferedReader ber = new BufferedReader(iesr);

        Runnable runnableSTDERR = new Runnable() {
            @Override
            public void run() {
                try {
                    String line;
                    while ((line = ber.readLine()) != null){
                        View.frpConsole.append("STDERR: " + line + "\n");
                        logger.error("STDERR: {}", line);
                    }
                    int exitCode = process.waitFor();
                    logger.debug("\nProcess STDERR exited with error code : {}", exitCode);
                }catch (Exception e){
                    logger.error("Exception in STDERR FRPC thread", e);
                }
            }
        };

        Thread thread = new Thread(runnableSTDOUT);
        Thread thread1 = new Thread(runnableSTDERR);
        thread.start();
        thread1.start();
    }

    public void stopFRPClient() throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        out.write(2);
        out.flush();
        setConnected(false);
        View.frpConsole.append(LocalDateTime.now() + " FRP Client Stopped" + "\n");
        logger.info("Stopped frp client");
    }

    private void setTomlRemotePort(int port) throws IOException {
        Map<String, Object> tomlMap = this.toml.toMap();

        ((ArrayList<HashMap<String,Object>>) tomlMap.get("proxies")).getFirst()
                .put("remotePort", port);

        TomlWriter tomlWriter = new TomlWriter();
        tomlWriter.write(tomlMap, new File(this.clientTomlPath));
        this.remotePort = port;
        logger.info("set remote port on toml to {}", port);
    }

    private void setTomlLocalPort(int port) throws IOException {
        Map<String, Object> tomlMap = this.toml.toMap();

        ((ArrayList<HashMap<String,Object>>) tomlMap.get("proxies")).getFirst()
                .put("localPort", port);

        TomlWriter tomlWriter = new TomlWriter();
        tomlWriter.write(tomlMap, new File(this.clientTomlPath));
        this.localPort = port;
        logger.info("set local port on toml to {}", port);
    }

    private void setTomlName() throws IOException {
        Map<String, Object> tomlMap = this.toml.toMap();

        ((ArrayList<HashMap<String,Object>>) tomlMap.get("proxies")).getFirst()
                .put("name", "MCDC-bot");

        TomlWriter tomlWriter = new TomlWriter();
        tomlWriter.write(tomlMap, new File(this.clientTomlPath));
        logger.info("set name on toml");
    }

    public void setTomlPublicIP(String ip) throws IOException {
        Map<String, Object> tomlMap = this.toml.toMap();

        tomlMap.put("serverAddr", ip);

        TomlWriter tomlWriter = new TomlWriter();
        tomlWriter.write(tomlMap, new File(this.clientTomlPath));
        this.serverAddr = ip;
        logger.info("set public ip on toml to {}", ip);
    }

    public long getServerPort(){
        Map<String, Object> tomlMap = this.toml.toMap();

        return (long) tomlMap.get("serverPort");
    }

    public void setFrpcPathMac(String fileName) throws IOException {
        File tempFrpc = File.createTempFile("frpc","");
        tempFrpc.deleteOnExit();
        File tempFrpcToml = File.createTempFile("frpc",".toml");
        tempFrpcToml.deleteOnExit();

        try (InputStream inputStream = Main.class.getResourceAsStream("/" + fileName)) {
            if (inputStream == null){
                logger.error("Input stream of frpc mac is null");
                return;
            }
            try (FileOutputStream outputStream = new FileOutputStream(tempFrpc)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }

        boolean a = tempFrpc.setExecutable(true,false);
        if (a){
            logger.info("Successfully set FRPC as an mac executable");
        }else{
            logger.error("Failed to set FRPC as an mac executable");
        }

        try (InputStream inputStreamToml = Main.class.getResourceAsStream("/frpc.toml")) {
            if (inputStreamToml == null){
                logger.error("Input stream of mac toml is null");
                return;
            }

            try (FileOutputStream outputStream = new FileOutputStream(tempFrpcToml)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStreamToml.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }

        this.clientPath = tempFrpc.getParentFile().getAbsolutePath();
        this.clientFilePath = tempFrpc.getAbsolutePath();
        this.clientTomlPath = tempFrpcToml.getAbsolutePath();

        logger.info("Output mac frp client with filename: {}", fileName);
    }

    public void setFrpcPathWin(String fileName) throws IOException {
        File tempFrpc = File.createTempFile("frpc",".exe");
        tempFrpc.deleteOnExit();
        File tempFrpcToml = File.createTempFile("frpc",".toml");
        tempFrpcToml.deleteOnExit();

        try (InputStream inputStream = Main.class.getResourceAsStream("/" + fileName)) {
            if (inputStream == null) {
                logger.error("Input stream of frpc windows is null");
                return;
            }
            try (FileOutputStream outputStream = new FileOutputStream(tempFrpc)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }

        boolean b = tempFrpc.setExecutable(true,false);
        if (b){
            logger.info("Successfully set FRPC as an windows executable");
        }else {
            logger.error("Failed to set FRPC as an windows executable");
        }


        try (InputStream inputStreamToml = Main.class.getResourceAsStream("/frpc.toml")) {
            if (inputStreamToml == null){
                logger.error("Input stream of windows toml is null");
                return;
            }
            try (FileOutputStream outputStream = new FileOutputStream(tempFrpcToml)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStreamToml.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }

        this.clientPath = tempFrpc.getParentFile().getAbsolutePath();
        this.clientFilePath = tempFrpc.getAbsolutePath();
        this.clientTomlPath = tempFrpcToml.getAbsolutePath();

        logger.info("Output windows frp client with filename: {}", fileName);
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public int getLocalPort() {
        return localPort;
    }
}
