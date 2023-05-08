package com.jerseybot.db.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Range;

@Entity
public class DiscordServerSettings {
    @Id
    private Long discordServerId;

    @Getter@Setter
    @Column(nullable = false)
    @Range(from = 0, to = 100)
    private byte volume;

    @Getter@Setter
    @Column(nullable = false, length = 2)
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
