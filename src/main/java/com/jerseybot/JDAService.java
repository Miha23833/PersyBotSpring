package com.jerseybot;

import com.jerseybot.adapters.UserInteractionEventListener;
import com.jerseybot.config.BotConfig;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@SpringBootApplication
public class JDAService implements CommandLineRunner {
    @Getter
    private final JDA jda;
    private final ApplicationContext applicationContext;

    @Autowired
    public JDAService(ApplicationContext applicationContext) throws InterruptedException {
        this.applicationContext = applicationContext;

        this. jda = JDABuilder.createDefault(applicationContext.getBean(BotConfig.class).getToken())
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
                .addEventListeners(applicationContext.getBean(UserInteractionEventListener.class))
                .build()
                .awaitReady();
    }

    @Override
    public void run(String... args) throws Exception {
        // Run as JDA instance created
    }

    public static void main(String[] args) {
        SpringApplication.run(JDAService.class, args);
    }
}
