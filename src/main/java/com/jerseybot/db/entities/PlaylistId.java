package com.jerseybot.db.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;

import java.io.Serializable;

@Embeddable
public class PlaylistId implements Serializable {
    public static final int MAX_PLAYLIST_NAME_LENGTH = 16;

    @Column(name = "name", nullable = false, length = MAX_PLAYLIST_NAME_LENGTH)
    @Getter
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional=false)
    @JoinColumn(name="discord_server_id", nullable=false)
    private DiscordServer discordServer;

    public PlaylistId(String name, DiscordServer discordServer) {
        this.name = name;
        this.discordServer = discordServer;
    }
    public PlaylistId() {}
}
