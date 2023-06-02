package com.jerseybot.db.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

@Entity
@Cacheable("DiscordServer")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "DiscordServer")
@EqualsAndHashCode
public class DiscordServer {
    @Id
    @Getter
    private Long discordServerId;

    @Getter@Setter
    @Column(nullable = false, length = 2)
    private String countryCode;

    @Getter
    @PrimaryKeyJoinColumn
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DiscordServerSettings settings;

    @OneToMany(mappedBy = "discordServer", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Playlist> playlists;

    public DiscordServer(long discordServerId) {
        this.discordServerId = discordServerId;
        this.countryCode = "RU";
        this.settings = new DiscordServerSettings(discordServerId, 100, "$");
    }

    public DiscordServer() {}
}
