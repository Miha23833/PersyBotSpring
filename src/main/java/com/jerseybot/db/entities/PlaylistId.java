package com.jerseybot.db.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.io.Serializable;

@EqualsAndHashCode
@Embeddable
public class PlaylistId implements Serializable {
    public static final int MAX_PLAYLIST_NAME_LENGTH = 16;

    @Column(name = "name", nullable = false, length = MAX_PLAYLIST_NAME_LENGTH)
    @Getter
    private String name;

    @Column(name = "discord_server_id")
    private Long discordServerId;

    public PlaylistId(String name, DiscordServer discordServer) {
        this.name = name;
        this.discordServerId = discordServer.getDiscordServerId();
    }
    public PlaylistId() {}
}
