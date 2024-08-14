package me.ErenY.GUI;

import me.ErenY.DiscordBot;
import me.ErenY.SSHManager.SSHManager;
import me.ErenY.servermanager.ServerManager;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.prefs.Preferences;

public class View {

    public static JTextArea sshConsole;
    public static JTextArea frpConsole;
    public static JTextArea serverConsole;

    public static JLabel serverLabel;
    public static JTextField serverip;
    public static JLabel dcbot;
    public static JLabel playerC;
    public static JPanel playersPanel;

    public static JButton refresh;
    public static JButton save;
    public static JButton cancel;
    public static JButton run;
    public static JButton refreshO;
    public static JButton installfrps;
    public static JButton showLogs;

    //options
    public static JTextField token;
    public static JTextField xmx;
    public static JButton sv_dir;
    public static JTextField sv_port;
    public static JTextField command_to;
    public static JTextField sv_dc_cid;
    public static JTextField sv_to_min;
    public static JComboBox<String> use_oci;
    public static JTextField instance_ocid;
    public static JTextField oci_to;
    public static JButton private_key_path;
    public static JTextField public_ip_ocid;
    public static JTextField public_ip;
    public static JTextField frpcPath;
    public static JComboBox<String> os;
    public static JComboBox<String> comPer;
    public static JComboBox<String> lockPer;
    public static JComboBox<String> playersPer;
    public static JComboBox<String> dcBotAct;
    public static JTextField dcBotActSt;
    public static JTextField dcBotNick;


    private static final Logger logger = LoggerFactory.getLogger(View.class);
    public static Preferences pref = Preferences.userRoot().node(View.class.getName());
    public static Properties prop = new Properties();

