package com.jerseybot.db.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jetbrains.annotations.Range;
import org.springframework.cache.annotation.Cacheable;

@Entity
@Cacheable("DiscordServer.DiscordServerSettings")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "DiscordServerSettings")
public class DiscordServerSettings {
    public static final int PREFIX_MAX_LEN = 2;
    @Id
    private Long discordServerId;

    @Getter@Setter
    @Column(nullable = false)
    @Range(from = 0, to = 100)
    private byte volume;

    @Getter@Setter
    @Column(nullable = false, length = PREFIX_MAX_LEN)
    private String prefix;

//    @Getter
//    @Column(columnDefinition = "TEXT")
//    private String meetAudioLink;

//    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    @JoinColumn(name = "name")
//    @OnDelete(action = OnDeleteAction.NO_ACTION)
//    private EqualizerPreset preset;

    DiscordServerSettings(long discordServerId, byte volume, String prefix) {
        this.discordServerId = discordServerId;
        this.volume = volume;
        this.prefix = prefix;
    }



    public DiscordServerSettings() {}
}
