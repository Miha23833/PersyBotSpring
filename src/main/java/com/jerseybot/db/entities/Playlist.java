package com.jerseybot.db.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.cache.annotation.Cacheable;

@Entity
@Cacheable("Playlist")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "Playlist")
public class Playlist {
    @EmbeddedId
    private PlaylistId playlistId;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Getter@Setter
    private String url;

    @ManyToOne
    @JoinColumn(name = "discord_server_id", insertable = false, updatable = false)
    private DiscordServer discordServer;

    public String getName() {
        return playlistId.getName();
    }

    public Playlist(DiscordServer discordServer, String name, String url) {
        this.playlistId = new PlaylistId(name, discordServer);
        this.url = url;
    }

    public Playlist() {}
}
