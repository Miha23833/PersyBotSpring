package com.jerseybot.adapters;

import com.jerseybot.JDAStorage;
import com.jerseybot.config.BotConfig;
import com.jerseybot.music.PlayerRepository;
import com.jerseybot.music.player.Player;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.StatusChangeEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

    private final Map<Long, Long> lastActivityByVoiceChannelId;

    @Autowired
    public VoiceChannelInactivityChecker(BotConfig config, PlayerRepository playerRepository, JDAStorage jdaStorage) {
        this.lastActivityByVoiceChannelId = new ConcurrentHashMap<>();

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
                lastActivityByVoiceChannelId.remove(event.getChannelLeft().getIdLong());
                event.getChannelLeft().getGuild().getAudioManager().closeAudioConnection();
                if (playerRepository.hasInitializedPlayer(event.getGuild().getIdLong())) {
                    Player player = playerRepository.get(event.getGuild().getIdLong());
                    player.stop();
                }
            } else if (event.getChannelJoined() != null) {
                lastActivityByVoiceChannelId.put(event.getChannelJoined().getIdLong(), System.currentTimeMillis());
            }
        }
    }

    @Override
    public void onStatusChange(@NotNull StatusChangeEvent event) {
        if (event.getNewStatus().equals(JDA.Status.CONNECTED)) {
            this.executorService.scheduleWithFixedDelay(() -> lastActivityByVoiceChannelId.forEach(this::updateVoiceChannelLastActivityAndLeaveIfInactive), 0, checkPauseMillis, TimeUnit.MILLISECONDS);
        }
    }

    private boolean isSelfMember(GuildVoiceUpdateEvent event) {
        return event.getGuild().getSelfMember().getIdLong() == event.getMember().getIdLong();
    }

    private void updateVoiceChannelLastActivityAndLeaveIfInactive(long voiceChannelId, long lastActivityTimeMillis) {
        long currentTime = System.currentTimeMillis();
        VoiceChannel voiceChannel = jdaStorage.getJda().getVoiceChannelById(voiceChannelId);
        if (voiceChannel == null) {
            return;
        }
        Member selfMember = Objects.requireNonNull(jdaStorage.getJda().getGuildById(voiceChannel.getGuild().getIdLong())).getSelfMember();
        if (!isBotConnectedToVoiceChannel(voiceChannel, selfMember)) {
            return;
        }
        if (!isAnyRealUserConnectedToVoiceChannel(voiceChannel)) {
            leaveVoiceChannel(voiceChannelId);
        }

        Player player = null;
        if (playerRepository.hasInitializedPlayer(voiceChannel.getGuild().getIdLong())) {
            player = playerRepository.get(voiceChannel.getGuild().getIdLong());
            if (player.isPlaying() && !player.isPaused()) {
                this.lastActivityByVoiceChannelId.put(voiceChannelId, currentTime);
            }
        }
        if (player == null) {
            leaveVoiceChannel(voiceChannelId);
        } else if (player.isPaused()) {
            if (currentTime - lastActivityTimeMillis > maxInactivityTimeIfPaused) {
                leaveVoiceChannel(voiceChannelId);
            }
        } else if (!player.isPlaying()) {
            if (currentTime - lastActivityTimeMillis > maxInactivityTimeIfNotPlaying) {
                leaveVoiceChannel(voiceChannelId);
            }
        }
    }

    private boolean isBotConnectedToVoiceChannel(VoiceChannel voiceChannel, Member selfMember) {
        if (selfMember.getVoiceState() != null && selfMember.getVoiceState().inAudioChannel() && selfMember.getVoiceState().getChannel() != null) {
            return selfMember.getVoiceState().getChannel().asVoiceChannel().getIdLong() == voiceChannel.getIdLong();
        }
        return false;
    }

    private boolean isAnyRealUserConnectedToVoiceChannel(VoiceChannel voiceChannel) {
        List<Member> connectedMembers = voiceChannel.getMembers();
            return connectedMembers.stream().anyMatch(member -> !member.getUser().isBot());
    }

    private void leaveVoiceChannel(long voiceChannelId) {
        VoiceChannel voiceChannel = jdaStorage.getJda().getVoiceChannelById(voiceChannelId);
        if (voiceChannel != null) {
            Optional.ofNullable(jdaStorage.getJda().getGuildById(voiceChannel.getGuild().getIdLong()))
                    .ifPresent(guild -> guild.getAudioManager().closeAudioConnection());
        }
    }
}
