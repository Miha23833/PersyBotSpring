package com.jerseybot.db.repositories;

import com.jerseybot.db.entities.DiscordServerSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscordServerSettingsRepository extends JpaRepository<DiscordServerSettings, Long> {
}
