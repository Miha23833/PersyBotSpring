package com.jerseybot;

import com.jerseybot.config.BotConfig;
import com.jerseybot.config.PlayerConfig;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;

@Configuration
@PropertySource("classpath:bot.properties")
@Import({BotConfig.class, PlayerConfig.class})
public class ApplicationConfiguration {
    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public DefaultAudioPlayerManager getAudioSourceManager() {
        DefaultAudioPlayerManager manager = new DefaultAudioPlayerManager();
        manager.registerSourceManager(new YoutubeAudioSourceManager(true));
        manager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        manager.registerSourceManager(new TwitchStreamAudioSourceManager());
        return manager;
    }

//    @Bean
//    public AudioContentCache getAudioContentCache(@Value("${player.cacheSize}") int cacheSize) {
//        return new AudioContentCache(cacheSize);
//    }
//
//    @Bean
//    public Player getPlayer(DefaultAudioPlayerManager audioSourceManager, AudioContentCache audioContentCache) {
//        return new Player(audioSourceManager, audioContentCache);
//    }
//
//    @Bean
//    public PlayerRepository getPlayerRepository() {
//        return new PlayerRepository();
//    }
//
//    @Bean
//    public PlayMusicTextCommand getPlayMusicTextCommand(PlayerRepository playerRepository) {
//        return new PlayMusicTextCommand(playerRepository);
//    }
//
//    @Bean
//    public UserEventListener getUserEventListener(PlayMusicTextCommand playMusicTextCommand) {
//        return new UserEventListener(playMusicTextCommand);
//    }


}
