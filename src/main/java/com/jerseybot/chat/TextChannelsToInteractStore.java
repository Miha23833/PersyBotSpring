package com.jerseybot.chat;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TextChannelsToInteractStore {
    private final Map<Long, Long> textChannelByGuild = new ConcurrentHashMap<>();

    public void update(Long guildId, Long textChatId) {
        this.textChannelByGuild.put(guildId, textChatId);
    }

    public Long get(Long guildId) {
        return this.textChannelByGuild.get(guildId);
    }

    public boolean contains(Long guildId) {
        return textChannelByGuild.containsKey(guildId);
    }
}
