package com.jerseybot.music;

import com.jerseybot.music.player.Player;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PlayerRepository {
    private final ApplicationContext applicationContext;
    private final Map<Long, Player> playersByGuildId = new ConcurrentHashMap<>();

    public PlayerRepository(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Player get(Long guildId) {
        if (playersByGuildId.containsKey(guildId)) {
            return playersByGuildId.get(guildId);
        }
        Player newPlayer = applicationContext.getBean(Player.class);
        playersByGuildId.put(guildId, newPlayer);
        return newPlayer;
    }
}
