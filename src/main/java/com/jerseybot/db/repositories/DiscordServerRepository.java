package com.jerseybot.db.repositories;

import com.jerseybot.db.entities.DiscordServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface DiscordServerRepository extends JpaRepository<DiscordServer, Long> {
    default DiscordServer getOrCreateDefault(Long id) {
        return findById(id).orElseGet(() -> save(new DiscordServer(id)));
    }
}
