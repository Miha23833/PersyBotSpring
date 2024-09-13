package com.jerseybot.config;

import com.google.api.services.youtube.YouTube;
import com.jerseybot.music.audiomanager.spotify.SpotifyAudioSourceManager;
import com.jerseybot.music.audiomanager.youtube.LazyYoutubeAudioSourceManager;
import com.jerseybot.music.audiomanager.youtube.LazyYoutubeAudioTrackFactory;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import se.michaelthelin.spotify.SpotifyApi;

@Configuration
public class AudioConfiguration {
    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public DefaultAudioPlayerManager getAudioSourceManager(LazyYoutubeAudioSourceManager youtubeAudioSourceManager,
                                                           SoundCloudAudioSourceManager soundCloudAudioSourceManager,
                                                           TwitchStreamAudioSourceManager twitchStreamAudioSourceManager,
                                                           SpotifyAudioSourceManager spotifyAudioSourceManager) {
        DefaultAudioPlayerManager manager = new DefaultAudioPlayerManager();
        manager.registerSourceManager(youtubeAudioSourceManager);
        manager.registerSourceManager(soundCloudAudioSourceManager);
        manager.registerSourceManager(twitchStreamAudioSourceManager);
        manager.registerSourceManager(spotifyAudioSourceManager);
        return manager;
    }

    @Bean
    public SoundCloudAudioSourceManager getSoundCloudAudioSourceManager() {
        return SoundCloudAudioSourceManager.createDefault();
    }

    @Bean
    public TwitchStreamAudioSourceManager getTwitchStreamAudioSourceManager() {
        return new TwitchStreamAudioSourceManager();
    }

    @Bean
    public SpotifyAudioSourceManager getSpotifyAudioSourceManager(SpotifyApi spotifyApi, LazyYoutubeAudioTrackFactory lazyYoutubeAudioTrackFactory) {
        return new SpotifyAudioSourceManager(spotifyApi, lazyYoutubeAudioTrackFactory);

    }

    @Bean
    public YoutubeSearchProvider getYoutubeSearchProvider() {
        return new YoutubeSearchProvider();
    }

    @Bean
    public YoutubeAudioSourceManager getYoutubeAudioSourceManager() {
        YoutubeAudioSourceManager manager = new YoutubeAudioSourceManager();
        manager.useOauth2(null, false);
        return manager;
    }
}
