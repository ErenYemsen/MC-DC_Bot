package me.ErenY;

import io.github.cdimascio.dotenv.Dotenv;
import me.ErenY.commands.CommandManager;
import me.ErenY.listeners.MyEventListener;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;


public class DiscordBot {
    public static final Dotenv config = Dotenv.configure().load();
    private final ShardManager shardManager;
    private static DiscordBot staticDiscordBot;

    public DiscordBot() {
        String token = config.get("TOKEN");

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);
        builder.enableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT);
        builder.setStatus(OnlineStatus.DO_NOT_DISTURB);
        builder.setActivity(Activity.watching("Porno"));
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.setChunkingFilter(ChunkingFilter.ALL);
        builder.enableCache(CacheFlag.ONLINE_STATUS);
        shardManager = builder.build();

        shardManager.addEventListener(new MyEventListener(), new CommandManager());
    }

    public ShardManager getShardManager(){
        return  this.shardManager;
    }

    public static DiscordBot getStaticDiscordBot() {
        return staticDiscordBot;
    }

    public static void main(String[] args) {

        DiscordBot discordBot = new DiscordBot();
        staticDiscordBot = discordBot;

    }


}
