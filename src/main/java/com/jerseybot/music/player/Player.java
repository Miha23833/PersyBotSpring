package com.jerseybot.music.player;

import com.jerseybot.chat.MessageSendService;
import com.jerseybot.chat.message.template.InfoMessage;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.jerseybot.utils.DateTimeUtils.toTimeDuration;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class Player {
    private final DefaultAudioPlayerManager audioSourceManager;
    private final AudioPlayer audioPlayer;
    private final MessageSendService messageSendService;

    @Getter
    private final AudioPlayerSendHandler sendHandler;

    private final TrackScheduler scheduler = new TrackScheduler();

    private final AtomicReference<GuildMessageChannel> lastUsedTextChannel = new AtomicReference<>();

    private boolean isOnRepeat = false;

    @Autowired
    public Player(DefaultAudioPlayerManager manager, MessageSendService messageSendService) {
        this.audioSourceManager = manager;
        this.audioPlayer = manager.createPlayer();
        this.messageSendService = messageSendService;
        this.audioPlayer.addListener(new AudioEventAdapter());
        this.sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
    }

    public void scheduleTrack(String source, GuildMessageChannel rspChannel) {
        this.lastUsedTextChannel.set(rspChannel);
        this.audioSourceManager.loadItemOrdered(this, source, new AudioLoadResultHandler(source));
    }

    public void skip() {
        if (scheduler.isEmpty()) {
            stop();
        } else {
            this.audioPlayer.playTrack(scheduler.nextTrack());
            this.isOnRepeat = false;
        }
    }

    public void stop() {
        this.scheduler.clear();
        this.audioPlayer.stopTrack();
        this.isOnRepeat = false;
    }

    public void pause() {
        this.audioPlayer.setPaused(true);
    }

    public void resume() {
        this.audioPlayer.setPaused(false);
    }

    public boolean isPlaying() {
        return this.audioPlayer.getPlayingTrack() != null;
    }

    public boolean isPaused() {
        return this.audioPlayer.isPaused();
    }

    public boolean isTracksQueueEmpty() {
        return this.scheduler.isEmpty();
    }

    public List<AudioTrack> getScheduledTracks() {
        return this.scheduler.getTracks();
    }

    public void mixQueue() {
        this.scheduler.shuffle();
        this.isOnRepeat = false;
    }

    public void setVolume(int volume) {
        this.audioPlayer.setVolume(volume);
    }

    public void repeat() {
        this.isOnRepeat = true;
        messageSendService.sendRepeatingTrack(lastUsedTextChannel.get(), this.audioPlayer.getPlayingTrack());
    }

    private class AudioLoadResultHandler implements com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler {
        private final String source;

        private AudioLoadResultHandler(String source) {
            this.source = source;
        }

        @Override
        public void trackLoaded(AudioTrack track) {
            if (scheduler.isEmpty() && !isPlaying() && !isOnRepeat) {
                resume();
                audioPlayer.playTrack(track);
            } else {
                scheduler.addTrack(track);
                messageSendService.sendQueuedTrack(lastUsedTextChannel.get(), track, isPaused());
            }
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            if (playlist.getTracks().isEmpty()) {
                messageSendService.sendInfoMessage(lastUsedTextChannel.get(), "Playlist is empty");
                return;
            }
            if (playlist.isSearchResult() || playlist.getTracks().size() == 1) {
                trackLoaded(playlist.getTracks().get(0));
            } else {
                for (AudioTrack track : playlist.getTracks()) {
                    scheduler.addTrack(track);
                }
                messageSendService.sendQueuedTracks(lastUsedTextChannel.get(), playlist.getTracks());
                if (!isPlaying()) {
                    resume();
                    audioPlayer.playTrack(scheduler.nextTrack());
                }
            }
        }

        @Override
        public void noMatches() {
            messageSendService.sendErrorMessage(lastUsedTextChannel.get(), "Could not find \"" + this.source + "\"");
        }

        @Override
        public void loadFailed(FriendlyException exception) {
            log.error("Could not load track\n" + exception.getMessage(), exception);
        }
    }

    private class AudioEventAdapter extends com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter {
        private int playTries = 0;
        private final int maxPlayTries = 3;

        @Override
        public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
            if (!endReason.mayStartNext) {
                return;
            }
            if (isOnRepeat) {
                player.playTrack(track.makeClone());
            } else if (endReason.equals(AudioTrackEndReason.LOAD_FAILED) && playTries++ < maxPlayTries) {
                player.playTrack(track.makeClone());
            } else if (scheduler.isEmpty()) {
                player.stopTrack();
            } else {
                player.playTrack(scheduler.nextTrack());
            }
        }

        @Override
        public void onTrackStart(AudioPlayer player, AudioTrack track) {
            this.playTries = 0;
            if (!isOnRepeat) {
                messageSendService.sendNowPlaying(lastUsedTextChannel.get(), track, player.isPaused(), scheduler.isEmpty());
            }
        }

        @Override
        public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
            log.error("Track exception\n" + exception.getMessage(), exception);
            super.onTrackException(player, track, exception);
        }

        @Override
        public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
            super.onTrackStuck(player, track, thresholdMs);
        }

        @Override
        public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs, StackTraceElement[] stackTrace) {
            super.onTrackStuck(player, track, thresholdMs, stackTrace);
        }
    }
}
