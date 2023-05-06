package com.jerseybot;

import com.jerseybot.config.BotConfig;
import com.jerseybot.config.PlayerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:bot.properties")
@Import({BotConfig.class, PlayerConfig.class})
public class ApplicationConfiguration {
}
