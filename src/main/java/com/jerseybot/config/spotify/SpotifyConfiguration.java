package com.jerseybot.config.spotify;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import se.michaelthelin.spotify.SpotifyApi;

@Configuration
@EnableScheduling
@PropertySource("classpath:spotify.properties")
public class SpotifyConfiguration {
    @Getter
    @Value("${clientId}")
    private String clientId;

    @Getter
    @Value("${clientSecret}")
    private String clientSecret;

    @Bean
    public SpotifyApi getSpotifyApi() {
        return SpotifyApi.builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();
    }
}
