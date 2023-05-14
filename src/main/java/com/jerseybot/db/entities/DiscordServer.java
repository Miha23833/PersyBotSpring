package com.jerseybot.db.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.cache.annotation.Cacheable;

@Entity
@Cacheable("DiscordServer")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "DiscordServer")
public class DiscordServer {
    @Id
    private Long discordServerId;

    @Getter@Setter
    @Column(nullable = false, length = 2)
    private String countryCode;

    @Getter
    @PrimaryKeyJoinColumn
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DiscordServerSettings settings;

//    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    @JoinColumn(name = "play_list_id")
//    @OnDelete(action = OnDeleteAction.CASCADE)
//    @MapKey(name = "name")
//    private Map<String, PlayList> playLists = new HashMap<>();

    public DiscordServer(long discordServerId) {
        this.discordServerId = discordServerId;
        this.countryCode = "RU";
        this.settings = new DiscordServerSettings(discordServerId, (byte) 100, "$");
    }

    public DiscordServer() {}
}
