package com.jerseybot.music;

import com.jerseybot.db.repositories.DiscordServerRepository;
import com.jerseybot.music.player.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PlayerRepository {
    private final ApplicationContext applicationContext;
    private final DiscordServerRepository discordServerRepository;
    private final Map<Long, Player> playersByGuildId = new ConcurrentHashMap<>();

    @Autowired
    public PlayerRepository(ApplicationContext applicationContext, DiscordServerRepository discordServerRepository) {
        this.applicationContext = applicationContext;
        this.discordServerRepository = discordServerRepository;
    }

    public Player get(Long guildId) {
        if (playersByGuildId.containsKey(guildId)) {
            return playersByGuildId.get(guildId);
        }
        Player newPlayer = applicationContext.getBean(Player.class);
        newPlayer.setVolume(discordServerRepository.getOrCreateDefault(guildId).getSettings().getVolume());
        playersByGuildId.put(guildId, newPlayer);
        return newPlayer;
    }

    public boolean hasInitializedPlayer(Long guildId) {
        return playersByGuildId.containsKey(guildId);
    }
}
