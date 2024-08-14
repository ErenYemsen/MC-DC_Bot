package me.ErenY;

import me.ErenY.FRPManager.FRPManager;
import me.ErenY.GUI.View;
import me.ErenY.SSHManager.SSHManager;
import me.ErenY.commands.CommandManager;
import me.ErenY.listeners.DiscordListener;
import me.ErenY.ocimanager.OCIManager;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DiscordBot {
    private static final Logger logger = LoggerFactory.getLogger(DiscordBot.class);
    public static boolean lockdownMode = false;
    private final ShardManager shardManager;
    private static DiscordBot staticDiscordBot = null;
    private final OCIManager ociManager;
    private final SSHManager sshManager;
    private final FRPManager frpManager;

    public DiscordBot() throws Exception {
        String token = View.pref.get("TOKEN", "");

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);
        builder.enableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT);
        builder.setStatus(OnlineStatus.DO_NOT_DISTURB);
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.setChunkingFilter(ChunkingFilter.ALL);
        builder.enableCache(CacheFlag.ONLINE_STATUS);
        shardManager = builder.build();
        shardManager.addEventListener(new DiscordListener(), new CommandManager());

        staticDiscordBot = this;

        if (View.pref.get("USE_OCI", View.prop.getProperty("USE_OCI")).equalsIgnoreCase("true")) {
            this.ociManager = new OCIManager();
            this.sshManager = new SSHManager();
            this.frpManager = new FRPManager();
        }else {
            this.ociManager = null;
            this.sshManager = null;
            this.frpManager = null;
        }

        logger.info("Created Discord bot object");
    }

    public ShardManager getShardManager(){
        return this.shardManager;
    }

    public OCIManager getOciManager(){
        return this.ociManager;
    }

    public static DiscordBot getStaticDiscordBot() {
        return staticDiscordBot;
    }

    public SSHManager getSshManager() {
        return sshManager;
    }

    public FRPManager getFrpManager() {
        return frpManager;
    }
}
