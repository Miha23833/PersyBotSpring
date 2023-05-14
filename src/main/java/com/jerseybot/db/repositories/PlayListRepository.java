package com.jerseybot.db.repositories;

import com.jerseybot.db.entities.Playlist;
import com.jerseybot.db.entities.PlaylistId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayListRepository extends JpaRepository<Playlist, PlaylistId> {

}
