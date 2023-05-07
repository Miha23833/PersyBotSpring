package com.jerseybot.db.repositories;

import com.jerseybot.db.entities.DiscordServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscordServerRepository extends JpaRepository<DiscordServer, Long> {
}
