package com.jerseybot.functests.base;

import com.jerseybot.functests.config.BotConfig;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BotTestBase {
    @Getter
    protected final JDA jda;

    @Autowired
    public BotTestBase(BotConfig botConfig) throws InterruptedException {
        jda = createJdaInstance(botConfig);
    }

    public JDA createJdaInstance(BotConfig botConfig) throws InterruptedException {
        return JDABuilder.createDefault(botConfig.getToken())
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
                .build()
                .awaitReady();
    }
}
