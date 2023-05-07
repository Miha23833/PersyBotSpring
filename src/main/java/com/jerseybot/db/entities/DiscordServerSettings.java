package com.jerseybot.db.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.jetbrains.annotations.Range;

@Entity
public class DiscordServerSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long settingsId;

    @Column(nullable = false)
    @Range(from = 0, to = 100)
    private byte volume;

    @Column(nullable = false, length = 2)
    private String prefix;

    @Column(columnDefinition = "TEXT")
    private String meetAudioLink;

//    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    @JoinColumn(name = "name")
//    @OnDelete(action = OnDeleteAction.NO_ACTION)
//    private EqualizerPreset preset;

    public DiscordServerSettings(byte volume, String prefix) {
        this.volume = volume;
        this.prefix = prefix;
    }

    public DiscordServerSettings() {}
}
