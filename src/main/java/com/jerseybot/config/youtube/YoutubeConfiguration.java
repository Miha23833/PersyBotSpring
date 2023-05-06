package com.jerseybot.config.youtube;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
@EnableScheduling
@PropertySource("classpath:youtube.properties")
public class YoutubeConfiguration {
    @Value("${apiKey}")
    private String youTubeApiKey;

    @Value("${appName}")
    private String applicationName;

    @Getter
    @Value("${countryCodeForSearchEngine}")
    private String countryCodeForSearchEngine;

    @Getter
    @Value("${playlistItemsLimit}")
    private int playlistItemsLimit;

    @Bean
    public YouTube getYouTubeApi() throws GeneralSecurityException, IOException {
        return new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), new ApiKeyHttpRequestInitializer(youTubeApiKey))
                .setApplicationName(applicationName)
                .build();
    }

    private record ApiKeyHttpRequestInitializer(String apiKey) implements HttpRequestInitializer, HttpExecuteInterceptor {
        @Override
            public void initialize(HttpRequest request) {
                request.setInterceptor(this);
            }

            @Override
            public void intercept(HttpRequest request) {
                request.getUrl().set("key", apiKey);
            }
        }
}
