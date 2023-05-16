package com.jerseybot.adapters;

import com.jerseybot.JDAStorage;
import com.jerseybot.config.BotConfig;
import com.jerseybot.music.PlayerRepository;
import com.jerseybot.music.player.Player;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.StatusChangeEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class VoiceChannelInactivityChecker extends ListenerAdapter {
    private final ScheduledExecutorService executorService;

    private final JDAStorage jdaStorage;
    private final PlayerRepository playerRepository;

    private final long maxInactivityTimeIfNotPlaying;
    private final long maxInactivityTimeIfPaused;
    private final long checkPauseMillis;

    private final Map<Long, Long> lastActivityByServerId;

    @Autowired
    public VoiceChannelInactivityChecker(BotConfig config, PlayerRepository playerRepository, JDAStorage jdaStorage) {
        this.lastActivityByServerId = new ConcurrentHashMap<>();

        this.checkPauseMillis = config.getActivityCheckPauseMillis();
        this.maxInactivityTimeIfNotPlaying = config.getMaxInactivityTimeMillisIfNotPlaying();
        this.maxInactivityTimeIfPaused = config.getMaxInactivityTimeIfPaused();

        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.playerRepository = playerRepository;
        this.jdaStorage = jdaStorage;
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if (isSelfMember(event)) {
            if (event.getChannelLeft() != null) {
                lastActivityByServerId.remove(event.getChannelLeft().getIdLong());
            } else if (event.getChannelJoined() != null) {
                lastActivityByServerId.put(event.getChannelJoined().getIdLong(), System.currentTimeMillis());
            }
        }
    }

    @Override
    public void onStatusChange(@NotNull StatusChangeEvent event) {
        if (event.getNewStatus().equals(JDA.Status.CONNECTED)) {
            this.executorService.scheduleWithFixedDelay(() -> lastActivityByServerId.forEach(this::leaveVoiceChannelIfInactive), 0, checkPauseMillis, TimeUnit.MILLISECONDS);
        }
    }

    private boolean isSelfMember(GuildVoiceUpdateEvent event) {
        return event.getGuild().getSelfMember().getIdLong() == event.getMember().getIdLong();
    }

    private void leaveVoiceChannelIfInactive(long voiceChannelId, long lastActivityTimeMillis) {
        VoiceChannel voiceChannel = jdaStorage.getJda().getVoiceChannelById(voiceChannelId);
        if (voiceChannel == null) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        Player player = playerRepository.get(voiceChannel.getGuild().getIdLong());

        AudioChannelUnion connectedChannel = voiceChannel.getGuild().getAudioManager().getConnectedChannel();
        if (connectedChannel == null ||
                !playerRepository.hasInitializedPlayer(voiceChannel.getGuild().getIdLong()) ||
                (!player.isPlaying() && currentTime - lastActivityTimeMillis > maxInactivityTimeIfNotPlaying) ||
                (player.isPaused() && currentTime - lastActivityTimeMillis > maxInactivityTimeIfPaused)) {
            player.stop();
            voiceChannel.getGuild().getAudioManager().closeAudioConnection();
        }  else if (player.isPlaying() && !player.isPaused()) {
            lastActivityByServerId.put(voiceChannelId, System.currentTimeMillis());
        }
    }
}
