package com.jerseybot.db.repositories;

import com.jerseybot.db.entities.DiscordServer;
import com.jerseybot.db.entities.Playlist;
import com.jerseybot.db.entities.PlaylistId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface PlayListRepository extends JpaRepository<Playlist, PlaylistId> {
    @Query(value = "SELECT * FROM PLAYLIST WHERE discord_server_id = :dsid", nativeQuery = true)
    List<Playlist> findAllByDiscordServerId(@Param("dsid") long discordServerId);

    default Optional<Playlist> findById(String name, DiscordServer discordServerId) {
        return findById(new PlaylistId(name, discordServerId));
    }
}
