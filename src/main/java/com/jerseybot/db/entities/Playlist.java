package com.jerseybot.db.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.cache.annotation.Cacheable;

@Entity
@Cacheable("DiscordServer.DiscordServerSettings")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Playlist {
    public static final int MAX_PLAYLIST_NAME_LENGTH = 16;

    @EmbeddedId
    private PlaylistId playlistId;

    @Column(nullable = false, length = MAX_PLAYLIST_NAME_LENGTH)
    @Getter@Setter
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Getter@Setter
    private String url;

    public Playlist(DiscordServer discordServer, String name, String url) {
        this.playlistId = new PlaylistId(discordServer);
        this.name = name;
        this.url = url;
    }

    public Playlist() {}
}
