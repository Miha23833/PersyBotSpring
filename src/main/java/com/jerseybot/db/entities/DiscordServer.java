package com.jerseybot.db.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
public class DiscordServer {
    @Id
    private Long discordServerId;

    @Column(nullable = false)
    private Integer languageId;

    @OneToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "server_settings_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DiscordServerSettings settings;

//    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    @JoinColumn(name = "play_list_id")
//    @OnDelete(action = OnDeleteAction.CASCADE)
//    @MapKey(name = "name")
//    private Map<String, PlayList> playLists = new HashMap<>();

    public DiscordServer(long discordServerId, int languageId, DiscordServerSettings settings) {
        this.discordServerId = discordServerId;
        this.languageId = languageId;
        this.settings = settings;
    }

    public DiscordServer() {}
}
