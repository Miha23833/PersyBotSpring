package com.jerseybot;

import com.jerseybot.config.BotConfig;
import dev.lavalink.youtube.clients.Web;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@SpringBootApplication
public class App implements CommandLineRunner {

    @Autowired
    public App(List<ListenerAdapter> listenerAdapters, BotConfig botConfig) throws InterruptedException {
        Web.setPoTokenAndVisitorData(botConfig.getYtOauthPoToken(), botConfig.getYtOauthVisitor_data());

        JDABuilder.createDefault(botConfig.getToken())
                .enableIntents(GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MODERATION,
                        GatewayIntent.GUILD_WEBHOOKS,
                        GatewayIntent.GUILD_INVITES,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_MESSAGE_TYPING,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                        GatewayIntent.DIRECT_MESSAGE_TYPING,
                        GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(listenerAdapters.toArray())
                .build()
                .awaitReady();
    }

    @Override
    public void run(String... args) throws Exception {
        // Run as JDA instance created
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
