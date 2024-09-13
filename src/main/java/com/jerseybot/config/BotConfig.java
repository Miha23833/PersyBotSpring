package com.jerseybot.config;

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

    @Getter
    @Value("${chat.defaultPrefix}")
    private String defaultPrefix;

    @Getter
    @Value("${performance.player.activityCheckPauseMillis}")
    private int activityCheckPauseMillis;
    @Getter
    @Value("${performance.player.maxInactivityTimeMillisIfNotPlaying}")
    private int maxInactivityTimeMillisIfNotPlaying;
    @Getter
    @Value("${performance.player.maxInactivityTimeMillisIfPaused}")
    private int maxInactivityTimeIfPaused;

    @Getter
    @Value("${chatgpt.context.maxHistoryLength}")
    private int chatgptMaxHistoryLength;
    @Getter
    @Value("${chatgpt.streamMessageUpdateMillis}")
    private int chatgptStreamMessageUpdateMillis;

    @Getter
    @Value("${yt.oauth.visitor_data}")
    private String ytOauthVisitor_data;
    @Getter
    @Value("${yt.oauth.po_token}")
    private String ytOauthPoToken;
}
