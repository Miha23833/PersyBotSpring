package com.jerseybot.db.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;

@Embeddable
public class PlaylistId implements Serializable {
    @Column(name = "playlist_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long playlistId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="discord_server_id", nullable=false)
    private DiscordServer discordServer;

    PlaylistId(DiscordServer discordServer) {
        this.discordServer = discordServer;
    }
    public PlaylistId() {}
}
