package com.jerseybot.functests.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:testServerData.properties")
public class TestServerData {
    @Getter
    @Value("${testServer.guildId}")
    Long guildId;

    @Getter
    @Value("${testServer.textChannelId_1}")
    Long textChannelId_1;

    @Getter
    @Value("${testServer.textChannelId_2}")
    Long textChannelId_2;

    @Getter
    @Value("${testServer.voiceChannelId_1}")
    Long voiceChannelId_1;

    @Getter
    @Value("${testServer.voiceChannelId_2}")
    Long voiceChannelId_2;

}
