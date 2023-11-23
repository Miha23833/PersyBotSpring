package com.jerseybot.functests.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:bot.properties")
public class BotConfig {
    @Getter
    @Value("${api.token}")
    private String token;
}
