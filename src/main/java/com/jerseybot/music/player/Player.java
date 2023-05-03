package com.jerseybot.music.player;

import com.jerseybot.chat.MessageSendService;
import com.jerseybot.command.button.enums.PLAYER_BUTTON;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.Getter;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.jerseybot.utils.DateTimeUtils.toTimeDuration;

@Component
public class Player {
    private final DefaultAudioPlayerManager audioSourceManager;
    private final AudioPlayer audioPlayer;
    private final MessageSendService messageSendService;

    @Getter
    private final AudioPlayerSendHandler sendHandler;

    private final TrackScheduler scheduler = new TrackScheduler();

    private TextChannel lastUsedTextChannel;

    @Autowired
    public Player(DefaultAudioPlayerManager manager, MessageSendService messageSendService) {
        this.audioSourceManager = manager;
        this.audioPlayer = manager.createPlayer();
        this.messageSendService = messageSendService;
        this.audioPlayer.addListener(new AudioEventAdapter());
        this.sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
    }

    public void scheduleTrack(String source, TextChannel rspChannel) {
        this.lastUsedTextChannel = rspChannel;
        this.audioSourceManager.loadItemOrdered(this, source, new AudioLoadResultHandler(source));
    }

    public void skip() {
        if (scheduler.isEmpty()) {
            stop();
        } else {
            this.audioPlayer.playTrack(scheduler.nextTrack());
        }
    }

    public void stop() {
        this.scheduler.clear();
        this.audioPlayer.stopTrack();
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

    private class AudioLoadResultHandler implements com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler {
        private final String source;

        private AudioLoadResultHandler(String source) {
            this.source = source;
        }

        @Override
        public void trackLoaded(AudioTrack track) {
            if (scheduler.isEmpty() && !isPlaying()) {
                resume();
                audioPlayer.playTrack(track);
            } else {
                scheduler.addTrack(track);
            }
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            if (playlist.isSearchResult()) {
                trackLoaded(playlist.getTracks().get(0));
            } else {
                for (AudioTrack track : playlist.getTracks()) {
                    scheduler.addTrack(track);
                }
            }
        }

        @Override
        public void noMatches() {
            messageSendService.sendErrorMessage(lastUsedTextChannel, "Could not find \"" + this.source + "\"");
        }

        @Override
        public void loadFailed(FriendlyException exception) {
            throw exception;
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
            if (endReason.equals(AudioTrackEndReason.LOAD_FAILED) && playTries++ < maxPlayTries) {
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
            AudioTrackInfo info = track.getInfo();
            String title = info.author + " - " + info.title + " (" + toTimeDuration(info.length) + ")";
            messageSendService.sendNowPlaying(lastUsedTextChannel, title, player.isPaused());
        }

        @Override
        public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
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