    public View() throws IOException {
        prop.load(View.class.getClassLoader().getResourceAsStream("default.properties"));
        logger.info("Loaded default properties");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Started shutdown hook");
            SSHManager sshManager = DiscordBot.getStaticDiscordBot() != null ? DiscordBot.getStaticDiscordBot().getSshManager() : null;
            if (sshManager != null){
                try {
                    sshManager.stopFRPCommand();
                    logger.info("Stopped FRP Server");
                } catch (Exception e) {
                    logger.error("Exception while stopping FRP Server", e);
                }
                sshManager.disconnect();
                logger.info("Disconnected SSH");
            }

            if (ServerManager.isStarted()){
                try {
                    ServerManager.StopServer();
                } catch (Exception e) {
                    logger.error("Exception on shutdown hook when stopping server", e);
                }
            }
        }));

        MyController myController = new MyController();

        JFrame jFrame = new JFrame();
        jFrame.setSize(1100,500);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTabbedPane tab = new JTabbedPane();

        JPanel consoleTab = new JPanel(new GridLayout(1, 2, 5, 5));

        JPanel sshPanel = new JPanel(new BorderLayout());
        sshConsole = new JTextArea();
        sshConsole.setFont(sshConsole.getFont().deriveFont(12f));
        sshConsole.setEditable(false);
        ScrollPane scrollssh = new ScrollPane();
        scrollssh.add(sshConsole);
        sshPanel.add(scrollssh,BorderLayout.CENTER);
        sshPanel.setBorder(BorderFactory.createTitledBorder("SSH Console Output"));


        JPanel frpPanel = new JPanel(new BorderLayout());
        frpConsole = new JTextArea();
        frpConsole.setFont(frpConsole.getFont().deriveFont(12f));
        frpConsole.setEditable(false);
        ScrollPane scrollfrp = new ScrollPane();
        scrollfrp.add(frpConsole);
        frpPanel.add(scrollfrp, BorderLayout.CENTER);
        frpPanel.setBorder(BorderFactory.createTitledBorder("FRP Console Output"));

        JPanel serverPanel = new JPanel(new BorderLayout());
        serverConsole = new JTextArea();
        JTextField serverInput = new JTextField();
        serverInput.addActionListener(myController);
        serverInput.setBorder(BorderFactory.createTitledBorder(""));
        serverConsole.setFont(serverConsole.getFont().deriveFont(12f));
        serverConsole.setEditable(false);
        ScrollPane scrollSer = new ScrollPane();
        scrollSer.add(serverConsole);
        serverPanel.add(scrollSer, BorderLayout.CENTER);
        serverPanel.add(serverInput, BorderLayout.SOUTH);
        serverPanel.setBorder(BorderFactory.createTitledBorder("Server Console Output"));

        consoleTab.add(sshPanel);
        consoleTab.add(frpPanel);
        //consoleTab.add(serverPanel);


        JPanel statusTab = new JPanel(new GridLayout(1,2));
        JPanel left = new JPanel(new BorderLayout());
        JPanel leftin = new JPanel(new GridLayout(2,1));
        JPanel leftTop = new JPanel(new GridLayout(2,2,5,5));

        JLabel server = new JLabel("Server: ");
        View.serverLabel = server;
        leftTop.add(server);

        JLabel dcbot = new JLabel("Discord Bot: ");
        View.dcbot = dcbot;
        leftTop.add(dcbot);

        JTextField serverip = new JTextField();
        serverip.setCaret(new DefaultCaret() {

            @Override
            public void paint(Graphics g) {
            }

            @Override
            public boolean isVisible() {
                return false;
            }

            @Override
            public boolean isSelectionVisible() {
                return false;
            }

        });
        serverip.setText("Server ip: ");
        serverip.setEditable(false);
        serverip.setBackground(null);
        serverip.setBorder(null);
        View.serverip = serverip;
        leftTop.add(serverip);

        JLabel playerC = new JLabel("Player Count: ");
        View.playerC = playerC;
        leftTop.add(playerC);

        leftTop.setBorder(BorderFactory.createTitledBorder("Status"));
        leftin.add(leftTop);

        JPanel players = new JPanel(new GridLayout(0,1,0,0));
        View.playersPanel = players;
        players.setBorder(BorderFactory.createTitledBorder("Players"));
        ScrollPane sp = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);

        sp.add(players);

        leftin.add(sp);

        left.add(leftin,BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(myController);
        View.refresh = refresh;

        JButton run = new JButton("Run");
        run.addActionListener(myController);
        View.run = run;

        JPanel leftBot = new JPanel();
        leftBot.add(run);
        leftBot.add(refresh);

        left.add(leftBot, BorderLayout.SOUTH);


        JPanel right = new JPanel(new BorderLayout());
        right.add(serverPanel);

        statusTab.add(left);
        statusTab.add(right);

        JPanel optionsTab = new JPanel(new BorderLayout());
        JPanel optionin = new JPanel(new GridLayout(22,2,10,5));
        JScrollPane options = new JScrollPane(optionin);
        JPanel optionBot = new JPanel();

        JButton save = new JButton("Save");
        save.addActionListener(myController);
        View.save = save;
        optionBot.add(save, BorderLayout.SOUTH);

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(myController);
        View.cancel = cancel;
        optionBot.add(cancel, BorderLayout.SOUTH);

        JButton refreshO = new JButton("Refresh");
        refreshO.addActionListener(myController);
        View.refreshO = refreshO;
        optionBot.add(refreshO, BorderLayout.SOUTH);

        JButton Installfrps = new JButton("Install frp to Server");
        Installfrps.addActionListener(myController);
        View.installfrps = Installfrps;
        optionBot.add(installfrps, BorderLayout.SOUTH);

        JButton showLogs = new JButton("Show Logs");
        showLogs.addActionListener(myController);
        View.showLogs = showLogs;
        optionBot.add(showLogs, BorderLayout.SOUTH);

        optionsTab.add(options, BorderLayout.CENTER);
        optionsTab.add(optionBot, BorderLayout.SOUTH);

        JPopupMenu menu = new JPopupMenu();
        Action cut = new DefaultEditorKit.CutAction();
        cut.putValue(Action.NAME, "Cut");
        cut.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control X"));
        menu.add(cut);

        Action copy = new DefaultEditorKit.CopyAction();
        copy.putValue(Action.NAME, "Copy");
        copy.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"));
        menu.add(copy);

        Action paste = new DefaultEditorKit.PasteAction();
        paste.putValue(Action.NAME, "Paste");
        paste.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control V"));
        menu.add(paste);

        optionin.add(new JLabel("Discord Bot Token"));
        token = new JTextField();
        token.setComponentPopupMenu(menu);
        optionin.add(token);

        optionin.add(new JLabel("Private Key Path"));
        private_key_path = new JButton("Select File");
        private_key_path.setHorizontalAlignment(JButton.LEFT);
        private_key_path.addActionListener(myController);

        optionin.add(private_key_path);

        optionin.add(new JLabel("INSTANCE OCID"));
        instance_ocid = new JTextField();
        instance_ocid.setComponentPopupMenu(menu);
        optionin.add(instance_ocid);

        optionin.add(new JLabel("Server Path"));
        sv_dir = new JButton("Select File");
        sv_dir.setHorizontalAlignment(JButton.LEFT);
        sv_dir.addActionListener(myController);
        optionin.add(sv_dir);

        optionin.add(new JLabel("OS"));
        os = new JComboBox<>();
        os.addItem("Windows AMD64");
        os.addItem("Windows ARM");
        os.addItem("MacOS AMD64");
        os.addItem("MacOS ARM");
        os.addItem("Other");
        optionin.add(os);

        optionin.add(new JLabel("Server To Discord Channel ID"));
        sv_dc_cid = new JTextField();
        sv_dc_cid.setComponentPopupMenu(menu);
        optionin.add(sv_dc_cid);

        optionin.add(new JLabel("XMX"));
        xmx = new JTextField();
        xmx.setComponentPopupMenu(menu);
        optionin.add(xmx);

        optionin.add(new JLabel("Server Port"));
        sv_port = new JTextField();
        sv_port.setComponentPopupMenu(menu);
        optionin.add(sv_port);

        optionin.add(new JLabel("Command Timeout"));
        command_to = new JTextField();
        command_to.setComponentPopupMenu(menu);
        optionin.add(command_to);

        optionin.add(new JLabel("Server Timeout Min"));
        sv_to_min = new JTextField();
        sv_to_min.setComponentPopupMenu(menu);
        optionin.add(sv_to_min);

        optionin.add(new JLabel("Use OCI"));
        use_oci = new JComboBox<>();
        use_oci.addItem("True");
        use_oci.addItem("False");
        optionin.add(use_oci);

        optionin.add(new JLabel("OCI Timeout"));
        oci_to = new JTextField();
        oci_to.setComponentPopupMenu(menu);
        optionin.add(oci_to);

        optionin.add(new JLabel("Public Ip OCID"));
        public_ip_ocid = new JTextField();
        public_ip_ocid.setComponentPopupMenu(menu);
        optionin.add(public_ip_ocid);

        optionin.add(new JLabel("Public Ip"));
        public_ip = new JTextField();
        public_ip.setComponentPopupMenu(menu);
        optionin.add(public_ip);

        optionin.add(new JLabel("FRPC Path"));
        frpcPath = new JTextField();
        frpcPath.setComponentPopupMenu(menu);
        optionin.add(frpcPath);

        optionin.add(new JLabel("!command permissions"));
        comPer = new JComboBox<>();
        comPer.addItem("Owner");
        comPer.addItem("Everyone");
        optionin.add(comPer);

        optionin.add(new JLabel("!server switch lockdown permissions"));
        lockPer = new JComboBox<>();
        lockPer.addItem("Owner");
        lockPer.addItem("Everyone");
        optionin.add(lockPer);

        optionin.add(new JLabel("!server players permissions"));
        playersPer = new JComboBox<>();
        playersPer.addItem("Owner");
        playersPer.addItem("Everyone");
        optionin.add(playersPer);

        optionin.add(new JLabel("Discord Bot Activity"));
        dcBotAct = new JComboBox<>();
        dcBotAct.addItem("Playing");
        dcBotAct.addItem("Watching");
        dcBotAct.addItem("Competing");
        dcBotAct.addItem("Listening");
        dcBotAct.addItem("Streaming");
        dcBotAct.addItem("Custom Status");
        optionin.add(dcBotAct);

        optionin.add(new JLabel("Discord Bot Activity Message"));
        dcBotActSt = new JTextField();
        dcBotActSt.setComponentPopupMenu(menu);
        optionin.add(dcBotActSt);

        optionin.add(new JLabel("Discord Bot Nickname"));
        dcBotNick = new JTextField();
        dcBotNick.setComponentPopupMenu(menu);
        optionin.add(dcBotNick);

        tab.addTab("Status", statusTab);
        tab.addTab("Console", consoleTab);
        tab.addTab("Options", optionsTab);


        jFrame.add(tab);
        refresh.doClick();
        jFrame.setVisible(true);

    }

    static class MyController implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if ((e.getSource() instanceof JButton button)){
                if (button.getText().equalsIgnoreCase("refresh")) {


                    serverLabel.setText("Server: " + (ServerManager.isStarted() ? "On" : "Off"));
                    serverip.setText("Server ip: " + ServerManager.getPublicIpString());
                    dcbot.setText("Discord Bot: " + (DiscordBot.getStaticDiscordBot()!=null ? "On":"Off"));

                    token.setText(pref.get("TOKEN",""));
                    xmx.setText(pref.get("XMX", prop.getProperty("XMX")));
                    sv_dir.setText(pref.get("SERVER_DIRECTORY",""));
                    sv_port.setText(pref.get("SERVER_PORT", prop.getProperty("SERVER_PORT")));
                    command_to.setText(pref.get("COMMAND_TIMEOUT", prop.getProperty("COMMAND_TIMEOUT")));
                    sv_dc_cid.setText(pref.get("SERVER_TO_DISCORD_CHANNEL_ID",""));
                    sv_to_min.setText(pref.get("SERVER_TIMEOUT_MIN", prop.getProperty("SERVER_TIMEOUT_MIN")));
                    use_oci.setSelectedItem(pref.get("USE_OCI", prop.getProperty("USE_OCI")));
                    instance_ocid.setText(pref.get("INSTANCE_OCID",""));
                    oci_to.setText(pref.get("OCI_TIMEOUT", prop.getProperty("OCI_TIMEOUT")));
                    private_key_path.setText(pref.get("PRIVATE_KEY_PATH",""));
                    public_ip_ocid.setText(pref.get("PUBLIC_IP_OCID", prop.getProperty("PUBLIC_IP_OCID")));
                    public_ip.setText(pref.get("PUBLIC_IP", prop.getProperty("PUBLIC_IP")));
                    os.setSelectedItem(pref.get("OS", "Other"));
                    comPer.setSelectedItem(pref.get("COMMAND_PERMISSION", prop.getProperty("COMMAND_PERMISSION")));
                    lockPer.setSelectedItem(pref.get("LOCKDOWN_PERMISSION", prop.getProperty("LOCKDOWN_PERMISSION")));
                    playersPer.setSelectedItem(pref.get("PLAYERS_PERMISSION", prop.getProperty("PLAYERS_PERMISSION")));
                    dcBotAct.setSelectedItem(pref.get("DISCORD_BOT_ACTIVITY", prop.getProperty("DISCORD_BOT_ACTIVITY")));
                    dcBotActSt.setText(pref.get("DISCORD_BOT_ACTIVITY_MESSAGE", prop.getProperty("DISCORD_BOT_ACTIVITY_MESSAGE")));
                    dcBotNick.setText(pref.get("DISCORD_BOT_NICKNAME", prop.getProperty("DISCORD_BOT_NICKNAME")));

                    logger.info("Refreshed");

                    if (DiscordBot.getStaticDiscordBot() != null){

                        Activity activity = switch ((String) Objects.requireNonNull(dcBotAct.getSelectedItem())) {
                            case "Playing" -> Activity.playing(dcBotActSt.getText());
                            case "Watching" -> Activity.watching(dcBotActSt.getText());
                            case "Competing" -> Activity.competing(dcBotActSt.getText());
                            case "Listening" -> Activity.listening(dcBotActSt.getText());
                            case "Streaming" -> Activity.streaming("", dcBotActSt.getText());
                            case "Custom Status" -> Activity.customStatus(dcBotActSt.getText());
                            default -> null;
                        };
                        if (!ServerManager.isStarted()){
                            DiscordBot.getStaticDiscordBot().getShardManager().getGuilds().getFirst().getSelfMember()
                                    .modifyNickname(pref.get("DISCORD_BOT_NICKNAME", "MC-DC Bot")).queue();
                            logger.info("Set discord bot nickname");
                            DiscordBot.getStaticDiscordBot().getShardManager().setActivity(activity);
                            logger.info("Set custom activity for discord bot");
                        }
                    }

                    if (ServerManager.isStarted()){

                        try {
                            ServerManager.SendMessageToServer("list");
                        } catch (Exception ex) {
                            logger.error("Exception while sending list command to server", ex);
                        }

                    }else {
                        playerC.setText("Player Count: Off");
                        playersPanel.removeAll();
                        playersPanel.revalidate();
                        playersPanel.repaint();
                    }

                }
                if (button.getText().equalsIgnoreCase("run") && dcbot.getText().equalsIgnoreCase("Discord Bot: off")){
                    if (!pref.get("TOKEN","").isBlank()){
                        if (!(pref.get("USE_OCI", prop.getProperty("USE_OCI")).equalsIgnoreCase("true")
                                && (pref.get("PRIVATE_KEY_PATH","").isBlank() || pref.get("INSTANCE_OCID","").isBlank()))){
                            try {
                                new DiscordBot();
                            } catch (Exception ex) {
                                logger.error("Failed to start discord bot", ex);
                            }
                            refresh.doClick();
                            logger.info("Ran Discord Bot");
                        }else {
                            JOptionPane.showMessageDialog(null,"Mandatory OCID information is blank even though Use OCI is set to true");
                            logger.warn("Mandatory ocid info is blank even though use ocid is true");
                        }
                    }else {
                        JOptionPane.showMessageDialog(null, "Discord bot token is blank, cannot start bot");
                        logger.warn("Discord bot token is blank therefore start process aborted");
                    }
                }
                if (button.getText().equalsIgnoreCase("save")){

                    if (!pref.get("INSTANCE_OCID","").equalsIgnoreCase(instance_ocid.getText())){
                        JOptionPane.showMessageDialog(null, "OCID changed you may want to install frp to server");
                        logger.info("OCID changed");
                    }

                    pref.put("TOKEN",token.getText());

                    try {
                        Integer.parseInt(xmx.getText());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "XMX must be an integer");
                        return;
                    }
                    pref.put("XMX",xmx.getText());
                    pref.put("SERVER_DIRECTORY",sv_dir.getText());

                    try {
                        Integer.parseInt(sv_port.getText());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Server Port must be an integer");
                        return;
                    }
                    pref.put("SERVER_PORT",sv_port.getText());

                    try {
                        Integer.parseInt(command_to.getText());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Command Timeout must be an integer");
                        return;
                    }
                    pref.put("COMMAND_TIMEOUT",command_to.getText());
                    pref.put("SERVER_TO_DISCORD_CHANNEL_ID",sv_dc_cid.getText());

                    try {
                        Integer.parseInt(sv_to_min.getText());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Server Timeout Min must be an integer");
                        return;
                    }
                    pref.put("SERVER_TIMEOUT_MIN",sv_to_min.getText());
                    pref.put("USE_OCI", (String) use_oci.getSelectedItem());
                    pref.put("INSTANCE_OCID",instance_ocid.getText());

                    try {
                        Integer.parseInt(oci_to.getText());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "OCI Timeout must be an integer");
                        return;
                    }
                    pref.put("OCI_TIMEOUT",oci_to.getText());
                    pref.put("PRIVATE_KEY_PATH",private_key_path.getText());
                    pref.put("PUBLIC_IP_OCID",public_ip_ocid.getText());
                    pref.put("PUBLIC_IP",public_ip.getText());
                    pref.put("OS", (String) os.getSelectedItem());
                    pref.put("COMMAND_PERMISSION", (String) comPer.getSelectedItem());
                    pref.put("LOCKDOWN_PERMISSION", (String) lockPer.getSelectedItem());
                    pref.put("PLAYERS_PERMISSION", (String) playersPer.getSelectedItem());
                    pref.put("DISCORD_BOT_ACTIVITY", (String) dcBotAct.getSelectedItem());
                    pref.put("DISCORD_BOT_ACTIVITY_MESSAGE", dcBotActSt.getText());
                    pref.put("DISCORD_BOT_NICKNAME", dcBotNick.getText());

                    logger.info("Saved preferences");

                    switch ((String) Objects.requireNonNull(os.getSelectedItem())){
                        case "Windows AMD64":
                            try {
                                DiscordBot.getStaticDiscordBot().getFrpManager().setFrpcPathWin("frpcWinAMD.exe");
                            } catch (Exception ex) {
                                logger.error("Failed to create frpc", ex);
                            }
                            break;
                        case "Windows ARM":
                            try {
                                DiscordBot.getStaticDiscordBot().getFrpManager().setFrpcPathWin("frpcWinARM.exe");
                            } catch (Exception ex) {
                                logger.error("Failed to create frpc", ex);
                            }
                            break;
                        case "MacOS AMD64":
                            try {
                                DiscordBot.getStaticDiscordBot().getFrpManager().setFrpcPathMac("frpcMacOSAMD");
                            } catch (Exception ex) {
                                logger.error("Failed to create frpc", ex);
                            }
                            break;
                        case "MacOS ARM":
                            try {
                                DiscordBot.getStaticDiscordBot().getFrpManager().setFrpcPathMac("frpcMacOSARM");
                                logger.info("Succesfully created frpc");
                            } catch (Exception ex) {
                                logger.error("Failed to create frpc", ex);
                            }
                            break;
                        case "Other":
                            pref.put("FRPC_PATH", frpcPath.getText());
                            break;
                    }

                }

                if (button.getText().equalsIgnoreCase(installfrps.getText())){
                    logger.info("Started install of frps to server");
                        try {
                            DiscordBot.getStaticDiscordBot().getSshManager().uploadfrps();
                            logger.info("Installed frps to server");
                            JOptionPane.showMessageDialog(null, "Installed FRPS to server");
                        } catch (Exception ex) {
                            logger.info("Failed to upload frps", ex);
                            JOptionPane.showMessageDialog(null, "Failed to install FRPS to server");
                        }

                }
                if (button.getText().equalsIgnoreCase("Cancel")){
                    refresh.doClick();
                    logger.info("Cancelled");
                }
                if (button.getText().equalsIgnoreCase("Show Logs")){
                    if (Desktop.isDesktopSupported()){
                        try {
                            Desktop.getDesktop().open(new File(System.getProperty("java.io.tmpdir")+"/mcdcbotLogs"));
                            logger.info("Showed Logs");
                        } catch (IOException ex) {
                            logger.error("Failed to open log folder", ex);
                        }
                    }
                }

                if (button.equals(private_key_path)){
                    JFileChooser jFileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                    jFileChooser.setFileFilter(new FileNameExtensionFilter("*.key","key"));
                    int response = jFileChooser.showDialog(null, "Select");
                    if (response == JFileChooser.APPROVE_OPTION){
                        String s = jFileChooser.getSelectedFile().getAbsolutePath();
                        s = s.replace("\\", "/");
                        if (!s.endsWith(".key")){
                            JOptionPane.showMessageDialog(null, "Selected file must be a .key file");
                            logger.info("selected file is not key file");
                            return;
                        }
                        private_key_path.setText(s);
                        logger.info("Selected private key file");
                    }else {
                        logger.info("Cancelled private key file selection");
                    }
                }

                if (button.equals(sv_dir)){
                    JFileChooser jFileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                    jFileChooser.setFileFilter(new FileNameExtensionFilter("*.jar","jar"));
                    int response = jFileChooser.showDialog(null, "Select");
                    if (response == JFileChooser.APPROVE_OPTION){
                        File f = jFileChooser.getSelectedFile();
                        String s = jFileChooser.getSelectedFile().getAbsolutePath();
                        if (!s.endsWith(".jar")){
                            JOptionPane.showMessageDialog(null, "Selected file must be a .jar file");
                            logger.info("Selected file is not jar file");
                            return;
                        }
                        s = f.getParent().replace("\\", "/");
                        sv_dir.setText(s);
                        logger.info("Selected server jar file");
                    }else {
                        logger.info("Cancelled server jar file selection");
                    }
                }
            }


            if (e.getSource() instanceof JTextField t){
                if (!t.getText().isEmpty() && ServerManager.isStarted()){
                    try {
                        ServerManager.SendMessageToServer(t.getText());
                        logger.info("Sent {} text to server", t.getText());
                        t.setText("");
                    } catch (IOException ex) {
                        logger.error("Failed to send command to server", ex);
                    }
                }
            }
        }
    }
}
