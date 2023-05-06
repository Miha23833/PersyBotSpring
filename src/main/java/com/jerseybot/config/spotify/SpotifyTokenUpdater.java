package com.jerseybot.config.spotify;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.michaelthelin.spotify.SpotifyApi;

import java.util.concurrent.TimeUnit;

@Component
public class SpotifyTokenUpdater {
    private final SpotifyApi spotifyApi;

    public SpotifyTokenUpdater(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    @Scheduled(fixedDelay = 59, timeUnit = TimeUnit.MINUTES)
    private void updateToken() {
        HttpResponse<JsonNode> jsonResponse = Unirest.post("https://accounts.spotify.com/api/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .field("grant_type", "client_credentials")
                .field("client_id", spotifyApi.getClientId())
                .field("client_secret", spotifyApi.getClientSecret()).asJson();

        spotifyApi.setAccessToken(jsonResponse.getBody().getObject().getString("access_token"));
    }
}
