package com.jerseybot.music;

import com.jerseybot.collections.RollIngFixedSizeMap;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.jerseybot.config.PlayerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class AudioContentCache {
    private final RollIngFixedSizeMap<String, AudioTrack> cache;

    public AudioContentCache(@Value("${player.cacheSize}") int cacheSize) {
        cache = new RollIngFixedSizeMap<>(cacheSize);
    }

    public void put(String id, AudioTrack track) {
        this.cache.put(id, track);
    }

    public boolean contains(String id) {
        return cache.containsKey(id);
    }

    public AudioTrack get(String id) {
        if (contains(id)) {
            return cache.get(id);
        }
        return null;
    }
}
